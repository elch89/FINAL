package com.earwormfix.earwormfix;

public class UserProfile {
    private String fullName;
    private String email;
    private String phone;
    private String bDate;
    private String genre;
    private String gender;
    private String avatar;
    public UserProfile(String fullName, String email, String bDate, String genre,
                       String gender, String avatar, String phone){
        this.phone = phone;
        this.email = email;
        this.bDate = bDate;
        this.avatar = avatar;
        this.genre = genre;
        this.gender = gender;
        this.fullName = fullName;

    }

    public String getAvatar() {
        return avatar;
    }

    public String getbDate() {
        return bDate;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGenre() {
        return genre;
    }

    public String getPhone() {
        return phone;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setbDate(String bDate) {
        this.bDate = bDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
