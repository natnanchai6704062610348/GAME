import java.awt.*;
/** Deterministic simulation clock: idle -> warning -> active, paused by the shop. */
public final class StageTrap {
    public enum Type { SPIKES, FLAME, ELECTRIC, RUBBLE }
    public enum Phase { SAFE, WARNING, ACTIVE }
    private final int x,width=86,level,idle,warning,active;
    private final Type type;
    private int tick,damageCooldown;
    public StageTrap(int x,Type type,int level,int offset){this.x=x;this.type=type;this.level=level;idle=160-level*5;warning=58-level;active=40+level*2;tick=Math.floorMod(offset,idle+warning+active);}
    public int getX(){return x;}
    public Type getType(){return type;}
    public int getTick(){return tick;}
    public Phase getPhase(){int t=tick%(idle+warning+active);return t<idle?Phase.SAFE:t<idle+warning?Phase.WARNING:Phase.ACTIVE;}
    public void update(){tick++;if(damageCooldown>0)damageCooldown--;}
    private int activeAge(){return tick%(idle+warning+active)-idle-warning;}
    private Rectangle hitbox(){
        switch(type){
            case FLAME:return new Rectangle(x,360,width,110);
            case ELECTRIC:return new Rectangle(x,442,width,28);
            case RUBBLE:return new Rectangle(x+width/2-19,20+activeAge()*11,38,38);
            default:return new Rectangle(x,442,width,28);
        }
    }
    public boolean tryHit(Player p){
        if(getPhase()!=Phase.ACTIVE || damageCooldown>0)return false;
        if(hitbox().intersects(p.getX()+8,p.getY()+8,p.getWidth()-16,p.getHeight()-8)){
            damageCooldown=48;int before=p.getHp()+p.getArmor();p.hurt(8+level);return before>p.getHp()+p.getArmor();
        }return false;
    }
    public void draw(Graphics2D g,int camera){
        int sx=x-camera;Rectangle clip=g.getClipBounds();if(clip!=null && (sx<clip.x-150||sx>clip.x+clip.width+180))return;
        Graphics2D copy=(Graphics2D)g.create();Phase phase=getPhase();
        Color color=type==Type.FLAME?Color.ORANGE:type==Type.ELECTRIC?Color.CYAN:type==Type.RUBBLE?new Color(215,185,135):new Color(230,140,100);
        copy.setColor(new Color(17,23,29));copy.fillRoundRect(sx-4,461,width+8,13,4,4);copy.setColor(new Color(90,100,110));copy.drawRect(sx,461,width,10);
        for(int i=0;i<width;i+=18){copy.setColor(phase==Phase.ACTIVE?color:new Color(85,110,100));copy.fillRect(sx+i,465,9,4);}
        if(phase==Phase.ACTIVE){
            Rectangle r=hitbox();
            if(type==Type.FLAME)BossSkillEffects.draw(copy,2,sx,r.y,width,r.height,tick,false,false,0,0);
            else if(type==Type.ELECTRIC)BossSkillEffects.draw(copy,3,sx,r.y,width,r.height,tick,false,false,0,0);
            else if(type==Type.RUBBLE){
                copy.setColor(new Color(110,90,70));copy.fillPolygon(new int[]{sx+24,sx+35,sx+61,sx+57,sx+30},new int[]{r.y+10,r.y,r.y+10,r.y+36,r.y+38},5);copy.setColor(color);copy.drawLine(sx+34,r.y+8,sx+45,r.y+26);
                if(r.y>430)BossSkillEffects.impact(copy,sx+width/2,462,20+activeAge()%12,color,9);
            }else{copy.setColor(new Color(210,215,220));for(int i=0;i<width-10;i+=14)copy.fillPolygon(new int[]{sx+i,sx+i+6,sx+i+13},new int[]{470,442,470},3);}
        }
        copy.dispose();
    }
}


