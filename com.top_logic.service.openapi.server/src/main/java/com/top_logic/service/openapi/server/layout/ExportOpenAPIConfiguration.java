/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.layout;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.utils.OpenAPIServerUtils;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} exporting the configured server API in <i>OpenAPI</i> format.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExportOpenAPIConfiguration extends AbstractCommandHandler {

	/**
	 * Configuration for the {@link ExportOpenAPIConfiguration}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * The format of the export.
		 */
		@Mandatory
		OpenAPIExporter.ExportFormat getExportFormat();

	}

	/**
	 * Creates a new {@link ExportOpenAPIConfiguration}.
	 */
	public ExportOpenAPIConfiguration(InstantiationContext context, Config config) {
		super(context, config);
	}

	Config config() {
		return (Config) getConfig();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		OpenAPIExporter exporter = new OpenAPIExporter(openAPIServerConfig());
		exporter.setExportFormat(config().getExportFormat());
		exporter.createDocument(aContext);

		aContext.getWindowScope().deliverContent(new BinaryDataSource() {

			@Override
			public String getName() {
				return exporter.getExportFileName();
			}

			@Override
			public long getSize() {
				return -1;
			}

			@Override
			public String getContentType() {
				return JsonUtilities.JSON_CONTENT_TYPE_HEADER;
			}

			@Override
			public void deliverTo(OutputStream out) throws IOException {
				exporter.deliverTo(out);
			}
		});
		return HandlerResult.DEFAULT_RESULT;
	}

	private OpenApiServer.Config<?> openAPIServerConfig() {
		try {
			return OpenAPIServerUtils.storedOpenAPIServerConfig();
		} catch (ConfigurationException ex) {
			throw new TopLogicException(I18NConstants.ACCESSING_SERVER_CONFIGURATION_FAILED, ex);
		}
	}

}
