/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.LabelPositioning;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.tl.ai.core.agent.environment.PromptPlaceholders;
import com.top_logic.tl.ai.core.agent.environment.RunEnvironment;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.tl.ai.core.security.AllAgentNames;
import com.top_logic.util.Resources;

/**
 * Adds a fixed text to the system prompt of the named agents, in the language of the account the
 * run acts for.
 *
 * <p>
 * For what an application wants an agent to know in every run and the model does not show: what
 * the application is, its domain terms, its fixed business rules. The text may use the
 * {@code {{...}}} placeholders of an agent's own prompt.
 * </p>
 *
 * <p>
 * A changed text takes effect when this service is restarted, from the next run on.
 * </p>
 *
 * @see SystemPromptContributor How a contribution reaches the prompt, and why it must not change
 *      within a conversation.
 */
public class TextPromptContributor extends AbstractConfiguredInstance<TextPromptContributor.Config<?>>
		implements SystemPromptContributor {

	/**
	 * Configuration of a {@link TextPromptContributor}.
	 */
	@DisplayOrder({
		Config.AGENTS,
		Config.TEXT,
	})
	public interface Config<I extends TextPromptContributor> extends PolymorphicConfiguration<I> {

		/** Configuration property name for {@link #getAgents()}. */
		String AGENTS = "agents";

		/** Configuration property name for {@link #getText()}. */
		String TEXT = "text";

		/**
		 * The agents that get the text.
		 *
		 * <p>
		 * The choice offers every agent of the application: the agents of the configuration and the
		 * agents created in the running application. Left empty, every agent gets the text, also the
		 * ones the application runs for a user, such as the chat's prompt improver. Pick the agents
		 * when the text is about the application's data: an agent that only rewrites a draft does not
		 * need it, and pays for it on every run.
		 * </p>
		 *
		 * @see #AGENTS
		 */
		@Name(AGENTS)
		@Label("Agents")
		@Format(CommaSeparatedStrings.class)
		@Options(fun = AgentNames.class)
		@Constraint(KnownAgents.class)
		List<String> getAgents();

		/**
		 * The text to add to the system prompt, in the language of the account the run acts for.
		 *
		 * <p>
		 * When there is no text in that language, the text in the application's fallback language
		 * is used. An unknown placeholder is refused when the configuration is saved. The text may
		 * use these placeholders:
		 * </p>
		 *
		 * <ul>
		 * <li>{@code {{user}}} - the name of the account the run acts for.</li>
		 * <li>{@code {{language}}} - the UI language of that account, in which the agent answers.</li>
		 * <li>{@code {{date}}} - today's date in the time zone of that account.</li>
		 * <li>{@code {{agent}}} - the display name of the agent.</li>
		 * <li>{@code {{model}}} - the language model the run uses.</li>
		 * <li>{@code {{application}}} - the name of this application.</li>
		 * <li>{@code {{context}}} - the facts the page states for the whole conversation, one per
		 * line.</li>
		 * <li>{@code {{turn_context}}} - the explanation of the per-turn context block, for runs that
		 * get one.</li>
		 * <li>{@code {{preferences}}} - the preference text of the account.</li>
		 * <li>{@code {{memories}}} - what the agent keeps about the account from earlier
		 * conversations.</li>
		 * <li>{@code {{environment}}} - the complete environment block. Without it, the block comes
		 * at the end of the prompt.</li>
		 * </ul>
		 *
		 * <p>
		 * The text is part of the cached system prompt and is paid for once per conversation. A
		 * change takes effect when this service is restarted.
		 * </p>
		 *
		 * @see #TEXT
		 */
		@Name(TEXT)
		@Label("Text")
		@Mandatory
		@LabelPositioning(LabelPosition.ABOVE)
		@ControlProvider(MultiLineText.class)
		@Constraint(KnownPlaceholders.class)
		@Binding(VerbatimTextBinding.class)
		ResKey getText();

	}

	private final Set<String> _agents;

	/**
	 * Creates a {@link TextPromptContributor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TextPromptContributor(InstantiationContext context, Config<?> config) {
		super(context, config);
		_agents = Set.copyOf(config.getAgents());
	}

	@Override
	public String contribute(AgentDefinition agent, AgentModelAssignment assignment) {
		if (!appliesTo(agent.name())) {
			return null;
		}
		return text(RunEnvironment.runLocale());
	}

	/**
	 * Whether the agent with the given technical name gets the text.
	 */
	public boolean appliesTo(String agentName) {
		return _agents.isEmpty() || _agents.contains(agentName);
	}

	/**
	 * The configured text in the given language, or in the fallback language when it has none.
	 */
	public String text(Locale locale) {
		return unindent(Resources.getInstance(locale).getString(getConfig().getText(), null));
	}

	/**
	 * The text without the indentation that all its lines but the first have in common, and
	 * without leading and trailing blank space.
	 *
	 * <p>
	 * For a text a host writes into the XML indented to the depth of its element, as XML usually
	 * is: the reader keeps that indentation on every line but the first, and Markdown reads a line
	 * indented by four spaces or a tab as code.
	 * </p>
	 */
	static String unindent(String text) {
		if (text == null) {
			return null;
		}
		String[] lines = text.strip().split("\n", -1);
		int common = Integer.MAX_VALUE;
		for (int n = 1; n < lines.length; n++) {
			String line = lines[n];
			if (!line.isBlank()) {
				common = Math.min(common, indentation(line));
			}
		}
		if (common == 0 || common == Integer.MAX_VALUE) {
			return String.join("\n", lines);
		}
		for (int n = 1; n < lines.length; n++) {
			String line = lines[n];
			lines[n] = line.isBlank() ? "" : line.substring(common);
		}
		return String.join("\n", lines);
	}

	private static int indentation(String line) {
		int result = 0;
		while (result < line.length() && (line.charAt(result) == '\t' || line.charAt(result) == ' ')) {
			result++;
		}
		return result;
	}

	/**
	 * Writes the text of a {@link TextPromptContributor} as CDATA, so that it comes back exactly
	 * as it was entered.
	 *
	 * <p>
	 * The service editor writes the configuration with a pretty-printer that indents every line of
	 * an element's text to the element's depth and strips the indentation the line had. A text
	 * saved there would come back with tabs in front of every line - in the editor and in the
	 * prompt - and a nested Markdown list would lose its nesting. CDATA content is left alone.
	 * </p>
	 */
	public static class VerbatimTextBinding extends ResKey.ValueBinding {

		/** The instance the configuration uses. */
		public static final VerbatimTextBinding INSTANCE = new VerbatimTextBinding();

		/**
		 * Creates a {@link VerbatimTextBinding}.
		 */
		@CalledByReflection
		public VerbatimTextBinding() {
			super(true);
		}
	}

	/**
	 * The agents an administrator can pick from: the agents of the configuration and the agents
	 * created in the running application.
	 */
	public static class AgentNames extends Function0<List<String>> {

		@Override
		public List<String> apply() {
			return List.copyOf(new TreeSet<>(AllAgentNames.existingAgentNames()));
		}
	}

	/**
	 * Refuses an agent name that no agent of the application has.
	 *
	 * <p>
	 * Asked when an administrator saves the service in the service editor. It does not refuse
	 * anything while the application starts: the agent definitions are not readable yet when the
	 * engine checks the configuration, and an agent of the knowledge base is not readable before
	 * the knowledge base is up. A name that matches no agent then simply gets the text nowhere.
	 * </p>
	 */
	public static class KnownAgents extends ValueConstraint<List<String>> {

		/**
		 * Creates a {@link KnownAgents} constraint.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@CalledByReflection
		public KnownAgents() {
			super((Class) List.class);
		}

		@Override
		protected void checkValue(PropertyModel<List<String>> property) {
			List<String> agents = property.getValue();
			if (agents == null || agents.isEmpty() || !TopLogicAgentDefinitions.Module.INSTANCE.isActive()) {
				return;
			}
			List<String> unknown = unknownAgents(agents, AllAgentNames.existingAgentNames());
			if (!unknown.isEmpty()) {
				property.setProblemDescription(
					I18NConstants.UNKNOWN_AGENTS__NAMES.fill(String.join(", ", unknown)));
			}
		}

		/**
		 * The names of the given agents that are not among the existing ones, in order.
		 */
		static List<String> unknownAgents(List<String> agents, Collection<String> existing) {
			Set<String> known = new HashSet<>(existing);
			return agents.stream().filter(name -> !known.contains(name)).distinct().toList();
		}
	}

	/**
	 * Refuses a text that names a placeholder the harness cannot fill, in any of its languages.
	 *
	 * <p>
	 * A constraint rather than a check in the constructor, because the engine asks the constraints
	 * of a service configuration at both moments that matter: when the application configuration is
	 * read at start-up, and when an administrator saves the service in the service editor - before
	 * the file is written. A constructor check would only refuse the restart that follows the save.
	 * </p>
	 */
	public static class KnownPlaceholders extends ValueConstraint<ResKey> {

		/**
		 * Creates a {@link KnownPlaceholders} constraint.
		 */
		@CalledByReflection
		public KnownPlaceholders() {
			super(ResKey.class);
		}

		@Override
		protected void checkValue(PropertyModel<ResKey> property) {
			List<String> unknown = unknownPlaceholders(property.getValue());
			if (unknown.isEmpty()) {
				return;
			}
			String names = String.join(", ", unknown.stream().map(name -> "{{" + name + "}}").toList());
			property.setProblemDescription(com.top_logic.tl.ai.core.agent.environment.I18NConstants
				.UNKNOWN_PLACEHOLDER__NAMES_KNOWN.fill(names, PromptPlaceholders.knownNames()));
		}

		/**
		 * The unknown placeholder names in any language of the given text.
		 *
		 * <p>
		 * Only a literal text - the one written into the configuration or entered in the service
		 * editor - carries its languages with it; a reference to a resource key is not inspected.
		 * </p>
		 */
		static List<String> unknownPlaceholders(ResKey text) {
			if (!(text instanceof ResKey.LiteralKey literal)) {
				return List.of();
			}
			Collection<String> translations = literal.getTranslations().values();
			Set<String> unknown = new LinkedHashSet<>();
			for (String translation : translations) {
				unknown.addAll(PromptPlaceholders.unknownIn(translation));
			}
			return List.copyOf(unknown);
		}
	}

}
