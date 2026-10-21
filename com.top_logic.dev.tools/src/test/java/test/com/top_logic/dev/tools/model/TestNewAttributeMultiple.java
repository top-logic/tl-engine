/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dev.tools.model;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dev.tools.model.I18NConstants;
import com.top_logic.dev.tools.model.ModelEditAction;
import com.top_logic.dev.tools.model.ModelEditAction.Mode;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.form.DynamicVisibility;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;

/**
 * Test that the "new attribute" dialog of the model editor offers the multiple setting only for
 * value types that can store several values: the mode of {@code tl.devtools:NewAttribute#multiple}
 * and the validation of {@link ModelEditAction} in {@link Mode#CREATE_ATTRIBUTE} mode.
 */
@SuppressWarnings("javadoc")
public class TestNewAttributeMultiple extends TestCase {

	private static final String NEW_ATTRIBUTE = "tl.devtools:NewAttribute";

	private static final String NAME = "name";

	private static final String TYPE = "type";

	private static final String MULTIPLE = "multiple";

	/** Primitive type without the supports-multiple annotation. */
	private static final String TRISTATE = "tl.core:Tristate";

	/** Primitive type annotated with supports-multiple. */
	private static final String BINARY = "tl.core:Binary";

	/** A class type. */
	private static final String CLASS_TYPE = "tl.devtools:NewModule";

	/** Name of the channel holding the type to create the attribute in. */
	private static final String CONTAINER = "container";

	private static final String TEST_MODULE = "test.devtools.multiple";

	private static final String TEST_CLASS = "Owner";

	public void testModeForPrimitiveWithoutSupport() {
		assertEquals(FormVisibility.DISABLED, multipleMode(TRISTATE, false));
	}

	public void testModeForPrimitiveWithoutSupportWhileChecked() {
		// Stays editable, so a value entered for another type can be cleared.
		assertEquals(FormVisibility.DEFAULT, multipleMode(TRISTATE, true));
	}

	public void testModeForPrimitiveWithSupport() {
		assertEquals(FormVisibility.DEFAULT, multipleMode(BINARY, false));
	}

	public void testModeForClass() {
		assertEquals(FormVisibility.DEFAULT, multipleMode(CLASS_TYPE, false));
	}

	public void testModeWithoutType() {
		assertEquals(FormVisibility.DEFAULT, multipleMode(null, false));
	}

	public void testCreateRefusesUnsupportedMultiple() {
		inTestModule(owner -> {
			try {
				createAttribute(owner, newAttribute(TRISTATE, true));
				fail("Multi-valued attribute of a type without multiple support must be refused.");
			} catch (TopLogicException ex) {
				assertEquals(I18NConstants.ERROR_MULTIPLE_NOT_SUPPORTED__TYPE, ex.getErrorKey().plain());
			}
			assertNull(owner.getPart("a"));
		});
	}

	public void testCreateSupportedMultiple() {
		inTestModule(owner -> {
			TLStructuredTypePart binaries = createAttribute(owner, newAttribute(BINARY, true));
			assertTrue(binaries.isMultiple());

			TLObject single = newAttribute(TRISTATE, false);
			single.tUpdateByName(NAME, "b");
			assertFalse(createAttribute(owner, single).isMultiple());
		});
	}

	/**
	 * The mode of the multiple field of a new attribute input with the given value type and
	 * multiple setting.
	 */
	private static FormVisibility multipleMode(String type, boolean multiple) {
		FormVisibility[] result = new FormVisibility[1];
		ThreadContextManager.inSystemInteraction(TestNewAttributeMultiple.class, () -> {
			TLObject newAttribute = newAttribute(type, multiple);
			TLStructuredTypePart part = newAttribute.tType().getPartOrFail(MULTIPLE);
			ModeSelector selector = DynamicVisibility.modeSelector(part);
			assertNotNull(selector);
			result[0] = selector.getMode(newAttribute, part, true);
		});
		return result[0];
	}

	private static TLObject newAttribute(String type, boolean multiple) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject((TLClass) TLModelUtil.findType(NEW_ATTRIBUTE));
		result.tUpdateByName(NAME, "a");
		result.tUpdateByName(TYPE, type == null ? null : TLModelUtil.findType(type));
		result.tUpdateByName(MULTIPLE, Boolean.valueOf(multiple));
		return result;
	}

	private static TLStructuredTypePart createAttribute(TLClass owner, TLObject input) {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ModelEditAction.Config.class);
		ModelEditAction.Config config = TypedConfiguration.newConfigItem(ModelEditAction.Config.class);
		config.update(descriptor.getProperty(ModelEditAction.Config.MODE), Mode.CREATE_ATTRIBUTE);
		config.update(descriptor.getProperty(ModelEditAction.Config.CONTAINER), CONTAINER);
		ModelEditAction action = new ModelEditAction(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);

		ViewContext context = new DefaultViewContext(null);
		DefaultViewChannel container = new DefaultViewChannel(CONTAINER);
		context.registerChannel(CONTAINER, container);
		container.set(owner);
		return (TLStructuredTypePart) action.execute(context, input);
	}

	private interface OwnerTest {
		void run(TLClass owner);
	}

	/**
	 * Runs the given test with a class of a fresh module in a transaction that is rolled back
	 * afterwards.
	 */
	private static void inTestModule(OwnerTest test) {
		ThreadContextManager.inSystemInteraction(TestNewAttributeMultiple.class, () -> {
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				TLModule module = TLModelUtil.addModule(ModelService.getApplicationModel(), TEST_MODULE);
				test.run(TLModelUtil.addClass(module, TEST_CLASS));
				tx.rollback();
			}
		});
	}

	/**
	 * Test suite starting the model with the developer tools types.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestNewAttributeMultiple.class,
			ServiceTestSetup.createStarterFactoryForModules(
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE, AttributeSettings.Module.INSTANCE,
				ModelService.Module.INSTANCE));
	}
}
