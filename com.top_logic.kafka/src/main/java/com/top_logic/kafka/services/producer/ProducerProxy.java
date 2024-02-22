/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.producer;

import static java.util.Objects.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.ProducerFencedException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.logging.LogUtil;
import com.top_logic.basic.time.TimeUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.kafka.log.KafkaHeaderPrinter;
import com.top_logic.kafka.log.KafkaLogUtil;
import com.top_logic.kafka.log.KafkaLogWriter;
import com.top_logic.kafka.services.common.CommonClientConfig;
import com.top_logic.kafka.services.common.KafkaCommonClient;

/**
 * Proxy for a {@link Producer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ProducerProxy<K, V> implements Producer<K, V>, KafkaHeaderPrinter {

	private static final String PRODUCER_RECORD = "producer-record";

	private static final String TOPIC = "topic";

	private static final String KEY = "key";

	private static final String PARTITION = "partition";

	private static final String TIMESTAMP = "timestamp";

	private static final String HEADERS = "headers";

	private static final String VALUE = "value";

	private Throwable _lastError;

	private Instant _lastSendTimestamp;

	private final KafkaLogWriter<V> _logWriter;

	/** Creates a {@link ProducerProxy}. */
	public ProducerProxy(KafkaLogWriter<V> logWriter) {
		_logWriter = requireNonNull(logWriter);
	}

	/**
	 * The {@link Producer} to delegate to.
	 */
	protected abstract Producer<K, V> getImpl();

	@Override
	public void close() {
		withLogMarkAndStateLogging("Closing " + toString() + ".", () -> getImpl().close());
	}

	@Override
	public void close(Duration timeout) {
		withLogMarkAndStateLogging("Closing " + toString() + ".", () -> getImpl().close(timeout));
	}

	@Override
	public void flush() {
		withLogMarkAndStateLogging("Flushing " + toString() + ".", () -> getImpl().flush());
	}

	@Override
	public Map<MetricName, ? extends Metric> metrics() {
		// No need to log the call of a getter.
		return withLogMark(() -> getImpl().metrics());
	}

	@Override
	public List<PartitionInfo> partitionsFor(String topic) {
		String description = "Retrieving partition data for topic '" + topic + "'.";
		return withLogMarkAndStateLogging(description, () -> getImpl().partitionsFor(topic));
	}

	@Override
	public Future<RecordMetadata> send(ProducerRecord<K, V> record) {
		return send(record, null);
	}

	@Override
	public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback onAcknowledge) {
		return withLogMark(() -> {
			UUID id = KafkaLogUtil.attachId(record);
			logSendBegin(id, record);
			try {
				Callback callback = (metaData, exception) -> {
					onTransmissionEnd(metaData, exception, id);
					if (onAcknowledge != null) {
						onAcknowledge.onCompletion(metaData, exception);
					}
				};
				Future<RecordMetadata> future = getImpl().send(record, callback);
				logSendEnd(id);
				return future;
			} catch (Throwable exception) {
				logSendFailed(id);
				setLastRunError(exception);
				throw exception;
			}
		});
	}

	/** Kafka invokes this callback when a record has been sent, whether it succeeded or failed */
	protected void onTransmissionEnd(RecordMetadata metaData, Exception exception, UUID id) {
		if (exception == null) {
			logTransmissionSucceeded(metaData, id);
			setLastRunSuccess();
		} else {
			logTransmissionFailed(id, exception);
			setLastRunError(exception);
		}
	}

	private void logSendBegin(UUID id, ProducerRecord<K, V> record) {
		logInfo("Begin: Sending record " + id + ". " + writeMetaData(record));
		if (isLoggingDebug()) {
			logDebug("Record data " + id + ": " + writeAllData(record.value()));
		}
	}

	private void logSendEnd(UUID id) {
		logInfo("End: Sending record " + id + ". Data was handed to the network layer for transmission.");
	}

	private void logSendFailed(UUID id) {
		logInfo("End with exception: Sending record " + id + ".");
	}

	private void logTransmissionFailed(UUID id, Throwable exception) {
		logError("Transmission failed. Record id: " + id + ".", exception);
	}

	private void logTransmissionSucceeded(RecordMetadata metaData, UUID id) {
		/* The "offset" was not known when the record meta data were logged. It is therefore logged
		 * here. */
		logInfo("Transmission succeeded. Record id: " + id + ". Offset: " + metaData.offset() + ".");
	}

	/** Writes the {@link ConsumerRecord} meta data in XML format. */
	protected final String writeMetaData(ProducerRecord<K, V> record) {
		TagWriter output = new TagWriter();
		writeMetaData(output, record);
		return output.toString();
	}

	/** Writes the {@link ProducerRecord} meta data in XML format. */
	protected void writeMetaData(TagWriter output, ProducerRecord<K, V> record) {
		output.beginTag(PRODUCER_RECORD);
		{
			KafkaLogUtil.writeTextTag(output, TOPIC, record.topic());
			KafkaLogUtil.writeTextTag(output, KEY, record.key());
			KafkaLogUtil.writeTextTag(output, PARTITION, record.partition());
			/* The date has to be human readable. */
			KafkaLogUtil.writeTextTag(output, TIMESTAMP, TimeUtil.toStringEpoche(record.timestamp()));
			output.beginTag(HEADERS);
			write(output, record.headers());
			output.endTag(HEADERS);
			output.beginTag(VALUE);
			writeMetaData(output, record.value());
			output.endTag(VALUE);
		}
		output.endTag(PRODUCER_RECORD);
	}

	/**
	 * Writes all the meta data of the given object.
	 * <p>
	 * The result should be XML, as many text editors can automatically format XML for better
	 * readability. Other formats, such as JSON, don't have the same level of support, yet.
	 * </p>
	 * <p>
	 * "Meta data" means: The amount of data written should be fixed and not grow for large
	 * messages.
	 * </p>
	 * 
	 * @see CommonClientConfig#getLogWriter()
	 * @see KafkaLogWriter#writeMetaData(TagWriter, Object)
	 */
	protected void writeMetaData(TagWriter output, V value) {
		_logWriter.writeMetaData(output, value);
	}

	/** Writes the all data of the given object. */
	protected final String writeAllData(V value) {
		TagWriter output = new TagWriter();
		writeAllData(output, value);
		return output.toString();
	}

	/**
	 * Writes all data of the given object.
	 * <p>
	 * The result should be XML, as many text editors can automatically format XML for better
	 * readability. Other formats, such as JSON, don't have the same level of support, yet.
	 * </p>
	 * <p>
	 * "All data" means: The data should be detailed enough to fully reconstruct the message from
	 * the written data.
	 * </p>
	 * 
	 * @see CommonClientConfig#getLogWriter()
	 * @see KafkaLogWriter#writeAllData(TagWriter, Object)
	 */
	protected void writeAllData(TagWriter output, V value) {
		_logWriter.writeAllData(output, value);
	}

	@Override
	public void abortTransaction() throws ProducerFencedException {
		withLogMarkAndStateLogging("Aborting transaction.", () -> getImpl().abortTransaction());
	}

	@Override
	public void initTransactions() {
		withLogMarkAndStateLogging("Initializing transaction.", () -> getImpl().initTransactions());
	}

	@Override
	public void commitTransaction() throws ProducerFencedException {
		withLogMarkAndStateLogging("Committing transaction.", () -> getImpl().commitTransaction());
	}

	@Override
	public void beginTransaction() throws ProducerFencedException {
		withLogMarkAndStateLogging("Beginning transaction.", () -> getImpl().beginTransaction());
	}

	@Override
	public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets,
			ConsumerGroupMetadata groupMetadata) throws ProducerFencedException {
		String description = "Sending offset to transaction. Offsets: '"
			+ offsets + "'. Group metadata: " + groupMetadata + ".";
		withLogMarkAndStateLogging(description, () -> getImpl().sendOffsetsToTransaction(offsets, groupMetadata));
	}

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String consumerGroupId)
			throws ProducerFencedException {
		String description = "Sending offset to transaction. CunsumerGroupId: '"
			+ consumerGroupId + "'. Offsets: " + offsets + ".";
		withLogMarkAndStateLogging(description, () -> getImpl().sendOffsetsToTransaction(offsets, consumerGroupId));
	}

	private void withLogMarkAndStateLogging(String description, Runnable runnable) {
		String hostAddress = KafkaLogUtil.getHostAddress();
		String fullDescription = "Producer '" + getName() + "' on '" + hostAddress + "': " + description;
		withLogMark(() -> LogUtil.withBeginEndLogging(ProducerProxy.class, fullDescription, runnable));
	}

	private <T> T withLogMarkAndStateLogging(String description, Supplier<T> supplier) {
		String hostAddress = KafkaLogUtil.getHostAddress();
		String fullDescription = "Producer '" + getName() + "' on '" + hostAddress + "': " + description;
		return withLogMark(() -> LogUtil.withBeginEndLogging(ProducerProxy.class, fullDescription, supplier));
	}

	private void withLogMark(Runnable runnable) {
		LogUtil.withLogMark(KafkaCommonClient.LOG_MARK_KAFKA, "true", runnable);
	}

	private <T> T withLogMark(Supplier<T> supplier) {
		return LogUtil.withLogMark(KafkaCommonClient.LOG_MARK_KAFKA, "true", supplier);
	}

	private void logError(String message, Throwable exception) {
		String hostAddress = KafkaLogUtil.getHostAddress();
		String messagePrefix = "Producer '" + getName() + "' on '" + hostAddress + "': ";
		Logger.error(messagePrefix + message + " Cause: " + exception.getMessage(), exception, ProducerProxy.class);
	}

	private void logInfo(String message) {
		String hostAddress = KafkaLogUtil.getHostAddress();
		String messagePrefix = "Producer '" + getName() + "' on '" + hostAddress + "': ";
		Logger.info(messagePrefix + message, ProducerProxy.class);
	}

	private void logDebug(String message) {
		String hostAddress = KafkaLogUtil.getHostAddress();
		String messagePrefix = "Producer '" + getName() + "' on '" + hostAddress + "': ";
		Logger.debug(messagePrefix + message, ProducerProxy.class);
	}

	private boolean isLoggingDebug() {
		return Logger.isDebugEnabled(ProducerProxy.class);
	}

	/** The {@link Throwable} which ended the last run, or null if it succeeded. */
	public synchronized Throwable getLastError() {
		return _lastError;
	}

	/** @see #getLastError() */
	public synchronized void setLastRunSuccess() {
		setLastRunError(null);
	}

	/** @see #getLastError() */
	public synchronized void setLastRunError(Throwable lastError) {
		_lastError = lastError;
		_lastSendTimestamp = Instant.now();
	}

	/**
	 * The time stamp at which the last run ended.
	 * <p>
	 * null, when it did not run, yet.
	 * </p>
	 */
	public Instant getLastSendTimestamp() {
		return _lastSendTimestamp;
	}

	/** A name for this producer, suitable for log messages. */
	public abstract String getName();

}
