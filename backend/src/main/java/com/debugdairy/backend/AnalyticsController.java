
package com.debugdairy.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
    "http://127.0.0.1:5500",
    "http://localhost:5500"
})
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. Get total number of code analyses
    @GetMapping("/summary")
    public Map<String, Object> getSummary() {

        String sql = """
            SELECT
                COUNT(*) AS totalAnalyses,
                COUNT(DISTINCT language) AS totalLanguages
            FROM code_analyses
            """;

        return jdbcTemplate.queryForMap(sql);
    }

    // 2. Get analysis counts grouped by error category
    @GetMapping("/categories")
    public List<Map<String, Object>> getCategoryStats() {

        String sql = """
            SELECT category, COUNT(*) AS total
            FROM code_analyses
            GROUP BY category
            ORDER BY total DESC
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // 3. Get analysis counts grouped by programming language
    @GetMapping("/languages")
    public List<Map<String, Object>> getLanguageStats() {

        String sql = """
            SELECT language, COUNT(*) AS total
            FROM code_analyses
            GROUP BY language
            ORDER BY total DESC
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // 4. Get the 10 most recent analyses
    @GetMapping("/recent")
    public List<Map<String, Object>> getRecentAnalyses() {

        String sql = """
            SELECT id, language, issue_title, category, created_at
            FROM code_analyses
            ORDER BY created_at DESC
            LIMIT 10
            """;

        return jdbcTemplate.queryForList(sql);
    }
}
