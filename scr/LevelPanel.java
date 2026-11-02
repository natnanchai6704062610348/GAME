import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * หน้าเลือกด่าน
 * แสดงการ์ดด่าน ข้อมูลบอส และปุ่มเริ่มเล่น
 */
public class LevelPanel extends JPanel {

    // สีหลักของหน้าจอ
    private static final Color INK = new Color(12, 18, 25);
    private static final Color MINT = new Color(116, 234, 197);
    private static final Color MUTED = new Color(146, 160, 172);

    private final JFrame frame;
    private final int unlocked;

    private int selected;

    // ปุ่มควบคุม
    private final JButton launch = new JButton();
    private final JButton back = new JButton("<  MAIN MENU");

    // การ์ดและภาพพื้นหลังแต่ละด่าน
    private final StageCard[] cards = new StageCard[10];
    private final BufferedImage[] images = new BufferedImage[10];

    private BufferedImage backdrop;

    // ภาพบอส
    private final StageBossArt bossArt = new StageBossArt();
    private final BossSpriteAnimation ironArt =
        new BossSpriteAnimation();

    // ตำแหน่งและขนาดพื้นที่แสดงข้อมูล
    private int detailX;
    private int detailWidth;
    private int contentTop = 132;
    private int contentHeight;

    // ==================== Constructor ====================

    public LevelPanel(JFrame frame) {
        this(frame, Progress.unlocked());
    }

    LevelPanel(JFrame frame, int unlocked) {
        this.frame = frame;
        this.unlocked = Math.max(1, Math.min(10, unlocked));

        selected = this.unlocked;

        setLayout(null);
        setBackground(INK);
        setFocusable(true);

        backdrop = load("menu_bg.png");

        // โหลดภาพและสร้างการ์ดทั้งหมด 10 ด่าน
        for (int i = 0; i < 10; i++) {
            images[i] = load("BG" + (i + 1) + ".png");

            if (images[i] == null) {
                images[i] = load("BG1.png");
            }

            cards[i] = new StageCard(i + 1);
            add(cards[i]);
        }

        // จัดรูปแบบและเพิ่มปุ่ม
        style(launch, true);
        style(back, false);

        add(launch);
        add(back);

        // เหตุการณ์คลิกปุ่ม
        launch.addActionListener(e -> startSelected());
        back.addActionListener(e -> back());

        // ปุ่มลูกศรสำหรับเลือกด่าน
        bind("LEFT", () ->
            select(Math.max(1, selected - 1))
        );

        bind("RIGHT", () ->
            select(Math.min(10, selected + 1))
        );

        bind("UP", () ->
            select(selected > 5 ? selected - 5 : selected)
        );

        bind("DOWN", () ->
            select(selected <= 5 ? selected + 5 : selected)
        );

        bind("ENTER", this::startSelected);
        bind("ESCAPE", this::back);

        select(selected);
    }

    // ==================== โหลดรูปภาพ ====================

    private BufferedImage load(String name) {
        String[] roots = {
            "images/",
            "../images/",
            "ProjectGameOOP/images/"
        };

        for (String root : roots) {
            try {
                File file = new File(root + name);

                if (file.isFile()) {
                    return ImageIO.read(file);
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    // ==================== การควบคุม ====================

    /**
     * ผูกปุ่มแป้นพิมพ์เข้ากับคำสั่ง
     */
    private void bind(String key, Runnable task) {
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
            KeyStroke.getKeyStroke(key),
            key
        );

        getActionMap().put(key, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                task.run();
            }
        });
    }

    /**
     * เลือกด่านและอัปเดตสถานะปุ่มเริ่ม
     */
    private void select(int level) {
        selected = level;

        boolean open = level <= unlocked;

        launch.setEnabled(open);

        launch.setText(
            open
                ? "DEPLOY TO STAGE "
                    + String.format("%02d", level)
                    + "  >"
                : "LOCKED  /  CLEAR STAGE " + (level - 1)
        );

        repaint();
    }

    /**
     * เริ่มด่านที่เลือก หากปลดล็อกแล้ว
     */
    private void startSelected() {
        if (selected > unlocked || frame == null) {
            return;
        }

        frame.setContentPane(new GamePanel(frame, selected));
        frame.revalidate();
        frame.repaint();
    }

    /**
     * กลับหน้าเมนูหลัก
     */
    private void back() {
        if (frame == null) {
            return;
        }

        frame.setContentPane(new StartPanel(frame));
        frame.revalidate();
        frame.repaint();
    }

    /**
     * กำหนดรูปแบบปุ่ม
     */
    private void style(JButton button, boolean primary) {
        button.setFont(
            new Font("SansSerif", Font.BOLD, 13)
        );

        button.setForeground(
            primary ? INK : new Color(215, 225, 231)
        );

        button.setBackground(
            primary ? MINT : new Color(30, 40, 51)
        );

        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.setBorder(
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        );

        button.setFocusPainted(true);

        button.setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
    }

    // ==================== จัดวางองค์ประกอบ ====================

    @Override
    public void doLayout() {
        int margin = 36;
        int gap = 12;

        detailWidth = Math.max(
            250,
            (int) (getWidth() * 0.275)
        );

        detailX = getWidth() - margin - detailWidth;

        int leftWidth = detailX - margin - 24;

        contentHeight = Math.max(
            240,
            getHeight() - contentTop - 104
        );

        // การ์ด 5 คอลัมน์ 2 แถว
        int cardWidth = (leftWidth - gap * 4) / 5;
        int cardHeight = (contentHeight - gap) / 2;

        for (int i = 0; i < 10; i++) {
            int column = i % 5;
            int row = i / 5;

            cards[i].setBounds(
                margin + column * (cardWidth + gap),
                contentTop + row * (cardHeight + gap),
                cardWidth,
                cardHeight
            );
        }

        back.setBounds(
            margin,
            getHeight() - 66,
            164,
            40
        );

        launch.setBounds(
            detailX,
            getHeight() - 66,
            detailWidth,
            40
        );
    }

    // ==================== วาดพื้นหลังและหัวข้อ ====================

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics.create();

        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        // ภาพพื้นหลัง
        if (backdrop != null) {
            cover(
                g,
                backdrop,
                0,
                0,
                getWidth(),
                getHeight()
            );

            g.setColor(new Color(6, 12, 20, 225));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // สีไล่ระดับทับพื้นหลัง
        g.setPaint(new GradientPaint(
            0,
            0,
            new Color(12, 26, 32, 200),
            getWidth(),
            getHeight(),
            new Color(8, 12, 20, 230)
        ));

        g.fillRect(0, 0, getWidth(), getHeight());

        // เส้นตกแต่งแนวทแยง
        g.setColor(new Color(100, 215, 181, 16));

        for (int x = -getHeight(); x < getWidth(); x += 46) {
            g.drawLine(
                x,
                0,
                x + getHeight(),
                getHeight()
            );
        }

        // ชื่อหมวด
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(MINT);

        g.drawString(
            "ZOMBIE SURVIVAL   /   CAMPAIGN",
            36,
            31
        );

        // หัวข้อหลัก
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        g.setColor(Color.WHITE);

        g.drawString(
            "CHOOSE YOUR NEXT FIGHT",
            34,
            73
        );

        // คำอธิบาย
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(MUTED);

        g.drawString(
            "Ten zones. Ten bosses. One way out.",
            36,
            98
        );

        // จำนวนด่านที่ปลดล็อก
        String progress = String.format(
            "%02d / 10 ZONES UNLOCKED",
            unlocked
        );

        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(MINT);

        g.drawString(
            progress,
            getWidth() - 36
                - g.getFontMetrics().stringWidth(progress),
            55
        );

        // แถบความคืบหน้า
        int progressX = getWidth() - 216;

        g.setColor(new Color(46, 62, 68));
        g.fillRoundRect(
            progressX, 70,
            180, 4,
            4, 4
        );

        g.setColor(MINT);
        g.fillRoundRect(
            progressX, 70,
            18 * unlocked, 4,
            4, 4
        );

        drawIntel(g);

        // คำแนะนำการควบคุม
        g.setColor(MUTED);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));

        g.drawString(
            "ARROWS  Select    ENTER  Deploy    ESC  Menu",
            222,
            getHeight() - 41
        );

        g.dispose();
    }

    // ==================== ข้อมูลบอสและด่าน ====================

    private void drawIntel(Graphics2D g) {
        int x = detailX;
        int y = contentTop;
        int width = detailWidth;
        int height = contentHeight;

        Stage stage = Stage.get(selected);
        boolean open = selected <= unlocked;

        // พื้นหลังกรอบข้อมูล
        g.setColor(new Color(21, 31, 41));
        g.fillRoundRect(x, y, width, height, 16, 16);

        g.setColor(new Color(52, 73, 83));
        g.drawRoundRect(
            x, y,
            width - 1, height - 1,
            16, 16
        );

        // หมายเลขด่าน
        g.setColor(MINT);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));

        g.drawString(
            "BOSS INTEL  /  ZONE "
                + String.format("%02d", selected),
            x + 18,
            y + 24
        );

        // ชื่อบอส
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));

        g.drawString(
            stage.getBossName(),
            x + 18,
            y + 50
        );

        int artTop = y + 65;
        int artHeight = Math.max(100, height - 202);

        // แสงด้านหลังภาพบอส
        g.setPaint(new RadialGradientPaint(
            new Point(
                x + width / 2,
                artTop + artHeight / 2
            ),
            Math.max(60, width / 2),
            new float[] {0, 1},
            new Color[] {
                new Color(
                    stage.accent.getRed(),
                    stage.accent.getGreen(),
                    stage.accent.getBlue(),
                    65
                ),
                new Color(16, 24, 33, 0)
            }
        ));

        g.fillRect(
            x + 8,
            artTop,
            width - 16,
            artHeight + 20
        );

        // จำกัดพื้นที่วาดภาพบอส
        Graphics2D art = (Graphics2D) g.create();

        art.clipRect(
            x + 12,
            artTop,
            width - 24,
            artHeight + 15
        );

        if (selected == 1) {
            ironArt.draw(
                art,
                0,
                x + width / 2,
                artTop + artHeight,
                false
            );
        } else {
            bossArt.draw(
                art,
                selected,
                x + width / 2,
                artTop + artHeight,
                artHeight,
                false
            );
        }

        art.dispose();

        // เส้นแบ่งภาพและรายละเอียด
        int infoY = y + height - 115;

        g.setColor(new Color(57, 74, 84));
        g.drawLine(
            x + 18,
            infoY - 13,
            x + width - 18,
            infoY - 13
        );

        // ระดับความอันตราย
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(MUTED);
        g.drawString("THREAT", x + 18, infoY + 4);

        for (int i = 0; i < 10; i++) {
            Color threatColor = i < selected
                ? (selected > 7
                    ? new Color(245, 113, 81)
                    : MINT)
                : new Color(47, 61, 70);

            g.setColor(threatColor);

            g.fillRoundRect(
                x + 78 + i * (width - 100) / 10,
                infoY - 4,
                Math.max(4, (width - 120) / 10),
                9,
                2,
                2
            );
        }

        // จำนวนศัตรูและความยาวด่าน
        g.setColor(new Color(221, 231, 236));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));

        g.drawString(
            stage.quota
                + " ENEMIES   /   "
                + (stage.worldWidth / 1000.0)
                + "k ROUTE",
            x + 18,
            infoY + 29
        );

        // ลักษณะเด่นของด่าน
        g.setColor(MUTED);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));

        g.drawString(
            stage.feature.toUpperCase(java.util.Locale.ROOT),
            x + 18,
            infoY + 50
        );

        // สถานะการปลดล็อก
        g.setColor(
            open ? MINT : new Color(225, 169, 103)
        );

        g.setFont(new Font("SansSerif", Font.BOLD, 11));

        g.drawString(
            open
                ? "READY FOR DEPLOYMENT"
                : "CLEAR STAGE " + (selected - 1) + " TO UNLOCK",
            x + 18,
            infoY + 79
        );
    }

    // ==================== วาดภาพให้เต็มพื้นที่ ====================

    private static void cover(
            Graphics2D g,
            BufferedImage image,
            int x,
            int y,
            int width,
            int height) {

        Graphics2D copy = (Graphics2D) g.create();

        copy.clipRect(x, y, width, height);

        // ขยายภาพให้เต็มพื้นที่โดยรักษาสัดส่วน
        double scale = Math.max(
            width / (double) image.getWidth(),
            height / (double) image.getHeight()
        );

        int drawWidth = (int) (image.getWidth() * scale);
        int drawHeight = (int) (image.getHeight() * scale);

        copy.drawImage(
            image,
            x + (width - drawWidth) / 2,
            y + (height - drawHeight) / 2,
            drawWidth,
            drawHeight,
            null
        );

        copy.dispose();
    }

    // ==================== การ์ดเลือกด่าน ====================

    private class StageCard extends JButton {

        private final int level;

        StageCard(int level) {
            this.level = level;

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);

            setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );

            getAccessibleContext().setAccessibleName(
                "Stage "
                    + level
                    + " "
                    + Stage.get(level).name
                    + (level > unlocked
                        ? " locked"
                        : " available")
            );

            // คลิกการ์ดเพื่อเลือกด่าน
            addActionListener(e -> select(level));

            // วาดใหม่เมื่อสถานะปุ่มเปลี่ยน
            getModel().addChangeListener(e -> repaint());
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            boolean chosen = selected == level;
            boolean locked = level > unlocked;
            boolean hover = getModel().isRollover();

            // จำกัดการวาดให้เป็นกรอบมุมมน
            g.setClip(new RoundRectangle2D.Float(
                0, 0,
                width, height,
                12, 12
            ));

            // ภาพพื้นหลังการ์ด
            if (images[level - 1] != null) {
                cover(
                    g,
                    images[level - 1],
                    0,
                    0,
                    width,
                    height
                );
            } else {
                g.setColor(Stage.get(level).sky);
                g.fillRect(0, 0, width, height);
            }

            // เงาทับภาพ โดยด่านที่ล็อกจะมืดกว่า
            g.setPaint(new GradientPaint(
                0,
                0,
                new Color(4, 9, 15, locked ? 140 : 25),
                0,
                height,
                new Color(5, 12, 20, 250)
            ));

            g.fillRect(0, 0, width, height);

            if (hover) {
                g.setColor(new Color(116, 234, 197, 28));
                g.fillRect(0, 0, width, height);
            }

            // พื้นหลังหมายเลขด่าน
            g.setColor(
                chosen ? MINT : new Color(17, 25, 34, 200)
            );

            g.fillRoundRect(10, 10, 32, 24, 6, 6);

            g.setColor(chosen ? INK : Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));

            g.drawString(
                String.format("%02d", level),
                17,
                27
            );

            // สัญลักษณ์ล็อก
            if (locked) {
                g.setColor(new Color(235, 192, 135));
                g.setStroke(new BasicStroke(2));

                g.drawArc(
                    width - 28,
                    15,
                    10,
                    12,
                    0,
                    180
                );

                g.drawRoundRect(
                    width - 30,
                    21,
                    14,
                    11,
                    2,
                    2
                );
            }

            // ชื่อด่าน แบ่งได้สูงสุด 2 บรรทัด
            String[] words = Stage.get(level).name.split(" ", 2);

            g.setFont(new Font("SansSerif", Font.BOLD, 11));

            g.setColor(
                locked
                    ? new Color(180, 187, 193)
                    : Color.WHITE
            );

            g.drawString(words[0], 10, height - 48);

            if (words.length > 1) {
                g.drawString(words[1], 10, height - 33);
            }

            // สถานะของด่าน
            g.setFont(new Font("SansSerif", Font.BOLD, 9));

            g.setColor(
                locked
                    ? new Color(225, 169, 103)
                    : MINT
            );

            String status = locked
                ? "LOCKED"
                : (level == unlocked
                    ? "NEXT MISSION"
                    : "AVAILABLE");

            g.drawString(status, 10, height - 13);

            // กรอบการ์ด
            g.setClip(null);

            Color borderColor = chosen
                ? MINT
                : (hover || hasFocus()
                    ? new Color(150, 190, 190)
                    : new Color(65, 78, 88));

            g.setColor(borderColor);
            g.setStroke(new BasicStroke(chosen ? 2.5f : 1));

            g.drawRoundRect(
                1,
                1,
                width - 3,
                height - 3,
                12,
                12
            );

            g.dispose();
        }
    }
}