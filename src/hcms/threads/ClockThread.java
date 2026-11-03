package hcms.threads;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

public class ClockThread extends Thread {

    private JLabel lblClock;
    private boolean running = true;
    private DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy   HH:mm:ss");

    public ClockThread(JLabel lblClock) {
        this.lblClock = lblClock;
        setName("ClockThread");
        setDaemon(true);
    }

    public void stopClock() {
        running = false;
        interrupt();
    }

    public void run() {
        while (running) {
            lblClock.setText(LocalDateTime.now().format(format));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
}
