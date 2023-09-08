/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.listener;

import static com.top_logic.knowledge.objects.KOAttributes.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.top_logic.base.bus.DocumentEvent;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.InlineList;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.objects.ChangeInspectable;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.knowledge.service.event.ModificationListener;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.util.Utils;

/**
 * Create appropriate DocumentEvents
 * in case the physical resource of a knowledge object changes.
 * 
 * Do not use this class as it possibly result in deadlocks
 * when used multithreaded.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
@ServiceDependencies({ PersistencyLayer.Module.class, ThreadContextManager.Module.class })
public final class ContentChangeEventListener extends ManagedClass implements ModificationListener {

    /** Key in Configuration defining the types of KOs to consider */
    public static final String TYPES_ENTRY = "types";
    
	private static final Property<Object> DOCUMENTS_DELETED_EVENTS_KEY = TypedAnnotatable.property(Object.class,
		"documentDeletedEvents", InlineList.newInlineList());

    /** The handled types of KOs. */
    private Collection<String> types;
    
    /** Sender used to brodcast the Events to the bus. */
	Sender sender = new Sender(Bus.CHANGES, Bus.DOCUMENT);

	/**
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<ContentChangeEventListener> {
		/**
		 * Types.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getTypes();
	}

	/**
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        Configuration for {@link PersistencyLayer}.
	 */
	public ContentChangeEventListener(InstantiationContext context, Config config) {
		super(context, config);

		types = new HashSet<>(config.getTypes());
	}

	@Override
	protected void startUp() {
		super.startUp();
		PersistencyLayer.getKnowledgeBase().addModificationListener(this);

	}

	@Override
	protected void shutDown() {
		PersistencyLayer.getKnowledgeBase().removeModificationListener(this);
		super.shutDown();
	}

	@Override
	public Modification createModification(KnowledgeBase kb,
			Map<ObjectKey, ? extends KnowledgeItem> createdObjects,
			Map<ObjectKey, ? extends KnowledgeItem> updatedObjects,
			Map<ObjectKey, ? extends KnowledgeItem> removedObjects) {

		Object inlineEventList = getDeletions();
		inlineEventList = handleCreations(inlineEventList, createdObjects);
		inlineEventList = handleUpdates(inlineEventList, updatedObjects);
		if (InlineList.isEmpty(inlineEventList)) {
			return Modification.NONE;
		}
		final List<DocumentEvent> documentEvents = InlineList.toList(DocumentEvent.class, inlineEventList);
		return new Modification() {

			@Override
			public void execute() throws DataObjectException {
				Sender s = ContentChangeEventListener.this.sender;
				for (DocumentEvent documentEvent : documentEvents) {
					s.send(documentEvent);
				}
			}
		};
	}

	@Override
	public Modification notifyUpcomingDeletion(KnowledgeBase kb, KnowledgeItem item) {
		if (!types.contains(item.tTable().getName())) {
			return Modification.NONE;
		}
		if (getPhysicalResource(item) == null) {
			return Modification.NONE;
		}

		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction == null) {
			assert interaction != null : "An interaction is present when operating with KnowledgeBase.";
			Logger.error("Unable to handle deletion without interaction.", ContentChangeEventListener.class);
			return Modification.NONE;
		}
		Object inlineDeleteEventList = interaction.get(DOCUMENTS_DELETED_EVENTS_KEY);
		try {
			item = HistoryUtils.getKnowledgeItem(HistoryUtils.getSessionRevision(), item);
		} catch (DataObjectException ex) {
			Logger.error("Unable to get stable version of item to delete.", ex, ContentChangeEventListener.class);
			return Modification.NONE;
		}
		inlineDeleteEventList = addDocumentEvent(inlineDeleteEventList, item, DocumentEvent.DELETED);
		interaction.set(DOCUMENTS_DELETED_EVENTS_KEY, inlineDeleteEventList);
		return Modification.NONE;
	}
    
	private Object getDeletions() {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction == null) {
			assert interaction != null : "An interaction is present when operating with KnowledgeBase.";
			// is already logged during processing upcoming deletions.
			return InlineList.newInlineList();
		}
		return interaction.reset(DOCUMENTS_DELETED_EVENTS_KEY);
	}

	private Object handleUpdates(Object inlineEventList, Map<ObjectKey, ? extends KnowledgeItem> updatedObjects) {
		for (KnowledgeItem updatedObject : updatedObjects.values()) {
			if (!types.contains(updatedObject.tTable().getName())) {
				continue;
			}
			ItemUpdate change = (ItemUpdate) ((ChangeInspectable) updatedObject).getChange();
			Object oldPhysRes = change.getOldValues().get(PHYSICAL_RESOURCE);
			Object newPhysRes = change.getValues().get(PHYSICAL_RESOURCE);

			if (Utils.equals(oldPhysRes, newPhysRes)) {
				// no relevant change
				continue;
			}

			String eventKind = eventKind(oldPhysRes, newPhysRes);
			inlineEventList = addDocumentEvent(inlineEventList, updatedObject, eventKind);
		}
		return inlineEventList;
	}

	private Object handleCreations(Object inlineEventList, Map<ObjectKey, ? extends KnowledgeItem> createdObjects) {
		for (KnowledgeItem createdObject : createdObjects.values()) {
			if (!types.contains(createdObject.tTable().getName())) {
				continue;
			}
			inlineEventList = handleCreation(inlineEventList, createdObject);
		}
		return inlineEventList;

	}

	private Object handleCreation(Object inlineEventList, KnowledgeItem createdItem) {
		// Will never happen as Attribues cannot be set during create.
		if (getPhysicalResource(createdItem) != null) {
			// physical resource set
			inlineEventList = addDocumentEvent(inlineEventList, createdItem, DocumentEvent.CREATED);
		}
		return inlineEventList;
	}

	private String eventKind(Object oldPhysRes, Object newPhysRes) {
		String eventKind;
		if (oldPhysRes == null) {
			if (newPhysRes != null) {
				eventKind = DocumentEvent.CREATED;
			} else {
				throw new IllegalArgumentException("Old value equals new value: " + oldPhysRes);
			}
		} else {
			if (newPhysRes == null) {
				eventKind = DocumentEvent.DELETED;
			} else {
				if (oldPhysRes.equals(newPhysRes)) {
					throw new IllegalArgumentException("Old value equals new value: " + oldPhysRes);
				} else {
					eventKind = DocumentEvent.MODIFIED;
				}
			}
		}
		return eventKind;
	}
	
	private Object addDocumentEvent(Object inlineEventList, KnowledgeItem item, String type) {
		DocumentEvent documentEvent = newDocumentEvent(item, type);
		if (documentEvent != null) {
			inlineEventList = InlineList.add(DocumentEvent.class, inlineEventList, documentEvent);
		}
		return inlineEventList;
	}

	private DocumentEvent newDocumentEvent(KnowledgeItem documentKI, String eventKind) {
		try {
			Wrapper document = WrapperFactory.getWrapper((KnowledgeObject) documentKI);
			return DocumentEvent.getDocumentEvent(this.sender, document, eventKind);
		} catch (Exception ex) {
			// creation of DocumentEvent failed
			Logger.warn("Exception while trying to send a DocumentEvent to the Bus.", ex,
				ContentChangeEventListener.class);
			return null;
		}
	}

	private Object getPhysicalResource(KnowledgeItem item) {
		try {
			return item.getAttributeValue(PHYSICAL_RESOURCE);
		} catch (NoSuchAttributeException ex) {
			// will not happen, entry type must have this attribute
			Logger.error("Entry has not attribute '" + PHYSICAL_RESOURCE + "'.", ex, ContentChangeEventListener.class);
			return null;
		}
	}

	/**
	 * Module for {@link ContentChangeEventListener}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<ContentChangeEventListener> {

		/**
		 * Module instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ContentChangeEventListener> getImplementation() {
			return ContentChangeEventListener.class;
		}

	}

}
