/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A composite {@link ReactControl} that renders toggle buttons for a {@link FormField}'s disabled,
 * immutable, and mandatory properties via the {@code TLFieldToggles} React component.
 *
 * <p>
 * Demonstrates how to build a composite control with multiple commands dispatched from a single
 * React component. The React state reflects the current field properties so that buttons can show
 * active/inactive styling.
 * </p>
 */
public class DemoFieldTogglesControl extends ReactControl {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ToggleDisabledCommand(),
		new ToggleImmutableCommand(),
		new ToggleMandatoryCommand());

	private final FormField _field;

	/**
	 * Creates a new {@link DemoFieldTogglesControl}.
	 *
	 * @param field
	 *        The form field whose properties are toggled.
	 */
	public DemoFieldTogglesControl(FormField field) {
		super(field, "TLFieldToggles", COMMANDS);
		_field = field;
		syncState();
	}

	private void syncState() {
		getReactState().put("disabled", _field.isDisabled());
		getReactState().put("immutable", _field.isImmutable());
		getReactState().put("mandatory", _field.isMandatory());
	}

	/**
	 * Toggles the disabled state of the field and pushes the updated state via SSE.
	 */
	public static class ToggleDisabledCommand extends ControlCommand {

		/** Creates a {@link ToggleDisabledCommand}. */
		public ToggleDisabledCommand() {
			super("toggleDisabled");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("demo.react.formFields.toggleDisabled");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			DemoFieldTogglesControl toggles = (DemoFieldTogglesControl) control;
			FormField field = toggles._field;
			field.setDisabled(!field.isDisabled());
			toggles.patchReactState(Collections.singletonMap("disabled", field.isDisabled()));
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Toggles the immutable state of the field and pushes the updated state via SSE.
	 */
	public static class ToggleImmutableCommand extends ControlCommand {

		/** Creates a {@link ToggleImmutableCommand}. */
		public ToggleImmutableCommand() {
			super("toggleImmutable");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("demo.react.formFields.toggleImmutable");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			DemoFieldTogglesControl toggles = (DemoFieldTogglesControl) control;
			FormField field = toggles._field;
			field.setImmutable(!field.isImmutable());
			toggles.patchReactState(Collections.singletonMap("immutable", field.isImmutable()));
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Toggles the mandatory state of the field and pushes the updated state via SSE.
	 */
	public static class ToggleMandatoryCommand extends ControlCommand {

		/** Creates a {@link ToggleMandatoryCommand}. */
		public ToggleMandatoryCommand() {
			super("toggleMandatory");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("demo.react.formFields.toggleMandatory");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			DemoFieldTogglesControl toggles = (DemoFieldTogglesControl) control;
			FormField field = toggles._field;
			field.setMandatory(!field.isMandatory());
			toggles.patchReactState(Collections.singletonMap("mandatory", field.isMandatory()));
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
