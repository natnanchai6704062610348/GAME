import java.awt.*;
import java.awt.geom.*;
/** Visual skill rendering, independent of collision and damage logic. */
public final class BossSkillEffects {
    private static final Color[] COLORS={Color.ORANGE,Color.ORANGE,Color.CYAN,new Color(150,230,80),new Color(190,170,130),new Color(255,70,140),new Color(120,230,40),new Color(255,215,100),new Color(255,95,20),new Color(195,85,255)};
    public static void draw(Graphics2D original,int skill,int x,int y,int w,int h,int age,boolean warning,boolean projectile,double vx,double vy){
        Graphics2D g=(Graphics2D)original.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color c=COLORS[Math.max(1,Math.min(10,skill))-1];
        if(warning){
            int alpha=55+(int)(35*(1+Math.sin(age*.24)));
            g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));g.fillRoundRect(x,y,w,h,12,12);
            g.setColor(c);g.setStroke(new BasicStroke(2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{8,5},age%13));g.drawRoundRect(x,y,w,h,12,12);
            int cy=y+h/2;g.drawOval(x+w/2-15,cy-15,30,30);g.drawLine(x+w/2-22,cy,x+w/2+22,cy);g.drawLine(x+w/2,cy-22,x+w/2,cy+22);
            g.dispose();return;
        }
        if(projectile){
            double angle=Math.atan2(vy,vx);int cx=x+w/2,cy=y+h/2;
            for(int i=5;i>0;i--){int tx=cx-(int)(Math.cos(angle)*i*7),ty=cy-(int)(Math.sin(angle)*i*7);g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),150-i*23));g.fillOval(tx-w/3,ty-h/3,w*2/3,h*2/3);}
            if(skill==5){g.translate(cx,cy);g.rotate(age*.16);g.setColor(new Color(130,110,85));int[] px={-w/2,0,w/2,w/3,-w/3},py={0,-h/2,-h/5,h/2,h/2};g.fillPolygon(px,py,5);g.setColor(new Color(230,190,130));g.drawLine(-w/3,0,w/4,-h/4);}
            else{g.setColor(c);g.fillOval(x,y,w,h);g.setColor(new Color(255,255,225));g.fillOval(x+w/4,y+h/4,w/2,h/2);}
            g.dispose();return;
        }
        switch(skill){
            case 1:
                g.setColor(new Color(255,175,50,180));g.setStroke(new BasicStroke(3));for(int i=0;i<5;i++){int radius=(age*17+i*45)%Math.max(1,w/2);g.drawOval(x+w/2-radius,y+h/2-radius/8,radius*2,Math.max(6,radius/4));}break;
            case 2: case 9:
                for(int i=0;i<w;i+=14){
                    double wave=.5+.5*Math.sin(age*.45+i*.3);int flame=(int)(h*(.4+.6*wave));int fy=y+h-flame;
                    Path2D path=new Path2D.Double();path.moveTo(x+i-8,y+h);path.curveTo(x+i-18,fy+flame*.4,x+i+15,fy+flame*.3,x+i+4,fy);path.curveTo(x+i+30,fy+flame*.4,x+i+17,y+h-10,x+i+22,y+h);path.closePath();
                    g.setColor(new Color(255,70+(i%60),15,180));g.fill(path);g.setColor(new Color(255,220,70,210));g.fillOval(x+i,y+h-flame/2,10,flame/2);
                }break;
            case 3:
                for(int bolt=0;bolt<3;bolt++){
                    Path2D path=new Path2D.Double();path.moveTo(x+w/2,y);
                    for(int i=1;i<=12;i++){double jitter=Math.sin(i*8.1+(age/3)*3.7+bolt)*w*.42;path.lineTo(x+w/2+jitter,y+h*i/12.0);}
                    g.setColor(new Color(55,150,255,70));g.setStroke(new BasicStroke(12));g.draw(path);g.setColor(Color.CYAN);g.setStroke(new BasicStroke(4));g.draw(path);g.setColor(Color.WHITE);g.setStroke(new BasicStroke(1.5f));g.draw(path);
                }break;
            case 4:
                g.setColor(new Color(125,210,80,220));for(int i=0;i<8;i++){int sx=x+i*w/8;g.fillPolygon(new int[]{sx,sx+8,sx+16},new int[]{y+h,y-(i%3)*6,y+h},3);}break;
            case 6:
                int cx=x+w/2,cy=y+h/2;g.setColor(new Color(150,25,100,35));g.fillOval(x,y,w,h);
                g.setColor(new Color(255,60,130,150));g.setStroke(new BasicStroke(3));for(int i=0;i<4;i++){int radius=(age*5+i*45)%(w/2);g.drawOval(cx-radius,cy-radius,radius*2,radius*2);}
                for(int i=0;i<18;i++){double a=i*2.399+age*.04;int r=(age*3+i*17)%(w/2);g.fillOval(cx+(int)(Math.cos(a)*r),cy+(int)(Math.sin(a)*r*.7),5,5);}break;
            case 7:
                g.setColor(new Color(90,180,15,190));g.fillOval(x,y,w,h);g.setColor(new Color(175,250,55));g.drawOval(x,y,w,h);
                for(int i=0;i<7;i++){int bx=x+(i*29+age/2)%Math.max(1,w-10),rise=(age+i*11)%40;g.setColor(new Color(150,240,55,190-rise*4));g.drawOval(bx,y-rise/2,5+rise/4,5+rise/4);}break;
            default:
                g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),110));g.fillOval(x,y,w,h);break;
        }
        g.dispose();
    }
    public static void impact(Graphics2D g,int x,int y,int radius,Color color,int life){
        g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),Math.min(255,life*18)));g.setStroke(new BasicStroke(3));g.drawOval(x-radius,y-radius,radius*2,radius*2);
        for(int i=0;i<10;i++){double a=i*Math.PI/5;int dx=(int)(Math.cos(a)*radius),dy=(int)(Math.sin(a)*radius);g.drawLine(x+dx/2,y+dy/2,x+dx,y+dy);}
    }
}

