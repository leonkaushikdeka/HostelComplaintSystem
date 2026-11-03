package hcms.gui;

import hcms.data.HostelData;
import hcms.model.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;

public class StatisticsFrame extends JFrame implements ActionListener {

    private static final int GOOD_RATE = 60;

    private HostelData data;
    private Hostel hostel;
    private JTextArea taStatistics;
    private JButton btnClose;

    public StatisticsFrame(HostelData data, Hostel hostel) {
        super("Statistics - " + hostel.getName());
        this.data = data;
        this.hostel = hostel;

        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        taStatistics = new JTextArea(buildStatistics());
        taStatistics.setFont(new Font("Monospaced", Font.PLAIN, 13));
        taStatistics.setEditable(false);
        pnlMain.add(new JScrollPane(taStatistics), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout());
        btnClose = new JButton("Close");
        btnClose.addActionListener(this);
        pnlButtons.add(btnClose);
        pnlMain.add(pnlButtons, BorderLayout.SOUTH);

        setContentPane(pnlMain);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 640);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private String buildStatistics() {
        String[] categories = Complaint.CATEGORIES;
        String[] statuses = Complaint.STATUSES;
        ArrayList<Complaint> list = data.getHostelComplaints(hostel.getCode());

        int total = 0;
        int pending = 0;
        int inProgress = 0;
        int escalated = 0;
        int resolved = 0;
        long totalDays = 0;
        int[] categoryCount = new int[categories.length];
        int[][] categoryStatus = new int[categories.length][statuses.length];

        for (int i = 0; i < list.size(); i++) {
            Complaint c = list.get(i);
            total++;
            switch (c.getStatus()) {
                case "Pending":
                    pending++;
                    break;
                case "In Progress":
                    inProgress++;
                    break;
                case "Escalated":
                    escalated++;
                    break;
                case "Resolved":
                    resolved++;
                    totalDays += c.getDaysToResolve();
                    break;
                default:
                    break;
            }
            int row = indexOf(categories, c.getCategory());
            int column = indexOf(statuses, c.getStatus());
            categoryCount[row] += 1;
            categoryStatus[row][column] += 1;
        }

        double resolutionRate = 0.0;
        if (total > 0) {
            resolutionRate = (double) resolved / total * 100;
        }
        int ratePercent = (int) Math.round(resolutionRate);

        double averageDays = 0.0;
        if (resolved > 0) {
            averageDays = totalDays;
            averageDays /= resolved;
        }

        int maxIndex = 0;
        for (int i = 1; i < categoryCount.length; i++) {
            if (categoryCount[i] > categoryCount[maxIndex]) {
                maxIndex = i;
            }
        }
        String mostReported = total == 0 ? "None" : categories[maxIndex] + " (" + categoryCount[maxIndex] + ")";
        String performance = ratePercent >= GOOD_RATE ? "Good" : "Needs attention";
        int open = pending + inProgress + escalated;

        StringBuffer text = new StringBuffer();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        text.append("STATISTICS - ").append(hostel.getName()).append(" (").append(hostel.getCode()).append(")\n");
        text.append("Generated on ").append(LocalDateTime.now().format(format)).append("\n\n");
        text.append("Total complaints         : ").append(total).append("\n");
        text.append("Pending                  : ").append(pending).append("\n");
        text.append("In Progress              : ").append(inProgress).append("\n");
        text.append("Escalated                : ").append(escalated).append("\n");
        text.append("Resolved                 : ").append(resolved).append("\n");
        text.append("Open complaints          : ").append(open).append("\n\n");
        text.append("Resolution rate          : ").append(String.format("%.1f", resolutionRate));
        text.append(" %  (rounded: ").append(ratePercent).append(" %)\n");
        text.append("Average days to resolve  : ").append(String.format("%.2f", averageDays)).append("\n");
        text.append("Most reported category   : ").append(mostReported).append("\n");
        text.append("Performance              : ").append(performance).append("\n");
        text.append("Beds occupied            : ").append(hostel.getBedsOccupied()).append(" of ");
        text.append(hostel.getTotalBeds()).append("\n\n");

        text.append("CATEGORY x STATUS\n");
        text.append(String.format("%-16s", "Category"));
        for (int j = 0; j < statuses.length; j++) {
            text.append(String.format("%12s", statuses[j]));
        }
        text.append(String.format("%8s", "Total")).append("\n");
        for (int i = 0; i < categories.length; i++) {
            text.append(String.format("%-16s", categories[i]));
            for (int j = 0; j < statuses.length; j++) {
                text.append(String.format("%12d", categoryStatus[i][j]));
            }
            text.append(String.format("%8d", categoryCount[i])).append("\n");
        }
        return text.toString();
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return array.length - 1;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnClose) {
            dispose();
        }
    }
}
