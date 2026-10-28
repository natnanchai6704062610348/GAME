/** Encapsulated charge state; shield absorption fuels a single-use radial skill. */
public final class MutationArmor {
    public static final int MAX_CHARGE=100,RADIUS=210,DAMAGE=120;
    private int charge,releaseTicks,animationTicks;
    public void update(){animationTicks++;if(releaseTicks>0)releaseTicks--;}
    public void absorb(int blockedDamage,int remainingArmor){
        if(remainingArmor<=0){charge=0;return;}
        if(blockedDamage>0)charge=(int)Math.min(MAX_CHARGE,(long)charge+(long)blockedDamage*3);
    }
    public int getCharge(){return charge;}
    public boolean isReady(){return charge==MAX_CHARGE;}
    public boolean release(){if(!isReady())return false;charge=0;releaseTicks=24;return true;}
    public int getReleaseTicks(){return releaseTicks;}
    public int getAnimationTicks(){return animationTicks;}
    public int getVisualStage(){return releaseTicks>0 || charge==100?2:charge>=40?1:0;}
}
