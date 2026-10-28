import javax.swing.*;
import java.awt.*;
public class LevelPanel extends JPanel {
    public LevelPanel(JFrame frame){
        setLayout(new BorderLayout(20,20));setBackground(new Color(19,21,32));
        JLabel title=new JLabel("SELECT STAGE   /   UNLOCKED: "+Progress.unlocked()+" / 10",SwingConstants.CENTER);
        title.setForeground(Color.WHITE);title.setFont(new Font(Font.MONOSPACED,Font.BOLD,25));add(title,BorderLayout.NORTH);
        JPanel grid=new JPanel(new GridLayout(5,2,16,16));grid.setOpaque(false);grid.setBorder(BorderFactory.createEmptyBorder(20,65,20,65));
        for(int i=1;i<=10;i++){
            final int stage=i;Stage info=Stage.get(i);JButton btn=new JButton(""+i+" - "+info.name+(i>Progress.unlocked()?" [LOCKED]":""));
            btn.setEnabled(i<=Progress.unlocked());btn.setFont(new Font(Font.MONOSPACED,Font.BOLD,14));
            btn.setBackground(info.sky);btn.setForeground(i<=Progress.unlocked()?Color.WHITE:Color.GRAY);btn.setOpaque(true);
            btn.addActionListener(e->{frame.setContentPane(new GamePanel(frame,stage));frame.revalidate();frame.repaint();});grid.add(btn);
        }
        add(grid,BorderLayout.CENTER);
        JButton back=new JButton("BACK TO MENU");back.addActionListener(e->{frame.setContentPane(new StartPanel(frame));frame.revalidate();frame.repaint();});add(back,BorderLayout.SOUTH);
    }
}