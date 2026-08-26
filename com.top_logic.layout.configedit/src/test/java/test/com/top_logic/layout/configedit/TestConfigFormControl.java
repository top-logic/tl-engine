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
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.configedit.I18NConstants;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
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
	 * The very {@link ConfigFormControl} under test - a plain subclass, kept for symmetry with the
	 * {@code Testable*} classes the sibling test suites in this package declare, even though
	 * nothing here needs a protected member {@link ConfigFormControl} does not already expose.
	 */
	static class TestableConfigFormControl extends ConfigFormControl {

		TestableConfigFormControl(ReactContext context, ConfigurationItem config, boolean withEditMode) {
			super(context, config, withEditMode);
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
