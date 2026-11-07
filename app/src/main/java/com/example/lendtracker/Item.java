package com.example.lendtracker;

/**
 * Model class: one thing that I lent to a friend.
 * Firebase needs an empty constructor + public getters/setters.
 */
public class Item {

    private String id;
    private String itemName;     // what I lent (e.g. "Calculator")
    private String friendName;   // who took it
    private String phone;        // friend's phone number
    private String lentDate;     // when I gave it
    private String imageBase64;  // photo of the item (stored as text)
    private long createdAt;      // used to sort newest first

    public Item() {
        // Required empty constructor for Firebase
    }

    public Item(String id, String itemName, String friendName, String phone,
                String lentDate, String imageBase64, long createdAt) {
        this.id = id;
        this.itemName = itemName;
        this.friendName = friendName;
        this.phone = phone;
        this.lentDate = lentDate;
        this.imageBase64 = imageBase64;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLentDate() { return lentDate; }
    public void setLentDate(String lentDate) { this.lentDate = lentDate; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
