package com.hit.dm;

import java.io.Serializable;
import java.util.Objects;

public class FlightTicket implements Serializable {
    private String id;
    private String customerName;
    private String source;
    private String destination;
    private int price;

    public FlightTicket(String id, String customerName, String source, String destination, int price) {
        this.id = id;
        this.customerName = customerName;
        this.source = source;
        this.destination = destination;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightTicket that = (FlightTicket) o;
        return price == that.price &&
                Objects.equals(id, that.id) &&
                Objects.equals(customerName, that.customerName) &&
                Objects.equals(source, that.source) &&
                Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerName, source, destination, price);
    }

    @Override
    public String toString() {
        return "FlightTicket{" +
                "id='" + id + '\'' +
                ", customerName='" + customerName + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", price=" + price +
                '}';
    }
}
