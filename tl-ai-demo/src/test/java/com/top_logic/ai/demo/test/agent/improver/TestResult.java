/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.improver;

/**
 * Holds the complete result of a single test for analysis by the system prompt improver.
 */
public class TestResult {

	private String _testName;
	private String _userPrompt;
	private String _agentAnswer;
	private String _judgeEvaluationJson;
	private String _finalScoreJson;

	/**
	 * Creates a test result.
	 */
	public TestResult(String testName, String userPrompt, String agentAnswer, String judgeEvaluationJson,
		String finalScoreJson) {
		_testName = testName;
		_userPrompt = userPrompt;
		_agentAnswer = agentAnswer;
		_judgeEvaluationJson = judgeEvaluationJson;
		_finalScoreJson = finalScoreJson;
	}

	/**
	 * The test name.
	 */
	public String getTestName() {
		return _testName;
	}

	/**
	 * The user prompt.
	 */
	public String getUserPrompt() {
		return _userPrompt;
	}

	/**
	 * The agent's answer.
	 */
	public String getAgentAnswer() {
		return _agentAnswer;
	}

	/**
	 * The judge evaluation as JSON.
	 */
	public String getJudgeEvaluationJson() {
		return _judgeEvaluationJson;
	}

	/**
	 * The final score as JSON.
	 */
	public String getFinalScoreJson() {
		return _finalScoreJson;
	}
}
