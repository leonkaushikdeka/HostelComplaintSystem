package hcms.gui;

import hcms.data.HostelData;
import hcms.model.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class RoomMapFrame extends Frame implements ActionListener {

    private static final String[] FLOOR_NAMES = {"Ground", "First", "Second"};
    private static final Color VACANT = new Color(144, 238, 144);
    private static final Color OCCUPIED = Color.LIGHT_GRAY;
    private static final Color OPEN_COMPLAINT = new Color(240, 110, 110);

    private HostelData data;
    private Hostel hostel;
    private MenuItem miGround, miFirst, miSecond, miClose;
    private Button[] btnRooms = new Button[Hostel.ROOMS_PER_FLOOR];
    private Label lblFloor, lblInfo;

    public RoomMapFrame(HostelData data, Hostel hostel) {
        super("Room Map - " + hostel.getName());
        this.data = data;
        this.hostel = hostel;

        MenuBar mbRoomMap = new MenuBar();
        Menu mnuFloor = new Menu("Floor");
        miGround = addItem(mnuFloor, "Ground");
        miFirst = addItem(mnuFloor, "First");
        miSecond = addItem(mnuFloor, "Second");
        Menu mnuWindow = new Menu("Window");
        miClose = addItem(mnuWindow, "Close");
        mbRoomMap.add(mnuFloor);
        mbRoomMap.add(mnuWindow);
        setMenuBar(mbRoomMap);

        setLayout(new BorderLayout(10, 10));
        Panel pnlTop = new Panel(new GridLayout(2, 1));
        lblFloor = new Label("", Label.CENTER);
        lblFloor.setFont(new Font("SansSerif", Font.BOLD, 14));
        pnlTop.add(lblFloor);
        pnlTop.add(new Label("Green: vacant     Grey: occupied     Red: open complaint", Label.CENTER));
        add(pnlTop, BorderLayout.NORTH);

        Panel pnlRooms = new Panel(new GridLayout(4, 5, 8, 8));
        for (int i = 0; i < btnRooms.length; i++) {
            btnRooms[i] = new Button();
            btnRooms[i].addActionListener(this);
            pnlRooms.add(btnRooms[i]);
        }
        add(pnlRooms, BorderLayout.CENTER);

        lblInfo = new Label();
        add(lblInfo, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        showFloor(0);
        setSize(720, 440);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private MenuItem addItem(Menu menu, String text) {
        MenuItem item = new MenuItem(text);
        item.addActionListener(this);
        menu.add(item);
        return item;
    }

    private void showFloor(int floor) {
        lblFloor.setText(hostel.getName() + " - " + FLOOR_NAMES[floor] + " Floor");
        for (int i = 0; i < btnRooms.length; i++) {
            String roomNo = Hostel.roomNumber(floor, i);
            int beds = hostel.getBedsUsed(floor, i);
            btnRooms[i].setLabel(roomNo + "   (" + beds + "/" + Hostel.BEDS_PER_ROOM + ")");
            btnRooms[i].setActionCommand(roomNo);
            if (data.getOpenComplaints(hostel.getCode(), roomNo).size() > 0) {
                btnRooms[i].setBackground(OPEN_COMPLAINT);
            } else if (beds > 0) {
                btnRooms[i].setBackground(OCCUPIED);
            } else {
                btnRooms[i].setBackground(VACANT);
            }
        }
        lblInfo.setText(" Click a room to see its occupants and open complaints.");
    }

    private void showRoomInfo(String roomNo) {
        ArrayList<Student> occupants = data.getStudentsInRoom(hostel.getCode(), roomNo);
        ArrayList<Complaint> open = data.getOpenComplaints(hostel.getCode(), roomNo);
        StringBuffer info = new StringBuffer(" Room " + roomNo + ": ");
        if (occupants.size() == 0) {
            info.append("Vacant");
        }
        for (int i = 0; i < occupants.size(); i++) {
            if (i > 0) {
                info.append(", ");
            }
            info.append(occupants.get(i).getName());
        }
        info.append(".   Open complaints: ");
        if (open.size() == 0) {
            info.append("None");
        }
        for (int i = 0; i < open.size(); i++) {
            if (i > 0) {
                info.append(", ");
            }
            Complaint c = open.get(i);
            info.append(c.getId()).append(" ").append(c.getCategory()).append(" (").append(c.getStatus()).append(")");
        }
        lblInfo.setText(info.toString());
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == miGround) {
            showFloor(0);
        } else if (source == miFirst) {
            showFloor(1);
        } else if (source == miSecond) {
            showFloor(2);
        } else if (source == miClose) {
            dispose();
        } else {
            showRoomInfo(e.getActionCommand());
        }
    }
}
