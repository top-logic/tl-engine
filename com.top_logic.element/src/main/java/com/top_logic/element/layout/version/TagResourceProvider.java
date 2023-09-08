/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.version;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.element.version.intf.Tag;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TagResourceProvider extends WrapperResourceProvider {

    private static final char NO_DATE_TIME = 'n';

    private static final char DATE = 'd';

    private static final char DATE_TIME = 't';

    public static final ResourceProvider SIMPLE_INSTANCE = new TagResourceProvider(NO_DATE_TIME);

    public static final ResourceProvider DATE_INSTANCE = new TagResourceProvider(DATE);
    
    public static final ResourceProvider DATETIME_INSTANCE = new TagResourceProvider(DATE_TIME);

    private final char flag;

    /** 
     * Creates a {@link TagResourceProvider}.
     */
    private TagResourceProvider(char aFlag) {
        this.flag = aFlag;
    }

    @Override
	public String getLabel(Object anObject) {
        if (anObject instanceof Tag) {
            Tag    theTag  = (Tag) anObject;
            String theName = theTag.getName();

            if (this.flag != TagResourceProvider.NO_DATE_TIME) {
				Formatter theInstance = HTMLFormatter.getInstance();

                theName += " (" + theInstance.getDateFormat().format(theTag.getDate());

                if (this.flag == TagResourceProvider.DATE_TIME) {
                    theName += ' ' + theInstance.getTimeFormat().format(theTag.getDate());
                }

                theName += ')';
            }

            return theName;
        }
        else { 
            return super.getLabel(anObject);
        }
    }
}

