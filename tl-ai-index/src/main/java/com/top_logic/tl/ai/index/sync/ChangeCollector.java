/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLClass;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.tl.ai.index.config.IndexedTypes;
import com.top_logic.util.model.ModelService;

/**
 * Which objects a range of the history changed, read from the database rather than from events in
 * memory.
 *
 * <p>
 * The engine's {@code ChangeSetReader} reads the rows of the tables the indexed types are stored
 * in, and of the tables that hold their references (link tables). A row of an object's own table
 * names the object; a row of a link table names its owner, the way the engine's
 * {@code ChangedValueOwners} maps it - for an update both the new and the old owner, because the row
 * may have moved.
 * </p>
 *
 * <p>
 * Because the history is the source, a crash, a restart and a long downtime are read the same way
 * as the last commit: by the range since the watermark.
 * </p>
 */
public final class ChangeCollector {

	private final KnowledgeBase _kb;

	private final Set<String> _ownTables;

	private final Map<String, List<AssociationStorageDescriptor>> _linkTables;

	/**
	 * Creates a {@link ChangeCollector} for the tables of the given types.
	 */
	public ChangeCollector(KnowledgeBase kb, IndexedTypes types) {
		_kb = kb;
		_ownTables = new HashSet<>();
		_linkTables = new HashMap<>();
		ModelTables tables = new ModelTables(ModelService.getApplicationModel());
		for (TLClass type : types.concreteClasses()) {
			_ownTables.add(TLAnnotations.getTable(type));
			for (Map.Entry<MOStructure, List<AssociationStorageDescriptor>> separate : tables.lookupSeparateStorage(type)
				.entrySet()) {
				List<AssociationStorageDescriptor> descriptors =
					_linkTables.computeIfAbsent(separate.getKey().getName(), x -> new ArrayList<>());
				for (AssociationStorageDescriptor descriptor : separate.getValue()) {
					if (!descriptors.contains(descriptor)) {
						descriptors.add(descriptor);
					}
				}
			}
		}
	}

	/**
	 * The names of the tables this collector reads.
	 */
	private Set<String> tables() {
		Set<String> result = new HashSet<>(_ownTables);
		result.addAll(_linkTables.keySet());
		return result;
	}

	/**
	 * The current keys of the objects changed in the revisions {@code (after, upTo]}, each once, in
	 * the order of their first change.
	 *
	 * <p>
	 * An object of an own table is reported whatever its type: the caller resolves it and leaves out
	 * what is not indexed. A deleted object is reported too, so the caller removes it.
	 * </p>
	 *
	 * @param after
	 *        The last revision already applied.
	 * @param upTo
	 *        The last revision to read.
	 */
	public Set<ObjectKey> changed(long after, long upTo) {
		Set<ObjectKey> result = new LinkedHashSet<>();
		if (upTo <= after || _ownTables.isEmpty()) {
			return result;
		}
		HistoryManager history = _kb.getHistoryManager();
		Revision start = history.getRevision(after + 1);
		Revision stop = history.getRevision(upTo);
		ReaderConfigBuilder config = ReaderConfigBuilder.createComplexConfig(start, stop, tables(),
			Set.of(history.getTrunk()), false, false, null);
		try (ChangeSetReader reader = _kb.getChangeSetReader(config)) {
			for (ChangeSet changes = reader.read(); changes != null; changes = reader.read()) {
				add(result, changes.getCreations());
				add(result, changes.getUpdates());
				add(result, changes.getDeletions());
			}
		}
		return result;
	}

	private void add(Set<ObjectKey> result, Collection<? extends ItemEvent> events) {
		for (ItemEvent event : events) {
			String table = event.getObjectType().getName();
			if (_ownTables.contains(table)) {
				result.add(current(event.getOriginalObject()));
			}
			List<AssociationStorageDescriptor> descriptors = _linkTables.get(table);
			if (descriptors == null || !(event instanceof ItemChange change)) {
				continue;
			}
			for (AssociationStorageDescriptor descriptor : descriptors) {
				addOwner(result, descriptor, change.getValues());
				if (change instanceof ItemUpdate update && update.getOldValues() != null) {
					addOwner(result, descriptor, update.getOldValues());
				}
			}
		}
	}

	private static void addOwner(Set<ObjectKey> result, AssociationStorageDescriptor descriptor,
			Map<String, Object> values) {
		ObjectKey owner = descriptor.getBaseObjectId(values);
		if (owner != null) {
			result.add(current(owner));
		}
	}

	private static ObjectKey current(ObjectKey key) {
		return KBUtils.ensureHistoryContext(key, Revision.CURRENT_REV);
	}

}
