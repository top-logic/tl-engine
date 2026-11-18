/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

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
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ComputedColumn;
import com.top_logic.layout.view.table.DynamicColumns;
import com.top_logic.layout.view.table.ValueCellEditing;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests the options an edited cell of a {@link ComputedColumn} or of {@link DynamicColumns} is chosen
 * from, when the values of the column are objects of a class or classifiers of an enumeration.
 */
public class TestColumnOptions extends AbstractModelAccessTest {

	private static final String TABLE = "table";

	/** The single-valued reference of a project to a category. */
	private static final String PROJECT_CATEGORY = qualified(PROJECT) + "#" + CATEGORY_REF;

	/** The multi-valued reference of a project to categories. */
	private static final String PROJECT_CATEGORIES = qualified(PROJECT) + "#" + CATEGORIES;

	/** An enumeration of the application model. */
	private static final String ENUMERATION = "tl.changelog:ErrorAction";

	/** The channel holding the categories an options expression chooses from. */
	private static final String CANDIDATES = "candidates";

	private ViewContext _context;

	private ViewChannel _candidates;

	private TLObject _deletedCategory;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_candidates = new DefaultViewChannel(CANDIDATES);
		_context.registerChannel(CANDIDATES, _candidates);

		_deletedCategory = newObject(qualified(CATEGORY), "deletedCategory");
		inTransaction(() -> {
			_project.tUpdateByName(CATEGORY_REF, _hiddenCategory);
			_deletedCategory.tDelete();
		});
		_candidates.set(List.of(_otherCategory, _deletedCategory, _category, _hiddenCategory));
	}

	/**
	 * The tests are about the options of a cell, not about the access rights.
	 */
	@Override
	protected boolean checksSecurity() {
		return false;
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_candidates = null;
		_deletedCategory = null;
		super.tearDown();
	}

	/**
	 * A column over a class without options offers all instances of the class in a select.
	 */
	public void testClassWithoutOptionsOffersAllInstances() throws Exception {
		BoundFieldModel field = model(computed(
			"<computed-column name='c' type='" + qualified(CATEGORY) + "'"
				+ " value='$row.get(`" + PROJECT_CATEGORY + "`)'"
				+ " update='$row.set(`" + PROJECT_CATEGORY + "`, $value)'/>"),
			_project);

		SelectFieldModel select = assertSelect(field);
		assertFalse(select.isMultiple());
		assertEquals(Set.of(_category, _hiddenCategory, _otherCategory), new HashSet<>(select.getOptions()));
		assertEquals("The value is the selection of the current category.", List.of(_hiddenCategory),
			field.getValue());
	}

	/**
	 * Options computed by an expression are computed per row, in the order of the expression, and
	 * without deleted objects.
	 */
	public void testOptionsAreComputedPerRow() throws Exception {
		ColumnSetup setup = computed(
			"<computed-column name='c' type='" + qualified(CATEGORY) + "'"
				+ " value='$row.get(`" + PROJECT_CATEGORY + "`)'"
				+ " update='$row.set(`" + PROJECT_CATEGORY + "`, $value)'>"
				+ "<inputs><input channel='" + CANDIDATES + "'/></inputs>"
				+ "<options>$" + CANDIDATES + ".filter(c -> $c != $row.get(`" + PROJECT_CATEGORY + "`))</options>"
				+ "</computed-column>");

		assertEquals("Without the category of the row, in the order computed, without the deleted one.",
			List.of(_otherCategory, _category), assertSelect(model(setup, _project)).getOptions());
		assertEquals("Another row has other options.",
			List.of(_otherCategory, _category, _hiddenCategory),
			assertSelect(model(setup, _otherProject)).getOptions());
	}

	/**
	 * A row added in an edit session, not stored yet, offers the options computed for it.
	 */
	public void testNewRowOffersItsOptions() throws Exception {
		ColumnSetup setup = computed(
			"<computed-column name='c' type='" + qualified(CATEGORY) + "'"
				+ " value='$row.get(`" + PROJECT_CATEGORY + "`)'"
				+ " update='$row.set(`" + PROJECT_CATEGORY + "`, $value)'>"
				+ "<inputs><input channel='" + CANDIDATES + "'/></inputs>"
				+ "<options>$row.container() == null ? $" + CANDIDATES + " : list()</options>"
				+ "</computed-column>");
		TLObjectOverlay newRow =
			new TLObjectOverlay(TransientObjectFactory.INSTANCE.createObject(type(PROJECT), null));

		assertEquals(List.of(_otherCategory, _category, _hiddenCategory),
			assertSelect(model(setup, newRow)).getOptions());
		assertEquals("Without options, a new row offers the instances of the class.",
			Set.of(_category, _hiddenCategory, _otherCategory),
			new HashSet<>(assertSelect(model(computed(
				"<computed-column name='c' type='" + qualified(CATEGORY) + "'"
					+ " value='$row.get(`" + PROJECT_CATEGORY + "`)'"
					+ " update='$row.set(`" + PROJECT_CATEGORY + "`, $value)'/>"),
				newRow)).getOptions()));
	}

	/**
	 * Choosing an option writes it through the update of the column.
	 */
	public void testChoiceIsWrittenByTheUpdate() throws Exception {
		ColumnSetup setup = computed(
			"<computed-column name='c' type='" + qualified(CATEGORY) + "'"
				+ " value='$row.get(`" + PROJECT_CATEGORY + "`)'"
				+ " update='$row.set(`" + PROJECT_CATEGORY + "`, $value)'/>");

		BoundFieldModel field = ((ValueCellEditing) setup.editing()).createInPlaceModel(_context, _project,
			NoTokenHandling.INSTANCE, () -> {
				// Nothing depends on the cell.
			});
		field.setValue(List.of(_otherCategory));

		assertSame(_otherCategory, _project.tValueByName(CATEGORY_REF));
		assertEquals(List.of(_otherCategory), field.getValue());

		field.setValue(Collections.emptyList());
		assertNull("Choosing nothing clears the value.", _project.tValueByName(CATEGORY_REF));
	}

	/**
	 * A column over an enumeration offers its classifiers, in their order.
	 */
	public void testEnumerationOffersItsClassifiers() throws Exception {
		BoundFieldModel field = model(computed(
			"<computed-column name='c' type='" + ENUMERATION + "' value='null' update='null'/>"), _project);

		TLEnumeration enumeration = (TLEnumeration) TLModelUtil.findType(ModelService.getApplicationModel(),
			ENUMERATION);
		assertEquals(enumeration.getClassifiers(), assertSelect(field).getOptions());
	}

	/**
	 * A column holding several values chooses several options, which the update receives as a
	 * list.
	 */
	public void testMultipleChoice() throws Exception {
		ColumnSetup setup = computed(
			"<computed-column name='c' type='" + qualified(CATEGORY) + "' multiple='true'"
				+ " value='$row.get(`" + PROJECT_CATEGORIES + "`)'"
				+ " update='$row.set(`" + PROJECT_CATEGORIES + "`, $value)'/>");

		BoundFieldModel field = ((ValueCellEditing) setup.editing()).createInPlaceModel(_context, _project,
			NoTokenHandling.INSTANCE, () -> {
				// Nothing depends on the cell.
			});
		assertTrue(assertSelect(field).isMultiple());
		assertEquals(List.of(), field.getValue());

		field.setValue(List.of(_category, _otherCategory));

		assertEquals(Set.of(_category, _otherCategory),
			new HashSet<>((Collection<?>) _project.tValueByName(CATEGORIES)));
		assertEquals(Set.of(_category, _otherCategory), new HashSet<>((List<?>) field.getValue()));
	}

	/**
	 * A value of a primitive type is entered, not chosen.
	 */
	public void testPrimitiveIsEntered() throws Exception {
		BoundFieldModel field = model(computed(
			"<computed-column name='c' type='tl.core:String'"
				+ " value='$row.get(`" + qualified(PROJECT) + "#" + NAME + "`)'"
				+ " update='$row.set(`" + qualified(PROJECT) + "#" + NAME + "`, $value)'/>"),
			_project);

		assertFalse(field instanceof SelectFieldModel);
		assertEquals("project", field.getValue());
	}

	/**
	 * The columns of a set offer the instances of the class without options, and the options
	 * computed for the column object they stand for with options; a choice is written by the
	 * update.
	 */
	public void testDynamicColumns() throws Exception {
		List<ColumnSetup> plain = dynamic(
			"<dynamic-columns name='d' type='" + qualified(CATEGORY) + "'"
				+ " columns=\"['x']\""
				+ " value='$row.get(`" + PROJECT_CATEGORY + "`)'"
				+ " update='$row.set(`" + PROJECT_CATEGORY + "`, $value)'/>");
		assertEquals(Set.of(_category, _hiddenCategory, _otherCategory),
			new HashSet<>(assertSelect(model(plain.get(0), _project)).getOptions()));

		List<ColumnSetup> computed = dynamic(
			"<dynamic-columns name='d' type='" + qualified(CATEGORY) + "'"
				+ " columns=\"['other', 'hidden']\""
				+ " value='$row.get(`" + PROJECT_CATEGORY + "`)'"
				+ " update='$row.set(`" + PROJECT_CATEGORY + "`, $value)'>"
				+ "<inputs><input channel='" + CANDIDATES + "'/></inputs>"
				+ "<options>$" + CANDIDATES + ".filter(c -> $c.get(`" + qualified(CATEGORY) + "#" + NAME
				+ "`).stringStartsWith($column))</options>"
				+ "</dynamic-columns>");
		assertEquals(2, computed.size());
		assertEquals(List.of(_otherCategory), assertSelect(model(computed.get(0), _project)).getOptions());
		assertEquals(List.of(_hiddenCategory), assertSelect(model(computed.get(1), _project)).getOptions());

		BoundFieldModel field = ((ValueCellEditing) computed.get(0).editing()).createInPlaceModel(_context,
			_otherProject, NoTokenHandling.INSTANCE, () -> {
				// Nothing depends on the cell.
			});
		field.setValue(List.of(_otherCategory));
		assertSame(_otherCategory, _otherProject.tValueByName(CATEGORY_REF));
	}

	/**
	 * Options at a column that is not edited, or at a column whose values are entered, are a
	 * configuration error.
	 */
	public void testMisplacedOptionsAreAnError() throws Exception {
		assertError(
			"<computed-column name='c' type='" + qualified(CATEGORY) + "' value='$row' options='[]'/>",
			"update");
		assertError(
			"<computed-column name='c' type='tl.core:String' value='$row' update='$row' options='[]'/>",
			"tl.core:String");
		assertError(
			"<dynamic-columns name='d' type='" + qualified(CATEGORY) + "' columns='[]' value='$row'"
				+ " options='[]'/>",
			"update");
		assertError(
			"<dynamic-columns name='d' type='tl.core:String' columns='[]' value='$row' update='$row'"
				+ " options='[]'/>",
			"tl.core:String");
	}

	private void assertError(String column, String expected) throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(declaration(column));
		assertTrue("Misplaced options must be rejected: " + column, log.hasErrors());
		assertTrue(log.getError(), log.getError().contains(expected));
	}

	private static SelectFieldModel assertSelect(BoundFieldModel field) {
		assertTrue("A value of a class or enumeration is chosen: " + field, field instanceof SelectFieldModel);
		return (SelectFieldModel) field;
	}

	private static BoundFieldModel model(ColumnSetup setup, Object row) {
		return setup.editing().createModel(row, null, null);
	}

	private ColumnSetup computed(String column) throws ConfigurationException {
		List<ColumnSetup> setups = resolve(column);
		assertEquals(1, setups.size());
		return setups.get(0);
	}

	private List<ColumnSetup> dynamic(String column) throws ConfigurationException {
		return resolve(column);
	}

	private List<ColumnSetup> resolve(String column) throws ConfigurationException {
		return instantiate(declaration(column)).resolve(new ColumnResolution(List.of(), _context));
	}

	private static PolymorphicConfiguration<? extends ColumnDeclaration> declaration(String column)
			throws ConfigurationException {
		TableElement.Config table =
			(TableElement.Config) parseTable("<table rows='[]'><columns>" + column + "</columns></table>");
		return table.getColumns().getColumns().get(0);
	}

	private static <T> T instantiate(PolymorphicConfiguration<T> config) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestColumnOptions.class);
		T result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	private static ConfigurationItem parseTable(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestColumnOptions.class);
		ConfigurationReader reader = new ConfigurationReader(context,
			Collections.singletonMap(TABLE, TypedConfiguration.getConfigurationDescriptor(TableElement.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	private static void inTransaction(Runnable change) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			change.run();
			tx.commit();
		}
	}

	/**
	 * The test suite, starting the scripting the columns compute with.
	 */
	public static Test suite() {
		return suiteWith(TestColumnOptions.class, TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE);
	}

}
