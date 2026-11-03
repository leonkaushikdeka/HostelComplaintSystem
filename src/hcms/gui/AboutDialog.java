package hcms.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AboutDialog extends JDialog implements ActionListener {

    private JButton btnClose;

    public AboutDialog(JFrame owner) {
        super(owner, "About", true);

        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));

        JPanel pnlInfo = new JPanel(new GridLayout(0, 1, 4, 4));
        JLabel lblTitle = new JLabel("Hostel Complaint Management System", JLabel.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        pnlInfo.add(lblTitle);
        pnlInfo.add(new JLabel("Java Programming (PCC-01) Project", JLabel.CENTER));
        pnlInfo.add(new JLabel("Developed by Leon Kaushik Deka (IT25B0233)", JLabel.CENTER));
        pnlInfo.add(new JLabel("and Banasri Sarma (IT26L0103)", JLabel.CENTER));
        pnlInfo.add(new JLabel("B.Tech CSE (Honours) Cyber Security, 3rd Semester", JLabel.CENTER));
        pnlInfo.add(new JLabel("Department of IT, Assam Skill University, Mangaldoi", JLabel.CENTER));
        pnlInfo.add(new JLabel(" "));
        pnlInfo.add(new JLabel("Java version: " + System.getProperty("java.version")));
        pnlInfo.add(new JLabel("Java vendor: " + System.getProperty("java.vendor")));
        pnlInfo.add(new JLabel("OS name: " + System.getProperty("os.name")));
        pnlInfo.add(new JLabel("OS architecture: " + System.getProperty("os.arch")));
        pnlInfo.add(new JLabel("User name: " + System.getProperty("user.name")));
        pnlMain.add(pnlInfo, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout());
        btnClose = new JButton("Close");
        btnClose.addActionListener(this);
        pnlButtons.add(btnClose);
        pnlMain.add(pnlButtons, BorderLayout.SOUTH);

        setContentPane(pnlMain);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnClose) {
            dispose();
        }
    }
}
