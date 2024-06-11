
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/** This class manages all Knowledgebases availeable in a TopLogic System.
 *<p>
 * It uses an entry from XMLProperties for every Knowledgebase.
 * When required it uses this entry to create a new Insatnce of a 
 * Knowledgebase, Knowledgebases are created on demand.
 *</p>
 *
 * @author  Klaus Halfmann / Jörg Connotte
 */
@ServiceDependencies({ ConnectionPoolRegistry.Module.class,
	ApplicationConfig.Module.class,
	DataAccessService.Module.class,
	ClusterManager.Module.class,
	FlexDataManagerFactory.Module.class,
	SchedulerService.Module.class })
public class KnowledgeBaseFactory extends ManagedClass {

	/** The configurations of all {@link KnowledgeBase}s indexed by name. */
	private Map<String, KnowledgeBaseConfiguration> _kbConfigurations;

    /** The actual KnowledgeBases are found here, indexed by name. */
    private HashMap<String, KnowledgeBase>  _kbases;

    
    /** Get Instance of singleton */
    public static KnowledgeBaseFactory getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Creates a new KnowledgeBaseFactory.
	 * 
	 * @see ManagedClass#ManagedClass(InstantiationContext, ServiceConfiguration)
	 */
	@CalledByReflection
	public KnowledgeBaseFactory(InstantiationContext context, KnowledgeBaseFactoryConfig config) {
		super(context, config);
		_kbConfigurations = config.getKnowledgeBases();
		_kbases = MapUtil.newMap(_kbConfigurations.size());
	}

    /**
     * Gets a list with the names of all KnowledgeBases.
     *
     * @return  A List of Strings, the names of all KnowledgeBases.
     */
    public Collection<String> getKnowledgeBaseNames () {
		return Collections.unmodifiableCollection(_kbConfigurations.keySet());
    }

    /**
     * Gets a KnowledgeBaseInstance for a given 'name'.
     *<p>
     *  The KB is created in case it does not yet exist.
     *</p>
     * <p>
     *  This function is not synchronized since this will provoke
     *  a Deadlock in case of a loopback RemoteKnowledgeBase.
     *  A change in the Implementation of the Remote KB might solve this
     *  problem, later.
     * </p>
     * @param   aKnowledgeBaseName    The name of the KnowledgeBase.
     * @return  A KnowledgeBase matching to the name or null.
     *
     */
    public KnowledgeBase getKnowledgeBase (String aKnowledgeBaseName) {
        KnowledgeBase result = _kbases.get(aKnowledgeBaseName);
        if (result != null) {
        	return result;
        }
        
        Protocol protocol = new LogProtocol(KnowledgeBaseFactory.class);
		return initKnowledgeBase(aKnowledgeBaseName, protocol);
    }
    
    public KnowledgeBase getKnowledgeBase (String aKnowledgeBaseName, Protocol protocol) {
        KnowledgeBase result = _kbases.get(aKnowledgeBaseName);
        if (result != null) {
        	return result;
        }
        
        return initKnowledgeBase(aKnowledgeBaseName, protocol);
    }

	public boolean destroyKnowledgeBase(String aKnowledgeBaseName) {
		KnowledgeBase result = _kbases.remove(aKnowledgeBaseName);
		if (result == null) {
			return false;
		}

		shutdownKnowledgeBase(result);
		return true;
	}

	protected KnowledgeBase initKnowledgeBase(String aKnowledgeBaseName, Protocol protocol) {
		KnowledgeBaseConfiguration configuration = getKBConfig(aKnowledgeBaseName);
	    
	    KnowledgeBase result = createNamedKnowledgeBase(aKnowledgeBaseName, protocol, configuration);

	    startupKnowledgeBase(protocol, result);

	    // The following breaks test cases that try to set up the same knowledge
		// base multiple times (e.g. after creating tables). Seems to be a
		// conflict of combining knowledge base preload in test setups that
		// create tables.
	    // 
		//	    // Remove reference to Properties so GC may collect it    
		//	    kbProperties.remove(aKnowledgeBaseName);
	    
		return result;
	}

	/**
	 * Create a new configured {@link KnowledgeBase} that has not yet been
	 * {@link KnowledgeBase#startup(Protocol) started}.
	 * 
	 * <p>
	 * The resulting {@link KnowledgeBase} is added to the currently installed
	 * knowledge bases. The caller is responsible to call
	 * {@link #startupNamedKnowledgeBase(String, Protocol)} after e.g. setting
	 * up tables but before actually using the framework.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This method is for testing only.
	 * </p>
	 * 
	 * @param aKnowledgeBaseName
	 *        Name of the configured {@link KnowledgeBase}.
	 * @param protocol
	 *        Error output.
	 * @return a new {@link KnowledgeBase} instance corresponding to the
	 */
	public KnowledgeBase createNamedKnowledgeBase(String aKnowledgeBaseName, Protocol protocol) {
		KnowledgeBaseConfiguration configuration = getKBConfig(aKnowledgeBaseName);
	    return createNamedKnowledgeBase(aKnowledgeBaseName, protocol, configuration);
	}
	
	private KnowledgeBase createNamedKnowledgeBase(String aKnowledgeBaseName, Protocol protocol,
			KnowledgeBaseConfiguration configuration) {
		if (configuration == null) {
	    	throw protocol.fatal("Knowledge base '" + aKnowledgeBaseName + "' is not configured");
	    }
	    
	    KnowledgeBase result = createKnowledgeBase(protocol, configuration);
		protocol.checkErrors();

		// Publish before startup to give listeners (which are registered
		// during startup) a chance to find their knowledge base.
		addKnowledgeBase(result);
	    
		return result;
	}

	/**
	 * {@link KnowledgeBase#startup(Protocol) Starts up} the
	 * knowledge base with the given name.
	 * 
	 * <p>
	 * The knowledge base must have been created with a call to
	 * {@link #createNamedKnowledgeBase(String, Protocol)} before.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This method is for testing only.
	 * </p>
	 */
	public void startupNamedKnowledgeBase(String aKnowledgeBaseName, Protocol protocol) {
		KnowledgeBase kbase = _kbases.get(aKnowledgeBaseName);

		startupKnowledgeBase(protocol, kbase);
	}

	public KnowledgeBaseConfiguration getKBConfig(String aKnowledgeBaseName) {
		return _kbConfigurations.get(aKnowledgeBaseName);
	}
	
	private void startupKnowledgeBase(Protocol protocol, KnowledgeBase result) {
		result.startup(protocol);
		protocol.checkErrors();
	}

	private void shutdownKnowledgeBase(KnowledgeBase result) {
		result.shutDown();
	}

	/**
	 * Creates a new configured {@link KnowledgeBase} instance without without
	 * registering the result within this factory and even without setting up
	 * this factory at all.
	 * 
	 * <p>
	 * The result is {@link KnowledgeBase#initialize(Protocol, KnowledgeBaseConfiguration)
	 * initialized} but has not yet been
	 * {@link KnowledgeBase#startup(Protocol) started} and can
	 * therefore be used for creating tables.
	 * </p>
	 * 
	 * @param protocol
	 *        Error output.
	 * @param configuration
	 *        The knowledge base configuration.
	 * @return The new knowledge base instance.
	 */
	public static KnowledgeBase createKnowledgeBase(Protocol protocol, KnowledgeBaseConfiguration configuration) {
		KnowledgeBase result = new DBKnowledgeBase();
		result.initialize(protocol, configuration);
		return result;
	}

	/**
     * Allow dynamic addition of a new KnowledgeBases.
     * 
     * This will not allow setting of the defaultKB.
     * Existing KBs with the same name will be overridden,
     * (which will confuse most of the TLSystems). 
     * 
     * @return the KB previously found using that name, should always be null.
     */
	@CalledByReflection
    protected KnowledgeBase addKnowledgeBase(KnowledgeBase aBase) {
        
        String        theName    = aBase.getName();
        return _kbases.put(theName, aBase);
    }
    
	@Override
	protected void shutDown() {
		for (KnowledgeBase kb : _kbases.values().toArray(new KnowledgeBase[0])) {
			kb.shutDown();
		}
		_kbases = null;
		super.shutDown();
	}

	public static final class Module extends TypedRuntimeModule<KnowledgeBaseFactory> {
    	
		public static final Module INSTANCE = new Module();

		@Override
		public Class<KnowledgeBaseFactory> getImplementation() {
			return KnowledgeBaseFactory.class;
		}

	}

}
