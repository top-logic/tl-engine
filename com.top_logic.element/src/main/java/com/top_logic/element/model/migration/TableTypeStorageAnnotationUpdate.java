/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.knowledge.service.db2.migration.TLModelMigrationUtil.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.kbbased.storage.ForeignKeyStorage;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.service.db2.migration.rewriters.ModelIndexer;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.internal.PersistentModelPart;
import com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link EventRewriter} fixing storage implementation annotations for legacy table types.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableTypeStorageAnnotationUpdate extends ModelIndexer<ModelIndexer.Config<?>> {

	private ObjectKey _moduleKey;

	private Set<ObjectKey> _typeKeys = new HashSet<>();

	/**
	 * Creates a {@link TableTypeStorageAnnotationUpdate} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TableTypeStorageAnnotationUpdate(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void processModuleCreation(ObjectCreation creation) {
		super.processModuleCreation(creation);

		String moduleName = (String) creation.getValues().get(MODULE_NAME_ATTRIBUTE);
		if (ApplicationObjectUtil.LEGACY_TABLE_TYPES_MODULE.equals(moduleName)) {
			_moduleKey = creation.getObjectId().toCurrentObjectKey();
		}
	}

	@Override
	protected void processStructuredTypeCreation(ObjectCreation creation) {
		super.processStructuredTypeCreation(creation);

		ObjectKey moduleKey = (ObjectKey) creation.getValues().get(STRUCTURED_TYPE_MODULE_ATTRIBUTE);
		if (_moduleKey != null && _moduleKey.equals(moduleKey)) {
			_typeKeys.add(creation.getObjectId().toCurrentObjectKey());
		}
	}

	@Override
	protected void processStructuredTypePartCreation(ObjectCreation creation) {
		super.processStructuredTypePartCreation(creation);
		
		processChange(creation);
	}

	@Override
	protected void processStructuredTypePartUpdate(ItemUpdate update) {
		super.processStructuredTypePartUpdate(update);

		processChange(update);
	}

	private void processChange(ItemChange creation) {
		Map<String, Object> values = creation.getValues();
		ObjectKey typeKey = (ObjectKey) values.get(STRUCTURED_TYPE_PART_OWNER_ATTRIBUTE);
		if (_typeKeys.contains(typeKey)) {
			Object impl = values.get("impl");
			if ("reference".equals(impl)) {
				AnnotationConfigs annotations =
					(AnnotationConfigs) values.get(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE);
				if (annotations == null) {
					annotations = TypedConfiguration.newConfigItem(AnnotationConfigs.class);
				}

				String typeName = ApplicationObjectUtil.tableName(localName(getName(typeKey)));
				String partName = (String) values.get(TLTypePart.NAME_ATTRIBUTE);
				TLStorage storageAnnotation = TypedConfiguration.newConfigItem(TLStorage.class);
				ForeignKeyStorage.Config<?> refStorage =
					TypedConfiguration.newConfigItem(ForeignKeyStorage.Config.class);
				refStorage.setStorageType(typeName);
				refStorage.setStorageAttribute(partName);
				storageAnnotation.setImplementation(refStorage);

				annotations.getAnnotations().add(storageAnnotation);

				values.put(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE, annotations);
			}
		}
	}

	private String localName(String name) {
		return name.substring(name.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR) + 1);
	}

}
