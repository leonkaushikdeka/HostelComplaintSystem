package hcms.model;

import hcms.exceptions.InvalidRoomException;

public class Hostel {

    public static final int FLOORS = 3;
    public static final int ROOMS_PER_FLOOR = 20;
    public static final int BEDS_PER_ROOM = 2;

    private String code;
    private String name;
    private int[][] occupancy = new int[FLOORS][ROOMS_PER_FLOOR];

    public Hostel(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void addOccupant(String roomNo) {
        char floorDigit = roomNo.charAt(0);
        int floor = floorDigit - '0';
        int room = Integer.parseInt(roomNo.substring(1));
        occupancy[floor][room - 1]++;
    }

    public String checkRoom(String roomNo) throws InvalidRoomException {
        if (roomNo.length() != 3) {
            throw new InvalidRoomException("Room No. must have 3 digits, for example 018.");
        }
        int number = Integer.parseInt(roomNo);
        int floor = number / 100;
        int room = number % 100;
        if (number < 0 || floor >= FLOORS || room < 1 || room > ROOMS_PER_FLOOR) {
            throw new InvalidRoomException("Room " + roomNo + " does not exist in " + name + ".");
        }
        return roomNumber(floor, room - 1);
    }

    public static String roomNumber(int floor, int roomIndex) {
        int number = floor * 100 + roomIndex + 1;
        return String.format("%03d", number);
    }

    public int getBedsUsed(int floor, int roomIndex) {
        return occupancy[floor][roomIndex];
    }

    public int getBedsOccupied() {
        int total = 0;
        for (int f = 0; f < FLOORS; f++) {
            for (int r = 0; r < ROOMS_PER_FLOOR; r++) {
                total += occupancy[f][r];
            }
        }
        return total;
    }

    public int getTotalBeds() {
        return FLOORS * ROOMS_PER_FLOOR * BEDS_PER_ROOM;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
}
