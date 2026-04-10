/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.configedit.ConfigEditorControl;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * A {@link UIElement} that renders a config-editor form for the selected {@link DesignTreeNode}.
 *
 * <p>
 * Reads a {@link DesignTreeNode} from an input channel and renders a {@link ConfigEditorControl}
 * for its configuration properties. When the selection changes, the editor is rebuilt to reflect
 * the newly selected node.
 * </p>
 */
public class ConfigEditorElement implements UIElement {

	/**
	 * Configuration for {@link ConfigEditorElement}.
	 */
	@TagName("config-editor")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(ConfigEditorElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Reference to the channel providing the currently selected {@link DesignTreeNode}.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	/**
	 * A {@link ReactCompositeControl} wrapper that allows replacing its single child.
	 *
	 * <p>
	 * Used by {@link ConfigEditorElement} to swap in a new {@link ConfigEditorControl} when the
	 * selected {@link DesignTreeNode} changes.
	 * </p>
	 */
	private static final class EditorWrapperControl extends ReactCompositeControl {

		EditorWrapperControl(ReactContext context) {
			super(context, null, "TLStack");
		}

		/**
		 * Replaces the current child with the given one.
		 *
		 * <p>
		 * Cleans up existing children, then adds the new child.
		 * </p>
		 *
		 * @param child
		 *        The new child control to display, or {@code null} to clear.
		 */
		void setChild(ReactControl child) {
			List<ReactControl> children = getChildren();
			for (ReactControl old : children) {
				old.cleanupTree();
			}
			children.clear();
			if (child != null) {
				addChild(child);
			} else {
				putState("children", children);
			}
		}
	}

	private final Config _config;

	/**
	 * Creates a new {@link ConfigEditorElement} from configuration.
	 */
	@CalledByReflection
	public ConfigEditorElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel inputChannel = context.resolveChannel(_config.getInput());

		EditorWrapperControl wrapper = new EditorWrapperControl(context);

		// Build initial editor if a node is already selected.
		Object initialValue = inputChannel.get();
		if (initialValue instanceof DesignTreeNode node && !node.isVirtual()) {
			ConfigEditorControl editor = new ConfigEditorControl(context, node.getConfigItem(),
				Collections.emptySet(), true);
			installDirtyTracking(node);
			wrapper.addChild(editor);
		}

		// Listen for selection changes and rebuild the editor.
		inputChannel.addListener((sender, oldValue, newValue) -> {
			if (newValue instanceof DesignTreeNode node && !node.isVirtual()) {
				ConfigEditorControl editor = new ConfigEditorControl(context, node.getConfigItem(),
					Collections.emptySet(), true);
				installDirtyTracking(node);
				wrapper.setChild(editor);
			} else {
				wrapper.setChild(null);
			}
		});

		return wrapper;
	}

	/**
	 * Registers a {@link com.top_logic.basic.config.ConfigurationListener} on all properties of
	 * the node's config that marks the node as dirty on any change.
	 */
	private static void installDirtyTracking(DesignTreeNode node) {
		ConfigurationItem config = node.getConfigItem();
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			config.addConfigurationListener(property, change -> node.markDirty());
		}
	}

}
