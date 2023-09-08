/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.dsa.evt.DataChangeListener;
import com.top_logic.dsa.ex.UnknownDBException;

/**
 * Implements the interface for the data access service.
 *
 * @author  Karsten Buch
 */
public class DataAccessService extends ConfiguredManagedClass<DataAccessService.Config> {

	/**
	 * Configuration of the {@link DataAccessService}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<DataAccessService> {

		/** Configuration name of {@link #getDataSourceAdaptors}. */
		String DATA_SOURCE_ADAPTORS_NAME = "adaptors";

		/**
		 * All {@link DataSourceAdaptor} indexed by the registration name.
		 */
		@Key(DataSourceAdaptorConfig.NAME_ATTRIBUTE)
		@Name(DATA_SOURCE_ADAPTORS_NAME)
		Map<String, DataSourceAdaptorConfig> getDataSourceAdaptors();
	}

	/**
	 * Configuration of a single {@link DataSourceAdaptor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface DataSourceAdaptorConfig extends NamedConfigMandatory {

		/**
		 * The actual configuration of the adaptor
		 */
		@Mandatory
		PolymorphicConfiguration<? extends DataSourceAdaptor> getConfig();

		/**
		 * The {@link DataChangeListener} for the configured adaptor.
		 */
		List<PolymorphicConfiguration<? extends DataChangeListener>> getListeners();

	}

	/** The {@link Map} that holds the registered DSAs. */
	private final ConcurrentHashMap<String, DataSourceAdaptor> _dbTable;

	/**
	 * Creates a new {@link DataAccessService} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DataAccessService}.
	 * 
	 */
	public DataAccessService(InstantiationContext context, Config config) {
		super(context, config);

		_dbTable = new ConcurrentHashMap<>();
	}

    /** Return String Collection of all registered Protocols (used for Testing) */
	public Collection<String> getProtocols() {
		return _dbTable.keySet();
    } 
    
    // Implementation the interface DataAccessService

    /**
     * Registers a new DataSourceAdaptor for use
     *
     * @param       datasourceID    name under which the DataSourceAdaptor should be registered
     * @param       datasource      the DataSourceAdaptor to be registered
     * @exception   DatabaseAccessException    if the DataSourceAdaptor is already registered
     */
    public void registerDataSource (String datasourceID, DataSourceAdaptor datasource)
                throws DatabaseAccessException {
		DataSourceAdaptor clash = MapUtil.putIfAbsent(_dbTable, datasourceID, datasource);

		if (clash != datasource) {
			throw new DatabaseAccessException(
				"registerDataSource(" + datasourceID + ", " + datasource + ") :duplicate registration");
        }
		datasource.setProtocol(datasourceID);
    }

    /**
     * Unregisters a DataSourceAdaptor
     *
     * @param       datasourceID        the name of the DataSourceAdaptor to be unregistered
     * @exception   UnknownDBException  if the DataSourceAdaptor is not registered
     */
    public void unregisterDataSource (String datasourceID)
            throws UnknownDBException {
		DataSourceAdaptor db = _dbTable.remove(datasourceID);
        if (db == null) {
            throw new UnknownDBException ("unregisterDataSource (" + datasourceID + "): unknown datasource");
        }
    }

    /**
     * Helper method to get the DataSourceAdaptor 
     * registered under the given protocol.
     * e.g. for the parameter: mail://[MailConfig1]INBOX --> mail is the protocol
     *
     *
     * @param   aProtocol  part of the DNS before the :// Separator.
     * @return  the DataSourceAdaptor that is assoziated with the given protocol
     *
     * @throws UnknownDBException in case no such DSA is registered.
     */
    public DataSourceAdaptor getDataSourceByName (String aProtocol)
            throws UnknownDBException {
		DataSourceAdaptor db = _dbTable.get(aProtocol);
        if (db == null) {    // datasource name not in hashtable -> not registered
			DataSourceAdaptorConfig configForProtocol = getConfig().getDataSourceAdaptors().get(aProtocol);
			if (configForProtocol == null) {
				throw new UnknownDBException("The data source protocol '" + aProtocol + "' is not registered");
			}
			DataSourceAdaptor newDSA = newDSAFromConfig(configForProtocol);
			db = MapUtil.putIfAbsent(_dbTable, aProtocol, newDSA);
        }
        return db;
    }

	private DataSourceAdaptor newDSAFromConfig(DataSourceAdaptorConfig config) {

		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		DataSourceAdaptor dsa = context.getInstance(config.getConfig());
		dsa.setProtocol(config.getName());

		for (PolymorphicConfiguration<? extends DataChangeListener> listenerConf : config.getListeners()) {
			DataChangeListener listener = context.getInstance(listenerConf);
			if (Logger.isDebugEnabled(this)) {
				Logger.debug("Appending listener " + listener + " to " + dsa, DataAccessService.class);
			}
			dsa.addDataChangeListener(listener);

		}

		return dsa;
	}

	/**
     * Singleton pattern: get the instance, lazy initialisation
     *
     * @return  the instance
     */
    public static synchronized DataAccessService getInstance () {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
    /**
     * close() all DSA on shutdown 
     */
    @Override
    protected void shutDown() {
    	closeDSAs();
		super.shutDown();
    }

	private void closeDSAs() {
		Iterator<DataSourceAdaptor> dsas = _dbTable.values().iterator();
    	while (dsas.hasNext()) {
			DataSourceAdaptor dsa = dsas.next();
    		try {
    			dsa.close();
    		} catch (DatabaseAccessException e) {
    			Logger.error("failed to close " + dsa.getProtocol(), e , this);
    		}
			dsas.remove();
    	}
	}

	public static final class Module extends TypedRuntimeModule<DataAccessService> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<DataAccessService> getImplementation() {
			return DataAccessService.class;
		}

	}

}
