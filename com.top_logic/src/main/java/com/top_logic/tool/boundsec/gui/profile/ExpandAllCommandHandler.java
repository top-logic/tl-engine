/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.tool.boundsec.gui.profile;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.gui.profile.CollapseAllCommandHandler.TreeNameConfig;

/**
 * Command to expand all nodes in a form tree.
 * 
 * @see CollapseAllCommandHandler
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExpandAllCommandHandler extends AbstractCommandHandler {

	/**
	 * Typed configuration interface definition for {@link ExpandAllCommandHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config, TreeNameConfig {
		// sum interface
	}

	/**
	 * Creates a new {@link ExpandAllCommandHandler}.
	 */
    public ExpandAllCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aContext,
			LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
		TreeUIModel theModel =
			CollapseAllCommandHandler.resolveTreeModel(aComponent, ((Config) getConfig()).getTreeName());
		if (theModel != null) {
			TreeUIModelUtil.setExpandedAll(theModel, theModel.getRoot(), true);
		}
		return DefaultHandlerResult.DEFAULT_RESULT;
    }
}
