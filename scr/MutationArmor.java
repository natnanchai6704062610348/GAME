/**
 * จัดการพลังสะสมของเกราะชีวภาพ
 * เกราะรับความเสียหายเพื่อสะสมพลังสำหรับสกิลโจมตีรอบตัว
 */
public final class MutationArmor {

    public static final int MAX_CHARGE = 100;
    public static final int RADIUS = 210;
    public static final int DAMAGE = 120;

    // ซ่อนสถานะภายในคลาสตามหลัก Encapsulation
    private int charge;
    private int releaseTicks;
    private int animationTicks;

    /**
     * อัปเดตเวลาสำหรับแอนิเมชันและเอฟเฟกต์ปล่อยพลัง
     */
    public void update() {
        animationTicks++;

        if (releaseTicks > 0) {
            releaseTicks--;
        }
    }

    /**
     * สะสมพลังเป็น 3 เท่าของความเสียหายที่เกราะรับ
     * หากเกราะหมด จะล้างพลังสะสม
     */
    public void absorb(int blockedDamage, int remainingArmor) {
        if (remainingArmor <= 0) {
            charge = 0;
            return;
        }

        if (blockedDamage > 0) {
            charge = (int) Math.min(
                MAX_CHARGE,
                (long) charge + (long) blockedDamage * 3
            );
        }
    }

    public int getCharge() {
        return charge;
    }

    // พร้อมใช้สกิลเมื่อพลังเต็ม
    public boolean isReady() {
        return charge == MAX_CHARGE;
    }

    /**
     * ใช้พลังสะสมและเริ่มเอฟเฟกต์ 24 รอบอัปเดต
     * คืนค่า true เมื่อปล่อยพลังสำเร็จ
     */
    public boolean release() {
        if (!isReady()) {
            return false;
        }

        charge = 0;
        releaseTicks = 24;

        return true;
    }

    public int getReleaseTicks() {
        return releaseTicks;
    }

    public int getAnimationTicks() {
        return animationTicks;
    }

    /**
     * คืนระดับรูปลักษณ์ของเกราะ
     * 0 = พลังต่ำ
     * 1 = พลังตั้งแต่ 40 แต่ยังไม่เต็ม
     * 2 = พลังเต็มหรือกำลังแสดงเอฟเฟกต์ปล่อยพลัง
     */
    public int getVisualStage() {
        if (releaseTicks > 0 || charge == 100) {
            return 2;
        }

        if (charge >= 40) {
            return 1;
        }

        return 0;
    }
}