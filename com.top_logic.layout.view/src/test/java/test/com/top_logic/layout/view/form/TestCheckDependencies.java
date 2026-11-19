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
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.ValidationResult;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.providers.ConstraintCheckByExpression;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests that the {@link FormValidationModel} of a {@link FormControl} checks an attribute again
 * when an attribute changes that its check reads through a derived attribute.
 *
 * <p>
 * The entry (<code>TestViewCheckDependencies.model.xml</code>) has a {@link #SUMMARY} derived from
 * its {@link #TITLE}, and a {@link #CODE} with a {@link ConstraintCheckByExpression} rejecting an
 * entry without {@link #SUMMARY}. The derived {@link #SUMMARY} is computed over the overlay of the
 * form by reading the edited object, see
 * {@link com.top_logic.model.form.TracingScope#computeOnOverlay(TLObject, TLObject, com.top_logic.model.TLStructuredTypePart)}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestCheckDependencies extends AbstractModelAccessTest {

	/** The test type. */
	private static final String ENTRY = "TestViewCheckDependencies:Entry";

	/** The stored attribute the {@link #SUMMARY} is derived from. */
	private static final String TITLE = "title";

	/** The derived attribute the check of the {@link #CODE} reads. */
	private static final String SUMMARY = "summary";

	/** The checked attribute. */
	private static final String CODE = "code";

	private ReactContext _reactContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		super.tearDown();
	}

	/**
	 * The tests are about the checks, not about the access rights.
	 */
	@Override
	protected boolean checksSecurity() {
		return false;
	}

	/**
	 * A check reading a derived attribute follows the attribute the derived attribute is computed
	 * from, without saving.
	 */
	public void testCheckFollowsDerivedAttribute() {
		TLObject entry = newEntry();
		FormControl form = new FormControl(_reactContext, entry, "no model", NoTokenHandling.INSTANCE);
		form.setModelScope(new RecordingScope());
		AttributeFieldControl title = field(form, TITLE);
		field(form, CODE);
		form.attach();
		assertTrue(form.enterEditMode());

		assertTrue(validation(form, CODE).isValid());

		model(title).setValue(null);

		assertNull(form.getOverlay().tValueByName(SUMMARY));
		assertFalse("The check sees that the cleared title leaves no summary.", validation(form, CODE).isValid());
		assertFalse(form.getValidationModel().isValid());

		model(title).setValue("Logout fails");

		assertEquals("Logout fails", form.getOverlay().tValueByName(SUMMARY));
		assertTrue("The check sees the summary computed from the entered title.", validation(form, CODE).isValid());
		assertTrue(form.getValidationModel().isValid());
		assertEquals("Nothing is stored.", "Login fails", entry.tValueByName(TITLE));
	}

	private TLObject newEntry() {
		TLClass type = (TLClass) TLModelUtil.findType(model(), ENTRY);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject result = DynamicModelService.getFactoryFor(type.getModule().getName()).createObject(type);
			result.tUpdateByName(TITLE, "Login fails");
			tx.commit();
			return result;
		}
	}

	private static ValidationResult validation(FormControl form, String attribute) {
		TLObject overlay = form.getOverlay();
		return form.getValidationModel().getValidation(overlay, overlay.tType().getPartOrFail(attribute));
	}

	private AttributeFieldControl field(FormControl form, String attribute) {
		AttributeFieldControl result =
			new AttributeFieldControl(_reactContext, form, form, attribute, null, false, null);
		result.createChromeControl();
		return result;
	}

	private static AbstractFieldModel model(AttributeFieldControl field) {
		return (AbstractFieldModel) ((ReactFormFieldControl) field.getInnerControl()).getFieldModel();
	}

	/**
	 * The test suite, starting the services building the fields and compiling the scripts of the
	 * model.
	 */
	public static Test suite() {
		return suiteWith(TestCheckDependencies.class, TypeIndex.Module.INSTANCE,
			ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, SearchBuilder.Module.INSTANCE);
	}

}
