package academy.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.json.JSONObject;
import org.json.JSONTokener;
import academy.kafka.entities.Person;

public class PojoToJsonSchema {

    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();
    private static final JsonSchemaGenerator generator = new JsonSchemaGenerator(JACKSON_MAPPER);

    public static void main(String args[]) throws JsonProcessingException {
        Person person = new Person("BSN1234", "Pietje", "Puk", "IBAN1234");
        System.out.println("original:" + person);
        String json = JACKSON_MAPPER.writeValueAsString(person);
        System.out.println("serialize result:" + json);
        Person result = JACKSON_MAPPER.readValue(json, Person.class);
        System.out.println("deserialize result:" + result);

        JsonNode jsonSchema = generator.generateJsonSchema(Person.class);
        JSONObject schemaObject = new JSONObject(JACKSON_MAPPER.writeValueAsString(jsonSchema));
        Schema schema = SchemaLoader.load(schemaObject, new DefaultSchemaClient());
        System.out.println("schema:" + schema);

        String json2 = "{\"bsn\":\"BSN1235\",\"lastName\":\"xxx\"}";

        schema.validate(new JSONObject(json2));

        Person result2 = JACKSON_MAPPER.readValue(json2, Person.class);

        System.out.println("deserialize result2 with null values:" + result2);

        String json3 = "{\"bsn\":\"BSN1235\",\"firstName\":\"xxx\"}";
        try {
            schema.validate(new JSONObject(json3));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

}
