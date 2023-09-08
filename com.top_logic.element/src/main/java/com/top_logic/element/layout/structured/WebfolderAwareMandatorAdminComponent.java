/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.layout.webfolder.ExtendedWebfolderAware;
import com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * The WebfolderAwareMandatorAdminComponent extends the MandatorAdminComponent for 
 * the usage of document or webfolder attributes
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class WebfolderAwareMandatorAdminComponent extends MandatorAdminComponent implements
        ExtendedWebfolderAware, WebFolderAware {

    public interface Config extends MandatorAdminComponent.Config {
		@Name(XML_WEBFOLDER_ATTR)
		@StringDefault("documents")
		String getWebfolderAttribute();
	}

	private static final String XML_WEBFOLDER_ATTR = "webfolderAttribute";
    
    private String webfolderName;
    
    /** 
     * Creates a {@link WebfolderAwareMandatorAdminComponent}.
     */
    public WebfolderAwareMandatorAdminComponent(InstantiationContext context, Config aSomeAttrs) throws ConfigurationException {
        super(context, aSomeAttrs);
        this.webfolderName = aSomeAttrs.getWebfolderAttribute(); 
    }

    @Override
	public Collection<TLStructuredTypePart> getRelevantWebFolderAttributes(boolean aNonEmptyOnly, BoundCommandGroup aBCG) {
    	// TODO KBU SEC CHECK re-implement, cf. ContractEditComponent

        List theReturn = new ArrayList(1);
        try {
			theReturn.add(((Wrapper) getModel()).tType().getPart(this.webfolderName));
        } catch (Exception ex) {
            
        }
        return theReturn;
    }

    @Override
	public WebFolder getWebFolder(WebFolder aWebFolder) {
        return aWebFolder;
    }

    
    
    @Override
	public Wrapper getWebFolderHolder() {
        return (Wrapper) getModel();
    }

    @Override
	public boolean useOCR() {
        return false;
    }
    
    
    /**
     * @see com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware#getWebFolder()
     */
    @Override
	public WebFolder getWebFolder() {
        WebFolder theFolder = (WebFolder) ((Wrapper) this.getModel()).getValue(this.webfolderName);
        return theFolder;
    }
    
}

