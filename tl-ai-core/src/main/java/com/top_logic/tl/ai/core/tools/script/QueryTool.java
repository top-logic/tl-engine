/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Answers a question about the data with one TL-Script expression, evaluated on the server.
 *
 * <p>
 * The tool for an answer that is <em>stated</em> rather than shown: "which event has the most
 * seats?", "how many registrations are there?". Selecting, sorting, maximum and minimum, top n,
 * counting, summing and filtering happen inside the expression, so the model reads the result and
 * never the rows it was computed from - which is what keeps a question over a thousand objects as
 * cheap as one over four (#616). To show objects, {@code present} takes the same kind of query.
 * </p>
 *
 * <h2>What bounds it</h2>
 *
 * <ul>
 * <li><b>Read only.</b> An expression that would create, set or delete anything - or have any other
 * side effect, anywhere in it - is refused before it runs
 * ({@link TLScriptExecutor#compileReadOnly(String)}), so the tool is {@link ToolMode#READ}, needs no
 * approval, and can be a core tool of an agent. Changing data is {@code changeData}'s, with its
 * approval.</li>
 * <li><b>Rights.</b> TL-Script reads with the model access rights of the acting user, and an object
 * in the result the user may not read is left out ({@link ReadFilter}).</li>
 * <li><b>Size.</b> A list in the result is cut at {@link #MAX_ROWS} entries and says how many there
 * were: a question whose answer is a thousand rows is a question for {@code present}.</li>
 * </ul>
 *
 * <p>
 * Named {@code query} because that is what the model does with it, and because {@code present}'s
 * parameter of the same name takes the same expressions: what one of them is told about writing a
 * query holds for the other. A query that fails - it does not compile, or fails while it runs - is
 * answered with the language's reading functions ({@link QueryLanguage#READING}), which the
 * description leaves out.
 * </p>
 */
public class QueryTool {

	/** How many entries of a list result are written out. */
	public static final int MAX_ROWS = 50;

	/**
	 * Creates a {@link QueryTool}.
	 */
	public QueryTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
	}

	/**
	 * Evaluates a read-only TL-Script expression and returns its result.
	 *
	 * @param expression
	 *        The expression.
	 * @param context
	 *        The run, supplying the rights the result is filtered with.
	 * @return The result: a value, an object as {@code label [type:...] [id:...]}, or one line per
	 *         entry of a list; or why it could not be evaluated.
	 */
	@ToolMethod(
		description = "Answer a question about the data with one read-only TL-Script expression, "
			+ "evaluated on the server with the user's rights. Put selecting, sorting, max/min, "
			+ "top-n, counting, summing and filtering INTO the expression and read only the result - "
			+ "never list instances to evaluate rows yourself. E.g. "
			+ "all(`crm:Customer`).size(), "
			+ "all(`crm:Customer`).sort((c -> $c.get(`crm:Customer#revenue`)).comparator().desc()).firstElement() "
			+ "(sort takes a comparator, not a key), or "
			+ "all(`crm:Customer`).map(c -> $c.get(`crm:Customer#revenue`)).sum(). "
			+ "Objects come back as label [type:...] [id:...]; a list is cut at 50 entries. "
			+ "The expression must not change data. To SHOW objects to the user, call present "
			+ "with the query instead.",
		mode = ToolMode.READ, group = ToolGroups.DATA_READ,
		marking = AnswerMarking.TOOL)
	public String query(
			@P("TL-Script expression, using the qualified names outlineModel or searchModel reported.")
			String expression,
			ToolContext context) {
		String source = expression == null ? "" : expression.trim();
		if (source.isEmpty()) {
			return "Error: no expression. Pass a TL-Script expression, e.g. all(`crm:Customer`).size().";
		}
		TLScriptExecutor.ExecutionResult result = TLScriptExecutor.executeReadOnly(source);
		if (!result.isSuccess()) {
			// Help on demand: the language's functions go with a query that failed for how it is
			// written, and with nothing else - a sheet in this tool's description would be paid for in
			// every request. A query refused for changing data is not helped by it.
			return "Error: " + (result.isRefusal() ? result.getErrorMessage()
				: QueryLanguage.withReadingHelp(result.getErrorMessage()));
		}
		Object visible = ReadFilter.of(context).visible(result.getResult());
		return ReadFilter.marked("query", render(visible));
	}

	/** The result as the model reads it: one value, or one line per entry of a list. */
	static String render(Object value) {
		if (value instanceof Collection<?> many) {
			List<Object> entries = new ArrayList<>(many);
			if (entries.isEmpty()) {
				return "(empty list)";
			}
			StringBuilder out = new StringBuilder();
			int shown = Math.min(entries.size(), MAX_ROWS);
			for (int n = 0; n < shown; n++) {
				if (n > 0) {
					out.append('\n');
				}
				out.append(one(entries.get(n)));
			}
			if (entries.size() > shown) {
				out.append("\n... and ").append(entries.size() - shown).append(" more (")
					.append(entries.size()).append(" in all). Narrow the expression, aggregate it,")
					.append(" or show the list with present.");
			}
			return out.toString();
		}
		return one(value);
	}

	private static String one(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof TLObject object) {
			TLStructuredType type = object.tType();
			if (type == null) {
				return ReadFilter.labelled(object);
			}
			return ReadFilter.withId(MetaLabelProvider.INSTANCE.getLabel(object)
				+ " [type:" + TLModelUtil.qualifiedName(type) + "]", ReadFilter.idOf(object));
		}
		if (value instanceof Number number) {
			double asDouble = number.doubleValue();
			if (asDouble == Math.rint(asDouble) && !Double.isInfinite(asDouble) && Math.abs(asDouble) < 1e15) {
				return Long.toString((long) asDouble);
			}
			return String.valueOf(number);
		}
		if (value instanceof Map<?, ?> map) {
			StringBuilder out = new StringBuilder("{");
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (out.length() > 1) {
					out.append(", ");
				}
				out.append(entry.getKey()).append(": ").append(one(entry.getValue()));
			}
			return out.append('}').toString();
		}
		if (value instanceof Collection<?> nested) {
			List<String> parts = new ArrayList<>();
			for (Object element : nested) {
				parts.add(one(element));
			}
			return parts.toString();
		}
		return SearchExpression.asString(value);
	}
}
