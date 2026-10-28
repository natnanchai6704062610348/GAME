import java.awt.*;
public final class MovingPlatform extends Platform {
    private final int originX,originY,amplitude;
    private final boolean vertical;
    private int ticks;
    public MovingPlatform(int x,int y,int width,int amplitude,boolean vertical){super(x,y,width);originX=x;originY=y;this.amplitude=amplitude;this.vertical=vertical;}
    @Override public void update(){super.update();ticks++;int offset=(int)Math.round(Math.sin(ticks*.025)*amplitude);if(vertical)y=originY+offset;else x=originX+offset;}
    @Override public void draw(Graphics2D g,int camera,Color accent){
        Graphics2D copy=(Graphics2D)g.create();copy.setColor(new Color(85,195,225,90));copy.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10,new float[]{5,5},0));
        if(vertical)copy.drawLine(originX+width/2-camera,originY-amplitude,originX+width/2-camera,originY+amplitude+25);
        else copy.drawLine(originX-amplitude-camera,originY+28,originX+width+amplitude-camera,originY+28);
        super.draw(copy,camera,new Color(85,215,245));copy.setColor(Color.CYAN);copy.setFont(new Font(Font.MONOSPACED,Font.BOLD,12));copy.drawString(vertical?"LIFT":"<  MOVING  >",x-camera+width/2-40,y+15);copy.dispose();
    }
}

