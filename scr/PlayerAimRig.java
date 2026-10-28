import java.awt.*;
/** Shared shoulder/hand/muzzle geometry for both rendering and bullet spawning. */
public final class PlayerAimRig {
    public static final class Pose {
        public final double shoulderX,shoulderY,handX,handY,angle;
        Pose(double sx,double sy,double hx,double hy,double a){shoulderX=sx;shoulderY=sy;handX=hx;handY=hy;angle=a;}
        public double muzzleX(int gunWidth){return handX+Math.cos(angle)*gunWidth*.75;}
        public double muzzleY(int gunWidth){return handY+Math.sin(angle)*gunWidth*.75;}
    }
    public static Pose solve(Player player,MouseAim mouse,int camera){
        double sx=player.getX()+player.getWidth()/2.0+(player.isFacingRight()?-6:6),sy=player.getY()+33;
        double reachAngle=mouse.angle(sx,sy,camera,player.isFacingRight());
        double hx=sx+Math.cos(reachAngle)*22,hy=sy+Math.sin(reachAngle)*22+7;
        double angle=mouse.angle(hx,hy,camera,player.isFacingRight());
        return new Pose(sx,sy,hx,hy,angle);
    }
    public static void drawArm(Graphics2D original,Pose pose,int camera,boolean rear,boolean right){
        Graphics2D g=(Graphics2D)original.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        double sx=pose.shoulderX-camera+(rear?(right?-5:5):0),sy=pose.shoulderY+(rear?3:0);
        double hx=pose.handX-camera+(rear?Math.cos(pose.angle)*10:0),hy=pose.handY+(rear?Math.sin(pose.angle)*10+2:0);
        double dx=hx-sx,dy=hy-sy,len=Math.max(1,Math.hypot(dx,dy));
        double bend=Math.sqrt(Math.max(0,19*19-len*len/4));double sign=dx>=0?1:-1;
        double ex=(sx+hx)/2-dy/len*bend*sign,ey=(sy+hy)/2+dx/len*bend*sign;
        segment(g,sx,sy,ex,ey,10,new Color(12,25,30));segment(g,ex,ey,hx,hy,9,new Color(12,25,30));
        Color sleeve=rear?new Color(37,63,65):new Color(62,97,97);
        segment(g,sx,sy,ex,ey,7,sleeve);segment(g,ex,ey,hx,hy,6,sleeve.brighter());
        g.setColor(new Color(28,32,32));g.fillOval((int)hx-4,(int)hy-4,8,8);g.dispose();
    }
    private static void segment(Graphics2D g,double x,double y,double xx,double yy,float width,Color color){g.setColor(color);g.setStroke(new BasicStroke(width,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.drawLine((int)x,(int)y,(int)xx,(int)yy);}
    public static void drawFallenArms(Graphics2D g,Player p,int camera){
        Graphics2D copy=(Graphics2D)g.create();int cx=p.getX()+p.getWidth()/2-camera,cy=p.getY()+p.getHeight()-10;
        segment(copy,cx,cy-7,cx+17,cy,7,new Color(50,80,80));segment(copy,cx+17,cy,cx+29,cy-2,6,new Color(50,80,80));copy.dispose();
    }
}

