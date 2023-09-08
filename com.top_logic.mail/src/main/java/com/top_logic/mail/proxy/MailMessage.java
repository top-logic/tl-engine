/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface MailMessage extends MailServerMessage {

	/** Refer to <a href="https://tools.ietf.org/html/rfc3462">https://tools.ietf.org/html/rfc3462</a>. */
	public static final String MULTIPART_REPORT = "multipart/report";

	/**
	 * Refer to
	 * <a href="https://tools.ietf.org/html/rfc2387">https://tools.ietf.org/html/rfc2387</a>.
	 */
	String MULTIPART_RELATED = "multipart/related";

	/** 
     * The text content of the mail.
     * 
     * This method returns the user readable content of a mail.
     * 
     * @return    The text contained in the mail, never <code>null</code>.
     */
    public String getBodyContent();
    
    /**
     * Return the content type of {@link #getBodyContent()}
     */
    public String getBodyContentType();

    /** 
	 * Server managing messages (such as not delivered) shouldn't be handled by the daemon.
	 * 
	 * Refer for details at <a href="https://tools.ietf.org/html/rfc3462">https://tools.ietf.org/html/rfc3462</a>.
	 * 
	 * @return   <code>true</code> when this mail has been send by server itself.
	 */
	public boolean isReportMail();
}
