/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.sched.Batch;
import com.top_logic.mig.html.layout.ProgressComponent;

/**
 * Show the Progress of the SAPVolumeImporter.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SAPVolumeProgressComponent extends ProgressComponent {

    public interface Config extends ProgressComponent.Config {
		@Name(XML_ATTRIBUTE_MANDATORE_AWARE)
		@BooleanDefault(true)
		boolean getMandatorAware();
	}

	/** Copy of model actually found in scheduler */
    protected transient Batch model;
	/** Copy of model actually stored in AssitentComponenet */
	public static final String XML_ATTRIBUTE_MANDATORE_AWARE = "mandatorAware";
	protected boolean mandatorAware;
    
    /** 
     * Create a new SAPVolumeProgressComponent from XML.
     */
    public SAPVolumeProgressComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        
        this.mandatorAware = atts.getMandatorAware();
    }
    
}
