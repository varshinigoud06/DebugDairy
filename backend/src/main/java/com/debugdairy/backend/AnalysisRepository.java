
package com.debugdairy.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AnalysisRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalysisRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Save a new analysis
    public Long saveAnalysis(
            String language,
            String submittedCode,
            Map<String, String> analysis) {

        String sql = """
            INSERT INTO code_analyses
            (
                language,
                submitted_code,
                issue_title,
                explanation,
                what_went_wrong,
                where_is_the_mistake,
                why_it_happened,
                how_to_correct_it,
                corrected_code,
                category,
                concept_to_revise
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(
                sql,
                language,
                submittedCode,
                analysis.get("issueTitle"),
                analysis.get("whatWentWrong"),
                analysis.get("whatWentWrong"),
                analysis.get("whereIsTheMistake"),
                analysis.get("whyItHappened"),
                analysis.get("howToCorrectIt"),
                analysis.get("correctedCode"),
                analysis.get("category"),
                analysis.get("conceptToRevise")
        );

        return jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()", Long.class);
    }

    // Retrieve all saved analyses
    public List<Map<String, Object>> findAll() {
        String sql = """
            SELECT id, language, submitted_code, issue_title,
                   explanation, what_went_wrong,
                   where_is_the_mistake, why_it_happened,
                   how_to_correct_it, corrected_code, category,
                   concept_to_revise, created_at
            FROM code_analyses
            ORDER BY created_at DESC
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // Retrieve one analysis by ID
    public Map<String, Object> findById(Long id) {
        String sql = """
            SELECT id, language, submitted_code, issue_title,
                   explanation, what_went_wrong,
                   where_is_the_mistake, why_it_happened,
                   how_to_correct_it, corrected_code, category,
                   concept_to_revise, created_at
            FROM code_analyses
            WHERE id = ?
            """;

        List<Map<String, Object>> results =
                jdbcTemplate.queryForList(sql, id);

        return results.isEmpty() ? null : results.get(0);
    }

    // Delete an analysis by ID
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM code_analyses WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
