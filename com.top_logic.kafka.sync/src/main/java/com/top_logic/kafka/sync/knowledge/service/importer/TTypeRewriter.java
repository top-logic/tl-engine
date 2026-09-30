/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.knowledge.service.importer;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static com.top_logic.basic.util.Utils.*;
import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static java.util.Collections.*;
import static java.util.Comparator.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.shared.collection.map.MappedCollection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.kafka.sync.knowledge.service.exporter.TypeFilterRewriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.ConfiguredRewritingEventVisitor;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ExtReference;
import com.top_logic.knowledge.objects.identifier.ExtReferenceFormat;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.InternalExpressionFactory;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.ExtIDFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.util.TLModelUtil;

/**
 * Rewrites the {@link PersistentObject#TYPE_REF type} of an {@link TLObject}, and the
 * {@link ApplicationObjectUtil#META_ATTRIBUTE_ATTR attribute} reference of a link, due to a
 * configured mapping.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TTypeRewriter extends ConfiguredRewritingEventVisitor<TTypeRewriter.Config> {

	private interface ResolveType {

		void handleTypeResolved(TLType type);

	}

	private class ResolveExtReference implements ResolveType {

		private final ExtReference _reference;

		private final ResolveObject _callback;

		public ResolveExtReference(ExtReference reference, ResolveObject callback) {
			_reference = reference;
			_callback = callback;
		}

		@Override
		public void handleTypeResolved(TLType type) {
			MOClass table = getTable(type);
			getResolveObjects(table, _reference).add(_callback);
		}

	}

	private interface ResolveObject {

		void handleObjectKeyResolved(ObjectKey objectKey);

	}

	private final class SetObjectId implements ResolveObject {

		private final ItemEvent _event;

		SetObjectId(ItemEvent event) {
			_event = event;
		}

		@Override
		public void handleObjectKeyResolved(ObjectKey objectKey) {
			_event.setObjectId(ObjectBranchId.toObjectBranchId(objectKey));
		}
	}

	private final class SetAttributeValue implements ResolveObject {

		private final Map<String, Object> _values;

		private final String _targetAttributeName;

		SetAttributeValue(Map<String, Object> values, String targetAttributeName) {
			_values = values;
			_targetAttributeName = targetAttributeName;
		}

		@Override
		public void handleObjectKeyResolved(ObjectKey objectKey) {
			_values.put(_targetAttributeName, objectKey);
		}
	}

	/**
	 * Configuration of a {@link TTypeRewriter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredRewritingEventVisitor.Config<TTypeRewriter> {

		/**
		 * {@link KafkaImportConfiguration} for this {@link TTypeRewriter}.
		 */
		@InstanceFormat
		@Mandatory
		KafkaImportConfiguration getImportConfiguration();

		/**
		 * Setter for {@link #getImportConfiguration()}.
		 */
		void setImportConfiguration(KafkaImportConfiguration config);

	}

	final KnowledgeBase _kb = PersistencyLayer.getKnowledgeBase();

	private final Map<ObjectKey, List<ResolveType>> _resolveTypes = new HashMap<>();

	private final Map<MOClass, Map<ExtReference, List<ResolveObject>>> _resolveObjects = new HashMap<>();

	/**
	 * Map of the {@link ExtReference} of an newly created object to the new {@link ObjectKey}.
	 * 
	 * <p>
	 * This map is needed as newly created objects may be referenced in other events.
	 * </p>
	 */
	final Map<ExtReference, ObjectKey> _createdObjects = new HashMap<>();

	/**
	 * Creates a new {@link TTypeRewriter} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link TTypeRewriter}.
	 */
	public TTypeRewriter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void processEvents(ChangeSet cs) {
		// Ensure the callback maps are not changed concurrently
		synchronized (this) {
			try {
				super.processEvents(cs);
				resolveCallbacks(cs);
			} finally {
				_resolveTypes.clear();
				_resolveObjects.clear();
				_createdObjects.clear();
			}
		}
	}

	private void resolveCallbacks(ChangeSet cs) {
		List<KnowledgeItem> loadedTypes = BulkIdLoad.load(_kb, _resolveTypes.keySet());
		for (KnowledgeItem typeItem : loadedTypes) {
			TLType type = (TLType) typeItem.getWrapper();
			for (ResolveType callback : _resolveTypes.remove(type.tId())) {
				callback.handleTypeResolved(type);
			}
		}
		if (!_resolveTypes.isEmpty()) {
			Logger.warn("Unable to resolve types: " + _resolveTypes.keySet(), TTypeRewriter.class);
		}

		for (Entry<MOClass, Map<ExtReference, List<ResolveObject>>> entry : _resolveObjects.entrySet()) {
			MOClass table = entry.getKey();
			Map<ExtReference, List<ResolveObject>> callbacksByReference = entry.getValue();
			Set<ExtReference> unresolvedRefs = resolveItems(table, callbacksByReference);
			removeUnresolvedReferences(cs, unresolvedRefs);
		}
		/* The external reference attribute has to be removed from the changeset. It is explicitly
		 * stored in another attribute. But that removal has to happen after all unresolved
		 * references have been detected and handled, as it is needed there. Therefore, removing the
		 * external reference is done at the end of the code rewriting the changeset. */
		removeExternalReferenceAttribute(cs);
	}

	/** @return The {@link ExtReference}s that could not be resolved. */
	private Set<ExtReference> resolveItems(MOClass table, Map<ExtReference, List<ResolveObject>> callbacksByReference) {
		Set<ExtReference> extReferences = callbacksByReference.keySet();
		List<TLObject> resolvedObjects = resolveItemsPerBranch(table, extReferences);
		callCallbacks(callbacksByReference, resolvedObjects);
		return callbacksByReference.keySet();
	}

	private void callCallbacks(Map<ExtReference, List<ResolveObject>> callbacksByReference,
			Collection<TLObject> resolvedObjects) {
		for (TLObject resolvedObject : resolvedObjects) {
			ExtReference externalRef = ExtIDFactory.getInstance().lookupExtReference(resolvedObject);
			for (ResolveObject callback : callbacksByReference.remove(externalRef)) {
				callback.handleObjectKeyResolved(resolvedObject.tId());
			}
		}
		for (Entry<ExtReference, ObjectKey> createdObject : _createdObjects.entrySet()) {
			List<ResolveObject> callbacks = callbacksByReference.remove(createdObject.getKey());
			if (callbacks == null) {
				// No reference to newly created object.
				continue;
			}
			for (ResolveObject callback : callbacks) {
				callback.handleObjectKeyResolved(createdObject.getValue());
			}
		}
	}

	private void removeUnresolvedReferences(ChangeSet changeset, Set<ExtReference> unresolvedRefs) {
		if (!unresolvedRefs.isEmpty()) {
			removeUnresolvedObjects(changeset, unresolvedRefs);
			removeReferencesToUnresolvedObjects(changeset, unresolvedRefs);
			/* It is always an error when an ExtReference cannot be resolved: If it is used as the
			 * initial value of a mandatory attribute of a new object, that object's creation will
			 * fail. And that will cause the whole commit to fail, which will stop TL-Sync
			 * completely. */
			logError("Some external references could not be resolved: " + unresolvedRefs);
		}
	}

	private void removeUnresolvedObjects(ChangeSet changeset, Set<ExtReference> unresolvedRefs) {
		/* It is necessary to remove updates and deletions for objects which could not be resolved.
		 * But that must not happen for creations: Newly creates objects don't exist until this
		 * changeset is committed. Therefore, their ExtReferences can never be resolved. */
		removeUnresolvedObjects(unresolvedRefs, changeset.getDeletions());
		removeUnresolvedObjects(unresolvedRefs, changeset.getUpdates());
	}

	private void removeUnresolvedObjects(Collection<ExtReference> unresolvedRefs, List<? extends ItemChange> changes) {
		Iterator<? extends ItemChange> changeIterator = changes.iterator();
		while (changeIterator.hasNext()) {
			ItemChange change = changeIterator.next();
			if (unresolvedRefs.contains(change.getValues().get(TypeFilterRewriter.EXT_REFERENCE_ATTRIBUTE))) {
				changeIterator.remove();
			}
		}
	}

	private void removeReferencesToUnresolvedObjects(ChangeSet changeset, Set<ExtReference> unresolvedRefs) {
		/* It is necessary to remove references to unresolved objects in creations and updates. But
		 * that is not necessary for deletions: If an object is deleted, it cannot change its
		 * attribute values at the same time. */
		removeReferencesToUnresolvedObjects(unresolvedRefs, changeset.getCreations());
		removeReferencesToUnresolvedObjects(unresolvedRefs, changeset.getUpdates());
	}

	private void removeReferencesToUnresolvedObjects(
			Collection<ExtReference> unresolvedRefs, Collection<? extends ItemChange> changes) {
		Iterator<? extends ItemChange> changesIterator = changes.iterator();
		while (changesIterator.hasNext()) {
			ItemChange change = changesIterator.next();
			Iterator<Object> valuesIterator = change.getValues().values().iterator();
			while (valuesIterator.hasNext()) {
				if (!unresolvedRefs.contains(valuesIterator.next())) {
					continue;
				}
				if (isAssociationType(change.getObjectType())) {
					/* If one of the ends of an association is missing, the whole association needs
					 * to be dropped. */
					changesIterator.remove();
					break;
				}
				/* Just remove this single attribute change from the changeset, but still apply the
				 * other attribute changes. */
				valuesIterator.remove();
			}
		}
	}

	private void removeExternalReferenceAttribute(ChangeSet cs) {
		removeExternalReferenceAttribute(cs.getCreations());
		removeExternalReferenceAttribute(cs.getUpdates());
		removeExternalReferenceAttribute(cs.getDeletions());
	}

	private void removeExternalReferenceAttribute(Collection<? extends ItemChange> changes) {
		for (ItemChange change : changes) {
			change.getValues().remove(TypeFilterRewriter.EXT_REFERENCE_ATTRIBUTE);
		}
	}

	MOClass getTable(TLType type) {
		String tableName = TLAnnotations.getTable(type);
		MOClass table;
		try {
			table = (MOClass) _kb.getMORepository().getMetaObject(tableName);
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
		return table;
	}

	private ExtReference getExtReference(Map<String, Object> values) {
		return (ExtReference) values.get(TypeFilterRewriter.EXT_REFERENCE_ATTRIBUTE);
	}

	private String getExternalRefAttribute(MetaObject type) {
		return ExtIDFactory.getInstance().getExternalIDAttribute(type);
	}

	@Override
	public Object visitCreateObject(ObjectCreation event, Void arg) {
		Map<String, Object> values = event.getValues();
		if (isTrue((Boolean) values.get(TypeFilterRewriter.PLAIN_IMPORT_MARKER))) {
			return APPLY_EVENT;
		}
		ExtReference extReference = getExtReference(values);
		if (extReference == null) {
			logErrorNoExtReference(event);
			return SKIP_EVENT;
		}
		if (isAssociationType(event.getObjectType())) {
			return handleAssociationCreation(extReference, event);
		} else {
			return handleObjectCreation(extReference, event);
		}
	}

	private void logErrorNoExtReference(ItemChange event) {
		logError("Skipped '" + event + "' (event without external reference encountered).");
	}

	private Object handleAssociationCreation(ExtReference extReference, ObjectCreation event) {
		// Object ID is used by KnowledgeBase to create item
		event.setObjectId(new ObjectBranchId(event.getOwnerBranch(), event.getObjectType(), newID()));
		setExternalReferenceRef(event, extReference);
		return mapAssociationAttributes(event.getObjectType(), event.getValues());
	}

	private Object handleObjectCreation(ExtReference extReference, ObjectCreation event) {
		String srcType = extReference.getObjectType();
		ObjectKey typeKey = typeMapping(srcType);
		if (typeKey == null) {
			infoSrcTypeNotImported(event, srcType);
			return SKIP_EVENT;
		}
		getResolveTypes(typeKey).add(type -> {
			ObjectBranchId newID = setNewId(type, event);
			setExternalReferenceRef(event, extReference);
			_createdObjects.put(extReference, newID.toCurrentObjectKey());
		});
		Map<String, Object> values = event.getValues();
		mapAttributes(extReference, values);
		// Add as last because it may be removed otherwise in processing of attributes.
		values.put(PersistentObject.TYPE_REF, typeKey);
		return APPLY_EVENT;
	}

	private ObjectBranchId setNewId(TLType type, ObjectCreation event) {
		MOClass table = getTable(type);
		// Object ID is used by KnowledgeBase to create item
		ObjectBranchId newID = new ObjectBranchId(event.getOwnerBranch(), table, newID());
		event.setObjectId(newID);
		return newID;
	}

	private String name(ItemEvent event) {
		return event.getObjectType().getName();
	}

	TLID newID() {
		// TODO #22251: Resurrected objects get new ID instead of old one.
		return _kb.createID();
	}

	void setExternalReferenceRef(ItemChange event, ExtReference extRef) {
		String externalIDAttribute = getExternalRefAttribute(event.getObjectType());
		event.getValues().put(externalIDAttribute, ExtReferenceFormat.INSTANCE.format(extRef));
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		Map<String, Object> values = event.getValues();
		if (isTrue((Boolean) values.get(TypeFilterRewriter.PLAIN_IMPORT_MARKER))) {
			return APPLY_EVENT;
		}
		ExtReference typeRef = getExtReference(values);
		if (typeRef == null) {
			logErrorNoExtReference(event);
			return SKIP_EVENT;
		}
		if (isAssociationType(event.getObjectType())) {
			return handleAssociationUpdate(typeRef, event);
		} else {
			return handleObjectUpdate(event, typeRef);
		}
	}

	private Object handleAssociationUpdate(ExtReference typeRef, ItemUpdate event) {
		MetaObject objectType = event.getObjectType();
		Object result = mapAssociationAttributes(objectType, event.getValues());
		if (result == SKIP_EVENT) {
			return SKIP_EVENT;
		}
		getResolveObjects((MOClass) event.getObjectType(), typeRef).add(new SetObjectId(event));
		Map<String, Object> oldValues = event.getOldValues();
		if (oldValues != null) {
			result = mapAssociationAttributes(objectType, event.getOldValues());
		}
		return result;
	}

	private Object handleObjectUpdate(ItemUpdate event, ExtReference typeRef) {
		Map<String, Object> values = event.getValues();
		String srcType = typeRef.getObjectType();
		ObjectKey typeKey = typeMapping(srcType);
		if (typeKey == null) {
			infoSrcTypeNotImported(event, srcType);
			return SKIP_EVENT;
		}
		getResolveTypes(typeKey).add(new ResolveExtReference(typeRef, new SetObjectId(event)));
		mapAttributes(typeRef, values);
		// Add as last because it may be removed otherwise in processing of attributes.
		values.put(PersistentObject.TYPE_REF, typeKey);
		Map<String, Object> oldValues = event.getOldValues();
		if (oldValues != null) {
			mapAttributes(typeRef, oldValues);
			// Add as last because it may be removed otherwise in processing of attributes.
			// Type must not be changed
			oldValues.put(PersistentObject.TYPE_REF, typeKey);
		}
		return APPLY_EVENT;
	}

	private void infoSrcTypeNotImported(ItemEvent event, String srcType) {
		logInfo("Skipped '" + name(event) + "' (Source type '" + srcType + "' not imported).");
	}

	@Override
	public Object visitDelete(ItemDeletion event, Void arg) {
		Map<String, Object> values = event.getValues();
		if (isTrue((Boolean) values.get(TypeFilterRewriter.PLAIN_IMPORT_MARKER))) {
			return APPLY_EVENT;
		}
		ExtReference typeRef = getExtReference(values);
		if (typeRef == null) {
			logErrorNoExtReference(event);
			return SKIP_EVENT;
		}
		if (isAssociationType(event.getObjectType())) {
			return handleAssociationDeletion(typeRef, event);
		} else {
			return handleObjectDeletion(typeRef, event);
		}
	}

	private Object handleAssociationDeletion(ExtReference typeRef, ItemDeletion event) {
		Object result = mapAssociationAttributes(event.getObjectType(), event.getValues());
		if (result == APPLY_EVENT) {
			getResolveObjects((MOClass) event.getObjectType(), typeRef).add(new SetObjectId(event));
		}
		return result;
	}

	private Object mapAssociationAttributes(MetaObject objectType, Map<String, Object> values) {
		ExtReference source = (ExtReference) values.get(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		String sourceType = source.getObjectType();
		ObjectKey targetSourceTypeName = typeMapping(sourceType);
		if (targetSourceTypeName == null) {
			logInfo("Skipped '" + objectType.getName() + "' (Type '" + sourceType + "' of association source '"
				+ objectType.getName() + "' not imported).");
			return SKIP_EVENT;
		}
		String sourceAttributeName = (String) values.get(ApplicationObjectUtil.META_ATTRIBUTE_ATTR);
		String targetAttributeName = targetAttribute(sourceType, sourceAttributeName);
		if (targetAttributeName == null) {
			logInfo("Skipped '" + objectType.getName() + "' (Attribute '" + sourceAttributeName + "' of '" + sourceType
				+ "' not imported).");
			return SKIP_EVENT;
		}
		/* Don't map the "source" attribute: It is the container/owner of the mapped value and must
		 * therefore not be mapped. */
		Object destinationResult = handleAssociationDestination(objectType, values, sourceType, sourceAttributeName);
		if (destinationResult == SKIP_EVENT) {
			return SKIP_EVENT;
		}
		getResolveTypes(targetSourceTypeName).add(new ResolveExtReference(source,
			new SetAttributeValue(values, DBKnowledgeAssociation.REFERENCE_SOURCE_NAME)));
		getResolveTypes(targetSourceTypeName).add(type -> setAttributeId(values, type, targetAttributeName));
		return APPLY_EVENT;

	}

	private Object handleAssociationDestination(MetaObject objectType, Map<String, Object> values, String sourceType,
			String sourceAttributeName) {
		try {
			Object dest = values.get(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
			Object destMapped = mapValue(sourceType, sourceAttributeName, dest);
			if (destMapped instanceof ExtReference) {
				ExtReference mappedExtRef = (ExtReference) destMapped;
				ObjectKey targetDestTypeName = typeMapping(mappedExtRef.getObjectType());
				if (targetDestTypeName == null) {
					logInfo("Skipped '" + objectType.getName() + "' (Type '" + sourceType + "' of association destination '"
						+ objectType.getName() + "' not imported).");
					return SKIP_EVENT;
				}
				getResolveTypes(targetDestTypeName).add(new ResolveExtReference(mappedExtRef,
					new SetAttributeValue(values, DBKnowledgeAssociation.REFERENCE_DEST_NAME)));
			} else {
				TLObject destTLObject = (TLObject) destMapped;
				values.put(DBKnowledgeAssociation.REFERENCE_DEST_NAME, destTLObject.tId());
			}
			return APPLY_EVENT;
		} catch (RuntimeException exception) {
			String message = "Failed to handle assocation destination. MetaObject: " + objectType + ". Source type: "
				+ sourceType + ". Source attribute name: " + sourceAttributeName;
			throw new RuntimeException(message, exception);
		}
	}

	private String targetAttribute(String sourceType, String sourceAttributeName) {
		return attributeMapping(sourceType + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + sourceAttributeName);
	}

	private String attributeMapping(String qualifiedAttributeName) {
		return getConfig().getImportConfiguration().getAttributeMapping().map(qualifiedAttributeName);
	}

	private void setAttributeId(Map<String, Object> values, TLType ownerType, String attributeName) {
		ObjectKey attributeId = getAttributeId(ownerType, attributeName);
		values.put(ApplicationObjectUtil.META_ATTRIBUTE_ATTR, attributeId);
	}

	private ObjectKey getAttributeId(TLType ownerType, String attributeName) {
		TLStructuredTypePart attribute = ((TLStructuredType) ownerType).getPart(attributeName);
		return attribute.getDefinition().tId();
	}

	private Object handleObjectDeletion(ExtReference typeRef, ItemDeletion event) {
		Map<String, Object> values = event.getValues();
		String srcType = typeRef.getObjectType();
		ObjectKey typeKey = typeMapping(srcType);
		if (typeKey == null) {
			infoSrcTypeNotImported(event, srcType);
			return SKIP_EVENT;
		}
		getResolveTypes(typeKey).add(new ResolveExtReference(typeRef, new SetObjectId(event)));
		getResolveTypes(typeKey).add(type -> setExternalReferenceRef(event, typeRef));
		mapAttributes(typeRef, values);
		// Add as last because it may be removed otherwise in processing of attributes.
		values.put(PersistentObject.TYPE_REF, typeKey);
		return APPLY_EVENT;
	}

	private void mapAttributes(ExtReference extReference, Map<String, Object> values) {
		String sourceTypeName = extReference.getObjectType();
		List<Entry<String, Object>> additionalEntries = null;
		for (Iterator<Entry<String, Object>> it = values.entrySet().iterator(); it.hasNext();) {
			Entry<String, Object> additionalEntry = handleAttribute(values, it, sourceTypeName);
			if (additionalEntry == null) {
				continue;
			}
			if (additionalEntries == null) {
				additionalEntries = new ArrayList<>();
			}
			additionalEntries.add(additionalEntry);
		}
		if (additionalEntries != null) {
			for (Entry<String, Object> entry : additionalEntries) {
				values.put(entry.getKey(), entry.getValue());
			}
		}
	}

	private Entry<String, Object> handleAttribute(Map<String, Object> values, Iterator<Entry<String, Object>> it,
			String sourceTypeName) {
		Entry<String, Object> entry = it.next();
		String sourceAttributeName = entry.getKey();
		Object value = entry.getValue();
		try {
			if (sourceAttributeName.equals(TypeFilterRewriter.EXT_REFERENCE_ATTRIBUTE)) {
				/* Keep this attribute as it will be used later to identify this changeset: When
				 * changes for objects are removed, which don't exist locally, this attribute is
				 * used to identify this changeset. */
				return null;
			}
			String targetAttributeName = targetAttribute(sourceTypeName, sourceAttributeName);
			if (targetAttributeName == null) {
				it.remove();
				// source attribute not imported
				return null;
			}

			Object mappedValue = mapValue(sourceTypeName, targetAttributeName, value);
			if (mappedValue instanceof TLObject) {
				mappedValue = ((TLObject) mappedValue).tId();
			} else if (mappedValue instanceof ExtReference) {
				// resolve ExtReference
				ExtReference extRef = (ExtReference) mappedValue;
				ObjectKey typeKey = typeMapping(extRef.getObjectType());
				if (typeKey == null) {
					Object ownerRef = values.get(TypeFilterRewriter.EXT_REFERENCE_ATTRIBUTE);
					logDebugAttributeValueNotImported(ownerRef, sourceTypeName, targetAttributeName, extRef);
					it.remove();
					return null;
				}
				getResolveTypes(typeKey)
					.add(new ResolveExtReference(extRef, new SetAttributeValue(values, targetAttributeName)));
				// Remove ExtReference, it is later reinstalled.
				it.remove();
				return null;
			}
			if (sourceAttributeName.equals(targetAttributeName) && Objects.equals(mappedValue, value)) {
				// No change
				return null;
			}
			// source attribute has been renamed.
			it.remove();
			// Can not modify values due to concurrent modification.
			return new AbstractMap.SimpleImmutableEntry<>(targetAttributeName, mappedValue);
		} catch (RuntimeException exception) {
			throw new RuntimeException("Failed to handle attribute " + sourceTypeName + "." + sourceAttributeName
				+ ". Value: " + debug(value));
		}
	}

	private Object mapValue(String qualifiedOwnerTypeName, String tlAttribute, Object value) {
		try {
			Function<Object, ?> valueMapping = getValueMapping(qualifiedOwnerTypeName, tlAttribute);
			if (valueMapping == null) {
				return value;
			}
			if (value instanceof ExtReference) {
				logError("It is not supported to map an ExtReference."
					+ "Value mappings are used to convert a value in a different form"
					+ " that contains the necessary data to reconstruct it."
					+ " But ExtReferences don't contain any data."
					+ " Therefore, it does not make sense to map an ExtReference."
					+ " Type: " + qualifiedOwnerTypeName + ". Attribute: " + tlAttribute + ". Value: " + value);
				/* Don't break the transmission, just drop the value. */
				return null;
			}
			return valueMapping.apply(value);
		} catch (RuntimeException exception) {
			String message = "Failed to map value of attribute " + tlAttribute + " on type " + qualifiedOwnerTypeName
				+ ". Value: " + debug(value);
			throw new RuntimeException(message, exception);
		}
	}

	private Function<Object, ?> getValueMapping(String qualifiedOwnerTypeName, String tlAttribute) {
		return getConfig().getImportConfiguration().getValueMapping(qualifiedOwnerTypeName, tlAttribute);
	}

	private void logDebugAttributeValueNotImported(Object ownerRef, String ownerType, String attribute,
			ExtReference valueRef) {
		if (isDebugLoggingEnabled()) {
			logDebug("Skipping attribute " + ownerType + "#" + attribute + " of object " + ownerRef
				+ " as the type of the value is not imported: " + valueRef);
		}
	}

	List<ResolveType> getResolveTypes(ObjectKey typeKey) {
		List<ResolveType> result = _resolveTypes.get(typeKey);
		if (result == null) {
			result = new ArrayList<>();
			_resolveTypes.put(typeKey, result);
		}
		return result;
	}

	List<ResolveObject> getResolveObjects(MOClass table, ExtReference reference) {
		Map<ExtReference, List<ResolveObject>> map = _resolveObjects.get(table);
		if (map == null) {
			map = new HashMap<>();
			_resolveObjects.put(table, map);
		}
		List<ResolveObject> list = map.get(reference);
		if (list == null) {
			list = new ArrayList<>();
			map.put(reference, list);
		}
		return list;
	}

	/** The result is in the same order as the given list of {@link ExtReference}s. */
	private List<TLObject> resolveItemsPerBranch(MOClass type, Collection<ExtReference> references) {
		switch (references.size()) {
			case 0: {
				return emptyList();
			}
			case 1: {
				ExtReference reference = references.iterator().next();
				long branch = reference.getBranchId();
				return search(type, branch, Collections.singletonList(reference));
			}
			default: {
				List<TLObject> result = null;
				List<ExtReference> sortedReferences = list(references);
				sortByBranchAndName(sortedReferences);
				long branch = sortedReferences.get(0).getBranchId();
				int start = 0;
				int stop = 1;
				while (stop < sortedReferences.size()) {
					long newBranch = sortedReferences.get(stop).getBranchId();
					if (newBranch != branch) {
						List<TLObject> searchResult = search(type, branch, sortedReferences.subList(start, stop));
						stop++;
						if (result == null) {
							result = searchResult;
						} else {
							result.addAll(searchResult);
						}
						start = stop;
						branch = newBranch;
						continue;
					}
					stop++;
				}
				List<TLObject> searchResult = search(type, branch, sortedReferences.subList(start, stop));
				if (result == null) {
					return searchResult;
				}
				result.addAll(searchResult);
				return result;
			}
		}
	}

	private void sortByBranchAndName(List<ExtReference> sortedReferences) {
		sortedReferences.sort(comparing(ExtReference::getBranchId).thenComparing(ExtReference::getObjectName));
	}

	private List<TLObject> search(MOClass type, long branch, Collection<ExtReference> objectNames) {
		AllOf allOfTyped = InternalExpressionFactory.allOfTyped(type);
		Expression filter = createSearchFilter(type, objectNames);
		RevisionQueryArguments queryArguments = ExpressionFactory.revisionArgs();
		queryArguments.setRequestedBranch(_kb.getHistoryManager().getBranch(branch));
		RevisionQuery<TLObject> query = queryResolved(filter(allOfTyped, filter), TLObject.class);
		return _kb.search(query, queryArguments);
	}

	private Expression createSearchFilter(MOClass type, Collection<ExtReference> objectNames) {
		if (objectNames.size() == 1) {
			return eqBinaryLiteral(extIdAttr(type), ExtReferenceFormat.INSTANCE.format(objectNames.iterator().next()));
		}
		return inLiteralSet(extIdAttr(type), new MappedCollection<>(ExtReferenceFormat.INSTANCE::format, objectNames));
	}

	private Expression extIdAttr(MOClass type) {
		String extIdAttrName = getExternalRefAttribute(type);
		MOAttribute extIdAttribute = type.getAttributeOrNull(extIdAttrName);
		if (extIdAttribute != null) {
			return InternalExpressionFactory.attributeTyped(extIdAttribute);
		} else {
			return InternalExpressionFactory.flexTyped(MOPrimitive.STRING, extIdAttrName);
		}
	}

	private ObjectKey typeMapping(String qualifiedTypeName) {
		return getConfig().getImportConfiguration().getTypeMapping().map(qualifiedTypeName);
	}

	private boolean isAssociationType(MetaObject type) {
		return getConfig().getImportConfiguration().getAssociationTypes().contains(type);
	}

	private static boolean isDebugLoggingEnabled() {
		return Logger.isDebugEnabled(TTypeRewriter.class);
	}

	private static void logDebug(String message) {
		Logger.debug(message, TTypeRewriter.class);
	}

	private static void logInfo(String message) {
		Logger.info(message, TTypeRewriter.class);
	}

	private static void logError(String message) {
		Logger.error(message, TTypeRewriter.class);
	}

}
