/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.NamedValues;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.persist.DataManager;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AttributeLoader;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.NoFlexData;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * Flexible Wrapper, able to store any kind of data.
 * 
 * This wrapper will check on every used attribute, if it is defined in the knowledge object or not.
 * If it is stored there, that object will be used for storing the attribute, if not, a generic data
 * object information storage through {@link DataManager}s.
 * 
 * <p>
 * Problems: Keys that differ in case only can not be stored with MySQL (as of now), since indexes
 * in MySQL are per default case insensitive.
 * </p>
 * 
 * @deprecated TODO #6121: Delete TL 5.8.0 deprecation {@link LegacyFlexWrapper} uses own
 *             {@link NamedValues} to store dynamic values. These are now (#9703) included in the
 *             {@link KnowledgeObject} but not public accessible.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@Deprecated
public abstract class LegacyFlexWrapper extends AbstractBoundWrapper implements Committable {

	/**
	 * {@link PreloadOperation} that bulk-loads flex attribute data.
	 */
	public static final PreloadOperation FLEX_ATTRIBUTE_FETCH = new PreloadOperation() {
		@Override
		public void prepare(final PreloadContext context, Collection<?> baseObjects) {
			List<LegacyFlexWrapper> wrappers = new ArrayList<>();
			for (Object obj : baseObjects) {
				if (obj instanceof LegacyFlexWrapper) {
					LegacyFlexWrapper wrapper = (LegacyFlexWrapper) obj;
					FlexData cachedGlobalState = wrapper.getCachedGlobalState();
					if (cachedGlobalState == null) {
						wrappers.add(wrapper);
					} else {
						context.keepValue(cachedGlobalState);
					}
				}
			}
			
			if (wrappers.isEmpty()) {
				return;
			}
			
			AttributeLoader<LegacyFlexWrapper> loader = new AttributeLoader<>() {
				@Override
				public void loadData(long dataRevision, LegacyFlexWrapper baseObject, FlexData data) {
					baseObject.installGlobalState(data);
					context.keepValue(data);
				}

				@Override
				public void loadEmpty(long dataRevision, LegacyFlexWrapper baseObject) {
					baseObject.installGlobalState(NoFlexData.INSTANCE);
				}
			};
			FlexDataManager flexDataManager = wrappers.get(0).tHandle().getFlexDataManager();
			flexDataManager.loadAll(loader, Wrapper.KEY_MAPPING, wrappers, wrappers.get(0).tHandle()
				.getKnowledgeBase());
		}
	};

    /** {@link Reference} to unmodified attribute data. */
	private Reference<FlexData> globalStateRef;

    /** @see #getDynamicAttributesLocally() */
	private FlexData localModifications;

    /** A sticky flag that indicates that BinaryData was set */
    private boolean     binaryDataSet;

	private Object _modificationContext = null;

    /**
     * Create a new instance of this wrapper.
     * 
     * @param    ko         The knowledge object to be wrapped.
     */
	public LegacyFlexWrapper(KnowledgeObject ko) {
        super(ko);
    }

    /**
     * Store the underlying data object using the same (DB-) Context.
     */
    @Override
	public boolean prepare(CommitContext aContext) {
        if (this.localModifications == null) {  
        	// May have been registered as Committable by a subclass.
            return true;
        }
        
		{
            KnowledgeObject ko = tHandle();
			if (!canUpdate()) {
				throw new IllegalStateException("Commit for object locked in foreign context.");
			}
			
			return getFlexDataManager(ko).store(ko.tId(), this.localModifications, aContext);
		}
    }

    /**
     * Delete the flexible data associated with this object.
     * 
     * @see Committable#prepareDelete(CommitContext)
     */
    @Override
	public boolean prepareDelete(CommitContext aContext) {
		KnowledgeObject ko = tHandle();
    	
		return getFlexDataManager(ko).delete(ko.tId(), aContext);
    }

    /**
     * Pushes local modifications to the data cache.
     * 
     * @see com.top_logic.knowledge.service.Committable#commit(CommitContext)
     */
    @Override
	public synchronized boolean commit(CommitContext aContext) {
		unlock();
        if (this.localModifications != null) {
            if (binaryDataSet) {
                resetData(); // Must re-fetch globalStateRef to get stable binary data
            } else {
				installGlobalState(this.localModifications);
                this.localModifications = null;
            }
        }
        return true;
    }
    
	@Override
	public void complete(CommitContext aContext) {
        // nothing happens here
	}

    /**
     * Will be called by the CommitContext to rollback our Status.
     */
    @Override
	public boolean rollback(CommitContext aContext) {
		unlock();
        resetData();
        return true;
    }

	private void unlock() {
		_modificationContext = null;
	}

	private boolean canUpdate() {
		checkInvalid();
		if (_modificationContext == null) {
			return true;
		}
		return _modificationContext == KBUtils.getCurrentContext(getKnowledgeBase());
	}

    /** 
     * Cleanup the state of my Flex-Attributes. 
     */
    private void resetData() {
        this.localModifications = null;
        
        // For safety: Drop cached data.
        this.globalStateRef = null;
        
        // Rest sticky flag only here
        this.binaryDataSet  = false;
    }

    @Override
	public void refetchWrapper() {
        super.refetchWrapper();
        
        // Drop cached data.
        this.globalStateRef = null;
        
        // Do not set storage to null , this should fail on next commit().
    }

	/**
	 * Delete the underlying KnowledgeObject and the data held via data manager.
	 */
	@Override
	public void tDelete() {
		// Note: This reference must be computed before the underlying object is
		// deleted. After actual deletion the call throws an exception (at least
		// for new objects, see below).
		KnowledgeBase kb = getKnowledgeBase();

    	boolean hasLocalModifications = localModifications != null;
    	
    	// Re-fetch original storage to decide whether data must be removed in
    	// the data manager upon commit.
    	// 
    	// Note: This must happen before the underlying object is deleted.
		// Afterwards, no reference to the knowledge base exists and the
		// FlexDataManager lookup fails. This is true at least for
		// new objects, because those are immediately deleted, not upon commit
		// through the KnowledgeBase context.
		FlexData persistentState = lookupPersistentState();

    	// Make sure to lock this object
		super.tDelete();
    	
		{
    		// Release potentially modified storage, keep persistent storage for
			// prepareDelete() to be able to recursively delete nested objects.
    		this.localModifications = persistentState;
    		
			boolean hasPersistentData = localModifications != NoFlexData.INSTANCE;
    		
			if (hasPersistentData) {
    			// Add to commit handler for delete. Committable is automatically
    			// removed as regular committable from commit handler.
    			((CommitHandler) kb).addCommittableDelete(this);
    		} 
    		else if (hasLocalModifications) {
    			// Prevent storing changes for deleted object.
    			((CommitHandler) kb).removeCommittable(this);
    		}
    	}
	}

    /**
     * Return the attribute value for the given key.
     *
     * This generic method can be used for reading data from the FlexWrapper
     * without using the explicit methods.
     *
     * @param    aKey    The name of the needed attribute.
     * @return   The read value or <code>null</code>, if reading fails.
     */
    public final Object reallyGetValue(String aKey) {
		{
            if (this.isKOAttribute(aKey)) {
				return tGetData(aKey);
			} else {
			    return getDOValue(aKey);
			}
        }
    }

	private Object getDOValue(String dynamicAttributeName) {
		FlexData attributeData = this.getDataObjectForRead();
		if (attributeData == null) {
			// No attribute data.
			return null;
		}
		
		try {
			return attributeData.getAttributeValue(dynamicAttributeName);
		} catch (NoSuchAttributeException e) {
			// Attribute not yet set. 
			return null;
		}
	}
    
    /**
     * Return the attribute value for the given key.
     *
     * @param    aKey    The name of the needed attribute.
     * @return   The read value or <code>null</code>, if reading fails.
     */
    @Override
	public Object getValue(String aKey) {
        return this.reallyGetValue(aKey);
    }

    /**
     * Set the attribute value defined by the given name to the given one.
     * 
     * This method will first try, if the requested value is an attribute
     * of the knowledge object. Therefore it will use the {@link #isKOAttribute(String)}
     * method. If it is, fine, otherwise the data will be set in
     * the {@link #getDataObjectForWrite() data object}. 
     * 
     * @param    aName     The name of the attribute to be set.
     * @param    aValue    The value to be set.
     * 
     * @throws WrapperRuntimeException if any problem occurs 
     *         (normally object is locked by other user)
     */
    public final void reallySetValue(String aName, Object aValue) {
        try {
            if (this.isKOAttribute(aName)) {
				this.tSetData(aName, aValue);
            }
            else {
                this.setDOValue(aName, aValue);
            }
        } 
        catch (WrapperRuntimeException wrx) {
            throw wrx;
        }
        catch (Exception ex) {
            Logger.warn("Unable to set attribute '" + aName + "'!", ex, this);
            throw new WrapperRuntimeException(ex);
        }
    }
    
    /**
     * Set the attribute value defined by the given name to the given one.
     * 
     * This method will first try, if the requested value is an attribute
     * of the knowledge object. Therefore it will use the {@link #isKOAttribute(String)}
     * method. If it is, fine, otherwise the data will be set in
     * the {@link #getDataObjectForWrite() data object}. 
     * 
     * @param    aName     The name of the attribute to be set.
     * @param    aValue    The value to be set.
     * 
     * @throws WrapperRuntimeException if any problem occurs 
     *         (normally object is locked by other user)
     */
    @Override
	public void setValue(String aName, Object aValue) {
        this.reallySetValue(aName, aValue);
    }
    
    
    /**
     * Returns a list with the names of all attributes of this FlexWrapper.
     * 
     * These are the names of the attributes from the data object plus
     * the names of the attributes of the KnowledgeObject of this FlexWrapper.
     * 
     * @return    The list of attribute names.
     */
	@Override
	public Set<String> getAllAttributeNames() {
        String[]    koAttributeNames = this.tHandle().getAttributeNames();
		FlexData attributeData = this.getDataObjectForRead();
		Collection<String> dynamicAttributeNames = attributeData.getAttributes();
		Set<String> result = new HashSet<>(koAttributeNames.length + dynamicAttributeNames.size());

        for (int n = 0; n < koAttributeNames.length; n++) {
            result.add(koAttributeNames[n]);
        }
		for (String dynamicAttribute : dynamicAttributeNames) {
			result.add(dynamicAttribute);
        }

        return result;
    }   

    /**
     * Check, if the given attribute name is stored in the knowledge object.
     * 
     * @param    aKey    The name of the attribute.
     * @return   <code>true</code>, if there is an attribute with that name.
     */
    public final boolean isKOAttribute(String aKey) {
    	return MetaObjectUtils.hasAttribute(this.tTable(), aKey);
    }

    /**
	 * Create the Reference for the {@link FlexData}.
	 */
	private Reference<FlexData> createReference(FlexData values) {
		return new SoftReference<>(values);
    }
    
    /**
	 * Return the data object used for reading the additional attributes.
	 * 
	 * @return The data object holding the data of this instance,
	 *         <code>null</code> if there is not attribute data. Must not be
	 *         modified.
	 */
	protected final synchronized FlexData getDataObjectForRead() {
		if (canUpdate() && localModifications != null) {
			return localModifications;
		}
		
		return lookupPersistentState();
    }

    /**
	 * The persistent attribute data without thread-local modifications.
	 * 
	 * <p>
	 * <b>Note:</b> This method is intended for creating journal entries for
	 * this object only.
	 * </p>
	 * 
	 * @return the persistent state of this object's data without uncommitted
	 *         changes. The result must not be modified.
	 * 
	 * @see #getDynamicAttributesLocally() for access to thread-local changes.
	 * @see #getDataObjectForRead() for regular read access.
	 * @see #getDataObjectForWrite() for regular write access.
	 */
	protected final synchronized FlexData getDynamicAttributesGlobally() {
        // Unconditionally provoke InvalidWrapperException, if wrapped object is invalid.
		checkInvalid();
		
		return lookupPersistentState();
    }

    /**
	 * Thread-local modifications to the dynamic attributes in the thread that
	 * has {@link #touch() locked} this object.
	 * 
	 * <p>
	 * <b>Note:</b> This method is intended for creating journal entries for
	 * this object only.
	 * </p>
	 * 
	 * @return The local uncommited changes to the dynamic attributes.
	 *         <code>null</code>, if the dynamic attributes are not modified.
	 * 
	 * @see #getDynamicAttributesGlobally() for access to the attribute values
	 *      without local modifications.
	 * @see #getDataObjectForRead() for regular read access.
	 * @see #getDataObjectForWrite() for regular write access.
	 */
	protected final FlexData getDynamicAttributesLocally() {
		return localModifications;
	}

    /**
     * Lookup or load the persistent attribute data. 
     * 
     * <p>
     * Note: must be called from within a context synchronized on this object.
     * </p>
     * 
	 * @return the persistent state of this object's data without any
	 *         uncomitted changes. Must not be modified.
     */
	private FlexData lookupPersistentState() {
		FlexData cachedGlobalState = getCachedGlobalState();
		if (cachedGlobalState != null) {
			return cachedGlobalState;
		}
		this.globalStateRef = null;
    	
		KnowledgeObject theKO = tHandle();
		FlexData persistentAttributeData = getFlexDataManager(theKO).load(theKO.getKnowledgeBase(), theKO.tId(), false);
		installGlobalState(NoFlexData.INSTANCE);
		return persistentAttributeData;
    }

	final void installGlobalState(FlexData persistentAttributeData) {
		this.globalStateRef = createReference(persistentAttributeData);
	}

	FlexData getCachedGlobalState() {
		if (this.globalStateRef != null) {
			FlexData cachedAttributeData = globalStateRef.get();
    		if (cachedAttributeData != null) {
    			// Return cached data.
    			return cachedAttributeData;
    		}
    	}
		return null;
	}

    /**
	 * Return the data object used for writing the additional attributes.
	 * 
	 * @return The data object holding the data of this instance, never <code>null</code>.
	 * @throws IllegalStateException
	 *         If this object is locked in another context.
	 */
	protected final synchronized FlexData getDataObjectForWrite() {
        // Unconditionally provoke InvalidWrapperException, if wrapped object is invalid.
		KnowledgeObject theKO = tHandle();
		        
		if (localModifications != null) {
			if (!canUpdate()) {
				throw new IllegalStateException("Object locked in other context: " + this);
			}
			return localModifications;
		}
		
		// check that this wrapper does not wrap a historic object
		if (theKO.getHistoryContext() != Revision.CURRENT_REV) {
			throw new IllegalStateException("Historic objects must not be modified.");
		}
		
		FlexData attributeData = getFlexDataManager(theKO).load(theKO.getKnowledgeBase(), theKO.tId(), true);
		
		// Lock object to provoke commit.
		touch();
		
		((CommitHandler) theKO.getKnowledgeBase()).addCommittable(this);
		this.localModifications = attributeData;
		this._modificationContext = KBUtils.getCurrentContext(getKnowledgeBase());
		assert _modificationContext != null : "touch should create context";
		
		return attributeData;
    }

    /**
     * Set the given value in the data object representing this instance.
     * 
     * @param    dynamicAttributeName      The name of the attribute to be used for storage.
     * @param    newValue    The value to be stored.
     */
	private void setDOValue(String dynamicAttributeName, Object newValue) throws DataObjectException {
    	Object oldValue = getDOValue(dynamicAttributeName);
		if (CollectionUtil.equals(oldValue, newValue)) {
			// Prevent locking the object and starting a commit in case of only
			// no-ops.
    		return;
    	}
    	
		FlexData updatedData = this.getDataObjectForWrite();
        updatedData.setAttributeValue(dynamicAttributeName, newValue);
        binaryDataSet |= newValue instanceof BinaryData;
    }

	private static FlexDataManager getFlexDataManager(KnowledgeObject ko) {
		return ko.getFlexDataManager();
	}
    
}
