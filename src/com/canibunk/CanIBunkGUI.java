package com.canibunk;

import com.canibunk.ds.CustomHashMap;
import com.canibunk.model.*;
import com.canibunk.service.*;
import com.canibunk.scraper.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * BUNKiT - Professional & Clean Swing Desktop Application
 * Built using javax.swing and java.awt for the academic project.
 * Features an executive dark theme, unified design language, clean KPI dashboards,
 * custom pill badges, and refined typography.
 */
public class CanIBunkGUI extends JFrame {

    // ── Modern Professional Color Palette (Slate / Indigo Dark Theme) ─
    private static final Color BG_MAIN           = new Color(13, 16, 23);       // #0D1017 deep dark base
    private static final Color BG_SURFACE        = new Color(21, 26, 38);       // #151A26 elevated surface
    private static final Color BG_SURFACE_HOVER  = new Color(28, 35, 51);       // #1C2333 hovered surface
    private static final Color BG_SURFACE_SUBTLE = new Color(17, 21, 31);       // #11151F input/row alt
    private static final Color BORDER_SUBTLE     = new Color(36, 43, 61);       // #242B3D fine border
    private static final Color BORDER_HOVER      = new Color(59, 70, 99);       // #3B4663 border highlight
    private static final Color ACCENT_PRIMARY    = new Color(99, 102, 241);     // #6366F1 Indigo-500
    private static final Color ACCENT_PRIMARY_HOVER = new Color(129, 140, 248);// #818CF8 Indigo-400
    private static final Color ACCENT_CYAN       = new Color(56, 189, 248);     // #38BDF8 Sky-400
    private static final Color ACCENT_EMERALD    = new Color(34, 197, 94);      // #22C55E Green-500
    private static final Color ACCENT_AMBER      = new Color(245, 158, 11);     // #F59E0B Amber-500
    private static final Color ACCENT_ROSE       = new Color(239, 68, 68);      // #EF4444 Rose-500
    private static final Color ACCENT_PURPLE     = new Color(168, 85, 247);     // #A855F7 Purple-500

    private static final Color TEXT_PRIMARY      = new Color(241, 245, 249);    // #F1F5F9 Slate-100
    private static final Color TEXT_SECONDARY    = new Color(148, 163, 184);    // #94A3B8 Slate-400
    private static final Color TEXT_MUTED        = new Color(100, 116, 139);    // #64748B Slate-500

    private static final Color SAFE_GREEN        = ACCENT_EMERALD;
    private static final Color DANGER_RED        = ACCENT_ROSE;
    private static final Color EDGE_YELLOW       = ACCENT_AMBER;

    // ── Typography ──────────────────────────────────────────────────
    private static final Font FONT_DISPLAY       = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_TITLE         = new Font("Segoe UI", Font.BOLD, 17);
    private static final Font FONT_SUBHEAD       = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private static final Font FONT_BODY          = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BODY_SEMIBOLD = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private static final Font FONT_BUTTON        = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private static final Font FONT_SMALL         = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BADGE         = new Font("Segoe UI Semibold", Font.BOLD, 11);
    private static final Font FONT_MONO          = new Font("Consolas", Font.PLAIN, 13);

    // ── Backend services ─────────────────────────────────────────────
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
        super("BUNKiT — Attendance Monitor & Bunk Predictor");
        initSampleData();
        initUI();
    }

    // ══════════════════════════════════════════════════════════════════
    //  DATA INITIALIZATION (mirrors Main.initSampleData)
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
        tt.addSession("THURSDAY",  new ClassSession(2, "10:00 - 11:00", "CS203", "Data Structures & Algorithms", "LH-101"));
        tt.addSession("THURSDAY",  new ClassSession(3, "11:15 - 12:15", "EST200", "Design & Engineering",        "LH-104"));
        tt.addSession("THURSDAY",  new ClassSession(4, "01:15 - 02:15", "CS205", "Java Programming",             "CC-Lab 2"));
        tt.addSession("THURSDAY",  new ClassSession(5, "02:15 - 03:15", "CS201", "Discrete Mathematics",         "LH-101"));
        tt.addSession("FRIDAY",    new ClassSession(1, "09:00 - 10:00", "CS201", "Discrete Mathematics",         "LH-101"));
        tt.addSession("FRIDAY",    new ClassSession(2, "10:00 - 11:00", "EST200", "Design & Engineering",        "LH-104"));
        tt.addSession("FRIDAY",    new ClassSession(3, "11:15 - 01:15", "CSL201", "Data Structures Lab",         "Lab 3"));
        tt.addSession("FRIDAY",    new ClassSession(4, "02:15 - 03:15", "CS203", "Data Structures & Algorithms", "LH-101"));
        return tt;
    }

    // ══════════════════════════════════════════════════════════════════
    //  UI INITIALISATION
    // ══════════════════════════════════════════════════════════════════
    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 740);
        setMinimumSize(new Dimension(960, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);
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
    //  1. LOGIN PANEL — Clean, Modern Authentication Card
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildLoginPanel() {
        final float[] pulsePhase = {0f};

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Subtle deep background fill
                g2.setColor(BG_MAIN);
                g2.fillRect(0, 0, w, h);

                // Very subtle top-center ambient glow
                float pulse = (float) (0.05 + 0.03 * Math.sin(pulsePhase[0]));
                RadialGradientPaint rgp = new RadialGradientPaint(
                    w / 2f, h * 0.32f, Math.max(w, h) * 0.5f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(99, 102, 241, (int)(pulse * 255)), new Color(0, 0, 0, 0)});
                g2.setPaint(rgp);
                g2.fillRect(0, 0, w, h);

                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 16, 10, 16);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Clean Authentication Card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Card background
                g2.setColor(BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

                // 1px subtle border
                g2.setColor(BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 16, 16));

                // Top accent indicator line
                GradientPaint topGlow = new GradientPaint(0, 0, ACCENT_PRIMARY, w, 0, ACCENT_CYAN);
                g2.setPaint(topGlow);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, 3, 4, 4));

                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(420, 500));
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 28, 6, 28);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;

        // ── Brand Logo Icon & Badge ──
        JPanel logoWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoWrap.setOpaque(false);
        JPanel logoBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_PRIMARY, getWidth(), getHeight(), new Color(79, 70, 229));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String s = "B";
                g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logoBadge.setPreferredSize(new Dimension(44, 44));
        logoBadge.setOpaque(false);
        logoWrap.add(logoBadge);
        c.insets = new Insets(26, 28, 8, 28);
        card.add(logoWrap, c);

        // ── Title ──
        JLabel title = new JLabel("BUNKiT", SwingConstants.CENTER);
        title.setFont(FONT_DISPLAY);
        title.setForeground(TEXT_PRIMARY);
        c.insets = new Insets(0, 28, 2, 28);
        card.add(title, c);

        // ── Subtitle ──
        JLabel subtitle = new JLabel("Intelligent College Attendance Monitor", SwingConstants.CENTER);
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(TEXT_SECONDARY);
        c.insets = new Insets(0, 28, 20, 28);
        card.add(subtitle, c);

        // ── Username field ──
        JLabel userLabel = new JLabel("Admission Number / Username");
        userLabel.setFont(FONT_SMALL);
        userLabel.setForeground(TEXT_SECONDARY);
        c.insets = new Insets(4, 28, 3, 28);
        card.add(userLabel, c);

        JTextField userField = createStyledTextField("e.g. 12345");
        c.insets = new Insets(0, 28, 12, 28);
        card.add(userField, c);

        // ── Password field with show/hide toggle ──
        JLabel passLabel = new JLabel("Portal Password");
        passLabel.setFont(FONT_SMALL);
        passLabel.setForeground(TEXT_SECONDARY);
        c.insets = new Insets(2, 28, 3, 28);
        card.add(passLabel, c);

        JPanel passPanel = new JPanel(new BorderLayout(6, 0));
        passPanel.setOpaque(false);
        JPasswordField passField = createStyledPasswordField();
        passPanel.add(passField, BorderLayout.CENTER);

        JButton togglePass = new JButton("Show");
        togglePass.setFont(FONT_BADGE);
        togglePass.setPreferredSize(new Dimension(54, 38));
        togglePass.setBackground(BG_SURFACE_SUBTLE);
        togglePass.setForeground(TEXT_SECONDARY);
        togglePass.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true));
        togglePass.setFocusPainted(false);
        togglePass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final boolean[] passVisible = {false};
        togglePass.addActionListener(e -> {
            passVisible[0] = !passVisible[0];
            passField.setEchoChar(passVisible[0] ? (char) 0 : '\u2022');
            togglePass.setText(passVisible[0] ? "Hide" : "Show");
        });
        passPanel.add(togglePass, BorderLayout.EAST);
        c.insets = new Insets(0, 28, 16, 28);
        card.add(passPanel, c);

        // ── Primary Button: Log In & Sync ──
        JButton loginBtn = createPrimaryButton("Sign In & Sync Portal");
        c.insets = new Insets(4, 28, 8, 28);
        card.add(loginBtn, c);

        // ── Secondary Button: Use Sample Data ──
        JButton skipBtn = createOutlineButton("Continue with Sample Data");
        c.insets = new Insets(0, 28, 12, 28);
        card.add(skipBtn, c);

        // ── Status label ──
        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(TEXT_MUTED);
        c.insets = new Insets(0, 28, 18, 28);
        card.add(statusLabel, c);

        // ── Actions ──
        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            char[] password = passField.getPassword();
            if (username.isEmpty() || password.length == 0) {
                statusLabel.setText("Please enter both admission number and password.");
                statusLabel.setForeground(ACCENT_AMBER);
                return;
            }
            statusLabel.setText("Connecting to AES student portal...");
            statusLabel.setForeground(ACCENT_CYAN);
            loginBtn.setEnabled(false);
            skipBtn.setEnabled(false);

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
                            statusLabel.setText("Synced successfully! Redirecting...");
                            statusLabel.setForeground(ACCENT_EMERALD);
                        } else {
                            statusLabel.setText("Could not sync live. Proceeding with cached data.");
                            statusLabel.setForeground(ACCENT_AMBER);
                            loadCachedDataIfAvailable();
                        }
                    } catch (Exception ex) {
                        statusLabel.setText("Connection failed. Loaded offline sample data.");
                        statusLabel.setForeground(ACCENT_ROSE);
                    }
                    java.util.Arrays.fill(password, '\0');
                    Timer t = new Timer(800, ev -> {
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

        passField.addActionListener(e -> loginBtn.doClick());

        panel.add(card, gbc);

        // Gentle pulse timer for ambient lighting
        loginAnimTimer = new Timer(40, e -> {
            pulsePhase[0] += 0.04f;
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
    //  2. MAIN MENU / DASHBOARD PANEL
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildMainMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);

        // ── Top Navigation Bar ──
        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setOpaque(false);
        topNav.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(18, 36, 18, 36)
        ));

        // Brand section
        JPanel brandSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandSection.setOpaque(false);

        JLabel brandIcon = new JLabel("●");
        brandIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brandIcon.setForeground(ACCENT_PRIMARY);
        brandSection.add(brandIcon);

        JLabel brandLabel = new JLabel("BUNKiT");
        brandLabel.setFont(FONT_TITLE);
        brandLabel.setForeground(TEXT_PRIMARY);
        brandSection.add(brandLabel);

        // Status badge
        JLabel badge = createPillBadge("Semester 3 • 75% Rule", new Color(99, 102, 241, 35), ACCENT_PRIMARY);
        brandSection.add(badge);
        topNav.add(brandSection, BorderLayout.WEST);

        // Right details: Date & Status
        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightSection.setOpaque(false);

        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")));
        dateLabel.setFont(FONT_SMALL);
        dateLabel.setForeground(TEXT_SECONDARY);
        rightSection.add(dateLabel);

        JLabel syncStatus = createPillBadge("● AES Active", new Color(34, 197, 94, 30), ACCENT_EMERALD);
        rightSection.add(syncStatus);

        topNav.add(rightSection, BorderLayout.EAST);
        panel.add(topNav, BorderLayout.NORTH);

        // ── Center Content: KPI Summary + Module Grid ──
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(24, 36, 20, 36));

        // ── Quick KPI Strip ──
        JPanel kpiStrip = buildDashboardKpiStrip();
        centerPanel.add(kpiStrip);
        centerPanel.add(Box.createVerticalStrut(24));

        // Section Title
        JLabel sectionTitle = new JLabel("Workspace Modules");
        sectionTitle.setFont(FONT_SUBHEAD);
        sectionTitle.setForeground(TEXT_SECONDARY);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(sectionTitle);
        centerPanel.add(Box.createVerticalStrut(12));

        // ── Module Cards (2x2 Grid) ──
        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(createDashboardCard(
                "📅",
                "Today's Schedule",
                "Daily lecture timeline, room allocations, and real-time bunkability verdicts.",
                ACCENT_CYAN,
                "SCHEDULE"));

        grid.add(createDashboardCard(
                "📊",
                "Attendance Matrix",
                "Comprehensive subject status, attended hours, percentage standings, and safe bunks.",
                ACCENT_EMERALD,
                "ATTENDANCE"));

        grid.add(createDashboardCard(
                "🗓️",
                "Academic Calendar",
                "Instructional days elapsed, remaining working days, condonation deadlines & exam schedule.",
                ACCENT_AMBER,
                "CALENDAR"));

        grid.add(createDashboardCard(
                "🎯",
                "Bunkability Calculator",
                "Simulate exact percentage drops before skipping classes and verify recovery needs.",
                ACCENT_PRIMARY,
                "BUNKABILITY"));

        centerPanel.add(grid);
        centerPanel.add(Box.createVerticalGlue());

        JScrollPane centerScroll = createModernScrollPane(centerPanel);
        panel.add(centerScroll, BorderLayout.CENTER);

        // ── Footer ──
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(12, 36, 12, 36)
        ));

        JLabel infoText = new JLabel("BUNKiT Academic Platform  •  Custom Data Structures (HashMap, BST, DLL, Stack, Queue)");
        infoText.setFont(FONT_SMALL);
        infoText.setForeground(TEXT_MUTED);
        footer.add(infoText, BorderLayout.WEST);

        JButton exitBtn = createOutlineButton("Exit Application");
        exitBtn.setPreferredSize(new Dimension(130, 32));
        exitBtn.addActionListener(e -> System.exit(0));
        footer.add(exitBtn, BorderLayout.EAST);

        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildDashboardKpiStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 4, 16, 0));
        strip.setOpaque(false);
        strip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        strip.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Calculate dynamic stats
        int totalAttended = 0;
        int totalClasses = 0;
        int safeCount = 0;
        int criticalCount = 0;

        for (Subject s : subjectRegistry.values()) {
            totalAttended += s.getAttendedClasses();
            totalClasses += s.getTotalClasses();
            if (s.getStatus() == AttendanceStatus.SAFE) safeCount++;
            else criticalCount++;
        }
        double avgPct = totalClasses > 0 ? ((double) totalAttended / totalClasses) * 100.0 : 0.0;
        int workingDaysLeft = calendarService.countWorkingDaysRemaining(calendarManager, LocalDate.now());

        strip.add(createMetricCard("OVERALL ATTENDANCE", String.format("%.1f%%", avgPct),
                avgPct >= 75.0 ? "On Track (≥ 75%)" : "Shortage Warning",
                avgPct >= 75.0 ? ACCENT_EMERALD : ACCENT_ROSE));

        strip.add(createMetricCard("SAFE SUBJECTS", safeCount + " of " + subjectRegistry.size(),
                "Meeting attendance quota", ACCENT_EMERALD));

        strip.add(createMetricCard("ACTION REQUIRED", String.valueOf(criticalCount),
                criticalCount == 0 ? "All subjects clear" : criticalCount + " subject(s) at risk",
                criticalCount == 0 ? ACCENT_EMERALD : ACCENT_ROSE));

        strip.add(createMetricCard("WORKING DAYS LEFT", String.valueOf(workingDaysLeft),
                "Instructional days to exams", ACCENT_CYAN));

        return strip;
    }

    private JPanel createMetricCard(String kicker, String value, String sub, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2.setColor(BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

                g2.setColor(BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 12, 12));

                // Left accent bar
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 3, h, 3, 3));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel kickerLabel = new JLabel(kicker);
        kickerLabel.setFont(FONT_BADGE);
        kickerLabel.setForeground(TEXT_MUTED);
        card.add(kickerLabel);
        card.add(Box.createVerticalStrut(3));

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLabel.setForeground(TEXT_PRIMARY);
        card.add(valLabel);
        card.add(Box.createVerticalStrut(2));

        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(accent);
        card.add(subLabel);

        return card;
    }

    private JPanel createDashboardCard(String icon, String title, String desc, Color accent, String target) {
        final boolean[] hovered = {false};

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Surface
                g2.setColor(hovered[0] ? BG_SURFACE_HOVER : BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

                // Border
                g2.setColor(hovered[0] ? accent : BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(hovered[0] ? 1.5f : 1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 14, 14));

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 20, 24));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Header: icon badge
        JPanel iconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 32));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(38, 38));
        iconBadge.setMaximumSize(new Dimension(38, 38));
        iconBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        iconBadge.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconBadge.add(iconLabel);
        card.add(iconBadge);
        card.add(Box.createVerticalStrut(14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));

        JLabel descLabel = new JLabel("<html>" + desc + "</html>");
        descLabel.setFont(FONT_BODY);
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(descLabel);
        card.add(Box.createVerticalGlue());

        // Footer action link
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel linkText = new JLabel("Launch Module →");
        linkText.setFont(FONT_BODY_SEMIBOLD);
        linkText.setForeground(accent);
        linkRow.add(linkText);
        card.add(linkRow);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered[0] = true;
                card.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hovered[0] = false;
                card.repaint();
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);
        panel.add(buildTopBar("Today's Schedule", "Daily lecture timetable & skip impact analysis", ACCENT_CYAN), BorderLayout.NORTH);

        scheduleContent = new JPanel();
        scheduleContent.setOpaque(false);
        scheduleContent.setLayout(new BorderLayout());
        populateScheduleContent();

        panel.add(createModernScrollPane(scheduleContent), BorderLayout.CENTER);
        return panel;
    }

    private void populateScheduleContent() {
        scheduleContent.removeAll();

        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        String dayName = dayOfWeek.name();

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            // Clean Weekend Card
            JPanel weekendPanel = new JPanel();
            weekendPanel.setOpaque(false);
            weekendPanel.setLayout(new BoxLayout(weekendPanel, BoxLayout.Y_AXIS));
            weekendPanel.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));

            JPanel card = new JPanel();
            card.setOpaque(false);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(36, 40, 36, 40)
            ));
            card.setMaximumSize(new Dimension(640, 300));
            card.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel title = new JLabel("Weekend — No Lectures Scheduled");
            title.setFont(FONT_DISPLAY);
            title.setForeground(TEXT_PRIMARY);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(title);
            card.add(Box.createVerticalStrut(10));

            JLabel subtitle = new JLabel("It's " + dayName + ". College classes will resume on Monday morning.", SwingConstants.CENTER);
            subtitle.setFont(FONT_BODY);
            subtitle.setForeground(TEXT_SECONDARY);
            subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(subtitle);
            card.add(Box.createVerticalStrut(24));

            // Summary row inside weekend card
            JPanel quickSummary = new JPanel(new GridLayout(1, 2, 16, 0));
            quickSummary.setOpaque(false);
            quickSummary.setMaximumSize(new Dimension(540, 70));

            quickSummary.add(createMetricCard("OVERALL STATUS", "On Track", "All critical subjects monitored", ACCENT_EMERALD));
            quickSummary.add(createMetricCard("UPCOMING MONDAY", "5 Lectures", "09:00 AM First Period", ACCENT_CYAN));
            card.add(quickSummary);

            weekendPanel.add(card);
            scheduleContent.add(weekendPanel, BorderLayout.CENTER);
        } else {
            // Weekday Table View
            scheduleManager.loadDay(dayName);
            List<ClassSession> sessions = scheduleManager.getSessionsForDay(dayName);

            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setOpaque(false);
            tablePanel.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));

            JPanel headerRow = new JPanel(new BorderLayout());
            headerRow.setOpaque(false);
            headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

            JLabel dayLabel = new JLabel(dayName + "  •  " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            dayLabel.setFont(FONT_TITLE);
            dayLabel.setForeground(TEXT_PRIMARY);
            headerRow.add(dayLabel, BorderLayout.WEST);

            int totalClasses = sessions != null ? sessions.size() : 0;
            int bunkableCount = 0;
            if (sessions != null) {
                for (ClassSession cs : sessions) {
                    Subject sub = subjectRegistry.get(cs.getSubjectCode());
                    if (sub != null && sub.isBunkableNextHour()) bunkableCount++;
                }
            }

            JLabel badgeSummary = createPillBadge(
                    totalClasses + " Classes Today  •  " + bunkableCount + " Safe to Skip",
                    new Color(56, 189, 248, 30), ACCENT_CYAN);
            headerRow.add(badgeSummary, BorderLayout.EAST);
            tablePanel.add(headerRow, BorderLayout.NORTH);

            String[] columns = {"Period", "Time Slot", "Code", "Subject Name", "Room", "Attendance %", "Bunkable Next?"};
            Object[][] data;

            if (sessions != null && !sessions.isEmpty()) {
                data = new Object[sessions.size()][7];
                for (int i = 0; i < sessions.size(); i++) {
                    ClassSession cs = sessions.get(i);
                    Subject sub = subjectRegistry.get(cs.getSubjectCode());
                    data[i][0] = "Period " + cs.getPeriodNumber();
                    data[i][1] = cs.getTimeSlot();
                    data[i][2] = cs.getSubjectCode();
                    data[i][3] = cs.getSubjectName();
                    data[i][4] = cs.getClassroom();
                    data[i][5] = sub != null ? String.format("%.2f%%", sub.getAttendancePercentage()) : "—";
                    data[i][6] = sub != null ? (sub.isBunkableNextHour() ? "SAFE" : "CRITICAL") : "—";
                }
            } else {
                data = new Object[0][7];
            }

            JTable table = createStyledTable(data, columns);
            table.getColumnModel().getColumn(6).setCellRenderer(new PillCellRenderer());

            JScrollPane tableScroll = createModernScrollPane(table);
            tablePanel.add(tableScroll, BorderLayout.CENTER);

            scheduleContent.add(tablePanel, BorderLayout.CENTER);
        }

        scheduleContent.revalidate();
        scheduleContent.repaint();
    }

    // ══════════════════════════════════════════════════════════════════
    //  4. ATTENDANCE STATUS PANEL
    // ══════════════════════════════════════════════════════════════════
    private JPanel attendanceContent;

    private JPanel buildAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);
        panel.add(buildTopBar("Attendance Status Matrix", "Complete subject metrics & safety buffer overview", ACCENT_EMERALD), BorderLayout.NORTH);

        attendanceContent = new JPanel(new BorderLayout());
        attendanceContent.setOpaque(false);
        populateAttendanceContent();

        panel.add(createModernScrollPane(attendanceContent), BorderLayout.CENTER);
        return panel;
    }

    private void populateAttendanceContent() {
        attendanceContent.removeAll();
        attendanceContent.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));

        // Summary Cards Row
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 16, 0));
        summaryRow.setOpaque(false);
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));

        int safeCount = 0, edgeCount = 0, critCount = 0;
        for (Subject s : subjectRegistry.values()) {
            switch (s.getStatus()) {
                case SAFE: safeCount++; break;
                case ON_THE_EDGE: edgeCount++; break;
                case CRITICAL: critCount++; break;
            }
        }
        summaryRow.add(createMetricCard("SAFE SUBJECTS", safeCount + " Subjects", "Attendance strictly ≥ 80%", ACCENT_EMERALD));
        summaryRow.add(createMetricCard("BORDERLINE / ON EDGE", edgeCount + " Subjects", "Between 75% and 80%", ACCENT_AMBER));
        summaryRow.add(createMetricCard("CRITICAL SHORTAGE", critCount + " Subjects", "Below 75% university rule", ACCENT_ROSE));

        attendanceContent.add(summaryRow, BorderLayout.NORTH);

        // Table
        String[] columns = {"Code", "Subject Title", "Attended / Conducted", "Current %", "Status", "Safe Bunks Left", "Recovery Needed", "Can Skip Next?"};
        java.util.Collection<Subject> subjects = subjectRegistry.values();
        Object[][] data = new Object[subjects.size()][8];
        int i = 0;
        for (Subject s : subjects) {
            data[i][0] = s.getSubjectCode();
            data[i][1] = s.getSubjectName();
            data[i][2] = s.getAttendedClasses() + " of " + s.getTotalClasses();
            data[i][3] = String.format("%.2f%%", s.getAttendancePercentage());
            data[i][4] = s.getStatus().name();
            data[i][5] = s.getSafeBunks() > 0 ? s.getSafeBunks() + " classes" : "0 classes";
            data[i][6] = s.getClassesNeededTo75() > 0 ? "Must attend " + s.getClassesNeededTo75() : "Quota satisfied";
            data[i][7] = s.isBunkableNextHour() ? "YES" : "NO";
            i++;
        }

        JTable table = createStyledTable(data, columns);
        table.getColumnModel().getColumn(4).setCellRenderer(new PillCellRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new PillCellRenderer());

        JScrollPane tableScroll = createModernScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        attendanceContent.add(tableScroll, BorderLayout.CENTER);

        attendanceContent.revalidate();
        attendanceContent.repaint();
    }

    // ══════════════════════════════════════════════════════════════════
    //  5. ACADEMIC CALENDAR PANEL
    // ══════════════════════════════════════════════════════════════════
    private JPanel calendarContent;

    private JPanel buildCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);
        panel.add(buildTopBar("Academic Calendar & Projections", "Semester milestones & projected attendance at semester end", ACCENT_AMBER), BorderLayout.NORTH);

        calendarContent = new JPanel();
        calendarContent.setOpaque(false);
        calendarContent.setLayout(new BoxLayout(calendarContent, BoxLayout.Y_AXIS));
        populateCalendarContent();

        panel.add(createModernScrollPane(calendarContent), BorderLayout.CENTER);
        return panel;
    }

    private void populateCalendarContent() {
        calendarContent.removeAll();
        calendarContent.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));

        LocalDate today = LocalDate.now();
        int workingDaysLeft = calendarService.countWorkingDaysRemaining(calendarManager, today);
        int workingDaysElapsed = calendarService.countWorkingDaysElapsed(calendarManager, today);
        int totalWorkingDays = calendarService.getTotalInstructionalWorkingDays();
        double pctComplete = totalWorkingDays > 0 ? ((double) workingDaysElapsed / totalWorkingDays) * 100.0 : 0;

        // Stat cards
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.add(createMetricCard("WORKING DAYS ELAPSED", String.valueOf(workingDaysElapsed), "Days completed in S3", ACCENT_EMERALD));
        statsRow.add(createMetricCard("WORKING DAYS REMAINING", String.valueOf(workingDaysLeft), "Instructional days left", ACCENT_CYAN));
        statsRow.add(createMetricCard("TOTAL INSTRUCTIONAL DAYS", String.valueOf(totalWorkingDays), "Prescribed calendar days", ACCENT_PURPLE));
        statsRow.add(createMetricCard("SEMESTER PROGRESS", String.format("%.1f%%", pctComplete), "Completion status", ACCENT_AMBER));
        calendarContent.add(statsRow);
        calendarContent.add(Box.createVerticalStrut(20));

        // Semester Progress Bar Card
        JPanel progressCard = new JPanel(new BorderLayout(0, 8));
        progressCard.setOpaque(false);
        progressCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        progressCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel labelRow = new JPanel(new BorderLayout());
        labelRow.setOpaque(false);
        JLabel progTitle = new JLabel("Semester Instructional Completion");
        progTitle.setFont(FONT_SUBHEAD);
        progTitle.setForeground(TEXT_SECONDARY);
        labelRow.add(progTitle, BorderLayout.WEST);

        JLabel progPct = new JLabel(String.format("%.1f%% Completed", pctComplete));
        progPct.setFont(FONT_SUBHEAD);
        progPct.setForeground(TEXT_PRIMARY);
        labelRow.add(progPct, BorderLayout.EAST);
        progressCard.add(labelRow, BorderLayout.NORTH);

        JPanel progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2.setColor(BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 8, 8));

                double pct2 = Math.min(pctComplete, 100.0) / 100.0;
                int fillWidth = (int) (w * pct2);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_PRIMARY, fillWidth, 0, ACCENT_CYAN);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, fillWidth, h, 8, 8));
                g2.dispose();
            }
        };
        progressBar.setPreferredSize(new Dimension(0, 14));
        progressBar.setOpaque(false);
        progressCard.add(progressBar, BorderLayout.CENTER);
        calendarContent.add(progressCard);
        calendarContent.add(Box.createVerticalStrut(24));

        // Key Milestones
        JLabel milestoneTitle = new JLabel("Key Semester Milestones");
        milestoneTitle.setFont(FONT_TITLE);
        milestoneTitle.setForeground(TEXT_PRIMARY);
        milestoneTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        calendarContent.add(milestoneTitle);
        calendarContent.add(Box.createVerticalStrut(12));

        LocalDate lastInst = calendarService.getLastInstructionalDay();
        LocalDate condonation = calendarService.getCondonationDeadlineNoFine();
        LocalDate examDate = calendarService.getFirstExamDate();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy (EEE)");

        addMilestoneCard("🏁  Last Instructional Day", lastInst != null ? lastInst.format(fmt) : "N/A",
                lastInst != null ? calendarManager.countDaysUntilExam(lastInst, today) + " days remaining" : "", ACCENT_EMERALD);

        addMilestoneCard("⚠️  Condonation Application Deadline", condonation != null ? condonation.format(fmt) : "N/A",
                condonation != null ? calendarManager.countDaysUntilExam(condonation, today) + " days remaining" : "", ACCENT_AMBER);

        addMilestoneCard("📚  First End-Semester Examination", examDate != null ? examDate.format(fmt) : "N/A",
                examDate != null ? calendarManager.countDaysUntilExam(examDate, today) + " days countdown" : "", ACCENT_CYAN);

        calendarContent.add(Box.createVerticalStrut(24));

        // Projection Table
        JLabel projTitle = new JLabel("Semester-End Attendance Projections");
        projTitle.setFont(FONT_TITLE);
        projTitle.setForeground(TEXT_PRIMARY);
        projTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        calendarContent.add(projTitle);
        calendarContent.add(Box.createVerticalStrut(12));

        String[] cols = {"Code", "Subject Title", "Attended", "Current %", "Remaining Classes", "Max Possible %", "Must Attend", "Semester Bunk Budget"};
        java.util.Collection<Subject> subjects = subjectRegistry.values();
        Object[][] data = new Object[subjects.size()][8];
        int i = 0;
        for (Subject s : subjects) {
            SemesterProjection proj = SemesterAttendanceProjector.project(s, today, calendarManager, calendarService, scheduleManager);
            data[i][0] = s.getSubjectCode();
            data[i][1] = s.getSubjectName();
            data[i][2] = s.getAttendedClasses() + " of " + s.getTotalClasses();
            data[i][3] = String.format("%.2f%%", s.getAttendancePercentage());
            data[i][4] = proj.getRemainingClasses() + " hours";
            data[i][5] = String.format("%.2f%%", proj.getMaxPossiblePercentage());
            data[i][6] = proj.getMustAttendToPass() + " classes";
            data[i][7] = proj.getSemesterBunksRemaining() > 0 ? proj.getSemesterBunksRemaining() + " bunks allowed" : "0 bunks allowed";
            i++;
        }

        JTable table = createStyledTable(data, cols);
        JScrollPane tableScroll = createModernScrollPane(table);
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableScroll.setPreferredSize(new Dimension(800, 220));
        tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        calendarContent.add(tableScroll);

        calendarContent.revalidate();
        calendarContent.repaint();
    }

    private void addMilestoneCard(String label, String date, String countdown, Color accent) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

                g2.setColor(BORDER_SUBTLE);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 10, 10));

                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 3, getHeight(), 3, 3));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel left = new JLabel(label + "   —   " + date);
        left.setFont(FONT_BODY);
        left.setForeground(TEXT_PRIMARY);
        card.add(left, BorderLayout.WEST);

        JLabel right = createPillBadge(countdown, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25), accent);
        card.add(right, BorderLayout.EAST);

        calendarContent.add(card);
        calendarContent.add(Box.createVerticalStrut(8));
    }

    // ══════════════════════════════════════════════════════════════════
    //  6. BUNKABILITY CHECKER PANEL
    // ══════════════════════════════════════════════════════════════════
    private JPanel bunkContent;

    private JPanel buildBunkabilityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);
        panel.add(buildTopBar("Bunkability Calculator", "Pre-skip evaluation & 1-hour drop impact engine", ACCENT_PRIMARY), BorderLayout.NORTH);

        bunkContent = new JPanel(new BorderLayout());
        bunkContent.setOpaque(false);
        populateBunkContent();

        panel.add(createModernScrollPane(bunkContent), BorderLayout.CENTER);
        return panel;
    }

    private void populateBunkContent() {
        bunkContent.removeAll();
        bunkContent.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));

        // Selector Bar
        JPanel selectorCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(BORDER_SUBTLE);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 12, 12));
                g2.dispose();
            }
        };
        selectorCard.setOpaque(false);
        selectorCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel selectLabel = new JLabel("Select Subject:");
        selectLabel.setFont(FONT_BODY_SEMIBOLD);
        selectLabel.setForeground(TEXT_PRIMARY);
        selectorCard.add(selectLabel);

        java.util.Vector<String> items = new java.util.Vector<>();
        items.add("ALL SUBJECTS OVERVIEW");
        for (Subject s : subjectRegistry.values()) {
            items.add(s.getSubjectCode() + " — " + s.getSubjectName());
        }
        JComboBox<String> subjectCombo = new JComboBox<>(items);
        subjectCombo.setFont(FONT_BODY);
        subjectCombo.setBackground(BG_SURFACE_SUBTLE);
        subjectCombo.setForeground(TEXT_PRIMARY);
        subjectCombo.setPreferredSize(new Dimension(380, 36));
        subjectCombo.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true));
        ((JComponent) subjectCombo.getRenderer()).setOpaque(true);
        subjectCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean foc) {
                Component c = super.getListCellRendererComponent(list, value, index, sel, foc);
                c.setBackground(sel ? BG_SURFACE_HOVER : BG_SURFACE);
                c.setForeground(TEXT_PRIMARY);
                return c;
            }
        });
        selectorCard.add(subjectCombo);

        JButton checkBtn = createPrimaryButton("Evaluate Subject");
        checkBtn.setPreferredSize(new Dimension(150, 36));
        selectorCard.add(checkBtn);

        bunkContent.add(selectorCard, BorderLayout.NORTH);

        // Results Container
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setOpaque(false);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        // Default: show overview table
        resultsPanel.add(buildAllSubjectsBunkTable(), BorderLayout.CENTER);
        bunkContent.add(resultsPanel, BorderLayout.CENTER);

        checkBtn.addActionListener(e -> {
            resultsPanel.removeAll();
            String selected = (String) subjectCombo.getSelectedItem();
            if (selected == null || selected.equals("ALL SUBJECTS OVERVIEW")) {
                resultsPanel.add(buildAllSubjectsBunkTable(), BorderLayout.CENTER);
            } else {
                String code = selected.split("—")[0].trim();
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

        String[] cols = {"Code", "Subject Title", "Attended", "Current %", "If Bunked (1 Hr)", "% Drop", "Safe Bunks Left", "Can Skip Next?"};
        java.util.Collection<Subject> subjects = subjectRegistry.values();
        Object[][] data = new Object[subjects.size()][8];
        int i = 0;
        for (Subject s : subjects) {
            double cur = s.getAttendancePercentage();
            double ifBunk = s.getProjectedPercentageIfBunk();
            double drop = cur - ifBunk;
            data[i][0] = s.getSubjectCode();
            data[i][1] = s.getSubjectName();
            data[i][2] = s.getAttendedClasses() + " of " + s.getTotalClasses();
            data[i][3] = String.format("%.2f%%", cur);
            data[i][4] = String.format("%.2f%%", ifBunk);
            data[i][5] = String.format("-%.2f%%", drop);
            data[i][6] = s.getSafeBunks() + " classes";
            data[i][7] = s.isBunkableNextHour() ? "YES" : "NO";
            i++;
        }

        JTable table = createStyledTable(data, cols);
        table.getColumnModel().getColumn(7).setCellRenderer(new PillCellRenderer());

        JScrollPane scroll = createModernScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSingleSubjectBunkDetail(Subject s) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        double cur  = s.getAttendancePercentage();
        double bunk = s.getProjectedPercentageIfBunk();
        double att  = s.getProjectedPercentageIfAttend();
        double drop = cur - bunk;
        int safeBunks = s.getSafeBunks();
        int needed = s.getClassesNeededTo75();
        boolean safe = s.isBunkableNextHour();

        // 4 Metric cards at top
        JPanel metricStrip = new JPanel(new GridLayout(1, 4, 16, 0));
        metricStrip.setOpaque(false);
        metricStrip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        metricStrip.setAlignmentX(Component.LEFT_ALIGNMENT);

        metricStrip.add(createMetricCard("CURRENT ATTENDANCE", String.format("%.2f%%", cur),
                s.getAttendedClasses() + " / " + s.getTotalClasses() + " classes",
                cur >= 80.0 ? ACCENT_EMERALD : cur >= 75.0 ? ACCENT_AMBER : ACCENT_ROSE));

        metricStrip.add(createMetricCard("IF YOU SKIP 1 HOUR", String.format("%.2f%%", bunk),
                String.format("Drop of -%.2f%%", drop), ACCENT_ROSE));

        metricStrip.add(createMetricCard("IF YOU ATTEND 1 HOUR", String.format("%.2f%%", att),
                String.format("Gain of +%.2f%%", att - cur), ACCENT_EMERALD));

        metricStrip.add(createMetricCard("SAFE BUNK BUFFER", safeBunks + " Classes",
                safeBunks > 0 ? "Before dropping below 75%" : "No bunk buffer available",
                safeBunks > 0 ? ACCENT_CYAN : ACCENT_AMBER));

        panel.add(metricStrip);
        panel.add(Box.createVerticalStrut(20));

        // Analytical Detail Card
        JPanel infoCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(BORDER_SUBTLE);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 12, 12));
                g2.dispose();
            }
        };
        infoCard.setOpaque(false);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subject Header Row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel nameLabel = new JLabel(s.getSubjectCode() + "  —  " + s.getSubjectName());
        nameLabel.setFont(FONT_TITLE);
        nameLabel.setForeground(TEXT_PRIMARY);
        headerRow.add(nameLabel, BorderLayout.WEST);

        JLabel statusBadge = createPillBadge(s.getStatus().name(),
                s.getStatus() == AttendanceStatus.SAFE ? new Color(34, 197, 94, 30) : new Color(239, 68, 68, 30),
                s.getStatus() == AttendanceStatus.SAFE ? ACCENT_EMERALD : ACCENT_ROSE);
        headerRow.add(statusBadge, BorderLayout.EAST);
        infoCard.add(headerRow);
        infoCard.add(Box.createVerticalStrut(16));

        // Visual Progress Gauge
        JPanel progWrapper = new JPanel(new BorderLayout(0, 6));
        progWrapper.setOpaque(false);
        progWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel progLabelRow = new JPanel(new BorderLayout());
        progLabelRow.setOpaque(false);
        JLabel pl = new JLabel("Attendance Threshold Gauge (75% Minimum Required)");
        pl.setFont(FONT_SMALL);
        pl.setForeground(TEXT_SECONDARY);
        progLabelRow.add(pl, BorderLayout.WEST);

        JLabel pr = new JLabel(String.format("%.2f%%", cur));
        pr.setFont(FONT_SUBHEAD);
        pr.setForeground(cur >= 75.0 ? ACCENT_EMERALD : ACCENT_ROSE);
        progLabelRow.add(pr, BorderLayout.EAST);
        progWrapper.add(progLabelRow, BorderLayout.NORTH);

        JPanel progBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2.setColor(BG_SURFACE_SUBTLE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 8, 8));

                double pct = Math.min(cur, 100.0) / 100.0;
                int fillW = (int) (w * pct);
                Color barColor = cur >= 80 ? ACCENT_EMERALD : cur >= 75 ? ACCENT_AMBER : ACCENT_ROSE;
                g2.setColor(barColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, 8, 8));

                // 75% threshold line
                int threshX = (int) (w * 0.75);
                g2.setColor(new Color(255, 255, 255, 140));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{3, 3}, 0));
                g2.drawLine(threshX, 0, threshX, h);
                g2.dispose();
            }
        };
        progBar.setPreferredSize(new Dimension(0, 14));
        progBar.setOpaque(false);
        progWrapper.add(progBar, BorderLayout.CENTER);
        infoCard.add(progWrapper);
        infoCard.add(Box.createVerticalStrut(20));

        // Breakdown items
        addDetailRow(infoCard, "Total Classes Conducted", s.getTotalClasses() + " lectures held", TEXT_PRIMARY);
        addDetailRow(infoCard, "Classes You Attended", s.getAttendedClasses() + " lectures attended", TEXT_PRIMARY);
        addDetailRow(infoCard, "Drop If You Skip 1 Hour", String.format("%.2f%%  (Attended %d of %d)", bunk, s.getAttendedClasses(), s.getTotalClasses() + 1), ACCENT_ROSE);
        addDetailRow(infoCard, "Gain If You Attend 1 Hour", String.format("%.2f%%  (Attended %d of %d)", att, s.getAttendedClasses() + 1, s.getTotalClasses() + 1), ACCENT_EMERALD);
        addDetailRow(infoCard, "Safe Consecutive Skips Allowed", safeBunks + " classes before falling below 75%", ACCENT_CYAN);

        if (cur < 75.0) {
            addDetailRow(infoCard, "Shortage Recovery Requirement", "Must attend " + needed + " consecutive classes without skip!", ACCENT_ROSE);
        }

        infoCard.add(Box.createVerticalStrut(20));

        // Verdict Banner Card
        JPanel verdictCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = safe ? new Color(34, 197, 94, 25) : new Color(239, 68, 68, 25);
                Color border = safe ? ACCENT_EMERALD : ACCENT_ROSE;

                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 10, 10));
                g2.dispose();
            }
        };
        verdictCard.setOpaque(false);
        verdictCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        verdictCard.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel verdictLabel = new JLabel(safe
                ? String.format("VERDICT: SAFE TO BUNK  —  Projected attendance stays at %.2f%% (≥ 75.0%% threshold)", bunk)
                : String.format("VERDICT: DO NOT BUNK  —  Projected attendance drops to %.2f%% (< 75.0%% requirement)", bunk));
        verdictLabel.setFont(FONT_BUTTON);
        verdictLabel.setForeground(safe ? ACCENT_EMERALD : ACCENT_ROSE);
        verdictCard.add(verdictLabel, BorderLayout.WEST);

        infoCard.add(verdictCard);
        panel.add(infoCard);

        return panel;
    }

    private void addDetailRow(JPanel parent, String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_SECONDARY);
        row.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel(value);
        val.setFont(FONT_BODY_SEMIBOLD);
        val.setForeground(valueColor);
        row.add(val, BorderLayout.EAST);

        parent.add(row);
    }

    // ══════════════════════════════════════════════════════════════════
    //  SHARED COMPONENT HELPERS & STYLED CONTROLS
    // ══════════════════════════════════════════════════════════════════

    /** Clean internal top navigation bar */
    private JPanel buildTopBar(String title, String subtitle, Color accent) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(16, 36, 16, 36)
        ));

        // Back button
        JButton backBtn = new JButton("← Back to Menu");
        backBtn.setFont(FONT_BUTTON);
        backBtn.setForeground(TEXT_PRIMARY);
        backBtn.setBackground(BG_SURFACE);
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
            BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { backBtn.setBackground(BG_SURFACE_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { backBtn.setBackground(BG_SURFACE); }
        });
        backBtn.addActionListener(e -> cardLayout.show(contentPanel, "MENU"));
        bar.add(backBtn, BorderLayout.WEST);

        // Center Title & Subtitle
        JPanel centerWrap = new JPanel();
        centerWrap.setOpaque(false);
        centerWrap.setLayout(new BoxLayout(centerWrap, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrap.add(titleLabel);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subLabel = new JLabel(subtitle, SwingConstants.CENTER);
            subLabel.setFont(FONT_SMALL);
            subLabel.setForeground(TEXT_MUTED);
            subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerWrap.add(Box.createVerticalStrut(2));
            centerWrap.add(subLabel);
        }
        bar.add(centerWrap, BorderLayout.CENTER);

        // Right Context Pill
        JLabel contextPill = createPillBadge("Semester 3", new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25), accent);
        bar.add(contextPill, BorderLayout.EAST);

        return bar;
    }

    /** Creates a modern styled dark JTable */
    private JTable createStyledTable(Object[][] data, String[] columns) {
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_SURFACE);
        table.setGridColor(new Color(30, 37, 53));
        table.setRowHeight(38);
        table.setSelectionBackground(BG_SURFACE_HOVER);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BADGE);
        header.setBackground(new Color(24, 30, 44));
        header.setForeground(TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // Cell Alignment & Alternating Row Colors
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? BG_SURFACE : BG_SURFACE_SUBTLE);
                }
                setHorizontalAlignment(col == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(0, col == 1 ? 16 : 8, 0, 8));
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        return table;
    }

    /** Custom Table Cell Renderer for Pill Badges */
    private static class PillCellRenderer extends DefaultTableCellRenderer {
        private Color pillBg = new Color(0, 0, 0, 0);
        private Color pillFg = Color.WHITE;
        private String pillText = "";

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String str = value != null ? value.toString() : "";
            if (str.contains("SAFE") || str.equalsIgnoreCase("YES")) {
                pillBg = new Color(34, 197, 94, 25);
                pillFg = ACCENT_EMERALD;
                pillText = str.equalsIgnoreCase("YES") ? "● YES" : "● SAFE";
            } else if (str.contains("EDGE")) {
                pillBg = new Color(245, 158, 11, 25);
                pillFg = ACCENT_AMBER;
                pillText = "● ON EDGE";
            } else if (str.contains("CRIT") || str.equalsIgnoreCase("NO")) {
                pillBg = new Color(239, 68, 68, 25);
                pillFg = ACCENT_ROSE;
                pillText = str.equalsIgnoreCase("NO") ? "● NO" : "● CRITICAL";
            } else {
                pillBg = new Color(55, 65, 81, 35);
                pillFg = TEXT_SECONDARY;
                pillText = str;
            }
            setBackground(isSelected ? BG_SURFACE_HOVER : (row % 2 == 0 ? BG_SURFACE : BG_SURFACE_SUBTLE));
            setText("");
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            int h = getHeight();
            int pillW = 86;
            int pillH = 22;
            int pillX = (w - pillW) / 2;
            int pillY = (h - pillH) / 2;

            g2.setColor(pillBg);
            g2.fill(new RoundRectangle2D.Float(pillX, pillY, pillW, pillH, 10, 10));

            g2.setColor(pillFg);
            g2.setFont(FONT_BADGE);
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(pillText)) / 2;
            int textY = pillY + (pillH + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(pillText, textX, textY);

            g2.dispose();
        }
    }

    /** Modern pill badge component */
    private JLabel createPillBadge(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(FONT_BADGE);
        l.setForeground(fg);
        l.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return l;
    }

    /** Creates modern scroll pane with dark custom scrollbars */
    private JScrollPane createModernScrollPane(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Minimalist dark scrollbar UI */
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(55, 65, 81);
            this.trackColor = new Color(0, 0, 0, 0);
        }
        @Override
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(75, 85, 99) : new Color(55, 65, 81));
            g2.fill(new RoundRectangle2D.Float(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 6, 6));
            g2.dispose();
        }
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {}
    }

    /** Primary solid button */
    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? new Color(79, 70, 229) :
                          getModel().isRollover() ? ACCENT_PRIMARY_HOVER : ACCENT_PRIMARY;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
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
        return btn;
    }

    /** Secondary outline button */
    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_SURFACE_HOVER : BG_SURFACE_SUBTLE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(getModel().isRollover() ? BORDER_HOVER : BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(TEXT_PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(280, 40));
        return btn;
    }

    /** Styled text field */
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FONT_BODY);
        field.setBackground(BG_SURFACE_SUBTLE);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(300, 38));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_PRIMARY, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
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
        field.setBackground(BG_SURFACE_SUBTLE);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(240, 38));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_PRIMARY, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return field;
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
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Panel.background", BG_MAIN);
            UIManager.put("OptionPane.background", BG_SURFACE);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(CanIBunkGUI::new);
    }
}
