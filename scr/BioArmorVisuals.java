import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
/** Bone-attached sprite armor, glowing veins, and cosmetic release particles. */
public final class BioArmorVisuals {
    private static final BufferedImage[][] PIECES=load();
    private static BufferedImage[][] load(){
        BufferedImage[][] result=new BufferedImage[2][3];
        for(String root:new String[]{"images/EFFECT/","../images/EFFECT/"})try{
            File file=new File(root+"bio-armor-parts-v1.png");if(!file.isFile())continue;BufferedImage sheet=ImageIO.read(file);
            for(int row=0;row<2;row++)for(int col=0;col<3;col++){
                int left=col*sheet.getWidth()/3,top=row*sheet.getHeight()/2;
                BufferedImage cell=sheet.getSubimage(left,top,(col+1)*sheet.getWidth()/3-left,(row+1)*sheet.getHeight()/2-top);
                int minX=cell.getWidth(),minY=cell.getHeight(),maxX=-1,maxY=-1;
                for(int y=0;y<cell.getHeight();y++)for(int x=0;x<cell.getWidth();x++)if((cell.getRGB(x,y)>>>24)>40){minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);}
                if(maxX>=minX)result[row][col]=cell.getSubimage(minX,minY,maxX-minX+1,maxY-minY+1);
            }break;
        }catch(Exception e){System.err.println("Bio armor art: "+e.getMessage());}
        return result;
    }
    public static boolean isLoaded(){for(BufferedImage[] row:PIECES)for(BufferedImage image:row)if(image==null)return false;return true;}
    private static boolean visible(Player p){return p.getArmor()>0&&!p.isDead();}
    public static void drawShoulder(Graphics2D original,Player p,PlayerAimRig.Pose pose,int camera){
        if(!visible(p))return;MutationArmor mutation=p.getMutationArmor();int stage=mutation.getVisualStage();
        Graphics2D g=(Graphics2D)original.create();int cx=(int)pose.shoulderX-camera,cy=(int)pose.shoulderY;
        int w=24+stage*5,h=20+stage*5;BufferedImage piece=PIECES[0][stage];
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        if(piece!=null){if(p.isFacingRight())g.drawImage(piece,cx-w/2,cy-h/2-3,w,h,null);else g.drawImage(piece,cx+w/2,cy-h/2-3,-w,h,null);}
        else{g.setColor(new Color(27,17,42));g.fillOval(cx-12,cy-12,25,22);}
        int pulse=100+(int)(60*(1+Math.sin(mutation.getAnimationTicks()*.16)));
        g.setColor(new Color(170,100,255,pulse));g.setStroke(new BasicStroke(1.2f));g.drawPolyline(new int[]{cx-7,cx-3,cx+1,cx+5},new int[]{cy-5,cy-8,cy-1,cy-3},4);g.dispose();
    }
    public static void drawForearm(Graphics2D original,Player p,PlayerAimRig.Pose pose,int camera,boolean rear){
        if(!visible(p))return;int stage=p.getMutationArmor().getVisualStage();
        double sx=pose.shoulderX-camera+(rear?(p.isFacingRight()?-5:5):0),sy=pose.shoulderY+(rear?3:0);
        double hx=pose.handX-camera+(rear?Math.cos(pose.angle)*10:0),hy=pose.handY+(rear?Math.sin(pose.angle)*10+2:0);
        double dx=hx-sx,dy=hy-sy,len=Math.max(1,Math.hypot(dx,dy)),bend=Math.sqrt(Math.max(0,19*19-len*len/4)),sign=dx>=0?1:-1;
        double ex=(sx+hx)/2-dy/len*bend*sign,ey=(sy+hy)/2+dx/len*bend*sign;
        Graphics2D g=(Graphics2D)original.create();g.translate(ex,ey);g.rotate(Math.atan2(hy-ey,hx-ex));
        int width=(int)Math.hypot(hx-ex,hy-ey)+5,height=10+stage*3;BufferedImage piece=PIECES[1][stage];
        if(piece!=null)g.drawImage(piece,-2,-height/2,width,height,null);else{g.setColor(new Color(28,16,42));g.fillRoundRect(-2,-6,width,12,5,5);}
        g.setColor(new Color(190,120,255,190));g.setStroke(new BasicStroke(1.2f));g.drawPolyline(new int[]{0,width/3,width/2,width-4},new int[]{0,-2,2,0},4);g.dispose();
    }
    public static void drawAura(Graphics2D original,Player p,int camera){
        if(!visible(p))return;MutationArmor m=p.getMutationArmor();if(m.getCharge()<40)return;
        Graphics2D g=(Graphics2D)original.create();int cx=p.getX()+p.getWidth()/2-camera,cy=p.getY()+p.getHeight()/2;
        int radius=m.isReady()?70:48;g.setPaint(new RadialGradientPaint(new Point(cx,cy),radius,new float[]{0,.65f,1},new Color[]{new Color(130,40,225,0),new Color(135,55,245,m.isReady()?35:18),new Color(130,40,225,0)}));g.fillOval(cx-radius,cy-radius,radius*2,radius*2);
        g.setColor(new Color(195,120,255,160));for(int i=0;i<(m.isReady()?12:5);i++){double a=i*2.399+m.getAnimationTicks()*.025;int r=30+(i*7)%32;int xx=cx+(int)(Math.cos(a)*r),yy=cy+(int)(Math.sin(a)*r);g.fillOval(xx,yy,2,3);}g.dispose();
    }
    public static void drawRelease(Graphics2D original,Player p,int camera){
        int remaining=p.getMutationArmor().getReleaseTicks();if(remaining<=0)return;
        Graphics2D g=(Graphics2D)original.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int age=24-remaining,radius=18+age*(MutationArmor.RADIUS-18)/23,cx=p.getX()+p.getWidth()/2-camera,cy=p.getY()+p.getHeight()/2;
        g.setColor(new Color(165,80,255,remaining*8));g.setStroke(new BasicStroke(4));g.drawOval(cx-radius,cy-radius,radius*2,radius*2);
        for(int i=0;i<18;i++){
            double a=i*Math.PI/9;int tip=radius+16;Path2D thorn=new Path2D.Double();thorn.moveTo(cx+Math.cos(a-.035)*(radius-13),cy+Math.sin(a-.035)*(radius-13));thorn.lineTo(cx+Math.cos(a)*tip,cy+Math.sin(a)*tip);thorn.lineTo(cx+Math.cos(a+.035)*(radius-13),cy+Math.sin(a+.035)*(radius-13));thorn.closePath();
            g.setColor(new Color(55,22,80,remaining*9));g.fill(thorn);g.setColor(new Color(225,160,255,remaining*9));g.draw(thorn);
        }g.dispose();
    }
}
