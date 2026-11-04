/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.form.TestAttributeFieldDisabledMode.FixedMode;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.annotate.TLDynamicVisibility;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the required marker of an {@link AttributeFieldControl} follows the mode of the field,
 * computed by the {@link TLDynamicVisibility} of its attribute, also after the validation of the
 * field changed.
 */
@SuppressWarnings("javadoc")
public class TestAttributeFieldMandatoryMarker extends AbstractTicketFormTest {

	/** Name of the attribute that is mandatory by its dynamic visibility only. */
	private static final String REVIEWER = "reviewer";

	/** Name of the attribute that is statically mandatory but read-only by its dynamic visibility. */
	private static final String OWNER = "owner";

	/** Name of the property {@link TLDynamicVisibility#getModeSelector()}. */
	private static final String MODE_SELECTOR = "mode-selector";

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		TLProperty reviewer = TLModelUtil.addProperty(_ticketType, REVIEWER, text);
		reviewer.setAnnotation(dynamicVisibility(FormVisibility.MANDATORY));

		TLProperty owner = TLModelUtil.addProperty(_ticketType, OWNER, text);
		owner.setMandatory(true);
		owner.setAnnotation(dynamicVisibility(FormVisibility.READ_ONLY));
	}

	/**
	 * A field mandatory by its dynamic visibility keeps its required marker when an error appears
	 * and when it disappears again.
	 */
	public void testDynamicMandatoryKeptOnValidationChange() {
		AttributeFieldControl field = field(REVIEWER);
		_form.enterEditMode();
		assertTrue("The mode makes the field mandatory.", required(field));

		model(field).setError(ResKey.text("Refused."));
		assertTrue("An error keeps the required marker.", required(field));

		model(field).setError(null);
		assertTrue("A cleared error keeps the required marker.", required(field));
	}

	/**
	 * A statically mandatory field that its dynamic visibility makes read-only gets no required
	 * marker from a validation change.
	 */
	public void testReadOnlyStaysWithoutMarkerOnValidationChange() {
		AttributeFieldControl field = field(OWNER);
		_form.enterEditMode();
		assertFalse("A read-only field is not marked required.", required(field));

		model(field).setError(ResKey.text("Refused."));
		assertFalse("An error does not mark a read-only field required.", required(field));

		model(field).setError(null);
		assertFalse("A cleared error does not mark a read-only field required.", required(field));
	}

	/** Whether the chrome of the given field marks it required. */
	private static boolean required(AttributeFieldControl field) {
		return Boolean.TRUE.equals(field.getChromeControl().scriptingScalarState().get(FormFieldState.REQUIRED__PROP));
	}

	private static TLDynamicVisibility dynamicVisibility(FormVisibility mode) {
		FixedMode.Config<?> selector = TypedConfiguration.newConfigItem(FixedMode.Config.class);
		selector.setMode(mode);
		TLDynamicVisibility annotation = TypedConfiguration.newConfigItem(TLDynamicVisibility.class);
		annotation.update(annotation.descriptor().getProperty(MODE_SELECTOR), selector);
		return annotation;
	}

	/**
	 * Test suite requiring a knowledge base for the form's edit session, and the
	 * {@link FieldControlService} building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestAttributeFieldMandatoryMarker.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
