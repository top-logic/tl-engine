/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.services;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.kafka.knowledge.service.TLSyncRecord;
import com.top_logic.kafka.knowledge.service.importer.KBDataProcessor;
import com.top_logic.kafka.knowledge.service.importer.KBDataWriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;

/**
 * {@link KBDataProcessor} for {@link AbstractTLSyncTest}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestingKBDataProcessor extends KBDataProcessor {

	/** Set of the revision numbers of the processed {@link ChangeSet}s. */
	private final Set<Long> _processedRevisions = new HashSet<>();

	/**
	 * Creates a new {@link TestingKBDataProcessor}.
	 */
	public TestingKBDataProcessor(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected void process(KBDataWriter writer, EventWriter out,
			ConsumerRecord<String, TLSyncRecord<ChangeSet>> record) {
		super.process(writer, out, record);
		registerProcessedRevision(record.value().getRecord().getRevision());
	}

	private void registerProcessedRevision(long revision) {
		synchronized (_processedRevisions) {
			_processedRevisions.add(revision);
			_processedRevisions.notifyAll();
		}
	}

	/**
	 * Waits (at most timeout) until all given revisions have been processed.
	 * 
	 * @param revisions
	 *        Set of revisions which had to be processed before method is leaved.
	 * @param timeout
	 *        Maximal time to wait.
	 * @return Commit numbers of the revisions that have not yet been processed.
	 * @throws InterruptedException
	 *         When waiting is interrupted.
	 */
	public Collection<Long> waitUntilAllProcessed(Iterable<Long> revisions, Duration timeout)
			throws InterruptedException {
		long stopTime = now() + timeout.toMillis();
		Iterator<Long> iter = revisions.iterator();
		synchronized (_processedRevisions) {
			while (iter.hasNext()) {
				Long revision = iter.next();
				while (!_processedRevisions.contains(revision)) {
					long now = now();
					if (now >= stopTime) {
						Set<Long> unprocessed = new HashSet<>();
						unprocessed.add(revision);
						while (iter.hasNext()) {
							Long potentiallyUnprocessed = iter.next();
							if (!_processedRevisions.contains(potentiallyUnprocessed)) {
								unprocessed.add(potentiallyUnprocessed);
							}
						}
						return unprocessed;
					}
					_processedRevisions.wait(stopTime - now);
				}
			}
			return Collections.emptySet();
		}

	}

	private long now() {
		return System.currentTimeMillis();
	}

}

