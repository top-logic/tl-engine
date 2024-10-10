/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link PostCreateAction} that selects the newly created process in a table.
 */
public class ShowTaskAction extends AbstractConfiguredInstance<ShowTaskAction.Config>
		implements PostCreateAction {

	/**
	 * Configuration options for {@link ShowTaskAction}.
	 */
	public interface Config extends PolymorphicConfiguration<ShowTaskAction> {

		/**
		 * @see #getTokenTable()
		 */
		String TOKEN_TABLE = "tokenTable";

		/**
		 * The component that should select the new process token.
		 */
		@Name(TOKEN_TABLE)
		ComponentName getTokenTable();
	}

	/**
	 * Creates a {@link ShowTaskAction} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ShowTaskAction(InstantiationContext context, ShowTaskAction.Config config) {
		super(context, config);
	}

	@Override
	public void handleNew(LayoutComponent component, Object newModel) {
		Token task = (Token) newModel;

		DisplayContext aContext = DefaultDisplayContext.getDisplayContext();
		new StepOutHelper(component).stepOut(aContext);

		if (task != null) {
			/* Stepping out leads to a deferred display of the outer tile, so also displaying the
			 * active task component must occur deferred. */
			aContext.getLayoutContext().notifyInvalid(displayContext -> showActiveTask(component, task));
		}
	}

	private void showActiveTask(LayoutComponent aComponent, Token task) {
		LayoutComponent listComponent = componentByName(aComponent, getConfig().getTokenTable());
		if (listComponent != null) {
			listComponent.makeVisible();
			((Selectable) listComponent).setSelected(task);
		}
	}

	private LayoutComponent componentByName(LayoutComponent aComponent, ComponentName name) {
		if (name == null) {
			return null;
		}
		return aComponent.getMainLayout().getComponentByName(name);
	}
}