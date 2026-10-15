
package com.debugdairy.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();

    // 1. Analyze submitted code
    public String analyzeCode(String code, String language) {

        Map<String, Object> requestBody = Map.of(
            "model", "openai/gpt-oss-20b",
            "messages", List.of(
                Map.of(
                    "role", "system",
                    "content", """
                        You are DebugDairy, a careful and beginner-friendly
                        programming code reviewer.

                        Analyze the submitted code in the given language.

                        Return ONLY one valid JSON object with EXACTLY
                        these eight fields:

                        {
                          "issueTitle": "Short title",
                          "whatWentWrong": "What is wrong, or state that no clear mistake was found",
                          "whereIsTheMistake": "Exact line, expression, or code location",
                          "whyItHappened": "Underlying technical reason",
                          "howToCorrectIt": "Specific correction and explanation",
                          "correctedCode": "Complete suggested code",
                          "category": "Syntax, Logic, Input Handling, Runtime, or Other",
                          "conceptToRevise": "Relevant concept to learn"
                        }

                        IMPORTANT RULES:

                        1. Generate every explanation from the submitted code.
                        Do not use the same explanation for all fields.

                        2. Explain what went wrong, where the issue occurs,
                        why it happened, and how to correct it separately.

                        3. Do not invent compiler errors, runtime results,
                        line numbers, or facts that cannot be established
                        from the supplied code.

                        4. Distinguish actual mistakes from possible risks.
                        If the code already handles an exception correctly,
                        acknowledge the existing handling.

                        5. For division by zero, inspect whether the divisor
                        can be zero and whether an appropriate handler exists.

                        6. Preserve the original program's intended behavior.
                        Return complete corrected code when a correction
                        is needed. Otherwise return the original code unchanged.

                        7. Do not claim that code was compiled or executed.

                        8. Recommend a genuinely relevant concept.
                        Use "General Review" if no suitable topic is available.

                        9. All eight fields must be JSON strings.
                        Escape quotes and newlines correctly.
                        Return valid JSON only, without Markdown fences.
                        """
                ),
                Map.of(
                    "role", "user",
                    "content", "Programming language: " + language
                            + "\n\nSubmitted code:\n" + code
                )
            ),
            "temperature", 0.2
        );

        Map<?, ?> response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Groq returned an empty response.");
        }

        List<?> choices = (List<?>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException(
                    "Groq returned no analysis choices.");
        }

        Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");

        if (message == null || message.get("content") == null) {
            throw new IllegalStateException(
                    "Groq returned no analysis content.");
        }

        return message.get("content").toString();
    }

    // 2. Generate a programming concept lesson
    public String generateConceptLesson(
            String concept, String language) {

        Map<String, Object> requestBody = Map.of(
            "model", "openai/gpt-oss-20b",
            "messages", List.of(
                Map.of(
                    "role", "system",
                    "content", """
                        You are DebugDairy's programming tutor.

                        Create a complete, beginner-friendly lesson for
                        the requested concept and programming language.

                        Return ONLY one valid JSON object with these fields:
                        title, subtitle, language, difficulty, topic, focus,
                        icon, definition, importance, visualTitle, visualIntro,
                        visualLabel, visualHint, visual, visualCaption, steps,
                        wrong, correct, wrongExplanation, correctExplanation,
                        codeExplanation, approaches, mistakes, takeaways, facts,
                        related, tip, question, options, answer, feedback.

                        REQUIREMENTS:

                        1. Explain the requested concept accurately.
                        2. Use examples in the requested programming language.
                        3. Make wrong and correct code examples meaningful.
                        4. Include 3-5 learning steps, common mistakes,
                        takeaways, and practice quiz options.
                        5. The answer field must be the zero-based index of the correct option in options.

6. Every quiz question must have EXACTLY ONE correct answer and at least three incorrect options.

7. Verify that the correct answer is factually accurate for the requested programming language and concept.

8. Check every option before returning the lesson. There must be exactly one correct option, and all other options must be clearly incorrect.

9. Ensure the answer field points to the correct option's exact index in the options array. Count from zero.

10. Never create a question if none of its options is correct. Rewrite the question or options until exactly one correct answer exists.

11. For Java HashMap questions, remember that HashMap permits one null key and multiple null values. It does not guarantee insertion order and is not thread-safe by default. Do not claim that HashMap allows multiple null keys.
                        12. All examples must match the requested language.
                        13. Do not claim code was compiled or executed.
                        14. Return valid JSON only, with no Markdown fences.
                        15. Use double quotes for JSON keys and string values.
                        16. Escape double quotes and newline characters
                        inside JSON strings correctly.
                        17. Do not leave any JSON string unfinished.
                        18. Keep the lesson focused on the requested concept.
                        """
                ),
                Map.of(
                    "role", "user",
                    "content", "Concept: " + concept
                        + "\nProgramming language: " + language
                        + "\nGenerate a complete lesson for this concept."
                )
            ),
            "temperature", 0.1,
            "max_tokens", 7000
        );

        Map<?, ?> response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Groq returned an empty lesson response.");
        }

        List<?> choices = (List<?>) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException(
                    "Groq returned no lesson choices.");
        }

        Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");

        if (message == null || message.get("content") == null) {
            throw new IllegalStateException(
                    "Groq returned no lesson content.");
        }

        String content = message.get("content").toString().trim();

        // Remove Markdown fences if Groq includes them.
        if (content.startsWith("```")) {
            content = content
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }

        if (content.isBlank()) {
            throw new IllegalStateException(
                    "Groq returned an empty lesson.");
        }

        // Validate the JSON before returning it to the controller.
        try {
            new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(content);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(
                    "Groq returned invalid lesson JSON. Please retry.", e);
        }

        return content;
    }
}
