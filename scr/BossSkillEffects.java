import java.awt.*;
import java.awt.geom.Path2D;

/**
 * วาดเอฟเฟกต์สกิลของบอส
 * แยกจากการตรวจชนและการคำนวณความเสียหาย
 */
public final class BossSkillEffects {

    // สีประจำสกิลของบอสแต่ละด่าน
    private static final Color[] COLORS = {
        Color.ORANGE,
        Color.ORANGE,
        Color.CYAN,
        new Color(150, 230, 80),
        new Color(190, 170, 130),
        new Color(255, 70, 140),
        new Color(120, 230, 40),
        new Color(255, 215, 100),
        new Color(255, 95, 20),
        new Color(195, 85, 255)
    };

    /**
     * วาดพื้นที่เตือน กระสุน หรือเอฟเฟกต์สกิล
     */
    public static void draw(
            Graphics2D original,
            int skill,
            int x,
            int y,
            int w,
            int h,
            int age,
            boolean warning,
            boolean projectile,
            double vx,
            double vy) {

        Graphics2D g = (Graphics2D) original.create();

        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        int colorIndex = Math.max(1, Math.min(10, skill)) - 1;
        Color c = COLORS[colorIndex];

        // แสดงพื้นที่เตือนก่อนโจมตี
        if (warning) {
            int alpha = 55 + (int) (
                35 * (1 + Math.sin(age * 0.24))
            );

            g.setColor(new Color(
                c.getRed(),
                c.getGreen(),
                c.getBlue(),
                alpha
            ));

            g.fillRoundRect(x, y, w, h, 12, 12);

            g.setColor(c);
            g.setStroke(new BasicStroke(
                2,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                0,
                new float[] {8, 5},
                age % 13
            ));

            g.drawRoundRect(x, y, w, h, 12, 12);

            int centerX = x + w / 2;
            int centerY = y + h / 2;

            g.drawOval(centerX - 15, centerY - 15, 30, 30);

            g.drawLine(
                centerX - 22, centerY,
                centerX + 22, centerY
            );

            g.drawLine(
                centerX, centerY - 22,
                centerX, centerY + 22
            );

            g.dispose();
            return;
        }

        // แสดงกระสุนสกิลพร้อมหางตามทิศทางการเคลื่อนที่
        if (projectile) {
            double angle = Math.atan2(vy, vx);

            int centerX = x + w / 2;
            int centerY = y + h / 2;

            for (int i = 5; i > 0; i--) {
                int trailX =
                    centerX - (int) (Math.cos(angle) * i * 7);

                int trailY =
                    centerY - (int) (Math.sin(angle) * i * 7);

                g.setColor(new Color(
                    c.getRed(),
                    c.getGreen(),
                    c.getBlue(),
                    150 - i * 23
                ));

                g.fillOval(
                    trailX - w / 3,
                    trailY - h / 3,
                    w * 2 / 3,
                    h * 2 / 3
                );
            }

            if (skill == 5) {
                // ก้อนหินหมุนของบอสด่าน 5
                g.translate(centerX, centerY);
                g.rotate(age * 0.16);
                g.setColor(new Color(130, 110, 85));

                int[] pointsX = {
                    -w / 2, 0, w / 2, w / 3, -w / 3
                };

                int[] pointsY = {
                    0, -h / 2, -h / 5, h / 2, h / 2
                };

                g.fillPolygon(pointsX, pointsY, 5);

                g.setColor(new Color(230, 190, 130));
                g.drawLine(-w / 3, 0, w / 4, -h / 4);

            } else {
                // กระสุนพลังทั่วไป
                g.setColor(c);
                g.fillOval(x, y, w, h);

                g.setColor(new Color(255, 255, 225));
                g.fillOval(
                    x + w / 4,
                    y + h / 4,
                    w / 2,
                    h / 2
                );
            }

            g.dispose();
            return;
        }

        // เลือกรูปแบบเอฟเฟกต์ตามหมายเลขสกิล
        switch (skill) {

            // คลื่นกระแทก
            case 1: {
                g.setColor(new Color(255, 175, 50, 180));
                g.setStroke(new BasicStroke(3));

                for (int i = 0; i < 5; i++) {
                    int radius =
                        (age * 17 + i * 45) % Math.max(1, w / 2);

                    g.drawOval(
                        x + w / 2 - radius,
                        y + h / 2 - radius / 8,
                        radius * 2,
                        Math.max(6, radius / 4)
                    );
                }

                break;
            }

            // เปลวไฟและแมกมา
            case 2:
            case 9: {
                for (int i = 0; i < w; i += 14) {
                    double wave =
                        0.5 + 0.5 * Math.sin(age * 0.45 + i * 0.3);

                    int flame = (int) (h * (0.4 + 0.6 * wave));
                    int flameY = y + h - flame;

                    Path2D path = new Path2D.Double();

                    path.moveTo(x + i - 8, y + h);

                    path.curveTo(
                        x + i - 18, flameY + flame * 0.4,
                        x + i + 15, flameY + flame * 0.3,
                        x + i + 4, flameY
                    );

                    path.curveTo(
                        x + i + 30, flameY + flame * 0.4,
                        x + i + 17, y + h - 10,
                        x + i + 22, y + h
                    );

                    path.closePath();

                    g.setColor(new Color(
                        255, 70 + (i % 60), 15, 180
                    ));
                    g.fill(path);

                    g.setColor(new Color(255, 220, 70, 210));
                    g.fillOval(
                        x + i,
                        y + h - flame / 2,
                        10,
                        flame / 2
                    );
                }

                break;
            }

            // สายฟ้า
            case 3: {
                for (int bolt = 0; bolt < 3; bolt++) {
                    Path2D path = new Path2D.Double();
                    path.moveTo(x + w / 2, y);

                    for (int i = 1; i <= 12; i++) {
                        double jitter = Math.sin(
                            i * 8.1 + (age / 3) * 3.7 + bolt
                        ) * w * 0.42;

                        path.lineTo(
                            x + w / 2 + jitter,
                            y + h * i / 12.0
                        );
                    }

                    // แสงด้านนอก
                    g.setColor(new Color(55, 150, 255, 70));
                    g.setStroke(new BasicStroke(12));
                    g.draw(path);

                    // ตัวสายฟ้า
                    g.setColor(Color.CYAN);
                    g.setStroke(new BasicStroke(4));
                    g.draw(path);

                    // แกนแสงด้านใน
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(1.5f));
                    g.draw(path);
                }

                break;
            }

            // หนาม
            case 4: {
                g.setColor(new Color(125, 210, 80, 220));

                for (int i = 0; i < 8; i++) {
                    int spikeX = x + i * w / 8;

                    g.fillPolygon(
                        new int[] {
                            spikeX,
                            spikeX + 8,
                            spikeX + 16
                        },
                        new int[] {
                            y + h,
                            y - (i % 3) * 6,
                            y + h
                        },
                        3
                    );
                }

                break;
            }

            // วงพลังดูด
            case 6: {
                int centerX = x + w / 2;
                int centerY = y + h / 2;

                g.setColor(new Color(150, 25, 100, 35));
                g.fillOval(x, y, w, h);

                g.setColor(new Color(255, 60, 130, 150));
                g.setStroke(new BasicStroke(3));

                for (int i = 0; i < 4; i++) {
                    int radius = (age * 5 + i * 45) % (w / 2);

                    g.drawOval(
                        centerX - radius,
                        centerY - radius,
                        radius * 2,
                        radius * 2
                    );
                }

                for (int i = 0; i < 18; i++) {
                    double angle = i * 2.399 + age * 0.04;
                    int radius = (age * 3 + i * 17) % (w / 2);

                    g.fillOval(
                        centerX + (int) (Math.cos(angle) * radius),
                        centerY + (int) (
                            Math.sin(angle) * radius * 0.7
                        ),
                        5,
                        5
                    );
                }

                break;
            }

            // พื้นที่พิษและฟองลอย
            case 7: {
                g.setColor(new Color(90, 180, 15, 190));
                g.fillOval(x, y, w, h);

                g.setColor(new Color(175, 250, 55));
                g.drawOval(x, y, w, h);

                for (int i = 0; i < 7; i++) {
                    int bubbleX =
                        x + (i * 29 + age / 2) % Math.max(1, w - 10);

                    int rise = (age + i * 11) % 40;
                    int size = 5 + rise / 4;

                    g.setColor(new Color(
                        150, 240, 55, 190 - rise * 4
                    ));

                    g.drawOval(
                        bubbleX,
                        y - rise / 2,
                        size,
                        size
                    );
                }

                break;
            }

            // เอฟเฟกต์พื้นฐานสำหรับสกิลอื่น
            default: {
                g.setColor(new Color(
                    c.getRed(),
                    c.getGreen(),
                    c.getBlue(),
                    110
                ));

                g.fillOval(x, y, w, h);
                break;
            }
        }

        g.dispose();
    }

    /**
     * วาดวงกระแทกและเส้นพลังแผ่ออกจากจุดปะทะ
     */
    public static void impact(
            Graphics2D g,
            int x,
            int y,
            int radius,
            Color color,
            int life) {

        g.setColor(new Color(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            Math.min(255, life * 18)
        ));

        g.setStroke(new BasicStroke(3));

        g.drawOval(
            x - radius,
            y - radius,
            radius * 2,
            radius * 2
        );

        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5;

            int deltaX = (int) (Math.cos(angle) * radius);
            int deltaY = (int) (Math.sin(angle) * radius);

            g.drawLine(
                x + deltaX / 2,
                y + deltaY / 2,
                x + deltaX,
                y + deltaY
            );
        }
    }
}