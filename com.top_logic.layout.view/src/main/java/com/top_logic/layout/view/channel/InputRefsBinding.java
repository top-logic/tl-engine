/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.config.AbstractConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Reads and writes a list of {@link InputRef}s as nested elements, one per channel.
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;inputs&gt;
 *   &lt;input channel="selection" as="ticket"/&gt;
 *   &lt;input channel="draft"/&gt;
 * &lt;/inputs&gt;
 * </pre>
 *
 * @see CommaSeparatedInputRefs
 */
public class InputRefsBinding extends AbstractConfigurationValueBinding<List<InputRef>> {

	/** Element name of an entry. */
	public static final String INPUT = "input";

	/** Attribute naming the channel of an entry. */
	public static final String CHANNEL = "channel";

	/** Attribute naming the argument of an entry. */
	public static final String AS = CommaSeparatedInputRefs.AS;

	@Override
	public List<InputRef> loadConfigItem(XMLStreamReader in, List<InputRef> baseValue)
			throws XMLStreamException, ConfigurationException {
		List<InputRef> result = baseValue == null ? new ArrayList<>() : new ArrayList<>(baseValue);
		while (in.nextTag() != XMLStreamConstants.END_ELEMENT) {
			if (!INPUT.equals(in.getLocalName())) {
				throw new ConfigurationException(I18NConstants.ERROR_UNEXPECTED_INPUT_ELEMENT__NAME_EXPECTED_LOCATION
					.fill(in.getLocalName(), INPUT, XMLStreamUtil.atLocation(in)));
			}
			String channel = in.getAttributeValue(null, CHANNEL);
			if (channel == null || channel.isBlank()) {
				throw new ConfigurationException(
					I18NConstants.ERROR_MISSING_INPUT_CHANNEL__ATTRIBUTE_LOCATION.fill(CHANNEL,
						XMLStreamUtil.atLocation(in)));
			}
			result.add(new InputRef(new ChannelRef(channel.trim()), in.getAttributeValue(null, AS)));
			XMLStreamUtil.nextEndTag(in);
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public void saveConfigItem(XMLStreamWriter out, List<InputRef> item) throws XMLStreamException {
		for (InputRef input : item) {
			out.writeEmptyElement(INPUT);
			out.writeAttribute(CHANNEL, input.getChannel().getChannelName());
			if (input.getAs() != null) {
				out.writeAttribute(AS, input.getAs());
			}
		}
	}

	@Override
	public List<InputRef> defaultValue() {
		return Collections.emptyList();
	}

	@Override
	public Object normalize(Object value) {
		return value == null ? Collections.emptyList() : value;
	}
}
