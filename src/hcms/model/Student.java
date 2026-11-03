package hcms.model;

import hcms.exceptions.InvalidRollNumberException;

public class Student extends Person {

    private String rollNo;
    private String hostelCode;
    private String roomNo;
    private String password;

    public Student(String rollNo, String name, String mobile, String hostelCode, String roomNo) {
        super(name, rollNo.toLowerCase() + "@asu.ac.in", mobile);
        this.rollNo = rollNo;
        this.hostelCode = hostelCode;
        this.roomNo = roomNo;
        this.password = "asu@" + rollNo.substring(5);
    }

    public static void checkRollNumber(String roll) throws InvalidRollNumberException {
        if (roll.length() != 9) {
            throw new InvalidRollNumberException("Roll No. must have 9 characters, for example IT25B9001.");
        }
        if (!roll.startsWith("IT25B")) {
            throw new InvalidRollNumberException("Roll No. must start with IT25B.");
        }
        String lastPart = roll.substring(5);
        for (int i = 0; i < lastPart.length(); i++) {
            char ch = lastPart.charAt(i);
            if (!Character.isDigit(ch)) {
                throw new InvalidRollNumberException("Roll No. must end with 4 digits.");
            }
        }
    }

    public String getRole() {
        return "Student";
    }

    public String showDetails() {
        return super.showDetails() + "\nRoll No.: " + rollNo + "\nHostel: " + hostelCode + "\nRoom No.: " + roomNo;
    }

    public boolean checkPassword(String text) {
        return password.equals(text);
    }

    public String getMaskedMobile() {
        StringBuffer masked = new StringBuffer(mobile);
        if (masked.length() == 10) {
            masked.replace(5, 8, "XXX");
        }
        return masked.toString();
    }

    public String getRollNo() { return rollNo; }
    public String getHostelCode() { return hostelCode; }
    public String getRoomNo() { return roomNo; }

    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public void setHostelCode(String hostelCode) { this.hostelCode = hostelCode; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public void setPassword(String password) { this.password = password; }

    public String toString() {
        return rollNo + " " + name + " (" + hostelCode + ", Room " + roomNo + ")";
    }
}
