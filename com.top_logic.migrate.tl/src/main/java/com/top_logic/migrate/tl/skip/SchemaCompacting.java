/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.skip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.StackedEventWriter;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.migrate.tl.util.MigrateUtils;

/**
 * Rewriter which compacts all schema events to one event, i.e. if the first schema event occurs,
 * then the whole source is inspected and all found schema events are replayed in that revision.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SchemaCompacting implements EventRewriter {

	private static final class EventID {
		private long branch;

		private long revision;

		private Object name;

		private String type;

		private int hash;

		public EventID(long branch, long revision, Object name, String type) {
			super();
			this.branch = branch;
			this.revision = revision;
			assert name != null : "no null name";
			this.name = name;
			assert type != null : "no null type";
			this.type = type;
			this.hash = createHash();
		}

		@Override
		public int hashCode() {
			return hash;
		}

		private int createHash() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (this.branch ^ (this.branch >>> 32));
			result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
			result = prime * result + (int) (this.revision ^ (this.revision >>> 32));
			result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			EventID other = (EventID) obj;
			if (this.branch != other.branch)
				return false;
			if (!this.name.equals(other.name))
				return false;
			if (this.revision != other.revision)
				return false;
			if (!this.type.equals(other.type))
				return false;
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("EventID [branch=").append(this.branch).append(", revision=").append(this.revision)
				.append(", type=").append(this.type).append(", name=").append(this.name).append("]");
			return builder.toString();
		}
	}

	private static Set<String> SCHEMA_TYPE_NAMES = new HashSet<>();
	static {
		SCHEMA_TYPE_NAMES.add(KBBasedMetaElement.META_ELEMENT_KO);
		SCHEMA_TYPE_NAMES.add(KBBasedMetaAttribute.OBJECT_NAME);

		SCHEMA_TYPE_NAMES.add(FastList.OBJECT_NAME);
		SCHEMA_TYPE_NAMES.add(FastListElement.OBJECT_NAME);
	}

	private long schemaRevision = -1;

	private final KnowledgeBase srcKB;

	private final KnowledgeBase destKB;

	private final Protocol log;

	private HashSet<EventID> replayedEvents = new HashSet<>();

	private boolean compacted = false;

	public SchemaCompacting(KnowledgeBase srcKB, KnowledgeBase destKB, Protocol log) {
		super();
		this.srcKB = srcKB;
		this.destKB = destKB;
		this.log = log;
	}

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		compactSchemaEvents(cs, out);
		replayOriginalEvent(cs);
		out.write(cs);
	}

	private void compactSchemaEvents(ChangeSet cs, EventWriter out) {
		if (!compacted && isSchemaEvt(cs)) {
			replayAllSchemaEvents(out);
			compacted = true;
		}
	}

	private boolean isSchemaEvt(ChangeSet cs) {
		for (ItemEvent event : cs.getCreations()) {
			if (isSchemaEvt(event)) {
				return true;
			}
		}
		for (ItemEvent event : cs.getUpdates()) {
			if (isSchemaEvt(event)) {
				return true;
			}
		}
		for (ItemEvent event : cs.getDeletions()) {
			if (isSchemaEvt(event)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSchemaEvt(ItemEvent event) {
		String typeName = event.getObjectType().getName();
		boolean isSchemaEvent = SCHEMA_TYPE_NAMES.contains(typeName);
		if (isSchemaEvent) {
			schemaRevision = event.getRevision();
		}
		return isSchemaEvent;
	}

	private void replayOriginalEvent(ChangeSet cs) {
		replayOriginalEvent(cs.getCreations().iterator());
		replayOriginalEvent(cs.getUpdates().iterator());
		replayOriginalEvent(cs.getDeletions().iterator());
	}

	private void replayOriginalEvent(Iterator<? extends ItemEvent> iterator) {
		while (iterator.hasNext()) {
			ItemEvent event = iterator.next();
			EventID eventID = createReplayID(event);
			if (replayedEvents.contains(eventID)) {
				if (eventID.revision != schemaRevision) {
					log.info("Skip replay of '" + event + "' as it was replayed in revision '" + schemaRevision + "'",
						Protocol.DEBUG);
				}
				iterator.remove();
			}
		}
	}

	private EventID createReplayID(ItemEvent event) {
		ObjectBranchId objectId = event.getObjectId();
		long branchId = objectId.getBranchId();
		Object objectName = objectId.getObjectName();
		String typeName = objectId.getObjectType().getName();
		long revision = event.getRevision();
		return new EventID(branchId, revision, objectName, typeName);
	}

	private void replayAllSchemaEvents(EventWriter out) {
		try (ChangeSetReader schemaEvents = getSchemaEvents()) {
			addNecessaryRewriters(out);
			ChangeSet cs;
			while ((cs = schemaEvents.read()) != null) {
				if (cs.getRevision() != schemaRevision) {
					cs.setRevision(schemaRevision);
					log.info("Replay '" + cs + "' in revision '" + schemaRevision + "'", Protocol.DEBUG);
				}
				for (ObjectCreation creation : cs.getCreations()) {
					adapt(creation);
				}
				for (ItemDeletion deletion : cs.getDeletions()) {
					adapt(deletion);
				}
				for (ItemUpdate update : cs.getUpdates()) {
					adapt(update);
				}
			}
		}
	}

	private void adapt(ItemEvent event) {
		EventID eventID = createReplayID(event);
		// fake revision as all occurs in schema revision.
		event.setRevision(schemaRevision);
		replayedEvents.add(eventID);
	}

	private EventWriter addNecessaryRewriters(EventWriter out) {
		List<EventRewriter> rewriters = new ArrayList<>();
		MigrateUtils.addNecessaryRewriters(destKB, null, rewriters);
		return StackedEventWriter.createWriter(0, out, rewriters);
	}

	private ChangeSetReader getSchemaEvents() {
		HistoryManager historyManager = srcKB.getHistoryManager();
		Revision startRev = historyManager.getRevision(schemaRevision);
		Revision stopRev = Revision.CURRENT;
		Set<Branch> branches = null;
		Comparator<? super ItemEvent> order = null;
		ReaderConfig config = ReaderConfigBuilder.createComplexConfig(startRev, stopRev, SCHEMA_TYPE_NAMES, branches,
			false, false, order);
		return srcKB.getChangeSetReader(config);
	}

}
