package com.wikipedia.articles.services;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.font.FontProvider;
import com.wikipedia.articles.models.Article;
import com.wikipedia.articles.models.Statistic;
import com.wikipedia.articles.repositories.ArticleRepository;
import com.wikipedia.articles.repositories.StatisticRepository;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.BitmapEncoder;
import java.awt.image.BufferedImage;

/**
 * Service responsible for generating statistics related to articles and searches.
 * Responsibilities:
 * - Retrieving aggregated statistics and articles grouped by category
 * - Exporting statistics to a PDF file including charts and tables
 * - Preparing chart data for most searched keywords and top categories
 */
@Service
public class StatisticService {

    private final StatisticRepository statisticRepository;
    private final ArticleRepository articleRepository;

    /**
     * Constructor-based dependency injection.
     *
     * @param statisticRepository repository for managing search statistics
     * @param articleRepository   repository for managing articles
     */
    public StatisticService(StatisticRepository statisticRepository, ArticleRepository articleRepository) {
        this.statisticRepository = statisticRepository;
        this.articleRepository = articleRepository;
    }

    /**
     * Retrieves statistics and articles grouped by category in JSON format.
     *
     * @return a map containing:
     * - "categories": articles grouped by category
     * - "statistics": all search statistics ordered by counter
     */
    public Map<String, Object> getStatisticsJson() {
        List<Statistic> statistics = statisticRepository.findAllByOrderByCounterDesc();
        List<Article> articles = articleRepository.findAll();

        Map<String, List<Article>> groupedArticles =
                articles.stream().collect(Collectors.groupingBy(a -> a.getCategory().getTitle()));

        Map<String, Object> response = new HashMap<>();
        response.put("categories", groupedArticles);
        response.put("statistics", statistics);

        return response;
    }

    /**
     * Exports statistics to a PDF file.
     * PDF includes:
     * - Front page with title and date
     * - Articles grouped by category
     * - Grades and comments
     * - Pie chart of top categories
     * - Table of most searched keywords
     * - Pie chart of most searched keywords
     *
     * @return byte array representing the generated PDF
     * @throws RuntimeException if PDF generation fails
     */
    public byte[] exportStatisticsPdf() {
        List<Statistic> statistics = statisticRepository.findAllByOrderByCounterDesc();
        List<Article> articles = articleRepository.findAll();

        Map<String, List<Article>> groupedArticles =
                articles.stream().collect(Collectors.groupingBy(a -> a.getCategory().getTitle()));

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            StringBuilder html = new StringBuilder();
            html.append("<html><head>");
            html.append("<style>")
                    .append(".front-page { height:842pt; display:flex; flex-direction:column; page-break-after: always; text-align:center; }")
                    .append(".front-page .center { flex:1; display:flex; flex-direction:column; justify-content:center; align-items:center; }")
                    .append(".front-page h1 { font-size:32pt; margin:0; }")
                    .append(".front-page h3 { font-size:16pt; margin:10pt 0 0 0; }")
                    .append(".category { font-size:16pt; font-weight:bold; background-color:#f0f0f0; padding:5px; margin-top:20px; }")
                    .append(".article { margin-left:20px; margin-bottom:10px; padding:5px; background-color:#fafafa; }")
                    .append(".grade { color: gold; }")
                    .append(".statistics { page-break-before: always; margin-top:20pt; }")
                    .append("table { width:100%; border-collapse: collapse; margin-top:10px; }")
                    .append("th, td { border:1px solid #ccc; padding:5px; }")
                    .append("th { background-color:#e0e0e0; }")
                    .append("</style>");
            html.append("</head><body>");

            html.append("<div class='front-page'>")
                    .append("<div class='center'>")
                    .append("<h1>Statistics of Wikipedia Articles</h1>")
                    .append("<h3>").append(today).append("</h3>")
                    .append("</div></div>");

            html.append("<h2>Articles by Category</h2>");
            for (Map.Entry<String, List<Article>> entry : groupedArticles.entrySet()) {
                html.append("<div class='category'>")
                        .append(entry.getKey())
                        .append(" (")
                        .append(entry.getValue().size())
                        .append(entry.getValue().size() == 1 ? " article" : " articles")
                        .append(")")
                        .append("</div>");

                for (Article article : entry.getValue()) {
                    String stars = "";
                    if (article.getGrade() != null && article.getGrade() > 0) {
                        stars = "★".repeat(article.getGrade()) + "☆".repeat(5 - article.getGrade());
                    }
                    html.append("<div class='article'>")
                            .append("<b>Title:</b> ").append(article.getTitle()).append("<br>")
                            .append("<b>Comments:</b> ").append(article.getComments().isEmpty() ? "—" : article.getComments()).append("<br>")
                            .append("<b>Grade:</b> <span class='grade'>").append(stars).append("</span>")
                            .append("</div>");
                }
            }

            Map<String, Integer> categoryChartData = buildTopCategoriesChartData(groupedArticles);

            if (!categoryChartData.isEmpty()) {

                PieChart categoryPieChart = new PieChartBuilder()
                        .width(600)
                        .height(400)
                        .title("Most Popular Categories Graph")
                        .build();

                categoryPieChart.getStyler().setLegendVisible(true);
                categoryPieChart.getStyler().setPlotContentSize(0.7);
                categoryPieChart.getStyler().setCircular(true);

                int totalCategories = categoryChartData.values()
                        .stream()
                        .mapToInt(Integer::intValue)
                        .sum();

                categoryChartData.forEach((key, value) -> {
                    double percent = (value * 100.0) / totalCategories;
                    categoryPieChart.addSeries(
                            key + " (" + String.format("%.1f%%", percent) + ")",
                            value
                    );
                });

                BufferedImage categoryChartImage = BitmapEncoder.getBufferedImage(categoryPieChart);
                ByteArrayOutputStream categoryChartBaos = new ByteArrayOutputStream();
                javax.imageio.ImageIO.write(categoryChartImage, "png", categoryChartBaos);
                String base64CategoryChart =
                        Base64.getEncoder().encodeToString(categoryChartBaos.toByteArray());

                html.append("<div style='text-align:center; margin-top:40px; page-break-before: always;'>")
                        .append("<h2>Most Popular Categories Graph</h2>")
                        .append("<img src='data:image/png;base64,")
                        .append(base64CategoryChart)
                        .append("' alt='Category Pie Chart' style='max-width:100%; height:auto;'/>")
                        .append("</div>");
            }

            html.append("<div class='statistics'>")
                    .append("<h2>Most searched keywords</h2>")
                    .append("<table><tr><th>Keyword</th><th>Times Used</th></tr>");
            for (Statistic stat : statistics) {
                html.append("<tr>")
                        .append("<td>").append(stat.getWord()).append("</td>")
                        .append("<td>").append(stat.getCounter()).append("</td>")
                        .append("</tr>");
            }
            html.append("</table>");

            if (!statistics.isEmpty()) {
                Map<String, Integer> chartData = buildTopKeywordsChartData(statistics);

                PieChart pieChart = new PieChartBuilder()
                        .width(600)
                        .height(400)
                        .title("Most Searched Keywords Graph")
                        .build();

                pieChart.getStyler().setLegendVisible(true);
                pieChart.getStyler().setPlotContentSize(0.7);
                pieChart.getStyler().setCircular(true);

                int total = chartData.values().stream().mapToInt(Integer::intValue).sum();

                chartData.forEach((key, value) -> {
                    double percent = (value * 100.0) / total;
                    pieChart.addSeries(key + " (" + String.format("%.1f%%", percent) + ")", value);
                });

                BufferedImage chartImage = BitmapEncoder.getBufferedImage(pieChart);
                ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
                javax.imageio.ImageIO.write(chartImage, "png", chartBaos);
                String base64Chart = Base64.getEncoder().encodeToString(chartBaos.toByteArray());

                html.append("<div style='text-align:center; margin-top:20px; page-break-before: always;'>")
                        .append("<h2>Most Searched Keywords Graph</h2>")
                        .append("<img src='data:image/png;base64,")
                        .append(base64Chart)
                        .append("' alt='Keyword Pie Chart' style='max-width:100%; height:auto;'/>")
                        .append("</div>");
            }


            html.append("</div>");
            html.append("</body></html>");

            ConverterProperties props = new ConverterProperties();
            FontProvider fontProvider = new FontProvider();
            fontProvider.addStandardPdfFonts();
            fontProvider.addSystemFonts();
            props.setFontProvider(fontProvider);

            HtmlConverter.convertToPdf(html.toString(), baos, props);

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    /**
     * Builds chart data for the most searched keywords.
     * Logic:
     * - Includes top 5 keywords by count
     * - If more keywords have the same count as the 5th, include them as well
     * - Remaining keywords are grouped into "Others"
     *
     * @param statistics list of all statistics
     * @return map of keyword to its count for charting
     */
    private Map<String, Integer> buildTopKeywordsChartData(List<Statistic> statistics) {
        Map<String, Integer> chartData = new LinkedHashMap<>();
        if (statistics == null || statistics.isEmpty()) {
            return chartData;
        }

        List<Statistic> sortedStats = statistics.stream()
                .sorted(Comparator.comparingInt(Statistic::getCounter).reversed())
                .toList();

        int countIncluded = 0;
        int thresholdCount = -1;
        int othersSum = 0;

        for (Statistic stat : sortedStats) {
            if (countIncluded < 5) {
                chartData.put(stat.getWord(), stat.getCounter());
                thresholdCount = stat.getCounter();
                countIncluded++;
            } else {
                if (stat.getCounter() == thresholdCount) {
                    chartData.put(stat.getWord(), stat.getCounter());
                } else {
                    othersSum += stat.getCounter();
                }
            }
        }

        if (othersSum > 0) {
            chartData.put("Others", othersSum);
        }

        return chartData;
    }

    /**
     * Builds chart data for the most popular categories.
     * Logic:
     * - Includes top 5 categories by article count
     * - Categories with same count as the 5th are also included
     * - Remaining categories are grouped into "Others"
     *
     * @param groupedArticles map of category name to list of articles
     * @return map of category to its article count for charting
     */
    private Map<String, Integer> buildTopCategoriesChartData(Map<String, List<Article>> groupedArticles) {

        Map<String, Integer> chartData = new LinkedHashMap<>();

        if (groupedArticles == null || groupedArticles.isEmpty()) {
            return chartData;
        }

        List<Map.Entry<String, List<Article>>> sortedCategories =
                groupedArticles.entrySet().stream()
                        .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                        .toList();

        int countIncluded = 0;
        int thresholdCount = -1;
        int othersSum = 0;

        for (Map.Entry<String, List<Article>> entry : sortedCategories) {
            int articleCount = entry.getValue().size();

            if (countIncluded < 5) {
                chartData.put(entry.getKey(), articleCount);
                thresholdCount = articleCount;
                countIncluded++;
            } else {
                if (articleCount == thresholdCount) {
                    chartData.put(entry.getKey(), articleCount);
                } else {
                    othersSum += articleCount;
                }
            }
        }

        if (othersSum > 0) {
            chartData.put("Others", othersSum);
        }

        return chartData;
    }
}


