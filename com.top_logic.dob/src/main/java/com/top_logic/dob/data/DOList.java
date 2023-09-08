
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.bean.BeanMetaObject;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.simple.ExampleDataObject;


/**
 * Implements a typysafe list of DataObjects.
 * 
 * List of Primitives (e.g. MOPrimitive.INTEGER) are not supported.
 *
 *             25.09.2000    mpe    modified ----> isRequiredElement method
 *
 * @author  Marco Perra
 */
public class DOList extends AbstractDOCollection implements List {

    /**
     * The inner list doing the actual work.
     */
    private List  list;

	private TLID identifier;

    /**
     * Create a new DOList with the given MetaObject as class.
     */
    public DOList (MOCollection aMetaObject) {
        super (aMetaObject);
        list = createEmptyList ();
    }

    
    @Override
	public void clear () {
        this.getList ().clear ();
    }

    /**
     * Two DOList are equal hen there elements, (regardless of Order) are the same.
     *
     * TODO KHA this semantic is questionable and should be changed.
     *
     * @return  true when the given DO is equal to this one, 
     */
    @Override
	public boolean equals (Object anObject) {
    	if (anObject == this) {
    		return true;
    	}
        if (!(anObject instanceof DOList)) {
            return false;
        } else {
            DOList anotherDOList = ( DOList ) anObject;

            return this         .getList().containsAll(anotherDOList.getList())
                && anotherDOList.getList().containsAll(this         .getList());
        }
    }

    /**
     * xored hashcode of all elements.
     */
    @Override
	public int hashCode () {
        int result = 0;
        for (Iterator iter = getList ().iterator (); iter.hasNext (); ) {
            result ^= iter.next ().hashCode();
        }
        return result;
    }

    /**
     * Forward List method to inner List
     *
     * @return true if this collection changed as a result of the call.
     */
    @Override
	public boolean add(Object obj) {
        if (canContain(obj)) {
            return this.getList().add(obj);
        }
        return false;
    }

    /**
     * Forward List method to inner List
     *
     * @return true if this collection changed as a result of the call
     */
    @Override
	public boolean addAll (int index, Collection c) {
        for (Iterator iter = c.iterator (); iter.hasNext (); ) {
            if (! canContain(iter.next())) {
                throw new IllegalArgumentException ("The object doesn´t have the right type.");
            }
        }

        return this.getList().addAll(index, c);
    }

    /**
     * Forward List method to inner List
     */
    @Override
	public void add (int index, Object element) {
        if (canContain(element)) {
            this.getList ().add (index, element);
        } else {
            throw new IllegalArgumentException (
            "The object doesn´t have the right type.");
        }
    }

    /**
     * Forward List method to inner List
     *
     * @return true if this List contains the specified element
     */
    @Override
	public boolean contains (Object obj) {
        return this.getList ().contains (obj);
    }

    /**
     * Forward List method to inner List
     *
     * @return the Object at the given index.
     */
    @Override
	public Object get (int index) {
        return this.getList ().get (index);
    }

    /**
     * Forward List method to inner List
     *
     * @return the index in this list of the first occurrence of the specified element, 
     *          or -1 if this list does not contain this element.
     *
     */
    @Override
	public int indexOf (Object obj) {
        return getList ().indexOf (obj);
    }

    /**
     * an Iterator over the elemts of the List.
     */
    @Override
	public Iterator iterator () {
        return getList ().iterator ();
    }

    /**
     * the index in this list of the last occurrence of the specified element, 
     *          or -1 if this list does not contain this element.
     */
    @Override
	public int lastIndexOf (Object obj) {
        return getList ().lastIndexOf (obj);
    }

    /**
     * a list iterator of the elements in this list (in proper sequence).
     */
    @Override
	public ListIterator listIterator () {
        return getList ().listIterator ();
    }

    /**
     * a list iterator of the elements in this list (in proper sequence), 
     *         starting at the specified position in this list.
     */
    @Override
	public ListIterator listIterator (int index) {
        return getList ().listIterator (index);
    }

    /**
     * Forward List method to inner List
     */
    @Override
	public boolean remove (Object obj) {
        return getList ().remove (obj);

    }

    /**
     * Forward List method to inner List
     */
    @Override
	public Object remove (int index) {
        return getList ().remove (index);
    }

    /**
     * Forward List method to inner List
     */
    @Override
	public Object set (int index, Object element) {
        return getList ().set (index, element);
    }

    /**
     * Forward List method to inner List
     */
    @Override
	public int size () {
        return getList ().size ();
    }

    /**
     * Forward List method to inner List
     */
    @Override
	public List subList (int fromIndex, int toIndex) {
        return getList ().subList (fromIndex, toIndex);
    }

    /**
     * Forward List method to inner List
     */
    protected List getList () {
        return this.list;
    }

	/**
	 * Whether the given object is assignment compatible to the element type of
	 * this collection.
	 * 
	 * @param anObject
	 *        The object to be inspected.
	 */
    protected boolean canContain(Object anObject) {
        if (anObject == null) {
        	// Null is assignment compatible to all types.
            return true;
        }

		MetaObject elementType = ((MOCollection) tTable()).getElementType();

        if (anObject instanceof ExampleDataObject) {
        	return ((ExampleDataObject) anObject).isInstanceOf(elementType);
        }
        else if (anObject instanceof DataObject) {
            MetaObject objectType = ((DataObject) anObject).tTable();
			return objectType.isSubtypeOf(elementType);
        }
        else if (elementType instanceof BeanMetaObject) {
        	try {
				Class<?> beanElementClass = Class.forName(elementType.getName());
				return beanElementClass.isAssignableFrom(anObject.getClass());
			} catch (ClassNotFoundException ex) {
				return false;
			}
        } else {
        	return false;
        }
    }

    /**
     * Strin representation suiteable for debugging.
     */
    @Override
	protected String toStringValues() {
        MetaObject theType = getCollectionType ();

        return super.toStringValues()
                + ", elementType: "
                + ((theType != null) ? theType.getName() : "null");
    }

    /**
     * an empty list, as base for this element.
     */
    private static List createEmptyList () {
        return new ArrayList ();
    }
    
    /** 
     * @see java.util.Collection#toArray()
     */
    @Override
	public Object[] toArray() {
    	return this.getList().toArray();
    }
    
    /** 
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    @Override
	public Object[] toArray(Object[] a) {
    	return this.getList().toArray(a);
    }


    /**
     * Returns the identifier for this object. If the identifier not exist 
     * it returns null.
     *
     * @return    The unique ID of this instance.
     */
    @Override
	public final TLID getIdentifier () {
        if (this.identifier == null) {
        	// TODO BHU: Remove lazy identifier creation.
            this.identifier = StringID.createRandomID();
        }

        return this.identifier;
    }

    /**
     * Sets the identifier you specified for this object. Once the 
     * identifier is set you can´t override the existing one.
     *
	 * Replaced for reimporting of xml saved Objects (by JCO)
     */
    @Override
	public final void setIdentifier (TLID anIdentifier) {
        this.identifier = anIdentifier;
    }
}
