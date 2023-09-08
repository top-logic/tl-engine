/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service.exporter;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Objects.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.element.meta.AssociationStorage;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.storage.ForeignKeyStorage;
import com.top_logic.kafka.knowledge.service.AbstractModelBasedKafkaConfiguration;
import com.top_logic.kafka.knowledge.service.TLExported;
import com.top_logic.kafka.knowledge.service.TLSynced;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLTypePart;
import com.top_logic.util.model.ModelService;

/**
 * Default {@link KafkaExportConfiguration} based on {@link ModelService#getApplicationModel() the
 * application model}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModelBasedExportConfiguration extends AbstractModelBasedKafkaConfiguration
		implements KafkaExportConfiguration {

	/**
	 * Typed configuration interface definition for {@link ModelBasedExportConfiguration}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractModelBasedKafkaConfiguration.Config {
		// configuration interface definition
	}

	private final Set<MetaObject> _staticAssociationTypes;

	private final Set<MetaObject> _allAssociationTypes = new HashSet<>();

	private final Map<ObjectKey, Map<String, String>> _attributeNames = new HashMap<>();

	private final Set<ObjectKey> _attributeIds = new HashSet<>();

	private final Map<ObjectKey, String> _attributeIdToName = map();

	private final Map<ObjectKey, ObjectKey> _attributeIdToOwnerId = map();

	/**
	 * Create a {@link ModelBasedExportConfiguration}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ModelBasedExportConfiguration(InstantiationContext context, Config config) {
		super(context, config);
		_staticAssociationTypes = findStaticAssociationTypes(context);

		handleModelPartChanged();
		attachAsListener();
	}

	private void clearCache() {
		_attributeIds.clear();
		_attributeNames.clear();
		_allAssociationTypes.clear();
		_attributeIdToName.clear();
		_attributeIdToOwnerId.clear();
	}

	@Override
	protected void handleModelPartChanged() {
		super.handleModelPartChanged();
		clearCache();
		_allAssociationTypes.addAll(_staticAssociationTypes);
		for (TLClass type : getGlobalClassesSorted()) {
			processType(type);
		}
		afterTypeSystemAnalysis();
	}

	private void processType(TLClass type) {
		TLExported externalizeAnnotation = getAnnotation(type);
		boolean isTypeExported = (externalizeAnnotation != null) && externalizeAnnotation.getValue();
		/* TODO #22251: Here it would be fine to be able to say subtypes are not externalized.
		 * This is currently not easy, because for associations (hasWrapperAttValue), it must be
		 * checked whether the ends are of correct subtype. Without exclude-subtype it must only
		 * be checked whether the attribute is of exported type. */
		Map<String, String> attributeNames = new HashMap<>();
		for (TLClassPart part : type.getAllClassParts()) {
			processTypePart(isTypeExported, type, part, attributeNames);
		}
		boolean anyAttributeHasExportAnnotation = !attributeNames.isEmpty();
		if (isTypeExported || anyAttributeHasExportAnnotation) {
			_attributeNames.put(type.tId(), attributeNames);
		}
	}

	private void processTypePart(boolean isTypeExported, TLClass owner, TLClassPart part,
			Map<String, String> attributeNames) {
		if (part.getName().equals(BasicTypes.REV_CREATE_ATTRIBUTE_NAME)) {
			/* revCreate is a special attribute of the KnowledgeBase. It cannot be transfered with
			 * TL-Sync, as it has to store the revision number in which an object was created in the
			 * local system. */
			return;
		}
		TLExported annotation = getAnnotation(owner, part.getName());
		if (annotation != null && !annotation.getValue()) {
			return;
		}
		if ((annotation == null) && !isTypeExported) {
			return;
		}
		handleTypePartAnnotation(owner, part.getName(), annotation);
		StorageDetail storageDetail = AttributeOperations.getStorageImplementation(part);
		if (storageDetail instanceof AssociationStorage) {
			addAssociationStorage((AssociationStorage) storageDetail);
		} else {
			String moName = getMoName(part, storageDetail);
			attributeNames.put(moName, part.getName());
		}
		addAttribute(part);
		addAttributeDefinition(part.getDefinition());
	}

	private void addAssociationStorage(AssociationStorage storage) {
		String associationTableName = storage.getTable();
		try {
			_allAssociationTypes.add(typeSystem().getType(associationTableName));
		} catch (UnknownTypeException ex) {
			Logger.error("Misconfigured type system. Table " + associationTableName + " does not exist.", ex,
				ModelBasedExportConfiguration.class);
		}
	}

	private String getMoName(TLClassPart part, StorageDetail storage) {
		if (storage instanceof ForeignKeyStorage) {
			// Inline reference. Where is it stored?
			return ((ForeignKeyStorage<?>) storage).getStorageAttribute();
		}
		// Primitive values are stored under the name of the part.
		return part.getName();
	}

	private void addAttributeDefinition(TLTypePart definition) {
		ObjectKey definitionId = definition.tId();
		if (!_attributeIds.contains(definitionId)) {
			_attributeIds.add(definitionId);
			addAttribute(definition);
		}
	}

	private void addAttribute(TLTypePart attribute) {
		_attributeIdToName.put(attribute.tId(), attribute.getName());
		_attributeIdToOwnerId.put(attribute.tId(), attribute.getOwner().tId());
	}

	@Override
	public synchronized Set<ObjectKey> getExportAttributeIds() {
		return _attributeIds;
	}

	@Override
	public synchronized Map<ObjectKey, Map<String, String>> getExportAttributeNames() {
		return _attributeNames;
	}

	@Override
	public synchronized String getAttributeName(ObjectKey attributeId) {
		/* When the attribute-id or the result is null, something is broken. Force an exception to
		 * prevent hard to debug follow-up-problems. */
		return requireNonNull(_attributeIdToName.get(requireNonNull(attributeId)));
	}

	@Override
	public synchronized ObjectKey getAttributeOwnerId(ObjectKey attributeId) {
		/* Dito. */
		return requireNonNull(_attributeIdToOwnerId.get(requireNonNull(attributeId)));
	}

	@Override
	public synchronized Set<MetaObject> getAssociationTypes() {
		return _allAssociationTypes;
	}

	@Override
	public KnowledgeBase getKnowledgeBase() {
		return kb();
	}

	@Override
	protected Class<? extends TLSynced> getAnnotationType() {
		return TLExported.class;
	}

}
