/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.gui.layout.person.PersonResourceProvider;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.util.TLContext;

/**
 * {@link PersonResourceProvider} generating links that opens the "edit current person" dialog for
 * current person.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DemoPersonResourceProvider extends PersonResourceProvider {

	/**
	 * Configuration of the {@link DemoPersonResourceProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PersonResourceProvider.Config {
		/**
		 * Component to go to when the user executes a Goto to his own account.
		 */
		@Mandatory
		ComponentName getGotoSelfTarget();
	}

	/**
	 * Creates a new {@link DemoPersonResourceProvider}.
	 */
	public DemoPersonResourceProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String getLink(DisplayContext context, Object anObject) {
		if (anObject != TLContext.getContext().getCurrentPersonWrapper()) {
			return super.getLink(context, anObject);
		}
		return GotoHandler.getJSCallStatement(context, anObject, getConfig().getGotoSelfTarget());
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}
}

