/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.SelectControlProvider;
import com.top_logic.layout.view.form.TextInputControlProvider;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the control a form field names decides the model of the field, so that the model fits
 * the control: a field naming a select is a select field even where the attribute offers no
 * options, a field naming a plain input is a plain field even where the attribute is a reference.
 */
public class TestFieldInputControl extends AbstractTicketFormTest {

	/** Name of a reference of the ticket to another ticket. */
	private static final String RELATED = "related";

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		TLModelUtil.addReference(_ticketType, RELATED, _ticketType, null);
	}

	/**
	 * A text field naming a select is edited by a select, whose options are empty, since the text
	 * offers none.
	 */
	public void testSelectOnText() {
		AttributeFieldControl field = field(TITLE, select());

		assertTrue(model(field) instanceof SelectFieldModel);
		assertEquals(List.of(), ((SelectFieldModel) model(field)).getOptions());
		assertTrue(field.getInnerControl() instanceof ReactDropdownSelectControl);
	}

	/**
	 * A text field naming no control is a plain text field.
	 */
	public void testTextWithoutControl() {
		AttributeFieldControl field = field(TITLE, null);

		assertFalse(model(field) instanceof SelectFieldModel);
		assertTrue(field.getInnerControl() instanceof ReactTextInputControl);
	}

	/**
	 * A reference field naming a plain input is a plain field, not the select its structure asks
	 * for.
	 */
	public void testTextInputOnReference() {
		AttributeFieldControl field = field(RELATED, textInput());

		assertFalse(model(field) instanceof SelectFieldModel);
		assertTrue(field.getInnerControl() instanceof ReactTextInputControl);
	}

	/**
	 * Creates the field of the given attribute in {@link #_form}, edited by the given control.
	 */
	private AttributeFieldControl field(String attribute,
			PolymorphicConfiguration<? extends ReactFieldControlProvider> inputControl) {
		AttributeFieldControl result =
			new AttributeFieldControl(_context, _form, _form, attribute, null, false, null, null, inputControl);
		result.createChromeControl();
		return result;
	}

	private static SelectControlProvider.Config select() {
		return TypedConfiguration.newConfigItem(SelectControlProvider.Config.class);
	}

	private static TextInputControlProvider.Config textInput() {
		return TypedConfiguration.newConfigItem(TextInputControlProvider.Config.class);
	}

	/**
	 * Test suite requiring a knowledge base for the form and the {@link FieldControlService}
	 * building the models and inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFieldInputControl.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE));
	}

}
