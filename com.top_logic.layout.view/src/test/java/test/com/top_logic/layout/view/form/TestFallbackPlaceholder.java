/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeWithFallbackStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FallbackPlaceholder;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests the field of an attribute with an {@link AttributeWithFallbackStorage}, see
 * {@link FallbackPlaceholder}.
 *
 * <p>
 * The ticket has a {@link #BRANCH} whose explicit value is kept in {@link #CUSTOM_BRANCH} and
 * whose fallback is the {@link #DEFAULT_BRANCH}, a derived attribute computed from the
 * {@link #STATUS}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFallbackPlaceholder extends AbstractTicketFormTest {

	/** Name of the attribute with the fallback. */
	private static final String BRANCH = "branch";

	/** Name of the attribute storing the explicit value of the {@link #BRANCH}. */
	private static final String CUSTOM_BRANCH = "customBranch";

	/** Name of the derived attribute providing the fallback of the {@link #BRANCH}. */
	private static final String DEFAULT_BRANCH = "defaultBranch";

	private static final String CSS_EXPLICIT = "tl-info";

	private static final String CSS_FALLBACK = "tl-debug";

	private AttributeFieldControl _branch;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_branch = field(BRANCH);
	}

	@Override
	protected void tearDown() throws Exception {
		_branch = null;
		super.tearDown();
	}

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		TLModelUtil.addProperty(_ticketType, CUSTOM_BRANCH, text);
		TLModelUtil.addProperty(_ticketType, DEFAULT_BRANCH, text).setAnnotation(JoinedValues.annotation(STATUS));
		TLClassProperty branch = TLModelUtil.addProperty(_ticketType, BRANCH, text);
		branch.setAnnotation(fallbackStorage(CUSTOM_BRANCH, DEFAULT_BRANCH));
		// A part of a transient model does not initialize its storage, which the fallback storage
		// needs to resolve the attributes it reads.
		((StorageImplementation) branch.getStorageImplementation()).init(branch);
	}

	/**
	 * In view mode, the field shows the value of the attribute, which is the fallback while no
	 * explicit value is set.
	 */
	public void testViewModeShowsFallbackAsValue() {
		assertEquals("open", shown(_branch));
		assertNull(placeholder(_branch));
		assertEquals(CSS_FALLBACK, _branch.getChromeControl().getCssClass());
	}

	/**
	 * While editing, an empty field shows the fallback as placeholder, which follows the edit of
	 * the attribute the fallback is derived from.
	 */
	public void testEditShowsFallbackAsPlaceholder() {
		_form.enterEditMode();

		assertNull("The field holds the explicit value only.", shown(_branch));
		assertEquals("open", placeholder(_branch));
		assertEquals(CSS_FALLBACK, _branch.getChromeControl().getCssClass());

		model(_status).setValue("closed");

		assertEquals("The placeholder follows the edit of the attribute the fallback is derived from.",
			"closed", placeholder(_branch));
		assertNull(shown(_branch));
		assertFalse(_branch.isDirty());
	}

	/**
	 * An entered value is saved as explicit value.
	 */
	public void testSaveExplicitValue() {
		_form.enterEditMode();

		model(_branch).setValue("dev");

		assertEquals("dev", shown(_branch));
		assertEquals(CSS_EXPLICIT, _branch.getChromeControl().getCssClass());
		assertTrue(_branch.isDirty());

		_form.executeSave();

		assertEquals("dev", _ticket.tValueByName(CUSTOM_BRANCH));
		assertEquals("dev", _ticket.tValueByName(BRANCH));
		assertEquals("dev", shown(_branch));
		assertEquals(CSS_EXPLICIT, _branch.getChromeControl().getCssClass());
	}

	/**
	 * Clearing the field removes the explicit value, the fallback applies again.
	 */
	public void testSaveClearedValue() {
		_ticket.tUpdateByName(BRANCH, "dev");
		_form.enterEditMode();
		assertEquals("dev", shown(_branch));
		assertEquals(CSS_EXPLICIT, _branch.getChromeControl().getCssClass());

		model(_branch).setValue(null);

		assertNull(shown(_branch));
		assertEquals("open", placeholder(_branch));
		assertEquals(CSS_FALLBACK, _branch.getChromeControl().getCssClass());

		_form.executeSave();

		assertNull("No explicit value is stored.", _ticket.tValueByName(CUSTOM_BRANCH));
		assertEquals("open", _ticket.tValueByName(BRANCH));
		assertEquals("open", shown(_branch));
	}

	/**
	 * A field that is not editable shows the value of the attribute also while the form is edited.
	 */
	public void testReadOnlyFieldShowsFallbackAsValue() {
		AttributeFieldControl readOnly = new AttributeFieldControl(_context, _form, _form, BRANCH, null, true, null);
		readOnly.createChromeControl();
		_form.enterEditMode();

		assertEquals("open", shown(readOnly));
		assertNull(placeholder(readOnly));
		assertEquals(CSS_FALLBACK, readOnly.getChromeControl().getCssClass());

		model(_status).setValue("closed");
		assertEquals("open", shown(readOnly));
	}

	/**
	 * An overlay buffers the explicit value of an attribute with a fallback in the storage
	 * attribute and computes the fallback over its edits.
	 */
	public void testOverlay() {
		TLObjectOverlay overlay = new TLObjectOverlay(_ticket);
		assertEquals("open", overlay.tValue(part(BRANCH)));

		overlay.tUpdate(part(STATUS), "closed");
		assertEquals("The fallback is computed over the edits.", "closed", overlay.tValue(part(BRANCH)));

		overlay.tUpdate(part(BRANCH), "dev");
		assertEquals("dev", overlay.tValue(part(BRANCH)));
		assertEquals("The explicit value is buffered in the storage attribute.",
			"dev", overlay.tValue(part(CUSTOM_BRANCH)));
		assertNull("The object is not changed.", _ticket.tValueByName(CUSTOM_BRANCH));

		overlay.tUpdate(part(BRANCH), null);
		assertEquals("Without explicit value, the fallback applies.", "closed", overlay.tValue(part(BRANCH)));

		overlay.tUpdate(part(BRANCH), "dev");
		overlay.apply();
		assertEquals("dev", _ticket.tValueByName(CUSTOM_BRANCH));
		assertEquals("dev", _ticket.tValueByName(BRANCH));
		assertEquals("closed", _ticket.tValueByName(STATUS));
	}

	private TLStructuredTypePart part(String name) {
		return _ticketType.getPart(name);
	}

	private static Object placeholder(AttributeFieldControl field) {
		return field.getInnerControl().scriptingScalarState().get(FieldState.PLACEHOLDER__PROP);
	}

	private static TLStorage fallbackStorage(String storageAttribute, String fallbackAttribute) {
		AttributeWithFallbackStorage.Config<?> storageConfig =
			TypedConfiguration.newConfigItem(AttributeWithFallbackStorage.Config.class);
		storageConfig.update(storageConfig.descriptor().getProperty(AttributeWithFallbackStorage.Config.STORAGE_ATTRIBUTE), storageAttribute);
		storageConfig.update(storageConfig.descriptor().getProperty(AttributeWithFallbackStorage.Config.FALLBACK_ATTRIBUTE), fallbackAttribute);

		TLStorage annotation = TypedConfiguration.newConfigItem(TLStorage.class);
		annotation.setImplementation(storageConfig);
		return annotation;
	}

	/**
	 * Test suite requiring a knowledge base for the save, and the {@link FieldControlService}
	 * building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFallbackPlaceholder.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
