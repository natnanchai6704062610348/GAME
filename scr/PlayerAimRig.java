import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * คำนวณตำแหน่งไหล่ มือ และปากกระบอกปืน
 * ใช้ข้อมูลร่วมกันสำหรับวาดอาวุธและสร้างกระสุน
 */
public final class PlayerAimRig {

    /**
     * เก็บตำแหน่งท่าเล็งในพิกัดโลกเกม
     */
    public static final class Pose {

        public final double shoulderX;
        public final double shoulderY;

        public final double handX;
        public final double handY;

        public final double angle;

        Pose(
                double shoulderX,
                double shoulderY,
                double handX,
                double handY,
                double angle) {

            this.shoulderX = shoulderX;
            this.shoulderY = shoulderY;
            this.handX = handX;
            this.handY = handY;
            this.angle = angle;
        }

        // ตำแหน่งปากกระบอกปืนในแนวนอน
        public double muzzleX(int gunWidth) {
            return handX + Math.cos(angle) * gunWidth * 0.75;
        }

        // ตำแหน่งปากกระบอกปืนในแนวตั้ง
        public double muzzleY(int gunWidth) {
            return handY + Math.sin(angle) * gunWidth * 0.75;
        }
    }

    /**
     * คำนวณท่าเล็งจากตำแหน่งผู้เล่นและเมาส์
     */
    public static Pose solve(
            Player player,
            MouseAim mouse,
            int camera) {

        // ตำแหน่งหัวไหล่
        double shoulderX = player.getX()
            + player.getWidth() / 2.0
            + (player.isFacingRight() ? -6 : 6);

        double shoulderY = player.getY() + 33;

        // มุมจากไหล่ไปยังเป้าหมาย
        double reachAngle = mouse.angle(
            shoulderX,
            shoulderY,
            camera,
            player.isFacingRight()
        );

        // ตำแหน่งมือที่ยื่นออกจากไหล่
        double handX =
            shoulderX + Math.cos(reachAngle) * 22;

        double handY =
            shoulderY + Math.sin(reachAngle) * 22 + 7;

        // คำนวณมุมเล็งจากมือไปยังเป้าหมายอีกครั้ง
        double angle = mouse.angle(
            handX,
            handY,
            camera,
            player.isFacingRight()
        );

        return new Pose(
            shoulderX,
            shoulderY,
            handX,
            handY,
            angle
        );
    }

    /**
     * วาดแขนตามท่าเล็ง
     * rear ระบุว่าเป็นแขนด้านหลังหรือไม่
     */
    public static void drawArm(
            Graphics2D original,
            Pose pose,
            int camera,
            boolean rear,
            boolean right) {

        Graphics2D g = (Graphics2D) original.create();

        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        // แปลงตำแหน่งไหล่เป็นพิกัดหน้าจอ
        double shoulderX = pose.shoulderX - camera
            + (rear ? (right ? -5 : 5) : 0);

        double shoulderY = pose.shoulderY
            + (rear ? 3 : 0);

        // ปรับตำแหน่งมือสำหรับแขนด้านหลัง
        double handX = pose.handX - camera
            + (rear ? Math.cos(pose.angle) * 10 : 0);

        double handY = pose.handY
            + (rear ? Math.sin(pose.angle) * 10 + 2 : 0);

        // ระยะและทิศทางระหว่างไหล่กับมือ
        double deltaX = handX - shoulderX;
        double deltaY = handY - shoulderY;

        double length = Math.max(
            1,
            Math.hypot(deltaX, deltaY)
        );

        // คำนวณความงอและตำแหน่งข้อศอก
        double bend = Math.sqrt(
            Math.max(0, 19 * 19 - length * length / 4)
        );

        double direction = deltaX >= 0 ? 1 : -1;

        double elbowX = (shoulderX + handX) / 2
            - deltaY / length * bend * direction;

        double elbowY = (shoulderY + handY) / 2
            + deltaX / length * bend * direction;

        // เส้นขอบแขน
        Color outline = new Color(12, 25, 30);

        segment(
            g,
            shoulderX, shoulderY,
            elbowX, elbowY,
            10,
            outline
        );

        segment(
            g,
            elbowX, elbowY,
            handX, handY,
            9,
            outline
        );

        // สีแขนด้านหลังเข้มกว่าแขนด้านหน้า
        Color sleeve = rear
            ? new Color(37, 63, 65)
            : new Color(62, 97, 97);

        segment(
            g,
            shoulderX, shoulderY,
            elbowX, elbowY,
            7,
            sleeve
        );

        segment(
            g,
            elbowX, elbowY,
            handX, handY,
            6,
            sleeve.brighter()
        );

        // มือ
        g.setColor(new Color(28, 32, 32));
        g.fillOval(
            (int) handX - 4,
            (int) handY - 4,
            8,
            8
        );

        g.dispose();
    }

    /**
     * วาดส่วนแขนเป็นเส้นที่มีปลายมน
     */
    private static void segment(
            Graphics2D g,
            double startX,
            double startY,
            double endX,
            double endY,
            float width,
            Color color) {

        g.setColor(color);

        g.setStroke(new BasicStroke(
            width,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        ));

        g.drawLine(
            (int) startX,
            (int) startY,
            (int) endX,
            (int) endY
        );
    }

    /**
     * วาดแขนในท่าล้มเมื่อผู้เล่นเสียชีวิต
     */
    public static void drawFallenArms(
            Graphics2D g,
            Player player,
            int camera) {

        Graphics2D copy = (Graphics2D) g.create();

        int centerX = player.getX()
            + player.getWidth() / 2
            - camera;

        int centerY = player.getY()
            + player.getHeight()
            - 10;

        Color sleeve = new Color(50, 80, 80);

        segment(
            copy,
            centerX, centerY - 7,
            centerX + 17, centerY,
            7,
            sleeve
        );

        segment(
            copy,
            centerX + 17, centerY,
            centerX + 29, centerY - 2,
            6,
            sleeve
        );

        copy.dispose();
    }
}