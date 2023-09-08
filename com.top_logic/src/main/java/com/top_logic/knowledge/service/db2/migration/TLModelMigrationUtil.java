/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.util.Map;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;

/**
 * Util class for migration of {@link TLModelPart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLModelMigrationUtil {

	/** Name of the {@link MetaObject} which are used to store {@link TLModule}. */
	public static final String MODULE_MO = ApplicationObjectUtil.MODULE_OBJECT_TYPE;

	/**
	 * Name of the {@link MOAttribute} in {@value #MODULE_MO} containing
	 * {@link TLModule#getName()}.
	 */
	public static final String MODULE_NAME_ATTRIBUTE = TLModule.NAME_ATTRIBUTE;

	/** Name of the {@link MetaObject} which are used to Store {@link TLStructuredType}. */
	public static final String STRUCTURED_TYPE_MO = ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE;

	/**
	 * Name of the {@link MOAttribute} in {@value #STRUCTURED_TYPE_MO} containing
	 * {@link TLStructuredType#getName()}.
	 */
	public static final String STRUCTURED_TYPE_NAME_ATTRIBUTE = TLModule.NAME_ATTRIBUTE;

	/**
	 * Name of the {@link MOAttribute} in {@value #STRUCTURED_TYPE_MO} containing
	 * {@link TLStructuredType#getModule()}.
	 */
	public static final String STRUCTURED_TYPE_MODULE_ATTRIBUTE = ApplicationObjectUtil.META_ELEMENT_MODULE_REF;

	/** Name of the {@link MetaObject} which are used to Store {@link TLStructuredTypePart}. */
	public static final String STRUCTURED_TYPE_PART_MO = ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE;

	/**
	 * Name of the {@link MOAttribute} in {@value #STRUCTURED_TYPE_PART_MO} containing
	 * {@link TLStructuredTypePart#getName()}.
	 */
	public static final String STRUCTURED_TYPE_PART_NAME_ATTRIBUTE = TLTypePart.NAME_ATTRIBUTE;

	/**
	 * Name of the {@link MOAttribute} in {@value #STRUCTURED_TYPE_PART_MO} containing
	 * {@link TLStructuredTypePart#getOwner()}.
	 */
	public static final String STRUCTURED_TYPE_PART_OWNER_ATTRIBUTE = ApplicationObjectUtil.META_ELEMENT_ATTR;

	/**
	 * Searches for the {@link ObjectCreation} creating the {@link TLModule} with the given name.
	 * 
	 * @param module
	 *        The {@link TLModule#getName() name} of the {@link TLModule} to find creation for.
	 * @return May be <code>null</code> in case the module is not created in the given
	 *         {@link ChangeSet}.
	 */
	public static ObjectCreation getTLModuleCreation(ChangeSet cs, String module) {
		for (ObjectCreation event : cs.getCreations()) {
			String tableName = event.getObjectType().getName();
			if (MODULE_MO.equals(tableName)) {
				Map<String, Object> values = event.getValues();
				if (module.equals(values.get(MODULE_NAME_ATTRIBUTE))) {
					return event;
				}
			}
		}
		return null;
	}

	/**
	 * Searches for the {@link ObjectCreation} creating the {@link TLStructuredType} with the given
	 * name in the given {@link TLModule}.
	 * 
	 * @param module
	 *        The {@link ObjectKey} of the {@link TLModule} containing the type.
	 * @param type
	 *        The {@link TLStructuredType#getName() name} of the {@link TLStructuredType} to find
	 *        creation for.
	 * @return May be <code>null</code> in case the type is not created in the given
	 *         {@link ChangeSet}.
	 */
	public static ObjectCreation getTLTypeCreation(ChangeSet cs, ObjectKey module, String type) {
		for (ObjectCreation event : cs.getCreations()) {
			String tableName = event.getObjectType().getName();
			if (STRUCTURED_TYPE_MO.equals(tableName)) {
				Map<String, Object> values = event.getValues();
				if (type.equals(values.get(STRUCTURED_TYPE_NAME_ATTRIBUTE))
					&& module.equals(values.get(STRUCTURED_TYPE_MODULE_ATTRIBUTE))) {
					return event;
				}
			}
		}
		return null;
	}

	/**
	 * Searches for the {@link ObjectCreation} creating the {@link TLStructuredTypePart} with the
	 * given name in the given {@link TLStructuredType}.
	 * 
	 * @param type
	 *        The {@link ObjectKey} of the {@link TLStructuredType} holding the part.
	 * @param part
	 *        The {@link TLStructuredTypePart#getName() name} of the {@link TLStructuredTypePart} to
	 *        find creation for.
	 * @return May be <code>null</code> in case the type part is not created in the given
	 *         {@link ChangeSet}.
	 */
	public static ObjectCreation getTLTypePartCreation(ChangeSet cs, ObjectKey type, String part) {
		for (ObjectCreation event : cs.getCreations()) {
			String tableName = event.getObjectType().getName();
			if (STRUCTURED_TYPE_PART_MO.equals(tableName)) {
				Map<String, Object> values = event.getValues();
				if (part.equals(values.get(STRUCTURED_TYPE_PART_NAME_ATTRIBUTE))
					&& type.equals(values.get(STRUCTURED_TYPE_PART_OWNER_ATTRIBUTE))) {
					return event;
				}
			}
		}
		return null;
	}

}

