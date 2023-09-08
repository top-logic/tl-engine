/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.ResPrefix;
import com.top_logic.util.Resources;

/**
 * The PartialI18NResourceProvider creates internationalized labels. In opposition to the
 * {@link I18NResourceProvider}, the default label gets returned instead of the key
 * identifier, if no internationalization was found.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class PartialI18NResourceProvider extends DefaultResourceProvider {

    /** Instance of this resource provider without a resource key prefix. */
    public static final PartialI18NResourceProvider INSTANCE = new PartialI18NResourceProvider();


    /** The resource key prefix. */
	private ResPrefix resPrefix;


    /**
     * Creates a new PartialI18NResourceProvider without a resource key prefix.
     */
    public PartialI18NResourceProvider() {
		this(ResPrefix.GLOBAL);
    }

    /**
     * Creates a new I18NResourceProvider with the given resource key prefix.
     *
     * @param aResPrefix
     *            the resource prefix to use
     */
	public PartialI18NResourceProvider(ResPrefix aResPrefix) {
		resPrefix = aResPrefix == null ? ResPrefix.GLOBAL : aResPrefix;
    }

    @Override
	public String getLabel(Object aObject) {
        String theKey = super.getLabel(aObject);
        if (StringServices.isEmpty(theKey)) {
            return "";
        }
		String theLabel = Resources.getInstance().getString(resPrefix.key(theKey), null);
        return theLabel == null ? theKey : theLabel;
    }

}
