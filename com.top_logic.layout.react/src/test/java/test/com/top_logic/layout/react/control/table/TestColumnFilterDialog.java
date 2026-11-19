/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.table.ColumnFilterDialog;
import com.top_logic.layout.react.control.table.FilterFieldFactory;
import com.top_logic.layout.react.control.table.OpenFilterArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.AlertState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.layout.react.state.TextState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnFilterState;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.Option;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.BoundCodec;
import com.top_logic.table.filter.ColumnFilterEditor;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.ComparisonOperator;
import com.top_logic.table.filter.FilterEditors;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RangeFilterState;
import com.top_logic.table.filter.RegexpOptionsFilter;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.util.Resources;

/**
 * Test for the filter dialog of a {@link TableViewControl} built by a {@link ColumnFilterDialog}:
 * the switch between the kinds of filter, the notice of a kind not available, the invert checkbox
 * of every kind, the rows of a list of values, and the inputs of typed bounds.
 */
public class TestColumnFilterDialog extends TestCase {

	private static final String CITY = "city";

	private static final String AMOUNT = "amount";

	private static final String FLAG = "flag";

	/** Prefix of the text the {@link Factory} displays a value with. */
	private static final String DISPLAYED = "displayed:";

	/** A column filtered by patterns of its text. */
	private static final String PATTERN = "pattern";

	private record Row(String city, Integer amount, Boolean flag) {
		// Test fixture.
	}

	private static List<Row> rows() {
		return List.of(
			new Row("Bern", Integer.valueOf(3), Boolean.TRUE),
			new Row("Hamburg", Integer.valueOf(5), Boolean.FALSE),
			new Row("Bern", Integer.valueOf(7), Boolean.TRUE));
	}

	private static Collection<String> cityKeys(Object value) {
		return value == null ? List.of() : List.of((String) value);
	}

	private static List<Column<Row, ?>> columns(int maxValues) {
		Column<Row, Object> city = DefaultColumn.<Row, Object> builder(CITY, Row::city)
			.renderer(value -> CellContent.text(String.valueOf(value)))
			.sort(() -> Comparator.comparing(String::valueOf))
			.filters(List.of(
				OptionsColumnFilter.<Object, String> occurring(TestColumnFilterDialog::cityKeys,
					BoundCodec.text(Function.identity()), maxValues),
				new TextColumnFilter<>(String::valueOf)))
			.build();
		Column<Row, Integer> amount = DefaultColumn.<Row, Integer> builder(AMOUNT, Row::amount)
			.renderer(value -> CellContent.text(String.valueOf(value)))
			.filter(ComparableColumnFilter.integers())
			.build();
		Column<Row, Boolean> flag = DefaultColumn.<Row, Boolean> builder(FLAG, Row::flag)
			.renderer(value -> CellContent.text(String.valueOf(value)))
			.filter(BooleanColumnFilter.INSTANCE)
			.build();
		return List.of(city, amount, flag);
	}

	private static TableView<Row> view(int maxValues) {
		List<Column<Row, ?>> columns = columns(maxValues);
		return DefaultTableView.create(columns, new ListRowSource<>(rows(), columns), null, null, SortSpec.NONE,
			List.of(), List.of(), null);
	}

	private static ReactContext context() {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		queue.setDialogManager(new Dialogs());
		return new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test"));
	}

	private static ColumnFilterDialog dialog(TableView<Row> view, String column, FilterState current) {
		ColumnFilterEditor editor = FilterEditors.create(view.columnFilters(column), current,
			kind -> view.columnMatchCounts(column, kind));
		return new ColumnFilterDialog(context(), column, editor, new Factory());
	}

	/** A column offering several kinds gets a switch between them, the first offered one active. */
	public void testKindSwitch() {
		ColumnFilterDialog dialog = dialog(view(10), CITY, null);
		ReactDropdownSelectControl kinds = single(dialog.body(), ReactDropdownSelectControl.class);
		assertEquals(List.of(ColumnFilter.KIND_VALUES, ColumnFilter.KIND_TEXT),
			((com.top_logic.layout.form.model.SelectFieldModel) kinds.getModel()).getOptions());
		assertEquals(List.of(ColumnFilter.KIND_VALUES), ((FieldModel) kinds.getModel()).getValue());

		switchTo(dialog, ColumnFilter.KIND_TEXT);
		assertEquals(ColumnFilter.KIND_TEXT, dialog.editor().activeKind());
		assertTrue("The form of the text kind replaced the value rows.", checkFields(dialog.body()).isEmpty());
		assertFalse(all(dialog.body(), ReactTextInputControl.class).isEmpty());

		assertTrue("A column with a single kind has no switch.",
			all(dialog(view(10), AMOUNT, null).body(), ReactDropdownSelectControl.class).isEmpty());
	}

	/**
	 * A row per value: a checkbox named by the label and count of the value, the value displayed as
	 * the factory displays it, and its count.
	 */
	public void testValueRows() {
		ColumnFilterDialog dialog = dialog(view(10), CITY, null);
		List<ReactFormFieldChromeControl> checks = checkFields(dialog.body());
		assertEquals(2, checks.size());
		Map<?, ?> bern = state(checks.get(0));
		assertEquals("Bern (2)", bern.get(FormFieldState.LABEL__PROP));
		assertEquals(LabelPosition.HIDDEN.getExternalName(), bern.get(FormFieldState.LABEL_POSITION__PROP));

		List<String> texts = texts(dialog.body());
		assertTrue(texts.toString(), texts.contains(DISPLAYED + "Bern"));
		assertTrue(texts.toString(), texts.contains("(2)"));
		assertTrue(texts.toString(), texts.contains(DISPLAYED + "Hamburg"));
		assertTrue(texts.toString(), texts.contains("(1)"));

		((FieldModel) checks.get(1).getField().getModel()).setValue(Boolean.TRUE);
		assertEquals(new OptionsFilterState(Set.of("Hamburg")), dialog.read());
	}

	/** Switching the kind shows its form and keeps the input of each kind. */
	public void testSwitchKeepsInput() {
		ColumnFilterDialog dialog = dialog(view(10), CITY, null);
		((FieldModel) checkFields(dialog.body()).get(0).getField().getModel()).setValue(Boolean.TRUE);

		switchTo(dialog, ColumnFilter.KIND_TEXT);
		assertTrue("The text form has no value rows.", checkFields(dialog.body()).isEmpty());
		((FieldModel) all(dialog.body(), ReactTextInputControl.class).get(0).getModel()).setValue("ham");
		assertEquals(new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("ham"), false),
			dialog.read());

		switchTo(dialog, ColumnFilter.KIND_VALUES);
		assertEquals(new OptionsFilterState(Set.of("Bern")), dialog.read());
		switchTo(dialog, ColumnFilter.KIND_TEXT);
		assertEquals("ham", ((FieldModel) all(dialog.body(), ReactTextInputControl.class).get(0).getModel()).getValue());
	}

	/** Switching to the list of values shows the counts of the values. */
	public void testCountsAfterSwitch() {
		ColumnFilterDialog dialog = dialog(view(10), CITY,
			new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("e"), false));
		assertTrue(checkFields(dialog.body()).isEmpty());

		switchTo(dialog, ColumnFilter.KIND_VALUES);
		assertEquals(List.of("Bern (2)", "Hamburg (1)"),
			checkFields(dialog.body()).stream().map(c -> state(c).get(FormFieldState.LABEL__PROP)).toList());
	}

	/** The search mode and the operator always have a value: their selects offer no empty choice. */
	public void testSelectsNotNullable() {
		ColumnFilterDialog text = dialog(view(10), CITY,
			new ColumnFilterState(ColumnFilter.KIND_TEXT, TextFilterState.contains("e"), false));
		ReactSelectFormFieldControl mode = single(text.body(), ReactSelectFormFieldControl.class);
		assertEquals(Boolean.FALSE, state(mode).get(FieldState.NULLABLE__PROP));

		ReactSelectFormFieldControl operator =
			single(dialog(view(10), AMOUNT, null).body(), ReactSelectFormFieldControl.class);
		assertEquals(Boolean.FALSE, state(operator).get(FieldState.NULLABLE__PROP));
	}

	/**
	 * Every kind of filter offers to invert it but the boolean one, whose options already cover
	 * every selection.
	 */
	public void testInvert() {
		String invert = Resources.getInstance().getString(com.top_logic.table.filter.I18NConstants.FILTER_INVERT);
		TableView<Row> view = view(10);
		ColumnFilterDialog values = dialog(view, CITY, null);
		assertTrue(labels(values.body()).contains(invert));
		switchTo(values, ColumnFilter.KIND_TEXT);
		assertTrue(labels(values.body()).contains(invert));
		assertTrue(labels(dialog(view, AMOUNT, null).body()).contains(invert));

		ColumnFilterDialog flag = dialog(view, FLAG, null);
		assertFalse(labels(flag.body()).contains(invert));
		ColumnFilterDialog inverted =
			dialog(view, FLAG, ColumnFilterState.invert(new BooleanFilterState(true, false, false)));
		assertTrue("An inverted state keeps the checkbox, to undo it.", labels(inverted.body()).contains(invert));
	}

	/**
	 * The cells without a value are a row of the list of values of their own, shown by its label,
	 * after the values.
	 */
	public void testNoValueRow() {
		List<Row> rows = new ArrayList<>(rows());
		rows.add(new Row(null, Integer.valueOf(1), Boolean.TRUE));
		List<Column<Row, ?>> columns = columns(10);
		TableView<Row> view = DefaultTableView.create(columns, new ListRowSource<>(rows, columns), null, null,
			SortSpec.NONE, List.of(), List.of(), null);
		ColumnFilterDialog dialog = dialog(view, CITY, null);
		List<ReactFormFieldChromeControl> checks = checkFields(dialog.body());
		String noValue = Resources.getInstance().getString(com.top_logic.table.filter.I18NConstants.VALUE_NONE);
		assertEquals(List.of("Bern (2)", "Hamburg (1)", noValue + " (1)"),
			checks.stream().map(c -> state(c).get(FormFieldState.LABEL__PROP)).toList());
		List<String> texts = texts(dialog.body());
		assertTrue(texts.toString(), texts.contains(noValue));
		assertFalse(texts.toString(), texts.contains(DISPLAYED + Option.NO_VALUE));

		((FieldModel) checks.get(2).getField().getModel()).setValue(Boolean.TRUE);
		assertEquals(new OptionsFilterState(Set.of(Option.NO_VALUE)), dialog.read());
	}

	/**
	 * An option standing for a condition - a pattern of an application filter - is shown by its
	 * label, not as a value of the column.
	 */
	public void testPatternOptionsShowLabels() {
		Column<Row, String> pattern = DefaultColumn.<Row, String> builder(PATTERN, Row::city)
			.renderer(value -> CellContent.text(String.valueOf(value)))
			.filter(new RegexpOptionsFilter(List.of(new Option("^B", ResKey.text("B places")))))
			.build();
		List<Column<Row, ?>> columns = List.of(pattern);
		TableView<Row> view = DefaultTableView.create(columns, new ListRowSource<>(rows(), columns), null, null,
			SortSpec.NONE, List.of(), List.of(), null);
		ColumnFilterDialog dialog = dialog(view, PATTERN, null);
		assertEquals(List.of("B places (2)"),
			checkFields(dialog.body()).stream().map(c -> state(c).get(FormFieldState.LABEL__PROP)).toList());
		List<String> texts = texts(dialog.body());
		assertTrue(texts.toString(), texts.contains("B places"));
		assertFalse(texts.toString(), texts.contains(DISPLAYED + "^B"));
	}

	/** A kind the column does not offer for its values is shown as not available while it is active. */
	public void testKindNotAvailable() {
		// Two distinct places, but a list of at most one value.
		ColumnFilterDialog dialog = dialog(view(1), CITY, new OptionsFilterState(Set.of("Bern")));
		ReactDropdownSelectControl kinds = single(dialog.body(), ReactDropdownSelectControl.class);
		assertEquals(List.of(ColumnFilter.KIND_VALUES), ((FieldModel) kinds.getModel()).getValue());
		assertEquals(1, all(dialog.body(), ReactAlertControl.class).size());
		assertEquals(Resources.getInstance().getString(I18NConstants.FILTER_KIND_NOT_AVAILABLE_NOTICE),
			state(all(dialog.body(), ReactAlertControl.class).get(0)).get(AlertState.MESSAGE_TEXT__PROP));

		switchTo(dialog, ColumnFilter.KIND_TEXT);
		assertTrue("An offered kind has no notice.", all(dialog.body(), ReactAlertControl.class).isEmpty());

		ColumnFilterDialog unfiltered = dialog(view(1), CITY, null);
		assertTrue("A kind not offered is not offered in the switch of an unfiltered column.",
			all(unfiltered.body(), ReactDropdownSelectControl.class).isEmpty());
		assertEquals(ColumnFilter.KIND_TEXT, unfiltered.editor().activeKind());
	}

	/** The bounds of a range are entered by the input the factory creates for the column. */
	public void testTypedBounds() {
		ColumnFilterDialog dialog = dialog(view(10), AMOUNT, RangeFilterState.of(ComparisonOperator.GE,
			Integer.valueOf(4)));
		List<ValueInput> inputs = all(dialog.body(), ValueInput.class);
		assertEquals("Primary and upper bound.", 2, inputs.size());
		assertEquals(Integer.valueOf(4), ((FieldModel) inputs.get(0).getModel()).getValue());
		((FieldModel) inputs.get(0).getModel()).setValue(Integer.valueOf(6));
		assertEquals(RangeFilterState.of(ComparisonOperator.GE, Integer.valueOf(6)), dialog.read());
	}

	/**
	 * The bounds follow the operator: a comparison with a single value shows the value alone, a
	 * range its lower and upper bound.
	 */
	public void testBoundsFollowOperator() {
		ColumnFilterDialog dialog = dialog(view(10), AMOUNT, RangeFilterState.of(ComparisonOperator.GE,
			Integer.valueOf(4)));
		List<ReactFormFieldChromeControl> bounds = boundFields(dialog.body());
		assertEquals(2, bounds.size());
		Resources resources = Resources.getInstance();
		assertEquals(resources.getString(com.top_logic.table.filter.I18NConstants.VALUE),
			state(bounds.get(0)).get(FormFieldState.LABEL__PROP));
		assertTrue(bounds.get(0).isVisible());
		assertFalse("A comparison with a single value has no upper bound.", bounds.get(1).isVisible());

		FieldModel operator = (FieldModel) single(dialog.body(), ReactSelectFormFieldControl.class).getModel();
		operator.setValue(ComparisonOperator.BETWEEN);
		assertEquals(resources.getString(com.top_logic.table.filter.I18NConstants.LOWER_BOUND),
			state(bounds.get(0)).get(FormFieldState.LABEL__PROP));
		assertEquals(resources.getString(com.top_logic.table.filter.I18NConstants.UPPER_BOUND),
			state(bounds.get(1)).get(FormFieldState.LABEL__PROP));
		assertTrue(bounds.get(1).isVisible());

		operator.setValue(ComparisonOperator.LT);
		assertFalse(bounds.get(1).isVisible());
	}

	/** The fields entering a value of the column. */
	private static List<ReactFormFieldChromeControl> boundFields(ReactControl root) {
		List<ReactFormFieldChromeControl> result = new ArrayList<>();
		for (ReactFormFieldChromeControl chrome : all(root, ReactFormFieldChromeControl.class)) {
			if (chrome.getField() instanceof ValueInput) {
				result.add(chrome);
			}
		}
		return result;
	}

	/** Without a factory, a bound is entered as text, written and read by the codec of the filter. */
	public void testTextBoundsWithoutFactory() {
		TableView<Row> view = view(10);
		ColumnFilterEditor editor = FilterEditors.create(view.columnFilters(AMOUNT),
			RangeFilterState.of(ComparisonOperator.GE, Integer.valueOf(4)), kind -> MatchCounts.NONE);
		ColumnFilterDialog dialog = new ColumnFilterDialog(context(), AMOUNT, editor, FilterFieldFactory.NONE);
		List<ReactTextInputControl> inputs = all(dialog.body(), ReactTextInputControl.class);
		assertEquals("4", ((FieldModel) inputs.get(0).getModel()).getValue());
		((FieldModel) inputs.get(0).getModel()).setValue("9");
		assertEquals(RangeFilterState.of(ComparisonOperator.GE, Integer.valueOf(9)), dialog.read());
	}

	/** The table opens its filter dialog with the factory set on it. */
	public void testTableUsesFactory() {
		Dialogs dialogs = new Dialogs();
		SSEUpdateQueue queue = new SSEUpdateQueue();
		queue.setDialogManager(dialogs);
		ReactContext context = new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test"));
		TableViewControl<Row> table = new TableViewControl<>(context, view(10), false);
		table.setFilterFieldFactory(new Factory());

		table.executeCommand(TableViewControl.CMD_OPEN_FILTER, Map.of(OpenFilterArguments.COLUMN, CITY));
		assertTrue(texts(dialogs._opened).contains(DISPLAYED + "Bern"));
	}

	/**
	 * Chooses the given kind in the switch of the kinds, the way the client does: by the command
	 * naming the id of the chosen option.
	 */
	private static void switchTo(ColumnFilterDialog dialog, String kind) {
		ReactDropdownSelectControl kinds = single(dialog.body(), ReactDropdownSelectControl.class);
		String id = kinds.objectIdentities().idOf(kind);
		assertNotNull("The switch offers the kind '" + kind + "'.", id);
		kinds.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldValueArguments.VALUE, List.of(id)));
	}

	/** The fields of the value rows: a checkbox named by a hidden label. */
	private static List<ReactFormFieldChromeControl> checkFields(ReactControl root) {
		List<ReactFormFieldChromeControl> result = new ArrayList<>();
		for (ReactFormFieldChromeControl chrome : all(root, ReactFormFieldChromeControl.class)) {
			if (chrome.getField() instanceof ReactCheckboxControl
				&& LabelPosition.HIDDEN.getExternalName().equals(state(chrome).get(FormFieldState.LABEL_POSITION__PROP))) {
				result.add(chrome);
			}
		}
		return result;
	}

	/**
	 * The labels of the fields below the given root, the switch of the kinds left out: its state
	 * resolves the options in a model scope the test does not have.
	 */
	private static List<Object> labels(ReactControl root) {
		List<Object> result = new ArrayList<>();
		for (ReactFormFieldChromeControl chrome : all(root, ReactFormFieldChromeControl.class)) {
			if (!(chrome.getField() instanceof ReactDropdownSelectControl)) {
				result.add(state(chrome).get(FormFieldState.LABEL__PROP));
			}
		}
		return result;
	}

	private static List<String> texts(ReactControl root) {
		List<String> result = new ArrayList<>();
		for (ReactTextControl text : all(root, ReactTextControl.class)) {
			result.add(String.valueOf(state(text).get(TextState.TEXT__PROP)));
		}
		return result;
	}

	private static <T> T single(ReactControl root, Class<T> type) {
		List<T> found = all(root, type);
		assertEquals("Exactly one " + type.getSimpleName() + ": " + found, 1, found.size());
		return found.get(0);
	}

	/** All controls of the given type in the tree below the given root, in display order. */
	private static <T> List<T> all(ReactControl root, Class<T> type) {
		List<T> result = new ArrayList<>();
		collect(root, type, result);
		return result;
	}

	private static <T> void collect(ReactControl control, Class<T> type, List<T> result) {
		if (type.isInstance(control)) {
			result.add(type.cast(control));
		}
		for (ReactControl child : control.displayedChildren()) {
			collect(child, type, result);
		}
	}

	/** The state the given control sends to the client. */
	private static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/** The input the {@link Factory} creates for a value: a text input over the typed model. */
	private static class ValueInput extends ReactTextInputControl {
		ValueInput(ReactContext context, FieldModel model) {
			super(context, model);
		}
	}

	/** A {@link FilterFieldFactory} creating recognizable controls. */
	private static class Factory implements FilterFieldFactory {
		@Override
		public ReactControl valueInput(ReactContext context, String column, FieldModel model) {
			return new ValueInput(context, model);
		}

		@Override
		public ReactControl valueDisplay(ReactContext context, String column, Object value) {
			return new ReactTextControl(context, DISPLAYED + value);
		}
	}

	/** Stands in for the dialogs of a window: holds the one that was opened. */
	private static class Dialogs implements DialogManager {

		ReactControl _opened;

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			_opened = child;
			return null;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			_opened = null;
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException("No dialog is stacked on the filter dialog.");
		}
	}

	/**
	 * Suite requiring the {@link TypeIndex} the commands are dispatched by, the resources the labels
	 * are resolved from, and the {@link ThemeFactory} the buttons and notices take their icons from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnFilterDialog.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE));
	}

}
