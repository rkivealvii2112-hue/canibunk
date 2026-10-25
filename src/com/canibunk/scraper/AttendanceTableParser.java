package com.canibunk.scraper;

import com.canibunk.ds.CustomHashMap;
import com.canibunk.model.Subject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Robust HTML table parser that extracts Subject records from college attendance tables.
 * Supports both vertical row-per-subject tables and horizontal column-per-subject tables (e.g. AES portal).
 */
public class AttendanceTableParser {

    private static final Pattern FRACTION_PATTERN = Pattern.compile("(\\d+)\\s*[/\\\\]\\s*(\\d+)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b(\\d{1,3})\\b");
    private static final Pattern CODE_PATTERN = Pattern.compile("^(?:\\d{2})?[A-Z]{2,4}\\s*\\d{3}[A-Z]?$");

    private static final Map<String, String> KNOWN_NAMES = new HashMap<>();
    static {
        // 2024 Scheme KTU / Amal Jyothi S3 CSE Subjects (from student portal)
        KNOWN_NAMES.put("24MAT205", "Number Theory, Transforms and Queueing Theory");
        KNOWN_NAMES.put("MAT205", "Number Theory, Transforms and Queueing Theory");
        KNOWN_NAMES.put("24CST201", "Data Structures and Algorithms");
        KNOWN_NAMES.put("CST201", "Data Structures and Algorithms");
        KNOWN_NAMES.put("24CST203", "Object Oriented Programming-Java");
        KNOWN_NAMES.put("CST203", "Object Oriented Programming-Java");
        KNOWN_NAMES.put("24CST205", "Computer Organization and Architecture");
        KNOWN_NAMES.put("CST205", "Computer Organization and Architecture");
        KNOWN_NAMES.put("24MNC201", "Essentials of Office Automation");
        KNOWN_NAMES.put("MNC201", "Essentials of Office Automation");
        KNOWN_NAMES.put("24CSL201", "Data Structures Lab");
        KNOWN_NAMES.put("CSL201", "Data Structures Lab");
        KNOWN_NAMES.put("24CSL203", "Object Oriented Programming-Java Lab");
        KNOWN_NAMES.put("CSL203", "Object Oriented Programming-Java Lab");
        KNOWN_NAMES.put("24HUT210", "Universal Human Values - II");
        KNOWN_NAMES.put("HUT210", "Universal Human Values - II");

        // Legacy / Fallback mappings
        KNOWN_NAMES.put("EST200", "Design & Engineering");
        KNOWN_NAMES.put("CS201", "Discrete Mathematics");
        KNOWN_NAMES.put("CS203", "Data Structures & Algorithms");
        KNOWN_NAMES.put("CS205", "Object Oriented Programming (Java)");
        KNOWN_NAMES.put("CS207", "Digital Electronics & Logic Design");
    }

    /**
     * Resolves a human-readable course title from a course code.
     */
    public static String resolveSubjectName(String code) {
        if (code == null || code.trim().isEmpty()) return "Unknown Subject";
        String clean = code.trim().replaceAll("\\s+", "").toUpperCase();
        if (KNOWN_NAMES.containsKey(clean)) {
            return KNOWN_NAMES.get(clean);
        }
        if (clean.matches("^\\d{2}[A-Z].*")) {
            String stripped = clean.substring(2);
            if (KNOWN_NAMES.containsKey(stripped)) {
                return KNOWN_NAMES.get(stripped);
            }
        }
        return code;
    }

    /**
     * Tests if a cell contains a valid college subject/course code (e.g., 24MAT205, CST201, CS203).
     */
    public static boolean isSubjectCode(String text) {
        if (text == null) return false;
        String clean = text.replaceAll("\\s+", "").toUpperCase();
        if (clean.length() < 4 || clean.length() > 12) return false;
        if (clean.matches("^(?:SEMESTER|SLNO|DATE|STATUS|ACTION|PERCENT|TOTAL|ATTENDANCE|REPORT|DETAILS|SUBJECT|COURSE|THEORY|PRACTICAL)$")) {
            return false;
        }
        if (CODE_PATTERN.matcher(clean).matches()) return true;
        return clean.matches("^(?:\\d{2})?[A-Z]{2,5}\\d{2,4}[A-Z0-9]?$");
    }

    /**
     * Parses an HTML string (whole page or table block) into a list of Subject objects.
     */
    public static List<Subject> parseHtmlTable(String htmlContent) {
        List<Subject> subjects = new ArrayList<>();
        if (htmlContent == null || htmlContent.isEmpty()) {
            return subjects;
        }

        // If htmlContent contains multiple <table> tags, inspect each table individually
        Pattern tablePattern = Pattern.compile("(?is)<table[^>]*>(.*?)</table>");
        Matcher tableMatcher = tablePattern.matcher(htmlContent);
        List<String> tableBlocks = new ArrayList<>();
        while (tableMatcher.find()) {
            tableBlocks.add(tableMatcher.group(0));
        }

        if (tableBlocks.isEmpty()) {
            tableBlocks.add(htmlContent);
        }

        List<Subject> bestSubjects = new ArrayList<>();

        for (String block : tableBlocks) {
            List<Subject> parsed = parseSingleTableBlock(block);
            if (parsed.size() > bestSubjects.size()) {
                bestSubjects = parsed;
            }
        }

        return bestSubjects;
    }

    /**
     * Parses a single table block, detecting whether it is formatted horizontally or vertically.
     */
    private static List<Subject> parseSingleTableBlock(String tableHtml) {
        Pattern rowPattern = Pattern.compile("(?is)<tr[^>]*>(.*?)</tr>");
        Matcher rowMatcher = rowPattern.matcher(tableHtml);

        List<List<String>> allRows = new ArrayList<>();
        while (rowMatcher.find()) {
            List<String> cells = extractCells(rowMatcher.group(1));
            if (!cells.isEmpty()) {
                allRows.add(cells);
            }
        }

        if (allRows.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. First, check if this is a HORIZONTAL table (Course Codes across row 1, percentages/hours across row 2)
        List<Subject> horizontalSubjects = parseHorizontalTable(allRows);
        if (!horizontalSubjects.isEmpty()) {
            return horizontalSubjects;
        }

        // 2. Otherwise, parse as a traditional VERTICAL table (one subject per row)
        return parseVerticalTable(allRows);
    }

    /**
     * Parses horizontal tables where course codes are headers across columns and data rows follow.
     */
    public static List<Subject> parseHorizontalTable(List<List<String>> allRows) {
        List<Subject> subjects = new ArrayList<>();
        if (allRows == null || allRows.size() < 2) {
            return subjects;
        }

        // Search for the row containing subject codes
        int codeRowIdx = -1;
        List<String> codeRow = null;

        for (int r = 0; r < allRows.size(); r++) {
            List<String> row = allRows.get(r);
            int codeCount = 0;
            for (String cell : row) {
                if (isSubjectCode(cell)) {
                    codeCount++;
                }
            }
            if (codeCount >= 2 && codeCount >= Math.max(2, (row.size() / 2))) {
                codeRowIdx = r;
                codeRow = row;
                break;
            }
        }

        if (codeRow == null || codeRowIdx + 1 >= allRows.size()) {
            return subjects;
        }

        // Search subsequent rows for numbers or percentages corresponding to these codes
        for (int valRowIdx = codeRowIdx + 1; valRowIdx < allRows.size(); valRowIdx++) {
            List<String> valRow = allRows.get(valRowIdx);
            int validValCount = 0;

            for (int c = 0; c < Math.min(codeRow.size(), valRow.size()); c++) {
                String codeCandidate = codeRow.get(c);
                if (!isSubjectCode(codeCandidate)) continue;
                String valCell = valRow.get(c);
                if (hasValidAttendanceValue(valCell)) {
                    validValCount++;
                }
            }

            if (validValCount >= 2) {
                for (int c = 0; c < Math.min(codeRow.size(), valRow.size()); c++) {
                    String rawCode = codeRow.get(c);
                    if (!isSubjectCode(rawCode)) continue;
                    String code = rawCode.replaceAll("\\s+", "").toUpperCase();

                    String valCell = valRow.get(c);
                    int attended = -1;
                    int total = -1;

                    // A. Check for fraction (e.g. "24/26")
                    Matcher fracMatcher = FRACTION_PATTERN.matcher(valCell);
                    if (fracMatcher.find()) {
                        attended = Integer.parseInt(fracMatcher.group(1));
                        total = Integer.parseInt(fracMatcher.group(2));
                    } else {
                        // B. Check for percentage or integer (e.g. 91, 91%, 91.5)
                        Matcher numMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(valCell);
                        if (numMatcher.find()) {
                            try {
                                double pct = Double.parseDouble(numMatcher.group(1));
                                if (pct >= 0 && pct <= 100) {
                                    if (Math.abs(pct - Math.round(pct)) < 0.001) {
                                        total = 100;
                                        attended = (int) Math.round(pct);
                                    } else {
                                        total = 1000;
                                        attended = (int) Math.round(pct * 10.0);
                                    }
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    if (attended >= 0 && total > 0 && attended <= total) {
                        String name = resolveSubjectName(code);
                        subjects.add(new Subject(code, name, attended, total));
                    }
                }

                if (!subjects.isEmpty()) {
                    return subjects;
                }
            }
        }

        return subjects;
    }

    private static boolean hasValidAttendanceValue(String cell) {
        if (cell == null || cell.trim().isEmpty()) return false;
        if (FRACTION_PATTERN.matcher(cell).find()) return true;
        Matcher m = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(cell);
        if (m.find()) {
            try {
                double v = Double.parseDouble(m.group(1));
                return v >= 0 && v <= 100;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Parses standard vertical tables (one subject per row).
     */
    private static List<Subject> parseVerticalTable(List<List<String>> allRows) {
        List<Subject> subjects = new ArrayList<>();
        int attendedCol = -1;
        int attendedWithDlCol = -1;
        int totalCol = -1;
        int codeCol = -1;
        int nameCol = -1;

        for (List<String> cells : allRows) {
            if (cells.isEmpty()) continue;

            // Check if this is a header row to map column indices
            if (isHeaderRow(cells)) {
                for (int i = 0; i < cells.size(); i++) {
                    String h = cells.get(i).toLowerCase().trim().replaceAll("\\s+", " ");
                    if (h.contains("code") || h.contains("course id")) {
                        codeCol = i;
                    } else if (h.contains("title") || h.contains("name") || h.contains("subject")) {
                        nameCol = i;
                    } else if (h.contains("ah+dl") || h.contains("ah + dl") || h.contains("with dl")) {
                        if (!h.contains("%")) {
                            attendedWithDlCol = i;
                        }
                    } else if (h.equals("ah") || h.startsWith("ah ") || h.contains("(ah)") || (h.contains("attended") && !h.contains("%"))) {
                        attendedCol = i;
                    } else if (h.equals("th") || h.startsWith("th ") || h.contains("(th)") || h.contains("conducted") || h.contains("total hour") || h.contains("classes held") || h.contains("total classes")) {
                        totalCol = i;
                    }
                }
                continue;
            }

            if (cells.size() < 3) {
                continue;
            }

            // Prefer AH+DL (attended including duty leave) as official attendance figure
            int effectiveAttendedCol = (attendedWithDlCol >= 0) ? attendedWithDlCol : attendedCol;
            Subject subject = parseRowCells(cells, codeCol, nameCol, effectiveAttendedCol, totalCol);
            if (subject != null) {
                subjects.add(subject);
            }
        }

        return subjects;
    }

    public static Subject parseRowCells(List<String> cells) {
        return parseRowCells(cells, -1, -1, -1, -1);
    }

    public static Subject parseRowCells(List<String> cells, int codeCol, int nameCol, int attendedCol, int totalCol) {
        String code = null;
        String name = null;
        int attended = -1;
        int total = -1;
        int codeIndex = -1;

        // 1. Identify Subject Code
        if (codeCol >= 0 && codeCol < cells.size()) {
            String raw = cells.get(codeCol).trim().replaceAll("\\s+", "");
            if (raw.matches("^(?:\\d{2})?[A-Z0-9_-]{4,10}$")) {
                code = raw;
                codeIndex = codeCol;
            }
        }

        if (code == null) {
            for (int i = 0; i < cells.size(); i++) {
                String text = cells.get(i).trim();
                String normalizedCode = text.replaceAll("\\s+", "");
                if (isSubjectCode(normalizedCode)) {
                    code = normalizedCode;
                    codeIndex = i;
                    break;
                }
            }
        }

        // Fallback for code
        if (code == null) {
            for (int i = 0; i < cells.size(); i++) {
                String cell = cells.get(i).trim();
                if (!cell.matches("\\d+") && cell.matches("^(?:\\d{2})?[A-Z0-9]{4,10}$")) {
                    code = cell;
                    codeIndex = i;
                    break;
                }
            }
        }

        // 2. Identify Subject Name
        if (nameCol >= 0 && nameCol < cells.size()) {
            name = cells.get(nameCol);
        } else if (codeIndex >= 0 && codeIndex + 1 < cells.size() && cells.get(codeIndex + 1).length() > 2) {
            name = cells.get(codeIndex + 1);
        } else if (codeIndex > 0 && cells.get(codeIndex - 1).length() > 4) {
            name = cells.get(codeIndex - 1);
        }

        // 3. Extract Attended & Total
        // A. Check for fraction (e.g. "24 / 26" or "19/24") anywhere in the row
        int startIdx = (codeIndex >= 0) ? codeIndex + 1 : 0;
        for (int i = startIdx; i < cells.size(); i++) {
            String cell = cells.get(i);
            Matcher fracMatcher = FRACTION_PATTERN.matcher(cell);
            if (fracMatcher.find()) {
                attended = Integer.parseInt(fracMatcher.group(1));
                total = Integer.parseInt(fracMatcher.group(2));
                break;
            }
        }

        // B. Direct column mapping from header (if no fraction was found)
        if ((attended == -1 || total == -1) && attendedCol >= 0 && totalCol >= 0 && attendedCol < cells.size() && totalCol < cells.size()) {
            try {
                String aStr = cells.get(attendedCol).replaceAll("[^0-9]", "").trim();
                String tStr = cells.get(totalCol).replaceAll("[^0-9]", "").trim();
                if (!aStr.isEmpty() && !tStr.isEmpty()) {
                    attended = Integer.parseInt(aStr);
                    total = Integer.parseInt(tStr);
                }
            } catch (Exception ignored) {}
        }

        // C. Heuristic for AES numbers (e.g. TH=36, AH=32, DL=2, AH+DL=34)
        if (attended == -1 || total == -1) {
            List<Integer> numbers = new ArrayList<>();
            for (int i = startIdx; i < cells.size(); i++) {
                String cell = cells.get(i);
                if (cell.contains("%") || cell.contains(".")) continue;
                Matcher numMatcher = NUMBER_PATTERN.matcher(cell);
                if (numMatcher.find()) {
                    try {
                        numbers.add(Integer.parseInt(numMatcher.group(1)));
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (numbers.size() >= 2) {
                int n1 = numbers.get(0);
                int n2 = numbers.get(1);
                int maxVal = Math.max(n1, n2);
                int minVal = Math.min(n1, n2);

                if (numbers.size() >= 4) {
                    int ahPlusDl = numbers.get(3);
                    if (ahPlusDl <= maxVal && ahPlusDl >= minVal) {
                        minVal = ahPlusDl;
                    }
                }
                attended = minVal;
                total = maxVal;
            }
        }

        // Final validations
        if (code != null && attended >= 0 && total > 0 && attended <= total) {
            if (name == null || name.isEmpty() || name.equalsIgnoreCase(code)) {
                name = resolveSubjectName(code);
            }
            return new Subject(code, name, attended, total);
        }

        return null;
    }

    public static int populateRegistry(List<Subject> subjects, CustomHashMap<String, Subject> registry) {
        return populateRegistry(subjects, registry, false);
    }

    public static int populateRegistry(List<Subject> subjects, CustomHashMap<String, Subject> registry, boolean clearExisting) {
        if (clearExisting && registry != null) {
            registry.clear();
        }
        int count = 0;
        for (Subject s : subjects) {
            registry.put(s.getSubjectCode(), s);
            count++;
        }
        return count;
    }

    private static List<String> extractCells(String rowHtml) {
        List<String> cells = new ArrayList<>();
        Pattern cellPattern = Pattern.compile("(?is)<(?:td|th)[^>]*>(.*?)</(?:td|th)>");
        Matcher matcher = cellPattern.matcher(rowHtml);
        while (matcher.find()) {
            String cleanText = cleanHtmlTags(matcher.group(1)).trim();
            cells.add(cleanText);
        }
        return cells;
    }

    private static boolean isHeaderRow(List<String> cells) {
        String combined = String.join(" ", cells).toLowerCase();
        boolean hasCourseIdentifier = combined.contains("subject") || combined.contains("course") || combined.contains("code") || combined.contains("title");
        boolean hasAttendanceMetric = combined.contains("th") || combined.contains("ah") || combined.contains("dl") 
                || combined.contains("attended") || combined.contains("conducted") || combined.contains("total")
                || combined.contains("%") || combined.contains("percent");
        return hasCourseIdentifier && hasAttendanceMetric;
    }

    private static String cleanHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ")
                   .replaceAll("&nbsp;", " ")
                   .replaceAll("&amp;", "&")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
}

