/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.knowledge.service.exporter;

import static com.top_logic.kafka.sync.knowledge.service.TLSyncUtils.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.logging.LogUtil;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.ExponentialBackoff;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.kafka.services.producer.KafkaProducerService;
import com.top_logic.kafka.services.producer.TLKafkaProducer;
import com.top_logic.kafka.sync.knowledge.service.TLSyncRecord;
import com.top_logic.kafka.sync.knowledge.service.TLSyncUtils;
import com.top_logic.kafka.sync.knowledge.service.importer.KBDataProcessor;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.StackedEventWriter;
import com.top_logic.knowledge.service.ExtIDFactory;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseName;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.sched.task.impl.StateHandlingTask;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * {@link StateHandlingTask} sending changes in the {@link KnowledgeBase} to the Kafka Bus.
 * 
 * @see KBDataProcessor
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KBDataProducerTask extends StateHandlingTask<KBDataProducerTask.Config<?>> implements EventWriter {

	private static final char REVISION_DATE_SEPARATOR = '@';

	private static final String NODE = DBProperties.GLOBAL_PROPERTY;

	private static final class ProducerException extends I18NRuntimeException {

		public ProducerException(ResKey errorKey, Throwable cause) {
			super(errorKey, cause);
		}

	}

	/**
	 * Configuration of the {@link KBDataProducerTask}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends KBDataProducerTask> extends StateHandlingTask.Config<I>, KnowledgeBaseName {

		/** Configuration name of value of {@link #getKafkaProducer()}. */
		String KAFKA_PRODUCER = "kafka-producer";

		/** Configuration name of value of {@link #getRewriters()}. */
		String REWRITERS = "rewriters";

		/**
		 * Name of the {@link TLKafkaProducer} in the {@link KafkaProducerService} to send events
		 * to.
		 * 
		 * <p>
		 * It is expected that the {@link TLKafkaProducer} can send {@link TLSyncRecord} containing
		 * {@link ChangeSet}.
		 * </p>
		 */
		@Mandatory
		@Name(KAFKA_PRODUCER)
		String getKafkaProducer();

		/**
		 * List of {@link EventRewriter} that rewrites the received {@link ChangeSet}s.
		 */
		@Name(REWRITERS)
		List<PolymorphicConfiguration<? extends EventRewriter>> getRewriters();

		/**
		 * Time in ms which this task has to process the events in the {@link KnowledgeBase}.
		 * 
		 * <p>
		 * This timeout is just to detect whether a task is potentially broken.
		 * </p>
		 */
		@LongDefault(5 * 6 * 1000)
		long getLockTimeout();

		/**
		 * Number of cached events.
		 * 
		 * <p>
		 * The given number of events sent by this {@link KBDataProducerTask} are cached transiently
		 * for {@link KBDataProducerTask#getSentRecords() further processing by the application}.
		 * </p>
		 * 
		 * <p>
		 * A value <code>&lt;= 0</code> means no events are cached.
		 * </p>
		 * 
		 */
		int getCachedEventSize();

		/**
		 * The pause in milliseconds after an error happened.
		 * <p>
		 * This is necessary to prevent this task from flooding the logs when a revision cannot be
		 * sent.
		 * </p>
		 * <p>
		 * See {@link #getErrorPauseFactor()} for the formula for the error pauses in case of
		 * repeated failures.
		 * </p>
		 */
		@FormattedDefault("1min")
		@Format(MillisFormat.class)
		long getErrorPauseStart();

		/**
		 * When sending messages keeps failing, the time between the retries increases by this
		 * factor every time.
		 * <p>
		 * When it fails for the first time, the first retry happens after
		 * {@link #getErrorPauseStart()} milliseconds. The second retry happens after
		 * {@link #getErrorPauseStart()} ms multiplied by this factor. The third retry happens after
		 * <code>start * factor * factor</code> ms and so on. But the time between retries never
		 * increases above {@link #getErrorPauseMax()}. It is capped there. When it has reached this
		 * value, it stays there, until the problem is resolved and sending works again. The formula
		 * for the n'th retry is therefore:
		 * <code>min(error-pause-max, error-pause-start * (error-pause-factor ** (n-1)))</code> When
		 * it works again, but later starts failing again, the time between retries starts at
		 * {@link #getErrorPauseStart()} again.
		 * </p>
		 */
		@FloatDefault(2)
		float getErrorPauseFactor();

		/**
		 * The maximum pause in milliseconds when sending messages keeps failing.
		 * <p>
		 * See {@link #getErrorPauseFactor()} for the formula for the error pauses in case of
		 * repeated failures.
		 * </p>
		 */
		@FormattedDefault("10min")
		@Format(MillisFormat.class)
		long getErrorPauseMax();

	}

	private final TLKafkaProducer<String, TLSyncRecord<ChangeSet>> _producer;

	private final KnowledgeBase _kb;

	private final DBProperties _dbProperties;

	private final EventWriter _eventWriter;

	private final Queue<SentRecord> _lastSentEvents;

	private ExponentialBackoff _exponentialBackoff;

	private long _resumeTime = Long.MIN_VALUE;

	/**
	 * Creates a new {@link KBDataProducerTask}.
	 */
	public KBDataProducerTask(InstantiationContext context, Config<?> config) {
		super(context, config);
		_producer = findProducer();
		if (_producer == null) {
			context.error("No " + TLKafkaProducer.class.getName() + " with name '" + config.getKafkaProducer()
				+ "' known in the " + KafkaProducerService.class.getSimpleName());
		}
		int cachedEventSize = config.getCachedEventSize();
		if (cachedEventSize <= 0) {
			_lastSentEvents = null;
		} else {
			_lastSentEvents = new CircularFifoQueue<>(cachedEventSize);
		}
		_kb = KnowledgeBaseFactory.getInstance().getKnowledgeBase(getConfig().getKnowledgeBase());
		_dbProperties = createDbProperties(_kb);
		List<? extends EventRewriter> rewriters =
			TypedConfiguration.getInstanceListReadOnly(context, config.getRewriters());
		_eventWriter = StackedEventWriter.createWriter(0, this, rewriters);
	}

	@SuppressWarnings("unchecked")
	private TLKafkaProducer<String, TLSyncRecord<ChangeSet>> findProducer() {
		return (TLKafkaProducer<String, TLSyncRecord<ChangeSet>>) KafkaProducerService.getInstance()
			.getProducer(getConfig().getKafkaProducer());
	}

	private DBProperties createDbProperties(KnowledgeBase knowledgeBase) {
		return new DBProperties(((DBKnowledgeBase) knowledgeBase).getConnectionPool());
	}

	@Override
	protected void runHook() {
		LogUtil.withLogMark(LOG_MARK_TL_SYNC, "true", this::runWithLogMark);
	}

	/**
	 * Called by {@link #runHook()} with a mark for Log4j that everything within is TL-Sync related.
	 */
	protected void runWithLogMark() {
		long now = now();
		if (now < _resumeTime) {
			ResKey message = I18NConstants.SKIPPING_DUE_TO_EARLIER_PROBLEMS__RESUME_TIME.fill(new Date(_resumeTime));
			getLog().taskEnded(ResultType.WARNING, message);
			return;
		}
		HistoryManager hm = _kb.getHistoryManager();
		UpdateChainLink lastKBRevision = ((UpdateChainView) _kb.getUpdateChain()).current();

		Map<String, String> storedRevisions = getRevisionProperties();
		String lastSentRevisionAtDateLock = storedRevisions.get(TLSyncUtils.getLastSentRevisionAtDateLockKey());
		String lastSentRevisionAtDate = storedRevisions.get(TLSyncUtils.getLastSentRevisionAtDateKey());
		Long lastSentRevision = checkLockAndGetLastSentRevision(now, lastKBRevision.getRevision(),
			lastSentRevisionAtDateLock, lastSentRevisionAtDate);
		if (lastSentRevision == null) {
			return;
		}
		logDebugLockObtained();
		processRevisions(now, hm, lastKBRevision, lastSentRevisionAtDate, lastSentRevision);
	}

	private void logDebugLockObtained() {
		if (isLoggingDebug()) {
			logDebug("Lock obtained.");
		}
	}

	private Long checkLockAndGetLastSentRevision(long now, long lastKBRevision, String lastSentRevisionAtDateLock,
			String lastSentRevisionAtDate) {
		if (lastSentRevisionAtDateLock == null) {
			// No one has ever inserted anything into the database
			if (!compareAndSetLastRevisionLock(lastSentRevisionAtDateLock, toString(lastKBRevision, now))) {
				// Another task has updated lock first time and is currently processing
				getLog().taskEnded(ResultType.SUCCESS, I18NConstants.SENDING_IN_PROGRESS);
				return null;
			}
			return Revision.FIRST_REV;
		}
		if (lastSentRevisionAtDateLock.equals(lastSentRevisionAtDate)) {
			// Lock is free
			long lastSentRevision = getRevision(lastSentRevisionAtDate);
			if (lastSentRevision == lastKBRevision) {
				// No changes in the database
				getLog().taskEnded(ResultType.SUCCESS, I18NConstants.NO_CHANGES);
				return null;
			}
			if (lastKBRevision < lastSentRevision) {
				/* This may happen when a different cluster node has made a commit and sent the
				 * event to Kafka, but this cluster node is not up to date as the KB refetch has not
				 * been run yet. */
				getLog().taskEnded(ResultType.SUCCESS, I18NConstants.SENDING_IN_PROGRESS);
				return null;
			}
			if (!compareAndSetLastRevisionLock(lastSentRevisionAtDateLock, toString(lastKBRevision, now))) {
				// Different task currently sends events
				getLog().taskEnded(ResultType.SUCCESS, I18NConstants.SENDING_IN_PROGRESS);
				return null;
			}
			return lastSentRevision;
		}
		// The lock is not free.
		long lastLockDate = getDate(lastSentRevisionAtDateLock);
		long lockTime = now() - lastLockDate;
		if (lockTime <= getConfig().getLockTimeout()) {
			// Another task has updated lock and is currently processing
			getLog().taskEnded(ResultType.SUCCESS, I18NConstants.SENDING_IN_PROGRESS);
		} else {
			// Another task has updated lock but has timed out. Release lock
			compareAndSetLastRevisionLock(lastSentRevisionAtDateLock, lastSentRevisionAtDate);
			getLog().taskEnded(ResultType.ERROR,
				I18NConstants.LOCK_TIMED_OUT__TIME.fill(StopWatch.toStringMillis(lockTime)));
		}
		return null;
	}

	private void processRevisions(long now, HistoryManager hm, UpdateChainLink lastKBRevision,
			String lastSentRevisionAtDate, Long lastSentRevision) {
		boolean progress = false;
		try {
			Revision startRev = hm.getRevision(lastSentRevision + 1);
			Revision stopRev = hm.getRevision(lastKBRevision.getRevision());
			logDebugBeginSending(startRev, stopRev);
			TLSubSessionContext context = TLContextManager.getSubSession();
			UpdateChainLink updateChainLink = createUpdateChain(startRev, lastKBRevision);
			try (ChangeSetReader reader = _kb.getChangeSetReader(ReaderConfigBuilder.createConfig(startRev, stopRev))) {
				if (getShouldStop()) {
					getLog().taskEnded(ResultType.CANCELED, I18NConstants.CANCELED_DURING_STARTUP);
					return;
				}
				while (true) {
					logDebugReadingNextRevision();
					ChangeSet cs = reader.read();
					if (cs == null) {
						logDebugFinishedSending(startRev, stopRev, updateChainLink.getRevision());
						break;
					}
					logDebugReadNextChangeset(cs);
					while (updateChainLink.getRevision() != cs.getRevision()) {
						updateChainLink = updateChainLink.getNextUpdate();
						logDebugMovingToNextRevision(updateChainLink, cs);
					}
					HistoryUtils.updateSessionAndInteractionRevision(hm, context, updateChainLink);
					try {
						_eventWriter.write(cs);
						logInfoFinishedSendingRevision(cs.getRevision());
					} catch (ProducerException ex) {
						logTaskEndedWriteFailed(progress, updateChainLink.getRevision(), ex);
						_resumeTime = System.currentTimeMillis() + calcErrorPause();
						return;
					}
					long sentTime = now();
					if (sentTime - now > ((getConfig().getLockTimeout() * 2) / 3)) {
						/* This task is currently active and sends event, but the timeout is almost
						 * over. Update lock with new timeout time to ensure that no different task
						 * "kills" this task. */
						setLastRevisionLock(toString(lastKBRevision.getRevision(), sentTime));
						now = sentTime;
					}
					lastSentRevisionAtDate = toString(cs.getRevision(), sentTime);
					setLastRevision(lastSentRevisionAtDate);
					progress = true;
					logDebugStoreRevisionHasBeenSent(cs.getRevision());
					if (getShouldStop()) {
						ResKey message = getMessageTaskCanceled(startRev, cs);
						getLog().taskEnded(ResultType.CANCELED, message);
						return;
					}
					/* Reset the backoff every time at least one changeset was sent. This allows
					 * incremental progress, even if there are temporary problems from time to time.
					 * Resetting it only at the end of the task would prevent incremental progress
					 * in some situations. */
					resetExponentialBackoff();
				}
				logDebugFinishedSendingNormally();
			} catch (Throwable exception) {
				logErrorProcessingRevisionsFailed(updateChainLink.getRevision(), exception);
				throw exception;
			} finally {
				hm.updateSessionRevision();
			}
			logTaskEndedSuccessfully(startRev, stopRev);
		} catch (Throwable exception) {
			logErrorSendingFailed(exception);
			_resumeTime = System.currentTimeMillis() + calcErrorPause();
			ResKey messageKey = getMessageUnhandledException(progress);
			getLog().taskEnded(ResultType.ERROR, messageKey, exception);
		} finally {
			setLastRevisionLock(lastSentRevisionAtDate);
		}
	}

	private void logDebugBeginSending(Revision startRev, Revision stopRev) {
		if (isLoggingDebug()) {
			logDebug("Begin sending. First revision: " + startRev.getCommitNumber() + ". Last revision "
				+ stopRev.getCommitNumber());
		}
	}

	private void logDebugReadingNextRevision() {
		if (isLoggingDebug()) {
			/* Don't log updateChainLink.getRevision here, as its meaning depends on the situation:
			 * In the first iteration, it is the first revision to send. After that, it is the last
			 * revision which has been sent. That would be too confusing. */
			logDebug("Reading next revision.");
		}
	}

	private void logDebugFinishedSending(Revision startRev, Revision stopRev, long actualEnd) {
		if (isLoggingDebug()) {
			long start = startRev.getCommitNumber();
			long plannedEnd = stopRev.getCommitNumber();
			logDebug("Finished sending. No more revisions. Start was: " + start
				+ ". Planned end was: " + plannedEnd + ". Actual end was: " + actualEnd);
		}
	}

	private void logDebugReadNextChangeset(ChangeSet changeSet) {
		if (isLoggingDebug()) {
			logDebug("Read next changeset. Revision: " + changeSet.getRevision()
				+ ". Date: " + changeSet.getCommit().getDate()
				+ ". Kind: " + changeSet.getCommit().getKind()
				+ ". Author: " + changeSet.getCommit().getAuthor()
				+ ". Creates: " + changeSet.getCreations().size()
				+ ". Updates: " + changeSet.getUpdates().size()
				+ ". Deletes: " + changeSet.getDeletions().size()
				+ ". Branch events: " + changeSet.getBranchEvents().size()
				+ ". Log: " + changeSet.getCommit().getLog());
		}
	}

	private void logDebugMovingToNextRevision(UpdateChainLink updateChainLink, ChangeSet changeSet) {
		if (isLoggingDebug()) {
			long updateChainRevision = updateChainLink.getRevision();
			long changeSetRevision = changeSet.getRevision();
			logDebug("Moving forward to next revision: " + updateChainRevision
				+ ". Next change: " + changeSetRevision);
		}
	}

	private void logInfoFinishedSendingRevision(long revision) {
		logInfo("Finished sending revision: " + revision);
	}

	private void logTaskEndedWriteFailed(boolean progress, long revision, ProducerException ex) {
		ResKey messageKey = getMessageProducerException(progress).fill(revision, ex.getErrorKey());
		getLog().taskEnded(ResultType.ERROR, messageKey, ex);
	}

	private void logDebugStoreRevisionHasBeenSent(long revision) {
		if (isLoggingDebug()) {
			logDebug("Stored that revision " + revision + " has been sent.");
		}
	}

	private void logDebugFinishedSendingNormally() {
		if (isLoggingDebug()) {
			logDebug("Finished sending normally.");
		}
	}

	private void logErrorProcessingRevisionsFailed(long revision, Throwable exception) {
		String message = "Processing the revisions failed due to an unexpected exception."
			+ " Revision: " + revision + ". Exception: " + exception.getMessage();
		logError(message, exception);
	}

	private void logTaskEndedSuccessfully(Revision startRev, Revision stopRev) {
		ResKey2 successMessage = I18NConstants.SEND_TO_KAFKA_SUCCESSFUL__START_REVISION__STOP_REVISION;
		getLog().taskEnded(ResultType.SUCCESS,
			successMessage.fill(startRev.getCommitNumber(), stopRev.getCommitNumber()));
	}

	private void logErrorSendingFailed(Throwable exception) {
		logError("Sending failed due to an unexpected exception: " + exception.getMessage(), exception);
	}

	private long getDate(String revisionAtDate) {
		int separatorIndex = revisionAtDate.indexOf(REVISION_DATE_SEPARATOR);
		String dateString = revisionAtDate.substring(separatorIndex + 1);
		return Long.parseLong(dateString);
	}

	private long getRevision(String revisionAtDate) {
		int separatorIndex = revisionAtDate.indexOf(REVISION_DATE_SEPARATOR);
		String revisionString = revisionAtDate.substring(0, separatorIndex);
		return Long.parseLong(revisionString);
	}

	private boolean compareAndSetLastRevisionLock(String currentLock, String newLock) {
		ConnectionPool pool = KBUtils.getConnectionPool(_kb);
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			boolean success = DBProperties.compareAndSet(connection,
				NODE,
				TLSyncUtils.getLastSentRevisionAtDateLockKey(),
				currentLock,
				newLock);
			connection.commit();
			return success;
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			releaseWriteConnection(pool, connection);
		}
	}

	private UpdateChainLink createUpdateChain(Revision startRev, UpdateChainLink lastKBRevision) {
		UpdateChainLink updateChainLink = lastKBRevision;
		for (long revision = lastKBRevision.getRevision() - 1; revision >= startRev.getCommitNumber(); revision--) {
			UpdateChainLink predecessor = new UpdateChainLink(revision);
			predecessor.setNextUpdate(updateChainLink);
			updateChainLink = predecessor;
		}
		return updateChainLink;
	}

	private ResKey2 getMessageProducerException(boolean progress) {
		if (progress) {
			return I18NConstants.WRITE_FAILED_BUT_PROGRESS__REVISION__EXCEPTION;
		} else {
			return I18NConstants.WRITE_FAILED_NO_PROGRESS__REVISION__EXCEPTION;
		}
	}

	private ResKey getMessageTaskCanceled(Revision startRevision, ChangeSet lastChangeset) {
		long startRevisionNumber = startRevision.getCommitNumber();
		long lastRevisionNumber = lastChangeset.getRevision();
		return I18NConstants.CANCELED_DURING_PROCESSING__START_REVISION__LAST_PROCESSED
			.fill(startRevisionNumber, lastRevisionNumber);
	}

	private ResKey getMessageUnhandledException(boolean progress) {
		if (progress) {
			return I18NConstants.SEND_FAILED_UNHANDLED_EXCEPTION_BUT_PROGRESS;
		} else {
			return I18NConstants.SEND_FAILED_UNHANDLED_EXCEPTION_NO_PROGRESS;
		}
	}

	private void setLastRevisionLock(String revisionAtDate) {
		setProperty(TLSyncUtils.getLastSentRevisionAtDateLockKey(), revisionAtDate);
	}

	private void setLastRevision(String revisionAtDate) {
		setProperty(TLSyncUtils.getLastSentRevisionAtDateKey(), revisionAtDate);
	}

	private String toString(long revision, long date) {
		return Long.toString(revision) + REVISION_DATE_SEPARATOR + Long.valueOf(date);
	}

	private void setProperty(String key, String revision) {
		ConnectionPool pool = KBUtils.getConnectionPool(_kb);
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			DBProperties.setProperty(connection, NODE, key, revision);
			connection.commit();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			releaseWriteConnection(pool, connection);
		}
	}

	private void releaseWriteConnection(ConnectionPool pool, PooledConnection connection) {
		try {
			pool.releaseWriteConnection(connection);
		} catch (RuntimeException exception) {
			String message = "Failed to release write connection. Cause: " + exception.getMessage();
			Logger.error(message, exception, KBDataProducerTask.class);
		}
	}

	private Map<String, String> getRevisionProperties() {
		ConnectionPool pool = KBUtils.getConnectionPool(_kb);
		PooledConnection connection = pool.borrowReadConnection();
		try {
			return DBProperties.getProperties(connection, NODE,
				TLSyncUtils.getLastSentRevisionAtDateKey(),
				TLSyncUtils.getLastSentRevisionAtDateLockKey());
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			releaseReadConnection(pool, connection);
		}
	}

	private void releaseReadConnection(ConnectionPool pool, PooledConnection connection) {
		try {
			pool.releaseReadConnection(connection);
		} catch (RuntimeException exception) {
			String message = "Failed to release write connection. Cause: " + exception.getMessage();
			Logger.error(message, exception, KBDataProducerTask.class);
		}
	}

	private long calcErrorPause() {
		if (_exponentialBackoff == null) {
			_exponentialBackoff = createExponentialBackoff();
		}
		return Math.round(_exponentialBackoff.next());
	}

	private ExponentialBackoff createExponentialBackoff() {
		long start = getConfig().getErrorPauseStart();
		float factor = getConfig().getErrorPauseFactor();
		long max = getConfig().getErrorPauseMax();
		return new ExponentialBackoff(start, factor, max);
	}

	private void resetExponentialBackoff() {
		_exponentialBackoff = null;
	}

	@Override
	public void write(ChangeSet cs) {
		checkChangeSetNotEmpty(cs);
		TLSyncRecord<ChangeSet> tlSyncRecord = createTLSyncRecord(cs);
		ProducerRecord<String, TLSyncRecord<ChangeSet>> producerRecord = createProducerRecord(tlSyncRecord);
		send(producerRecord);
		setLastMessageRevision(cs.getRevision());
		offerSentEvent(producerRecord);
	}

	private void send(ProducerRecord<String, TLSyncRecord<ChangeSet>> producerRecord) {
		Future<RecordMetadata> send = _producer.send(producerRecord);
		try {
			send.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			throw new ProducerException(I18NConstants.ERROR_SEND_TO_KAFKA_INTERRUPTED, ex);
		} catch (ExecutionException ex) {
			throw new ProducerException(I18NConstants.ERROR_SEND_TO_KAFKA_EXCEPTION, ex);
		} catch (TimeoutException ex) {
			throw new ProducerException(I18NConstants.ERROR_SEND_TO_KAFKA_TIMEOUT, ex);
		}
	}

	private TLSyncRecord<ChangeSet> createTLSyncRecord(ChangeSet changeSet) {
		long systemId = ExtIDFactory.getInstance().getSystemId();
		return new TLSyncRecord<>(systemId, getLastMessageRevision(), changeSet);
	}

	private ProducerRecord<String, TLSyncRecord<ChangeSet>> createProducerRecord(TLSyncRecord<ChangeSet> value) {
		String topic = _producer.getTopic();
		return new ProducerRecord<>(topic, value);
	}

	/**
	 * Check that the {@link ChangeSet} is not empty.
	 * <p>
	 * Sending an empty {@link ChangeSet} will cause no problems. But it is unnecessary and should
	 * therefore not happen.
	 * </p>
	 */
	private void checkChangeSetNotEmpty(ChangeSet changeSet) {
		if (changeSet.getCreations().isEmpty()
			&& changeSet.getUpdates().isEmpty()
			&& changeSet.getDeletions().isEmpty()
			&& changeSet.getBranchEvents().isEmpty()) {
			logWarning("Sending seemingly empty changeset: " + changeSet);
		}
	}

	private long getLastMessageRevision() {
		String lastMessageRevision = _dbProperties.getProperty(TLSyncUtils.getLastMessageRevisionKey());
		if (StringServices.isEmpty(lastMessageRevision)) {
			return TLSyncRecord.LAST_MESSAGE_REVISION_NONE_SEND;
		}
		return Long.parseLong(lastMessageRevision);
	}

	private void setLastMessageRevision(long revision) {
		setProperty(TLSyncUtils.getLastMessageRevisionKey(), Long.toString(revision));
	}

	private void offerSentEvent(ProducerRecord<String, TLSyncRecord<ChangeSet>> producerRecord) {
		if (_lastSentEvents != null) {
			synchronized (_lastSentEvents) {
				_lastSentEvents.add(new SentRecord(producerRecord));
			}
		}
	}

	@Override
	public void flush() {
		// Nothing to flush. All events processed directly.
	}

	@Override
	public void close() {
		// Nothing to close.
	}

	private static boolean isLoggingDebug() {
		return Logger.isDebugEnabled(KBDataProducerTask.class);
	}

	private static void logDebug(String message) {
		Logger.debug(message, KBDataProducerTask.class);
	}

	private static void logInfo(String message) {
		Logger.info(message, KBDataProducerTask.class);
	}

	private static void logWarning(String message) {
		Logger.warn(message, KBDataProducerTask.class);
	}

	private static void logError(String message, Throwable exception) {
		Logger.error(message + " Cause: " + exception.getMessage(), exception, KBDataProducerTask.class);
	}

	/**
	 * The sent records. The events contains at most {@link Config#getCachedEventSize()} events
	 * ordered by date, i.e. previously sent events occur before later sent events.
	 * 
	 * @return The last sent events.
	 */
	public List<SentRecord> getSentRecords() {
		if (_lastSentEvents == null) {
			return Collections.emptyList();
		}
		synchronized (_lastSentEvents) {
			return Arrays.asList(_lastSentEvents.toArray(new SentRecord[_lastSentEvents.size()]));
		}
	}

	/**
	 * A record send to Kafka.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class SentRecord {

		private final ProducerRecord<String, TLSyncRecord<ChangeSet>> _producerRecord;

		private final Date _sentDate;

		SentRecord(ProducerRecord<String, TLSyncRecord<ChangeSet>> producerRecord) {
			_producerRecord = producerRecord;
			_sentDate = new Date();
		}

		/**
		 * This method returns the sent {@link ProducerRecord}.
		 */
		public ProducerRecord<String, TLSyncRecord<ChangeSet>> getRecord() {
			return _producerRecord;
		}

		/**
		 * Data at which the record was successfully sent.
		 */
		public Date getDate() {
			return _sentDate;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Date: " + _sentDate + ": " + getRecord();
		}

	}

}
