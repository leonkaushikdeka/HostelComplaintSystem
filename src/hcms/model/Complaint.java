package hcms.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Complaint implements Trackable, Reportable {

    public static final int ESCALATION_DAYS = 3;
    public static final String[] CATEGORIES = {"Plumbing", "Electrical", "Carpentry", "Housekeeping", "Mess",
            "Drinking Water", "Wifi", "Safety", "Study Hours", "Other"};
    public static final String[] PRIORITIES = {"Low", "Medium", "High"};
    public static final String[] STATUSES = {"Pending", "In Progress", "Escalated", "Resolved"};
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static int counter = 1001;
    private static int totalCreated = 0;

    private int id;
    private String rollNo;
    private String hostelCode;
    private String roomNo;
    private String category;
    private String priority;
    private String description;
    private boolean anonymous;
    private boolean allowEntry;
    private LocalDate filedDate;
    private String status;
    private String remark;
    private LocalDate resolvedDate;

    public Complaint(String rollNo, String hostelCode, String roomNo, String category, String description,
            boolean anonymous, boolean allowEntry) {
        this(rollNo, hostelCode, roomNo, category, "Medium", description, anonymous, allowEntry);
    }

    public Complaint(String rollNo, String hostelCode, String roomNo, String category, String priority,
            String description, boolean anonymous, boolean allowEntry) {
        this(nextId(), rollNo, hostelCode, roomNo, category, priority, description, anonymous, allowEntry,
                LocalDate.now(), "Pending", "-", null);
    }

    public Complaint(int id, String rollNo, String hostelCode, String roomNo, String category, String priority,
            String description, boolean anonymous, boolean allowEntry, LocalDate filedDate, String status,
            String remark, LocalDate resolvedDate) {
        this.id = id;
        this.rollNo = rollNo;
        this.hostelCode = hostelCode;
        this.roomNo = roomNo;
        this.category = category;
        this.priority = priority;
        this.description = description;
        this.anonymous = anonymous;
        this.allowEntry = allowEntry;
        this.filedDate = filedDate;
        this.status = status;
        this.remark = remark;
        this.resolvedDate = resolvedDate;
        totalCreated++;
    }

    private static int nextId() {
        return counter++;
    }

    public static void setCounter(int value) { counter = value; }
    public static int getNextId() { return counter; }
    public static int getTotalCreated() { return totalCreated; }

    public synchronized String getStatus() {
        return status;
    }

    public synchronized boolean updateStatus(String newStatus, String remark, Person changedBy) {
        if (!(changedBy instanceof Warden)) {
            return false;
        }
        Warden warden = (Warden) changedBy;
        if (!warden.canManage(hostelCode)) {
            return false;
        }
        this.status = newStatus;
        this.remark = remark;
        if (newStatus.equals("Resolved")) {
            resolvedDate = LocalDate.now();
        }
        return true;
    }

    public synchronized boolean escalate() {
        if (status.equals("Pending") && getDaysOpen() > ESCALATION_DAYS) {
            status = "Escalated";
            priority = "High";
            remark = "Auto escalated after " + ESCALATION_DAYS + " days";
            return true;
        }
        return false;
    }

    public boolean isOpen() {
        return !getStatus().equals("Resolved");
    }

    public long getDaysOpen() {
        LocalDate end = LocalDate.now();
        if (resolvedDate != null) {
            end = resolvedDate;
        }
        return ChronoUnit.DAYS.between(filedDate, end);
    }

    public long getDaysToResolve() {
        if (resolvedDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(filedDate, resolvedDate);
    }

    public int getPriorityLevel() {
        for (int i = 0; i < PRIORITIES.length; i++) {
            if (PRIORITIES[i].equals(priority)) {
                return i;
            }
        }
        return 0;
    }

    public String generateSlip() {
        StringBuffer slip = new StringBuffer();
        String line = "--------------------------------------------\n";
        slip.append("ASSAM SKILL UNIVERSITY, MANGALDOI\n");
        slip.append("HOSTEL COMPLAINT SLIP\n");
        slip.append(line);
        slip.append("Complaint ID : ").append(id).append("\n");
        slip.append("Filed on     : ").append(getFiledDateText()).append("\n");
        slip.append("Roll No.     : ").append(rollNo).append("\n");
        slip.append("Hostel       : ").append(hostelCode).append("\n");
        slip.append("Room No.     : ").append(roomNo).append("\n");
        slip.append("Category     : ").append(category).append("\n");
        slip.append("Priority     : ").append(priority).append("\n");
        slip.append("Anonymous    : ").append(anonymous ? "Yes (name hidden from Warden)" : "No").append("\n");
        slip.append("Allow entry  : ").append(allowEntry ? "Yes" : "No").append("\n");
        slip.append("Status       : ").append(status).append("\n");
        slip.append(line);
        slip.append("Description:\n").append(description).append("\n");
        slip.append(line);
        slip.append("Pending complaints are escalated after ").append(ESCALATION_DAYS).append(" days.\n");
        return slip.toString();
    }

    public final int getId() { return id; }
    public String getRollNo() { return rollNo; }
    public String getHostelCode() { return hostelCode; }
    public String getRoomNo() { return roomNo; }
    public String getCategory() { return category; }
    public synchronized String getPriority() { return priority; }
    public String getDescription() { return description; }
    public boolean isAnonymous() { return anonymous; }
    public boolean isAllowEntry() { return allowEntry; }
    public LocalDate getFiledDate() { return filedDate; }
    public synchronized String getRemark() { return remark; }
    public LocalDate getResolvedDate() { return resolvedDate; }

    public String getFiledDateText() {
        return filedDate.format(DATE_FORMAT);
    }

    public String toString() {
        return "Complaint " + id + " [" + category + ", " + getStatus() + "]";
    }
}
