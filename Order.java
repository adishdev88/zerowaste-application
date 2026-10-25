package com.zerowaste;

/**
 * Order.java — A simple class to hold details of one order.
 *
 * Objects of this class are used to display order history in a table.
 */
public class Order {

    // Fields
    private int id;
    private int userId;
    private String hotelName;
    private String foodName;
    private int quantity;
    private double totalPrice;
    private String status;

    // Constructor
    public Order(int id, int userId, String hotelName, String foodName,
                 int quantity, double totalPrice, String status) {
        this.id = id;
        this.userId = userId;
        this.hotelName = hotelName;
        this.foodName = foodName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Getter methods

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public String getFoodName() {
        return foodName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }
}
