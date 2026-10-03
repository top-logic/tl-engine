/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.present;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.TidResolver;
import com.top_logic.tl.ai.core.tools.script.QueryLanguage;
import com.top_logic.tl.ai.core.tools.script.TLScriptExecutor;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Shows model objects to the user - as a table, a chart or a form - in whatever the user is looking
 * at beside the conversation.
 *
 * <p>
 * The agent says <em>what</em> to show: a TL-Script query or the ids of objects it already holds,
 * and how. This class finds the objects, with the acting user's rights, and checks that the request
 * can be shown at all; <em>where</em> it is shown is a subclass's business ({@link #deliver}). A
 * chat page puts it into its pane, an eval records it. An application gets the tool by registering
 * one of those subclasses as the factory {@code present}; tl-ai-core registers none, because without
 * a page there is nothing to show on. For the same reason a run that has no place to show anything
 * is not offered the tool at all ({@link #noPlaceToShow}).
 * </p>
 *
 * <h2>Why a query</h2>
 *
 * <p>
 * "Show me all events as a table" used to cost a model ten tool calls: load the skill with the
 * instance tools, list the instances, fetch each one, write a Markdown table, cite every row. With a
 * query the server finds the objects itself and the page renders them with their own columns, so the
 * same request is one call. The objects come back in the answer as {@code label [id:TID]}, so the
 * model can talk about them without fetching them again.
 * </p>
 *
 * <h2>What bounds it</h2>
 *
 * <ul>
 * <li><b>Rights.</b> A query's objects are reduced to the ones the acting user may read
 * ({@link ReadFilter}), the way {@code filterSecurity} would. Ids are resolved with the same rights
 * ({@link TidResolver}) and all or nothing: one unreadable or unknown id refuses the call, since a
 * page showing three of four objects the model named would quietly lie about what the user sees.</li>
 * <li><b>No writes.</b> The query and the chart functions are refused when they would change anything
 * ({@link TLScriptExecutor#compileReadOnly}). That is why the tool is {@link ToolMode#READ} and needs no
 * approval - the approval dialog would sit in the very page this is trying to fill.</li>
 * <li><b>Checked before shown.</b> A chart's functions are tried on every object first, so a function
 * that fails or a value that is not a number is answered as an error the model can correct, rather
 * than as an empty chart.</li>
 * </ul>
 */
public abstract class PresentTool {

	/** How many objects the answer lists before it only counts the rest. */
	public static final int MAX_LISTED = 50;

	/**
	 * Shows objects to the user beside the conversation.
	 *
	 * @param query
	 *        TL-Script expression evaluating to the objects to show; alternative to {@code ids}.
	 * @param ids
	 *        Ids of objects the model already holds; alternative to {@code query}.
	 * @param as
	 *        How to show them; {@code null} for a form of one object and a table of several.
	 * @param chart
	 *        What a chart computes from each object; required for {@link PresentAs#CHART}.
	 * @param title
	 *        What to call the view, or {@code null}.
	 * @param channel
	 *        Where on the page, for a page that offers more than one place; {@code null} otherwise.
	 * @param context
	 *        The run, supplying the rights the objects are found with.
	 * @return What the user now sees, with the objects as {@code label [id:TID]}, or why nothing was
	 *         shown.
	 */
	@ToolMethod(
		description = "Show model objects to the user beside this conversation, as a table, a chart or a "
			+ "form. Use it whenever the user asks to see, show, list, open or display objects, and "
			+ "for any answer that is a set of objects - never write such a set as a Markdown table. "
			+ "Pass a TL-Script 'query' that evaluates to the objects, e.g. all(`module:Type`), "
			+ "or all(`module:Type`).filter(x -> ...), and the server finds them with the user's "
			+ "rights: do not list or fetch them yourself first. Pass 'ids' instead only for objects "
			+ "you already hold. 'as': TABLE (columns come from the objects' type), CHART (needs "
			+ "'chart') or FORM (exactly one object); omitted, one object is a form and several are "
			+ "a table. The answer lists the objects as label [id:TID]. This changes no data. After "
			+ "it, answer in one or two sentences what the user now sees, and call no further tool.",
		mode = ToolMode.READ, group = ToolGroups.INTERACTION,
		marking = AnswerMarking.TOOL, unavailable = "noPlaceToShow")
	public String present(
			@P(value = "TL-Script expression evaluating to the objects to show, e.g. "
				+ "all(`crm:Customer`).filter(c -> $c.get(`crm:Customer#active`)). "
				+ "Use the qualified names outlineModel or searchModel reported.",
				required = false) String query,
			@P(value = "Ids of objects you already hold, as tool results wrote them ([id:...]). "
				+ "Instead of 'query'.", required = false) List<String> ids,
			@P(value = "TABLE, CHART or FORM. Omit to get a form for one object and a table for several.",
				required = false) PresentAs as,
			@P(value = "For as=CHART: kind (BAR, LINE or PIE), label and value - each a TL-Script "
				+ "function of one object, e.g. label: c -> $c.get(`crm:Customer#name`), value: "
				+ "c -> $c.get(`crm:Customer#revenue`).", required = false) ChartSpec chart,
			@P(value = "Short title of the view in the user's language, e.g. \"Umsatz pro Kunde\".",
				required = false) String title,
			@P(value = "Page channel to write. Omit it: only needed when the page offers several.",
				required = false) String channel,
			ToolContext context) {
		String expression = trimmed(query);
		List<String> named = cleaned(ids);
		if (expression.isEmpty() && named.isEmpty()) {
			return "Error: nothing to show. Pass a TL-Script 'query' evaluating to the objects, or their 'ids'.";
		}
		if (!expression.isEmpty() && !named.isEmpty()) {
			return "Error: pass either 'query' or 'ids', not both.";
		}

		ReadFilter readFilter = ReadFilter.of(context);
		List<TLObject> objects;
		if (expression.isEmpty()) {
			objects = new ArrayList<>();
			for (String id : named) {
				try {
					// A read check, with the acting user's rights: an object they may not see is
					// reported exactly like one that does not exist.
					objects.add(TidResolver.resolve(id, readFilter.security()));
				} catch (IllegalArgumentException ex) {
					// The resolver's message is a whole sentence that says which kind of wrong
					// identifier this was (TidDiagnostics); it is passed on as it stands.
					return "Nothing was presented: " + ex.getMessage();
				}
			}
		} else {
			TLScriptExecutor.ExecutionResult result = TLScriptExecutor.executeReadOnly(expression);
			if (!result.isSuccess()) {
				return "Nothing was presented: the query failed: " + (result.isRefusal()
					? result.getErrorMessage()
					: QueryLanguage.withReadingHelp(result.getErrorMessage()));
			}
			String notObjects = notObjects(result.getResult());
			if (notObjects != null) {
				return "Nothing was presented: the query must evaluate to model objects, but it returned "
					+ notObjects + ". Return the objects themselves; a table shows their attributes and "
					+ "a chart computes its values with 'chart'.";
			}
			objects = readFilter.readable(asObjects(result.getResult()));
		}
		// Duplicates drop out here: the same object can be named twice, or found twice, and a table
		// showing it twice would be the page saying something the model did not.
		objects = List.copyOf(new LinkedHashSet<>(objects));
		if (objects.isEmpty()) {
			return expression.isEmpty()
				? "Nothing was presented: no object."
				: "Nothing was presented: the query found no object the user may see. Tell the user.";
		}

		PresentAs shownAs = as != null ? as : (objects.size() == 1 ? PresentAs.FORM : PresentAs.TABLE);
		String name = trimmed(title);
		String heading = name.isEmpty() ? null : name;
		String source = expression.isEmpty() ? null : expression;
		Presentation presentation;
		List<Presentation.Point> points = List.of();
		try {
			switch (shownAs) {
				case FORM:
					if (objects.size() != 1) {
						return "Nothing was presented: a FORM shows exactly one object, and this is "
							+ objects.size() + ". Use TABLE.";
					}
					presentation = Presentation.of(PresentAs.FORM, objects, heading, source);
					break;
				case CHART:
					if (chart == null || chart.kind() == null) {
						return "Nothing was presented: a CHART needs 'chart' with kind, label and value.";
					}
					presentation = Presentation.chart(objects, chart, heading, source);
					points = presentation.points();
					break;
				default:
					presentation = Presentation.of(PresentAs.TABLE, objects, heading, source);
					break;
			}
		} catch (IllegalArgumentException ex) {
			return "Nothing was presented: " + ex.getMessage();
		}

		String refusal = deliver(presentation, trimmed(channel), context);
		if (refusal != null) {
			return refusal;
		}
		return describe(presentation, points);
	}

	/**
	 * Why the given run has no place to show anything, or <code>null</code> when it has one.
	 *
	 * <p>
	 * Asked before the model is offered this tool (see {@link ToolMethod#unavailable()}): a run that
	 * cannot be shown anything is not offered it, and answers a request to see objects in text instead
	 * of with a call that can only be refused. Every run has a place here; a subclass whose place a run
	 * can lack - a chat page that names no channel - says so, with the sentence {@link #deliver} would
	 * refuse with.
	 * </p>
	 *
	 * @param context
	 *        The run.
	 * @return The reason, phrased for the model, or <code>null</code>.
	 */
	protected String noPlaceToShow(ToolExecutionContext context) {
		return null;
	}

	/**
	 * Puts the presentation where the user sees it.
	 *
	 * @param presentation
	 *        What to show; its objects are readable by the acting user.
	 * @param channel
	 *        The place the model named, or the empty string when it named none.
	 * @param context
	 *        The run.
	 * @return {@code null} when the user now sees it, otherwise the sentence the model is answered
	 *         with instead - a whole sentence, since it is the tool's entire answer.
	 */
	protected abstract String deliver(Presentation presentation, String channel, ToolContext context);

	/**
	 * The answer to a call that reached the user: what is shown, and the objects as
	 * {@code label [id:TID]}, marked as business data.
	 */
	private static String describe(Presentation presentation, List<Presentation.Point> points) {
		List<TLObject> objects = presentation.objects();
		StringBuilder rows = new StringBuilder();
		int listed = Math.min(objects.size(), MAX_LISTED);
		for (int n = 0; n < listed; n++) {
			if (n > 0) {
				rows.append('\n');
			}
			if (points.isEmpty()) {
				rows.append(ReadFilter.labelled(objects.get(n)));
			} else {
				Presentation.Point point = points.get(n);
				rows.append(ReadFilter.withId(point.label() + ": " + number(point.value()),
					ReadFilter.idOf(point.object())));
			}
		}
		if (objects.size() > listed) {
			rows.append("\n... and ").append(objects.size() - listed).append(" more");
		}
		String what;
		switch (presentation.as()) {
			case FORM:
				what = "The user now sees the form of this object:";
				break;
			case CHART:
				what = "The user now sees a " + presentation.chart().kind().name().toLowerCase() + " chart of "
					+ objects.size() + (objects.size() == 1 ? " object" : " objects") + " (label: value):";
				break;
			default:
				what = "The user now sees a table of " + objects.size()
					+ (objects.size() == 1 ? " object:" : " objects:");
				break;
		}
		return what + "\n" + ReadFilter.marked("present", rows.toString())
			+ "\nDo not repeat these as a table. Say in one or two sentences what the user sees.";
	}

	/** A number as a person writes it: TL-Script computes in doubles, and 7.0 is 7. */
	private static String number(Number value) {
		double asDouble = value.doubleValue();
		if (asDouble == Math.rint(asDouble) && !Double.isInfinite(asDouble) && Math.abs(asDouble) < 1e15) {
			return Long.toString((long) asDouble);
		}
		return String.valueOf(value);
	}

	/** What a query result is instead of model objects, or {@code null} if it is model objects. */
	private static String notObjects(Object result) {
		if (result == null || result instanceof TLObject) {
			return null;
		}
		if (result instanceof Collection<?> many) {
			for (Object element : many) {
				if (element != null && !(element instanceof TLObject)) {
					return "a list containing " + kindOf(element);
				}
			}
			return null;
		}
		return kindOf(result);
	}

	private static String kindOf(Object value) {
		if (value instanceof Number) {
			return "a number";
		}
		if (value instanceof CharSequence) {
			return "a text";
		}
		if (value instanceof java.util.Map) {
			return "a dictionary";
		}
		return "a value of type " + value.getClass().getSimpleName();
	}

	private static List<TLObject> asObjects(Object result) {
		if (result == null) {
			return List.of();
		}
		if (result instanceof TLObject object) {
			return List.of(object);
		}
		List<TLObject> objects = new ArrayList<>();
		for (Object element : (Collection<?>) result) {
			if (element != null) {
				objects.add((TLObject) element);
			}
		}
		return objects;
	}

	private static String trimmed(String value) {
		return value == null ? "" : value.trim();
	}

	/** The given ids without the blank ones, in the order they were named. */
	private static List<String> cleaned(List<String> ids) {
		if (ids == null) {
			return List.of();
		}
		Set<String> result = new LinkedHashSet<>();
		for (String id : ids) {
			if (id != null && !id.isBlank()) {
				result.add(id.trim());
			}
		}
		return new ArrayList<>(result);
	}
}
