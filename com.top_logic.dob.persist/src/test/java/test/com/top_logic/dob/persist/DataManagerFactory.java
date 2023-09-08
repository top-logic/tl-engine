/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.persist;

import java.sql.SQLException;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.dob.persist.DataManager;
import com.top_logic.dob.persist.NewDataManager;

/**
 * Factory interface for producing {@link DataManager}s for
 * {@link DataManagerTestSetup}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DataManagerFactory {
	
	/**
	 * Instantiates the (default) {@link NewDataManager}.
	 */
	public static final DataManagerFactory NEW_DATA_MANAGER_DEFAULT_INSTANCE = new DataManagerFactory() {
		@Override
		public DataManager createDataManager(ConnectionPool currentDB) throws SQLException {
			return DataManagerFactory.createDataManager(NewDataManager.class);
		}
	};

	/**
	 * Creates a {@link DataManager} that uses the given database connection.
	 * 
	 * @param currentDB
	 *        The database connection to use.
	 * @return The created data manager.
	 */
	DataManager createDataManager(ConnectionPool currentDB) throws SQLException;
	
	/**
	 * @param dataManagerClass
	 *        DataManager class object.
	 * @return The created data manager.
	 */
	static DataManager createDataManager(Class<? extends DataManager> dataManagerClass) {
		try {
			ServiceConfiguration<DataManager> serviceConfiguration =
				ApplicationConfig.getInstance().getServiceConfiguration(DataManager.class);
			ServiceConfiguration<DataManager> specialConfig = TypedConfiguration.copy(serviceConfiguration);
			String implementationClass = PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME;
			TypedConfigUtil.setProperty(specialConfig, implementationClass, dataManagerClass);

			return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(specialConfig);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

}