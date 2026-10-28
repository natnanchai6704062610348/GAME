import java.awt.*;
public final class CrumblingPlatform extends Platform {
    private int collapse=-1,respawn;
    public CrumblingPlatform(int x,int y,int width){super(x,y,width);}
    @Override public void update(){super.update();if(respawn>0){respawn--;return;}if(collapse>0&&--collapse==0){respawn=180;collapse=-1;}}
    @Override public boolean isSolid(){return respawn==0;}
    @Override public void onLand(){if(isSolid()&&collapse<0)collapse=48;}
    public int getCollapseTicks(){return collapse;}
    @Override public void draw(Graphics2D g,int camera,Color accent){
        Graphics2D copy=(Graphics2D)g.create();
        if(!isSolid()){copy.setColor(new Color(190,145,85,80));copy.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10,new float[]{4,6},0));copy.drawRect(x-camera,y,width,18);copy.dispose();return;}
        super.draw(copy,camera,new Color(230,170,85));
        copy.setColor(new Color(255,195,100));copy.setStroke(new BasicStroke(2));
        for(int i=30;i<width;i+=46){int sx=x-camera+i;copy.drawPolyline(new int[]{sx,sx+8,sx+2,sx+11},new int[]{y,y+5,y+10,y+18},4);}
        copy.dispose();
    }
}


