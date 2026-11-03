package hcms.data;

import hcms.model.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;

public class RiskRadar {

    public static final String[] FLOOR_NAMES = {"Ground", "First", "Second"};
    public static final int[] PRIORITY_POINTS = {4, 8, 15};
    public static final int[] CATEGORY_POINTS = {6, 15, 2, 2, 0, 8, 0, 20, 0, 2};
    public static final String[] CATEGORY_REASONS = {"water leaks spread to walls and rooms nearby",
            "risk of electric shock or fire", "broken furniture or doors", "hygiene problem",
            "not a fault in the room", "unsafe drinking water", "no danger to people", "people can get hurt",
            "not a fault in the room", "unknown problem"};
    public static final String[] SHARED_PARTS = {"pipes", "wiring", null, null, null, "water line", null,
            "corridor and exits", null, null};
    public static final int POINTS_PER_DAY = 3;
    public static final int MAX_AGE_POINTS = 30;
    public static final int ESCALATED_POINTS = 10;
    public static final int RECENT_DAYS = 30;
    public static final int REPEAT_POINTS = 5;
    public static final int MAX_REPEAT_POINTS = 10;
    public static final int SAME_PROBLEM_POINTS = 10;
    public static final int MAX_SAME_PROBLEM_POINTS = 20;
    public static final int BUSY_FLOOR_POINTS = 3;
    public static final int MAX_BUSY_FLOOR_POINTS = 9;
    public static final int NEIGHBOUR_POINTS = 10;
    public static final int MAX_NEIGHBOUR_POINTS = 20;
    public static final int POINTS_PER_STUDENT = 4;

    private HostelData data;
    private Hostel hostel;
    private RoomRisk[][] rooms = new RoomRisk[Hostel.FLOORS][Hostel.ROOMS_PER_FLOOR];
    private LocalDate today;

    public RiskRadar(HostelData data, Hostel hostel) {
        this.data = data;
        this.hostel = hostel;
        calculate();
    }

    public void calculate() {
        today = LocalDate.now();
        ArrayList<Complaint> list = data.getHostelComplaints(hostel.getCode());
        createRooms();
        for (int f = 0; f < Hostel.FLOORS; f++) {
            for (int r = 0; r < Hostel.ROOMS_PER_FLOOR; r++) {
                addComplaintPoints(rooms[f][r], list);
                addRepeatPoints(rooms[f][r], list);
                addSameProblemPoints(rooms[f][r], list);
                addBusyFloorPoints(rooms[f][r], list);
            }
        }
        for (int f = 0; f < Hostel.FLOORS; f++) {
            for (int r = 0; r < Hostel.ROOMS_PER_FLOOR; r++) {
                markIfRisky(rooms[f][r]);
            }
        }
        for (int f = 0; f < Hostel.FLOORS; f++) {
            for (int r = 0; r < Hostel.ROOMS_PER_FLOOR; r++) {
                addNeighbourPoints(rooms[f][r]);
            }
        }
        for (int f = 0; f < Hostel.FLOORS; f++) {
            for (int r = 0; r < Hostel.ROOMS_PER_FLOOR; r++) {
                addOccupancyPoints(rooms[f][r]);
            }
        }
    }

    private void createRooms() {
        for (int f = 0; f < Hostel.FLOORS; f++) {
            for (int r = 0; r < Hostel.ROOMS_PER_FLOOR; r++) {
                String roomNo = Hostel.roomNumber(f, r);
                ArrayList<String> names = new ArrayList<String>();
                for (Student s : data.getStudentsInRoom(hostel.getCode(), roomNo)) {
                    names.add(s.getName());
                }
                rooms[f][r] = new RoomRisk(hostel.getName(), roomNo, FLOOR_NAMES[f], f, r, names);
            }
        }
    }

    private void addComplaintPoints(RoomRisk room, ArrayList<Complaint> list) {
        for (Complaint c : list) {
            synchronized (c) {
                if (c.isOpen() && c.getRoomNo().equals(room.getRoomNo())) {
                    room.addOpenComplaint(c.getCategory());
                    int days = (int) c.getDaysOpen();
                    int k = indexOf(Complaint.CATEGORIES, c.getCategory());
                    room.addPoints(Math.min(days * POINTS_PER_DAY, MAX_AGE_POINTS), "Complaint " + c.getId() + " ("
                            + c.getCategory() + ") open " + dayText(days) + ", " + POINTS_PER_DAY + " a day");
                    room.addPoints(PRIORITY_POINTS[c.getPriorityLevel()], "Complaint " + c.getId() + " has "
                            + c.getPriority() + " priority");
                    room.addPoints(CATEGORY_POINTS[k], c.getCategory() + ": " + CATEGORY_REASONS[k]);
                    if (c.getStatus().equals("Escalated")) {
                        room.addPoints(ESCALATED_POINTS, "Complaint " + c.getId() + " was escalated (Pending over "
                                + Complaint.ESCALATION_DAYS + " days)");
                    }
                }
            }
        }
    }

    private void addRepeatPoints(RoomRisk room, ArrayList<Complaint> list) {
        int given = 0;
        for (Complaint c : list) {
            if (!c.isOpen() && c.getResolvedDate() != null && c.getRoomNo().equals(room.getRoomNo())) {
                long ago = ChronoUnit.DAYS.between(c.getResolvedDate(), today);
                if (ago <= RECENT_DAYS && given < MAX_REPEAT_POINTS) {
                    room.addPoints(REPEAT_POINTS, "Complaint " + c.getId() + " (" + c.getCategory() + ") was fixed "
                            + agoText(ago) + ", check the fix is holding");
                    given += REPEAT_POINTS;
                }
            }
        }
    }

    private void addSameProblemPoints(RoomRisk room, ArrayList<Complaint> list) {
        int given = 0;
        ArrayList<String> counted = new ArrayList<String>();
        for (String category : room.getOpenCategories()) {
            for (Complaint c : list) {
                String key = category + " " + c.getRoomNo();
                boolean recent = c.isOpen() || ChronoUnit.DAYS.between(c.getFiledDate(), today) <= RECENT_DAYS;
                if (c.getCategory().equals(category) && recent && isOtherRoomOnFloor(room, c)
                        && !counted.contains(key) && given < MAX_SAME_PROBLEM_POINTS) {
                    counted.add(key);
                    room.addPoints(SAME_PROBLEM_POINTS, "Floor pattern: " + category + " also reported in room "
                            + c.getRoomNo() + ", may be one shared fault");
                    given += SAME_PROBLEM_POINTS;
                }
            }
        }
    }

    private void addBusyFloorPoints(RoomRisk room, ArrayList<Complaint> list) {
        if (room.getOpenComplaints() == 0) {
            return;
        }
        ArrayList<String> busyRooms = new ArrayList<String>();
        for (Complaint c : list) {
            if (c.isOpen() && isOtherRoomOnFloor(room, c) && !busyRooms.contains(c.getRoomNo())) {
                busyRooms.add(c.getRoomNo());
            }
        }
        Collections.sort(busyRooms);
        room.addPoints(Math.min(busyRooms.size() * BUSY_FLOOR_POINTS, MAX_BUSY_FLOOR_POINTS),
                "Floor pattern: open complaints also in " + (busyRooms.size() == 1 ? "room " : "rooms ")
                        + RoomRisk.joinRooms(busyRooms) + ", " + BUSY_FLOOR_POINTS + " each, at most "
                        + MAX_BUSY_FLOOR_POINTS);
    }

    private void markIfRisky(RoomRisk room) {
        int firstScore = room.getPoints() + occupancyPoints(room);
        String spread = findSpreadCategory(room);
        if (firstScore >= RoomRisk.HIGH_LEVEL && spread == null) {
            room.markRisky("High risk room");
        } else if (firstScore >= RoomRisk.MEDIUM_LEVEL && spread != null) {
            int k = indexOf(Complaint.CATEGORIES, spread);
            room.markRisky("open " + spread + " problem, shares " + SHARED_PARTS[k]);
        }
    }

    private String findSpreadCategory(RoomRisk room) {
        String spread = null;
        int best = -1;
        for (String category : room.getOpenCategories()) {
            int k = indexOf(Complaint.CATEGORIES, category);
            if (SHARED_PARTS[k] != null && CATEGORY_POINTS[k] > best) {
                best = CATEGORY_POINTS[k];
                spread = category;
            }
        }
        return spread;
    }

    private void addNeighbourPoints(RoomRisk room) {
        int given = 0;
        for (RoomRisk next : getNeighbours(room)) {
            if (next.isRisky()) {
                next.addRoomToCheck(room.getRoomNo());
                if (room.getOpenComplaints() == 0) {
                    room.setPreventiveCheck(true);
                }
                if (given < MAX_NEIGHBOUR_POINTS) {
                    room.addPoints(NEIGHBOUR_POINTS, "Next to room " + next.getRoomNo() + ": " + next.getRiskNote());
                    given += NEIGHBOUR_POINTS;
                }
            }
        }
    }

    private void addOccupancyPoints(RoomRisk room) {
        int students = room.getOccupantCount();
        room.addPoints(occupancyPoints(room), (students == 1 ? "1 student lives" : students + " students live")
                + " here, " + POINTS_PER_STUDENT + " each (people affected)");
    }

    private int occupancyPoints(RoomRisk room) {
        return room.getPoints() > 0 ? room.getOccupantCount() * POINTS_PER_STUDENT : 0;
    }

    public ArrayList<RoomRisk> getNeighbours(RoomRisk room) {
        ArrayList<RoomRisk> list = new ArrayList<RoomRisk>();
        int f = room.getFloor();
        int r = room.getRoomIndex();
        if (r > 0) {
            list.add(rooms[f][r - 1]);
        }
        if (r < Hostel.ROOMS_PER_FLOOR - 1) {
            list.add(rooms[f][r + 1]);
        }
        if (f > 0) {
            list.add(rooms[f - 1][r]);
        }
        if (f < Hostel.FLOORS - 1) {
            list.add(rooms[f + 1][r]);
        }
        return list;
    }

    private boolean isOtherRoomOnFloor(RoomRisk room, Complaint c) {
        return floorOf(c.getRoomNo()) == room.getFloor() && !c.getRoomNo().equals(room.getRoomNo());
    }

    private int floorOf(String roomNo) {
        if (roomNo.length() != 3 || !Character.isDigit(roomNo.charAt(0))) {
            return -1;
        }
        return roomNo.charAt(0) - '0';
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return array.length - 1;
    }

    private String dayText(long days) {
        return days == 1 ? "1 day" : days + " days";
    }

    private String agoText(long days) {
        if (days == 0) {
            return "today";
        } else if (days == 1) {
            return "yesterday";
        }
        return days + " days ago";
    }

    public RoomRisk getRoom(int floor, int roomIndex) {
        return rooms[floor][roomIndex];
    }

    public ArrayList<RoomRisk> getRanking() {
        ArrayList<RoomRisk> list = new ArrayList<RoomRisk>();
        for (int f = 0; f < Hostel.FLOORS; f++) {
            for (int r = 0; r < Hostel.ROOMS_PER_FLOOR; r++) {
                if (rooms[f][r].getScore() > 0) {
                    list.add(rooms[f][r]);
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    public int countLevel(String level) {
        int count = 0;
        for (RoomRisk room : getRanking()) {
            if (room.getLevel().equals(level)) {
                count++;
            }
        }
        return count;
    }

    public int countPreventiveChecks() {
        int count = 0;
        for (RoomRisk room : getRanking()) {
            if (room.isPreventiveCheck()) {
                count++;
            }
        }
        return count;
    }

    public String getSummary() {
        return countLevel("High") + " High, " + countLevel("Medium") + " Medium, " + countLevel("Low") + " Low, "
                + countPreventiveChecks() + " rooms for preventive check";
    }

    public String buildInspectionList() {
        StringBuffer text = new StringBuffer();
        text.append("MAINTENANCE RISK RADAR: INSPECTION LIST\n");
        text.append("Hostel : ").append(hostel.getName()).append(" (").append(hostel.getCode()).append(")\n");
        text.append("Date   : ").append(today.format(Complaint.DATE_FORMAT)).append("\n");
        text.append("Rooms  : ").append(getSummary()).append("\n");
        text.append("==============================================================\n");
        text.append(String.format("%-5s %-6s %-6s %-8s %s\n", "Rank", "Room", "Score", "Level", "Action"));
        ArrayList<RoomRisk> ranking = getRanking();
        for (int i = 0; i < ranking.size(); i++) {
            RoomRisk room = ranking.get(i);
            text.append(String.format("%-5d %-6s %-6d %-8s %s\n", i + 1, room.getRoomNo(), room.getScore(),
                    room.getLevel(), room.getAction()));
        }
        text.append("==============================================================\n\n");
        for (RoomRisk room : ranking) {
            text.append(room.generateSlip()).append("\n");
        }
        text.append(getRulesText());
        return text.toString();
    }

    public static String getRulesText() {
        StringBuffer text = new StringBuffer();
        text.append("HOW THE RISK SCORE IS WORKED OUT\n");
        text.append("Every room starts at 0. Points are added only for the reasons below.\n");
        text.append("The same fixed rules are used for every room. The total is capped at ")
                .append(RoomRisk.MAX_SCORE).append(".\n\n");
        text.append("1. Each open complaint in the room\n");
        text.append("   Age          ").append(POINTS_PER_DAY).append(" points per day open, at most ")
                .append(MAX_AGE_POINTS).append("\n");
        text.append("   Priority     ");
        for (int i = 0; i < Complaint.PRIORITIES.length; i++) {
            text.append(Complaint.PRIORITIES[i]).append(" ").append(PRIORITY_POINTS[i]).append("   ");
        }
        text.append("\n   Category\n");
        for (int i = 0; i < Complaint.CATEGORIES.length; i++) {
            text.append(String.format("      %-16s %3d  %s\n", Complaint.CATEGORIES[i], CATEGORY_POINTS[i],
                    CATEGORY_REASONS[i]));
        }
        text.append("   Escalated    ").append(ESCALATED_POINTS).append(" (left Pending for more than ")
                .append(Complaint.ESCALATION_DAYS).append(" days)\n\n");
        text.append("2. Room history\n");
        text.append("   Fixed in the last ").append(RECENT_DAYS).append(" days: ").append(REPEAT_POINTS)
                .append(" per complaint, at most ").append(MAX_REPEAT_POINTS).append("\n\n");
        text.append("3. Floor pattern (only for rooms with an open complaint)\n");
        text.append("   Same category in another room on the floor in the last ").append(RECENT_DAYS)
                .append(" days:\n      ").append(SAME_PROBLEM_POINTS).append(" per room, at most ")
                .append(MAX_SAME_PROBLEM_POINTS).append("\n");
        text.append("   Other rooms on the floor with open complaints: ").append(BUSY_FLOOR_POINTS)
                .append(" per room, at most ").append(MAX_BUSY_FLOOR_POINTS).append("\n\n");
        text.append("4. Next to a risky room (left, right, above or below)\n");
        text.append("   ").append(NEIGHBOUR_POINTS).append(" per risky room, at most ").append(MAX_NEIGHBOUR_POINTS)
                .append("\n");
        text.append("   A room is risky when its score is ").append(RoomRisk.HIGH_LEVEL)
                .append(" or more, or ").append(RoomRisk.MEDIUM_LEVEL).append(" or more with an open\n");
        text.append("   Plumbing, Electrical, Drinking Water or Safety problem, because these\n");
        text.append("   spread through shared pipes, wiring, water lines and corridors.\n");
        text.append("   A nearby room with no open complaint of its own gets a preventive check\n");
        text.append("   (a visit within 2 days instead if its score reaches ").append(RoomRisk.MEDIUM_LEVEL)
                .append(").\n\n");
        text.append("5. People affected (only when the room already has points)\n");
        text.append("   ").append(POINTS_PER_STUDENT).append(" per student living in the room\n\n");
        text.append("LEVELS\n");
        text.append("   Low 1 to ").append(RoomRisk.MEDIUM_LEVEL - 1).append("      Medium ")
                .append(RoomRisk.MEDIUM_LEVEL).append(" to ").append(RoomRisk.HIGH_LEVEL - 1)
                .append("      High ").append(RoomRisk.HIGH_LEVEL).append(" to ").append(RoomRisk.MAX_SCORE)
                .append("\n");
        return text.toString();
    }
}
