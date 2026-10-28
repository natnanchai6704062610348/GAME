import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
/** Body-only sprites; arms and gun are drawn independently by PlayerAimRig. */
public class PlayerAnimation {
    public enum State { IDLE, WALK, JUMP, SHOOT, HURT, DEATH }
    private final BufferedImage[] frames=new BufferedImage[16];
    private State state=State.IDLE;
    private int ticks,hurtTicks,landingTicks,jumpTicks,jumpFrame=8;
    private double lastX,walkDistance;
    private boolean initialized,wasGrounded=true;
    private long deathAt;
    private int standingHeight=1;
    private boolean reverseWalk;
    public PlayerAnimation(){
        for(String root:new String[]{"images/PLAYER/","../images/PLAYER/","ProjectGameOOP/images/PLAYER/"})try{
            File f=new File(root+"survivor-body-v2.png");if(f.isFile()){load(ImageIO.read(f));return;}
        }catch(Exception e){System.err.println("Player body: "+e.getMessage());}
    }
    private void load(BufferedImage sheet){
        for(int row=0;row<4;row++)for(int col=0;col<4;col++){
            double[] rows={0,328.0/1254,635.0/1254,937.0/1254,1};
            double[] cols=row==3?new double[]{0,315.0/1254,624.0/1254,920.0/1254,1}:new double[]{0,.25,.5,.75,1};
            int x=(int)(cols[col]*sheet.getWidth()),y=(int)(rows[row]*sheet.getHeight());
            BufferedImage cell=mainComponent(sheet.getSubimage(x,y,(int)(cols[col+1]*sheet.getWidth())-x,(int)(rows[row+1]*sheet.getHeight())-y));
            int minX=cell.getWidth(),minY=cell.getHeight(),maxX=-1,maxY=-1;
            for(int yy=0;yy<cell.getHeight();yy++)for(int xx=0;xx<cell.getWidth();xx++)if((cell.getRGB(xx,yy)>>>24)>40){minX=Math.min(minX,xx);minY=Math.min(minY,yy);maxX=Math.max(maxX,xx);maxY=Math.max(maxY,yy);}
            if(maxX>=minX){int index=row*4+col;frames[index]=cell.getSubimage(minX,minY,maxX-minX+1,maxY-minY+1);if(index<8)standingHeight=Math.max(standingHeight,frames[index].getHeight());}
        }
    }
    private BufferedImage mainComponent(BufferedImage cell){
        int w=cell.getWidth(),h=cell.getHeight();boolean[] seen=new boolean[w*h];int[] queue=new int[w*h],best=new int[0];
        for(int start=0;start<w*h;start++){
            if(seen[start] || (cell.getRGB(start%w,start/w)>>>24)<=40)continue;
            int head=0,size=0;queue[size++]=start;seen[start]=true;
            while(head<size){int n=queue[head++],x=n%w,y=n/w;
                for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){
                    int xx=x+dx,yy=y+dy;if(xx<0||xx>=w||yy<0||yy>=h)continue;int k=yy*w+xx;
                    if(!seen[k]&&(cell.getRGB(xx,yy)>>>24)>40){seen[k]=true;queue[size++]=k;}
                }
            }
            if(size>best.length)best=java.util.Arrays.copyOf(queue,size);
        }
        BufferedImage clean=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        for(int k:best)clean.setRGB(k%w,k/w,cell.getRGB(k%w,k/w));return clean;
    }
    public boolean isLoaded(){for(BufferedImage frame:frames)if(frame==null)return false;return true;}
    public void setState(State next){if(state!=next){state=next;ticks=0;}}
    public State getCurrentState(){return state;}
    public void update(Player player,boolean shooting){
        ticks++;
        if(!initialized){lastX=player.getX();initialized=true;}
        double dx=player.getX()-lastX;lastX=player.getX();
        if(player.isMoving()){walkDistance+=Math.abs(dx);reverseWalk=(dx>0)!=player.isFacingRight();}
        boolean grounded=player.isGrounded();
        if(grounded&&!wasGrounded)landingTicks=4;
        if(!grounded&&wasGrounded)jumpTicks=0;
        if(!grounded){jumpTicks++;jumpFrame=jumpTicks<4?8:(player.getVerticalVelocity()<0?9:10);}
        wasGrounded=grounded;
        if(player.isDead()){playDeath();return;}
        if(hurtTicks>0){hurtTicks--;setState(State.HURT);return;}
        if(!grounded){setState(State.JUMP);return;}
        if(landingTicks>0){landingTicks--;setState(State.JUMP);return;}
        // Locomotion remains active while firing: shooting no longer freezes the legs.
        if(player.isMoving())setState(State.WALK);
        else setState(shooting?State.SHOOT:State.IDLE);
    }
    public void playHurt(){if(state!=State.DEATH){hurtTicks=12;setState(State.HURT);}}
    public void playDeath(){if(state!=State.DEATH){deathAt=System.currentTimeMillis();setState(State.DEATH);}}
    private int index(){
        switch(state){
            case WALK:int f=(int)(walkDistance/12)%4;return 4+(reverseWalk?Math.floorMod(-f,4):f);
            case JUMP:return wasGrounded?11:jumpFrame;
            case HURT:return hurtTicks>6?12:13;
            case DEATH:return System.currentTimeMillis()-deathAt<260?14:15;
            default:return (ticks/14)%4;
        }
    }
    public BufferedImage getCurrentFrame(){return frames[index()];}
    public void drawFrame(Graphics2D g,int index,int centerX,int bottom,int height,boolean right){
        BufferedImage image=frames[index];if(image==null)return;
        double scale=height/(double)standingHeight;int w=(int)Math.round(image.getWidth()*scale),h=(int)Math.round(image.getHeight()*scale);
        if(right)g.drawImage(image,centerX-w/2,bottom-h,w,h,null);else g.drawImage(image,centerX+w/2,bottom-h,-w,h,null);
    }
    public void draw(Graphics2D original,Player player,int camera){
        Graphics2D g=(Graphics2D)original.create();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int center=player.getX()+player.getWidth()/2-camera,bottom=player.getY()+player.getHeight();
        if(player.isDashing())g.rotate(player.isFacingRight()?.07:-.07,center,bottom);
        drawFrame(g,index(),center,bottom,player.getHeight()-2,player.isFacingRight());g.dispose();
    }
}

