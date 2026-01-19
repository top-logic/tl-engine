/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface UMLScorer {
	@UserMessage("""
			You are a scoring agent.  Given a JSON critique of a UML specification,
			compute a quality score between 0.0 and 1.0.  Use the following
			heuristic:  if there are any critical issues, return a score below
			0.5; for every every important issues but no critical ones, reduce the score (starting from 1.0) by 0.05 ;
			if there are no issues, return a score 1.0.  Return only the numeric score.

			Critique: {{critique}}
			""")
	@Agent("Scores a UML critique result")
	double score(@V("critique") String critiqueForScoring);
}
