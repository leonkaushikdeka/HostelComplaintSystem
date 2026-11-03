package hcms.threads;

import java.awt.*;

public class LockoutThread extends Thread {

    private Button btnLogin;
    private Label lblMessage;
    private int seconds;

    public LockoutThread(Button btnLogin, Label lblMessage, int seconds) {
        this.btnLogin = btnLogin;
        this.lblMessage = lblMessage;
        this.seconds = seconds;
        setName("LockoutThread");
        setDaemon(true);
    }

    public void run() {
        System.out.println("[" + getName() + "] Login locked for " + seconds + " seconds");
        for (int i = seconds; i > 0; i--) {
            lblMessage.setText("Too many wrong attempts. Try again in " + i + " seconds.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
        btnLogin.setEnabled(true);
        lblMessage.setText("Login unlocked. You can try again.");
        System.out.println("[" + getName() + "] Login unlocked");
    }
}
