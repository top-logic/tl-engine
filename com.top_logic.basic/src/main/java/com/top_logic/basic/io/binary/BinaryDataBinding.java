/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.config.AbstractConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.xml.NewLineStyle;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.XmlTextWriter;

/**
 * {@link ConfigurationValueBinding} for {@link BinaryDataSource}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BinaryDataBinding extends AbstractConfigurationValueBinding<BinaryDataSource> {

	/**
	 * Here is not default Mime-Encoder ({@link Base64#getMimeEncoder()}) used, because is always
	 * use linebreak "\r\n" to separate blocks. Better use the system linebreak.
	 */
	private static final Encoder MIME_ENCODER = Base64.getMimeEncoder(76, NewLineStyle.SYSTEM.getChars().getBytes());

	private static final String NAME_ATTR = "name";
	private static final String CONTENT_TYPE_ATTR = "content-type";

	/**
	 * Singleton {@link BinaryDataBinding} instance.
	 */
	public static final BinaryDataBinding INSTANCE = new BinaryDataBinding();

	private BinaryDataBinding() {
		// Singleton constructor.
	}

	@Override
	public BinaryDataSource loadConfigItem(XMLStreamReader in, BinaryDataSource baseValue)
			throws XMLStreamException, ConfigurationException {
		String name = in.getAttributeValue(null, NAME_ATTR);
		String contentType = in.getAttributeValue(null, CONTENT_TYPE_ATTR);
		String contents = XMLStreamUtil.nextText(in);

		return BinaryDataFactory.createBinaryData(Base64.getMimeDecoder().decode(contents), contentType, name);
	}

	@Override
	public void saveConfigItem(XMLStreamWriter out, BinaryDataSource item) throws XMLStreamException {
		out.writeAttribute(NAME_ATTR, item.getName());
		out.writeAttribute(CONTENT_TYPE_ATTR, item.getContentType());
		try (OutputStream mime = MIME_ENCODER.wrap(new AsciiOutputStream(new XmlTextWriter(out)))) {
			item.deliverTo(mime);
		} catch (IOException ex) {
			throw new XMLStreamException(ex);
		}
	}

}
