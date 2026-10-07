/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.LongID;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.DynamicVisibility;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.annotate.TLDynamicVisibility;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.Pointer;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests how an {@link AttributeFieldControl} applies the mode {@link FormVisibility#DISABLED}
 * computed by the {@link TLDynamicVisibility} of its attribute.
 *
 * <p>
 * A disabled field is never editable. In edit mode it is {@link FieldModel#isDisabled() disabled},
 * so its input is presented as an inactive input; in view mode it shows its value like any other
 * field.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestAttributeFieldDisabledMode extends BasicTestCase {

	/** Name of the attribute whose mode the tests compute. */
	private static final String TITLE = "title";

	/** Name of the multi-valued attribute the value-list tests edit. */
	private static final String ALIASES = "aliases";

	/** Name of the attribute the {@link Lock} of the value-list tests depends on. */
	private static final String LOCKED = "locked";

	/** Name of the property {@link TLDynamicVisibility#getModeSelector()}. */
	private static final String MODE_SELECTOR = "mode-selector";

	private ReactContext _context;

	private TLProperty _title;

	private TLProperty _aliases;

	private TLProperty _locked;

	private FormControl _form;

	private AttributeFieldControl _field;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.attributeFieldDisabledMode");
		TLPrimitive text =
			TLModelUtil.addDatatype(module, module, "Text", Kind.STRING, directMapping(String.class));
		TLClass ticketType = TLModelUtil.addClass(module, "Ticket");
		_title = TLModelUtil.addProperty(ticketType, TITLE, text);
		_aliases = TLModelUtil.addProperty(ticketType, ALIASES, text);
		_aliases.setMultiple(true);
		_aliases.setOrdered(true);
		_locked = TLModelUtil.addProperty(ticketType, LOCKED, text);

		IdentifiedObject ticket = new IdentifiedObject(ticketType);
		ticket.tUpdateByName(TITLE, "Login fails");
		ticket.tUpdateByName(ALIASES, List.of("A1", "A2"));

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_form = new FormControl(_context, ticket, "no model", NoTokenHandling.INSTANCE);
		_form.setModelScope(new NoScope());
	}

	@Override
	protected void tearDown() throws Exception {
		_form = null;
		_field = null;
		_title = null;
		_aliases = null;
		_locked = null;
		_context = null;

		super.tearDown();
	}

	/**
	 * In edit mode, a disabled field is not editable and presented as an inactive input.
	 */
	public void testDisabledInEditMode() {
		createField(FormVisibility.DISABLED);
		_form.enterEditMode();

		assertFalse(model().isEditable());
		assertTrue(model().isDisabled());
		assertEquals(Boolean.FALSE, inputState(FieldState.EDITABLE__PROP));
		assertEquals(Boolean.TRUE, inputState(FieldState.DISABLED__PROP));
	}

	/**
	 * In view mode, a disabled field shows its value like any other field.
	 */
	public void testDisabledInViewMode() {
		createField(FormVisibility.DISABLED);

		assertFalse(model().isEditable());
		assertFalse(model().isDisabled());
		assertEquals(Boolean.FALSE, inputState(FieldState.DISABLED__PROP));
	}

	/**
	 * Leaving edit mode takes the disabled presentation back.
	 */
	public void testDisabledResetOnCancel() {
		createField(FormVisibility.DISABLED);
		_form.enterEditMode();
		assertTrue(model().isDisabled());

		_form.executeCancel();

		assertFalse(_form.isEditMode());
		assertFalse(model().isDisabled());
		assertFalse(model().isEditable());
		assertEquals(Boolean.FALSE, inputState(FieldState.DISABLED__PROP));
	}

	/**
	 * A read-only field in edit mode is not editable, but also not disabled: it shows its value
	 * instead of an inactive input.
	 */
	public void testReadOnlyIsNotDisabled() {
		createField(FormVisibility.READ_ONLY);
		_form.enterEditMode();

		assertFalse(model().isEditable());
		assertFalse(model().isDisabled());
	}

	/**
	 * An editable field in edit mode is editable and not disabled.
	 */
	public void testEditableIsNotDisabled() {
		createField(FormVisibility.EDITABLE);
		_form.enterEditMode();

		assertTrue(model().isEditable());
		assertFalse(model().isDisabled());
	}

	/**
	 * A value list that is locked while edited shows its values as inactive inputs, and as
	 * read-only values once the form leaves edit mode.
	 */
	public void testValueListLockedThenView() {
		Lock lock = createListField();
		_form.enterEditMode();
		assertElements(true, false);

		lock(lock, true);
		assertElements(false, true);

		_form.executeCancel();
		assertElements(false, false);
	}

	/**
	 * A value list that is locked while edited and then saved shows its values as read-only
	 * values.
	 */
	public void testValueListLockedThenSaved() {
		Lock lock = createListField();
		_form.enterEditMode();
		lock(lock, true);

		_form.executeSave();
		assertFalse(_form.isEditMode());
		assertElements(false, false);
	}

	/**
	 * A value list that is edited while locked shows its values as inactive inputs, and as editable
	 * inputs once unlocked.
	 */
	public void testValueListEditLockedThenUnlock() {
		Lock lock = createListField();
		lock(lock, true);
		assertElements(false, false);

		_form.enterEditMode();
		assertElements(false, true);

		lock(lock, false);
		assertElements(true, false);

		_form.executeCancel();
		assertElements(false, false);
	}

	/**
	 * Repeated edit sessions of a locked value list leave its values read-only in view mode.
	 */
	public void testValueListLockedRepeatedly() {
		Lock lock = createListField();
		_form.enterEditMode();
		lock(lock, true);
		_form.executeCancel();
		_form.enterEditMode();
		assertElements(false, true);
		_form.executeCancel();

		assertElements(false, false);
	}

	private Lock createListField() {
		_aliases.setAnnotation(lockVisibility());
		Lock lock = (Lock) DynamicVisibility.modeSelector(_aliases);
		_field = new AttributeFieldControl(_context, _form, _form, ALIASES, null, false, null);
		_field.createChromeControl();
		_form.attach();
		return lock;
	}

	private void lock(Lock lock, boolean locked) {
		lock.setLocked(locked);
		_form.notifyFieldChanged(_locked);
	}

	private void assertElements(boolean editable, boolean disabled) {
		List<ReactControl> elements = _field.getInnerControl().displayedChildren();
		assertEquals(2, elements.size());
		for (ReactControl element : elements) {
			FieldModel elementModel = ((ReactFormFieldControl) element).getFieldModel();
			assertEquals("editable", editable, elementModel.isEditable());
			assertEquals("disabled", disabled, elementModel.isDisabled());
			assertEquals(Boolean.valueOf(editable), element.scriptingScalarState().get(FieldState.EDITABLE__PROP));
			assertEquals(Boolean.valueOf(disabled), element.scriptingScalarState().get(FieldState.DISABLED__PROP));
		}
	}

	private static TLDynamicVisibility lockVisibility() {
		Lock.Config<?> selector = TypedConfiguration.newConfigItem(Lock.Config.class);
		TLDynamicVisibility annotation = TypedConfiguration.newConfigItem(TLDynamicVisibility.class);
		annotation.update(annotation.descriptor().getProperty(MODE_SELECTOR), selector);
		return annotation;
	}

	private void createField(FormVisibility mode) {
		_title.setAnnotation(dynamicVisibility(mode));
		_field = new AttributeFieldControl(_context, _form, _form, TITLE, null, false, null);
		_field.createChromeControl();
		_form.attach();
	}

	private FieldModel model() {
		return ((ReactFormFieldControl) _field.getInnerControl()).getFieldModel();
	}

	private Object inputState(String key) {
		return _field.getInnerControl().scriptingScalarState().get(key);
	}

	private static TLDynamicVisibility dynamicVisibility(FormVisibility mode) {
		FixedMode.Config<?> selector = TypedConfiguration.newConfigItem(FixedMode.Config.class);
		selector.setMode(mode);
		TLDynamicVisibility annotation = TypedConfiguration.newConfigItem(TLDynamicVisibility.class);
		annotation.update(annotation.descriptor().getProperty(MODE_SELECTOR), selector);
		return annotation;
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
	 * {@link ModeSelector} computing a configured mode for every object.
	 */
	public static class FixedMode extends AbstractConfiguredInstance<FixedMode.Config<?>> implements ModeSelector {

		/**
		 * Configuration options for {@link FixedMode}.
		 */
		public interface Config<I extends FixedMode> extends PolymorphicConfiguration<I> {

			/** Configuration name for {@link #getMode()}. */
			String MODE = "mode";

			/**
			 * The mode computed for every object.
			 */
			@Name(MODE)
			FormVisibility getMode();

			/** @see #getMode() */
			void setMode(FormVisibility value);
		}

		/**
		 * Creates a {@link FixedMode} from configuration.
		 */
		public FixedMode(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public FormVisibility getMode(TLObject object, TLStructuredTypePart attribute, boolean editMode) {
			return getConfig().getMode();
		}

		@Override
		public void traceDependencies(TLObject object, TLStructuredTypePart attribute, boolean editMode,
				Sink<Pointer> trace, OverlayLookup overlays) {
			// Depends on nothing.
		}
	}

	/**
	 * {@link ModeSelector} disabling a field while a lock is set, depending on the attribute
	 * {@link #LOCKED}.
	 */
	public static class Lock extends AbstractConfiguredInstance<Lock.Config<?>> implements ModeSelector {

		/**
		 * Configuration options for {@link Lock}.
		 */
		public interface Config<I extends Lock> extends PolymorphicConfiguration<I> {
			// No options.
		}

		private boolean _isLocked;

		/**
		 * Creates a {@link Lock} from configuration.
		 */
		public Lock(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		/**
		 * Sets or removes the lock.
		 */
		public void setLocked(boolean locked) {
			_isLocked = locked;
		}

		@Override
		public FormVisibility getMode(TLObject object, TLStructuredTypePart attribute, boolean editMode) {
			return _isLocked ? FormVisibility.DISABLED : FormVisibility.DEFAULT;
		}

		@Override
		public void traceDependencies(TLObject object, TLStructuredTypePart attribute, boolean editMode,
				Sink<Pointer> trace, OverlayLookup overlays) {
			trace.add(Pointer.create(object, object.tType().getPart(LOCKED)));
		}
	}

	/**
	 * Object with an identity that is not transient, which is what a form observes in its scope.
	 */
	private static class IdentifiedObject extends TransientTLObjectImpl {

		private static final MOClassImpl TABLE = new MOClassImpl("test");

		private static long _nextId = 1;

		private final ObjectKey _id =
			new DefaultObjectKey(1, Revision.CURRENT_REV, TABLE, LongID.valueOf(_nextId++));

		IdentifiedObject(TLStructuredType type) {
			super(type, null);
		}

		@Override
		public ObjectKey tId() {
			return _id;
		}

		@Override
		public boolean tTransient() {
			return false;
		}
	}

	/**
	 * {@link ModelScope} that delivers no changes.
	 */
	private static class NoScope implements ModelScope {

		@Override
		public boolean addModelListener(ModelListener listener) {
			return true;
		}

		@Override
		public boolean addModelListener(TLStructuredType type, ModelListener listener) {
			return true;
		}

		@Override
		public boolean addModelListener(TLObject object, ModelListener listener) {
			return true;
		}

		@Override
		public boolean removeModelListener(ModelListener listener) {
			return true;
		}

		@Override
		public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
			return true;
		}

		@Override
		public boolean removeModelListener(TLObject object, ModelListener listener) {
			return true;
		}
	}

	/**
	 * Test suite requiring a knowledge base, and the {@link FieldControlService} building the
	 * inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestAttributeFieldDisabledMode.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
