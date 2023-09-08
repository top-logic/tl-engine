/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.Set;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAlternative;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;


/**
 * This class implements the method that describe the meta-data of
 * MetaObjects.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractMetaObject extends MOPartImpl implements MetaObject {

	private String definingResource;
    
   /**
     * Creates a new AbstractMetaObject with a given name.
     *
     * @param name the name resp. type of this {@link MetaObject}
     */
    public AbstractMetaObject (String name) {
        super(name);
    }

    @Override
	public String getDefiningResource() {
    	return this.definingResource;
    }
    
    @Override
	public void setDefiningResource(String definingResource) {
    	checkUpdate();
    	this.definingResource = definingResource;
    }

    @Override
	public String toString() {
        return getName();
    }
    
    @Override
	public final boolean equals (Object other) { // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.obeyEqualsContract.obeyGeneralContractOfEquals
    	return super.equals(other);
    }

    @Override
	public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Return the type of value represented by a MetaObject, i.e. value is a String
     * then typeOfValue() returns MOPrimitive.STRING or value is a instance of
     * DataObject then value return the type of the DataObject.
     * If value is of no known type the typeOfValue() returns null.
     *
     * @param   value the object to get corresponding MetaObject for
     */
    public static MetaObject typeOfValue (Object value) {
        if (value instanceof DataObject) {
            return (( DataObject ) value).tTable ();
        } 
		if (value instanceof BinaryContent) {
			return MOPrimitive.BLOB;
		}
        if (value instanceof Class) {
            return MOPrimitive.getPrimitive(( Class<?> ) value);
        }
        if (value != null) {
            return MOPrimitive.getPrimitive(value.getClass ());
        } 
        return null;
    }

	@Override
	public boolean isSubtypeOf(MetaObject anObject) {
		switch (anObject.getKind()) {
			case ANY:
				return true;
			case alternative:
				return isSpecialisationOf(anObject);
			default:
				return this.equals(anObject);
		}
    }

	/** 
	 * <code>true</code> iff this is a specialisation of the given type
	 */
	protected boolean isSpecialisationOf(MetaObject type) {
		Set<? extends MetaObject> specialisations = ((MOAlternative) type).getSpecialisations();
		if (this instanceof MOClass) {
			MOClass clazz = (MOClass) this;
			do {
				if (specialisations.contains(clazz)) {
					return true;
				}
			}
			while ((clazz = clazz.getSuperclass()) != null);
			return false;
		} else {
			return specialisations.contains(this);
		}
	}

    @Override
	public boolean isSubtypeOf(String aName) {
        return (this.getName().equals(aName));
    }

}
