package blaketen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MainForm extends JFrame {

    private final MainScreen scr;

    public static boolean isC(char c) {
        if (c >= 0xE001 && c <= 0xE009) return true;
        if (c >= 128) return false;
        return Character.isLetterOrDigit(c) || c == ' ';
    }

    public void doKey(KeyEvent e) {
        char c = e.getKeyChar();
        if (!isC(c)) return;
        scr.c = scr.c >= '1' && scr.c <= '9' && c == '0' ? ((char) (0xE000 + scr.c - '0')) : c;
        EventQueue.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        MainForm.this.repaint();
                        sayCH(scr.c);
                    }
                });
    }

    private class MyDispatcher implements KeyEventDispatcher {

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_RELEASED)
                if (e.getModifiers() == 0 && !e.isActionKey()) doKey(e);
            return false;
        }
    }

    public MainForm() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        scr = new MainScreen();
        add(scr);
        pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(
                dim.width / 2 - this.getSize().width / 2,
                dim.height / 2 - this.getSize().height / 2);
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException ex) {
            throw new RuntimeException(ex);
        }

        EventQueue.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        MainForm mf = new MainForm();
                        mf.setVisible(true);
                        mf.setExtendedState(mf.getExtendedState() | JFrame.MAXIMIZED_BOTH);
                    }
                });
    }

    private class MainScreen extends JComponent {

        private BufferedImage img;
        private char c = ' ';

        private MainScreen() {
            try (InputStream in =
                    getClass().getClassLoader().getResourceAsStream("blaketen/logo.jpg")) {
                this.img = ImageIO.read(in);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            RenderingHints rh =
                    new RenderingHints(
                            RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHints(rh);
            g.setColor(Color.black);
            Dimension z = getSize();
            g.fillRect(0, 0, z.width, z.height);
            int imgX = (z.width / 2) - (img.getWidth() / 2);
            int imgY = (z.height / 2) - (img.getHeight() / 2);
            if (c == ' ') {
                g.drawImage(img, imgX, imgY, null);
                g.setColor(Color.CYAN);
                int pixelSize = 50;
                double fontSize =
                        pixelSize * Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
                Font f = new Font("Arial", Font.BOLD, (int) fontSize);
                g.setFont(f);
                FontMetrics fm = g.getFontMetrics(f);
                g.drawString("BLAKE", imgX - 40, imgY - 20);
                Rectangle2D b = fm.getStringBounds("BLAKE", g);
                g.setColor(Color.GREEN);
                g.drawString("10", (int) (imgX + b.getWidth() - 40), imgY - 20);
            } else {
                String s =
                        c >= 0xE001 && c <= 0xE009
                                ? (c - 0xE000) + "0"
                                : Character.isLetter(c)
                                        ? String.valueOf(c).toUpperCase()
                                                + String.valueOf(c).toLowerCase()
                                        : String.valueOf(c);
                double fontSize =
                        0.80
                                * img.getHeight()
                                * Toolkit.getDefaultToolkit().getScreenResolution()
                                / 72.0;
                Font f = new Font("Arial", Font.BOLD, (int) fontSize);
                g.setFont(f);
                g.setColor(Color.GREEN);
                g.drawString(s, imgX, imgY + g.getFontMetrics(f).getAscent());
            }
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(img.getWidth() + 200, img.getHeight() + 200);
        }
    }

    private static final ScriptEngine ase = getAppleScriptEngine();

    public static ScriptEngine getAppleScriptEngine() {
        try {
            Class<?> c = Class.forName("apple.applescript.AppleScriptEngineFactory");
            Object f = c.newInstance();
            Method m = c.getMethod("getScriptEngine", new Class[0]);
            return (ScriptEngine) m.invoke(f, new Object[0]);
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | NoSuchMethodException
                | SecurityException
                | IllegalArgumentException
                | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void sayCH(char c) {
        if (!isC(c)) return;
        String s =
                c >= 0xE001 && c <= 0xE009 ? (c - 0xE000) + "0" : String.valueOf(c).toLowerCase();
        say(s);
    }

    private static final ReentrantLock lock = new ReentrantLock();

    public static void say(final String s) {
        if (s.trim().isEmpty()) return;
        Thread th =
                new Thread(
                        new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    if (!lock.tryLock(1, TimeUnit.SECONDS)) return;
                                    try {
                                        Thread.sleep(200);
                                        ase.eval("say \"" + s + "\"");
                                    } finally {
                                        lock.unlock();
                                    }
                                } catch (InterruptedException | ScriptException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        });
        th.start();
    }
}
