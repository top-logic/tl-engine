/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.layout.view.form.RecordingScope;
import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.ExpandArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.AbstractTableElement;
import com.top_logic.layout.view.element.TreeTableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.CellContent;
import com.top_logic.table.Row;
import com.top_logic.table.TableView;

/**
 * Tests a {@code <tree-table>} with {@code row-edit} inside a form: the rows join the edit session
 * of the form when they are displayed - the top-level rows when the form enters edit mode, the
 * children of a row when it is opened -, the edits are buffered until the form is saved or
 * cancelled, the tree is frozen while the form is edited and follows the model again afterwards,
 * the expansion survives the switches between view and edit mode, an attribute is edited only in
 * the rows whose type has it, the {@link RowEditPolicy#SELECTED} policy edits the selected row
 * alone, and the options a row-editing tree table cannot have are reported.
 */
public class TestTreeTableRowEdit extends AbstractModelAccessTest {

	/** Tag of the tree table configurations under test. */
	private static final String TREE_TABLE = "tree-table";

	/** Name of the input channel holding the project the tree is built from. */
	private static final String PROJECT_CHANNEL = "project";

	/** Name of the selection channel. */
	private static final String SELECTED = "selected";

	/** Name of an attribute of a {@link #STEP} that an {@link #ITEM} lacks. */
	private static final String NOTE = "note";

	private static final String STEP_A = "A";

	private static final String STEP_B = "B";

	private static final String STEP_C = "C";

	private static final String SUBSTEP_A1 = "A1";

	private static final String ITEM_A = "item of A";

	private static final String RENAMED = "renamed";

	/** State key telling whether the input of a cell can be changed. */
	private static final String EDITABLE = "editable";

	/** State key of the value the input of a cell shows. */
	private static final String VALUE = "value";

	/**
	 * The children of the tree: the steps of the project, the substeps and items of a step.
	 */
	private static final String CHILDREN = "if($node.instanceOf(`" + qualified(PROJECT) + "`), $node.get(`"
		+ qualified(PROJECT) + "#" + STEPS + "`), if($node.instanceOf(`" + qualified(STEP) + "`), concat($node.get(`"
		+ qualified(STEP) + "#" + SUBSTEPS + "`), $node.get(`" + qualified(STEP) + "#" + ITEMS + "`)), []))";

	private ReactContext _reactContext;

	private DefaultViewContext _context;

	private RecordingScope _scope;

	private DefaultViewChannel _selected;

	private TLObject _stepA;

	private TLObject _stepB;

	private TLObject _substepA1;

	private TLObject _itemA;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_scope = new RecordingScope();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")) {
			@Override
			public ModelScope getModelScope() {
				return _scope;
			}
		};
		_context = new DefaultViewContext(_reactContext);
		DefaultViewChannel project = new DefaultViewChannel(PROJECT_CHANNEL);
		project.set(_project);
		_context.registerChannel(PROJECT_CHANNEL, project);
		_selected = new DefaultViewChannel(SELECTED);
		_context.registerChannel(SELECTED, _selected);

		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_stepA = newNamed(STEP, STEP_A);
			_stepB = newNamed(STEP, STEP_B);
			_substepA1 = newNamed(STEP, SUBSTEP_A1);
			_itemA = newNamed(ITEM, ITEM_A);
			_stepA.tUpdateByName(NOTE, "note of A");
			_stepA.tUpdateByName(SUBSTEPS, List.of(_substepA1));
			_stepA.tUpdateByName(ITEMS, List.of(_itemA));
			_project.tUpdateByName(STEPS, List.of(_stepA, _stepB));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_stepA = null;
		_stepB = null;
		_substepA1 = null;
		_itemA = null;
		_selected = null;
		_context = null;
		_reactContext = null;
		_scope = null;
		super.tearDown();
	}

	private static TLObject newNamed(String typeName, String name) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(typeName));
		result.tUpdateByName(NAME, name);
		return result;
	}

	/**
	 * In edit mode, the displayed rows are the buffers the session edits the stored objects
	 * through, keyed by the stored objects; the session holds exactly the displayed rows.
	 */
	public void testTopLevelRowsAreBuffers() throws Exception {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table = editableTable(form, RowEditPolicy.ALL);

		assertEquals(List.of(_stepA, _stepB), rowData(table));
		assertTrue(form.enterEditMode());

		assertEquals(List.of(_stepA, _stepB), rowKeys(table));
		assertBuffersOf(List.of(_stepA, _stepB), rowData(table));
		assertEquals("Only the displayed rows join the session.", 2, table.session().currentRows().size());
		assertNull(table.session().findRow(_substepA1));
		assertFalse(form.isDirty());
	}

	/**
	 * Opening a row in edit mode adopts its children; a change of an adopted row is buffered and
	 * stored when the form is saved.
	 */
	public void testOpenedRowsJoinAndAreSaved() throws Exception {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table = editableTable(form, RowEditPolicy.ALL);
		assertTrue(form.enterEditMode());

		expand(table, _stepA);
		assertEquals(List.of(_stepA, _substepA1, _itemA, _stepB), rowKeys(table));
		List<Object> rows = rowData(table);
		assertBuffersOf(List.of(_stepA, _substepA1, _itemA, _stepB), rows);
		assertSame(rows.get(1), table.session().findRow(_substepA1));
		assertFalse("Adopting rows is no change.", form.isDirty());

		TLObject substepRow = (TLObject) rows.get(1);
		substepRow.tUpdateByName(NAME, RENAMED);
		assertTrue(form.isDirty());
		assertEquals("Nothing is stored before the save.", SUBSTEP_A1, _substepA1.tValueByName(NAME));

		form.executeSave();

		assertFalse(form.isEditMode());
		assertEquals(RENAMED, _substepA1.tValueByName(NAME));
		assertEquals("The edit writes no row into the objects holding them.", List.of(_substepA1),
			_stepA.tValueByName(SUBSTEPS));
		assertEquals(List.of(_stepA, _stepB), _project.tValueByName(STEPS));
		assertEquals("Back in view mode, the rows are the stored objects.",
			List.of(_stepA, _substepA1, _itemA, _stepB), rowData(table));
	}

	/**
	 * Cancelling the form discards the buffered changes.
	 */
	public void testCancelDiscards() throws Exception {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table = editableTable(form, RowEditPolicy.ALL);
		assertTrue(form.enterEditMode());

		((TLObject) rowData(table).get(0)).tUpdateByName(NAME, RENAMED);
		assertTrue(form.isDirty());

		form.executeCancel();

		assertFalse(form.isEditMode());
		assertEquals(STEP_A, _stepA.tValueByName(NAME));
		assertEquals(List.of(_stepA, _stepB), rowData(table));
	}

	/**
	 * The rows opened in view mode stay open in edit mode and after it, and a row opened in edit
	 * mode stays open after the save.
	 */
	public void testExpansionSurvivesModeSwitches() throws Exception {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table = editableTable(form, RowEditPolicy.ALL);

		expand(table, _stepA);
		assertEquals(List.of(_stepA, _substepA1, _itemA, _stepB), rowKeys(table));

		assertTrue(form.enterEditMode());
		assertEquals(List.of(_stepA, _substepA1, _itemA, _stepB), rowKeys(table));
		assertBuffersOf(List.of(_stepA, _substepA1, _itemA, _stepB), rowData(table));

		form.executeCancel();
		assertEquals(List.of(_stepA, _substepA1, _itemA, _stepB), rowKeys(table));

		collapse(table, _stepA);
		assertTrue(form.enterEditMode());
		assertEquals(List.of(_stepA, _stepB), rowKeys(table));
		expand(table, _stepA);
		form.executeSave();
		assertEquals("The row opened in edit mode stays open.", List.of(_stepA, _substepA1, _itemA, _stepB),
			rowKeys(table));
	}

	/**
	 * While the form is edited, a change of the model does not change the tree; once the edit
	 * ends, the tree takes it up and follows the model again.
	 */
	public void testTreeFrozenWhileEdited() throws Exception {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table = editableTable(form, RowEditPolicy.ALL);
		table.attach();
		try {
			assertTrue(form.enterEditMode());

			TLObject stepC = addStep(STEP_C);
			_scope.reportCreation(stepC);
			_scope.reportUpdate(_project);
			assertEquals("The tree is frozen while it is edited.", List.of(_stepA, _stepB), rowKeys(table));

			form.executeCancel();
			assertEquals("After the edit the tree takes up what changed meanwhile.", List.of(_stepA, _stepB, stepC),
				rowKeys(table));

			TLObject stepD = addStep("D");
			_scope.reportCreation(stepD);
			_scope.reportUpdate(_project);
			assertEquals("In view mode the tree follows the model.", List.of(_stepA, _stepB, stepC, stepD),
				rowKeys(table));
		} finally {
			table.detach();
		}
	}

	/**
	 * An attribute column is edited in the rows whose type has the attribute only; a row lacking it
	 * has an empty cell, without any control.
	 */
	public void testAttributeEditedPerRowType() throws Exception {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table = editableTable(form, RowEditPolicy.ALL);
		assertTrue(form.enterEditMode());
		expand(table, _stepA);

		TLObject stepRow = table.session().findRow(_stepA);
		TLObject itemRow = table.session().findRow(_itemA);
		assertNotNull(itemRow);

		assertTrue("A step has a note.", isEdited(table, stepRow, NOTE));
		assertSame("An item has no note, so no cell in the column.", CellContent.empty(),
			cell(table, itemRow, NOTE));
		assertTrue("An item has a name.", isEdited(table, itemRow, NAME));
	}

	/**
	 * A Boolean attribute column has no cell for a row whose type lacks the attribute - no
	 * checkbox, in view mode as in edit mode -, while a row holding the attribute with the value
	 * {@code false} shows its checkbox.
	 */
	public void testBooleanCellOnlyWhereAttributeHeld() throws Exception {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(APPROVED, Boolean.FALSE);
			tx.commit();
		}
		becomeUser(_root);
		FormControl form = newForm();
		String xml = "<" + TREE_TABLE + " inputs='" + PROJECT_CHANNEL + "' root=\"'root'\" row-edit='ALL'"
			+ " types='" + qualified(PROJECT) + "," + qualified(STEP) + "'>"
			+ "<children>if($node == 'root', [$" + PROJECT_CHANNEL + "], " + CHILDREN + ")</children>"
			+ "<columns><column attribute='" + APPROVED + "'/><column attribute='" + NAME + "'/></columns>"
			+ "</" + TREE_TABLE + ">";
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeTableRowEdit.class);
		TreeTableElement element =
			(TreeTableElement) context.getInstance((PolymorphicConfiguration<?>) parseXml(xml));
		context.checkErrors();
		RowSetTableControl table = (RowSetTableControl) element.createControl(_context);

		expand(table, _project);
		assertEquals(List.of(_project, _stepA, _stepB), rowKeys(table));
		List<Object> rows = rowData(table);
		assertEquals("A project holds the attribute.", Boolean.FALSE,
			cellState(table, (TLObject) rows.get(0), APPROVED).get(VALUE));
		assertSame("A step lacks the attribute.", CellContent.empty(),
			cell(table, (TLObject) rows.get(1), APPROVED));

		assertTrue(form.enterEditMode());
		rows = rowData(table);
		Map<?, ?> projectCell = cellState(table, (TLObject) rows.get(0), APPROVED);
		assertEquals(Boolean.FALSE, projectCell.get(VALUE));
		assertEquals(Boolean.TRUE, projectCell.get(EDITABLE));
		assertSame("A step lacks the attribute.", CellContent.empty(),
			cell(table, (TLObject) rows.get(1), APPROVED));
	}

	/**
	 * Under {@link RowEditPolicy#SELECTED} exactly the selected row is edited; the selection channel
	 * holds the row the session edits, as that of a flat row-editing table does, and the selection
	 * survives the switches between view and edit mode.
	 */
	public void testSelectedRowEdited() throws Exception {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table = editableTable(form, RowEditPolicy.SELECTED);

		_selected.set(_substepA1);
		assertEquals("The selected object is revealed.", List.of(_stepA, _substepA1, _itemA, _stepB),
			rowKeys(table));

		assertTrue(form.enterEditMode());
		TLObject substepRow = table.session().findRow(_substepA1);
		assertNotNull(substepRow);
		assertTrue("The selection survives the switch into edit mode.",
			table.getTableControl().getSelectedKeys().contains(_substepA1));
		assertTrue(isEdited(table, substepRow, NAME));
		assertFalse(isEdited(table, table.session().findRow(_stepA), NAME));

		table.getTableControl().selectRow(_stepB);
		TLObject stepRow = table.session().findRow(_stepB);
		assertSame("In edit mode the channel holds the edited row.", stepRow, _selected.get());
		assertTrue(isEdited(table, stepRow, NAME));
		assertFalse(isEdited(table, substepRow, NAME));

		form.executeCancel();
		assertTrue("The selection survives the switch into view mode.",
			table.getTableControl().getSelectedKeys().contains(_stepB));
		table.getTableControl().selectRow(_stepA);
		assertSame("In view mode the channel holds the stored object.", _stepA, _selected.get());
	}

	/**
	 * Without a selection channel, the selected row stays selected across the switches between view
	 * and edit mode, and under {@link RowEditPolicy#SELECTED} it is the edited one.
	 */
	public void testSelectionKeptWithoutChannel() throws Exception {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table =
			(RowSetTableControl) treeTable(RowEditPolicy.SELECTED, "", false).createControl(_context);

		table.getTableControl().selectRow(_stepB);
		assertTrue(form.enterEditMode());
		assertEquals(Set.of(_stepB), table.getTableControl().getSelectedKeys());
		assertTrue(isEdited(table, table.session().findRow(_stepB), NAME));
		assertFalse(isEdited(table, table.session().findRow(_stepA), NAME));

		table.getTableControl().selectRow(_stepA);
		form.executeCancel();
		assertEquals(Set.of(_stepA), table.getTableControl().getSelectedKeys());
		assertNull("No channel is written.", _selected.get());
	}

	/**
	 * A row-editing tree table outside a form cannot be built.
	 */
	public void testRowEditRequiresForm() throws Exception {
		becomeUser(_root);
		TreeTableElement element = treeTable(RowEditPolicy.ALL, "", true);
		try {
			element.createControl(_context);
			fail("A row-editing tree table requires a form.");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("<" + TREE_TABLE + ">"));
			assertTrue(ex.getMessage(), ex.getMessage().contains("<form>"));
		}
	}

	/**
	 * A row-editing tree table edited in place or offering a drag is a configuration error; one
	 * without these options is not.
	 */
	public void testOptionsReported() throws Exception {
		assertEquals(List.of(), errors(TREE_TABLE + " row-edit='ALL'", ""));
		assertContains(AbstractTableElement.Config.EDIT_IN_PLACE,
			errors(TREE_TABLE + " row-edit='ALL' edit-in-place='true'", ""));
		assertContains("<" + AbstractTableElement.Config.DRAG + ">",
			errors(TREE_TABLE + " row-edit='ALL'", "<drag kind='step'/>"));
		assertEquals("The options are those of a tree table without row-edit.", List.of(),
			errors(TREE_TABLE + " edit-in-place='true'", "<drag kind='step'/>"));
	}

	private TLObject addStep(String name) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject step = newNamed(STEP, name);
			List<Object> steps = new ArrayList<>((List<?>) _project.tValueByName(STEPS));
			steps.add(step);
			_project.tUpdateByName(STEPS, steps);
			tx.commit();
			return step;
		}
	}

	/**
	 * Whether the cell of the given row in the given column offers an input.
	 */
	private boolean isEdited(RowSetTableControl table, TLObject row, String column) {
		Map<?, ?> state = cellState(table, row, column);
		return Boolean.TRUE.equals(state.get(EDITABLE));
	}

	/**
	 * The state of the control of the given cell as the client receives it.
	 */
	private Map<?, ?> cellState(RowSetTableControl table, TLObject row, String column) {
		CellContent cell = cell(table, row, column);
		assertTrue("Not shown through a control: " + cell,
			cell instanceof CellContent.Raw raw && raw.payload() instanceof CellControlFactory);
		ReactControl control = ((CellControlFactory) ((CellContent.Raw) cell).payload()).create(_reactContext);
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control.", ex);
		}
	}

	/**
	 * The content of the given cell.
	 */
	private static CellContent cell(RowSetTableControl table, TLObject row, String column) {
		TableView<TLObject> view = table.getTableControl().getView();
		for (Row<TLObject> displayed : view.rows(0, view.rowCount())) {
			if (displayed.data() == row) {
				return view.cell(displayed, column);
			}
		}
		throw new AssertionError("No row " + row);
	}

	private static void assertBuffersOf(List<TLObject> expected, List<Object> rows) {
		assertEquals(expected.size(), rows.size());
		for (int n = 0; n < rows.size(); n++) {
			Object row = rows.get(n);
			assertTrue("Not a buffer: " + row, row instanceof TLObjectOverlay);
			assertSame(expected.get(n), ((TLObjectOverlay) row).getBase());
		}
	}

	private static void expand(RowSetTableControl table, TLObject object) {
		setExpanded(table, object, true);
	}

	private static void collapse(RowSetTableControl table, TLObject object) {
		setExpanded(table, object, false);
	}

	private static void setExpanded(RowSetTableControl table, TLObject object, boolean expanded) {
		int index = rowKeys(table).indexOf(object);
		assertTrue("No row displayed for " + object, index >= 0);
		table.getTableControl().executeCommand(TableViewControl.CMD_EXPAND,
			Map.of(ExpandArguments.ROW_INDEX, Integer.valueOf(index), ExpandArguments.EXPANDED,
				Boolean.valueOf(expanded)));
	}

	/** The keys of the displayed rows, in display order. */
	private static List<Object> rowKeys(RowSetTableControl table) {
		List<Object> result = new ArrayList<>();
		for (Row<TLObject> row : rows(table)) {
			result.add(row.key());
		}
		return result;
	}

	/** The row objects of the displayed rows, in display order. */
	private static List<Object> rowData(RowSetTableControl table) {
		List<Object> result = new ArrayList<>();
		for (Row<TLObject> row : rows(table)) {
			result.add(row.data());
		}
		return result;
	}

	private static List<Row<TLObject>> rows(RowSetTableControl table) {
		TableView<TLObject> view = table.getTableControl().getView();
		return view.rows(0, view.rowCount());
	}

	private FormControl newForm() {
		FormControl form = new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
		_context.setFormModel(form);
		return form;
	}

	/**
	 * The control of a row-editing tree table over the project, with the name and the note as
	 * columns, in the form of the {@link #_context}.
	 */
	private RowSetTableControl editableTable(FormControl form, RowEditPolicy policy) throws Exception {
		assertSame(form, _context.getFormModel());
		return (RowSetTableControl) treeTable(policy, "", true).createControl(_context);
	}

	/**
	 * A row-editing tree table over the project.
	 *
	 * @param content
	 *        Further content in front of the columns.
	 * @param selection
	 *        Whether the selection is bound to the {@link #SELECTED} channel.
	 */
	private static TreeTableElement treeTable(RowEditPolicy policy, String content, boolean selection)
			throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeTableRowEdit.class);
		String start = TREE_TABLE + " row-edit='" + policy.name() + "'"
			+ (selection ? " selection='" + SELECTED + "'" : "");
		TreeTableElement result =
			(TreeTableElement) context.getInstance((PolymorphicConfiguration<?>) parse(start, content));
		context.checkErrors();
		return result;
	}

	private static List<String> errors(String start, String content) throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance((PolymorphicConfiguration<?>) parse(start, content));
		return log.getErrors();
	}

	private static void assertContains(String expected, List<String> errors) {
		for (String error : errors) {
			if (error.contains(expected)) {
				return;
			}
		}
		fail("Expected '" + expected + "' to be named in one of: " + errors);
	}

	/**
	 * Parses a tree table over the project.
	 *
	 * @param start
	 *        The tag name followed by further attributes.
	 * @param content
	 *        Further content in front of the columns.
	 */
	private static ConfigurationItem parse(String start, String content) throws ConfigurationException {
		String xml = "<" + start + " inputs='" + PROJECT_CHANNEL + "' root='$" + PROJECT_CHANNEL + "'"
			+ " types='" + qualified(STEP) + "," + qualified(ITEM) + "'>"
			+ "<children>" + CHILDREN + "</children>"
			+ content
			+ "<columns><column attribute='" + NAME + "'/><column attribute='" + NOTE + "'/></columns>"
			+ "</" + TREE_TABLE + ">";
		return parseXml(xml);
	}

	/**
	 * Parses the given {@code <tree-table>} configuration.
	 */
	private static ConfigurationItem parseXml(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeTableRowEdit.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(TREE_TABLE,
			TypedConfiguration.getConfigurationDescriptor(TreeTableElement.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its columns and their cells, the labels of the rows and the scripting computing the
	 * tree.
	 */
	public static Test suite() {
		return suiteWith(TestTreeTableRowEdit.class, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
			FieldControlService.Module.INSTANCE, ColumnProviderService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, SearchBuilder.Module.INSTANCE);
	}

}
