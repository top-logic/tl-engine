/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeWithFallbackStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.layout.view.command.FormValid;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.form.ValidationResult;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.providers.ConstraintCheckByExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the checks of an attribute with an {@link AttributeWithFallbackStorage} follow its
 * fallback value in a {@link FormControl}: the {@link FormValidationModel} checks the attribute
 * again when an attribute the fallback is computed from changes.
 *
 * <p>
 * The ticket starts without {@link #TITLE}. The {@link #SUGGESTION} is derived from the
 * {@link #TITLE} and is the fallback of the mandatory {@link #CODE} and of the {@link #REF}, which
 * has a {@link ConstraintCheckByExpression} rejecting an empty value. Neither has an explicit value.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFallbackValidation extends AbstractTicketFormTest {

	/** Name of the derived attribute providing the fallback of {@link #CODE} and {@link #REF}. */
	private static final String SUGGESTION = "suggestion";

	/** Name of the mandatory attribute with fallback. */
	private static final String CODE = "code";

	/** Name of the attribute storing the explicit value of the {@link #CODE}. */
	private static final String CUSTOM_CODE = "customCode";

	/** Name of the attribute with fallback checked by an expression. */
	private static final String REF = "ref";

	/** Name of the attribute storing the explicit value of the {@link #REF}. */
	private static final String CUSTOM_REF = "customRef";

	/** Name of the property {@link ConstraintCheckByExpression.Config#getCheck()}. */
	private static final String CHECK_PROPERTY = "check";

	private AttributeFieldControl _code;

	private AttributeFieldControl _ref;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_code = field(CODE);
		_ref = field(REF);
	}

	@Override
	protected void tearDown() throws Exception {
		_code = null;
		_ref = null;
		super.tearDown();
	}

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		TLModelUtil.addProperty(_ticketType, SUGGESTION, text).setAnnotation(JoinedValues.annotation(TITLE));
		TLModelUtil.addProperty(_ticketType, CUSTOM_CODE, text);
		TLModelUtil.addProperty(_ticketType, CUSTOM_REF, text);

		TLClassProperty code = TLModelUtil.addProperty(_ticketType, CODE, text);
		code.setMandatory(true);
		withFallback(code, CUSTOM_CODE);

		TLClassProperty ref = TLModelUtil.addProperty(_ticketType, REF, text);
		ref.setAnnotation(rejectEmpty());
		withFallback(ref, CUSTOM_REF);
	}

	@Override
	protected IdentifiedObject newTicket(String title, String status) {
		return super.newTicket(null, status);
	}

	/**
	 * The mandatory check of an attribute with fallback follows the attribute its fallback is
	 * derived from, without saving.
	 */
	public void testMandatoryFollowsFallback() {
		checkFollowsFallback(CODE);
	}

	/**
	 * A {@link ConstraintCheckByExpression} of an attribute with fallback follows the attribute its
	 * fallback is derived from, without saving.
	 */
	public void testExpressionCheckFollowsFallback() {
		checkFollowsFallback(REF);
	}

	private void checkFollowsFallback(String attribute) {
		_form.enterEditMode();
		_form.revealAllValidation();

		assertFalse("Without fallback value, the attribute is empty.", validation(attribute).isValid());
		assertFalse(_form.getValidationModel().isValid());
		assertTrue("The command guarded by the form validity is disabled.", formInvalid());

		model(_title).setValue("Login fails");

		assertEquals("Login fails", _form.getOverlay().tValue(part(attribute)));
		assertTrue("The fallback value satisfies the check.", validation(attribute).isValid());
		assertTrue(_form.getValidationModel().isValid());
		assertFalse("The command guarded by the form validity is enabled.", formInvalid());
		assertNull("Nothing is stored.", _ticket.tValueByName(TITLE));

		model(_title).setValue(null);

		assertFalse("Without fallback value, the error is back.", validation(attribute).isValid());
		assertFalse(_form.getValidationModel().isValid());
		assertTrue(formInvalid());
	}

	/**
	 * Whether a command guarded by {@link FormValid} is disabled, which it is while the form shows
	 * errors.
	 */
	private boolean formInvalid() {
		return _form.hasVisibleErrors();
	}

	private ValidationResult validation(String attribute) {
		return _form.getValidationModel().getValidation(_form.getOverlay(), part(attribute));
	}

	private TLStructuredTypePart part(String name) {
		return _ticketType.getPart(name);
	}

	/**
	 * Gives the given attribute an {@link AttributeWithFallbackStorage} keeping its explicit value
	 * in the given attribute, with the {@link #SUGGESTION} as fallback.
	 */
	private static void withFallback(TLClassProperty attribute, String storageAttribute) {
		AttributeWithFallbackStorage.Config<?> storageConfig =
			TypedConfiguration.newConfigItem(AttributeWithFallbackStorage.Config.class);
		storageConfig.update(
			storageConfig.descriptor().getProperty(AttributeWithFallbackStorage.Config.STORAGE_ATTRIBUTE),
			storageAttribute);
		storageConfig.update(
			storageConfig.descriptor().getProperty(AttributeWithFallbackStorage.Config.FALLBACK_ATTRIBUTE),
			SUGGESTION);
		TLStorage annotation = TypedConfiguration.newConfigItem(TLStorage.class);
		annotation.setImplementation(storageConfig);
		attribute.setAnnotation(annotation);

		// A part of a transient model does not initialize its storage, which the fallback storage
		// needs to resolve the attributes it reads.
		((StorageImplementation) attribute.getStorageImplementation()).init(attribute);
	}

	/**
	 * A {@link TLConstraints} annotation with a {@link ConstraintCheckByExpression} rejecting an
	 * empty value.
	 */
	private static TLConstraints rejectEmpty() {
		ConstraintCheckByExpression.Config<?> check =
			TypedConfiguration.newConfigItem(ConstraintCheckByExpression.Config.class);
		try {
			check.update(check.descriptor().getProperty(CHECK_PROPERTY),
				ExprFormat.INSTANCE.getValue(CHECK_PROPERTY, "value -> object -> if($value == null, 'No reference.')"));
		} catch (ConfigurationException ex) {
			throw new AssertionError(ex);
		}
		TLConstraints result = TypedConfiguration.newConfigItem(TLConstraints.class);
		result.getConstraints().add(check);
		return result;
	}

	/**
	 * Test suite requiring a knowledge base for the form, the {@link FieldControlService} building
	 * the inputs of the fields, and the {@link SearchBuilder} compiling the expression check.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFallbackValidation.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE));
	}
}
