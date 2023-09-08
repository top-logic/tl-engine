/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.layout;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueListener;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProvider;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProviderDelegate;

/**
 * Component displaying a mail folder.
 * 
 * The display will result in a table of mails and mail folders contained in this folder.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 *
 */
public class MailFolderTableComponent extends TableComponent implements DecorationValueProvider {

    private DecorationValueProviderDelegate decorationValueProviderDelegate = new DecorationValueProviderDelegate();

    /** 
     * Create a new instance of this class.
     * 
     * @param    someAttrs    Attributes configuring this instance.
     */
    public MailFolderTableComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
    }
    
    @Override
	public String[] getTabBarDecorationValues() {
        String theSizeString = "0";
        MailFolder theModel = MailFolderListModelBuilder.getMailFolder(this.getModel());
        if (theModel != null) {
			theSizeString = "" + theModel.getContentSize();
        }
        return new String[] { theSizeString };
    }
    
    @Override
	public void registerDecorationValueListener(DecorationValueListener aListener) {
        decorationValueProviderDelegate.registerDecorationValueListener(aListener);
    }
    
    @Override
	public boolean unregisterDecorationValueListener(DecorationValueListener aListener) {
        return decorationValueProviderDelegate.unregisterDecorationValueListener(aListener);
    }
}
