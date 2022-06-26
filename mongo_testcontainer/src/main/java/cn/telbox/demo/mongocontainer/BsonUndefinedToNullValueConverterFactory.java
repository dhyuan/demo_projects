package cn.telbox.demo.mongocontainer;


import org.bson.BsonUndefined;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class BsonUndefinedToNullValueConverterFactory implements ConverterFactory<BsonUndefined, Object> {

    @Override
    public <T extends Object> Converter<BsonUndefined, T> getConverter(Class<T> targetType) {
        return o -> null;
    }

//    @Override
//    public <T extends Object> Converter<BsonUndefined, T> getConverter(Class<T> targetType) {
//        return new Converter<BsonUndefined, T>() {
//            @Override
//            public T convert(BsonUndefined source) {
//                return null;
//            }
//        };
//    }

}
