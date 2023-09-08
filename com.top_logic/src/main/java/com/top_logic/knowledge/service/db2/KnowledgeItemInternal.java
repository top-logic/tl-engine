/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * {@link DBKnowledgeBase} internal functionality of a {@link KnowledgeItem}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/ 
interface KnowledgeItemInternal extends KnowledgeItem {

	/**
	 * Type safe variant of {@link KnowledgeItem#getKnowledgeBase()}.
	 */
	@Override
	DBKnowledgeBase getKnowledgeBase();
	
	/**
	 * Type-safe internal version of {@link KnowledgeItem#tId()}.
	 */
	@Override
	DBObjectKey tId();

	/**
	 * Checks that this item is alive in the given context.
	 * 
	 * @throws DeletedObjectAccess
	 *         if item is deleted in the given context.
	 */
	void checkAlive(DBContext context) throws DeletedObjectAccess;

	/**
	 * Initializes this object after it has been fetched from persistent
	 * storage.
	 */
    void onLoad(PooledConnection readConnection);

	/**
	 * Returns the object key of the object that is referenced by the given attribute without any
	 * local modifications.
	 * 
	 * @param reference
	 *        must not be <code>null</code> and part of the {@link #tTable()} of this
	 *        {@link KnowledgeItem}.
	 * 
	 * @return the object key of the referenced object without resolving it or <code>null</code> if
	 *         no object is referenced.
	 */
	ObjectKey getGlobalReferencedKey(MOReference reference);
	
	/**
	 * @see #getValue(MOAttribute)
	 */
	Object getValue(MOAttribute attribute, long revision);

	/**
	 * @see #getAttributeValue(String)
	 */
	Object getAttributeValue(String attrName, long revision) throws NoSuchAttributeException;
	
	/**
	 * @see #getGlobalAttributeValue(String)
	 */
	Object getGlobalAttributeValue(String attribute, long revision) throws NoSuchAttributeException;

	/**
	 * @see #getReferencedKey(MOReference)
	 */
	ObjectKey getReferencedKey(MOReference reference, long revision);

	ObjectKey lookupKey(MOReference reference, Object[] storage);

	Object[] getGlobalValues();
}
