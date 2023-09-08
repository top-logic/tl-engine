/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;

/**
 * Copies data from one {@link KnowledgeBase} to another through event
 * extraction and re-play.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KnowledgeBaseCopy implements Runnable {

	protected final KnowledgeBase sourceKb;
	protected final KnowledgeBase destKb;

	/**
	 * Creates a {@link KnowledgeBaseCopy}.
	 * 
	 * @param sourceKb
	 *        The {@link KnowledgeBase} to read data from.
	 * @param destKb
	 *        The {@link KnowledgeBase} to write data to.
	 */
	public KnowledgeBaseCopy(KnowledgeBase sourceKb, KnowledgeBase destKb) {
		this.sourceKb = sourceKb;
		this.destKb = destKb;
	}
	
	@Override
	public void run() {
		convert();
	}

	/**
	 * Perform the copy/conversion of all revisions in the source
	 * {@link KnowledgeBase}.
	 */
	public final void convert() {
		Revision firstRevision = sourceKb.getHistoryManager().getRevision(Revision.FIRST_REV);
		convert(firstRevision, Revision.CURRENT);
	}

	/**
	 * Perform the copy/conversion of the given revision range in the source
	 * {@link KnowledgeBase}.
	 * 
	 * @param startRevision
	 *        The first revision to copy.
	 * @param stopRevision
	 *        The last (exclusive) revision to copy.
	 */
	public final void convert(Revision startRevision, Revision stopRevision) {
		final ReaderConfig readerConfig = ReaderConfigBuilder.createConfig(startRevision, stopRevision);
		convert(readerConfig);
	}

	/**
	 * Perform the copy/conversion based on the given configuration.
	 */
	public final void convert(ReaderConfig readerConfig) {
		try (ChangeSetReader reader = sourceKb.getChangeSetReader(readerConfig)) {
			try (EventWriter eventWriter = destKb.getReplayWriter()) {
				internalConvert(reader, eventWriter);
			}
		}
	}

	/**
	 * Reads {@link ChangeSet} from the given reader and writes them to an adapted version of the
	 * given writer.
	 * 
	 * @param reader
	 *        the reader to read from
	 * @param eventWriter
	 *        the writer to write (finally) to.
	 * 
	 * @see #adoptWriter(EventWriter)
	 */
	protected void internalConvert(ChangeSetReader reader, EventWriter eventWriter) {
		ChangeSet cs;
		EventWriter writer = adoptWriter(eventWriter);
		while ((cs = reader.read()) != null) {
			writer.write(cs);
		}
	}

	/**
	 * Returns the writer to which events are written to.
	 * 
	 * @param eventWriter
	 *        the writer which must finally receive the events
	 */
	protected EventWriter adoptWriter(EventWriter eventWriter) {
		return KnowledgeEventConverter.createEventConverter(destKb.getMORepository(), eventWriter);
	}
	
}
