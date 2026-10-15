USE debugdairy;
SELECT DATABASE();
CREATE TABLE code_analyses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    language VARCHAR(50) NOT NULL,
    submitted_code LONGTEXT NOT NULL,
    issue_title VARCHAR(255),
    explanation TEXT,
    corrected_code LONGTEXT,
    category VARCHAR(100),
    concept_to_revise VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
desc code_analyses;
SELECT * FROM code_analyses ORDER BY id DESC LIMIT 5;

SELECT COUNT(*) AS total_records FROM code_analyses;
SELECT id, language, issue_title, category, created_at FROM code_analyses ORDER BY id DESC;
SELECT * FROM code_analyses ORDER BY id DESC;
ALTER TABLE code_analyses
ADD COLUMN what_went_wrong TEXT,
ADD COLUMN where_is_the_mistake TEXT,
ADD COLUMN why_it_happened TEXT,
ADD COLUMN how_to_correct_it TEXT;
SELECT COUNT(*) AS total_records FROM code_analyses;
SELECT id, language, issue_title, category, created_at FROM code_analyses
ORDER BY id DESC LIMIT 5;
SELECT COUNT(*) AS total_records FROM code_analyses;
SELECT id, language, issue_title FROM code_analyses
ORDER BY id DESC LIMIT 5;
SELECT * FROM code_analyses WHERE id = 31;
SELECT
    COUNT(*) AS total_rows,
    MIN(id) AS first_id,
    MAX(id) AS highest_id,
    COUNT(DISTINCT id) AS unique_ids
FROM code_analyses;
SELECT n.id AS missing_id
FROM (
    SELECT 1 AS id UNION ALL SELECT 2 UNION ALL SELECT 3
    UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
    UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
    UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18
    UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL SELECT 21
    UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24
    UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27
    UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
    UNION ALL SELECT 31
) n
LEFT JOIN code_analyses c ON c.id = n.id
WHERE c.id IS NULL;
SELECT DISTINCT language FROM code_analyses ORDER BY language;