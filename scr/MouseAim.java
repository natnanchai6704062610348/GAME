/**
 * เก็บตำแหน่งเมาส์ภายในพื้นที่หน้าจอเกม
 * และแปลงเป็นพิกัดโลกเกมตามตำแหน่งกล้อง
 */
public final class MouseAim {

    private int screenX;
    private int screenY;

    // เป็น true หลังได้รับตำแหน่งเมาส์ครั้งแรก
    private boolean active;

    /**
     * บันทึกตำแหน่งเมาส์ล่าสุด
     */
    public void move(int x, int y) {
        screenX = x;
        screenY = y;
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }

    /**
     * แปลงตำแหน่งแนวนอนจากหน้าจอเป็นพิกัดโลกเกม
     */
    public double worldX(int camera) {
        return screenX + camera;
    }

    /**
     * คำนวณมุมเล็งจากตำแหน่งมือไปยังเมาส์
     * คืนค่ามุมเป็นเรเดียน
     */
    public double angle(
            double handX,
            double handY,
            int camera,
            boolean right) {

        if (active) {
            double deltaX = worldX(camera) - handX;
            double deltaY = screenY - handY;

            return Math.atan2(deltaY, deltaX);
        }

        // หากยังไม่ได้ใช้เมาส์ ให้เล็งตามทิศทางที่หัน
        return right ? 0 : Math.PI;
    }
}