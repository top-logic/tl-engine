/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.ProgressComponent;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.compound.CompoundSecurityProjectLayout;

/**
 * Show the Progress of the ExcelPersonContactImporter.
 * 
 * This is the GUI view into the {@link ExcelPersonContactImporter}.
 * 
 * @author    <a href=mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class ExcelPersonContactImportProgress extends ProgressComponent {

    /** The importer is our model */
    protected transient ExcelPersonContactImporter model;
    
    /** 
     * Create a new ExcelPersonContactImportProgress from XML.
     */
    public ExcelPersonContactImportProgress(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

	static Mandator findMandator(LayoutComponent aComponent) {
		CompoundSecurityLayout checker = CompoundSecurityProjectLayout.getNearestCompoundLayout(aComponent);
		return (Mandator) checker.getCurrentObject(checker.getDefaultCommandGroup(), checker.getModel());
	}

    /** 
     * Hook for subclasses so they are called when the import is finished.
     */
    @Override
	protected void progressFinished(String aContextPath, TagWriter aOut, HttpServletRequest aReq) 
        throws IOException {
        if (model != null) {
            AssistentComponent assi = AssistentComponent.getEnclosingAssistentComponent(this);
            assi.invalidateButtons(); // so next step is shown
        }
    }    
    
}
