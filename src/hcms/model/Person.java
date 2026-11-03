package hcms.model;

public abstract class Person {

    protected String name;
    protected String email;
    protected String mobile;

    public Person(String name, String email, String mobile) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    public abstract String getRole();

    public String showDetails() {
        return "Name: " + name + "\nEmail: " + email + "\nMobile: " + mobile;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
}
