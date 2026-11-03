package hcms.gui;

import hcms.data.HostelData;
import hcms.data.RiskRadar;
import hcms.model.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class RiskRadarFrame extends JFrame implements ActionListener {

    private static final Color NO_RISK = new Color(225, 242, 225);
    private static final Color CHECK_BLUE = new Color(30, 90, 200);

    private HostelData data;
    private Hostel hostel;
    private RiskRadar radar;
    private ArrayList<RoomRisk> ranking = new ArrayList<RoomRisk>();
    private RoomRisk selected;

    private HeatmapPanel pnlHeatmap;
    private JTable tblRanking;
    private DefaultTableModel model;
    private JTextArea taExplain;
    private JLabel lblSummary;
    private JButton btnRefresh, btnRules, btnSave, btnClose;

    public RiskRadarFrame(HostelData data, Hostel hostel) {
        super("Maintenance Risk Radar - " + hostel.getName());
        this.data = data;
        this.hostel = hostel;
        this.radar = new RiskRadar(data, hostel);

        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlMain.add(buildTopPanel(), BorderLayout.NORTH);

        JPanel pnlBody = new JPanel(new BorderLayout(10, 10));
        pnlHeatmap = new HeatmapPanel();
        pnlHeatmap.setBorder(BorderFactory.createTitledBorder("Floor heatmap (click a room)"));
        pnlBody.add(pnlHeatmap, BorderLayout.NORTH);
        pnlBody.add(buildDetailPanel(), BorderLayout.CENTER);
        pnlMain.add(pnlBody, BorderLayout.CENTER);

        JPanel pnlSouth = new JPanel(new BorderLayout());
        JPanel pnlButtons = new JPanel(new FlowLayout());
        btnRefresh = addButton(pnlButtons, "Refresh");
        btnRules = addButton(pnlButtons, "How Scores Work");
        btnSave = addButton(pnlButtons, "Save Inspection List");
        btnClose = addButton(pnlButtons, "Close");
        lblSummary = new JLabel();
        pnlSouth.add(pnlButtons, BorderLayout.CENTER);
        pnlSouth.add(lblSummary, BorderLayout.SOUTH);
        pnlMain.add(pnlSouth, BorderLayout.SOUTH);

        setContentPane(pnlMain);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        refresh();
        setSize(1200, 740);
        setMinimumSize(new Dimension(800, 560));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildTopPanel() {
        JPanel pnlTop = new JPanel(new GridLayout(2, 1, 4, 4));
        JLabel lblTitle = new JLabel("Maintenance Risk Radar - " + hostel.getName(), JLabel.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        pnlTop.add(lblTitle);
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlLegend.add(legendLabel("No risk", NO_RISK, Color.GRAY));
        pnlLegend.add(legendLabel("Low 1 to " + (RoomRisk.MEDIUM_LEVEL - 1), colorFor(15), Color.GRAY));
        pnlLegend.add(legendLabel("Medium " + RoomRisk.MEDIUM_LEVEL + " to " + (RoomRisk.HIGH_LEVEL - 1),
                colorFor(45), Color.GRAY));
        pnlLegend.add(legendLabel("High " + RoomRisk.HIGH_LEVEL + " to " + RoomRisk.MAX_SCORE, colorFor(80),
                Color.GRAY));
        pnlLegend.add(legendLabel("CHECK: preventive check", Color.WHITE, CHECK_BLUE));
        pnlTop.add(pnlLegend);
        return pnlTop;
    }

    private JLabel legendLabel(String text, Color background, Color border) {
        JLabel label = new JLabel(" " + text + " ");
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(colorFor(80).equals(background) ? Color.WHITE : Color.BLACK);
        label.setBorder(BorderFactory.createLineBorder(border, 2));
        return label;
    }

    private JPanel buildDetailPanel() {
        String[] columns = {"Rank", "Room", "Score", "Level", "Action", "Main reason"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRanking = new JTable(model);
        tblRanking.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {40, 45, 45, 60, 115, 260};
        for (int i = 0; i < widths.length; i++) {
            tblRanking.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        tblRanking.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = tblRanking.getSelectedRow();
                if (!e.getValueIsAdjusting() && row != -1 && row < ranking.size()) {
                    selectRoom(ranking.get(row));
                }
            }
        });
        JScrollPane spTable = new JScrollPane(tblRanking);
        spTable.setBorder(BorderFactory.createTitledBorder("Rooms ranked by risk"));

        taExplain = new JTextArea();
        taExplain.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taExplain.setEditable(false);
        taExplain.setLineWrap(true);
        taExplain.setWrapStyleWord(true);
        JScrollPane spExplain = new JScrollPane(taExplain);
        spExplain.setBorder(BorderFactory.createTitledBorder("Why this score"));

        JPanel pnlDetail = new JPanel(new GridLayout(1, 2, 10, 10));
        pnlDetail.add(spTable);
        pnlDetail.add(spExplain);
        return pnlDetail;
    }

    private JButton addButton(JPanel panel, String text) {
        JButton button = new JButton(text);
        button.addActionListener(this);
        panel.add(button);
        return button;
    }

    private void refresh() {
        String keepRoom = selected == null ? null : selected.getRoomNo();
        radar.calculate();
        ranking = radar.getRanking();
        model.setRowCount(0);
        for (int i = 0; i < ranking.size(); i++) {
            RoomRisk room = ranking.get(i);
            model.addRow(new Object[] {i + 1, room.getRoomNo(), room.getScore(), room.getLevel(), room.getAction(),
                    room.getMainReason()});
        }
        lblSummary.setText("Rooms: " + radar.getSummary());
        System.out.println("[RiskRadar] " + hostel.getName() + ": " + radar.getSummary());
        selected = null;
        if (keepRoom != null) {
            selectRoom(findRoom(keepRoom));
        } else if (ranking.size() > 0) {
            selectRoom(ranking.get(0));
        } else {
            taExplain.setText("No room has any risk points right now.");
            pnlHeatmap.repaint();
        }
    }

    private RoomRisk findRoom(String roomNo) {
        int floor = roomNo.charAt(0) - '0';
        int index = Integer.parseInt(roomNo.substring(1)) - 1;
        return radar.getRoom(floor, index);
    }

    private void selectRoom(RoomRisk room) {
        if (room == selected) {
            return;
        }
        selected = room;
        taExplain.setText(room.generateSlip());
        taExplain.setCaretPosition(0);
        int row = ranking.indexOf(room);
        if (row == -1) {
            tblRanking.clearSelection();
        } else if (tblRanking.getSelectedRow() != row) {
            tblRanking.setRowSelectionInterval(row, row);
            tblRanking.scrollRectToVisible(tblRanking.getCellRect(row, 0, true));
        }
        pnlHeatmap.repaint();
    }

    private void showRules() {
        JTextArea taRules = new JTextArea(RiskRadar.getRulesText(), 26, 72);
        taRules.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taRules.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(taRules), "How Scores Work",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveInspectionList() {
        String fileName = data.exportInspectionList(hostel.getCode(), radar.buildInspectionList());
        if (fileName != null) {
            System.out.println("[RiskRadar] Inspection list saved: " + fileName);
            JOptionPane.showMessageDialog(this, "Inspection list saved: " + fileName);
        } else {
            JOptionPane.showMessageDialog(this, "Could not save the inspection list.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnRefresh) {
            refresh();
        } else if (source == btnRules) {
            showRules();
        } else if (source == btnSave) {
            saveInspectionList();
        } else if (source == btnClose) {
            dispose();
        }
    }

    static Color colorFor(int score) {
        if (score <= 0) {
            return NO_RISK;
        }
        int red = 255 - 44 * score / 100;
        int green = 230 - 183 * score / 100;
        int blue = 120 - 73 * score / 100;
        return new Color(red, green, blue);
    }

    private class HeatmapPanel extends JPanel {

        private static final int LABEL_WIDTH = 100;
        private static final int TOP = 22;
        private static final int GAP = 3;
        private static final int FLOOR_GAP = 10;

        HeatmapPanel() {
            setPreferredSize(new Dimension(1100, 260));
            setToolTipText("");
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    RoomRisk room = roomAt(e.getX(), e.getY());
                    if (room != null) {
                        selectRoom(room);
                    }
                }
            });
        }

        private int cellWidth() {
            return (getWidth() - LABEL_WIDTH - 15) / Hostel.ROOMS_PER_FLOOR;
        }

        private int cellHeight() {
            return (getHeight() - TOP - 12 - FLOOR_GAP * (Hostel.FLOORS - 1)) / Hostel.FLOORS;
        }

        private int rowOf(int floor) {
            return Hostel.FLOORS - 1 - floor;
        }

        private RoomRisk roomAt(int x, int y) {
            int width = cellWidth();
            int height = cellHeight() + FLOOR_GAP;
            if (width <= 0 || height <= 0) {
                return null;
            }
            int column = (x - LABEL_WIDTH) / width;
            int row = (y - TOP) / height;
            if (x < LABEL_WIDTH || y < TOP || column >= Hostel.ROOMS_PER_FLOOR || row >= Hostel.FLOORS) {
                return null;
            }
            return radar.getRoom(Hostel.FLOORS - 1 - row, column);
        }

        public String getToolTipText(MouseEvent e) {
            RoomRisk room = roomAt(e.getX(), e.getY());
            if (room == null) {
                return null;
            }
            return "Room " + room.getRoomNo() + ": score " + room.getScore() + " (" + room.getLevel() + "), "
                    + room.getAction();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int width = cellWidth();
            int height = cellHeight();
            for (int f = 0; f < Hostel.FLOORS; f++) {
                int y = TOP + rowOf(f) * (height + FLOOR_GAP);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2.drawString(RiskRadar.FLOOR_NAMES[f] + " Floor", 10, y + height / 2 + 5);
                for (int r = 0; r < Hostel.ROOMS_PER_FLOOR; r++) {
                    drawRoom(g2, radar.getRoom(f, r), LABEL_WIDTH + r * width, y, width - GAP, height);
                }
            }
        }

        private void drawRoom(Graphics2D g2, RoomRisk room, int x, int y, int width, int height) {
            int score = room.getScore();
            g2.setColor(colorFor(score));
            g2.fillRect(x, y, width, height);
            g2.setStroke(new BasicStroke(1));
            g2.setColor(Color.GRAY);
            g2.drawRect(x, y, width, height);
            if (room.isPreventiveCheck()) {
                g2.setStroke(new BasicStroke(3));
                g2.setColor(CHECK_BLUE);
                g2.drawRect(x + 2, y + 2, width - 4, height - 4);
            }
            if (room == selected) {
                g2.setStroke(new BasicStroke(3));
                g2.setColor(Color.BLACK);
                g2.drawRect(x - 1, y - 1, width + 2, height + 2);
            }
            g2.setColor(score >= RoomRisk.HIGH_LEVEL ? Color.WHITE : Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(room.getRoomNo(), x + 6, y + 16);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString(score == 0 ? "-" : "" + score, x + 6, y + 34);
            if (room.isPreventiveCheck()) {
                g2.setColor(CHECK_BLUE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                g2.drawString("CHECK", x + 6, y + height - 8);
            }
        }
    }
}
