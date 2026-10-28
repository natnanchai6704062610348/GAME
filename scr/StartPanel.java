
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class StartPanel extends JPanel {

    private BufferedImage backgroundImage;

    public StartPanel(JFrame frame) {

        setLayout(new GridBagLayout());
        setBackground(new Color(25, 9, 18));

        
String[] paths = {
    "images/menu_bg.png",
    "../images/menu_bg.png",
    "ProjectGameOOP/images/menu_bg.png"
};

System.out.println("Working directory: "
        + System.getProperty("user.dir"));

for (String path : paths) {
    File file = new File(path);

    System.out.println("Checking: " + file.getAbsolutePath());

    if (file.isFile()) {
        try {
            backgroundImage = ImageIO.read(file);

            if (backgroundImage != null) {
                System.out.println("IMAGE LOADED SUCCESSFULLY!");
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

if (backgroundImage == null) {
    System.out.println("ERROR: menu_bg.png not found!");
}


        JButton start = new JButton("START GAME") {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color top;
                Color bottom;

                if (getModel().isPressed()) {
                    top = new Color(110, 10, 15);
                    bottom = new Color(65, 5, 10);
                } else if (getModel().isRollover()) {
                    top = new Color(245, 65, 50);
                    bottom = new Color(145, 15, 20);
                } else {
                    top = new Color(190, 35, 30);
                    bottom = new Color(85, 10, 15);
                }

                g2.setPaint(new GradientPaint(
                    0, 0, top,
                    0, getHeight(), bottom
                ));

                g2.fillRoundRect(
                    3, 3,
                    getWidth() - 6,
                    getHeight() - 6,
                    14, 14
                );

                g2.setColor(new Color(255, 170, 140));
                g2.setStroke(new BasicStroke(3));

                g2.drawRoundRect(
                    3, 3,
                    getWidth() - 7,
                    getHeight() - 7,
                    14, 14
                );

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();

                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2
                        + fm.getAscent();

                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        start.setPreferredSize(new Dimension(320, 65));
        start.setFont(new Font(Font.MONOSPACED, Font.BOLD, 25));
        start.setContentAreaFilled(false);
        start.setBorderPainted(false);
        start.setFocusPainted(false);
        start.setCursor(new Cursor(Cursor.HAND_CURSOR));

        start.addActionListener(e -> {
            frame.setContentPane(new LevelPanel(frame));
            frame.revalidate();
            frame.repaint();
        });

        JPanel menu = new JPanel(new GridBagLayout());
        menu.setOpaque(false);

        menu.setPreferredSize(new Dimension(450, 180));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(90, 0, 0, 0);

        menu.add(start, gbc);

        add(menu);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(
                backgroundImage,
                0, 0,
                getWidth(), getHeight(),
                null
            );
        }
    }
}
