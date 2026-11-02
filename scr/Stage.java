import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * เก็บข้อมูลและสร้างองค์ประกอบของแต่ละด่าน
 * ได้แก่ แท่นยืน กับดัก และไอเทม
 */
public class Stage {

    public final String name;
    public final String feature;

    public final Color sky;
    public final Color accent;

    public final int level;
    public final int quota;
    public final int worldWidth;
    public final int hazardType;

    // ==================== Constructor ====================

    private Stage(
            int level,
            String name,
            String feature,
            Color sky,
            Color accent,
            int hazardType) {

        this.level = level;
        this.name = name;
        this.feature = feature;
        this.sky = sky;
        this.accent = accent;
        this.hazardType = hazardType;

        // เพิ่มจำนวนศัตรูและความยาวตามระดับด่าน
        quota = 22 + level * 4;
        worldWidth = 6000 + level * 500;
    }

    // ==================== ข้อมูลด่าน ====================

    public int getEnemyLimit() {
        return 5 + level / 2;
    }

    public int getSpawnInterval() {
        return Math.max(430, 950 - level * 45);
    }

    public String getBossName() {
        return BossFactory.name(level);
    }

    public String getBackgroundPath() {
        return "images/BACKGROUND/stage" + level + ".png";
    }

    // ==================== สร้างแท่นยืน ====================

    /**
     * สร้างกลุ่มแท่นที่สูงขึ้นครั้งละ 90 พิกเซล
     * สลับใช้แท่นเคลื่อนที่และแท่นที่พังได้
     */
    public List<Platform> getPlatforms() {
        List<Platform> result = new ArrayList<>();

        for (int base = 300;
                base + 650 < worldWidth - 170;
                base += 1000) {

            int group = (base - 300) / 1000;

            // แท่นชั้นแรก
            result.add(new Platform(
                base,
                380,
                220
            ));

            // แท่นชั้นสอง สลับแนวการเคลื่อนที่
            result.add(new MovingPlatform(
                base + 180,
                290,
                220,
                group % 2 == 0 ? 28 : 18,
                group % 2 != 0
            ));

            // แท่นชั้นสาม สลับแท่นธรรมดากับแท่นพัง
            if (group % 2 == 0) {
                result.add(new CrumblingPlatform(
                    base + 360,
                    200,
                    290
                ));
            } else {
                result.add(new Platform(
                    base + 360,
                    200,
                    290
                ));
            }
        }

        // ป้องกันการเพิ่มหรือลบสมาชิกผ่านรายการที่คืนออกไป
        return Collections.unmodifiableList(result);
    }

    // ==================== สร้างกับดัก ====================

    public List<StageTrap> getTraps() {
        List<StageTrap> result = new ArrayList<>();

        StageTrap.Type[] types = StageTrap.Type.values();
        int index = 0;

        for (int x = 1050; x < worldWidth - 1300; x += 760) {
            int typeIndex = (index + level - 1) % types.length;

            result.add(new StageTrap(
                x,
                types[typeIndex],
                level,
                index * 47
            ));

            index++;
        }

        return result;
    }

    // ==================== สร้างไอเทม ====================

    public List<SupplyPickup> getSupplies() {
        List<SupplyPickup> result = new ArrayList<>();

        SupplyPickup.Type[] types = SupplyPickup.Type.values();
        int index = 0;

        // วางไอเทมบนพื้น โดยวนประเภทไปเรื่อย ๆ
        for (int x = 750; x < worldWidth - 350; x += 650) {
            result.add(new SupplyPickup(
                x,
                440,
                types[index % types.length]
            ));

            index++;
        }

        // วางเงินบนแท่นทุกลำดับที่สาม
        int ledge = 0;

        for (Platform platform : getPlatforms()) {
            if ((ledge++ % 3) == 2) {
                result.add(new SupplyPickup(
                    platform.getX() + platform.getWidth() / 2,
                    platform.getY() - 24,
                    SupplyPickup.Type.CREDITS
                ));
            }
        }

        return result;
    }

    // ==================== สร้างข้อมูลด่านที่เลือก ====================

    public static Stage get(int level) {
        String[] names = {
            "ABANDONED CITY",
            "BURNING FACTORY",
            "FLOODED SUBWAY",
            "FOGGY FOREST",
            "BROKEN HIGHWAY",
            "ABANDONED HOSPITAL",
            "TOXIC SEWERS",
            "MILITARY BASE",
            "VOLCANIC ZONE",
            "ZOMBIE NEST"
        };

        String[] features = {
            "ruined towers",
            "fire vents",
            "electric pools",
            "thick fog",
            "falling debris",
            "infected halls",
            "toxic gas",
            "turret zone",
            "lava cracks",
            "mutant lair"
        };

        Color[] skies = {
            new Color(27, 40, 58),
            new Color(78, 29, 25),
            new Color(16, 48, 60),
            new Color(17, 42, 39),
            new Color(65, 45, 39),
            new Color(38, 44, 57),
            new Color(25, 53, 29),
            new Color(30, 40, 47),
            new Color(93, 31, 22),
            new Color(59, 15, 37)
        };

        Color[] accents = {
            new Color(76, 101, 116),
            new Color(230, 93, 41),
            new Color(50, 189, 207),
            new Color(75, 130, 97),
            new Color(152, 118, 88),
            new Color(94, 151, 159),
            new Color(105, 216, 66),
            new Color(117, 143, 149),
            new Color(251, 94, 34),
            new Color(221, 59, 128)
        };

        // จำกัดหมายเลขด่านให้อยู่ระหว่าง 1–10
        level = Math.max(1, Math.min(10, level));

        int index = level - 1;

        return new Stage(
            level,
            names[index],
            features[index],
            skies[index],
            accents[index],
            level
        );
    }
}