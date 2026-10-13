/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.knowledge.objects.DestinationIterator;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.SourceIterator;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.searching.FullTextSearchable;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.TLContext;

/**
 * Interface for Wrapper.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface Wrapper extends WrapperLifecycle, TLNamed, Comparable<Wrapper>, ValueProvider, FullTextSearchable {

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
	 * Reads the value through {@link #getValue(String)} with the part's name.
	 */
	@Override
	default Object tValue(TLStructuredTypePart part) {
		return getValue(part.getName());
	}

	/**
	 * Updates the value through {@link #setValue(String, Object)} with the part's name.
	 */
	@Override
	default void tUpdate(TLStructuredTypePart part, Object value) {
		setValue(part.getName(), value);
	}

	/**
	 * Returns a list with the names of all attributes of this {@link Wrapper}.
	 * 
	 * <p>
	 * These are the names of the attributes of the {@link #tHandle() underlying knowledge object}.
	 * </p>
	 * 
	 * @return The list of attribute names.
	 */
	default Set<String> getAllAttributeNames() {
		return tHandle().getAllAttributeNames();
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
	 * @implNote The default implementation reads the {@link KOAttributes#PHYSICAL_RESOURCE}
	 *           attribute, if the table of this object has one.
	 */
	default String getDSN() {
		String theDSN = null;
		if (MetaObjectUtils.hasAttribute(this.tTable(), KOAttributes.PHYSICAL_RESOURCE)) {
			theDSN = tGetDataString(KOAttributes.PHYSICAL_RESOURCE);
		}

		return theDSN;
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
    
	@Override
	default void generateFullText(FullTextBuBuffer buffer) {
		buffer.add(getName());
	}

	/**
	 * Returns a mapping contains all values of this wrapper.
	 * 
	 * <p>
	 * The mapping maps the name of an attribute to its value in this {@link TLObject}.
	 * </p>
	 * 
	 * @return A map containing all values of this wrapper.
	 */
	default Map<String, Object> getAllValues() {
		Map<String, Object> result = new LinkedHashMap<>();
		KnowledgeItem handle = tHandle();
		for (String attributeName : handle.getAllAttributeNames()) {
			result.put(attributeName, getValue(attributeName));
		}
		return result;
	}

    /**
     * Stable comparison along the name of the Wrappers.
     * 
     * Will care for invalid Wrappers. In case of equal names the KO-Id
     * will be used to discriminate them.
     * 
     * In case you want to sort Wrappers by {@link #getName()} use the {@link WrapperNameComparator}. 
     */
    @Override
	default int compareTo(Wrapper anOtherWrapper) {

        if (this == anOtherWrapper) {
            return 0;
        }
        if (!this.tValid()) {
            if (!anOtherWrapper.tValid()) {
                return 0;
            }
            return 1;
        }
        if (!anOtherWrapper.tValid()) {
            return -1;
        }
        
        String  theOtherName    = anOtherWrapper.getName();
        String  theName         = getName();
        
        int result = 0;
        if (theName != null) {
            if (theOtherName != null) {
                result = theName.compareTo(theOtherName);
            } else { // theOtherName == null 
                return  -1;
            }
        } else { // theName == null
            if (theOtherName != null) {
                return 1;
            } // else { // both are null
        }
        if (result == 0) { // Not the same wrapper, but the same name ?
            // Use ObjectKey as stable order 
			result = KBUtils.getObjectKeyString(tHandle())
				.compareTo(KBUtils.getObjectKeyString(anOtherWrapper.tHandle()));
        }
        return result;
    }

	/**
	 * Returns the String value of the given attribute.
	 * 
	 * @param    aName    Attribute name.
	 * @return   Value of the attribute, <code>null</code> if the attribute has no value.
	 * @see      #setString(java.lang.String, java.lang.String)
	 */
	default String getString(String aName) {
		return ((String) this.getValue(aName));
	}

    /**
     * Returns the int value of the given attribute.
     * 
     * @param    aName    Name of the requested attribute.
     * @return   The value of the attribute or 0, if no such attribute defined.
     */
	default int getInt(String aName) {
		Object theNum = this.getValue(aName);
		if (theNum == null) {
			return 0;
		} else {
			return ((Number) theNum).intValue();
		}
	}

    /**
     * Return the Integer value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setInteger(String, Integer)
     */
	default Integer getInteger(String aName) {
		return (Integer) this.getValue(aName);
	}

    /**
     * Return the boolean value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setBoolean(String, boolean)
     */
	default boolean getBooleanValue(String aName) {
		Boolean theResult = this.getBoolean(aName);

		return ((theResult != null) ? theResult.booleanValue() : false);
	}

    /**
     * Return the boolean value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setBoolean(String, Boolean)
     */
	default Boolean getBoolean(String aName) {
		return (Boolean) this.getValue(aName);
	}

    /**
     * Return the date value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setDate(String, Date)
     */
	default Date getDate(String aName) {
		return (Date) this.getValue(aName);
	}

    /**
     * Return the long value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setLong(String, long)
     */
	default Long getLong(String aName) {
		return (Long) this.getValue(aName);
	}

    /**
     * Return the long value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setLong(String, long)
     */
	default long getLongValue(String aName) {
		Object value = this.getValue(aName);
		if (value == null) {
			return 0L;
		} else {
			return ((Number) value).longValue();
		}
	}

    /**
     * Return the boolean value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setFloat(String, Float)
     */
	default Float getFloat(String aName) {
		return (Float) this.getValue(aName);
	}

    /**
     * Return the double value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     */
	default Double getDouble(String aName) {
		return (Double) this.getValue(aName);
	}

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getDate(String)
     */
	default void setDate(String aName, Date aValue) {
		this.setValue(aName, aValue);
	}

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getLong(String)
     */
	default void setLong(String aName, long aValue) {
		this.setValue(aName, Long.valueOf(aValue));
	}

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getLong(String)
     */
	default void setLong(String aName, Long aValue) {
		this.setValue(aName, aValue);
	}

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getFloat(String)
     */
	default void setFloat(String aName, Float aValue) {
		this.setValue(aName, aValue);
	}

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getInteger(String)
     */
	default void setInteger(String aName, int aValue) {
		this.setInteger(aName, Integer.valueOf(aValue));
	}

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getInteger(String)
     */
	default void setInteger(String aName, Integer aValue) {
		this.setValue(aName, aValue);
	}

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName    The name of attribute to be set.
     * @param    aValue   Value of attribute to be set.
     * @see      #getBooleanValue(String)
     */
	default void setBoolean(String aName, boolean aValue) {
		this.setBoolean(aName, aValue ? Boolean.TRUE : Boolean.FALSE);
	}

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getBoolean(String)
     */
	default void setBoolean(String aName, Boolean aValue) {
		this.setValue(aName, aValue);
	}

	/**
	 * Set the value of the given attribute.
	 * 
	 * @param    aName     The name of attribute to be set.
	 * @param    aValue    Value of attribute to be set.
	 * @see      #getString(String)
	 */
	default void setString(String aName, String aValue) {
		this.setValue(aName, aValue);
	}

	/**
	 * Access to a directly referenced object.
	 * 
	 * @param dynamicType
	 *        The expected return type.
	 * @param attributeName
	 *        The name of the reference attribute.
	 * @return The referenced object, or <code>null</code>, if the reference is empty.
	 */
	default <T> T getReference(Class<T> dynamicType, String attributeName) {
		return tGetDataReference(dynamicType, attributeName);
	}

	/**
	 * Sets a reference attribute.
	 * 
	 * @param attributeName
	 *        The name of the reference attribute.
	 * @param value
	 *        The referenced object, or <code>null</code> to clear the reference.
	 */
	default void setReference(String attributeName, TLObject value) {
		AbstractWrapper.setReference(this, attributeName, value);
	}

	/**
	 * Resolve the given query with this as base object.
	 * 
	 * @return The resulting set of opposite objects.
	 */
	default <C extends Collection<KnowledgeAssociation>> Set<? extends TLObject> resolveWrappers(
			AbstractAssociationQuery<? extends KnowledgeAssociation, C> query) {
		return AbstractWrapper.resolveWrappers(this, query);
	}

	/**
	 * Resolve the given query with this as base object.
	 * 
	 * @return The resulting set of opposite objects.
	 */
	default <C extends Collection<KnowledgeAssociation>, T extends TLObject> Set<T> resolveWrappersTyped(
			AbstractAssociationQuery<? extends KnowledgeAssociation, C> query, Class<T> wrapperType) {
		return AbstractWrapper.resolveWrappersTyped(this, query, wrapperType);
	}

	/**
	 * Resolve the given query with this as base object.
	 * 
	 * @param query
	 *        The query to resolve.
	 * 
	 * @return The resulting unmodifiable set of wrappers that directly point to this wrapper via
	 *         the reference attribute {@link AbstractAssociationQuery#getReferenceAttribute()} of the given query.
	 *         query.
	 */
	default <T extends TLObject, C> C resolveLinks(AbstractAssociationQuery<T, C> query) {
		return AbstractWrapper.resolveLinks(this, query);
	}

	/**
	 * Updates the ordered link set attribute specified by the given {@link OrderedLinkQuery}.
	 * 
	 * <p>
	 * Note: The (virtual) attribute is implemented by a foreign key and order attribute in the
	 * target object pointing to this instance. Those attributes are updated by this call.
	 * </p>
	 * 
	 * @param <L>
	 *        The type of the link object being updated.
	 * @param attribute
	 *        The attribute (implemented by a foreign key and an order attribute in the in the
	 *        values).
	 * @param newOrder
	 *        The ordered set of values (must not contain the same value more than once).
	 */
	default <L extends TLObject> void updateOrderedLinkSet(OrderedLinkQuery<L> attribute, List<? extends L> newOrder) {
		AbstractWrapper.updateOrderedLinkSet(this, attribute, newOrder);
	}

    /**
	 * Get the wrappers on the destination side of the associations of the given type.
	 *
	 * @param anKAType
	 *        the name of the association used.
	 * @param aKOType
	 *        the type of KOS to return null means all.
	 * 
	 * @return the partners or an empty List if none is found; never null
	 */
	default List getOutgoingWrappers(String anKAType, String aKOType) {
		DestinationIterator theAssociations = new DestinationIterator(this.tHandle(), anKAType);

		return AbstractWrapper.getWrappersFromAssociations(theAssociations, aKOType);
	}

    /**
	 * Get the wrappers on the source side of the associations of the given type.
	 *
	 * @param anKAType
	 *        the name of the association used.
	 * @param aKOType
	 *        the type of KOS to return null means all.
	 * 
	 * @return the partners or an empty List if none is found; never null
	 */
	default List getIncomingWrappers(String anKAType, String aKOType) {
		SourceIterator theAssociations = new SourceIterator(this.tHandle(), anKAType);

		return AbstractWrapper.getWrappersFromAssociations(theAssociations, aKOType);
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

