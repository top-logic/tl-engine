/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.ddlimporter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCommandHandler} importing a model from an existing database.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DDLImporter extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link DDLImporter}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Name of the connection pool to import data from.
		 */
		@Mandatory
		@Name("poolName")
		String getPoolName();

	}

	/**
	 * Creates a {@link DDLImporter}.
	 */
	public DDLImporter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {

		ConnectionPool pool = ConnectionPoolRegistry.getConnectionPool(((Config) getConfig()).getPoolName());
		PooledConnection connection = pool.borrowReadConnection();
		try {
			DBSchema schema = DBSchemaUtils.extractSchema(pool);

			BinaryDataSource xml = new BinaryDataSource() {
				@Override
				public String getName() {
					return "schema.xml";
				}

				@Override
				public long getSize() {
					return -1;
				}

				@Override
				public String getContentType() {
					return "application/xml";
				}

				@Override
				public void deliverTo(OutputStream out) throws IOException {
					try (OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
						ConfigurationWriter writer = new ConfigurationWriter(w);
						writer.write("schema", DBSchema.class, schema);
					} catch (XMLStreamException ex) {
						Logger.error("Cannot write schema.", ex, DDLImporter.class);
					}
				}
			};
			aContext.getWindowScope().deliverContent(xml);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			pool.releaseReadConnection(connection);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

}
