import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
public final class StageBossArt {
    private final BufferedImage[] frames=new BufferedImage[9];
    public StageBossArt(){
        for(String path:new String[]{"images/BOSS/stage-bosses-v1.png","../images/BOSS/stage-bosses-v1.png"})try{
            File f=new File(path);if(!f.isFile())continue;load(ImageIO.read(f));return;
        }catch(Exception e){System.err.println("Stage boss art: "+e.getMessage());}
    }
    public StageBossArt(BufferedImage sheet){load(sheet);}
    private void load(BufferedImage image){
        for(int row=0;row<3;row++)for(int col=0;col<3;col++){
            double[] rows={0,402.0/1254,802.0/1254,1};
            double[] cols=row==2?new double[]{0,420.0/1254,861.0/1254,1}:new double[]{0,455.0/1254,861.0/1254,1};
            int left=(int)(cols[col]*image.getWidth()),top=(int)(rows[row]*image.getHeight());
            BufferedImage cell=mainComponent(image.getSubimage(left,top,(int)(cols[col+1]*image.getWidth())-left,(int)(rows[row+1]*image.getHeight())-top));
            int minX=cell.getWidth(),minY=cell.getHeight(),maxX=-1,maxY=-1;
            for(int y=0;y<cell.getHeight();y++)for(int x=0;x<cell.getWidth();x++)if((cell.getRGB(x,y)>>>24)>40){minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);}
            if(maxX>=minX)frames[row*3+col]=cell.getSubimage(minX,minY,maxX-minX+1,maxY-minY+1);
        }
    }
    /** Ignore detached pixels belonging to neighboring atlas cells at the gutters. */
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
    public boolean isLoaded(){for(BufferedImage f:frames)if(f==null)return false;return true;}
    public boolean draw(Graphics2D g,int level,int cx,int bottom,int height,boolean right){
        BufferedImage f=frames[level-2];if(f==null)return false;
        int w=f.getWidth()*height/f.getHeight();
        if(right)g.drawImage(f,cx+w/2,bottom-height,-w,height,null);else g.drawImage(f,cx-w/2,bottom-height,w,height,null);
        return true;
    }
}

