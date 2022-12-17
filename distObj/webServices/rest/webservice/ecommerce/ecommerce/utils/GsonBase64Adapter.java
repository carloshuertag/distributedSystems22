package ecommerce.utils;

import java.lang.reflect.Type;
import java.util.Base64;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;

public class GsonBase64Adapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
    }

    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        String s = json.getAsString().replaceAll("\\ ", "+"); // puts back the + signs that were removed when parsing
        return Base64.getDecoder().decode(s);
    }
}
