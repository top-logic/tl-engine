/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import com.top_logic.basic.XMain;

/**
 * Allow generation of compiled JS Files via main method for deployment.
 * 
 * This way the js-file can be deployed to a frontend-server (e.g. Apache/IIS) 
 * instead of creating and serving it from the Web-container
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class JSDeploy extends XMain {
    
    /**
     * @see com.top_logic.basic.Main#showHelpArguments()
     */
    @Override
	protected void showHelpArguments() {
        System.out.println("\tNo arguments as this Command does a single job only.");
    }
    
    /**
     * No arguments required, here.
     */
    @Override
	protected boolean argumentsRequired() {
        return false;
    }
    
    /**
     * @see com.top_logic.basic.XMain#doActualPerformance()
     */
    @Override
	protected void doActualPerformance() throws Exception {
        initFileManager();
		setupModuleContext(JSFileCompiler.Module.INSTANCE);
        JSFileCompiler jsf = JSFileCompiler.getInstance(); 
        if (jsf.isDeployed()) {
            throw new RuntimeException("JSFileCompiler.isDeployed() mut not be true, check your configuration.");
        }

        jsf.getTarget(); // NOt that we actually need this
    }
    
    /** 
     * Call XMain#runMainCommandLine to get thing going.
     */
    public static void main(String[] args) throws Exception {
        new JSDeploy().runMainCommandLine (args);
    }

}

