import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * แท่นที่เคลื่อนที่ไปกลับในแนวนอนหรือแนวตั้ง
 */
public final class MovingPlatform extends Platform {

    private final int originX;
    private final int originY;

    // ระยะเคลื่อนที่สูงสุดจากจุดเริ่มต้นในแต่ละทิศทาง
    private final int amplitude;

    // true = แนวตั้ง, false = แนวนอน
    private final boolean vertical;

    private int ticks;

    public MovingPlatform(
            int x,
            int y,
            int width,
            int amplitude,
            boolean vertical) {

        super(x, y, width);

        originX = x;
        originY = y;

        this.amplitude = amplitude;
        this.vertical = vertical;
    }

    /**
     * อัปเดตตำแหน่งให้เคลื่อนที่ไปกลับด้วยฟังก์ชัน sine
     */
    @Override
    public void update() {
        // บันทึกตำแหน่งก่อนเคลื่อนที่ผ่านคลาสแม่
        super.update();

        ticks++;

        int offset = (int) Math.round(
            Math.sin(ticks * 0.025) * amplitude
        );

        if (vertical) {
            y = originY + offset;
        } else {
            x = originX + offset;
        }
    }

    /**
     * วาดแนวทางเคลื่อนที่ ตัวแท่น และข้อความ
     */
    @Override
    public void draw(Graphics2D g, int camera, Color accent) {
        Graphics2D copy = (Graphics2D) g.create();

        // เส้นประแสดงแนวทางเคลื่อนที่
        copy.setColor(new Color(85, 195, 225, 90));

        copy.setStroke(new BasicStroke(
            1,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10,
            new float[] {5, 5},
            0
        ));

        if (vertical) {
            int centerX = originX + width / 2 - camera;

            copy.drawLine(
                centerX,
                originY - amplitude,
                centerX,
                originY + amplitude + 25
            );
        } else {
            copy.drawLine(
                originX - amplitude - camera,
                originY + 28,
                originX + width + amplitude - camera,
                originY + 28
            );
        }

        // วาดตัวแท่นด้วยเมธอดของคลาสแม่
        super.draw(copy, camera, new Color(85, 215, 245));

        // ข้อความระบุชนิดการเคลื่อนที่
        copy.setColor(Color.CYAN);
        copy.setFont(
            new Font(Font.MONOSPACED, Font.BOLD, 12)
        );

        copy.drawString(
            vertical ? "LIFT" : "<  MOVING  >",
            x - camera + width / 2 - 40,
            y + 15
        );

        copy.dispose();
    }
}