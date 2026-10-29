/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.sched.DaemonThreadFactory;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ChannelSink;
import com.top_logic.layout.view.channel.ChannelSource;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;

/**
 * {@link com.top_logic.layout.view.channel.ChannelSource} publishing the log of the application
 * of a workspace while it is written: the last lines of the output of the main process of the
 * workspace on the configured {@link Config#getWorkspace() channel}, as one text.
 *
 * <pre>
 * &lt;source-channel name="appLog" update-interval="1000"&gt;
 *   &lt;source class="com.top_logic.factory.project.WorkspaceLogSource" workspace="workspace" tail-lines="500"/&gt;
 * &lt;/source-channel&gt;
 * </pre>
 *
 * <p>
 * Each displayed view follows the log of the workspace its channel holds, and follows another one
 * when the channel changes. A restart of the workspace ends the output of its main process; the
 * next commit changing the workspace - its state - starts following the new output. The delivery
 * ends when the view is no longer displayed.
 * </p>
 *
 * @see WorkspaceLogFollow
 */
public class WorkspaceLogSource extends AbstractConfiguredInstance<WorkspaceLogSource.Config<?>>
		implements ChannelSource {

	/**
	 * Configuration of a {@link WorkspaceLogSource}.
	 */
	public interface Config<I extends WorkspaceLogSource> extends PolymorphicConfiguration<I> {

		/** Configuration name of {@link #getWorkspace()}. */
		String WORKSPACE = "workspace";

		/** Configuration name of {@link #getTailLines()}. */
		String TAIL_LINES = "tail-lines";

		/**
		 * The channel of the view holding the {@code tl.factory:Workspace} whose log is published.
		 */
		@Name(WORKSPACE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getWorkspace();

		/**
		 * The number of the last lines of the log that are published.
		 */
		@Name(TAIL_LINES)
		@IntDefault(500)
		int getTailLines();

	}

	/** Runs the requests to the provider off the threads of the windows and of the commits. */
	private final ExecutorService _executor =
		Executors.newCachedThreadPool(DaemonThreadFactory.daemonThreadFactory());

	/**
	 * Creates a {@link WorkspaceLogSource} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public WorkspaceLogSource(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Registration subscribe(ViewContext context, ChannelSink sink) {
		ViewChannel channel = context.resolveChannel(getConfig().getWorkspace());
		WorkspaceLogFollow follow = new WorkspaceLogFollow(WorkspaceService.getInstance().getProvider(),
			getConfig().getTailLines(), sink, _executor);
		Registration channelRegistration = channel.addListener((sender, oldValue, newValue) -> follow.follow(newValue));
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		UpdateListener commits = (sender, event) -> {
			TLObject workspace = follow.workspace();
			if (workspace == null) {
				return;
			}
			for (KnowledgeItem item : event.getUpdatedObjects().values()) {
				if (item == workspace.tHandle()) {
					follow.refresh(workspace);
					return;
				}
			}
		};
		kb.addUpdateListener(commits);
		follow.follow(channel.get());
		return Registration.onDispose(() -> {
			channelRegistration.dispose();
			kb.removeUpdateListener(commits);
			follow.close();
		});
	}

}
