/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
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

import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.util.LayoutBasedSecurity;
import com.top_logic.util.model.ModelService;

/**
 * {@link ManagedClass} loading and updating the application's access configuration.
 * 
 * <p>
 * The access configuration must be located at <code>WEB-INF/conf/security.xml</code>
 * </p>
 * 
 * @see AccessConfiguration
 */
@ServiceDependencies({
	SecurityComponentCache.Module.class,
	ModelService.Module.class,
	InitialGroupManager.Module.class,
	LayoutBasedSecurity.Module.class,
})
public class AccessConfigurationSetupService extends ManagedClass {

	private static final String DB_PROPERTY = "setup-access-configuration";
	private static final String DATA_PATH = "/WEB-INF/conf/security.xml";

	@Override
	protected void startUp() {
		BinaryData accessData = FileManager.getInstance().getDataOrNull(DATA_PATH);
		if (accessData == null) {
			Logger.info("No access configuration.", AccessConfigurationSetupService.class);
			return;
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		ConnectionPool pool = KBUtils.getConnectionPool(kb);
		PooledConnection readCon = pool.borrowReadConnection();

		String dataHash;
		try {
			String hash;
			try {
				hash = DBProperties.getProperty(readCon, DBProperties.GLOBAL_PROPERTY, DB_PROPERTY);
			} catch (SQLException ex) {
				Logger.error("Cannot load configuration hash.", ex, AccessConfigurationSetupService.class);
				hash = null;
			}

			dataHash = hash(accessData);
			if (hash != null && hash.equals(dataHash)) {
				Logger.info("Access configuration unchanged.", AccessConfigurationSetupService.class);
				return;
			}
		} finally {
			pool.releaseReadConnection(readCon);
		}

		try (Transaction tx = kb.beginTransaction(I18NConstants.ACCESS_CONFIG_IMPORT)) {
			CommitContext commitContext = KBUtils.getCurrentContext(kb);
			PooledConnection commitCon = commitContext.getConnection();

			try {
				AccessConfiguration config = (AccessConfiguration) ConfigurationReader
					.readContent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, Collections
					.singletonMap("security", TypedConfiguration.getConfigurationDescriptor(AccessConfiguration.class)),
					accessData);

				new AccessImporter().importAccessConfig(config);

				try {
					DBProperties.setProperty(commitCon, DBProperties.GLOBAL_PROPERTY, DB_PROPERTY, dataHash);
				} catch (SQLException ex) {
					Logger.error("Cannot mark configuration as applied.", ex, AccessConfigurationSetupService.class);
				}

				tx.commit();
			} catch (ConfigurationException | ConfigurationError | IllegalArgumentException ex) {
				Logger.error("Cannot load access configuration.", ex, AccessConfigurationSetupService.class);
			}
		}
	}

	private static String hash(BinaryData accessData) {
		try {
			MessageDigest digest = MessageDigest.getInstance("md5");
			byte[] buffer = new byte[4096];
			try (InputStream in = accessData.getStream()) {
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
			Logger.error("Cannot hash access configuration.", ex, AccessConfigurationSetupService.class);
			return null;
		}
	}

	/**
	 * Singleton holder for the {@link AccessConfigurationSetupService}.
	 */
	public static final class Module extends TypedRuntimeModule<AccessConfigurationSetupService> {

		/**
		 * Singleton {@link AccessConfigurationSetupService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<AccessConfigurationSetupService> getImplementation() {
			return AccessConfigurationSetupService.class;
		}
	}

}

