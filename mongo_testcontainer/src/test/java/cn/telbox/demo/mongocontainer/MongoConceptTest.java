package cn.telbox.demo.mongocontainer;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Sets;
import org.assertj.core.util.Lists;
import org.bson.BsonUndefined;
import org.bson.Document;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@SpringJUnitConfig(MongoTestContextConfig.class)
@Testcontainers
@Slf4j
public class MongoConceptTest {

    private static final String MONGODB4_VERSION = "mongo:4.4.9";
    private static final String MONGODB5_VERSION = "mongo:5.0";
    @Container
    public static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse(MONGODB5_VERSION));

    @Autowired
    private MongoOperations mongoTemplate;

    @Autowired
    private MongoOperations enhancedMongoTemplate;

    private String[] expectedBookNames = new String[]{"book_1", "book_6", "book_8"};
    private String nameOfBookWithUndefinedValue = "SpecialBook";
    private List<Object> pagesNumb = Lists.newArrayList(10L, new BsonUndefined(), 99L);
    private Document bookWithUndefinedValue;
    private Set<String> expectedNameResults = Sets.newHashSet(expectedBookNames);
    private String bookCollectionName = "book";
    private int bookNumb = 10;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        assertTrue(MONGO_DB_CONTAINER.isRunning());

        String replicaSetUrl = MONGO_DB_CONTAINER.getReplicaSetUrl();
        log.info("testcontainers MongoDB replicaSetUrl: {}", replicaSetUrl);

        registry.add("replicaSetUrl", () -> replicaSetUrl);
    }
    
    @AfterAll
    public static void stopMongo() {
        MONGO_DB_CONTAINER.stop();
    }

    @BeforeEach
    public void setupData() {
        generateSomeBookData(bookNumb)
                .forEach(c -> mongoTemplate.insert(c, bookCollectionName));

        bookWithUndefinedValue = generateBookDataWithUndefinedValue();
        Stream.of(bookWithUndefinedValue)
                .forEach(bookWithUndefinedValue -> mongoTemplate.getCollection(bookCollectionName).insertOne(bookWithUndefinedValue));
    }

    /**
     * This Document of book's authorName is undefined. And one of the value in pagesWithPicture is undefined.
     * @return
     */
    @NotNull
    private Document generateBookDataWithUndefinedValue() {
        bookWithUndefinedValue = new Document();
        bookWithUndefinedValue.put("_id", 999L);
        bookWithUndefinedValue.put("name", nameOfBookWithUndefinedValue);
        bookWithUndefinedValue.put("authorName", new BsonUndefined());
        bookWithUndefinedValue.put("starLevel", 5);
        bookWithUndefinedValue.put("pagesWithPicture", pagesNumb);
        return bookWithUndefinedValue;
    }

    @AfterEach
    public void cleanData() {
        removeAllBooks();
    }

    private List<Book> generateSomeBookData(int numb) {
        long maxStarLevel = 20L;
        return LongStream.range(0, numb)
                .mapToObj(i -> {
                    String bookName = "book_" + i;
                    String authorName = "author_" + i;
                    int starLevel = (int) (i % maxStarLevel);
                    List<Long> pagesWithPic = Lists.newArrayList(5L, 7L, 11L);
                    return new Book(i, bookName, authorName, starLevel, pagesWithPic);
                }).collect(Collectors.toList());
    }

    private void removeAllBooks() {
        mongoTemplate.remove(new Query(), Book.class);
    }

    @Test
    public void testQueryBookCursor_ByUsingMongoCollectionFindMethod() {
        Query query = queryToGetExpectedBooks();
        MongoCollection<Document> mongoCollectionOfBook = mongoTemplate.getCollection(bookCollectionName);
        try (MongoCursor<Document> cursor = mongoCollectionOfBook.find(query.getQueryObject()).projection(query.getFieldsObject()).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                expectedNameResults.remove(doc.get("name"));
            }
        }
        assertEquals(0, expectedNameResults.size());
    }

    @Test
    public void testQueryBook_ByUsingMongoCollectionFindMethodWithClassName() {
        Query query = queryToGetExpectedBooks();
        // This is because the object of mongoTemplate.getCollection() returns is a MongoDriver object and MongoDriver layer has no idea how to map Document to POJO.
        // On the other hand the mongoTemplate.find() still is in the Spring-DataMongo stack, spring-data-mongo will take care of the mapping.
        CodecConfigurationException codexException = assertThrows(CodecConfigurationException.class, () -> {
                    MongoCursor<Book> books = mongoTemplate.getCollection(bookCollectionName)
                            .find(query.getQueryObject(), Book.class)
                            .iterator();
                });
        assertTrue(codexException.getMessage().equals("Can't find a codec for class cn.telbox.demo.mongocontainer.Book."));
    }

    @Test
    public void testQueryBook_ByUsingMongoTemplate() {
        Query query = queryToGetExpectedBooks();
        // "@Document: Applied at the class level to indicate this class is a candidate for mapping to the database.
        // You can specify the name of the collection where the data will be stored."
        // When you read the data out from MongoDB, spring-data-mongo will convert the BSON to POJO with the help of type mapping "_class" value.
        // job. https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mapping.fundamentals
        // So, the following query workable without the @Document on the Book class.
        List<Book> books = mongoTemplate.find(query, Book.class);
        books.forEach(b -> expectedNameResults.remove(b.getName()));
        assertEquals(0, expectedNameResults.size());
    }

    @Test
    public void testReadDataIncludesUndefinedValue() {
        Query query = new Query(new Criteria("name").is(nameOfBookWithUndefinedValue));
        Book book = enhancedMongoTemplate.findOne(query, Book.class);
        assertEquals(nameOfBookWithUndefinedValue, book.getName());
        assertEquals(bookWithUndefinedValue.get("name"), book.getName());
        assertEquals(bookWithUndefinedValue.get("starLevel"), book.getStarLevel());

        List<Long> pagesWithPicture = book.getPagesWithPicture();
        assertEquals(pagesNumb.get(0), pagesWithPicture.get(0));
        assertEquals(null, pagesWithPicture.get(1));
        assertEquals(pagesNumb.get(2), pagesWithPicture.get(2));
    }

    @NotNull
    private Query queryToGetExpectedBooks() {
        Query query = new Query(new Criteria("name").in(expectedBookNames));
        query.with(Sort.by(Sort.Direction.DESC, "starLevel"));
        return query;
    }
}
