import java.awt.*;

/**
 * บอส IRON MAW
 * สลับระหว่างการพุ่งชนและกระโดดทุบพื้น
 *
 * เวลาพฤติกรรมนับเป็นรอบอัปเดตเกม
 * เมื่อเปิดร้านค้า การโจมตีจึงหยุดตามการอัปเดตเกม
 */
public final class MutantBoss extends Boss {

    // สถานะพฤติกรรมของบอส
    public enum Attack {
        CHASE,
        WARN_CHARGE,
        CHARGE,
        WARN_SLAM,
        LEAP,
        SHOCK,
        RECOVER
    }

    private static final BossSpriteAnimation ART =
        new BossSpriteAnimation();

    // เวลาแสดงภาพบาดเจ็บและภาพตาย
    private long hurtUntil;
    private long deathStarted;

    // ระยะทางเดินสะสมสำหรับเลือกเฟรมเดิน
    private double walkDistance;

    private Attack attack = Attack.CHASE;

    private int ticks = 70;
    private int direction = -1;

    private final int worldWidth;

    // ตำแหน่งเริ่มกระโดดและตำแหน่งเป้าหมาย
    private double leapStart;
    private double targetX;

    // กำหนดรูปแบบโจมตีครั้งถัดไป
    private boolean slamNext;

    // ป้องกันสกิลเดียวสร้างความเสียหายซ้ำ
    private boolean hitDealt;

    // ==================== Constructor ====================

    public MutantBoss(int x, int level, int worldWidth) {
        super(x, "IRON MAW", level);

        this.worldWidth = worldWidth;

        width = 120;
        height = 170;

        maxHp = 550 + level * 90;
        hp = maxHp;

        damage = 24 + level;
        speed = 1.5;
    }

    // ==================== การรับความเสียหาย ====================

    @Override
    public void hurt(int amount) {
        super.hurt(amount);

        hurtUntil = System.currentTimeMillis() + 170;
    }

    @Override
    public void startDeath() {
        super.startDeath();

        deathStarted = System.currentTimeMillis();
    }

    // ==================== การเลือกเฟรมแอนิเมชัน ====================

    private int animationFrame() {
        if (isDying()) {
            int deathFrame = Math.min(
                3,
                (int) (
                    (System.currentTimeMillis() - deathStarted) / 180
                )
            );

            return 12 + deathFrame;
        }

        if (System.currentTimeMillis() < hurtUntil) {
            return 12;
        }

        switch (attack) {
            case WARN_CHARGE:
                return 4;

            case CHARGE:
                return 5 + (34 - ticks) / 4 % 3;

            case WARN_SLAM:
                return 8;

            case LEAP:
                return 9;

            case SHOCK:
                return ticks > 12 ? 10 : 11;

            case RECOVER:
                return 0;

            default:
                return (int) (walkDistance / 12) % 4;
        }
    }

    public Attack getAttack() {
        return attack;
    }

    /**
     * เปลี่ยนสถานะและกำหนดระยะเวลา
     */
    private void enter(Attack next, int duration) {
        attack = next;
        ticks = duration;
        hitDealt = false;
    }

    // ==================== อัปเดตพฤติกรรม ====================

    @Override
    public void update(Player player) {
        if (dead()) {
            return;
        }

        // เคลื่อนที่ตามสถานะปัจจุบัน
        switch (attack) {
            case CHASE: {
                double previousX = x;

                pursue(player);

                walkDistance += Math.abs(x - previousX);
                break;
            }

            case CHARGE:
                x += direction * (isEnraged() ? 12 : 9);
                break;

            case LEAP: {
                double progress = (40 - ticks + 1) / 40.0;

                x = leapStart
                    + (targetX - leapStart) * progress;

                break;
            }

            default:
                break;
        }

        // จำกัดตำแหน่งไม่ให้ออกจากด่าน
        x = Math.max(
            0,
            Math.min(worldWidth - width, x)
        );

        // ยังไม่เปลี่ยนสถานะจนกว่าจะครบเวลา
        if (--ticks > 0) {
            return;
        }

        // เปลี่ยนสถานะเมื่อครบเวลา
        switch (attack) {
            case CHASE:
                direction = player.getX() > x ? 1 : -1;

                targetX = Math.max(
                    0,
                    Math.min(
                        worldWidth - width,
                        player.getX()
                            + player.getWidth() / 2
                            - width / 2
                    )
                );

                enter(
                    slamNext
                        ? Attack.WARN_SLAM
                        : Attack.WARN_CHARGE,
                    50
                );

                break;

            case WARN_CHARGE:
                enter(Attack.CHARGE, 34);
                break;

            case CHARGE:
                slamNext = true;
                enter(Attack.RECOVER, 45);
                break;

            case WARN_SLAM:
                leapStart = x;
                enter(Attack.LEAP, 40);
                break;

            case LEAP:
                enter(Attack.SHOCK, 25);
                break;

            case SHOCK:
                slamNext = false;
                enter(Attack.RECOVER, 55);
                break;

            case RECOVER:
                enter(
                    Attack.CHASE,
                    isEnraged() ? 35 : 65
                );
                break;
        }
    }

    /**
     * คำนวณตำแหน่งแนวตั้งขณะกระโดด
     */
    @Override
    public int getY() {
        if (attack == Attack.LEAP) {
            double progress = (40 - ticks) / 40.0;

            int jumpHeight = (int) (
                Math.sin(Math.PI * progress) * 200
            );

            return 470 - height - jumpHeight;
        }

        return 470 - height;
    }

    // ==================== ตรวจการโจมตีพิเศษ ====================

    /**
     * การโจมตีพิเศษหนึ่งครั้งโดนผู้เล่นได้ไม่เกินหนึ่งครั้ง
     */
    @Override
    public boolean specialHits(Player player) {
        if (dead() || hitDealt) {
            return false;
        }

        boolean hit = false;

        // ตรวจการชนตัวบอสขณะพุ่ง
        if (attack == Attack.CHARGE) {
            hit = player.getX() + player.getWidth() > x
                && player.getX() < x + width
                && player.getY() + player.getHeight() > getY()
                && player.getY() < getY() + height;
        }

        // คลื่นกระแทกโดนผู้เล่นที่อยู่ใกล้พื้น
        if (attack == Attack.SHOCK) {
            int playerCenterX =
                player.getX() + player.getWidth() / 2;

            double bossCenterX = x + width / 2;

            hit = Math.abs(playerCenterX - bossCenterX) < 260
                && player.getY() + player.getHeight() >= 450;
        }

        if (hit) {
            hitDealt = true;
        }

        return hit;
    }

    // ==================== รางวัล ====================

    @Override
    public int getReward() {
        return 500;
    }

    @Override
    public double getDropChance() {
        return 1.0;
    }

    // ==================== วาดบอส ====================

    public void draw(Graphics2D g, int camera) {
        draw(g, camera, direction > 0);
    }

    @Override
    public void draw(
            Graphics2D g,
            int camera,
            boolean facingRight) {

        int screenX = getX() - camera;
        int screenY = getY();

        // พื้นที่เตือนก่อนพุ่งชน
        if (attack == Attack.WARN_CHARGE) {
            g.setColor(new Color(255, 65, 45, 80));

            g.fillRect(
                direction < 0
                    ? screenX - 430
                    : screenX + width,
                420,
                430,
                50
            );
        }

        // พื้นที่เป้าหมายของการกระโดดทุบ
        if (attack == Attack.WARN_SLAM
                || attack == Attack.LEAP) {

            int targetScreenX =
                (int) targetX + width / 2 - camera;

            g.setColor(new Color(255, 70, 40, 95));

            g.fillOval(
                targetScreenX - 260,
                444,
                520,
                26
            );

            g.setColor(Color.ORANGE);

            g.drawLine(
                targetScreenX - 260,
                465,
                targetScreenX + 260,
                465
            );
        }

        // ฝุ่นด้านหลังขณะพุ่ง
        if (attack == Attack.CHARGE) {
            Graphics2D dust = (Graphics2D) g.create();

            dust.setColor(new Color(195, 160, 110, 90));

            for (int i = 0; i < 5; i++) {
                dust.fillOval(
                    screenX + width / 2
                        - direction * (35 + i * 14),
                    450 - i * 2,
                    18 + i * 5,
                    14 + i * 2
                );
            }

            dust.dispose();
        }

        // เอฟเฟกต์คลื่นกระแทก
        if (attack == Attack.SHOCK) {
            BossSkillEffects.draw(
                g,
                1,
                screenX + width / 2 - 260,
                450,
                520,
                20,
                25 - ticks,
                false,
                false,
                0,
                0
            );
        }

        // วาดภาพบอสตามเฟรมปัจจุบัน
        boolean drawn = ART.draw(
            g,
            animationFrame(),
            screenX + width / 2,
            screenY + height,
            facingRight
        );

        // ภาพสำรองกรณีโหลด Sprite ไม่ได้
        if (!drawn) {
            g.setColor(new Color(75, 115, 95));

            g.fillRoundRect(
                screenX,
                screenY,
                width,
                height,
                20,
                20
            );

            g.setColor(Color.RED);

            g.fillRect(
                screenX + 30,
                screenY + 20,
                14,
                8
            );

            g.fillRect(
                screenX + 75,
                screenY + 20,
                14,
                8
            );
        }

        // ข้อความเตือนการโจมตี
        g.setColor(Color.ORANGE);
        g.setFont(
            new Font(Font.MONOSPACED, Font.BOLD, 14)
        );

        if (attack == Attack.WARN_CHARGE) {
            g.drawString(
                "CHARGE!",
                screenX + 18,
                screenY - 24
            );
        }

        if (attack == Attack.WARN_SLAM) {
            g.drawString(
                "JUMP TO DODGE!",
                screenX - 10,
                screenY - 24
            );
        }
    }
}