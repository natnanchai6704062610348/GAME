import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/** Standalone practice does not unlock stages or modify progression. */
public class BossPractice {
 public static void main(String[] args){SwingUtilities.invokeLater(()->{
  Object choice=JOptionPane.showInputDialog(null,"Choose boss stage (1-10)","Boss Practice",JOptionPane.QUESTION_MESSAGE,null,new Integer[]{1,2,3,4,5,6,7,8,9,10},1);if(choice==null)return;int level=(Integer)choice;
  JFrame frame=new JFrame(BossFactory.name(level)+" - Boss Practice");frame.setSize(1100,650);frame.setResizable(false);frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);frame.setLocationRelativeTo(null);
  frame.setContentPane(new GamePanel(frame,level,true));frame.setVisible(true);
  KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e->{
   if(frame.getContentPane() instanceof GamePanel && (e.getID()==KeyEvent.KEY_PRESSED || e.getID()==KeyEvent.KEY_RELEASED)){
    ((GamePanel)frame.getContentPane()).handleKey(e.getKeyCode(),e.getID()==KeyEvent.KEY_PRESSED);return true;
   }return false;
  });
 });}
}

