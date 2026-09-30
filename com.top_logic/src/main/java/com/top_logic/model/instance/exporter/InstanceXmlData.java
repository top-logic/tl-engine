/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.exporter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.model.instance.importer.schema.ObjectsConf;

/**
 * {@link BinaryData} containing serialized instances as XML.
 */
public class InstanceXmlData implements BinaryDataSource {
	private final String _downloadName;

	private final ObjectsConf _export;

	/** 
	 * Creates a {@link InstanceXmlData}.
	 */
	public InstanceXmlData(String downloadName, ObjectsConf export) {
		_downloadName = downloadName;
		_export = export;
	}

	@Override
	public String getName() {
		return _downloadName;
	}

	@Override
	public long getSize() {
		return -1;
	}

	@Override
	public String getContentType() {
		return "text/xml";
	}

	@Override
	public void deliverTo(OutputStream out) throws IOException {
		try {
			StringWriter buffer = new StringWriter();
			try (ConfigurationWriter w =
				new ConfigurationWriter(buffer)) {
				w.write("objects", ObjectsConf.class, _export);
			}
			XMLPrettyPrinter.dump(out, DOMUtil.parse(buffer.toString()));
		} catch (XMLStreamException ex) {
			Logger.error("Failed to export objects.", ex, InstanceXmlData.class);
			throw new IOException(ex);
		}
	}
}