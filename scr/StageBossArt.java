import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * โหลดภาพบอสด่าน 2–10 จากภาพรวม 9 ช่อง
 * แยกภาพ ตัดขอบโปร่งใส และวาดบอสตามขนาดที่กำหนด
 */
public final class StageBossArt {

    private final BufferedImage[] frames = new BufferedImage[9];

    // ==================== Constructor ====================

    public StageBossArt() {
        String[] paths = {
            "images/BOSS/stage-bosses-v1.png",
            "../images/BOSS/stage-bosses-v1.png"
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
                    "Stage boss art: " + e.getMessage()
                );
            }
        }
    }

    public StageBossArt(BufferedImage sheet) {
        load(sheet);
    }

    // ==================== แบ่งภาพบอส ====================

    private void load(BufferedImage image) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {

                // ขอบแถวตามตำแหน่งภาพในไฟล์ต้นฉบับ
                double[] rows = {
                    0,
                    402.0 / 1254,
                    802.0 / 1254,
                    1
                };

                // แถวสุดท้ายใช้ขอบคอลัมน์ต่างจากแถวอื่น
                double[] cols = row == 2
                    ? new double[] {
                        0,
                        420.0 / 1254,
                        861.0 / 1254,
                        1
                    }
                    : new double[] {
                        0,
                        455.0 / 1254,
                        861.0 / 1254,
                        1
                    };

                int left = (int) (
                    cols[col] * image.getWidth()
                );

                int top = (int) (
                    rows[row] * image.getHeight()
                );

                int width = (int) (
                    cols[col + 1] * image.getWidth()
                ) - left;

                int height = (int) (
                    rows[row + 1] * image.getHeight()
                ) - top;

                BufferedImage cell = image.getSubimage(
                    left,
                    top,
                    width,
                    height
                );

                // ตัดเศษภาพที่ไม่เชื่อมต่อกับตัวบอส
                cell = mainComponent(cell);

                int minX = cell.getWidth();
                int minY = cell.getHeight();
                int maxX = -1;
                int maxY = -1;

                // หาขอบเขตพิกเซลที่มองเห็นได้
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

                // เก็บภาพที่ตัดขอบโปร่งใสแล้ว
                if (maxX >= minX) {
                    int index = row * 3 + col;

                    frames[index] = cell.getSubimage(
                        minX,
                        minY,
                        maxX - minX + 1,
                        maxY - minY + 1
                    );
                }
            }
        }
    }

    // ==================== ตัดเศษภาพ ====================

    /**
     * เก็บเฉพาะกลุ่มพิกเซลที่เชื่อมต่อกันขนาดใหญ่ที่สุด
     * ใช้ BFS ตรวจพิกเซลรอบข้างทั้ง 8 ทิศทาง
     */
    private BufferedImage mainComponent(BufferedImage cell) {
        int width = cell.getWidth();
        int height = cell.getHeight();
        int pixelCount = width * height;

        boolean[] seen = new boolean[pixelCount];
        int[] queue = new int[pixelCount];
        int[] largestComponent = new int[0];

        for (int start = 0; start < pixelCount; start++) {
            int startX = start % width;
            int startY = start / width;

            int alpha = cell.getRGB(startX, startY) >>> 24;

            if (seen[start] || alpha <= 40) {
                continue;
            }

            // เริ่มค้นหากลุ่มพิกเซลใหม่
            int head = 0;
            int size = 0;

            queue[size++] = start;
            seen[start] = true;

            while (head < size) {
                int current = queue[head++];

                int x = current % width;
                int y = current / width;

                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    for (int offsetX = -1; offsetX <= 1; offsetX++) {
                        int nextX = x + offsetX;
                        int nextY = y + offsetY;

                        boolean outside =
                            nextX < 0 || nextX >= width
                            || nextY < 0 || nextY >= height;

                        if (outside) {
                            continue;
                        }

                        int nextIndex = nextY * width + nextX;
                        int nextAlpha =
                            cell.getRGB(nextX, nextY) >>> 24;

                        if (!seen[nextIndex] && nextAlpha > 40) {
                            seen[nextIndex] = true;
                            queue[size++] = nextIndex;
                        }
                    }
                }
            }

            // จดจำกลุ่มที่มีจำนวนพิกเซลมากที่สุด
            if (size > largestComponent.length) {
                largestComponent = Arrays.copyOf(queue, size);
            }
        }

        BufferedImage clean = new BufferedImage(
            width,
            height,
            BufferedImage.TYPE_INT_ARGB
        );

        for (int index : largestComponent) {
            int x = index % width;
            int y = index / width;

            clean.setRGB(x, y, cell.getRGB(x, y));
        }

        return clean;
    }

    // ==================== ตรวจสถานะและวาดภาพ ====================

    public boolean isLoaded() {
        for (BufferedImage frame : frames) {
            if (frame == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * วาดภาพบอสด่าน 2–10 โดยรักษาสัดส่วน
     * centerX คือตำแหน่งกึ่งกลางแนวนอน
     * bottom คือตำแหน่งเท้า
     */
    public boolean draw(
            Graphics2D g,
            int level,
            int centerX,
            int bottom,
            int height,
            boolean right) {

        BufferedImage image = frames[level - 2];

        if (image == null) {
            return false;
        }

        int width = image.getWidth() * height / image.getHeight();

        if (right) {
            // กลับภาพแนวนอนให้หันขวา
            g.drawImage(
                image,
                centerX + width / 2,
                bottom - height,
                -width,
                height,
                null
            );
        } else {
            g.drawImage(
                image,
                centerX - width / 2,
                bottom - height,
                width,
                height,
                null
            );
        }

        return true;
    }
}