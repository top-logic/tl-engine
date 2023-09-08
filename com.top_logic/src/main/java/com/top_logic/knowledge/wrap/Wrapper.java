/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Date;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.searching.FullTextSearchable;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRefetch;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.TLContext;

/**
 * Interface for Wrapper.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface Wrapper extends TLNamed, Comparable<Wrapper>, ValueProvider, FullTextSearchable {

	@Override
	KnowledgeObject tHandle();

    /**
	 * Return the attribute value for the given key.
	 * 
	 * May be overridden to return data form sources other than the KO. e.g.
	 * {@link AbstractBoundWrapper}.
	 *
	 * @param attributeName
	 *        The name of the needed attribute.
	 * @return The read value or <code>null</code>, if reading fails.
	 */
    @Override
	default Object getValue(String attributeName) {
		return tGetData(attributeName);
	}
    
    /**
     * Set the attribute value defined by the given name to the given one.
     * 
     * @param    attributeName     The name of the attribute to be set.
     * @param    aValue    The value to be set. (Should be a Java primitive)
     */
    @Override
	default void setValue(String attributeName, Object aValue) {
		tSetData(attributeName, aValue);
	}
    
    /**
     * Return the last modification date of the KnowledgeObject.
     * 
     * @return    The last modification date.
     */
	default Long getLastModified() {
		return tLastModificationTime();
	}

	/**
	 * Return the date of the last modification of this wrapper.
	 * 
	 * This method first tries to find the date via it's properties. If this fails, the life cycle
	 * attribute will be used.
	 * 
	 * @return The modification date of the wrapper.
	 * @see #getProperties()
	 */
	default Date getModified() {
		return tLastModificationDate();
	}
    
    /**
     * Returns the Person who modified this instance the last time.
     * 
     * Will <em>not</em> Work when {@link TLContext#setContextId(String)} was used.
     */
	default Person getModifier() {
		return tLastModifier();
	}

    /**
     * Return the date of the creation of this wrapper.
     * 
     * This method first tries to find the date via it's properties. If this
     * fails, the lifecycle attribute will be used.
     *  
     * @return    The modification date of the wrapper.
     */
	default Date getCreated() {
		return tCreationDate();
	}

	/**
     * Returns the Person who created this instance the last time.
     */
	default Person getCreator() {
		return tCreator();
	}
	
    /**
	 * Get the physical representation of this Wrapper.
	 * <p>
	 * Note that the result is <code>null</code> when no physical representation is present. This is
	 * the same as new DataAccessProxy({@link #getDSN()}) except for a null DSN. The DataAccessProxy
	 * may be cached, so access should be faster.
	 * </p>
	 *
	 * @return the physical representation; may be null
	 * @implNote The default implementation returns <code>null</code>.
	 */
	default DataAccessProxy getDAP() throws DatabaseAccessException {
		return null;
	}
    
    /**
	 * Get a Data Source Name identifying the physical representation of this Wrapper.
	 * <p>
	 * Note that the result may be e.g <code>null</code> or an empty String depending on the
	 * underlying KnowledgeObject.
	 * </p>
	 *
	 * @return the DSN; may be null
	 * @implNote The default implementation returns <code>null</code>.
	 */
	default String getDSN() {
		return null;
	}
    
    /**
	 * Get the properties associated with the DAP.
	 * 
	 * So this is the same as <code>getDAP().getProperties()</code> but the Properties can be
	 * cached.
	 * 
	 * @return the properties associated with the DAP, <code>null</code> if there is no DAP for the
	 *         knowledge object, or there are no properties associated.
	 * 
	 * @implNote The default implementation returns <code>null</code>.
	 */
	default DataObject getProperties() {
		return null;
	}
    
    /**
     * Get the {@link KnowledgeBase} of the wrapped object.
     * 
     * Always use this KnowledgeBase when you need a KnowledgeBase to
     * create new Objects. This will make your Wrapper Multi-KnowledgeBase
     * aware.
     * 
     * @return KnowledgeBase
     */
	default KnowledgeBase getKnowledgeBase() {
		return tKnowledgeBase();
	}
    
    /**
	 * Force the wrapper to refetch the cached values.
	 * 
	 * This method will be called (indirectly) by the {@link KnowledgeBaseRefetch}, which
	 * synchronizes the content between different VMs (in a cluster).
	 * 
	 * @implNote The default implementation is a no-op.
	 */
	default void refetchWrapper() {
		// Noithing to do here
	}
    
	@Override
	default void generateFullText(FullTextBuBuffer buffer) {
		buffer.add(getName());
	}
    
	/**
	 * Map {@link Wrapper}s to their {@link #tHandle()}.
	 */
	public static final Mapping<TLObject, KnowledgeObject> KO_MAPPING = new Mapping<>() {
		@Override
		public KnowledgeObject map(TLObject aInput) {
			return (KnowledgeObject) aInput.tHandle();
		}
	};

    /**
     * Map {@link Wrapper}s to their {@link #getName() name}.
     */
    public static final Mapping NAME_MAPPING = new Mapping() {
         @Override
		public Object map(Object aInput) {
            return ((Wrapper)aInput).getName();
         }
    };

    /**
	 * Map {@link Wrapper}s to their {@link KBUtils#getWrappedObjectName(TLObject)}.
	 * 
	 * @see WrapperByIDMapping
	 */
	public static final Mapping<Wrapper, TLID> IDENTIFIER_MAPPING = new Mapping<>() {
         @Override
		public TLID map(Wrapper aInput) {
			return KBUtils.getWrappedObjectName(aInput);
         }
    };

	/**
	 * Map {@link Wrapper}s to their {@link ObjectKey}.
	 */
	public static final Mapping<Wrapper, ObjectKey> KEY_MAPPING = new Mapping<>() {
		@Override
		public ObjectKey map(Wrapper aInput) {
			return aInput.tHandle().tId();
		}
	};

}

