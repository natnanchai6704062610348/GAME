import java.awt.*;
/** Boss state machine uses simulation ticks, so shop pause also pauses attacks. */
public final class MutantBoss extends Boss {
    public enum Attack { CHASE, WARN_CHARGE, CHARGE, WARN_SLAM, LEAP, SHOCK, RECOVER }
    private static final BossSpriteAnimation ART=new BossSpriteAnimation();
    private long hurtUntil,deathStarted;
    private double walkDistance;
    private Attack attack=Attack.CHASE;
    private int ticks=70, direction=-1;
    private final int worldWidth;
    private double leapStart, targetX;
    private boolean slamNext, hitDealt;
    public MutantBoss(int x,int level,int worldWidth){
        super(x,"IRON MAW",level);this.worldWidth=worldWidth;
        width=120;height=170;maxHp=550+level*90;hp=maxHp;damage=24+level;speed=1.5;
    }
    @Override public void hurt(int amount){super.hurt(amount);hurtUntil=System.currentTimeMillis()+170;}
    @Override public void startDeath(){super.startDeath();deathStarted=System.currentTimeMillis();}
    private int animationFrame(){
        if(isDying())return 12+Math.min(3,(int)((System.currentTimeMillis()-deathStarted)/180));
        if(System.currentTimeMillis()<hurtUntil)return 12;
        switch(attack){
            case WARN_CHARGE:return 4;
            case CHARGE:return 5+(34-ticks)/4%3;
            case WARN_SLAM:return 8;
            case LEAP:return 9;
            case SHOCK:return ticks>12?10:11;
            case RECOVER:return 0;
            default:return (int)(walkDistance/12)%4;
        }
    }
    public Attack getAttack(){return attack;}
    private void enter(Attack next,int duration){attack=next;ticks=duration;hitDealt=false;}
    @Override public void update(Player p){
        if(dead())return;
        switch(attack){
            case CHASE: double previous=x;pursue(p);walkDistance+=Math.abs(x-previous);break;
            case CHARGE: x+=direction*(isEnraged()?12:9);break;
            case LEAP: double t=(40-ticks+1)/40.0;x=leapStart+(targetX-leapStart)*t;break;
            default: break;
        }
        x=Math.max(0,Math.min(worldWidth-width,x));
        if(--ticks>0)return;
        switch(attack){
            case CHASE:
                direction=p.getX()>x?1:-1;
                targetX=Math.max(0,Math.min(worldWidth-width,p.getX()+p.getWidth()/2-width/2));
                enter(slamNext?Attack.WARN_SLAM:Attack.WARN_CHARGE,50);break;
            case WARN_CHARGE: enter(Attack.CHARGE,34);break;
            case CHARGE: slamNext=true;enter(Attack.RECOVER,45);break;
            case WARN_SLAM: leapStart=x;enter(Attack.LEAP,40);break;
            case LEAP: enter(Attack.SHOCK,25);break;
            case SHOCK: slamNext=false;enter(Attack.RECOVER,55);break;
            case RECOVER: enter(Attack.CHASE,isEnraged()?35:65);break;
        }
    }
    @Override public int getY(){
        if(attack==Attack.LEAP){double t=(40-ticks)/40.0;return 470-height-(int)(Math.sin(Math.PI*t)*200);}
        return 470-height;
    }
    /** Each special attack can damage the player at most once. */
    public boolean specialHits(Player p){
        if(dead()||hitDealt)return false;
        boolean hit=false;
        if(attack==Attack.CHARGE)hit=p.getX()+p.getWidth()>x && p.getX()<x+width && p.getY()+p.getHeight()>getY() && p.getY()<getY()+height;
        if(attack==Attack.SHOCK)hit=Math.abs(p.getX()+p.getWidth()/2-(x+width/2))<260 && p.getY()+p.getHeight()>=450;
        if(hit)hitDealt=true;
        return hit;
    }
    @Override public int getReward(){return 500;}
    @Override public double getDropChance(){return 1.0;}
    public void draw(Graphics2D g,int camera){draw(g,camera,direction>0);}
    public void draw(Graphics2D g,int camera,boolean facingRight){
        int sx=getX()-camera,sy=getY();
        if(attack==Attack.WARN_CHARGE){
            g.setColor(new Color(255,65,45,80));g.fillRect(direction<0?sx-430:sx+width,420,430,50);
        }
        if(attack==Attack.WARN_SLAM || attack==Attack.LEAP){
            int tx=(int)targetX+width/2-camera;
            g.setColor(new Color(255,70,40,95));g.fillOval(tx-260,444,520,26);
            g.setColor(Color.ORANGE);g.drawLine(tx-260,465,tx+260,465);
        }
        if(attack==Attack.CHARGE){Graphics2D dust=(Graphics2D)g.create();dust.setColor(new Color(195,160,110,90));for(int i=0;i<5;i++)dust.fillOval(sx+width/2-direction*(35+i*14),450-i*2,18+i*5,14+i*2);dust.dispose();}
        if(attack==Attack.SHOCK){BossSkillEffects.draw(g,1,sx+width/2-260,450,520,20,25-ticks,false,false,0,0);}
        if(!ART.draw(g,animationFrame(),sx+width/2,sy+height,facingRight)){
            g.setColor(new Color(75,115,95));g.fillRoundRect(sx,sy,width,height,20,20);
            g.setColor(Color.RED);g.fillRect(sx+30,sy+20,14,8);g.fillRect(sx+75,sy+20,14,8);
        }
        g.setColor(Color.ORANGE);g.setFont(new Font(Font.MONOSPACED,Font.BOLD,14));
        if(attack==Attack.WARN_CHARGE)g.drawString("CHARGE!",sx+18,sy-24);
        if(attack==Attack.WARN_SLAM)g.drawString("JUMP TO DODGE!",sx-10,sy-24);
    }
}






