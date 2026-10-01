/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.DefaultFullTextBuffer;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * Indexes document contents so that they can be found via full-text search.
 *
 * @author    Dieter Rothb&auml;cher
 */
@ServiceDependencies({
	KnowledgeBaseFactory.Module.class
})
@Label("Full-text indexing")
public abstract class DefaultIndexingService
		extends KBBasedManagedClass<DefaultIndexingService.DefaultIndexingServiceConfig>
		implements IndexingService, UpdateListener {

	/**
	 * Configuration for {@link DefaultIndexingService}
	 */
	public static interface DefaultIndexingServiceConfig extends KBBasedManagedClass.Config<DefaultIndexingService> {
		
		/**
		 * Set of KO MetaObject names to index.
		 */
		@ListBinding()
		List<String> getMetaObjects();
		
	}

	/**
	 * Create from Properties
	 */
	public DefaultIndexingService(InstantiationContext context, DefaultIndexingServiceConfig config) {
		super(context, config);
		Logger.info("Indexer listening for objects of the following types: "
			+ StringServices.toString(config.getMetaObjects(), ", "), DefaultIndexingService.class);
    }

	@Override
	protected void startUp() {
		super.startUp();

		kb().addUpdateListener(this);
	}

	@Override
	protected void shutDown() {
		kb().removeUpdateListener(this);
		super.shutDown();
	}

	@Override
	public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
		if (event.isRemote()) {
			return;
		}

		for (ObjectKey key : keysToIndex(event)) {
			this.index(sender.resolveObjectKey(key));
		}

		for (ObjectKey key : event.getDeletedObjectKeys()) {
			this.remove(key);
		}
    }

	/**
	 * The keys of the objects whose index entries must be built for the given event.
	 * 
	 * <p>
	 * These are the objects created or updated in the given event. Objects deleted in the given
	 * event must not be reported.
	 * </p>
	 * 
	 * @param event
	 *        The committed change.
	 * @return The keys of the objects to (re-)index.
	 */
	protected Collection<ObjectKey> keysToIndex(UpdateEvent event) {
		List<ObjectKey> result = new ArrayList<>(event.getCreatedObjectKeys());
		result.addAll(event.getUpdatedObjectKeys());
		return result;
	}

    /**
     * @see com.top_logic.knowledge.indexing.IndexingService#getKnowledgeBase()
     */
    @Override
	public KnowledgeBase getKnowledgeBase() {
		return kb();
    }
    
	@Override
	public Collection<String> getTypesToIndex() {
		return new ArrayList<>(getConfig().getMetaObjects());
    }
    
    /**
     * TODO unable to handle all types (this.indexedKOTypes == null)
     * 
     * @see com.top_logic.knowledge.indexing.IndexingService#reindex(Collection, Collection, boolean)
     */
	@Override
	public int reindex(Collection<String> typesToIndex, Collection<Wrapper> objectsToIndex, boolean onlyNew)
			throws IndexException {
		String messageStartReindexing = "Start reindexing search index. Only new: " + onlyNew
			+ "; Number of additional objects: " + (objectsToIndex == null ? 0 : objectsToIndex.size())
			+ "; Types: " + typesToIndex;
		Logger.info(messageStartReindexing, DefaultIndexingService.class);

        KnowledgeBase  theKB = this.getKnowledgeBase();
        int count = 0;
        
        // Handle types
		Collection<String> theTypes = getConfig().getMetaObjects();
        if (typesToIndex != null) {
        	theTypes = typesToIndex;
        }
        
		List<Wrapper> allObjectsToIndex = new ArrayList<>();
        if (objectsToIndex != null) {
            allObjectsToIndex.addAll(objectsToIndex);
        }
        
        if (theTypes != null) {
			for (String theType : theTypes) {
				allObjectsToIndex.addAll(WrapperFactory.getWrappersByType(theType, theKB));
	        }
        }

		for (Object theObject : allObjectsToIndex) {
			ContentObject content;
			ObjectKey key;
	        if (theObject instanceof Wrapper) {
				key = getObjectKey((Wrapper) theObject);
	            content = createContent((Wrapper) theObject);
	        }
	        else if (theObject instanceof KnowledgeObject) {
				key = ((KnowledgeObject) theObject).tId();
	            content = createContent((KnowledgeObject) theObject);
	        }
			else {
				String message = "Unexpected type: '" + theObject.getClass()
					+ "' Don't know how to index! Object will be skipped. Index will be incomplete!";
				Logger.error(message, DefaultIndexingService.class);
				continue;
			}
	        
			if (onlyNew && containsContent(key)) {
				continue;
	        }
			this.indexContent(content);
			count++;
	    }
	    
		String messageFinishedReindexing = "Finished reindexing search index. Number of objects indexed: " + count;
		Logger.info(messageFinishedReindexing, DefaultIndexingService.class);
	    return count;
    }

	/**
	 * Re-index all objects.
	 * 
	 * @return the number of indexed objects
	 */
	public int reindexAll() {
		return reindex(null, null, false);
	}
    
    /**
     * Index the object with this indexing service .
     * 
     * @param   aDoc     the object to be indexed
     */
    protected void index(DataObject aDoc) {
        // will only index KOs
        if (!(aDoc instanceof KnowledgeObject)) {
            return;
        }

        KnowledgeObject theKO = (KnowledgeObject) aDoc;
        
		if (!getConfig().getMetaObjects().contains(theKO.tTable().getName())) {
			return; // ignore unwanted MO-types early
        }
        
        // ===============================!!!!!!!!!!!!!
        // TODO KHA fetching the wrapper at this point may cause deadlocks
        //          use background thread to do the actual work
        
        // try to resolve associated wrapper
        try {
            Wrapper theWrap = WrapperFactory.getWrapper(theKO);
            if (this.allowWrapper(theWrap)) { // check if wrapper is allowed to be indexed
                this.indexContent(createContent(theWrap));
            }
        } 
        catch (IndexException ix) {
            Logger.error("Failed to index '" + theKO + "'.", ix, this);
        }
    }
    
    
    /** 
     * Check if indexing is allowed for given Wrapper.
     *
     * @param   aWrapper   The wrapper to check
     * @return true if indexing allowed
     */
    protected boolean allowWrapper(Wrapper aWrapper) {
        return true; // Default implementation
    }
    
    /**
	 * Remove the document from this indexing service.
	 * 
	 * By default only KOs will be removed from the index since indexing is only done for KOs.
	 * 
	 * @param key
	 *        The identifier of the object to be removed from the index
	 */
	protected void remove(ObjectKey key) {
        try {
			// Ignore MO-types early that are not indexed.
			if (!getConfig().getMetaObjects().contains(key.getObjectType().getName())) {
				return;
			}
			this.removeContent(key);
        }
        catch (IndexException ie) {
            Logger.error("Index operation 'remove(aDoc)' failed.", ie, this);
        }
    }
    
	@Override
	public void rebuildIndex() {
		Logger.info("Start rebuilding search index", DefaultIndexingService.class);
		resetIndex();
		reindex(null, null, false);
		Logger.info("Finished rebuilding search index", DefaultIndexingService.class);
	}

	private static ObjectKey getObjectKey(Wrapper wrapper) {
		return wrapper.tHandle().tId();
	}

	public static ContentObject createContent(Wrapper wrapper) {
		return new WrapperContentObject(wrapper);
    }
    
	/**
	 * Creates the {@link ContentObject} of the given {@link KnowledgeObject}, see
	 * {@link #createContent(Wrapper)}.
	 */
	public static ContentObject createContent(KnowledgeObject ko) {
		return createContent((Wrapper) WrapperFactory.getWrapper(ko));
	}
    
    public static ContentObject createContent(KnowledgeObject object, String content, String description) {
        return new DefaultContentObject(object, content, description);
    }
    
    private static abstract class AbstractContentObject implements ContentObject {
        
        /** The KO to be indexed */
        private final KnowledgeObject object;
        
        public AbstractContentObject(KnowledgeObject ko) {
            this.object = ko;
        }
        
        @Override
		public final KnowledgeObject getKnowledgeObject() {
            return this.object;
        }
        
		@Override
        public String toString() {
            return (this.getClass().getName() + " [" 
                    + ", object: " + this.getKnowledgeObject()
                    + "]");
        }
        
		@Override
        public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
            if (obj instanceof ContentObject) {
                return this.object.equals(((ContentObject) obj).getKnowledgeObject());
            }
            return  this.object.equals(obj);
        }
        
		@Override
        public int hashCode() {
			return this.object.tId().hashCode();
        }
    }
    
    private static class DefaultContentObject extends AbstractContentObject {


        /** The content of the KO to be indexed */
        private final String content;

        /** The description of the KO to be indexed */
        private final String description;

        /**
         * Constructor
         * 
         * @param   anObject          The KO to be indexed
         * @param   someContent       The content of the KO to be indexed
         * @param   someDescription   The description of the KO to be indexed
         */
        private DefaultContentObject(KnowledgeObject anObject, 
                             String someContent,
                             String someDescription) {
            super(anObject);
            this.content     = someContent;
            this.description = someDescription;
        }
        
		@Override
        public String toString() {
            return (this.getClass().getName() + " [" 
                    + "content: #" + this.content.length()
                    + "description: #" + this.description.length()
                    + ", object: " + this.getKnowledgeObject()
                    + "]");
        }
        
        @Override
		public String getContent() {
            return this.content;
        }
        
        @Override
		public String getDescription() {
            return this.description;
        }
    }
    
    private static class WrapperContentObject extends AbstractContentObject {
        
		private final Wrapper _wrapper;
        
        public WrapperContentObject(Wrapper wrapper) {
            super(wrapper.tHandle());
			_wrapper = wrapper;
        }
        
        @Override
		public String getContent() {
			if (_wrapper.tValid()) {
				DefaultFullTextBuffer buffer = new DefaultFullTextBuffer();
				_wrapper.generateFullText(buffer);
				return buffer.toString();
            }
            return null;
        }
        
        @Override
		public String getDescription() {
			if (_wrapper.tValid()) {
				return _wrapper.getName();
            }
            return null;
        }
    }

    public static final boolean isAvailable() {
    	return Module.INSTANCE.isActive() && Module.INSTANCE.getImplementationInstance().serviceAvailable();
    }
    
	public static final IndexingService getIndexingService() {
		if (!isAvailable()) {
			throw new IndexException("Indexing service is not available.");
		}
		return Module.INSTANCE.getImplementationInstance();
	}
    
	/**
	 * {@link TypedRuntimeModule} for access to the {@link DefaultIndexingService}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<DefaultIndexingService> {

		/**
		 * Sole module instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<DefaultIndexingService> getImplementation() {
			return DefaultIndexingService.class;
		}
		
		@Override
		protected DefaultIndexingService newImplementationInstance() throws ModuleException {
			DefaultIndexingService newInstance = super.newImplementationInstance();
			if (!newInstance.serviceAvailable()) {
				throw new ModuleException("Indexing service is not available.", this.getImplementation());
			}
			newInstance.prepareService();
			return newInstance;
		}
	}

}
