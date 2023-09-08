/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.kafka.interceptor.TopicViewerInterceptor;
import com.top_logic.kafka.layout.sensors.ProgressTableComponent;
import com.top_logic.kafka.layout.sensors.ProgressTableComponent.TableComponentValueUpdater;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.EditableRowTableModel;

/**
 * {@link TableComponentValueUpdater} implementation which resolves new messages for the configured
 * topic only.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class TopicMessagesTableUpdater extends AbstractConfiguredInstance<TopicMessagesTableUpdater.Config>
		implements TableComponentValueUpdater {

	/**
	 * Typed configuration interface definition for {@link TopicMessagesTableUpdater}.
	 * 
	 * @author <a href="mailto:wta@top-logic.com">wta</a>
	 */
	public interface Config extends PolymorphicConfiguration<TopicMessagesTableUpdater> {

		/**
		 * the name of the topic to display the contents for
		 */
		@Mandatory
		String getTopic();
	}

	/**
	 * Create a {@link TopicMessagesTableUpdater}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public TopicMessagesTableUpdater(final InstantiationContext context, final Config config) {
		super(context, config);
	}

	@Override
	public void update(final ProgressTableComponent component) {
		final KafkaTopic<?> topic = getTopic();

		if (topic != null) {
			final EditableRowTableModel table = component.getTableModel();

			for (final KafkaMessage msg : topic.getMessages()) {
				if (table.containsRowObject(msg)) {
					final int row = table.getRowOfObject(msg);
					if (TableModel.NO_ROW != row) {
						table.updateRows(row, row);
					}
				} else {
					table.addRowObject(msg);
				}
			}
		}
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
