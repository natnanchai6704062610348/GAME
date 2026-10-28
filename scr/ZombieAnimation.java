
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ZombieAnimation {

    public enum State {
        IDLE, WALK, ATTACK, HURT, DEATH
    }

    private final BufferedImage[] idle;
    private final BufferedImage[] walk;
    private final BufferedImage[] attack;
    private final BufferedImage[] hurt;
    private final BufferedImage[] death;

    private State currentState = State.IDLE;
    private long stateStart = System.currentTimeMillis();

    public ZombieAnimation() {
        idle = loadFrames("idle", 4);
        walk = loadFrames("walk", 6);
        attack = loadFrames("attack", 4);
        hurt = loadFrames("hurt", 3);
        death = loadFrames("death", 5);
    }

    private BufferedImage[] loadFrames(String name, int count) {
        BufferedImage[] frames = new BufferedImage[count];

        for (int i = 0; i < count; i++) {
            String filename = String.format(
                "%s_%02d.png", name, i + 1
            );

            String[] paths = {
                "images/ZOMBIE/" + filename,
                "../images/ZOMBIE/" + filename,
                "ProjectGameOOP/images/ZOMBIE/" + filename
            };

            for (String path : paths) {
                try {
                    File file = new File(path);
                    if (file.isFile()) {
                        frames[i] = ImageIO.read(file);
                        if (frames[i] != null) break;
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }

            if (frames[i] == null) {
                System.err.println(
                    "Missing zombie frame: " + filename
                );
            }
        }

        return frames;
    }

    private void setState(State state) {
        if (currentState != state) {
            currentState = state;
            stateStart = System.currentTimeMillis();
        }
    }

    public void update(boolean attacking) {
        if (currentState == State.DEATH) return;

        long now = System.currentTimeMillis();

        if (currentState == State.HURT &&
            now - stateStart < 240) {
            return;
        }

        setState(attacking ? State.ATTACK : State.WALK);
    }

    public void playHurt() {
        if (currentState != State.DEATH) {
            setState(State.HURT);
        }
    }

    public void playDeath() {
        setState(State.DEATH);
    }

    public boolean isDeathFinished() {
        return currentState == State.DEATH &&
            System.currentTimeMillis() - stateStart >= 750;
    }

    public State getCurrentState() {
        return currentState;
    }

    private BufferedImage[] getFrames() {
        switch (currentState) {
            case WALK: return walk;
            case ATTACK: return attack;
            case HURT: return hurt;
            case DEATH: return death;
            default: return idle;
        }
    }

    private int getFrameDuration() {
        switch (currentState) {
            case WALK: return 125;
            case ATTACK: return 110;
            case HURT: return 80;
            case DEATH: return 220;
            default: return 220;
        }
    }

    public BufferedImage getCurrentFrame() {
        BufferedImage[] frames = getFrames();

        long elapsed = Math.max(
            0, System.currentTimeMillis() - stateStart
        );

        int index = (int)(elapsed / getFrameDuration());

        if (currentState == State.DEATH ||
            currentState == State.HURT) {
            index = Math.min(index, frames.length - 1);
        } else {
            index %= frames.length;
        }

        if (frames[index] != null) {
            return frames[index];
        }

        for (BufferedImage frame : frames) {
            if (frame != null) return frame;
        }

        return null;
    }

    public void draw(Graphics2D g, Zombie zombie,
                     Player player, int camera) {

        BufferedImage image = getCurrentFrame();
        if (image == null) return;

        int x = zombie.getX() - camera;
        int y = zombie.getY();
        int w = zombie.getWidth();
        int h = zombie.getHeight();

        boolean faceRight =
            player.getX() > zombie.getX();

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        // ภาพต้นฉบับหันขวา
        // ถ้าผู้เล่นอยู่ซ้าย จะ Flip Horizontal
        if (faceRight) {
            g2.drawImage(image, x, y, w, h, null);
        } else {
            g2.drawImage(image, x + w, y, -w, h, null);
        }

        g2.dispose();
    }
}
