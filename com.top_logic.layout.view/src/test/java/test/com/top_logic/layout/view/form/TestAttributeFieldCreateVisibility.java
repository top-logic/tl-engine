/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.LongID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLDynamicVisibility;
import com.top_logic.model.annotate.Visibility;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that an {@link AttributeFieldControl} applies the create visibility of its attribute on a
 * new object, e.g. the draft of a create dialog, and its visibility on a stored object.
 */
@SuppressWarnings("javadoc")
public class TestAttributeFieldCreateVisibility extends BasicTestCase {

	/** Read-only, but entered when the object is created. */
	private static final String KEY = "key";

	/** Hidden when the object is created. */
	private static final String RESULT = "result";

	/** Without visibility annotations. */
	private static final String TITLE = "title";

	/** Name of the property {@link TLDynamicVisibility#getModeSelector()}. */
	private static final String MODE_SELECTOR = "mode-selector";

	private ReactContext _context;

	private TLClass _type;

	private TLProperty _key;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.attributeFieldCreateVisibility");
		TLPrimitive text =
			TLModelUtil.addDatatype(module, module, "Text", Kind.STRING, directMapping(String.class));
		_type = TLModelUtil.addClass(module, "Project");
		_key = TLModelUtil.addProperty(_type, KEY, text);
		_key.setAnnotation(DisplayAnnotations.newVisibility(Visibility.READ_ONLY));
		_key.setAnnotation(DisplayAnnotations.newCreateVisibility(Visibility.EDITABLE));
		TLProperty result = TLModelUtil.addProperty(_type, RESULT, text);
		result.setAnnotation(DisplayAnnotations.newCreateVisibility(Visibility.HIDDEN));
		TLModelUtil.addProperty(_type, TITLE, text);
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_type = null;
		_key = null;
		super.tearDown();
	}

	/**
	 * On the draft of a new object, the create visibility applies: a read-only attribute that is
	 * editable on creation can be entered, an attribute hidden on creation is not shown.
	 */
	public void testNewObject() {
		FormControl form = form(TransientObjectFactory.INSTANCE.createObject(_type));
		AttributeFieldControl key = field(form, KEY);
		AttributeFieldControl result = field(form, RESULT);
		AttributeFieldControl title = field(form, TITLE);
		form.enterEditMode();

		assertTrue(model(key).isEditable());
		assertFalse("Hidden on creation.", isShown(result));
		assertTrue(model(title).isEditable());
	}

	/**
	 * On a stored object, the visibility applies.
	 */
	public void testStoredObject() {
		FormControl form = form(new IdentifiedObject(_type));
		AttributeFieldControl key = field(form, KEY);
		AttributeFieldControl result = field(form, RESULT);
		AttributeFieldControl title = field(form, TITLE);
		form.enterEditMode();

		assertFalse(model(key).isEditable());
		assertTrue(isShown(result));
		assertTrue(model(result).isEditable());
		assertTrue(model(title).isEditable());
	}

	/**
	 * A dynamic visibility takes precedence over the create visibility.
	 */
	public void testDynamicVisibilityFirst() {
		_key.setAnnotation(dynamicVisibility(FormVisibility.READ_ONLY));
		FormControl form = form(TransientObjectFactory.INSTANCE.createObject(_type));
		AttributeFieldControl key = field(form, KEY);
		form.enterEditMode();

		assertFalse(model(key).isEditable());
	}

	private FormControl form(TLObject object) {
		FormControl form = new FormControl(_context, object, "no model", NoTokenHandling.INSTANCE);
		form.setModelScope(new NoScope());
		return form;
	}

	private AttributeFieldControl field(FormControl form, String attribute) {
		AttributeFieldControl field = new AttributeFieldControl(_context, form, form, attribute, null, false, null);
		field.createChromeControl();
		form.attach();
		return field;
	}

	private static boolean isShown(AttributeFieldControl field) {
		return field.getChromeControl().isVisible() && field.getInnerControl() instanceof ReactFormFieldControl;
	}

	private static FieldModel model(AttributeFieldControl field) {
		return ((ReactFormFieldControl) field.getInnerControl()).getFieldModel();
	}

	private static TLDynamicVisibility dynamicVisibility(FormVisibility mode) {
		TestAttributeFieldDisabledMode.FixedMode.Config<?> selector =
			TypedConfiguration.newConfigItem(TestAttributeFieldDisabledMode.FixedMode.Config.class);
		selector.setMode(mode);
		TLDynamicVisibility annotation = TypedConfiguration.newConfigItem(TLDynamicVisibility.class);
		annotation.update(annotation.descriptor().getProperty(MODE_SELECTOR), selector);
		return annotation;
	}

	private static StorageMapping<?> directMapping(Class<?> applicationType) throws ConfigurationException {
		PolymorphicConfiguration<?> config =
			TypedConfiguration.createConfigItemForImplementationClass(DirectMapping.class);
		config.update(config.descriptor().getProperty(DirectMapping.Config.APPLICATION_TYPE), applicationType);
		return (StorageMapping<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Object with an identity that is not transient, standing for a stored object.
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
		public Registration addModelListener(ModelListener listener) {
			return Registration.NONE;
		}

		@Override
		public Registration addModelListener(TLStructuredType type, ModelListener listener) {
			return Registration.NONE;
		}

		@Override
		public Registration addModelListener(TLObject object, ModelListener listener) {
			return Registration.NONE;
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
	 * Test suite requiring a knowledge base, and the {@link FieldControlService} building the
	 * inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestAttributeFieldCreateVisibility.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
