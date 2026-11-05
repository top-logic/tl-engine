/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeWithFallbackStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.state.CheckboxState;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FallbackPlaceholder;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.providers.OptionsByExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Tests {@link FallbackPlaceholder} for the kinds of input other than a text field: the inherited
 * value is shown while the field holds no explicit value, follows the attribute it is read from,
 * an entered value is kept explicitly, and the reset of the field container removes it again.
 *
 * <p>
 * Each attribute {@code x} with a fallback keeps its explicit value in {@code customX} and takes
 * its fallback from {@code defaultX}, a stored attribute edited in the same form.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFallbackFieldKinds extends AbstractTicketFormTest {

	private static final String STAGE = "stage";

	private static final String STAGES = "stages";

	private static final String PARENT = "parent";

	private static final String COUNT = "count";

	private static final String DUE = "due";

	private static final String URGENT = "urgent";

	private static final String CUSTOM = "custom";

	private static final String DEFAULT = "default";

	/** The options expression offering nothing. */
	private static final String NO_OPTIONS = "x -> []";

	private static final String CSS_EXPLICIT = "tl-info";

	private static final String CSS_FALLBACK = "tl-debug";

	private TLEnumeration _stageType;

	private TLClassifier _open;

	private TLClassifier _closed;

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		try {
			_stageType = TLModelUtil.addEnumeration(module, "Stage");
			_open = TLModelUtil.addClassifier(_stageType, "open");
			_closed = TLModelUtil.addClassifier(_stageType, "closed");
			addWithFallback(STAGE, _stageType, false);
			addWithFallback(STAGES, _stageType, true);
			addWithFallback(PARENT, _ticketType, false);
			// The objects of a type of a transient model are not found by a query; the field offers no
			// options of its own and shows the value it holds.
			_ticketType.getPart(PARENT).setAnnotation(noOptions());
			_ticketType.getPart(fallback(PARENT)).setAnnotation(noOptions());
			addWithFallback(COUNT,
				TLModelUtil.addDatatype(module, module, "Count", Kind.INT, directMapping(Long.class)), false);
			addWithFallback(DUE,
				TLModelUtil.addDatatype(module, module, "Day", Kind.DATE, directMapping(Date.class)), false);
			addWithFallback(URGENT,
				TLModelUtil.addDatatype(module, module, "Flag", Kind.BOOLEAN, directMapping(Boolean.class)), false);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Adds the attribute of the given name with a fallback, its storage attribute and its fallback
	 * attribute, all of the given type.
	 */
	private void addWithFallback(String name, TLType type, boolean multiple) {
		TLModelUtil.addProperty(_ticketType, custom(name), type).setMultiple(multiple);
		TLModelUtil.addProperty(_ticketType, fallback(name), type).setMultiple(multiple);
		TLStructuredTypePart part = TLModelUtil.addProperty(_ticketType, name, type);
		part.setMultiple(multiple);
		part.setAnnotation(fallbackStorage(custom(name), fallback(name)));
		// A part of a transient model does not initialize its storage.
		((StorageImplementation) part.getStorageImplementation()).init(part);
	}

	/**
	 * A select of a classifier shows the label of the inherited classifier where it holds none, and
	 * follows a change of it; a chosen classifier is stored explicitly, the reset falls back again.
	 */
	public void testSelect() {
		_ticket.tUpdateByName(fallback(STAGE), _open);
		AttributeFieldControl field = field(STAGE);
		AttributeFieldControl source = field(fallback(STAGE));
		_form.enterEditMode();

		ReactDropdownSelectControl select = (ReactDropdownSelectControl) field.getInnerControl();
		assertEquals(select.formatValue(_open), emptyLabel(field));
		assertNotNull(emptyLabel(field));
		assertEquals(CSS_FALLBACK, field.getChromeControl().getCssClass());
		assertNull("Nothing to reset without an explicit value.", resetLabel(field));

		model(source).setValue(List.of(_closed));
		assertEquals("The placeholder follows the inherited classifier.",
			select.formatValue(_closed), emptyLabel(field));

		model(field).setValue(List.of(_open));
		assertEquals(CSS_EXPLICIT, field.getChromeControl().getCssClass());
		assertNotNull("An explicit value is offered to be reset.", resetLabel(field));

		// The classifiers of a transient model cannot be stored, so the edits are checked in the
		// form's buffer.
		assertEquals(_open, _form.getOverlay().tValueByName(custom(STAGE)));

		reset(field);
		assertNull(selection(field));
		assertEquals(select.formatValue(_closed), emptyLabel(field));
		assertEquals(CSS_FALLBACK, field.getChromeControl().getCssClass());
		assertNull("The reset leaves no explicit value.", _form.getOverlay().tValueByName(custom(STAGE)));
		assertEquals(_closed, _form.getOverlay().tValueByName(STAGE));
	}

	/**
	 * A select of another object shows the label of the inherited object where it holds none.
	 */
	public void testReferenceSelect() {
		IdentifiedObject epic = newTicket("Epic", "open");
		IdentifiedObject story = newTicket("Story", "open");
		_ticket.tUpdateByName(fallback(PARENT), epic);
		AttributeFieldControl field = field(PARENT);
		AttributeFieldControl source = field(fallback(PARENT));
		_form.enterEditMode();

		ReactFormFieldControl select = (ReactFormFieldControl) field.getInnerControl();
		assertEquals(select.formatValue(epic), emptyLabel(field));

		model(source).setValue(List.of(story));
		assertEquals(select.formatValue(story), emptyLabel(field));

		model(field).setValue(List.of(epic));
		assertEquals(CSS_EXPLICIT, field.getChromeControl().getCssClass());
		reset(field);
		assertEquals(CSS_FALLBACK, field.getChromeControl().getCssClass());
		assertEquals(select.formatValue(story), emptyLabel(field));
	}

	/**
	 * A select of several classifiers shows the inherited classifiers while its explicit value is
	 * empty; clearing the selection falls back again.
	 */
	public void testMultiSelect() {
		_ticket.tUpdateByName(fallback(STAGES), List.of(_open, _closed));
		AttributeFieldControl field = field(STAGES);
		_form.enterEditMode();

		ReactFormFieldControl select = (ReactFormFieldControl) field.getInnerControl();
		assertEquals(select.formatValue(_ticket.tValueByName(fallback(STAGES))), emptyLabel(field));
		assertEquals(CSS_FALLBACK, field.getChromeControl().getCssClass());

		model(field).setValue(List.of(_closed));
		assertEquals(CSS_EXPLICIT, field.getChromeControl().getCssClass());

		model(field).setValue(List.of());
		assertEquals("An empty selection is no explicit value.", CSS_FALLBACK, field.getChromeControl().getCssClass());
		assertNull(resetLabel(field));

		assertEquals(Set.of(_open, _closed), Set.copyOf((Collection<?>) _form.getOverlay().tValueByName(STAGES)));
	}

	/**
	 * A number field shows the inherited number in the field's format.
	 */
	public void testNumber() {
		_ticket.tUpdateByName(fallback(COUNT), 1200L);
		AttributeFieldControl field = field(COUNT);
		AttributeFieldControl source = field(fallback(COUNT));
		_form.enterEditMode();

		ReactNumberInputControl input = (ReactNumberInputControl) field.getInnerControl();
		assertEquals(input.getFormat().format(1200L), placeholder(field));

		model(source).setValue(7L);
		assertEquals(input.getFormat().format(7L), placeholder(field));

		model(field).setValue(3L);
		reset(field);
		assertNull(model(field).getValue());
		assertEquals(CSS_FALLBACK, field.getChromeControl().getCssClass());
	}

	/**
	 * A date field shows the inherited date in the field's format.
	 */
	public void testDate() {
		Date day = new Date(1780000000000L);
		_ticket.tUpdateByName(fallback(DUE), day);
		AttributeFieldControl field = field(DUE);
		_form.enterEditMode();

		ReactDatePickerControl input = (ReactDatePickerControl) field.getInnerControl();
		assertNotNull(placeholder(field));
		assertEquals(input.formatValue(day), placeholder(field));
		assertNull(model(field).getValue());
	}

	/**
	 * A boolean field is tri-state, its third state standing for the inherited value, which it
	 * describes; the reset returns to that state.
	 */
	public void testBoolean() {
		_ticket.tUpdateByName(fallback(URGENT), Boolean.TRUE);
		AttributeFieldControl field = field(URGENT);
		_form.enterEditMode();

		ReactCheckboxControl box = (ReactCheckboxControl) field.getInnerControl();
		assertEquals(Boolean.TRUE, box.scriptingScalarState().get(CheckboxState.TRI_STATE__PROP));
		assertNull(model(field).getValue());
		assertEquals(Resources.getInstance().getString(
			I18NConstants.VALUE_INHERITED__VALUE.fill(MetaLabelProvider.INSTANCE.getLabel(Boolean.TRUE))),
			placeholder(field));

		model(field).setValue(Boolean.FALSE);
		assertEquals(CSS_EXPLICIT, field.getChromeControl().getCssClass());
		assertNotNull(resetLabel(field));

		reset(field);
		assertNull(model(field).getValue());
		assertEquals(CSS_FALLBACK, field.getChromeControl().getCssClass());

		_form.executeSave();
		assertNull(_ticket.tValueByName(custom(URGENT)));
		assertEquals(Boolean.TRUE, _ticket.tValueByName(URGENT));
	}

	private static String custom(String name) {
		return CUSTOM + Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	private static String fallback(String name) {
		return DEFAULT + Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	private static Object placeholder(AttributeFieldControl field) {
		return field.getInnerControl().scriptingScalarState().get(FieldState.PLACEHOLDER__PROP);
	}

	private static Object emptyLabel(AttributeFieldControl field) {
		return field.getInnerControl().scriptingScalarState().get(DropdownSelectState.EMPTY_OPTION_LABEL__PROP);
	}

	private static Object resetLabel(AttributeFieldControl field) {
		return field.getChromeControl().scriptingScalarState().get(FormFieldState.RESET_LABEL__PROP);
	}

	private static Object selection(AttributeFieldControl field) {
		return model(field).getValue();
	}

	private static void reset(AttributeFieldControl field) {
		field.getChromeControl().executeCommand(ReactFormFieldChromeControl.RESET_COMMAND, Map.of());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static TLOptions noOptions() throws ConfigurationException {
		OptionsByExpression.Config<?> generator = TypedConfiguration.newConfigItem(OptionsByExpression.Config.class);
		generator.update(generator.descriptor().getProperty(OptionsByExpression.Config.FUNCTION),
			ExprFormat.INSTANCE.getValue(OptionsByExpression.Config.FUNCTION, NO_OPTIONS));
		TLOptions result = TypedConfiguration.newConfigItem(TLOptions.class);
		result.setGenerator((PolymorphicConfiguration) generator);
		return result;
	}

	/**
	 * Runs the test in the system context: an options expression is compiled against the
	 * application model, which reads the knowledge base.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> inSystemContext(TestFallbackFieldKinds.class,
			() -> {
				super.runTest();
				return null;
			});
	}

	private static TLStorage fallbackStorage(String storageAttribute, String fallbackAttribute) {
		AttributeWithFallbackStorage.Config<?> storageConfig =
			TypedConfiguration.newConfigItem(AttributeWithFallbackStorage.Config.class);
		storageConfig.update(storageConfig.descriptor().getProperty(AttributeWithFallbackStorage.Config.STORAGE_ATTRIBUTE),
			storageAttribute);
		storageConfig.update(storageConfig.descriptor().getProperty(AttributeWithFallbackStorage.Config.FALLBACK_ATTRIBUTE),
			fallbackAttribute);

		TLStorage annotation = TypedConfiguration.newConfigItem(TLStorage.class);
		annotation.setImplementation(storageConfig);
		return annotation;
	}

	/**
	 * Test suite requiring a knowledge base for the save, and the {@link FieldControlService}
	 * building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFallbackFieldKinds.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE));
	}
}
