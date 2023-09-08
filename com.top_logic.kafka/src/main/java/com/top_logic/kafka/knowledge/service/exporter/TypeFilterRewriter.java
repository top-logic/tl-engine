/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service.exporter;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.shared.collection.map.MapUtilShared.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.kafka.knowledge.service.KafkaExportImportConfiguration;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CopyKnowledgeEventVisitor;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ExtReference;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.ExtIDFactory;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link EventRewriter} that creates from the incoming {@link ChangeSet} a copy only containing
 * {@link KnowledgeEvent events} that are relevant as described in a given
 * {@link KafkaExportConfiguration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TypeFilterRewriter implements EventRewriter {

	/** {@link ConfigurationItem} for the {@link TypeFilterRewriter}. */
	public interface Config extends ConfigurationItem {

		/** Property name of {@link #getLogSizeThreshold()}. */
		String LOG_SIZE_THRESHOLD = "log-size-threshold";

		/** Property name of {@link #getLogInterval()}. */
		String LOG_INTERVAL = "log-interval";

		/**
		 * {@link ChangeSet}s with more than that amount of {@link KnowledgeEvent}s are being
		 * logged.
		 * <p>
		 * The log message contains the revision number and the amount of events per type.
		 * </p>
		 * <p>
		 * The log level is INFO.
		 * </p>
		 */
		@IntDefault(1000)
		@Name(LOG_SIZE_THRESHOLD)
		int getLogSizeThreshold();

		/**
		 * The interval in which the progress should be logged.
		 * <p>
		 * If processing takes less time than this, nothing is logged.
		 * </p>
		 * <p>
		 * The log level is INFO, just like long running GUI updates.
		 * </p>
		 */
		@FormattedDefault("1min")
		@Format(MillisFormat.class)
		@Name(LOG_INTERVAL)
		long getLogInterval();

	}

	/**
	 * A special value for {@link KnowledgeEvent#setRevision(long)} for marking it for removal from
	 * its {@link ChangeSet}.
	 * <p>
	 * This value was chosen by random, to avoid hard to find bugs caused by clashes with potential
	 * other (future) special values for {@link ItemEvent#getRevision()}.
	 * </p>
	 * 
	 * @see #isMarkedForRemoval(KnowledgeEvent)
	 */
	private static final long REMOVE_CHANGE = -5657368386814439349L;

	/** Attribute to write {@link ExtReference} in serialized {@link ChangeSet}. */
	public static final String EXT_REFERENCE_ATTRIBUTE = "__ext_reference_attribute";

	/** Pseudo attribute to signal that a change has to be imported as it is without rewriting. */
	public static final String PLAIN_IMPORT_MARKER = "__plain_import";

	/**
	 * Deferred fetching of {@link ItemChange} values, when the values are not available at call
	 * time.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private interface GetValues extends Provider<Map<String, Object>> {

		// Just to have a new name

	}

	/**
	 * {@link GetValues} delivering {@link ItemChange#getValues()}
	 */
	static GetValues newValues(final ItemChange event) {
		class NewValues implements GetValues {

			@Override
			public Map<String, Object> get() {
				return event.getValues();
			}

		}
		return new NewValues();
	}

	/**
	 * {@link GetValues} delivering {@link ItemUpdate#getOldValues()}
	 */
	static GetValues oldValues(final ItemUpdate event) {
		class OldValues implements GetValues {

			@Override
			public Map<String, Object> get() {
				return event.getOldValues();
			}

		}
		return new OldValues();
	}

	/**
	 * Callback to use to access an {@link TLObject}. Instead of resolving direct (which is a
	 * database access), use {@link ResolveObject}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private interface ResolveObject {

		void handleObjectResolved(TLObject object);

	}

	private final KafkaExportConfiguration _exportConfig;

	final ExtIDFactory _idFactory = ExtIDFactory.getInstance();

	private final Map<ObjectKey, List<ResolveObject>> _callbacks = new HashMap<>();

	private final int _logSizeThreshold;

	private final long _logInterval;

	private long _startTime;

	/**
	 * Creates a new {@link TypeFilterRewriter} with given {@link KafkaExportConfiguration}.
	 */
	public TypeFilterRewriter(KafkaExportConfiguration exportConfig) {
		_exportConfig = exportConfig;
		Config config = getConfig();
		_logInterval = config.getLogInterval();
		_logSizeThreshold = config.getLogSizeThreshold();
	}

	/** The {@link Config} for this class. */
	protected static Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Creates a new {@link TypeFilterRewriter} with
	 * {@link KafkaExportImportConfiguration#getExportConfig()}.
	 */
	public TypeFilterRewriter() {
		this(KafkaExportImportConfiguration.getExportConfig());
	}

	@Override
	public synchronized void rewrite(ChangeSet cs, EventWriter out) {
		_startTime = System.currentTimeMillis();
		logLargeChangesets(cs);
		// TODO #22251: Handle Branched objects.
		ChangeSet output = null;
		try {
			output = handleDeletions(output, cs);
			output = handleCreations(output, cs);
			output = handleUpdates(output, cs);
			if (output == null) {
				return;
			}

			resolveCallbacks(output);
			removeMarkedEvents(output);
		} finally {
			_callbacks.clear();
		}

		output.setCommit(CopyKnowledgeEventVisitor.INSTANCE.copy(cs.getCommit()));
		out.write(output);
	}

	private void logLargeChangesets(ChangeSet changeSet) {
		int creations = changeSet.getCreations().size();
		int updates = changeSet.getUpdates().size();
		int deletions = changeSet.getDeletions().size();
		int branchEvents = changeSet.getBranchEvents().size();
		int size = creations + updates + deletions + branchEvents;
		if (size > getLogSizeThreshold()) {
			logInfo("Processing large changeset. Revision: " + changeSet.getRevision() + ". Creations: " + creations
				+ ". Updates: " + updates + ". Deletions: " + deletions + ". Branch events: " + branchEvents);
		}
	}

	private void resolveCallbacks(ChangeSet cs) {
		BulkIdLoad idLoader = new BulkIdLoad(_exportConfig.getKnowledgeBase());
		idLoader.addAll(_callbacks.keySet());
		List<KnowledgeItem> currentObjects = idLoader.loadUncachedInRevision(cs.getRevision());
		int all = _callbacks.size();
		int current = 0;
		for (KnowledgeItem item : currentObjects) {
			TLObject referencedObject = item.getWrapper();
			for (ResolveObject callback : _callbacks.remove(item.tId())) {
				callback.handleObjectResolved(referencedObject);
				current += 1;
				logProgress("Callbacks", all, current);
			}
		}
		if (!_callbacks.isEmpty()) {
			Logger.warn("No objects for keys '" + _callbacks.keySet() + "'  in ChangeSet " + cs + " found.",
				TypeFilterRewriter.class);
		}
	}

	private void removeMarkedEvents(ChangeSet output) {
		removeMarkedEvents(output.getDeletions());
		removeMarkedEvents(output.getCreations());
		removeMarkedEvents(output.getUpdates());
	}

	private <T extends KnowledgeEvent> void removeMarkedEvents(List<T> allEvents) {
		if (allEvents.isEmpty()) {
			return;
		}
		/* This is a performance optimization: Instead of deleting all elements individually, create
		 * a new list instead and replace the old list with it. This prevents one array copy
		 * operation per removal. */
		List<T> keptEvents = copyKeptEvents(allEvents);
		/* ChangeSet does not allow to replace the list. Therefore, clear it to replace the
		 * content. */
		allEvents.clear();
		if (keptEvents.isEmpty()) {
			return;
		}
		allEvents.addAll(keptEvents);
	}

	private <T extends KnowledgeEvent> List<T> copyKeptEvents(List<T> changes) {
		List<T> keptEvents = list();
		for (T event : changes) {
			if (!isMarkedForRemoval(event)) {
				keptEvents.add(event);
			}
		}
		return keptEvents;
	}

	private ChangeSet handleUpdates(ChangeSet output, ChangeSet cs) {
		int all = cs.getUpdates().size();
		int current = 0;
		for (ItemUpdate event : cs.getUpdates()) {
			output = handleUpdate(output, event);
			current += 1;
			logProgress("Updates", all, current);
		}
		return output;
	}

	private ChangeSet handleUpdate(ChangeSet output, ItemUpdate event) {
		/* Actually here it has to be checked whether the change is the update event of the local
		 * copy of an imported object, but the _externalId is not changed therefore it does not
		 * occur in the changes. the workaround is to resolve the changed object and check it, which
		 * is done in #removeUpdateEventIfImported(...) */
		if (_exportConfig.getAssociationTypes().contains(event.getObjectType())) {
			return handleAssociationUpdate(output, event);
		} else {
			return handleObjectUpdate(output, event);
		}
	}

	private ChangeSet handleAssociationUpdate(ChangeSet output, ItemUpdate event) {
		return handleAssociationEvent(output, event, Revision.CURRENT_REV);
	}

	private ChangeSet handleCreations(ChangeSet output, ChangeSet cs) {
		int all = cs.getCreations().size();
		int current = 0;
		for (ObjectCreation event : cs.getCreations()) {
			output = handleCreation(output, event);
			current += 1;
			logProgress("Creations", all, current);
		}
		return output;
	}

	private ChangeSet handleCreation(ChangeSet output, ObjectCreation event) {
		if (_idFactory.isImported(event)) {
			return output;
		}
		if (_exportConfig.getAssociationTypes().contains(event.getObjectType())) {
			return handleAssociationCreation(output, event);
		} else {
			return handleObjectCreation(output, event);
		}
	}

	private ChangeSet handleAssociationCreation(ChangeSet output, ObjectCreation event) {
		return handleAssociationEvent(output, event, Revision.CURRENT_REV);
	}

	private ChangeSet handleDeletions(ChangeSet output, ChangeSet cs) {
		int all = cs.getDeletions().size();
		int current = 0;
		for (ItemDeletion event : cs.getDeletions()) {
			output = handleDeletion(output, event);
			current += 1;
			logProgress("Deletions", all, current);
		}
		return output;
	}

	private ChangeSet handleDeletion(ChangeSet output, ItemDeletion event) {
		if (_idFactory.isImported(event)) {
			return output;
		}
		if (_exportConfig.getAssociationTypes().contains(event.getObjectType())) {
			return handleAssociationDeletion(output, event);
		} else {
			return handleObjectDeletion(output, event);
		}
	}

	private ChangeSet handleAssociationDeletion(ChangeSet output, ItemDeletion event) {
		return handleAssociationEvent(output, event, event.getRevision() - 1);
	}

	private ChangeSet handleObjectUpdate(ChangeSet output, ItemUpdate event) {
		ObjectBranchId changedItem = event.getObjectId();
		Map<String, Object> origOldValues = event.getOldValues();
		long updateRev = event.getRevision();
		ItemUpdate newUpdate = new ItemUpdate(updateRev, changedItem, origOldValues != null);
		Map<String, Object> exportValues =
			exportValues(changedItem, event.getValues(), newValues(newUpdate), Revision.CURRENT_REV, true);
		if (exportValues == null) {
			return output;
		}
		output = initOutput(output, updateRev);
		newUpdate.getValues().putAll(exportValues);
		if (origOldValues != null) {
			Map<String, Object> oldExportedValues =
				exportValues(changedItem, origOldValues, oldValues(newUpdate), updateRev - 1, true);
			if (oldExportedValues != null) {
				newUpdate.getOldValues().putAll(oldExportedValues);
			}
		}
		output.addUpdate(newUpdate);
		removeUpdateEventIfImported(newUpdate);
		return output;
	}

	/**
	 * Removes the given update event from the given change set when the item is imported.
	 * 
	 * <p>
	 * This method loads the object which is a workaround , because it can not be terminated from
	 * the update itself, whether it is imported.
	 * </p>
	 * 
	 * @see #handleUpdate(ChangeSet, ItemUpdate)
	 */
	private void removeUpdateEventIfImported(ItemUpdate update) {
		getCallbacks(update.getRevision(), update.getObjectId().toCurrentObjectKey())
			.add(object -> removeIfImported(update, object));
	}

	private void removeIfImported(ItemUpdate update, TLObject object) {
		if (_idFactory.isImportedObject(object)) {
			markForRemoval(update);
		}
	}

	private ChangeSet handleAssociationEvent(ChangeSet output, ItemChange event,
			long resolveRevision) {
		ObjectKey metaAttributeAttr =
			(ObjectKey) event.getValues().get(ApplicationObjectUtil.META_ATTRIBUTE_ATTR);
		if (!_exportConfig.getExportAttributeIds().contains(metaAttributeAttr)) {
			// fast return for associations for parts that are definitely not exported.
			return output;
		}
		if (_idFactory.isImported(event)) {
			return output;
		}
		output = initOutput(output, event.getRevision());
		ItemChange eventCopy = CopyKnowledgeEventVisitor.INSTANCE.copy(event);
		output.add(eventCopy);
		Object rawValue = eventCopy.getValues().get(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
		Object mappedValue = mapValue(metaAttributeAttr, rawValue);
		if (mappedValue != rawValue) {
			eventCopy.getValues().put(DBKnowledgeAssociation.REFERENCE_DEST_NAME, mappedValue);
		}
		if (mappedValue instanceof ObjectKey) {
			removeIfReferenceNotExported(DBKnowledgeAssociation.REFERENCE_DEST_NAME, eventCopy, resolveRevision);
		}
		removeIfReferenceNotExported(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, eventCopy, resolveRevision);
		getCallbacks(resolveRevision, metaAttributeAttr)
			.add(attribute -> setMetaAttribute(eventCopy, (TLStructuredTypePart) attribute));
		addExtReferenceForAssociation(eventCopy, resolveRevision);
		if (event instanceof ItemUpdate) {
			removeUpdateEventIfImported((ItemUpdate) eventCopy);
		}
		return output;
	}

	private void setMetaAttribute(ItemChange event, TLStructuredTypePart attribute) {
		event.getValues().put(ApplicationObjectUtil.META_ATTRIBUTE_ATTR, attribute.getName());
	}

	private void addExtReferenceForAssociation(ItemChange event, long resolveRevision) {
		getCallbacks(resolveRevision, event.getObjectId().toCurrentObjectKey())
			.add(association -> setExtReference(event, (KnowledgeAssociation) association));
	}

	private void setExtReference(ItemChange event, KnowledgeAssociation association) {
		TLStructuredType type = association.tType();
		String typeName;
		if (type == null) {
			// Associations have no configured type
			typeName = ApplicationObjectUtil.tableTypeQName(association.tTable());
		} else {
			typeName = TLModelUtil.qualifiedName(type);
		}
		addExtReference(event.getObjectId(), newValues(event), typeName);
	}

	void removeIfReferenceNotExported(String attribute, ItemChange event, long resolveRevision) {
		/** Remove event because references object is unknown by target system. */
		ObjectKey sourceKey = (ObjectKey) event.getValues().get(attribute);
		getCallbacks(resolveRevision, sourceKey)
			.add(referencedObject -> removeIfNotExported(attribute, event, referencedObject));
	}

	private void removeIfNotExported(String reference, ItemChange event, TLObject referencedObject) {
		ExtReference extReference = _idFactory.extReference(referencedObject);
		if (typeExported(referencedObject)) {
			event.getValues().put(reference, extReference);
			return;
		}
		if (_idFactory.isImportedObject(referencedObject, extReference)) {
			/* Type is not exported, but the referenced object itself may be an external object.
			 * Export it!. */
			event.getValues().put(reference, extReference);
			return;
		}
		markForRemoval(event);
	}

	private boolean typeExported(TLObject referencedObject) {
		ObjectKey typeKey = referencedObject.tType().tId();
		return exportAttributeNames(typeKey) != null;
	}

	/**
	 * Marks the {@link ItemChange} for being removed from its {@link ChangeSet}.
	 * <p>
	 * The removal happens after the {@link #getCallbacks(long, ObjectKey) callbacks} have been
	 * processed.
	 * </p>
	 * <p>
	 * This "two-phase removal" is a performance optimization: Removing it directly cannot happen
	 * with {@link List#remove(int)}, as the index is unknown and changes due to other deletions.
	 * And {@link List#remove(Object)} has a runtime complexity of O(n). That has lead to a
	 * complexity of O(n²) for removing all unwanted events from the {@link ChangeSet}. Creating a
	 * new list and copying only the wanted events there, is too complex to implement. Therefore,
	 * this two-phase approach is used: Mark them in the first phase, and then use
	 * {@link Iterator#remove()} in a second phase.
	 * </p>
	 * 
	 * @see #REMOVE_CHANGE
	 */
	protected static void markForRemoval(KnowledgeEvent event) {
		event.setRevision(REMOVE_CHANGE);
	}

	/** @see #markForRemoval(KnowledgeEvent) */
	protected boolean isMarkedForRemoval(KnowledgeEvent event) {
		return event.getRevision() == REMOVE_CHANGE;
	}

	private ChangeSet initOutput(ChangeSet output, long revision) {
		if (output != null) {
			return output;
		}
		return new ChangeSet(revision);
	}

	private Map<String, Object> exportValues(ObjectBranchId changedItem, Map<String, Object> origValues,
			GetValues newValues, long resolveRevision, boolean isUpdate) {
		ObjectKey objectType = (ObjectKey) origValues.remove(PersistentObject.TYPE_REF);
		if (objectType == null) {
			// Changed item is not a "real" TLObject.
			return null;
		}
		Map<String, String> exportNames = exportAttributeNames(objectType);
		if (exportNames == null) {
			return null;
		}
		Map<String, Object> result = getExportValues(objectType, exportNames, origValues, newValues, resolveRevision);
		if (result.isEmpty() && isUpdate) {
			// No change of unexported attributes. Skip complete event.
			return null;
		}
		addExtReferenceCallback(changedItem, newValues, resolveRevision, objectType);
		return result;
	}

	private void addExtReferenceCallback(ObjectBranchId changedItem, GetValues newValues,
			long resolveRevision, ObjectKey typeKey) {
		getCallbacks(resolveRevision, typeKey).add(type -> addExtReference(changedItem, newValues, type));
	}

	private void addExtReference(ObjectBranchId changedItem, GetValues newValues, TLObject type) {
		String typeName = TLModelUtil.qualifiedName((TLModelPart) type);
		addExtReference(changedItem, newValues, typeName);
	}

	private void addExtReference(ObjectBranchId changedItem, GetValues newValues, String typeName) {
		/* can not use extIDAttribute of static type, because it is unknown in the targets system
		 * which attribute is used by this system for the external ID attribute. */
		String extIDAttribute = EXT_REFERENCE_ATTRIBUTE;
		long branch = changedItem.getBranchId();
		// TODO #22251: Only own items are modified?
		ExtID id = _idFactory.extID(changedItem.getObjectName());
		// TODO #22251: Revision of object is ignored. Which is the meaning in the target system?
		newValues.get().put(extIDAttribute, new ExtReference(branch, typeName, id));
	}

	private Map<String, Object> getExportValues(ObjectKey objectType, Map<String, String> exportNames,
			Map<String, Object> values, GetValues newValues, long resolveRevision) {
		Map<String, Object> result = newMap(exportNames.size() + 1);
		for (Entry<String, String> exportName : exportNames.entrySet()) {
			String moAttrName = exportName.getKey();
			String tlAttrName = exportName.getValue();
			Object exportValue = values.get(moAttrName);
			if (exportValue == null && !values.containsKey(moAttrName)) {
				continue;
			}
			if (exportValue instanceof ObjectKey) {
				Object mappedValue = mapValue(objectType, tlAttrName, exportValue);
				if (!(mappedValue instanceof ObjectKey)) {
					result.put(tlAttrName, mappedValue);
					continue;
				}
				// Replace by reference later
				getCallbacks(resolveRevision, (ObjectKey) mappedValue).add(resolvedReference -> {
					ExtReference extReference = _idFactory.extReference(resolvedReference);
					boolean typeExported = exportAttributeNames(resolvedReference.tType().tId()) != null;
					if (typeExported) {
						newValues.get().put(tlAttrName, extReference);
					} else {
						if (isImportedObject(resolvedReference)) {
							/* Type is not exported, but the referenced object itself may be an
							 * external object. Export it! */
							newValues.get().put(tlAttrName, extReference);
						} else {
							// Not an exported object
							newValues.get().remove(tlAttrName);
						}
					}
				});
				/* Need to install here anything to ensure that there is a change. Otherwise the
				 * event may be suppressed. The value is set correct in callback. */
				result.put(tlAttrName, mappedValue);
			} else {
				Object mappedValue = mapValue(objectType, tlAttrName, exportValue);
				result.put(tlAttrName, mappedValue);
			}
		}
		return result;
	}

	private Object mapValue(ObjectKey attributeId, Object value) {
		String attributeName = _exportConfig.getAttributeName(attributeId);
		ObjectKey attributeOwnerId = _exportConfig.getAttributeOwnerId(attributeId);
		return mapValue(attributeOwnerId, attributeName, value);
	}

	/**
	 * @param value
	 *        Either the {@link ObjectKey} of the {@link TLObject} that should be mapped, or
	 *        directly the (non-{@link TLObject}) value that should be mapped. Can be null.
	 * @return Is allowed to be null.
	 */
	private Object mapValue(ObjectKey ownerTypeId, String tlAttribute, Object value) {
		Function<Object, ?> valueMapping = getValueMapping(ownerTypeId, tlAttribute);
		if (valueMapping == null) {
			return value;
		}
		return valueMapping.apply(value);
	}

	private Function<Object, ?> getValueMapping(ObjectKey ownerTypeId, String tlAttribute) {
		return _exportConfig.getValueMapping(ownerTypeId, tlAttribute);
	}

	private List<ResolveObject> getCallbacks(long resolveRevision, ObjectKey item) {
		// Ensure item is resolved in correct revision.
		item = KBUtils.ensureHistoryContext(item, resolveRevision);
		List<ResolveObject> callbacksForItem = _callbacks.get(item);
		if (callbacksForItem == null) {
			callbacksForItem = new ArrayList<>();
			_callbacks.put(item, callbacksForItem);
		}
		return callbacksForItem;
	}

	private boolean isImportedObject(TLObject tlObject) {
		return _idFactory.isImportedObject(tlObject);
	}

	private ChangeSet handleObjectDeletion(ChangeSet output, ItemDeletion deletion) {
		ObjectBranchId changedItem = deletion.getObjectId();
		ItemDeletion newDeletion = new ItemDeletion(deletion.getRevision(), changedItem);
		Map<String, Object> exportValues =
			exportValues(changedItem, deletion.getValues(), newValues(newDeletion), deletion.getRevision() - 1, false);
		if (exportValues == null) {
			return output;
		}
		output = initOutput(output, deletion.getRevision());
		newDeletion.getValues().putAll(exportValues);
		output.addDeletion(newDeletion);
		return output;
	}

	private ChangeSet handleObjectCreation(ChangeSet output, ObjectCreation creation) {
		ObjectBranchId changedItem = creation.getObjectId();
		ObjectCreation newCreation = new ObjectCreation(creation.getRevision(), changedItem);
		Map<String, Object> exportValues =
			exportValues(changedItem, creation.getValues(), newValues(newCreation), Revision.CURRENT_REV, false);
		if (exportValues == null) {
			return output;
		}
		output = initOutput(output, creation.getRevision());
		newCreation.getValues().putAll(exportValues);
		output.addCreation(newCreation);
		return output;
	}

	Map<String, String> exportAttributeNames(ObjectKey objectKey) {
		// Map is indexed with current keys.
		objectKey = KBUtils.ensureHistoryContext(objectKey, Revision.CURRENT_REV);
		return _exportConfig.getExportAttributeNames().get(objectKey);
	}

	/** Logs the progress if it takes too long. */
	protected void logProgress(String stage, int events, int current) {
		long now = System.currentTimeMillis();
		long timePassed = now - _startTime;
		if (timePassed > getLogInterval()) {
			_startTime = now;
			logInfo("Processing large changeset. Stage: " + stage + ". Progress: " + current + " / " + events);
		}
	}

	/** @see Config#getLogSizeThreshold() */
	public int getLogSizeThreshold() {
		return _logSizeThreshold;
	}

	/** @see Config#getLogInterval() */
	public long getLogInterval() {
		return _logInterval;
	}

	private static void logInfo(String message) {
		Logger.info(message, TypeFilterRewriter.class);
	}

}
