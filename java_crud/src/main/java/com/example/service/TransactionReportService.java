package com.example.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.util.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;

@Service
@RequiredArgsConstructor
public class TransactionReportService {
    private final JdbcTemplate jdbcTemplate;

    public byte[] generateSpendingReport(UUID userId, int month, int year, String format) {
        String sql = """
                    SELECT t.date, t.category, t.description, t.amount, a.name AS account_name
                    FROM transactions t
                    LEFT JOIN accounts a ON t.account_id = a.id
                    WHERE t.user_id = ? AND t.type = 'EXPENSE'
                      AND MONTH(t.date) = ? AND YEAR(t.date) = ?
                    ORDER BY t.date
                """;

        List<Map<String, Object>> transactions = jdbcTemplate.queryForList(sql, userId.toString(), month, year);

        if (format.equalsIgnoreCase("pdf")) {
            return exportToPDF(transactions, month, year);
        } else {
            throw new UnsupportedOperationException("Only PDF supported currently");
        }
    }

    private byte[] exportToPDF(List<Map<String, Object>> transactions, int month, int year) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            String fontPath = "src/main/resources/fonts/Roboto-Regular.ttf";
            BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(baseFont, 12);

            document.add(new Paragraph("Báo cáo chi tiêu tháng " + month + "/" + year, font));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidths(new float[] { 4, 4, 4, 4, 4 });
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new Paragraph("Ngày", font)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new Paragraph("Danh mục", font)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new Paragraph("Mô tả", font)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new Paragraph("Số tiền", font)));
            table.addCell(new com.itextpdf.text.pdf.PdfPCell(new Paragraph("Nguồn giao dịch", font)));

            double total = 0;

            for (Map<String, Object> t : transactions) {
                String accountName = t.get("account_name") != null ? t.get("account_name").toString() : "N/A";
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new Paragraph(t.get("date").toString(), font)));
                table.addCell(
                        new com.itextpdf.text.pdf.PdfPCell(new Paragraph(String.valueOf(t.get("category")), font)));
                table.addCell(
                        new com.itextpdf.text.pdf.PdfPCell(new Paragraph(String.valueOf(t.get("description")), font)));
                double amount = ((Number) t.get("amount")).doubleValue();
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new Paragraph(String.format("%,.0f", amount), font)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new Paragraph(accountName, font)));
                total += amount;
            }

            document.add(table);
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Tổng chi tiêu: " + String.format("%,.0f VND", total), font));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
