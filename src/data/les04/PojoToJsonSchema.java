package academy.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

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

        JsonNode schema = generator.generateJsonSchema(Person.class);
        System.out.println("schema:" + schema.toPrettyString());

        String json2 = "{\"bsn\":\"BSN1235\",\"lastName\":\"xxx\"}";

        Person result2 = JACKSON_MAPPER.readValue(json2, Person.class);

        System.out.println("deserialize result2 with null values:" + result2);

        String json3 = "{\"bsn\":\"BSN1235\",\"FirstName\":\"xxx\"}";

        try {
            JACKSON_MAPPER.readValue(json3, Person.class);
        } catch (com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException e) {
            System.out
                    .println("this exception is ok, required field (lastName) is missing, expected exception for input "
                            + json3);
        }

    }

}
