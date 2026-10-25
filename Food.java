package com.zerowaste;

/**
 * Food.java — A simple class to hold details of one food item.
 *
 * This is a plain Java class (sometimes called a "model" or "data class").
 * It has fields, a constructor, and getter methods.
 * Objects of this class are used to pass food data between screens.
 */
public class Food {

    // Fields — these store the details of one food item
    private int id;
    private int hotelId;
    private String name;
    private int quantity;
    private double price;
    private String description;

    // Constructor — called when creating a new Food object
    public Food(int id, int hotelId, String name, int quantity, double price, String description) {
        this.id = id;
        this.hotelId = hotelId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.description = description;
    }

    // Getter methods — used to read the field values from outside this class

    public int getId() {
        return id;
    }

    public int getHotelId() {
        return hotelId;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    // Setter for quantity — needed to update the available quantity after an order
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // toString — useful for displaying the food name in a list or combo box
    @Override
    public String toString() {
        return name;
    }
}
