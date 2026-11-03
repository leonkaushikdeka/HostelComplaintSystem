package hcms.threads;

import hcms.data.HostelData;
import hcms.gui.WardenDashboard;
import hcms.model.Complaint;
import java.util.ArrayList;
import javax.swing.*;

public class EscalationThread extends Thread {

    private static final long CHECK_INTERVAL = 15000;
    private static WardenDashboard openDashboard = null;

    private HostelData data;

    public EscalationThread(HostelData data) {
        this.data = data;
        setName("EscalationThread");
        setDaemon(true);
    }

    public static synchronized void setOpenDashboard(WardenDashboard dashboard) {
        openDashboard = dashboard;
    }

    public static synchronized WardenDashboard getOpenDashboard() {
        return openDashboard;
    }

    public void run() {
        System.out.println("[" + getName() + "] Started. Checking every " + (CHECK_INTERVAL / 1000) + " seconds.");
        while (true) {
            try {
                Thread.sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                System.out.println("[" + getName() + "] Stopped.");
                return;
            }
            ArrayList<Complaint> escalated = data.escalateOldComplaints();
            for (int i = 0; i < escalated.size(); i++) {
                System.out.println("[" + getName() + "] Complaint " + escalated.get(i).getId() + " escalated");
            }
            if (escalated.size() > 0) {
                data.save();
                final WardenDashboard dashboard = getOpenDashboard();
                if (dashboard != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            dashboard.refreshTable();
                        }
                    });
                }
            }
        }
    }
}
