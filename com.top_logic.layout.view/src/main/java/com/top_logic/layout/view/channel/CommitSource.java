/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.listener.Registration;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link ChannelSource} publishing the commit number of every commit that creates, changes or
 * deletes an object of the configured {@link Config#getTypes() types}, whichever thread committed
 * it.
 *
 * <p>
 * A window learns of a commit made outside its own requests - by a background job, a scheduled
 * task, another session - only when the model events of the window are synthesized, which happens
 * on the next request of the window or on the heartbeat of its event stream. Every delivery of a
 * source channel synthesizes them as well, so a view declaring a channel fed by this source follows
 * such commits within the {@link SourceChannelConfig#getUpdateInterval() update interval} of the
 * channel: its tables, forms and derived channels observing the changed objects or their types
 * are updated as if the window had made the change itself. The value of the channel, the number of
 * the last relevant commit, is rarely read.
 * </p>
 *
 * <pre>
 * &lt;source-channel name="commits" update-interval="1000"&gt;
 *   &lt;source class="com.top_logic.layout.view.channel.CommitSource" types="my.module:Job"/&gt;
 * &lt;/source-channel&gt;
 * </pre>
 */
public class CommitSource extends AbstractConfiguredInstance<CommitSource.Config<?>> implements ChannelSource {

	/**
	 * Configuration of a {@link CommitSource}.
	 */
	public interface Config<I extends CommitSource> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration name of {@link #getTypes()}.
		 */
		String TYPES = "types";

		/**
		 * The types whose objects are watched, specializations included.
		 *
		 * <p>
		 * Empty publishes every commit.
		 * </p>
		 */
		@Name(TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getTypes();

	}

	/**
	 * Creates a {@link CommitSource} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CommitSource(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Registration subscribe(ViewContext context, ChannelSink sink) {
		Set<TLStructuredType> types = ObservedTypes.resolve(getConfig().getTypes());
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		UpdateListener listener = (sender, event) -> {
			if (isRelevant(types, event)) {
				sink.publish(Long.valueOf(event.getCommitNumber()));
			}
		};
		kb.addUpdateListener(listener);
		sink.publish(Long.valueOf(kb.getHistoryManager().getLastRevision()));
		return Registration.onDispose(() -> kb.removeUpdateListener(listener));
	}

	/**
	 * Whether the given commit touches an object of one of the given types.
	 *
	 * @param types
	 *        The watched types, empty for all.
	 * @param event
	 *        The commit.
	 */
	static boolean isRelevant(Set<TLStructuredType> types, UpdateEvent event) {
		if (types.isEmpty()) {
			return true;
		}
		return touches(types, event.getCreatedObjects().values())
			|| touches(types, event.getUpdatedObjects().values())
			|| touches(types, event.getCachedDeletedObjects());
	}

	private static boolean touches(Set<TLStructuredType> types, Iterable<KnowledgeItem> items) {
		for (KnowledgeItem item : items) {
			TLStructuredType type;
			try {
				TLObject object = item.getWrapper();
				type = object == null ? null : object.tType();
			} catch (RuntimeException ex) {
				// An object whose type cannot be read any more might be one of the watched ones.
				return true;
			}
			if (type == null) {
				continue;
			}
			for (TLStructuredType watched : types) {
				if (TLModelUtil.isCompatibleType(watched, type)) {
					return true;
				}
			}
		}
		return false;
	}

}
