/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command handler that exports all instances of a configured TL type to an XML file in the
 * application's <code>WEB-INF/data/</code> folder.
 *
 * <p>
 * This is intended for development workflows where persistent object definitions need to be
 * exported back into the source tree.
 * </p>
 */
public class ExportToDataFolderCommand extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link ExportToDataFolderCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Qualified TL type name in the form <code>module:type</code>, e.g.
		 * <code>tl.ai.core:Agent</code>.
		 */
		@Mandatory
		String getTypeName();

		/**
		 * Output file name, e.g. <code>agents.objects.xml</code>.
		 */
		@Mandatory
		String getFileName();
	}

	/**
	 * Creates a new {@link ExportToDataFolderCommand}.
	 */
	public ExportToDataFolderCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Clears the {@code initial-data.<fileName>} flag in {@link DBProperties} so that
	 * {@code InitialDataSetupService} re-imports the file on the next startup.
	 */
	private void resetInitialDataFlag(String fileName) {
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		try {
			PooledConnection connection = pool.borrowWriteConnection();
			try {
				DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY,
					"initial-data." + fileName, null);
				connection.commit();
			} finally {
				pool.releaseWriteConnection(connection);
			}
		} catch (SQLException ex) {
			// Non-fatal: file was written, just won't auto-reimport on next restart.
		}
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		Config config = (Config) getConfig();
		String typeName = config.getTypeName();
		String fileName = config.getFileName();

		String[] parts = typeName.split(":");
		String moduleName = parts[0];
		String localTypeName = parts[1];

		TLClass type = MetaElementUtil.getGlobalType(moduleName, localTypeName);
		List<TLObject> instances = new ArrayList<>(MetaElementUtil.getAllInstancesOf(type, TLObject.class));
		instances.sort(Comparator.comparing(o -> (String) o.tValueByName("name")));

		XMLInstanceExporter exporter = new XMLInstanceExporter();
		exporter.addResolver(type, new NameBasedInstanceResolver());
		BinaryDataSource data = exporter.exportInstances(fileName, instances);

		File targetFile = FileManager.getInstance().getIDEFile("/WEB-INF/data/" + fileName);
		if (targetFile == null) {
			InfoService.showError(I18NConstants.EXPORT_TO_DEV_ENV_ERROR);
			return HandlerResult.DEFAULT_RESULT;
		}

		try (FileOutputStream out = new FileOutputStream(targetFile)) {
			data.deliverTo(out);
		} catch (IOException ex) {
			InfoService.showError(I18NConstants.EXPORT_TO_DEV_ENV_ERROR);
			return HandlerResult.DEFAULT_RESULT;
		}

		resetInitialDataFlag(fileName);

		InfoService.showInfo(I18NConstants.EXPORT_TO_DEV_ENV_SUCCESS);
		return HandlerResult.DEFAULT_RESULT;
	}
}
