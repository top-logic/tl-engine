/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.AttributeFieldModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the field of a derived attribute in a {@link FormControl} follows the edits of the
 * fields it is computed from, see {@link AttributeFieldModel}.
 *
 * <p>
 * The ticket has a stored {@link #NOTE}, a {@link #SUMMARY} joining {@link #TITLE} and
 * {@link #STATUS}, and a {@link #HEADLINE} joining {@link #SUMMARY} and {@link #NOTE}. The
 * {@link #HEADLINE} reads the {@link #TITLE} only through the {@link #SUMMARY}, so it follows an
 * edit of the {@link #TITLE} only through the change announced by the {@link #SUMMARY} field.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestDerivedAttributeField extends AbstractTicketFormTest {

	/** Name of a stored attribute that only the {@link #HEADLINE} reads. */
	private static final String NOTE = "note";

	/** Name of the derived attribute joining {@link #TITLE} and {@link #STATUS}. */
	private static final String SUMMARY = "summary";

	/** Name of the derived attribute joining {@link #SUMMARY} and {@link #NOTE}. */
	private static final String HEADLINE = "headline";

	private AttributeFieldControl _note;

	private AttributeFieldControl _summary;

	private AttributeFieldControl _headline;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertTrue(part(SUMMARY).isDerived());
		assertTrue(part(HEADLINE).isDerived());

		_note = field(NOTE);
		_summary = field(SUMMARY);
		_headline = field(HEADLINE);
	}

	@Override
	protected void tearDown() throws Exception {
		_note = null;
		_summary = null;
		_headline = null;
		super.tearDown();
	}

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		TLModelUtil.addProperty(_ticketType, NOTE, text);
		TLModelUtil.addProperty(_ticketType, SUMMARY, text).setAnnotation(JoinedValues.annotation(TITLE, STATUS));
		TLModelUtil.addProperty(_ticketType, HEADLINE, text).setAnnotation(JoinedValues.annotation(SUMMARY, NOTE));
	}

	@Override
	protected IdentifiedObject newTicket(String title, String status) {
		IdentifiedObject result = super.newTicket(title, status);
		result.tUpdateByName(NOTE, "urgent");
		return result;
	}

	/**
	 * Editing a field a derived attribute is computed from updates the derived field, which holds
	 * no change of its own, and the derived field depending on it.
	 */
	public void testEditUpdatesDerivedFields() {
		_form.enterEditMode();
		assertFalse("A derived attribute is not edited.", model(_summary).isEditable());
		assertEquals("Login fails open", shown(_summary));

		model(_title).setValue("Logout fails");

		assertEquals("The derived field shows the value computed over the edit.",
			"Logout fails open", shown(_summary));
		assertFalse("A derived field holds no change to save.", _summary.isDirty());
		assertEquals("The change of the derived field reaches the derived field depending on it.",
			"Logout fails open urgent", shown(_headline));
		assertFalse(_headline.isDirty());
		assertTrue(_title.isDirty());
		assertEquals("The edit is not stored before the save.",
			"Login fails open", _ticket.tValueByName(SUMMARY));

		model(_status).setValue("closed");

		assertEquals("Logout fails closed", shown(_summary));
		assertEquals("Logout fails closed urgent", shown(_headline));
	}

	/**
	 * Editing a field the derived attribute does not read leaves the derived field untouched.
	 */
	public void testEditOfUnreadFieldLeavesDerivedFieldAlone() {
		_form.enterEditMode();
		List<Object> summaryChanges = recordChanges(_summary);

		model(_note).setValue("later");

		assertEquals("Login fails open", shown(_summary));
		assertEquals("A field the derived attribute does not read does not change it.",
			List.of(), summaryChanges);
		assertEquals("The derived attribute reading the field follows it.",
			"Login fails open later", shown(_headline));
	}

	/**
	 * After a cancel, the derived field shows the stored value again; in view mode it follows a
	 * stored change.
	 */
	public void testCancelShowsStoredValue() {
		_form.enterEditMode();
		model(_title).setValue("Logout fails");
		assertEquals("Logout fails open", shown(_summary));

		_form.executeCancel();

		assertFalse(_form.isEditMode());
		assertEquals("Login fails open", shown(_summary));
		assertEquals("Login fails open urgent", shown(_headline));

		storeChange("Logout fails", "closed");

		assertEquals("Logout fails closed", shown(_summary));
		assertEquals("Logout fails closed urgent", shown(_headline));
	}

	/**
	 * After a save, the derived field shows the value computed from the stored values.
	 */
	public void testSaveShowsStoredValue() {
		_form.enterEditMode();
		model(_title).setValue("Logout fails");

		_form.executeSave();

		assertFalse(_form.isEditMode());
		assertEquals("Logout fails open", _ticket.tValueByName(SUMMARY));
		assertEquals("Logout fails open", shown(_summary));
		assertEquals("Logout fails open urgent", shown(_headline));
	}

	/**
	 * An overlay computes a derived attribute over its own edits.
	 */
	public void testOverlayComputesDerivedValueOverEdits() {
		TLObjectOverlay overlay = new TLObjectOverlay(_ticket);
		assertEquals("Without edits, the overlay delivers the value of the object.",
			"Login fails open", overlay.tValue(part(SUMMARY)));

		overlay.tUpdate(part(TITLE), "Logout fails");

		assertEquals("Logout fails open", overlay.tValue(part(SUMMARY)));
		assertEquals("A derived attribute read by a derived attribute is computed over the edits as well.",
			"Logout fails open urgent", overlay.tValue(part(HEADLINE)));
		assertEquals("The object is not changed.", "Login fails open", _ticket.tValue(part(SUMMARY)));
		assertFalse("A computed value is no edit.", overlay.isChanged(part(SUMMARY)));
	}

	private TLStructuredTypePart part(String name) {
		return _ticketType.getPart(name);
	}

	/**
	 * The values the model of the given field changes to, from now on.
	 */
	private static List<Object> recordChanges(AttributeFieldControl field) {
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

	/**
	 * Test suite requiring a knowledge base for the save, and the {@link FieldControlService}
	 * building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestDerivedAttributeField.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
