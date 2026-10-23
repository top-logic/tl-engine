/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.nio.charset.StandardCharsets;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.element.meta.form.constraint.I18NConstants;
import com.top_logic.layout.react.control.form.ReactFileChipsControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Tests the field of a mandatory multi-valued binary attribute in a {@link FormControl}: its files
 * are edited as file chips, the field is marked required, and a save refused
 * for the missing files shows the message of the mandatory check under the chips until a file is
 * added.
 */
@SuppressWarnings("javadoc")
public class TestMandatoryFileChips extends AbstractTicketFormTest {

	/** Name of the mandatory multi-valued binary attribute. */
	private static final String ATTACHMENTS = "attachments";

	private AttributeFieldControl _attachments;

	private ReactFormFieldChromeControl _chrome;

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		try {
			TLPrimitive binary = TLModelUtil.addDatatype(module, module, "Binary", Kind.BINARY,
				directMapping(BinaryDataSource.class));
			TLStructuredTypePart part = TLModelUtil.addProperty(_ticketType, ATTACHMENTS, binary);
			part.setMandatory(true);
			part.setMultiple(true);
		} catch (Exception ex) {
			throw new AssertionError(ex);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_attachments = field(ATTACHMENTS);
		_chrome = _attachments.getChromeControl();
	}

	@Override
	protected void tearDown() throws Exception {
		_attachments = null;
		_chrome = null;
		super.tearDown();
	}

	/** The files of the attribute are edited as file chips. */
	public void testFilesAreEditedAsChips() {
		assertEquals(ReactFileChipsControl.class, _attachments.getInnerControl().getClass());
	}

	/**
	 * A save without files is refused, and the chips show why; a file added clears the message.
	 */
	public void testRefusedSaveShowsTheMissingFiles() {
		_form.enterEditMode();
		assertTrue("The field is marked required while it is edited.", required());
		assertNull("A field the user did not touch shows no error.", error());

		try {
			_form.validateOrThrow();
			fail("A save without the mandatory files must be refused.");
		} catch (TopLogicException ex) {
			// Expected.
		}

		assertEquals("The refused save shows the missing files at the chips.",
			Resources.getInstance().getString(I18NConstants.ERROR_MANDATORY_FIELD_EMPTY), error());

		model(_attachments).setValue(List.of(file("a.txt")));

		assertNull("A file added clears the message.", error());
		_form.validateOrThrow();
	}

	/** Whether the chrome of the field marks it required. */
	private boolean required() {
		return Boolean.TRUE.equals(_chrome.scriptingScalarState().get(FormFieldState.REQUIRED__PROP));
	}

	/** The error the chrome of the field shows, or {@code null}. */
	private Object error() {
		return _chrome.scriptingScalarState().get(FormFieldState.ERROR__PROP);
	}

	/** A file of the given name. */
	private static BinaryData file(String name) {
		return BinaryDataFactory.createBinaryData(name.getBytes(StandardCharsets.UTF_8), "text/plain", name);
	}

	/**
	 * Test suite requiring a knowledge base for the form's edit session, and the
	 * {@link FieldControlService} building the input of the field.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestMandatoryFileChips.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
