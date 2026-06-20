package com.cropplanner.report;

import com.cropplanner.model.User;
import com.cropplanner.security.SessionUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final PdfReportService pdfReportService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/harvest")
    public ResponseEntity<byte[]> harvestReport(HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        byte[] pdf = pdfReportService.generateHarvestReport(user);
        return pdfResponse(pdf, "harvest-report.pdf");
    }

    @GetMapping("/expenses/monthly")
    public ResponseEntity<byte[]> monthlyExpenseReport(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        int y = (year  == 0) ? LocalDate.now().getYear()        : year;
        int m = (month == 0) ? LocalDate.now().getMonthValue()  : month;
        byte[] pdf = pdfReportService.generateMonthlyExpenseReport(user, y, m);
        return pdfResponse(pdf, "expenses-" + y + "-" + String.format("%02d", m) + ".pdf");
    }

    @GetMapping("/seasonal")
    public ResponseEntity<byte[]> seasonalReport(
            @RequestParam(defaultValue = "Summer") String season,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        byte[] pdf = pdfReportService.generateSeasonalReport(user, season);
        return pdfResponse(pdf, season.toLowerCase() + "-season-report.pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
