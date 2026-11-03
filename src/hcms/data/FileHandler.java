package hcms.data;

import hcms.model.Complaint;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class FileHandler {

    private static final String FOLDER = "data";
    private static final String COMPLAINT_FILE = FOLDER + File.separator + "complaints.txt";

    public boolean loadComplaints(ArrayList<Complaint> list) {
        BufferedReader reader = null;
        boolean fileFound = false;
        try {
            reader = new BufferedReader(new FileReader(COMPLAINT_FILE));
            fileFound = true;
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() > 0) {
                    Complaint c = parseLine(line);
                    if (c != null) {
                        list.add(c);
                    }
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            System.out.println("[FileHandler] " + COMPLAINT_FILE + " not found. First run, loading sample complaints.");
        } catch (IOException e) {
            System.out.println("[FileHandler] Could not read complaints: " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("[FileHandler] Could not close the complaints file.");
            }
        }
        return fileFound;
    }

    private Complaint parseLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 13) {
            System.out.println("[FileHandler] Skipped a line with missing fields: " + line);
            return null;
        }
        try {
            int id = Integer.parseInt(parts[0].trim());
            LocalDate filedDate = LocalDate.parse(parts[6], Complaint.DATE_FORMAT);
            LocalDate resolvedDate = null;
            if (!parts[12].equals("-")) {
                resolvedDate = LocalDate.parse(parts[12], Complaint.DATE_FORMAT);
            }
            boolean anonymous = parts[8].equals("Yes");
            boolean allowEntry = parts[9].equals("Yes");
            return new Complaint(id, parts[1], parts[2], parts[3], parts[4], parts[5], parts[10], anonymous,
                    allowEntry, filedDate, parts[7], parts[11], resolvedDate);
        } catch (NumberFormatException e) {
            System.out.println("[FileHandler] Skipped a line with a wrong ID: " + line);
        } catch (DateTimeParseException e) {
            System.out.println("[FileHandler] Skipped a line with a wrong date: " + line);
        }
        return null;
    }

    private String toLine(Complaint c) {
        String resolved = "-";
        if (c.getResolvedDate() != null) {
            resolved = c.getResolvedDate().format(Complaint.DATE_FORMAT);
        }
        return c.getId() + "|" + c.getRollNo() + "|" + c.getHostelCode() + "|" + c.getRoomNo() + "|"
                + c.getCategory() + "|" + c.getPriority() + "|" + c.getFiledDateText() + "|" + c.getStatus() + "|"
                + (c.isAnonymous() ? "Yes" : "No") + "|" + (c.isAllowEntry() ? "Yes" : "No") + "|"
                + c.getDescription() + "|" + c.getRemark() + "|" + resolved;
    }

    public synchronized void saveComplaints(ArrayList<Complaint> list) {
        PrintWriter writer = null;
        try {
            new File(FOLDER).mkdirs();
            writer = new PrintWriter(new FileWriter(COMPLAINT_FILE));
            for (Complaint c : list) {
                writer.println(toLine(c));
            }
        } catch (IOException e) {
            System.out.println("[FileHandler] Could not save complaints: " + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public String writeReport(String hostelCode, String reportText) {
        String fileName = FOLDER + File.separator + "report_" + hostelCode + "_"
                + LocalDate.now().format(Complaint.DATE_FORMAT) + ".txt";
        FileWriter writer = null;
        try {
            new File(FOLDER).mkdirs();
            writer = new FileWriter(fileName);
            writer.write(reportText.replace("\n", System.lineSeparator()));
            return fileName;
        } catch (IOException e) {
            System.out.println("[FileHandler] Could not write report: " + e.getMessage());
            return null;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                System.out.println("[FileHandler] Could not close the report file.");
            }
        }
    }
}
