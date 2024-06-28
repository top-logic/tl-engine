/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.component.Editor;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.layout.form.component.edit.EditMode.EditorMode;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.DynamicMandatory;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutableState.CommandVisibility;

/**
 * {@link ExecutabilityRule} that reacts on the mode of an {@link Editor}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class EditorExecutabilityRule extends AbstractConfiguredInstance<EditorExecutabilityRule.Config<?>>
		implements ExecutabilityRule {

	/**
	 * Configuration options for {@link EditorExecutabilityRule}.
	 */
	public interface Config<I extends EditorExecutabilityRule> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getMode()
		 */
		String MODE = "mode";

		/**
		 * @see #getVisibility()
		 */
		String VISIBILITY = "visibility";

		/**
		 * @see #getReason()
		 */
		String REASON = "reason";

		/**
		 * The mode of the {@link Editor} that should be restricted.
		 * 
		 * <p>
		 * All other modes are considered executable.
		 * </p>
		 */
		@Name(MODE)
		@Mandatory
		EditorMode getMode();

		/**
		 * The visibility of the command in the specified {@link #getMode()}.
		 */
		@Name(VISIBILITY)
		@Mandatory
		CommandVisibility getVisibility();

		/**
		 * Reason for the {@link #getMode()}.
		 */
		@Name(REASON)
		@DynamicMode(fun = ShowDisabled.class, args = @Ref(VISIBILITY))
		@DynamicMandatory(fun = IfDisabled.class, args = @Ref(VISIBILITY))
		ResKey getReason();

		/**
		 * {@link FieldMode#ACTIVE} if the given {@link CommandVisibility} is
		 * {@link CommandVisibility#DISABLED}, {@link FieldMode#INVISIBLE}, otherwise.
		 */
		class ShowDisabled extends Function1<FieldMode, CommandVisibility> {
			@Override
			public FieldMode apply(CommandVisibility arg) {
				return arg == CommandVisibility.DISABLED ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
			}
		}

		/**
		 * Whether the given {@link CommandVisibility} is {@link CommandVisibility#DISABLED}.
		 */
		class IfDisabled extends Function1<Boolean, CommandVisibility> {
			@Override
			public Boolean apply(CommandVisibility arg) {
				return arg == CommandVisibility.DISABLED;
			}
		}

	}

	private EditorMode _mode;

	private CommandVisibility _visibility;

	private ResKey _reason;

	/**
	 * Creates a {@link EditorExecutabilityRule} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public EditorExecutabilityRule(InstantiationContext context, Config<?> config) {
		super(context, config);

		_mode = getConfig().getMode();
		_visibility = config.getVisibility();
		_reason = config.getReason() == null ? ResKey.NONE : config.getReason();
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (aComponent instanceof EditMode) {
			EditMode editor = (EditMode) aComponent;
			if (editor.getEditorMode() == _mode) {
				return new ExecutableState(_visibility, _reason);
			}
		}
		return ExecutableState.EXECUTABLE;
	}
}
