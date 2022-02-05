
package academy.kafka.generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Person
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "bsn",
    "firstName",
    "lastName",
    "bancAccount"
})
public class Person {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bsn")
    private String bsn;
    @JsonProperty("firstName")
    private String firstName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("bancAccount")
    private String bancAccount;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bsn")
    public String getBsn() {
        return bsn;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("bsn")
    public void setBsn(String bsn) {
        this.bsn = bsn;
    }

    public Person withBsn(String bsn) {
        this.bsn = bsn;
        return this;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Person withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Person withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    @JsonProperty("bancAccount")
    public String getBancAccount() {
        return bancAccount;
    }

    @JsonProperty("bancAccount")
    public void setBancAccount(String bancAccount) {
        this.bancAccount = bancAccount;
    }

    public Person withBancAccount(String bancAccount) {
        this.bancAccount = bancAccount;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Person.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("bsn");
        sb.append('=');
        sb.append(((this.bsn == null)?"<null>":this.bsn));
        sb.append(',');
        sb.append("firstName");
        sb.append('=');
        sb.append(((this.firstName == null)?"<null>":this.firstName));
        sb.append(',');
        sb.append("lastName");
        sb.append('=');
        sb.append(((this.lastName == null)?"<null>":this.lastName));
        sb.append(',');
        sb.append("bancAccount");
        sb.append('=');
        sb.append(((this.bancAccount == null)?"<null>":this.bancAccount));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
