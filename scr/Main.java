import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class Main {
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            JFrame frame=new JFrame("Zombie Survival - ProjectGameOOP");
            frame.setSize(1100,650);frame.setResizable(false);frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);frame.setContentPane(new StartPanel(frame));frame.setVisible(true);
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e->{
                if(frame.getContentPane() instanceof GamePanel){
                    if(e.getID()==KeyEvent.KEY_PRESSED || e.getID()==KeyEvent.KEY_RELEASED){
                        ((GamePanel)frame.getContentPane()).handleKey(e.getKeyCode(),e.getID()==KeyEvent.KEY_PRESSED);
                        return true;
                    }
                }
                return false;
            });
        });
    }
}