/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.annotation.InApp;
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
 * {@link ViewAction} that writes the value it processes to a named channel, or empties the channel.
 *
 * <p>
 * Example: delete the selected object and clear the selection:
 * </p>
 *
 * <pre>
 * &lt;delete-object/&gt;
 * &lt;write-channel name="selection" clear="true"/&gt;
 * </pre>
 *
 * <p>
 * A form displaying the channel value vetoes the write while it holds unsaved changes. The action
 * then asks the user how to proceed and continues the chain with the answer: after saving or
 * discarding those changes the write is retried, on cancel the channel keeps its value. Either way
 * the chain runs on, so the actions following the write (e.g. closing the dialog whose command
 * wrote the channel) still happen.
 * </p>
 */
@InApp
public class WriteChannelAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link WriteChannelAction}.
	 */
	@TagName("write-channel")
	public interface Config extends PolymorphicConfiguration<WriteChannelAction>, WithValueArgument {

		@Override
		@ClassDefault(WriteChannelAction.class)
		Class<? extends WriteChannelAction> getImplementationClass();

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #isClear()}. */
		String CLEAR = "clear";

		/**
		 * Name of the channel to write to.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * Whether the channel is emptied instead of receiving a value.
		 *
		 * <p>
		 * A clearing action writes nothing of the arguments of the chain, so it selects none with
		 * {@link #getValue() value}.
		 * </p>
		 */
		@Name(CLEAR)
		boolean isClear();
	}

	private final String _channelName;

	private final boolean _clear;

	/**
	 * Creates a new {@link WriteChannelAction}.
	 */
	@CalledByReflection
	public WriteChannelAction(InstantiationContext context, Config config) {
		_channelName = config.getName();
		_clear = config.isClear();
		String value = config.getValue();
		if (_clear && value != null && !value.isBlank()) {
			context.error("A <write-channel> either clears its channel ('" + Config.CLEAR
				+ "') or writes an argument ('" + WithValueArgument.VALUE + "'), not both (at " + config.location()
				+ ").");
		}
	}

	@Override
	public void execute(ReactContext context, Object value, Continuation continuation) {
		Object input = _clear ? null : value;
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
