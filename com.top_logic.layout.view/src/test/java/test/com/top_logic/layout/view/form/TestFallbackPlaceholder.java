/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeWithFallbackStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FallbackPlaceholder;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLValueListeners;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests the field of an attribute with an {@link AttributeWithFallbackStorage}, see
 * {@link FallbackPlaceholder}.
 *
 * <p>
 * The ticket has a {@link #BRANCH} whose explicit value is kept in {@link #CUSTOM_BRANCH} and
 * whose fallback is the {@link #DEFAULT_BRANCH}, a derived attribute computed from the
 * {@link #STATUS}. A change of the {@link #TITLE} resets the {@link #BRANCH}, a change of the
 * {@link #COMPONENT} sets it to the value of its fallback.
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

	/** Name of an attribute whose change sets the {@link #BRANCH} to {@link #OPEN}. */
	private static final String COMPONENT = "component";

	/** The fallback of the {@link #BRANCH} for the status of the ticket. */
	private static final String OPEN = "open";

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
		_ticketType.getPart(TITLE).setAnnotation(assignBranch(null));
		TLModelUtil.addProperty(_ticketType, COMPONENT, text).setAnnotation(assignBranch(OPEN));
	}

	private static TLValueListeners assignBranch(String value) {
		TestFormValueListeners.AssignValue.Config assign =
			TypedConfiguration.newConfigItem(TestFormValueListeners.AssignValue.Config.class);
		assign.setTarget(BRANCH);
		assign.setValue(value);
		TLValueListeners result = TypedConfiguration.newConfigItem(TLValueListeners.class);
		result.getListeners().add(assign);
		return result;
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
	 * A value listener of another attribute resetting the attribute with fallback changes the
	 * attribute: its field shows the reset immediately, with the fallback as placeholder, and the
	 * listeners of the attribute run.
	 */
	public void testListenerResetIsShown() {
		_ticket.tUpdateByName(BRANCH, "dev");
		_form.enterEditMode();
		assertEquals("dev", shown(_branch));
		TestFormValueListeners.CALLS.clear();

		model(_title).setValue("Logout fails");

		assertEquals(List.of("title:Login fails->Logout fails=>branch"), TestFormValueListeners.CALLS);
		assertTrue(_form.getOverlay().isChanged(part(BRANCH)));
		assertNull("The field shows the reset before the save.", shown(_branch));
		assertEquals("open", placeholder(_branch));
		assertEquals(CSS_FALLBACK, _branch.getChromeControl().getCssClass());
		assertTrue(_branch.isDirty());
		assertEquals("Nothing is stored before the save.", "dev", _ticket.tValueByName(CUSTOM_BRANCH));
		TestFormValueListeners.CALLS.clear();
	}

	/**
	 * A value listener resetting an explicit value equal to the fallback removes the explicit value:
	 * the field shows the reset, although the value in force of the attribute stays the same.
	 */
	public void testListenerResetOfExplicitValueEqualToFallback() {
		_ticket.tUpdateByName(BRANCH, OPEN);
		_form.enterEditMode();
		assertEquals(OPEN, shown(_branch));
		assertNotNull(resetLabel(_branch));

		model(_title).setValue("Logout fails");

		assertNull("The field shows the reset explicit value.", shown(_branch));
		assertEquals(OPEN, placeholder(_branch));
		assertEquals(CSS_FALLBACK, _branch.getChromeControl().getCssClass());
		assertNull("Without explicit value, there is nothing to reset.", resetLabel(_branch));
		TestFormValueListeners.CALLS.clear();
	}

	/**
	 * A value listener setting an explicit value equal to the fallback: the field shows it as its
	 * own value, although the value in force of the attribute stays the same.
	 */
	public void testListenerSetsExplicitValueEqualToFallback() {
		AttributeFieldControl component = field(COMPONENT);
		_form.enterEditMode();
		assertNull(shown(_branch));
		assertEquals(OPEN, placeholder(_branch));

		model(component).setValue("Login");

		assertEquals("The field shows the explicit value set.", OPEN, shown(_branch));
		assertEquals(CSS_EXPLICIT, _branch.getChromeControl().getCssClass());
		assertNotNull("The explicit value can be reset.", resetLabel(_branch));
		TestFormValueListeners.CALLS.clear();
	}

	/**
	 * Clearing a field whose explicit value equals the fallback removes the explicit value without
	 * the field taking another value afterwards.
	 */
	public void testClearExplicitValueEqualToFallback() {
		_ticket.tUpdateByName(BRANCH, OPEN);
		_form.enterEditMode();
		assertEquals(OPEN, shown(_branch));
		List<Object> changes = recordValueChanges(_branch);

		model(_branch).setValue(null);

		assertNull(shown(_branch));
		assertEquals(OPEN, placeholder(_branch));
		assertEquals(CSS_FALLBACK, _branch.getChromeControl().getCssClass());
		assertNull(resetLabel(_branch));
		assertEquals("The cleared value is the only change of the field.", Collections.singletonList(null), changes);
	}

	/**
	 * Entering a value equal to the fallback sets it as explicit value without the field taking
	 * another value afterwards.
	 */
	public void testEnterExplicitValueEqualToFallback() {
		_form.enterEditMode();
		assertNull(shown(_branch));
		List<Object> changes = recordValueChanges(_branch);

		model(_branch).setValue(OPEN);

		assertEquals(OPEN, shown(_branch));
		assertEquals(CSS_EXPLICIT, _branch.getChromeControl().getCssClass());
		assertNotNull(resetLabel(_branch));
		assertEquals("The entered value is the only change of the field.", List.of(OPEN), changes);
	}

	/**
	 * A field that is not editable shows the value of the attribute also while the form is edited,
	 * and follows a changed fallback.
	 */
	public void testReadOnlyFieldShowsFallbackAsValue() {
		AttributeFieldControl readOnly = new AttributeFieldControl(_context, _form, _form, BRANCH, null, true, null);
		readOnly.createChromeControl();
		_form.enterEditMode();

		assertEquals("open", shown(readOnly));
		assertNull(placeholder(readOnly));
		assertEquals(CSS_FALLBACK, readOnly.getChromeControl().getCssClass());

		model(_status).setValue("closed");
		assertEquals("The displayed fallback follows the edit.", "closed", shown(readOnly));
		assertFalse(readOnly.isDirty());
	}

	/**
	 * A field that became not editable while the form is edited (e.g. dynamically read-only)
	 * follows a changed fallback.
	 *
	 * @see "Ticket #29329"
	 */
	public void testFallbackChangeInNonEditableField() {
		_form.enterEditMode();
		model(_branch).setEditable(false);
		assertEquals("open", shown(_branch));

		model(_status).setValue("closed");

		assertEquals("Non-editable field follows the changed fallback.", "closed", shown(_branch));
	}

	/**
	 * A change of the storage attribute in its own field is shown as explicit value.
	 *
	 * @see "Ticket #29329"
	 */
	public void testStorageAttributeChange() {
		AttributeFieldControl custom = field(CUSTOM_BRANCH);
		_form.enterEditMode();
		assertNull(shown(_branch));

		model(custom).setValue("dev");

		assertEquals("Field of the attribute with fallback shows the new explicit value.", "dev",
			shown(_branch));
	}

	/**
	 * A change of the storage attribute in its own field is shown as explicit value also by a field
	 * that is not editable.
	 *
	 * @see "Ticket #29329"
	 */
	public void testStorageAttributeChangeNonEditable() {
		AttributeFieldControl custom = field(CUSTOM_BRANCH);
		_form.enterEditMode();
		model(_branch).setEditable(false);
		assertEquals("open", shown(_branch));

		model(custom).setValue("dev");

		assertEquals("Non-editable field shows the new explicit value.", "dev", shown(_branch));
	}

	/**
	 * Fallback, explicit value, changed fallback and reset of the explicit value in an editable
	 * field: an explicit value hides a changed fallback, after the reset the new fallback shows.
	 *
	 * @see "Ticket #29329"
	 */
	public void testExplicitValueAndChangedFallback() {
		_form.enterEditMode();
		assertNull(shown(_branch));
		assertEquals("open", placeholder(_branch));

		model(_branch).setValue("dev");
		assertEquals("dev", shown(_branch));
		assertEquals(CSS_EXPLICIT, _branch.getChromeControl().getCssClass());

		model(_status).setValue("closed");
		assertEquals("The explicit value is kept.", "dev", shown(_branch));
		assertEquals("closed", placeholder(_branch));

		model(_branch).setValue(null);
		assertNull(shown(_branch));
		assertEquals("The new fallback shows.", "closed", placeholder(_branch));
		assertEquals(CSS_FALLBACK, _branch.getChromeControl().getCssClass());
	}

	/**
	 * Fallback, explicit value, changed fallback and reset of the explicit value in a field that is
	 * not editable, with the explicit value set in the field of the storage attribute.
	 *
	 * @see "Ticket #29329"
	 */
	public void testExplicitValueAndChangedFallbackNonEditable() {
		AttributeFieldControl custom = field(CUSTOM_BRANCH);
		_form.enterEditMode();
		model(_branch).setEditable(false);
		assertEquals("open", shown(_branch));
		assertEquals(CSS_FALLBACK, _branch.getChromeControl().getCssClass());

		model(custom).setValue("dev");
		assertEquals("dev", shown(_branch));
		assertEquals(CSS_EXPLICIT, _branch.getChromeControl().getCssClass());

		model(_status).setValue("closed");
		assertEquals("The explicit value is kept.", "dev", shown(_branch));

		model(custom).setValue(null);
		assertEquals("The new fallback shows.", "closed", shown(_branch));
		assertEquals(CSS_FALLBACK, _branch.getChromeControl().getCssClass());
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

	/**
	 * The label of the reset offered by the container of the field, {@code null} if it offers
	 * none.
	 */
	private static Object resetLabel(AttributeFieldControl field) {
		return field.getChromeControl().scriptingScalarState().get(FormFieldState.RESET_LABEL__PROP);
	}

	/**
	 * Records the new values the model of the given field announces.
	 */
	private static List<Object> recordValueChanges(AttributeFieldControl field) {
		List<Object> result = new ArrayList<>();
		model(field).addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				result.add(newValue);
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Not recorded.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Not recorded.
			}
		});
		return result;
	}

	static TLStorage fallbackStorage(String storageAttribute, String fallbackAttribute) {
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
