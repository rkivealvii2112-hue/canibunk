package com.canibunk;

import com.canibunk.ds.CustomHashMap;
import com.canibunk.model.*;
import com.canibunk.service.*;
import com.canibunk.scraper.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CanIBunk.com - Colourful & Animated Swing GUI Menu
 * Built using javax.swing and java.awt for the academic project.
 * Features animated login page with floating particles, glowing elements,
 * and a polished main menu with hover glow effects.
 */
public class CanIBunkGUI extends JFrame {

    // ── Colour Palette ──────────────────────────────────────────────
    private static final Color BG_DARK        = new Color(18, 18, 30);
    private static final Color BG_CARD        = new Color(30, 30, 52);
    private static final Color BG_CARD_HOVER  = new Color(42, 42, 70);
    private static final Color ACCENT_PURPLE  = new Color(138, 92, 246);
    private static final Color ACCENT_CYAN    = new Color(34, 211, 238);
    private static final Color ACCENT_PINK    = new Color(244, 114, 182);
    private static final Color ACCENT_GREEN   = new Color(74, 222, 128);
    private static final Color ACCENT_AMBER   = new Color(251, 191, 36);
    private static final Color ACCENT_RED     = new Color(248, 113, 113);
    private static final Color TEXT_PRIMARY    = new Color(240, 240, 255);
    private static final Color TEXT_SECONDARY  = new Color(160, 160, 190);
    private static final Color GRADIENT_START  = new Color(88, 28, 135);
    private static final Color GRADIENT_MID    = new Color(55, 15, 100);
    private static final Color GRADIENT_END    = new Color(15, 23, 42);
    private static final Color SAFE_GREEN      = new Color(34, 197, 94);
    private static final Color DANGER_RED      = new Color(239, 68, 68);
    private static final Color EDGE_YELLOW     = new Color(234, 179, 8);
    private static final Color GLASS_BG        = new Color(35, 35, 60, 180);
    private static final Color GLASS_BORDER    = new Color(100, 100, 160, 100);

    // ── Fonts ────────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON  = new Font("Segoe UI Semibold", Font.BOLD, 15);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 13);
    private static final Font FONT_LOGO    = new Font("Segoe UI", Font.BOLD, 36);

    // ── Backend services (reused from the CLI Main) ─────────────────
    private final CustomHashMap<String, Subject> subjectRegistry = new CustomHashMap<>();
    private final DangerZoneService dangerZoneService = new DangerZoneService();
    private final ScheduleManager scheduleManager = new ScheduleManager();
    private final CalendarManager calendarManager = new CalendarManager();
    private final AcademicCalendarService calendarService = new AcademicCalendarService();

    // ── UI panels ───────────────────────────────────────────────────
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    // ── Animation timers ────────────────────────────────────────────
    private Timer loginAnimTimer;

    // ── Constructor ─────────────────────────────────────────────────
    public CanIBunkGUI() {
        super("CanIBunk.com — Attendance Monitor & Bunk Predictor");
        initSampleData();
        initUI();
    }

    // ══════════════════════════════════════════════════════════════════
    //  DATA INITIALIZATION  (mirrors Main.initSampleData)
    // ══════════════════════════════════════════════════════════════════
    private void initSampleData() {
        subjectRegistry.put("CS201", new Subject("CS201", "Discrete Mathematical Structures", 22, 25, 4));
        subjectRegistry.put("CS203", new Subject("CS203", "Data Structures & Algorithms",    19, 24, 4));
        subjectRegistry.put("CS205", new Subject("CS205", "Object Oriented Programming (Java)", 24, 26, 4));
        subjectRegistry.put("CS207", new Subject("CS207", "Digital Electronics & Logic Design", 14, 20, 3));
        subjectRegistry.put("EST200", new Subject("EST200", "Design & Engineering",             9, 14, 2));
        subjectRegistry.put("CSL201", new Subject("CSL201", "Data Structures Lab",             18, 18, 2));

        dangerZoneService.rebuildTree(subjectRegistry.values());

        TimetableParser.TimetableResult sampleTt = getSampleTimetableResult();
        scheduleManager.setWeeklySchedule(sampleTt.getScheduleByDay());

        scheduleManager.addSession(new ClassSession(1, "09:00 - 10:00", "CS203", "Data Structures & Algorithms", "LH-101"));
        scheduleManager.addSession(new ClassSession(2, "10:00 - 11:00", "CS207", "Digital Electronics",          "LH-101"));
        scheduleManager.addSession(new ClassSession(3, "11:15 - 12:15", "CS205", "Java Programming",             "CC-Lab 2"));
        scheduleManager.addSession(new ClassSession(4, "01:15 - 02:15", "CS201", "Discrete Mathematics",         "LH-101"));
        scheduleManager.addSession(new ClassSession(5, "02:15 - 03:15", "EST200", "Design & Engineering",        "LH-104"));

        boolean calendarLoaded = calendarService.loadCalendar(calendarManager, true, false);
        if (!calendarLoaded || calendarManager.getTotalDays() == 0) {
            LocalDate base = LocalDate.of(2026, 10, 19);
            calendarManager.addDay(new CalendarDay(base,                true,  "Regular Working Day",        41));
            calendarManager.addDay(new CalendarDay(base.plusDays(1),     true,  "Regular Working Day - Today",42));
            calendarManager.addDay(new CalendarDay(base.plusDays(2),     true,  "Regular Working Day",        43));
            calendarManager.addDay(new CalendarDay(base.plusDays(3),     true,  "Regular Working Day",        44));
            calendarManager.addDay(new CalendarDay(base.plusDays(4),     true,  "Regular Working Day",        45));
            calendarManager.addDay(new CalendarDay(base.plusDays(5),     false, "Saturday - Weekend",         46));
            calendarManager.addDay(new CalendarDay(base.plusDays(6),     false, "Sunday - Weekend",           47));
            calendarManager.addDay(new CalendarDay(base.plusDays(7),     false, "Diwali Festival Holiday",    48));
            calendarManager.addDay(new CalendarDay(base.plusDays(8),     true,  "Regular Working Day",        49));
            calendarManager.addDay(new CalendarDay(base.plusDays(9),     true,  "Series Exam 2 (Internal)",   50));
            calendarManager.addDay(new CalendarDay(base.plusDays(10),    true,  "Series Exam 2 (Internal)",   51));
            calendarManager.addDay(new CalendarDay(base.plusDays(11),    true,  "Series Exam 2 (Internal)",   52));
            calendarManager.addDay(new CalendarDay(base.plusDays(12),    false, "Weekend",                    53));
            calendarManager.addDay(new CalendarDay(base.plusDays(13),    false, "Weekend",                    54));
            calendarManager.addDay(new CalendarDay(base.plusDays(14),    true,  "End Semester Revision Starts",55));
        }
    }

    private TimetableParser.TimetableResult getSampleTimetableResult() {
        TimetableParser.TimetableResult tt = new TimetableParser.TimetableResult();
        tt.addSession("MONDAY",    new ClassSession(1, "09:00 - 10:00", "CS203", "Data Structures & Algorithms", "LH-101"));
        tt.addSession("MONDAY",    new ClassSession(2, "10:00 - 11:00", "CS207", "Digital Electronics",          "LH-101"));
        tt.addSession("MONDAY",    new ClassSession(3, "11:15 - 12:15", "CS205", "Java Programming",             "CC-Lab 2"));
        tt.addSession("MONDAY",    new ClassSession(4, "01:15 - 02:15", "CS201", "Discrete Mathematics",         "LH-101"));
        tt.addSession("MONDAY",    new ClassSession(5, "02:15 - 03:15", "EST200", "Design & Engineering",        "LH-104"));
        tt.addSession("TUESDAY",   new ClassSession(1, "09:00 - 10:00", "CS201", "Discrete Mathematics",         "LH-101"));
        tt.addSession("TUESDAY",   new ClassSession(2, "10:00 - 11:00", "CS205", "Java Programming",             "CC-Lab 2"));
        tt.addSession("TUESDAY",   new ClassSession(3, "11:15 - 12:15", "CS203", "Data Structures & Algorithms", "LH-101"));
        tt.addSession("TUESDAY",   new ClassSession(4, "01:15 - 02:15", "CS207", "Digital Electronics",          "LH-101"));
        tt.addSession("TUESDAY",   new ClassSession(5, "02:15 - 03:15", "EST200", "Design & Engineering",        "LH-104"));
        tt.addSession("WEDNESDAY", new ClassSession(1, "09:00 - 10:00", "CS205", "Java Programming",             "CC-Lab 2"));
        tt.addSession("WEDNESDAY", new ClassSession(2, "10:00 - 11:00", "CS201", "Discrete Mathematics",         "LH-101"));
        tt.addSession("WEDNESDAY", new ClassSession(3, "11:15 - 12:15", "CS203", "Data Structures & Algorithms", "LH-101"));
        tt.addSession("WEDNESDAY", new ClassSession(4, "01:15 - 02:15", "CS207", "Digital Electronics",          "LH-101"));
        tt.addSession("WEDNESDAY", new ClassSession(5, "02:15 - 03:15", "CS201", "Discrete Mathematics",         "LH-101"));
        tt.addSession("THURSDAY",  new ClassSession(1, "09:00 - 10:00", "CS207", "Digital Electronics",          "LH-101"));
        tt.addSession("THURSDAY",  new ClassSession(2, "10:00 - 11:00", "CS205", "Java Programming",             "CC-Lab 2"));
        tt.addSession("THURSDAY",  new ClassSession(3, "11:15 - 12:15", "CS201", "Discrete Mathematics",         "LH-101"));
        tt.addSession("THURSDAY",  new ClassSession(4, "01:15 - 02:15", "CS203", "Data Structures & Algorithms", "LH-101"));
        tt.addSession("THURSDAY",  new ClassSession(5, "02:15 - 03:15", "EST200", "Design & Engineering",        "LH-104"));
        tt.addSession("FRIDAY",    new ClassSession(1, "09:00 - 10:00", "CS201", "Discrete Mathematics",         "LH-101"));
        tt.addSession("FRIDAY",    new ClassSession(2, "10:00 - 11:00", "CS205", "Java Programming",             "CC-Lab 2"));
        tt.addSession("FRIDAY",    new ClassSession(3, "11:15 - 12:15", "CS207", "Digital Electronics",          "LH-101"));
        tt.addSession("FRIDAY",    new ClassSession(4, "01:15 - 02:15", "CS203", "Data Structures & Algorithms", "LH-101"));
        return tt;
    }

    // ══════════════════════════════════════════════════════════════════
    //  UI INITIALISATION
    // ══════════════════════════════════════════════════════════════════
    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 720);
        setMinimumSize(new Dimension(920, 660));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        // Build all card panels
        contentPanel.setOpaque(false);
        contentPanel.add(buildLoginPanel(),       "LOGIN");
        contentPanel.add(buildMainMenuPanel(),    "MENU");
        contentPanel.add(buildSchedulePanel(),    "SCHEDULE");
        contentPanel.add(buildAttendancePanel(),  "ATTENDANCE");
        contentPanel.add(buildCalendarPanel(),    "CALENDAR");
        contentPanel.add(buildBunkabilityPanel(), "BUNKABILITY");

        add(contentPanel, BorderLayout.CENTER);

        // Start with the login screen
        cardLayout.show(contentPanel, "LOGIN");
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  1. LOGIN PANEL — Animated with floating particles & glow
    // ══════════════════════════════════════════════════════════════════

    /** A single floating particle for the login background animation */
    private static class Particle {
        double x, y, vx, vy, radius;
        float alpha;
        Color color;
        Particle(Random r, int w, int h) {
            x = r.nextDouble() * w;
            y = r.nextDouble() * h;
            vx = (r.nextDouble() - 0.5) * 1.2;
            vy = (r.nextDouble() - 0.5) * 1.2;
            radius = 2 + r.nextDouble() * 4;
            alpha = 0.15f + r.nextFloat() * 0.45f;
            Color[] palette = {ACCENT_CYAN, ACCENT_PURPLE, ACCENT_PINK, ACCENT_GREEN, ACCENT_AMBER};
            color = palette[r.nextInt(palette.length)];
        }
        void tick(int w, int h) {
            x += vx; y += vy;
            if (x < 0 || x > w) vx = -vx;
            if (y < 0 || y > h) vy = -vy;
        }
    }

    private JPanel buildLoginPanel() {
        final ArrayList<Particle> particles = new ArrayList<>();
        final Random rng = new Random(42);
        for (int i = 0; i < 50; i++) particles.add(new Particle(rng, 1050, 720));

        // Animated gradient + particle background
        final float[] gradAngle = {0f};
        final float[] glowPhase = {0f};
        final int[] typingIdx = {0};
        final String typingText = "Log in to AES Portal to sync your attendance...";

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Animated diagonal gradient background
                double angle = Math.toRadians(gradAngle[0]);
                float dx = (float) (Math.cos(angle) * w);
                float dy = (float) (Math.sin(angle) * h);
                GradientPaint gp = new GradientPaint(
                    w / 2f - dx / 2, h / 2f - dy / 2, GRADIENT_START,
                    w / 2f + dx / 2, h / 2f + dy / 2, GRADIENT_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Draw a subtle radial glow at the center
                float glow = (float) (0.12 + 0.08 * Math.sin(glowPhase[0]));
                RadialGradientPaint rgp = new RadialGradientPaint(
                    w / 2f, h / 2f - 30, Math.max(w, h) * 0.45f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(138, 92, 246, (int)(glow * 255)), new Color(0, 0, 0, 0)});
                g2.setPaint(rgp);
                g2.fillRect(0, 0, w, h);

                // Floating particles
                for (Particle p : particles) {
                    p.tick(w, h);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha));
                    g2.setColor(p.color);
                    g2.fill(new Ellipse2D.Double(p.x - p.radius, p.y - p.radius, p.radius * 2, p.radius * 2));
                    // Draw a subtle glow around each particle
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha * 0.3f));
                    g2.fill(new Ellipse2D.Double(p.x - p.radius * 2.5, p.y - p.radius * 2.5, p.radius * 5, p.radius * 5));
                }
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                // Draw connection lines between close particles
                g2.setStroke(new BasicStroke(0.5f));
                for (int i = 0; i < particles.size(); i++) {
                    for (int j = i + 1; j < particles.size(); j++) {
                        Particle a = particles.get(i), b = particles.get(j);
                        double dist = Math.hypot(a.x - b.x, a.y - b.y);
                        if (dist < 120) {
                            float lineAlpha = (float)((120 - dist) / 120.0 * 0.15);
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, lineAlpha));
                            g2.setColor(new Color(160, 160, 220));
                            g2.draw(new Line2D.Double(a.x, a.y, b.x, b.y));
                        }
                    }
                }
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Glassmorphism login card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Glass background
                g2.setColor(GLASS_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 32, 32));
                // Subtle glass border with glow
                float borderGlow = (float) (0.4 + 0.2 * Math.sin(glowPhase[0] * 1.5));
                g2.setColor(new Color(130, 130, 200, (int)(borderGlow * 255)));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 32, 32));
                // Top accent glow line
                GradientPaint topGlow = new GradientPaint(0, 0, ACCENT_PURPLE, getWidth(), 0, ACCENT_CYAN);
                g2.setPaint(topGlow);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 8, 8));
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(400, 480));
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 24, 6, 24);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;

        // ── Logo icon with glow ──
        JLabel icon = new JLabel("\uD83C\uDF93", SwingConstants.CENTER); // 🎓
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        icon.setForeground(TEXT_PRIMARY);
        c.insets = new Insets(24, 24, 0, 24);
        card.add(icon, c);

        // ── Title with gradient-like colouring ──
        JLabel title = new JLabel("CanIBunk.com", SwingConstants.CENTER);
        title.setFont(FONT_LOGO);
        title.setForeground(ACCENT_CYAN);
        c.insets = new Insets(4, 24, 0, 24);
        card.add(title, c);

        // ── Animated typing subtitle ──
        JLabel subtitle = new JLabel(" ", SwingConstants.CENTER);
        subtitle.setFont(FONT_BODY);
        subtitle.setForeground(TEXT_SECONDARY);
        c.insets = new Insets(0, 24, 18, 24);
        card.add(subtitle, c);

        // ── Username field ──
        JLabel userLabel = new JLabel("  \uD83D\uDC64  Admission No / Username");
        userLabel.setFont(FONT_SMALL);
        userLabel.setForeground(TEXT_SECONDARY);
        c.insets = new Insets(4, 24, 2, 24);
        card.add(userLabel, c);

        JTextField userField = createStyledTextField();
        c.insets = new Insets(0, 24, 10, 24);
        card.add(userField, c);

        // ── Password field with show/hide toggle ──
        JLabel passLabel = new JLabel("  \uD83D\uDD12  Password");
        passLabel.setFont(FONT_SMALL);
        passLabel.setForeground(TEXT_SECONDARY);
        c.insets = new Insets(4, 24, 2, 24);
        card.add(passLabel, c);

        JPanel passPanel = new JPanel(new BorderLayout(4, 0));
        passPanel.setOpaque(false);
        JPasswordField passField = createStyledPasswordField();
        passPanel.add(passField, BorderLayout.CENTER);

        JButton togglePass = new JButton("\uD83D\uDC41"); // 👁
        togglePass.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        togglePass.setPreferredSize(new Dimension(42, 38));
        togglePass.setBackground(new Color(50, 50, 75));
        togglePass.setForeground(TEXT_SECONDARY);
        togglePass.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 110), 1, true));
        togglePass.setFocusPainted(false);
        togglePass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final boolean[] passVisible = {false};
        togglePass.addActionListener(e -> {
            passVisible[0] = !passVisible[0];
            passField.setEchoChar(passVisible[0] ? (char) 0 : '\u2022');
            togglePass.setText(passVisible[0] ? "\uD83D\uDE48" : "\uD83D\uDC41"); // 🙈 / 👁
        });
        passPanel.add(togglePass, BorderLayout.EAST);
        c.insets = new Insets(0, 24, 16, 24);
        card.add(passPanel, c);

        // ── Login button (gradient purple → cyan) ──
        JButton loginBtn = createGradientButton("  \uD83D\uDD13  Log In & Sync  ", ACCENT_PURPLE, ACCENT_CYAN);
        c.insets = new Insets(4, 24, 6, 24);
        card.add(loginBtn, c);

        // ── Skip button (subtle dark) ──
        JButton skipBtn = createGradientButton("  \u26A1  Skip  —  Use Sample Data  ", new Color(55, 55, 80), new Color(75, 75, 110));
        c.insets = new Insets(0, 24, 10, 24);
        card.add(skipBtn, c);

        // ── Status label ──
        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(ACCENT_AMBER);
        c.insets = new Insets(0, 24, 20, 24);
        card.add(statusLabel, c);

        // ── Actions ──
        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            char[] password = passField.getPassword();
            if (username.isEmpty() || password.length == 0) {
                statusLabel.setText("\u26A0 Please enter both username and password.");
                statusLabel.setForeground(ACCENT_AMBER);
                return;
            }
            statusLabel.setText("\u23F3 Connecting to AES portal...");
            statusLabel.setForeground(ACCENT_CYAN);
            loginBtn.setEnabled(false);
            skipBtn.setEnabled(false);

            // Run scraper in background thread to keep UI responsive
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    try {
                        AesPortalScraper scraper = new AesPortalScraper(true);
                        AesPortalScraper.ScraperResult result = scraper.scrapePortal(
                                username, password, subjectRegistry, scheduleManager);
                        if (result.getSubjectCount() > 0) {
                            dangerZoneService.rebuildTree(subjectRegistry.values());
                            return true;
                        }
                    } catch (Exception ex) {
                        // Fall through
                    }
                    return false;
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            statusLabel.setText("\u2714 Synced successfully!");
                            statusLabel.setForeground(ACCENT_GREEN);
                        } else {
                            statusLabel.setText("\u26A0 Could not sync. Proceeding with cached data.");
                            statusLabel.setForeground(ACCENT_AMBER);
                            loadCachedDataIfAvailable();
                        }
                    } catch (Exception ex) {
                        statusLabel.setText("\u26A0 Connection error. Using sample data.");
                        statusLabel.setForeground(ACCENT_RED);
                    }
                    // Clear password from memory
                    java.util.Arrays.fill(password, '\0');
                    // Transition to main menu after a brief pause
                    Timer t = new Timer(1200, ev -> {
                        stopLoginAnimation();
                        refreshAllPanels();
                        cardLayout.show(contentPanel, "MENU");
                    });
                    t.setRepeats(false);
                    t.start();
                }
            };
            worker.execute();
        });

        skipBtn.addActionListener(e -> {
            loadCachedDataIfAvailable();
            stopLoginAnimation();
            refreshAllPanels();
            cardLayout.show(contentPanel, "MENU");
        });

        // Enter key on password field triggers login
        passField.addActionListener(e -> loginBtn.doClick());

        panel.add(card, gbc);

        // ── Start animation timer ──
        loginAnimTimer = new Timer(30, e -> {
            gradAngle[0] += 0.3f;
            if (gradAngle[0] > 360) gradAngle[0] -= 360;
            glowPhase[0] += 0.05f;
            // Typing animation
            if (typingIdx[0] < typingText.length()) {
                typingIdx[0]++;
                subtitle.setText(typingText.substring(0, typingIdx[0]) + "\u258C"); // ▌ cursor
            } else {
                // Blinking cursor
                boolean showCursor = ((int)(glowPhase[0] * 10)) % 2 == 0;
                subtitle.setText(typingText + (showCursor ? "\u258C" : " "));
            }
            panel.repaint();
        });
        loginAnimTimer.start();

        return panel;
    }

    private void stopLoginAnimation() {
        if (loginAnimTimer != null && loginAnimTimer.isRunning()) {
            loginAnimTimer.stop();
        }
    }

    private void loadCachedDataIfAvailable() {
        java.nio.file.Path attPath = java.nio.file.Path.of("scraped_attendance_debug.html");
        if (java.nio.file.Files.exists(attPath)) {
            try {
                String html = java.nio.file.Files.readString(attPath);
                List<Subject> parsed = AttendanceTableParser.parseHtmlTable(html);
                if (!parsed.isEmpty()) {
                    AttendanceTableParser.populateRegistry(parsed, subjectRegistry, true);
                    dangerZoneService.rebuildTree(subjectRegistry.values());
                }
            } catch (Exception ignored) {}
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  2. MAIN MENU PANEL — Enhanced with glow cards
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildMainMenuPanel() {
        GradientPanel panel = new GradientPanel(BG_DARK, new Color(10, 10, 25));
        panel.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));

        JLabel titleLabel = new JLabel("\uD83C\uDF93 CanIBunk.com");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(ACCENT_CYAN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(titleLabel);

        JLabel subLabel = new JLabel("Intelligent Attendance Monitor & Bunk Predictor");
        subLabel.setFont(FONT_BODY);
        subLabel.setForeground(TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(Box.createVerticalStrut(4));
        header.add(subLabel);

        JLabel ruleLabel = new JLabel("75.0% Strict Attendance Threshold  \u2022  Semester 3");
        ruleLabel.setFont(FONT_SMALL);
        ruleLabel.setForeground(new Color(120, 120, 160));
        ruleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(Box.createVerticalStrut(4));
        header.add(ruleLabel);

        panel.add(header, BorderLayout.NORTH);

        // Button grid — 2×2 with glow cards
        JPanel grid = new JPanel(new GridLayout(2, 2, 22, 22));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(24, 60, 24, 60));

        grid.add(createMenuCard("\u23F0", "View Today's Schedule",
                "Check the day & see upcoming classes",    ACCENT_CYAN,   "SCHEDULE"));
        grid.add(createMenuCard("\uD83D\uDCCA", "Attendance Status",
                "View all subjects & attendance %",        ACCENT_GREEN,  "ATTENDANCE"));
        grid.add(createMenuCard("\uD83D\uDCC5", "Academic Calendar",
                "Working days left & semester milestones",  ACCENT_AMBER,  "CALENDAR"));
        grid.add(createMenuCard("\uD83C\uDFAF", "Check Bunkability",
                "Choose subject \u2192 see % drop & verdict", ACCENT_PINK,   "BUNKABILITY"));

        panel.add(grid, BorderLayout.CENTER);

        // Footer with user-info bar and exit button
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 60, 20, 60));

        // Left side: current date
        JLabel dateLabel = new JLabel("\uD83D\uDCC6  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        dateLabel.setFont(FONT_SMALL);
        dateLabel.setForeground(TEXT_SECONDARY);
        footer.add(dateLabel, BorderLayout.WEST);

        // Center: exit button
        JPanel exitWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exitWrap.setOpaque(false);
        JButton exitBtn = createGradientButton("  Exit  ", new Color(80, 20, 20), new Color(140, 30, 30));
        exitBtn.setPreferredSize(new Dimension(120, 36));
        exitBtn.addActionListener(e -> System.exit(0));
        exitWrap.add(exitBtn);
        footer.add(exitWrap, BorderLayout.CENTER);

        // Right side: subject count
        JLabel countLabel = new JLabel(subjectRegistry.size() + " subjects loaded  \u2714");
        countLabel.setFont(FONT_SMALL);
        countLabel.setForeground(ACCENT_GREEN);
        footer.add(countLabel, BorderLayout.EAST);

        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a single menu card with glow-on-hover effect.
     * Paints a rounded rectangle with an accent-coloured top bar and 
     * draws a glow border ring on hover using transparent accent colours.
     */
    private JPanel createMenuCard(String emoji, String title, String desc, Color accent, String target) {
        final boolean[] hovered = {false};
        final float[] hoverAnim = {0f}; // 0 → 1 for smooth transition
        final Timer[] animTimer = {null};

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Glow shadow behind card on hover
                if (hoverAnim[0] > 0.01f) {
                    int glowSize = (int) (12 * hoverAnim[0]);
                    for (int i = glowSize; i > 0; i--) {
                        float alpha = hoverAnim[0] * 0.04f * (glowSize - i + 1);
                        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(),
                                (int)(Math.min(alpha, 1f) * 255)));
                        g2.fill(new RoundRectangle2D.Float(-i, -i, w + i * 2, h + i * 2, 24 + i, 24 + i));
                    }
                }

                // Card fill
                Color fillColor = hovered[0] ? BG_CARD_HOVER : BG_CARD;
                g2.setColor(fillColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 24, 24));

                // Accent bar on top
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, 5, 6, 6));

                // Border on hover
                if (hoverAnim[0] > 0.01f) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(),
                            (int)(hoverAnim[0] * 180)));
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 24, 24));
                }

                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        emojiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(emojiLabel);
        card.add(Box.createVerticalStrut(12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADING);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(FONT_BODY);
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(descLabel);
        card.add(Box.createVerticalGlue());

        // Arrow indicator at bottom right
        JLabel arrowLabel = new JLabel("\u2192");
        arrowLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrowLabel.setForeground(accent);
        arrowLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        card.add(arrowLabel);

        // Hover animation
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered[0] = true;
                if (animTimer[0] != null) animTimer[0].stop();
                animTimer[0] = new Timer(16, ev -> {
                    hoverAnim[0] = Math.min(1f, hoverAnim[0] + 0.12f);
                    card.repaint();
                    if (hoverAnim[0] >= 1f) ((Timer) ev.getSource()).stop();
                });
                animTimer[0].start();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hovered[0] = false;
                if (animTimer[0] != null) animTimer[0].stop();
                animTimer[0] = new Timer(16, ev -> {
                    hoverAnim[0] = Math.max(0f, hoverAnim[0] - 0.08f);
                    card.repaint();
                    if (hoverAnim[0] <= 0f) ((Timer) ev.getSource()).stop();
                });
                animTimer[0].start();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshPanel(target);
                cardLayout.show(contentPanel, target);
            }
        });

        return card;
    }

    // ══════════════════════════════════════════════════════════════════
    //  3. TODAY'S SCHEDULE PANEL
    // ══════════════════════════════════════════════════════════════════
    private JPanel scheduleContent;

    private JPanel buildSchedulePanel() {
        GradientPanel panel = new GradientPanel(BG_DARK, new Color(5, 15, 30));
        panel.setLayout(new BorderLayout());

        // Top bar
        panel.add(buildTopBar("\u23F0  Today's Schedule", ACCENT_CYAN), BorderLayout.NORTH);

        scheduleContent = new JPanel();
        scheduleContent.setOpaque(false);
        scheduleContent.setLayout(new BorderLayout());
        populateScheduleContent();

        JScrollPane scroll = new JScrollPane(scheduleContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void populateScheduleContent() {
        scheduleContent.removeAll();

        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        String dayName = dayOfWeek.name();

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            // ── Weekend message — animated & creative ──
            JPanel weekendPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    // Radial glow
                    RadialGradientPaint rgp = new RadialGradientPaint(
                        w / 2f, h / 2f - 40, Math.max(w, h) * 0.35f,
                        new float[]{0f, 1f},
                        new Color[]{new Color(34, 197, 94, 30), new Color(0, 0, 0, 0)});
                    g2.setPaint(rgp);
                    g2.fillRect(0, 0, w, h);
                    g2.dispose();
                }
            };
            weekendPanel.setOpaque(false);
            weekendPanel.setLayout(new BoxLayout(weekendPanel, BoxLayout.Y_AXIS));
            weekendPanel.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

            JLabel sunEmoji = new JLabel("\uD83C\uDFD6\uFE0F", SwingConstants.CENTER);
            sunEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
            sunEmoji.setAlignmentX(Component.CENTER_ALIGNMENT);
            weekendPanel.add(sunEmoji);
            weekendPanel.add(Box.createVerticalStrut(24));

            JLabel woohoo = new JLabel("Woohoo! It's " + dayName + "!", SwingConstants.CENTER);
            woohoo.setFont(new Font("Segoe UI", Font.BOLD, 32));
            woohoo.setForeground(ACCENT_GREEN);
            woohoo.setAlignmentX(Component.CENTER_ALIGNMENT);
            weekendPanel.add(woohoo);
            weekendPanel.add(Box.createVerticalStrut(14));

            JLabel restMsg = new JLabel("Take rest! You are completely free today.", SwingConstants.CENTER);
            restMsg.setFont(FONT_HEADING);
            restMsg.setForeground(TEXT_PRIMARY);
            restMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
            weekendPanel.add(restMsg);
            weekendPanel.add(Box.createVerticalStrut(10));

            JLabel coffeeMsg = new JLabel("\u2615  No college classes scheduled. Enjoy your weekend and recharge!", SwingConstants.CENTER);
            coffeeMsg.setFont(FONT_BODY);
            coffeeMsg.setForeground(TEXT_SECONDARY);
            coffeeMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
            weekendPanel.add(coffeeMsg);
            weekendPanel.add(Box.createVerticalStrut(20));

            // Fun tips
            JPanel tipCard = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(30, 60, 40, 160));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.dispose();
                }
            };
            tipCard.setOpaque(false);
            tipCard.setLayout(new BoxLayout(tipCard, BoxLayout.Y_AXIS));
            tipCard.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
            tipCard.setMaximumSize(new Dimension(500, 120));
            tipCard.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel tipTitle = new JLabel("\uD83D\uDCA1  Weekend Tips");
            tipTitle.setFont(FONT_BUTTON);
            tipTitle.setForeground(ACCENT_GREEN);
            tipTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            tipCard.add(tipTitle);
            tipCard.add(Box.createVerticalStrut(8));

            String[] tips = {
                "\u2022  Catch up on pending assignments",
                "\u2022  Review notes for subjects below 80%",
                "\u2022  Get some fresh air and exercise!"
            };
            for (String tip : tips) {
                JLabel tipLabel = new JLabel(tip);
                tipLabel.setFont(FONT_BODY);
                tipLabel.setForeground(TEXT_SECONDARY);
                tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                tipCard.add(tipLabel);
            }
            weekendPanel.add(tipCard);

            scheduleContent.add(weekendPanel, BorderLayout.CENTER);
        } else {
            // ── Weekday: show today's schedule table ──
            scheduleManager.loadDay(dayName);
            List<ClassSession> sessions = scheduleManager.getSessionsForDay(dayName);

            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setOpaque(false);
            tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

            JLabel dayLabel = new JLabel("\uD83D\uDCC5  " + dayName + " \u2014 " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            dayLabel.setFont(FONT_HEADING);
            dayLabel.setForeground(TEXT_PRIMARY);
            dayLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
            tablePanel.add(dayLabel, BorderLayout.NORTH);

            String[] columns = {"Period", "Time Slot", "Subject Code", "Subject Name", "Room", "Attendance", "Bunkable?"};
            Object[][] data;

            if (sessions != null && !sessions.isEmpty()) {
                data = new Object[sessions.size()][7];
                for (int i = 0; i < sessions.size(); i++) {
                    ClassSession cs = sessions.get(i);
                    Subject sub = subjectRegistry.get(cs.getSubjectCode());
                    data[i][0] = cs.getPeriodNumber();
                    data[i][1] = cs.getTimeSlot();
                    data[i][2] = cs.getSubjectCode();
                    data[i][3] = cs.getSubjectName();
                    data[i][4] = cs.getClassroom();
                    data[i][5] = sub != null ? String.format("%.1f%%", sub.getAttendancePercentage()) : "N/A";
                    data[i][6] = sub != null ? (sub.isBunkableNextHour() ? "\u2714 YES" : "\u2716 NO") : "N/A";
                }
            } else {
                data = new Object[0][7];
            }

            JTable table = createStyledTable(data, columns);
            // Custom cell renderer for bunkable column
            table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                    if (value != null) {
                        String v = value.toString();
                        if (v.contains("YES")) setForeground(SAFE_GREEN);
                        else if (v.contains("NO")) setForeground(DANGER_RED);
                        else setForeground(TEXT_PRIMARY);
                    }
                    setBackground(sel ? BG_CARD_HOVER : BG_CARD);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    return c;
                }
            });

            JScrollPane tableScroll = new JScrollPane(table);
            tableScroll.setOpaque(false);
            tableScroll.getViewport().setOpaque(false);
            tableScroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 80), 1, true));
            tablePanel.add(tableScroll, BorderLayout.CENTER);

            // Summary bar below table
            JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
            summaryBar.setOpaque(false);
            summaryBar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            int totalClasses = sessions != null ? sessions.size() : 0;
            int bunkableCount = 0;
            if (sessions != null) {
                for (ClassSession cs : sessions) {
                    Subject sub = subjectRegistry.get(cs.getSubjectCode());
                    if (sub != null && sub.isBunkableNextHour()) bunkableCount++;
                }
            }
            JLabel classesSummary = new JLabel("\uD83D\uDCDA  " + totalClasses + " classes today");
            classesSummary.setFont(FONT_BODY);
            classesSummary.setForeground(TEXT_PRIMARY);
            summaryBar.add(classesSummary);

            JLabel bunkSummary = new JLabel("\u2714  " + bunkableCount + " bunkable");
            bunkSummary.setFont(FONT_BODY);
            bunkSummary.setForeground(bunkableCount > 0 ? SAFE_GREEN : DANGER_RED);
            summaryBar.add(bunkSummary);

            tablePanel.add(summaryBar, BorderLayout.SOUTH);

            scheduleContent.add(tablePanel, BorderLayout.CENTER);
        }

        scheduleContent.revalidate();
        scheduleContent.repaint();
    }

    // ══════════════════════════════════════════════════════════════════
    //  4. ATTENDANCE STATUS PANEL — with progress bars
    // ══════════════════════════════════════════════════════════════════
    private JPanel attendanceContent;

    private JPanel buildAttendancePanel() {
        GradientPanel panel = new GradientPanel(BG_DARK, new Color(5, 20, 15));
        panel.setLayout(new BorderLayout());
        panel.add(buildTopBar("\uD83D\uDCCA  My Attendance Status", ACCENT_GREEN), BorderLayout.NORTH);

        attendanceContent = new JPanel(new BorderLayout());
        attendanceContent.setOpaque(false);
        populateAttendanceContent();

        JScrollPane scroll = new JScrollPane(attendanceContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void populateAttendanceContent() {
        attendanceContent.removeAll();
        attendanceContent.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Summary cards row
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 16, 0));
        summaryRow.setOpaque(false);
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        int safeCount = 0, edgeCount = 0, critCount = 0;
        for (Subject s : subjectRegistry.values()) {
            switch (s.getStatus()) {
                case SAFE: safeCount++; break;
                case ON_THE_EDGE: edgeCount++; break;
                case CRITICAL: critCount++; break;
            }
        }
        summaryRow.add(createStatCard("\u2705 Safe", String.valueOf(safeCount), SAFE_GREEN));
        summaryRow.add(createStatCard("\u26A0\uFE0F On Edge", String.valueOf(edgeCount), EDGE_YELLOW));
        summaryRow.add(createStatCard("\uD83D\uDEA8 Critical", String.valueOf(critCount), DANGER_RED));

        attendanceContent.add(summaryRow, BorderLayout.NORTH);

        // Table
        String[] columns = {"Code", "Subject Name", "Attended", "Percent", "Status", "Bunks Left", "Needed", "Bunkable?"};
        java.util.Collection<Subject> subjects = subjectRegistry.values();
        Object[][] data = new Object[subjects.size()][8];
        int i = 0;
        for (Subject s : subjects) {
            data[i][0] = s.getSubjectCode();
            data[i][1] = s.getSubjectName();
            data[i][2] = s.getAttendedClasses() + " / " + s.getTotalClasses();
            data[i][3] = String.format("%.2f%%", s.getAttendancePercentage());
            data[i][4] = s.getStatus().name();
            data[i][5] = s.getSafeBunks();
            data[i][6] = s.getClassesNeededTo75();
            data[i][7] = s.isBunkableNextHour() ? "\u2714 YES" : "\u2716 NO";
            i++;
        }

        JTable table = createStyledTable(data, columns);

        // Colour the Status column
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                if (value != null) {
                    String v = value.toString();
                    if (v.equals("SAFE")) { setForeground(SAFE_GREEN); setText("\u2705 SAFE"); }
                    else if (v.equals("ON_THE_EDGE")) { setForeground(EDGE_YELLOW); setText("\u26A0 EDGE"); }
                    else if (v.equals("CRITICAL")) { setForeground(DANGER_RED); setText("\uD83D\uDEA8 CRIT"); }
                    else setForeground(TEXT_PRIMARY);
                }
                setBackground(sel ? BG_CARD_HOVER : BG_CARD);
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
        // Colour the Bunkable column
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                if (value != null) {
                    String v = value.toString();
                    if (v.contains("YES")) setForeground(SAFE_GREEN);
                    else if (v.contains("NO")) setForeground(DANGER_RED);
                    else setForeground(TEXT_PRIMARY);
                }
                setBackground(sel ? BG_CARD_HOVER : BG_CARD);
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
        // Colour the Percent column based on value
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                if (value != null) {
                    try {
                        double pct = Double.parseDouble(value.toString().replace("%", ""));
                        if (pct >= 80) setForeground(SAFE_GREEN);
                        else if (pct >= 75) setForeground(EDGE_YELLOW);
                        else setForeground(DANGER_RED);
                    } catch (NumberFormatException ex) {
                        setForeground(TEXT_PRIMARY);
                    }
                }
                setBackground(sel ? BG_CARD_HOVER : BG_CARD);
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        attendanceContent.add(tableScroll, BorderLayout.CENTER);

        attendanceContent.revalidate();
        attendanceContent.repaint();
    }

    // ══════════════════════════════════════════════════════════════════
    //  5. ACADEMIC CALENDAR PANEL
    // ══════════════════════════════════════════════════════════════════
    private JPanel calendarContent;

    private JPanel buildCalendarPanel() {
        GradientPanel panel = new GradientPanel(BG_DARK, new Color(20, 15, 5));
        panel.setLayout(new BorderLayout());
        panel.add(buildTopBar("\uD83D\uDCC5  Academic Calendar", ACCENT_AMBER), BorderLayout.NORTH);

        calendarContent = new JPanel();
        calendarContent.setOpaque(false);
        calendarContent.setLayout(new BoxLayout(calendarContent, BoxLayout.Y_AXIS));
        populateCalendarContent();

        JScrollPane scroll = new JScrollPane(calendarContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void populateCalendarContent() {
        calendarContent.removeAll();
        calendarContent.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        LocalDate today = LocalDate.now();
        int workingDaysLeft = calendarService.countWorkingDaysRemaining(calendarManager, today);
        int workingDaysElapsed = calendarService.countWorkingDaysElapsed(calendarManager, today);
        int totalWorkingDays = calendarService.getTotalInstructionalWorkingDays();
        double pctComplete = totalWorkingDays > 0 ? ((double) workingDaysElapsed / totalWorkingDays) * 100.0 : 0;

        // Stat cards
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.add(createStatCard("Days Elapsed", String.valueOf(workingDaysElapsed), ACCENT_GREEN));
        statsRow.add(createStatCard("Days Left", String.valueOf(workingDaysLeft), ACCENT_CYAN));
        statsRow.add(createStatCard("Total Working", String.valueOf(totalWorkingDays), ACCENT_PURPLE));
        statsRow.add(createStatCard("% Complete", String.format("%.1f%%", pctComplete), ACCENT_AMBER));
        calendarContent.add(statsRow);
        calendarContent.add(Box.createVerticalStrut(20));

        // Semester progress bar
        JPanel progressWrapper = new JPanel(new BorderLayout());
        progressWrapper.setOpaque(false);
        progressWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        progressWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel progressTitle = new JLabel("Semester Progress");
        progressTitle.setFont(FONT_SMALL);
        progressTitle.setForeground(TEXT_SECONDARY);
        progressWrapper.add(progressTitle, BorderLayout.NORTH);

        JPanel progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                // Background track
                g2.setColor(new Color(40, 40, 65));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
                // Fill
                double pct2 = Math.min(pctComplete, 100.0) / 100.0;
                int fillWidth = (int) (w * pct2);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_PURPLE, fillWidth, 0, ACCENT_CYAN);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, fillWidth, h, 12, 12));
                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_SMALL);
                String txt = String.format("%.1f%%", pctComplete);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(txt, (w - fm.stringWidth(txt)) / 2, (h + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        progressBar.setPreferredSize(new Dimension(0, 24));
        progressBar.setOpaque(false);
        progressWrapper.add(progressBar, BorderLayout.CENTER);
        calendarContent.add(progressWrapper);
        calendarContent.add(Box.createVerticalStrut(20));

        // Milestones
        JLabel milestoneTitle = new JLabel("\uD83C\uDFAF  Key Semester Milestones");
        milestoneTitle.setFont(FONT_HEADING);
        milestoneTitle.setForeground(TEXT_PRIMARY);
        milestoneTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        calendarContent.add(milestoneTitle);
        calendarContent.add(Box.createVerticalStrut(10));

        LocalDate lastInst = calendarService.getLastInstructionalDay();
        LocalDate condonation = calendarService.getCondonationDeadlineNoFine();
        LocalDate examDate = calendarService.getFirstExamDate();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy (EEE)");

        addMilestoneRow("\uD83C\uDFC1  Last Instructional Day", lastInst != null ? lastInst.format(fmt) : "N/A",
                lastInst != null ? calendarManager.countDaysUntilExam(lastInst, today) + " days" : "", ACCENT_GREEN);
        addMilestoneRow("\u26A0\uFE0F  Condonation Deadline", condonation != null ? condonation.format(fmt) : "N/A",
                condonation != null ? calendarManager.countDaysUntilExam(condonation, today) + " days" : "", ACCENT_AMBER);
        addMilestoneRow("\uD83D\uDCDA  End Semester Exam", examDate != null ? examDate.format(fmt) : "N/A",
                examDate != null ? calendarManager.countDaysUntilExam(examDate, today) + " days" : "", ACCENT_PINK);

        calendarContent.add(Box.createVerticalStrut(20));

        // Projection table
        JLabel projTitle = new JLabel("\uD83D\uDCC8  Semester-End Attendance Projections");
        projTitle.setFont(FONT_HEADING);
        projTitle.setForeground(TEXT_PRIMARY);
        projTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        calendarContent.add(projTitle);
        calendarContent.add(Box.createVerticalStrut(10));

        String[] cols = {"Code", "Subject", "Attended", "Current %", "Classes Rem.", "Max %", "Must Attend", "Bunk Budget"};
        java.util.Collection<Subject> subjects = subjectRegistry.values();
        Object[][] data = new Object[subjects.size()][8];
        int i = 0;
        for (Subject s : subjects) {
            SemesterProjection proj = SemesterAttendanceProjector.project(s, today, calendarManager, calendarService, scheduleManager);
            data[i][0] = s.getSubjectCode();
            data[i][1] = s.getSubjectName().length() > 28 ? s.getSubjectName().substring(0, 25) + "..." : s.getSubjectName();
            data[i][2] = s.getAttendedClasses() + " / " + s.getTotalClasses();
            data[i][3] = String.format("%.1f%%", s.getAttendancePercentage());
            data[i][4] = proj.getRemainingClasses();
            data[i][5] = String.format("%.1f%%", proj.getMaxPossiblePercentage());
            data[i][6] = proj.getMustAttendToPass();
            data[i][7] = proj.getSemesterBunksRemaining() > 0 ? proj.getSemesterBunksRemaining() + " bunks" : "0 bunks";
            i++;
        }

        JTable table = createStyledTable(data, cols);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setBorder(null);
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Set a preferred size to show properly inside BoxLayout
        tableScroll.setPreferredSize(new Dimension(800, 200));
        tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        calendarContent.add(tableScroll);

        calendarContent.revalidate();
        calendarContent.repaint();
    }

    private void addMilestoneRow(String label, String date, String countdown, Color accent) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 50, 120));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                // Left accent bar
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 3, getHeight(), 3, 3));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        JLabel left = new JLabel(label + "   " + date);
        left.setFont(FONT_BODY);
        left.setForeground(TEXT_PRIMARY);
        row.add(left, BorderLayout.WEST);

        JLabel right = new JLabel(countdown);
        right.setFont(FONT_BUTTON);
        right.setForeground(accent);
        row.add(right, BorderLayout.EAST);

        calendarContent.add(row);
        calendarContent.add(Box.createVerticalStrut(6));
    }

    // ══════════════════════════════════════════════════════════════════
    //  6. BUNKABILITY CHECKER PANEL
    // ══════════════════════════════════════════════════════════════════
    private JPanel bunkContent;

    private JPanel buildBunkabilityPanel() {
        GradientPanel panel = new GradientPanel(BG_DARK, new Color(25, 5, 15));
        panel.setLayout(new BorderLayout());
        panel.add(buildTopBar("\uD83C\uDFAF  Bunkability Checker", ACCENT_PINK), BorderLayout.NORTH);

        bunkContent = new JPanel(new BorderLayout());
        bunkContent.setOpaque(false);
        populateBunkContent();

        JScrollPane scroll = new JScrollPane(bunkContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void populateBunkContent() {
        bunkContent.removeAll();
        bunkContent.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Subject selector
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        selectorPanel.setOpaque(false);
        selectorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel selectLabel = new JLabel("\uD83D\uDCDD  Choose Subject:");
        selectLabel.setFont(FONT_BODY);
        selectLabel.setForeground(TEXT_PRIMARY);
        selectorPanel.add(selectLabel);

        // Build combo box items
        java.util.Vector<String> items = new java.util.Vector<>();
        items.add("ALL SUBJECTS");
        for (Subject s : subjectRegistry.values()) {
            items.add(s.getSubjectCode() + " \u2014 " + s.getSubjectName());
        }
        JComboBox<String> subjectCombo = new JComboBox<>(items);
        subjectCombo.setFont(FONT_BODY);
        subjectCombo.setBackground(BG_CARD);
        subjectCombo.setForeground(TEXT_PRIMARY);
        subjectCombo.setPreferredSize(new Dimension(380, 36));
        ((JComponent) subjectCombo.getRenderer()).setOpaque(true);
        subjectCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean foc) {
                Component c = super.getListCellRendererComponent(list, value, index, sel, foc);
                c.setBackground(sel ? BG_CARD_HOVER : BG_CARD);
                c.setForeground(TEXT_PRIMARY);
                return c;
            }
        });
        selectorPanel.add(subjectCombo);

        JButton checkBtn = createGradientButton("  \uD83D\uDD0D  Check  ", ACCENT_PINK, ACCENT_PURPLE);
        checkBtn.setPreferredSize(new Dimension(140, 36));
        selectorPanel.add(checkBtn);

        bunkContent.add(selectorPanel, BorderLayout.NORTH);

        // Results area
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setOpaque(false);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Default: show all subjects overview table
        JPanel allTable = buildAllSubjectsBunkTable();
        resultsPanel.add(allTable, BorderLayout.CENTER);

        bunkContent.add(resultsPanel, BorderLayout.CENTER);

        // Button action
        checkBtn.addActionListener(e -> {
            resultsPanel.removeAll();
            String selected = (String) subjectCombo.getSelectedItem();
            if (selected == null || selected.equals("ALL SUBJECTS")) {
                resultsPanel.add(buildAllSubjectsBunkTable(), BorderLayout.CENTER);
            } else {
                String code = selected.split("\u2014")[0].trim();
                Subject sub = subjectRegistry.get(code);
                if (sub != null) {
                    resultsPanel.add(buildSingleSubjectBunkDetail(sub), BorderLayout.CENTER);
                }
            }
            resultsPanel.revalidate();
            resultsPanel.repaint();
        });

        bunkContent.revalidate();
        bunkContent.repaint();
    }

    private JPanel buildAllSubjectsBunkTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] cols = {"Code", "Subject Name", "Attended", "Current %", "If Bunk 1hr", "% Drop", "Safe Bunks", "Bunkable?"};
        java.util.Collection<Subject> subjects = subjectRegistry.values();
        Object[][] data = new Object[subjects.size()][8];
        int i = 0;
        for (Subject s : subjects) {
            double cur = s.getAttendancePercentage();
            double ifBunk = s.getProjectedPercentageIfBunk();
            double drop = cur - ifBunk;
            data[i][0] = s.getSubjectCode();
            data[i][1] = s.getSubjectName().length() > 28 ? s.getSubjectName().substring(0, 25) + "..." : s.getSubjectName();
            data[i][2] = s.getAttendedClasses() + " / " + s.getTotalClasses();
            data[i][3] = String.format("%.2f%%", cur);
            data[i][4] = String.format("%.2f%%", ifBunk);
            data[i][5] = String.format("-%.2f%%", drop);
            data[i][6] = s.getSafeBunks();
            data[i][7] = s.isBunkableNextHour() ? "\u2714 YES" : "\u2716 NO";
            i++;
        }

        JTable table = createStyledTable(data, cols);

        // Colour drop column red
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setForeground(DANGER_RED);
                setBackground(sel ? BG_CARD_HOVER : BG_CARD);
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
        // Bunkable column
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                if (value != null) {
                    if (value.toString().contains("YES")) setForeground(SAFE_GREEN);
                    else setForeground(DANGER_RED);
                }
                setBackground(sel ? BG_CARD_HOVER : BG_CARD);
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSingleSubjectBunkDetail(Subject s) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        double cur  = s.getAttendancePercentage();
        double bunk = s.getProjectedPercentageIfBunk();
        double att  = s.getProjectedPercentageIfAttend();
        double drop = cur - bunk;
        int safeBunks = s.getSafeBunks();
        int needed = s.getClassesNeededTo75();
        boolean safe = s.isBunkableNextHour();

        // Subject info card
        JPanel infoCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                // Top accent
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_PINK, getWidth(), 0, ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 4, 8, 8));
                g2.dispose();
            }
        };
        infoCard.setOpaque(false);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(BorderFactory.createEmptyBorder(28, 28, 24, 28));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 550));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        addDetailRow(infoCard, "\uD83D\uDCD8  Subject", s.getSubjectCode() + " \u2014 " + s.getSubjectName(), ACCENT_AMBER);
        addDetailRow(infoCard, "\uD83D\uDCCA  Classes Attended", s.getAttendedClasses() + " / " + s.getTotalClasses() + " conducted", TEXT_PRIMARY);

        // Attendance progress bar
        JPanel progRow = new JPanel(new BorderLayout());
        progRow.setOpaque(false);
        progRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        progRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        progRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel progLabel = new JLabel("\uD83D\uDCC8  Current Attendance");
        progLabel.setFont(FONT_BODY);
        progLabel.setForeground(TEXT_SECONDARY);
        progLabel.setPreferredSize(new Dimension(220, 24));
        progRow.add(progLabel, BorderLayout.WEST);

        JPanel attBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(40, 40, 65));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));
                double pct = Math.min(cur, 100.0) / 100.0;
                int fillW = (int) (w * pct);
                Color barColor = cur >= 80 ? SAFE_GREEN : cur >= 75 ? EDGE_YELLOW : DANGER_RED;
                g2.setColor(barColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, 10, 10));
                // 75% threshold marker
                int threshX = (int) (w * 0.75);
                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{4, 4}, 0));
                g2.drawLine(threshX, 0, threshX, h);
                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_SMALL);
                String txt = String.format("%.2f%% [%s]", cur, s.getStatus().name());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(txt, (w - fm.stringWidth(txt)) / 2, (h + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        attBar.setPreferredSize(new Dimension(0, 22));
        attBar.setOpaque(false);
        progRow.add(attBar, BorderLayout.CENTER);
        infoCard.add(progRow);

        // Separator
        infoCard.add(Box.createVerticalStrut(14));
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 90));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        infoCard.add(sep);
        infoCard.add(Box.createVerticalStrut(14));

        // Bunk impact
        JLabel impactTitle = new JLabel("\uD83D\uDCA5  1-HOUR BUNK IMPACT");
        impactTitle.setFont(FONT_BUTTON);
        impactTitle.setForeground(ACCENT_PINK);
        impactTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCard.add(impactTitle);
        infoCard.add(Box.createVerticalStrut(8));

        addDetailRow(infoCard, "If you bunk 1 hour",
                String.format("%.2f%%  (attended %d / %d)", bunk, s.getAttendedClasses(), s.getTotalClasses() + 1), DANGER_RED);
        addDetailRow(infoCard, "Percentage Drop", String.format("-%.2f%%", drop), DANGER_RED);
        addDetailRow(infoCard, "If you attend 1 hour",
                String.format("%.2f%%  (attended %d / %d,  +%.2f%% gain)", att, s.getAttendedClasses() + 1, s.getTotalClasses() + 1, att - cur), SAFE_GREEN);

        infoCard.add(Box.createVerticalStrut(14));
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(60, 60, 90));
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        infoCard.add(sep2);
        infoCard.add(Box.createVerticalStrut(14));

        // Safety
        addDetailRow(infoCard, "\uD83D\uDEE1\uFE0F  Safe Bunks Left", safeBunks + " class(es) before falling below 75%", SAFE_GREEN);
        if (cur < 75.0) {
            addDetailRow(infoCard, "\uD83D\uDEA8  Recovery Needed", "Must attend " + needed + " class(es) straight to reach 75%!", DANGER_RED);
        }

        infoCard.add(Box.createVerticalStrut(18));

        // Verdict banner
        JPanel verdictPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgStart = safe ? new Color(22, 101, 52) : new Color(127, 29, 29);
                Color bgEnd = safe ? new Color(16, 130, 70) : new Color(160, 30, 30);
                GradientPaint gp = new GradientPaint(0, 0, bgStart, getWidth(), 0, bgEnd);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        verdictPanel.setOpaque(false);
        verdictPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        verdictPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        verdictPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        verdictPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel verdictLabel = new JLabel(safe
                ? String.format("\uD83D\uDEA6  VERDICT:  \u2714 SAFE TO BUNK!  (Attendance stays at %.2f%% \u2265 75%%)", bunk)
                : String.format("\uD83D\uDEA6  VERDICT:  \u2716 DO NOT BUNK!  (Attendance drops to %.2f%% < 75%%)", bunk));
        verdictLabel.setFont(FONT_BUTTON);
        verdictLabel.setForeground(Color.WHITE);
        verdictPanel.add(verdictLabel);

        infoCard.add(verdictPanel);

        panel.add(infoCard);
        return panel;
    }

    private void addDetailRow(JPanel parent, String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(220, 24));
        row.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel(value);
        val.setFont(FONT_BODY);
        val.setForeground(valueColor);
        row.add(val, BorderLayout.CENTER);

        parent.add(row);
    }

    // ══════════════════════════════════════════════════════════════════
    //  SHARED UI UTILITIES
    // ══════════════════════════════════════════════════════════════════

    /** Top bar with back button and title */
    private JPanel buildTopBar(String title, Color accent) {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 38), getWidth(), 0, new Color(25, 25, 45));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, accent),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JButton backBtn = new JButton("\u2190 Back to Menu");
        backBtn.setFont(FONT_BODY);
        backBtn.setForeground(TEXT_PRIMARY);
        backBtn.setBackground(new Color(50, 50, 75));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 100), 1, true),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { backBtn.setBackground(new Color(70, 70, 100)); }
            @Override public void mouseExited(MouseEvent e)  { backBtn.setBackground(new Color(50, 50, 75)); }
        });
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "MENU"));
        bar.add(backBtn, BorderLayout.WEST);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADING);
        titleLabel.setForeground(accent);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bar.add(titleLabel, BorderLayout.CENTER);

        // Spacer to balance
        JLabel spacer = new JLabel();
        spacer.setPreferredSize(new Dimension(160, 1));
        bar.add(spacer, BorderLayout.EAST);

        return bar;
    }

    /** Creates a styled dark-themed JTable */
    private JTable createStyledTable(Object[][] data, String[] columns) {
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(FONT_MONO);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_CARD);
        table.setGridColor(new Color(50, 50, 75));
        table.setRowHeight(34);
        table.setSelectionBackground(BG_CARD_HOVER);
        table.setSelectionForeground(ACCENT_CYAN);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BUTTON);
        header.setBackground(new Color(35, 35, 60));
        header.setForeground(ACCENT_CYAN);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_PURPLE));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // Default cell alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        return table;
    }

    /** Small stat card for summary rows */
    private JPanel createStatCard(String label, String value, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                // Accent left bar
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                // Subtle top glow
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 30, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(FONT_TITLE);
        valLabel.setForeground(accent);
        valLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valLabel);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(FONT_SMALL);
        lblLabel.setForeground(TEXT_SECONDARY);
        lblLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblLabel);

        return card;
    }

    /** Styled text field */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT_BODY);
        field.setBackground(new Color(40, 40, 65));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_CYAN);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 110), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(300, 38));
        // Focus glow effect
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_CYAN, 2, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 70, 110), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return field;
    }

    /** Styled password field */
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(FONT_BODY);
        field.setBackground(new Color(40, 40, 65));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_CYAN);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 110), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(300, 38));
        // Focus glow effect
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_CYAN, 2, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 70, 110), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return field;
    }

    /** Gradient-style button with hover brighten effect */
    private JButton createGradientButton(String text, Color c1, Color c2) {
        final float[] hoverBrightness = {0f};
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color col1 = brighten(c1, hoverBrightness[0]);
                Color col2 = brighten(c2, hoverBrightness[0]);
                GradientPaint gp = new GradientPaint(0, 0, col1, getWidth(), 0, col2);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(280, 40));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hoverBrightness[0] = 0.15f; btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hoverBrightness[0] = 0f;    btn.repaint(); }
        });
        return btn;
    }

    /** Brighten a colour by a fraction */
    private static Color brighten(Color c, float fraction) {
        int r = Math.min(255, (int) (c.getRed()   + 255 * fraction));
        int g = Math.min(255, (int) (c.getGreen() + 255 * fraction));
        int b = Math.min(255, (int) (c.getBlue()  + 255 * fraction));
        return new Color(r, g, b, c.getAlpha());
    }

    // ── Gradient background panel ────────────────────────────────────
    private static class GradientPanel extends JPanel {
        private final Color top, bottom;

        GradientPanel(Color top, Color bottom) {
            this.top = top;
            this.bottom = bottom;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, top, 0, getHeight(), bottom);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Panel refresh helpers ────────────────────────────────────────
    private void refreshPanel(String name) {
        switch (name) {
            case "SCHEDULE":    populateScheduleContent();   break;
            case "ATTENDANCE":  populateAttendanceContent(); break;
            case "CALENDAR":    populateCalendarContent();   break;
            case "BUNKABILITY": populateBunkContent();       break;
        }
    }

    private void refreshAllPanels() {
        populateScheduleContent();
        populateAttendanceContent();
        populateCalendarContent();
        populateBunkContent();
    }

    // ══════════════════════════════════════════════════════════════════
    //  MAIN ENTRY POINT
    // ══════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        // Set look-and-feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Override defaults for dark theme compatibility
            UIManager.put("Panel.background", BG_DARK);
            UIManager.put("OptionPane.background", BG_CARD);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(CanIBunkGUI::new);
    }
}
