/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.scorer;

import java.util.ArrayList;
import java.util.List;

/**
 * Result from the neutral scorer that provides a final assessment.
 * 
 * @author jhu
 */
public class NeutralScorerResult {

	private int _finalScore;
	private String _explanation;
	private List<String> _keyIssues = new ArrayList<>();
	private List<String> _strengths = new ArrayList<>();

	/**
	 * The final score (0-10) assigned by the neutral scorer.
	 */
	public int getFinalScore() {
		return _finalScore;
	}

	/**
	 * Sets the final score.
	 */
	public void setFinalScore(int finalScore) {
		_finalScore = finalScore;
	}

	/**
	 * Brief explanation of the score.
	 */
	public String getExplanation() {
		return _explanation;
	}

	/**
	 * Sets the explanation.
	 */
	public void setExplanation(String explanation) {
		_explanation = explanation;
	}

	/**
	 * Key issues identified (max 3-5).
	 */
	public List<String> getKeyIssues() {
		return _keyIssues;
	}

	/**
	 * Sets the key issues.
	 */
	public void setKeyIssues(List<String> keyIssues) {
		_keyIssues = keyIssues;
	}

	/**
	 * Strengths identified (max 3-5).
	 */
	public List<String> getStrengths() {
		return _strengths;
	}

	/**
	 * Sets the strengths.
	 */
	public void setStrengths(List<String> strengths) {
		_strengths = strengths;
	}
}
