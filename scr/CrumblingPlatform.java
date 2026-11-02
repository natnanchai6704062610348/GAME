import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * แท่นที่พังหลังผู้เล่นเหยียบ และกลับมาใช้งานได้ภายหลัง
 */
public final class CrumblingPlatform extends Platform {

    // -1 หมายถึงยังไม่เริ่มนับเวลาพัง
    private int collapse = -1;

    // เวลารอก่อนแท่นกลับมา
    private int respawn;

    public CrumblingPlatform(int x, int y, int width) {
        super(x, y, width);
    }

    /**
     * อัปเดตเวลาพังและเวลารอเกิดใหม่
     */
    @Override
    public void update() {
        super.update();

        // แท่นกำลังรอเกิดใหม่
        if (respawn > 0) {
            respawn--;
            return;
        }

        // ลดเวลาพังเมื่อเริ่มนับแล้ว
        if (collapse > 0 && --collapse == 0) {
            respawn = 180;
            collapse = -1;
        }
    }

    // แท่นรองรับผู้เล่นได้เมื่อไม่ได้รอเกิดใหม่
    @Override
    public boolean isSolid() {
        return respawn == 0;
    }

    // เริ่มนับเวลาพังเมื่อผู้เล่นลงเหยียบครั้งแรก
    @Override
    public void onLand() {
        if (isSolid() && collapse < 0) {
            collapse = 48;
        }
    }

    public int getCollapseTicks() {
        return collapse;
    }

    /**
     * วาดแท่นพร้อมรอยแตก
     * หากแท่นพัง จะแสดงกรอบเส้นประแทน
     */
    @Override
    public void draw(Graphics2D g, int camera, Color accent) {
        Graphics2D copy = (Graphics2D) g.create();

        if (!isSolid()) {
            copy.setColor(new Color(190, 145, 85, 80));

            copy.setStroke(new BasicStroke(
                1,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10,
                new float[] {4, 6},
                0
            ));

            copy.drawRect(x - camera, y, width, 18);

            copy.dispose();
            return;
        }

        // ใช้การวาดพื้นฐานจาก Platform
        super.draw(copy, camera, new Color(230, 170, 85));

        // เพิ่มรอยแตกบนแท่น
        copy.setColor(new Color(255, 195, 100));
        copy.setStroke(new BasicStroke(2));

        for (int i = 30; i < width; i += 46) {
            int screenX = x - camera + i;

            copy.drawPolyline(
                new int[] {
                    screenX,
                    screenX + 8,
                    screenX + 2,
                    screenX + 11
                },
                new int[] {
                    y,
                    y + 5,
                    y + 10,
                    y + 18
                },
                4
            );
        }

        copy.dispose();
    }
}