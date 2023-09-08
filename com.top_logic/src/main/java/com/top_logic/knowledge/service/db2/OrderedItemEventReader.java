/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import com.top_logic.knowledge.event.AbstractEventReader;
import com.top_logic.knowledge.event.EventReader;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * {@link EventReader} that reads chunks of {@link ItemEvent}s using a
 * {@link ItemEventReader} and sorts them by revision order before returning
 * them.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OrderedItemEventReader extends AbstractEventReader<ItemEvent> {

	private final DBKnowledgeBase kb;
	private final boolean keepOldValues;
	private final long stopRev;
	private final long chunkSize;
	private final Set<String> typeNameFilter;
	private final Set<Long> branchFilter;
	private final Comparator<? super ItemEvent> order;
	
	private long chunkStartRev;
	private long chunkStopRev;
	private boolean lastChunk;
	private ArrayList<ItemEvent> buffer;
	private int pos;

	/**
	 * Creates a {@link OrderedItemEventReader}.
	 * 
	 * @param kb
	 *        {@link KnowledgeBase} to read events from.
	 * @param keepOldValues
	 *        Whether {@link ItemUpdate}s should keep old values. See
	 *        {@link ItemUpdate#ItemUpdate(long, ObjectBranchId, boolean)}
	 * @param startRev
	 *        The first revision (inclusive) to read.
	 * @param stopRev
	 *        The last revision (exclusive) to read. must be less than {@link Long#MAX_VALUE}.
	 * @param chunkSize
	 *        The chunk size. <code>(stopRev - startRev) / chunkSize</code> requests to the database
	 *        are issued.
	 * @param typeNameFilter
	 *        See
	 *        {@link ItemEventReader#ItemEventReader(DBKnowledgeBase, boolean, long, long, Set, Set)}
	 * @param branchFilter
	 *        See
	 *        {@link ItemEventReader#ItemEventReader(DBKnowledgeBase, boolean, long, long, Set, Set)}
	 */
	public OrderedItemEventReader(DBKnowledgeBase kb, boolean keepOldValues, long startRev, long stopRev,
			long chunkSize, Set<String> typeNameFilter, Set<Long> branchFilter, Comparator<? super ItemEvent> order)
			throws SQLException {
		this.kb = kb;
		this.keepOldValues = keepOldValues;
		this.chunkStartRev = startRev;
		this.chunkStopRev = startRev;
		this.order = order;
		
		this.stopRev = stopRev;
		
		this.chunkSize = chunkSize;
		this.typeNameFilter = typeNameFilter;
		this.branchFilter = branchFilter;
		
		this.buffer = new ArrayList<>();
		
		boolean success = false;
		try {
			init();
			success = true;
		} finally {
			if (! success) {
				// Free potentially allocated resources, caller has no chance to
				// close reader, because the object construction fails.
				cleanup();
			}
		}
	}

	private void init() throws SQLException {
		nextChunk();
	}
	
	private void cleanup() {
		// No allocated resources.
	}

	private void nextChunk() throws SQLException {
		if (this.lastChunk) {
			// Add new guardian element to the queue.
			this.pos = buffer.size() - 1;
			return;
		}
		
		this.buffer.clear();
		this.pos = 0;
		
		while (true) {
			this.chunkStartRev = this.chunkStopRev;
			this.chunkStopRev = Math.min(stopRev, this.chunkStartRev + chunkSize);
			
			ItemEventReader chunkReader = new ItemEventReader(kb, keepOldValues, chunkStartRev, chunkStopRev, typeNameFilter, branchFilter);
			try {
				ItemEvent event;
				while ((event = chunkReader.readEvent()) != null) {
					buffer.add(event);
				}
			} finally {
				chunkReader.close();
			}
			Collections.sort(buffer, order);
			
			this.lastChunk = this.chunkStopRev >= this.stopRev;
			if (lastChunk) {
				// Add guardian element to the buffer.
				buffer.add(null);
				return;
			} 
			else if (buffer.size() > 0) {
				return;
			}
			else {
				// No events found in this chunk, continue with next chunk.
			}
		}
	}

	@Override
	public ItemEvent readEvent() {
		try {
			ItemEvent result = buffer.get(pos++);
			if (pos >= buffer.size()) {
				nextChunk();
			}
			return result;
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	@Override
	public void close() {
		cleanup();
	}

}
