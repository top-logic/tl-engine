/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.inspector;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewAction;

/**
 * Takes the inspected node anew, either at its own address or at the address of the element
 * enclosing it.
 *
 * <p>
 * Both moves are the same step - resolve an address in the inspected window and project what sits
 * there now - and differ only in the address they start from, which the {@link Mode} names. The
 * projection is a snapshot, so taking it again is what shows a state that has changed meanwhile.
 * </p>
 *
 * <p>
 * App-specific action, referenced by {@code class=} in the inspector view rather than claiming a
 * global {@code @TagName}.
 * </p>
 */
public class InspectNodeAction implements ViewAction {

	/**
	 * Which element a {@link InspectNodeAction} inspects.
	 */
	public enum Mode {
		/** The element enclosing the inspected one. */
		PARENT,

		/** The inspected element itself, as it is now. */
		REFRESH;
	}

	/**
	 * Configuration for {@link InspectNodeAction}.
	 */
	public interface Config extends PolymorphicConfiguration<InspectNodeAction> {

		/** Configuration name for {@link #getNode()}. */
		String NODE = "node";

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		@Override
		@ClassDefault(InspectNodeAction.class)
		Class<? extends InspectNodeAction> getImplementationClass();

		/**
		 * The channel holding the {@link InspectedNode}, read and written by this action.
		 */
		@Name(NODE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getNode();

		/**
		 * Which element to inspect.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();
	}

	private final ChannelRef _nodeRef;

	private final Mode _mode;

	/**
	 * Creates a new {@link InspectNodeAction} from configuration.
	 */
	@CalledByReflection
	public InspectNodeAction(InstantiationContext context, Config config) {
		_nodeRef = config.getNode();
		_mode = config.getMode();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext viewContext)) {
			return input;
		}
		ViewChannel nodeChannel = viewContext.resolveChannel(_nodeRef);
		if (!(nodeChannel.get() instanceof InspectedNode current)) {
			ViewMessages.info(context, I18NConstants.ERROR_NO_NODE);
			return input;
		}

		String address = _mode == Mode.PARENT ? current.parentAddress() : current.address();
		if (address == null) {
			ViewMessages.info(context, I18NConstants.ERROR_NO_PARENT);
			return input;
		}

		InspectedNode inspected = InspectorAccess.inspectAddress(context, current.windowName(), address);
		if (inspected == null) {
			ViewMessages.info(context, I18NConstants.ERROR_NODE_GONE);
			return input;
		}
		nodeChannel.set(inspected);
		return input;
	}

}
