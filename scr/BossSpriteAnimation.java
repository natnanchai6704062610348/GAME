import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * โหลดภาพ Sprite Sheet ของบอส IRON MAW
 * และแยกเป็นภาพแอนิเมชันทั้งหมด 16 เฟรม
 */
public final class BossSpriteAnimation {

    private final BufferedImage[] frames = new BufferedImage[16];

    /**
     * โหลดภาพจากตำแหน่งไฟล์ที่กำหนด
     */
    public BossSpriteAnimation() {
        String[] paths = {
            "images/BOSS/iron-maw-sprites-v1.png",
            "../images/BOSS/iron-maw-sprites-v1.png"
        };

        for (String path : paths) {
            try {
                File file = new File(path);

                if (!file.isFile()) {
                    continue;
                }

                load(ImageIO.read(file));
                return;

            } catch (Exception e) {
                System.err.println(
                    "Boss atlas: " + e.getMessage()
                );
            }
        }
    }

    /**
     * รับภาพที่โหลดไว้แล้วมาแบ่งเป็นเฟรม
     */
    public BossSpriteAnimation(BufferedImage image) {
        load(image);
    }

    /**
     * แบ่งภาพเป็น 4 แถว 4 คอลัมน์
     * และตัดพื้นที่โปร่งใสรอบแต่ละเฟรมออก
     */
    private void load(BufferedImage image) {

        // ขอบแถวอิงจากช่องว่างในภาพต้นฉบับ
        double[] rows = {
            0,
            320.0 / 1254,
            594.0 / 1254,
            936.0 / 1254,
            1
        };

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {

                // คำนวณขอบเขตของช่องภาพ
                int left = col * image.getWidth() / 4;
                int right = (col + 1) * image.getWidth() / 4;

                int top = (int) (
                    rows[row] * image.getHeight()
                );

                int bottom = (int) (
                    rows[row + 1] * image.getHeight()
                );

                BufferedImage cell = image.getSubimage(
                    left,
                    top,
                    right - left,
                    bottom - top
                );

                // หาขอบเขตพิกเซลที่มองเห็นได้
                int minX = cell.getWidth();
                int minY = cell.getHeight();
                int maxX = -1;
                int maxY = -1;

                for (int y = 0; y < cell.getHeight(); y++) {
                    for (int x = 0; x < cell.getWidth(); x++) {
                        int alpha = cell.getRGB(x, y) >>> 24;

                        if (alpha > 24) {
                            minX = Math.min(minX, x);
                            minY = Math.min(minY, y);
                            maxX = Math.max(maxX, x);
                            maxY = Math.max(maxY, y);
                        }
                    }
                }

                // บันทึกภาพที่ตัดขอบโปร่งใสแล้ว
                if (maxX >= minX) {
                    int frameIndex = row * 4 + col;

                    frames[frameIndex] = cell.getSubimage(
                        minX,
                        minY,
                        maxX - minX + 1,
                        maxY - minY + 1
                    );
                }
            }
        }
    }

    /**
     * ตรวจสอบว่าโหลดครบทั้ง 16 เฟรมหรือไม่
     */
    public boolean isLoaded() {
        for (BufferedImage frame : frames) {
            if (frame == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * วาดเฟรมที่เลือก โดยยึดตำแหน่งเท้าเป็นหลัก
     * คืนค่า false หากไม่มีภาพเฟรมนั้น
     */
    public boolean draw(
            Graphics2D g,
            int index,
            int centerX,
            int feetY,
            boolean facingRight) {

        int frameIndex = Math.max(0, Math.min(15, index));
        BufferedImage frame = frames[frameIndex];

        if (frame == null) {
            return false;
        }

        // ย่อภาพเหลือประมาณ 61% ของขนาดเฟรม
        int width = (int) (frame.getWidth() * 0.61);
        int height = (int) (frame.getHeight() * 0.61);

        Graphics2D copy = (Graphics2D) g.create();

        copy.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        if (facingRight) {
            // กลับภาพแนวนอนเพื่อให้หันขวา
            copy.drawImage(
                frame,
                centerX + width / 2,
                feetY - height,
                -width,
                height,
                null
            );
        } else {
            copy.drawImage(
                frame,
                centerX - width / 2,
                feetY - height,
                width,
                height,
                null
            );
        }

        copy.dispose();
        return true;
    }
}