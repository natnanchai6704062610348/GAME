# Abstract Boss และเกราะชีวภาพ

ปิดเกมเก่าแล้วเปิด run_windows.bat หรือ run_boss_practice.bat ใหม่

## เกราะชีวภาพ
- เกราะเดิมยังป้องกันความเสียหายก่อน HP ตามเดิม เริ่มต้นมี40เกราะ
- มีชิ้นไหล่และปลอกแขนสีดำอมม่วง พร้อมเส้นพลังเรืองแสง ไม่ติดปืนลงในภาพตัวละคร
- เกราะรับความเสียหาย1หน่วย จะเพิ่มพลังกลายพันธุ์3หน่วย สูงสุด100
- Dash หลบได้จึงไม่ถือว่าได้รับความเสียหาย และไม่เพิ่มพลัง
- เก็บหรือซื้อเกราะไม่เพิ่มพลังกลายพันธุ์โดยตรง
- เมื่อพลังเต็ม กด F เพื่อปล่อย BIO BURST ทำดาเมจ120 รอบตัวรัศมี210พิกเซล และผลักซอมบี้ทั่วไป110พิกเซล
- บอสรับดาเมจแต่ไม่ถูกผลัก
- ใช้สกิลแล้วพลังกลับ0 ต้องสะสมใหม่ หากเกราะแตกก่อนใช้ พลังจะหาย
- ภาพเกราะมี3ระดับตามพลัง พร้อมออร่าและอนุภาคสีม่วงเมื่อใกล้เต็ม
- แถบ MUTATION สีม่วงแยกจาก ARMOR สีฟ้า เมื่อเต็มมีปุ่ม F แสดงข้างแถบ
- F ในร้านยังใช้เติมกระสุนปืนตามเดิม ไม่ใช้พลังสกิล
- เงิน อาวุธ และพลังเกราะยังเริ่มใหม่เมื่อเข้าแต่ละด่านตามระบบเดิม

## Abstract Class ที่ใช้จริง
scr/Boss.java ประกาศ public abstract class Boss extends Zombie implements BossCombat
มี abstract methods update(Player), specialHits(Player), draw(Graphics2D,int,boolean)
MutantBoss และ StageBoss extends Boss และ override เมธอดเหล่านี้
BossFactory คืนชนิด Boss และสร้างบอสที่ใช้งานจริงทั้ง10ด่าน
เมธอด pursue() ในคลาสแม่รวมการเดินเข้าหาผู้เล่นให้บอสเรียกใช้ร่วมกัน

## ตัวอย่าง OOP ครบ6หัวข้อ
| หัวข้อ | ตัวอย่างจริง |
|---|---|
| Constructor | Player(int x,int y), protected Boss(int x,String name,int level) |
| Encapsulation | MutationArmor เก็บ charge เป็น private เปลี่ยนผ่าน absorb()/release() |
| Composition | Player สร้างและเป็นเจ้าของ MutationArmor; GamePanel เป็นเจ้าของ Player |
| Polymorphism | List<Zombie> เรียก update() ตามชนิดจริง และ BossFactory คืน Boss ที่เป็นบอสชนิดต่างกัน |
| Abstract | Boss เป็น abstract class มี abstract methods และบอสที่ใช้จริงสืบทอดต่อ |
| Inheritance | MutantBoss/StageBoss extends Boss extends Zombie |

ภาพ: images/EFFECT/bio-armor-parts-v1.png สร้างด้วย built-in ImageGen ชิ้นไหล่และแขนแยกจากตัวละครแล้วผูกกับตำแหน่งข้อต่อขณะเล็ง

ตรวจคอมไพล์และทดสอบคลาสแม่บอสทุกด่าน การสะสมพลัง/เกราะแตก/ใช้ครั้งเดียว/หมดอายุเอฟเฟกต์ ไม่สะสมจากDash รัศมีและดาเมจ การผลักซอมบี้และต้านแรงผลักของบอส ป้องกันการกดค้างซ้ำ การใช้ F ในร้าน และเรนเดอร์ภาพผู้เล่นพร้อมเกราะและHUDผ่านแล้ว ตรวจระบบบอส10ด่านและเล็งเมาส์เดิมผ่านด้วย
