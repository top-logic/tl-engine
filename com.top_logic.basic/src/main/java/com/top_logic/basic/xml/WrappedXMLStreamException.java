/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import javax.xml.stream.XMLStreamException;

/**
 * Wrapper to transport a {@link XMLStreamException} out of a method that cannot
 * declare the exception.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class WrappedXMLStreamException extends RuntimeException {

	/**
	 * Creates a {@link WrappedXMLStreamException}.
	 * 
	 * @param ex
	 *        See {@link #getStreamException()}.
	 */
	public WrappedXMLStreamException(XMLStreamException ex) {
		super(ex);
	}
	
	/**
	 * The wrapped {@link XMLStreamException}.
	 */
	public XMLStreamException getStreamException() {
		return (XMLStreamException) getCause();
	}
	
	/**
	 * Re-throws the wrapped {@link XMLStreamException}.
	 * 
	 * @see #getStreamException()
	 */
	public void unwrap() throws XMLStreamException {
		throw getStreamException();
	}

	/**
	 * Wraps the given given {@link XMLStreamException} into a
	 * {@link WrappedXMLStreamException}.
	 * 
	 * @param ex
	 *        The exception to wrap.
	 * @return The wrapped exception.
	 */
	public static WrappedXMLStreamException wrap(XMLStreamException ex) {
		return new WrappedXMLStreamException(ex);
	}

}