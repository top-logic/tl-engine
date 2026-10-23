/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.config;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.migration.StartupAction;
import com.top_logic.util.StartupHooks;

/**
 * Loads and updates the application's access configuration.
 *
 * <p>
 * The access configuration must be located at <code>WEB-INF/conf/security.xml</code>. It is
 * imported, when its content has changed since the last import.
 * </p>
 *
 * <p>
 * The import must run as {@link StartupHooks startup hook}, because it may require any service of
 * the application.
 * </p>
 *
 * @see AccessConfiguration
 */
public class AccessConfigurationSetup implements StartupAction {

	private static final String DB_PROPERTY = "setup-access-configuration";

	private static final String DATA_PATH = "/WEB-INF/conf/security.xml";

	@Override
	public void perform() {
		BinaryData accessData = FileManager.getInstance().getDataOrNull(DATA_PATH);
		if (accessData == null) {
			Logger.info("No access configuration.", AccessConfigurationSetup.class);
			return;
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		String dataHash = hash(accessData);
		String hash = storedHash(kb);
		if (hash != null && hash.equals(dataHash)) {
			Logger.info("Access configuration unchanged.", AccessConfigurationSetup.class);
			return;
		}

		AccessConfiguration config;
		try {
			config = (AccessConfiguration) ConfigurationReader.readContent(
				SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				Collections.singletonMap("security",
					TypedConfiguration.getConfigurationDescriptor(AccessConfiguration.class)),
				accessData);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}

		new AccessImporter().importAccessConfig(config);

		PooledConnection commitCon = KBUtils.getCurrentContext(kb).getConnection();
		try {
			DBProperties.setProperty(commitCon, DBProperties.GLOBAL_PROPERTY, DB_PROPERTY, dataHash);
		} catch (SQLException ex) {
			Logger.error("Cannot mark configuration as applied.", ex, AccessConfigurationSetup.class);
		}
	}

	private static String storedHash(KnowledgeBase kb) {
		ConnectionPool pool = KBUtils.getConnectionPool(kb);
		PooledConnection readCon = pool.borrowReadConnection();
		try {
			return DBProperties.getProperty(readCon, DBProperties.GLOBAL_PROPERTY, DB_PROPERTY);
		} catch (SQLException ex) {
			Logger.error("Cannot load configuration hash.", ex, AccessConfigurationSetup.class);
			return null;
		} finally {
			pool.releaseReadConnection(readCon);
		}
	}

	/**
	 * Computes an MD5 hash of the given {@link BinaryContent} and returns it as a Base64-encoded
	 * string.
	 *
	 * <p>
	 * The hash is used to detect whether the content has changed since it was last processed, so
	 * that re-importing can be skipped when the file is unchanged.
	 * </p>
	 *
	 * @param data
	 *        The content to hash.
	 * @return Base64-encoded MD5 digest of the content, or <code>null</code> if an error occurred
	 *         while reading the data.
	 */
	public static String hash(BinaryContent data) {
		try {
			MessageDigest digest = MessageDigest.getInstance("md5");
			byte[] buffer = new byte[4096];
			try (InputStream in = data.getStream()) {
				while (true) {
					int direct = in.read(buffer);
					if (direct < 0) {
						break;
					}
					digest.update(buffer, 0, direct);
				}
			}
			return Base64.getEncoder().encodeToString(digest.digest());
		} catch (IOException | NoSuchAlgorithmException ex) {
			Logger.error("Cannot hash data.", ex, AccessConfigurationSetup.class);
			return null;
		}
	}

}
