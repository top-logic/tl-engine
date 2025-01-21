/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCommandHandler} exporting the selected objects to XML for later re-import.
 * 
 * @see XMLInstanceExporter
 */
public class InstanceExportCommand extends AbstractCommandHandler {

	/**
	 * Creates a {@link InstanceExportCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InstanceExportCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {

		Collection<?> values = CollectionUtil.asList(model);
		if (values.isEmpty()) {
			return HandlerResult.DEFAULT_RESULT;
		}

		XMLInstanceExporter exporter = new XMLInstanceExporter();
		for (Object value : values) {
			if (value instanceof TLObject obj) {
				exporter.export(obj);
			}
		}
		ObjectsConf export = exporter.getExportConfig();

		aContext.getWindowScope().deliverContent(new BinaryDataSource() {
			@Override
			public String getName() {
				return "objects.xml";
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
						w.write("objects", ObjectsConf.class, export);
					}
					XMLPrettyPrinter.dump(out, DOMUtil.parse(buffer.toString()));
				} catch (XMLStreamException ex) {
					Logger.error("Failed to export objects.", ex, InstanceExportCommand.class);
					throw new IOException(ex);
				}
			}
		});

		return HandlerResult.DEFAULT_RESULT;
	}

}
