/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import java.util.Map;

import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeValue;

/**
 * Reference to an object by values of a unique index.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @deprecated Use {@link IndexedObjectNaming}
 */
@Deprecated
public interface IndexRef extends VersionedObjectRef {

	/**
	 * The name of the object's {@link MOClass}.
	 */
	String getTableName();
	void setTableName(String typeName);

	/**
	 * {@link AttributeValue}s that describe values of a unique index of the
	 * referenced object.
	 */
	@EntryTag("attribute")
	@Key(AttributeValue.NAME_ATTRIBUTE)
	Map<String, AttributeValue> getKeyValues();
	
	/**
	 * Sets the {@link #getKeyValues()} property.
	 */
	void setKeyValues(Map<String, AttributeValue> keyValues);

}
