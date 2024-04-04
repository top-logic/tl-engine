/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.consumer;

import static java.util.Objects.*;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.Deserializer;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.InlineList;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.logging.LogUtil;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.TimeUtil;
import com.top_logic.basic.util.ExponentialBackoff;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.kafka.log.KafkaHeaderPrinter;
import com.top_logic.kafka.log.KafkaLogUtil;
import com.top_logic.kafka.log.KafkaLogWriter;
import com.top_logic.kafka.services.common.CommonClientConfig;
import com.top_logic.kafka.services.common.KafkaCommonClient;

/**
 * The consumer {@link Thread} which will forward {@link ConsumerRecords} to
 * all registered {@link ConsumerProcessor}s.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class ConsumerDispatcher<K, V> extends Thread
		implements KafkaCommonClient<V, ConsumerDispatcherConfiguration<K, V>>, KafkaHeaderPrinter {

	private static final String TOPIC = "topic";

	private static final String KEY = "key";

	private static final String PARTITION = "partition";

	private static final String OFFSET = "offset";

	private static final String CONSUMER_RECORD = "consumer-record";

	private static final String LEADER_EPOCH = "leader-epoch";

	private static final String TIMESTAMP = "timestamp";

	private static final String TIMESTAMP_TYPE = "timestamp-type";

	private static final String SERIALIZED_VALUE_SIZE = "serialized-value-size";

	private static final String HEADERS = "headers";

	private static final String VALUE = "value";

	/**
	 * A message format for information to be logged when consumer was forced to
	 * wake up.
	 */
	private static final String INFO_EXECUTE = "Consumer %s woke up.";

	/**
	 * A message format for errors to be logged when the {@link Thread} is being terminated.
	 */
	private static final String ERROR_THREAD_TERMINATION = "Consumer %s failed, as its thread is being terminated.";

	/**
	 * A message format for errors to be logged when consumer execution fails.
	 */
	private static final String ERROR_EXECUTE = "Consumer %s failed. Retry in %dms.";

	/**
	 * @see #terminate()
	 */
	private final ConsumerState _consumerState = new ConsumerState(ConsumerState.STARTING);
	
	/**
	 * A {@link List} of currently registered {@link ConsumerProcessor}s.
	 */
	private final List<ConsumerProcessor<K, V>> _processors;
	
	/**
	 * The {@link ConsumerDispatcherConfiguration} this instance was created
	 * with.
	 */
	private final ConsumerDispatcherConfiguration<K, V> _config;
	
	/**
	 * The {@link KafkaConsumer} to be used for receiving messages from kafka.
	 */
	private final KafkaConsumer<K, V> _consumer;

	private Throwable _lastRunError;
	
	private Instant _lastRunEndTimestamp;

	private final KafkaLogWriter<V> _logWriter;

	private ExponentialBackoff _exponentialBackoff;

	/**
	 * Create a {@link ConsumerDispatcher}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public ConsumerDispatcher(final InstantiationContext context, final ConsumerDispatcherConfiguration<K, V> config) {
		super(config.getName());
		_config = config;
		_processors = TypedConfiguration.getInstanceList(context, config.getProcessors());
		_consumer = requireNonNull(newKafkaConsumer(context, config));
		_logWriter = requireNonNull(context.getInstance(config.getLogWriter()));
	}

	/**
	 * This method is not intended to be overridden. It is private to enable overrides in cases
	 * where nothing else helps.
	 */
	protected KafkaConsumer<K, V> newKafkaConsumer(InstantiationContext context,
			ConsumerDispatcherConfiguration<K, V> config) {
		try {
			return newKafkaConsumerInternal(context, config);
		} catch (RuntimeException ex) {
			StringBuilder msg = new StringBuilder();
			msg.append("Unable to create ");
			msg.append(ConsumerDispatcher.class.getName());
			msg.append(" with name ");
			msg.append(config.getName());
			context.error(msg.toString(), ex);
			return null;
 		}
	}

	/**
	 * This method is not intended to be overridden. It is private to enable overrides in cases
	 * where nothing else helps.
	 */
	protected KafkaConsumer<K, V> newKafkaConsumerInternal(InstantiationContext context,
			ConsumerDispatcherConfiguration<K, V> config) {
		Map<String, Object> properties = getAllProperties();
		Deserializer<K> keyDeserializer = getKeyDeserializer(context, config, properties);
		Deserializer<V> valueDeserializer = getValueDeserializer(context, config, properties);
		return new KafkaConsumer<>(properties, keyDeserializer, valueDeserializer);
	}

	/**
	 * The {@link Deserializer} that is passed in the constructor of the {@link KafkaProducer}.
	 * <p>
	 * This method is called in the constructor. Fields from subclasses are therefore not
	 * initialized, yet.
	 * </p>
	 * 
	 * @param context
	 *        Never null.
	 * @param config
	 *        Never null.
	 * 
	 * @return If null, the value in the Kafka properties is used.
	 */
	protected Deserializer<K> getKeyDeserializer(InstantiationContext context,
			ConsumerDispatcherConfiguration<K, V> config, Map<String, Object> kafkaConf) {
		if (config.getKeyDeserializerClass() != null) {
			kafkaConf.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getKeyDeserializerClass());
			return null;
		}
		return context.getInstance(config.getKeyDeserializer());
	}

	/**
	 * The {@link Deserializer} that is passed in the constructor of the {@link KafkaProducer}.
	 * <p>
	 * This method is called in the constructor. Fields from subclasses are therefore not
	 * initialized, yet.
	 * </p>
	 * 
	 * @param context
	 *        Never null.
	 * @param config
	 *        Never null.
	 * 
	 * @return If null, the value in the Kafka properties is used.
	 */
	protected Deserializer<V> getValueDeserializer(InstantiationContext context,
			ConsumerDispatcherConfiguration<K, V> config, Map<String, Object> kafkaConf) {
		if (config.getValueDeserializerClass() != null) {
			kafkaConf.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getValueDeserializerClass());
			return null;
		}
		return context.getInstance(config.getValueDeserializer());
	}
	
	/**
	 * Request termination of this {@link ConsumerDispatcher}.
	 */
	public void terminate() {
		logInfo("Terminating");
		_consumerState.set(ConsumerState.TERMINATED);
		if (this.isAlive()) {
			_consumer.wakeup();
		}
	}

	/**
	 * The state of this {@link ConsumerDispatcher}.
	 */
	public final ConsumerState getConsumerState() {
		return _consumerState;
	}

	@Override
	public void run() {
		LogUtil.withLogMark(LOG_MARK_KAFKA, "true", this::runWithLogMark);
	}

	/** Called by {@link #run()} with a mark for Log4j that everything within is Kafka related. */
	protected void runWithLogMark() {
		startup();
		try {
			execute();
		} finally {
			shutdown();
		}
	}

	/**
	 * Initialize this {@link ConsumerDispatcher} by instantiating
	 * {@link KafkaConsumer} and subscribing to the configured topics.
	 */
	protected void startup() {
		logInfo("Starting");
		subscribe();
	}

    /** 
     * Return the names of the topics to subscribe to.
     * 
     * <p>When the {@link ConsumerDispatcherConfiguration#getTopics()} is
     * not set, the given consumer will be asked for all topics available
     * (via {@link #discoverTopics(KafkaConsumer)}) and return that names.</p>
     * 
     * @param aConsumer
     *            Consumer to get the topics for.
     * @return    Names of topics to subscribe to.
     * @see       #discoverTopics(KafkaConsumer)
     */
    protected Collection<String> getTopics(KafkaConsumer<K, V> aConsumer) {
		Set<String> theTopics = getConfig().getTopics();

        if (!CollectionUtil.isEmpty(theTopics)) {
            return theTopics;
        }
        else {
            return this.discoverTopics(aConsumer);
        }
    }

	/** 
	 * Find the topics by asking the given consumer.
	 * 
     * @param aConsumer
     *            Consumer to get the topics for.
     * @return    Names of discovered topics.
     * @see       #getTopics(KafkaConsumer)
	 */
	protected Collection<String> discoverTopics(KafkaConsumer<K, V> aConsumer) {
	    return aConsumer.listTopics().keySet();
    }

    /**
	 * Start the dispatch loop after successful {@link #startup()}.
	 */
	protected void execute() {
		logInfo("Executing");
		// continue execution until termination is requested
		initialPoll();
		while (true) {
			if (_consumerState.get() == ConsumerState.TERMINATED) {
				break;
			}
			poll();
		}
	}

	private void poll() {
		poll(ConsumerState.POLLING, ConsumerState.PROCESSING);
	}

	private void initialPoll() {
		// Do not switch state during initial polling
		int processedRecords = poll(ConsumerState.STARTING, ConsumerState.STARTING);
		if (processedRecords != 0) {
			StringBuilder msg = new StringBuilder();
			msg.append("Processed ");
			msg.append(processedRecords);
			msg.append(" records occured during downtime.");
			Logger.info(msg.toString(), ConsumerDispatcher.class);
		}
	}

	private int poll(int pollingState, int processingState) {
		if (isLoggingDebug()) {
			logDebug("Polling: Begin");
		}
		try {
			_consumerState.set(pollingState);
			final ConsumerRecords<K, V> records = _consumer.poll(getConfig().getPollingTimeout());
			if (records.isEmpty()) {
				if (isLoggingDebug()) {
					/* As this can happen once per second, it is only logged on level DEBUG, not INFO. */
					logDebug("Polling: Received 0 records");
				}
				setLastRunError(null);
				resetExponentialBackoff();
				return 0;
			}
			logInfo("Polling: Received " + records.count() + " records");
			_consumerState.set(processingState);

			/* Always create a new Context for safety reasons. If not, and a KB commit failed
			 * without rollback, the context is never rolled back. */
			ThreadContext.inSystemContext(ConsumerDispatcher.class, () -> {
				dispatch(records);
				return null;
			});
			if (!getConfig().getEnableAutoCommit()) {
				_consumer.commitSync();
			}
			int count = records.count();
			setLastRunError(null);
			resetExponentialBackoff();
			return count;
		} catch (WakeupException e) {
			Logger.info(String.format(INFO_EXECUTE, getConfig().getName()), e, ConsumerDispatcher.class);
			setLastRunError(e);
		} catch (Throwable e) {
			long errorPause = calcErrorPause();
			Logger.error(String.format(ERROR_EXECUTE, getConfig().getName(), errorPause), e, ConsumerDispatcher.class);
			setLastRunError(e);
			_consumerState.set(ConsumerState.ERROR);
			skip(errorPause);
			// Ensure that not committed changes are polled again.
			reset();
		} finally {
			if (isLoggingDebug()) {
				logDebug("Polling: End");
			}
		}
		return 0;
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

	/**
	 * Resets the {@link KafkaConsumer} to the last committed offset.
	 */
	private void reset() {
		unsubscribe();
		subscribe();
	}

	/**
	 * Subscribes the {@link KafkaConsumer} to the topic.
	 */
	private void subscribe() {
		_consumer.subscribe(this.getTopics(_consumer));
	}

	/**
	 * Unsubscribes the {@link KafkaConsumer}.
	 */
	private void unsubscribe() {
		_consumer.unsubscribe();
	}

	/**
	 * Dispatch the given records to all currently registered
	 * {@link ConsumerProcessor}s.
	 * 
	 * @param records
	 *            the {@link ConsumerRecords} to dispatch
	 */
	protected void dispatch(final ConsumerRecords<K, V> records) throws Exception {
		for (ConsumerRecord<K, V> record : records) {
			String id = KafkaLogUtil.toStringTLMessageId(record.headers());
			logInfo("Processing record" + id + ": " + writeMetaData(record));
			if (isLoggingDebug()) {
				logDebug("Record data" + id + ": " + writeAllData(record.value()));
			}
		}
		Object errors = InlineList.newInlineList();
		for (final ConsumerProcessor<K, V> processor : _processors) {
			try {
				logInfo("Processor '" + processor + "': Begin");
				processor.process(records);
				logInfo("Processor '" + processor + "': End");
			} catch (Throwable e) {
				logError("Processor '" + processor + "': End with exception", e);
				String message = "Processor " + processor.getClass() + " failed. Cause: " + e.getMessage();
				RuntimeException newError = new RuntimeException(message, e);
				errors = InlineList.add(RuntimeException.class, errors, newError);
			}
		}
		RuntimeException exception = toException(errors);
		if (exception != null) {
			throw exception;
		}
	}

	private RuntimeException toException(Object errors) {
		switch (InlineList.size(errors)) {
			case 0:
				// No problem
				return null;
			case 1:
				return InlineList.iterator(RuntimeException.class, errors).next();
			default:
				return ExceptionUtil.createException("Unable to process records",
					InlineList.toList(RuntimeException.class, errors));
		}
	}

	/** Writes the {@link ConsumerRecord} meta data in XML format. */
	protected final String writeMetaData(ConsumerRecord<K, V> record) {
		TagWriter output = new TagWriter();
		writeMetaData(output, record);
		return output.toString();
	}

	/** Writes the {@link ConsumerRecord} meta data in XML format. */
	protected void writeMetaData(TagWriter output, ConsumerRecord<K, V> record) {
		output.beginTag(CONSUMER_RECORD);
		{
			KafkaLogUtil.writeTextTag(output, TOPIC, record.topic());
			KafkaLogUtil.writeTextTag(output, KEY, record.key());
			KafkaLogUtil.writeTextTag(output, PARTITION, record.partition());
			KafkaLogUtil.writeTextTag(output, OFFSET, record.offset());
			KafkaLogUtil.writeTextTag(output, LEADER_EPOCH, record.leaderEpoch());
			/* The date has to be human readable. */
			KafkaLogUtil.writeTextTag(output, TIMESTAMP, TimeUtil.toStringEpoche(record.timestamp()));
			KafkaLogUtil.writeTextTag(output, TIMESTAMP_TYPE, record.timestampType());
			KafkaLogUtil.writeTextTag(output, SERIALIZED_VALUE_SIZE, record.serializedValueSize());
			output.beginTag(HEADERS);
			write(output, record.headers());
			output.endTag(HEADERS);
			output.beginTag(VALUE);
			writeMetaData(output, record.value());
			output.endTag(VALUE);
		}
		output.endTag(CONSUMER_RECORD);
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

	/**
	 * Skip execution for at most the given amount of time. If the thread is
	 * interrupted while sleeping, the execution is resumed.
	 * 
	 * @param millis
	 *            the maximum amount of milliseconds to skip execution for
	 */
	protected void skip(final long millis) {
		try {
			// use the polling interval for retry back-off
			Thread.sleep(millis);
		} catch(InterruptedException i) {
			// ignore interruption and continue without waiting EXACTLY <timeout>
		}
	}
	
	/**
	 * Shutdown this {@link ConsumerDispatcher} and close the underlying
	 * {@link KafkaConsumer}.
	 */
	protected void shutdown() {
		logInfo("Shutting down");
		// continue shutdown() until the consumer is reset
		while (true) {
			try {
				if (getConfig().getEnableAutoCommit()) {
					// Commit when enableAutoCommit=true to avoid fetching formerly consumed events
					_consumer.commitSync();
				}
				unsubscribe();
				// close the consumer first
				_consumer.close();
			} catch(InterruptException e) {
				// close() interrupted, consumer not reset => try again
				continue;
			}
			break;
		}
	}

	@Override
	public ConsumerDispatcherConfiguration<K, V> getConfig() {
		return _config;
	}

	/** The {@link Throwable} which ended the last run, or null if it succeeded. */
	public synchronized Throwable getLastRunError() {
		return _lastRunError;
	}

	/** @see #getLastRunError() */
	public synchronized void setLastRunError(Throwable lastRunError) {
		_lastRunError = lastRunError;
		_lastRunEndTimestamp = Instant.now();
	}

	/**
	 * The time stamp at which the last run ended.
	 * <p>
	 * null, when it did not run, yet.
	 * </p>
	 */
	public Instant getLastRunEndTimestamp() {
		return _lastRunEndTimestamp;
	}

	private void logError(String message, Throwable exception) {
		String hostAddress = KafkaLogUtil.getHostAddress();
		String prefix = "Consumer '" + getName() + "' on '" + hostAddress + "': ";
		Logger.error(prefix + message, exception, ConsumerDispatcher.class);
	}

	private void logInfo(String message) {
		String hostAddress = KafkaLogUtil.getHostAddress();
		String prefix = "Consumer '" + getName() + "' on '" + hostAddress + "': ";
		Logger.info(prefix + message, ConsumerDispatcher.class);
	}

	private void logDebug(String message) {
		String hostAddress = KafkaLogUtil.getHostAddress();
		String prefix = "Consumer '" + getName() + "' on '" + hostAddress + "': ";
		Logger.debug(prefix + message, ConsumerDispatcher.class);
	}

	private boolean isLoggingDebug() {
		return Logger.isDebugEnabled(ConsumerDispatcher.class);
	}

}