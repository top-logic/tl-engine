/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.meta.MOIndexImpl;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemUtil;

/**
 * {@link TypeProvider} for the storage table.
 */
public class ElementSecurityStorageTypes implements TypeProvider {

	/**
	 * Singleton {@link ElementSecurityStorageTypes} instance.
	 */
	public static final ElementSecurityStorageTypes INSTANCE = new ElementSecurityStorageTypes();

	private ElementSecurityStorageTypes() {
		// Singleton constructor.
	}

	@Override
	public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
		try {
			tryCreateTypes(typeRepository);
		} catch (DuplicateAttributeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} catch (IncompatibleTypeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} catch (DuplicateTypeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private void tryCreateTypes(MORepository typeRepository)
			throws DuplicateAttributeException, IncompatibleTypeException, DuplicateTypeException {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(SecurityStorage.SECURITY_STORAGE_OBJECT_NAME);
		type.setDBName(ElementSecurityStorage.TABLE_NAME);
		type.setCompress(1);
		type.setPkeyStorage(true);
		MOKnowledgeItemUtil.setSystem(type, true);
		type.setSuperclass(null);
		type.setVersioned(false);
		List<DBAttribute> pKeyAttributes = new ArrayList<>();
		{
			MOAttributeImpl attr =
				IdentifierTypes.newIdentifierAttribute("group", ElementSecurityStorage.ATTRIBUTE_GROUP);
			attr.setDBName(ElementSecurityStorage.ATTRIBUTE_GROUP);
			attr.setMandatory(true);
			pKeyAttributes.add(attr);
			type.addAttribute(attr);
		}
		MOAttributeImpl objectAttr;
		{
			objectAttr =
				IdentifierTypes.newIdentifierAttribute("object", ElementSecurityStorage.ATTRIBUTE_BUSINESS_OBJECT);
			objectAttr.setMandatory(true);
			pKeyAttributes.add(objectAttr);
			type.addAttribute(objectAttr);
		}
		MOAttributeImpl roleAttr;
		{
			roleAttr = IdentifierTypes.newIdentifierAttribute("role", ElementSecurityStorage.ATTRIBUTE_ROLE);
			roleAttr.setMandatory(true);
			pKeyAttributes.add(roleAttr);
			type.addAttribute(roleAttr);
		}
		MOAttributeImpl reasonAttr;
		{
			reasonAttr = new MOAttributeImpl("reason", MOPrimitive.SHORT);
			reasonAttr.setDBName(ElementSecurityStorage.ATTRIBUTE_REASON);
			reasonAttr.setMandatory(true);
			pKeyAttributes.add(reasonAttr);
			type.addAttribute(reasonAttr);
		}
		{
			DBAttribute[] columns = { objectAttr, roleAttr, reasonAttr };
			MOIndexImpl index = new MOIndexImpl("ObjectRoleReason", "BORR", columns, false, false, 1);

			type.addIndex(index);
			type.setPrimaryKey(pKeyAttributes.toArray(DBAttribute.NO_DB_ATTRIBUTES));
		}
		typeRepository.addMetaObject(type);
	}

}