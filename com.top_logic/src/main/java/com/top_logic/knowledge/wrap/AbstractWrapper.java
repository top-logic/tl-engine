/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.MappedList;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.repos.RepositoryDataSourceAdaptor;
import com.top_logic.dsa.repos.file.FileRepository;
import com.top_logic.knowledge.objects.DestinationIterator;
import com.top_logic.knowledge.objects.KAIterator;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.SourceIterator;
import com.top_logic.knowledge.service.AssociationQueryUtil;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseRefetch;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.LiveAssociationsEndList;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.knowledge.util.ReferenceAccess;
import com.top_logic.knowledge.wrap.exceptions.InvalidWrapperException;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * Basic class for all wrappers.
 * 
 * Serializable will only work for valid objects when writing.
 * Reading will not work when using custom WrapperFactories,
 * or when Knowledgebase has changed meanwhile.
 *
 * @author    <a href="mailto:mer@top-logic.com">Michael Eriksson</a>
 */
public abstract class AbstractWrapper extends PersistentObject implements Wrapper {

    /** Cached access to the DataAccessProxy */
    protected transient DataAccessProxy   dap;

    /** Cached Properties from the DAP (if availeable) */
    protected transient DataObject        properties;
    
    /** Indicates that the properties are valid (even) when null */
    protected transient boolean           propertiesValid;

    /**
	 * Construct an instance wrapped around the specified
	 * {@link com.top_logic.knowledge.objects.KnowledgeItem}.
	 *
	 * This CTor is only for the WrapperFactory! <b>DO NEVER USE THIS CONSTRUCTOR!</b> Use always
	 * the getInstance() method of the wrappers.
	 * 
	 * @param ko
	 *        The KnowledgeObject, must never be <code>null</code>.
	 * 
	 * @throws NullPointerException
	 *         If the KO is <code>null</code>.
	 */
	public AbstractWrapper(KnowledgeObject ko) {
		super(ko);
    }

    @Override
	protected String toStringValues() {
		final String name = this.getName();
		if (KBUtils.getObjectKeyString(this.tHandle()).equals(name)) {
			return "";
		} else {
			return ", name: " + name;
		}
    }
    
    /**
     * Create an association to another wrapper.
     */
	protected KnowledgeAssociation associateWith(TLObject aWrapper, String aType)
			throws DataObjectException {
		return AbstractWrapper.associateWith(this, aWrapper, aType);
    }

	/**
	 * Create an association to another wrapper.
	 */
	public static KnowledgeAssociation associateWith(TLObject source, TLObject target, String aType)
			throws DataObjectException {
		return source.tKnowledgeBase().createAssociation(source.tHandle(), target.tHandle(), aType);
	}

	/** get a cached acces to the associated Datasource. 
     * 
     * @return null when getDSN() is null. 
     */
    @Override
	public DataAccessProxy getDAP() throws DatabaseAccessException {
        if (dap == null) {
            String theDSN = this.getDSN ();
    
            if (theDSN != null) {
				dap = new DataAccessProxy(theDSN);
            } // Else: keep null default.
        }
        else
            this.checkInvalid();

        return dap;
    }

    /**
      * Get a Data Source Name identifying the physical representation of this
      * Wrapper.
      * <p>
      * Note that the result may be e.g <code>null</code> or an empty String
      * depending on the underlying KnowledgeObject.
      * </p>
      *
      * @return the DSN; may be null
      */
    @Override
	public String getDSN() {
        String theDSN = null;
		if (MetaObjectUtils.hasAttribute(this.tTable(), KOAttributes.PHYSICAL_RESOURCE)) {
			theDSN = tGetDataString(KOAttributes.PHYSICAL_RESOURCE);
        }

        return theDSN;
    }
    
    /** return the properties of the underlying DAP if possible.
     * 
     * @return null when DAP is invalid.
     */
    @Override
	public DataObject getProperties() {
        checkInvalid();
        if (propertiesValid) {
            return properties;
        }

        DataAccessProxy theDAP = getDAP();
		if (theDAP != null) {
			if (theDAP.exists()) {
				properties = theDAP.getProperties();
			}
        }

		propertiesValid = true; // even if null

		return properties;
    }

    /** 
     * Call this when the Physical resource was changed externally 
     * 
     * TODO FMA/KHA properties may still not work in cluster, 
     *              e.g. when used with 
     *              {@link FileRepository} /
     *              {@link RepositoryDataSourceAdaptor}
     */
    public void resetDAP() {
        dap              = null;
        properties       = null;
        propertiesValid  = false;
    }

    /**
	 * Provoke modification of the underlying {@link KnowledgeItem}.
	 * 
	 * <p>
	 * After calling this method it is guaranteed that the values does not change until a commit. In
	 * case a different session or node changes this object, the changes are not visible. In such
	 * case a following commit would fail.
	 * </p>
	 */
	public final void touch() {
		tHandle().touch(); // Touch Object to provoke modification
    }
    
	@Override
	public Object tValue(TLStructuredTypePart part) {
		return getValue(part.getName());
	}

	@Override
	public void tUpdate(TLStructuredTypePart part, Object value) {
		setValue(part.getName(), value);
	}

	/**
	 * Return the attribute value for the given key.
	 * 
	 * <p>
	 * May be overridden to return data form sources other than the {@link #tHandle()} (see
	 * {@link AbstractBoundWrapper}).
	 * </p>
	 * 
	 * @param attributeName
	 *        The name of the requested attribute.
	 * @return The read value or <code>null</code>, if reading fails.
	 */
    @Override
	public Object getValue(String attributeName) {
		return tGetData(attributeName);
    }

    /**
     * Set the attribute value defined by the given name to the given one.
     * 
     * @param    attributeName     The name of the attribute to be set.
     * @param    aValue    The value to be set.
     */
    @Override
	public void setValue(String attributeName, Object aValue) {
		tSetData(attributeName, aValue);
    }

    /**
	 * Returns the knowledge base of the wrapped object.
	 * 
	 * @return The requested knowledge base, never <code>null</code>.
	 */
    @Override
	public KnowledgeBase getKnowledgeBase() {
        return internalGetKnowledgeBase();
    }

	/**
	 * The {@link KnowledgeBase} of the {@link #tHandle()}.
	 * 
	 * @return The knowledge base.
	 * 
	 * @see KnowledgeItem#getKnowledgeBase()
	 */
	private KnowledgeBase internalGetKnowledgeBase() {
		return this.tHandle().getKnowledgeBase();
	}

    /**
     * Set the given security values in the given knowledge object.
     * 
     * @param    aKey        The name of the security attribute.
     * @param    aValue      The value of the attribute.
     * @param    anObject    The object to be used for setting the value.
     * @return   <code>true</code>, if setting succeeds.
     */
    protected boolean setSecurity(String aKey, 
                                  String aValue, 
			KnowledgeItem anObject) {
		anObject.setAttributeValue(aKey, aValue);
		return true;
    }

	/**
	 * @deprecated Use {@link #tGetData(String)}, or one of its typed alternatives, e.g.
	 *             {@link #tGetDataString(String)}.
	 */
	@Deprecated
    protected final Object getKOValue(String attributeName) {
		return tGetData(attributeName);
    }

	/**
	 * @deprecated Use {@link #tSetData(String, Object)}.
	 */
	@Deprecated
    protected final boolean setKOValue(String attributeName, Object newValue) {
		tSetData(attributeName, newValue);
		return true;
	}

	@Override
	public Object tSetData(String property, Object newValue) {
		Object oldValue = super.tSetData(property, newValue);
		if (KOAttributes.PHYSICAL_RESOURCE.equals(property)) {
			if (!CollectionUtil.equals(newValue, oldValue)) {
				this.resetDAP();
			}
		}
		return oldValue;
    }

    /**
     * Get the single other end of an outgoing association.
     * It is assumed that only one entry is present. In case of
     * several entries one of them, with no guarantee which one,
     * will be returned.
     *
     * @param anAssociation the association used (e.g. {@link WebFolder#CONTENTS_ASSOCIATION})
     *
     * @return the partner or null if none is found
     *
     * #author Michael Eriksson
     */
	protected KnowledgeItem getSingleOutgoingPartner(String anAssociation) {
		return (KnowledgeItem) this.getOneEntry
                                    (this.getOutgoingPartners (anAssociation));
    }

    /**
     * Get the single other end of an incoming association.
     * It is assumed that only one entry is present. In case of
     * several entries one of them, with no guarantee which one,
     * will be returned.
     *
     * @param anAssociation the association used (e.g. {@link WebFolder#CONTENTS_ASSOCIATION})
     *
     * @return the partner or null if none is found
     *
     * #author Michael Eriksson
     */
	protected KnowledgeItem getSingleIncomingPartner(String anAssociation) {
		return (KnowledgeItem) this.getOneEntry
                                    (this.getIncomingPartners (anAssociation));
    }

    /**
     * Get the KnowledgeObjects contained at the other end of the specified
     * KnowledgeAssociations.
     * <br/>
     * Any invalid associations are silently filtered out.
     *
     * @param theAssociations the Associations;
     *                        must never null;
     *                        must be proper KnowledgeAssociations
     *
     * @return the KnowledgeObjects; never null
     *
     * #author Michael Eriksson
     */
    protected static List getKOsFromAssociations (KAIterator theAssociations) {
        List theKOs = new ArrayList ();

        while (theAssociations.hasNext ()) {
            theKOs.add (theAssociations.nextKO());
        }

        return theKOs;
    }

    /**
     * Get any other end of an incoming association.
     *
     * @param anAssociation the association used (e.g. {@link WebFolder#CONTENTS_ASSOCIATION})
     *
     * @return the partners or an empty List if none is found; never null
     */
	protected List getIncomingPartners(String anAssociation) {
		SourceIterator theAssociations = new SourceIterator(this.tHandle(), anAssociation);

        return getKOsFromAssociations (theAssociations);
    }

    /**
     * Return true if any Associations refers to this object.
     * 
     * @param anKAType may be null indication all types of KAs.
     */
	public boolean hasIncoming(String anKAType) {
        // TODO KHA this is only used in EditFastList (and wrong) -> elimate
		KnowledgeObject handle = this.tHandle();
		if (anKAType == null) {
			return handle.getIncomingAssociations().hasNext();
        }//  else {
		return handle.getIncomingAssociations(anKAType).hasNext();
        // }
    }

    /**
     * Get any other end of an outgoing association.
     *
     * @param anAssociation the association used (e.g. {@link WebFolder#CONTENTS_ASSOCIATION})
     *
     * @return the partners or an empty Listif none is found; never null
     */
	protected List getOutgoingPartners(String anAssociation) {
		DestinationIterator theAssociations = new DestinationIterator(this.tHandle(), anAssociation);

        return getKOsFromAssociations (theAssociations);
    }

    /**
	 * Get Wrapper form destination side of anAssociation for aType.
	 *
	 * @param anKAType
	 *        the name of the association used.
	 * @param aKOType
	 *        the type of KOS to return null means all.
	 * 
	 * @return the partners or an empty List if none is found; never null
	 */
	public List getOutgoingWrappers(String anKAType, String aKOType) {
		DestinationIterator theAssociations = new DestinationIterator(this.tHandle(), anKAType);

        return getWrappersFromAssociations(theAssociations, aKOType);
    }

    /**
	 * Get Wrapper from source side of anAssociation for aType.
	 *
	 * @param anKAType
	 *        the name of the association used.
	 * @param aKOType
	 *        the type of KOS to return null means all.
	 * 
	 * @return the partners or an empty List if none is found; never null
	 */
	public List getIncomingWrappers(String anKAType, String aKOType) {
		SourceIterator theAssociations = new SourceIterator(this.tHandle(), anKAType);

        return getWrappersFromAssociations(theAssociations, aKOType);
    }    

    /**
     * Get an entry from the specified Collection.
     * If the Collection contains several entries
     * there is no guarantee which one is taken.
     * If no entry is present <code>null</code> is returned.
     *
     * @param aCollection the Collection; must not be null
     *
     * @return the entry; null if no entry is found
     *
     * #author Michael Eriksson
     */
    private Object getOneEntry (Collection aCollection) {
        Object   theEntry  = null;
        Iterator theIterator = aCollection.iterator ();

        if (theIterator.hasNext ()) {
            theEntry = theIterator.next ();
        }

        return theEntry;
    }

    /**
	 * Check invalid and raise exception if invalid.
	 * 
	 * throws {@link InvalidWrapperException} in case the wrapper is invalid.
	 */
    protected void checkInvalid() throws InvalidWrapperException {
        if (! tValid()) {
			// Note: Do not try to call toString(), since this must fail for the deleted object, if
			// it tries to access some additional information from the object.
			throw new InvalidWrapperException("Deleted object access: " + tId());
        }
    }

	/**
	 * TODO #2829: Delete TL 6 deprecation.
	 * 
	 * @deprecated Use {@link #tValid()} instead
	 */
	@Deprecated
	@Override
	public final boolean isValid() {
		return tValid();
	}

    @Override
	public final boolean tValid() {
		return super.tValid();
    }

	/**
	 * Get a valid Wrapper for the current one if possible, i.e. try to get a
	 * new Wrapper for the wrapped KO.
	 * If this is valid, returns this.
	 * If the Wrapper cannot be revalidated, return null.
	 * 
	 * @return a valid Wrapper if possible (cf. above)
	 */
	public Wrapper revalidate () {
		if (this.tValid()) {
			// If this is valid just return this
			return this;    
		}
		else {
			Logger.error ("Couldn't revalidate Wrapper", this);
			return null;
		}
	}

    /**
	 * Force the wrapper to refetch the cached values.
	 * 
	 * This method will be called (indirectly) by the {@link KnowledgeBaseRefetch}, which
	 * synchronizes the content between different VMs (in a cluster).
	 * 
	 * By default we will forget about eventually cached Attributes.
	 */
    @Override
	public void refetchWrapper() {
        resetDAP(); // Forget about the DataAccessProxy / Physical resource
    }

    /**
     * Stable compareTo along the name of the Wrappers.
     * 
     * Will care for invalid Wrappers. In case of equal names the KO-Id
     * will be used to discriminate them.
     * 
     * In case you want to sort Wrappers by {@link #getName()} use the {@link WrapperNameComparator}. 
     */
    @Override
	public int compareTo(Wrapper anOtherWrapper) {

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
	 * Returns the String value of the attribute named aName
	 * if the value is null, an empty String is returned.
	 * 
	 * @param    aName    Attribute name.
	 * @return   Value of attribute named aName.
	 * @see      #setString(java.lang.String, java.lang.String)
	 */
    public final String getString(String aName) {
    	return ((String) this.getValue(aName));
    }

    /**
     * Returns the int value of the given attribute.
     * 
     * @param    aName    Name of the requested attribute.
     * @return   The value of the attribute or 0, if no such attribute defined.
     */
    public final int getInt(String aName) {
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
    public final Integer getInteger(String aName) {
    	return (Integer) this.getValue(aName);
    }

    /**
     * Return the boolean value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setBoolean(String, boolean)
     */
    public final boolean getBooleanValue(String aName) {
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
    public final Boolean getBoolean(String aName) {
    	return (Boolean) this.getValue(aName);
    }

    /**
     * Return the date value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setDate(String, Date)
     */
    public final Date getDate(String aName) {
    	return (Date) this.getValue(aName);
    }

    /**
     * Return the long value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setLong(String, long)
     */
    public final Long getLong(String aName) {
    	return (Long) this.getValue(aName);
    }

    /**
     * Return the long value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setLong(String, long)
     */
    public final long getLongValue(String aName) {
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
    public final Float getFloat(String aName) {
    	return (Float) this.getValue(aName);
    }

    /**
     * Return the double value of the given attribute.
     * 
     * @param    aName    The name of the requested attribute.
     * @return   The value of the requested attribute.
     * @see      #setDouble(String, Double)
     */
    public final Double getDouble(String aName) {
    	return (Double) this.getValue(aName);
    }

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getDate(String)
     */
    public final void setDate(String aName, Date aValue) {
        this.setValue(aName, aValue);
    }

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getLong(String)
     */
    public final void setLong(String aName, long aValue) {
		this.setValue(aName, Long.valueOf(aValue));
    }

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getLong(String)
     */
    public final void setLong(String aName, Long aValue) {
        this.setValue(aName, aValue);
    }

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getFloat(String)
     */
    public final void setFloat(String aName, Float aValue) {
        this.setValue(aName, aValue);
    }

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getDouble(String)
     */
    public final void setDouble(String aName, Double aValue) {
        this.setValue(aName, aValue);
    }

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getInteger(String)
     */
    public final void setInteger(String aName, int aValue) {
        this.setInteger(aName, Integer.valueOf(aValue));
    }

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getInteger(String)
     */
    public final void setInteger(String aName, Integer aValue) {
        this.setValue(aName, aValue);
    }

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName    The name of attribute to be set.
     * @param    aValue   Value of attribute to be set.
     * @see      #getBooleanValue(String)
     */
    public final void setBoolean(String aName, boolean aValue) {
        this.setBoolean(aName, aValue ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the value of the given attribute.
     * 
     * @param    aName     The name of attribute to be set.
     * @param    aValue    Value of attribute to be set.
     * @see      #getBoolean(String)
     */
    public final void setBoolean(String aName, Boolean aValue) {
        this.setValue(aName, aValue);
    }

	/**
	 * Set the value of the given attribute.
	 * 
	 * @param    aName     The name of attribute to be set.
	 * @param    aValue    Value of attribute to be set.
	 * @see      #getString(String)
	 */
	public final void setString(String aName, String aValue) {
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
	public final <T> T getReference(Class<T> dynamicType, String attributeName) {
		return tGetDataReference(dynamicType, attributeName);
	}

	/**
	 * Access to a directly referenced object.
	 * 
	 * @param self
	 *        The base object.
	 * @param dynamicType
	 *        The expected return type.
	 * @param attributeName
	 *        The name of the reference attribute.
	 * @return The referenced object, or <code>null</code>, if the reference is empty.
	 */
	public static <T> T getReference(Wrapper self, Class<T> dynamicType, String attributeName) {
		return self.tGetDataReference(dynamicType, attributeName);
	}

	/**
	 * Sets a reference attribute.
	 * 
	 * @param attributeName
	 *        The name of the reference attribute.
	 * @param value
	 *        The referenced object, or <code>null</code> to clear the reference.
	 */
	public final void setReference(String attributeName, TLObject value) {
		AbstractWrapper.setReference(this, attributeName, value);
	}

	/**
	 * Sets a reference attribute.
	 * 
	 * @param self
	 *        The base object.
	 * @param attributeName
	 *        The name of the reference attribute.
	 * @param value
	 *        The referenced object, or <code>null</code> to clear the reference.
	 */
	public static void setReference(AbstractWrapper self, String attributeName, TLObject value) {
		assert MetaObjectUtils.hasAttribute(self.tTable(), attributeName) : "Table type '"
			+ self.tTable().getName() + "' has no attribute '" + attributeName + "'.";
		self.tSetData(attributeName, value == null ? null : value.tHandle());
	}

	/**
	 * Resolve the given query with this as base object.
	 * 
	 * @return The resulting set of opposite objects.
	 */
	protected final <C extends Collection<KnowledgeAssociation>> Set<? extends TLObject> resolveWrappers(
			AbstractAssociationQuery<? extends KnowledgeAssociation, C> query) {
		return resolveWrappers(this, query);
	}
	
	/**
	 * Resolve the given query with this as base object.
	 * 
	 * @return The resulting set of opposite objects.
	 */
	protected final <C extends Collection<KnowledgeAssociation>, T extends TLObject> Set<T> resolveWrappersTyped(
			AbstractAssociationQuery<? extends KnowledgeAssociation, C> query, Class<T> wrapperType) {
		return resolveWrappersTyped(this, query, wrapperType);
	}
	
	/**
	 * Resolve the given query with the given object as base object.
	 * 
	 * @return The resulting set of opposite objects.
	 */
	public static <C extends Collection<KnowledgeAssociation>> Set<? extends TLObject> resolveWrappers(TLObject object,
			AbstractAssociationQuery<? extends KnowledgeAssociation, C> query) {
		return resolveWrappersTyped(object, query, TLObject.class);
	}

	/**
	 * Resolve the given query with the given object as base object.
	 * 
	 * @return The resulting set of opposite objects.
	 * 
	 * @throws IllegalStateException
	 *         if the given {@link Wrapper} was deleted before
	 */
	public static <C extends Collection<KnowledgeAssociation>, T extends TLObject> Set<T> resolveWrappersTyped(
			TLObject object, AbstractAssociationQuery<? extends KnowledgeAssociation, C> query, Class<T> wrapperType) {
		// TODO: Use wrapperType for checked access.
		
		KnowledgeObject ko = (KnowledgeObject) object.tHandle();
		if (!ko.isAlive()) {
			throw new IllegalStateException("Lookup for deleted object.");
		}
		return AssociationQueryUtil.resolveWrappers(ko, wrapperType, query);
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
	protected final <T extends TLObject, C> C resolveLinks(AbstractAssociationQuery<T, C> query) {
		return resolveLinks(this, query);
	}
	
	/**
	 * Resolve the given query with the given object as base object.
	 * 
	 * @param object
	 *        The base object that is pointed to by resulting objects.
	 * @param query
	 *        The query to resolve.
	 * 
	 * @return The resulting unmodifiable set of wrappers that directly point to this wrapper via
	 *         the reference attribute {@link AbstractAssociationQuery#getReferenceAttribute()} of the given query.
	 *         query.
	 */
	public static <T extends TLObject, C> C resolveLinks(TLObject object, AbstractAssociationQuery<T, C> query) {
		KnowledgeObject ko = (KnowledgeObject) object.tHandle();
		KnowledgeBase kb = ko.getKnowledgeBase();
		return kb.resolveLinks(ko, query);
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
	protected <L extends TLObject> void updateOrderedLinkSet(OrderedLinkQuery<L> attribute, List<? extends L> newOrder) {
		updateOrderedLinkSet(this, attribute, newOrder);
	}

	/**
	 * Updates the ordered link set attribute specified by the given {@link OrderedLinkQuery}.
	 * 
	 * <p>
	 * Note: The (virtual) attribute is implemented by a foreign key and order attribute in the
	 * target object pointing to the given self instance. Those attributes are updated by this call.
	 * </p>
	 * 
	 * <p>
	 * Note: Even if the value type is declared {@link List}, only sorted set values are supported
	 * (a certain item in the list must occur at most once). For full {@link List} support,
	 * {@link OrderedLinkUtil#setOrderedValue(TLObject, OrderedLinkQuery, List)} must be used.
	 * </p>
	 * 
	 * @param <L>
	 *        The type of the link object being updated.
	 * @param self
	 *        The owner object of the (virtual) attribute (implemented by foreign key attributes in
	 *        the values).
	 * @param attribute
	 *        The attribute (implemented by a foreign key and an order attribute in the in the
	 *        values).
	 * @param newValues
	 *        The ordered set of values (must not contain the same value more than once).
	 * 
	 * @see #resolveLinks(TLObject, AbstractAssociationQuery) Resolving the corresponding attribute value.
	 * @see OrderedLinkUtil#setOrderedValue(TLObject, OrderedLinkQuery, List) Storing the association externally in
	 *      additional link instances.
	 */
	public static <L extends TLObject> void updateOrderedLinkSet(TLObject self, OrderedLinkQuery<L> attribute,
			List<? extends L> newValues) {
		String ref = attribute.getReferenceAttribute();
		String orderAttr = attribute.getOrderAttribute();
		
		// Clear removed assignments.
		HashSet<L> newValuesSet = new HashSet<>(newValues);
		List<L> oldValues = AbstractWrapper.resolveLinks(self, attribute);
		for (L entry : oldValues) {
			if (!newValuesSet.contains(entry)) {
				entry.tSetDataReference(ref, null);
				entry.tSetData(orderAttr, null);
			}
		}

		// Install new assignment.
		for (L entry : newValues) {
			entry.tSetDataReference(ref, self);
		}
		OrderedLinkUtil.updateIndices(newValues, orderAttr);
	}

	/**
	 * Retrieves an ordered collection value stored in association links with an order attribute and
	 * this instance as source.
	 * 
	 * @param associationQuery
	 *        The association to retrieve links from.
	 * @return The ordered application value.
	 * 
	 * @see #resolveLinks(TLObject, AbstractAssociationQuery) Retrieving the link instances instead of the other
	 *      ends.
	 */
	protected <T extends TLObject> List<T> resolveOrderedValue(OrderedLinkQuery<KnowledgeAssociation> associationQuery,
			Class<T> targetType) {
		return resolveOrderedValue(this, associationQuery, targetType);
	}

	/**
	 * Retrieves an ordered collection value stored in association links with an order attribute and
	 * the given object as source.
	 * 
	 * @param self
	 *        The object from which the {@link OrderedLinkQuery} is resolved.
	 * @param associationQuery
	 *        The association to retrieve links from.
	 * @return The ordered application value.
	 * 
	 * @see OrderedLinkUtil#setOrderedValue(TLObject, OrderedLinkQuery, List) Updating the corresponding attribute
	 *      value for non live queries.
	 * @see #resolveLinks(TLObject, AbstractAssociationQuery) Retrieving the link instances instead of the other
	 *      ends.
	 */
	public static <T extends TLObject> List<T> resolveOrderedValue(TLObject self,
			OrderedLinkQuery<KnowledgeAssociation> associationQuery, Class<T> targetType) {
		List<KnowledgeAssociation> links = resolveLinks(self, associationQuery);
		if (associationQuery.hasLiveResult()) {
			return new LiveAssociationsEndList<>(self, associationQuery, links, targetType);
		} else {
			switch (links.size()) {
				case 0:
					return Collections.emptyList();
				default:
					ReferenceAccess<KnowledgeAssociation, T> endAccess;
					if (AssociationQueryUtil.isOutgoing(associationQuery)) {
						endAccess = ReferenceAccess.outgoingAccess(targetType);
					} else {
						endAccess = ReferenceAccess.incomingAccess(targetType);
					}
					return new MappedList<>(endAccess, links);
			}
		}
	}

	/**
	 * Get the Wrappers found at the other end of the specified KnowledgeAssociations.
	 *
	 * Any invalid associations are silently filtered out.
	 *
	 * @param kas             the Associations, never null
	 * @param aKAFilter       arbitraty filter for the KA must not be null.
	 * @param aKOFilter       arbitraty filter for the KO must not be null.
	 *
	 * @return the Wrappers never null
	 */
	public static List getWrappersFromAssociations(KAIterator kas, Filter aKAFilter, Filter aKOFilter) {
	    List theKOs = new ArrayList ();
	    
	    while (kas.hasNext ()) {
			KnowledgeItem theKO = kas.nextKO();
	        if (aKAFilter.accept(kas.currentKA())) {
	            if (aKOFilter.accept(theKO)) {
					theKOs.add(WrapperFactory.getWrapper(theKO));
	            }
	        }
	    }
	
	    return theKOs;
	}

	/**
	 * Get the Wrappers found at the other end of the specified KnowledgeAssociations.
	 *
	 * Any invalid associations are silently filtered out.
	 *
	 * @param kas             the Associations, never null
	 * @param aType           The type of KOS to return null means all.
	 *
	 * @return the Wrappers never null
	 */
	public static List getWrappersFromAssociations(KAIterator kas, String aType) {
	    List theKOs = new ArrayList ();
	    
	    while (kas.hasNext ()) {
			KnowledgeItem theKO = kas.nextKO();
	        if (aType == null || theKO.isInstanceOf(aType)) {
				theKOs.add(WrapperFactory.getWrapper(theKO));
	        }
	    }
	
	    return theKOs;
	}

	/**
	 * Verify that the specified KnowledgeObject has the correct type.
	 * <p>
	 * If everything is ok nothing happens, else an Exception is thrown.
	 * </p>
	 *
	 * @param aKO
	 *        the KnowledgeObject; may be null
	 * @param aType
	 *        the type; must not be null
	 * @throws WrapperRuntimeException
	 *         if the KnowledgeObject is null or of the wrong type
	 *
	 *         #author Michael Eriksson
	 */
	public static void verifyType(KnowledgeItem aKO, String aType) {
	    if (!aKO.isInstanceOf(aType)) {
			throw new WrapperRuntimeException("KnowledgeObject " + aKO
	                                                + " is not a " + aType);
	    }
	}

	/**
	 * Get the default knowledge base.
	 * 
	 * @return KnowledgeBase    the default knowledge base
	 */
	public static KnowledgeBase getDefaultKnowledgeBase () {
	    return (KnowledgeBaseFactory.getInstance ().getDefaultKnowledgeBase ());
	}

	/**
	 * Resolve the Wrappers for some Collection, order is preserved.
	 *
	 * @param aCol an arbitrary collection of KOs, may be null.
	 *
	 * @return A modifiable list with resolved Wrappers for the given Collection. null when aCol was null.
	 */
	public static ArrayList getWrappersFromCollection(Collection aCol) {
	    if (aCol != null) {
	        int            size   = aCol.size();
	        ArrayList      result = new ArrayList(size);
	        if (aCol instanceof RandomAccess) {
	            List theList = (List) aCol;
	            for (int i=0; i < size; i++) {
					KnowledgeItem theKO = (KnowledgeItem) theList.get(i);
					result.add(WrapperFactory.getWrapper(theKO));
	            }
	        } else for (Iterator i=aCol.iterator(); i.hasNext(); ) {
					KnowledgeItem theKO = (KnowledgeItem) i.next();
					result.add(WrapperFactory.getWrapper(theKO));
	        }
	        return result;
	    }
	    return null;
	}

	/**
	 * Resolve {@link Wrapper}s for all {@link KnowledgeItem}s returned from the given iterator.
	 * 
	 * @param iterator
	 *        The {@link Iterator} to retrieve {@link KnowledgeItem}s from.
	 * @return The list of resolved {@link Wrapper}s.
	 * 
	 * @see #getWrappersFromIterator(Class, Iterator) for a typed result.
	 */
	protected static List<Wrapper> getWrappersFromIterator(Iterator<? extends KnowledgeItem> iterator) {
	    ArrayList<Wrapper> result = new ArrayList<>();
	    while (iterator.hasNext()) {
			Wrapper wrapper = WrapperFactory.getWrapper((KnowledgeItem) iterator.next());
			result.add(wrapper);
	    }
	    return result;
	}
	
	/**
	 * Resolve {@link Wrapper}s for all {@link KnowledgeItem}s returned from the given iterator.
	 * 
	 * @param expectedType
	 *        The type of {@link Wrapper}s expected.
	 * @param iterator
	 *        The {@link Iterator} to retrieve {@link KnowledgeItem}s from.
	 * @return The list of resolved {@link Wrapper}s.
	 * 
	 * @see #getWrappersFromIterator(Iterator) for a generic lookup.
	 */
	protected static <T> List<T> getWrappersFromIterator(Class<T> expectedType, Iterator<? extends KnowledgeItem> iterator) {
	    ArrayList<T> result = new ArrayList<>();
	    while (iterator.hasNext()) {
			Wrapper wrapper = WrapperFactory.getWrapper((KnowledgeItem) iterator.next());
			result.add(CollectionUtil.dynamicCast(expectedType, wrapper));
	    }
	    return result;
	}

	/**
	 * Map given Collection of Wrappers by theire name.
	 * 
	 * Is similar to {@link CollectionUtil#toMap(Collection, com.top_logic.basic.col.Mapping) } with
	 * {@link Wrapper#NAME_MAPPING} but always returns a modifieable map.
	 *
	 * @param aCol an arbitrary Collection of Wrappers, must not be null.
	 *
	 * @return A modifiable map of Wrappers indexed by name.
	 */
	public static Map mapWrappersByName(Collection aCol) {
	    int size   = aCol != null ? aCol.size() : 0; 
	    Map result = new HashMap(size);
	    if (aCol instanceof RandomAccess) {
	        List theList = (List) aCol;
	        for (int i=0; i < size; i++) {
	            Wrapper theWrapper =  (Wrapper) theList.get(i);
	            result.put (theWrapper.getName(), theWrapper);
	        }
	    } else if (aCol != null) for (Iterator i=aCol.iterator(); i.hasNext(); ) {
	        Wrapper theWrapper =  (Wrapper) i.next();
	        result.put (theWrapper.getName(), theWrapper);
	    }
	    return result;
	}

	/**
	 * Reduce a List of Wrapper to there (unique) IDs.
	 * 
	 * @param aCol
	 *        an arbitrary Collection of Wrappers, may be null.
	 * 
	 * @return A modifiable Set of {@link KBUtils#getWrappedObjectName(TLObject)} of objects in the
	 *         given collection.
	 */
	public static Set<TLID> idSet(Collection<? extends Wrapper> aCol) {
	    if (aCol == null) {
	        return null;
	    }
	    int size   = aCol.size(); 
		Set<TLID> result = new HashSet<>(size);
	    if (aCol instanceof RandomAccess) {
			List<?> theList = (List<?>) aCol;
	        for (int i = 0; i < size; i++) {
	            Wrapper theWrapper = (Wrapper)theList.get(i);
				result.add(KBUtils.getWrappedObjectName(theWrapper));
	        }
		} else
			for (Iterator<?> it = aCol.iterator(); it.hasNext();) {
				Wrapper theWrapper = (Wrapper) it.next();
				result.add(KBUtils.getWrappedObjectName(theWrapper));
	    }
	    return result;
	}

	/**
	 * Reduce a List of Wrappers to there IDs.
	 * 
	 * @param aCol
	 *        an arbitrary Collection of Wrappers, may be null.
	 *
	 * @return An array of {@link KBUtils#getWrappedObjectName(TLObject)} of the given collection.
	 */
	public static TLID[] idArray(Collection<? extends Wrapper> aCol) {
	    if (aCol == null) {
	        return null;
	    }
	    int      size   = aCol.size(); 
		TLID[] result = new TLID[size];
	    int      i      = 0;
	    if (aCol instanceof RandomAccess) {
	        List<? extends Wrapper> theList = (List<? extends Wrapper>)aCol;
	        for (; i < size; i++) {
	            Wrapper theWrapper = theList.get(i);
				result[i] = KBUtils.getWrappedObjectName(theWrapper);
	        }
	    } else for (Wrapper theWrapper : aCol) {
				result[i++] = KBUtils.getWrappedObjectName(theWrapper);
	    }
	    return result;
	}

	/**
	 * Returns a list with the names of all attributes of this FlexWrapper.
	 * 
	 * These are the names of the attributes from the data object plus the names of the attributes
	 * of the KnowledgeObject of this FlexWrapper.
	 * 
	 * @return The list of attribute names.
	 */
	public Set<String> getAllAttributeNames() {
		return tHandle().getAllAttributeNames();
	}

	/**
	 * Returns a mapping contains all values of this wrapper.
	 * 
	 * <p>
	 * The mapping maps the name of an attribute to its value in this {@link Wrapper}.
	 * </p>
	 * 
	 * @return A map containing all values of this wrapper.
	 */
	public Map<String, Object> getAllValues() {
		Map<String, Object> result = new LinkedHashMap<>();
		KnowledgeItem handle = tHandle();
		for (String attributeName : handle.getAllAttributeNames()) {
			result.put(attributeName, getValue(attributeName));
		}
		return result;
	}

}