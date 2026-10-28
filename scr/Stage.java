import java.awt.Color;
public class Stage {
    public final String name,feature; public final Color sky,accent;
    public final int level,quota,worldWidth,hazardType;
    private Stage(int i,String n,String f,Color s,Color a,int hazard){
        level=i;name=n;feature=f;sky=s;accent=a;hazardType=hazard;quota=22+i*4;worldWidth=6000+i*500;
    }
    public int getEnemyLimit(){return 5+level/2;}
    public int getSpawnInterval(){return Math.max(430,950-level*45);}
    public String getBossName(){return BossFactory.name(level);}
    public String getBackgroundPath(){

        return "images/BACKGROUND/stage"
                + level
                + ".png";
    }
    /** Successive ledges rise 90 pixels; the existing jump reaches about 125. */
    public java.util.List<Platform> getPlatforms() {
        java.util.List<Platform> result = new java.util.ArrayList<>();
        for (int base = 300; base + 650 < worldWidth - 170; base += 1000) {
            result.add(new Platform(base, 380, 220));
            int group=(base-300)/1000;
            result.add(new MovingPlatform(base+180,290,220,group%2==0?28:18,group%2!=0));
            result.add(group%2==0?new CrumblingPlatform(base+360,200,290):new Platform(base+360,200,290));
        }
        return java.util.Collections.unmodifiableList(result);
    }
    public java.util.List<StageTrap> getTraps(){
        java.util.List<StageTrap> result=new java.util.ArrayList<>();
        StageTrap.Type[] types=StageTrap.Type.values();int index=0;
        for(int x=1050;x<worldWidth-1300;x+=760){
            result.add(new StageTrap(x,types[(index+level-1)%types.length],level,index*47));index++;
        }
        return result;
    }
    public java.util.List<SupplyPickup> getSupplies() {
        java.util.List<SupplyPickup> result = new java.util.ArrayList<>();
        SupplyPickup.Type[] types = SupplyPickup.Type.values();
        int i=0;
        for(int x=750;x<worldWidth-350;x+=650)
            result.add(new SupplyPickup(x,440,types[(i++)%types.length]));
        int ledge=0;
        for(Platform p:getPlatforms()) {
            if((ledge++ % 3)==2)
                result.add(new SupplyPickup(p.getX()+p.getWidth()/2,p.getY()-24,SupplyPickup.Type.CREDITS));
        }
        return result;
    }
    public static Stage get(int i){
        String[] names={"ABANDONED CITY","BURNING FACTORY","FLOODED SUBWAY","FOGGY FOREST","BROKEN HIGHWAY","ABANDONED HOSPITAL","TOXIC SEWERS","MILITARY BASE","VOLCANIC ZONE","ZOMBIE NEST"};
        String[] features={"ruined towers","fire vents","electric pools","thick fog","falling debris","infected halls","toxic gas","turret zone","lava cracks","mutant lair"};
        Color[] skies={new Color(27,40,58),new Color(78,29,25),new Color(16,48,60),new Color(17,42,39),new Color(65,45,39),new Color(38,44,57),new Color(25,53,29),new Color(30,40,47),new Color(93,31,22),new Color(59,15,37)};
        Color[] accents={new Color(76,101,116),new Color(230,93,41),new Color(50,189,207),new Color(75,130,97),new Color(152,118,88),new Color(94,151,159),new Color(105,216,66),new Color(117,143,149),new Color(251,94,34),new Color(221,59,128)};
        i=Math.max(1,Math.min(10,i));return new Stage(i,names[i-1],features[i-1],skies[i-1],accents[i-1],i);
    }
}



