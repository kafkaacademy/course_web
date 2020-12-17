package academy.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonSchemaToPojo {

    public static void main(String args[]) throws JsonProcessingException {
        academy.kafka.generated.Person person = new academy.kafka.generated.Person().withBsn("BSN1234")
                .withFirstName("Pietje").withLastName("Puk").withBancAccount("IBAN1234");
        System.out.println("generated person used:" + person);
    }

}
