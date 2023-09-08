/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.OneHitVisitor;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.WrapperValueFilter;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Mandators provide a tree structure with presets
 * for various kind of objects that may be attached
 * to the elements of the Mandator structure.
 * 
 * Methods for dealing with Mandator-based
 * security presets for business objects are provided.
 * 
 * The business object itself is responsible for
 * registering with the Mandator structure (cf. @link com.top_logic.element.structured.wrap.Mandator#registerType(String, String, Collection))
 * and creating the security attributes on it 
 * (as we don't know the correct attribute types).
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class Mandator extends AttributedStructuredElementWrapper {
    // TODO KBU get rid of sap and overview attributes...
	/** Attribute identifiying an (optional) SAP-Datasource associated with the mandator */
    public static final String SAP_SUPPLIERS = "sapSuppliers";
    
	private static final String ALLOW_ATT_PREFIX = "allow";
	/** Description of this mandator. */
	public static final String DESCRIPTION = "description";
	/** The type of the structured element. */
	public static final String MANDATOR_TYPE = "Mandator";
	/** The number handler id of this mandator. */
	public static final String NUMBER_HANDLER_ID = "numberHandlerID";

	/** The root node of the mandator structure. */
	private static volatile Mandator root;
	
	/** name of the structure for mandator */
	public static final String STRUCTURE_NAME = "mandatorStructure";
	/** contact persons for mandator overview. */
	public static final String CONTACT_PERSONS = "contactPersons";
	/** general description field for mandator overview. */
	public static final String CONTRACT_GUIDELINES = "contractGuidelines";

	/**
	 * Stores all info about all registered types.
	 */
	private static Map<String, Map<String, TLStructuredTypePart>> typeConfigs;
	protected static final String KEY_MANDATORATTID = "KEY_MANDATORATTID";
    
	public Mandator(KnowledgeObject ko) {
		super(ko);
	}

    /** 
     * Return the mandator ID to be used for generating unique IDs.
     * 
     * @return    The requested mandator ID.
     * @see       #NUMBER_HANDLER_ID
     */
    public String getMandatorID() {
        return this.getString(NUMBER_HANDLER_ID);
    }

	/** 
	 * all registered type names
	 */
	public static synchronized Collection<String> getKnownTypes() {
		if (typeConfigs == null) {
			return Collections.emptyList();
		}
		
		return Collections.unmodifiableCollection(typeConfigs.keySet());
	}

	/**
	 * The root node of the mandator structure.
	 */
	public static Mandator getRootMandator() {
	    if (Mandator.root == null) {
	        Mandator.root = (Mandator) StructuredElementFactory.getInstanceForStructure(Mandator.STRUCTURE_NAME).getRoot();
	    }
	
	    return (Mandator.root);
	}

	/** 
	 * Helper to get registered info
	 * 
	 * @param aType	the object type
	 * @param aKey	the key (one of KEY_...)
	 * @return the info. <code>null</code> if none is registered
	 */
	protected static synchronized TLStructuredTypePart getInfoForType(String aType, String aKey) {
		if (typeConfigs == null) {
			return null;
		}
		
		Map<String, TLStructuredTypePart> theInfos = typeConfigs.get(aType);
		if (theInfos == null) {
			return null;
		}
		
		return theInfos.get(aKey);
	}

	@CalledFromJSP
	public boolean allowsMove() {
	    return (!this.isRoot());
	}

	/** 
	 * Check if object creation is permitted on this Mandator
	 * for the given type
	 * 
	 * @param aType	the type
	 * @return true if object creation is permitted on this Mandator for the given type
	 */
	public boolean allowType(String aType) {
	    Object theValue = this.getValue(ALLOW_ATT_PREFIX + aType);
	
	    if (theValue instanceof Boolean) {
	        boolean theBoolean = ((Boolean) theValue).booleanValue();
	
	        return (theBoolean);
	    }
	    else {
	        return (false);
	    }
	}

	/**
	 * @param aType
	 *        the type
	 * @return all objects of the given type attached to this mandator via the registered attribute
	 */
	public Collection getObjectsOfType(String aType) {
		TLStructuredTypePart attribute = getInfoForType(aType, KEY_MANDATORATTID);
		
		return (WrapperMetaAttributeUtil.getWrappersWithValue(attribute, this));
	}

	/**
	 * true if there are any objects of the any type attached to this mandator via the
	 *         registered attribute
	 */
	public boolean hasObjectsOfAnyType() {
		boolean hasObjects = false;
		Iterator theTypes = getKnownTypes().iterator();
		while (!hasObjects && theTypes.hasNext()) {
			String theType = (String) theTypes.next();
			if (this.allowType(theType)) {
				hasObjects = this.hasObjectsOfType(theType);
			}
		}
		
		return hasObjects;
	}

	/**
	 * @param aType
	 *        the type
	 * @return true if there are any objects of the the given type attached to this mandator via the
	 *         registered attribute
	 */
	public boolean hasObjectsOfType(String aType) {
		TLStructuredTypePart attribute = getInfoForType(aType, KEY_MANDATORATTID);
		
		return (WrapperMetaAttributeUtil.hasWrappersWithValue(attribute, this));
	}

	/** 
	 * Set if creation of objects of the given type
	 * should be allowed on this Mandator
	 * 
	 * @param aType		the type
	 * @param anAllow	the flag
	 */
	public void setAllowType(String aType, boolean anAllow) {
		this.setValue(ALLOW_ATT_PREFIX + aType, Boolean.valueOf(anAllow));
	}

    /** 
     * Get a mandator for a given number handler ID.
     * 
     * @param    anID    The number handler ID for the requested mandator, must not be <code>null</code>.
     * @return   The requested mandator, never <code>null</code>.
     */
    public static Mandator getMandatorByID(String anID) {
        Mandator      theRoot    = Mandator.getRootMandator();
        OneHitVisitor theVisitor = new OneHitVisitor(new WrapperValueFilter(Mandator.NUMBER_HANDLER_ID, anID));

        TraversalFactory.traverse(theRoot, theVisitor, TraversalFactory.BREADTH_FIRST);

        return (Mandator) theVisitor.getHit();
    }

    public static Mandator getMandatorByName(String aName) {
        Mandator      theRoot    = Mandator.getRootMandator();
        OneHitVisitor theVisitor = new OneHitVisitor(new WrapperValueFilter(TLNamed.NAME_ATTRIBUTE, aName));
        
        TraversalFactory.traverse(theRoot, theVisitor, TraversalFactory.BREADTH_FIRST);
        
        return (Mandator) theVisitor.getHit();
    }
    
    public static class MandatorByLabelComparator implements Comparator {
        public static final MandatorByLabelComparator INSTANCE = new MandatorByLabelComparator();
        
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
		public int compare(Object aO1, Object aO2) {
            return (MetaLabelProvider.INSTANCE.getLabel(aO1).
                    compareTo(MetaLabelProvider.INSTANCE.getLabel(aO2)));
        }
    }
}
