
public class MiniBoss extends Zombie {

    public MiniBoss(int x, int level) {
        super(x, "Mini Boss", level);

        width = 105;
        height = 152;
        maxHp = 260 + level * 70;
        hp = maxHp;
        damage = 20 + level * 2;
        speed = 1.15 + level * .12;
    }

    @Override
    public int getReward() {
        return 250;
    }

    // โอกาสดรอปอาวุธ 65%
    @Override
    public double getDropChance() {
        return 0.65;
    }
}
