/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.Resources;

/**
 * What the host states about <em>this</em> turn - the facts that change while the conversation
 * runs.
 *
 * <p>
 * The counterpart of {@link RunEnvironment}, split from it by one question: does the value hold for
 * the whole conversation? A fact the host marked {@link EnvironmentFact#stable() stable} does, and
 * goes into the {@code <environment>} block of the system prompt. Everything else - which object is
 * open, which row is selected, which section the user is reading - changes with the next click, and
 * a system prompt that changes with it would miss the providers' prompt caches on every turn of the
 * conversation. Those facts travel here instead: in the turn itself, beside the user's text.
 * </p>
 *
 * <h2>What it looks like</h2>
 *
 * <pre>
 * &lt;turn-context&gt;
 * Turn: 3
 * Time: 14:32 (Europe/Berlin)
 * Open invoice: RE-2026-0042
 * &lt;/turn-context&gt;
 * </pre>
 *
 * <p>
 * The block is a content block of the user message, ahead of the user's own text, which stays
 * exactly what the user wrote - in the bubble, in the log and in the request. Every provider this
 * product speaks to renders a message's content parts as separate blocks, so the two arrive apart;
 * the {@code <turn-context>} tags are there because nothing guarantees a <em>visible</em> separator
 * between them once the model reads them. Self-delimiting text needs no {@code [user]} marker on the
 * other half, which is what lets the user's text stay untouched.
 * </p>
 *
 * <h2>Turn number and time of day</h2>
 *
 * <p>
 * Added by the framework, because both change per turn by nature and neither can therefore sit in
 * the environment block: the date there is stated at day granularity for exactly that reason.
 * </p>
 *
 * <h2>When there is no block</h2>
 *
 * <p>
 * Only when the host declared at least one per-turn fact. Turn number and time of day are added
 * <em>to</em> a block, they do not create one: an installation that states nothing per turn keeps
 * the turns it has today, with no tokens and no wording it never asked for.
 * </p>
 *
 * @see EnvironmentFact#stable()
 * @see RunEnvironment
 */
public final class TurnContext {

	/**
	 * What the system prompt says about this block, in English.
	 *
	 * <p>
	 * The shipped wording is the resource {@link EnvironmentSettings#TURN_CONTEXT_RESOURCE}, for the
	 * reason the template itself is a resource; this constant is the fallback, and a test pins the
	 * two together. It is rendered as the {@code {{turn_context}}} section of the environment block,
	 * and only for a run that actually gets a block - an installation that states nothing per turn
	 * is told nothing about a block it never sends.
	 * </p>
	 *
	 * <p>
	 * It says one thing the block itself cannot: which of several blocks is the current one. The
	 * blocks of the earlier turns stay in the history - that is what keeps them out of the prompt
	 * cache's way - so a model reading the conversation sees the selection as it was three turns ago
	 * beside the selection as it is now.
	 * </p>
	 */
	public static final String NOTE = "Live state: each user message carries a <turn-context> block"
		+ " ahead of the user's own text, naming what this application states about that moment -"
		+ " among it the object the user has selected. The newest block is what is true now; an"
		+ " earlier one describes a moment that has passed.";

	/** Opens the block. */
	public static final String OPEN_TAG = "<turn-context>";

	/** Closes the block. */
	public static final String CLOSE_TAG = "</turn-context>";

	/** Hours and minutes; the date is in the environment block, at day granularity. */
	private static final DateTimeFormatter TIME_OF_DAY = DateTimeFormatter.ofPattern("HH:mm");

	/** Either of this block's tags, wherever a fact tries to write one. */
	private static final Pattern BLOCK_TAG_IN_VALUE =
		Pattern.compile("</?turn-context\\s*>", Pattern.CASE_INSENSITIVE);

	private TurnContext() {
		// Static utility.
	}

	/**
	 * Composes the block one turn sends, in the language and time zone of the acting account.
	 *
	 * @param facts
	 *        Everything the host stated about this run; only the ones that are not
	 *        {@link EnvironmentFact#stable() stable} are rendered here. May be {@code null}.
	 * @param turnNumber
	 *        The 1-based turn of the conversation this run answers, or {@code 0} when the entry
	 *        point counts no turns (a headless or a scheduled run), in which case the line is
	 *        omitted.
	 * @return The block, or the empty string when the host stated no per-turn fact.
	 */
	public static String compose(List<EnvironmentFact> facts, int turnNumber) {
		if (perTurn(facts).isEmpty()) {
			// Cheap exit before anything is looked up: this is the case in every installation that
			// states nothing per turn, which is every one of them until a host writes a <fact>.
			return "";
		}
		Person account = RunEnvironment.currentAccount();
		Locale locale = RunEnvironment.localeOf(account);
		Resources resources = Resources.getInstance(locale);
		return render(facts, turnNumber,
			resources.getString(I18NConstants.TURN_CONTEXT_TURN),
			resources.getString(I18NConstants.TURN_CONTEXT_TIME),
			timeOfDay(RunEnvironment.zoneOf(account)));
	}

	/**
	 * The seam {@link #compose} is built on, and what a test uses: it looks nothing up, so it has no
	 * opinion about which services are running.
	 *
	 * @param turnLabel
	 *        What to call the turn number in front of the model.
	 * @param timeLabel
	 *        What to call the time of day in front of the model.
	 * @param time
	 *        The time of day, already formatted with its zone.
	 */
	static String render(List<EnvironmentFact> facts, int turnNumber, String turnLabel,
			String timeLabel, String time) {
		List<EnvironmentFact> perTurn = perTurn(facts);
		if (perTurn.isEmpty()) {
			return "";
		}
		StringBuilder block = new StringBuilder();
		block.append(OPEN_TAG).append('\n');
		if (turnNumber > 0) {
			block.append(line(turnLabel, String.valueOf(turnNumber)));
		}
		if (time != null && !time.isBlank()) {
			block.append(line(timeLabel, time));
		}
		for (EnvironmentFact fact : perTurn) {
			block.append(line(fact.name(), fact.value()));
		}
		block.append(CLOSE_TAG);
		return block.toString();
	}

	/** The facts of this block: the ones whose value does not hold for the whole conversation. */
	private static List<EnvironmentFact> perTurn(List<EnvironmentFact> facts) {
		if (facts == null || facts.isEmpty()) {
			return List.of();
		}
		List<EnvironmentFact> perTurn = new ArrayList<>(facts.size());
		for (EnvironmentFact fact : facts) {
			if (!fact.stable()) {
				perTurn.add(fact);
			}
		}
		return perTurn;
	}

	/**
	 * One {@code Label: value} line.
	 *
	 * <p>
	 * The block is read line by line, so a value keeps neither its line breaks - a second line would
	 * read as a fact of its own - nor a tag of this block's own, which would end the block early and
	 * let everything after it read as the user's words. The tag's leading {@code <} is escaped
	 * rather than the phrase deleted, the way {@code UntrustedEnvelope} does it: the text stays
	 * readable and structurally inert.
	 * </p>
	 */
	private static String line(String label, String value) {
		return flatten(label) + ": " + flatten(value) + "\n";
	}

	private static String flatten(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		String oneLine = text.replace("\r\n", " ").replace('\r', ' ').replace('\n', ' ');
		Matcher matcher = BLOCK_TAG_IN_VALUE.matcher(oneLine);
		if (!matcher.find()) {
			return oneLine;
		}
		StringBuilder out = new StringBuilder(oneLine.length() + 8);
		int copied = 0;
		do {
			out.append(oneLine, copied, matcher.start());
			// Keep the matched text verbatim apart from its opening angle bracket.
			out.append("&lt;").append(oneLine, matcher.start() + 1, matcher.end());
			copied = matcher.end();
		} while (matcher.find());
		out.append(oneLine, copied, oneLine.length());
		return out.toString();
	}

	/** The time it is where the user is, not on the server, with the zone it is stated in. */
	private static String timeOfDay(ZoneId zone) {
		return TIME_OF_DAY.format(ZonedDateTime.now(zone)) + " (" + zone.getId() + ")";
	}
}
