package grpcbridge.swagger.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LowercaseEnumTypeAdapterFactory implements TypeAdapterFactory {
    private final Set<Class<? extends Enum>> classes;

    public LowercaseEnumTypeAdapterFactory(Set<Class<? extends Enum>> classes) {
        this.classes = classes;
    }

    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();
        if (!rawType.isEnum() || !classes.contains(rawType)) {
            return null;
        }

        final Map<String, T> lowercaseToConstant = new HashMap<>();
        for (T constant : rawType.getEnumConstants()) {
            lowercaseToConstant.put(toLowercase(constant), constant);
        }

        return new TypeAdapter<T>() {
            public void write(JsonWriter out, T value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    out.value(toLowercase(value));
                }
            }

            public T read(JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                } else {
                    return lowercaseToConstant.get(reader.nextString());
                }
            }
        };
    }

    private String toLowercase(Object object) {
        return object.toString().toLowerCase();
    }
}
