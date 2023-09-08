/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.gui.layout.webfolder;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.common.webfolder.ui.WebFolderComponent;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.scripting.action.ComponentAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.ComponentActionOp;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Operation that checks the existence of a certain document in a referenced
 * {@link WebFolderComponent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CheckFolderActionOp extends ComponentActionOp<CheckFolderActionOp.Config> {

	/**
	 * Configuration of a {@link CheckFolderActionOp}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static interface Config extends ComponentAction {
		/**
		 * The document name to check existence of.
		 */
		String getDocumentName();
	}
	
	/**
	 * Creates a {@link CheckFolderActionOp}.
	 *
	 * @see AbstractApplicationActionOp#AbstractApplicationActionOp(InstantiationContext, com.top_logic.layout.scripting.action.ApplicationAction)
	 */
	public CheckFolderActionOp(InstantiationContext context, CheckFolderActionOp.Config config) {
		super(context, config);
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		WebFolder folder = (WebFolder) ((WebFolderComponent) component).getModel();
		ApplicationAssertions.assertNotNull(config, "No folder in '" + component.getName() + "'.", folder);
		
		{
			String documentName = config.getDocumentName();
			ApplicationAssertions.assertTrue(config,
				"No document named '" + documentName + "' in folder '" + component.getName() + "'.",
				folder.hasChild(documentName));
		}
		
		return argument;
	}

}