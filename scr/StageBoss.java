import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/** Distinct attack patterns per stage, with shared telegraph/recovery lifecycle. */
public final class StageBoss extends Boss {
    public enum Phase { HUNT, WARNING, ATTACK, RECOVER }
    private static final StageBossArt ART=new StageBossArt();
    private static final BossWalkAtlas WALK=new BossWalkAtlas();
    private double walkDistance;
    private final int level,worldWidth;
    private final List<BossHazard> hazards=new ArrayList<>();
    private final Color color;
    private Phase phase=Phase.HUNT;
    private int ticks=85,damageCooldown,attackCount,summons,leapTicks;
    private double targetX,targetY,leapStartX,leapEndX,leapHeight;
    private long deathAt;
    public StageBoss(int x,int level,int worldWidth){
        super(x,BossFactory.name(level),level);this.level=level;this.worldWidth=worldWidth;
        width=level==9?150:120;height=level==10?205:180;
        maxHp=580+level*115;hp=maxHp;damage=18+level*2;speed=1.1+level*.1;
        Color[] colors={Color.ORANGE,Color.CYAN,new Color(155,215,90),new Color(255,190,80),new Color(230,90,160),new Color(140,230,55),new Color(255,100,90),new Color(255,100,20),new Color(205,110,255)};
        color=colors[level-2];
    }
    public Phase getPhase(){return phase;}
    public int getHazardCount(){return hazards.size();}
    public int takeSummons(){int n=summons;summons=0;return n;}
    @Override public int getReward(){return 350+level*100;}
    @Override public double getDropChance(){return 1;}
    @Override public void startDeath(){super.startDeath();deathAt=System.currentTimeMillis();hazards.clear();}
    @Override public int getY(){return 470-height-(leapTicks>0?(int)(Math.sin(Math.PI*(40-leapTicks)/40.0)*leapHeight):0);}
    @Override public void update(Player p){
        if(dead()){hazards.clear();return;}
        if(damageCooldown>0)damageCooldown--;
        for(BossHazard h:hazards)h.update();hazards.removeIf(BossHazard::expired);
        if(leapTicks>0){double t=(41-leapTicks)/40.0;x=leapStartX+(leapEndX-leapStartX)*t;leapTicks--;}
        double beforeX=x;
        if(phase==Phase.HUNT)pursue(p);
        walkDistance+=Math.abs(x-beforeX);
        x=Math.max(0,Math.min(worldWidth-width,x));
        if(--ticks>0)return;
        switch(phase){
            case HUNT:
                targetX=p.getX()+p.getWidth()/2.0;targetY=p.getY()+p.getHeight()/2.0;
                phase=Phase.WARNING;ticks=isEnraged()?38:52;prepare();break;
            case WARNING: phase=Phase.ATTACK;ticks=level==7?100:60;attack();break;
            case ATTACK:phase=Phase.RECOVER;ticks=isEnraged()?35:55;break;
            case RECOVER:phase=Phase.HUNT;ticks=isEnraged()?35:65;break;
        }
    }
    private int warningTicks(){return isEnraged()?38:52;}
    private void zone(double x,double y,int w,int h,int delay,int life){hazards.add(new BossHazard(x,y,w,h,delay,life,color).withSkill(level));}
    private void prepare(){
        int warn=warningTicks();
        switch(level){
            case 2:zone(targetX<x?x-360:x+width,370,360,100,warn,40);break;
            case 3:for(int i=-1;i<=1;i++)zone(targetX+i*170-27,0,54,470,warn,20);break;
            case 4:zone(targetX-65,440,130,30,warn+40,18);break;
            case 6:zone(x+width/2-220,100,440,370,warn,32);break;
            case 7:for(int i=-1;i<=1;i++)zone(targetX+i*130-45,targetY+35,90,28,warn+i*6+6,160);break;
            case 9:for(int i=-2;i<=2;i++)zone(targetX+i*140-40,145,80,325,warn+(i+2)*13,50);break;
            default:break;
        }
    }
    private void shoot(double dx,double dy,double velocity,int radius,double gravity){
        double length=Math.max(1,Math.hypot(dx,dy));BossHazard h=new BossHazard(x+width/2,getY()+55,radius*2,radius*2,0,160,color,true).withSkill(level);h.vx=dx/length*velocity;h.vy=dy/length*velocity;h.gravity=gravity;hazards.add(h);
    }
    private void attack(){
        attackCount++;
        switch(level){
            case 4:leapStartX=x;leapEndX=Math.max(0,Math.min(worldWidth-width,targetX-width/2));leapHeight=Math.min(300,Math.max(150,470-(targetY+30)));leapTicks=40;break;
            case 5:for(int i=-2;i<=2;i++){
                BossHazard h=new BossHazard(x+width/2,getY()+30,28,28,0,110,color,true).withSkill(level);
                h.vx=(targetX+i*65-h.x)/50.0;h.vy=-8-i*.6;h.gravity=.34;hazards.add(h);
            }break;
            case 8:for(int i=-2;i<=2;i++)shoot(targetX-(x+width/2),targetY-(getY()+55)+i*55,8+level*.2,7,0);break;
            case 10:for(int i=-1;i<=1;i++)shoot(targetX-(x+width/2),targetY-(getY()+55)+i*100,7,11,0);summons=isEnraged()?3:2;break;
            default:break;
        }
    }
    @Override public boolean specialHits(Player p){
        if(dead()||damageCooldown>0)return false;
        boolean hit=p.getX()+p.getWidth()>x&&p.getX()<x+width&&p.getY()+p.getHeight()>getY()&&p.getY()<getY()+height;
        for(BossHazard h:hazards)if(h.touches(p)){hit=true;h.consume();break;}
        if(hit){damageCooldown=38;if(level==6)hp=Math.min(maxHp,hp+45);}
        return hit;
    }
    @Override public void draw(Graphics2D g,int camera,boolean right){
        for(BossHazard h:hazards)h.draw(g,camera);
        if(phase==Phase.WARNING && (level==5 || level==8 || level==10)){
            g.setColor(new Color(255,190,75,150));g.drawLine(getX()+width/2-camera,getY()+55,(int)targetX-camera,(int)targetY);
            g.drawOval((int)targetX-camera-20,(int)targetY-20,40,40);
        }
        Graphics2D art=(Graphics2D)g.create();int drawHeight=height+15;
        if(isDying()){
            float alpha=Math.max(0,1-(System.currentTimeMillis()-deathAt)/750f);art.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
        }
        int bob=0;
        int frame=phase==Phase.HUNT?(int)(walkDistance/12)%4:0;
        if(phase==Phase.WARNING){double angle=(right?1:-1)*Math.sin((52-ticks)*.05)*.06;art.rotate(angle,getX()+width/2-camera,getY()+height);}
        if(!WALK.draw(art,level,frame,getX()+width/2-camera,getY()+height+bob,drawHeight,right) && !ART.draw(art,level,getX()+width/2-camera,getY()+height+bob,drawHeight,right)){
            art.setColor(color);art.fillRoundRect(getX()-camera,getY(),width,height,18,18);
        }
        art.dispose();
        if(phase==Phase.WARNING){g.setFont(new Font(Font.MONOSPACED,Font.BOLD,15));g.setColor(color);g.drawString(attackLabel(),getX()-camera-20,getY()-22);}
    }
    public String attackLabel(){
        switch(level){case 2:return "FLAME SWEEP!";case 3:return "LIGHTNING! MOVE!";case 4:return "POUNCE!";case 5:return "ROCK VOLLEY!";case 6:return "DRAIN! KEEP AWAY!";case 7:return "TOXIC POOLS!";case 8:return "BULLET FAN!";case 9:return "MAGMA FISSURES!";default:return "SWARM + ACID!";}
    }
}



