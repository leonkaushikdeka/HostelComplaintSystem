package hcms.gui;

import hcms.data.HostelData;
import hcms.exceptions.InvalidRollNumberException;
import hcms.model.*;
import hcms.threads.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.*;

public class WardenDashboard extends JFrame implements ActionListener {

    private HostelData data;
    private Warden warden;
    private ClockThread clock;
    private String viewType = "Status";
    private String viewValue = "All";

    private JMenuItem miSave, miLogout, miExit;
    private JMenuItem miAll, miPending, miInProgress, miEscalated, miResolved, miSearchId, miSearchRoll;
    private JMenuItem miRoomMap, miStudentList, miStatistics, miExport, miAbout;
    private JTable tblComplaints;
    private DefaultTableModel model;
    private JButton btnInProgress;
    private JButton btnResolved;
    private JLabel lblView;
    private JLabel lblClock;

    public WardenDashboard(HostelData data, Warden warden) {
        super("Warden Dashboard - " + warden.getHostel().getName());
        this.data = data;
        this.warden = warden;

        JMenuBar mbWarden = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        miSave = addItem(mnuFile, "Save");
        miLogout = addItem(mnuFile, "Logout");
        mnuFile.addSeparator();
        miExit = addItem(mnuFile, "Exit");
        JMenu mnuComplaints = new JMenu("Complaints");
        miAll = addItem(mnuComplaints, "All");
        miPending = addItem(mnuComplaints, "Pending");
        miInProgress = addItem(mnuComplaints, "In Progress");
        miEscalated = addItem(mnuComplaints, "Escalated");
        miResolved = addItem(mnuComplaints, "Resolved");
        mnuComplaints.addSeparator();
        miSearchId = addItem(mnuComplaints, "Search by ID");
        miSearchRoll = addItem(mnuComplaints, "Search by Roll No.");
        JMenu mnuHostel = new JMenu("Hostel");
        miRoomMap = addItem(mnuHostel, "Room Map");
        miStudentList = addItem(mnuHostel, "Student List");
        JMenu mnuReports = new JMenu("Reports");
        miStatistics = addItem(mnuReports, "Statistics");
        miExport = addItem(mnuReports, "Export Report");
        JMenu mnuHelp = new JMenu("Help");
        miAbout = addItem(mnuHelp, "About");
        mbWarden.add(mnuFile);
        mbWarden.add(mnuComplaints);
        mbWarden.add(mnuHostel);
        mbWarden.add(mnuReports);
        mbWarden.add(mnuHelp);
        setJMenuBar(mbWarden);

        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea taProfile = new JTextArea(warden.showDetails());
        taProfile.setEditable(false);
        JPanel pnlProfile = new JPanel(new BorderLayout());
        pnlProfile.setBorder(BorderFactory.createTitledBorder("Warden"));
        pnlProfile.add(taProfile, BorderLayout.CENTER);
        pnlMain.add(pnlProfile, BorderLayout.NORTH);

        String[] columns = {"ID", "Filed On", "Roll No.", "Name", "Room No.", "Category", "Priority", "Status",
                "Days", "Description", "Remark"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblComplaints = new JTable(model);
        int[] widths = {45, 80, 85, 140, 65, 100, 60, 80, 40, 300, 170};
        for (int i = 0; i < widths.length; i++) {
            tblComplaints.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        pnlMain.add(new JScrollPane(tblComplaints), BorderLayout.CENTER);

        JPanel pnlSouth = new JPanel(new BorderLayout());
        JPanel pnlButtons = new JPanel(new FlowLayout());
        btnInProgress = new JButton("Mark In Progress");
        btnResolved = new JButton("Mark Resolved");
        btnInProgress.addActionListener(this);
        btnResolved.addActionListener(this);
        pnlButtons.add(btnInProgress);
        pnlButtons.add(btnResolved);
        JPanel pnlStatus = new JPanel(new BorderLayout());
        lblView = new JLabel();
        lblClock = new JLabel();
        pnlStatus.add(lblView, BorderLayout.WEST);
        pnlStatus.add(lblClock, BorderLayout.EAST);
        pnlSouth.add(pnlButtons, BorderLayout.CENTER);
        pnlSouth.add(pnlStatus, BorderLayout.SOUTH);
        pnlMain.add(pnlSouth, BorderLayout.SOUTH);
        setContentPane(pnlMain);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        refreshTable();
        EscalationThread.setOpenDashboard(this);
        clock = new ClockThread(lblClock);
        clock.start();
        setSize(1180, 640);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JMenuItem addItem(JMenu menu, String text) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(this);
        menu.add(item);
        return item;
    }

    public void refreshTable() {
        String hostelCode = warden.getHostelCode();
        ArrayList<Complaint> list = new ArrayList<Complaint>();
        String heading;
        if (viewType.equals("ID")) {
            Complaint c = data.search(Integer.parseInt(viewValue));
            if (c != null && c.getHostelCode().equals(hostelCode)) {
                list.add(c);
            }
            heading = "Search result for ID " + viewValue;
        } else if (viewType.equals("Roll")) {
            for (Complaint c : data.search(viewValue)) {
                if (!c.isAnonymous()) {
                    list.add(c);
                }
            }
            heading = "Complaints of " + viewValue + " (anonymous complaints are not shown)";
        } else {
            for (Complaint c : data.search("All", viewValue)) {
                if (c.getHostelCode().equals(hostelCode)) {
                    list.add(c);
                }
            }
            heading = "Showing: " + viewValue;
        }
        model.setRowCount(0);
        for (Complaint c : list) {
            model.addRow(new Object[] {c.getId(), c.getFiledDateText(), shownRoll(c), shownName(c), c.getRoomNo(),
                    c.getCategory(), c.getPriority(), c.getStatus(), c.getDaysOpen(), c.getDescription(),
                    c.getRemark()});
        }
        lblView.setText(heading + "   |   Complaints: " + list.size());
    }

    private String shownRoll(Complaint c) {
        return c.isAnonymous() ? "Anonymous" : c.getRollNo();
    }

    private String shownName(Complaint c) {
        if (c.isAnonymous()) {
            return "Anonymous";
        }
        Student s = data.getStudent(c.getRollNo());
        return s == null ? "Unknown" : s.getName();
    }

    private void showStatus(String status) {
        viewType = "Status";
        viewValue = status;
        refreshTable();
    }

    private void changeStatus(String newStatus) {
        int row = tblComplaints.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a complaint in the table first.");
            return;
        }
        int id = (Integer) model.getValueAt(row, 0);
        Complaint c = data.search(id);
        if (c.getStatus().equals("Resolved")) {
            JOptionPane.showMessageDialog(this, "Complaint " + id + " is already resolved.");
            return;
        }
        if (c.getStatus().equals(newStatus)) {
            JOptionPane.showMessageDialog(this, "Complaint " + id + " is already " + newStatus + ".");
            return;
        }
        String remark = JOptionPane.showInputDialog(this, "Remark for complaint " + id + ":", "Mark " + newStatus,
                JOptionPane.QUESTION_MESSAGE);
        if (remark == null) {
            return;
        }
        remark = remark.trim().replace('|', '/');
        if (remark.length() == 0) {
            JOptionPane.showMessageDialog(this, "Remark cannot be empty.");
            return;
        }
        if (c.updateStatus(newStatus, remark, warden)) {
            data.save();
            refreshTable();
            System.out.println("[Warden] " + warden.getStaffId() + " marked complaint " + id + " as " + newStatus);
            JOptionPane.showMessageDialog(this, "Complaint " + id + " marked " + newStatus + ".");
        } else {
            JOptionPane.showMessageDialog(this, "You can only update complaints of your own hostel.");
        }
    }

    private void searchById() {
        String text = JOptionPane.showInputDialog(this, "Enter Complaint ID:", "Search by ID",
                JOptionPane.QUESTION_MESSAGE);
        if (text == null) {
            return;
        }
        try {
            int id = Integer.parseInt(text.trim());
            Complaint c = data.search(id);
            if (c == null || !c.getHostelCode().equals(warden.getHostelCode())) {
                JOptionPane.showMessageDialog(this, "No complaint with ID " + id + " in your hostel.");
                return;
            }
            viewType = "ID";
            viewValue = "" + id;
            refreshTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Complaint ID must be a number, for example 1003.", "Invalid ID",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchByRoll() {
        String text = JOptionPane.showInputDialog(this, "Enter Roll No.:", "Search by Roll No.",
                JOptionPane.QUESTION_MESSAGE);
        if (text == null) {
            return;
        }
        String roll = text.trim().toUpperCase();
        try {
            Student.checkRollNumber(roll);
            Student s = data.getStudent(roll);
            if (s == null || !s.getHostelCode().equals(warden.getHostelCode())) {
                JOptionPane.showMessageDialog(this, "No student with Roll No. " + roll + " in your hostel.");
                return;
            }
            viewType = "Roll";
            viewValue = roll;
            refreshTable();
        } catch (InvalidRollNumberException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Roll No.", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStudentList() {
        ArrayList<Student> list = data.getStudentsOfHostel(warden.getHostelCode());
        StringBuffer text = new StringBuffer();
        text.append(String.format("%-9s %-11s %-24s %s\n", "Room No.", "Roll No.", "Name", "Mobile"));
        text.append("--------------------------------------------------------\n");
        for (Student s : list) {
            text.append(String.format("%-9s %-11s %-24s %s\n", s.getRoomNo(), s.getRollNo(), s.getName(),
                    s.getMaskedMobile()));
        }
        text.append("\nTotal students: ").append(list.size());
        JTextArea taList = new JTextArea(text.toString(), 14, 58);
        taList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taList.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(taList), "Student List - "
                + warden.getHostel().getName(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportReport() {
        String hostelCode = warden.getHostelCode();
        ArrayList<Complaint> list = data.getHostelComplaints(hostelCode);
        StringBuffer report = new StringBuffer();
        report.append("HOSTEL COMPLAINT REPORT\n");
        report.append("Assam Skill University, Mangaldoi\n");
        report.append("Hostel : ").append(warden.getHostel().getName()).append(" (").append(hostelCode).append(")\n");
        report.append("Warden : ").append(warden.getName()).append("\n");
        report.append("Date   : ").append(LocalDate.now().format(Complaint.DATE_FORMAT)).append("\n");
        report.append("==============================================================\n\n");
        int open = 0;
        for (Complaint c : list) {
            report.append(c.getId()).append("  ").append(c.getFiledDateText()).append("  ").append(shownRoll(c));
            report.append("  Room ").append(c.getRoomNo()).append("  ").append(c.getCategory());
            report.append("  ").append(c.getPriority()).append("  ").append(c.getStatus()).append("\n");
            report.append("      Description: ").append(c.getDescription()).append("\n");
            report.append("      Remark     : ").append(c.getRemark()).append("\n\n");
            if (c.isOpen()) {
                open++;
            }
        }
        report.append("==============================================================\n");
        report.append("Total complaints : ").append(list.size()).append("\n");
        report.append("Open complaints  : ").append(open).append("\n");
        report.append("Resolved         : ").append(list.size() - open).append("\n");
        String fileName = data.exportReport(hostelCode, report.toString());
        if (fileName != null) {
            JOptionPane.showMessageDialog(this, "Report saved: " + fileName);
        } else {
            JOptionPane.showMessageDialog(this, "Could not save the report.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnInProgress) {
            changeStatus("In Progress");
        } else if (source == btnResolved) {
            changeStatus("Resolved");
        } else if (source == miAll) {
            showStatus("All");
        } else if (source == miPending) {
            showStatus("Pending");
        } else if (source == miInProgress) {
            showStatus("In Progress");
        } else if (source == miEscalated) {
            showStatus("Escalated");
        } else if (source == miResolved) {
            showStatus("Resolved");
        } else if (source == miSearchId) {
            searchById();
        } else if (source == miSearchRoll) {
            searchByRoll();
        } else if (source == miRoomMap) {
            new RoomMapFrame(data, warden.getHostel());
        } else if (source == miStudentList) {
            showStudentList();
        } else if (source == miStatistics) {
            new StatisticsFrame(data, warden.getHostel());
        } else if (source == miExport) {
            exportReport();
        } else if (source == miAbout) {
            new AboutDialog(this);
        } else if (source == miSave) {
            data.save();
            JOptionPane.showMessageDialog(this, "Complaints saved.");
        } else if (source == miLogout) {
            EscalationThread.setOpenDashboard(null);
            clock.stopClock();
            System.out.println("[Login] Warden " + warden.getStaffId() + " logged out");
            dispose();
            new LoginFrame(data);
        } else if (source == miExit) {
            exitApplication();
        }
    }

    private void exitApplication() {
        data.save();
        System.out.println("[Main] Complaints saved. Exiting.");
        System.exit(0);
    }
}
