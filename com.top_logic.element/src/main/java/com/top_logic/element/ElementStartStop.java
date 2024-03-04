/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element;


import jakarta.servlet.ServletContext;

import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.util.StartStopListener;

/**
 * Setup the element Project on startup.
 *
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class ElementStartStop extends StartStopListener {

    /** Overridden to setup some element specific factories */
    @Override
	protected void initSubClassHook(ServletContext aContext) throws Exception {
        this.setupAttributeFormFactory();
        this.setupElementFactories();
		this.setupNumberHandler();

        this.setupInitialDisplayDescriptions();
        
        // Cannot call this later alas this result in some strange dependencies
        super.initSubClassHook(aContext);
    }

	/** This method sets up the DefaultNumberHandler. */
	protected void setupNumberHandler() throws Exception {
		// Ignore.
	}

    /**
     * Setup the AttributeFormFactory.
     */
    private void setupAttributeFormFactory() throws ClassNotFoundException {
        if (null == AttributeFormFactory.getInstance()) {
            throw new NullPointerException("Failed to setupAttributeFormFactory");
        }
    }

    /**
     * Setup all the MetaFactories for your Project.
     *
     * After the call the Knowledgebase will be committed, so
     * you do not have to worry about wild commits().
     *
     * @throws    ElementException    If setting up the factories fails for a reason.
     */
    protected void setupElementFactories() throws ElementException {
        this.setupElementFactory();
    }



    /**
     * Setup the Element factory, override as needed.
     *
     * @throws    ElementException    If setting up the factories fails for a reason.
     */
    protected void setupElementFactory() throws ElementException {
        // hook for subclasses only
    }

    protected void setupInitialDisplayDescriptions() throws Exception {
        // hook for subclasses only
    }
}
