/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Document;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLCompare;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Compares an XML file for semantically matching an expected value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLMatch extends AbstractFileEqualityCheck {

	/**
	 * Create a {@link XMLMatch} from configuration.
	 */
	public XMLMatch(InstantiationContext context, FileMatchConfig config) {
		super(context, config);
	}

	@Override
	protected void checkContents(BinaryData actual, BinaryData expected) throws IOException {
		try {
			Document actualXML = DOMUtil.parseStripped(actual.getStream());
			Document expectedXML = DOMUtil.parseStripped(expected.getStream());

			BufferingProtocol log = new BufferingProtocol();
			log.setVerbosity(Protocol.DEBUG);
			new XMLCompare(log, false, FilterFactory.trueFilter()).assertEqualsNode(expectedXML, actualXML);

			if (log.hasErrors()) {
				ApplicationAssertions.fail(_config, "Contents does not match: " + log.getError());
			}

		} catch (XMLStreamException ex) {
			ApplicationAssertions.fail(_config, "Invalid XML.", ex);
		}
	}

}
