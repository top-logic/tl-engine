/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core.util;

import java.util.Collection;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaAttributeFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Util class for maintenance operations. Normally used by maintenance pages or data migrations.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class MaintenanceUtil {

    /**
     * This class provides only static functions.
     */
    private MaintenanceUtil() {
        // static only
    }



    /**
     * Deletes the given meta element inclusive all values.
     *
     * @param aMetaElementType
     *        the meta element to remove
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaElementComplete(String aMetaElementType) {
        return deleteMetaElementValues(aMetaElementType)
             | deleteMetaElement(aMetaElementType);
    }

    /**
     * Deletes the given meta attribute from the given meta element inclusive all attribute
     * values.
     *
     * @param aMetaElementType
     *        the meta element to remove the meta attribute from
     * @param aMetaAttributeName
     *        the meta attribute to remove
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaAttributeComplete(String aMetaElementType, String aMetaAttributeName) {
        return deleteMetaAttributeValues(aMetaElementType, aMetaAttributeName, true)
             | deleteMetaAttribute(aMetaElementType, aMetaAttributeName);
    }



    /**
     * Deletes the given meta element and all sub meta elements inclusive all their meta attributes.
     *
     * @param aMetaElementType
     *        the meta element to delete
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaElement(String aMetaElementType) {
        return deleteMetaElement(MetaElementUtil.getMetaElement(aMetaElementType));
    }

    /**
     * Deletes the given meta element and all sub meta elements inclusive all their meta attributes.
     *
     * @param aMetaElement
     *        the meta element to delete
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaElement(TLClass aMetaElement) {
        if (aMetaElement == null) return false;
        boolean result = false;
		for (TLClass me : aMetaElement.getSpecializations()) {
            result |= deleteMetaElement(me);
        }
        if (aMetaElement instanceof Wrapper) {
			for (TLStructuredTypePart metaAttribute : TLModelUtil.getLocalMetaAttributes(aMetaElement)) {
                result |= deleteMetaAttribute(aMetaElement, metaAttribute);
            }
			((Wrapper) aMetaElement).tDelete();
			result = true;
        }
		else
			throw new IllegalArgumentException("Don't know how to delete type '" + aMetaElement + "'.");
        return result;
    }



    /**
     * Deletes the given meta attribute from the given meta element.
     *
     * @param aMetaElementType
     *        the meta element to remove the meta attribute from
     * @param aMetaAttributeName
     *        the meta attribute to remove
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaAttribute(String aMetaElementType, String aMetaAttributeName) {
        try {
            TLClass metaElement = MetaElementUtil.getMetaElement(aMetaElementType);
            if (metaElement == null) return false;
			TLStructuredTypePart metaAttribute = MetaElementUtil.getLocalMetaAttribute(metaElement, aMetaAttributeName);
            return deleteMetaAttribute(metaElement, metaAttribute);
        }
        catch (NoSuchAttributeException e) {
            return false;
        }
    }

    /**
     * Deletes the given meta attribute from the given meta element.
     *
     * @param aMetaElement
     *        the meta element to remove the meta attribute from
     * @param aMetaAttribute
     *        the meta attribute to remove
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaAttribute(TLClass aMetaElement, TLStructuredTypePart aMetaAttribute) {
        if (aMetaElement == null || aMetaAttribute == null) return false;
        try {
            MetaAttributeFactory.getInstance().removeMetaAttribute(aMetaElement, aMetaAttribute);
            return true;
        }
        catch (NoSuchAttributeException e) {
            return false;
        }
    }



    /**
     * Deletes the FastList with the given name.
     *
     * @param aListName
     *        the list to delete
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteList(String aListName) {
        return deleteList(FastList.getFastList(aListName));
    }

    /**
     * Deletes the given FastList.
     *
     * @param aList
     *        the list to delete
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteList(FastList aList) {
        if (aList == null) return false;
        // FastListElements get deleted automatically when the list gets deleted
		aList.tDelete();
		return true;
    }



    /**
     * Deletes all list elements of the given FastList with the given name.
     *
     * @param aListName
     *        the list to delete all elements from
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteListElements(String aListName) {
        return deleteListElements(FastList.getFastList(aListName));
    }

    /**
     * Deletes all list elements of the given FastList.
     *
     * @param aList
     *        the list to delete all elements from
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteListElements(FastList aList) {
        if (aList == null) return false;
        boolean result = false;
        for (FastListElement element : aList.elements()) {
            result |= deleteListElement(aList, element);
        }
        return result;
    }



    /**
     * Deletes the given FastListElement from the given FastList.
     *
     * @param aListName
     *        the list to delete the list element from
     * @param aListElementName
     *        the list element to delete
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteListElement(String aListName, String aListElementName) {
        FastList list = FastList.getFastList(aListName);
        if (list == null) return false;
        FastListElement element = list.getElementByName(aListElementName);
        return deleteListElement(list, element);
    }

    /**
     * Deletes the given FastListElement from the given FastList.
     *
     * @param aList
     *        the list to delete the list element from
     * @param aListElement
     *        the list element to delete
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteListElement(FastList aList, FastListElement aListElement) {
        if (aList == null || aListElement == null) return false;
        return aList.removeElement(aListElement);
    }



    /**
     * Deletes all values of all attributes of all objects of the given ME type or a subtype
     * of the given ME type.
     *
     * @param aMetaElementType
     *        the meta element to delete all values of
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaElementValues(String aMetaElementType) {
        return deleteMetaElementValues(aMetaElementType, true);
    }

    /**
     * Deletes all values of all attributes of all objects of the given ME type or a subtype
     * of the given ME type.
     *
     * @param aMetaElementType
     *        the meta element to delete all values of
     * @param includeSubMetaElements
     *        flag to indicate whether also to delete instances of sub meta elements or only
     *        direct instances of given me
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaElementValues(String aMetaElementType, boolean includeSubMetaElements) {
        return deleteMetaElementValues(MetaElementUtil.getMetaElement(aMetaElementType), includeSubMetaElements);
    }

    /**
     * Deletes all values of all attributes of all objects of the given ME type or a subtype
     * of the given ME type.
     *
     * @param aMetaElement
     *        the meta element to delete all values of
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaElementValues(TLClass aMetaElement) {
        return deleteMetaElementValues(aMetaElement, true);
    }

    /**
     * Deletes all values of all attributes of all objects of the given ME type or a subtype
     * of the given ME type.
     *
     * @param aMetaElement
     *        the meta element to delete all values of
     * @param includeSubMetaElements
     *        flag to indicate whether also to delete instances of sub meta elements or only
     *        direct instances of given me
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaElementValues(TLClass aMetaElement, boolean includeSubMetaElements) {
        if (aMetaElement == null) return false;
        boolean result = false;
        if (includeSubMetaElements) {
			for (TLClass me : aMetaElement.getSpecializations()) {
                result |= deleteMetaElementValues(me, includeSubMetaElements);
            }
        }
		for (Wrapper object : MetaElementUtil.getAllDirectInstancesOf(aMetaElement)) {
			object.tDelete();
			result = true;
        }
        return result;
    }



    /**
     * Deletes all attribute values of attributes of the given name (meta or flex
     * attribute) of all objects of the given ME type. If the attribute is an mandatory attribute,
     * the mandatory state will be set to <code>false</code>.
     *
     * @param aMetaElementType
     *        the ME type of the objects from which to delete the given attribute values
     * @param aAttributeName
     *        the name of the (meta or flex) attribute to delete values from
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaAttributeValues(String aMetaElementType, String aAttributeName) {
        return deleteMetaAttributeValues(MetaElementUtil.getMetaElement(aMetaElementType), aAttributeName, true);
    }

    /**
     * Deletes all attribute values of attributes of the given name (meta or flex
     * attribute) of all objects of the given ME type.
     *
     * @param aMetaElementType
     *        the ME type of the objects from which to delete the given attribute values
     * @param aAttributeName
     *        the name of the (meta or flex) attribute to delete values from
     * @param deleteMandatory
     *        if <code>true</code> and the attribute is an mandatory attribute,
     *        the mandatory state will be set to <code>false</code>.
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaAttributeValues(String aMetaElementType, String aAttributeName, boolean deleteMandatory) {
        return deleteMetaAttributeValues(MetaElementUtil.getMetaElement(aMetaElementType), aAttributeName, deleteMandatory);
    }

    /**
     * Deletes all attribute values of attributes of the given name (meta or flex
     * attribute) of all objects of the given ME type. If the attribute is an mandatory attribute,
     * the mandatory state will be set to <code>false</code>.
     *
     * @param aMetaElement
     *        the ME type of the objects from which to delete the given attribute values
     * @param aAttributeName
     *        the name of the (meta or flex) attribute to delete values from
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaAttributeValues(TLClass aMetaElement, String aAttributeName) {
        return deleteMetaAttributeValues(aMetaElement, aAttributeName, true);
    }



    /**
     * Deletes all attribute values of attributes of the given name (meta or flex
     * attribute) of all objects of the given ME type. If the attribute is an mandatory attribute,
     * the mandatory state will be set to <code>false</code>.
     *
     * @param aMetaElement
     *        the ME type of the objects from which to delete the given attribute values
     * @param aAttributeName
     *        the name of the (meta or flex) attribute to delete values from
     * @param deleteMandatory
     *        if <code>true</code> and the attribute is an mandatory attribute,
     *        the mandatory state will be set to <code>false</code>.
     * @return <code>true</code>, if something were deleted, <code>false</code> otherwise
     */
    public static boolean deleteMetaAttributeValues(TLClass aMetaElement, String aAttributeName, boolean deleteMandatory) {
        if (aMetaElement == null || aAttributeName == null) return false;
        boolean result = false;
        if (MetaElementUtil.hasLocalMetaAttribute(aMetaElement, aAttributeName)) {
            try {
				TLStructuredTypePart metaAttribute = MetaElementUtil.getLocalMetaAttribute(aMetaElement, aAttributeName);
				if (AttributeOperations.isReadOnly(metaAttribute)) {
                    return false;
                }
                else if (metaAttribute.isMandatory()) {
                    if (!deleteMandatory) return false;
                    metaAttribute.setMandatory(false);
                    result = true;
                }
            }
            catch (NoSuchAttributeException e) {
                // ignore
            }
        }
        for (Wrapper object : MetaElementUtil.getAllInstancesOf(aMetaElement)) {
            Object value = object.getValue(aAttributeName);
            if (value != null && ( !(value instanceof Collection) || !((Collection)value).isEmpty() )) {
                object.setValue(aAttributeName, null);
                result = true;
            }
        }
        return result;
    }

    /**
     * Changes the mandatory state of the given meta attribute.
     *
     * @param aMetaElementType
     *        the meta element of the meta attribute
     * @param aMetaAttributeName
     *        the meta attribute to change the mandatory state for
     * @param mandatory
     *        the new mandatory state for the meta attribute
     * @return <code>true</code>, if something was changed, <code>false</code> otherwise
     * @throws NoSuchAttributeException
     *         if the given meta attribute was not found.
     */
    public static boolean changeMetaAttributeMandatoryState(String aMetaElementType, String aMetaAttributeName, boolean mandatory) throws NoSuchAttributeException {
        TLClass metaElement = MetaElementUtil.getMetaElement(aMetaElementType);
        if (metaElement == null) return false;
		TLStructuredTypePart metaAttribute = MetaElementUtil.getLocalMetaAttribute(metaElement, aMetaAttributeName);
        return changeMetaAttributeMandatoryState(metaAttribute, mandatory);
    }



    /**
     * Changes the mandatory state of the given meta attribute.
     *
     * @param aMetaAttribute
     *        the meta attribute to change the mandatory state for
     * @param mandatory
     *        the new mandatory state for the meta attribute
     * @return <code>true</code>, if something was changed, <code>false</code> otherwise
     */
    public static boolean changeMetaAttributeMandatoryState(TLStructuredTypePart aMetaAttribute, boolean mandatory) {
        boolean oldValue = aMetaAttribute.isMandatory();
        if (mandatory != oldValue) {
            aMetaAttribute.setMandatory(mandatory);
            return true;
        }
        return false;
    }

}
