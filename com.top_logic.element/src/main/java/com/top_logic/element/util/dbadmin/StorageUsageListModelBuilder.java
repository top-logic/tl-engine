/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.element.meta.kbbased.storage.LinkStorage.LinkStorageConfig;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.util.model.ModelService;

/**
 * {@link ListModelBuilder} that finds {@link TLClass}es and {@link TLReference}s that use the model
 * as storage table.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StorageUsageListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link StorageUsageListModelBuilder} instance.
	 */
	public static final StorageUsageListModelBuilder INSTANCE = new StorageUsageListModelBuilder();

	private StorageUsageListModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel == null) {
			return Collections.emptyList();
		}

		MetaObjectConfig table = (MetaObjectConfig) businessModel;

		String tableName = table.getObjectName();
		return Stream.<TLModelPart> concat(
			classesStream()
				.filter(t -> !t.isAbstract())
				.filter(t -> tableName.equals(TLAnnotations.getTable(t))),
			classesStream()
				.flatMap(c -> c.getLocalParts().stream())
				.filter(p -> p.getModelKind() == ModelKind.REFERENCE)
				.map(p -> (TLReference) p)
				.filter(r -> tableName.equals(storageTable(r))))
			.collect(Collectors.toList());
	}

	private static String storageTable(TLReference r) {
		TLStorage storageAnnotation = r.getAnnotation(TLStorage.class);
		if (storageAnnotation != null) {
			PolymorphicConfiguration<? extends StorageImplementation> storageConfig =
				storageAnnotation.getImplementation();
			if (storageConfig instanceof LinkStorageConfig) {
				return ((LinkStorageConfig) storageConfig).getTable();
			}
		}

		return WrapperMetaAttributeUtil.WRAPPER_ATTRIBUTE_ASSOCIATION;
	}

	private Stream<TLClass> classesStream() {
		return ModelService.getApplicationModel()
			.getModules()
			.stream()
			.flatMap(m -> m.getTypes().stream())
			.filter(t -> t.getModelKind() == ModelKind.CLASS)
			.map(t -> (TLClass) t);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null || (aModel instanceof MetaObjectConfig && !((MetaObjectConfig) aModel).isAbstract());
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return true;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return null;
	}

}
