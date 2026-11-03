package hcms.model;

public interface Trackable {

    String getStatus();

    boolean updateStatus(String newStatus, String remark, Person changedBy);
}
