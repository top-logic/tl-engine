/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.model;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.common.webfolder.ui.WebFolderComponent;
import com.top_logic.knowledge.objects.label.ObjectLabel;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo model builder for a {@link WebFolderComponent} using a configured web folder.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DemoWebFolderModelBuilder extends AbstractConfiguredInstance<DemoWebFolderModelBuilder.Config>
		implements ModelBuilder {

	public interface Config extends PolymorphicConfiguration<ModelBuilder> {
		@Name(XML_CONF_KEY_FOLDER_ID)
		@Mandatory
		String getFolderID();
	}

	public static final String XML_CONF_KEY_FOLDER_ID = "folderID";

    private String webFolderID;

    private WebFolder folder;

    /**
	 * Creates a {@link DemoWebFolderModelBuilder}.
	 */
    public DemoWebFolderModelBuilder(InstantiationContext context, Config someAttr) throws ConfigurationException {
		super(context, someAttr);
        this.webFolderID = someAttr.getFolderID();

        if (this.webFolderID != null) {
            try {
                this.folder = this.getInitialFolder();
            }
            catch (Exception ex) {
				throw new ConfigurationException("", ex);
            }
        }
    }

    @Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
        return this.folder;
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return true;
    }

    /**
     * Get the WebFolder from the layout config.
     * 
     * @return    The WebFolder from the layout config.
     * @throws    Exception if creation fails.
     */
    protected final WebFolder getInitialFolder() throws Exception {
		WebFolder theFolder = (WebFolder) WrapperFactory.getWrapper(ObjectLabel.getLabeledObject(this.webFolderID));

        if (theFolder == null) {
            Logger.error("Failed to getInitialFolder() for '" + this.webFolderID  + "'", this);
        }

        return theFolder;
    }
}
