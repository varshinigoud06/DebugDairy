
package com.debugdairy.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
    "http://127.0.0.1:5500",
    "http://localhost:5500"
})
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final GroqService groqService;
    private final AnalysisRepository analysisRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisController(
            GroqService groqService,
            AnalysisRepository analysisRepository) {

        this.groqService = groqService;
        this.analysisRepository = analysisRepository;
    }

    // 1. Analyse code using Groq and save the result
    @PostMapping
    public Map<String, String> analyzeCode(
            @RequestBody Map<String, String> request) {

        String code = request.get("code");
        String language = request.get("language");

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Code cannot be empty");
        }

        if (language == null || language.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Programming language is required");
        }

        try {
            String aiResponse =
                    groqService.analyzeCode(code, language);

            Map<String, String> result = objectMapper.readValue(
                    aiResponse,
                    new TypeReference<Map<String, String>>() {}
            );

            String[] requiredFields = {
                "issueTitle",
                "whatWentWrong",
                "whereIsTheMistake",
                "whyItHappened",
                "howToCorrectIt",
                "correctedCode",
                "category",
                "conceptToRevise"
            };

            for (String field : requiredFields) {
                if (result.get(field) == null
                        || result.get(field).isBlank()) {
                    throw new IllegalStateException(
                            "AI response is missing field: " + field);
                }
            }

            Long id = analysisRepository.saveAnalysis(
                    language, code, result);

            result.put("id", String.valueOf(id));
            result.put("language", language);
            result.put("originalCode", code);

            return result;

        } catch (Exception e) {
            System.err.println(
                    "Analysis processing error: " + e.getMessage());

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to process or save the code analysis.");
        }
    }

    // 2. Retrieve all saved analyses
    @GetMapping
    public List<Map<String, Object>> getAllAnalyses() {
        return analysisRepository.findAll();
    }

    // 3. Retrieve one analysis by ID
    @GetMapping("/{id}")
    public Map<String, Object> getAnalysisById(
            @PathVariable Long id) {

        Map<String, Object> analysis =
                analysisRepository.findById(id);

        if (analysis == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Analysis not found");
        }

        return analysis;
    }

    // 4. Delete an analysis by ID
    @DeleteMapping("/{id}")
    public Map<String, String> deleteAnalysis(
            @PathVariable Long id) {

        boolean deleted = analysisRepository.deleteById(id);

        if (!deleted) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Analysis not found");
        }

        return Map.of(
                "message", "Analysis deleted successfully",
                "id", String.valueOf(id)
        );
    }

    // 5. Generate a programming concept lesson using Groq
    @PostMapping("/lesson")
    public Map<String, Object> generateConceptLesson(
            @RequestBody Map<String, String> request) {

        String concept = request.get("concept");
        String language = request.get("language");

        if (concept == null || concept.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Concept is required");
        }

        if (language == null || language.isBlank()) {
            language = "Java";
        }

        try {
            String aiResponse = groqService.generateConceptLesson(
                    concept.trim(), language.trim());

            // Parse JSON containing nested arrays and objects
            Map<String, Object> lesson = objectMapper.readValue(
                    aiResponse,
                    new TypeReference<Map<String, Object>>() {}
            );

            if (lesson.get("title") == null
                    || lesson.get("definition") == null
                    || lesson.get("wrong") == null
                    || lesson.get("correct") == null
                    || lesson.get("steps") == null
                    || lesson.get("question") == null
                    || lesson.get("options") == null
                    || lesson.get("answer") == null) {

                throw new IllegalStateException(
                        "AI lesson is missing required fields");
            }

            lesson.put("requestedConcept", concept.trim());
            lesson.put("language", language.trim());

            return lesson;

        } catch (Exception e) {
            System.err.println(
                    "Lesson generation error: " + e.getMessage());

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to generate the concept lesson. Please try again.");
        }
    }

    // 5. Retrieve learning history
    @GetMapping("/learning")
    public List<Map<String, Object>> getLearningHistory() {
        return analysisRepository.findAll();
    }
}
