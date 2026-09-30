/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.AbstractDeleteCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link EditComponent} that provides mode switch buttons without actually saving any changes.
 * 
 * <p>
 * Note: Only for testing form elements.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DummyEditComponent extends EditComponent {

	/**
	 * Configuration options for {@link DummyEditComponent}.
	 */
	public interface Config extends EditComponent.Config {
		@Override
		@NullDefault
		String getLockOperation();

		@Override
		@StringDefault(DummyApplyCommand.COMMAND_ID)
		String getApplyCommand();

		@Override
		@StringDefault(DummyDeleteCommand.COMMAND_ID)
		String getDeleteCommand();
	}

	/**
	 * Creates a {@link DummyEditComponent} from configuration.
	 */
	public DummyEditComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(new Object());
		}
		return super.validateModel(context);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return true;
	}

	/**
	 * Apply command for this component. Does actually nothing.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static final class DummyApplyCommand extends AbstractApplyCommandHandler {

		public static final String COMMAND_ID = "dummyApply";

		/**
		 * Creates a {@link DummyApplyCommand}.
		 */
		public DummyApplyCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
			return false;
		}

	}

	/**
	 * Delete command for this component. Does actually nothing.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static final class DummyDeleteCommand extends AbstractDeleteCommandHandler {

		public static final String COMMAND_ID = "dummyDelete";

		/**
		 * Creates a {@link DummyDeleteCommand}.
		 */
		public DummyDeleteCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected void deleteObject(LayoutComponent component, Object model, Map<String, Object> arguments) {
			// Ignore.
		}

	}

}
