package com.cropplanner.report;

import com.cropplanner.expense.ExpenseCategory;
import com.cropplanner.expense.ExpenseRepository;
import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.repository.ScheduleRepository;
import com.cropplanner.yieldprediction.YieldRecord;
import com.cropplanner.yieldprediction.YieldRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates PDF reports using MinimalPdfWriter — a small, dependency-free
 * PDF writer built on raw PDF syntax (see that class for why OpenPDF was
 * removed). Returns a byte[] so the controller can stream it as a download
 * without writing temp files to disk.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final ScheduleRepository scheduleRepository;
    private final YieldRecordRepository yieldRecordRepository;
    private final ExpenseRepository expenseRepository;

    // ── Harvest / Yield report ────────────────────────────────────────────────

    public byte[] generateHarvestReport(User user) {
        List<PlantingSchedule> schedules = scheduleRepository.findByUserAndDeletedAtIsNullOrderByPlantingDateDesc(user);
        List<YieldRecord> yields = yieldRecordRepository.findBySchedule_User(user);
        Map<Long, YieldRecord> yieldBySchedule = yields.stream()
                .collect(Collectors.toMap(y -> y.getSchedule().getId(), y -> y));

        MinimalPdfWriter pdf = new MinimalPdfWriter();
        pdf.title("Harvest Report — " + user.getFullName());
        pdf.subtitle("Generated: " + LocalDate.now().format(DATE_FMT));
        pdf.gap(10);

        String[] headers = {"Crop", "Planted", "Expected Harvest", "Status", "Predicted (kg)", "Actual (kg)"};
        List<String[]> rows = new ArrayList<>();
        for (PlantingSchedule s : schedules) {
            YieldRecord yr = yieldBySchedule.get(s.getId());
            rows.add(new String[]{
                    s.getCrop().getName(),
                    s.getPlantingDate().format(DATE_FMT),
                    s.getExpectedHarvestDate().format(DATE_FMT),
                    s.getStatus(),
                    yr != null ? String.format("%.0f", yr.getPredictedYieldKg()) : "-",
                    yr != null && yr.getActualYieldKg() != null ? String.format("%.0f", yr.getActualYieldKg()) : "-"
            });
        }
        pdf.table(headers, rows, new float[]{2.2f, 1.4f, 1.6f, 1.2f, 1.3f, 1.1f});

        log.info("Harvest report generated for {}", user.getEmail());
        return pdf.build();
    }

    // ── Monthly expense report ─────────────────────────────────────────────────

    public byte[] generateMonthlyExpenseReport(User user, int year, int month) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        List<ExpenseRepository.CategoryTotal> totals = expenseRepository.sumByCategory(user, from, to);

        MinimalPdfWriter pdf = new MinimalPdfWriter();
        pdf.title("Monthly Expense Report — " + Month.of(month).name() + " " + year);
        pdf.subtitle("Farmer: " + user.getFullName() + "  |  Generated: " + LocalDate.now().format(DATE_FMT));
        pdf.gap(10);

        String[] headers = {"Category", "Amount (ZAR)"};
        List<String[]> rows = new ArrayList<>();
        double grandTotal = 0;
        for (ExpenseRepository.CategoryTotal t : totals) {
            rows.add(new String[]{formatCategory(t.getCategory()), String.format("%.2f", t.getTotal())});
            grandTotal += t.getTotal();
        }
        rows.add(new String[]{"TOTAL", String.format("%.2f", grandTotal)});
        pdf.table(headers, rows, new float[]{2f, 1f});

        return pdf.build();
    }

    // ── Seasonal report ───────────────────────────────────────────────────────

    public byte[] generateSeasonalReport(User user, String season) {
        List<PlantingSchedule> schedules = scheduleRepository.findByUserAndDeletedAtIsNullOrderByPlantingDateDesc(user)
                .stream()
                .filter(s -> season.equalsIgnoreCase(s.getCrop().getSeason()))
                .toList();

        MinimalPdfWriter pdf = new MinimalPdfWriter();
        pdf.title(season + " Season Report — " + user.getFullName());
        pdf.subtitle("Generated: " + LocalDate.now().format(DATE_FMT));
        pdf.gap(6);

        long harvested = schedules.stream().filter(s -> "Harvested".equals(s.getStatus())).count();
        pdf.paragraph("Total schedules: " + schedules.size());
        pdf.paragraph("Harvested: " + harvested);
        pdf.gap(8);

        String[] headers = {"Crop", "Planted", "Expected Harvest", "Status"};
        List<String[]> rows = new ArrayList<>();
        for (PlantingSchedule s : schedules) {
            rows.add(new String[]{
                    s.getCrop().getName(),
                    s.getPlantingDate().format(DATE_FMT),
                    s.getExpectedHarvestDate().format(DATE_FMT),
                    s.getStatus()
            });
        }
        pdf.table(headers, rows, new float[]{2f, 1.5f, 1.5f, 1f});

        return pdf.build();
    }

    private String formatCategory(ExpenseCategory c) {
        return c.name().charAt(0) + c.name().substring(1).toLowerCase();
    }
}
