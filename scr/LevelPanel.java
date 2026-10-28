import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Stage cards, boss intel, and launch controls share a single selection model. */
public class LevelPanel extends JPanel {
    private static final Color INK=new Color(12,18,25),MINT=new Color(116,234,197),MUTED=new Color(146,160,172);
    private final JFrame frame;
    private final int unlocked;
    private int selected;
    private final JButton launch=new JButton(),back=new JButton("<  MAIN MENU");
    private final StageCard[] cards=new StageCard[10];
    private final BufferedImage[] images=new BufferedImage[10];
    private BufferedImage backdrop;
    private final StageBossArt bossArt=new StageBossArt();
    private final BossSpriteAnimation ironArt=new BossSpriteAnimation();
    private int detailX,detailWidth,contentTop=132,contentHeight;

    public LevelPanel(JFrame frame){this(frame,Progress.unlocked());}
    LevelPanel(JFrame frame,int unlocked){
        this.frame=frame;this.unlocked=Math.max(1,Math.min(10,unlocked));selected=this.unlocked;
        setLayout(null);setBackground(INK);setFocusable(true);
        backdrop=load("menu_bg.png");
        for(int i=0;i<10;i++){
            images[i]=load("BG"+(i+1)+".png");if(images[i]==null)images[i]=load("BG1.png");
            cards[i]=new StageCard(i+1);add(cards[i]);
        }
        style(launch,true);style(back,false);add(launch);add(back);
        launch.addActionListener(e->startSelected());back.addActionListener(e->back());
        bind("LEFT",()->select(Math.max(1,selected-1)));bind("RIGHT",()->select(Math.min(10,selected+1)));
        bind("UP",()->select(selected>5?selected-5:selected));bind("DOWN",()->select(selected<=5?selected+5:selected));
        bind("ENTER",this::startSelected);bind("ESCAPE",this::back);select(selected);
    }
    private BufferedImage load(String name){
        for(String root:new String[]{"images/","../images/","ProjectGameOOP/images/"})try{
            File f=new File(root+name);if(f.isFile())return ImageIO.read(f);
        }catch(Exception ignored){}
        return null;
    }
    private void bind(String key,Runnable task){getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key),key);getActionMap().put(key,new AbstractAction(){public void actionPerformed(ActionEvent e){task.run();}});}
    private void select(int level){selected=level;boolean open=level<=unlocked;launch.setEnabled(open);launch.setText(open?"DEPLOY TO STAGE "+String.format("%02d",level)+"  >":"LOCKED  /  CLEAR STAGE "+(level-1));repaint();}
    private void startSelected(){if(selected>unlocked || frame==null)return;frame.setContentPane(new GamePanel(frame,selected));frame.revalidate();frame.repaint();}
    private void back(){if(frame==null)return;frame.setContentPane(new StartPanel(frame));frame.revalidate();frame.repaint();}
    private void style(JButton b,boolean primary){
        b.setFont(new Font("SansSerif",Font.BOLD,13));b.setForeground(primary?INK:new Color(215,225,231));b.setBackground(primary?MINT:new Color(30,40,51));
        b.setOpaque(true);b.setContentAreaFilled(true);b.setBorder(BorderFactory.createEmptyBorder(12,16,12,16));b.setFocusPainted(true);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    @Override public void doLayout(){
        int margin=36,gap=12;detailWidth=Math.max(250,(int)(getWidth()*.275));detailX=getWidth()-margin-detailWidth;
        int leftWidth=detailX-margin-24;contentHeight=Math.max(240,getHeight()-contentTop-104);
        int cw=(leftWidth-gap*4)/5,ch=(contentHeight-gap)/2;
        for(int i=0;i<10;i++)cards[i].setBounds(margin+(i%5)*(cw+gap),contentTop+(i/5)*(ch+gap),cw,ch);
        back.setBounds(margin,getHeight()-66,164,40);launch.setBounds(detailX,getHeight()-66,detailWidth,40);
    }
    @Override protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        if(backdrop!=null){cover(g,backdrop,0,0,getWidth(),getHeight());g.setColor(new Color(6,12,20,225));g.fillRect(0,0,getWidth(),getHeight());}
        g.setPaint(new GradientPaint(0,0,new Color(12,26,32,200),getWidth(),getHeight(),new Color(8,12,20,230)));g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(new Color(100,215,181,16));for(int x=-getHeight();x<getWidth();x+=46)g.drawLine(x,0,x+getHeight(),getHeight());
        g.setFont(new Font("SansSerif",Font.BOLD,11));g.setColor(MINT);g.drawString("ZOMBIE SURVIVAL   /   CAMPAIGN",36,31);
        g.setFont(new Font("SansSerif",Font.BOLD,34));g.setColor(Color.WHITE);g.drawString("CHOOSE YOUR NEXT FIGHT",34,73);
        g.setFont(new Font("SansSerif",Font.PLAIN,13));g.setColor(MUTED);g.drawString("Ten zones. Ten bosses. One way out.",36,98);
        String progress=String.format("%02d / 10 ZONES UNLOCKED",unlocked);g.setFont(new Font("SansSerif",Font.BOLD,11));g.setColor(MINT);g.drawString(progress,getWidth()-36-g.getFontMetrics().stringWidth(progress),55);
        int px=getWidth()-216;g.setColor(new Color(46,62,68));g.fillRoundRect(px,70,180,4,4,4);g.setColor(MINT);g.fillRoundRect(px,70,18*unlocked,4,4,4);
        drawIntel(g);
        g.setColor(MUTED);g.setFont(new Font("SansSerif",Font.PLAIN,11));g.drawString("ARROWS  Select    ENTER  Deploy    ESC  Menu",222,getHeight()-41);
        g.dispose();
    }
    private void drawIntel(Graphics2D g){
        int x=detailX,y=contentTop,w=detailWidth,h=contentHeight;Stage s=Stage.get(selected);boolean open=selected<=unlocked;
        g.setColor(new Color(21,31,41));g.fillRoundRect(x,y,w,h,16,16);g.setColor(new Color(52,73,83));g.drawRoundRect(x,y,w-1,h-1,16,16);
        g.setColor(MINT);g.setFont(new Font("SansSerif",Font.BOLD,10));g.drawString("BOSS INTEL  /  ZONE "+String.format("%02d",selected),x+18,y+24);
        g.setColor(Color.WHITE);g.setFont(new Font("SansSerif",Font.BOLD,18));g.drawString(s.getBossName(),x+18,y+50);
        int artTop=y+65,artH=Math.max(100,h-202);
        g.setPaint(new RadialGradientPaint(new Point(x+w/2,artTop+artH/2),Math.max(60,w/2),new float[]{0,1},new Color[]{new Color(s.accent.getRed(),s.accent.getGreen(),s.accent.getBlue(),65),new Color(16,24,33,0)}));g.fillRect(x+8,artTop,w-16,artH+20);
        Graphics2D art=(Graphics2D)g.create();art.clipRect(x+12,artTop,w-24,artH+15);
        if(selected==1)ironArt.draw(art,0,x+w/2,artTop+artH,false);else bossArt.draw(art,selected,x+w/2,artTop+artH,artH,false);art.dispose();
        int infoY=y+h-115;g.setColor(new Color(57,74,84));g.drawLine(x+18,infoY-13,x+w-18,infoY-13);
        g.setFont(new Font("SansSerif",Font.PLAIN,11));g.setColor(MUTED);g.drawString("THREAT",x+18,infoY+4);
        for(int i=0;i<10;i++){g.setColor(i<selected?(selected>7?new Color(245,113,81):MINT):new Color(47,61,70));g.fillRoundRect(x+78+i*(w-100)/10,infoY-4,Math.max(4,(w-120)/10),9,2,2);}
        g.setColor(new Color(221,231,236));g.setFont(new Font("SansSerif",Font.BOLD,12));g.drawString(s.quota+" ENEMIES   /   "+(s.worldWidth/1000.0)+"k ROUTE",x+18,infoY+29);
        g.setColor(MUTED);g.setFont(new Font("SansSerif",Font.PLAIN,11));g.drawString(s.feature.toUpperCase(java.util.Locale.ROOT),x+18,infoY+50);
        g.setColor(open?MINT:new Color(225,169,103));g.setFont(new Font("SansSerif",Font.BOLD,11));g.drawString(open?"READY FOR DEPLOYMENT":"CLEAR STAGE "+(selected-1)+" TO UNLOCK",x+18,infoY+79);
    }
    private static void cover(Graphics2D g,BufferedImage image,int x,int y,int w,int h){
        Graphics2D copy=(Graphics2D)g.create();copy.clipRect(x,y,w,h);double scale=Math.max(w/(double)image.getWidth(),h/(double)image.getHeight());int dw=(int)(image.getWidth()*scale),dh=(int)(image.getHeight()*scale);copy.drawImage(image,x+(w-dw)/2,y+(h-dh)/2,dw,dh,null);copy.dispose();
    }
    private class StageCard extends JButton {
        private final int level;
        StageCard(int level){
            this.level=level;setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setRolloverEnabled(true);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getAccessibleContext().setAccessibleName("Stage "+level+" "+Stage.get(level).name+(level>unlocked?" locked":" available"));
            addActionListener(e->select(level));getModel().addChangeListener(e->repaint());
        }
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();boolean chosen=selected==level,locked=level>unlocked,hover=getModel().isRollover();
            g.setClip(new java.awt.geom.RoundRectangle2D.Float(0,0,w,h,12,12));
            if(images[level-1]!=null)cover(g,images[level-1],0,0,w,h);else{g.setColor(Stage.get(level).sky);g.fillRect(0,0,w,h);}
            g.setPaint(new GradientPaint(0,0,new Color(4,9,15,locked?140:25),0,h,new Color(5,12,20,250)));g.fillRect(0,0,w,h);
            if(hover){g.setColor(new Color(116,234,197,28));g.fillRect(0,0,w,h);}
            g.setColor(chosen?MINT:new Color(17,25,34,200));g.fillRoundRect(10,10,32,24,6,6);g.setColor(chosen?INK:Color.WHITE);g.setFont(new Font("SansSerif",Font.BOLD,12));g.drawString(String.format("%02d",level),17,27);
            if(locked){g.setColor(new Color(235,192,135));g.setStroke(new BasicStroke(2));g.drawArc(w-28,15,10,12,0,180);g.drawRoundRect(w-30,21,14,11,2,2);}
            String[] words=Stage.get(level).name.split(" ",2);g.setFont(new Font("SansSerif",Font.BOLD,11));g.setColor(locked?new Color(180,187,193):Color.WHITE);
            g.drawString(words[0],10,h-48);if(words.length>1)g.drawString(words[1],10,h-33);
            g.setFont(new Font("SansSerif",Font.BOLD,9));g.setColor(locked?new Color(225,169,103):MINT);g.drawString(locked?"LOCKED":(level==unlocked?"NEXT MISSION":"AVAILABLE"),10,h-13);
            g.setClip(null);g.setColor(chosen?MINT:(hover||hasFocus()?new Color(150,190,190):new Color(65,78,88)));g.setStroke(new BasicStroke(chosen?2.5f:1));g.drawRoundRect(1,1,w-3,h-3,12,12);g.dispose();
        }
    }
}
