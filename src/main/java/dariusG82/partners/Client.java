package dariusG82.partners;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "clients")
public class Client {
    @Id
    private String clientID;
    private String clientName;
    private String clientStreetAddress;
    private String clientCityAddress;
    private String clientCountryAddress;

    public Client(){

    }

    public Client(String clientName, String clientID, String clientStreetAddress, String clientCityAddress, String clientCountryAddress) {
        this.clientName = clientName;
        this.clientID = clientID;
        this.clientStreetAddress = clientStreetAddress;
        this.clientCityAddress = clientCityAddress;
        this.clientCountryAddress = clientCountryAddress;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientStreetAddress() {
        return clientStreetAddress;
    }

    public void setClientStreetAddress(String clientStreetAddress) {
        this.clientStreetAddress = clientStreetAddress;
    }

    public String getClientCityAddress() {
        return clientCityAddress;
    }

    public void setClientCityAddress(String clientCityAddress) {
        this.clientCityAddress = clientCityAddress;
    }

    public String getClientCountryAddress() {
        return clientCountryAddress;
    }

    public void setClientCountryAddress(String clientCountryAddress) {
        this.clientCountryAddress = clientCountryAddress;
    }
}
