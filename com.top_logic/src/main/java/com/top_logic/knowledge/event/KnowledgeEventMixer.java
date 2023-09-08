/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.Comparator;
import java.util.PriorityQueue;


/**
 * Mixer that sorts {@link KnowledgeEvent}s from up to three source
 * {@link EventReader}s in an arbitrary order by interleaving events from all
 * source readers.
 * 
 * <p>
 * This implementation assumes that all source readers deliver events in the
 * order that this mixer should produce.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KnowledgeEventMixer extends AbstractEventReader<KnowledgeEvent> {

	private final PriorityQueue<KnowledgeEvent> queue;
	private final Comparator<? super KnowledgeEvent> order;
	private final EventReader<BranchEvent> branchReader;
	private final EventReader<? extends ItemEvent> itemReader;
	private final EventReader<CommitEvent> commitReader;

	/**
	 * Creates a {@link KnowledgeEventMixer}.
	 * 
	 * @param order
	 *        See {@link #getOrder()}.
	 * @param branchReader
	 *        {@link EventReader} of {@link BranchEvent}s. The reader must
	 *        produce events in the given order.
	 * @param itemReader
	 *        {@link EventReader} of {@link ItemEvent}s. The reader must produce
	 *        events in the given order.
	 * @param commitReader
	 *        {@link EventReader} of {@link CommitEvent}s. The reader must
	 *        produce events in the given order.
	 */
	public KnowledgeEventMixer(Comparator<? super KnowledgeEvent> order, EventReader<BranchEvent> branchReader, EventReader<? extends ItemEvent> itemReader, EventReader<CommitEvent> commitReader) {
		this.order = order;
		this.branchReader = branchReader;
		this.itemReader = itemReader;
		this.commitReader = commitReader;
		
		this.queue = new PriorityQueue<>(3, order);
		
		init();
	}

	/**
	 * The order in which this reader should produce events.
	 * 
	 * <p>
	 * Note: The source readers are required to produce events in the very same
	 * order.
	 * </p>
	 */
	public Comparator<? super KnowledgeEvent> getOrder() {
		return order;
	}
	
	private void init() {
		initReader(branchReader);
		initReader(itemReader);
		initReader(commitReader);
	}

	private void initReader(EventReader<? extends KnowledgeEvent> r) {
		if (r != null) {
			nextEvent(r);
		}
	}

	private void nextEvent(EventReader<? extends KnowledgeEvent> r) {
		KnowledgeEvent event = r.readEvent();
		if (event != null) {
			queue.add(event);
		}
	}

	@Override
	public KnowledgeEvent readEvent() {
		KnowledgeEvent result = queue.poll();
		if (result != null) {
			EventKind kind = result.getKind();
			switch (kind) {
			case branch:
				nextEvent(branchReader);
				break;
			case create:
			case update:
			case delete:
				nextEvent(itemReader);
				break;
			case commit:
				nextEvent(commitReader);
				break;
			}
		}
		return result;
	}

	@Override
	public void close() {
		cleanupStep0();
	}

	private void cleanupStep0() {
		try {
			if (branchReader != null) {
				branchReader.close();
			}
		} finally {
			cleanupStep1();
		}
	}

	private void cleanupStep1() {
		try {
			if (itemReader != null) {
				itemReader.close();
			}
		} finally {
			cleanupStep2();
		}
	}

	private void cleanupStep2() {
		if (commitReader != null) {
			commitReader.close();
		}
	}

}
