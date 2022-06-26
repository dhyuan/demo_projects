package cn.telbox.demo.mongocontainer;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
//@Document("book")
public class Book {

    private Long id;

    private String name;

    private String authorName;

    private Integer starLevel;

    private List<Long> pagesWithPicture;

}
