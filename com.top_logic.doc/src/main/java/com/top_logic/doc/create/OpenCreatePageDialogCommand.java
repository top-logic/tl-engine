/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.create;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.doc.component.DocumentationTreeComponent;
import com.top_logic.doc.model.Page;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.FunctionalSuccess;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link Command} to open a {@link CreatePageDialog} that creates a {@link Page} after the given
 * {@link Page}.
 * 
 * @author <a href="mailto:dpa@top-logic.com">dpa</a>
 */
public class OpenCreatePageDialogCommand extends PreconditionCommandHandler {

	/** {@link ConfigurationItem} for the {@link OpenCreatePageDialogCommand}. */
	public interface Config extends AbstractCommandHandler.Config {

		/** Menu group for import buttons */
		String MENU = "createMenu";

		@Override
		@StringDefault(MENU)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
		CommandGroupReference getGroup();

		/**
		 * Whether the new page is created as child of the target page. If <code>false</code> the
		 * new page will be a sibling.
		 */
		@BooleanDefault(true)
		boolean isCreateChild();

	}

	/** {@link TypedConfiguration} constructor for {@link OpenCreatePageDialogCommand}. */
	public OpenCreatePageDialogCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(model instanceof Page)) {
			return new Failure(com.top_logic.doc.command.I18NConstants.SELECTION_IS_NO_PAGE);
		}
		Page parent = (Page) model;
		return new FunctionalSuccess(ignored -> openDialog(component, parent));
	}

	/**
	 * Opens an {@link CreatePageDialog} to create a {@link Page} after the selected {@link Page}.
	 * 
	 * @param component
	 *        {@link WindowScope} in which the dialog should be opened.
	 * @param parent
	 *        Currently selected {@link Page}.
	 */
	public HandlerResult openDialog(LayoutComponent component, Page parent) {
		CreatePageDialog createDialog = getCreatePageDialog(component, parent);
		WindowScope scope = component.getMainLayout().getWindowScope();
		createDialog.open(scope);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The {@link CreatePageDialog} to open.
	 * 
	 * @param component
	 *        The {@link LayoutComponent} of the dialog.
	 * @param selected
	 *        The currently selected {@link Page} of the {@link DocumentationTreeComponent}.
	 * @return A new {@link CreatePageDialog} to open.
	 */
	protected CreatePageDialog getCreatePageDialog(LayoutComponent component, Page selected) {
		boolean isCreateChild = config().isCreateChild();
		return new CreatePageDialog(component,
			DisplayDimension.dim(400, DisplayUnit.PIXEL),
			DisplayDimension.dim(260, DisplayUnit.PIXEL), selected, isCreateChild);
	}
}
