/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service.importer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.AssociationStorage;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.storage.ForeignKeyStorage;
import com.top_logic.kafka.knowledge.service.AbstractModelBasedKafkaConfiguration;
import com.top_logic.kafka.knowledge.service.TLImported;
import com.top_logic.kafka.knowledge.service.TLSynced;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Default {@link KafkaImportConfiguration} based on {@link ModelService#getApplicationModel() the
 * application model}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModelBasedImportConfiguration extends AbstractModelBasedKafkaConfiguration
		implements KafkaImportConfiguration {

	/**
	 * Typed configuration interface definition for {@link ModelBasedImportConfiguration}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractModelBasedKafkaConfiguration.Config {
		// configuration interface definition
	}

	private final Map<String, ObjectKey> _typeMappingSource = new HashMap<>();

	private final Map<String, String> _attributeMappingSource = new HashMap<>();

	private final Mapping<String, ObjectKey> _typeMapping = input -> _typeMappingSource.get(input);

	private final Mapping<String, String> _attributeMapping = input -> _attributeMappingSource.get(input);

	private final Set<MetaObject> _staticAssociationTypes;

	private final Set<MetaObject> _allAssociationTypes = new HashSet<>();

	/**
	 * Create a {@link ModelBasedImportConfiguration}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ModelBasedImportConfiguration(InstantiationContext context, Config config) {
		super(context, config);
		_staticAssociationTypes = findStaticAssociationTypes(context);

		handleModelPartChanged();
		attachAsListener();
	}

	private void clearCache() {
		_typeMappingSource.clear();
		_attributeMappingSource.clear();
		_allAssociationTypes.clear();
	}

	@Override
	protected void handleModelPartChanged() {
		super.handleModelPartChanged();
		clearCache();
		_allAssociationTypes.addAll(_staticAssociationTypes);
		processTypes();
		afterTypeSystemAnalysis();
	}

	private void processTypes() {
		for (TLClass type : getGlobalClassesSorted()) {
			processType(type);
		}
	}

	private void processType(TLClass type) {
		TLImported importedAnnotation = getAnnotation(type);
		boolean isTypeImported = (importedAnnotation != null) && importedAnnotation.getValue();
		String importedTypeName = getImportedTypeName(type, importedAnnotation);
		if (isTypeImported) {
			_typeMappingSource.put(importedTypeName, type.tId());
		}
		for (TLClassPart part : type.getAllClassParts()) {
			processTypePart(isTypeImported, type, part, importedTypeName);
		}

	}

	private String getImportedTypeName(TLClass type, TLImported importedAnnotation) {
		if ((importedAnnotation == null) || importedAnnotation.getSource().isEmpty()) {
			/* If no name is configured, use the name of target type also as source type. */
			return TLModelUtil.qualifiedName(type);
		}
		return importedAnnotation.getSource();
	}

	private void processTypePart(boolean isTypeImported, TLClass owner, TLClassPart part, String importedTypeName) {
		TLImported annotation = getAnnotation(owner, part.getName());
		if (annotation != null && !annotation.getValue()) {
			return;
		}
		if ((annotation == null) && !isTypeImported) {
			return;
		}
		handleTypePartAnnotation(owner, part.getName(), annotation);
		String importedAttributeName = getImportedAttributeName(part, annotation);
		StorageDetail storageDetail = AttributeOperations.getStorageImplementation(part);
		if (storageDetail instanceof AssociationStorage) {
			addAssociationType((AssociationStorage) storageDetail);
		}
		String sourceQualifiedPartName =
			importedTypeName + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + importedAttributeName;
		String targetMoName = getTargetMoName(part, storageDetail);
		_attributeMappingSource.put(sourceQualifiedPartName, targetMoName);

	}

	private String getImportedAttributeName(TLClassPart part, TLImported annotation) {
		if (annotation == null || annotation.getSource().isEmpty()) {
			/* If no name is configured, use the name of target part also as source name. */
			return part.getName();
		}
		return annotation.getSource();
	}

	private void addAssociationType(AssociationStorage storage) {
		String associationTableName = storage.getTable();
		try {
			_allAssociationTypes.add(typeSystem().getType(associationTableName));
		} catch (UnknownTypeException ex) {
			Logger.error("Misconfigured type system. Table " + associationTableName + " does not exist.", ex,
				ModelBasedImportConfiguration.class);
		}
	}

	private String getTargetMoName(TLClassPart part, StorageDetail storageDetail) {
		if (storageDetail instanceof ForeignKeyStorage<?>) {
			return ((ForeignKeyStorage<?>) storageDetail).getStorageAttribute();
		}
		return part.getName();
	}

	@Override
	public synchronized Mapping<? super String, ObjectKey> getTypeMapping() {
		return _typeMapping;
	}

	@Override
	public synchronized Mapping<? super String, String> getAttributeMapping() {
		return _attributeMapping;
	}

	@Override
	public synchronized Set<MetaObject> getAssociationTypes() {
		return _allAssociationTypes;
	}

	@Override
	public synchronized Function<Object, ?> getValueMapping(String qualifiedOwnerTypeName, String tlAttribute) {
		return getValueMapping(getTypeMapping().apply(qualifiedOwnerTypeName), tlAttribute);
	}

	@Override
	protected Class<? extends TLSynced> getAnnotationType() {
		return TLImported.class;
	}

}
