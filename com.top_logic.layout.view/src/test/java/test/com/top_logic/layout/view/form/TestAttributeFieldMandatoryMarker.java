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

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DynamicVisibility;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.annotate.TLDynamicVisibility;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.Pointer;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the required marker of an {@link AttributeFieldControl} follows the mode of the field,
 * computed by the {@link TLDynamicVisibility} of its attribute, also after the validation of the
 * field changed, and that the input of the field is mandatory exactly while the marker is shown.
 */
@SuppressWarnings("javadoc")
public class TestAttributeFieldMandatoryMarker extends AbstractTicketFormTest {

	/** Name of the attribute that is mandatory by its dynamic visibility only. */
	private static final String REVIEWER = "reviewer";

	/** Name of the attribute that is statically mandatory but read-only by its dynamic visibility. */
	private static final String OWNER = "owner";

	/** Name of the attribute that is mandatory while its {@link Condition} holds. */
	private static final String DUE = "due";

	/** Name of the attribute the {@link Condition} of {@link #DUE} depends on. */
	private static final String DUE_REQUIRED = "dueRequired";

	/** Name of the property {@link TLDynamicVisibility#getModeSelector()}. */
	private static final String MODE_SELECTOR = "mode-selector";

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		TLModelUtil.addProperty(_ticketType, DUE_REQUIRED, text);
		TLProperty due = TLModelUtil.addProperty(_ticketType, DUE, text);
		due.setAnnotation(conditionVisibility());

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

	/**
	 * The input of a field mandatory by a condition of its dynamic visibility is mandatory while the
	 * condition holds, as its label is marked required, and optional again once it no longer holds.
	 */
	public void testConditionalMandatoryReachesInput() {
		Condition condition = condition();
		condition.setHolds(false);
		AttributeFieldControl field = field(DUE);
		_form.enterEditMode();
		assertFalse("Without the condition the field is optional.", required(field));
		assertEquals("Without the condition the input is optional.", Boolean.FALSE, inputMandatory(field));

		holds(condition, true);
		assertTrue("The condition marks the field required.", required(field));
		assertEquals("The condition makes the input mandatory.", Boolean.TRUE, inputMandatory(field));

		holds(condition, false);
		assertFalse("A condition no longer holding removes the marker.", required(field));
		assertEquals("A condition no longer holding makes the input optional.", Boolean.FALSE,
			inputMandatory(field));
	}

	/**
	 * The input of a field whose condition already holds when it is displayed is mandatory from
	 * the start, also after the form switched to another object, which binds the field anew.
	 */
	public void testConditionalMandatoryFromTheStart() {
		ViewChannel input = new DefaultViewChannel("input");
		input.set(_ticket);
		_form.setInputChannel(input);
		Condition condition = condition();
		condition.setHolds(true);
		AttributeFieldControl field = field(DUE);
		_form.enterEditMode();
		assertTrue("The condition marks the field required.", required(field));
		assertEquals("The condition makes the input mandatory.", Boolean.TRUE, inputMandatory(field));

		_form.executeCancel();
		input.set(newTicket("Logout fails", "open"));
		_form.enterEditMode();
		assertTrue("The field of the other object is marked required.", required(field));
		assertEquals("Binding the field to the other object keeps the input mandatory.", Boolean.TRUE,
			inputMandatory(field));
	}

	private Condition condition() {
		return (Condition) DynamicVisibility.modeSelector(_ticketType.getPart(DUE));
	}

	private void holds(Condition condition, boolean holds) {
		condition.setHolds(holds);
		_form.notifyFieldChanged(_ticketType.getPart(DUE_REQUIRED));
	}

	/** Whether the input of the given field is mandatory, as the client receives it. */
	private static Object inputMandatory(AttributeFieldControl field) {
		return field.getInnerControl().scriptingScalarState().get(FieldState.MANDATORY__PROP);
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

	private static TLDynamicVisibility conditionVisibility() {
		Condition.Config<?> selector = TypedConfiguration.newConfigItem(Condition.Config.class);
		TLDynamicVisibility annotation = TypedConfiguration.newConfigItem(TLDynamicVisibility.class);
		annotation.update(annotation.descriptor().getProperty(MODE_SELECTOR), selector);
		return annotation;
	}

	/**
	 * {@link ModeSelector} making a field mandatory while a condition holds, depending on the
	 * attribute {@link #DUE_REQUIRED}.
	 */
	public static class Condition extends AbstractConfiguredInstance<Condition.Config<?>> implements ModeSelector {

		/**
		 * Configuration options for {@link Condition}.
		 */
		public interface Config<I extends Condition> extends PolymorphicConfiguration<I> {
			// No options.
		}

		private boolean _holds;

		/**
		 * Creates a {@link Condition} from configuration.
		 */
		public Condition(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		/**
		 * Sets whether the condition holds.
		 */
		public void setHolds(boolean holds) {
			_holds = holds;
		}

		@Override
		public FormVisibility getMode(TLObject object, TLStructuredTypePart attribute, boolean editMode) {
			return _holds ? FormVisibility.MANDATORY : FormVisibility.DEFAULT;
		}

		@Override
		public void traceDependencies(TLObject object, TLStructuredTypePart attribute, boolean editMode,
				Sink<Pointer> trace, OverlayLookup overlays) {
			trace.add(Pointer.create(object, object.tType().getPart(DUE_REQUIRED)));
		}
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
