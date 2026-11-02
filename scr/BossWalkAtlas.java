import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * โหลดภาพท่าเดินของบอสด่าน 2–10
 * บอสแต่ละตัวมีภาพเดิน 4 เฟรม
 */
public final class BossWalkAtlas {

    private final BufferedImage[][] frames = new BufferedImage[9][4];
    private final int[] maxHeights = new int[9];

    /**
     * โหลดภาพ 3 ชุด โดยแต่ละชุดมีบอส 3 ตัว
     */
    public BossWalkAtlas() {
        String[] roots = {
            "images/BOSS/",
            "../images/BOSS/"
        };

        for (int group = 0; group < 3; group++) {
            String fileName =
                "boss-walk-" + (char) ('a' + group) + ".png";

            for (String root : roots) {
                try {
                    File file = new File(root + fileName);

                    if (file.isFile()) {
                        load(ImageIO.read(file), group);
                        break;
                    }

                } catch (Exception e) {
                    System.err.println(
                        "Boss walk: " + e.getMessage()
                    );
                }
            }
        }
    }

    /**
     * แบ่งภาพเป็น 3 แถว 4 คอลัมน์
     * แต่ละแถวเป็นบอสหนึ่งตัว และแต่ละคอลัมน์เป็นท่าเดิน
     */
    private void load(BufferedImage image, int group) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {

                int left = col * image.getWidth() / 4;
                int top = row * image.getHeight() / 3;

                int width =
                    (col + 1) * image.getWidth() / 4 - left;

                int height =
                    (row + 1) * image.getHeight() / 3 - top;

                BufferedImage cell = image.getSubimage(
                    left, top, width, height
                );

                // เก็บเฉพาะกลุ่มพิกเซลที่เชื่อมต่อกันและใหญ่ที่สุด
                cell = mainComponent(cell);

                // หาขอบเขตภาพที่ไม่โปร่งใส
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

                if (maxX >= minX) {
                    int bossIndex = group * 3 + row;
                    int croppedWidth = maxX - minX + 1;
                    int croppedHeight = maxY - minY + 1;

                    frames[bossIndex][col] = cell.getSubimage(
                        minX,
                        minY,
                        croppedWidth,
                        croppedHeight
                    );

                    maxHeights[bossIndex] = Math.max(
                        maxHeights[bossIndex],
                        croppedHeight
                    );
                }
            }
        }
    }

    /**
     * ใช้ BFS หากลุ่มพิกเซลทึบที่เชื่อมต่อกันขนาดใหญ่ที่สุด
     * เพื่อตัดเศษภาพที่แยกออกจากตัวบอส
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

                // ตรวจพิกเซลรอบข้างทั้ง 8 ทิศทาง
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

            // เก็บกลุ่มที่ใหญ่ที่สุดที่พบ
            if (size > largestComponent.length) {
                largestComponent = Arrays.copyOf(queue, size);
            }
        }

        // สร้างภาพโปร่งใส แล้วใส่เฉพาะกลุ่มพิกเซลที่เลือก
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

    /**
     * ตรวจว่าภาพเดินของบอสครบทั้ง 4 เฟรมหรือไม่
     * ใช้กับหมายเลขด่าน 2–10
     */
    public boolean loaded(int level) {
        int bossIndex = level - 2;

        for (BufferedImage image : frames[bossIndex]) {
            if (image == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * วาดเฟรมเดิน โดยยึดตำแหน่งเท้าเป็นหลัก
     * และปรับขนาดด้วยสเกลเดียวกันสำหรับทุกเฟรมของบอส
     */
    public boolean draw(
            Graphics2D g,
            int level,
            int frame,
            int centerX,
            int bottom,
            int height,
            boolean facingRight) {

        if (!loaded(level)) {
            return false;
        }

        int bossIndex = level - 2;
        int frameIndex = Math.floorMod(frame, 4);

        BufferedImage image = frames[bossIndex][frameIndex];

        double scale =
            height / (double) maxHeights[bossIndex];

        int drawWidth = (int) (image.getWidth() * scale);
        int drawHeight = (int) (image.getHeight() * scale);

        if (facingRight) {
            // กลับภาพแนวนอน
            g.drawImage(
                image,
                centerX + drawWidth / 2,
                bottom - drawHeight,
                -drawWidth,
                drawHeight,
                null
            );
        } else {
            g.drawImage(
                image,
                centerX - drawWidth / 2,
                bottom - drawHeight,
                drawWidth,
                drawHeight,
                null
            );
        }

        return true;
    }
}