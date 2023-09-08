/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.io.Serializable;
import java.util.Objects;

import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Holder for a CommandHanlder and the component for which the handler must be executed.
 * 
 * TODO KHA may not be need with new implementation of CommandChain.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class CommandHolder implements Serializable {

    /** the command to execute */
    protected CommandHandler handler;
    
    /** the component the command should be executed for. */
    protected LayoutComponent targetComp;
    
    /**
     * CTor.
     * 
     * @param aHandler the handler to perform, may not be null
     * @param aTargetCompName the components name this handler belongs to.
     */
	public CommandHolder(MainLayout aMainLayout, CommandHandler aHandler, ComponentName aTargetCompName) {
		this(aHandler, componentByName(aMainLayout, aTargetCompName));
    }

	private static LayoutComponent componentByName(MainLayout mainLayout, ComponentName name) {
		LayoutComponent comp = mainLayout.getComponentByName(name);
		if (comp == null) {
			throw new NullPointerException("cannot find component: " + name);
		}
		return comp;
	}
    
    /**
     * CTor.
     * 
     * @param aHandler the handler to perform.
     * @param aTargetComp the component this handler belongs to.
     */
    public CommandHolder(CommandHandler aHandler, LayoutComponent aTargetComp) {
    	Objects.requireNonNull(aHandler,"Handler must not be null");
		Objects.requireNonNull(aTargetComp, "Component must not be null");

        this.handler = aHandler;
        this.targetComp =aTargetComp;
    }

    /**
     * the commandHanlder
     */
    public CommandHandler getHandler() {
        return this.handler;
    }

    /**
     * the component for the command.
     */
    public LayoutComponent getTargetComponent() {
        return this.targetComp;
    }
}
