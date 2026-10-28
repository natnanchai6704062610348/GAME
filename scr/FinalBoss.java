
public class FinalBoss extends Zombie {

    public FinalBoss(int x) {
        super(x, "FINAL BOSS", 10);

        width = 155;
        height = 190;
        maxHp = 1800;
        hp = maxHp;
        damage = 35;
        speed = 2.1;
    }

    @Override
    public int getReward() {
        return 1500;
    }

    // โอกาสดรอปอาวุธ 100%
    @Override
    public double getDropChance() {
        return 1.0;
    }
}
