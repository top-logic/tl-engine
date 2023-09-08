/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.IdentifierUtil;

/**
 * Parses the attribute id and the object id out of the attribute field name.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AttributeIdParser {

	protected final String attributeFieldName;

	/**
	 * Takes care of parsing an AttributeFieldName.
	 * 
	 * <p>
	 * Performance note: The parsing happens in the constructor, which means, just once.
	 * </p>
	 * 
	 * @param attributeFieldName
	 *        Must not be <code>null</code>.
	 */
	protected AttributeIdParser(String attributeFieldName) {
		assert (attributeFieldName != null) : new IllegalArgumentException("AttributeFieldName must not be null.");
		this.attributeFieldName = attributeFieldName;
	}

	public abstract boolean isAttributeIdParsable();

	public abstract boolean isObjectIdParsable();

	/**
	 * The attribute id from the attribute field name.
	 * 
	 * @return Is never <code>null</code>. Throws an assertion if AttributeFieldName is not
	 *         parsable.
	 */
	public abstract String getAttributeId();

	/**
	 * The object id from the attribute field name.
	 * 
	 * @return Is never <code>null</code>. Throws an assertion if AttributeFieldName is not
	 *         parsable.
	 */
	public abstract String getObjectId();

	/**
	 * Creates a new {@link AttributeIdParser} for the given attribute field name.
	 * 
	 * @param attributeFieldName
	 *        must not be <code>null</code>.
	 */
	public static AttributeIdParser newAttributeIdParser(String attributeFieldName) {
		if (IdentifierUtil.SHORT_IDS) {
			return new ShortAttributeIdParser(attributeFieldName);
		} else {
			return new LongAttributeIdParser(attributeFieldName);
		}
	}

}
