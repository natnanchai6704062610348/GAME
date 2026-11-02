import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * จัดการกับดักภายในด่าน
 * ใช้รอบอัปเดตเกมควบคุมเวลา จึงหยุดตามเกมเมื่อเปิดร้านค้า
 */
public final class StageTrap {

    // ประเภทกับดัก
    public enum Type {
        SPIKES,
        FLAME,
        ELECTRIC,
        RUBBLE
    }

    // สถานะการทำงาน
    public enum Phase {
        SAFE,
        WARNING,
        ACTIVE
    }

    private final int x;
    private final int width = 86;
    private final int level;

    // ระยะเวลาแต่ละสถานะ นับเป็นรอบอัปเดต
    private final int idle;
    private final int warning;
    private final int active;

    private final Type type;

    private int tick;
    private int damageCooldown;

    // ==================== Constructor ====================

    public StageTrap(
            int x,
            Type type,
            int level,
            int offset) {

        this.x = x;
        this.type = type;
        this.level = level;

        // ด่านสูงขึ้นจะพักสั้นลงและทำงานนานขึ้น
        idle = 160 - level * 5;
        warning = 58 - level;
        active = 40 + level * 2;

        // เลื่อนจังหวะเริ่มต้นของกับดักแต่ละจุด
        tick = Math.floorMod(
            offset,
            idle + warning + active
        );
    }

    // ==================== ข้อมูลกับดัก ====================

    public int getX() {
        return x;
    }

    public Type getType() {
        return type;
    }

    public int getTick() {
        return tick;
    }

    /**
     * หาสถานะปัจจุบันจากตำแหน่งเวลาในวงจร
     */
    public Phase getPhase() {
        int cycleLength = idle + warning + active;
        int time = tick % cycleLength;

        if (time < idle) {
            return Phase.SAFE;
        }

        if (time < idle + warning) {
            return Phase.WARNING;
        }

        return Phase.ACTIVE;
    }

    public void update() {
        tick++;

        if (damageCooldown > 0) {
            damageCooldown--;
        }
    }

    /**
     * จำนวนรอบที่ผ่านไปนับจากเริ่มสถานะ ACTIVE
     */
    private int activeAge() {
        return tick % (idle + warning + active)
            - idle
            - warning;
    }

    // ==================== พื้นที่ตรวจการชน ====================

    private Rectangle hitbox() {
        switch (type) {
            case FLAME:
                return new Rectangle(
                    x,
                    360,
                    width,
                    110
                );

            case ELECTRIC:
                return new Rectangle(
                    x,
                    442,
                    width,
                    28
                );

            case RUBBLE:
                // ก้อนหินตกลงตามเวลาที่ผ่านไป
                return new Rectangle(
                    x + width / 2 - 19,
                    20 + activeAge() * 11,
                    38,
                    38
                );

            default:
                // หนาม
                return new Rectangle(
                    x,
                    442,
                    width,
                    28
                );
        }
    }

    /**
     * ตรวจการชนและสร้างความเสียหาย
     * คืนค่า true เมื่อพลังชีวิตหรือเกราะลดลงจริง
     */
    public boolean tryHit(Player player) {
        if (getPhase() != Phase.ACTIVE
                || damageCooldown > 0) {

            return false;
        }

        boolean hit = hitbox().intersects(
            player.getX() + 8,
            player.getY() + 8,
            player.getWidth() - 16,
            player.getHeight() - 8
        );

        if (hit) {
            damageCooldown = 48;

            int before =
                player.getHp() + player.getArmor();

            player.hurt(8 + level);

            return before
                > player.getHp() + player.getArmor();
        }

        return false;
    }

    // ==================== วาดกับดัก ====================

    public void draw(Graphics2D g, int camera) {
        int screenX = x - camera;

        // ข้ามการวาดเมื่ออยู่นอกพื้นที่ที่มองเห็น
        Rectangle clip = g.getClipBounds();

        if (clip != null
                && (screenX < clip.x - 150
                    || screenX > clip.x + clip.width + 180)) {

            return;
        }

        Graphics2D copy = (Graphics2D) g.create();
        Phase phase = getPhase();

        // สีประจำประเภทกับดัก
        Color color = type == Type.FLAME
            ? Color.ORANGE
            : (type == Type.ELECTRIC
                ? Color.CYAN
                : (type == Type.RUBBLE
                    ? new Color(215, 185, 135)
                    : new Color(230, 140, 100)));

        // ฐานกับดัก
        copy.setColor(new Color(17, 23, 29));

        copy.fillRoundRect(
            screenX - 4,
            461,
            width + 8,
            13,
            4,
            4
        );

        copy.setColor(new Color(90, 100, 110));
        copy.drawRect(screenX, 461, width, 10);

        // แถบไฟบนฐาน
        for (int i = 0; i < width; i += 18) {
            copy.setColor(
                phase == Phase.ACTIVE
                    ? color
                    : new Color(85, 110, 100)
            );

            copy.fillRect(screenX + i, 465, 9, 4);
        }

        // แสดงเอฟเฟกต์เฉพาะช่วงที่กับดักทำงาน
        if (phase == Phase.ACTIVE) {
            Rectangle bounds = hitbox();

            if (type == Type.FLAME) {
                BossSkillEffects.draw(
                    copy,
                    2,
                    screenX,
                    bounds.y,
                    width,
                    bounds.height,
                    tick,
                    false,
                    false,
                    0,
                    0
                );

            } else if (type == Type.ELECTRIC) {
                BossSkillEffects.draw(
                    copy,
                    3,
                    screenX,
                    bounds.y,
                    width,
                    bounds.height,
                    tick,
                    false,
                    false,
                    0,
                    0
                );

            } else if (type == Type.RUBBLE) {
                // ก้อนหินตก
                copy.setColor(new Color(110, 90, 70));

                copy.fillPolygon(
                    new int[] {
                        screenX + 24,
                        screenX + 35,
                        screenX + 61,
                        screenX + 57,
                        screenX + 30
                    },
                    new int[] {
                        bounds.y + 10,
                        bounds.y,
                        bounds.y + 10,
                        bounds.y + 36,
                        bounds.y + 38
                    },
                    5
                );

                copy.setColor(color);

                copy.drawLine(
                    screenX + 34,
                    bounds.y + 8,
                    screenX + 45,
                    bounds.y + 26
                );

                // เอฟเฟกต์ใกล้พื้น
                if (bounds.y > 430) {
                    BossSkillEffects.impact(
                        copy,
                        screenX + width / 2,
                        462,
                        20 + activeAge() % 12,
                        color,
                        9
                    );
                }

            } else {
                // หนามแหลม
                copy.setColor(new Color(210, 215, 220));

                for (int i = 0; i < width - 10; i += 14) {
                    copy.fillPolygon(
                        new int[] {
                            screenX + i,
                            screenX + i + 6,
                            screenX + i + 13
                        },
                        new int[] {
                            470,
                            442,
                            470
                        },
                        3
                    );
                }
            }
        }

        copy.dispose();
    }
}