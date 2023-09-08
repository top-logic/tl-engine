/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.layout;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.mail.base.Mail;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.base.MailFolderAware;
import com.top_logic.mail.base.MailServer;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Returns the contents of a {@link MailFolder} of an {@link MailFolderAware}.
 * 
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MailFolderListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link MailFolderListModelBuilder} instance.
	 */
	public static final MailFolderListModelBuilder INSTANCE = new MailFolderListModelBuilder();

	private MailFolderListModelBuilder() {
		// Singleton constructor.
	}

    @Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		MailFolder elementFolder;
        if (anObject instanceof Mail) {
			elementFolder = ((Mail) anObject).getFolder();
		} else {
			elementFolder = ((MailFolder) anObject).getParent();
        }
		Object componentModel = aComponent.getModel();
		if (elementFolder == getMailFolder(componentModel)) {
			// stability
			return componentModel;
        }
		return elementFolder;
    }

    @Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
		return (anObject instanceof Mail) || (anObject instanceof MailFolder);
    }

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
        try {
			MailFolder theModel = getMailFolder(businessModel);
            if (theModel != null) {
                return CollectionUtil.toList(theModel.getContent());
            }
        }
        catch (Exception ex) {
            Logger.warn("Failed to get mails from mail server", ex, MailFolderListModelBuilder.class);
        }
        return (Collections.EMPTY_LIST);
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return MailServer.isActivated() && ((aModel instanceof MailFolderAware) || (aModel instanceof MailFolder));
    }

    public static MailFolder getMailFolder(Object aModel) {
        
        if (aModel == null) {
            return null;
        }
        else if (aModel instanceof MailFolder) {
            return (MailFolder) aModel;
        }
        else if (aModel instanceof MailFolderAware) {
            return ((MailFolderAware) aModel).getMailFolder();
        }
        
        throw new IllegalArgumentException("Model must be a MailFolder or MailFolderAware, but it was " + aModel);
    }
}
