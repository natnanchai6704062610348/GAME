import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * จัดการแอนิเมชันส่วนลำตัวของผู้เล่น
 * ส่วนแขนและอาวุธวาดแยกด้วย PlayerAimRig
 */
public class PlayerAnimation {

    public enum State {
        IDLE,
        WALK,
        JUMP,
        SHOOT,
        HURT,
        DEATH
    }

    private final BufferedImage[] frames = new BufferedImage[16];

    private State state = State.IDLE;

    // ตัวนับสำหรับแอนิเมชันแต่ละสถานะ
    private int ticks;
    private int hurtTicks;
    private int landingTicks;
    private int jumpTicks;
    private int jumpFrame = 8;

    // ตำแหน่งและระยะทางเดินสะสม
    private double lastX;
    private double walkDistance;

    private boolean initialized;
    private boolean wasGrounded = true;
    private boolean reverseWalk;

    private long deathAt;

    // ความสูงอ้างอิงสำหรับปรับขนาดทุกเฟรม
    private int standingHeight = 1;

    // ==================== โหลดภาพ ====================

    public PlayerAnimation() {
        String[] roots = {
            "images/PLAYER/",
            "../images/PLAYER/",
            "ProjectGameOOP/images/PLAYER/"
        };

        for (String root : roots) {
            try {
                File file = new File(root + "survivor-body-v2.png");

                if (file.isFile()) {
                    load(ImageIO.read(file));
                    return;
                }

            } catch (Exception e) {
                System.err.println(
                    "Player body: " + e.getMessage()
                );
            }
        }
    }

    /**
     * แบ่ง Sprite Sheet เป็น 16 เฟรม
     * และตัดพื้นที่โปร่งใสรอบภาพออก
     */
    private void load(BufferedImage sheet) {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {

                // ขอบแถวอิงตำแหน่งภาพใน Sprite Sheet
                double[] rows = {
                    0,
                    328.0 / 1254,
                    635.0 / 1254,
                    937.0 / 1254,
                    1
                };

                // แถวสุดท้ายมีความกว้างช่องต่างจากแถวอื่น
                double[] cols = row == 3
                    ? new double[] {
                        0,
                        315.0 / 1254,
                        624.0 / 1254,
                        920.0 / 1254,
                        1
                    }
                    : new double[] {
                        0, 0.25, 0.5, 0.75, 1
                    };

                int x = (int) (cols[col] * sheet.getWidth());
                int y = (int) (rows[row] * sheet.getHeight());

                int width =
                    (int) (cols[col + 1] * sheet.getWidth()) - x;

                int height =
                    (int) (rows[row + 1] * sheet.getHeight()) - y;

                BufferedImage cell = sheet.getSubimage(
                    x,
                    y,
                    width,
                    height
                );

                // ตัดเศษภาพที่ไม่เชื่อมต่อกับตัวละครออก
                cell = mainComponent(cell);

                int minX = cell.getWidth();
                int minY = cell.getHeight();
                int maxX = -1;
                int maxY = -1;

                // หาขอบเขตพิกเซลที่มองเห็น
                for (int pixelY = 0;
                        pixelY < cell.getHeight();
                        pixelY++) {

                    for (int pixelX = 0;
                            pixelX < cell.getWidth();
                            pixelX++) {

                        int alpha =
                            cell.getRGB(pixelX, pixelY) >>> 24;

                        if (alpha > 40) {
                            minX = Math.min(minX, pixelX);
                            minY = Math.min(minY, pixelY);
                            maxX = Math.max(maxX, pixelX);
                            maxY = Math.max(maxY, pixelY);
                        }
                    }
                }

                if (maxX >= minX) {
                    int frameIndex = row * 4 + col;

                    frames[frameIndex] = cell.getSubimage(
                        minX,
                        minY,
                        maxX - minX + 1,
                        maxY - minY + 1
                    );

                    // ใช้เฟรมยืนและเดินเป็นความสูงอ้างอิง
                    if (frameIndex < 8) {
                        standingHeight = Math.max(
                            standingHeight,
                            frames[frameIndex].getHeight()
                        );
                    }
                }
            }
        }
    }

    /**
     * ใช้ BFS หากลุ่มพิกเซลที่เชื่อมต่อกันขนาดใหญ่ที่สุด
     * โดยตรวจพิกเซลรอบข้างทั้ง 8 ทิศทาง
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

    // ==================== จัดการสถานะ ====================

    public boolean isLoaded() {
        for (BufferedImage frame : frames) {
            if (frame == null) {
                return false;
            }
        }

        return true;
    }

    public void setState(State next) {
        if (state != next) {
            state = next;
            ticks = 0;
        }
    }

    public State getCurrentState() {
        return state;
    }

    /**
     * เลือกสถานะแอนิเมชันตามสถานะผู้เล่น
     */
    public void update(Player player, boolean shooting) {
        ticks++;

        if (!initialized) {
            lastX = player.getX();
            initialized = true;
        }

        double deltaX = player.getX() - lastX;
        lastX = player.getX();

        // เปลี่ยนเฟรมเดินตามระยะทางที่เคลื่อนจริง
        if (player.isMoving()) {
            walkDistance += Math.abs(deltaX);

            reverseWalk =
                (deltaX > 0) != player.isFacingRight();
        }

        boolean grounded = player.isGrounded();

        // เพิ่งลงพื้น
        if (grounded && !wasGrounded) {
            landingTicks = 4;
        }

        // เพิ่งออกจากพื้น
        if (!grounded && wasGrounded) {
            jumpTicks = 0;
        }

        // เลือกท่าขึ้นและตกขณะกระโดด
        if (!grounded) {
            jumpTicks++;

            jumpFrame = jumpTicks < 4
                ? 8
                : (player.getVerticalVelocity() < 0 ? 9 : 10);
        }

        wasGrounded = grounded;

        // สถานะตายมีความสำคัญสูงสุด
        if (player.isDead()) {
            playDeath();
            return;
        }

        if (hurtTicks > 0) {
            hurtTicks--;
            setState(State.HURT);
            return;
        }

        if (!grounded) {
            setState(State.JUMP);
            return;
        }

        // แสดงท่าลงพื้นช่วงสั้น ๆ
        if (landingTicks > 0) {
            landingTicks--;
            setState(State.JUMP);
            return;
        }

        // การเดินยังแสดงต่อเนื่องแม้กำลังยิง
        if (player.isMoving()) {
            setState(State.WALK);
        } else {
            setState(shooting ? State.SHOOT : State.IDLE);
        }
    }

    public void playHurt() {
        if (state != State.DEATH) {
            hurtTicks = 12;
            setState(State.HURT);
        }
    }

    public void playDeath() {
        if (state != State.DEATH) {
            deathAt = System.currentTimeMillis();
            setState(State.DEATH);
        }
    }

    // ==================== เลือกเฟรม ====================

    private int index() {
        switch (state) {
            case WALK: {
                int frame = (int) (walkDistance / 12) % 4;

                return 4 + (
                    reverseWalk
                        ? Math.floorMod(-frame, 4)
                        : frame
                );
            }

            case JUMP:
                return wasGrounded ? 11 : jumpFrame;

            case HURT:
                return hurtTicks > 6 ? 12 : 13;

            case DEATH:
                return System.currentTimeMillis() - deathAt < 260
                    ? 14
                    : 15;

            default:
                // IDLE และ SHOOT ใช้ชุดภาพนี้ร่วมกัน
                return (ticks / 14) % 4;
        }
    }

    public BufferedImage getCurrentFrame() {
        return frames[index()];
    }

    // ==================== วาดภาพ ====================

    /**
     * วาดเฟรมโดยใช้ความสูงอ้างอิงเดียวกัน
     * และยึดตำแหน่งเท้าไว้ที่ bottom
     */
    public void drawFrame(
            Graphics2D g,
            int index,
            int centerX,
            int bottom,
            int height,
            boolean right) {

        BufferedImage image = frames[index];

        if (image == null) {
            return;
        }

        double scale = height / (double) standingHeight;

        int drawWidth = (int) Math.round(
            image.getWidth() * scale
        );

        int drawHeight = (int) Math.round(
            image.getHeight() * scale
        );

        if (right) {
            g.drawImage(
                image,
                centerX - drawWidth / 2,
                bottom - drawHeight,
                drawWidth,
                drawHeight,
                null
            );
        } else {
            g.drawImage(
                image,
                centerX + drawWidth / 2,
                bottom - drawHeight,
                -drawWidth,
                drawHeight,
                null
            );
        }
    }

    /**
     * วาดตัวผู้เล่นและเอียงภาพเล็กน้อยขณะพุ่งหลบ
     */
    public void draw(
            Graphics2D original,
            Player player,
            int camera) {

        Graphics2D g = (Graphics2D) original.create();

        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        int centerX = player.getX()
            + player.getWidth() / 2
            - camera;

        int bottom = player.getY() + player.getHeight();

        if (player.isDashing()) {
            g.rotate(
                player.isFacingRight() ? 0.07 : -0.07,
                centerX,
                bottom
            );
        }

        drawFrame(
            g,
            index(),
            centerX,
            bottom,
            player.getHeight() - 2,
            player.isFacingRight()
        );

        g.dispose();
    }
}