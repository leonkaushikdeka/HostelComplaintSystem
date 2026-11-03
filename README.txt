HOSTEL COMPLAINT MANAGEMENT SYSTEM
Java Programming (PCC-01), B.Tech CSE (Honours) Cyber Security, 3rd Semester
Department of IT, Assam Skill University, Mangaldoi
Leon Kaushik Deka (IT25B0233) and Banasri Sarma (IT26L0103)

REQUIREMENTS
    JDK 8 or above (javac and java on the PATH). No other libraries.

HOW TO RUN (Windows)
    1. Double click compile.bat
    2. Double click run.bat

LOGINS
    Warden  : BHA, BHB or GH with password warden@123
    Student : Roll No. with password asu@ + last 4 digits, for example IT25B9001 / asu@9001

FOLDER STRUCTURE
    HostelComplaintSystem\
        compile.bat
        run.bat
        README.txt
        SYLLABUS_MAP.txt
        DEMO_ORDER.txt
        src\
            hcms\
                Main.java
                model\
                    Person.java
                    Student.java
                    Staff.java
                    Warden.java
                    Hostel.java
                    Complaint.java
                    Trackable.java
                    Reportable.java
                exceptions\
                    InvalidRollNumberException.java
                    InvalidRoomException.java
                    DuplicateComplaintException.java
                    EmptyFieldException.java
                threads\
                    EscalationThread.java
                    ClockThread.java
                    LockoutThread.java
                data\
                    HostelData.java
                    FileHandler.java
                gui\
                    LoginFrame.java
                    RoomMapFrame.java
                    StudentDashboard.java
                    ComplaintForm.java
                    WardenDashboard.java
                    StatisticsFrame.java
                    AboutDialog.java
        bin\                    created by compile.bat
        data\                   created on the first run
            complaints.txt
            report_<hostelcode>_<date>.txt
