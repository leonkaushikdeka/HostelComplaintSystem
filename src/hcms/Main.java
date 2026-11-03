package hcms;

import hcms.data.HostelData;
import hcms.gui.LoginFrame;
import hcms.model.Complaint;
import hcms.threads.EscalationThread;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static void main(String[] args) {
        HostelData data = new HostelData();
        printBanner(data);
        EscalationThread escalation = new EscalationThread(data);
        escalation.start();
        new LoginFrame(data);
    }

    private static void printBanner(HostelData data) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String line = "==============================================================";
        System.out.println(line);
        System.out.println("            HOSTEL COMPLAINT MANAGEMENT SYSTEM");
        System.out.println("       Department of IT, Assam Skill University, Mangaldoi");
        System.out.println(line);
        System.out.println("Java version       : " + System.getProperty("java.version"));
        System.out.println("Java vendor        : " + System.getProperty("java.vendor"));
        System.out.println("OS name            : " + System.getProperty("os.name"));
        System.out.println("OS architecture    : " + System.getProperty("os.arch"));
        System.out.println("User name          : " + System.getProperty("user.name"));
        System.out.println("Date and time      : " + LocalDateTime.now().format(format));
        System.out.println("Students loaded    : " + data.getStudentCount());
        System.out.println("Complaints loaded  : " + data.getComplaintCount());
        System.out.println("Objects created    : " + Complaint.getTotalCreated());
        System.out.println("Next complaint ID  : " + Complaint.getNextId());
        System.out.println("Escalation after   : " + Complaint.ESCALATION_DAYS + " days");
        System.out.println(line);
    }
}
