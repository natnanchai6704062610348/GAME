import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * แสดงเกราะชีวภาพบนไหล่และแขนของผู้เล่น
 * รวมถึงออร่า เส้นพลัง และเอฟเฟกต์ปล่อยพลัง
 */
public final class BioArmorVisuals {

    // แถวที่ 0: เกราะไหล่ / แถวที่ 1: เกราะแขน
    // แต่ละแถวมีภาพเกราะ 3 ระดับ
    private static final BufferedImage[][] PIECES = load();

    /**
     * โหลดภาพเกราะ แล้วแบ่งเป็นชิ้นส่วน
     */
    private static BufferedImage[][] load() {
        BufferedImage[][] result = new BufferedImage[2][3];

        String[] roots = {
            "images/EFFECT/",
            "../images/EFFECT/"
        };

        for (String root : roots) {
            try {
                File file = new File(root + "bio-armor-parts-v1.png");

                if (!file.isFile()) {
                    continue;
                }

                BufferedImage sheet = ImageIO.read(file);

                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < 3; col++) {
                        // แบ่งภาพเป็นตาราง 2 แถว 3 คอลัมน์
                        int left = col * sheet.getWidth() / 3;
                        int top = row * sheet.getHeight() / 2;

                        int width =
                            (col + 1) * sheet.getWidth() / 3 - left;

                        int height =
                            (row + 1) * sheet.getHeight() / 2 - top;

                        BufferedImage cell = sheet.getSubimage(
                            left, top, width, height
                        );

                        // หาขอบเขตของภาพที่ไม่โปร่งใส
                        int minX = cell.getWidth();
                        int minY = cell.getHeight();
                        int maxX = -1;
                        int maxY = -1;

                        for (int y = 0; y < cell.getHeight(); y++) {
                            for (int x = 0; x < cell.getWidth(); x++) {
                                int alpha = cell.getRGB(x, y) >>> 24;

                                if (alpha > 40) {
                                    minX = Math.min(minX, x);
                                    minY = Math.min(minY, y);
                                    maxX = Math.max(maxX, x);
                                    maxY = Math.max(maxY, y);
                                }
                            }
                        }

                        // ตัดพื้นที่โปร่งใสรอบชิ้นส่วนออก
                        if (maxX >= minX) {
                            result[row][col] = cell.getSubimage(
                                minX,
                                minY,
                                maxX - minX + 1,
                                maxY - minY + 1
                            );
                        }
                    }
                }

                break;

            } catch (Exception e) {
                System.err.println(
                    "Bio armor art: " + e.getMessage()
                );
            }
        }

        return result;
    }

    /**
     * ตรวจสอบว่าโหลดภาพเกราะครบทุกชิ้นหรือไม่
     */
    public static boolean isLoaded() {
        for (BufferedImage[] row : PIECES) {
            for (BufferedImage image : row) {
                if (image == null) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * แสดงเกราะเมื่อผู้เล่นยังมีเกราะและยังไม่เสียชีวิต
     */
    private static boolean visible(Player player) {
        return player.getArmor() > 0 && !player.isDead();
    }

    /**
     * วาดเกราะไหล่และเส้นพลังเรืองแสง
     */
    public static void drawShoulder(
            Graphics2D original,
            Player player,
            PlayerAimRig.Pose pose,
            int camera) {

        if (!visible(player)) {
            return;
        }

        MutationArmor mutation = player.getMutationArmor();
        int stage = mutation.getVisualStage();

        int centerX = (int) pose.shoulderX - camera;
        int centerY = (int) pose.shoulderY;

        int width = 24 + stage * 5;
        int height = 20 + stage * 5;

        BufferedImage piece = PIECES[0][stage];
        Graphics2D g = (Graphics2D) original.create();

        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        if (piece != null) {
            if (player.isFacingRight()) {
                g.drawImage(
                    piece,
                    centerX - width / 2,
                    centerY - height / 2 - 3,
                    width,
                    height,
                    null
                );
            } else {
                // ใช้ความกว้างติดลบเพื่อกลับภาพในแนวนอน
                g.drawImage(
                    piece,
                    centerX + width / 2,
                    centerY - height / 2 - 3,
                    -width,
                    height,
                    null
                );
            }
        } else {
            // วาดเกราะสำรองหากโหลดภาพไม่ได้
            g.setColor(new Color(27, 17, 42));
            g.fillOval(centerX - 12, centerY - 12, 25, 22);
        }

        // ปรับความโปร่งใสให้เส้นพลังดูเต้นเป็นจังหวะ
        int pulse = 100 + (int) (
            60 * (1 + Math.sin(mutation.getAnimationTicks() * 0.16))
        );

        g.setColor(new Color(170, 100, 255, pulse));
        g.setStroke(new BasicStroke(1.2f));

        g.drawPolyline(
            new int[] {
                centerX - 7,
                centerX - 3,
                centerX + 1,
                centerX + 5
            },
            new int[] {
                centerY - 5,
                centerY - 8,
                centerY - 1,
                centerY - 3
            },
            4
        );

        g.dispose();
    }

    /**
     * วาดเกราะท่อนแขนให้หมุนตามท่าเล็ง
     */
    public static void drawForearm(
            Graphics2D original,
            Player player,
            PlayerAimRig.Pose pose,
            int camera,
            boolean rear) {

        if (!visible(player)) {
            return;
        }

        int stage = player.getMutationArmor().getVisualStage();

        // ตำแหน่งหัวไหล่
        double shoulderX = pose.shoulderX - camera
            + (rear ? (player.isFacingRight() ? -5 : 5) : 0);

        double shoulderY = pose.shoulderY + (rear ? 3 : 0);

        // ตำแหน่งมือ
        double handX = pose.handX - camera
            + (rear ? Math.cos(pose.angle) * 10 : 0);

        double handY = pose.handY
            + (rear ? Math.sin(pose.angle) * 10 + 2 : 0);

        // คำนวณตำแหน่งข้อศอกจากระยะระหว่างไหล่กับมือ
        double deltaX = handX - shoulderX;
        double deltaY = handY - shoulderY;
        double length = Math.max(1, Math.hypot(deltaX, deltaY));

        double bend = Math.sqrt(
            Math.max(0, 19 * 19 - length * length / 4)
        );

        double direction = deltaX >= 0 ? 1 : -1;

        double elbowX = (shoulderX + handX) / 2
            - deltaY / length * bend * direction;

        double elbowY = (shoulderY + handY) / 2
            + deltaX / length * bend * direction;

        Graphics2D g = (Graphics2D) original.create();

        g.translate(elbowX, elbowY);
        g.rotate(Math.atan2(handY - elbowY, handX - elbowX));

        int width = (int) Math.hypot(
            handX - elbowX,
            handY - elbowY
        ) + 5;

        int height = 10 + stage * 3;
        BufferedImage piece = PIECES[1][stage];

        if (piece != null) {
            g.drawImage(
                piece, -2, -height / 2, width, height, null
            );
        } else {
            g.setColor(new Color(28, 16, 42));
            g.fillRoundRect(-2, -6, width, 12, 5, 5);
        }

        // วาดเส้นพลังบนเกราะแขน
        g.setColor(new Color(190, 120, 255, 190));
        g.setStroke(new BasicStroke(1.2f));

        g.drawPolyline(
            new int[] {0, width / 3, width / 2, width - 4},
            new int[] {0, -2, 2, 0},
            4
        );

        g.dispose();
    }

    /**
     * วาดออร่าเมื่อมีพลังสะสมอย่างน้อย 40
     */
    public static void drawAura(
            Graphics2D original,
            Player player,
            int camera) {

        if (!visible(player)) {
            return;
        }

        MutationArmor mutation = player.getMutationArmor();

        if (mutation.getCharge() < 40) {
            return;
        }

        Graphics2D g = (Graphics2D) original.create();

        int centerX = player.getX() + player.getWidth() / 2 - camera;
        int centerY = player.getY() + player.getHeight() / 2;
        int radius = mutation.isReady() ? 70 : 48;

        // ไล่สีออร่ารอบตัวผู้เล่น
        g.setPaint(new RadialGradientPaint(
            new Point(centerX, centerY),
            radius,
            new float[] {0, 0.65f, 1},
            new Color[] {
                new Color(130, 40, 225, 0),
                new Color(
                    135, 55, 245,
                    mutation.isReady() ? 35 : 18
                ),
                new Color(130, 40, 225, 0)
            }
        ));

        g.fillOval(
            centerX - radius,
            centerY - radius,
            radius * 2,
            radius * 2
        );

        // วาดอนุภาคพลังที่เคลื่อนรอบตัว
        g.setColor(new Color(195, 120, 255, 160));

        int particleCount = mutation.isReady() ? 12 : 5;

        for (int i = 0; i < particleCount; i++) {
            double angle =
                i * 2.399 + mutation.getAnimationTicks() * 0.025;

            int distance = 30 + (i * 7) % 32;

            int particleX =
                centerX + (int) (Math.cos(angle) * distance);

            int particleY =
                centerY + (int) (Math.sin(angle) * distance);

            g.fillOval(particleX, particleY, 2, 3);
        }

        g.dispose();
    }

    /**
     * วาดวงพลังและหนามขณะปล่อยสกิลเกราะ
     */
    public static void drawRelease(
            Graphics2D original,
            Player player,
            int camera) {

        int remaining = player.getMutationArmor().getReleaseTicks();

        if (remaining <= 0) {
            return;
        }

        Graphics2D g = (Graphics2D) original.create();

        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        int age = 24 - remaining;
        int radius = 18 + age * (MutationArmor.RADIUS - 18) / 23;

        int centerX = player.getX() + player.getWidth() / 2 - camera;
        int centerY = player.getY() + player.getHeight() / 2;

        // วาดวงพลังที่ขยายออกจากตัวผู้เล่น
        g.setColor(new Color(165, 80, 255, remaining * 8));
        g.setStroke(new BasicStroke(4));

        g.drawOval(
            centerX - radius,
            centerY - radius,
            radius * 2,
            radius * 2
        );

        // วาดหนามรอบวงพลัง
        for (int i = 0; i < 18; i++) {
            double angle = i * Math.PI / 9;
            int tip = radius + 16;

            Path2D thorn = new Path2D.Double();

            thorn.moveTo(
                centerX + Math.cos(angle - 0.035) * (radius - 13),
                centerY + Math.sin(angle - 0.035) * (radius - 13)
            );

            thorn.lineTo(
                centerX + Math.cos(angle) * tip,
                centerY + Math.sin(angle) * tip
            );

            thorn.lineTo(
                centerX + Math.cos(angle + 0.035) * (radius - 13),
                centerY + Math.sin(angle + 0.035) * (radius - 13)
            );

            thorn.closePath();

            g.setColor(new Color(55, 22, 80, remaining * 9));
            g.fill(thorn);

            g.setColor(new Color(225, 160, 255, remaining * 9));
            g.draw(thorn);
        }

        g.dispose();
    }
}