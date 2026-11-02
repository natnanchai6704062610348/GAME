import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ควบคุมบอสของด่าน 2–10
 * ใช้วงจรสถานะร่วมกัน แต่เลือกสกิลตามหมายเลขด่าน
 */
public final class StageBoss extends Boss {

    public enum Phase {
        HUNT,
        WARNING,
        ATTACK,
        RECOVER
    }

    private static final StageBossArt ART = new StageBossArt();
    private static final BossWalkAtlas WALK = new BossWalkAtlas();

    private final int level;
    private final int worldWidth;
    private final Color color;

    private final List<BossHazard> hazards = new ArrayList<>();

    private Phase phase = Phase.HUNT;

    private int ticks = 85;
    private int damageCooldown;
    private int attackCount;
    private int summons;
    private int leapTicks;

    private double walkDistance;

    // ตำแหน่งเป้าหมาย
    private double targetX;
    private double targetY;

    // ข้อมูลการกระโดด
    private double leapStartX;
    private double leapEndX;
    private double leapHeight;

    private long deathAt;

    // ==================== Constructor ====================

    public StageBoss(int x, int level, int worldWidth) {
        super(x, BossFactory.name(level), level);

        this.level = level;
        this.worldWidth = worldWidth;

        width = level == 9 ? 150 : 120;
        height = level == 10 ? 205 : 180;

        maxHp = 580 + level * 115;
        hp = maxHp;

        damage = 18 + level * 2;
        speed = 1.1 + level * 0.1;

        Color[] colors = {
            Color.ORANGE,
            Color.CYAN,
            new Color(155, 215, 90),
            new Color(255, 190, 80),
            new Color(230, 90, 160),
            new Color(140, 230, 55),
            new Color(255, 100, 90),
            new Color(255, 100, 20),
            new Color(205, 110, 255)
        };

        color = colors[level - 2];
    }

    // ==================== ข้อมูลสถานะ ====================

    public Phase getPhase() {
        return phase;
    }

    public int getHazardCount() {
        return hazards.size();
    }

    /**
     * คืนจำนวนศัตรูที่ต้องเรียก แล้วล้างค่ารอเรียก
     */
    public int takeSummons() {
        int count = summons;
        summons = 0;

        return count;
    }

    @Override
    public int getReward() {
        return 350 + level * 100;
    }

    @Override
    public double getDropChance() {
        return 1;
    }

    @Override
    public void startDeath() {
        super.startDeath();

        deathAt = System.currentTimeMillis();
        hazards.clear();
    }

    /**
     * คำนวณความสูงขณะกระโดดด้วยเส้นโค้ง sine
     */
    @Override
    public int getY() {
        int jumpOffset = leapTicks > 0
            ? (int) (
                Math.sin(Math.PI * (40 - leapTicks) / 40.0)
                * leapHeight
            )
            : 0;

        return 470 - height - jumpOffset;
    }

    // ==================== อัปเดตพฤติกรรม ====================

    @Override
    public void update(Player player) {
        if (dead()) {
            hazards.clear();
            return;
        }

        if (damageCooldown > 0) {
            damageCooldown--;
        }

        // อัปเดตพื้นที่โจมตีและลบส่วนที่หมดอายุ
        for (BossHazard hazard : hazards) {
            hazard.update();
        }

        hazards.removeIf(BossHazard::expired);

        // เคลื่อนที่ระหว่างกระโดด
        if (leapTicks > 0) {
            double progress = (41 - leapTicks) / 40.0;

            x = leapStartX
                + (leapEndX - leapStartX) * progress;

            leapTicks--;
        }

        double beforeX = x;

        if (phase == Phase.HUNT) {
            pursue(player);
        }

        walkDistance += Math.abs(x - beforeX);

        // จำกัดตำแหน่งภายในด่าน
        x = Math.max(
            0,
            Math.min(worldWidth - width, x)
        );

        if (--ticks > 0) {
            return;
        }

        // เปลี่ยนสถานะเมื่อครบเวลา
        switch (phase) {
            case HUNT:
                targetX =
                    player.getX() + player.getWidth() / 2.0;

                targetY =
                    player.getY() + player.getHeight() / 2.0;

                phase = Phase.WARNING;
                ticks = isEnraged() ? 38 : 52;

                prepare();
                break;

            case WARNING:
                phase = Phase.ATTACK;
                ticks = level == 7 ? 100 : 60;

                attack();
                break;

            case ATTACK:
                phase = Phase.RECOVER;
                ticks = isEnraged() ? 35 : 55;
                break;

            case RECOVER:
                phase = Phase.HUNT;
                ticks = isEnraged() ? 35 : 65;
                break;
        }
    }

    private int warningTicks() {
        return isEnraged() ? 38 : 52;
    }

    // ==================== สร้างพื้นที่โจมตี ====================

    private void zone(
            double x,
            double y,
            int width,
            int height,
            int delay,
            int life) {

        hazards.add(
            new BossHazard(
                x,
                y,
                width,
                height,
                delay,
                life,
                color
            ).withSkill(level)
        );
    }

    /**
     * สร้างพื้นที่โจมตีล่วงหน้าตามชนิดบอส
     */
    private void prepare() {
        int warning = warningTicks();

        switch (level) {
            case 2:
                // เปลวไฟด้านที่ผู้เล่นอยู่
                zone(
                    targetX < x ? x - 360 : x + width,
                    370,
                    360,
                    100,
                    warning,
                    40
                );
                break;

            case 3:
                // สายฟ้า 3 ตำแหน่ง
                for (int i = -1; i <= 1; i++) {
                    zone(
                        targetX + i * 170 - 27,
                        0,
                        54,
                        470,
                        warning,
                        20
                    );
                }
                break;

            case 4:
                // พื้นที่กระแทกเมื่อกระโดดลง
                zone(
                    targetX - 65,
                    440,
                    130,
                    30,
                    warning + 40,
                    18
                );
                break;

            case 6:
                // พื้นที่ดูดพลังรอบบอส
                zone(
                    x + width / 2 - 220,
                    100,
                    440,
                    370,
                    warning,
                    32
                );
                break;

            case 7:
                // พื้นที่พิษ 3 จุด
                for (int i = -1; i <= 1; i++) {
                    zone(
                        targetX + i * 130 - 45,
                        targetY + 35,
                        90,
                        28,
                        warning + i * 6 + 6,
                        160
                    );
                }
                break;

            case 9:
                // รอยแยกแมกมา 5 จุด
                for (int i = -2; i <= 2; i++) {
                    zone(
                        targetX + i * 140 - 40,
                        145,
                        80,
                        325,
                        warning + (i + 2) * 13,
                        50
                    );
                }
                break;

            default:
                break;
        }
    }

    // ==================== สร้างกระสุนสกิล ====================

    private void shoot(
            double deltaX,
            double deltaY,
            double velocity,
            int radius,
            double gravity) {

        double length = Math.max(
            1,
            Math.hypot(deltaX, deltaY)
        );

        BossHazard hazard = new BossHazard(
            x + width / 2,
            getY() + 55,
            radius * 2,
            radius * 2,
            0,
            160,
            color,
            true
        ).withSkill(level);

        hazard.vx = deltaX / length * velocity;
        hazard.vy = deltaY / length * velocity;
        hazard.gravity = gravity;

        hazards.add(hazard);
    }

    // ==================== เริ่มการโจมตี ====================

    private void attack() {
        attackCount++;

        switch (level) {
            case 4:
                // กระโดดเข้าหาตำแหน่งเป้าหมาย
                leapStartX = x;

                leapEndX = Math.max(
                    0,
                    Math.min(
                        worldWidth - width,
                        targetX - width / 2
                    )
                );

                leapHeight = Math.min(
                    300,
                    Math.max(150, 470 - (targetY + 30))
                );

                leapTicks = 40;
                break;

            case 5:
                // ขว้างก้อนหิน 5 ก้อน
                for (int i = -2; i <= 2; i++) {
                    BossHazard hazard = new BossHazard(
                        x + width / 2,
                        getY() + 30,
                        28,
                        28,
                        0,
                        110,
                        color,
                        true
                    ).withSkill(level);

                    hazard.vx =
                        (targetX + i * 65 - hazard.x) / 50.0;

                    hazard.vy = -8 - i * 0.6;
                    hazard.gravity = 0.34;

                    hazards.add(hazard);
                }
                break;

            case 8:
                // ยิงกระสุนกระจาย 5 นัด
                for (int i = -2; i <= 2; i++) {
                    shoot(
                        targetX - (x + width / 2),
                        targetY - (getY() + 55) + i * 55,
                        8 + level * 0.2,
                        7,
                        0
                    );
                }
                break;

            case 10:
                // ยิงกรดพร้อมเรียกศัตรูเสริม
                for (int i = -1; i <= 1; i++) {
                    shoot(
                        targetX - (x + width / 2),
                        targetY - (getY() + 55) + i * 100,
                        7,
                        11,
                        0
                    );
                }

                summons = isEnraged() ? 3 : 2;
                break;

            default:
                break;
        }
    }

    // ==================== ตรวจการโจมตีโดนผู้เล่น ====================

    @Override
    public boolean specialHits(Player player) {
        if (dead() || damageCooldown > 0) {
            return false;
        }

        // ตรวจการชนกับตัวบอส
        boolean hit =
            player.getX() + player.getWidth() > x
            && player.getX() < x + width
            && player.getY() + player.getHeight() > getY()
            && player.getY() < getY() + height;

        // ตรวจการชนกับพื้นที่หรือกระสุนสกิล
        for (BossHazard hazard : hazards) {
            if (hazard.touches(player)) {
                hit = true;
                hazard.consume();
                break;
            }
        }

        if (hit) {
            damageCooldown = 38;

            // บอสด่าน 6 ฟื้นพลังเมื่อโจมตีโดน
            if (level == 6) {
                hp = Math.min(maxHp, hp + 45);
            }
        }

        return hit;
    }

    // ==================== วาดบอสและเอฟเฟกต์ ====================

    @Override
    public void draw(
            Graphics2D g,
            int camera,
            boolean right) {

        for (BossHazard hazard : hazards) {
            hazard.draw(g, camera);
        }

        // เส้นเล็งเตือนสำหรับบอสที่โจมตีด้วยกระสุน
        if (phase == Phase.WARNING
                && (level == 5 || level == 8 || level == 10)) {

            g.setColor(new Color(255, 190, 75, 150));

            g.drawLine(
                getX() + width / 2 - camera,
                getY() + 55,
                (int) targetX - camera,
                (int) targetY
            );

            g.drawOval(
                (int) targetX - camera - 20,
                (int) targetY - 20,
                40,
                40
            );
        }

        Graphics2D art = (Graphics2D) g.create();
        int drawHeight = height + 15;

        // ค่อย ๆ จางหายเมื่อบอสตาย
        if (isDying()) {
            float alpha = Math.max(
                0,
                1 - (System.currentTimeMillis() - deathAt) / 750f
            );

            art.setComposite(
                AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    alpha
                )
            );
        }

        int bob = 0;

        // เปลี่ยนเฟรมเดินตามระยะทางสะสม
        int frame = phase == Phase.HUNT
            ? (int) (walkDistance / 12) % 4
            : 0;

        // เอียงตัวเล็กน้อยขณะเตรียมโจมตี
        if (phase == Phase.WARNING) {
            double angle = (right ? 1 : -1)
                * Math.sin((52 - ticks) * 0.05)
                * 0.06;

            art.rotate(
                angle,
                getX() + width / 2 - camera,
                getY() + height
            );
        }

        // ลองวาดด้วยชุดภาพเดินก่อน แล้วใช้ภาพบอสสำรอง
        if (!WALK.draw(
                art,
                level,
                frame,
                getX() + width / 2 - camera,
                getY() + height + bob,
                drawHeight,
                right)
                && !ART.draw(
                    art,
                    level,
                    getX() + width / 2 - camera,
                    getY() + height + bob,
                    drawHeight,
                    right)) {

            art.setColor(color);

            art.fillRoundRect(
                getX() - camera,
                getY(),
                width,
                height,
                18,
                18
            );
        }

        art.dispose();

        // ข้อความเตือนสกิล
        if (phase == Phase.WARNING) {
            g.setFont(
                new Font(Font.MONOSPACED, Font.BOLD, 15)
            );

            g.setColor(color);

            g.drawString(
                attackLabel(),
                getX() - camera - 20,
                getY() - 22
            );
        }
    }

    // ==================== ชื่อการโจมตี ====================

    public String attackLabel() {
        switch (level) {
            case 2:
                return "FLAME SWEEP!";

            case 3:
                return "LIGHTNING! MOVE!";

            case 4:
                return "POUNCE!";

            case 5:
                return "ROCK VOLLEY!";

            case 6:
                return "DRAIN! KEEP AWAY!";

            case 7:
                return "TOXIC POOLS!";

            case 8:
                return "BULLET FAN!";

            case 9:
                return "MAGMA FISSURES!";

            default:
                return "SWARM + ACID!";
        }
    }
}