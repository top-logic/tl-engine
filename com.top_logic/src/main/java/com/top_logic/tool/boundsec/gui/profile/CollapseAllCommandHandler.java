/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.tool.boundsec.gui.profile;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.TreeTableField;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Collapses all nodes in a form tree.
 * 
 * @see ExpandAllCommandHandler
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CollapseAllCommandHandler extends AbstractCommandHandler {

	/**
	 * Configuration of the name of a form member holding a tree.
	 */
	public interface TreeNameConfig extends ConfigurationItem {

		/** 
		 * Name of the form member that holds the tree. 
		 */
		@Mandatory
		String getTreeName();

	}
	/**
	 * Typed configuration interface definition for {@link CollapseAllCommandHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config, TreeNameConfig {
		// sum interface
	}

	/**
	 * Creates a new {@link CollapseAllCommandHandler}.
	 */
    public CollapseAllCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aContext,
            LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
		TreeUIModel theModel = resolveTreeModel(aComponent, ((Config) getConfig()).getTreeName());
		if (theModel != null) {
			TreeUIModelUtil.setExpandedAll(theModel, theModel.getRoot(), false);
		}
        return DefaultHandlerResult.DEFAULT_RESULT;
    }

	/**
	 * Resolves the {@link TreeUIModel} of the form member with the given name in the given
	 * component.
	 *
	 * @return The resolved tree model, or <code>null</code> if no matching tree-valued form member
	 *         exists.
	 */
	static TreeUIModel resolveTreeModel(LayoutComponent component, String treeName) {
		FormMember treeField = ((FormComponent) component).getFormContext().getFirstMemberRecursively(treeName);
		if (treeField instanceof TreeTableField) {
			return ((TreeTableField) treeField).getTree();
		}
		if (treeField instanceof FormTree) {
			return ((FormTree) treeField).getTreeModel();
		}
		return null;
	}
}
