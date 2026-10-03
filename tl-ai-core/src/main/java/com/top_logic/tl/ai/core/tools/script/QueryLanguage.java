/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.script;

/**
 * The functions of the application's query language (TL-Script) that a model is told about: a cheat
 * sheet whose every line was run through the engine.
 *
 * <p>
 * No model has seen the language in training, and what it guesses of it is mostly wrong in the same
 * few places: months counted from zero, a date that is no calendar, {@code sort} taking a comparator.
 * The sheet says those once, where a model reads them:
 * </p>
 * <ul>
 * <li>{@link #READING} is appended to the answer of {@code query} or {@code present} whose query
 * fails - on demand, because a sheet in the tool's description would be paid for in every request, and
 * most questions need none of it.</li>
 * <li>{@link #CHEAT_SHEET}, reading and writing, is what a skill gets in place of
 * {@link #PLACEHOLDER} - the skill that teaches {@code changeData}, which a model loads before it writes
 * a change.</li>
 * </ul>
 *
 * <p>
 * Model-facing English, like every tool answer: the model writes the same language whoever started
 * the run.
 * </p>
 */
public final class QueryLanguage {

	/**
	 * What a skill's content writes where the whole cheat sheet belongs, see
	 * {@link #expand(String)}.
	 */
	public static final String PLACEHOLDER = "<<QUERY_LANGUAGE_CHEAT_SHEET>>";

	/** The functions for reading and computing. */
	public static final String READING = "Reading:\n"
		+ "- all(`m:T`) - every instance of a type.\n"
		+ "- \"T:123\".objectResolveKey() - the object with a known id, written exactly as your context or a tool"
		+ " answer gives it ('TypeName:id'); null if it does not exist or the user may not read it. Use it, never"
		+ " a search by name, for an object you already know.\n"
		+ "- $x.get(`m:T#attr`) - an attribute value; a list for an attribute with several values.\n"
		+ "- $x.referers(`m:Other#ref`) - the objects whose reference ref points to $x.\n"
		+ "- $set.filter(x -> $x.get(`m:T#attr`) == 5), $set.map(x -> ...), $set.size(), $set.isEmpty(),"
		+ " $set.firstElement(), $set.singleElement() (fails unless there is exactly one), $set.toSet()"
		+ " (without duplicates).\n"
		+ "- Numbers of a set: $set.map(x -> $x.get(`m:T#n`)).sum(), also .max(), .min(), .average() -"
		+ " numbers only, not dates.\n"
		+ "- Sort: $set.sort((x -> $x.get(`m:T#attr`)).comparator()), descending .comparator().desc(). The"
		+ " latest date is the first element of the descending sort.\n"
		+ "- Group: $set.groupBy(x -> $x.get(`m:T#status`), g -> $g.size()) - a dictionary from each value"
		+ " to the number of elements that have it; the second function may compute anything else from"
		+ " the group $g.\n"
		+ "- Conditions: ==, !=, <, <=, >, >=, && (or and), || (or or), !, ifElse(cond, then, else). An"
		+ " enumeration value is the literal `m:Enum#NAME`, never a string.\n"
		+ "- Texts: $s.isStringEqual(\"text\") to compare (not ==), $s.stringContains(\"part\"),"
		+ " $s.toLowerCase(), toString($x) for a label.\n"
		+ "- Dates: today(); compare a date attribute directly, $d >= today(). Months count from 0:"
		+ " date(2027, 4, 14) is 14 May 2027. Parts of a date: $d.toUserCalendar().year(), .month()"
		+ " (0 = January), .day(). Thirty days later: today().toUserCalendar().withDayAdded(30).toDate(),"
		+ " likewise withMonthAdded and withYearAdded. The month as a text: dateFormat(\"yyyy-MM\").format($d).\n"
		+ "- round(value: $n, digits: 2), $x.instanceOf(`m:T`).\n"
		+ "- filterSecurity(all(`m:T`)) - only the objects the user may read. Use it before you count objects"
		+ " or turn them into text: a number and a label are plain values and are not filtered afterwards.\n"
		+ "- Several results: a dictionary with quoted keys, {\"open\": $a.size(), \"total\": $b.size()}"
		+ " (not {open = ...}).\n";

	/** The functions for changing data, which only {@code changeData} runs. */
	public static final String WRITING = "Writing (changeData only):\n"
		+ "- $x.set(`m:T#attr`, value) - replaces the value; a list for an attribute with several values.\n"
		+ "- $x.add(`m:T#attr`, $v) / $x.remove(`m:T#attr`, $v) - one element of such an attribute.\n"
		+ "- $set.foreach(x -> $x.set(`m:T#attr`, ...)) - the same change on every element.\n"
		+ "- new(`m:T`), or new(type: `m:T`, context: $parent) for a part of $parent, placed with"
		+ " $parent.add(`m:P#parts`, $child).\n"
		+ "- $x.delete().\n"
		+ "- Several steps: a block { a = ...; b = ...; $b.set(...) } - bind a name without $, read it with $.\n"
		+ "Example - raise the price of every open item by 10 percent:\n"
		+ "  all(`m:Item`).filter(i -> $i.get(`m:Item#status`) == `m:Status#OPEN`).foreach(i ->"
		+ " $i.set(`m:Item#price`, round(value: $i.get(`m:Item#price`) * 1.1, digits: 2)))\n";

	/** The whole sheet. */
	public static final String CHEAT_SHEET = READING + WRITING;

	private QueryLanguage() {
		// Constant holder.
	}

	/**
	 * The answer to a query that failed for how it is written - it does not compile, or it fails while
	 * it runs: the engine's message, and what the language offers for reading.
	 *
	 * @param message
	 *        Why the query failed.
	 */
	public static String withReadingHelp(String message) {
		return message + "\n\nThe functions of the query language that are verified to work"
			+ " (`m:T` stands for the qualified names outlineModel or searchModel reported):\n" + READING;
	}

	/**
	 * The given skill content with the cheat sheet in place of {@link #PLACEHOLDER}.
	 *
	 * <p>
	 * So that a skill that teaches the language - the framework's, or an application's own - carries
	 * the sheet that is verified against the engine, rather than a copy that stops being true.
	 * </p>
	 *
	 * @param content
	 *        A skill's content; may be <code>null</code>.
	 */
	public static String expand(String content) {
		if (content == null || !content.contains(PLACEHOLDER)) {
			return content;
		}
		return content.replace(PLACEHOLDER, CHEAT_SHEET.strip());
	}
}
