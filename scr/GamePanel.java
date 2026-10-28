import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.File;


public class GamePanel extends JPanel {
    
    private final boolean bossPractice;
    private final MouseAim mouseAim=new MouseAim();
    private double shotX,shotY,shotAngle;
    private static final int[] GUN_WIDTHS={38,52,65,62,78};
    private static final int[] GUN_HEIGHTS={25,29,26,29,28};
    private int currentStage;
    private final JFrame frame;
    private BufferedImage background;
    private final Stage stage;
    private final List<Platform> platforms;
    private final List<StageTrap> traps;
    private final List<SupplyPickup> supplies;
    private final List<ExplosiveBarrel> barrels=new ArrayList<>();
    private final Set<Integer> heldKeys=new HashSet<>();
    private boolean shopOpen;
    private static final int[] GUN_PRICES={0,200,300,450,600};
    private final Player player=new Player(100,350);
    private final PlayerAnimation playerAnimation=new PlayerAnimation();
    private final List<Bullet> bullets=new ArrayList<>();
    private final List<Zombie> zombies=new ArrayList<>();
    private final Map<Zombie, ZombieAnimation> zombieAnimations =new IdentityHashMap<>();
    private final List<Zombie> dyingZombies = new ArrayList<>();
    private final List<WeaponDrop> weaponDrops=new ArrayList<>();

    // Pistol unlocked at the start; other guns are collected from enemy drops.
    private final boolean[] unlockedWeapons={true,false,false,false,false};
    private final Weapon[] weapons=Weapon.create();
    private final Random rng=new Random();
    private BufferedImage playerImage,zombieImage;
    private final BufferedImage[] weaponImages=new BufferedImage[5];
    private javax.swing.Timer timer;
    private boolean left,right,jump,fire,won,lost,finished,bossSpawned;
    private int selected=0,spawned=0,killed=0,credits=0,camera=0;
    private long lastSpawn=0,lastFire=0;
    private String banner="";
    private long bannerUntil=0;
    private static final int GROUND=470;
    private long lastFireEffect = 0;
    private MuzzleFlashEffect muzzleFlash = new MuzzleFlashEffect();
    private void loadBackground(int stage){

        try{

            File file = new File(
                "images/BG" + stage + ".png"
            );

            System.out.println(
                "Loading : " + file.getAbsolutePath()
            );

            System.out.println(
                "Exists : " + file.exists()
            );

            background = loadSprite("BG" + stage + ".png");
            if (background == null) background = loadSprite("BG1.png" );
            System.out.println(
                "BG Size : " 
                + background.getWidth() 
                + " x " 
                + background.getHeight()
            );

            System.out.println("BG Loaded");


        }catch(Exception e){

            System.out.println(
                "BG ERROR : " + e.getMessage()
            );

        }
    }
    public GamePanel(JFrame frame,int level){this(frame,level,false);}
    public GamePanel(JFrame frame,int level,boolean bossPractice){
        this.bossPractice=bossPractice;

        weaponImages[0] = loadSprite("GUN/gun1.png");

        weaponImages[1] = loadSprite("GUN/gun2.png");

        weaponImages[2] = loadSprite("GUN/gun3.png");

        weaponImages[3] = loadSprite("GUN/gun4.png");

        weaponImages[4] = loadSprite("GUN/gun5.png");

        this.frame=frame;stage=Stage.get(level);platforms=stage.getPlatforms();traps=bossPractice?Collections.emptyList():stage.getTraps();supplies=stage.getSupplies();setFocusable(true);setBackground(stage.sky);
        for(int x=900;x<stage.worldWidth-300;x+=850)barrels.add(new ExplosiveBarrel(x));
        currentStage = level;
        loadBackground(level);
        System.out.println(
            "Current Stage = " + level
        );
        playerImage=loadSprite("clean_player_idle.png");

        zombieImage=loadSprite("clean_Zombie.png");



        for (int i = 0; i < weaponImages.length; i++) {
            System.out.println("GUN " + (i + 1) + ": " +
                (weaponImages[i] == null ? "FAILED" : "LOADED"));
        }

        MouseAdapter mouse=new MouseAdapter(){
            private void aim(MouseEvent e){if(!shopOpen && !finished){mouseAim.move(e.getX(),e.getY());player.faceTowards(mouseAim.worldX(camera));}}
            @Override public void mouseMoved(MouseEvent e){aim(e);}
            @Override public void mouseDragged(MouseEvent e){aim(e);}
            @Override public void mousePressed(MouseEvent e){aim(e);if(SwingUtilities.isLeftMouseButton(e) && !shopOpen && !finished)fire=true;}
            @Override public void mouseReleased(MouseEvent e){if(SwingUtilities.isLeftMouseButton(e))fire=false;}
        };
        addMouseListener(mouse);addMouseMotionListener(mouse);
        if(bossPractice){zombies.add(BossFactory.create(650,level,stage.worldWidth));bossSpawned=true;message("BOSS PRACTICE: "+stage.getBossName());}
        timer=new javax.swing.Timer(16,e->tick());timer.start();

    }

    private BufferedImage loadSprite(String name){

        // Works when launched from VS Code, terminal or run_windows.bat.

        String[] roots={"images/", "GUN/", "images/GUN/", "ProjectGameOOP/images/", "ProjectGameOOP/GUN/", "../images/", "../GUN/"};

        for(String root:roots){

            try{File f=new File(root+name);if(f.isFile())return ImageIO.read(f);}catch(Exception ignored){}

        }

        java.net.URL url=getClass().getResource("/images/"+name);

        try{if(url!=null)return ImageIO.read(url);}catch(Exception ignored){}

        System.err.println("Missing sprite: "+name);

        return null;

    }

    public void handleKey(int code,boolean pressed){
        boolean fresh=pressed && heldKeys.add(code);
        if(!pressed)heldKeys.remove(code);
        if(finished && code!=KeyEvent.VK_ENTER && code!=KeyEvent.VK_ESCAPE)return;
        if(code==KeyEvent.VK_Q && fresh && !finished){
            shopOpen=!shopOpen;left=right=jump=fire=false;repaint();return;
        }
        if(shopOpen){
            if(code==KeyEvent.VK_ESCAPE && fresh){shopOpen=false;repaint();return;}
            if(fresh && code>=KeyEvent.VK_1 && code<=KeyEvent.VK_5)buyGun(code-KeyEvent.VK_1);
            if(fresh && code==KeyEvent.VK_U)buyUpgrade();
            if(fresh && code==KeyEvent.VK_F){if(credits>=75 && (weapons[selected].ammo()<weapons[selected].magazine || weapons[selected].isReloading())){credits-=75;weapons[selected].refillAmmo();message("AMMO REFILLED");}}
            repaint();return;
        }
        if(code==KeyEvent.VK_SHIFT && fresh)player.dash();

        if(code==KeyEvent.VK_A || code==KeyEvent.VK_LEFT)left=pressed;

        if(code==KeyEvent.VK_D || code==KeyEvent.VK_RIGHT)right=pressed;

        if(code==KeyEvent.VK_SPACE)jump=pressed;

        if(code==KeyEvent.VK_J)fire=pressed;

        if(!pressed || !fresh)return;

        if(code>=KeyEvent.VK_1 && code<=KeyEvent.VK_5){

            int requested=code-KeyEvent.VK_1;

            if(unlockedWeapons[requested])selected=requested;

            else message(weapons[requested].name+" LOCKED - FIND A DROP!");

        }

        if(code==KeyEvent.VK_F)releaseMutation();
        if(code==KeyEvent.VK_E)pickUpNearbyWeapon();

        if(code==KeyEvent.VK_R)weapons[selected].reload(System.currentTimeMillis());

        if(code==KeyEvent.VK_H && credits>=100){credits-=100;player.heal(35);message("+35 HEALTH");}

        if(code==KeyEvent.VK_B && credits>=100){credits-=100;player.addArmor(30);message("+30 ARMOR");}

        if(code==KeyEvent.VK_ESCAPE){timer.stop();frame.setContentPane(new LevelPanel(frame));frame.revalidate();frame.repaint();}

        if(code==KeyEvent.VK_ENTER && finished){timer.stop();frame.setContentPane(new LevelPanel(frame));frame.revalidate();frame.repaint();}

    }

    private void releaseMutation(){
        if(!player.releaseMutation()){message("MUTATION NOT READY");return;}
        double cx=player.getX()+player.getWidth()/2.0,cy=player.getY()+player.getHeight()/2.0;
        for(Zombie z:zombies){
            double closestX=Math.max(z.getX(),Math.min(cx,z.getX()+z.getWidth())),closestY=Math.max(z.getY(),Math.min(cy,z.getY()+z.getHeight()));
            if(!z.dead() && Math.hypot(closestX-cx,closestY-cy)<=MutationArmor.RADIUS){
                z.hurt(MutationArmor.DAMAGE);
                if(!(z instanceof Boss))z.knockBackFrom(cx,110,stage.worldWidth);
                zombieAnimations.computeIfAbsent(z,key->new ZombieAnimation()).playHurt();
            }
        }
        message("BIO BURST!");
    }
    private void buyGun(int index){
        if(unlockedWeapons[index]){selected=index;message("EQUIPPED "+weapons[index].name);return;}
        if(credits<GUN_PRICES[index]){message("NOT ENOUGH CREDITS");return;}
        credits-=GUN_PRICES[index];unlockedWeapons[index]=true;selected=index;message("BOUGHT "+weapons[index].name);
    }
    private void buyUpgrade(){
        Weapon w=weapons[selected];int cost=150*(w.getUpgradeLevel()+1);
        if(w.getUpgradeLevel()>=3){message("MAX UPGRADE");return;}
        if(credits<cost){message("NOT ENOUGH CREDITS");return;}
        if(w.upgrade()){credits-=cost;message("DAMAGE UPGRADED");}
    }
    private void detonate(ExplosiveBarrel first,long now){
        ArrayDeque<ExplosiveBarrel> pending=new ArrayDeque<>();pending.add(first);
        while(!pending.isEmpty()){
            ExplosiveBarrel barrel=pending.remove();if(!barrel.explode(now))continue;
            for(Zombie z:zombies)if(!z.dead() && barrel.inBlast(z.getX()+z.getWidth()/2,z.getY()+z.getHeight()/2))z.hurt(180);
            if(barrel.inBlast(player.getX()+player.getWidth()/2,player.getY()+player.getHeight()/2))player.hurt(30);
            for(ExplosiveBarrel other:barrels)if(!other.isExploded() && barrel.inBlast(other.centerX(),430))pending.add(other);
        }
    }
    private void drawShop(Graphics2D g){
        int x=getWidth()/2-310,y=120;
        g.setColor(new Color(0,0,0,210));g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(new Color(20,28,38));g.fillRoundRect(x,y,620,365,16,16);
        g.setColor(new Color(255,205,95));g.setFont(new Font(Font.MONOSPACED,Font.BOLD,24));g.drawString("SUPPLY SHOP   $"+credits,x+25,y+38);
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,18));
        for(int i=0;i<weapons.length;i++){
            g.setColor(i==selected?Color.CYAN:Color.WHITE);
            g.drawString((i+1)+"  "+weapons[i].name+"  "+(unlockedWeapons[i]?"OWNED":("$"+GUN_PRICES[i])),x+25,y+78+i*31);
        }
        Weapon w=weapons[selected];g.setColor(Color.YELLOW);
        g.drawString("U  Damage +25% base  "+(w.getUpgradeLevel()>=3?"MAX":"$"+(150*(w.getUpgradeLevel()+1))),x+25,y+246);
        g.setColor(Color.WHITE);g.drawString("F  Refill current gun  $75",x+25,y+278);
        g.drawString("Q / ESC: close | World paused",x+25,y+313);
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
        g.drawString(w.name+" DMG "+w.getDamage()+"  UPGRADE "+w.getUpgradeLevel()+"/3",x+25,y+342);
        if(System.currentTimeMillis()<bannerUntil){g.setColor(Color.YELLOW);g.drawString(banner,x+300,y+342);}
    }
    private void message(String s){banner=s;bannerUntil=System.currentTimeMillis()+1300;}

    private void tick(){

        long now=System.currentTimeMillis();

        if(finished || shopOpen){repaint();return;}

        for(Platform platform:platforms)platform.update();
        for(StageTrap trap:traps)trap.update();
        player.update(left,right,jump,stage.worldWidth,platforms);

        for (SupplyPickup supply : supplies) {
            if (supply.collect(player)) {
                switch (supply.getType()) {
                    case HEALTH: message("HEALTH +25"); break;
                    case ARMOR: message("ARMOR +25"); break;
                    case AMMO: weapons[selected].refillAmmo(); message("AMMO REFILLED"); break;
                    case CREDITS: credits += 50; message("CREDITS +50"); break;
                }
            }
        }


        camera=Math.max(0,Math.min(stage.worldWidth-getWidth(),player.getX()-getWidth()/3));
        if(mouseAim.isActive())player.faceTowards(mouseAim.worldX(camera));
        playerAnimation.update(player,fire && !weapons[selected].isReloading());

        for(Weapon w:weapons)w.update(now);

        if(fire){Weapon w=weapons[selected];if(w.shoot(now)){

            PlayerAimRig.Pose pose=PlayerAimRig.solve(player,mouseAim,camera);shotAngle=pose.angle;
            shotX=pose.muzzleX(GUN_WIDTHS[selected]);shotY=pose.muzzleY(GUN_WIDTHS[selected]);
            lastFireEffect=now;
            for(int k=0;k<w.pellets;k++){
                double angle=shotAngle+(k-(w.pellets-1)/2.0)*.075;
                bullets.add(new Bullet(shotX,shotY,Math.cos(angle)*15,Math.sin(angle)*15,w.getDamage()));
            }
            lastFire=now;

        }else if(w.ammo()==0){w.reload(now);}

        }

        if(!bossPractice && spawned<Math.min(stage.quota,3+player.getX()*stage.quota/(stage.worldWidth-700)) && zombies.size()<stage.getEnemyLimit() && now-lastSpawn>stage.getSpawnInterval()){

            String type=(spawned%Math.max(3,7-stage.level/3)==2)?"Brute":(spawned%Math.max(2,4-stage.level/4)==1?"Runner":"Walker");

            int xx=Math.min(stage.worldWidth-130,Math.max(player.getX()+480,camera+getWidth()+rng.nextInt(200)));

            zombies.add(new Zombie(xx,type,stage.level));spawned++;lastSpawn=now;

        }

        if(spawned>=stage.quota && zombies.isEmpty() && !bossSpawned && player.getX()>stage.worldWidth*0.75){

            Zombie boss=BossFactory.create(Math.min(stage.worldWidth-180,player.getX()+480),stage.level,stage.worldWidth);

            zombies.add(boss);bossSpawned=true;message("WARNING: "+boss.getKind());

        }

        for(Iterator<Bullet> it=bullets.iterator();it.hasNext();){

            Bullet b=it.next();b.update();boolean hit=false;

            for(ExplosiveBarrel barrel:barrels) {
                if(barrel.hit(b)){detonate(barrel,now);hit=true;break;}
            }
            if(!hit)for(Zombie z:zombies){if(intersects(b.getX(),b.getY(),b.getWidth(),b.getHeight(),z.getX(),z.getY(),z.getWidth(),z.getHeight())){

                z.hurt(b.getDamage());

                ZombieAnimation anim = zombieAnimations.computeIfAbsent(z, key -> new ZombieAnimation()
            );

            anim.playHurt();

            hit = true;
            break;

            }}

            if(hit || b.outside(stage.worldWidth,Math.max(640,getHeight())))it.remove();

        }

        for(Iterator<Zombie> it=zombies.iterator();it.hasNext();){

            Zombie z=it.next();
            if(z.dead() && !z.isDying()){

                killed++;
                credits += z.getReward();

                tryDropWeapon(z);

                z.startDeath();

                ZombieAnimation anim =
                    zombieAnimations.computeIfAbsent(
                        z,
                        key -> new ZombieAnimation()
                    );

                anim.playDeath();

                dyingZombies.add(z);

                it.remove();
                continue;
            }

            z.update(player);
            if(z instanceof BossCombat && ((BossCombat)z).specialHits(player)){player.hurt(z.getDamage());playerAnimation.playHurt();}
            ZombieAnimation anim = zombieAnimations.computeIfAbsent(
                z, key -> new ZombieAnimation()
            );

            boolean attacking =
                Math.abs(z.getX() - player.getX()) < 65;

            anim.update(attacking);

            if(intersects(player.getX()+12,player.getY()+12,player.getWidth()-24,player.getHeight()-20,z.getX(),z.getY(),z.getWidth(),z.getHeight())&&!(z instanceof BossCombat)&&z.canHit(now)){

                int before=player.getHp()+player.getArmor();

                player.hurt(z.getDamage());

                if(player.getHp()+player.getArmor()<before)playerAnimation.playHurt();

            }

        }

        List<Zombie> reinforcements=new ArrayList<>();
        for(Zombie z:zombies)if(z instanceof StageBoss){
            int requested=((StageBoss)z).takeSummons();
            for(int i=0;i<requested && zombies.size()+reinforcements.size()<stage.getEnemyLimit();i++){
                int sx=Math.max(0,Math.min(stage.worldWidth-100,z.getX()+(i%2==0?-160:160)));
                reinforcements.add(new Zombie(sx,"Runner",stage.level));
            }
        }
        zombies.addAll(reinforcements);
        for(StageTrap trap:traps)if(trap.tryHit(player))playerAnimation.playHurt();

        // Remove drops after 20 seconds to keep the world uncluttered.

        weaponDrops.removeIf(drop -> drop.isExpired(now));

        if(player.getHp()<=0){playerAnimation.playDeath();lost=true;finished=true;}

        if(bossSpawned && zombies.isEmpty() && (bossPractice || player.getX()>=stage.worldWidth-170)){

            won=true;finished=true;if(!bossPractice)Progress.complete(stage.level);

        }
        dyingZombies.removeIf(z -> {
            ZombieAnimation anim = zombieAnimations.get(z);

            if (anim == null || anim.isDeathFinished()) {
                zombieAnimations.remove(z);
                return true;
            }

            return false;
        });
        repaint();

    }

    private void tryDropWeapon(Zombie z){

        double chance=z.getDropChance();

        if(rng.nextDouble()>=chance)return;

        // Prefer weapons the player has not unlocked yet.

        List<Integer> candidates=new ArrayList<>();

        for(int i=1;i<weapons.length;i++)if(!unlockedWeapons[i])candidates.add(i);

        if(candidates.isEmpty())for(int i=0;i<weapons.length;i++)candidates.add(i);

        int weaponIndex=candidates.get(rng.nextInt(candidates.size()));

        weaponDrops.add(new WeaponDrop(z.getX()+z.getWidth()/2,GROUND-34,weaponIndex));

    }

    private void pickUpNearbyWeapon(){

        if(finished)return;

        for(Iterator<WeaponDrop> it=weaponDrops.iterator();it.hasNext();){

            WeaponDrop drop=it.next();

            if(drop.canPickup(player)){

                int idx=drop.getWeaponIndex();

                if(!unlockedWeapons[idx]){

                    unlockedWeapons[idx]=true;

                    selected=idx;

                    message("NEW WEAPON: "+weapons[idx].name+"!");

                }else{

                    weapons[idx].refillAmmo();

                    message("AMMO REFILL: "+weapons[idx].name);

                }

                it.remove();return;

            }

        }

    }

    private void drawWeaponDrops(Graphics2D g){

        for(WeaponDrop drop:weaponDrops){

            int index=drop.getWeaponIndex();

            if(index>=0 && index<weaponImages.length){

                drop.draw(g,camera,weaponImages[index]);

            }

            if(drop.canPickup(player)){

                g.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));

                g.setColor(Color.YELLOW);

                g.drawString("PRESS E TO PICK UP",getWidth()/2-110,getHeight()-65);

            }

        }

    }

    private static boolean intersects(int x,int y,int w,int h,int xx,int yy,int ww,int hh){return x<xx+ww && x+w>xx && y<yy+hh && y+h>yy;}

    private void drawCityDetails(Graphics2D g,int sw){

        // Parallax skyline and static debris, using reproducible pixel-style art.

        int moonX=840-camera/8;

        g.setColor(new Color(224,215,173));g.fillOval(moonX,112,62,62);

        g.setColor(new Color(34,47,67));g.fillOval(moonX+20,102,62,62);

        for(int i=0;i<16;i++){

            int x=i*285+85-camera;if(x < -280 || x > sw+260)continue;

            int variant=i%4;

            // Pavement props: ruined car, barrel, lamp post, barricades.

            if(variant==0){

                g.setColor(new Color(28,33,39));g.fillRect(x,GROUND-51,116,47);

                g.setColor(new Color(91,49,51));g.fillRect(x+7,GROUND-42,100,25);

                g.setColor(new Color(72,95,110));g.fillRect(x+35,GROUND-64,53,24);

                g.setColor(new Color(14,16,21));g.fillRect(x+16,GROUND-15,23,17);g.fillRect(x+82,GROUND-15,23,17);

                g.setColor(new Color(237,182,72));g.fillRect(x+103,GROUND-31,8,7);

            }else if(variant==1){

                g.setColor(new Color(70,77,80));g.fillRect(x+50,GROUND-170,7,170);

                g.setColor(new Color(70,77,80));g.fillRect(x+50,GROUND-170,67,7);

                g.setColor(new Color(255,198,103,130));g.fillRect(x+98,GROUND-160,20,11);

                g.setColor(new Color(35,39,42));g.fillRect(x+91,GROUND-166,31,8);

            }else if(variant==2){

                g.setColor(new Color(91,39,32));g.fillRect(x+15,GROUND-39,42,39);

                g.setColor(new Color(163,73,50));g.fillRect(x+15,GROUND-40,42,6);

                g.setColor(new Color(25,25,30));g.fillRect(x+14,GROUND-26,44,4);

                g.setColor(new Color(85,83,76));g.fillRect(x+75,GROUND-21,65,21);

            }else{

                g.setColor(new Color(83,66,53));g.fillRect(x,GROUND-39,111,39);

                g.setColor(new Color(180,117,56));for(int j=0;j<3;j++)g.fillRect(x+8+j*34,GROUND-35,10,29);

                g.setColor(new Color(36,39,42));g.fillRect(x+3,GROUND-5,108,5);

            }

        }

    }

    @Override protected 
    void paintComponent(Graphics g0){

        super.paintComponent(g0);Graphics2D g=(Graphics2D)g0.create();
        

        
        if(background != null){

        g.drawImage(
            background,
            0,
            0,
            getWidth(),
            getHeight(),
            null
        );

    }
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int sw=getWidth(),sh=getHeight();

        // g.setColor(stage.sky);
        // g.fillRect(0,0,sw,sh);

        // Per-stage skyline, fog, arches, industrial structures and thematic silhouettes.

        // for(int i=0;i<38;i++){

        //     int wx=i*137,xx=wx-camera/3;

        //     if(xx<-160||xx>sw+160)continue;

        //     int bh=90+(i*73+stage.level*37)%180;

        //     g.setColor(stage.sky.brighter());g.fillRect(xx,GROUND-bh,100,bh);

        //     g.setColor(stage.accent.darker().darker());

        //     for(int yy=GROUND-bh+15;yy<GROUND-12;yy+=29)for(int a=0;a<3;a++)g.fillRect(xx+13+a*29,yy,11,13);

        //     if(stage.level==4 || stage.level==7){g.setColor(new Color(27,69,43));g.fillOval(xx+24,GROUND-bh-26,70,75);}

        //     if(stage.level==2 || stage.level==9){g.setColor(stage.accent);g.fillRect(xx+10,GROUND-bh,7,bh/3);}

        // }

        //if(stage.level==1)drawCityDetails(g,sw);

        g.setColor(new Color(43,44,49));g.fillRect(0,GROUND,sw,sh-GROUND);

        g.setColor(stage.accent);g.fillRect(0,GROUND,sw,5);

        for(int x=-(camera%60);x<sw;x+=60){g.setColor(new Color(90,92,95));g.fillRect(x,GROUND+42,34,3);}

        for(StageTrap trap:traps)trap.draw(g,camera);

        if(bossSpawned && zombies.isEmpty()){

            int ex=stage.worldWidth-110-camera;g.setColor(Color.GREEN);g.fillRect(ex,GROUND-170,70,170);

            g.setColor(Color.BLACK);g.drawString("EXIT",ex+18,GROUND-95);

        }

        for(ExplosiveBarrel barrel:barrels)barrel.draw(g,camera,System.currentTimeMillis());
        for (Platform platform : platforms) platform.draw(g,camera,stage.accent);

        // The original artwork from the user's project is reused here.

        int px=player.getX()-camera,py=player.getY();

        BioArmorVisuals.drawAura(g,player,camera);
        if(!player.isDead()){
            PlayerAimRig.Pose rearPose=PlayerAimRig.solve(player,mouseAim,camera);
            PlayerAimRig.drawArm(g,rearPose,camera,true,player.isFacingRight());
            BioArmorVisuals.drawForearm(g,player,rearPose,camera,true);
        }
        if(playerAnimation.getCurrentFrame()!=null){

            playerAnimation.draw(g,player,camera);

        }else if(playerImage!=null){

            if(player.isFacingRight())g.drawImage(playerImage,px,py,player.getWidth(),player.getHeight(),null);

            else g.drawImage(playerImage,px+player.getWidth(),py,-player.getWidth(),player.getHeight(),null);

        }else{

            g.setColor(Color.CYAN);g.fillRect(px,py,player.getWidth(),player.getHeight());

        }

        drawEquippedWeapon(g,px,py);
        BioArmorVisuals.drawRelease(g,player,camera);
        if(System.currentTimeMillis()-lastFireEffect<60){
            Graphics2D flash=(Graphics2D)g.create();flash.translate(shotX-camera,shotY);flash.rotate(shotAngle);
            flash.setColor(new Color(255,135,20,175));flash.fillOval(-5,-8,24,16);
            flash.setColor(new Color(255,240,130));flash.fillOval(0,-4,14,8);flash.dispose();
        }
        if(mouseAim.isActive() && !shopOpen && !finished){
            Graphics2D cross=(Graphics2D)g.create();int mx=mouseAim.getScreenX(),my=mouseAim.getScreenY();cross.setColor(new Color(145,250,220));cross.setStroke(new BasicStroke(1));cross.drawOval(mx-7,my-7,14,14);cross.drawLine(mx-12,my,mx+12,my);cross.drawLine(mx,my-12,mx,my+12);cross.dispose();
        }
        //if(System.currentTimeMillis()-lastFire<80){g.setColor(Color.YELLOW);g.fillOval(px+(player.isFacingRight()?80:-26),py+40,18,18);}

        for(Zombie z:zombies){

            int zx=z.getX()-camera;if(zx<-180||zx>sw+180)continue;

            
            ZombieAnimation anim = zombieAnimations.get(z);


            if(z instanceof BossCombat){((BossCombat)z).draw(g,camera,player.getX()>z.getX());}
            else if (anim != null && anim.getCurrentFrame() != null) {
                anim.draw(g, z, player, camera);
            } else if (zombieImage != null) {
                g.drawImage(
                    zombieImage,
                    zx, z.getY(),
                    z.getWidth(), z.getHeight(),
                    null
                );
            } else {
                g.setColor(Color.GREEN);
                g.fillRect(
                    zx, z.getY(),
                    z.getWidth(), z.getHeight()
                );
            }


            if(z instanceof MiniBoss || z instanceof FinalBoss){

                g.setColor(new Color(152,40,39,100));g.fillRect(zx,z.getY(),z.getWidth(),z.getHeight());

            }

            g.setColor(Color.DARK_GRAY);g.fillRect(zx,z.getY()-11,z.getWidth(),6);

            g.setColor(Color.GREEN);g.fillRect(zx,z.getY()-11,(int)(z.getWidth()*(double)Math.max(0,z.getHp())/z.getMaxHp()),6);

        }
        // Draw zombie death animation
        for(Zombie z : dyingZombies){
            if(z instanceof BossCombat){((BossCombat)z).draw(g,camera,player.getX()>z.getX());continue;}

            ZombieAnimation anim =
                zombieAnimations.get(z);

            if(anim != null){

                anim.draw(
                    g,
                    z,
                    player,
                    camera
                );
            }
        }
        drawWeaponDrops(g);
        for (SupplyPickup supply : supplies) supply.draw(g,camera,System.currentTimeMillis());

        for(Bullet b:bullets)b.draw(g,camera);

        drawPlayerHUD(g);
        for(Zombie boss:zombies)if(boss instanceof BossCombat){
            int bx=getWidth()/2-220,by=getHeight()-55;
            g.setColor(new Color(12,15,22,225));g.fillRoundRect(bx-12,by-27,464,58,8,8);
            g.setFont(new Font(Font.MONOSPACED,Font.BOLD,13));g.setColor(Color.ORANGE);
            g.drawString(boss.getKind()+"  "+boss.getHp()+" / "+boss.getMaxHp()+(boss.getHp()<boss.getMaxHp()/2?"  ENRAGED":""),bx,by-8);
            g.setColor(Color.DARK_GRAY);g.fillRect(bx,by,440,13);g.setColor(new Color(220,60,60));g.fillRect(bx,by,440*Math.max(0,boss.getHp())/boss.getMaxHp(),13);
        }
        g.setColor(new Color(10,12,20,205));g.fillRoundRect(15,12,350,94,10,10);
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,14));g.setColor(Color.WHITE);
        g.drawString("DISTANCE " + Math.min(100,player.getX()*100/(stage.worldWidth-170)) + "%  |  KILLS " + killed + "/" + stage.quota,28,35);
        g.drawString("CREDITS $"+credits+"  |  SPACE: DOUBLE JUMP",28,57);
        g.drawString("SHIFT: DASH "+(player.getDashCooldown()==0?"READY":"WAIT")+" | Q: SHOP",28,78);
        g.setColor(Color.DARK_GRAY);g.fillRect(28,90,320,5);
        g.setColor(stage.accent.brighter());g.fillRect(28,90,Math.min(320,player.getX()*320/(stage.worldWidth-170)),5);

        if(System.currentTimeMillis()<bannerUntil){g.setColor(Color.YELLOW);g.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));g.drawString(banner,24,134);}

        if(shopOpen)drawShop(g);
        if(finished){

            g.setColor(new Color(0,0,0,210));g.fillRect(0,0,sw,sh);

            g.setColor(won?Color.GREEN:Color.RED);g.setFont(new Font(Font.MONOSPACED,Font.BOLD,55));

            g.drawString(won?(stage.level==10?"YOU WIN!":"STAGE CLEAR!"):"GAME OVER",sw/2-215,sh/2-35);

            g.setColor(Color.WHITE);g.setFont(new Font(Font.MONOSPACED,Font.BOLD,20));

            g.drawString(won?"Next stage unlocked!":"Try again!",sw/2-135,sh/2+12);

            g.drawString("Press ENTER to select stage",sw/2-180,sh/2+65);

        }

        g.dispose();

    }



    private void drawPlayerHUD(Graphics2D g){
        int width=260,height=198,x=getWidth()-272,y=12;
        Graphics2D hud=(Graphics2D)g.create();
        hud.setColor(new Color(10,12,20,220));hud.fillRoundRect(x,y,width,height,12,12);
        hud.setColor(new Color(75,92,110));hud.setStroke(new BasicStroke(2));hud.drawRoundRect(x,y,width,height,12,12);
        hud.setFont(new Font(Font.MONOSPACED,Font.BOLD,14));
        int hp=Math.max(0,Math.min(100,player.getHp()));
        int armor=Math.max(0,Math.min(100,player.getArmor()));
        hud.setColor(new Color(255,100,105));hud.drawString("HP",x+15,y+23);
        hud.setColor(Color.WHITE);hud.drawString(hp+" / 100",x+155,y+23);
        hud.setColor(new Color(70,25,30));hud.fillRoundRect(x+15,y+30,230,12,4,4);
        hud.setColor(hp>30?new Color(235,55,65):new Color(255,145,30));hud.fillRoundRect(x+15,y+30,230*hp/100,12,4,4);
        Color armorColor=armor==0?new Color(145,153,165):armor<=20?new Color(255,178,70):new Color(80,185,255);
        hud.setColor(armorColor);hud.drawString("ARMOR",x+15,y+65);
        hud.setColor(Color.WHITE);hud.drawString(armor+" / 100",x+155,y+65);
        hud.setColor(new Color(27,45,65));hud.fillRoundRect(x+15,y+72,230,12,4,4);
        hud.setColor(armorColor);hud.fillRoundRect(x+15,y+72,230*armor/100,12,4,4);
        if(armor==0){hud.setFont(new Font(Font.MONOSPACED,Font.BOLD,10));hud.setColor(armorColor);hud.drawString("EMPTY",x+85,y+64);hud.setFont(new Font(Font.MONOSPACED,Font.BOLD,14));}
        MutationArmor mutation=player.getMutationArmor();int charge=mutation.getCharge();
        hud.setColor(new Color(190,125,255));hud.drawString("MUTATION",x+15,y+107);
        hud.setColor(Color.WHITE);hud.drawString(charge+" / 100",x+155,y+107);
        hud.setColor(new Color(46,26,65));hud.fillRoundRect(x+15,y+114,230,12,4,4);
        hud.setColor(mutation.isReady()?new Color(225,170,255):new Color(155,85,235));hud.fillRoundRect(x+15,y+114,230*charge/100,12,4,4);
        if(mutation.isReady()){hud.setColor(new Color(220,160,255));hud.drawRoundRect(x+111,y+94,24,16,4,4);hud.drawString("F",x+119,y+107);}
        Weapon weapon=weapons[selected];int ammo=Math.max(0,weapon.ammo()),maxAmmo=Math.max(1,weapon.magazine);
        hud.setColor(new Color(255,200,80));hud.drawString(weapon.name,x+15,y+149);
        hud.setColor(Color.WHITE);hud.drawString(ammo+" / "+maxAmmo,x+155,y+149);
        hud.setColor(new Color(70,55,25));hud.fillRoundRect(x+15,y+156,230,12,4,4);
        hud.setColor(new Color(255,190,50));hud.fillRoundRect(x+15,y+156,230*Math.min(ammo,maxAmmo)/maxAmmo,12,4,4);
        if(weapon.isReloading()){hud.setColor(Color.YELLOW);hud.setFont(new Font(Font.MONOSPACED,Font.BOLD,12));hud.drawString("RELOADING",x+15,y+188);}
        hud.dispose();
    }
    private void drawEquippedWeapon(Graphics2D g,int px,int py){
        if(player.isDead()){PlayerAimRig.drawFallenArms(g,player,camera);return;}
        if(selected<0 || selected>=weaponImages.length || weaponImages[selected]==null)return;
        PlayerAimRig.Pose pose=PlayerAimRig.solve(player,mouseAim,camera);
        PlayerAimRig.drawArm(g,pose,camera,false,player.isFacingRight());
        BioArmorVisuals.drawForearm(g,player,pose,camera,false);
        BioArmorVisuals.drawShoulder(g,player,pose,camera);
        Graphics2D gun=(Graphics2D)g.create();gun.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        gun.translate(pose.handX-camera,pose.handY);gun.rotate(pose.angle);
        if(Math.cos(pose.angle)<0)gun.scale(1,-1);
        gun.drawImage(weaponImages[selected],-GUN_WIDTHS[selected]/4,-GUN_HEIGHTS[selected]/2,GUN_WIDTHS[selected],GUN_HEIGHTS[selected],null);gun.dispose();
    }
}


