/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.bpe.bpml.exporter.BPMLExporter;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command exporting the selected {@link Collaboration} as BPML XML.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExportBPMLDefinition extends AbstractCommandHandler {

	/**
	 * Creates a {@link ExportBPMLDefinition} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExportBPMLDefinition(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {

		Collaboration collaboration = (Collaboration) model;

		BinaryDataSource data = new BinaryDataSource() {
			@Override
			public String getName() {
				return collaboration.getName() + ".bpml";
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
					XMLStreamWriter xout =
						XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(out, StringServices.UTF8);
					BPMLExporter exporter = new BPMLExporter(xout, true);
					exporter.exportBPML(collaboration);
				} catch (XMLStreamException ex) {
					throw new IOException(ex);
				}
			}
		};

		aContext.getWindowScope().deliverContent(data);
		return HandlerResult.DEFAULT_RESULT;
	}

}
