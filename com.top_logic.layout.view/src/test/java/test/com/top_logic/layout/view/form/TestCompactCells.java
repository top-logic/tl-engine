/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCompactFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.ui.MultiLine;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.util.model.CompatibilityService;

/**
 * Test for values displayed and edited in a table cell: a value whose control needs more room than
 * a row offers is shown by a one-line preview with a button opening the control in a dialog, while
 * a form shows the control itself; and the parts of a composition are shown by their labels.
 */
public class TestCompactCells extends TestCase {

	/** The module holding the test model. */
	private static final String MODULE = "test.compactCells";

	/** The number of rows of a text displayed on several lines. */
	private static final int ROWS = 5;

	private ReactContext _context;

	/** Resolves the controls under test. */
	private FieldControlService _controls;

	/** The type the attributes under test belong to. */
	private TLClass _rowType;

	/** The type of the parts the composition under test holds. */
	private TLClass _partType;

	/** A datatype holding a text. */
	private TLPrimitive _textType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		FieldControlService.Config config = TypedConfiguration.newConfigItem(FieldControlService.Config.class);
		config.setImplementationClass(FieldControlService.class);
		_controls =
			(FieldControlService) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, MODULE);
		_rowType = TLModelUtil.addClass(module, "Row");
		_partType = TLModelUtil.addClass(module, "Part");
		_textType = TLModelUtil.addDatatype(module, module, "Text", Kind.STRING, directMapping(String.class));
		TLModelUtil.addProperty(_partType, "name", _textType);
	}

	/**
	 * Runs the test in an interaction: labelling a part of the test model looks up its meta type,
	 * which reads the knowledge base where one is running.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContextManager.<Void, Throwable, RuntimeException> inSystemInteraction(TestCompactCells.class, () -> {
			super.runTest();
			return null;
		});
	}

	@Override
	protected void tearDown() throws Exception {
		_controls = null;
		_context = null;

		super.tearDown();
	}

	/** A table cell is compact, whatever field it describes. */
	public void testACellIsCompact() {
		FieldSpec field = FieldControlService.fieldSpec(_textType, _textType, null, false, new AbstractFieldModel(""));

		assertFalse("A form field has the room it needs.", field.isCompact());
		assertTrue(FieldControlService.cellSpec(field).isCompact());
	}

	/** A text of several lines is edited in a cell by its first line and a button opening its editor. */
	public void testAMultiLineTextIsEditedInADialog() {
		ReactControl control = _controls.createCellControl(_context, multiLineText(), new AbstractFieldModel("a\nb"));

		assertEquals(ReactCompactFieldControl.class, control.getClass());
	}

	/** Several texts are edited in a cell by their previews and a button opening the list of them. */
	public void testSeveralTextsAreEditedInADialog() {
		TLStructuredTypePart part = TLModelUtil.addProperty(_rowType, "texts", _textType);
		part.setMultiple(true);

		ReactControl control =
			_controls.createCellControl(_context, part, new AbstractFieldModel(List.of("first", "second")));

		assertEquals(ReactCompactFieldControl.class, control.getClass());
		assertEquals("first, second", ((ReactCompactFieldControl) control).getPreviewText());
	}

	/** A single-line text fits into a cell and is edited there. */
	public void testASingleLineTextIsEditedInTheCell() {
		TLStructuredTypePart part = TLModelUtil.addProperty(_rowType, "title", _textType);

		ReactControl control = _controls.createCellControl(_context, part, new AbstractFieldModel("only one"));

		assertEquals(ReactTextInputControl.class, control.getClass());
	}

	/**
	 * A text of several lines is displayed in a cell by its first line and a button opening it,
	 * while a form displays it with all its lines.
	 */
	public void testAMultiLineTextIsDisplayedCompactOnlyInACell() {
		TLStructuredTypePart part = multiLineText();

		ReactControl cell = _controls.createDisplayControl(_context, ColumnType.of(part), "a\nb");
		ReactControl form = _controls.createDisplayControl(_context, part, "a\nb");

		assertEquals(ReactCompactFieldControl.class, cell.getClass());
		assertFalse("The value is displayed, not edited.", ((ReactCompactFieldControl) cell).getFieldModel().isEditable());
		assertEquals(ReactTextInputControl.class, form.getClass());
	}

	/** The parts of a composition are displayed by their labels, as the objects of a reference are. */
	public void testThePartsOfACompositionAreDisplayedByTheirLabels() {
		List<TLObject> parts = List.of(part("Alpha"), part("Beta"));

		ReactControl control = _controls.createDisplayControl(_context, ColumnType.of(composition()), parts);

		assertEquals(ReactDropdownSelectControl.class, control.getClass());
		List<?> selection = (List<?>) control.scriptingScalarState().get(FieldState.VALUE__PROP);
		assertEquals(labels(parts), optionLabels(selection));
	}

	/** A single part of a composition is displayed by its label as well. */
	public void testASinglePartIsDisplayedByItsLabel() {
		TLReference composition = composition();
		composition.setMultiple(false);
		TLObject part = part("Alpha");

		ReactControl control = _controls.createDisplayControl(_context, ColumnType.of(composition), part);

		assertEquals(ReactDropdownSelectControl.class, control.getClass());
		List<?> selection = (List<?>) control.scriptingScalarState().get(FieldState.VALUE__PROP);
		assertEquals(labels(List.of(part)), optionLabels(selection));
	}

	/** A column of the parts of a composition is searched, sorted and filtered by their labels. */
	public void testACompositionColumnIsSearchedByTheLabelsOfItsParts() {
		List<TLObject> alphaBeta = List.of(part("Alpha"), part("Beta"));
		List<TLObject> gamma = List.of(part("Gamma"));
		ColumnType type = ColumnType.of(composition());
		assertTrue("The column holds several parts per cell.", type.multiple());

		@SuppressWarnings("unchecked")
		Column<Object, Object> column = (Column<Object, Object>) ColumnProviderService.getInstance()
			.createColumn("parts", ResKey.text("parts"), type, row -> row);

		String expected = String.join(", ", labels(alphaBeta));
		assertEquals(expected, column.searchText(alphaBeta));

		Comparator<Object> order = column.sort().get().comparator();
		assertTrue("Sorted by the labels of the parts.", order.compare(alphaBeta, gamma) < 0);

		ColumnFilter<Object> filter = column.filter().get();
		var state = filter.fromJson(Map.of(TextColumnFilter.PATTERN, labels(List.of(alphaBeta.get(1))).get(0),
			TextColumnFilter.CASE_SENSITIVE, Boolean.FALSE,
			TextColumnFilter.REGEXP, Boolean.FALSE,
			TextColumnFilter.WHOLE_FIELD, Boolean.FALSE));
		assertTrue("Filtered by the labels of the parts.", filter.predicate(state).test(alphaBeta));
		assertFalse(filter.predicate(state).test(gamma));
	}

	/** An attribute of the row type holding a text displayed on several lines. */
	private TLStructuredTypePart multiLineText() {
		TLStructuredTypePart part = TLModelUtil.addProperty(_rowType, "notes", _textType);
		MultiLine multiLine = TypedConfiguration.newConfigItem(MultiLine.class);
		multiLine.setValue(true);
		multiLine.setRows(ROWS);
		part.setAnnotation(multiLine);
		return part;
	}

	/** A composition of the row type holding several parts. */
	private TLReference composition() {
		TLModelUtil.addReference(_rowType, "parts", _partType, null);
		TLReference result = (TLReference) _rowType.getPart("parts");
		result.getEnd().setComposite(true);
		result.setMultiple(true);
		return result;
	}

	/** A part of the given name, living only in this test. */
	private TLObject part(String name) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(_partType, null);
		result.tUpdate(_partType.getPart("name"), name);
		return result;
	}

	/** The labels the given objects are displayed by. */
	private static List<String> labels(List<TLObject> objects) {
		List<String> result = new ArrayList<>();
		for (TLObject object : objects) {
			result.add(ColumnProviderService.label(object));
		}
		return result;
	}

	/** The labels of the given option descriptors of a select control. */
	private static List<String> optionLabels(List<?> descriptors) {
		List<String> result = new ArrayList<>();
		for (Object descriptor : descriptors) {
			result.add((String) ((Map<?, ?>) descriptor).get(DropdownSelectState.Option.LABEL__PROP));
		}
		return result;
	}

	/**
	 * A mapping storing the values as they are, as the core datatypes of that application type use
	 * it.
	 */
	private static StorageMapping<?> directMapping(Class<?> applicationType) throws ConfigurationException {
		PolymorphicConfiguration<?> config =
			TypedConfiguration.createConfigItemForImplementationClass(DirectMapping.class);
		config.update(config.descriptor().getProperty(DirectMapping.Config.APPLICATION_TYPE), applicationType);
		return (StorageMapping<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Test suite requiring the {@link ColumnProviderService} building the columns under test, the
	 * {@link AttributeSettings} the test model is built with, the {@link CompatibilityService} the
	 * transient parts hold their values through, the {@link ThemeFactory} providing the icon of the
	 * button opening a compact field, the resources and label providers the displays use, and the
	 * {@link ThreadContextManager} each test runs its interaction in.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestCompactCells.class,
				ThreadContextManager.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				AttributeSettings.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE));
	}

}
