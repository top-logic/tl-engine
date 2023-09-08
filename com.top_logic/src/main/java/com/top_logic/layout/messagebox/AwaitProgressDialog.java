/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.layout.messagebox.ProgressDialog.State;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;

/**
 * {@link ApplicationAction} that waits until an open {@link ProgressDialog} has finished.
 */
@TagName("await-progress-dialog")
public interface AwaitProgressDialog extends ApplicationAction {

	@Override
	@ClassDefault(Op.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * The time to wait for completing the background task.
	 */
	@LongDefault(30000L)
	@Format(MillisFormat.class)
	long getTimeout();

	/**
	 * Whether the {@link ProgressDialog} is automatically closed, after the background progress has
	 * finished.
	 * 
	 * <p>
	 * If the background process does not complete successfully, an error is thrown.
	 * </p>
	 */
	@BooleanDefault(true)
	boolean isAutoCloseOnSuccess();

	/**
	 * Scripting action that waits until the background process controlled by a
	 * {@link ProgressDialog} completes.
	 */
	@Label("Wait for background process to finish")
	class Op extends AbstractApplicationActionOp<AwaitProgressDialog> {

		/**
		 * Creates a {@link AwaitProgressDialog.Op} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Op(InstantiationContext context, AwaitProgressDialog config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			DialogWindowControl activeDialog =
				context.getDisplayContext().getLayoutContext().getMainLayout().getWindowScope().getActiveDialog();

			if (activeDialog != null) {
				DialogModel dialogModel = activeDialog.getDialogModel();
				AbstractDialog dialog = AbstractDialog.getDialog(dialogModel);
				if (dialog instanceof ProgressDialog) {
					((ProgressDialog) dialog).await(getConfig().getTimeout());

					if (getConfig().isAutoCloseOnSuccess()) {
						State state = ((ProgressDialog) dialog).getState();
						if (state != State.COMPLETED) {
							throw ApplicationAssertions.fail(getConfig(),
								"The background process ended in state: " + state);
						}
						dialogModel.getCloseAction().executeCommand(context.getDisplayContext());
					}
				}
			}

			return argument;
		}

	}

}
