/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.UICategory;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.model.listen.impl.EventBuilder;
import com.top_logic.model.listen.impl.ModelChangeSummary;

/**
 * Service that lets every browser window follow the commits that reach the listeners of its model
 * scope, whatever thread made them.
 *
 * <p>
 * A window synthesizes its model events when it handles a request of its own. A commit of another
 * thread - a background job, a scheduled task, a request of another session - would only show once
 * the window sends a request or its heartbeat comes. This service listens to every commit of the
 * {@link PersistencyLayer#getKnowledgeBase() application knowledge base} and
 * {@link SSEUpdateQueue#requestModelSync(long) asks} each window whose client is present and whose
 * {@link WindowEntry#isAffectedBy(ModelChangeSummary) model scope is affected} to synthesize and
 * deliver its model events, at most once per {@link Config#getSyncInterval() interval}.
 * </p>
 *
 * <p>
 * The commit is checked on the committing thread against the registrations of the windows only;
 * the events are synthesized on the {@link SchedulerService}.
 * </p>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	SchedulerService.Module.class,
})
@ServiceCategory.Of(UICategory.class)
public class WindowModelEventService extends ConfiguredManagedClass<WindowModelEventService.Config<?>>
		implements UpdateListener {

	/**
	 * Configuration options for {@link WindowModelEventService}.
	 */
	public interface Config<I extends WindowModelEventService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration name for {@link #getSyncInterval()}.
		 */
		String SYNC_INTERVAL = "sync-interval";

		/**
		 * The minimum time between two deliveries of model events to the same window.
		 *
		 * <p>
		 * A window affected by a commit receives its events at once, if it received none within this
		 * interval, and the interval after its last delivery otherwise. All commits made in the
		 * meantime are delivered together.
		 * </p>
		 */
		@Name(SYNC_INTERVAL)
		@Format(MillisFormat.class)
		@LongDefault(500)
		long getSyncInterval();

	}

	private final long _syncInterval;

	private KnowledgeBase _kb;

	/**
	 * Creates a {@link WindowModelEventService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public WindowModelEventService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_syncInterval = config.getSyncInterval();
	}

	@Override
	protected void startUp() {
		super.startUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(this);
	}

	@Override
	protected void shutDown() {
		_kb.removeUpdateListener(this);
		_kb = null;
		super.shutDown();
	}

	@Override
	public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
		try {
			requestModelSync(event);
		} catch (RuntimeException ex) {
			// The commit is done; whatever goes wrong here must not reach the committing thread.
			Logger.error("Failed to inform the browser windows about a commit.", ex, WindowModelEventService.class);
		}
	}

	private void requestModelSync(UpdateEvent event) {
		long now = System.currentTimeMillis();
		List<WindowEntry> observing = new ArrayList<>();
		ReactWindowRegistry.forEachWindow(window -> {
			if (window.getQueue().hasClient(now)) {
				observing.add(window);
			}
		});
		if (observing.isEmpty()) {
			return;
		}

		ModelChangeSummary change = EventBuilder.summarize(GlobalModelEventForwarder.configuredSettings(), event);
		for (WindowEntry window : observing) {
			try {
				if (window.isAffectedBy(change)) {
					window.getQueue().requestModelSync(_syncInterval);
				}
			} catch (RuntimeException ex) {
				Logger.error("Failed to inform window '" + window.getWindowId() + "' about a commit.", ex,
					WindowModelEventService.class);
			}
		}
	}

	/**
	 * The minimum time between two deliveries of model events to the same window, in milliseconds.
	 *
	 * @see Config#getSyncInterval()
	 */
	public long getSyncInterval() {
		return _syncInterval;
	}

	/**
	 * The {@link WindowModelEventService} of the running application.
	 */
	public static WindowModelEventService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link WindowModelEventService}.
	 */
	public static final class Module extends TypedRuntimeModule<WindowModelEventService> {

		/** Singleton instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton.
		}

		@Override
		public Class<WindowModelEventService> getImplementation() {
			return WindowModelEventService.class;
		}

	}

}
