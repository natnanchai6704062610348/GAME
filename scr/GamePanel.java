import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel {

    // ==================== ข้อมูลด่าน ====================

    private static final int GROUND = 470;

    private final JFrame frame;
    private final Stage stage;
    private final boolean bossPractice;

    private int currentStage;
    private int camera = 0;

    private BufferedImage background;

    private final List<Platform> platforms;
    private final List<StageTrap> traps;
    private final List<SupplyPickup> supplies;

    private final List<ExplosiveBarrel> barrels = new ArrayList<>();

    // ==================== ผู้เล่นและการควบคุม ====================

    private final Player player = new Player(100, 350);
    private final PlayerAnimation playerAnimation = new PlayerAnimation();
    private final MouseAim mouseAim = new MouseAim();

    private final Set<Integer> heldKeys = new HashSet<>();

    private boolean left;
    private boolean right;
    private boolean jump;
    private boolean fire;

    private BufferedImage playerImage;

    // ==================== อาวุธและกระสุน ====================

    private static final int[] GUN_WIDTHS = {
        38, 52, 65, 62, 78
    };

    private static final int[] GUN_HEIGHTS = {
        25, 29, 26, 29, 28
    };

    private static final int[] GUN_PRICES = {
        0, 200, 300, 450, 600
    };

    private final boolean[] unlockedWeapons = {
        true, false, false, false, false
    };

    private final Weapon[] weapons = Weapon.create();
    private final BufferedImage[] weaponImages = new BufferedImage[5];

    private final List<Bullet> bullets = new ArrayList<>();
    private final List<WeaponDrop> weaponDrops = new ArrayList<>();

    private int selected = 0;

    private double shotX;
    private double shotY;
    private double shotAngle;

    private long lastFire = 0;
    private long lastFireEffect = 0;

    private MuzzleFlashEffect muzzleFlash = new MuzzleFlashEffect();

    // ==================== ศัตรู ====================

    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Zombie> dyingZombies = new ArrayList<>();

    private final Map<Zombie, ZombieAnimation> zombieAnimations =
        new IdentityHashMap<>();

    private BufferedImage zombieImage;

    private int spawned = 0;
    private int killed = 0;

    private boolean bossSpawned;
    private long lastSpawn = 0;

    // ==================== สถานะเกม ====================

    private final Random rng = new Random();

    private javax.swing.Timer timer;

    private boolean shopOpen;
    private boolean won;
    private boolean lost;
    private boolean finished;

    private int credits = 0;

    private String banner = "";
    private long bannerUntil = 0;

    // ==================== Constructor ====================

    public GamePanel(JFrame frame, int level) {
        this(frame, level, false);
    }

    public GamePanel(JFrame frame, int level, boolean bossPractice) {
        this.bossPractice = bossPractice;
        this.frame = frame;

        stage = Stage.get(level);
        platforms = stage.getPlatforms();

        traps = bossPractice
            ? Collections.emptyList()
            : stage.getTraps();

        supplies = stage.getSupplies();

        setFocusable(true);
        setBackground(stage.sky);

        // โหลดภาพอาวุธ
        weaponImages[0] = loadSprite("GUN/gun1.png");
        weaponImages[1] = loadSprite("GUN/gun2.png");
        weaponImages[2] = loadSprite("GUN/gun3.png");
        weaponImages[3] = loadSprite("GUN/gun4.png");
        weaponImages[4] = loadSprite("GUN/gun5.png");

        // วางถังระเบิดตามระยะของด่าน
        for (int x = 900; x < stage.worldWidth - 300; x += 850) {
            barrels.add(new ExplosiveBarrel(x));
        }

        currentStage = level;
        loadBackground(level);

        System.out.println("Current Stage = " + level);

        playerImage = loadSprite("clean_player_idle.png");
        zombieImage = loadSprite("clean_Zombie.png");

        for (int i = 0; i < weaponImages.length; i++) {
            System.out.println(
                "GUN " + (i + 1) + ": "
                + (weaponImages[i] == null ? "FAILED" : "LOADED")
            );
        }

        // รับเหตุการณ์เมาส์
        MouseAdapter mouse = new MouseAdapter() {

            private void aim(MouseEvent e) {
                if (!shopOpen && !finished) {
                    mouseAim.move(e.getX(), e.getY());
                    player.faceTowards(mouseAim.worldX(camera));
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                aim(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                aim(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                aim(e);

                if (SwingUtilities.isLeftMouseButton(e)
                        && !shopOpen
                        && !finished) {

                    fire = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    fire = false;
                }
            }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        // สร้างบอสทันทีในโหมดฝึก
        if (bossPractice) {
            zombies.add(
                BossFactory.create(650, level, stage.worldWidth)
            );

            bossSpawned = true;
            message("BOSS PRACTICE: " + stage.getBossName());
        }

        // เริ่มรอบอัปเดตเกม
        timer = new javax.swing.Timer(16, e -> tick());
        timer.start();
    }

    // ==================== โหลดรูปภาพ ====================

    private void loadBackground(int stage) {
        try {
            File file = new File("images/BG" + stage + ".png");

            System.out.println("Loading : " + file.getAbsolutePath());
            System.out.println("Exists : " + file.exists());

            background = loadSprite("BG" + stage + ".png");

            if (background == null) {
                background = loadSprite("BG1.png");
            }

            System.out.println(
                "BG Size : "
                + background.getWidth()
                + " x "
                + background.getHeight()
            );

            System.out.println("BG Loaded");

        } catch (Exception e) {
            System.out.println("BG ERROR : " + e.getMessage());
        }
    }

    private BufferedImage loadSprite(String name) {
        String[] roots = {
            "images/",
            "GUN/",
            "images/GUN/",
            "ProjectGameOOP/images/",
            "ProjectGameOOP/GUN/",
            "../images/",
            "../GUN/"
        };

        // ลองโหลดจากไฟล์ภายนอก
        for (String root : roots) {
            try {
                File file = new File(root + name);

                if (file.isFile()) {
                    return ImageIO.read(file);
                }

            } catch (Exception ignored) {
            }
        }

        // ลองโหลดจากทรัพยากรภายใน classpath
        java.net.URL url = getClass().getResource("/images/" + name);

        try {
            if (url != null) {
                return ImageIO.read(url);
            }

        } catch (Exception ignored) {
        }

        System.err.println("Missing sprite: " + name);
        return null;
    }

    // ==================== การควบคุมแป้นพิมพ์ ====================

    public void handleKey(int code, boolean pressed) {
        boolean fresh = pressed && heldKeys.add(code);

        if (!pressed) {
            heldKeys.remove(code);
        }

        if (finished
                && code != KeyEvent.VK_ENTER
                && code != KeyEvent.VK_ESCAPE) {

            return;
        }

        // เปิดหรือปิดร้านค้า
        if (code == KeyEvent.VK_Q && fresh && !finished) {
            shopOpen = !shopOpen;

            left = false;
            right = false;
            jump = false;
            fire = false;

            repaint();
            return;
        }

        // คำสั่งขณะเปิดร้านค้า
        if (shopOpen) {
            if (code == KeyEvent.VK_ESCAPE && fresh) {
                shopOpen = false;
                repaint();
                return;
            }

            if (fresh
                    && code >= KeyEvent.VK_1
                    && code <= KeyEvent.VK_5) {

                buyGun(code - KeyEvent.VK_1);
            }

            if (fresh && code == KeyEvent.VK_U) {
                buyUpgrade();
            }

            if (fresh && code == KeyEvent.VK_F) {
                if (credits >= 75
                        && (weapons[selected].ammo()
                            < weapons[selected].magazine
                            || weapons[selected].isReloading())) {

                    credits -= 75;
                    weapons[selected].refillAmmo();
                    message("AMMO REFILLED");
                }
            }

            repaint();
            return;
        }

        // คำสั่งที่ใช้สถานะการกดค้าง
        if (code == KeyEvent.VK_SHIFT && fresh) {
            player.dash();
        }

        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            left = pressed;
        }

        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            right = pressed;
        }

        if (code == KeyEvent.VK_SPACE) {
            jump = pressed;
        }

        if (code == KeyEvent.VK_J) {
            fire = pressed;
        }

        // คำสั่งด้านล่างทำงานเฉพาะเมื่อกดครั้งใหม่
        if (!pressed || !fresh) {
            return;
        }

        if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_5) {
            int requested = code - KeyEvent.VK_1;

            if (unlockedWeapons[requested]) {
                selected = requested;
            } else {
                message(
                    weapons[requested].name
                    + " LOCKED - FIND A DROP!"
                );
            }
        }

        if (code == KeyEvent.VK_F) {
            releaseMutation();
        }

        if (code == KeyEvent.VK_E) {
            pickUpNearbyWeapon();
        }

        if (code == KeyEvent.VK_R) {
            weapons[selected].reload(System.currentTimeMillis());
        }

        if (code == KeyEvent.VK_H && credits >= 100) {
            credits -= 100;
            player.heal(35);
            message("+35 HEALTH");
        }

        if (code == KeyEvent.VK_B && credits >= 100) {
            credits -= 100;
            player.addArmor(30);
            message("+30 ARMOR");
        }

        if (code == KeyEvent.VK_ESCAPE) {
            timer.stop();

            frame.setContentPane(new LevelPanel(frame));
            frame.revalidate();
            frame.repaint();
        }

        if (code == KeyEvent.VK_ENTER && finished) {
            timer.stop();

            frame.setContentPane(new LevelPanel(frame));
            frame.revalidate();
            frame.repaint();
        }
    }

    // ==================== สกิลเกราะชีวภาพ ====================

    private void releaseMutation() {
        if (!player.releaseMutation()) {
            message("MUTATION NOT READY");
            return;
        }

        double centerX = player.getX() + player.getWidth() / 2.0;
        double centerY = player.getY() + player.getHeight() / 2.0;

        for (Zombie zombie : zombies) {
            double closestX = Math.max(
                zombie.getX(),
                Math.min(
                    centerX,
                    zombie.getX() + zombie.getWidth()
                )
            );

            double closestY = Math.max(
                zombie.getY(),
                Math.min(
                    centerY,
                    zombie.getY() + zombie.getHeight()
                )
            );

            double distance = Math.hypot(
                closestX - centerX,
                closestY - centerY
            );

            if (!zombie.dead() && distance <= MutationArmor.RADIUS) {
                zombie.hurt(MutationArmor.DAMAGE);

                if (!(zombie instanceof Boss)) {
                    zombie.knockBackFrom(
                        centerX,
                        110,
                        stage.worldWidth
                    );
                }

                zombieAnimations
                    .computeIfAbsent(
                        zombie,
                        key -> new ZombieAnimation()
                    )
                    .playHurt();
            }
        }

        message("BIO BURST!");
    }

    // ==================== ร้านค้า ====================

    private void buyGun(int index) {
        if (unlockedWeapons[index]) {
            selected = index;
            message("EQUIPPED " + weapons[index].name);
            return;
        }

        if (credits < GUN_PRICES[index]) {
            message("NOT ENOUGH CREDITS");
            return;
        }

        credits -= GUN_PRICES[index];
        unlockedWeapons[index] = true;
        selected = index;

        message("BOUGHT " + weapons[index].name);
    }

    private void buyUpgrade() {
        Weapon weapon = weapons[selected];
        int cost = 150 * (weapon.getUpgradeLevel() + 1);

        if (weapon.getUpgradeLevel() >= 3) {
            message("MAX UPGRADE");
            return;
        }

        if (credits < cost) {
            message("NOT ENOUGH CREDITS");
            return;
        }

        if (weapon.upgrade()) {
            credits -= cost;
            message("DAMAGE UPGRADED");
        }
    }

    private void drawShop(Graphics2D g) {
        int x = getWidth() / 2 - 310;
        int y = 120;

        g.setColor(new Color(0, 0, 0, 210));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(20, 28, 38));
        g.fillRoundRect(x, y, 620, 365, 16, 16);

        g.setColor(new Color(255, 205, 95));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        g.drawString("SUPPLY SHOP   $" + credits, x + 25, y + 38);

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));

        for (int i = 0; i < weapons.length; i++) {
            g.setColor(i == selected ? Color.CYAN : Color.WHITE);

            g.drawString(
                (i + 1)
                    + "  "
                    + weapons[i].name
                    + "  "
                    + (unlockedWeapons[i]
                        ? "OWNED"
                        : "$" + GUN_PRICES[i]),
                x + 25,
                y + 78 + i * 31
            );
        }

        Weapon weapon = weapons[selected];

        g.setColor(Color.YELLOW);
        g.drawString(
            "U  Damage +25% base  "
                + (weapon.getUpgradeLevel() >= 3
                    ? "MAX"
                    : "$" + (150 * (weapon.getUpgradeLevel() + 1))),
            x + 25,
            y + 246
        );

        g.setColor(Color.WHITE);
        g.drawString(
            "F  Refill current gun  $75",
            x + 25,
            y + 278
        );

        g.drawString(
            "Q / ESC: close | World paused",
            x + 25,
            y + 313
        );

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        g.drawString(
            weapon.name
                + " DMG "
                + weapon.getDamage()
                + "  UPGRADE "
                + weapon.getUpgradeLevel()
                + "/3",
            x + 25,
            y + 342
        );

        if (System.currentTimeMillis() < bannerUntil) {
            g.setColor(Color.YELLOW);
            g.drawString(banner, x + 300, y + 342);
        }
    }

    // ==================== ถังระเบิด ====================

    private void detonate(ExplosiveBarrel first, long now) {
        ArrayDeque<ExplosiveBarrel> pending = new ArrayDeque<>();
        pending.add(first);

        while (!pending.isEmpty()) {
            ExplosiveBarrel barrel = pending.remove();

            if (!barrel.explode(now)) {
                continue;
            }

            for (Zombie zombie : zombies) {
                if (!zombie.dead()
                        && barrel.inBlast(
                            zombie.getX() + zombie.getWidth() / 2,
                            zombie.getY() + zombie.getHeight() / 2)) {

                    zombie.hurt(180);
                }
            }

            if (barrel.inBlast(
                    player.getX() + player.getWidth() / 2,
                    player.getY() + player.getHeight() / 2)) {

                player.hurt(30);
            }

            // เพิ่มถังใกล้เคียงเข้าคิวเพื่อให้ระเบิดต่อเนื่อง
            for (ExplosiveBarrel other : barrels) {
                if (!other.isExploded()
                        && barrel.inBlast(other.centerX(), 430)) {

                    pending.add(other);
                }
            }
        }
    }

    // ==================== ข้อความแจ้งสถานะ ====================

    private void message(String text) {
        banner = text;
        bannerUntil = System.currentTimeMillis() + 1300;
    }

    // ==================== รอบอัปเดตเกม ====================

    private void tick() {
        long now = System.currentTimeMillis();

        if (finished || shopOpen) {
            repaint();
            return;
        }

        // อัปเดตสิ่งแวดล้อมและผู้เล่น
        for (Platform platform : platforms) {
            platform.update();
        }

        for (StageTrap trap : traps) {
            trap.update();
        }

        player.update(
            left,
            right,
            jump,
            stage.worldWidth,
            platforms
        );

        // ตรวจการเก็บไอเทม
        for (SupplyPickup supply : supplies) {
            if (supply.collect(player)) {
                switch (supply.getType()) {
                    case HEALTH:
                        message("HEALTH +25");
                        break;

                    case ARMOR:
                        message("ARMOR +25");
                        break;

                    case AMMO:
                        weapons[selected].refillAmmo();
                        message("AMMO REFILLED");
                        break;

                    case CREDITS:
                        credits += 50;
                        message("CREDITS +50");
                        break;
                }
            }
        }

        // อัปเดตกล้องและทิศทางการหัน
        camera = Math.max(
            0,
            Math.min(
                stage.worldWidth - getWidth(),
                player.getX() - getWidth() / 3
            )
        );

        if (mouseAim.isActive()) {
            player.faceTowards(mouseAim.worldX(camera));
        }

        playerAnimation.update(
            player,
            fire && !weapons[selected].isReloading()
        );

        for (Weapon weapon : weapons) {
            weapon.update(now);
        }

        // ยิงกระสุน
        if (fire) {
            Weapon weapon = weapons[selected];

            if (weapon.shoot(now)) {
                PlayerAimRig.Pose pose = PlayerAimRig.solve(
                    player,
                    mouseAim,
                    camera
                );

                shotAngle = pose.angle;
                shotX = pose.muzzleX(GUN_WIDTHS[selected]);
                shotY = pose.muzzleY(GUN_WIDTHS[selected]);
                lastFireEffect = now;

                for (int k = 0; k < weapon.pellets; k++) {
                    double angle = shotAngle
                        + (k - (weapon.pellets - 1) / 2.0) * 0.075;

                    bullets.add(new Bullet(
                        shotX,
                        shotY,
                        Math.cos(angle) * 15,
                        Math.sin(angle) * 15,
                        weapon.getDamage()
                    ));
                }

                lastFire = now;

            } else if (weapon.ammo() == 0) {
                weapon.reload(now);
            }
        }

        // สร้างศัตรูตามความคืบหน้าในด่าน
        if (!bossPractice
                && spawned < Math.min(
                    stage.quota,
                    3 + player.getX() * stage.quota
                        / (stage.worldWidth - 700))
                && zombies.size() < stage.getEnemyLimit()
                && now - lastSpawn > stage.getSpawnInterval()) {

            String type =
                spawned % Math.max(3, 7 - stage.level / 3) == 2
                    ? "Brute"
                    : (spawned % Math.max(2, 4 - stage.level / 4) == 1
                        ? "Runner"
                        : "Walker");

            int spawnX = Math.min(
                stage.worldWidth - 130,
                Math.max(
                    player.getX() + 480,
                    camera + getWidth() + rng.nextInt(200)
                )
            );

            zombies.add(new Zombie(spawnX, type, stage.level));

            spawned++;
            lastSpawn = now;
        }

        // สร้างบอสเมื่อผ่านเงื่อนไข
        if (spawned >= stage.quota
                && zombies.isEmpty()
                && !bossSpawned
                && player.getX() > stage.worldWidth * 0.75) {

            Zombie boss = BossFactory.create(
                Math.min(
                    stage.worldWidth - 180,
                    player.getX() + 480
                ),
                stage.level,
                stage.worldWidth
            );

            zombies.add(boss);
            bossSpawned = true;

            message("WARNING: " + boss.getKind());
        }

        // อัปเดตกระสุนและตรวจการชน
        for (Iterator<Bullet> iterator = bullets.iterator();
                iterator.hasNext();) {

            Bullet bullet = iterator.next();
            bullet.update();

            boolean hit = false;

            for (ExplosiveBarrel barrel : barrels) {
                if (barrel.hit(bullet)) {
                    detonate(barrel, now);
                    hit = true;
                    break;
                }
            }

            if (!hit) {
                for (Zombie zombie : zombies) {
                    if (intersects(
                            bullet.getX(),
                            bullet.getY(),
                            bullet.getWidth(),
                            bullet.getHeight(),
                            zombie.getX(),
                            zombie.getY(),
                            zombie.getWidth(),
                            zombie.getHeight())) {

                        zombie.hurt(bullet.getDamage());

                        ZombieAnimation animation =
                            zombieAnimations.computeIfAbsent(
                                zombie,
                                key -> new ZombieAnimation()
                            );

                        animation.playHurt();
                        hit = true;
                        break;
                    }
                }
            }

            if (hit || bullet.outside(
                    stage.worldWidth,
                    Math.max(640, getHeight()))) {

                iterator.remove();
            }
        }

        // อัปเดตศัตรู
        for (Iterator<Zombie> iterator = zombies.iterator();
                iterator.hasNext();) {

            Zombie zombie = iterator.next();

            if (zombie.dead() && !zombie.isDying()) {
                killed++;
                credits += zombie.getReward();

                tryDropWeapon(zombie);
                zombie.startDeath();

                ZombieAnimation animation =
                    zombieAnimations.computeIfAbsent(
                        zombie,
                        key -> new ZombieAnimation()
                    );

                animation.playDeath();

                dyingZombies.add(zombie);
                iterator.remove();
                continue;
            }

            zombie.update(player);

            // ตรวจการโจมตีพิเศษของบอส
            if (zombie instanceof BossCombat
                    && ((BossCombat) zombie).specialHits(player)) {

                player.hurt(zombie.getDamage());
                playerAnimation.playHurt();
            }

            ZombieAnimation animation =
                zombieAnimations.computeIfAbsent(
                    zombie,
                    key -> new ZombieAnimation()
                );

            boolean attacking =
                Math.abs(zombie.getX() - player.getX()) < 65;

            animation.update(attacking);

            // ตรวจการสัมผัสกับซอมบี้ทั่วไป
            if (intersects(
                    player.getX() + 12,
                    player.getY() + 12,
                    player.getWidth() - 24,
                    player.getHeight() - 20,
                    zombie.getX(),
                    zombie.getY(),
                    zombie.getWidth(),
                    zombie.getHeight())
                    && !(zombie instanceof BossCombat)
                    && zombie.canHit(now)) {

                int before = player.getHp() + player.getArmor();

                player.hurt(zombie.getDamage());

                if (player.getHp() + player.getArmor() < before) {
                    playerAnimation.playHurt();
                }
            }
        }

        // เพิ่มศัตรูที่บอสเรียกมา
        List<Zombie> reinforcements = new ArrayList<>();

        for (Zombie zombie : zombies) {
            if (zombie instanceof StageBoss) {
                int requested =
                    ((StageBoss) zombie).takeSummons();

                for (int i = 0;
                        i < requested
                        && zombies.size() + reinforcements.size()
                            < stage.getEnemyLimit();
                        i++) {

                    int spawnX = Math.max(
                        0,
                        Math.min(
                            stage.worldWidth - 100,
                            zombie.getX()
                                + (i % 2 == 0 ? -160 : 160)
                        )
                    );

                    reinforcements.add(
                        new Zombie(spawnX, "Runner", stage.level)
                    );
                }
            }
        }

        zombies.addAll(reinforcements);

        // ตรวจความเสียหายจากกับดัก
        for (StageTrap trap : traps) {
            if (trap.tryHit(player)) {
                playerAnimation.playHurt();
            }
        }

        // ลบไอเทมอาวุธที่หมดเวลา
        weaponDrops.removeIf(drop -> drop.isExpired(now));

        // ตรวจเงื่อนไขแพ้
        if (player.getHp() <= 0) {
            playerAnimation.playDeath();

            lost = true;
            finished = true;
        }

        // ตรวจเงื่อนไขผ่านด่าน
        if (bossSpawned
                && zombies.isEmpty()
                && (bossPractice
                    || player.getX() >= stage.worldWidth - 170)) {

            won = true;
            finished = true;

            if (!bossPractice) {
                Progress.complete(stage.level);
            }
        }

        // ลบศัตรูที่แสดงแอนิเมชันตายเสร็จแล้ว
        dyingZombies.removeIf(zombie -> {
            ZombieAnimation animation =
                zombieAnimations.get(zombie);

            if (animation == null || animation.isDeathFinished()) {
                zombieAnimations.remove(zombie);
                return true;
            }

            return false;
        });

        repaint();
    }

    // ==================== ไอเทมอาวุธ ====================

    private void tryDropWeapon(Zombie zombie) {
        double chance = zombie.getDropChance();

        if (rng.nextDouble() >= chance) {
            return;
        }

        // เลือกอาวุธที่ยังไม่ปลดล็อกก่อน
        List<Integer> candidates = new ArrayList<>();

        for (int i = 1; i < weapons.length; i++) {
            if (!unlockedWeapons[i]) {
                candidates.add(i);
            }
        }

        if (candidates.isEmpty()) {
            for (int i = 0; i < weapons.length; i++) {
                candidates.add(i);
            }
        }

        int weaponIndex = candidates.get(
            rng.nextInt(candidates.size())
        );

        weaponDrops.add(new WeaponDrop(
            zombie.getX() + zombie.getWidth() / 2,
            GROUND - 34,
            weaponIndex
        ));
    }

    private void pickUpNearbyWeapon() {
        if (finished) {
            return;
        }

        for (Iterator<WeaponDrop> iterator = weaponDrops.iterator();
                iterator.hasNext();) {

            WeaponDrop drop = iterator.next();

            if (drop.canPickup(player)) {
                int index = drop.getWeaponIndex();

                if (!unlockedWeapons[index]) {
                    unlockedWeapons[index] = true;
                    selected = index;

                    message(
                        "NEW WEAPON: " + weapons[index].name + "!"
                    );

                } else {
                    weapons[index].refillAmmo();

                    message(
                        "AMMO REFILL: " + weapons[index].name
                    );
                }

                iterator.remove();
                return;
            }
        }
    }

    private void drawWeaponDrops(Graphics2D g) {
        for (WeaponDrop drop : weaponDrops) {
            int index = drop.getWeaponIndex();

            if (index >= 0 && index < weaponImages.length) {
                drop.draw(g, camera, weaponImages[index]);
            }

            if (drop.canPickup(player)) {
                g.setFont(
                    new Font(Font.MONOSPACED, Font.BOLD, 18)
                );

                g.setColor(Color.YELLOW);

                g.drawString(
                    "PRESS E TO PICK UP",
                    getWidth() / 2 - 110,
                    getHeight() - 65
                );
            }
        }
    }

    // ==================== การตรวจชน ====================

    private static boolean intersects(
            int x,
            int y,
            int width,
            int height,
            int otherX,
            int otherY,
            int otherWidth,
            int otherHeight) {

        return x < otherX + otherWidth
            && x + width > otherX
            && y < otherY + otherHeight
            && y + height > otherY;
    }

    // ==================== รายละเอียดฉากเมือง ====================

    private void drawCityDetails(Graphics2D g, int screenWidth) {
        int moonX = 840 - camera / 8;

        g.setColor(new Color(224, 215, 173));
        g.fillOval(moonX, 112, 62, 62);

        g.setColor(new Color(34, 47, 67));
        g.fillOval(moonX + 20, 102, 62, 62);

        for (int i = 0; i < 16; i++) {
            int x = i * 285 + 85 - camera;

            if (x < -280 || x > screenWidth + 260) {
                continue;
            }

            int variant = i % 4;

            if (variant == 0) {
                // รถพัง
                g.setColor(new Color(28, 33, 39));
                g.fillRect(x, GROUND - 51, 116, 47);

                g.setColor(new Color(91, 49, 51));
                g.fillRect(x + 7, GROUND - 42, 100, 25);

                g.setColor(new Color(72, 95, 110));
                g.fillRect(x + 35, GROUND - 64, 53, 24);

                g.setColor(new Color(14, 16, 21));
                g.fillRect(x + 16, GROUND - 15, 23, 17);
                g.fillRect(x + 82, GROUND - 15, 23, 17);

                g.setColor(new Color(237, 182, 72));
                g.fillRect(x + 103, GROUND - 31, 8, 7);

            } else if (variant == 1) {
                // เสาไฟ
                g.setColor(new Color(70, 77, 80));
                g.fillRect(x + 50, GROUND - 170, 7, 170);

                g.setColor(new Color(70, 77, 80));
                g.fillRect(x + 50, GROUND - 170, 67, 7);

                g.setColor(new Color(255, 198, 103, 130));
                g.fillRect(x + 98, GROUND - 160, 20, 11);

                g.setColor(new Color(35, 39, 42));
                g.fillRect(x + 91, GROUND - 166, 31, 8);

            } else if (variant == 2) {
                // ถังและเศษซาก
                g.setColor(new Color(91, 39, 32));
                g.fillRect(x + 15, GROUND - 39, 42, 39);

                g.setColor(new Color(163, 73, 50));
                g.fillRect(x + 15, GROUND - 40, 42, 6);

                g.setColor(new Color(25, 25, 30));
                g.fillRect(x + 14, GROUND - 26, 44, 4);

                g.setColor(new Color(85, 83, 76));
                g.fillRect(x + 75, GROUND - 21, 65, 21);

            } else {
                // แผงกั้น
                g.setColor(new Color(83, 66, 53));
                g.fillRect(x, GROUND - 39, 111, 39);

                g.setColor(new Color(180, 117, 56));

                for (int j = 0; j < 3; j++) {
                    g.fillRect(
                        x + 8 + j * 34,
                        GROUND - 35,
                        10,
                        29
                    );
                }

                g.setColor(new Color(36, 39, 42));
                g.fillRect(x + 3, GROUND - 5, 108, 5);
            }
        }
    }

    // ==================== วาดหน้าจอเกม ====================

    @Override
    protected void paintComponent(Graphics original) {
        super.paintComponent(original);

        Graphics2D g = (Graphics2D) original.create();

        if (background != null) {
            g.drawImage(
                background,
                0,
                0,
                getWidth(),
                getHeight(),
                null
            );
        }

        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        int screenWidth = getWidth();
        int screenHeight = getHeight();

        // พื้นถนน
        g.setColor(new Color(43, 44, 49));
        g.fillRect(
            0,
            GROUND,
            screenWidth,
            screenHeight - GROUND
        );

        g.setColor(stage.accent);
        g.fillRect(0, GROUND, screenWidth, 5);

        for (int x = -(camera % 60); x < screenWidth; x += 60) {
            g.setColor(new Color(90, 92, 95));
            g.fillRect(x, GROUND + 42, 34, 3);
        }

        // กับดัก
        for (StageTrap trap : traps) {
            trap.draw(g, camera);
        }

        // ประตูออกหลังจัดการศัตรูหมด
        if (bossSpawned && zombies.isEmpty()) {
            int exitX = stage.worldWidth - 110 - camera;

            g.setColor(Color.GREEN);
            g.fillRect(exitX, GROUND - 170, 70, 170);

            g.setColor(Color.BLACK);
            g.drawString("EXIT", exitX + 18, GROUND - 95);
        }

        // ถังระเบิดและแท่น
        for (ExplosiveBarrel barrel : barrels) {
            barrel.draw(g, camera, System.currentTimeMillis());
        }

        for (Platform platform : platforms) {
            platform.draw(g, camera, stage.accent);
        }

        int playerX = player.getX() - camera;
        int playerY = player.getY();

        BioArmorVisuals.drawAura(g, player, camera);

        // แขนด้านหลัง
        if (!player.isDead()) {
            PlayerAimRig.Pose rearPose = PlayerAimRig.solve(
                player,
                mouseAim,
                camera
            );

            PlayerAimRig.drawArm(
                g,
                rearPose,
                camera,
                true,
                player.isFacingRight()
            );

            BioArmorVisuals.drawForearm(
                g,
                player,
                rearPose,
                camera,
                true
            );
        }

        // ตัวผู้เล่น
        if (playerAnimation.getCurrentFrame() != null) {
            playerAnimation.draw(g, player, camera);

        } else if (playerImage != null) {
            if (player.isFacingRight()) {
                g.drawImage(
                    playerImage,
                    playerX,
                    playerY,
                    player.getWidth(),
                    player.getHeight(),
                    null
                );
            } else {
                g.drawImage(
                    playerImage,
                    playerX + player.getWidth(),
                    playerY,
                    -player.getWidth(),
                    player.getHeight(),
                    null
                );
            }

        } else {
            g.setColor(Color.CYAN);
            g.fillRect(
                playerX,
                playerY,
                player.getWidth(),
                player.getHeight()
            );
        }

        drawEquippedWeapon(g, playerX, playerY);
        BioArmorVisuals.drawRelease(g, player, camera);

        // แสงปากกระบอกปืน
        if (System.currentTimeMillis() - lastFireEffect < 60) {
            Graphics2D flash = (Graphics2D) g.create();

            flash.translate(shotX - camera, shotY);
            flash.rotate(shotAngle);

            flash.setColor(new Color(255, 135, 20, 175));
            flash.fillOval(-5, -8, 24, 16);

            flash.setColor(new Color(255, 240, 130));
            flash.fillOval(0, -4, 14, 8);

            flash.dispose();
        }

        // เป้าเล็ง
        if (mouseAim.isActive() && !shopOpen && !finished) {
            Graphics2D cross = (Graphics2D) g.create();

            int mouseX = mouseAim.getScreenX();
            int mouseY = mouseAim.getScreenY();

            cross.setColor(new Color(145, 250, 220));
            cross.setStroke(new BasicStroke(1));

            cross.drawOval(mouseX - 7, mouseY - 7, 14, 14);
            cross.drawLine(
                mouseX - 12, mouseY,
                mouseX + 12, mouseY
            );

            cross.drawLine(
                mouseX, mouseY - 12,
                mouseX, mouseY + 12
            );

            cross.dispose();
        }

        // ศัตรูที่ยังอยู่ในเกม
        for (Zombie zombie : zombies) {
            int zombieX = zombie.getX() - camera;

            if (zombieX < -180 || zombieX > screenWidth + 180) {
                continue;
            }

            ZombieAnimation animation =
                zombieAnimations.get(zombie);

            if (zombie instanceof BossCombat) {
                ((BossCombat) zombie).draw(
                    g,
                    camera,
                    player.getX() > zombie.getX()
                );

            } else if (animation != null
                    && animation.getCurrentFrame() != null) {

                animation.draw(g, zombie, player, camera);

            } else if (zombieImage != null) {
                g.drawImage(
                    zombieImage,
                    zombieX,
                    zombie.getY(),
                    zombie.getWidth(),
                    zombie.getHeight(),
                    null
                );

            } else {
                g.setColor(Color.GREEN);
                g.fillRect(
                    zombieX,
                    zombie.getY(),
                    zombie.getWidth(),
                    zombie.getHeight()
                );
            }

            if (zombie instanceof MiniBoss
                    || zombie instanceof FinalBoss) {

                g.setColor(new Color(152, 40, 39, 100));
                g.fillRect(
                    zombieX,
                    zombie.getY(),
                    zombie.getWidth(),
                    zombie.getHeight()
                );
            }

            // แถบพลังชีวิตศัตรู
            g.setColor(Color.DARK_GRAY);
            g.fillRect(
                zombieX,
                zombie.getY() - 11,
                zombie.getWidth(),
                6
            );

            g.setColor(Color.GREEN);
            g.fillRect(
                zombieX,
                zombie.getY() - 11,
                (int) (
                    zombie.getWidth()
                    * (double) Math.max(0, zombie.getHp())
                    / zombie.getMaxHp()
                ),
                6
            );
        }

        // แอนิเมชันศัตรูตาย
        for (Zombie zombie : dyingZombies) {
            if (zombie instanceof BossCombat) {
                ((BossCombat) zombie).draw(
                    g,
                    camera,
                    player.getX() > zombie.getX()
                );

                continue;
            }

            ZombieAnimation animation =
                zombieAnimations.get(zombie);

            if (animation != null) {
                animation.draw(g, zombie, player, camera);
            }
        }

        drawWeaponDrops(g);

        for (SupplyPickup supply : supplies) {
            supply.draw(g, camera, System.currentTimeMillis());
        }

        for (Bullet bullet : bullets) {
            bullet.draw(g, camera);
        }

        drawPlayerHUD(g);

        // แถบพลังชีวิตบอส
        for (Zombie boss : zombies) {
            if (boss instanceof BossCombat) {
                int x = getWidth() / 2 - 220;
                int y = getHeight() - 55;

                g.setColor(new Color(12, 15, 22, 225));
                g.fillRoundRect(x - 12, y - 27, 464, 58, 8, 8);

                g.setFont(
                    new Font(Font.MONOSPACED, Font.BOLD, 13)
                );

                g.setColor(Color.ORANGE);
                g.drawString(
                    boss.getKind()
                        + "  "
                        + boss.getHp()
                        + " / "
                        + boss.getMaxHp()
                        + (boss.getHp() < boss.getMaxHp() / 2
                            ? "  ENRAGED"
                            : ""),
                    x,
                    y - 8
                );

                g.setColor(Color.DARK_GRAY);
                g.fillRect(x, y, 440, 13);

                g.setColor(new Color(220, 60, 60));
                g.fillRect(
                    x,
                    y,
                    440 * Math.max(0, boss.getHp())
                        / boss.getMaxHp(),
                    13
                );
            }
        }

        // ข้อมูลความคืบหน้าของด่าน
        g.setColor(new Color(10, 12, 20, 205));
        g.fillRoundRect(15, 12, 350, 94, 10, 10);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        g.setColor(Color.WHITE);

        g.drawString(
            "DISTANCE "
                + Math.min(
                    100,
                    player.getX() * 100 / (stage.worldWidth - 170)
                )
                + "%  |  KILLS "
                + killed
                + "/"
                + stage.quota,
            28,
            35
        );

        g.drawString(
            "CREDITS $" + credits + "  |  SPACE: DOUBLE JUMP",
            28,
            57
        );

        g.drawString(
            "SHIFT: DASH "
                + (player.getDashCooldown() == 0 ? "READY" : "WAIT")
                + " | Q: SHOP",
            28,
            78
        );

        g.setColor(Color.DARK_GRAY);
        g.fillRect(28, 90, 320, 5);

        g.setColor(stage.accent.brighter());
        g.fillRect(
            28,
            90,
            Math.min(
                320,
                player.getX() * 320 / (stage.worldWidth - 170)
            ),
            5
        );

        // ข้อความแจ้งสถานะ
        if (System.currentTimeMillis() < bannerUntil) {
            g.setColor(Color.YELLOW);

            g.setFont(
                new Font(Font.MONOSPACED, Font.BOLD, 18)
            );

            g.drawString(banner, 24, 134);
        }

        if (shopOpen) {
            drawShop(g);
        }

        // หน้าจอจบเกม
        if (finished) {
            g.setColor(new Color(0, 0, 0, 210));
            g.fillRect(0, 0, screenWidth, screenHeight);

            g.setColor(won ? Color.GREEN : Color.RED);
            g.setFont(
                new Font(Font.MONOSPACED, Font.BOLD, 55)
            );

            g.drawString(
                won
                    ? (stage.level == 10 ? "YOU WIN!" : "STAGE CLEAR!")
                    : "GAME OVER",
                screenWidth / 2 - 215,
                screenHeight / 2 - 35
            );

            g.setColor(Color.WHITE);
            g.setFont(
                new Font(Font.MONOSPACED, Font.BOLD, 20)
            );

            g.drawString(
                won ? "Next stage unlocked!" : "Try again!",
                screenWidth / 2 - 135,
                screenHeight / 2 + 12
            );

            g.drawString(
                "Press ENTER to select stage",
                screenWidth / 2 - 180,
                screenHeight / 2 + 65
            );
        }

        g.dispose();
    }

    // ==================== HUD ผู้เล่น ====================

    private void drawPlayerHUD(Graphics2D g) {
        int width = 260;
        int height = 198;
        int x = getWidth() - 272;
        int y = 12;

        Graphics2D hud = (Graphics2D) g.create();

        hud.setColor(new Color(10, 12, 20, 220));
        hud.fillRoundRect(x, y, width, height, 12, 12);

        hud.setColor(new Color(75, 92, 110));
        hud.setStroke(new BasicStroke(2));
        hud.drawRoundRect(x, y, width, height, 12, 12);

        hud.setFont(
            new Font(Font.MONOSPACED, Font.BOLD, 14)
        );

        int hp = Math.max(0, Math.min(100, player.getHp()));
        int armor = Math.max(0, Math.min(100, player.getArmor()));

        // พลังชีวิต
        hud.setColor(new Color(255, 100, 105));
        hud.drawString("HP", x + 15, y + 23);

        hud.setColor(Color.WHITE);
        hud.drawString(hp + " / 100", x + 155, y + 23);

        hud.setColor(new Color(70, 25, 30));
        hud.fillRoundRect(x + 15, y + 30, 230, 12, 4, 4);

        hud.setColor(
            hp > 30
                ? new Color(235, 55, 65)
                : new Color(255, 145, 30)
        );

        hud.fillRoundRect(
            x + 15, y + 30,
            230 * hp / 100, 12,
            4, 4
        );

        // เกราะ
        Color armorColor = armor == 0
            ? new Color(145, 153, 165)
            : (armor <= 20
                ? new Color(255, 178, 70)
                : new Color(80, 185, 255));

        hud.setColor(armorColor);
        hud.drawString("ARMOR", x + 15, y + 65);

        hud.setColor(Color.WHITE);
        hud.drawString(armor + " / 100", x + 155, y + 65);

        hud.setColor(new Color(27, 45, 65));
        hud.fillRoundRect(x + 15, y + 72, 230, 12, 4, 4);

        hud.setColor(armorColor);
        hud.fillRoundRect(
            x + 15, y + 72,
            230 * armor / 100, 12,
            4, 4
        );

        if (armor == 0) {
            hud.setFont(
                new Font(Font.MONOSPACED, Font.BOLD, 10)
            );

            hud.setColor(armorColor);
            hud.drawString("EMPTY", x + 85, y + 64);

            hud.setFont(
                new Font(Font.MONOSPACED, Font.BOLD, 14)
            );
        }

        // พลังเกราะชีวภาพ
        MutationArmor mutation = player.getMutationArmor();
        int charge = mutation.getCharge();

        hud.setColor(new Color(190, 125, 255));
        hud.drawString("MUTATION", x + 15, y + 107);

        hud.setColor(Color.WHITE);
        hud.drawString(charge + " / 100", x + 155, y + 107);

        hud.setColor(new Color(46, 26, 65));
        hud.fillRoundRect(x + 15, y + 114, 230, 12, 4, 4);

        hud.setColor(
            mutation.isReady()
                ? new Color(225, 170, 255)
                : new Color(155, 85, 235)
        );

        hud.fillRoundRect(
            x + 15, y + 114,
            230 * charge / 100, 12,
            4, 4
        );

        if (mutation.isReady()) {
            hud.setColor(new Color(220, 160, 255));

            hud.drawRoundRect(
                x + 111, y + 94,
                24, 16,
                4, 4
            );

            hud.drawString("F", x + 119, y + 107);
        }

        // กระสุน
        Weapon weapon = weapons[selected];

        int ammo = Math.max(0, weapon.ammo());
        int maxAmmo = Math.max(1, weapon.magazine);

        hud.setColor(new Color(255, 200, 80));
        hud.drawString(weapon.name, x + 15, y + 149);

        hud.setColor(Color.WHITE);
        hud.drawString(
            ammo + " / " + maxAmmo,
            x + 155,
            y + 149
        );

        hud.setColor(new Color(70, 55, 25));
        hud.fillRoundRect(x + 15, y + 156, 230, 12, 4, 4);

        hud.setColor(new Color(255, 190, 50));
        hud.fillRoundRect(
            x + 15,
            y + 156,
            230 * Math.min(ammo, maxAmmo) / maxAmmo,
            12,
            4,
            4
        );

        if (weapon.isReloading()) {
            hud.setColor(Color.YELLOW);

            hud.setFont(
                new Font(Font.MONOSPACED, Font.BOLD, 12)
            );

            hud.drawString("RELOADING", x + 15, y + 188);
        }

        hud.dispose();
    }

    // ==================== วาดแขนและอาวุธ ====================

    private void drawEquippedWeapon(
            Graphics2D g,
            int playerX,
            int playerY) {

        if (player.isDead()) {
            PlayerAimRig.drawFallenArms(g, player, camera);
            return;
        }

        if (selected < 0
                || selected >= weaponImages.length
                || weaponImages[selected] == null) {

            return;
        }

        PlayerAimRig.Pose pose = PlayerAimRig.solve(
            player,
            mouseAim,
            camera
        );

        PlayerAimRig.drawArm(
            g,
            pose,
            camera,
            false,
            player.isFacingRight()
        );

        BioArmorVisuals.drawForearm(
            g,
            player,
            pose,
            camera,
            false
        );

        BioArmorVisuals.drawShoulder(
            g,
            player,
            pose,
            camera
        );

        Graphics2D gun = (Graphics2D) g.create();

        gun.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        gun.translate(pose.handX - camera, pose.handY);
        gun.rotate(pose.angle);

        if (Math.cos(pose.angle) < 0) {
            gun.scale(1, -1);
        }

        gun.drawImage(
            weaponImages[selected],
            -GUN_WIDTHS[selected] / 4,
            -GUN_HEIGHTS[selected] / 2,
            GUN_WIDTHS[selected],
            GUN_HEIGHTS[selected],
            null
        );

        gun.dispose();
    }
}