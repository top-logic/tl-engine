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
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.annotate.LabelPositionAnnotation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests the label of an {@link AttributeFieldControl} shown as placeholder of its input, see
 * {@link LabelPosition#PLACEHOLDER}.
 */
@SuppressWarnings("javadoc")
public class TestLabelPlaceholder extends AbstractTicketFormTest {

	/** Name of the attribute annotated to show its label as placeholder. */
	private static final String SUMMARY = "summary";

	/** Name of the attribute whose fallback is the {@link #TITLE}. */
	private static final String NICKNAME = "nickname";

	/** Name of the attribute storing the explicit value of the {@link #NICKNAME}. */
	private static final String CUSTOM_NICKNAME = "customNickname";

	/** Name of the property {@link LabelPositionAnnotation#getValue()}. */
	private static final String VALUE = "value";

	private static final String LABEL = "Ticket title";

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		TLModelUtil.addProperty(_ticketType, SUMMARY, text).setAnnotation(labelPosition(LabelPosition.PLACEHOLDER));

		TLModelUtil.addProperty(_ticketType, CUSTOM_NICKNAME, text);
		TLClassProperty nickname = TLModelUtil.addProperty(_ticketType, NICKNAME, text);
		nickname.setAnnotation(TestFallbackPlaceholder.fallbackStorage(CUSTOM_NICKNAME, TITLE));
		// A part of a transient model does not initialize its storage.
		((StorageImplementation) nickname.getStorageImplementation()).init(nickname);
	}

	/**
	 * A field with the label position {@link LabelPosition#PLACEHOLDER} hides its label beside the
	 * input and shows it as placeholder in the empty input, while the label still names the input.
	 */
	public void testViewConfiguredPosition() {
		AttributeFieldControl field = field(TITLE, LabelPosition.PLACEHOLDER);
		_form.enterEditMode();

		assertEquals("The label is not shown beside the input.", hidden(), labelPosition(field));
		assertEquals("The label still names the input.", LABEL, label(field));
		assertEquals("The label is shown in the empty input.", LABEL, placeholder(field));
	}

	/**
	 * The {@link LabelPositionAnnotation} of the attribute shows the label as placeholder, as the
	 * view configuration does.
	 */
	public void testAnnotatedPosition() {
		AttributeFieldControl field = field(SUMMARY);
		_form.enterEditMode();

		assertEquals(hidden(), labelPosition(field));
		assertNotNull(label(field));
		assertEquals("The label of the attribute is shown in the empty input.", label(field), placeholder(field));
	}

	/** A hidden label is not shown as placeholder. */
	public void testHiddenLabelIsNoPlaceholder() {
		AttributeFieldControl field = field(TITLE, LabelPosition.HIDE_LABEL);
		_form.enterEditMode();

		assertEquals(hidden(), labelPosition(field));
		assertNull("A hidden label is not shown in the input.", placeholder(field));
	}

	/**
	 * The inherited value of an attribute with a fallback takes the place of the label while there
	 * is one; without it, the empty input shows the label.
	 */
	public void testFallbackTakesPlaceOfLabel() {
		_ticket.tUpdateByName(TITLE, null);
		AttributeFieldControl field = field(NICKNAME, LabelPosition.PLACEHOLDER);
		_form.enterEditMode();
		assertEquals("Without inherited value, the empty input shows the label.", LABEL, placeholder(field));

		model(_title).setValue("Login fails");
		assertEquals("The inherited value takes the place of the label.", "Login fails", placeholder(field));

		model(_title).setValue(null);
		assertEquals("Without inherited value, the label is shown again.", LABEL, placeholder(field));
	}

	private AttributeFieldControl field(String attribute, LabelPosition position) {
		AttributeFieldControl result =
			new AttributeFieldControl(_context, _form, _form, attribute, ResKey.text(LABEL), false, position);
		result.createChromeControl();
		return result;
	}

	private static String hidden() {
		return com.top_logic.layout.react.control.layout.LabelPosition.HIDDEN.getExternalName();
	}

	private static Object labelPosition(AttributeFieldControl field) {
		return field.getChromeControl().scriptingScalarState().get(FormFieldState.LABEL_POSITION__PROP);
	}

	private static Object label(AttributeFieldControl field) {
		return field.getChromeControl().scriptingScalarState().get(FormFieldState.LABEL__PROP);
	}

	private static Object placeholder(AttributeFieldControl field) {
		return field.getInnerControl().scriptingScalarState().get(FieldState.PLACEHOLDER__PROP);
	}

	private static LabelPositionAnnotation labelPosition(LabelPosition position) {
		LabelPositionAnnotation annotation = TypedConfiguration.newConfigItem(LabelPositionAnnotation.class);
		annotation.update(annotation.descriptor().getProperty(VALUE), position);
		return annotation;
	}

	/**
	 * Test suite requiring a knowledge base for the form's edit session, and the
	 * {@link FieldControlService} building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestLabelPlaceholder.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
