/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.impl.DefaultColumn;

/**
 * A fully model- and script-defined column filter: the filter UI is a form over a transient
 * instance of a configured parameter type, and the filter logic is a TL-Script expression deciding
 * per row whether the row matches.
 *
 * <p>
 * The expression sees the object holding the parameters the user entered, the cell value of the
 * column and the row. The values of the inputs of the filter are taken when the filter is applied.
 * </p>
 *
 * <p>
 * The filter integrates its own column and dialog by implementing {@link ColumnBinding}: the column
 * cell carries a {@link ScriptedCell} (value + row), so the filter sees both without knowing where
 * the value comes from, and the filter registers its own {@link ScriptedFilterUI} form.
 * </p>
 *
 * @implNote The {@link ScriptedFilterUI} form is registered in {@link #installUI}.
 */
public class ScriptedFilter implements ColumnFilter<ScriptedCell>, ColumnBinding {

	/**
	 * Configuration of a {@link ScriptedFilter}.
	 */
	public interface Config extends PolymorphicConfiguration<ScriptedFilter>, Inputs, ConfigPart {

		/** Name of the variable holding the object with the filter parameters in {@link #getMatch()}. */
		String PARAMS_VARIABLE = "params";

		/** Configuration name of {@link #getModel()}. */
		String MODEL = "model";

		/** Configuration name of {@link #getMatch()}. */
		String MATCH = "match";

		/** Configuration name of {@link #getLabel()}. */
		String LABEL = "label";

		/**
		 * The model type whose attributes are the filter parameters (rendered as the filter form).
		 */
		@Name(MODEL)
		@Mandatory
		TLModelPartRef getModel();

		/**
		 * The expression deciding whether a row matches the filter.
		 *
		 * <p>
		 * The expression sees the object holding the filter parameters - an instance of the
		 * {@link #getModel() model} type - as the variable {@value #PARAMS_VARIABLE}, the cell value
		 * of the column as {@value ValueAsConfig#DEFAULT_VALUE_AS} (or by the name the column gives
		 * it with {@value ValueAsConfig#VALUE_AS}), the row as {@value RowAsConfig#DEFAULT_ROW_AS}
		 * (or by the name the table gives it with {@value RowAsConfig#ROW_AS}), and the values of the
		 * {@link #getInputs() inputs} as variables named after their channels or by their
		 * <code>as</code> names. The row matches, if the expression yields <code>true</code>.
		 * </p>
		 */
		@Name(MATCH)
		@Mandatory
		@NonNullable
		@Supplies(PARAMS_VARIABLE)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@Supplies(value = RowAsConfig.DEFAULT_ROW_AS, renamedBy = RowAsConfig.ROW_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getMatch();

		/**
		 * The dialog title / column-filter label.
		 */
		@Name(LABEL)
		ResKey getLabel();

	}

	private final QueryExecutor _match;

	private final TLStructuredType _filterType;

	private final List<InputRef> _inputs;

	private final ResKey _label;

	/**
	 * Creates a {@link ScriptedFilter} from configuration.
	 */
	@CalledByReflection
	public ScriptedFilter(InstantiationContext context, Config config) {
		_match = InputScript.compile(config, Config.MATCH);
		_filterType = (TLStructuredType) config.getModel().resolveType();
		_inputs = config.getInputs();
		_label = config.getLabel();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The configured inputs the match function is evaluated with.
	 * </p>
	 */
	@Override
	public List<InputRef> inputs() {
		return _inputs;
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
		Function<Object, Object> value = setup.value();
		return DefaultColumn.<Object, ScriptedCell> builder(setup.name(),
			row -> new ScriptedCell(value.apply(row), row))
			.label(setup.label())
			.width(ColumnProviderService.getInstance().defaultWidth(setup.type()))
			.renderer(cell -> CellContent.text(ColumnProviderService.label(cell.value())))
			// The cell pairs the value with its row for the filter; an export writes the value.
			.exportValue(ScriptedCell::value)
			.sort(() -> Comparator.comparing(ScriptedCell::value, ColumnProviderService.labelOrder()))
			.filter(this)
			.build();
	}

	@Override
	public Comparator<?> order(Comparator<Object> cellOrder) {
		return Comparator.comparing(ScriptedCell::value, Comparator.nullsLast(cellOrder));
	}

	@Override
	public void installUI(ColumnSetup setup, TableViewControl<?> control) {
		List<ViewChannel> inputChannels = ChannelInputs.resolve(setup.viewContext(), _inputs);
		control.setFilterUI(setup.name(), new ScriptedFilterUI(this, inputChannels));
	}

	@Override
	public FilterInput input() {
		return new FilterInput.Form();
	}

	@Override
	public Predicate<ScriptedCell> predicate(FilterState state) {
		ScriptedFilterState scripted = (ScriptedFilterState) state;
		Object[] inputValues = scripted.inputs();
		Object params = scripted.params();
		QueryExecutor match = _match;
		// A cell the user may not read is not given to the filter script: it matches no criterion.
		return cell -> !ColumnProviderService.isUnreadable(cell.value())
			&& Boolean.TRUE.equals(match.execute(arguments(inputValues, params, cell.value(), cell.row())));
	}

	/**
	 * The values of the variables of the {@link Config#getMatch() match expression}: the values of
	 * the inputs, followed by the supplied values.
	 */
	private static Object[] arguments(Object[] inputValues, Object... supplied) {
		Object[] result = new Object[inputValues.length + supplied.length];
		System.arraycopy(inputValues, 0, result, 0, inputValues.length);
		System.arraycopy(supplied, 0, result, inputValues.length, supplied.length);
		return result;
	}

}
