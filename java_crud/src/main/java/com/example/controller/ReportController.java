package com.example.controller;

import com.example.entity.ReportSummary;
import com.example.repository.ReportDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportDAO reportDAO;

    @GetMapping("/summary")
    public ResponseEntity<ReportSummary> getReportSummary(
            @RequestParam UUID userId,
            @RequestParam int month,
            @RequestParam int year) {
        ReportSummary summary = reportDAO.calculateMonthlyReport(userId, month, year);
        return ResponseEntity.ok(summary);
    }
}
