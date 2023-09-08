/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service.importer;

import static com.top_logic.kafka.knowledge.service.TLSyncUtils.*;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.logging.LogUtil;
import com.top_logic.kafka.knowledge.service.TLSyncRecord;
import com.top_logic.kafka.knowledge.service.TLSyncUtils;
import com.top_logic.kafka.knowledge.service.exporter.KBDataProducerTask;
import com.top_logic.kafka.log.KafkaLogUtil;
import com.top_logic.kafka.services.consumer.ConsumerProcessor;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.StackedEventWriter;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseName;

/**
 * {@link ConsumerProcessor} that accepts {@link ChangeSet} and writes them to the
 * {@link KnowledgeBase}.
 * 
 * @see KBDataProducerTask
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KBDataProcessor implements ConsumerProcessor<String, TLSyncRecord<ChangeSet>> {

	/**
	 * Configuration of the {@link KBDataProcessor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<KBDataProcessor>, KnowledgeBaseName {
		
		/**
		 * List of {@link EventRewriter} that rewrites the received {@link ChangeSet}s.
		 */
		List<PolymorphicConfiguration<EventRewriter>> getRewriters();

	}
	
	private final KnowledgeBase _kb;
	
	private final List<? extends EventRewriter> _rewriters;

	private final DBProperties _dbProperties;

	/**
	 * Creates a new {@link KBDataProcessor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link KBDataProcessor}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public KBDataProcessor(InstantiationContext context, Config config) throws ConfigurationException {
		_kb = KnowledgeBaseFactory.getInstance().getKnowledgeBase(config.getKnowledgeBase());
		_dbProperties = new DBProperties(KBUtils.getConnectionPool(_kb));
		_rewriters = TypedConfiguration.getInstanceListReadOnly(context, config.getRewriters());
	}

	@Override
	public void process(ConsumerRecords<String, TLSyncRecord<ChangeSet>> records) {
		LogUtil.withLogMark(LOG_MARK_TL_SYNC, "true", () -> processWithLogMark(records));
	}

	/**
	 * Called by {@link #process(ConsumerRecords)} with a mark for Log4j that everything within is
	 * TL-Sync related.
	 */
	protected void processWithLogMark(ConsumerRecords<String, TLSyncRecord<ChangeSet>> records) {
		if (records.isEmpty()) {
			return;
		}
		logInfo("Begin: Processing " + records.count() + " changesets.");
		try (
				KBDataWriter writer = new KBDataWriter(_kb);
				EventWriter out = StackedEventWriter.createWriter(0, writer, _rewriters)) {
			for (ConsumerRecord<String, TLSyncRecord<ChangeSet>> record : records) {
				String id = KafkaLogUtil.toStringTLMessageId(record.headers());
				long revision = record.value().getRecord().getRevision();
				long systemId = record.value().getSystemId();
				/* Don't log all the details here. They have already been logged by the ConsumerDispatcher. */
				String message = " Processing record" + id + " with changeset " + revision + " from system " + systemId + ".";
				LogUtil.withBeginEndLogging(KBDataProcessor.class, message, () -> process(writer, out, record));
			}
			logInfo("End: Processing.");
		} catch (Throwable exception) {
			logError("End with exception: Processing.", exception);
			throw exception;
		}
	}

	/** Processes a single {@link ChangeSet}. */
	protected void process(KBDataWriter writer, EventWriter out,
			ConsumerRecord<String, TLSyncRecord<ChangeSet>> record) {
		ChangeSet cs = record.value().getRecord();
		long systemId = record.value().getSystemId();
		long lastSentRevision = record.value().getLastMessageRevision();
		boolean apply = checkMessageOrder(systemId, lastSentRevision, cs);
		if (apply) {
			writer.setRevisionKey(TLSyncUtils.getProcessedRevisionKey(systemId));
			out.write(cs);
		}
	}

	private boolean checkMessageOrder(long systemId, long lastSentRevision, ChangeSet changeSet) {
		Long lastProcessedRevision = lastProcessedRevision(systemId);
		long newRevision = changeSet.getRevision();
		if (lastProcessedRevision == null) {
			return checkFirstMessage(systemId, lastSentRevision, newRevision);

		}
		if (lastProcessedRevision >= newRevision) {
			logInfo("Skipping old changeset " + newRevision + ". Last processed changeset: " + lastProcessedRevision);
			return false;
		}
		checkLastSentVsProcessedRevision(systemId, lastSentRevision, lastProcessedRevision, newRevision);
		return true;
	}

	private boolean checkFirstMessage(long systemId, long lastSentRevision, long newRevision) {
		if (lastSentRevision == TLSyncRecord.LAST_MESSAGE_REVISION_NONE_SEND) {
			/* This is the first changeset which the sender has sent and the first changeset the
			 * receiver has received. That fits together. */
			return true;
		}
		if (lastSentRevision == TLSyncRecord.LAST_MESSAGE_REVISION_NONE_RECEIVED) {
			/* This is a version 1.0.0 message. The lastSentRevision is unknown. There is
			 * nothing that can be checked here without that data. */
			return true;
		}
		throw failStartOfCommunicationIsMissing(systemId, lastSentRevision, newRevision);
	}

	private RuntimeException failStartOfCommunicationIsMissing(long systemId, long lastSentRevision, long newRevision) {
		throw new RuntimeException("Detected that messages are missing, when receiving changeset " + newRevision
			+ " from system " + systemId + ". This is the first received message."
			+ " But the message states that the last processed changeset should have been " + lastSentRevision + ".");
	}

	private void checkLastSentVsProcessedRevision(long systemId, long lastSentRevision, long lastProcessedRevision,
			long newRevision) {
		if (lastSentRevision == TLSyncRecord.LAST_MESSAGE_REVISION_NONE_RECEIVED) {
			/* This is a version 1.0.0 message. The lastSentRevision is unknown. There is nothing
			 * that can be checked here without that data. */
			return;
		}
		/* Correct order: lastSentRevision == lastProcessedRevision < newRevision */
		if (lastSentRevision >= newRevision) {
			failLastSentRevisionTooHigh(systemId, lastSentRevision, newRevision);
		}
		if (lastSentRevision > lastProcessedRevision) {
			failMissingMessages(systemId, lastSentRevision, lastProcessedRevision, newRevision);
		}
		if (lastSentRevision < lastProcessedRevision) {
			failLastSentRevisionTooLow(systemId, lastSentRevision, lastProcessedRevision, newRevision);
		}
	}

	private void failLastSentRevisionTooHigh(long systemId, long lastSentRevision, long newRevision) {
		throw new RuntimeException("Received inconsistent message from system " + systemId + ". It contains changeset "
			+ newRevision + " but states that the last sent message before that was " + lastSentRevision + ".");
	}

	private void failMissingMessages(long systemId, long lastSentRevision, long lastProcessedRevision,
			long newRevision) {
		throw new RuntimeException("Detected that messages are missing, when receiving changeset " + newRevision
			+ " from system " + systemId + ". The last processed changeset was " + lastProcessedRevision
			+ ". But the new message states that the last processed changeset should have been "
			+ lastSentRevision + ".");
	}

	private void failLastSentRevisionTooLow(long systemId, long lastSentRevision, Long lastProcessedRevision,
			long newRevision) {
		throw new RuntimeException("Detected an inconsistency when receiving changeset " + newRevision
			+ " from system " + systemId + ". The last processed changeset was " + lastProcessedRevision
			+ ". But the new message states that the last processed changeset should have been " + lastSentRevision
			+ ".");
	}

	/** @return null, if nothing is stored in the database. */
	private Long lastProcessedRevision(long systemId) {
		String dbValue = _dbProperties.getProperty(TLSyncUtils.getProcessedRevisionKey(systemId));
		if (StringServices.isEmpty(dbValue)) {
			return null;
		}
		return Long.parseLong(dbValue);
	}

	private void logError(String message, Throwable exception) {
		Logger.error(message + " Cause: " + exception.getMessage(), exception, KBDataProcessor.class);
	}

	private void logInfo(String message) {
		Logger.info(message, KBDataProcessor.class);
	}

}

