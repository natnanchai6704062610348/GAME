import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MuzzleFlashEffect {

    private BufferedImage[] frames;
    private int frameIndex = 0;
    private long lastFrameTime = 0;
    private boolean active = false;

    private int x, y;
    private boolean facingRight = true;

    public MuzzleFlashEffect() {
        loadSpriteSheet();
    }

    private void loadSpriteSheet() {
        try {
            BufferedImage sheet = ImageIO.read(
                new File("images/EFFECT/muzzle_flash_sheet.png")
            );

            int frameCount = 4;
            int frameWidth = sheet.getWidth() / frameCount;
            int frameHeight = sheet.getHeight();

            frames = new BufferedImage[frameCount];

            for (int i = 0; i < frameCount; i++) {
                frames[i] = sheet.getSubimage(
                    i * frameWidth,
                    0,
                    frameWidth,
                    frameHeight
                );
            }

            System.out.println("Muzzle flash sheet loaded.");
        } catch (Exception e) {
            System.out.println("Muzzle flash load error: " + e.getMessage());
            frames = null;
        }
    }

    public void trigger(int x, int y, boolean facingRight) {
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.frameIndex = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.active = true;
    }

    public void update() {
        if (!active || frames == null) return;

        long now = System.currentTimeMillis();

        if (now - lastFrameTime > 40) {
            frameIndex++;
            lastFrameTime = now;

            if (frameIndex >= frames.length) {
                active = false;
            }
        }
    }

    public void draw(Graphics2D g, int camera) {
        if (!active || frames == null) return;

        BufferedImage img = frames[frameIndex];
        int drawX = x - camera;
        int drawY = y;

        if (facingRight) {
            g.drawImage(img, drawX, drawY, img.getWidth(), img.getHeight(), null);
        } else {
            g.drawImage(img, drawX + img.getWidth(), drawY, -img.getWidth(), img.getHeight(), null);
        }
    }

    public boolean isActive() {
        return active;
    }
}
