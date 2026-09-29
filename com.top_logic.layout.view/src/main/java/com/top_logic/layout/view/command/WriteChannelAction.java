/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DirtyConfirmDialogControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * {@link ViewAction} that writes the input value to a named channel.
 *
 * <p>
 * Passes the input through as output so the chain can continue.
 * </p>
 *
 * <p>
 * A form displaying the channel value vetoes the write while it holds unsaved changes. The action
 * then asks the user how to proceed and continues the chain with the answer: after saving or
 * discarding those changes the write is retried, on cancel the channel keeps its value. Either way
 * the chain runs on, so the actions following the write (e.g. closing the dialog whose command
 * wrote the channel) still happen.
 * </p>
 */
public class WriteChannelAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link WriteChannelAction}.
	 */
	@TagName("write-channel")
	public interface Config extends PolymorphicConfiguration<WriteChannelAction> {

		@Override
		@ClassDefault(WriteChannelAction.class)
		Class<? extends WriteChannelAction> getImplementationClass();

		/**
		 * Name of the channel to write to.
		 */
		@Name("name")
		@Mandatory
		String getName();
	}

	private final String _channelName;

	/**
	 * Creates a new {@link WriteChannelAction}.
	 */
	@CalledByReflection
	public WriteChannelAction(InstantiationContext context, Config config) {
		_channelName = config.getName();
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		ViewChannel channel = resolveChannel(context);
		if (channel == null) {
			continuation.resume(input);
			return;
		}

		try {
			channel.set(input);
		} catch (ChannelVetoException veto) {
			DialogManager dialogManager = context.getDialogManager();
			if (dialogManager == null) {
				// No dialog possible (e.g. headless) - proceed rather than dead-ending the chain.
				continuation.resume(input);
				return;
			}
			DirtyConfirmDialogControl.openDialog(context, dialogManager, veto.getDirtyHandlers(),
				() -> {
					// The vetoing forms are clean now, so the retried write goes through.
					veto.getContinuation().run();
					continuation.resume(input);
				},
				() -> {
					// Cancelled: the channel keeps its value, so revert what was already
					// optimistically shown as changed.
					Runnable rollback = veto.getRollback();
					if (rollback != null) {
						rollback.run();
					}
					continuation.resume(input);
				});
			return;
		}
		continuation.resume(input);
	}

	private ViewChannel resolveChannel(ReactContext context) {
		if (context instanceof ViewContext viewContext && viewContext.hasChannel(_channelName)) {
			return viewContext.resolveChannel(new ChannelRef(_channelName));
		}
		return null;
	}
}
