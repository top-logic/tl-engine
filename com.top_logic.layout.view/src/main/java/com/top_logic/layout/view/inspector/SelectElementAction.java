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
import com.top_logic.layout.react.window.ElementPicker;
import com.top_logic.layout.react.window.PickKind;
import com.top_logic.layout.react.window.PickResult;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewAction;

/**
 * Lets the user click an element in the inspected window and puts what was hit onto the inspector's
 * node channel.
 *
 * <p>
 * The inspected window is the one that opened this side-window. The click is reported back
 * asynchronously, so the action does not return the inspected node: it writes the channel itself,
 * from the callback the picker runs once the user has clicked. The inspector window is brought back
 * to the front then, since the user was looking at the application window to click in it.
 * </p>
 *
 * <p>
 * App-specific action, referenced by {@code class=} in the inspector view rather than claiming a
 * global {@code @TagName}.
 * </p>
 *
 * @implNote The callback runs on the picked window's request thread with this window's sub-session
 *           installed, which is what lets it update this window's channel directly - see
 *           {@link ElementPicker}.
 */
public class SelectElementAction implements ViewAction {

	/**
	 * Configuration for {@link SelectElementAction}.
	 */
	public interface Config extends PolymorphicConfiguration<SelectElementAction> {

		/** Configuration name for {@link #getNode()}. */
		String NODE = "node";

		@Override
		@ClassDefault(SelectElementAction.class)
		Class<? extends SelectElementAction> getImplementationClass();

		/**
		 * The channel receiving the {@link InspectedNode} of the picked element.
		 */
		@Name(NODE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getNode();
	}

	private final ChannelRef _nodeRef;

	/**
	 * Creates a new {@link SelectElementAction} from configuration.
	 */
	@CalledByReflection
	public SelectElementAction(InstantiationContext context, Config config) {
		_nodeRef = config.getNode();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext viewContext)) {
			return input;
		}
		String inspectedWindow = context.getOpenerWindowName();
		ReactWindowRegistry registry = context.getWindowRegistry();
		if (inspectedWindow == null || registry == null) {
			ViewMessages.info(context, I18NConstants.ERROR_NO_INSPECTED_WINDOW);
			return input;
		}

		ViewChannel nodeChannel = viewContext.resolveChannel(_nodeRef);
		String inspectorWindow = context.getWindowName();
		String token = ElementPicker.start(context, inspectedWindow, PickKind.CONTROL, result -> {
			PickResult.ControlPicked picked = (PickResult.ControlPicked) result;
			InspectedNode node = InspectorAccess.inspectControl(picked.windowName(),
				picked.queue().getRootControl(), picked.control());
			if (node == null) {
				ViewMessages.info(context, I18NConstants.ERROR_NOT_ADDRESSABLE);
			} else {
				nodeChannel.set(node);
			}
			registry.focusWindow(inspectorWindow);
		});
		if (token == null) {
			ViewMessages.info(context, I18NConstants.ERROR_NO_INSPECTED_WINDOW);
		}
		return input;
	}

}
