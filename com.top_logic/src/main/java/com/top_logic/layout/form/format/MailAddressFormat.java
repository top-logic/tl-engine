/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.DescriptiveParsePosition;
import com.top_logic.util.Resources;

/**
 * {@link Format} for a single e-mail address.
 * 
 * <p>
 * An address is valid, if it is accepted by {@link #checkEMailAddress(String)}: a bare RFC 5322
 * address in the strict syntax of {@link InternetAddress}, whose domain contains a dot.
 * Internationalized addresses with non-ASCII characters in the local part or the domain (RFC 6531,
 * e.g. <code>müller@bücher.de</code>) are valid.
 * </p>
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class MailAddressFormat extends Format {

    @Override
	public Object parseObject(String aSource, ParsePosition aPos) {
        if (aPos == null) {
            throw new NullPointerException();
        }
        if (StringServices.isEmpty(aSource)) {
            return returnError(aPos, 0);
        }
        
        int    start = aPos.getIndex();
        String str   = aSource.substring(start).trim();
        if (checkEMailAddress(str)) {
            aPos.setIndex(aSource.length());
        } else {
            return returnError(aPos, start);
        }
        return str;
    }

    @Override
	public StringBuffer format(Object aMailAddress, StringBuffer aToAppendTo, FieldPosition aPos) {
        if (aToAppendTo == null || aPos == null) {
            throw new NullPointerException();
        }
        if (!(aMailAddress instanceof String)) {
            throw new IllegalArgumentException();
        }
        return aToAppendTo.append(aMailAddress);        
    }

    /**
     * Whether the given text is a valid e-mail address.
     * 
     * <p>
     * The text (without surrounding white space) is valid, if
     * </p>
     * <ul>
     * <li>{@link InternetAddress} parses it in strict mode and {@link InternetAddress#validate()}
     * accepts it (RFC 5322 address syntax, extended by RFC 6531 to non-ASCII characters in the
     * local part and the domain),</li>
     * <li>it is a bare address, i.e. the parsed {@link InternetAddress#getAddress()} is the text
     * itself: a personal name (<code>Max &lt;max@example.com&gt;</code>), a group or a list of
     * addresses is invalid,</li>
     * <li>the domain part after the last <code>@</code> contains a dot, so that a host name
     * without domain such as <code>admin@localhost</code> is invalid.</li>
     * </ul>
     * 
     * @param aAddress
     *        The e-mail address to check, may be <code>null</code>.
     * @return Whether the given address is valid, <code>false</code> for <code>null</code> or an
     *         empty text.
     */
    public static boolean checkEMailAddress(String aAddress) {
        if (aAddress == null) {
            return false;
        }
        String address = aAddress.trim();
        if (address.isEmpty()) {
            return false;
        }

        InternetAddress parsed;
        try {
            parsed = new InternetAddress(address, true);
            parsed.validate();
        } catch (AddressException ex) {
            return false;
        }
        if (parsed.isGroup() || !address.equals(parsed.getAddress())) {
            return false;
        }

        String domain = address.substring(address.lastIndexOf('@') + 1);
        return domain.indexOf('.') >= 0;
    }

    private Object returnError(ParsePosition aPos, int aIndex) {
        aPos.setErrorIndex(aIndex);
        if (aPos instanceof DescriptiveParsePosition) {
            DescriptiveParsePosition desPos = (DescriptiveParsePosition)aPos;
			desPos.setErrorDescription(Resources.getInstance().getString(I18NConstants.INVALID_EMAIL_ADDRESSES));
        }
        return null;
    }
    
}

