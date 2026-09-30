/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import java.io.IOException;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;

/**
 * Some GUI functions to handle MetaAttributes.
 *
 * @author    <a href="mailto:kbu@top-logic.com"></a>
 */
public class MetaAttributeGUIHelper {

    /** Normal attribute I/O. Used for creation, edition and (non-range) search. */
	public static final String ATT_PREFIX = "att";

	/**
	 * No need for a public constructor
	 */
	private MetaAttributeGUIHelper() {
		super();
	}

	/**
	 * Short-cut for {@link #getAttributeID(TLStructuredTypePart, TLObject)}, where only an
	 * attribute name is available.
	 */
	public static String getAttributeID(String attributeName, TLObject object) throws NoSuchAttributeException {
		TLStructuredTypePart attribute = object.tType().getPart(attributeName);
		return getAttributeID(attribute, object);
	}

	/**
	 * Creates an ID for a field displaying an the given attribute of the given edited object.
	 *
	 * @param attribute
	 *        The edited attribute. Must not be <code>null</code>.
	 * @param object
	 *        The edited object, must not be <code>null</code>. For a create field ID use
	 *        {@link #getAttributeIDCreate(TLStructuredTypePart)}.
	 * @return An ID for a form field.
	 */
	public static String getAttributeID(TLStructuredTypePart attribute, TLObject object) {
		return internalID(attribute, object, null);
    }

	/**
	 * Creates an ID for a field displaying an the given attribute for a new object being created.
	 *
	 * @param attribute
	 *        The edited attribute. Must not be <code>null</code>.
	 * @return An ID for a form field.
	 * 
	 * @see #getAttributeIDCreate(TLStructuredTypePart, String)
	 */
	public static String getAttributeIDCreate(TLStructuredTypePart attribute) {
		return getAttributeIDCreate(attribute, null);
	}

	/**
	 * Creates an ID for a field displaying an the given attribute for a new object being created.
	 *
	 * @param attribute
	 *        The edited attribute. Must not be <code>null</code>.
	 * @param domain
	 *        The name of the object being created. Can be used to differentiate fields for the same
	 *        attribute of multiple objects being created at once. If no domain is required,
	 *        {@link #getAttributeIDCreate(TLStructuredTypePart)} should be used.
	 * @return An ID for a form field.
	 */
	public static String getAttributeIDCreate(TLStructuredTypePart attribute, String domain) {
		return internalID(attribute, (TLObject) null, domain);
	}

	/**
	 * Creates an ID for the field represented by the given {@link AttributeUpdate}.
	 */
	public static String getAttributeID(AttributeUpdate update) {
		if (update.isUpdateForCreate()) {
			return getAttributeIDCreate(update.getAttribute(), update.getDomain());
		} else {
			return getAttributeID(update.getAttribute(), update.getObject());
		}
	}

	/**
	 * @see #internalID(String, TLStructuredTypePart, TLObject, String)
	 */
	@FrameworkInternal
	public static String internalID(TLStructuredTypePart attribute, TLObject object, String domain)
			throws IllegalArgumentException {
	    return internalID(ATT_PREFIX, attribute, object, domain);
	}

	private static void appendObjectId(StringBuilder buffer, TLObject object) {
		try {
			KBUtils.getWrappedObjectName(object).appendExternalForm(buffer);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	/**
	 * Get an ID for an attribute on an object. The object may be <code>null</code> e.g. for a
	 * search.
	 *
	 * @param prefix
	 *        An arbitrary prefix to prepend to the ID, may be <code>null</code>.
	 * @param attribute
	 *        the attribute. Must not be <code>null</code>.
	 * @param object
	 *        the object. May be <code>null</code>.
	 * @return the ID
	 * @throws IllegalArgumentException
	 *         if the attribute is <code>null</code>
	 */
	@FrameworkInternal
	public static String internalID(String prefix, TLStructuredTypePart attribute, TLObject object, String domain)
			throws IllegalArgumentException {
		// Check params
		if (attribute == null) {
			throw new IllegalArgumentException("Attribute must not be null.");
		}
		
		/* Ensure that overridden attributes produce the same member names. */
		TLStructuredTypePart definition = attribute.getDefinition();

		boolean hasPrefix = prefix != null;
		boolean hasObject = object != null && !(object instanceof TransientObject);
		boolean hasDomain = !StringServices.isEmpty(domain);
		
		final String theID;
		if (hasPrefix || hasObject || hasDomain) {
			StringBuilder buffer = new StringBuilder();
		
			if (hasPrefix) {
				buffer.append(prefix);
				buffer.append(AttributeUpdateContainer.ID_SEPARATOR);
			}

			if (hasDomain) {
				buffer.append(domain.replace('.', '_'));
				buffer.append(AttributeUpdateContainer.ID_SEPARATOR);
			}

			appendObjectId(buffer, definition);

			// Add object ID if available
			if (hasObject) {
				buffer.append(AttributeUpdateContainer.ID_SEPARATOR);
				appendObjectId(buffer, object);
			}
		
			theID = buffer.toString();
		} else {
			theID = IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(definition));
		}
		
		return toFormId(theID);
    }

	/**
	 * Make ID GUI-suitable
	 */
	private static String toFormId(final String theID) {
		return theID.replace(':', '_').replace('-', '_');
	}

	/**
	 * @param object
	 *        Unused, an update with a domain has no underlying object.
	 * @deprecated Use {@link #getAttributeIDCreate(TLStructuredTypePart, String)}
	 */
	@Deprecated
	public static String getAttributeID(TLStructuredTypePart attribute, TLObject object, String domain) {
		return getAttributeIDCreate(attribute, domain);
	}

	/**
	 * @deprecated Use {@link #getAttributeID(String, TLObject)}
	 */
	@Deprecated
	public static String getFormMemberIDForAttribute(String attributeName, TLObject object)
			throws NoSuchAttributeException {
		return getAttributeID(attributeName, object);
	}

}
