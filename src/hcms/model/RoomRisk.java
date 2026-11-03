package hcms.model;

import java.util.ArrayList;

public class RoomRisk implements Reportable, Comparable<RoomRisk> {

    public static final int MEDIUM_LEVEL = 30;
    public static final int HIGH_LEVEL = 60;
    public static final int MAX_SCORE = 100;

    private String hostelName;
    private String roomNo;
    private String floorName;
    private int floor;
    private int roomIndex;
    private ArrayList<String> occupants;
    private ArrayList<String> reasons = new ArrayList<String>();
    private ArrayList<String> openCategories = new ArrayList<String>();
    private ArrayList<String> roomsToCheck = new ArrayList<String>();
    private int points = 0;
    private int openComplaints = 0;
    private String mainReason = "-";
    private String riskNote = null;
    private boolean preventiveCheck = false;

    public RoomRisk(String hostelName, String roomNo, String floorName, int floor, int roomIndex,
            ArrayList<String> occupants) {
        this.hostelName = hostelName;
        this.roomNo = roomNo;
        this.floorName = floorName;
        this.floor = floor;
        this.roomIndex = roomIndex;
        this.occupants = occupants;
    }

    public void addPoints(int value, String reason) {
        if (value <= 0) {
            return;
        }
        points += value;
        if (reasons.size() == 0) {
            mainReason = reason;
        }
        reasons.add(String.format("%+4d  %s", value, reason));
    }

    public void addOpenComplaint(String category) {
        openComplaints++;
        if (!openCategories.contains(category)) {
            openCategories.add(category);
        }
    }

    public void markRisky(String note) {
        riskNote = note;
    }

    public void addRoomToCheck(String neighbourRoomNo) {
        roomsToCheck.add(neighbourRoomNo);
    }

    public void setPreventiveCheck(boolean preventiveCheck) {
        this.preventiveCheck = preventiveCheck;
    }

    public int getScore() {
        return Math.min(points, MAX_SCORE);
    }

    public String getLevel() {
        int score = getScore();
        if (score >= HIGH_LEVEL) {
            return "High";
        } else if (score >= MEDIUM_LEVEL) {
            return "Medium";
        } else if (score > 0) {
            return "Low";
        }
        return "None";
    }

    public String getAction() {
        String level = getLevel();
        if (level.equals("High")) {
            return "Attend today";
        } else if (level.equals("Medium")) {
            return "Visit within 2 days";
        } else if (preventiveCheck) {
            return "Preventive check";
        } else if (level.equals("Low")) {
            return "Keep watching";
        }
        return "No action";
    }

    public String generateSlip() {
        StringBuffer slip = new StringBuffer();
        String line = "------------------------------------------------------------\n";
        slip.append("ROOM ").append(roomNo).append(", ").append(hostelName).append(", ").append(floorName)
                .append(" Floor\n");
        slip.append("Students   : ").append(occupants.size() == 0 ? "Vacant" : joinNames()).append("\n");
        slip.append("Risk score : ").append(getScore()).append(" / ").append(MAX_SCORE);
        slip.append("     Level: ").append(getLevel()).append("\n");
        slip.append(line);
        if (reasons.size() == 0) {
            slip.append("No open complaints, no recent repairs and no risky room nearby.\n");
        } else {
            slip.append("How the score was worked out:\n");
            for (String reason : reasons) {
                slip.append(reason).append("\n");
            }
            slip.append(String.format("%4d  Total", points));
            if (points > MAX_SCORE) {
                slip.append(", counted as ").append(MAX_SCORE).append(" (the maximum)");
            }
            slip.append("\n");
        }
        slip.append(line);
        slip.append("What to do : ").append(getAction()).append("\n");
        if (roomsToCheck.size() > 0) {
            slip.append(roomsToCheck.size() == 1 ? "Also check the nearby room: " : "Also check nearby rooms: ")
                    .append(joinRooms(roomsToCheck)).append("\n");
        }
        if (preventiveCheck) {
            slip.append("No open complaint from this room. It is checked because a room next to it\n");
            slip.append("(left, right, above or below) has a problem that can spread.\n");
        }
        return slip.toString();
    }

    private String joinNames() {
        StringBuffer names = new StringBuffer();
        for (int i = 0; i < occupants.size(); i++) {
            if (i > 0) {
                names.append(", ");
            }
            names.append(occupants.get(i));
        }
        return names.toString();
    }

    public static String joinRooms(ArrayList<String> rooms) {
        StringBuffer text = new StringBuffer();
        for (int i = 0; i < rooms.size(); i++) {
            if (i > 0) {
                text.append(i == rooms.size() - 1 ? " and " : ", ");
            }
            text.append(rooms.get(i));
        }
        return text.toString();
    }

    public int compareTo(RoomRisk other) {
        if (other.getScore() != getScore()) {
            return other.getScore() - getScore();
        }
        return roomNo.compareTo(other.roomNo);
    }

    public String getRoomNo() { return roomNo; }
    public int getFloor() { return floor; }
    public int getRoomIndex() { return roomIndex; }
    public int getPoints() { return points; }
    public int getOccupantCount() { return occupants.size(); }
    public int getOpenComplaints() { return openComplaints; }
    public ArrayList<String> getOpenCategories() { return openCategories; }
    public String getMainReason() { return mainReason; }
    public String getRiskNote() { return riskNote; }
    public boolean isRisky() { return riskNote != null; }
    public boolean isPreventiveCheck() { return preventiveCheck; }

    public String toString() {
        return "Room " + roomNo + " [" + getScore() + ", " + getLevel() + "]";
    }
}
