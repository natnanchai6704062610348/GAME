
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PlayerAnimation {

    public enum State {
        IDLE, WALK, JUMP, SHOOT, HURT, DEATH
    }

    private final BufferedImage[] idle;
    private final BufferedImage[] walk;
    private final BufferedImage[] jump;
    private final BufferedImage[] shoot;
    private final BufferedImage[] hurt;
    private final BufferedImage[] death;

    private State currentState = State.IDLE;
    private long stateStart = System.currentTimeMillis();

    // ความเร็วแต่ละเฟรม (มิลลิวินาที)
    private static final int IDLE_SPEED = 220;
    private static final int WALK_SPEED = 110;
    private static final int JUMP_SPEED = 120;
    private static final int SHOOT_SPEED = 55;
    private static final int HURT_SPEED = 100;
    private static final int DEATH_SPEED = 160;

    public PlayerAnimation() {
        idle = loadFrames("idle", 4);
        walk = loadFrames("walk", 6);
        jump = loadFrames("jump", 4);
        shoot = loadFrames("shoot", 4);
        hurt = loadFrames("hurt", 3);
        death = loadFrames("death", 6);
    }

    private BufferedImage[] loadFrames(String name, int count) {
        BufferedImage[] frames = new BufferedImage[count];

        for (int i = 0; i < count; i++) {
            String filename = String.format(
                "%s_%02d.png", name, i + 1
            );

            String[] paths = {
                "images/PLAYER/" + filename,
                "../images/PLAYER/" + filename,
                "ProjectGameOOP/images/PLAYER/" + filename
            };

            for (String path : paths) {
                try {
                    File file = new File(path);

                    if (file.isFile()) {
                        frames[i] = ImageIO.read(file);
                        break;
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }

            if (frames[i] == null) {
                System.err.println(
                    "Missing animation: " + filename
                );
            }
        }

        return frames;
    }

    public void setState(State state) {
        if (currentState != state) {
            currentState = state;
            stateStart = System.currentTimeMillis();
        }
    }

    public void update(Player player, boolean shooting) {

        if (player.isDead()) {
            setState(State.DEATH);
        } else if (currentState == State.HURT &&
                   System.currentTimeMillis() - stateStart < 300) {
            return;
        } else if (player.isJumping()) {
            setState(State.JUMP);
        } else if (shooting) {
            setState(State.SHOOT);
        } else if (player.isMoving()) {
            setState(State.WALK);
        } else {
            setState(State.IDLE);
        }
    }

    public void playHurt() {
        if (currentState != State.DEATH) {
            setState(State.HURT);
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    private BufferedImage[] getFrames() {
        switch (currentState) {
            case WALK: return walk;
            case JUMP: return jump;
            case SHOOT: return shoot;
            case HURT: return hurt;
            case DEATH: return death;
            default: return idle;
        }
    }

    private int getSpeed() {
        switch (currentState) {
            case WALK: return WALK_SPEED;
            case JUMP: return JUMP_SPEED;
            case SHOOT: return SHOOT_SPEED;
            case HURT: return HURT_SPEED;
            case DEATH: return DEATH_SPEED;
            default: return IDLE_SPEED;
        }
    }

    public BufferedImage getCurrentFrame() {
        BufferedImage[] frames = getFrames();

        long elapsed = Math.max(
            0, System.currentTimeMillis() - stateStart
        );

        int frameIndex = (int)(elapsed / getSpeed());

        if (currentState == State.DEATH ||
            currentState == State.JUMP ||
            currentState == State.HURT) {
            frameIndex = Math.min(frameIndex, frames.length - 1);
        } else {
            frameIndex %= frames.length;
        }

        // ใช้ภาพแรกที่โหลดสำเร็จเป็น fallback
        if (frames[frameIndex] != null) {
            return frames[frameIndex];
        }

        for (BufferedImage frame : frames) {
            if (frame != null) return frame;
        }

        return null;
    }

    public void draw(Graphics2D g, Player player, int camera) {
        BufferedImage image = getCurrentFrame();

        if (image == null) return;

        int x = player.getX() - camera;
        int y = player.getY();
        int w = player.getWidth();
        int h = player.getHeight();

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        if (player.isFacingRight()) {
            g2.drawImage(image, x, y, w, h, null);
        } else {
            g2.drawImage(image, x + w, y, -w, h, null);
        }

        g2.dispose();
    }
}
