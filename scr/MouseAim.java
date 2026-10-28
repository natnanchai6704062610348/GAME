/** Mouse coordinates remain in screen space and are converted using the current camera. */
public final class MouseAim {
    private int screenX,screenY;
    private boolean active;
    public void move(int x,int y){screenX=x;screenY=y;active=true;}
    public boolean isActive(){return active;}
    public int getScreenX(){return screenX;}
    public int getScreenY(){return screenY;}
    public double worldX(int camera){return screenX+camera;}
    public double angle(double handX,double handY,int camera,boolean right){return active?Math.atan2(screenY-handY,worldX(camera)-handX):(right?0:Math.PI);}
}
