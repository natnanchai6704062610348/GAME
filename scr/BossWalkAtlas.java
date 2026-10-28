import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
/** Four drawn walk poses per boss; frame changes follow actual distance travelled. */
public final class BossWalkAtlas {
    private final BufferedImage[][] frames=new BufferedImage[9][4];
    private final int[] maxHeights=new int[9];
    public BossWalkAtlas(){
        for(int group=0;group<3;group++){
            String file="boss-walk-"+(char)('a'+group)+".png";
            for(String root:new String[]{"images/BOSS/","../images/BOSS/"})try{
                File path=new File(root+file);if(path.isFile()){load(ImageIO.read(path),group);break;}
            }catch(Exception e){System.err.println("Boss walk: "+e.getMessage());}
        }
    }
    private void load(BufferedImage image,int group){
        for(int row=0;row<3;row++)for(int col=0;col<4;col++){
            int left=col*image.getWidth()/4,top=row*image.getHeight()/3;
            BufferedImage cell=mainComponent(image.getSubimage(left,top,(col+1)*image.getWidth()/4-left,(row+1)*image.getHeight()/3-top));
            int minX=cell.getWidth(),minY=cell.getHeight(),maxX=-1,maxY=-1;
            for(int y=0;y<cell.getHeight();y++)for(int x=0;x<cell.getWidth();x++)if((cell.getRGB(x,y)>>>24)>40){minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);}
            if(maxX>=minX){int boss=group*3+row;frames[boss][col]=cell.getSubimage(minX,minY,maxX-minX+1,maxY-minY+1);maxHeights[boss]=Math.max(maxHeights[boss],maxY-minY+1);}
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
    public boolean loaded(int level){for(BufferedImage image:frames[level-2])if(image==null)return false;return true;}
    public boolean draw(Graphics2D g,int level,int frame,int cx,int bottom,int height,boolean right){
        if(!loaded(level))return false;BufferedImage f=frames[level-2][Math.floorMod(frame,4)];
        double scale=height/(double)maxHeights[level-2];int w=(int)(f.getWidth()*scale),h=(int)(f.getHeight()*scale);
        if(right)g.drawImage(f,cx+w/2,bottom-h,-w,h,null);else g.drawImage(f,cx-w/2,bottom-h,w,h,null);return true;
    }
}

