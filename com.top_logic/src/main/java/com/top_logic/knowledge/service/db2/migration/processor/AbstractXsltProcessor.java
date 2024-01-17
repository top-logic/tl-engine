/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.processor;

import java.io.StringReader;
import java.sql.SQLException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XsltUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * Superclass for {@link MigrationProcessor} that changes an XML value by applying an XSLT
 * transformation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractXsltProcessor<C extends AbstractXsltProcessor.Config<?>, I>
		extends AbstractConfiguredInstance<C> implements MigrationProcessor {

	/**
	 * Configuration options for {@link AbstractXsltProcessor}.
	 */
	public interface Config<I extends AbstractXsltProcessor<?, ?>> extends PolymorphicConfiguration<I> {

		/**
		 * The XSLT resource.
		 * 
		 * <p>
		 * The value is a resource name starting with a '/' character rooted at the web application
		 * root folder. It typically starts with the prefix <code>/WEB-INF/kbase/migration/</code>.
		 * </p>
		 * 
		 * @see FileManager#getStream(String)
		 */
		@Mandatory
		@Name("transform")
		String getTransform();

	}

	/**
	 * Creates a {@link AbstractXsltProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractXsltProcessor(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String oldXml;
		try {
			oldXml = readOldValue(connection);
		} catch (SQLException ex) {
			log.error("Unable to read baseline to migrate.", ex);
			return;
		}

		if (StringServices.isEmpty(oldXml)) {
			log.info("No stored baseline, ignoring transformation.");
			return;
		}

		try {
			Config<?> config = getConfig();
			String transform = config.getTransform();
			log.info("Applying '" + transform + "' to stored baseline.");

			Transformer transformer = XsltUtil.createTransformer(transform, false);

			Document transformed = DOMUtil.newDocument();
			transformer.transform(new StreamSource(new StringReader(oldXml)), new DOMResult(transformed));

			String newXml = transformedToString(transformed);

			storeNewValue(connection, newXml);
		} catch (TransformerException | SQLException ex) {
			log.error("Cannot migrate baseline.", ex);
		}
	}

	/**
	 * Serializes the transformed {@link Document}.
	 * 
	 * @param transformed
	 *        The {@link Document} transformed by the XSLT.
	 * @return The String representation to write in
	 *         {@link #storeNewValue(PooledConnection, String)}.
	 */
	protected String transformedToString(Document transformed) {
		return DOMUtil.toString(transformed, true, false);
	}

	/**
	 * Stores the new transformed value.
	 * 
	 * @param connection
	 *        Connection to database.
	 * @param newValue
	 *        Transformed value to store.
	 * @throws SQLException
	 *         iff access to database failed.
	 */
	protected abstract void storeNewValue(PooledConnection connection, String newValue) throws SQLException;

	/**
	 * Reads the value to transform.
	 * 
	 * @param connection
	 *        Connection to database.
	 * @return The value in the database to migrate.
	 * @throws SQLException
	 *         iff access to database failed.
	 */
	protected abstract String readOldValue(PooledConnection connection) throws SQLException;

}
