package com.top_logic.ai.demo.test.scoreAgent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.ai.demo.test.scoreAgent.messages.JudgeResult;
import com.top_logic.ai.service.AIService;
import com.top_logic.basic.Logger;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * Client for using Anthropic AI to score ModelExplorerTool outputs.
 */
public class AiJudgeClient {

	public static final String DEFAULT_JUDGE_MODEL = "claude-sonnet-4-5-20250929";

	private final AIService _aiService;
	private final String _judgeModelName;

	public AiJudgeClient(AIService aiService) {
		this(aiService, DEFAULT_JUDGE_MODEL);
	}

	public AiJudgeClient(AIService aiService, String judgeModelName) {
		_aiService = aiService;
		_judgeModelName = judgeModelName;
	}

	/**
	 * Legacy single-truth-set scoring (backward compatible).
	 */
	public JudgeResult scoreAnswer(
		String expectedFactsJson,
		String actualAnswer,
		String judgeSystemPrompt
	) {
		// Use legacy mode: pass expectedFactsJson as both required and available
		return scoreAnswerTwoTruthSet(expectedFactsJson, expectedFactsJson, actualAnswer, judgeSystemPrompt);
	}

	/**
	 * Two-truth-set scoring (new mode).
	 * 
	 * @param requiredFactsJson Prompt-specific minimum expectations (what MUST be mentioned)
	 * @param availableFactsJson Complete oracle truth (what exists in the model)
	 * @param actualAnswer The agent's answer
	 * @param judgeSystemPrompt The judge system prompt template
	 */
	public JudgeResult scoreAnswerTwoTruthSet(
		String requiredFactsJson,
		String availableFactsJson,
		String actualAnswer,
		String judgeSystemPrompt
	) {
		try {
			ChatModel judgeModel = _aiService.getChatModel(_judgeModelName);

			String userPrompt = buildUserPromptTwoTruthSet(requiredFactsJson, availableFactsJson, actualAnswer);

			SystemMessage systemMessage = SystemMessage.from(judgeSystemPrompt);
			UserMessage userMessage = UserMessage.from(userPrompt);

			ChatRequest request = ChatRequest.builder()
				.messages(systemMessage, userMessage)
				.build();

			ChatResponse response = judgeModel.chat(request);

			AiMessage aiMessage = response.aiMessage();
			String resultText = aiMessage.text();

			Logger.info("Judge response: " + resultText, AiJudgeClient.class);

			return parseJudgeResult(resultText);

		} catch (Exception ex) {
			Logger.error("Failed to score answer: " + ex.getMessage(), ex, AiJudgeClient.class);
			throw new RuntimeException("Failed to score answer: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Extracts JSON from a response that may contain surrounding text.
	 */
	private String extractJson(String response) {
		int start = response.indexOf('{');
		int end = response.lastIndexOf('}');
		if (start == -1 || end == -1 || end < start) {
			throw new IllegalArgumentException("Response does not contain valid JSON object.");
		}
		return response.substring(start, end + 1);
	}

	/**
	 * Parses the judge's JSON response using msgbuf JsonReader.
	 */
	private JudgeResult parseJudgeResult(String responseText) {
		try {
			String jsonOnly = extractJson(responseText);
			// Create JsonReader from StringR (msgbuf's Reader implementation)
			StringR reader = new StringR(jsonOnly);
			JsonReader jsonReader = new JsonReader(reader);
			return JudgeResult.readJudgeResult(jsonReader);
		} catch (Exception ex) {
			throw new RuntimeException(
				"Failed to parse judge result JSON: " + ex.getMessage() + "\nResponse was: " + responseText, ex);
		}
	}

	private String buildUserPromptTwoTruthSet(String requiredFactsJson, String availableFactsJson, String actualAnswer) {
		return "Required Facts (prompt-specific minimum):\n" + requiredFactsJson + "\n\n" +
			"Available Facts (complete oracle truth):\n" + availableFactsJson + "\n\n" +
			"Actual Answer:\n" + actualAnswer;
	}

	/**
	 * Scores an answer multiple times and returns the median score (legacy single-truth-set).
	 *
	 * <p>
	 * This helps reduce nondeterminism in AI judge responses by running the judge N times
	 * and taking the median score.
	 * </p>
	 *
	 * @param expectedFactsJson The expected facts JSON.
	 * @param actualAnswer The actual answer to score.
	 * @param judgeSystemPrompt The judge system prompt.
	 * @param numRuns Number of times to run the judge (default: 3, should be odd for median).
	 * @return The median judge result.
	 */
	public JudgeResult scoreAnswerWithMedian(
		String expectedFactsJson,
		String actualAnswer,
		String judgeSystemPrompt,
		int numRuns
	) {
		return scoreAnswerWithMedianTwoTruthSet(
			expectedFactsJson, expectedFactsJson, actualAnswer, judgeSystemPrompt, numRuns);
	}

	/**
	 * Scores an answer multiple times and returns the median score (two-truth-set mode).
	 *
	 * @param requiredFactsJson Prompt-specific minimum expectations
	 * @param availableFactsJson Complete oracle truth
	 * @param actualAnswer The actual answer to score
	 * @param judgeSystemPrompt The judge system prompt
	 * @param numRuns Number of times to run the judge (should be odd for median)
	 * @return The median judge result
	 */
	public JudgeResult scoreAnswerWithMedianTwoTruthSet(
		String requiredFactsJson,
		String availableFactsJson,
		String actualAnswer,
		String judgeSystemPrompt,
		int numRuns
	) {
		if (numRuns < 1) {
			throw new IllegalArgumentException("numRuns must be at least 1");
		}
		if (numRuns == 1) {
			return scoreAnswerTwoTruthSet(requiredFactsJson, availableFactsJson, actualAnswer, judgeSystemPrompt);
		}

		List<JudgeResult> results = new ArrayList<>();
		for (int i = 0; i < numRuns; i++) {
			Logger.info("Running judge evaluation " + (i + 1) + "/" + numRuns, AiJudgeClient.class);
			JudgeResult result = scoreAnswerTwoTruthSet(
				requiredFactsJson, availableFactsJson, actualAnswer, judgeSystemPrompt);
			results.add(result);
		}

		// Sort by score and take median
		results.sort((a, b) -> Integer.compare(a.getScore(), b.getScore()));
		int medianIndex = numRuns / 2;
		JudgeResult medianResult = results.get(medianIndex);

		// Merge all violations from all runs into the median result
		// (This gives a comprehensive view of all issues found across runs)
		JudgeResult merged = JudgeResult.create();
		merged.setScore(medianResult.getScore());

		// Collect all unique violations from all runs
		Set<String> allMissingFacts = new HashSet<>();
		Set<String> allIncorrectClaims = new HashSet<>();
		Set<String> allScopeViolations = new HashSet<>();
		Set<String> allHallucinations = new HashSet<>();
		Set<String> allNotFetched = new HashSet<>();
		Set<String> allFormatViolations = new HashSet<>();

		for (JudgeResult r : results) {
			allMissingFacts.addAll(r.getMissingFacts());
			allIncorrectClaims.addAll(r.getIncorrectClaims());
			allScopeViolations.addAll(r.getScopeViolations());
			allHallucinations.addAll(r.getHallucinations());
			allNotFetched.addAll(r.getNotFetched());
			allFormatViolations.addAll(r.getFormatViolations());
		}

		merged.getMissingFacts().addAll(allMissingFacts);
		merged.getIncorrectClaims().addAll(allIncorrectClaims);
		merged.getScopeViolations().addAll(allScopeViolations);
		merged.getHallucinations().addAll(allHallucinations);
		merged.getNotFetched().addAll(allNotFetched);
		merged.getFormatViolations().addAll(allFormatViolations);

		Logger.info("Median score: " + medianResult.getScore() + " (from " + numRuns + " runs)", AiJudgeClient.class);
		return merged;
	}
}
