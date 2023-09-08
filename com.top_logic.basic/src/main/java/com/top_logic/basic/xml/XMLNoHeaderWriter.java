/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * {@link XMLStreamWriter} that writes no XML header.
 * 
 * @author     <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class XMLNoHeaderWriter extends XMLStreamWriterProxy {

	/**
	 * Creates a {@link XMLNoHeaderWriter}.
	 */
	public XMLNoHeaderWriter(XMLStreamWriter impl) {
		super(impl);
	}
	
	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		//No XML declaration
	}
	
	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		//No XML declaration
	}
	
	@Override
	public void writeStartDocument() throws XMLStreamException {
		//No XML declaration
	}
}
