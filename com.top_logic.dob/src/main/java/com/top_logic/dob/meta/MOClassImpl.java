/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.InlineSet;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * Default Implementation of the MOClass interface
 *
 * @author  Klaus Halfmann / Marco Perra
 */
public class MOClassImpl extends MOStructureImpl implements MOClass {

    /** Superclass of this class, may be null. */
    private MOClass superclass;

    /** indicates abstractness of this class */
    private boolean isAbstract;

    /**
	 * Set of all classes that are assignment compatible to instances of this
	 * class.
	 * 
	 * <p>
	 * This set contains of the reflexive transitive hull of this classes
	 * superclass relation.
	 * </p>
	 */
	private Set<MOClass> cachedAncestorClasses;

	private boolean isFinal;

	/**
	 * Whether this class is a versioned type.
	 * 
	 * <p>
	 * Types are versioned by default and can be configured to be unversioned on
	 * demand.
	 * </p>
	 */
	private boolean versioned = true;
	
	private Map<Class<? extends MOAnnotation>, MOAnnotation> annotations;

	private List<MOIndex> _allIndices;

	private boolean resolved;

	/** @see #isAssociation() */
	private boolean _association;

	private Map<String, MOAttribute> _overriddenAttributes = new HashMap<>();
	
	private Map<String, MOAttribute> _cachedAllAttributes;

    /**
     * Construct a MOClass with the given name
     *
     * @param name the name of this class; see {@link MOPartImpl#MOPartImpl(String)}
     */
    public MOClassImpl(String name) {
        super(name);
    }

    @Override
	public Kind getKind() {
    	return Kind.item;
    }

    /**
     * Adds the usual semantics of "abstract" here.
     * 
     * @return true when instances of this class cannot be created. 
     */
    @Override
	public boolean isAbstract() {
        return isAbstract;
    }

    /**
     * Set the abstract flag, 
     * 
     * @param anAbstract true when instances of this class cannot be created. 
     */
    @Override
	public void setAbstract(boolean anAbstract) {
    	checkUpdate();
    	
        isAbstract = anAbstract;
    }
    
    @Override
	public boolean isFinal() {
    	return isFinal;
    }
    
    /**
     * @see #isFinal()
     */
    public void setFinal(boolean isFinal) {
    	checkUpdate();
    	
		this.isFinal = isFinal;
	}

    @Override
	public MOAttribute getAttributeOrNull(String attrName) {
		if (isFrozen()) {
			return _cachedAllAttributes.get(attrName);
		}
    	MOAttribute result = getDeclaredAttributeOrNull(attrName);
        if (result == null) {
        	if (superclass != null) {
				// fetch first overridden attributes of superclass
				result = _overriddenAttributes.get(attrName);
				if (result == null) {
					result = superclass.getAttributeOrNull(attrName);
				}
        	}
        }
        return result;
    }

    /**
     * Return the superclass of the class represented by this object.
     */
    @Override
	public final MOClass getSuperclass() {
        return superclass;
    }

    /**
	 * Sets the superclass of the class represented by this object.
	 * 
	 * @throws IllegalArgumentException
	 *         if an attempt is made to subclass a final class. 
	 */
    @Override
	public void setSuperclass(MOClass newSuperClass) {
    	checkUpdate();
        
        this.superclass = newSuperClass;
    }

	@Override
	public void overrideAttribute(MOAttribute attribute) {
		checkUpdate();

		MOAttribute clash = _overriddenAttributes.put(attribute.getName(), attribute);
		if (clash != null) {
			_overriddenAttributes.put(attribute.getName(), clash);
			StringBuilder clashMessage = new StringBuilder();
			clashMessage.append("An attribute with same name '");
			clashMessage.append(attribute.getName());
			clashMessage.append("' was registered before as override attribute: former: ");
			clashMessage.append(clash);
			clashMessage.append(", current: ");
			clashMessage.append(attribute);
			throw new IllegalArgumentException(clashMessage.toString());
		}
	}

    @Override
	public boolean isInherited(MOClass other) {
    	return this.isSubtypeOf(other) && (! this.equals(other));
    }

    @Override
	public boolean isSubtypeOf(MetaObject other) {
    	switch (other.getKind()) {
    		case ANY:
    			return true;
    		case alternative:
    			return isSpecialisationOf(other);
    		case item:
    			if (isFrozen()) {
    	    		return cachedAncestorClasses.contains(other);
    	    	} else {
    	    		MOClass ancestorOrSelf = this;
    	    		do {
    	    			if (other.equals(ancestorOrSelf)) {
    	    				return true;
    	    			}
    	    			
    	    			ancestorOrSelf = ancestorOrSelf.getSuperclass();
    	    		} while (ancestorOrSelf != null);
    	    		return false;
    	    	}
    		default:
    	    	return false;
    	}
    }

    @Override
	public boolean isSubtypeOf(String aName) {
        if (aName == null) {
        	return false;
        }
        
        MOClass ancestorOrSelf = this;
		do {
            if (aName.equals(ancestorOrSelf.getName())) {
                return true;
            }
            
            ancestorOrSelf = ancestorOrSelf.getSuperclass();
		} while (ancestorOrSelf != null);
		return false;
    }
    
	@Override
	public boolean isVersioned() {
		return versioned;
	}

	@Override
	public void setVersioned(boolean versioned) {
    	checkUpdate();
    	
		this.versioned = versioned;
	}
	
	@Override
	public List<MOIndex> getIndexes() {
		if (isFrozen()) {
			return _allIndices;
		} else {
			return computeAllIndices();
		}
	}
	
	@Override
	public MOIndex getPrimaryKey() {
		if (isFrozen()) {
			return localPrimaryKey();
		} else {
			return computePrimaryKey();
		}
	}

	private MOIndex computePrimaryKey() {
		MOIndex localPrimaryKey = localPrimaryKey();
		if (localPrimaryKey != null) {
			return localPrimaryKey;
		}
		if (superclass != null) {
			return superclass.getPrimaryKey();
		}
		return null;
	}

	private MOIndex localPrimaryKey() {
		return super.getPrimaryKey();
	}

	@Override
	public MetaObject copy() {
		MOClassImpl copy = new MOClassImpl(getName());
		copy.initFromClass(this);
		return copy;
	}
	
	protected final void initFromClass(MOClassImpl orig) {
		super.initFromStructure(orig);
		
		if (orig.annotations != null) {
			this.annotations = new HashMap<>(orig.annotations);
		}
		this.isAbstract = orig.isAbstract;
		this.isFinal = orig.isFinal;
		this.versioned = orig.versioned;
		
		this.superclass = (MOClass) typeRef(orig.superclass);

		for (MOAttribute attr : orig._overriddenAttributes.values()) {
			overrideAttribute(attr.copy());
		}
	}
	
	@Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
		if (resolved) {
			return this;
		} else {
			resolved = true;
		}
		
		if (superclass != null) {
			this.superclass = (MOClass) superclass.resolve(context);

			checkSuperClass(superclass);
		}
		
		for (MOAttribute attr : _overriddenAttributes.values()) {
			attr.resolve(context);
		}

		MetaObject result = super.resolve(context);
		
		checkDeclaredAttributes();
		checkOverriddenAttributes();

		return result;
	}

	private void checkSuperClass(MOClass newSuperClass) throws DataObjectException {
        if (newSuperClass.isFinal()) {
            throw new DataObjectException(
            	"A final class '" + newSuperClass.getName() + "' can not be sub-classed (as '" + getName() + "').");
        }

		MOClass ancestorClass = newSuperClass;
        while (ancestorClass != null)  {
            if (ancestorClass.equals(this)) {
				throw new DataObjectException("A class " + getName() + " cannot be a superclass of itself.");
            }
            ancestorClass = ancestorClass.getSuperclass();
        }

        for (MOAttribute localAttr : declaredAttributes()) {
			if (newSuperClass.hasAttribute(localAttr.getName())) {
				throw new DataObjectException("The super class '" + newSuperClass.getName() + "' of '" + getName()
					+ "' already contains the argument '"
					+ localAttr.getName() + "'.");
			}
		}
	}

	private void checkDeclaredAttributes() throws DuplicateAttributeException {
		if (superclass != null) {
			for (MOAttribute localAttr : declaredAttributes()) {
				String attributeName = localAttr.getName();
				if (superclass.hasAttribute(attributeName)) {
					throw new DuplicateAttributeException(
						"Class '" + getName() + "' redefines attribute '" + attributeName + "' from super class.");
				}
			}
		}
	}
	
	private void checkOverriddenAttributes() throws DataObjectException {
		if (_overriddenAttributes.isEmpty()) {
			return;
		}
		checkSuperClass();
		checkExistence();
		checkCacheDBSize();
		checkCompatibility();
	}

	private void checkCompatibility() throws DataObjectException {
		for (Entry<String, MOAttribute> overrideEntry : _overriddenAttributes.entrySet()) {
			MOAttribute overridingAttribute = overrideEntry.getValue();
			String attributeName = overrideEntry.getKey();
			MOAttribute overriddenAttribute = superclass.getAttributeOrNull(attributeName);
			if (overriddenAttribute instanceof MOReference) {
				if (!(overridingAttribute instanceof MOReference)) {
					StringBuilder wrongAttributeType = new StringBuilder();
					wrongAttributeType.append("Cannot override reference attribute '");
					wrongAttributeType.append(attributeName);
					wrongAttributeType.append("' with simple.");
					throw overrideFailure(wrongAttributeType.toString());
				}
				MOReference overriddenReference = (MOReference) overriddenAttribute;
				MOReference overridingReference = (MOReference) overridingAttribute;
				if (overriddenReference.isMonomorphic()) {
					if (!overridingReference.isMonomorphic()) {
						throw overrideFailure("Cannot override monomorphic reference '" + attributeName
							+ "' with polymorphic.");
					}
					if (overriddenReference.getMetaObject() != overridingReference.getMetaObject()) {
						throw overrideFailure("Cannot override monomorphic reference '" + attributeName
							+ "' with different type.");
					}
				}
				if (!overriddenReference.isBranchGlobal() && overridingReference.isBranchGlobal()) {
					throw overrideFailure("Cannot override branch local reference '" + attributeName
						+ "' with branch global.");
				}
				if (overriddenReference.getHistoryType() == HistoryType.CURRENT
					&& overridingReference.getHistoryType() != HistoryType.CURRENT) {
					throw overrideFailure("Cannot override current reference '" + attributeName
						+ "' with non current.");
				}
				if (overriddenReference.getHistoryType() == HistoryType.HISTORIC
					&& overridingReference.getHistoryType() != HistoryType.HISTORIC) {
					throw overrideFailure("Cannot override historic reference '" + attributeName
						+ "' with non historic.");
				}
				if (!overridingReference.useDefaultIndex() && overriddenReference.useDefaultIndex()) {
					throw overrideFailure("Cannot override the '" + DOXMLConstants.USE_DEFAULT_INDEX_ATTRIBUTE
						+ "' setting of the reference '" + attributeName
						+ "'. Instead declare an index with the same name as the reference to override the auto-generated index of the super class.");
				}
			} else {
				if (overridingAttribute instanceof MOReference) {
					StringBuilder wrongAttributeType = new StringBuilder();
					wrongAttributeType.append("Cannot override simple attribute '");
					wrongAttributeType.append(attributeName);
					wrongAttributeType.append("' with reference.");
					throw overrideFailure(wrongAttributeType.toString());
				}
			}
			if (overriddenAttribute.isMandatory() && !overridingAttribute.isMandatory()) {
				throw overrideFailure("Mandatory attribute '" + attributeName + "' must remain mandatory.");
			}
			if (overriddenAttribute.isInitial() && !overridingAttribute.isInitial()) {
				throw overrideFailure("Initial attribute '" + attributeName + "' must remain initial.");
			}

		}
	}

	private void checkCacheDBSize() throws DataObjectException {
		// check cache and DBSize
		List<MOAttribute> attributes = superclass.getAttributes();
		Object endAttributes = InlineSet.newInlineSet();
		for (int i = attributes.size() - 1; i >= 0; i--) {
			String superAttrName = attributes.get(i).getName();
			if (_overriddenAttributes.containsKey(superAttrName)) {
				endAttributes = InlineSet.add(String.class, endAttributes, superAttrName);
				continue;
			}
			break;
		}
		for (Entry<String, MOAttribute> overridingAttribute : _overriddenAttributes.entrySet()) {
			String attributeName = overridingAttribute.getKey();
			if (InlineSet.contains(endAttributes, attributeName)) {
				// attribute is contained in a end sequence of overridden attributes. Sizes does not
				// care.
				continue;
			}
			MOAttribute overriddenAttr = superclass.getAttributeOrNull(attributeName);
			if (overridingAttribute.getValue().getStorage().getCacheSize() != overriddenAttr.getStorage()
				.getCacheSize()) {
				StringBuilder differentCacheSize = new StringBuilder();
				differentCacheSize.append("Overridden attribute '");
				differentCacheSize.append(overriddenAttr);
				differentCacheSize.append("' and overriding attribute '");
				differentCacheSize.append(overridingAttribute.getValue());
				differentCacheSize.append("' have different cache size.");
				throw overrideFailure(differentCacheSize.toString());
			}
			if (overridingAttribute.getValue().getDbMapping().length != overriddenAttr.getDbMapping().length) {
				StringBuilder differentCacheSize = new StringBuilder();
				differentCacheSize.append("Overridden attribute '");
				differentCacheSize.append(overriddenAttr);
				differentCacheSize.append("' and overriding attribute '");
				differentCacheSize.append(overridingAttribute.getValue());
				differentCacheSize.append("' have different db Mapping.");
				throw overrideFailure(differentCacheSize.toString());
			}
		}
	}

	private void checkSuperClass() throws DataObjectException {
		// check superclass set.
		if (superclass == null) {
			StringBuilder noSuperClass = new StringBuilder();
			noSuperClass.append("Override of attributes '");
			noSuperClass.append(_overriddenAttributes.keySet());
			noSuperClass.append("' without super class.");
			throw overrideFailure(noSuperClass.toString());
		}
	}

	private void checkExistence() throws DataObjectException {
		// Check overriding attributes exists
		for (String overridenAttribute : _overriddenAttributes.keySet()) {
			if (!superclass.hasAttribute(overridenAttribute)) {
				StringBuilder noExistingAttr = new StringBuilder();
				noExistingAttr.append("Override of attribute '");
				noExistingAttr.append(overridenAttribute);
				noExistingAttr.append("' but super class '");
				noExistingAttr.append(superclass.getName());
				noExistingAttr.append("' does not have an attribute with that name.");
				throw overrideFailure(noExistingAttr.toString());
			}
		}
	}

	private DataObjectException overrideFailure(String message) throws DataObjectException {
		throw new DataObjectException("Type '" + getName() + "': " + message);
	}

	@Override
	protected void afterFreeze() {
		// Make sure that the super class is completed.
		if (superclass != null) {
			superclass.freeze();
		}
		
    	this.cachedAncestorClasses = computeAncestorClasses();

		setPrimaryKey(computePrimaryKey());

    	// Must be called after local initialization, because
		// initAttributeOwners() - which is called from the super implementation
		// does depend on this information.
    	super.afterFreeze();
    	
		initOwnerOfOverridden();
		_cachedAllAttributes = createAttributeMapping();

    	// Must be called after super.freeze(), because it depends on the index 
    	// information of the super class is available.
		_allIndices = computeAllIndices();
		optimizeOverriddenAttributes();
    }

	private HashMap<String, MOAttribute> createAttributeMapping() {
		List<MOAttribute> attributes = getAttributes();
		HashMap<String, MOAttribute> attributeCache = MapUtil.newMap(attributes.size());
		for (MOAttribute attribute : attributes) {
			attributeCache.put(attribute.getName(), attribute);
		}
		return attributeCache;
	}

	private void initOwnerOfOverridden() {
		if (_overriddenAttributes.isEmpty()) {
			return;
		}
		List<MOAttribute> attributes = getAttributes();
		int cacheIndex = 0;
		int columnIndex = 0;
		for (int n = 0, cnt = attributes.size(); n < cnt; n++) {
			MOAttribute attr = attributes.get(n);
			boolean init = _overriddenAttributes.containsKey(attr.getName());
			if (init) {
				attr.initOwner(this, cacheIndex);
			}
			cacheIndex += attr.getStorage().getCacheSize();

			DBAttribute[] dbMapping = attr.getDbMapping();
			for (DBAttribute dbAttr : dbMapping) {
				if (init) {
					dbAttr.initDBColumnIndex(columnIndex);
				}
				columnIndex++;
			}
		}
	}

	private void optimizeOverriddenAttributes() {
		switch (_overriddenAttributes.size()) {
			case 0:
				_overriddenAttributes = Collections.emptyMap();
				break;
			case 1:
				Entry<String, MOAttribute> entry = _overriddenAttributes.entrySet().iterator().next();
				_overriddenAttributes = Collections.singletonMap(entry.getKey(), entry.getValue());
				break;
			default:
				break;
		}
	}

	private List<MOIndex> computeAllIndices() {
		IndexBuilder indexBuilder = new IndexBuilder(getName());
		fill(indexBuilder);
		return indexBuilder.getIndices();
	}

    private void fill(IndexBuilder indexBuilder) {
    	if (superclass != null) {
    		((MOClassImpl) superclass).fill(indexBuilder);
    	}
    	for (MOIndex index : getLocalIndices()) {
    		indexBuilder.addIndex(index);
    	}
	}

	private List<MOIndex> getLocalIndices() {
		return super.getIndexes();
	}
	
	@Override
	protected int computeFirstColumnIndex() {
		final MOClass theSuperClass = getSuperclass();
		if (theSuperClass != null) {
			return ((DBTableMetaObject) theSuperClass).getDBColumnCount();
		} else {
			return super.computeFirstColumnIndex();
		}
	}

	@Override
	protected int computeFirstCacheIndex() {
    	final MOClass theSuperClass = getSuperclass();
		if (theSuperClass != null) {
			return theSuperClass.getCacheSize();
    	} else {
    		return super.computeFirstCacheIndex();
    	}
	}

	@Override
	protected List<MOAttribute> computeAllAttributes(List<MOAttribute> declaredAttributes) {
		MOClass theSuperClass = getSuperclass();
		
		int allAttributesCount = ((theSuperClass != null) ? theSuperClass.getAttributes().size() : 0) + declaredAttributes.size();
		ArrayList<MOAttribute> allAttributes = new ArrayList<>(allAttributesCount);
		if (theSuperClass != null) {
			List<MOAttribute> inheritedAttributes = theSuperClass.getAttributes();

			// Check for name clash with inherited attributes.
			Set<String> superNames = CollectionUtil.newSet(inheritedAttributes.size());
			for (MOAttribute inheritedAttribute : inheritedAttributes) {
				superNames.add(inheritedAttribute.getName());
			}
			for (MOAttribute ownAttribute : declaredAttributes) {
				if (superNames.contains(ownAttribute.getName())) {
					throw new IllegalStateException(
						"Dublicate attribute '" + ownAttribute.getName() + "' in '" + getName() + "'.");
				}
			}

			// Copy attributes
			for (MOAttribute inheritedAttr : inheritedAttributes) {
				MOAttribute overriddenAttr = _overriddenAttributes.get(inheritedAttr.getName());
				if (overriddenAttr != null) {
					allAttributes.add(overriddenAttr);
				} else {
					allAttributes.add(inheritedAttr);
				}
			}
		}
		allAttributes.addAll(declaredAttributes);
		return allAttributes;
	}
	
	protected HashSet<MOClass> computeAncestorClasses() {
		// Compute super class set.
    	HashSet<MOClass> ancestorClasses = new HashSet<>();
    	MOClass ancestor = this;
    	do {
    		boolean wasNew = ancestorClasses.add(ancestor);
    		if (!wasNew) {
    			// Cyclic class hierarchy.
    			throw new IllegalStateException("Cyclic inheritance hierarchy in '" + getName() + "'.");
    		}
    		ancestor = ancestor.getSuperclass();
    	} while (ancestor != null);
		return ancestorClasses;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends MOAnnotation> T getAnnotation(Class<T> annotationClass) {
		if (annotations == null) {
			return null;
		}
		return (T) annotations.get(annotationClass);
	}

	@Override
	public <T extends MOAnnotation> T removeAnnotation(Class<T> annotationClass) {
		if (annotations == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		T result = (T) annotations.remove(annotationClass);

		return result;
	}

	@Override
	public <T extends MOAnnotation> void addAnnotation(T annotation) {
		checkUpdate();
		@SuppressWarnings("unchecked")
		Class<? extends MOAnnotation> annotationClass =
			(Class<? extends MOAnnotation>) annotation.getConfigurationInterface();
		if (this.annotations == null) {
			this.annotations = new HashMap<>();
		}
		this.annotations.put(annotationClass, annotation);
	}

	@Override
	public void setAssociation(boolean isAssociation) {
		checkUpdate();
		_association = isAssociation;
	}

	@Override
	public boolean isAssociation() {
		return _association;
	}

	private static final class IndexBuilder {
		private final List<MOIndex> _indices = new ArrayList<>();
		private final Map<Tuple, Integer> _indexByAttributes = new HashMap<>();
		private final Map<String, Integer> _indexByName = new HashMap<>();
		private final String _typeName;
		private int _dropped;
		
		public IndexBuilder(String typeName) {
			_typeName = typeName;
		}

		public void addIndex(MOIndex index) {
			Integer indexPos = _indexByName.get(index.getName());
			
			Tuple attributes = TupleFactory.newTuple(index.getKeyAttributes());
			Integer clashPos = _indexByAttributes.get(attributes);
			if (clashPos != null && (!clashPos.equals(indexPos))) {
				// Same attributes, but not the same name, remove other index with same attributes.
				// 
				// Note: This code is necessary to ensure compatibility with schema definitions of 
				// the old knowledge base, where indices were not inherited (and were therefore declared 
				// in each sub-class).  
				Logger.warn(
					"Invalid indices in type '" + _typeName + "': " + 
					"More than one index for the same attributes: " + attributes, MOClassImpl.class);
				
				MOIndex droppedIndex = _indices.set(clashPos, null);
				_indexByName.remove(droppedIndex.getName());
				_dropped++;
			}
			
			if (indexPos != null) {
				_indexByAttributes.put(TupleFactory.newTuple(index.getKeyAttributes()), indexPos);
				_indices.set(indexPos, index);
			} else {
				int newPos = _indices.size();
				_indexByAttributes.put(TupleFactory.newTuple(index.getKeyAttributes()), newPos);
				_indexByName.put(index.getName(), newPos);
				_indices.add(index);
			}
		}
		
		public List<MOIndex> getIndices() {
			if (_dropped > 0) {
				ArrayList<MOIndex> result = new ArrayList<>(_indices.size() - _dropped);
				for (MOIndex index : _indices) {
					if (index != null) {
						result.add(index);
					}
				}
				return result; 
			} else {
				return _indices;
			}
		}
	} 

 }
