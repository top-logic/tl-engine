/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.configedit.ConfigListEditorControl;
import com.top_logic.layout.configedit.I18NConstants;
import com.top_logic.layout.configedit.PolymorphicItemControl;
import com.top_logic.layout.configedit.PolymorphicOptions;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Tests for {@link ConfigFormControl}.
 */
public class TestConfigFormControl extends TestCase {

	/** A simple configuration with a single value property, for the happy-path tests. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	/** A configuration with a mandatory property, for the refused-Apply tests. */
	public interface MandatoryConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		@Mandatory
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	/**
	 * A configuration whose only mandatory property is {@link Hidden @Hidden}, so the editor builds
	 * no field for it and the violation it produces has nowhere to be shown.
	 */
	public interface HiddenMandatoryConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		/** Property name for {@link #getSecret()}. */
		String SECRET = "secret";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);

		@Name(SECRET)
		@Mandatory
		@Hidden
		String getSecret();

		/** @see #getSecret() */
		void setSecret(String value);
	}

	/**
	 * A configuration with a property whose format can reject what is typed into it, next to a
	 * plain one - the shape that tells "Apply carried the edit over" apart from "Apply refused".
	 */
	public interface FormatConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		/** Property name for {@link #getTimeout()}. */
		String TIMEOUT = "timeout";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);

		@Name(TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(0L)
		long getTimeout();

		/** @see #getTimeout() */
		void setTimeout(long value);
	}

	/** A single unkeyed entry of {@link CollectionConfig#getItems()}. */
	public interface ListEntry extends ConfigurationItem {

		/** Property name for {@link #getTitle()}. */
		String TITLE = "title";

		@Name(TITLE)
		String getTitle();

		/** @see #getTitle() */
		void setTitle(String value);
	}

	/**
	 * A configuration with a value property and an unkeyed LIST property, for the read-only-view-
	 * mode tests: view mode must not offer the LIST's own add/remove/reorder actions any more than
	 * it lets a plain field accept input.
	 */
	public interface CollectionConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		/** Property name for {@link #getItems()}. */
		String ITEMS = "items";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);

		@Name(ITEMS)
		java.util.List<ListEntry> getItems();
	}

	/**
	 * A configuration with a monomorphic ITEM property that always has a value, so the editor
	 * builds a nested editor for it - the propagation path {@code createNestedEditor} takes.
	 */
	public interface NestedConfig extends ConfigurationItem {

		/** Property name for {@link #getInner()}. */
		String INNER = "inner";

		@Name(INNER)
		@ItemDefault
		ListEntry getInner();
	}

	/** Marker interface for the polymorphic handler implementations, for the type-selector tests. */
	public interface Handler {
		// Marker interface.
	}

	/** Base polymorphic configuration for {@link Handler}. */
	public interface HandlerConfig extends PolymorphicConfiguration<Handler> {
		// Nothing beyond the base - only the concrete type matters for these tests.
	}

	/** Concrete handler config A. */
	public interface HandlerAConfig extends HandlerConfig {
		// Nothing beyond the base.
	}

	/**
	 * Concrete handler config B - a second implementation, so
	 * {@link PolymorphicOptions.Choices#options()} has more than one entry and the type selector
	 * is actually rendered (a single-option collection needs no selector to begin with).
	 */
	public interface HandlerBConfig extends HandlerConfig {
		// Nothing beyond the base.
	}

	/** Concrete {@link Handler} implementation selected through {@link HandlerAConfig}. */
	public static class HandlerA implements Handler {
		/** Creates a {@link HandlerA} from configuration. */
		public HandlerA(InstantiationContext context, HandlerAConfig config) {
			// Nothing to do.
		}
	}

	/** Concrete {@link Handler} implementation selected through {@link HandlerBConfig}. */
	public static class HandlerB implements Handler {
		/** Creates a {@link HandlerB} from configuration. */
		public HandlerB(InstantiationContext context, HandlerBConfig config) {
			// Nothing to do.
		}
	}

	/**
	 * A configuration with a single polymorphic ITEM property, for
	 * {@link PolymorphicItemControl}'s own type-selector tests.
	 */
	public interface PolyConfig extends ConfigurationItem {

		/** Property name for {@link #getHandler()}. */
		String HANDLER = "handler";

		@Name(HANDLER)
		HandlerConfig getHandler();

		/** @see #getHandler() */
		void setHandler(HandlerConfig value);
	}

	/**
	 * A configuration with a LIST of polymorphic entries, for
	 * {@link ConfigListEditorControl}'s own type-selector tests - a different
	 * code path from {@link PolyConfig}'s, even though both end up disabling a
	 * {@link com.top_logic.layout.form.model.SimpleSelectFieldModel} the same way.
	 */
	public interface PolyListConfig extends ConfigurationItem {

		/** Property name for {@link #getItems()}. */
		String ITEMS = "items";

		@Name(ITEMS)
		java.util.List<HandlerConfig> getItems();
	}

	/**
	 * The very {@link ConfigFormControl} under test - a plain subclass, in the manner of the
	 * {@code Testable*} classes the sibling test suites in this package declare, reaching the one
	 * protected member {@link ConfigFormControl} does not expose itself: its child list.
	 */
	static class TestableConfigFormControl extends ConfigFormControl {

		TestableConfigFormControl(ReactContext context, ConfigurationItem config, boolean withEditMode) {
			super(context, config, withEditMode);
		}

		/**
		 * The control's own direct children, so a test can reach the form-level message line
		 * without having to tell it apart from the group headers nested deeper in the editor -
		 * those are {@link com.top_logic.layout.react.control.common.ReactTextControl}s too.
		 */
		java.util.List<ReactControl> children() {
			return getChildren();
		}
	}

	private ReactContext createTestContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	/**
	 * The button label {@link ConfigFormControl} actually renders for the given
	 * {@link I18NConstants} key, resolved through {@link Resources} the same way the control
	 * itself resolves it - rather than a hardcoded literal, which would only match by accident of
	 * the JVM's {@link java.util.Locale#getDefault() default locale}.
	 */
	private String label(ResKey key) {
		return Resources.getInstance().getString(key);
	}

	/**
	 * Simulates clicking the given button and returns the command's {@link HandlerResult}.
	 */
	private HandlerResult click(ReactButtonControl button) {
		return button.executeCommand("click", Map.of());
	}

	/**
	 * Finds the {@link ReactButtonControl} carrying the given label anywhere under the given
	 * control, or {@code null} if none is currently rendered.
	 *
	 * <p>
	 * Reached through {@link ReactControl#scriptingChildren()} and
	 * {@link ReactControl#scriptingScalarState()} - the same headless projection
	 * {@code TestConfigEditorControl#findHeaderButton} uses - walked recursively since a button
	 * here may sit several levels below the {@link ConfigFormControl} itself (e.g. inside the
	 * nested {@link com.top_logic.layout.configedit.ConfigEditorControl}'s own groups, for a
	 * property named the same as a button - not the case in these fixtures, but nothing here
	 * assumes otherwise).
	 * </p>
	 */
	private ReactButtonControl findButton(ReactControl control, String label) {
		if (control instanceof ReactButtonControl button
			&& label.equals(button.scriptingScalarState().get("label"))) {
			return button;
		}
		for (ReactControl child : control.scriptingChildren()) {
			ReactButtonControl found = findButton(child, label);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Finds the {@link ConfigListEditorControl}'s "+" add button
	 * anywhere under the given control, or {@code null} if none is currently rendered - the marker
	 * of "currently rendered" a view-mode form must not offer.
	 *
	 * <p>
	 * Identified by the literal {@code "+ "} prefix {@code ConfigListEditorControl#rebuild}
	 * hardcodes ahead of the property's own (locale-dependent) label, rather than by the full label
	 * text - the same reason {@link #label(ResKey)} exists for the mode buttons: matching the
	 * translated property label here would tie this test to whatever the JVM's default locale
	 * happens to resolve it to.
	 * </p>
	 */
	private ReactButtonControl findAddButton(ReactControl control) {
		if (control instanceof ReactButtonControl button) {
			Object label = button.scriptingScalarState().get("label");
			if (label instanceof String text && text.startsWith("+ ")) {
				return button;
			}
		}
		for (ReactControl child : control.scriptingChildren()) {
			ReactButtonControl found = findAddButton(child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Finds the {@link FieldModel} of the field bound to the given property, walking the same
	 * {@link ReactControl#scriptingChildren()} projection {@link #findButton(ReactControl, String)}
	 * uses down to the {@link ConfigFieldModel} the field control's {@link ReactControl#getModel()}
	 * carries - the way {@code TestConfigEditorControl#countKeyFields} reaches a field model too.
	 */
	private FieldModel fieldOf(ReactControl control, String propertyName) {
		if (control.getModel() instanceof ConfigFieldModel fieldModel
			&& propertyName.equals(fieldModel.getProperty().getPropertyName())) {
			return fieldModel;
		}
		for (ReactControl child : control.scriptingChildren()) {
			FieldModel found = fieldOf(child, propertyName);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Finds the {@link FieldModel} of a polymorphic type selector's field control anywhere under
	 * the given control, or {@code null} if none is currently rendered.
	 *
	 * <p>
	 * Both {@link PolymorphicItemControl} and {@link ConfigListEditorControl}'s own type selector
	 * wrap their {@link com.top_logic.layout.form.model.SimpleSelectFieldModel} in a
	 * {@link com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl} carrying the
	 * literal, hardcoded label {@code "Type"} - never resolved through {@link Resources}, so
	 * matching it is locale-independent by construction, the same reason
	 * {@code TestConfigEditorControl#findTypeFieldModel} matches it too. Walked fully recursively
	 * (unlike that one-level sibling helper), since the selector sits at a different depth in each
	 * of the two tests this is used for - directly under a {@link PolymorphicItemControl} for a
	 * single polymorphic property, one level deeper inside a {@link ConfigListEditorControl}
	 * element group for a polymorphic collection entry.
	 * </p>
	 */
	private FieldModel findTypeFieldModel(ReactControl control) {
		if ("Type".equals(control.scriptingScalarState().get("label"))) {
			for (ReactControl field : control.scriptingChildren()) {
				return (FieldModel) field.getModel();
			}
		}
		for (ReactControl child : control.scriptingChildren()) {
			FieldModel found = findTypeFieldModel(child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * The text currently shown on the form-level message line, or {@code null} if the form has no
	 * such line (view mode, or no edit mode at all).
	 */
	private String formErrorText(TestableConfigFormControl form) {
		for (ReactControl child : form.children()) {
			if (child instanceof ReactTextControl text) {
				return (String) text.scriptingScalarState().get("text");
			}
		}
		return null;
	}

	/** Without edit mode the form is the editor, writing straight through. */
	public void testWithoutEditModeThereAreNoButtons() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);

		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, false);

		assertNull(findButton(form, label(I18NConstants.EDIT)));
		assertNull(findButton(form, label(I18NConstants.APPLY)));
	}

	/** In view mode there is one button, and it starts editing. */
	public void testEditStartsTheWorkingCopy() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setName("before");
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);

		click(findButton(form, label(I18NConstants.EDIT)));

		assertNotNull("Edit mode offers Apply.", findButton(form, label(I18NConstants.APPLY)));
		assertNotNull("Edit mode offers Cancel.", findButton(form, label(I18NConstants.CANCEL)));
		assertNull("Edit mode does not offer Edit again.", findButton(form, label(I18NConstants.EDIT)));
	}

	/** Typing while editing does not reach the item until Apply. */
	public void testApplyCarriesTheChangeOver() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setName("before");
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);
		click(findButton(form, label(I18NConstants.EDIT)));

		fieldOf(form, TestConfig.NAME).setValue("after");
		assertEquals("Nothing reaches the item while editing.", "before", config.getName());

		click(findButton(form, label(I18NConstants.APPLY)));

		assertEquals("after", config.getName());
		assertNotNull("Applying returns to view mode.", findButton(form, label(I18NConstants.EDIT)));
	}

	/** Cancel throws the copy away. */
	public void testCancelDiscardsTheChange() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setName("before");
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);
		click(findButton(form, label(I18NConstants.EDIT)));
		fieldOf(form, TestConfig.NAME).setValue("after");

		click(findButton(form, label(I18NConstants.CANCEL)));

		assertEquals("before", config.getName());
		assertNotNull(findButton(form, label(I18NConstants.EDIT)));
	}

	/**
	 * A violation keeps edit mode open and shows itself at the field - the whole point of applying
	 * rather than writing through.
	 */
	public void testApplyIsRefusedAndReportedAtTheField() {
		MandatoryConfig config = TypedConfiguration.newConfigItem(MandatoryConfig.class);
		config.setName("given");
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);
		click(findButton(form, label(I18NConstants.EDIT)));
		fieldOf(form, MandatoryConfig.NAME).setValue(null);

		click(findButton(form, label(I18NConstants.APPLY)));

		assertNotNull("Edit mode must stay open.", findButton(form, label(I18NConstants.APPLY)));
		assertNotNull("The violation must show at its own field.",
			fieldOf(form, MandatoryConfig.NAME).getError());
	}

	/** A refused Apply leaves the item alone. */
	public void testARefusedApplyChangesNothing() {
		MandatoryConfig config = TypedConfiguration.newConfigItem(MandatoryConfig.class);
		config.setName("given");
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);
		click(findButton(form, label(I18NConstants.EDIT)));
		fieldOf(form, MandatoryConfig.NAME).setValue(null);

		click(findButton(form, label(I18NConstants.APPLY)));

		assertEquals("given", config.getName());
	}

	/**
	 * A violation the editor has no field for must be said out loud at form level: without it,
	 * Apply refuses with nothing whatsoever on screen, and Cancel - discarding the work - is the
	 * only way out of the form.
	 */
	public void testAnUnplaceableViolationIsShownAtFormLevel() {
		HiddenMandatoryConfig config = TypedConfiguration.newConfigItem(HiddenMandatoryConfig.class);
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);
		click(findButton(form, label(I18NConstants.EDIT)));

		click(findButton(form, label(I18NConstants.APPLY)));

		assertNotNull("Edit mode must stay open.", findButton(form, label(I18NConstants.APPLY)));
		assertEquals("The refusal must be readable at form level.",
			label(I18NConstants.ERROR_CANNOT_APPLY), formErrorText(form));
	}

	/**
	 * A violation that did reach its own field says nothing at form level - the field carries it.
	 * This is what pins {@link com.top_logic.layout.configedit.ConfigValidation#report(java.util.List,
	 * com.top_logic.layout.configedit.ConfigFieldIndex)}'s answer as actually being read, rather
	 * than a message shown on every refusal alike.
	 */
	public void testAPlacedViolationIsNotRepeatedAtFormLevel() {
		MandatoryConfig config = TypedConfiguration.newConfigItem(MandatoryConfig.class);
		config.setName("given");
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);
		click(findButton(form, label(I18NConstants.EDIT)));
		fieldOf(form, MandatoryConfig.NAME).setValue(null);

		click(findButton(form, label(I18NConstants.APPLY)));

		assertNotNull("Precondition: the violation must have reached its field.",
			fieldOf(form, MandatoryConfig.NAME).getError());
		assertEquals("Nothing to add at form level.", "", formErrorText(form));
	}

	/** Leaving and re-entering edit mode drops an earlier refusal's message. */
	public void testTheFormLevelMessageIsDroppedOnCancel() {
		HiddenMandatoryConfig config = TypedConfiguration.newConfigItem(HiddenMandatoryConfig.class);
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);
		click(findButton(form, label(I18NConstants.EDIT)));
		click(findButton(form, label(I18NConstants.APPLY)));

		click(findButton(form, label(I18NConstants.CANCEL)));
		click(findButton(form, label(I18NConstants.EDIT)));

		assertEquals("A fresh edit starts without the previous attempt's message.",
			"", formErrorText(form));
	}

	/**
	 * Apply must refuse while any field still rejects what was typed into it. The configuration
	 * itself is clean in that state - a rejected input never reached it - so nothing but the
	 * fields can tell, and applying would leave edit mode and drop the typed text unremarked.
	 */
	public void testApplyIsRefusedWhileAFieldRejectsItsInput() {
		FormatConfig config = TypedConfiguration.newConfigItem(FormatConfig.class);
		config.setName("before");
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);
		click(findButton(form, label(I18NConstants.EDIT)));
		fieldOf(form, FormatConfig.NAME).setValue("after");
		fieldOf(form, FormatConfig.TIMEOUT).setValue("5 potatoes");
		assertNotNull("Precondition: the format must actually have rejected the input.",
			fieldOf(form, FormatConfig.TIMEOUT).getError());

		click(findButton(form, label(I18NConstants.APPLY)));

		assertNotNull("Edit mode must stay open.", findButton(form, label(I18NConstants.APPLY)));
		assertEquals("Nothing may be carried over while an input is rejected.",
			"before", config.getName());
		assertNotNull("The rejected input must stay on screen to be corrected.",
			fieldOf(form, FormatConfig.TIMEOUT).getError());
	}

	/** Once the rejected input is corrected, the very same Apply goes through. */
	public void testApplyProceedsOnceTheRejectedInputIsCorrected() {
		FormatConfig config = TypedConfiguration.newConfigItem(FormatConfig.class);
		config.setName("before");
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);
		click(findButton(form, label(I18NConstants.EDIT)));
		fieldOf(form, FormatConfig.NAME).setValue("after");
		fieldOf(form, FormatConfig.TIMEOUT).setValue("5 potatoes");
		click(findButton(form, label(I18NConstants.APPLY)));

		fieldOf(form, FormatConfig.TIMEOUT).setValue("5min");
		click(findButton(form, label(I18NConstants.APPLY)));

		assertEquals("after", config.getName());
		assertNotNull("Applying returns to view mode.", findButton(form, label(I18NConstants.EDIT)));
	}

	/**
	 * View mode - {@code withEditMode = true}, model not editing - must not let a field accept
	 * input: the whole point of a mode is that nothing reaches the item outside of one, and a
	 * field that still writes through defeats that regardless of what the buttons show.
	 */
	public void testViewModeFieldsAreNotEditable() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);

		assertFalse("A field must not accept input before Bearbeiten.",
			fieldOf(form, TestConfig.NAME).isEditable());

		click(findButton(form, label(I18NConstants.EDIT)));

		assertTrue("Edit mode makes the field editable again.",
			fieldOf(form, TestConfig.NAME).isEditable());
	}

	/**
	 * View mode reaches a nested item's own fields too: a form is read-only at every nesting
	 * depth, not only at the top level.
	 */
	public void testViewModeNestedItemFieldsAreNotEditable() {
		NestedConfig config = TypedConfiguration.newConfigItem(NestedConfig.class);
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);

		assertFalse("A nested item's field must not accept input before Bearbeiten.",
			fieldOf(form, ListEntry.TITLE).isEditable());

		click(findButton(form, label(I18NConstants.EDIT)));

		assertTrue("Edit mode makes the nested field editable again.",
			fieldOf(form, ListEntry.TITLE).isEditable());
	}

	/**
	 * View mode must not offer a LIST/ARRAY/MAP property's own add/remove/reorder actions either -
	 * those are not fields, so nothing about {@link ConfigFieldModel#setEditable(boolean)} reaches
	 * them; the add button must not be rendered at all, not merely be present and disabled.
	 */
	public void testViewModeOffersNoCollectionAction() {
		CollectionConfig config = TypedConfiguration.newConfigItem(CollectionConfig.class);
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);

		assertNull("No add button before Bearbeiten.", findAddButton(form));

		click(findButton(form, label(I18NConstants.EDIT)));

		assertNotNull("Edit mode offers the add button again.", findAddButton(form));
	}

	/**
	 * {@code withEditMode = false} must behave exactly as it always has: every field and every
	 * collection action stays offered, since this is the view designer's write-through case, not a
	 * mode with a view side to lock down.
	 */
	public void testWithoutEditModeEverythingStaysEditable() {
		CollectionConfig config = TypedConfiguration.newConfigItem(CollectionConfig.class);
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, false);

		assertTrue("The field must stay editable.", fieldOf(form, CollectionConfig.NAME).isEditable());
		assertNotNull("The add button must stay offered.", findAddButton(form));
	}

	/**
	 * View mode must close a single polymorphic ITEM property's type selector too, not just a
	 * plain field: {@link PolymorphicItemControl} backs it with a
	 * {@link com.top_logic.layout.form.model.SimpleSelectFieldModel}, not a
	 * {@link ConfigFieldModel}, but it is the very same hole - a viewer could otherwise still
	 * swap the configured implementation - and the model editor this branch is being built for is
	 * exactly this shape of property.
	 */
	public void testViewModePolymorphicItemTypeSelectorIsNotEditable() {
		PolyConfig config = TypedConfiguration.newConfigItem(PolyConfig.class);
		config.setHandler(TypedConfiguration.newConfigItem(HandlerAConfig.class));
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);

		assertFalse("The type selector must not accept a change before Bearbeiten.",
			findTypeFieldModel(form).isEditable());

		click(findButton(form, label(I18NConstants.EDIT)));

		assertTrue("Edit mode makes the type selector editable again.",
			findTypeFieldModel(form).isEditable());
	}

	/**
	 * View mode must close a polymorphic collection entry's own type selector too - a different
	 * code path from {@link #testViewModePolymorphicItemTypeSelectorIsNotEditable()}:
	 * {@link ConfigListEditorControl} builds it for each of its own entries, not
	 * {@link PolymorphicItemControl}.
	 */
	public void testViewModePolymorphicListEntryTypeSelectorIsNotEditable() {
		PolyListConfig config = TypedConfiguration.newConfigItem(PolyListConfig.class);
		config.getItems().add(TypedConfiguration.newConfigItem(HandlerAConfig.class));
		TestableConfigFormControl form = new TestableConfigFormControl(createTestContext(), config, true);

		assertFalse("The entry's type selector must not accept a change before Bearbeiten.",
			findTypeFieldModel(form).isEditable());

		click(findButton(form, label(I18NConstants.EDIT)));

		assertTrue("Edit mode makes the entry's type selector editable again.",
			findTypeFieldModel(form).isEditable());
	}

	/**
	 * Suite requiring {@link TypeIndex} for {@link TypedConfiguration} and
	 * {@link ThreadContextManager} for the field label resolution the editor and the mandatory
	 * violation message use.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigFormControl.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}
}
