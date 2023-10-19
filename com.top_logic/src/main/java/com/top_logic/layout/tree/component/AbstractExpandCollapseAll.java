/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.Null;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Expands all nodes in a tree {@link LayoutComponent}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractExpandCollapseAll extends PreconditionCommandHandler {

	/** {@link ConfigurationItem} for the {@link AbstractExpandCollapseAll}. */
	public interface Config extends PreconditionCommandHandler.Config {

		/** Property name of {@link #getExpand()}. */
		String EXPAND = "expand";

		/** Whether the nodes should be expanded (true) or collapsed (false). */
		@Mandatory
		@Name(EXPAND)
		boolean getExpand();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		CommandGroupReference getGroup();

		@Override
		@ItemDefault(Null.class)
		ModelSpec getTarget();

	}

	private final boolean _expand;

	/** {@link TypedConfiguration} constructor for {@link AbstractExpandCollapseAll}. */
	public AbstractExpandCollapseAll(InstantiationContext context, Config config) {
		super(context, config);
		_expand = config.getExpand();
	}

	/**
	 * Whether the nodes should be expanded (true) or collapsed (false).
	 */
	public boolean expand() {
		return _expand;
	}

}
