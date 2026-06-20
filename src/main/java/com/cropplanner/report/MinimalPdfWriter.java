package com.cropplanner.report;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates simple single-page-per-call PDF documents using nothing but the
 * raw PDF file format (PDF is a plain-text-based format for basic content
 * like this — no external library is needed for monospaced text and lines).
 *
 * This replaces an OpenPDF-based implementation. OpenPDF consistently
 * failed to resolve in one deployment environment ("Unresolved dependency"
 * in Maven even though the artifact was confirmed valid and downloadable
 * directly via PowerShell) — rather than keep debugging an environment we
 * don't have direct access to, PDF generation was rewritten using only
 * java.io, so there's nothing left that can fail to download.
 *
 * Deliberately simple: one base-14 font (Helvetica, built into every PDF
 * reader, no embedding needed), left-aligned text lines, no word-wrap,
 * no images. This easily covers tabular reports like the ones this app
 * needs. If you outgrow this (e.g. need multi-page flowing text, images,
 * or complex layouts), reintroducing a real PDF library is the right call
 * — just make sure it resolves in your build environment first.
 *
 * PDF coordinate system: origin (0,0) is bottom-left of the page, y
 * increases upward. A4 in points: 595 x 842.
 */
class MinimalPdfWriter {

    static final float PAGE_WIDTH = 595f;
    static final float PAGE_HEIGHT = 842f;
    private static final float MARGIN = 40f;

    private final List<String> lines = new ArrayList<>();   // raw PDF content stream operators
    private float cursorY = PAGE_HEIGHT - MARGIN;

    // ── Public drawing API ──────────────────────────────────────────────────

    void title(String text) {
        text(text, 18, true);
        cursorY -= 6; // extra gap after a title
    }

    void subtitle(String text) {
        text(text, 11, false);
    }

    void paragraph(String text) {
        text(text, 10, false);
    }

    void gap(float points) {
        cursorY -= points;
    }

    void line() {
        lines.add(String.format("%.2f w 0 G %.2f %.2f m %.2f %.2f l S",
                0.5, MARGIN, cursorY, PAGE_WIDTH - MARGIN, cursorY));
        cursorY -= 10;
    }

    /**
     * Draws a simple table: a bold header row with a filled background,
     * followed by plain rows. Column widths are proportional to the
     * supplied weights and scaled to fill the printable width.
     */
    void table(String[] headers, List<String[]> rows, float[] columnWeights) {
        float printableWidth = PAGE_WIDTH - 2 * MARGIN;
        float totalWeight = 0;
        for (float w : columnWeights) totalWeight += w;

        float[] colWidths = new float[columnWeights.length];
        for (int i = 0; i < columnWeights.length; i++) {
            colWidths[i] = printableWidth * (columnWeights[i] / totalWeight);
        }

        float rowHeight = 18f;

        // Header row background (sea-green-ish fill) + text
        lines.add(String.format("%.3f %.3f %.3f rg %.2f %.2f %.2f %.2f re f",
                0.18f, 0.55f, 0.34f, MARGIN, cursorY - rowHeight, printableWidth, rowHeight));
        drawRow(headers, colWidths, rowHeight, true, true);
        cursorY -= rowHeight;

        boolean shade = false;
        for (String[] row : rows) {
            if (cursorY - rowHeight < MARGIN) {
                // Simple guard: this minimal writer is single-page. Truncate
                // gracefully rather than overflow off the page or throw.
                paragraph("… (additional rows omitted — report truncated to one page)");
                return;
            }
            if (shade) {
                lines.add(String.format("0.94 0.96 0.94 rg %.2f %.2f %.2f %.2f re f",
                        MARGIN, cursorY - rowHeight, printableWidth, rowHeight));
            }
            drawRow(row, colWidths, rowHeight, false, false);
            cursorY -= rowHeight;
            shade = !shade;
        }
        cursorY -= 6;
    }

    /** Renders the accumulated content as a complete, valid single-page PDF document. */
    byte[] build() {
        byte[] content = buildContentStream();
        return assemblePdf(content);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void text(String value, int fontSize, boolean bold) {
        String escaped = escape(value);
        lines.add((bold ? "/F2 " : "/F1 ") + fontSize + " Tf");
        lines.add(String.format("1 0 0 1 %.2f %.2f Tm", MARGIN, cursorY));
        lines.add("(" + escaped + ") Tj");
        cursorY -= (fontSize + 6);
    }

    private void drawRow(String[] values, float[] colWidths, float rowHeight, boolean header, boolean whiteText) {
        float x = MARGIN;
        float textY = cursorY - rowHeight + 5f;
        for (int i = 0; i < values.length; i++) {
            String val = values[i] != null ? values[i] : "";
            lines.add((header ? "/F2 9 Tf" : "/F1 9 Tf"));
            if (whiteText) {
                lines.add("1 1 1 rg 1 1 1 RG");
            } else {
                lines.add("0 0 0 rg 0 0 0 RG");
            }
            lines.add(String.format("1 0 0 1 %.2f %.2f Tm", x + 4, textY));
            lines.add("(" + escape(truncate(val, colWidths[i])) + ") Tj");
            x += colWidths[i];
        }
    }

    /** Rough character-width estimate to avoid text overrunning its column (no real font metrics needed for this). */
    private String truncate(String val, float colWidth) {
        int maxChars = Math.max(3, (int) (colWidth / 5.2f));
        return val.length() > maxChars ? val.substring(0, maxChars - 1) + "…" : val;
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    /**
     * Builds the actual content stream correctly: text operators wrapped in
     * BT/ET, but fill/stroke (rectangles, lines) issued outside that block,
     * exactly where they were added relative to the text — by tracking which
     * lines are graphics vs. text ops based on their PDF operator suffix.
     */
    private byte[] buildContentStream() {
        StringBuilder sb = new StringBuilder();
        boolean inText = false;
        for (String op : lines) {
            boolean isGraphics = op.endsWith(" f") || op.endsWith(" S") || op.endsWith(" re f");
            if (isGraphics) {
                if (inText) { sb.append("ET\n"); inText = false; }
                sb.append(op).append("\n");
            } else {
                if (!inText) { sb.append("BT\n"); inText = true; }
                sb.append(op).append("\n");
            }
        }
        if (inText) sb.append("ET\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Assembles a minimal, valid single-page PDF file (header, content
     * stream object, page/font/catalog objects, xref table, trailer) around
     * the given content stream bytes.
     */
    private byte[] assemblePdf(byte[] contentStreamBytes) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();

            write(out, "%PDF-1.4\n");

            // 1: Catalog
            offsets.add(out.size());
            write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            // 2: Pages
            offsets.add(out.size());
            write(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            // 3: Page
            offsets.add(out.size());
            write(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                    + PAGE_WIDTH + " " + PAGE_HEIGHT + "] "
                    + "/Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> "
                    + "/Contents 4 0 R >>\nendobj\n");

            // 4: Content stream
            offsets.add(out.size());
            write(out, "4 0 obj\n<< /Length " + contentStreamBytes.length + " >>\nstream\n");
            out.write(contentStreamBytes);
            write(out, "\nendstream\nendobj\n");

            // 5: Helvetica (regular)
            offsets.add(out.size());
            write(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

            // 6: Helvetica-Bold
            offsets.add(out.size());
            write(out, "6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

            int xrefStart = out.size();
            int objectCount = offsets.size() + 1; // +1 for the free-list head entry

            StringBuilder xref = new StringBuilder();
            xref.append("xref\n0 ").append(objectCount).append("\n");
            xref.append("0000000000 65535 f \n");
            for (int offset : offsets) {
                xref.append(String.format("%010d 00000 n \n", offset));
            }
            write(out, xref.toString());

            write(out, "trailer\n<< /Size " + objectCount + " /Root 1 0 R >>\n");
            write(out, "startxref\n" + xrefStart + "\n%%EOF");

            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to assemble PDF bytes", e);
        }
    }

    private void write(ByteArrayOutputStream out, String s) throws Exception {
        out.write(s.getBytes(StandardCharsets.US_ASCII));
    }
}
