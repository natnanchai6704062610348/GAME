import java.awt.Graphics2D;
/** Abstract domain class: all live campaign bosses must implement these behaviors. */
public abstract class Boss extends Zombie implements BossCombat {
    protected Boss(int x,String name,int level){super(x,name,level);}
    public final boolean isEnraged(){return hp<maxHp/2;}
    protected final void pursue(Player player){super.update(player);}
    @Override public abstract void update(Player player);
    @Override public abstract boolean specialHits(Player player);
    @Override public abstract void draw(Graphics2D g,int camera,boolean facingRight);
}
