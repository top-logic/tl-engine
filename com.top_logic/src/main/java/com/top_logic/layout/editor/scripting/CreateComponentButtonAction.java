/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.scripting;

import java.util.LinkedList;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.scripting.action.LabeledButtonAction;
import com.top_logic.layout.scripting.runtime.action.AbstractLabeledButtonActionOp;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Represents a button click to create a new component using the given stable identifiers for inner
 * template component references.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface CreateComponentButtonAction extends LabeledButtonAction, WithIdentifiers {

	@Override
	@ClassDefault(CreateComponentButtonActionOp.class)
	Class<? extends AbstractLabeledButtonActionOp<?>> getImplementationClass();

	/**
	 * Default implementation of {@link CreateComponentButtonAction}.
	 */
	public class CreateComponentButtonActionOp extends AbstractLabeledButtonActionOp<CreateComponentButtonAction> {

		/**
		 * @param context
		 *        {@link InstantiationContext} context to instantiate sub configurations.
		 * @param config
		 *        {@link CreateComponentButtonAction} configuration for this operation.
		 */
		public CreateComponentButtonActionOp(InstantiationContext context, CreateComponentButtonAction config) {
			super(context, config);
		}

		@Override
		protected HandlerResult execute(ButtonControl button, DisplayContext displayContext) {

			displayContext.set(LayoutTemplateUtils.COMPONENT_IDENTIFIERS,
				new LinkedList<>(config.getIdentifiers().getComponentKeys()));
			displayContext.set(LayoutTemplateUtils.UUIDS, new LinkedList<>(config.getIdentifiers().getUUIDs()));
			try {
				return super.execute(button, displayContext);
			} finally {
				displayContext.reset(LayoutTemplateUtils.UUIDS);
				displayContext.reset(LayoutTemplateUtils.COMPONENT_IDENTIFIERS);
			}
		}

	}
}
