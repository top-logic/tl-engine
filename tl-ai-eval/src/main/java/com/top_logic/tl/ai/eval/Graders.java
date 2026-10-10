/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.model.TidResolver;
import com.top_logic.tl.ai.core.tools.present.ChartKind;
import com.top_logic.tl.ai.core.tools.present.ChartSpec;
import com.top_logic.tl.ai.core.tools.present.PresentAs;
import com.top_logic.tl.ai.core.tools.present.Presentation;
import com.top_logic.tl.ai.core.tools.script.TLScriptExecutor;

/**
 * Deciding whether an {@link Expectation} held.
 *
 * <p>
 * Every grader here is positive and total: it either establishes the claim or reports what it found
 * instead. None of them can pass by finding nothing, and none of them returns "inconclusive" - an
 * expectation that cannot be evaluated is a failed expectation, with the reason in its
 * {@link Verdict#detail() detail}, because a grader that silently abstained would turn a broken
 * eval into a green one.
 * </p>
 */
public class Graders {

	/** The bare name of the tool whose presentation {@link Presented} grades. */
	private static final String PRESENT = "present";

	/** A web link in Markdown: {@code [text](url)} or {@code [text](url "title")}. */
	private static final Pattern WEB_LINK = Pattern.compile("\\[[^\\]]*\\]\\((https?://[^\\s)]+)(?:\\s+\"[^\"]*\")?\\)");

	private Graders() {
		// Static utility.
	}

	/**
	 * Grades every expectation of the scenario, in configuration order.
	 *
	 * @param toolCalls
	 *        Every tool the run dispatched, by qualified name, in call order.
	 */
	public static List<Verdict> grade(EvalScenario scenario, List<String> toolCalls) {
		return grade(scenario, toolCalls, List.of());
	}

	/**
	 * Grades every expectation of the scenario, in configuration order.
	 *
	 * @param toolCalls
	 *        Every tool the run dispatched, by qualified name, in call order.
	 * @param calls
	 *        The calls that ran, with their arguments, for the graders that read what a call did.
	 */
	public static List<Verdict> grade(EvalScenario scenario, List<String> toolCalls,
			List<RunObservation.Call> calls) {
		return grade(scenario, toolCalls, calls, RunAnswer.none(scenario.getPrompt()), Judge.NONE);
	}

	/**
	 * Grades every expectation of the scenario, in configuration order, including those on the
	 * answer.
	 *
	 * @param answer
	 *        What the run answered.
	 * @param judge
	 *        The model that decides the {@link JudgeExpectation}s.
	 */
	public static List<Verdict> grade(EvalScenario scenario, List<String> toolCalls,
			List<RunObservation.Call> calls, RunAnswer answer, Judge judge) {
		List<Verdict> result = new ArrayList<>();
		for (Expectation expectation : scenario.getExpectations()) {
			result.add(grade(expectation, toolCalls, calls, answer, judge));
		}
		return result;
	}

	/** Grades one expectation of a run whose calls' arguments are not at hand. */
	public static Verdict grade(Expectation expectation, List<String> toolCalls) {
		return grade(expectation, toolCalls, List.of());
	}

	/** Grades one expectation of a run whose answer is not at hand. */
	public static Verdict grade(Expectation expectation, List<String> toolCalls, List<RunObservation.Call> calls) {
		return grade(expectation, toolCalls, calls, RunAnswer.none(""), Judge.NONE);
	}

	/** Grades one expectation. */
	public static Verdict grade(Expectation expectation, List<String> toolCalls, List<RunObservation.Call> calls,
			RunAnswer answer, Judge judge) {
		if (expectation instanceof CitationExpectation citations) {
			return gradeCitations(citations, answer);
		}
		if (expectation instanceof JudgeExpectation rubric) {
			return judge.grade(rubric, answer);
		}
		if (expectation instanceof KbQuery query) {
			return gradeKbQuery(query);
		}
		if (expectation instanceof ToolSequence tools) {
			return gradeToolSequence(tools, toolCalls);
		}
		if (expectation instanceof Presented presented) {
			return gradePresented(presented, calls);
		}
		throw new IllegalArgumentException("No grader for expectation type "
			+ expectation.getClass().getName() + "; add one beside the others in " + Graders.class);
	}

	/**
	 * Evaluates the expression against the knowledge base as it stands now and compares the result,
	 * as text, with the written value.
	 *
	 * <p>
	 * Runs on the calling thread, so it sees the data with that thread's rights and at that thread's
	 * revision. The runner advances the session revision before grading, because the agent commits
	 * through its own transaction scope and an un-advanced reader would grade the state from before
	 * the run.
	 * </p>
	 */
	private static Verdict gradeKbQuery(KbQuery query) {
		String label = label(query, "kb-query " + query.getExpression());
		TLScriptExecutor.ExecutionResult evaluated;
		try {
			// Through the same executor the agent's own script tool uses, and without a commit: the
			// grader reads, with this thread's rights, exactly what an operator checking the result
			// by hand would read. A second evaluator would be a second answer to "what does this
			// expression mean".
			evaluated = TLScriptExecutor.execute(query.getExpression(), List.of());
		} catch (RuntimeException ex) {
			return Verdict.fail(label, "The expression did not evaluate: " + ex.getMessage()
				+ " -- expression: " + query.getExpression());
		}
		if (!evaluated.isSuccess()) {
			return Verdict.fail(label, "The expression did not evaluate: "
				+ evaluated.getErrorMessage() + " -- expression: " + query.getExpression());
		}
		String actual = render(evaluated.getResult());
		if (query.getExpected().equals(actual)) {
			return Verdict.pass(label, "produced '" + actual + "' as expected");
		}
		return Verdict.fail(label,
			"expected '" + query.getExpected() + "' but produced '" + actual + "'"
				+ " -- expression: " + query.getExpression());
	}

	/**
	 * Renders a TL-Script result for comparison with a configured string.
	 *
	 * <p>
	 * A whole-number {@code Double} is rendered without its decimal point, because TL-Script's
	 * {@code count(...)} yields {@code 4.0} and nobody writing a scenario would think to write that.
	 * Everything else is its {@code toString()}, and a scenario whose expression produces something
	 * whose rendering is not obvious should be rewritten to produce a scalar.
	 * </p>
	 */
	static String render(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof Number number) {
			double asDouble = number.doubleValue();
			if (asDouble == Math.rint(asDouble) && !Double.isInfinite(asDouble)) {
				return Long.toString((long) asDouble);
			}
		}
		return String.valueOf(value);
	}

	/** Checks that every required tool was called and no forbidden one was. */
	private static Verdict gradeToolSequence(ToolSequence expectation, List<String> called) {
		String label = label(expectation, "tool-sequence");
		if (expectation.getRequired().isEmpty() && expectation.getForbidden().isEmpty()) {
			return Verdict.fail(label, "This expectation names neither a required nor a forbidden"
				+ " tool, so it asserts nothing.");
		}
		List<String> missing = new ArrayList<>();
		for (String required : expectation.getRequired()) {
			if (!wasCalled(required, called)) {
				missing.add(required);
			}
		}
		List<String> forbidden = new ArrayList<>();
		for (String name : expectation.getForbidden()) {
			if (wasCalled(name, called)) {
				forbidden.add(name);
			}
		}
		int maxCalls = expectation.getMaxCalls();
		boolean tooMany = maxCalls > 0 && called.size() > maxCalls;
		if (missing.isEmpty() && forbidden.isEmpty() && !tooMany) {
			return Verdict.pass(label, "called " + expectation.getRequired()
				+ " and none of " + expectation.getForbidden()
				+ (maxCalls > 0 ? ", in " + called.size() + " of at most " + maxCalls + " calls" : ""));
		}
		StringBuilder detail = new StringBuilder();
		if (!missing.isEmpty()) {
			detail.append("never called ").append(missing);
		}
		if (!forbidden.isEmpty()) {
			if (detail.length() > 0) {
				detail.append("; ");
			}
			detail.append("called the forbidden ").append(forbidden);
		}
		if (tooMany) {
			if (detail.length() > 0) {
				detail.append("; ");
			}
			detail.append("made ").append(called.size()).append(" tool calls, at most ").append(maxCalls)
				.append(" allowed");
		}
		detail.append(" -- calls made: ").append(called);
		return Verdict.fail(label, detail.toString());
	}

	/**
	 * Checks the last {@code present} call: the objects it showed, by evaluating its query - or
	 * resolving its ids - the way the tool does, and how it showed them.
	 */
	private static Verdict gradePresented(Presented expectation, List<RunObservation.Call> calls) {
		String label = label(expectation, "presented");
		RunObservation.Call last = null;
		List<String> names = new ArrayList<>();
		for (RunObservation.Call call : calls) {
			names.add(call.name());
			if (PRESENT.equals(bareName(call.name()))) {
				last = call;
			}
		}
		if (last == null) {
			return Verdict.fail(label, "Nothing was presented -- calls made: " + names);
		}
		Map<String, Object> args;
		try {
			args = ToolCatalog.parseJson(last.argsJson());
		} catch (IOException | RuntimeException ex) {
			return Verdict.fail(label, "The present call's arguments do not parse: " + last.argsJson());
		}

		List<TLObject> rows = new ArrayList<>();
		Object query = args.get("query");
		if (query instanceof String text && !text.isBlank()) {
			TLScriptExecutor.ExecutionResult result = TLScriptExecutor.execute(text, List.of());
			if (!result.isSuccess()) {
				return Verdict.fail(label, "The presented query did not evaluate: " + result.getErrorMessage()
					+ " -- query: " + text);
			}
			Object value = result.getResult();
			for (Object element : value instanceof Collection<?> many ? many : List.of(value)) {
				if (element instanceof TLObject object && !rows.contains(object)) {
					rows.add(object);
				}
			}
		} else if (args.get("ids") instanceof Collection<?> ids) {
			for (Object id : ids) {
				try {
					TLObject object = TidResolver.resolve(String.valueOf(id), ModelSecurity.UNCHECKED);
					if (!rows.contains(object)) {
						rows.add(object);
					}
				} catch (IllegalArgumentException ex) {
					return Verdict.fail(label, "A presented id names nothing: " + ex.getMessage());
				}
			}
		}

		Object asArg = args.get("as");
		PresentAs shownAs = asArg instanceof String name && !name.isBlank()
			? PresentAs.valueOf(name.trim())
			: (rows.size() == 1 ? PresentAs.FORM : PresentAs.TABLE);
		if (expectation.getAs() != null && expectation.getAs() != shownAs) {
			return Verdict.fail(label, "shown as " + shownAs + ", expected " + expectation.getAs());
		}
		if (expectation.getChartKind() != null) {
			Object chart = args.get("chart");
			Object kind = chart instanceof Map<?, ?> spec ? spec.get("kind") : null;
			if (!expectation.getChartKind().name().equals(kind)) {
				return Verdict.fail(label, "chart kind " + kind + ", expected " + expectation.getChartKind());
			}
		}

		// A chart is graded on what it draws, not only on whom: the values the model's functions
		// compute, in row order, computed the way the tool computes them.
		List<Object> values = new ArrayList<>();
		if (shownAs == PresentAs.CHART && args.get("chart") instanceof Map<?, ?> spec) {
			try {
				ChartSpec chart = new ChartSpec(ChartKind.valueOf(String.valueOf(spec.get("kind"))),
					(String) spec.get("label"), (String) spec.get("value"));
				for (Presentation.Point point : Presentation.chart(rows, chart, null, null).points()) {
					values.add(point.value());
				}
			} catch (RuntimeException ex) {
				return Verdict.fail(label, "The presented chart does not compute: " + ex.getMessage());
			}
		}

		TLScriptExecutor.ExecutionResult evaluated =
			TLScriptExecutor.execute(expectation.getExpression(), List.of(rows, values));
		if (!evaluated.isSuccess()) {
			return Verdict.fail(label, "The expression did not evaluate: " + evaluated.getErrorMessage()
				+ " -- expression: " + expectation.getExpression());
		}
		String actual = render(evaluated.getResult());
		if (expectation.getExpected().equals(actual)) {
			return Verdict.pass(label, "showed " + rows.size() + " object(s) as " + shownAs + ", and '" + actual
				+ "' as expected");
		}
		return Verdict.fail(label, "expected '" + expectation.getExpected() + "' but produced '" + actual
			+ "' over the " + rows.size() + " presented object(s) -- expression: " + expectation.getExpression());
	}

	/**
	 * Whether the run called the tool a scenario named.
	 *
	 * <p>
	 * A scenario may write the qualified name an administrator configures ({@code
	 * script-exec.changeData}) or the provider-safe form ({@code script-exec__changeData}),
	 * while a run records the bare name the model actually used ({@code changeData}) - a tool
	 * reaches an agent's catalogue under its own name, not under its factory's. Accepting the
	 * qualified spelling and comparing on its last segment lets a scenario say which factory it
	 * means, which is what a reader of the file needs, without the grader silently missing every
	 * call.
	 * </p>
	 */
	static boolean wasCalled(String configured, List<String> called) {
		return called.contains(configured) || called.contains(bareName(configured));
	}

	/** The last segment of a name qualified with {@code .} or {@code __}. */
	static String bareName(String name) {
		int dot = name.lastIndexOf('.');
		int underscores = name.lastIndexOf("__");
		int cut = Math.max(dot + 1, underscores < 0 ? 0 : underscores + 2);
		return name.substring(cut);
	}

	/**
	 * Counts the distinct sources the answer cites, their hosts, and the web links of the model's text
	 * that point at no source.
	 */
	static Verdict gradeCitations(CitationExpectation expectation, RunAnswer answer) {
		String label = label(expectation, CitationExpectation.TAG_NAME);
		Set<String> sources = new LinkedHashSet<>();
		Set<String> hosts = new TreeSet<>();
		Set<String> cited = new HashSet<>();
		for (Citation citation : answer.citations()) {
			String address = citation.url().isEmpty() ? citation.id() : citation.url();
			sources.add(address);
			cited.add(normalized(citation.url()));
			cited.add(normalized(citation.id()));
			String host = host(address);
			if (host != null) {
				hosts.add(host);
			}
		}
		List<String> unread = new ArrayList<>();
		Matcher links = WEB_LINK.matcher(answer.rawAnswer());
		while (links.find()) {
			String address = links.group(1);
			if (!cited.contains(normalized(address))) {
				unread.add(address);
			}
		}

		List<String> problems = new ArrayList<>();
		if (sources.size() < expectation.getMinSources()) {
			problems.add("cites " + sources.size() + " sources, at least " + expectation.getMinSources()
				+ " required");
		}
		if (hosts.size() < expectation.getMinDomains()) {
			problems.add("cites " + hosts.size() + " hosts " + hosts + ", at least " + expectation.getMinDomains()
				+ " required");
		}
		if (unread.size() > expectation.getMaxUnreadLinks()) {
			problems.add("links " + unread.size() + " pages no run read, at most " + expectation.getMaxUnreadLinks()
				+ " allowed: " + unread);
		}
		String counts = sources.size() + " sources from " + hosts.size() + " hosts, " + unread.size()
			+ " unread links";
		if (problems.isEmpty()) {
			return Verdict.pass(label, counts);
		}
		return Verdict.fail(label, String.join("; ", problems) + " -- " + counts);
	}

	/** An address as compared: without a fragment and without a trailing slash. */
	private static String normalized(String address) {
		String result = address == null ? "" : address.strip();
		int hash = result.indexOf('#');
		if (hash >= 0) {
			result = result.substring(0, hash);
		}
		while (result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	/** The host of a web address without {@code www.}, or {@code null} for anything else. */
	private static String host(String address) {
		try {
			String host = URI.create(address.strip()).getHost();
			if (host == null) {
				return null;
			}
			host = host.toLowerCase(Locale.ROOT);
			return host.startsWith("www.") ? host.substring(4) : host;
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static String label(Expectation expectation, String fallback) {
		String label = expectation.getLabel();
		return label == null || label.isEmpty() ? fallback : label;
	}
}
