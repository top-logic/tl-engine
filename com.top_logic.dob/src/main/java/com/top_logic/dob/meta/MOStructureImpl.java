
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.DataObjectImpl;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Basic meta object for structured objects.
 *
 * This implementation must preserve the order of attributes, this
 * is necessary for Database implementations which need this.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class MOStructureImpl extends AbstractMetaObject implements DBTableMetaObject {

	//  Mandatory, Immutable, , Size, precision, PKeyIndex
        
    /** Name eventually used for a database table */
    private String dbName;
    
	/**
	 * List locally declared attributes.
	 */
	private List<MOAttribute> cachedDeclaredAttributes;

    /** The map containing the attributes defined in this structure.*/
    private List<MOAttribute> cachedAllAttributes;
    
    private List<MOReference> cachedAllReferences;
    
	/** The map containing the attributes defined in this structure. */
	private List<MOAttribute> cachedCacheAttributes;

    private int cacheSize;
    /** An array containing the attribute names for faster access */
    private String [] cachedAttributeNames;
    
    /** A list containing all DB-relevant attributes. */
    private List<DBAttribute> cachedAllDbAtttributes;
    private int cachedDbSize;
    

    /** A map indexed by name mirroring the attributes, for faster access. */
    private final LinkedHashMap<String, MOAttribute> attributesByName;

    /** List with the (optional) indexes for this class */
    private List<MOIndex> lazyIndexes;
    
    /** Hint for the database to store the table along the primary key */
    private boolean pkeyStorage;

    /** Hint for the database to compress the primary key. */
	private int compress = DOXMLConstants.NO_DB_COMPRESS;

	private MOIndex primaryKey;

	private boolean _multipleBranches;

    /**
     * Generate Structure with given name and size.
     *
     * @param   name    The name of this meta object.
     * @param   size    the expected number of Attributes
     */
    public MOStructureImpl (String name, int size) {
        super (name);
        attributesByName = new LinkedHashMap<>(size);
    }

    /**
     * The attribute hash will be initialized with the size of 10 elements.
     *
     * @param    name    The name of this meta object.
     */
    public MOStructureImpl (String name) {
        this (name, 10);
    }

    @Override
	public Kind getKind() {
    	return Kind.struct;
    }

    @Override
	public void addAttribute(MOAttribute aMOAttribute) throws DuplicateAttributeException {
    	checkUpdate();
    	
		// Check for duplicate attribute names locally. Note: hasAttribute() must not be called
		// during type construction, because the super class may not yet be reolved.
        String name = aMOAttribute.getName ();
        if (this.attributesByName.containsKey(name)) {
             throw new DuplicateAttributeException(name);
        }
        
        this.attributesByName.put(name, aMOAttribute);
		if (aMOAttribute instanceof MOReference) {
			MOIndex index = ((MOReference) aMOAttribute).getIndex();
			if (index != null) {
				addIndex(index);
			}
		}
    }

	/**
	 * Removes the attribute with the given name, if it does exist locally in
	 * this type.
	 * 
	 * @param attributeName
	 *        The attribute name to remove.
	 * @return Whether this type had an attribute with the given name before.
	 */
    public boolean removeAttribute (String attributeName) {
    	checkUpdate();
    	
    	return this.attributesByName.remove(attributeName) != null;
    }

    @Override
	public final int getCacheSize() {
    	if (isFrozen()) {
    		return cacheSize;
    	} else {
    		return computeCacheSize(getAttributes());
    	}
    }
    
    @Override
	public final int getDBColumnCount() {
    	if (isFrozen()) {
    		return cachedDbSize;
    	} else {
    		return computeDBColumnCount(getDBAttributes());
    	}
    }
    
    /**
     * Returns a vector of all MOAttribute objects reflecting just the attributes
     * declared by the class represented by this object.
     */
    @Override
	public final List<MOAttribute> getDeclaredAttributes() {
    	if (isFrozen()) {
    		return cachedDeclaredAttributes;
    	} else {
    		return computeDeclaredAttributes();
    	}
    }

    /**
     * Returns a vector of MOAttribute objects reflecting the attributes
     * declared by the structure represented by this object.
     *
     * @return    A Vector of all attributes of this instance.
     */
    @Override
	public final List<MOAttribute> getAttributes () {
    	if (isFrozen()) {
    		return cachedAllAttributes;
    	} else {
    		return computeAllAttributes(getDeclaredAttributes());
    	}
    }
    
    @Override
	public List<MOReference> getReferenceAttributes() {
    	if (isFrozen()) {
    		return cachedAllReferences;
    	} else {
    		return MetaObjectUtils.filterReferences(getAttributes());
    	}
    }

    /**
     * Returns the names of all attributes, this instance can contain. The 
     * returned array is ordered by name, so you can use a binary search.
     *
     * @return    The array of all known attribute names (may be null).
     */
    @Override
	public final String [] getAttributeNames () {
    	if (isFrozen()) {
    		return this.cachedAttributeNames;
    	} else {
    		return computeAttributeNames(this.getAttributes());
    	}
    }

	public final List<? extends MOAttribute> getCacheAttributes() {
		if (isFrozen()) {
			return this.cachedCacheAttributes;
		} else {
			return MetaObjectUtils.filterCachedAttributes(getAttributes());
		}
	}

    /**
     * Returns a MOAttribute that match to attrName.
     * If it not exist or if this object is not a structure
     * or not a class null be returned.
     *
     * @param     aKey    The name of the requested attribute.
     * @return    The requested attribute
     */
    @Override
	public final MOAttribute getAttribute (String aKey) throws NoSuchAttributeException {
        MOAttribute theAttr = getAttributeOrNull(aKey);

        if (theAttr == null) {
            throw new NoSuchAttributeException (
                "The attribute \"" + aKey + "\" does not exist in " + this.getName());
        }
        return theAttr;
    }

	@Override
	public MOAttribute getAttributeOrNull(String name) {
		return getDeclaredAttributeOrNull(name);
	}
	
	@Override
	public final MOAttribute getDeclaredAttributeOrNull(String name) {
		return attributesByName.get(name);
	}

	protected final Collection<MOAttribute> declaredAttributes() {
		return attributesByName.values();
	}
 
    @Override
	public final boolean hasAttribute (String attrName) {
        return getAttributeOrNull(attrName) != null;
    }
    
    @Override
	public final boolean hasDeclaredAttribute(String attrName) {
    	return getDeclaredAttributeOrNull(attrName) != null;
    }
   
    /**
     * Returns the list of {@link MOIndex}es for this type.
     */
    @Override
	public List<MOIndex> getIndexes () {
        return lazyIndexes != null ? lazyIndexes : Collections.<MOIndex>emptyList();
    }
    
    /**
     * @see com.top_logic.dob.sql.DBTableMetaObject#isPKeyStorage()
     */
    @Override
	public boolean isPKeyStorage() {
        return pkeyStorage;
    }
    
    /**
     * @see com.top_logic.dob.sql.DBTableMetaObject#getCompress()
     */
    @Override
	public int getCompress() {
        return compress;
    }
    
    /**
     * The lazily initialized list of {@link DBIndex indices} of this type, never <code>null</code>.
     */
	protected List<MOIndex> internalGetIndexList() {
        if (lazyIndexes == null) {
        	lazyIndexes = new ArrayList<>(5);
        }
		return lazyIndexes;
	}
    
    /** Add new Indexes */
    public void addIndex(MOIndex anIndex) {
    	checkUpdate();
    	
        internalGetIndexList().add(anIndex);   
    }
    
	/**
	 * Service method to create a primary key from the given columns and set it.
	 * 
	 * @param attributes
	 *        Attribute to contain in the key in the given order.
	 */
	public final void setPrimaryKey(DBAttribute... attributes) {
		setPrimaryKey(MOIndexImpl.newPrimaryKey(attributes));
	}

	/**
	 * Sets primary index.
	 * 
	 * @see MOIndexImpl#newPrimaryKey(DBAttribute...)
	 */
	public void setPrimaryKey(MOIndex primaryKey) {
		checkUpdate();

		this.primaryKey = primaryKey;
	}

	@Override
	public DBTableMetaObject getDBMapping() {
		return this;
	}

    /**
     * Returns a name suiteable as Table--Name for a Database.
     */
    @Override
	public String getDBName () {
        if (dbName == null) {
            dbName = SQLH.mangleDBName(getName());
        }
        return dbName;    
    }
    
    /**
     * Returns an List of DBAttributes for a DatabaseTable.
     *<p>
     * Order of the List is important since the layout of the table will
     * be derived from it. It can return other values than 
     *  {@link #getAttribute(String)}
     * does (e.g. a primary key derived for the getUniqueId method(). )
     * </p><p>
     *   Subclasses may choose to add addition DB-Only attributes here.
     * </p>
     * 
     * BHU: DBAttributes can be different than Attributes
     *      a) you want to hide DB-Attributes used internally only
     *      b) a Normal Attribute may result in two ore more DBAttributes (e.g. foreign key)
     * not deprecated use {@link #getAttributes()}
     */
     @Override
	public final List<DBAttribute> getDBAttributes() {
    	 if (isFrozen()) {
    		 return cachedAllDbAtttributes;
    	 } else {
    		 return computeAllDbAttributes(this.getAttributes());
    	 }
     }
     

    /**
     * Accessor for DBName. 
     * See e.g. <code>com.top_logic.knowledge.service.xml.KnowledgeBaseImporter</code>.
     */
    public void setDBName (String aName) {
    	checkUpdate();
        dbName = aName;
    }
    
    /**
     * This method sets the pkeyStorage.
     *
     * @param    usePKeyStorage    The pkeyStorage to set.
     */
    public void setPkeyStorage(boolean usePKeyStorage) {
        this.pkeyStorage = usePKeyStorage;
    }
    
    /**
     * This method sets the compress.
     *
     * @param    aCompress    The compress to set.
     */
    public void setCompress(int aCompress) {
        this.compress = aCompress;
    }
    
    /**
     * Compute the caches when we know we are stable.
     */
    @Override
	protected void afterFreeze() {
		super.afterFreeze();
		
    	this.cachedDeclaredAttributes = computeDeclaredAttributes();
	
		this.cachedAllAttributes = computeAllAttributes(cachedDeclaredAttributes);
		this.cachedAllReferences = MetaObjectUtils.filterReferences(cachedAllAttributes);
		this.cachedCacheAttributes = MetaObjectUtils.filterCachedAttributes(cachedAllAttributes);
		this.cacheSize = computeCacheSize(cachedAllAttributes);
		this.cachedAttributeNames = computeAttributeNames(cachedAllAttributes);
		this.cachedAllDbAtttributes  = computeAllDbAttributes(cachedAllAttributes);
		this.cachedDbSize  = computeDBColumnCount(cachedAllDbAtttributes);

		initAttributeOwner(cachedDeclaredAttributes);
	}

    protected final void initAttributeOwner(List<MOAttribute> declaredAttributes) {
    	int cacheIndex = computeFirstCacheIndex();
		int columnIndex = computeFirstColumnIndex();
    	
    	// Note: Must be deferred upon freeze, because one can remove attributes.
		for (int n = 0, cnt = declaredAttributes.size(); n < cnt; n++) {
			MOAttribute attr = declaredAttributes.get(n);
			
			attr.initOwner(this, cacheIndex);
			cacheIndex += attr.getStorage().getCacheSize();
			
			DBAttribute[] dbMapping = attr.getDbMapping();
			for (DBAttribute dbAttr : dbMapping) {
				dbAttr.initDBColumnIndex(columnIndex);
				columnIndex++;
			}
		}
    }
    
	protected int computeFirstColumnIndex() {
		return 0;
	}

	protected int computeFirstCacheIndex() {
		return 0;
	}

    protected String[] computeAttributeNames(List<MOAttribute> allAttributes) {
		int attributeCount = allAttributes.size();
		String[] result = new String[attributeCount];
		for (int i = 0; i < attributeCount; i++) {
			result[i] = allAttributes.get(i).getName();
		}
		
		return result;
	}

	protected int computeCacheSize(List<MOAttribute> attributes) {
		int size = 0;
		for (MOAttribute attribute : attributes) {
			size += attribute.getStorage().getCacheSize();
		}
		return size;
	}

	protected final List<MOAttribute> computeDeclaredAttributes() {
		return new ArrayList<>(declaredAttributes());
	}

	protected List<MOAttribute> computeAllAttributes(List<MOAttribute> declaredAttributes) {
		return declaredAttributes;
	}
	
	/**
	 * Computes the number of columns in the database.
	 */
	protected int computeDBColumnCount(List<DBAttribute> dbAtttributes) {
		return dbAtttributes.size();
	}

	/**
	 * Returns a list of {@link MOAttribute} which has columns in the database.
	 * 
	 * @param attributes
	 *        a List of {@link MOAttribute} to filter
	 */
	protected List<DBAttribute> computeAllDbAttributes(List<MOAttribute> attributes) {
		ArrayList<DBAttribute> result = new ArrayList<>(attributes.size());
		for (MOAttribute attr : attributes) {
			DBAttribute[] dbMapping = attr.getDbMapping();
			for (DBAttribute dbAttr : dbMapping) {
				result.add(dbAttr);
			}
		}
		return result;
	}

    @Override
	public MOIndex getPrimaryKey() {
		return primaryKey;
    }

	@Override
	public MetaObject copy() {
		MOStructureImpl copy = new MOStructureImpl(getName(), attributesByName.size());
		copy.initFromStructure(this);
		return copy;
	}

	protected final void initFromStructure(MOStructureImpl orig) {
		try {
			for (MOAttribute attr : orig.declaredAttributes()) {
				addAttribute(attr.copy());
			}
		} catch (DuplicateAttributeException ex) {
			throw new UnreachableAssertion("No duplicates in source attributes.", ex);
		}
		
		this.compress = orig.compress;
		this.dbName = orig.dbName;
		this.pkeyStorage = orig.pkeyStorage;

		if (orig.primaryKey != null) {
			this.primaryKey = orig.primaryKey.copy();
		}
		
		if (orig.lazyIndexes != null) {
			ArrayList<MOIndex> indices = new ArrayList<>(orig.lazyIndexes.size());
			for (MOIndex index : orig.lazyIndexes) {
				indices.add(index.copy());
			}
			this.lazyIndexes = indices;
		}
	}
	
	@Override
	public boolean multipleBranches() {
		return _multipleBranches;
	}

	@Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
		resolveAttributes(context);
		resolveIndices();
		_multipleBranches = context.multipleBranches();
		return this;
	}

	private void resolveAttributes(TypeContext context) throws DataObjectException {
		for (MOAttribute attribute : declaredAttributes()) {
			attribute.resolve(context);
		}
	}

	private void resolveIndices() {
		if (primaryKey != null) {
			primaryKey = primaryKey.resolve(this); 
		}
		
		if (lazyIndexes != null) {
			for (int n = 0, cnt = lazyIndexes.size(); n < cnt; n++) {
				lazyIndexes.set(n, lazyIndexes.get(n).resolve(this));
			}
		}
	}
    

    @Override
	public String toString() {
    	StringBuffer result = new StringBuffer();
    	
    	result.append(getName());
    	result.append(' ');
    	result.append('{');
    	
    	for (Iterator<MOAttribute> it = this.getAttributes().iterator(); it.hasNext(); ) {
    		MOAttribute attr = it.next();
    		
    		result.append(attr.getName());
    		result.append(':');
    		result.append(' ');
    		
    		result.append(attr.getMetaObject().toString());
    		result.append(';');
    		if (it.hasNext()) {
    			result.append(' ');
    		}
    	}
    	
    	result.append('}');
    	
		return result.toString();
    }
    
	/**
	 * Service method to read the values of all attributes of this structure into a cache and
	 * returns it
	 * 
	 * @param pool
	 *        Database which was used to create the given result.
	 * @param dbResult
	 *        the result to read values from
	 * @param dbOffset
	 *        the offset in the result set
	 * @param item
	 *        The item to read cache values for
	 * @throws SQLException
	 *         when accessing {@link ResultSet} fails.
	 */
	public final void readToCache(ConnectionPool pool, ResultSet dbResult, int dbOffset, DataObjectImpl item)
			throws SQLException {
		Object[] storage = item.getStorage();
    	final List<MOAttribute> dbAttributes = getAttributes();
    	for (MOAttribute attr : dbAttributes) {
			attr.getStorage().loadValue(pool, dbResult, dbOffset, attr, item, storage, AttributeStorage.NO_CONTEXT);
    	}
    }

}
