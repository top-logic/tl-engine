/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.services;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.kafka.KafkaTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.model.AbstractTLModelTest;

import com.top_logic.basic.util.Computation;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.kafka.services.consumer.ConsumerDispatcher;
import com.top_logic.kafka.services.consumer.KafkaConsumerService;
import com.top_logic.kafka.services.producer.KafkaProducerService;
import com.top_logic.kafka.sync.knowledge.service.exporter.KBDataProducerTask;
import com.top_logic.kafka.sync.knowledge.service.exporter.KBDataProducerTask.SentRecord;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ExtReference;
import com.top_logic.knowledge.objects.identifier.ExtReferenceFormat;
import com.top_logic.knowledge.service.ExtIDFactory;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.sched.Scheduler;

/**
 * Super class for {@link Test}s that tests data exchange between TL systems.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTLSyncTest extends AbstractTLModelTest {

	/** The {@link Duration} to wait for processing Kafka events. */
	private static final Duration WAIT_TIMEOUT = Duration.ofMillis(10_000);

	/** Name of the topic that is used to write KB data changes to. */
	public static final String KB_DATA_CHANGE_TOPIC = "KB-DATA-CHANGE";

	/** Name of the consumer that processes the sent {@link KnowledgeBase} events. */
	public static final String KB_CHANGE_CONSUMER = "KB-Data-Change-Consumer";

	/**
	 * Searches for the target {@link TLObject} that represents the given source item.
	 */
	public TLObject findReceivedObjectFor(TLType expectedTargetType, TLObject sourceItem) {
		ExtReference extReference = ExtIDFactory.getInstance().newExtReference(sourceItem);
		return getTLObjectByExternalId(expectedTargetType, extReference);
	}

	private TLObject getTLObjectByExternalId(TLType expectedTargetType, ExtReference extReference) {
		Iterator<KnowledgeItem> result = getKOByExternalId(expectedTargetType, extReference);
		if (!result.hasNext()) {
			return null;
		}
		KnowledgeItem foundItem = result.next();
		if (result.hasNext()) {
			throw new AssertionFailedError(
				"Found multiple items with same external ID: " + foundItem + ", " + toList(result));
		}
		return foundItem.getWrapper();
	}

	private Iterator<KnowledgeItem> getKOByExternalId(TLType expectedTargetType, ExtReference extReference) {
		String targetTable = TLAnnotations.getTable(expectedTargetType);
		MOClass staticTargetType = getMOClass(targetTable);
		String externalIDAttribute = ExtIDFactory.getInstance().getExternalIDAttribute(staticTargetType);
		return getKOByExternalId(targetTable, extReference, externalIDAttribute);
	}

	private Iterator<KnowledgeItem> getKOByExternalId(String targetTable, ExtReference extReference,
			String externalIDAttribute) {
		try {
			return kb().getObjectsByAttribute(targetTable, externalIDAttribute,
				ExtReferenceFormat.INSTANCE.format(extReference));
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private MOClass getMOClass(String moClassName) {
		try {
			return (MOClass) kb().getMORepository().getMetaObject(moClassName);
		} catch (UnknownTypeException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Triggers Kafka.
	 * 
	 * @return Number of events sent to Kafka.
	 */
	protected int triggerKafka() throws Exception {
		return runWithKafka(() -> {
			// just to trigger kafka
		});
	}

	/**
	 * Executes the given {@link Runnable} in a {@link Transaction}, commits it, sends potential
	 * changes via TL-Sync and waits for them to be received.
	 * 
	 * @throws AssertionFailedError
	 *         If a warning or error is logged.
	 */
	protected void sync(Runnable runnable) {
		sync(false, runnable);
	}

	/** Variant of {@link #sync(Runnable)} that can ignore errors and warnings in the log. */
	protected void sync(boolean ignoreLoggedErrors, Runnable runnable) {
		try {
			runWithKafka(ignoreLoggedErrors, () -> {
				KBUtils.inTransaction(() -> {
					runnable.run();
				});
			});
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Executes the given {@link Supplier} in a {@link Transaction}, commits it, sends potential
	 * changes via TL-Sync and waits for them to be received.
	 * 
	 * @return The result of the supplier.
	 * @throws AssertionFailedError
	 *         If a warning or error is logged.
	 */
	protected <T> T sync(Supplier<T> supplier) {
		return sync(false, supplier);
	}

	/** Variant of {@link #sync(Supplier)} that can ignore errors and warnings in the log. */
	protected <T> T sync(boolean ignoreLoggedErrors, Supplier<T> supplier) {
		try {
			AtomicReference<T> reference = new AtomicReference<>();
			runWithKafka(ignoreLoggedErrors, () -> {
				KBUtils.inTransaction(() -> {
					reference.set(supplier.get());
				});
			});
			return reference.get();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Runs the given {@link Computation} awaiting finishing processing of consumer
	 * {@value #KB_CHANGE_CONSUMER}.
	 * 
	 * @return Number of events sent to Kafka.
	 * @throws AssertionFailedError
	 *         If a warning or error is logged.
	 */
	protected int runWithKafka(Execution execution) throws Exception {
		return runWithKafka(false, execution);
	}

	/**
	 * Runs the given {@link Computation} awaiting finishing processing of consumer
	 * {@value #KB_CHANGE_CONSUMER}.
	 * 
	 * @param ignoreLoggedErrors
	 *        If true, this method ignores what happens in the log. This is necessary for tests of
	 *        failure situations.
	 * @return Number of events sent to Kafka.
	 * @throws AssertionFailedError
	 *         If a warning or error is logged.
	 */
	protected int runWithKafka(boolean ignoreLoggedErrors, Execution execution) throws Exception {
		if (Thread.interrupted()) {
			/* Try to narrow down what causes the "interrupt" flag to be set unexpectedly. See ticket: #27051 */
			throw new InterruptedException();
		}
		execution.run();
		AssertNoErrorLogListener listener = ignoreLoggedErrors ? null : new AssertNoErrorLogListener();
		if (listener != null) {
			listener.activate();
		}
		int sentEvents;
		try {
			TestingKBDataProcessor testProcessor = consumerProcessor();
			KBDataProducerTask producerTask = producerTask();
			Date now = new Date();
			producerTask.run();

			Set<Long> sentRevisions;
			List<SentRecord> sentRecords = producerTask.getSentRecords();
			if (sentRecords.isEmpty()) {
				sentEvents = 0;
				sentRevisions = Collections.emptySet();
			} else {
				sentRevisions = new HashSet<>();
				int index;
				for (index = sentRecords.size() - 1; index >= 0; index--) {
					SentRecord record = sentRecords.get(index);
					if (record.getDate().before(now)) {
						break;
					}
					sentRevisions.add(record.getRecord().value().getRecord().getRevision());
				}
				sentEvents = sentRecords.size() - index - 1;
			}
			Collection<Long> unprocessed = testProcessor.waitUntilAllProcessed(sentRevisions, WAIT_TIMEOUT);
			if (!unprocessed.isEmpty()) {
				throw new AssertionFailedError("Unprocessed Revisions: " + unprocessed);
			}
		} finally {
			if (listener != null) {
				listener.deactivate();
			}
		}
		HistoryUtils.updateSessionRevision(kb().getHistoryManager());
		return sentEvents;
	}

	private TestingKBDataProcessor consumerProcessor() {
		ConsumerDispatcher<?, ?> consumer = KafkaConsumerService.getInstance().getConsumer(KB_CHANGE_CONSUMER);
		if (consumer == null) {
			throw new IllegalStateException("No consumer with name " + KB_CHANGE_CONSUMER + " found.");
		}
		List<?> processors = ReflectionUtils.getValue(consumer, "_processors", List.class);

		TestingKBDataProcessor testProcessor = null;
		for (Object processor : processors) {
			if (processor instanceof TestingKBDataProcessor) {
				if (testProcessor != null) {
					throw new IllegalStateException("Multiple instances of TestingKBDataProcessor configured: "
						+ processor + " vs. " + testProcessor);
				}
				testProcessor = (TestingKBDataProcessor) processor;
			}
		}
		return testProcessor;
	}


	private KBDataProducerTask producerTask() {
		String producerTaskName = "KafkaProducerTask";
		KBDataProducerTask task = (KBDataProducerTask) Scheduler.getSchedulerInstance().getTaskByName(producerTaskName);
		if (task == null) {
			throw new IllegalStateException("No task with name " + producerTaskName + " available.");
		}
		return task;
	}

	public static Test suite(Class<?> testClass) {
		return suite(testClass, DefaultTestFactory.INSTANCE);
	}

	public static Test suite(Class<?> testClass, TestFactory factory) {
		// start producer and consumer
		factory = ServiceTestSetup.createStarterFactoryForModules(factory,
			KafkaProducerService.Module.INSTANCE,
			KafkaConsumerService.Module.INSTANCE);

		// start model.
		factory = ServiceTestSetup.createStarterFactory(ModelService.Module.INSTANCE, factory);

		// Need Scheduler to send events to Kafka.
		factory = ServiceTestSetup.createStarterFactory(Scheduler.Module.INSTANCE, factory);

		Test test = KBSetup.getKBTestWithoutSetups(testClass, factory, true);
		return KafkaTestSetup.suite(test);
	}

}

