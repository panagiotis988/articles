package com.wikipedia.articles.controllers;

import com.wikipedia.articles.services.StatisticService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

/**
 * REST controller responsible for handling statistics-related operations.
 * Provides endpoints for:
 * - Retrieving statistics data in JSON format
 * - Exporting statistics as a downloadable PDF file
 * Base path: /api
 */
@RestController
@RequestMapping("/api")
public class StatisticController {

    private final StatisticService statisticService;

    /**
     * Constructor-based dependency injection for StatisticService.
     *
     * @param statisticService service layer responsible for generating statistics data
     */
    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    /**
     * Exports statistics data as a PDF document.
     * The generated file is returned as a downloadable attachment
     * with a timestamp-based filename.
     *
     * @return ResponseEntity containing the generated PDF file bytes
     *         with Content-Disposition header set for download
     */
    @GetMapping("/statistics/export-pdf")
    public ResponseEntity<byte[]> exportPdf() {
        byte[] pdfBytes = statisticService.exportStatisticsPdf();

        long timestamp = Instant.now().getEpochSecond();
        String filename = "statistics_" + timestamp + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Retrieves statistics data in JSON format.
     * This endpoint is typically used by the frontend
     * to display categorized article statistics.
     *
     * @return ResponseEntity containing statistics data as a JSON map
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatisticsJson() {
        Map<String, Object> response = statisticService.getStatisticsJson();
        return ResponseEntity.ok(response);
    }
}
