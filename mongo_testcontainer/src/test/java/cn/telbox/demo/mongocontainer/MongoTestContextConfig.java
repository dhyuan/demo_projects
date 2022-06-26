package cn.telbox.demo.mongocontainer;

import com.mongodb.ConnectionString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoTestContextConfig extends AbstractMongoClientConfiguration {

    @Bean
    public ConnectionString mongoClientURI(@Value("${replicaSetUrl}") String uri) {
        return new ConnectionString(uri);
    }


    @Bean
    public MongoDatabaseFactory mongoDbFactory(ConnectionString mongoClientURI) throws UnknownHostException {
        return new SimpleMongoClientDatabaseFactory(mongoClientURI);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory) {
        return new MongoTemplate(mongoDbFactory);
    }

    @Autowired
    @Bean("enhancedMongoTemplate")
    public MongoTemplate enhancedMongoTemplate(MongoDatabaseFactory mongoDbFactory, MappingMongoConverter mappingMongoConverter) {
        return new MongoTemplate(mongoDbFactory, mappingMongoConverter);
    }

    @Bean
    @Primary
    public MongoCustomConversions mongoCustomConversions() {
        List<Object> converters = new ArrayList<>();
        converters.add(new BsonUndefinedToNullValueConverterFactory());
        return new MongoCustomConversions(converters);
    }

    @Override
    protected String getDatabaseName() {
        return "testDB";
    }
}
