package academy.kafka.serializers.examples;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonPerson {
    @JsonProperty(required = true)
    private String bsn;
    private String firstName;
    @JsonProperty(required = true)
    private String lastName;
    private String bancAccount;

    public JsonPerson() {
    }

    public JsonPerson(String bsn, String firstName, String lastName, String bancAccount) {
        this.bsn = bsn;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bancAccount = bancAccount;
    }

    public String getBsn() {
        return bsn;
    }

    public void setBsn(String bsn) {
        this.bsn = bsn;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBancAccount() {
        return bancAccount;
    }

    public void setBancAccount(String bancAccount) {
        this.bancAccount = bancAccount;
    }

    @Override
    public String toString() {
        return "Person [bancAccount=" + bancAccount + ", bsn=" + bsn + ", firstName=" + firstName + ", lastName="
                + lastName + "]";
    }
}
