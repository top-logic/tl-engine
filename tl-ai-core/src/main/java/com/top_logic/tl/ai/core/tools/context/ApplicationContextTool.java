/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.context;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tl.ai.core.agent.ApplicationContextContributor;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.environment.PromptPlaceholders;
import com.top_logic.tl.ai.core.model.ApplicationContext;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.TLContext;

/**
 * Reads and saves the application context - the text {@link ApplicationContextContributor} puts into
 * the system prompt of every agent a user talks to.
 *
 * <p>
 * The tools of {@code TL_init}, which writes that text with the user and then saves it here instead
 * of handing an administrator a document to paste somewhere. Saving is a {@link ToolMode#DATA_WRITE}
 * call, so the person approves the complete text before it is written - that approval is the review
 * step. It is refused before anybody is asked when the account may not change the context, when the
 * text is too long, or when it names a placeholder nobody fills, so an approval prompt is only shown
 * for a save that will go through.
 * </p>
 */
public class ApplicationContextTool {

	/** What a call in a system context is told; the engine checks no rights there. */
	static final String SYSTEM_CONTEXT = "This call runs without a user account, in a system context where"
		+ " the application checks no rights and records no author. The application context is only saved"
		+ " in the name of the user the run acts for. Nothing was saved.";

	/**
	 * Returns the current application context.
	 *
	 * @param context
	 *        The run.
	 * @return The text, or a sentence saying that there is none yet.
	 */
	@ToolMethod(description = "Read the application context: the document every agent of this application"
		+ " is given in its system prompt - its purpose, users, terms, calculated values and rules. Read it"
		+ " before writing a new one, so that you revise it rather than start over.",
		mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String readApplicationContext(ToolContext context) {
		ApplicationContext current = ApplicationContextContributor.current();
		if (current == null) {
			return "This application has no place for an application context: its model is not loaded.";
		}
		String text = current.getText();
		if (text == null || text.isBlank()) {
			return "No application context has been written yet. At most "
				+ ApplicationContextContributor.MAX_LENGTH + " characters.";
		}
		Set<String> agents = current.getAgents();
		return "The application context (" + text.length() + " of at most "
			+ ApplicationContextContributor.MAX_LENGTH + " characters), given to "
			+ (agents == null || agents.isEmpty() ? "every agent a user talks to"
				: "these agents: " + String.join(", ", agents.stream().sorted().toList()))
			+ ":\n\n" + text;
	}

	/**
	 * Replaces the application context with the given text.
	 *
	 * @param text
	 *        The complete document.
	 * @param context
	 *        The run: its rights and the turn's transaction.
	 * @return What was saved, or a failed result saying why nothing was.
	 */
	@ToolMethod(description = "Save the application context: replaces the whole document every agent of"
		+ " this application is given in its system prompt, from the user's next message on. Pass the"
		+ " complete document the user agreed to, not a part of it. The user approves the save and sees the"
		+ " text while doing so. Call it once, at the end, after the user said the draft is done.",
		mode = ToolMode.DATA_WRITE, group = ToolGroups.AGENT_AUTHORING, marking = AnswerMarking.NONE,
		check = "refusal")
	public ToolResult saveApplicationContext(
			@P("The complete application context, in Markdown") String text,
			ToolContext context) {
		String refusal = refusal(text, context);
		if (refusal != null) {
			return ToolResult.failure(refusal);
		}
		ApplicationContext current = ApplicationContextContributor.current();
		String before = current.getText();
		String saved = text.strip();
		current.setText(saved);
		return ToolResult.of("Saved the application context (" + saved.length() + " characters"
			+ (before == null || before.isBlank() ? "" : ", replacing one of " + before.length())
			+ "). Every agent a user talks to is given it from the next message on, also in conversations"
			+ " that are already open. An administrator can read and edit it in the Agent Editor, on the"
			+ " tab Application context.");
	}

	/**
	 * Why a save of the given text would be refused, or <code>null</code> when it would go through.
	 *
	 * <p>
	 * Asked before the approval prompt, so a person is never asked to approve a save that cannot
	 * happen; {@link #saveApplicationContext(String, ToolContext)} asks it again, because not every
	 * caller asks first.
	 * </p>
	 */
	String refusal(String text, ToolContext context) {
		if (text == null || text.isBlank()) {
			return "The text is empty. Pass the complete application context. Nothing was saved.";
		}
		String saved = text.strip();
		if (saved.length() > ApplicationContextContributor.MAX_LENGTH) {
			return "The text is " + saved.length() + " characters long; at most "
				+ ApplicationContextContributor.MAX_LENGTH + " are allowed, because every run of every agent"
				+ " pays for it. Shorten it - state meanings and rules, not structure the model shows anyway"
				+ " - and save again. Nothing was saved.";
		}
		List<String> unknown = PromptPlaceholders.unknownIn(saved);
		if (!unknown.isEmpty()) {
			return "The text names " + String.join(", ", unknown.stream().map(name -> "{{" + name + "}}").toList())
				+ ", which nothing fills; the known placeholders are " + PromptPlaceholders.knownNames()
				+ ". Remove the double braces or use a known placeholder. Nothing was saved.";
		}
		if (ThreadContext.isSystemContext() || TLContext.currentUser() == null) {
			return SYSTEM_CONTEXT;
		}
		ApplicationContext current = ApplicationContextContributor.current();
		if (current == null) {
			return "This application has no place for an application context: its model is not loaded."
				+ " Nothing was saved.";
		}
		TLStructuredTypePart textPart = current.tType().getPart(ApplicationContext.TEXT_ATTR);
		if (!context.modelSecurity().canWrite(current) || !context.modelSecurity().canWrite(current, textPart)) {
			return "Permission denied: your account may not change the application context - by default only"
				+ " an administrator may. Nothing was saved. Give the user the final document and tell them"
				+ " an administrator can save it in the Agent Editor, on the tab Application context.";
		}
		return null;
	}

}
