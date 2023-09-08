/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Abstract analyze service that defines the base functionality and abstract 
 * methods common to all analyze services.
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
@ServiceDependencies({
	KnowledgeBaseFactory.Module.class,
	PersistencyLayer.Module.class
})
public class DefaultAnalyzeService<C extends DefaultAnalyzeService.Config<?>> extends ConfiguredManagedClass<C>
		implements AnalyzeService {
    
    /** The knowledge base to be used. */
    protected final KnowledgeBase kbase;

	/**
	 * Configuration for {@link DefaultAnalyzeService}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config<I extends DefaultAnalyzeService<?>> extends ConfiguredManagedClass.Config<I> {
		/**
		 * Name of the knowledge base.
		 */
		String getKnowledgeBaseName();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link DefaultAnalyzeService}.
	 */
	public DefaultAnalyzeService(InstantiationContext context, C config) {
		super(context, config);

		this.kbase = createKnowledgeBase(config.getKnowledgeBaseName());
    }

    
    /**
     * We don't want this Service to be used for default implementation
     * 
     * @see com.top_logic.knowledge.analyze.AnalyzeService#serviceAvailable()
     */
    @Override
	public boolean serviceAvailable() {
        return false;
    }

    /**
     * Get the configured knowledge base for the knowledge objects to be analyzed.
     * 
     * @see AnalyzeService#getKnowledgeBase()
     */
    @Override
	public KnowledgeBase getKnowledgeBase() {
        return (this.kbase);
    }

	/**
	 * Actual knowledge base.
	 */
	public static KnowledgeBase createKnowledgeBase(String theKBName) {
		if (StringServices.isEmpty(theKBName)) {
		    return PersistencyLayer.getKnowledgeBase();
		}
		else {
			return KnowledgeBaseFactory.getInstance().
		                                      getKnowledgeBase(theKBName);
		}
	}
    

    /**
     * @see com.top_logic.knowledge.analyze.AnalyzeService#findSimilar(KnowledgeObject)
     * 
     * @return always an empty Collection.
     */
    @Override
	public Collection<? extends KnowledgeObject> findSimilar (KnowledgeObject document) 
        throws AnalyzeException, UnsupportedOperationException {
    
		return Collections.emptyList();
    }
    
    /**
     * @see com.top_logic.knowledge.analyze.AnalyzeService#findSimilarRanked(KnowledgeObject)
     * 
     * @return always an empty Collection.
     */
    @Override
	public Collection<? extends KnowledgeObjectResult> findSimilarRanked (KnowledgeObject aDoc)
        throws AnalyzeException, UnsupportedOperationException {
            
		return Collections.emptyList();
    }
    
    /**
     * @see com.top_logic.knowledge.analyze.AnalyzeService#extractKeywords(KnowledgeObject)
     * 
     * @return always an empty Collection.
     */
    @Override
	public Collection<String> extractKeywords (KnowledgeObject document)
        throws AnalyzeException, UnsupportedOperationException {

		return Collections.emptyList();
    }
    
    public static final boolean isAvailable() {
    	return Module.INSTANCE.isActive() && Module.INSTANCE.getImplementationInstance().serviceAvailable();
    }
    
    public static AnalyzeService getAnalyzeService() {
    	if (!isAvailable()) {
    		throw new AnalyzeException("Analyze service is not available.");
    	}
    	return Module.INSTANCE.getImplementationInstance();
    }
    
	/**
	 * {@link TypedRuntimeModule} for access to the {@link DefaultAnalyzeService}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<DefaultAnalyzeService> {
		
		/**
		 * Sole module instance.
		 */
		public static final Module INSTANCE = new Module();
		
		@Override
		public Class<DefaultAnalyzeService> getImplementation() {
			return DefaultAnalyzeService.class;
		}
		
	}
	
}
