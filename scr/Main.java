import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * จุดเริ่มต้นของโปรแกรม
 * สร้างหน้าต่างเกมและรับเหตุการณ์จากแป้นพิมพ์
 */
public class Main {

    public static void main(String[] args) {

        // สร้างและแสดง GUI บน Event Dispatch Thread ของ Swing
        SwingUtilities.invokeLater(() -> {

            // สร้างหน้าต่างหลัก
            JFrame frame = new JFrame(
                "Zombie Survival - ProjectGameOOP"
            );

            frame.setSize(1100, 650);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // จัดหน้าต่างไว้กลางจอและแสดงหน้าเริ่มเกม
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new StartPanel(frame));
            frame.setVisible(true);

            // รับเหตุการณ์จากแป้นพิมพ์
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {

                    // ส่งคำสั่งควบคุมเฉพาะขณะแสดงหน้าเกม
                    if (frame.getContentPane() instanceof GamePanel) {

                        boolean keyPressed =
                            e.getID() == KeyEvent.KEY_PRESSED;

                        boolean keyReleased =
                            e.getID() == KeyEvent.KEY_RELEASED;

                        if (keyPressed || keyReleased) {
                            GamePanel game =
                                (GamePanel) frame.getContentPane();

                            // true = กดปุ่ม, false = ปล่อยปุ่ม
                            game.handleKey(
                                e.getKeyCode(),
                                keyPressed
                            );

                            // เหตุการณ์ถูกจัดการแล้ว
                            return true;
                        }
                    }

                    // เปิดให้ระบบจัดการเหตุการณ์อื่นต่อ
                    return false;
                });
        });
    }
}