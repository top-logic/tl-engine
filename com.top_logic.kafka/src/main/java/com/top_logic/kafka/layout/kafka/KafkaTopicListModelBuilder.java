/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.kafka.interceptor.TopicViewerInterceptor;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;
import com.top_logic.layout.tree.component.AbstractTreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Build up the list of topics relevant to the user.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class KafkaTopicListModelBuilder extends AbstractTreeModelBuilder<Object>
		implements ConfiguredInstance<PolymorphicConfiguration<? extends KafkaTopicListModelBuilder>> {

	private final UnitWrapper _root;

	private PolymorphicConfiguration<? extends KafkaTopicListModelBuilder> _config;

	/**
	 * Creates a {@link KafkaTopicListModelBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public KafkaTopicListModelBuilder(InstantiationContext context,
			PolymorphicConfiguration<? extends KafkaTopicListModelBuilder> config) {
		_config = config;
		_root = UnitWrapper.getPiece0();
	}

	@Override
	public PolymorphicConfiguration<? extends KafkaTopicListModelBuilder> getConfig() {
		return _config;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return (aModel == _root) || (aModel == null);
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return (node == _root) || (node instanceof KafkaTopic) || (node instanceof KafkaMessage);
	}

	@Override
	public Iterator<? extends Object> getChildIterator(Object node) {
		return getChildren(node).iterator();
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, Object node) {
		KafkaTopic<?> topic = null;

		if (node instanceof KafkaMessage) {
			topic = ((KafkaMessage) node).getTopic();
		}
		if (node instanceof KafkaTopic<?>) {
			topic = (KafkaTopic<?>) node;
		}

		return (topic != null) ? _root : null;
	}

	@Override
	public Collection<? extends Object> getParents(LayoutComponent contextComponent, Object node) {
		if (node instanceof KafkaMessage) {
			return Collections.singleton(((KafkaMessage) node).getTopic());
		} else if (node instanceof KafkaTopic) {
			return Collections.singleton(_root);
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Gets the children to be displayed as children of the given object.
	 *
	 * @param node
	 *        The node of which the children are requested.
	 * @return The child wrappers of the given node.
	 */
	protected Collection<? extends Object> getChildren(Object node) {
		if (node == _root) {
			return TopicViewerInterceptor.getTopics();
		} else if (node instanceof KafkaTopic) {
			List<KafkaMessage> messages = ((KafkaTopic<?>) node).getMessages();

			Collections.reverse(messages);

			return messages;
		}

		return Collections.emptyList();
	}
}
