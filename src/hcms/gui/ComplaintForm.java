package hcms.gui;

import hcms.data.HostelData;
import hcms.exceptions.*;
import hcms.model.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ComplaintForm extends JDialog implements ActionListener {

    private static final int MIN_WORDS = 5;

    private HostelData data;
    private Student student;
    private Hostel hostel;

    private JTextField txtRoll, txtHostel, txtRoom;
    private JComboBox<String> cmbCategory;
    private JRadioButton rbLow, rbMedium, rbHigh;
    private ButtonGroup bgPriority;
    private JCheckBox chkAnonymous, chkAllowEntry;
    private JTextArea taDescription;
    private JButton btnSubmit, btnReset;

    public ComplaintForm(JFrame owner, HostelData data, Student student) {
        super(owner, "New Complaint", true);
        this.data = data;
        this.student = student;
        this.hostel = data.getHostel(student.getHostelCode());

        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel pnlFields = new JPanel(new GridLayout(7, 2, 8, 8));
        txtRoll = new JTextField(student.getRollNo());
        txtRoll.setEditable(false);
        txtHostel = new JTextField(hostel.getName() + " (" + hostel.getCode() + ")");
        txtHostel.setEditable(false);
        txtRoom = new JTextField(student.getRoomNo());
        cmbCategory = new JComboBox<String>(Complaint.CATEGORIES);

        rbLow = new JRadioButton("Low");
        rbMedium = new JRadioButton("Medium");
        rbHigh = new JRadioButton("High");
        bgPriority = new ButtonGroup();
        bgPriority.add(rbLow);
        bgPriority.add(rbMedium);
        bgPriority.add(rbHigh);
        JPanel pnlPriority = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlPriority.add(rbLow);
        pnlPriority.add(rbMedium);
        pnlPriority.add(rbHigh);

        chkAnonymous = new JCheckBox("Submit anonymously");
        chkAllowEntry = new JCheckBox("Allow entry to room when I am absent");

        pnlFields.add(new JLabel("Roll No.:"));
        pnlFields.add(txtRoll);
        pnlFields.add(new JLabel("Hostel:"));
        pnlFields.add(txtHostel);
        pnlFields.add(new JLabel("Room No.:"));
        pnlFields.add(txtRoom);
        pnlFields.add(new JLabel("Category:"));
        pnlFields.add(cmbCategory);
        pnlFields.add(new JLabel("Priority (default Medium):"));
        pnlFields.add(pnlPriority);
        pnlFields.add(new JLabel("Options:"));
        pnlFields.add(chkAnonymous);
        pnlFields.add(new JLabel(""));
        pnlFields.add(chkAllowEntry);
        pnlMain.add(pnlFields, BorderLayout.NORTH);

        taDescription = new JTextArea(5, 30);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        JPanel pnlDescription = new JPanel(new BorderLayout(5, 5));
        pnlDescription.add(new JLabel("Description (at least " + MIN_WORDS + " words):"), BorderLayout.NORTH);
        pnlDescription.add(new JScrollPane(taDescription), BorderLayout.CENTER);
        pnlMain.add(pnlDescription, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout());
        btnSubmit = new JButton("Submit");
        btnReset = new JButton("Reset");
        btnSubmit.addActionListener(this);
        btnReset.addActionListener(this);
        pnlButtons.add(btnSubmit);
        pnlButtons.add(btnReset);
        pnlMain.add(pnlButtons, BorderLayout.SOUTH);

        setContentPane(pnlMain);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSubmit) {
            submitComplaint();
        } else if (e.getSource() == btnReset) {
            resetForm();
        }
    }

    private void submitComplaint() {
        String category = (String) cmbCategory.getSelectedItem();
        String description = taDescription.getText().trim().replace('\n', ' ').replace('|', '/');
        try {
            String roomNo = validateForm(txtRoom.getText().trim(), description);
            data.checkDuplicate(student.getRollNo(), category);
            String priority = getSelectedPriority();
            Complaint complaint;
            if (priority == null) {
                complaint = new Complaint(student.getRollNo(), hostel.getCode(), roomNo, category, description,
                        chkAnonymous.isSelected(), chkAllowEntry.isSelected());
            } else {
                complaint = new Complaint(student.getRollNo(), hostel.getCode(), roomNo, category, priority,
                        description, chkAnonymous.isSelected(), chkAllowEntry.isSelected());
            }
            data.addComplaint(complaint);
            System.out.println("[ComplaintForm] Complaint " + complaint.getId() + " registered by "
                    + student.getRollNo() + ". Complaint objects created: " + Complaint.getTotalCreated());
            showSlip(complaint);
            dispose();
        } catch (EmptyFieldException ex) {
            showError(ex.getMessage(), "Empty Field");
        } catch (InvalidRoomException ex) {
            showError(ex.getMessage(), "Invalid Room");
        } catch (NumberFormatException ex) {
            showError("Room No. must be a number, for example 018.", "Invalid Room");
        } catch (DuplicateComplaintException ex) {
            showError(ex.getMessage(), "Duplicate Complaint");
        }
    }

    private String validateForm(String roomNo, String description) throws EmptyFieldException, InvalidRoomException {
        if (roomNo.length() == 0) {
            throw new EmptyFieldException("Room No. cannot be empty.");
        }
        String checkedRoom = hostel.checkRoom(roomNo);
        if (description.length() == 0) {
            throw new EmptyFieldException("Description cannot be empty.");
        }
        int words = countWords(description);
        if (words < MIN_WORDS) {
            throw new EmptyFieldException("Description must have at least " + MIN_WORDS + " words. You wrote "
                    + words + ".");
        }
        return checkedRoom;
    }

    private int countWords(String text) {
        String trimmed = text.trim();
        if (trimmed.length() == 0) {
            return 0;
        }
        String[] words = trimmed.split("\\s+");
        return words.length;
    }

    private String getSelectedPriority() {
        if (rbLow.isSelected()) {
            return "Low";
        } else if (rbMedium.isSelected()) {
            return "Medium";
        } else if (rbHigh.isSelected()) {
            return "High";
        }
        return null;
    }

    private void showSlip(Complaint complaint) {
        JTextArea taSlip = new JTextArea(complaint.generateSlip(), 21, 46);
        taSlip.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taSlip.setLineWrap(true);
        taSlip.setWrapStyleWord(true);
        taSlip.setEditable(false);
        Object[] message = {"Complaint registered. ID: " + complaint.getId(), new JScrollPane(taSlip)};
        JOptionPane.showMessageDialog(this, message, "Complaint Slip", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void resetForm() {
        txtRoom.setText(student.getRoomNo());
        cmbCategory.setSelectedIndex(0);
        bgPriority.clearSelection();
        chkAnonymous.setSelected(false);
        chkAllowEntry.setSelected(false);
        taDescription.setText("");
    }
}
