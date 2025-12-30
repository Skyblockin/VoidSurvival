package com.skyblockin.voidsurvival.social;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashSet;

@JsonSerialize(using = FriendList.Serializer.class)
@JsonDeserialize(using = FriendList.Deserializer.class)
public class FriendList extends HashSet<Friend> {

    public boolean contains(Player player) {
        return contains(new Friend(player));
    }

    public boolean add(Player player) {
        return add(new Friend(player));
    }

    public PaginatedList<Friend> asPaginatedList(int pageSize) {
        return new PaginatedList<>(this, pageSize);
    }

    public static class Serializer extends StdSerializer<FriendList> {

        protected Serializer() {
            super(FriendList.class);
        }

        @Override
        public void serialize(FriendList value, JsonGenerator gen, SerializerProvider provider) throws IOException {

            gen.writeStartArray();

            for (Friend friend : value) {
                gen.writeStartObject();
                gen.writeStringField("uuid", friend.uuid);
                gen.writeStringField("name", friend.name);
                gen.writeEndObject();
            }

            gen.writeEndArray();
        }
    }

    public static class Deserializer extends StdDeserializer<FriendList> {

        protected Deserializer() {
            super(FriendList.class);
        }

        @Override
        public FriendList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            JsonNode node = p.getCodec().readTree(p);

            FriendList list = new FriendList();

            node.elements().forEachRemaining(element -> {

                String uuid = element.get("uuid").asText();
                String name = element.get("name").asText();

                list.add(new Friend(uuid, name));
            });

            return list;
        }
    }

}
