
public class Player {

    // ตำแหน่งและความเร็วแนวตั้ง
    private double x = 100;
    private double y = 350;
    private double vy = 0;

    // ขนาดตัวละคร
    private final int width = 68;
    private final int height = 120;

    // พลังชีวิตและเกราะ
    private int hp = 100;
    private int armor = 40;
    private final MutationArmor mutation=new MutationArmor();

    // สถานะการเคลื่อนไหว
    private boolean ground = true;
    private boolean jumpHeld;
    private Platform support;
    private int jumpsUsed;
    private int dashTicks, dashCooldown, dashDirection;
    private boolean facingRight = true;
    private boolean moving = false;

    // Constructor
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // อัปเดตการเดินและกระโดด
    public void update(boolean left, boolean right,
                       boolean jump, int worldWidth) {
        update(left, right, jump, worldWidth, java.util.Collections.emptyList());
    }

    public void update(boolean left, boolean right, boolean jump, int worldWidth,
                       java.util.List<Platform> platforms) {

        mutation.update();
        if(ground && support!=null){
            if(support.isSolid()) {x+=support.getDeltaX();y+=support.getDeltaY();}
            else {ground=false;support=null;}
        }
        if(dashCooldown>0)dashCooldown--;
        if(dashTicks>0){x+=dashDirection*13;dashTicks--;}
        moving = left != right;

        if (left) {
            x -= 4.3;
            facingRight = false;
        }

        if (right) {
            x += 4.3;
            facingRight = true;
        }

        // ป้องกันเดินออกนอกแผนที่
        if (x < 0) {
            x = 0;
        }

        if (x > worldWidth - width) {
            x = worldWidth - width;
        }

        // กระโดด
        if (jump && !jumpHeld && (ground || jumpsUsed < 2)) {
            if (!ground && jumpsUsed == 0) jumpsUsed = 1;
            jumpsUsed++;
            vy = -13.0;
            ground = false;
        }

        jumpHeld = jump;
        double previousBottom = y + height;
        vy += 0.64;
        double nextY = y + vy;
        ground = false;
        support = null;
        if (vy >= 0) {
            double landingY = 470;
            Platform landingPlatform = null;
            for (Platform platform : platforms) {
                if (platform.isSolid() && platform.overlaps(x, width)
                        && previousBottom <= Math.max(platform.getY(),platform.getPreviousY()) + 0.001
                        && nextY + height >= platform.getY()) {
                    if(platform.getY()<landingY){landingY=platform.getY();landingPlatform=platform;}
                }
            }
            if (nextY + height >= landingY && previousBottom <= landingY + 0.001) {
                nextY = landingY - height;
                vy = 0;
                ground = true;
                jumpsUsed = 0;
                support=landingPlatform;
                if(support!=null)support.onLand();
            }
        }
        if (nextY < 0) { nextY = 0; vy = Math.max(0, vy); }
        y = nextY;
    }
// Damage is absorbed by armor first.
    public void hurt(int amount) {
        if(isDashing() || amount<=0)return;
        int blocked = Math.min(armor, amount);
        armor -= blocked;
        mutation.absorb(blocked,armor);
        hp = Math.max(0, hp - (amount - blocked));
    }

    // เพิ่มเลือด
    public void heal(int amount) {
        hp = Math.min(100, hp + amount);
    }

    // เพิ่มเกราะ
    public void addArmor(int amount) {
        armor = Math.min(100, armor + amount);
    }

    // Getter ตำแหน่ง
    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    // Getter ขนาดตัวละคร
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Getter สถานะตัวละคร
    public boolean dash() {
        if(dashCooldown>0 || isDead())return false;
        dashDirection=facingRight?1:-1;dashTicks=11;dashCooldown=100;return true;
    }
    public boolean isDashing(){return dashTicks>0;}
    public int getDashCooldown(){return dashCooldown;}
    public int getHp() {
        return hp;
    }

    public MutationArmor getMutationArmor(){return mutation;}
    public boolean releaseMutation(){return !isDead() && armor>0 && mutation.release();}
    public int getArmor() {
        return armor;
    }

    public void faceTowards(double worldX){double center=x+width/2.0;if(Math.abs(worldX-center)>1)facingRight=worldX>center;}
    public boolean isFacingRight() {
        return facingRight;
    }

    // สำหรับระบบ Animation
    public boolean isMoving() {
        return moving;
    }

    public boolean isGrounded() {
        return ground;
    }

    public double getVerticalVelocity(){return vy;}
    public boolean isJumping() {
        return !ground;
    }

    public boolean isDead() {
        return hp <= 0;
    }
}








