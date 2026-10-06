/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.util.Resources;

/**
 * Tests that an {@link AttributeFieldControl} follows the model access rights of the current user
 * on the displayed object, see
 * {@link ModelAccessPolicy#onAttribute(BoundCommandGroup, TLObject, TLStructuredTypePart)}.
 *
 * <p>
 * A value the user may not read is hidden and not sent to the client. A value the user may not
 * write is read-only, when the refusal does not depend on the object, and disabled with the
 * refusal as tooltip otherwise.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestAttributeFieldAccessRights extends AbstractModelAccessTest {

	/** Name of the {@link #TASK} attribute only {@link #ROLE_RESPONSIBLE} may write. */
	private static final String NOTE = "note";

	/** The {@link #BUDGET} of {@link #_project}. */
	private static final String BUDGET_VALUE = "budget-of-project";

	/** The {@link #BUDGET} of {@link #_own}. */
	private static final String OWN_BUDGET_VALUE = "budget-of-own";

	/** The {@link #CLASSIFIED} value of {@link #_project}. */
	private static final String CLASSIFIED_VALUE = "classified-of-project";

	/**
	 * A project the {@link #_roleless} user is responsible for; on {@link #_project} the same user
	 * is viewer only.
	 */
	private TLObject _own;

	private FormControl _form;

	private AttributeFieldControl _field;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_own = newObject(qualified(PROJECT), "own");
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(VIEWER, _roleless);
			_project.tUpdateByName(BUDGET, BUDGET_VALUE);
			_project.tUpdateByName(CLASSIFIED, CLASSIFIED_VALUE);
			_project.tUpdateByName(CODE, "P-1");
			_own.tUpdateByName(RESPONSIBLE, _roleless);
			_own.tUpdateByName(BUDGET, OWN_BUDGET_VALUE);
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_field = null;
		_form = null;
		_own = null;
		super.tearDown();
	}

	/**
	 * The user holds the role the attribute grants reading to: the value is shown.
	 */
	public void testReadGranted() {
		becomeUser(_responsible);
		createField(_project, BUDGET);

		assertTrue(chrome().isVisible());
		assertEquals(BUDGET_VALUE, inputState(FieldState.VALUE__PROP));
		assertTrue("The value reaches the client.", chrome().stateAsJSON().contains(BUDGET_VALUE));
	}

	/**
	 * Reading is refused on the displayed object: the field is hidden, and its value does not reach
	 * the client.
	 */
	public void testReadRefusedOnObject() {
		becomeUser(_roleless);
		createField(_project, BUDGET);

		assertHiddenWithout(BUDGET_VALUE);
	}

	/**
	 * Reading is granted to no role: the field is hidden, and its value does not reach the client.
	 */
	public void testReadGrantedToNoRole() {
		becomeUser(_responsible);
		createField(_project, CLASSIFIED);

		assertHiddenWithout(CLASSIFIED_VALUE);
	}

	/**
	 * A user bypassing the model security reads every value.
	 */
	public void testReadAsRoot() {
		becomeUser(_root);
		createField(_project, CLASSIFIED);

		assertTrue(chrome().isVisible());
		assertEquals(CLASSIFIED_VALUE, inputState(FieldState.VALUE__PROP));
	}

	/**
	 * A field follows the input of the form from an object whose value the user may not read to
	 * one whose value the user may read, and back.
	 */
	public void testReadAfterInputSwitch() {
		becomeUser(_roleless);
		ViewChannel input = createField(_project, BUDGET);
		assertHiddenWithout(BUDGET_VALUE);

		input.set(_own);
		assertTrue(chrome().isVisible());
		assertEquals(OWN_BUDGET_VALUE, inputState(FieldState.VALUE__PROP));
		assertTrue(chrome().stateAsJSON().contains(OWN_BUDGET_VALUE));

		input.set(_project);
		assertHiddenWithout(BUDGET_VALUE);
		assertFalse("The value of the former object is dropped.", chrome().stateAsJSON().contains(OWN_BUDGET_VALUE));

		input.set(_own);
		assertTrue(chrome().isVisible());
		assertEquals(OWN_BUDGET_VALUE, inputState(FieldState.VALUE__PROP));
	}

	/**
	 * Writing the attribute is allowed: the field is editable.
	 */
	public void testWriteAllowed() {
		becomeUser(_responsible);
		createField(_project, NAME);
		assertTrue(_form.enterEditMode());

		assertEditable();
	}

	/**
	 * Writing is granted to no role: the field is read-only while the form is edited.
	 */
	public void testWriteGrantedToNoRole() {
		becomeUser(_responsible);
		createField(_project, SECRET);
		assertTrue(_form.enterEditMode());

		assertReadOnly(part(PROJECT, SECRET));
	}

	/**
	 * Writing is refused on the edited object only (the user may write the project, but does not
	 * hold the role the attribute grants writing to on it): the field is disabled while the form is
	 * edited and gives the refusal as tooltip; outside edit mode it shows its value.
	 */
	public void testWriteRefusedOnObject() {
		becomeUser(_responsible);
		createField(_project, RATING);
		assertTrue(_form.enterEditMode());

		assertDisabled(part(PROJECT, RATING));

		_form.executeCancel();
		assertFalse(fieldModel().isEditable());
		assertFalse(fieldModel().isDisabled());
		assertTooltipWithoutReason(part(PROJECT, RATING));
	}

	/**
	 * A user bypassing the model security writes every value.
	 */
	public void testWriteAsRoot() {
		becomeUser(_root);
		createField(_project, SECRET);
		assertTrue(_form.enterEditMode());

		assertEditable();
	}

	/**
	 * A mode of the model stricter than the access rights wins: an attribute that is read-only by
	 * its display annotation stays read-only, also where the rights would disable it.
	 */
	public void testModelModeStricter() {
		becomeUser(_responsible);
		createField(_project, CODE);
		assertTrue(_form.enterEditMode());
		assertReadOnly(part(PROJECT, CODE));
	}

	/**
	 * A mode of the model stricter than the disabling refusal on the object wins.
	 */
	public void testModelModeStricterThanDisabled() {
		becomeUser(_responsible);
		createField(_project, CODE);
		assertTrue(_form.enterEditMode());
		assertReadOnly(part(PROJECT, CODE));
	}

	/**
	 * A draft is decided in its creation context: a user holding the role the attribute grants
	 * writing to in the container may set the value.
	 */
	public void testDraftWriteGrantedInContext() {
		becomeUser(_responsible);
		createField(draftIn(_project), NOTE);
		assertTrue(_form.enterEditMode());

		assertEditable();
	}

	/**
	 * A draft is decided in its creation context: a user not holding the role the attribute grants
	 * writing to in the container gets the field disabled, with the refusal as tooltip.
	 */
	public void testDraftWriteRefusedInContext() {
		becomeUser(_roleless);
		createField(draftIn(_project), NOTE);
		assertTrue(_form.enterEditMode());

		assertDisabled(part(TASK, NOTE));
	}

	/**
	 * An attribute of a draft without a grant of its own is not restricted, neither for a user
	 * without roles nor without a user at all (e.g. the input object of a login dialog).
	 */
	public void testDraftUngrantedAttribute() {
		becomeUser(_roleless);
		createField(draftIn(_project), NAME);
		assertTrue(_form.enterEditMode());
		assertEditable();

		becomeUser(null);
		try {
			createField(draftIn(null), NAME);
			assertTrue(chrome().isVisible());
			assertTrue(_form.enterEditMode());
			assertEditable();
		} finally {
			// Cleaning up the fixture needs a user.
			becomeUser(_root);
		}
	}

	/**
	 * An attribute of a draft granted to no role is read-only, in every context.
	 */
	public void testDraftWriteGrantedToNoRole() {
		becomeUser(_responsible);
		createField(draftIn(_project), SECRET);
		assertTrue(_form.enterEditMode());

		assertReadOnly(part(TASK, SECRET));
	}

	/**
	 * A user bypassing the model security sets every value of a draft.
	 */
	public void testDraftWriteAsRoot() {
		becomeUser(_root);
		createField(draftIn(_project), SECRET);
		assertTrue(_form.enterEditMode());

		assertEditable();
	}

	private ViewChannel createField(TLObject object, String attribute) {
		_form = new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")), object, "no model", NoTokenHandling.INSTANCE);
		ViewChannel input = new DefaultViewChannel("input");
		input.set(object);
		_form.setInputChannel(input);
		_field = new AttributeFieldControl(_form.getReactContext(), _form, _form, attribute, null, false, null);
		_field.createChromeControl();
		_form.attach();
		return input;
	}

	private static TLObject draftIn(TLObject container) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(type(TASK), container);
		result.tUpdateByName(NAME, "draft");
		return result;
	}

	private void assertHiddenWithout(String value) {
		assertFalse("The field is hidden.", chrome().isVisible());
		assertNull(inputState(FieldState.VALUE__PROP));
		assertFalse("The value does not reach the client.", chrome().stateAsJSON().contains(value));
	}

	private void assertEditable() {
		assertTrue(fieldModel().isEditable());
		assertFalse(fieldModel().isDisabled());
		assertEquals(Boolean.TRUE, inputState(FieldState.EDITABLE__PROP));
	}

	private void assertReadOnly(TLStructuredTypePart attribute) {
		assertFalse(fieldModel().isEditable());
		assertFalse("A read-only field shows its value instead of an inactive input.", fieldModel().isDisabled());
		assertEquals(Boolean.FALSE, inputState(FieldState.EDITABLE__PROP));
		assertEquals(Boolean.FALSE, inputState(FieldState.DISABLED__PROP));
		assertTooltipWithoutReason(attribute);
	}

	private void assertDisabled(TLStructuredTypePart attribute) {
		assertFalse(fieldModel().isEditable());
		assertTrue(fieldModel().isDisabled());
		assertEquals(Boolean.FALSE, inputState(FieldState.EDITABLE__PROP));
		assertEquals(Boolean.TRUE, inputState(FieldState.DISABLED__PROP));
		Object tooltip = chrome().scriptingScalarState().get(ReactFormFieldChromeControl.TOOLTIP_TEXT);
		assertNotNull("The refusal is given as tooltip.", tooltip);
		assertTrue("The tooltip gives the refusal: " + tooltip, tooltip.toString().contains(writeDenied(attribute)));
	}

	private void assertTooltipWithoutReason(TLStructuredTypePart attribute) {
		Object tooltip = chrome().scriptingScalarState().get(ReactFormFieldChromeControl.TOOLTIP_TEXT);
		assertTrue("No refusal in the tooltip: " + tooltip,
			tooltip == null || !tooltip.toString().contains(writeDenied(attribute)));
	}

	private static String writeDenied(TLStructuredTypePart attribute) {
		ResKey reason = com.top_logic.layout.view.security.I18NConstants.ERROR_ATTRIBUTE_WRITE_DENIED__ATTRIBUTE
			.fill(TLModelI18N.getI18NKey(attribute));
		return Resources.getInstance().getString(reason);
	}

	private ReactFormFieldChromeControl chrome() {
		return _field.getChromeControl();
	}

	private FieldModel fieldModel() {
		return ((ReactFormFieldControl) _field.getInnerControl()).getFieldModel();
	}

	private Object inputState(String key) {
		return _field.getInnerControl().scriptingScalarState().get(key);
	}

	/**
	 * The test suite, additionally starting the {@link FieldControlService} building the inputs of
	 * the fields.
	 */
	public static Test suite() {
		return suiteWith(TestAttributeFieldAccessRights.class, TypeIndex.Module.INSTANCE,
			FieldControlService.Module.INSTANCE);
	}
}
