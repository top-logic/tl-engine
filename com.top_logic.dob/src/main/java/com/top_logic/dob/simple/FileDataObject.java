/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.simple;

import java.io.File;

import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.AbstractDataObject;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;

/** A DataObject representing a {@link java.io.File}.
 *
 * Since File.equals() doe not normlize the path
 * this object will not be equal to an object with same
 * but Absolute path.
 * 
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class FileDataObject implements DataObject {

    /** The file wrapped by this DataObject. */
    File  file;
    
    /** 
     * Create the Object with the given File.
     * 
     */
    public FileDataObject(File aFile) {
        file = aFile;
    }

    /** 
     * Rely on the embedded file.
     */
    @Override
	public boolean equals(Object theObject) {
    	if (theObject == this) {
    		return true;
    	}
        if (theObject instanceof FileDataObject) {
            return file.equals(((FileDataObject) theObject).file);
        } 
        return false;
    }
    
    /** 
     * Use hashcode() of embedded file.
     */
    @Override
	public int hashCode() {
        return file.hashCode();
    }

    /** 
     * Use to String method of embedded file.
     */
    @Override
	public String toString() {
        return "FileDataObject [" + file + ']';
    }

	/**
	 * TODO #2829: Delete TL 6 deprecation.
	 * 
	 * @deprecated Use {@link #tTable()} instead
	 */
	@Override
	@Deprecated
	public final MetaObject getMetaObject() {
		return tTable();
	}

	/** Returns the MetaObject this object represents.
     *
     * @return somve private MetaObject unique for this instance.
     */
    @Override
	public MetaObject tTable () {
        return FileMetaObject.SINGLETON;
    }

    /** Similar to Class.instanceOf(),
     *
     * @param   aMetaObject check if we are of this type.
     * @return  true when the given MetaObject is a FileMetaObject
     */
    @Override
	public boolean isInstanceOf (MetaObject aMetaObject) {
        return tTable().isSubtypeOf(aMetaObject);
    }

    /** Similar to Class.instanceOf(),
     *
     * @param   aMetaObjectName check if we are of this type.
     * @return  true when the given MetaObject is a FileMetaObject
     */
    @Override
	public boolean isInstanceOf (String aMetaObjectName) {
        return FileMetaObject.SINGLETON.getName().equals(aMetaObjectName);
    }

    /** Returns the identifier for this object. 
     * 
     * @return the absolute path of the embedded file.
     */
    @Override
	public TLID getIdentifier() {
		return StringID.valueOf(file.getAbsolutePath());
    }

    /** Ignored since we cannot set the absolute path of the file.
     *
     * @param   anIdentifier    ignored
     */
    @Override
	public void setIdentifier(TLID anIdentifier) {
        // ignored        
    }

    @Override
	public Iterable<? extends MOAttribute> getAttributes () {
        return FileMetaObject.SINGLETON.getAttributes();
    }

	/** Returns the list of attribute names defined in the MetaObject.
	 *
	 * @return   The list of known attributes.
	 */
	@Override
	public String [] getAttributeNames () {

        return FileMetaObject.SINGLETON.getAttributeNames();
    }

	@Override
	public boolean hasAttribute(String attributeName) {
		return FileMetaObject.SINGLETON.hasAttribute(attributeName);
	}

    /** 
     * Return one of theAttribute starting with "is".
     *<p>
     * We can safely assume that the given string has at least 6 characters.
     *</p>
     */
    private boolean getIsAttributeValue (String attrName) 
         throws NoSuchAttributeException { 
        switch (attrName.charAt(2))
        {
            case 'C':
                if (attrName.equals("isContainer")) {
                    return file.isDirectory();
                }
                break;    
            case 'E':
                if (attrName.equals("isEntry")) {
                    return file.isFile();
                }
                break;    
            case 'R':
                if (attrName.equals("isReadable")) {
                    return file.canRead();
                }
                break;    
            case 'W':
                if (attrName.equals("isWriteable")) {
                    return file.canWrite();
                }
                break;    
            default: // fall through
        }
        throw new NoSuchAttributeException('"' + attrName + '"');
    }    

    @Override
	public Object getAttributeValue (String attrName)
        throws NoSuchAttributeException
    {
        int len = attrName.length();
        if (len >= 4) { // "name"
          switch (attrName.charAt(0))  {
            case 'i' :  // is ....
                return Boolean.valueOf(getIsAttributeValue(attrName));
            case 'e' :
                if (attrName.equals("exists")) {
                    return Boolean.valueOf(file.exists());
                }
                break;    
            case 'l' :
                if (attrName.equals("length")) {
                    return length();
                }
                if (attrName.equals("lastModified")) {
                    return Long.valueOf(file.lastModified());
                }
                break;    
            case 'n' :
                if (attrName.equals("name")) {
                    return file.getName();
                }
                break;    
            case 'p' :
                if (attrName.equals("physicalResource")) {
                    return file.getAbsolutePath();
                }
                break;    
            case 's' :
                if (attrName.equals("size")) {
                    return length();
                }
                break;    
              default: // fall through
          }
        }
        throw new NoSuchAttributeException(attrName);
    }

    /** 
     * Work around unspecified behavior of {@link File#length()} for {@link File#isDirectory()}
     */
    private Object length() {
        if (!file.isDirectory()) {
            return Long.valueOf(file.length());
        } else { // file.length is unspecified for Directories
            return Long.valueOf(0L);    
        }
    }
    
    @Override
	public Object setAttributeValue (String attrName, Object value)
        throws IncompatibleTypeException,NoSuchAttributeException
    {
		throw new UnsupportedOperationException("Sorry, not supported");
    }

	@Override
	public Object getValue(MOAttribute attribute) {
		return AbstractDataObject.getValue(this, attribute);
	}
	
	@Override
	public ObjectKey getReferencedKey(MOReference reference) {
		return AbstractDataObject.getReferencedKey(this, reference);
	}

	@Override
	public Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException {
		return AbstractDataObject.setValue(this, attribute, newValue);
	}

}
