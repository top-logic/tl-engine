/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DOCollection;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dob.util.MetaObjectUtils;

/** A MOClasss based on a Bean(Info).
 *
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class BeanMetaObject implements MOClass {

	/** This class is more or less a wrapper for this info */
	protected BeanInfo info;
	
	/** List of Attributes lazy extracted form the Info */
	protected List<MOAttribute> attributes;

	/** Map of Attributes indexed by name lazy extracted form the Info */
	protected Map<String, MOAttribute> attrMap;

	/** Map of PropertyDescriptors indexed by name lazy extracted form the Info */
	protected Map<String, PropertyDescriptor> propertyMap;

	/** Array of Attribute names lazy extracted form the Info */
	protected String attrnames[];
	
	/** Construct a BeanMetaObject fro a given (Bean)-Class */
	public BeanMetaObject(Class aClass) throws IntrospectionException
	{
		if (aClass.isInterface()) // mhh, will not care for superinterfaces
            info = Introspector.getBeanInfo(aClass);
        else
            info = Introspector.getBeanInfo(aClass, Object.class);
	}

	/** Construct a BeanMetaObject fro a given BeanInfo */
	public BeanMetaObject(BeanInfo anInfo)
	{
		info = anInfo;	
	}
	
	@Override
	public Kind getKind() {
		return Kind.item;
	}

	/** Not implemented since the classHierarchy predicts the superclass
	 */
	@Override
	public void setSuperclass(MOClass aMOClass) {
        // predicted by classHierarchy
	}

    /**
     * @see com.top_logic.dob.meta.MOClass#getSuperclass()
     */
    @Override
	public MOClass getSuperclass() {
        Class superClass = info.getBeanDescriptor().getBeanClass().getSuperclass();
        if (superClass != null) { // usually Object
            try {
                return (MOClass) BeanMORepository.getInstance().
                    getMetaObject(superClass.getName());
            } catch (UnknownTypeException ute) {
                Logger.warn ("Failed to retrieve type for " + superClass,
                             ute,
                             this);
            }
        }
        return null;
	}

	/** The declared Attributes are generated from the PropertyDescriptors as needed.
	 */
	@Override
	public List<MOAttribute> getDeclaredAttributes() {
		if (attributes == null) {
			createDeclaredAttributes(info.getPropertyDescriptors());
        }
			
		return attributes;
	}
	
	/** Create the declared Attributes  from the PropertyDescriptors */
    protected void createDeclaredAttributes(PropertyDescriptor[] descs) {
        ArrayList<String> attrnamesList = new ArrayList<>();
        int size = descs.length;
        attributes  = new ArrayList<>(size);
        attrMap     = new HashMap<>(size);
        propertyMap = new HashMap<>(size);
        int cacheSize = 0, dbSize = 0;
        for (int i=0; i < size; i++) {
            PropertyDescriptor desc = descs[i];
            Class type = desc.getPropertyType();
            if (type==null) { // happens with index properties, not supported
                continue;
            }
            MetaObject motype = MOPrimitive.getPrimitive(type);
            if (motype == null) { // Mhh, complex type ...
                try {
                    motype = BeanMORepository.getInstance().
                        getMetaObject(type.getName());
                } catch (UnknownTypeException ute) {
                    Logger.warn ("Failed to retrieve type " + type, ute, this);
                }
            }
            String name = desc.getName();
            MOAttributeImpl attr = new MOAttributeImpl(name, motype);
            attr.initOwner(this, cacheSize);
			cacheSize += attr.getStorage().getCacheSize();
            attr.initDBColumnIndex(dbSize);
            dbSize += attr.getDbMapping().length; 
            attributes.add(attr);
            attrMap.put(name, attr);
            attrnamesList.add(name);
            propertyMap.put(name, desc);
        }
        int numAttr = attrnamesList.size();
        attrnames = new String[numAttr];
        attrnamesList.toArray(attrnames);
    }

    /**
     * Return true if this class is inherited from other.
     *
     * @param  other a potential Superclass
     */
	@Override
	public boolean isInherited(MOClass other) {
		if (! (other instanceof BeanMetaObject)) {
			return false;
        }
		
		BeanMetaObject bmo = (BeanMetaObject) other;
		return bmo. info.getClass().isAssignableFrom(
			   this.info.getClass());
	}

	/**
	 * Check if some {@link BeanMetaObject} identified by superName is a superclass of this.
	 */
	public boolean isInherited(String superName) {
		BeanMetaObject bmo = null;
		try {
			bmo = (BeanMetaObject) BeanMORepository.getInstance().
										getMetaObject(superName);
		} catch (UnknownTypeException ute) {
            Logger.warn ("Failed to retrieve type " + superName, ute, this);
		}
		if (bmo != null) {
			return bmo. info.getClass().isAssignableFrom(
				   this.info.getClass());
        }
		// else
		return false;		   
	}

    /**
     * Checks if this object is an instance of the given meta object
     *
     * If the implementing and the given class is not instanceof
     * {@link com.top_logic.dob.meta.MOClass}, the test can only
     * go over on one level.
     * 
     * @param   anObject    The meta object to be inspected.
     * @return  <code>true</code>, if the meta object or a superclass of it
     *          is same.
     */
    @Override
	public boolean isSubtypeOf(MetaObject anObject) {
        boolean theResult = this.equals(anObject);

        if (!theResult && MetaObjectUtils.isClass(anObject)) {
            theResult = this.isInherited((MOClass) anObject);
        }

        return (theResult);
    }

    /**
     * Checks if this meta object is an instance of the given name of another
     * meta object.
     *
     * If the implementing class is not instanceof
     * {@link com.top_logic.dob.meta.MOClass}, the test can only
     * go over on one level.
     *
     * @param   aName    The name of the meta object to be inspected.
     * @return  <code>true</code>, if the meta object or a superclass of it
     *          has the same name than the given one.
     */
    @Override
	public boolean isSubtypeOf (String aName) {
        return (this.getName().equals(aName) || this.isInherited(aName));
    }

	/** Not supported since Atrributes are predicted by bean class.
	 */
	@Override
	public void addAttribute(MOAttribute anAttr) throws DuplicateAttributeException {
		throw new UnsupportedOperationException("Immutable type.");
	}

	@Override
	public void overrideAttribute(MOAttribute attribute) {
		throw new UnsupportedOperationException("Immutable type.");
	}

	@Override
	public int getCacheSize() {
		return this.getAttributes().size();
	}
	
	/** Equal to createDeclaredAttributes() since we cannot really distinguish these.
	 * 
	 *  This may become a problem in the future, mmh.
	 */
	@Override
	public List<MOAttribute> getAttributes() {
		return getDeclaredAttributes();
	}
	
	@Override
	public List<MOReference> getReferenceAttributes() {
		return MetaObjectUtils.filterReferences(getAttributes());
	}

	/** 
	 * Always returns an empty List.
	 */
	@Override
	public List<MOIndex> getIndexes() {
		return Collections.emptyList();
	}

	/**
	 * @see com.top_logic.dob.MetaObject#getName()
	 */
	@Override
	public String getName() {
		return info.getBeanDescriptor().getBeanClass().getName();
	}

    /**
     * This is derived from the underlying class.
     */
    @Override
	public boolean isAbstract() {
        return Modifier.isAbstract(this.info.getClass().getModifiers());
    }
    
    @Override
	public void setAbstract(boolean abstractModifier) {
    	throw new IllegalStateException("Type is frozen.");
    }
 
	@Override
	public MOAttribute getAttribute(String attrName) throws NoSuchAttributeException {
		MOAttribute result = getAttributeOrNull(attrName);
		if (result == null) {
			throw new NoSuchAttributeException(attrName);
        }
		return result;
	}
	
	@Override
	public MOAttribute getAttributeOrNull(String name) {
		if (attrMap == null) {
			createDeclaredAttributes(info.getPropertyDescriptors());
		}
		return attrMap.get(name);
	}

	@Override
	public boolean hasAttribute(String attrName) {
		if (attrMap == null) {
			createDeclaredAttributes(info.getPropertyDescriptors());
        }
		return attrMap.containsKey(attrName);
	}

	@Override
	public MOAttribute getDeclaredAttributeOrNull(String name) {
		return getAttributeOrNull(name);
	}
	
	@Override
	public boolean hasDeclaredAttribute(String attrName) {
		return hasAttribute(attrName);
	}
	
	@Override
	public String[] getAttributeNames() {
		if (attrnames == null) {
			createDeclaredAttributes(info.getPropertyDescriptors());
        }
		return attrnames;
	}

	/** Create a new, empty BeanDataObject based on an empty Bean.
	 */
	public DataObject createEmptyObject() throws UnknownTypeException {
		try {
			return 
				new BeanDataObject(info.getBeanDescriptor().getBeanClass().newInstance());
		} catch (Exception e) {
			throw new UnknownTypeException(e);
		}
	}

	/** not supported,
	 */
	public DOCollection createDOCollection(String collectionKind)
		throws UnknownTypeException {
		return null;
	}

	/** not supported,
	 */
	public DataObject createMOStructure() throws UnknownTypeException {
		return null;
	}

	/** Get a Property descriptors for a given attribute name */
	public PropertyDescriptor getDescriptor(String attrName) 
		throws NoSuchAttributeException
	{
		if (propertyMap == null) {
			createDeclaredAttributes(info.getPropertyDescriptors());
        }
		PropertyDescriptor result = propertyMap.get(attrName);
		if (result == null) {
			throw new NoSuchAttributeException(attrName);
        }
			
		return result;
	}

	@Override
	public void freeze() {
		// Already immutable.
	}
	
	@Override
	public boolean isFrozen() {
		// Already immutable.
		return true;
	}

	@Override
	public boolean isFinal() {
		// TODO BHU: Corresponding info in descriptor? Assume non-final. 
		return false;
	}
	
	@Override
	public MOIndex getPrimaryKey() {
		return null;
	}

	@Override
	public boolean isVersioned() {
		return false;
	}

	@Override
	public void setVersioned(boolean versioned) {
		throw new IllegalStateException("Frozen type cannto be updated.");
	}
	
	@Override
	public String getDefiningResource() {
		return "class:" + info.getClass().getName();
	}
	
	@Override
	public void setDefiningResource(String definingResource) {
		throw new UnsupportedOperationException("Immutable property.");
	}

	@Override
	public <T extends MOAnnotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

	@Override
	public <T extends MOAnnotation> T removeAnnotation(Class<T> annotationClass) {
		return null;
	}

	@Override
	public <T extends MOAnnotation> void addAnnotation(T annotation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MetaObject copy() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DBTableMetaObject getDBMapping() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setAssociation(boolean isAssociation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAssociation() {
		return false;
	}

}
