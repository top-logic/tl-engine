/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import com.top_logic.layout.form.model.FormContext;

/**
 * {@link Exception} that signals an irrecoverable error due to a missing
 * request.
 * 
 * <p>
 * After a server-side timeout, the user must reload the page and check entered
 * values, because the missing request could have been an AJAX request that
 * transports form data. If the server would continue processing after dropping
 * a missing request, the client-side view of a form and the server-side
 * {@link FormContext} may be out of sync. A following submit could then store
 * inconsistent values that the user had not intended to enter.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RequestTimeoutException extends Exception {
  
    /** 
     * Creates a new {@link RequestTimeoutException} with the given message.
     * 
     * @see Exception#Exception(String)
     */
    public RequestTimeoutException(String message) {
        super(message); 
    } 
}
