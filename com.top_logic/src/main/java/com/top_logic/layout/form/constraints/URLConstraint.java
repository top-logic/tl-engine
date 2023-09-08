/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.net.MalformedURLException;
import java.net.URL;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.util.Resources;

/**
 * Constraint for string denoting URLs.
 * 
 * Use the building java.net.URL for validattion.
 * 
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class URLConstraint extends StringLengthConstraint {

    /** ResourceKey for wrong Protocl Message */
	private static final ResKey URL_PROTOCOL = I18NConstants.ERROR_INVALID_PROTOCOL__GIVEN_EXPECTED;
    
    /** When not null this protocol must be used */
    protected String proto;

    public URLConstraint(int minLength) {
        super(minLength);
    }

    public URLConstraint(int minLength, int maxLength) {
        super(minLength, maxLength);
    }

    /**
     * Create a new URLConstraint with min, maxlength and protocol.
     */
    public URLConstraint(int minLength, int maxLength, String aProtocol) {
        super(minLength, maxLength);
        proto = aProtocol;
    }

    /** 
     * @see com.top_logic.layout.form.constraints.StringLengthConstraint#checkString(java.lang.String)
     */
    @Override
	protected boolean checkString(String aValue) throws CheckException {
        
		if (!StringServices.isEmpty(aValue))
			try {
            URL theURL = new URL(aValue);
            if (proto != null && !proto.equals(theURL.getProtocol())) {
					throw new CheckException(Resources.getInstance().getMessage(URL_PROTOCOL, theURL.getProtocol(),
						proto));
            }
        } catch (MalformedURLException mfux) {
            throw new CheckException(mfux.getMessage());
        }
        
        return super.checkString(aValue);
    }
}
