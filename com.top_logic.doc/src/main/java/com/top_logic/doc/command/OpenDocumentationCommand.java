/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command;

import static com.top_logic.basic.col.FilterUtil.*;
import static com.top_logic.basic.util.Utils.*;
import static com.top_logic.layout.tree.model.TLTreeModelUtil.*;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.doc.component.DocumentationViewer;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.help.HelpFinder;
import com.top_logic.layout.window.OpenWindowCommand;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.treeview.ComponentTreeViewNoDialogsOrWindows;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Opens the documentation window.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class OpenDocumentationCommand extends OpenWindowCommand {

	/** {@link ConfigurationItem} for the {@link OpenDocumentationCommand}. */
	public interface Config extends OpenWindowCommand.Config {

		/** The default value for {@link #getId()}. */
		String ID = "openDocWindow";

		@Override
		@StringDefault(ID)
		String getId();

		@Override
		@StringDefault(CommandHandlerFactory.HELP_GROUP)
		String getClique();

	}

	/** {@link TypedConfiguration} constructor for {@link OpenDocumentationCommand}. */
	public OpenDocumentationCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void initWindow(DisplayContext context, LayoutComponent opener, WindowComponent window,
			Map<String, Object> arguments) {
		String helpId = HelpFinder.findHelpId(opener);
		if (StringServices.isEmpty(helpId)) {
			InfoService.showInfo(I18NConstants.NO_HELP_FOR_VIEW);
			return;
		}
		boolean success = showDocumentation(window, helpId);
		if (!success) {
			InfoService.showInfo(I18NConstants.NO_DOCUMENTATION_FOR_VIEW__ID.fill(helpId));
		}
	}

	/**
	 * Shows the help for the {@link LayoutComponent} with the given help ID.
	 * 
	 * @param window
	 *        The window displaying the help.
	 * @param helpId
	 *        Help ID of some {@link LayoutComponent}.
	 * @return Whether the documentation for the given help ID could be displayed.
	 */
	public static boolean showDocumentation(WindowComponent window, String helpId) {
		List<DocumentationViewer> documentationViewers = findDocumentationViewer(window);
		if (documentationViewers.isEmpty()) {
			throw errorNoDocumentationViewer(window);
		}
		if (documentationViewers.size() > 1) {
			throw errorMultipleDocumentationViewer(window);
		}
		return documentationViewers.get(0).showDocumentation(helpId);
	}

	private static List<DocumentationViewer> findDocumentationViewer(WindowComponent window) {
		TreeView<LayoutComponent> componentTree = new ComponentTreeViewNoDialogsOrWindows();
		return filterList(DocumentationViewer.class, getChildrenRecursively(componentTree, window));
	}

	private static RuntimeException errorNoDocumentationViewer(WindowComponent window) {
		return new RuntimeException(
			"Found no " + DocumentationViewer.class.getSimpleName() + " in " + debug(window) + ".");
	}

	private static RuntimeException errorMultipleDocumentationViewer(WindowComponent window) {
		return new RuntimeException(
			"Found multiple " + DocumentationViewer.class.getSimpleName() + "s in " + debug(window) + ".");
	}

}
