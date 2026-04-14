/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.overlay.ReactMenuControl;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.channel.ChannelFactory;
import com.top_logic.layout.view.command.ContextMenuOpener;
import com.top_logic.layout.view.command.ContextMenuOpener.MenuRenderer;

/**
 * The mandatory root element of every {@code .view.xml} file.
 *
 * <p>
 * Establishes the scope boundary for a view. In the future, this is where channel declarations
 * and view-level configuration will be defined.
 * </p>
 */
public class ViewElement implements UIElement {

	/**
	 * Configuration for {@link ViewElement}.
	 */
	@TagName("view")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ViewElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getChannels()}. */
		String CHANNELS = "channels";

		/**
		 * Channel declarations for this view.
		 *
		 * <p>
		 * Channels are named reactive values that can be read and written by UI elements within
		 * this view.
		 * </p>
		 */
		@Name(CHANNELS)
		List<ChannelConfig> getChannels();

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/**
		 * The root content element of this view.
		 *
		 * <p>
		 * Exactly one element is expected. The list type enables {@code @TagName} resolution so that
		 * short element names (e.g. {@code <app-shell>}) can be used directly inside {@code <view>}.
		 * </p>
		 */
		@Name(CONTENT)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getContent();
	}

	private final List<Map.Entry<String, ChannelFactory>> _channelEntries;

	private final List<UIElement> _content;

	/**
	 * Creates a new {@link ViewElement} from configuration.
	 */
	@CalledByReflection
	public ViewElement(InstantiationContext context, Config config) {
		_channelEntries = config.getChannels().stream()
			.map(cc -> Map.entry(cc.getName(), context.getInstance(cc)))
			.collect(Collectors.toList());
		List<PolymorphicConfiguration<? extends UIElement>> contentList = config.getContent();
		if (contentList.isEmpty()) {
			context.error("View element must have at least one content element.");
			_content = List.of();
		} else {
			_content = contentList.stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// Phase 2a: Create and register channels via factories.
		for (Map.Entry<String, ChannelFactory> entry : _channelEntries) {
			String name = entry.getKey();
			if (context.hasChannel(name)) {
				// Pre-bound by parent via <view-ref> binding. Skip local instantiation.
				continue;
			}
			ChannelFactory factory = entry.getValue();
			context.registerChannel(name, factory.createChannel(context));
		}

		// Instantiate exactly one ContextMenuOpener per top-level frame and bind it onto the
		// context so that descendant elements can reach it via context.getContextMenuOpener().
		// The accompanying ReactMenuControl is attached as a sibling of the content so it
		// participates in the control tree and gets rendered.
		// Mutable handler holders: rewired on each opener.open() via MenuRenderer.show() below.
		// A single ReactMenuControl is constructed once, but its select/close handlers must
		// vary per invocation. Task 6 will replace these with real setters on the control.
		Consumer<String>[] selectHolder = new Consumer[] { itemId -> { /* no-op until bound */ } };
		Runnable[] closeHolder = new Runnable[] { () -> { /* no-op until bound */ } };
		ReactMenuControl menuControl = new ReactMenuControl(context, null, List.of(),
			itemId -> selectHolder[0].accept(itemId),
			() -> closeHolder[0].run());
		MenuRenderer renderer = new MenuRenderer() {
			@Override
			public void show(int x, int y, List<MenuEntry> items, Consumer<String> selectHandler,
					Runnable closeHandler) {
				menuControl.updateItems(items);
				selectHolder[0] = selectHandler;
				closeHolder[0] = closeHandler;
				// TODO: Task 6 - position the menu at the (x, y) pixel coordinates instead of
				// using anchor-based positioning. For now, clear the anchor as a placeholder.
				menuControl.setAnchorId(null);
				menuControl.open();
			}

			@Override
			public void hide() {
				menuControl.close();
			}
		};
		ContextMenuOpener opener = new ContextMenuOpener(renderer);
		opener.bindReactContext(() -> context);
		ViewContext frameContext = context.withContextMenuOpener(opener);

		List<ReactControl> children = new ArrayList<>();
		for (UIElement element : _content) {
			children.add((ReactControl) element.createControl(frameContext));
		}
		children.add(menuControl);
		return new ReactStackControl(frameContext, children);
	}
}
