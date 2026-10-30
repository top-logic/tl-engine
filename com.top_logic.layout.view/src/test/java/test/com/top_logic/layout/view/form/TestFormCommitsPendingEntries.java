/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.PolymorphicOptions;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.command.I18NConstants;
import com.top_logic.layout.view.form.AnnotationsFieldControlProvider;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormParticipant;
import com.top_logic.layout.react.control.form.PendingInput;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.TLModuleDisplayGroup;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Tests that saving a {@link FormControl} first confirms the entries a configuration list editor
 * in one of its fields holds unconfirmed, see {@link FormParticipant#commitPendingInput()} and
 * {@link PendingInput}.
 *
 * <p>
 * The field is edited by the annotation editor of the model editor
 * ({@link AnnotationsFieldControlProvider}), where an annotation added with "+" stays pending until
 * it is confirmed.
 * </p>
 */
public class TestFormCommitsPendingEntries extends BasicTestCase {

	/** Name of the attribute holding the edited annotations. */
	private static final String ANNOTATIONS = "annotations";

	/** Label of the button confirming a pending entry. */
	private static final String CONFIRM = "✓";

	private ReactContext _context;

	private TLObject _object;

	private FormControl _form;

	private AttributeFieldControl _field;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.formCommitsPendingEntries");
		TLPrimitive annotationType =
			TLModelUtil.addDatatype(module, module, "Annotation", Kind.CUSTOM, directMapping(Object.class));
		TLClass type = TLModelUtil.addClass(module, "Annotated");
		TLProperty annotations = TLModelUtil.addProperty(type, ANNOTATIONS, annotationType);
		annotations.setMultiple(true);
		annotations.setOrdered(true);

		_object = new TransientTLObjectImpl(type, null) {
			// Accessible constructor.
		};

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_form = new FormControl(_context, _object, "no model", NoTokenHandling.INSTANCE);
		_form.setModelScope(new NoScope());

		PolymorphicConfiguration<? extends ReactFieldControlProvider> control =
			TypedConfiguration.createConfigItemForImplementationClass(ModuleAnnotations.class);
		_field = new AttributeFieldControl(_context, _form, _form, ANNOTATIONS, null, false, null, null, control);
		_field.createChromeControl();
		_form.attach();
	}

	@Override
	protected void tearDown() throws Exception {
		_field = null;
		_form = null;
		_object = null;
		_context = null;

		super.tearDown();
	}

	/**
	 * An entry added but not confirmed is saved with the form, as if it had been confirmed.
	 */
	public void testUnconfirmedEntryIsSaved() {
		_form.enterEditMode();
		click(addButton());
		fillMandatoryValues();
		assertNotNull("The new entry must be pending.", button(CONFIRM));
		assertTrue("A pending entry is not yet part of the value.", storedAnnotations().isEmpty());

		_form.revealAllValidation();
		assertFalse("A pending entry alone must not make the form look invalid: Save confirms it.",
			_form.hasVisibleErrors());

		_form.executeSave();

		assertFalse("The save must have gone through.", _form.isEditMode());
		assertEquals("The pending entry must have been saved.", 1, storedAnnotations().size());
	}

	/**
	 * An entry whose confirmation is refused keeps the form from being saved, and stays pending, so
	 * that nothing the user entered is lost.
	 */
	public void testRefusedEntryRefusesTheSave() {
		_form.enterEditMode();
		// Two entries of the same kind: the annotations are keyed by their kind, so only the first
		// can be confirmed.
		click(addButton());
		click(addButton());
		List<ReactDropdownSelectControl> selectors = typeSelectors();
		FieldModel first = selectors.get(0).getFieldModel();
		FieldModel second = selectors.get(selectors.size() - 1).getFieldModel();
		assertFalse("A new entry is preselected with a type not yet used.",
			first.getValue().equals(second.getValue()));
		second.setValue(first.getValue());

		TopLogicException refusal = null;
		try {
			_form.executeSave();
		} catch (TopLogicException ex) {
			refusal = ex;
		}

		assertNotNull("The save must be refused.", refusal);
		assertEquals(com.top_logic.layout.configedit.I18NConstants.ERROR_ENTRY_NOT_TAKEN__REASON.getKey(),
			refusal.getErrorKey().getKey());
		assertTrue("The form must stay in edit mode.", _form.isEditMode());
		assertTrue("Nothing must have been stored.", storedAnnotations().isEmpty());
		assertEquals("The entry that could be confirmed is in the field's value.", 1, fieldValue().size());
		assertNotNull("The refused entry must still be pending.", button(CONFIRM));

		String message = Resources.getInstance().getString(refusal.getErrorKey());
		assertTrue("The type must be named as the type selector names it: " + message,
			message.contains(PolymorphicOptions.labelFor(TLModuleDisplayGroup.class)));
		assertFalse("The type must not be named by its class: " + message, message.contains("interface "));

		selectors = typeSelectors();
		assertNotNull("The refused entry's type selector must show the reason.",
			selectors.get(selectors.size() - 1).getFieldModel().getError());

		_form.executeCancel();
		assertTrue(storedAnnotations().isEmpty());
	}

	/**
	 * An entry lacking a mandatory value is confirmed - confirming asks for its key only - and then
	 * refuses the save as any other incomplete entry of the field does, staying in the field's value.
	 */
	public void testIncompleteEntryIsConfirmedAndRefusesTheSave() {
		_form.enterEditMode();
		click(addButton());

		TopLogicException refusal = null;
		try {
			_form.executeSave();
		} catch (TopLogicException ex) {
			refusal = ex;
		}

		assertNotNull("The incomplete entry must refuse the save.", refusal);
		assertEquals(I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS.getKey(), refusal.getErrorKey().getKey());
		assertTrue(_form.isEditMode());
		assertNull("The entry has been confirmed.", button(CONFIRM));
		assertEquals("The entry is kept in the field's value.", 1, fieldValue().size());
		assertTrue("Nothing must have been stored.", storedAnnotations().isEmpty());
	}

	/**
	 * Accepting the preselected type of each new entry never leads into a duplicate: a new entry
	 * is preselected with a type the list does not use yet.
	 */
	public void testNewEntriesArePreselectedWithUnusedTypes() {
		_form.enterEditMode();
		click(addButton());
		fillMandatoryValues();
		click(button(CONFIRM));
		click(addButton());
		fillMandatoryValues();

		_form.executeSave();

		assertFalse(_form.isEditMode());
		Collection<?> stored = storedAnnotations();
		assertEquals(2, stored.size());
		List<Class<?>> types = new ArrayList<>();
		for (Object annotation : stored) {
			types.add(((ConfigurationItem) annotation).descriptor().getConfigurationInterface());
		}
		assertFalse("The two annotations must be of different types: " + types,
			types.get(0) == types.get(1));
	}

	/**
	 * Without an unconfirmed entry, saving behaves as before: an untouched form is left without
	 * storing anything.
	 */
	public void testNoPendingEntry() {
		_form.enterEditMode();

		_form.executeSave();

		assertFalse(_form.isEditMode());
		assertTrue(storedAnnotations().isEmpty());
	}

	/**
	 * A confirmed entry is saved as it always was.
	 */
	public void testConfirmedEntryIsSaved() {
		_form.enterEditMode();
		click(addButton());
		fillMandatoryValues();
		click(button(CONFIRM));
		assertNull("Nothing is pending anymore.", button(CONFIRM));

		_form.executeSave();

		assertFalse(_form.isEditMode());
		assertEquals(1, storedAnnotations().size());
	}

	private Collection<?> storedAnnotations() {
		return (Collection<?>) _object.tValueByName(ANNOTATIONS);
	}

	/** What the form buffers for the attribute, not yet stored. */
	private Collection<?> fieldValue() {
		return (Collection<?>) _form.getOverlay().tValueByName(ANNOTATIONS);
	}

	/** Fills every empty mandatory text value of the edited annotations. */
	private void fillMandatoryValues() {
		fillMandatoryValues(_field.getInnerControl());
	}

	private static void fillMandatoryValues(ReactControl control) {
		if (control instanceof ReactFormFieldControl input
			&& input.getFieldModel() instanceof ConfigFieldModel field
			&& field.getProperty().isMandatory()
			&& field.getProperty().getType() == String.class
			&& StringServices.isEmpty(field.getValue())) {
			field.setValue("filled");
		}
		for (ReactControl child : control.scriptingChildren()) {
			fillMandatoryValues(child);
		}
	}

	/** The type selectors of the edited annotations, in display order. */
	private List<ReactDropdownSelectControl> typeSelectors() {
		List<ReactDropdownSelectControl> result = new ArrayList<>();
		collectSelectors(_field.getInnerControl(), result);
		return result;
	}

	private static void collectSelectors(ReactControl control, List<ReactDropdownSelectControl> result) {
		if (control instanceof ReactDropdownSelectControl select) {
			result.add(select);
		}
		for (ReactControl child : control.scriptingChildren()) {
			collectSelectors(child, result);
		}
	}

	private static void click(ReactButtonControl button) {
		assertNotNull(button);
		assertTrue(button.executeCommand("click", Collections.emptyMap()).isSuccess());
	}

	private ReactButtonControl addButton() {
		return find(_field.getInnerControl(), true, null);
	}

	private ReactButtonControl button(String label) {
		return find(_field.getInnerControl(), false, label);
	}

	/** The first button below the given control with the given label, or starting with "+". */
	private static ReactButtonControl find(ReactControl control, boolean add, String label) {
		if (control instanceof ReactButtonControl button) {
			String buttonLabel = String.valueOf(button.scriptingScalarState().get("label"));
			if (add ? buttonLabel.startsWith("+") : buttonLabel.equals(label)) {
				return button;
			}
		}
		for (ReactControl child : control.scriptingChildren()) {
			ReactButtonControl found = find(child, add, label);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * A mapping storing the values as they are.
	 */
	private static StorageMapping<?> directMapping(Class<?> applicationType) throws ConfigurationException {
		PolymorphicConfiguration<?> config =
			TypedConfiguration.createConfigItemForImplementationClass(DirectMapping.class);
		config.update(config.descriptor().getProperty(DirectMapping.Config.APPLICATION_TYPE), applicationType);
		return (StorageMapping<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Edits the field as the annotations of a module, with the editor of
	 * {@link AnnotationsFieldControlProvider}.
	 */
	public static class ModuleAnnotations implements ReactFieldControlProvider {

		@Override
		public boolean editsCollections() {
			return true;
		}

		@Override
		public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
			return AnnotationsFieldControlProvider.createControl(context, model,
				() -> TypedConfiguration.newConfigItem(ModuleConfig.class));
		}
	}

	/**
	 * {@link ModelScope} that delivers no changes.
	 */
	private static class NoScope implements ModelScope {

		@Override
		public Registration addModelListener(ModelListener listener) {
			return Registration.onDispose(() -> removeModelListener(listener));
		}

		@Override
		public Registration addModelListener(TLStructuredType type, ModelListener listener) {
			return Registration.onDispose(() -> removeModelListener(type, listener));
		}

		@Override
		public Registration addModelListener(TLObject object, ModelListener listener) {
			return Registration.onDispose(() -> removeModelListener(object, listener));
		}

		@Deprecated
		@Override
		public boolean removeModelListener(ModelListener listener) {
			return true;
		}

		@Deprecated
		@Override
		public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
			return true;
		}

		@Deprecated
		@Override
		public boolean removeModelListener(TLObject object, ModelListener listener) {
			return true;
		}
	}

	/**
	 * Suite with the services the form builds its field and the configuration editor its entries
	 * with.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFormCommitsPendingEntries.class,
			ServiceTestSetup.createStarterFactoryForModules(
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				ModelService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
