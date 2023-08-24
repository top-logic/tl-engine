/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.kafka.services.common;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.stream.Collectors.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;

import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.kafka.services.consumer.ConsumerDispatcherConfiguration;
import com.top_logic.kafka.services.producer.TLKafkaProducer;

/**
 * Checks whether topics exists on a Kafka server and creates missing topics.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class TopicChecker extends AbstractConfiguredInstance<TopicChecker.Config> {

	/** {@link ConfigurationItem} for the {@link TopicChecker}. */
	public interface Config extends PolymorphicConfiguration<TopicChecker> {

		@Override
		@ClassDefault(TopicChecker.class)
		Class<? extends TopicChecker> getImplementationClass();

		/**
		 * The timeout in milliseconds when waiting for the server response with the list of topics.
		 * <p>
		 * The value "0" means to not wait. Negative values are not allowed. To wait "forever" use
		 * {@link Long#MAX_VALUE}.
		 * </p>
		 */
		@FormattedDefault("10s")
		@Format(MillisFormat.class)
		@Constraint(NonNegative.class)
		long getWaitTimeout();

		/** Whether missing topics should be created. */
		@BooleanDefault(true)
		boolean shouldCreateTopics();

		/**
		 * Whether the check should be disabled.
		 * <p>
		 * Developers might want to disable the checks to start an application which uses Kafka
		 * without having to start Kafka.
		 * </p>
		 */
		boolean isDisabled();

	}

	/** The number of partitions when a topic is created. */
	public static final int PARTITION_COUNT = 1;

	/** The "replication factor" when a topic is created. */
	public static final short REPLICATION_FACTOR = 1;

	private InstantiationContext _context;

	private CommonClientConfig<?, ?> _commonClientConfig;

	private Set<String> _topics;

	/** {@link TypedConfiguration} constructor for {@link TopicChecker}. */
	public TopicChecker(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Reports an error in the {@link InstantiationContext} if any of the producers topics don't
	 * exist.
	 */
	public static boolean checkTopicExists(InstantiationContext context, TLKafkaProducer.Config<?, ?> config) {
		Set<String> topics = Set.of(config.getTopic());
		return checkTopicExists(context, config, topics);
	}

	/**
	 * Reports an error in the {@link InstantiationContext} if any of the consumers topics don't
	 * exist.
	 */
	public static boolean checkTopicsExists(InstantiationContext context,
			ConsumerDispatcherConfiguration<?, ?> config) {
		Set<String> topics = Set.copyOf(config.getTopics());
		return checkTopicExists(context, config, topics);
	}

	private static boolean checkTopicExists(InstantiationContext context, CommonClientConfig<?, ?> foreignConfig,
			Set<String> topics) {
		Config ownConfig = ApplicationConfig.getInstance().getConfig(Config.class);
		if (ownConfig.isDisabled()) {
			return true;
		}
		TopicChecker checker = context.getInstance(ownConfig);
		checker.init(context, foreignConfig, topics);
		return checker.checkTopicExists();
	}

	/** Initializes the {@link TopicChecker}. */
	public void init(InstantiationContext context, CommonClientConfig<?, ?> config, Set<String> topics) {
		_context = context;
		_commonClientConfig = config;
		_topics = topics;
	}

	/** Reports an error in the {@link InstantiationContext} if any of the topics don't exist. */
	protected boolean checkTopicExists() {
		AdminClient adminClient = createAdminClient();
		if (adminClient == null) {
			return false;
		}
		Set<String> existingTopics = fetchTopicNames(adminClient);
		Set<String> missingTopics = set(_topics);
		missingTopics.removeAll(existingTopics);
		if (missingTopics.isEmpty()) {
			return true;
		}
		if (getConfig().shouldCreateTopics()) {
			createTopics(adminClient, missingTopics, existingTopics);
			existingTopics = fetchTopicNames(adminClient);
			missingTopics.removeAll(existingTopics);
			if (missingTopics.isEmpty()) {
				return true;
			}
		}
		logMissingTopic(missingTopics, existingTopics, null);
		return false;
	}

	private void createTopics(AdminClient adminClient, Set<String> missingTopics, Set<String> existingTopics) {
		List<NewTopic> newTopics = missingTopics.stream()
			.map(name -> new NewTopic(name, PARTITION_COUNT, REPLICATION_FACTOR))
			.collect(toList());
		try {
			CreateTopicsResult request = adminClient.createTopics(newTopics);
			waitForReceive(request.all());
		} catch (RuntimeException exception) {
			RuntimeException cause = new RuntimeException("Failed to create the missing topics.", exception);
			logMissingTopic(missingTopics, existingTopics, cause);
		}
	}

	/** Creates the {@link AdminClient} which will retrieve the topic list from the server. */
	protected AdminClient createAdminClient() {
		Map<String, Object> adminConfig = deriveAdminClientConfig();
		try {
			return AdminClient.create(adminConfig);
		} catch (RuntimeException ex) {
			_context.error("Unable to create AdminClient for Kafka client '" + _commonClientConfig.getName() + "'.", ex);
			return null;
		}
	}

	/**
	 * Derives the {@link AdminClientConfig} from the {@link CommonClientConfig} by sorting out
	 * properties which are not used by the {@link AdminClient}.
	 */
	protected Map<String, Object> deriveAdminClientConfig() {
		Map<String, Object> adminConfig = _commonClientConfig.getAllProperties();
		adminConfig.keySet().retainAll(AdminClientConfig.configNames());
		return adminConfig;
	}

	/** Fetches the list of existing topics from the server. */
	protected Set<String> fetchTopicNames(AdminClient adminClient) {
		KafkaFuture<Set<String>> topicNamesFuture = adminClient.listTopics().names();
		return waitForReceive(topicNamesFuture);
	}

	/**
	 * Reports an error into the {@link InstantiationContext} that some of the required topics are
	 * missing.
	 */
	protected void logMissingTopic(Set<String> missing, Set<String> existing, Throwable cause) {
		String clientName = _commonClientConfig.getName();

		List<String> existingSorted = CollectionFactory.list(existing);
		existingSorted.sort(Comparator.naturalOrder());

		List<String> missingSorted = CollectionFactory.list(missing);
		missingSorted.sort(Comparator.naturalOrder());

		List<String> requiredSorted = CollectionFactory.list(_topics);
		requiredSorted.sort(Comparator.naturalOrder());

		_context.error("One or more topics for Kafka client '" + clientName + "' either do not exist,"
			+ " or access to it is restricted and the application does not have the necessary credentials."
			+ " Required topics: " + requiredSorted
			+ ". Missing topics: " + missingSorted
			+ ". Existing topics: " + existingSorted, cause);
	}

	/** Waits for the server to answer the request. */
	protected <T> T waitForReceive(KafkaFuture<T> future) {
		try {
			return future.get(getConfig().getWaitTimeout(), TimeUnit.MILLISECONDS);
		} catch (ExecutionException | TimeoutException | RuntimeException exception) {
			throw getExceptionWaitingFailed(exception);
		} catch (InterruptedException exception) {
			// Restore the interrupt flag:
			Thread.currentThread().interrupt();
			throw getExceptionWaitingFailed(exception);
		}
	}

	private RuntimeException getExceptionWaitingFailed(Exception exception) {
		String message =
			"Failed to retrieve the list of existing topics for Kafka client '" + _commonClientConfig.getName() + "'.";
		return new RuntimeException(message, exception);
	}

}
