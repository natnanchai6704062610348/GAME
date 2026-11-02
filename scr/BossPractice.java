import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * เปิดโหมดฝึกต่อสู้กับบอสแยกจากเกมหลัก
 * ไม่ปลดล็อกด่านหรือเปลี่ยนความคืบหน้าของผู้เล่น
 */
public class BossPractice {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // ให้ผู้เล่นเลือกบอสของด่านที่ต้องการฝึก
            Object choice = JOptionPane.showInputDialog(
                null,
                "Choose boss stage (1-10)",
                "Boss Practice",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
                1
            );

            // ยกเลิกการเปิดเกมหากผู้เล่นปิดกล่องเลือก
            if (choice == null) {
                return;
            }

            int level = (Integer) choice;

            // สร้างหน้าต่างโหมดฝึกบอส
            JFrame frame = new JFrame(
                BossFactory.name(level) + " - Boss Practice"
            );

            frame.setSize(1100, 650);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            // true หมายถึงเปิดโหมดฝึกบอส
            frame.setContentPane(new GamePanel(frame, level, true));
            frame.setVisible(true);

            // รับเหตุการณ์กดและปล่อยปุ่มขณะอยู่ในหน้าเกม
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {

                    boolean isGamePanel =
                        frame.getContentPane() instanceof GamePanel;

                    boolean isKeyEvent =
                        e.getID() == KeyEvent.KEY_PRESSED
                        || e.getID() == KeyEvent.KEY_RELEASED;

                    if (isGamePanel && isKeyEvent) {
                        GamePanel game =
                            (GamePanel) frame.getContentPane();

                        boolean pressed =
                            e.getID() == KeyEvent.KEY_PRESSED;

                        game.handleKey(e.getKeyCode(), pressed);

                        return true;
                    }

                    return false;
                });
        });
    }
}