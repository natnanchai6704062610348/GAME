import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
/** Loads the generated atlas once and extracts transparent animation frames. */
public final class BossSpriteAnimation {
    private final BufferedImage[] frames=new BufferedImage[16];
    public BossSpriteAnimation(){
        String[] paths={"images/BOSS/iron-maw-sprites-v1.png","../images/BOSS/iron-maw-sprites-v1.png"};
        for(String path:paths){try{File f=new File(path);if(!f.isFile())continue;load(ImageIO.read(f));return;}catch(Exception e){System.err.println("Boss atlas: "+e.getMessage());}}
    }
    public BossSpriteAnimation(BufferedImage image){load(image);}
    private void load(BufferedImage image){
        // Row boundaries follow the generated artwork's transparent gutters.
        double[] rows={0,320.0/1254,594.0/1254,936.0/1254,1};
        for(int row=0;row<4;row++)for(int col=0;col<4;col++){
            int left=col*image.getWidth()/4,right=(col+1)*image.getWidth()/4;
            int top=(int)(rows[row]*image.getHeight()),bottom=(int)(rows[row+1]*image.getHeight());
            BufferedImage cell=image.getSubimage(left,top,right-left,bottom-top);
            int minX=cell.getWidth(),minY=cell.getHeight(),maxX=-1,maxY=-1;
            for(int y=0;y<cell.getHeight();y++)for(int x=0;x<cell.getWidth();x++)if((cell.getRGB(x,y)>>>24)>24){minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);}
            if(maxX>=minX)frames[row*4+col]=cell.getSubimage(minX,minY,maxX-minX+1,maxY-minY+1);
        }
    }
    public boolean isLoaded(){for(BufferedImage frame:frames)if(frame==null)return false;return true;}
    public boolean draw(Graphics2D g,int index,int centerX,int feetY,boolean facingRight){
        BufferedImage frame=frames[Math.max(0,Math.min(15,index))];if(frame==null)return false;
        int w=(int)(frame.getWidth()*.61),h=(int)(frame.getHeight()*.61);
        Graphics2D copy=(Graphics2D)g.create();copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        if(facingRight)copy.drawImage(frame,centerX+w/2,feetY-h,-w,h,null);
        else copy.drawImage(frame,centerX-w/2,feetY-h,w,h,null);
        copy.dispose();return true;
    }
}
