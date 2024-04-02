/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.list;

import java.util.List;

import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.internal.PersistentTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * FastListElements are elements of a {@link FastList}. 
 * 
 * Although this is a FlexWrapper this implementation should not 
 * use any FlexAttributes by itself. 
 * 
 * A FastListElement must be contained in exactly on FastList otherwise
 * they are invalid. The Name Attribute is mandatory and must be unique 
 * in a FastList.
 *
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class FastListElement extends PersistentTypePart implements TLClassifier {

    /** Type Name of the underlying KnowledgeObject */
	public static final String OBJECT_NAME = TlModelFactory.KO_NAME_TL_CLASSIFIER;
    
    /** This defines the possible types of parents for a FastListElement */
	public static final String[] PARENT_TYPES = new String[] { FastList.OBJECT_NAME };

    /** 
     * Attribute name for the (optional) Description.
     */
    public static String DESC_ATTRIBUTE = "descr";

    /** 
     * Attribute name for the (sort) order
     */
    public static String ORDER_ATTRIBUTE = "order";

    /**
	 * Name of the database column for attribute {@link #ORDER_ATTRIBUTE}.
	 */
	public static String ORDER_DB_NAME = "SORT_ORDER";

	/**
	 * Attribute name for the Flags
	 */
    public static String FLAGS_ATTRIBUTE = "flags";

    /** 
     * Attribute name for (internal) Parent ID Attribute.
     */
    public static String PARENT_ATTRIBUTE = "parId";

	/**
	 * Name of the attribute containing the {@link FastList} this {@link FastListElement} belongs
	 * to.
	 */
	public static String OWNER_ATTRIBUTE = "owner";

    /** 
     * Bit indicating the checked state.
     */
    public static int     CHECKED_BIT    = 0x0001;

    /**
     * Create a FastListElement as of Contract with the WrapperFactory.
     */
    public FastListElement(KnowledgeObject ko) {
        super(ko);
    }

    /** This method creates a new fastlist element with the given parameter. aID may be <code>null</code> */
    public static FastListElement createFastListElement(TLID aID, String anUniqueName, String desc, int flags) {
        KnowledgeObject ko;
        FastListElement element = null;
		{
			ko = PersistencyLayer.getKnowledgeBase().createKnowledgeObject(aID, OBJECT_NAME);
			ko.setAttributeValue(FastListElement.NAME_ATTRIBUTE, anUniqueName);
            ko.setAttributeValue(FastListElement.ORDER_ATTRIBUTE , Integer.valueOf(0));
            if (desc != null)
                ko.setAttributeValue(FastListElement.DESC_ATTRIBUTE, desc);
            if (flags != 0)
                ko.setAttributeValue(FastListElement.FLAGS_ATTRIBUTE, Integer.valueOf(flags));
            
			element = WrapperFactory.getWrapper(ko);
        }
        return element;
    }

    /**
     * A FastListElement always has a name Attribute.
     */
    @Override
	public String getName () throws WrapperRuntimeException {
		return (String) tGetData(NAME_ATTRIBUTE);
    }

    /**
     * Allow Changing of the Name
     */
	@Override
	public void setName(String newName) {
		tSetData(NAME_ATTRIBUTE, newName);
    }

    /**
     * Return the List the Element belongs to.
     * 
     * @return null when Element is invalid.
	 * 
	 * @deprecated Use {@link #getOwner()}
	 */
	@Deprecated
	public FastList getList() {
		KnowledgeObject fastListKO = (KnowledgeObject) tGetData(OWNER_ATTRIBUTE);
		return (FastList) WrapperFactory.getWrapper(fastListKO);
    }

    /**
	 * This method returns the next {@link FastListElement} of the list if one
	 * exists, otherwise <code>null</code>.
	 */
    public FastListElement getNext() {
		List<FastListElement> elements = getList().elements();
		int index = elements.indexOf(this);
    	
    	if (index >= 0) {
    		int nextIndex = index + 1;
			if (nextIndex < elements.size()) {
    			return elements.get(nextIndex);
    		} 
    	}
    	
    	return null;
    } 
    
    /**
	 * This method returns the previous {@link FastListElement} of the list if one
	 * exists, otherwise <code>null</code>.
	 */
    public FastListElement getPrev() {
		List<FastListElement> elements = getList().elements();
		int index = elements.indexOf(this);
    	
    	if (index > 0) {
    		int prevIndex = index - 1;
    		if (prevIndex >= 0) {
    			return elements.get(prevIndex);
    		} 
    	}
    	
    	return null;
    }
    
    /**
     * Return the optional Description.
     */
    public String getDescription () {
		return tGetDataString(DESC_ATTRIBUTE);
    }

    /**
     * Set the  optional Description.
     */
    public void setDescription(String newDesc) {
		tSetDataString(DESC_ATTRIBUTE, newDesc);
    }

    /**
     * Return the Order which default to 0.
     */
	@Override
	public int getIndex() {
		Integer order = tGetDataInteger(ORDER_ATTRIBUTE);
        if (order != null)
            return order.intValue();
            
        return 0; // default
    }

    /**
     * Set the Order to some value.
     */
    public void setOrder(int order) {
		tSetDataInteger(ORDER_ATTRIBUTE, order);
    }

    /**
     * Return the Flags which default to 0.
     */
    public int getFlags() {
		Integer flags = tGetDataInteger(FLAGS_ATTRIBUTE);
        if (flags != null)
            return flags.intValue();
            
        return 0; // default
    }

    /**
     * Set the Flags to some value.
     */
    public void setFlags(Integer bits) {
		tSetDataInteger(FLAGS_ATTRIBUTE, bits);
    }

    /**
     * Set the Flags to some value.
     */
    public void setFlags(int bits) {
		tSetDataInteger(FLAGS_ATTRIBUTE, Integer.valueOf(bits));
    }
        
    /**
     * Get the checked state, which is bit 0 of the flags.
     */
    public boolean isChecked() {
        return 0 != (CHECKED_BIT & getFlags());
    }
   
    /**
     * Sets the checked state, which is bit 0 of the flags.
     */
    public void setChecked(boolean value) {
        int flags = getFlags();
        if (value)
            flags |=  CHECKED_BIT;
        else
            flags &= ~CHECKED_BIT;
        setFlags(flags);
    }

    // package private methods
    
    /**
	 * Must only be called by a creating (or removing) FastList
	 * 
	 * @param aList
	 *        may be null when FastListElement is invalidated.
	 */
	void setList(FastList aList) {
		tSetData(OWNER_ATTRIBUTE, aList.tHandle());
    }

    /**
	 * Create a new KnowledgeObject and copy my attributes into it.
	 * 
	 * @param owner
	 *        {@link KnowledgeObject} of owner List.
	 */
	KnowledgeObject cloneAttributesToKO(KnowledgeBase kBase, KnowledgeObject owner) {
        KnowledgeObject myKO    = tHandle();
		KnowledgeObject cloneKO = kBase.createKnowledgeObject(OBJECT_NAME);

		cloneKO.setAttributeValue(OWNER_ATTRIBUTE, owner);
        cloneKO.setAttributeValue(ORDER_ATTRIBUTE , myKO.getAttributeValue(ORDER_ATTRIBUTE));
		cloneKO.setAttributeValue(NAME_ATTRIBUTE, myKO.getAttributeValue(NAME_ATTRIBUTE));
        cloneKO.setAttributeValue(DESC_ATTRIBUTE  , myKO.getAttributeValue(DESC_ATTRIBUTE));
        cloneKO.setAttributeValue(FLAGS_ATTRIBUTE , myKO.getAttributeValue(FLAGS_ATTRIBUTE));
        
        return cloneKO;
    }

    /** 
     * Returns the element with the given name. 
     * 
     * @param    aName    The name of the list element.
     * @param    aKB      The knowledge base to get the element from.
     * @return   The found element or <code>null</code>, if there is no such element.
	 * @deprecated Use {@link TLModelUtil#resolveQualifiedName(String)}
	 */
	@Deprecated
    public static FastListElement getElementByName(String aName, KnowledgeBase aKB) {
		KnowledgeObject theKO = (KnowledgeObject) aKB.getObjectByAttribute(OBJECT_NAME, NAME_ATTRIBUTE, aName);

		return (FastListElement) WrapperFactory.getWrapper(theKO);
    }

    /**
     * Returns the element with the given name. 
     * 
     * @param    aName    The name of the list element.
     * @return   The found element or <code>null</code>, if there is no such element.
	 * 
	 * @deprecated Use {@link TLModelUtil#resolveQualifiedName(String)}
	 */
	@Deprecated
    public static FastListElement getElementByName(String aName) {
		return getElementByName(aName, PersistencyLayer.getKnowledgeBase());
    }

	/**
	 * Lookup of a {@link FastListElement} by internal ID.
	 * 
	 * @param anIdentifier
	 *        Identifier of a FastListElement.
	 * 
	 * @deprecated Use {@link TLModelUtil#resolveQualifiedName(String)}
	 */
	@Deprecated
    public static FastListElement getInstance(TLID anIdentifier, KnowledgeBase aKB) {
		return (FastListElement) WrapperFactory.getWrapper(anIdentifier, OBJECT_NAME, aKB);
    }

    /** Standard getInstance() method for a Wrapper 
     * 
     * @param anIdentifier Identifier of a FastListElement.
	 * 
	 * @deprecated Use {@link TLModelUtil#resolveQualifiedName(String)}
	 */
	@Deprecated
    public static FastListElement getInstance(TLID anIdentifier) {
		return getInstance(anIdentifier, PersistencyLayer.getKnowledgeBase());
    }

	@Override
	public TLEnumeration getOwner() {
		return getList();
	}

	@Override
	public boolean isDefault() {
		return getList().getDefaultElement() == this;
	}

	@Override
	public void setDefault(boolean b) {
		{
			if (b) {
				getList().setDefaultElement(this);
			} else {
				if (isDefault()) {
					getList().setDefaultElement(null);
				} else {
					// a different element is default. Nothing to do here.
				}
			}
		}
	}

	@Override
	public int compareTo(Wrapper other) {
		if (!(other instanceof FastListElement)) {
			return super.compareTo(other);
		}
		FastListElement otherClassifier = (FastListElement) other;
		if (getOwner().isOrdered()) {
			return Integer.compare(getIndex(), otherClassifier.getIndex());
		}
		return getLabel().compareTo(otherClassifier.getLabel());
	}

}
