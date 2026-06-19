/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.impl.DefaultColumn;

/**
 * A fully model- and script-defined column filter: the filter UI is a form over a transient
 * instance of a configured {@link #getModel() parameter type}, and the filter logic is a TL-Script
 * {@link #getMatch() match function}.
 *
 * <p>
 * The match function is a <em>closure builder</em>:
 * </p>
 *
 * <pre>
 * match : (params [, ...inputs]) -&gt; ( (value, row) -&gt; Boolean )
 * </pre>
 *
 * <p>
 * It is evaluated <em>once</em> per filter application (binding the form's parameter object and any
 * configured {@link #getInputs() extra inputs}) to produce a predicate closure, which is then
 * applied per row with the column's cell {@code value} and the whole {@code row} as context - so
 * expensive parameter-derived setup happens once, not per row.
 * </p>
 *
 * <p>
 * The filter integrates its own column and dialog by implementing {@link ColumnBinding}: the column
 * cell carries a {@link ScriptedCell} (value + row), so the filter sees both without knowing the
 * column attribute, and {@link #installUI} registers the {@link ScriptedFilterUI} form.
 * </p>
 */
public class ScriptedFilter implements ColumnFilter<ScriptedCell>, ColumnBinding {

	/**
	 * Configuration of a {@link ScriptedFilter}.
	 */
	public interface Config extends PolymorphicConfiguration<ScriptedFilter> {

		/** Configuration name of {@link #getModel()}. */
		String MODEL = "model";

		/** Configuration name of {@link #getMatch()}. */
		String MATCH = "match";

		/** Configuration name of {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name of {@link #getLabel()}. */
		String LABEL = "label";

		/**
		 * The model type whose attributes are the filter parameters (rendered as the filter form).
		 */
		@Name(MODEL)
		@Mandatory
		TLModelPartRef getModel();

		/**
		 * The closure-building match function {@code (params [, ...inputs]) -> ((value, row) ->
		 * Boolean)}.
		 */
		@Name(MATCH)
		@Mandatory
		@NonNullable
		Expr getMatch();

		/**
		 * References to channels whose values are passed to the match function after the parameter
		 * object, to pull additional context into the filter.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * The dialog title / column-filter label.
		 */
		@Name(LABEL)
		ResKey getLabel();

	}

	private final QueryExecutor _match;

	private final TLStructuredType _filterType;

	private final List<ChannelRef> _inputs;

	private final ResKey _label;

	/**
	 * Creates a {@link ScriptedFilter} from configuration.
	 */
	@CalledByReflection
	public ScriptedFilter(InstantiationContext context, Config config) {
		_match = QueryExecutor.compile(config.getMatch());
		_filterType = (TLStructuredType) config.getModel().resolveType();
		_inputs = config.getInputs();
		_label = config.getLabel();
	}

	/**
	 * The filter-parameter type whose transient instance backs the filter form.
	 */
	public TLStructuredType filterType() {
		return _filterType;
	}

	/**
	 * The dialog / filter label, or {@code null}.
	 */
	public ResKey label() {
		return _label;
	}

	@Override
	public Column<Object, ?> createColumn(ColumnSetup setup) {
		String attribute = setup.attribute();
		return DefaultColumn.<Object, ScriptedCell> builder(attribute,
			row -> new ScriptedCell(ColumnProviderService.attributeValue(row, attribute), row))
			.label(setup.label())
			.renderer(cell -> CellContent.text(ColumnProviderService.label(cell.value())))
			.sort(() -> Comparator.comparing((ScriptedCell cell) -> ColumnProviderService.label(cell.value())))
			.filter(this)
			.build();
	}

	@Override
	public void installUI(ColumnSetup setup, TableViewControl<Object> control) {
		List<ViewChannel> inputChannels = new ArrayList<>();
		for (ChannelRef ref : _inputs) {
			inputChannels.add(setup.viewContext().resolveChannel(ref));
		}
		control.setFilterUI(setup.attribute(), new ScriptedFilterUI(this, inputChannels));
	}

	@Override
	public FilterInput input() {
		return new FilterInput.Form();
	}

	@Override
	public Predicate<ScriptedCell> predicate(FilterState state) {
		ScriptedFilterState scripted = (ScriptedFilterState) state;
		Object[] inputValues = scripted.inputs();
		Object[] args = new Object[1 + inputValues.length];
		args[0] = scripted.params();
		System.arraycopy(inputValues, 0, args, 1, inputValues.length);

		// Build the predicate closure once, then apply it per row.
		Object closureValue = _match.execute(args);
		if (!(closureValue instanceof SearchExpression closure)) {
			return cell -> true;
		}
		EvalContext context = _match.context();
		return cell -> Boolean.TRUE.equals(closure.eval(context, cell.value(), cell.row()));
	}

}
