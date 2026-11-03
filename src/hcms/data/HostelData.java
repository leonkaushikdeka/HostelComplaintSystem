package hcms.data;

import hcms.exceptions.DuplicateComplaintException;
import hcms.model.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class HostelData {

    private Hostel[] hostels = new Hostel[3];
    private Warden[] wardens = new Warden[3];
    private HashMap<String, Student> students = new HashMap<String, Student>();
    private ArrayList<Complaint> complaints = new ArrayList<Complaint>();
    private FileHandler fileHandler = new FileHandler();

    public HostelData() {
        loadHostelsAndWardens();
        loadStudents();
        loadComplaints();
    }

    private void loadHostelsAndWardens() {
        hostels[0] = new Hostel("BHA", "Boys Hostel A");
        hostels[1] = new Hostel("BHB", "Boys Hostel B");
        hostels[2] = new Hostel("GH", "Girls Hostel");
        wardens[0] = new Warden("BHA", "Dr. Anil Mathur", "warden.bha@asu.ac.in", "NA", hostels[0], "warden@123");
        wardens[1] = new Warden("BHB", "Dr. Meera Iyer", "warden.bhb@asu.ac.in", "NA", hostels[1], "warden@123");
        wardens[2] = new Warden("GH", "Dr. Sunita Rao", "warden.gh@asu.ac.in", "NA", hostels[2],
                "warden@123");
    }

    private void addStudent(String rollNo, String name, String mobile, String hostelCode, String roomNo) {
        Student student = new Student(rollNo, name, mobile, hostelCode, roomNo);
        students.put(rollNo, student);
        getHostel(hostelCode).addOccupant(roomNo);
    }

    private void loadStudents() {
        addStudent("IT25B9001", "Rohan Sharma", "9000000001", "BHA", "018");
        addStudent("IT25B9002", "Aman Verma", "9000000002", "BHA", "014");
        addStudent("IT25B9003", "Rohit Mehta", "9000000003", "BHA", "105");
        addStudent("IT25B9004", "Vikram Singh", "9000000004", "BHA", "105");
        addStudent("IT25B9005", "Arjun Nair", "9000000005", "BHA", "112");
        addStudent("IT25B9006", "Rahul Gupta", "9000000006", "BHA", "112");
        addStudent("IT25B9007", "Nikhil Rao", "9000000007", "BHA", "203");
        addStudent("IT25B9008", "Sameer Khan", "9000000008", "BHA", "203");
        addStudent("IT25B9009", "Aditya Joshi", "9000000009", "BHB", "007");
        addStudent("IT25B9010", "Manish Tiwari", "9000000010", "BHB", "007");
        addStudent("IT25B9011", "Deepak Yadav", "9000000011", "BHB", "110");
        addStudent("IT25B9012", "Suresh Pillai", "9000000012", "BHB", "110");
        addStudent("IT25B9013", "Varun Iyer", "9000000013", "BHB", "204");
        addStudent("IT25B9014", "Imran Qureshi", "9000000014", "BHB", "215");
        addStudent("IT25B9015", "Priya Sen", "9000000015", "GH", "003");
        addStudent("IT25B9016", "Neha Bose", "9000000016", "GH", "003");
        addStudent("IT25B9017", "Ananya Roy", "9000000017", "GH", "108");
        addStudent("IT25B9018", "Kavya Menon", "9000000018", "GH", "108");
        addStudent("IT25B9019", "Sneha Patil", "9000000019", "GH", "201");
        addStudent("IT25B9020", "Ritika Jain", "9000000020", "GH", "209");
    }

    private void loadComplaints() {
        boolean fileFound = fileHandler.loadComplaints(complaints);
        if (!fileFound) {
            loadSeedComplaints();
            fileHandler.saveComplaints(complaints);
        }
        int highestId = 1000;
        for (int i = 0; i < complaints.size(); i++) {
            if (complaints.get(i).getId() > highestId) {
                highestId = complaints.get(i).getId();
            }
        }
        Complaint.setCounter(highestId + 1);
    }

    private void loadSeedComplaints() {
        LocalDate today = LocalDate.now();
        complaints.add(new Complaint(1001, "IT25B9001", "BHA", "018", "Plumbing", "High",
                "Washroom tap near room 018 leaks all night and the floor stays wet", false, true,
                today.minusDays(6), "Pending", "-", null));
        complaints.add(new Complaint(1002, "IT25B9002", "BHA", "014", "Wifi", "Medium",
                "Wifi disconnects every evening after 8 pm and study material does not download", false, false,
                today.minusDays(4), "In Progress", "Router reset requested", null));
        complaints.add(new Complaint(1003, "IT25B9005", "BHA", "112", "Safety", "High",
                "Emergency exit on the ground floor is blocked with old beds and mattresses", true, false,
                today.minusDays(5), "Pending", "-", null));
        complaints.add(new Complaint(1004, "IT25B9003", "BHA", "105", "Electrical", "Medium",
                "Fan regulator is broken and the fan runs only at full speed", false, true,
                today.minusDays(2), "Pending", "-", null));
        complaints.add(new Complaint(1005, "IT25B9007", "BHA", "203", "Drinking Water", "High",
                "Water filter on the second floor is not working and the tap water is muddy", false, false,
                today.minusDays(8), "Resolved", "Filter candle replaced", today.minusDays(3)));
        complaints.add(new Complaint(1006, "IT25B9009", "BHB", "007", "Carpentry", "Low",
                "Cupboard door hinge is broken and the door does not close", false, true,
                today.minusDays(3), "Pending", "-", null));
        complaints.add(new Complaint(1007, "IT25B9012", "BHB", "110", "Mess", "Medium",
                "Dinner is served after 9 pm on most days", false, false,
                today.minusDays(1), "Pending", "-", null));
        complaints.add(new Complaint(1008, "IT25B9015", "GH", "003", "Housekeeping", "Medium",
                "Corridor on the ground floor has not been cleaned for three days", false, false,
                today.minusDays(5), "Pending", "-", null));
        complaints.add(new Complaint(1009, "IT25B9017", "GH", "108", "Electrical", "High",
                "Generator was not started during the power cut and there was no light in study hours", false, false,
                today.minusDays(2), "In Progress", "Electrician informed", null));
        complaints.add(new Complaint(1010, "IT25B9019", "GH", "201", "Study Hours", "Low",
                "Loud music from nearby rooms during study hours", true, false,
                today.minusDays(1), "Pending", "-", null));
    }

    public synchronized Complaint search(int id) {
        for (int i = 0; i < complaints.size(); i++) {
            if (complaints.get(i).getId() == id) {
                return complaints.get(i);
            }
        }
        return null;
    }

    public synchronized ArrayList<Complaint> search(String rollNo) {
        ArrayList<Complaint> result = new ArrayList<Complaint>();
        for (Complaint c : complaints) {
            if (c.getRollNo().equalsIgnoreCase(rollNo)) {
                result.add(c);
            }
        }
        sortComplaints(result);
        return result;
    }

    public synchronized ArrayList<Complaint> search(String category, String status) {
        ArrayList<Complaint> result = new ArrayList<Complaint>();
        for (Complaint c : complaints) {
            boolean categoryMatches = category.equals("All") || c.getCategory().equals(category);
            boolean statusMatches = status.equals("All") || c.getStatus().equals(status);
            if (categoryMatches && statusMatches) {
                result.add(c);
            }
        }
        sortComplaints(result);
        return result;
    }

    public synchronized ArrayList<Complaint> getHostelComplaints(String hostelCode) {
        ArrayList<Complaint> result = new ArrayList<Complaint>();
        for (Complaint c : complaints) {
            if (c.getHostelCode().equals(hostelCode)) {
                result.add(c);
            }
        }
        sortComplaints(result);
        return result;
    }

    public synchronized ArrayList<Complaint> getOpenComplaints(String hostelCode, String roomNo) {
        ArrayList<Complaint> result = new ArrayList<Complaint>();
        for (Complaint c : complaints) {
            if (c.getHostelCode().equals(hostelCode) && c.getRoomNo().equals(roomNo) && c.isOpen()) {
                result.add(c);
            }
        }
        return result;
    }

    public void sortComplaints(ArrayList<Complaint> list) {
        Collections.sort(list, new Comparator<Complaint>() {
            public int compare(Complaint a, Complaint b) {
                if (a.getPriorityLevel() != b.getPriorityLevel()) {
                    return b.getPriorityLevel() - a.getPriorityLevel();
                }
                return a.getFiledDate().compareTo(b.getFiledDate());
            }
        });
    }

    public synchronized void checkDuplicate(String rollNo, String category) throws DuplicateComplaintException {
        for (Complaint c : complaints) {
            if (c.getRollNo().equals(rollNo) && c.getCategory().equals(category) && c.isOpen()) {
                throw new DuplicateComplaintException("You already have an open " + category + " complaint (ID "
                        + c.getId() + ", " + c.getStatus() + ").");
            }
        }
    }

    public synchronized void addComplaint(Complaint complaint) {
        complaints.add(complaint);
        save();
    }

    public synchronized ArrayList<Complaint> escalateOldComplaints() {
        ArrayList<Complaint> escalated = new ArrayList<Complaint>();
        for (Complaint c : complaints) {
            if (c.escalate()) {
                escalated.add(c);
            }
        }
        return escalated;
    }

    public synchronized void save() {
        fileHandler.saveComplaints(complaints);
    }

    public String exportReport(String hostelCode, String reportText) {
        return fileHandler.writeReport(hostelCode, reportText);
    }

    public Student getStudent(String rollNo) {
        return students.get(rollNo);
    }

    public Warden getWarden(String wardenId) {
        for (int i = 0; i < wardens.length; i++) {
            if (wardens[i].getStaffId().equalsIgnoreCase(wardenId)) {
                return wardens[i];
            }
        }
        return null;
    }

    public Hostel getHostel(String hostelCode) {
        for (int i = 0; i < hostels.length; i++) {
            if (hostels[i].getCode().equals(hostelCode)) {
                return hostels[i];
            }
        }
        return null;
    }

    public ArrayList<Student> getStudentsOfHostel(String hostelCode) {
        ArrayList<Student> result = new ArrayList<Student>();
        for (Student s : students.values()) {
            if (s.getHostelCode().equals(hostelCode)) {
                result.add(s);
            }
        }
        Collections.sort(result, new Comparator<Student>() {
            public int compare(Student a, Student b) {
                if (!a.getRoomNo().equals(b.getRoomNo())) {
                    return a.getRoomNo().compareTo(b.getRoomNo());
                }
                return a.getRollNo().compareTo(b.getRollNo());
            }
        });
        return result;
    }

    public ArrayList<Student> getStudentsInRoom(String hostelCode, String roomNo) {
        ArrayList<Student> result = new ArrayList<Student>();
        for (Student s : getStudentsOfHostel(hostelCode)) {
            if (s.getRoomNo().equals(roomNo)) {
                result.add(s);
            }
        }
        return result;
    }

    public int getStudentCount() {
        return students.size();
    }

    public synchronized int getComplaintCount() {
        return complaints.size();
    }
}
