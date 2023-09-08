/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.vars.IVariableExpander;

/**
 * {@link XMLStreamReader} based on an other {@link XMLStreamReader} which
 * resolved aliases in the attribute values.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AliasedXMLReader extends XMLStreamReaderAdapter {

	public static XMLStreamReader createAliasedXMLReader(XMLStreamReader reader, IVariableExpander expander) {
		return new AliasedXMLReader(reader, expander);
	}
	
	public static XMLStreamReader createAliasedXMLReader(XMLStreamReader reader, final String systemId,
			IVariableExpander expander) {
		if (reader.getClass().getName().equals("com.bea.xml.stream.MXParser")) {
			// bug in MXParser which always returns itself as location and null as systemId
			return new AliasedXMLReader(reader, expander) {
				
				@Override
				public Location getLocation() {
					final Location location = super.getLocation();
					return new Location() {
						
						@Override
						public String getSystemId() {
							return systemId;
						}
						
						@Override
						public String getPublicId() {
							return location.getPublicId();
						}
						
						@Override
						public int getLineNumber() {
							return location.getLineNumber();
						}
						
						@Override
						public int getColumnNumber() {
							return location.getColumnNumber();
						}
						
						@Override
						public int getCharacterOffset() {
							return location.getCharacterOffset();
						}
					};
				}
			};
		}
		return createAliasedXMLReader(reader, expander);
	}

	private final IVariableExpander _expander;

	public AliasedXMLReader(XMLStreamReader reader, IVariableExpander expander) {
		super(reader);
		_expander = expander;
	}

	@Override
	public String getAttributeValue(int index) {
		return resolveAlias(super.getAttributeValue(index));
	}

	private String resolveAlias(String attributeValue) {
		return _expander.expand(attributeValue);
	}

	@Override
	public String getAttributeValue(String namespaceUri, String localName) {
		return resolveAlias(super.getAttributeValue(namespaceUri, localName));
	}

	@Override
	public String getElementText() throws XMLStreamException {
		return resolveAlias(super.getElementText());
	}

}
