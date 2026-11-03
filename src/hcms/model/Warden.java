package hcms.model;

public class Warden extends Staff {

    private Hostel hostel;
    private String password;

    public Warden(String staffId, String name, String email, String mobile, Hostel hostel, String password) {
        super(staffId, name, email, mobile, "Warden");
        this.hostel = hostel;
        this.password = password;
    }

    public String getRole() {
        return "Warden";
    }

    public String showDetails() {
        return super.showDetails() + "\nHostel: " + hostel.getName() + " (" + hostel.getCode() + ")";
    }

    public boolean checkPassword(String text) {
        return password.equals(text);
    }

    boolean canManage(String hostelCode) {
        return hostel.getCode().equals(hostelCode);
    }

    public Hostel getHostel() { return hostel; }
    public String getHostelCode() { return hostel.getCode(); }
}
