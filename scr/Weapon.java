public class Weapon {
    public final String name; public final int damage,delay,magazine,reloadMs,pellets;
    private int upgradeLevel;
    private int ammo;private long lastShot=0,reloadUntil=0;
    public Weapon(String name,int damage,int delay,int magazine,int reloadMs,int pellets){
        this.name=name;this.damage=damage;this.delay=delay;this.magazine=magazine;this.reloadMs=reloadMs;this.pellets=pellets;ammo=magazine;
    }
    public boolean shoot(long now){if(reloadUntil>0 || ammo<=0 || now-lastShot<delay)return false;ammo--;lastShot=now;return true;}
    public void reload(long now){if(ammo<magazine && reloadUntil==0)reloadUntil=now+reloadMs;}
    public void update(long now){if(reloadUntil>0 && now>=reloadUntil){ammo=magazine;reloadUntil=0;}}
    public boolean isReloading(){return reloadUntil>0;}
    public int getDamage(){return damage + damage*upgradeLevel/4;}
    public int getUpgradeLevel(){return upgradeLevel;}
    public boolean upgrade(){if(upgradeLevel>=3)return false;upgradeLevel++;return true;}
    public int ammo(){return ammo;}
    
    public void refillAmmo() {
        ammo = magazine;
        reloadUntil = 0;
    }

    public static Weapon[] create(){return new Weapon[]{
        new Weapon("PISTOL",20,320,14,1050,1),
        new Weapon("SMG",11,90,40,1400,1),
        new Weapon("SHOTGUN",16,680,8,1900,5),
        new Weapon("ASSAULT RIFLE",24,165,30,1600,1),
        new Weapon("SNIPER",115,1050,5,2200,1)
    };}
}
