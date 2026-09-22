/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.text.Format;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.element.meta.kbbased.storage.mappings.LongMapping;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.form.ReactValueListControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AnnotationsFieldControlProvider;
import com.top_logic.layout.view.form.BooleanControlProvider;
import com.top_logic.layout.view.form.ConfigFieldControlProvider;
import com.top_logic.layout.view.form.DatePickerControlProvider;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.SelectControlProvider;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.Column;

/**
 * Test for the multiplicity of a field reaching the control that displays it: a value holding
 * several primitives is displayed and edited by a value list of element controls, while a value
 * whose control edits the whole collection itself is left to that control.
 */
public class TestFieldControlServiceMultiplicity extends TestCase {

	/** The label the fields under test carry. */
	private static final String LABEL = "Value";

	/** The module holding the test model. */
	private static final String MODULE = "test.fieldControlMultiplicity";

	private ReactContext _context;

	/** Resolves the controls under test. */
	private FieldControlService _controls;

	/** The type the attributes under test belong to. */
	private TLClass _rowType;

	/** A datatype holding a text. */
	private TLPrimitive _textType;

	/** A datatype holding a whole number. */
	private TLPrimitive _numberType;

	/** A datatype holding a point in time. */
	private TLPrimitive _dateType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_controls = controlService();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, MODULE);
		_rowType = TLModelUtil.addClass(module, "Row");

		_textType = TLModelUtil.addDatatype(module, module, "Text", Kind.STRING, directMapping(String.class));
		_numberType = TLModelUtil.addDatatype(module, module, "Number", Kind.INT, LongMapping.INSTANCE);
		_dateType = TLModelUtil.addDatatype(module, module, "Date", Kind.DATE, directMapping(Date.class));
	}

	@Override
	protected void tearDown() throws Exception {
		_controls = null;
		_context = null;

		super.tearDown();
	}

	/** A value holding several texts is displayed as one text input per text. */
	public void testSeveralTextsAreDisplayedAsAList() {
		ReactControl control = control(_textType, true, List.of("first", "second"));

		assertEquals(ReactValueListControl.class, control.getClass());
	}

	/** A value holding a single text is displayed by the text input itself. */
	public void testASingleTextIsDisplayedAsATextInput() {
		ReactControl control = control(_textType, false, "only one");

		assertEquals(ReactTextInputControl.class, control.getClass());
	}

	/**
	 * A value holding several numbers is displayed as one number input per number, each writing its
	 * value in the format of the field.
	 */
	public void testSeveralNumbersAreDisplayedAsAList() {
		assertEquals(ReactValueListControl.class,
			control(_numberType, true, List.of(Long.valueOf(1), Long.valueOf(2))).getClass());
		assertNotNull("Each number of the collection is written in the format of one number.",
			spec(_numberType, true).getNumberFormat());
	}

	/**
	 * A value holding several points in time is displayed as one date input per point in time, each
	 * writing its value in the format of the field.
	 */
	public void testSeveralDatesAreDisplayedAsAList() {
		assertEquals(ReactValueListControl.class,
			control(_dateType, true, List.of(new Date(0), new Date(1))).getClass());
		assertNotNull("Each point in time of the collection is written in the format of one date.",
			spec(_dateType, true).getDateFormat());
	}

	/** The element controls of a value list are the ones a single value is displayed by. */
	public void testTheElementsOfAListAreDisplayedLikeSingleValues() {
		assertEquals(ReactNumberInputControl.class, control(_numberType, false, Long.valueOf(1)).getClass());
		assertEquals(ReactDatePickerControl.class, control(_dateType, false, new Date(0)).getClass());
	}

	/**
	 * A control that edits the whole collection itself says so, which is what keeps a value list
	 * from being built around it.
	 */
	public void testCollectionEditingControlsAreLeftAlone() {
		assertTrue("A selection is made in one control, however many options it accepts.",
			new SelectControlProvider().editsCollections());
		assertTrue("A property holding several configurations is edited by the list editor.",
			new ConfigFieldControlProvider().editsCollections());
		assertTrue("The annotations of a model element are edited as a whole.",
			new AnnotationsFieldControlProvider().editsCollections());
	}

	/** A control that enters one value is the element control of a value list. */
	public void testSingleValueControlsBuildTheElementsOfAValueList() {
		assertFalse("A truth value is entered one value at a time.",
			new BooleanControlProvider().editsCollections());
		assertFalse("A point in time is entered one value at a time.",
			new DatePickerControlProvider().editsCollections());
		assertFalse("A text is entered one value at a time.", FieldControlRegistry.TEXT.editsCollections());
	}

	/**
	 * A column reaching a single-valued attribute over a multi-valued step shows all the values it
	 * collected: the attribute decides which control writes a value, the column how many values
	 * there are.
	 */
	public void testACollectedAttributeIsDisplayedAsAList() {
		TLStructuredTypePart part = TLModelUtil.addProperty(_rowType, "title", _textType);

		assertEquals("The title of one object is one text.",
			ReactTextInputControl.class, display(ColumnType.of(part), "T1").getClass());
		assertEquals("The titles collected over several objects are a list of texts.",
			ReactValueListControl.class,
			display(ColumnType.of(part).collected(), List.of("T1", "T2")).getClass());
	}

	/**
	 * The user arranges the values of an ordered attribute: the order of its values is part of its
	 * value, so the value list offers moving one of them.
	 */
	public void testTheValuesOfAnOrderedAttributeAreArranged() {
		ReactControl control = control(property("orderedTexts", _textType, true, true), List.of("first", "second"));

		assertEquals(ReactValueListControl.class, control.getClass());
		assertEquals(Boolean.TRUE, control.scriptingScalarState().get(ReactValueListControl.ORDERED));
	}

	/**
	 * The values of an unordered attribute form a set: moving one of them would change nothing, so
	 * the value list offers no arranging.
	 */
	public void testTheValuesOfAnUnorderedAttributeAreNotArranged() {
		ReactControl control = control(property("texts", _textType, true, false), List.of("first", "second"));

		assertEquals(ReactValueListControl.class, control.getClass());
		assertEquals(Boolean.FALSE, control.scriptingScalarState().get(ReactValueListControl.ORDERED));
	}

	/**
	 * A single value has nothing to arrange, whatever the attribute holding it says about the order
	 * of its values.
	 */
	public void testASingleValueIsNotArranged() {
		ReactControl control = control(property("text", _textType, false, true), "only one");

		assertEquals("A single text is entered in the control of one text, which arranges nothing.",
			ReactTextInputControl.class, control.getClass());
	}

	/**
	 * A value the view itself holds is not arranged: with no attribute holding it, nothing stores an
	 * order of its values.
	 */
	public void testAValueWithoutAnAttributeIsNotArranged() {
		assertFalse("Nothing states an order for a value no attribute holds.",
			spec(_textType, true).isOrdered());
	}

	/**
	 * A cell holding several numbers is searched by the text it shows: every number in the format
	 * of the column, separated from the next.
	 */
	public void testACollectionOfNumbersIsSearchedByAllOfItsValues() {
		Format format = HTMLFormatter.getInstance().getLongFormat();
		List<Long> values = List.of(Long.valueOf(1234), Long.valueOf(5678));

		assertEquals(format.format(values.get(0)) + ", " + format.format(values.get(1)),
			column("numbers", _numberType, true).searchText(values));
	}

	/**
	 * The control editing the value of the given attribute.
	 *
	 * @param part
	 *        The edited attribute.
	 * @param value
	 *        The edited value.
	 */
	private ReactControl control(TLStructuredTypePart part, Object value) {
		return _controls.createFieldControl(_context, part, new AbstractFieldModel(value));
	}

	/**
	 * An attribute of the type under test.
	 *
	 * @param name
	 *        The name of the attribute.
	 * @param type
	 *        The type of its values.
	 * @param multiple
	 *        Whether it holds a collection of values rather than a single one.
	 * @param ordered
	 *        Whether the order of its values is part of its value.
	 */
	private TLStructuredTypePart property(String name, TLType type, boolean multiple, boolean ordered) {
		TLStructuredTypePart part = TLModelUtil.addProperty(_rowType, name, type);
		part.setMultiple(multiple);
		part.setOrdered(ordered);
		return part;
	}

	/** The control displaying the given value in a cell of the described column. */
	private ReactControl display(ColumnType columnType, Object value) {
		return _controls.createDisplayControl(_context, columnType, value);
	}

	/**
	 * The control displaying a value of the given type, bound to a field the user cannot change.
	 *
	 * @param type
	 *        The model type of the value.
	 * @param multiple
	 *        Whether the value is a collection of values rather than a single one.
	 * @param value
	 *        The displayed value.
	 */
	private ReactControl control(TLType type, boolean multiple, Object value) {
		return control(type, multiple, new AbstractFieldModel(value));
	}

	/**
	 * The control displaying the value of the given field, which the user cannot change.
	 *
	 * @param type
	 *        The model type of the value.
	 * @param multiple
	 *        Whether the value is a collection of values rather than a single one.
	 * @param model
	 *        Holds the displayed value.
	 */
	private ReactControl control(TLType type, boolean multiple, AbstractFieldModel model) {
		model.setEditable(false);
		return _controls.createFieldControl(_context, type, spec(type, multiple, model), model);
	}

	/**
	 * The service resolving the controls, configured as the application configures it minus the
	 * controls declared for a model type.
	 *
	 * <p>
	 * Which control a model type is edited by is a question of its own; what is under test here is
	 * that the multiplicity of the field reaches the control whichever step of the resolution
	 * answered it.
	 * </p>
	 */
	private static FieldControlService controlService() {
		FieldControlService.Config config = TypedConfiguration.newConfigItem(FieldControlService.Config.class);
		config.setImplementationClass(FieldControlService.class);
		return (FieldControlService) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/** The description of a value of the given type, as a display builds it. */
	private static FieldSpec spec(TLType type, boolean multiple) {
		return spec(type, multiple, new AbstractFieldModel(null));
	}

	/** The description of the value of the given field, as a display builds it. */
	private static FieldSpec spec(TLType type, boolean multiple, FieldModel model) {
		return FieldControlService.fieldSpec(type, type, LABEL, multiple, model);
	}

	/**
	 * The type-derived column over an attribute of the given type, whose rows are the cell values
	 * themselves.
	 */
	private Column<Object, ?> column(String name, TLType type, boolean multiple) {
		return ColumnProviderService.getInstance()
			.createColumn(name, ResKey.text(name), ColumnType.of(property(name, type, multiple, false)), row -> row);
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
	 * Test suite requiring the {@link ColumnProviderService} building the columns under test and the
	 * {@link AttributeSettings} their display of an attribute value consults.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestFieldControlServiceMultiplicity.class,
				ColumnProviderService.Module.INSTANCE,
				AttributeSettings.Module.INSTANCE));
	}

}
