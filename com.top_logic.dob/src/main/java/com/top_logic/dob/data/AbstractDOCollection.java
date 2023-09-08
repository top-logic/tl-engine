
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.data;

import java.util.Collection;
import java.util.Iterator;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOCollection;

/**
 * Comment .
 *
 * @author  Marco Perra
 */
public abstract class AbstractDOCollection extends AbstractDataObject
        implements DOCollection {

    private MetaObject collectionType;

    public AbstractDOCollection (MetaObject aMetaObject) {
        super (collectionType (aMetaObject));
        this.collectionType =
            (( MOCollection ) aMetaObject).getElementType ();
    }

    /**
     * Return the element-types of this collection.
     */
    @Override
	public MetaObject getCollectionType () {
        return collectionType;
    }

    @Override
	public boolean add (Object obj) {
        throw new IllegalArgumentException (this.getClass ().toString ());
    }

    @Override
	public boolean addAll (Collection c) {
        for (Iterator iter = this.iterator (); iter.hasNext (); ) {
            this.add (iter.next ());
        }
        return true;
    }

    @Override
	public boolean containsAll (Collection col) {
        for (Iterator iter = this.iterator (); iter.hasNext (); ) {
            this.contains (iter.next ());
        }
        return true;
    }

    /**
     * Foreward <code>equals()</code> to the elements of the collection. 
     */
    @Override
	public boolean equals (Object o) {
        if (o == this)
            return true;
        if (o instanceof DOCollection && super.equals(o)) {
            DOCollection other = (DOCollection) o;
            if (!other.getCollectionType().equals(collectionType))
                return false;
            Iterator oIter = other.iterator();
            for (Iterator iter = this.iterator (); iter.hasNext (); ) {
                if (!oIter.hasNext() || !iter.next().equals(oIter.next()))
                    return false;
            }
            return !oIter.hasNext();
        }
        return false;
    }

    /**
     * Foreward <code>hashCode()</code> to the elements of the collection. 
     */
    @Override
	public int hashCode () {
        int result = 0;
        for (Iterator iter = this.iterator (); iter.hasNext (); ) {
            result ^= iter.next ().hashCode();
        }
        return result;
    }

    @Override
	public boolean isEmpty () {
        return size () == 0;
    }

    @Override
	public boolean remove (Object obj) {
        throw new IllegalArgumentException (this.getClass ().toString ());
    }

    @Override
	public boolean removeAll (Collection c) {
        for (Iterator iter = this.iterator (); iter.hasNext (); ) {
            this.remove (iter.next ());
        }
        return true;
    }

    @Override
	public boolean retainAll (Collection c) {
        for (Iterator iter = this.iterator (); iter.hasNext (); ) {
            if (!c.contains (iter.next ())) {
                c.remove (iter.next ());
            }
        }
        return true;
    }

    @Override
	public Object[] toArray () {
        throw new IllegalArgumentException (
        	this.getClass () + "\n"
            + "The method toArray will be supported later.");
    }

    @Override
	public Object[] toArray (Object[] a) {
        throw new IllegalArgumentException (
        	this.getClass () + "\n"
            + "The method toArray will be supported later.");
     }

    private static MetaObject collectionType (MetaObject mo) {
        if ((mo == null) ||!(mo instanceof MOCollection)) {
            throw new IllegalArgumentException (
            	"To create a DOCollectionyou have to instanciate \n"
              + "the coresponding MOCollection.");
        }
        return mo;

    }

	@Override
	public Object getValue(MOAttribute attribute) {
		throw new IllegalArgumentException("This '" + this + "' does not have attributes: " + attribute);
	}

	@Override
	public Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException {
		throw new IllegalArgumentException("This '" + this + "' does not have attributes: " + attribute);
	}
}
