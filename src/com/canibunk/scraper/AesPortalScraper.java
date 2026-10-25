package com.canibunk.scraper;

import com.canibunk.ds.CustomHashMap;
import com.canibunk.model.Subject;
import com.canibunk.service.ScheduleManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Selenium WebDriver scraper for Amal Jyothi College of Engineering AES portal (aesajce.in).
 * Authenticates the user, directly navigates to:
 *   1. https://www.aesajce.in/students/currentAttendance.php -> Subject-wise Attendance Table
 *   2. https://www.aesajce.in/students/classworkSchedule.php -> Class Timetable Grid
 *
 * Populates CustomHashMap (Subjects) and ScheduleManager (CustomQueue of today's periods).
 * Security: Credentials are used only for the live session and are never saved or persisted.
 */
public class AesPortalScraper {

    public static final String DEFAULT_LOGIN_URL = "https://login.aesajce.in/";
    public static final String ATTENDANCE_URL = "https://www.aesajce.in/students/currentAttendance.php";
    public static final String TIMETABLE_URL = "https://www.aesajce.in/students/classworkSchedule.php";
    private static final int TIMEOUT_SECONDS = 15;

    private boolean headless = true;

    /**
     * Container holding the results of a portal scraping session.
     */
    public static class ScraperResult {
        private final int subjectCount;
        private final int timetableSessionsCount;
        private final TimetableParser.TimetableResult timetableResult;

        public ScraperResult(int subjectCount, int timetableSessionsCount, TimetableParser.TimetableResult timetableResult) {
            this.subjectCount = subjectCount;
            this.timetableSessionsCount = timetableSessionsCount;
            this.timetableResult = timetableResult;
        }

        public int getSubjectCount() {
            return subjectCount;
        }

        public int getTimetableSessionsCount() {
            return timetableSessionsCount;
        }

        public TimetableParser.TimetableResult getTimetableResult() {
            return timetableResult;
        }
    }

    public AesPortalScraper() {
        this(true);
    }

    public AesPortalScraper(boolean headless) {
        this.headless = headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    /**
     * Authenticates with student credentials, scrapes the attendance table,
     * and populates the provided CustomHashMap registry.
     *
     * @param username student admission number or username
     * @param password student portal password
     * @param registry custom hash map to populate with scraped subjects
     * @return number of subjects successfully parsed and added
     * @throws Exception if scraping fails or credentials are invalid
     */
    public int scrapeAttendance(String username, char[] password, CustomHashMap<String, Subject> registry) throws Exception {
        return scrapePortal(username, password, registry, null).getSubjectCount();
    }

    /**
     * Complete portal scraping routine:
     * Authenticates student, scrapes current attendance from currentAttendance.php,
     * and scrapes the class timetable from classworkSchedule.php.
     *
     * @param username student admission number or username
     * @param password student portal password
     * @param registry custom hash map to populate with scraped subjects
     * @param scheduleManager optional schedule manager to populate with today's periods
     * @return ScraperResult with counts and timetable details
     * @throws Exception if authentication or scraping fails
     */
    public ScraperResult scrapePortal(String username, char[] password, CustomHashMap<String, Subject> registry, ScheduleManager scheduleManager) throws Exception {
        WebDriver driver = null;
        try {
            System.out.println("[Scraper] Initializing browser engine (Selenium WebDriver)...");
            driver = createDriver();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));

            System.out.println("[Scraper] Navigating to AES Login Portal: " + DEFAULT_LOGIN_URL);
            driver.get(DEFAULT_LOGIN_URL);

            // 1. Wait for login inputs
            WebElement usernameInput = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("txtUsername")));
            WebElement passwordInput = driver.findElement(By.id("txtPassword"));
            WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));

            System.out.println("[Scraper] Entering credentials for student user: " + username);
            usernameInput.clear();
            usernameInput.sendKeys(username);

            passwordInput.clear();
            passwordInput.sendKeys(new String(password));

            // Immediately wipe the password character array for security
            java.util.Arrays.fill(password, ' ');

            System.out.println("[Scraper] Submitting login request...");
            submitBtn.click();

            // 2. Wait for page transition after login
            Thread.sleep(2500); // Give portal time to process auth & redirects

            // Check if login failed (look for error alerts or staying on login page)
            String currentUrl = driver.getCurrentUrl();
            System.out.println("[Scraper] Current page URL after login: " + currentUrl);

            // Look for alert messages or error toasts if present
            List<WebElement> errorAlerts = driver.findElements(By.cssSelector(".alert-danger, .notifyjs-bootstrap-error, .text-danger"));
            for (WebElement alert : errorAlerts) {
                if (alert.isDisplayed() && !alert.getText().trim().isEmpty()) {
                    String err = alert.getText().trim();
                    if (err.toLowerCase().contains("invalid") || err.toLowerCase().contains("password") || err.toLowerCase().contains("user")) {
                        throw new IllegalArgumentException("Authentication Failed: " + err);
                    }
                }
            }

            // Save landing page debug HTML
            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Path.of("scraped_portal_debug.html"),
                        driver.getPageSource(),
                        java.nio.charset.StandardCharsets.UTF_8
                );
            } catch (Exception ignored) {}

            // 3. STEP A: Navigate Directly to Current Attendance Page
            System.out.println("\n[Scraper] ─── STEP 1: Subject-wise Attendance (currentAttendance.php) ───");
            System.out.println("[Scraper] Navigating directly to: " + ATTENDANCE_URL);
            driver.get(ATTENDANCE_URL);
            Thread.sleep(2000);

            // Fallback: If direct URL didn't navigate, click the Attendance link in the navigation menu
            if (!driver.getCurrentUrl().contains("currentAttendance")) {
                List<WebElement> curAttLinks = driver.findElements(
                        By.xpath("//a[contains(@href, 'currentAttendance.php')]"));
                if (!curAttLinks.isEmpty()) {
                    System.out.println("[Scraper] Following navigation link to currentAttendance.php...");
                    curAttLinks.get(0).click();
                    Thread.sleep(2000);
                }
            }

            // Save attendance page HTML for inspection
            String attendanceHtml = driver.getPageSource();
            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Path.of("scraped_attendance_debug.html"),
                        attendanceHtml,
                        java.nio.charset.StandardCharsets.UTF_8
                );
                System.out.println("[Scraper Debug] Saved attendance page HTML to 'scraped_attendance_debug.html'");
            } catch (Exception ignored) {}

            // Locate and Score All HTML Tables on Attendance Page
            System.out.println("[Scraper] Inspecting attendance tables...");
            List<WebElement> tables = driver.findElements(By.tagName("table"));
            System.out.println("[Scraper] Found " + tables.size() + " HTML table(s) on current attendance page.");

            List<Subject> bestSubjects = new java.util.ArrayList<>();
            String bestTableHtml = "";
            int bestTableIndex = -1;

            for (int i = 0; i < tables.size(); i++) {
                WebElement t = tables.get(i);
                try {
                    String tHtml = t.getAttribute("outerHTML");
                    List<Subject> candidate = AttendanceTableParser.parseHtmlTable(tHtml);
                    System.out.printf("[Scraper Debug] Attendance Table #%d: contains %d rows, parsed %d valid subject(s).\n",
                            i + 1, t.findElements(By.tagName("tr")).size(), candidate.size());

                    if (candidate.size() > bestSubjects.size()) {
                        bestSubjects = candidate;
                        bestTableHtml = tHtml;
                        bestTableIndex = i + 1;
                    }
                } catch (Exception ignored) {}
            }

            // Fallback: check whole page HTML if individual table elements didn't yield subjects
            if (bestSubjects.isEmpty()) {
                bestSubjects = AttendanceTableParser.parseHtmlTable(attendanceHtml);
            }

            // If still empty, check inside iframes
            if (bestSubjects.isEmpty()) {
                List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
                if (!iframes.isEmpty()) {
                    System.out.printf("[Scraper] Checking %d iframe(s) for nested attendance tables...\n", iframes.size());
                    for (WebElement frame : iframes) {
                        try {
                            driver.switchTo().frame(frame);
                            List<WebElement> frameTables = driver.findElements(By.tagName("table"));
                            for (WebElement ft : frameTables) {
                                String ftHtml = ft.getAttribute("outerHTML");
                                List<Subject> candidate = AttendanceTableParser.parseHtmlTable(ftHtml);
                                if (candidate.size() > bestSubjects.size()) {
                                    bestSubjects = candidate;
                                    bestTableHtml = ftHtml;
                                }
                            }
                            driver.switchTo().defaultContent();
                        } catch (Exception e) {
                            driver.switchTo().defaultContent();
                        }
                    }
                }
            }

            int loadedSubjectCount = 0;
            if (!bestSubjects.isEmpty()) {
                System.out.printf("[Scraper] Selected Table #%d as primary attendance table.\n", bestTableIndex);
                System.out.println("[Scraper] Extracted Subjects Summary:");
                for (Subject s : bestSubjects) {
                    System.out.printf("   ➜ [%-8s] %-28s | %d/%d (%.2f%%) | Bunks: %d | Needed: %d\n",
                            s.getSubjectCode(),
                            s.getSubjectName().length() > 28 ? s.getSubjectName().substring(0, 25) + "..." : s.getSubjectName(),
                            s.getAttendedClasses(),
                            s.getTotalClasses(),
                            s.getAttendancePercentage(),
                            s.getSafeBunks(),
                            s.getClassesNeededTo75()
                    );
                }
                loadedSubjectCount = AttendanceTableParser.populateRegistry(bestSubjects, registry, true);
                System.out.printf("[Scraper] Successfully loaded %d subjects into CustomHashMap!\n", loadedSubjectCount);
            } else {
                System.out.println("[Scraper] ⚠️ Warning: Could not find subjects on attendance page. Check 'scraped_attendance_debug.html'.");
            }

            // 4. STEP B: Navigate Directly to Classwork Schedule / Timetable Page
            int loadedTimetableSessions = 0;
            TimetableParser.TimetableResult timetableResult = null;

            if (scheduleManager != null) {
                try {
                    System.out.println("\n[Scraper] ─── STEP 2: Class Timetable (classworkSchedule.php) ───");
                    System.out.println("[Scraper] Navigating directly to: " + TIMETABLE_URL);
                    driver.get(TIMETABLE_URL);
                    Thread.sleep(2000);

                    // Fallback: If direct URL didn't navigate, click Timetable link in menu
                    if (!driver.getCurrentUrl().contains("classworkSchedule")) {
                        List<WebElement> schedLinks = driver.findElements(
                                By.xpath("//a[contains(@href, 'classworkSchedule.php')]"));
                        if (!schedLinks.isEmpty()) {
                            System.out.println("[Scraper] Following navigation link to classworkSchedule.php...");
                            schedLinks.get(0).click();
                            Thread.sleep(2000);
                        }
                    }

                    // Save timetable page HTML for inspection
                    String scheduleHtml = driver.getPageSource();
                    try {
                        java.nio.file.Files.writeString(
                                java.nio.file.Path.of("scraped_timetable_debug.html"),
                                scheduleHtml,
                                java.nio.charset.StandardCharsets.UTF_8
                        );
                        System.out.println("[Scraper Debug] Saved timetable page HTML to 'scraped_timetable_debug.html'");
                    } catch (Exception ignored) {}

                    // Parse timetable grid
                    timetableResult = TimetableParser.parseTimetable(scheduleHtml, registry);
                    if (!timetableResult.isEmpty()) {
                        scheduleManager.setWeeklySchedule(timetableResult.getScheduleByDay());
                        loadedTimetableSessions = TimetableParser.populateTodaySchedule(timetableResult, scheduleManager, registry);
                        System.out.printf("[Scraper] Timetable parsed successfully! Found %d total weekly period slots across %d days.\n",
                                timetableResult.getTotalSessions(), timetableResult.getScheduleByDay().size());
                        System.out.printf("[Scraper] Loaded %d period(s) into today's Schedule Queue (CustomQueue FIFO)!\n",
                                loadedTimetableSessions);
                    } else {
                        System.out.println("[Scraper] ⚠️ Note: No timetable grid identified on schedule page. Preserved existing schedule.");
                    }
                } catch (Exception e) {
                    System.out.println("[Scraper] Timetable scrape notice: " + e.getMessage() + ". Existing schedule preserved.");
                }
            }

            if (loadedSubjectCount == 0 && (timetableResult == null || timetableResult.isEmpty())) {
                throw new IllegalStateException("Could not identify attendance or timetable records. Check 'scraped_attendance_debug.html' & 'scraped_portal_debug.html'.");
            }

            return new ScraperResult(loadedSubjectCount, loadedTimetableSessions, timetableResult);

        } finally {
            if (driver != null) {
                System.out.println("\n[Scraper] Closing browser session & discarding temporary tokens.");
                driver.quit();
            }
        }
    }

    /**
     * Direct WebElement fallback parser for non-standard dynamic DOM layouts.
     */
    private List<Subject> parseTableElements(WebElement table) {
        List<Subject> subjects = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.xpath("./td|./th"));
            List<String> cellTexts = new java.util.ArrayList<>();
            for (WebElement c : cells) {
                cellTexts.add(c.getText().trim());
            }
            if (cellTexts.size() >= 3) {
                Subject s = AttendanceTableParser.parseRowCells(cellTexts);
                if (s != null) {
                    subjects.add(s);
                }
            }
        }
        return subjects;
    }

    private WebDriver createDriver() {
        try {
            ChromeOptions options = new ChromeOptions();
            if (headless) {
                options.addArguments("--headless=new");
            }
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            return new ChromeDriver(options);
        } catch (Exception e) {
            System.out.println("[Scraper] ChromeDriver initialization note: " + e.getMessage() + ". Trying EdgeDriver...");
            EdgeOptions edgeOptions = new EdgeOptions();
            if (headless) {
                edgeOptions.addArguments("--headless=new");
            }
            edgeOptions.addArguments("--disable-gpu");
            edgeOptions.addArguments("--no-sandbox");
            edgeOptions.addArguments("--disable-dev-shm-usage");
            return new EdgeDriver(edgeOptions);
        }
    }
}
