/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.util.List;

import com.top_logic.tl.ai.core.session.Citation;

/**
 * What a run answered: the text a {@link CitationExpectation} and a {@link JudgeExpectation} grade.
 *
 * <p>
 * Two texts, because the citation check rewrites the answer: {@link #rawAnswer()} is what the model
 * wrote, links and all; {@link #answer()} is what the reader sees, with a link to a read page turned
 * into a footnote and a link to anything else reduced to its words. Counting the links nobody read
 * needs the first, judging what the reader got needs the second.
 * </p>
 *
 * @param question
 *        The scenario's prompt.
 * @param rawAnswer
 *        The run's last answer as the model wrote it; empty if it wrote none.
 * @param answer
 *        The same answer after the citation check.
 * @param citations
 *        The footnotes the citation check kept, in their order.
 */
public record RunAnswer(String question, String rawAnswer, String answer, List<Citation> citations) {

	/** For a run whose answer is not graded, or that answered nothing. */
	public static RunAnswer none(String question) {
		return new RunAnswer(question, "", "", List.of());
	}
}
