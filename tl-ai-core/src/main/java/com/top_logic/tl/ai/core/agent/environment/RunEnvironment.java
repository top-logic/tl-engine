/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.version.Version;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * What one agent run is told about the situation it happens in.
 *
 * <p>
 * Composed once, at the start of the run, from the acting account, the agent, the model and the
 * facts the host application stated. The result fills the {@code {{...}}} placeholders of the
 * agent's system prompt and - whether the prompt names them or not - is appended to it as one
 * {@code <environment>} block.
 * </p>
 *
 * <h2>Why nothing in here may change during a conversation</h2>
 *
 * <p>
 * The block sits at the very front of every request of the conversation, which is exactly the part
 * the providers' prompt caches key on. One changed character there invalidates the cache for every
 * following turn, so the block carries the date at day granularity and never the time of day, and
 * it carries only the facts the host marked {@link EnvironmentFact#stable() stable}. Facts that
 * change while the conversation runs are rendered by {@link TurnContext} instead, into the turn
 * itself.
 * </p>
 *
 * <h2>Why the memories are a snapshot</h2>
 *
 * <p>
 * The memories the agent kept about the account are rendered here too, and they are read once per
 * conversation rather than once per run for that very reason - see {@link UserMemoryBinding}. A
 * memory the user had the agent write in one turn appears in the next conversation, not in the next
 * turn of this one.
 * </p>
 *
 * @see PromptPlaceholders
 * @see EnvironmentTemplate
 * @see TurnContext
 * @see UserMemories
 */
public class RunEnvironment {

	/** Day granularity, unambiguous across locales, with the zone it is stated in. */
	private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final Map<String, String> _values;

	private final String _block;

	private RunEnvironment(Map<String, String> values, String block) {
		_values = values;
		_block = block;
	}

	/**
	 * Composes the environment of a run.
	 *
	 * @param agentDisplayName
	 *        What the agent is called in front of a user; may be {@code null}, in which case the
	 *        agent name is used.
	 * @param agentName
	 *        The agent's identifier.
	 * @param model
	 *        The model this run talks to.
	 * @param facts
	 *        Everything the host application stated about this run; only the stable ones are
	 *        rendered here, the others by {@link TurnContext}. May be {@code null}.
	 */
	public static RunEnvironment compose(ResKey agentDisplayName, String agentName, String model,
			List<EnvironmentFact> facts) {
		Person account = currentAccount();
		Locale locale = localeOf(account);
		Resources resources = Resources.getInstance(locale);
		Function<ResKey, String> resolve = key -> key == null ? null : resources.getString(key);

		Map<String, String> values = new LinkedHashMap<>();
		values.put(PromptPlaceholders.USER, userName(account));
		values.put(PromptPlaceholders.LANGUAGE, language(locale));
		values.put(PromptPlaceholders.DATE, date(account));
		values.put(PromptPlaceholders.AGENT, agentLabel(agentDisplayName, agentName, resolve));
		values.put(PromptPlaceholders.MODEL, model == null ? "" : model);
		values.put(PromptPlaceholders.APPLICATION, application(resolve));
		values.put(PromptPlaceholders.CONTEXT,
			contextSection(facts, resources.getString(I18NConstants.ENVIRONMENT_CONTEXT_HEADING)));
		values.put(PromptPlaceholders.TURN_CONTEXT,
			turnContextSection(facts, EnvironmentSettings.turnContextNote(resources)));
		values.put(PromptPlaceholders.PREFERENCES, preferencesSection(UserPreferences.of(account),
			resources.getString(I18NConstants.ENVIRONMENT_PREFERENCES_HEADING)));
		values.put(PromptPlaceholders.MEMORIES, UserMemories.section(UserMemories.forRun(account),
			resources.getString(I18NConstants.ENVIRONMENT_MEMORIES_HEADING),
			kind -> resources.getString(UserMemories.labelKey(kind))));

		return of(EnvironmentSettings.template(resources), values);
	}

	/**
	 * A {@link RunEnvironment} over values that are already resolved.
	 *
	 * <p>
	 * The seam {@link #compose} is built on, and what a test uses: it looks nothing up, so it has no
	 * opinion about which services are running.
	 * </p>
	 */
	public static RunEnvironment of(String template, Map<String, String> values) {
		Map<String, String> all = new LinkedHashMap<>(values);
		String block = EnvironmentTemplate.render(template, all);
		all.put(PromptPlaceholders.ENVIRONMENT, block);
		return new RunEnvironment(Map.copyOf(all), block);
	}

	/** The composed {@code <environment>} block. */
	public String block() {
		return _block;
	}

	/** The value of every placeholder, for tests and for diagnostics. */
	public Map<String, String> values() {
		return _values;
	}

	/**
	 * Fills the placeholders of a system prompt and makes sure the block is in it.
	 *
	 * <p>
	 * A prompt that names {@code {{environment}}} decides where the block goes. A prompt that does
	 * not - which is every prompt shipped today - gets it appended at the end, so an installation
	 * gains the block without editing a single agent.
	 * </p>
	 *
	 * @param systemPrompt
	 *        The agent's prompt, possibly {@code null}.
	 * @return The prompt the run sends.
	 */
	public String apply(String systemPrompt) {
		String base = systemPrompt == null ? "" : systemPrompt;
		String filled = PromptPlaceholders.fill(base, _values);
		if (PromptPlaceholders.uses(base, PromptPlaceholders.ENVIRONMENT)) {
			return filled;
		}
		if (_block.isEmpty()) {
			return filled;
		}
		String trimmed = filled.stripTrailing();
		return trimmed.isEmpty() ? _block : trimmed + "\n\n" + _block;
	}

	/**
	 * The {@code {{context}}} section: the heading and the stable facts, or nothing when the host
	 * stated none.
	 */
	static String contextSection(List<EnvironmentFact> facts, String heading) {
		if (facts == null || facts.isEmpty()) {
			return "";
		}
		List<String> lines = new ArrayList<>();
		for (EnvironmentFact fact : facts) {
			if (!fact.stable()) {
				// Rendered by TurnContext, into the turn itself: a fact that changes with the next
				// click would break the prompt cache on every turn it changes.
				continue;
			}
			lines.add("- " + fact.name() + ": " + fact.value());
		}
		if (lines.isEmpty()) {
			return "";
		}
		return heading + "\n" + String.join("\n", lines);
	}

	/**
	 * The {@code {{turn_context}}} section: what a {@code <turn-context>} block is, or nothing at all
	 * when this run sends none.
	 *
	 * <p>
	 * Conditional for the same reason the sections around it are: an installation that states
	 * nothing per turn sends no block, and a sentence describing one would be wording it never asked
	 * for - and a promise the turns do not keep. What decides is the same predicate
	 * {@link TurnContext} composes by, so the two cannot disagree.
	 * </p>
	 *
	 * <p>
	 * It is stable within a conversation as long as the host states its per-turn facts from every
	 * place that conversation is reachable from. A page that states them and a dock that does not
	 * would flip this section between the turns of one conversation - and would flip the
	 * {@code {{context}}} section with it, which is the older half of the same rule.
	 * </p>
	 */
	static String turnContextSection(List<EnvironmentFact> facts, String note) {
		if (facts == null || note == null || note.isBlank()) {
			return "";
		}
		for (EnvironmentFact fact : facts) {
			if (!fact.stable()) {
				return note;
			}
		}
		return "";
	}

	/** The {@code {{preferences}}} section, or nothing when there is no text to send. */
	static String preferencesSection(String text, String heading) {
		if (text == null || text.isBlank()) {
			return "";
		}
		return heading + "\n" + text;
	}

	private static String agentLabel(ResKey displayName, String agentName,
			Function<ResKey, String> resolve) {
		String label = resolve.apply(displayName);
		if (label != null && !label.isBlank()) {
			return label;
		}
		return agentName == null ? "" : agentName;
	}

	private static String application(Function<ResKey, String> resolve) {
		String configured = resolve.apply(EnvironmentSettings.applicationName());
		if (configured != null && !configured.isBlank()) {
			return configured;
		}
		try {
			String name = Version.getApplicationName();
			return name == null ? "" : name;
		} catch (RuntimeException ex) {
			Logger.debug("No application name available for the environment block.", ex,
				RunEnvironment.class);
			return "";
		}
	}

	private static String userName(Person account) {
		if (account == null) {
			return "";
		}
		try {
			String full = account.getFullName();
			if (full != null && !full.isBlank()) {
				return full;
			}
			return account.getName();
		} catch (RuntimeException ex) {
			Logger.debug("Failed to read the acting account's name.", ex, RunEnvironment.class);
			return "";
		}
	}

	/**
	 * The language the answer is expected in, named in that language and tagged, so a model that
	 * does not recognise the name still recognises the tag.
	 */
	private static String language(Locale locale) {
		String name = locale.getDisplayLanguage(locale);
		String tag = locale.toLanguageTag();
		if (name == null || name.isBlank()) {
			return tag;
		}
		return name + " (" + tag + ")";
	}

	/** Today in the account's own time zone - the day it is where the user is, not on the server. */
	private static String date(Person account) {
		ZoneId zone = zoneOf(account);
		return DAY.format(ZonedDateTime.now(zone)) + " (" + zone.getId() + ")";
	}

	/** The acting account's own time zone, the system's when it states none. */
	static ZoneId zoneOf(Person account) {
		if (account != null) {
			try {
				TimeZone zone = account.getTimeZone();
				if (zone != null) {
					return zone.toZoneId();
				}
			} catch (RuntimeException ex) {
				Logger.debug("Failed to read the acting account's time zone.", ex, RunEnvironment.class);
			}
		}
		return ZoneId.systemDefault();
	}

	/** The acting account's UI language, the session's or English when it states none. */
	static Locale localeOf(Person account) {
		if (account != null) {
			try {
				Locale locale = account.getLocale();
				if (locale != null) {
					return locale;
				}
			} catch (RuntimeException ex) {
				Logger.debug("Failed to read the acting account's locale.", ex, RunEnvironment.class);
			}
		}
		TLContext context = TLContext.getContext();
		Locale sessionLocale = context == null ? null : context.getCurrentLocale();
		return sessionLocale != null ? sessionLocale : Locale.ENGLISH;
	}

	/** The account this run acts for, or <code>null</code> outside a session. */
	static Person currentAccount() {
		TLContext context = TLContext.getContext();
		return context == null ? null : context.getPerson();
	}
}
