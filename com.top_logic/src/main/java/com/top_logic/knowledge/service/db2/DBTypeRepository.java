/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.DOFactory;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAlternative;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.MetaObject.Kind;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.AbstractTypeSystem;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.MOReferenceInternal;
import com.top_logic.knowledge.service.BasicTypes;


/**
 * {@link DBKnowledgeBase} internal implementation of {@link MORepository}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBTypeRepository extends AbstractTypeSystem implements MORepository {

	/** Map of {@link MetaObject} index by name. */
    private Map<String, MetaObject> metaMap;

    /**
	 * Map of {@link MOClass}es in registered in this repository to a list of
	 * their concrete subtypes that are also registered in this repository.
	 */
	private final HashMap<MetaObject, List<MetaObject>> compatibleTypes;

	/** @see #getSystemTypes() */
	private final List<MOKnowledgeItem> systemTypes = new ArrayList<>();

	/** @see #getUnversionedItemApplicationTypes() */
	private final List<MOKnowledgeItem> _unversionedItemTypes = new ArrayList<>();

	private final DBHelper sqlDialect;

	private final boolean _multipleBranches;

	public DBTypeRepository(DBHelper sqlDialect, boolean multipleBranches) {
		this.sqlDialect = sqlDialect;
		_multipleBranches = multipleBranches;
		this.metaMap = new HashMap<>();
		this.compatibleTypes = new HashMap<>();
		
		initPrimitiveTypes();
	}
    
    private void initPrimitiveTypes() {
    	try {
			this.addMetaObject(MOPrimitive.BOOLEAN);
			this.addMetaObject(MOPrimitive.INTEGER);
			this.addMetaObject(MOPrimitive.LONG);
			this.addMetaObject(MOPrimitive.FLOAT);
			this.addMetaObject(MOPrimitive.DOUBLE);
			this.addMetaObject(MOPrimitive.STRING);
			this.addMetaObject(MOPrimitive.DATE);
			this.addMetaObject(MOPrimitive.BYTE);
			this.addMetaObject(MOPrimitive.SHORT);
			this.addMetaObject(MOPrimitive.CHARACTER);
			this.addMetaObject(MOPrimitive.TLID);
		} catch (DuplicateTypeException ex) {
			throw new UnreachableAssertion("Types with unique names added.");
		}
	}

	public DOFactory getFactory (String aObjectType) throws UnknownTypeException{
        throw new UnsupportedOperationException();
    }

	public DOFactory getFactory(MetaObject metaObject) {
        throw new UnsupportedOperationException();
	}

	@Override
	public MetaObject getTypeOrNull(String typeName) {
		MetaObject result = metaMap.get(typeName);
        
		if (result != null) {
			return result;
		}
		try {
			return MOCollectionImpl.createType(this, typeName);
		} catch (IllegalArgumentException ex) {
			return null;
		}
    }

    @Override
	public void addMetaObject(MetaObject aMetaObject) throws DuplicateTypeException {
    	String name = aMetaObject.getName();
		if (! name.equals(aMetaObject.getName())) {
			// TODO BHU: Change signature to addMetaObject(MetaObject).
			throw new IllegalArgumentException(
				"Type registered under wrong name: key='" + name + "', type='" + aMetaObject.getName() + "'");
		}

        MetaObject oldValue = metaMap.put(name, aMetaObject);
        if (oldValue != null) {
        	metaMap.put(name, oldValue);
        	throw new DuplicateTypeException(
        		"Type '" + name + "' imported from '" + aMetaObject.getDefiningResource() + "' already defined in '" + oldValue.getDefiningResource() + "'.");
        }
	}

	private void indexType(MOClass aMetaObject) {
		MOClass ancestor = aMetaObject.getSuperclass();
		if (ancestor != null) {
			// Check, whether the super type is already registered.
			MetaObject registeredAncestorType;
			try {
				registeredAncestorType = getMetaObject(ancestor.getName());
			} catch (UnknownTypeException e) {
				throw new IllegalArgumentException(
					"The super class ('" + ancestor.getName() + "') of a class ('" + aMetaObject.getName()
						+ "') must be registered before the class itself is registered.");
			}

			// Check, whether registerd super type and super type reference match.
			if (registeredAncestorType != ancestor) {
				throw new IllegalArgumentException(
					"Inconsistent type hierarchy. The super class of ('" + aMetaObject.getName()
						+ "') does not match the registerd type ('" + registeredAncestorType.getName() + "').");
			}
		}

		MOClass ancestorOrSelf = aMetaObject;
		if (!ancestorOrSelf.isAbstract()) {
			// Update the sub type map by adding the new type to the list of
			// concrete sub types of all of its ancestor types.
			do {
				List<MetaObject> subtypesOfAncestor = this.compatibleTypes.get(ancestorOrSelf);
				subtypesOfAncestor.add(aMetaObject);
				ancestorOrSelf = ancestorOrSelf.getSuperclass();
			} while (ancestorOrSelf != null);
		}
	}

	private void initDBAccess(MetaObject aMetaObject) {
		if (aMetaObject.getKind() == Kind.item) {
        	MOKnowledgeItemImpl type = (MOKnowledgeItemImpl) aMetaObject;
        	
        	if (! type.isAbstract()) {
				DBAccess dbAccess = MOKnowledgeItemUtil.getDBAccessFactory(type).createDBAccess(sqlDialect, type, this);
				type.setDBAccess(dbAccess);
        	}
        }
	}

	public String getLinkEndFieldName(int endIndex) {
		return "e" + endIndex;
	}
//
//	private static Set getPrimitiveAttributes(HashSet attributes) {
//		HashSet result = new HashSet();
//
//		fillPrimitiveAttributes(result, attributes);
//		
//		return result;
//	}

	private static void fillPrimitiveAttributes(Collection<MOAttribute> result,
			Collection<? extends MOAttribute> attributes) {
		for (Iterator<? extends MOAttribute> it = attributes.iterator(); it.hasNext();) {
			MOAttribute attr = it.next();
		
			MetaObject attrType = attr.getMetaObject();
			if (MetaObjectUtils.isPrimitive(attrType)) {
				result.add(attr);
			} else {
				fillPrimitiveAttributes(result, MetaObjectUtils.getAttributes(attrType));
			}
		}
	}

	@Override
	public MetaObject getMOCollection(String rawType, String elementTypeName) throws UnknownTypeException {
        throw new UnknownTypeException("Not supported by this Respository");    
    }

    @Override
	public boolean containsMetaObject (MetaObject aMetaObject) {
        return metaMap.containsKey(aMetaObject.getName());
    }
	
    /**
	 * Returns the names of all MetaObjects known in this repository.
	 * 
	 * @return a List of Strings representing the MetaObjects' names.
	 */
	@Override
	public List<String> getMetaObjectNames() {
		Set<String> keys = metaMap.keySet();
		List<String> typeNames = new ArrayList<>(keys.size());
		for (MetaObject type : metaMap.values()) {
			if (!MetaObjectUtils.isItemType(type)) {
				continue;
			}
			
			typeNames.add(type.getName());
		}
		return typeNames;
	}
	
	@Override
	public Collection<? extends MetaObject> getMetaObjects() {
		Collection<MetaObject> typeNames = new ArrayList<>(metaMap.size());
		for (MetaObject type : metaMap.values()) {
			if (!MetaObjectUtils.isItemType(type)) {
				continue;
			}

			typeNames.add(type);
		}
		return Collections.unmodifiableCollection(typeNames);
	}

	@Override
	public MOClass getItemType() {
		return (MOClass) metaMap.get(BasicTypes.ITEM_TYPE_NAME);
	}

	private MOClass getAssociationType() {
		return (MOClass) metaMap.get(BasicTypes.ASSOCIATION_TYPE_NAME);
	}

	@Override
	public List<? extends MetaObject> getConcreteSubtypes(MetaObject baseType) {
		if (MetaObjectUtils.isItemType(baseType)) {
			return compatibleTypes.get(baseType);
		} else {
			return Collections.singletonList(baseType);
		}
	}

	@Override
	public void resolveReferences() throws DataObjectException {
		for (Entry<String, MetaObject> entry : metaMap.entrySet()) {
			MetaObject resolvedType = entry.getValue().resolve(this);
			entry.setValue(resolvedType);

			// Install a new sub type list for the new type.
			this.compatibleTypes.put(resolvedType, new ArrayList<>());
		}
		final Collection<MetaObject> allTypes = metaMap.values();
		for (MetaObject type : allTypes) {
			if (type.isFrozen()) {
				continue;
			}
			initDBAccess(type);
		}

		for (MetaObject type : allTypes) {
			type.freeze();
		}

		indexTypes(allTypes);

		systemTypes.clear();
		_unversionedItemTypes.clear();
		MetaObject itemType = getType(BasicTypes.ITEM_TYPE_NAME);
		for (MetaObject type : allTypes) {
			if (type instanceof MOKnowledgeItem) {
				MOKnowledgeItem moKnowledgeItem = (MOKnowledgeItem) type;
				if (moKnowledgeItem.isSystem()) {
					assert !systemTypes.contains(moKnowledgeItem) : moKnowledgeItem + " already registered as system";
					systemTypes.add(moKnowledgeItem);
				} else {
					// System types may be unversioned but they don't care.
					if (moKnowledgeItem.isSubtypeOf(itemType) && !moKnowledgeItem.isVersioned()) {
						_unversionedItemTypes.add(moKnowledgeItem);
					}
				}
			}
		}
	}

	private void indexTypes(final Collection<MetaObject> allTypes) {
		Set<MOAlternative> alternatives = new HashSet<>();
		MOAlternative someAlternative = null;
		for (MetaObject type : allTypes) {
			switch (type.getKind()) {
				case item:
					indexType((MOClass) type);
					break;
				case alternative:
					someAlternative = (MOAlternative) type;
					alternatives.add(someAlternative);
					break;
				default:
					continue;
			}
		}
		if (someAlternative != null) {
			indexAlternatives(alternatives, someAlternative);
		}
	}

	private void indexAlternatives(Set<MOAlternative> alternatives, MOAlternative someAlternative) {
		if (!alternatives.remove(someAlternative)) {
			return;
		}
		LinkedHashSet<MetaObject> concreteSubClasses = new LinkedHashSet<>();
		for (MetaObject specialisation : someAlternative.getSpecialisations()) {
			if (MetaObjectUtils.isAlternative(specialisation)) {
				indexAlternatives(alternatives, (MOAlternative) specialisation);
			}
			concreteSubClasses.addAll(compatibleTypes.get(specialisation));
		}
		List<MetaObject> concreteTypesForAlternative = compatibleTypes.get(someAlternative);
		assert concreteTypesForAlternative.isEmpty() : "Duplicate initialisation of concrete types of alternatives";
		concreteTypesForAlternative.addAll(concreteSubClasses);
	}

	/**
	 * Returns a collection containing all registered {@link MOKnowledgeItem} which are
	 * {@link MOKnowledgeItem#isSystem() system types}.
	 */
	Collection<? extends MOKnowledgeItem> getSystemTypes() {
		return systemTypes;
	}

	/**
	 * Returns a mapping from the known non-abstract classes to the set of all reference attribute
	 * registered in that class.
	 */
	Map<MOClass, Collection<MOReferenceInternal>> getTypesWithReferenceAttribute() {
		Collection<MetaObject> allMetaObjects = metaMap.values();
		HashMap<MOClass, Collection<MOReferenceInternal>> result =
			new HashMap<>();
		for (MetaObject meta : allMetaObjects) {
			if (meta instanceof MOClass) {
				MOClass moClass = (MOClass) meta;
				if (moClass.isAbstract()) {
					continue;
				}
				List<MOAttribute> allAttributes = moClass.getAttributes();
				List<MOReferenceInternal> referenceAttributes = null;
				for (MOAttribute attr : allAttributes) {
					if (attr instanceof MOReferenceInternal) {
						if (referenceAttributes == null) {
							referenceAttributes = new ArrayList<>();
						}
						referenceAttributes.add((MOReferenceInternal) attr);
					}
				}
				if (referenceAttributes != null) {
					Collection<MOReferenceInternal> clash = result.put(moClass, referenceAttributes);
					assert clash == null : moClass + " occurs twice in  all " + MetaObject.class.getSimpleName();
				}
			}
		}
		return result;
	}

	/**
	 * All unversioned application types, i.e. subclasses of {@link #getItemType() item type} that
	 * are not {@link MOClass#isVersioned() versioned}.
	 * 
	 * <p>
	 * The returned list contains {@link MOClass#isAbstract() abstract} and concrete types.
	 * </p>
	 */
	Collection<? extends MOKnowledgeItem> getUnversionedItemApplicationTypes() {
		return _unversionedItemTypes;
	}

	@Override
	public boolean multipleBranches() {
		return _multipleBranches;
	}

	/**
	 * Creates a new {@link DBTypeRepository}.
	 * 
	 * @param schemaSetup
	 *        The {@link SchemaSetup} to fill types into the returned {@link MORepository}.
	 * @param versioning
	 *        Whether types support versioning.
	 */
	public static DBTypeRepository newRepository(DBHelper sqlDialect, SchemaSetup schemaSetup, boolean versioning)
			throws ConfigurationException, DataObjectException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(DBKnowledgeBase.class);
		DBTypeRepository repository = newRepository(context, sqlDialect, schemaSetup, versioning);
		context.checkErrors();
		return repository;
	}

	/**
	 * Creates a new {@link DBTypeRepository}.
	 * 
	 * @param schemaSetup
	 *        The {@link SchemaSetup} to fill types into the returned {@link MORepository}.
	 * @param versioning
	 *        Whether types support versioning.
	 */
	public static DBTypeRepository newRepository(InstantiationContext context, DBHelper sqlDialect,
			SchemaSetup schemaSetup, boolean versioning) throws ConfigurationException {
		DBTypeRepository result = new DBTypeRepository(sqlDialect, schemaSetup.getConfig().hasMultipleBranches());
		MOFactory typeFactory = new DBKnowledgeTypeFactory(versioning);
		schemaSetup.createTypes(context, result, typeFactory);
		context.checkErrors();
		result.resolveReferences();
		return result;
	}

}
