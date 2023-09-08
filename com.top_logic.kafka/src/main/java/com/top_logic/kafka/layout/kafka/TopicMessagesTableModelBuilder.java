/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.kafka.interceptor.TopicViewerInterceptor;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link ListModelBuilder} which displays the contents of the configured topic.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class TopicMessagesTableModelBuilder extends AbstractConfiguredInstance<TopicMessagesTableModelBuilder.Config>
		implements ListModelBuilder {

	/**
	 * Typed configuration interface definition for {@link TopicMessagesTableModelBuilder}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	public interface Config extends PolymorphicConfiguration<TopicMessagesTableModelBuilder> {

		/**
		 * the name of the topic to display the contents for
		 */
		@Mandatory
		String getTopic();
	}

	/**
	 * Create a {@link TopicMessagesTableModelBuilder}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public TopicMessagesTableModelBuilder(final InstantiationContext context, final Config config) {
		super(context, config);
	}

	@Override
	public Collection<?> getModel(final Object model, final LayoutComponent component) {
		final KafkaTopic<?> topic = getTopic();

		if (topic != null) {
			return topic.getMessages();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean supportsModel(final Object model, final LayoutComponent component) {
		return true;
	}

	@Override
	public boolean supportsListElement(final LayoutComponent component, final Object element) {
		if (element instanceof KafkaMessage) {
			return ((KafkaMessage) element).getTopic() == getTopic();
		}

		return false;
	}

	@Override
	public Object retrieveModelFromListElement(final LayoutComponent component, final Object element) {
		return null;
	}

	/**
	 * the configured {@link KafkaTopic} or {@code null} if it could not be resolved
	 */
	private KafkaTopic<?> getTopic() {
		final String topicName = getConfig().getTopic();

		for (final KafkaTopic<?> topic : TopicViewerInterceptor.getTopics()) {
			if (topic.getName().equals(topicName)) {
				return topic;
			}
		}

		return null;
	}
}
