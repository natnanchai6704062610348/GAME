public final class BossFactory {
    private static final String[] NAMES={"IRON MAW","FURNACE BUTCHER","STORM EEL","THORN STALKER","ROADBREAKER","THE SURGEON","PLAGUE BELLY","WAR COMMANDER","MAGMA TITAN","HIVE QUEEN"};
    public static String name(int level){return NAMES[Math.max(1,Math.min(10,level))-1];}
    public static Boss create(int x,int level,int worldWidth){return level==1?new MutantBoss(x,1,worldWidth):new StageBoss(x,level,worldWidth);}
}

