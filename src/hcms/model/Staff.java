package hcms.model;

public class Staff extends Person {

    private String staffId;
    private String designation;

    public Staff(String staffId, String name, String email, String mobile, String designation) {
        super(name, email, mobile);
        this.staffId = staffId;
        this.designation = designation;
    }

    public String getRole() {
        return "Staff";
    }

    public String showDetails() {
        return super.showDetails() + "\nStaff ID: " + staffId + "\nDesignation: " + designation;
    }

    public String getStaffId() { return staffId; }
    public String getDesignation() { return designation; }
}
