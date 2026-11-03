package hcms.gui;

import hcms.data.HostelData;
import hcms.model.*;
import hcms.threads.ClockThread;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.*;

public class StudentDashboard extends JFrame implements ActionListener {

    private HostelData data;
    private Student student;
    private ClockThread clock;

    private JMenuItem miLogout, miExit, miNewComplaint, miMyComplaints, miAbout;
    private JTextArea taProfile;
    private JTable tblComplaints;
    private DefaultTableModel model;
    private JLabel lblCount, lblClock;

    public StudentDashboard(HostelData data, Student student) {
        super("Student Dashboard - " + student.getName());
        this.data = data;
        this.student = student;

        JMenuBar mbStudent = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        miLogout = addItem(mnuFile, "Logout");
        mnuFile.addSeparator();
        miExit = addItem(mnuFile, "Exit");
        JMenu mnuComplaint = new JMenu("Complaint");
        miNewComplaint = addItem(mnuComplaint, "New Complaint");
        miMyComplaints = addItem(mnuComplaint, "My Complaints");
        JMenu mnuHelp = new JMenu("Help");
        miAbout = addItem(mnuHelp, "About");
        mbStudent.add(mnuFile);
        mbStudent.add(mnuComplaint);
        mbStudent.add(mnuHelp);
        setJMenuBar(mbStudent);

        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        taProfile = new JTextArea(student.showDetails());
        taProfile.setEditable(false);
        JPanel pnlProfile = new JPanel(new BorderLayout());
        pnlProfile.setBorder(BorderFactory.createTitledBorder("Profile"));
        pnlProfile.add(taProfile, BorderLayout.CENTER);
        pnlMain.add(pnlProfile, BorderLayout.NORTH);

        String[] columns = {"ID", "Filed On", "Category", "Priority", "Room No.", "Status", "Days", "Remark"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblComplaints = new JTable(model);
        tblComplaints.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblComplaints.getColumnModel().getColumn(6).setPreferredWidth(40);
        tblComplaints.getColumnModel().getColumn(7).setPreferredWidth(220);
        JScrollPane spTable = new JScrollPane(tblComplaints);
        spTable.setBorder(BorderFactory.createTitledBorder("My Complaints"));
        pnlMain.add(spTable, BorderLayout.CENTER);

        JPanel pnlStatus = new JPanel(new BorderLayout());
        lblCount = new JLabel();
        lblClock = new JLabel();
        pnlStatus.add(lblCount, BorderLayout.WEST);
        pnlStatus.add(lblClock, BorderLayout.EAST);
        pnlMain.add(pnlStatus, BorderLayout.SOUTH);
        setContentPane(pnlMain);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        refreshTable();
        clock = new ClockThread(lblClock);
        clock.start();
        setSize(900, 560);
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
        model.setRowCount(0);
        ArrayList<Complaint> list = data.search(student.getRollNo());
        int open = 0;
        for (Complaint c : list) {
            model.addRow(new Object[] {c.getId(), c.getFiledDateText(), c.getCategory(), c.getPriority(),
                    c.getRoomNo(), c.getStatus(), c.getDaysOpen(), c.getRemark()});
            if (c.isOpen()) {
                open++;
            }
        }
        lblCount.setText("My complaints: " + list.size() + "     Open: " + open);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == miNewComplaint) {
            new ComplaintForm(this, data, student);
            refreshTable();
        } else if (source == miMyComplaints) {
            refreshTable();
        } else if (source == miAbout) {
            new AboutDialog(this);
        } else if (source == miLogout) {
            clock.stopClock();
            System.out.println("[Login] " + student.getRollNo() + " logged out");
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
