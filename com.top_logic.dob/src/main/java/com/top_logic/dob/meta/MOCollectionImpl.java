
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.regex.Matcher;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;

/**
 * This is a template for classes....
 *
 * TODO the name of the collection should be the name of this collection an not
 * the kind of collection this object represent.Therby the different kinks are
 * LIST, ARRAY, SET.
 * @author 	Marco Perra
 */
public class MOCollectionImpl extends AbstractMetaObject implements MOCollection {
	
	@Override
	public Kind getKind() {
		return Kind.collection;
	}

	private final String rawType;
    private MetaObject elementType;

    private MOCollectionImpl(String rawType, MetaObject elementType) {
        super(createName(rawType, elementType));
		
        this.rawType = rawType;
		this.elementType = elementType;
    }

    public static MOCollection createCollectionType(MetaObject elementType) {
        return new MOCollectionImpl(MOCollection.COLLECTION, elementType);
    }

    public static MOCollection createListType(MetaObject elementType) {
        return new MOCollectionImpl(MOCollection.LIST, elementType);
    }

    public static MOCollection createSetType (MetaObject elementType) {
        return new MOCollectionImpl(MOCollection.SET, elementType);
    }

	/**
	 * Creates a name for an {@link MOCollection} with the given raw type and
	 * given element type.
	 * 
	 * @param rawType
	 *        the raw type. Either {@link MOCollection#COLLECTION},
	 *        {@link MOCollection#LIST}, or {@link MOCollection#SET}
	 * @param elementType
	 *        the element type of the resulting {@link MOCollection}
	 * 
	 * @return a name adequate for {@link #createCollectionType(MetaObject)}
	 */
    public static String createName(String rawType, MetaObject elementType) {
    	String name = rawType + '<' + elementType.getName() + '>';
    	assert MOCollection.MO_COLLECTION_NAME_PATTERN.matcher(name).matches();
    	return name;
    }
    
	/**
	 * Creates a {@link MOCollection} with the given name
	 * 
	 * @param typeSystem
	 *        callback to determine entry type
	 * @param name
	 *        name which matches the name convention, e.g. a name returned by
	 *        {@link #createName(String, MetaObject)}.
	 * 
	 * @return the newly constructed {@link MOCollection}
	 * 
	 * @throws UnknownTypeException
	 *         if entry type does not exists.
	 * @throws IllegalArgumentException
	 *         if name has wrong structure.
	 * 
	 * @see MOCollection#MO_COLLECTION_NAME_PATTERN
	 * @see MOCollection#getName()
	 */
    public static MOCollection createType(MORepository typeSystem, String name) throws UnknownTypeException {
    	Matcher collNameMatcher = MOCollection.MO_COLLECTION_NAME_PATTERN.matcher(name);
    	if (!collNameMatcher.matches()) {
    		throw new IllegalArgumentException("Name is not matched by the name pattern for " + MOCollection.class + ": " + name);
    	}
    	String entryTypeName = collNameMatcher.group(2);
    	MetaObject entryType = typeSystem.getMetaObject(entryTypeName);
    	String rawType = collNameMatcher.group(1);
    	if (MOCollection.COLLECTION.equals(rawType)) {
    		return createCollectionType(entryType);
    	} else if (MOCollection.LIST.equals(rawType)) {
    		return createListType(entryType);
    	} else if (MOCollection.SET.equals(rawType)) {
    		return createSetType(entryType);
    	}
    	throw new IllegalArgumentException("There is no collection type with raw type " + rawType);
    }

    @Override
	public String getRawType() {
		return rawType;
	}
    
    @Override
	public MetaObject getElementType () {
        return elementType;
    }
    
    @Override
	public MetaObject copy() {
    	return new MOCollectionImpl(rawType, typeRef(elementType));
    }
    
    @Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
    	this.elementType = elementType.resolve(context);
    	return this;
    }

    @Override
	public String toString () {
		return rawType + "<" + elementType + ">";
    }
    
    @Override
    public boolean isSubtypeOf(MetaObject anObject) {
    	switch (anObject.getKind()) {
    		case ANY:
    			return true;
    		case alternative:
    			return isSpecialisationOf(anObject);
    		case collection:
    			MOCollection theCollection  = (MOCollection) anObject;
        		String       superClassType = theCollection.getRawType();
    			String       subClassType   = this.getRawType();
    			
    			if (MOCollection.COLLECTION.equals(superClassType) || subClassType.equals(superClassType)) {
    				return this.getElementType().isSubtypeOf(theCollection.getElementType());
        		}
    			return false;
    		default:
    	    	return false;
    	}
    }
}
