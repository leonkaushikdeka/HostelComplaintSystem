package hcms.gui;

import hcms.data.HostelData;
import hcms.exceptions.InvalidRollNumberException;
import hcms.model.*;
import hcms.threads.LockoutThread;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends Frame implements ActionListener {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_SECONDS = 30;

    private HostelData data;
    private int wrongAttempts = 0;

    private Label lblTitle, lblUniversity, lblMessage;
    private TextField txtUser, txtPassword;
    private CheckboxGroup cbgLoginType;
    private Checkbox cbStudent, cbWarden;
    private Button btnLogin, btnClear;

    public LoginFrame(HostelData data) {
        super("HCMS - Login");
        this.data = data;
        setLayout(new BorderLayout(10, 10));

        Panel pnlTop = new Panel(new GridLayout(2, 1));
        lblTitle = new Label("Hostel Complaint Management System", Label.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblUniversity = new Label("Assam Skill University, Mangaldoi", Label.CENTER);
        pnlTop.add(lblTitle);
        pnlTop.add(lblUniversity);
        add(pnlTop, BorderLayout.NORTH);

        Panel pnlForm = new Panel(new GridLayout(3, 2, 10, 10));
        cbgLoginType = new CheckboxGroup();
        cbStudent = new Checkbox("Student", cbgLoginType, true);
        cbWarden = new Checkbox("Warden", cbgLoginType, false);
        Panel pnlType = new Panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlType.add(cbStudent);
        pnlType.add(cbWarden);
        txtUser = new TextField(18);
        txtPassword = new TextField(18);
        txtPassword.setEchoChar('*');
        pnlForm.add(new Label("Login as:"));
        pnlForm.add(pnlType);
        pnlForm.add(new Label("Roll No. / Warden ID:"));
        pnlForm.add(txtUser);
        pnlForm.add(new Label("Password:"));
        pnlForm.add(txtPassword);
        Panel pnlCenter = new Panel(new FlowLayout());
        pnlCenter.add(pnlForm);
        add(pnlCenter, BorderLayout.CENTER);

        Panel pnlBottom = new Panel(new GridLayout(2, 1));
        Panel pnlButtons = new Panel(new FlowLayout());
        btnLogin = new Button("Login");
        btnClear = new Button("Clear");
        btnLogin.addActionListener(this);
        btnClear.addActionListener(this);
        pnlButtons.add(btnLogin);
        pnlButtons.add(btnClear);
        lblMessage = new Label("Enter your Roll No. or Warden ID and password.", Label.CENTER);
        pnlBottom.add(pnlButtons);
        pnlBottom.add(lblMessage);
        add(pnlBottom, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeApplication();
            }
        });

        setSize(540, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin) {
            login();
        } else if (e.getSource() == btnClear) {
            txtUser.setText("");
            txtPassword.setText("");
            cbgLoginType.setSelectedCheckbox(cbStudent);
            lblMessage.setText("Enter your Roll No. or Warden ID and password.");
        }
    }

    private void login() {
        String user = txtUser.getText().trim().toUpperCase();
        String password = txtPassword.getText().trim();
        if (user.length() == 0 || password.length() == 0) {
            lblMessage.setText("Roll No. / Warden ID and password are required.");
            return;
        }
        String error = "Wrong Roll No. / Warden ID or password.";
        if (cbgLoginType.getSelectedCheckbox() == cbStudent) {
            try {
                Student.checkRollNumber(user);
                Student student = data.getStudent(user);
                if (student != null && student.checkPassword(password)) {
                    printLogin(student);
                    dispose();
                    new StudentDashboard(data, student);
                    return;
                }
            } catch (InvalidRollNumberException ex) {
                error = ex.getMessage();
            }
        } else {
            Warden warden = data.getWarden(user);
            if (warden != null && warden.checkPassword(password)) {
                printLogin(warden);
                dispose();
                new WardenDashboard(data, warden);
                return;
            }
        }
        wrongLogin(error);
    }

    private void wrongLogin(String error) {
        wrongAttempts++;
        txtPassword.setText("");
        System.out.println("[Login] Failed attempt " + wrongAttempts + " for " + txtUser.getText().trim());
        if (wrongAttempts >= MAX_ATTEMPTS) {
            wrongAttempts = 0;
            btnLogin.setEnabled(false);
            LockoutThread lockout = new LockoutThread(btnLogin, lblMessage, LOCK_SECONDS);
            lockout.start();
        } else {
            lblMessage.setText(error + " Attempts left: " + (MAX_ATTEMPTS - wrongAttempts));
        }
    }

    private void printLogin(Person person) {
        System.out.println("[Login] " + person.getRole() + " logged in");
        System.out.println(person.showDetails());
    }

    private void closeApplication() {
        data.save();
        System.out.println("[Main] Complaints saved. Exiting.");
        System.exit(0);
    }
}
