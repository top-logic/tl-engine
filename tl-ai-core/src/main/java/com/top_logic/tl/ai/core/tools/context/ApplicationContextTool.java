/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.context;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.base.operation.OperationMode;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.SystemPromptContributor;
import com.top_logic.tl.ai.core.agent.TextPromptContributor;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.environment.PromptPlaceholders;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.TLContext;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Reads and saves the application context: the {@link TextPromptContributor} named
 * {@value TextPromptContributor.Config#APPLICATION_CONTEXT} among the prompt contributors of
 * {@link LLMService}, which puts the text into the system prompt of the agents.
 *
 * <p>
 * The tools of {@code TL_init}. The application context is development material: a developer builds
 * the application and sets up its agents, and writing down what the application is for is part of
 * that. So it lives in the application's source configuration, in git, deployed with the application -
 * and saving writes it there ({@link ApplicationContextSource}), which is only possible where the
 * application runs from its source ({@link DevelopmentWorkspace}). Anywhere else the save is not
 * offered at all, and the run says why.
 * </p>
 *
 * <p>
 * The save is an {@link ToolMode#EXTERNAL} call: it leaves the running application as it is - the
 * text takes effect when the LLM service is restarted - and changes a file in the developer's working
 * copy, which no transaction of this application covers. Like every such call it needs the person's
 * approval, and the person approves the complete text before it is written - that approval is the
 * review step. It is refused before anybody is asked when the account is not an administrator - the
 * people who may change a service's configuration in the application - or when the text is empty, too long or names a placeholder nobody fills, so an
 * approval prompt is only shown for a save that will go through.
 * </p>
 */
public class ApplicationContextTool {

	/**
	 * The longest text that is saved.
	 *
	 * <p>
	 * Every run of every agent that is given the context pays for it; 8000 characters are about 2000
	 * tokens.
	 * </p>
	 */
	public static final int MAX_LENGTH = 8000;

	/** What a call in a system context is told; the engine checks no rights there. */
	static final String SYSTEM_CONTEXT = "This call runs without a user account, in a system context where"
		+ " the application checks no rights. The application context is only saved for a user the run acts"
		+ " for. Nothing was saved.";

	/**
	 * Returns the current application context.
	 *
	 * @param context
	 *        The run.
	 * @return The text, or a sentence saying that there is none yet.
	 */
	@ToolMethod(description = "Read the application context: the document the agents of this application"
		+ " are given in their system prompt - its purpose, users, terms, calculated values and rules. Read it"
		+ " before writing a new one, so that you revise it rather than start over.",
		mode = ToolMode.READ, group = ToolGroups.AGENT_META)
	public String readApplicationContext(ToolContext context) {
		Path webapp = DevelopmentWorkspace.webapp();
		ApplicationContextSource source = webapp == null ? null : new ApplicationContextSource(webapp);
		ApplicationContextSource.Found saved = null;
		String unreadable = "";
		if (source != null) {
			try {
				saved = source.read();
			} catch (IOException ex) {
				unreadable = "\n\nThe application's source configuration could not be read: " + ex.getMessage();
			}
		}

		TextPromptContributor running = running();
		Map<String, String> active = running == null ? Map.of() : texts(running);
		if (saved != null && !saved.texts().isEmpty() && !sameTexts(saved.texts(), active)) {
			return "The application context saved in " + source.display(saved.file())
				+ ". It is not active yet: it takes effect when the LLM service is restarted.\n\n"
				+ render(saved.texts()) + unreadable;
		}
		if (active.isEmpty()) {
			return "No application context has been written yet. At most " + MAX_LENGTH + " characters."
				+ unreadable;
		}
		List<String> agents = running.getConfig().getAgents();
		return "The application context (" + longest(active) + " of at most " + MAX_LENGTH
			+ " characters), given to "
			+ (agents.isEmpty() ? "every agent" : "these agents: " + String.join(", ", agents))
			+ (saved == null ? "" : ", saved in " + source.display(saved.file()))
			+ ":\n\n" + render(active) + unreadable;
	}

	/**
	 * Writes the application context into the application's source configuration.
	 *
	 * @param text
	 *        The complete document.
	 * @param context
	 *        The run.
	 * @return Which file was written and what the developer does next, or a failed result saying why
	 *         nothing was saved.
	 */
	@ToolMethod(description = "Save the application context into this application's source configuration,"
		+ " replacing the whole document the agents are given in their system prompt. Pass the complete"
		+ " document the user agreed to, not a part of it. The user approves the save and sees the text"
		+ " while doing so. Call it once, at the end, after the user said the draft is done. It takes effect"
		+ " when the LLM service is restarted.",
		mode = ToolMode.EXTERNAL, group = ToolGroups.AGENT_AUTHORING, marking = AnswerMarking.NONE,
		check = "refusal", unavailable = "noSource")
	public ToolResult saveApplicationContext(
			@P("The complete application context, in Markdown") String text,
			ToolContext context) {
		String refusal = refusal(text, context);
		if (refusal != null) {
			return ToolResult.failure(refusal);
		}
		ApplicationContextSource source = new ApplicationContextSource(DevelopmentWorkspace.webapp());
		List<String> languages = languages();
		ApplicationContextSource.Saved saved;
		try {
			saved = source.save(text.strip(), languages);
		} catch (IOException | IllegalArgumentException ex) {
			return ToolResult.failure("Nothing was saved: " + ex.getMessage());
		}
		List<String> files = saved.files().stream().map(source::display).toList();
		String where = String.join(" and ", files);
		String what = saved.created()
			? "Created " + where + ", which adds the application context to the configuration of the LLM"
				+ " service without replacing that configuration."
			: saved.added()
				? "Added the application context to the prompt contributors of the LLM service in " + where + "."
				: "Replaced the application context in " + where + ".";
		return ToolResult.of(what + " It is written in " + String.join(", ", languages)
			+ ", the same text in each, so the agents of every user are given it. It is not active yet: it takes"
			+ " effect when the LLM service is restarted - in the Service Editor, select the LLM service in the"
			+ " list and restart it - or when the application is restarted. The file is part of the"
			+ " application's source: commit it.");
	}

	/**
	 * Why the save is not offered in this run, or <code>null</code> when it is.
	 */
	String noSource(ToolExecutionContext context) {
		if (DevelopmentWorkspace.webapp() != null) {
			return null;
		}
		OperationMode mode = DevelopmentWorkspace.mode();
		return "saveApplicationContext writes the application context into the application's source"
			+ " configuration, so it is only offered in the development environment - an application started"
			+ " from its source with tl_operation_mode=development. This application runs "
			+ (mode == null ? "in no known environment" : "in the " + mode.name().toLowerCase(Locale.ROOT)
				+ " environment")
			+ ". Give the user the final document in one fenced code block and tell them that a developer"
			+ " saves it by running this in the development environment.";
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
		String unavailable = noSource(context);
		if (unavailable != null) {
			return unavailable;
		}
		if (text == null || text.isBlank()) {
			return "The text is empty. Pass the complete application context. Nothing was saved.";
		}
		String saved = text.strip();
		if (saved.length() > MAX_LENGTH) {
			return "The text is " + saved.length() + " characters long; at most " + MAX_LENGTH + " are allowed,"
				+ " because every run of every agent pays for it. Shorten it - state meanings and rules, not"
				+ " structure the model shows anyway - and save again. Nothing was saved.";
		}
		List<String> unknown = PromptPlaceholders.unknownIn(saved);
		if (!unknown.isEmpty()) {
			return "The text names " + String.join(", ", unknown.stream().map(name -> "{{" + name + "}}").toList())
				+ ", which nothing fills; the known placeholders are " + PromptPlaceholders.knownNames()
				+ ". Remove the double braces or use a known placeholder. Nothing was saved.";
		}
		Person user = TLContext.currentUser();
		if (ThreadContext.isSystemContext() || user == null) {
			return SYSTEM_CONTEXT;
		}
		if (!user.isAdmin()) {
			return "Permission denied: only an administrator may change the configuration of the LLM service,"
				+ " which is where the application context is saved. Nothing was saved. Give the user the final"
				+ " document in one fenced code block.";
		}
		try {
			new ApplicationContextSource(DevelopmentWorkspace.webapp()).serviceFiles();
		} catch (IOException ex) {
			return "Nothing was saved: " + ex.getMessage();
		}
		return null;
	}

	/**
	 * The application context of the running configuration, or <code>null</code> when it has none.
	 */
	static TextPromptContributor running() {
		for (SystemPromptContributor contributor : LLMService.promptContributors()) {
			if (contributor instanceof TextPromptContributor text
				&& TextPromptContributor.Config.APPLICATION_CONTEXT.equals(text.getConfig().getName())) {
				return text;
			}
		}
		return null;
	}

	/**
	 * The languages the application context is written in: the languages the application supports.
	 */
	static List<String> languages() {
		List<String> result = new ArrayList<>();
		if (ResourcesModule.Module.INSTANCE.isActive()) {
			for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
				String language = locale.getLanguage();
				if (!language.isEmpty() && !result.contains(language)) {
					result.add(language);
				}
			}
		}
		if (result.isEmpty()) {
			result.add(Locale.ENGLISH.getLanguage());
		}
		return result;
	}

	private static Map<String, String> texts(TextPromptContributor contributor) {
		Map<String, String> result = new LinkedHashMap<>();
		for (String language : languages()) {
			String text = contributor.text(Locale.forLanguageTag(language));
			if (text != null && !text.isBlank()) {
				result.put(language, text);
			}
		}
		return result;
	}

	private static boolean sameTexts(Map<String, String> a, Map<String, String> b) {
		if (!a.keySet().equals(b.keySet())) {
			return false;
		}
		for (Map.Entry<String, String> entry : a.entrySet()) {
			if (!entry.getValue().strip().equals(b.get(entry.getKey()).strip())) {
				return false;
			}
		}
		return true;
	}

	private static int longest(Map<String, String> texts) {
		return texts.values().stream().mapToInt(String::length).max().orElse(0);
	}

	private static String render(Map<String, String> texts) {
		if (texts.values().stream().map(String::strip).distinct().count() == 1) {
			return texts.values().iterator().next().strip();
		}
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, String> entry : texts.entrySet()) {
			if (result.length() > 0) {
				result.append("\n\n");
			}
			result.append("--- language ").append(entry.getKey()).append(" ---\n").append(entry.getValue().strip());
		}
		return result.toString();
	}

}
