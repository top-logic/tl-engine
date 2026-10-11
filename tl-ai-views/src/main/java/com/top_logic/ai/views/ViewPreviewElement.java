/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoadError;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.DisabledIf;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.ExprFormat;

/**
 * {@code <view-preview>}: one view of a change set, built by the engine from the text the change
 * set holds and shown with the real data - before anything is written.
 *
 * <p>
 * A preview changes nothing: its commands stay on the toolbar but are disabled, and the actions of
 * rows, inputs and forms are left out. The view gets channels of its own.
 * </p>
 *
 * <pre>
 * &lt;view-preview change="selectedChange" side="AFTER"/&gt;
 * </pre>
 */
public class ViewPreviewElement implements UIElement {

	/** Which text of the change is shown. */
	public enum Side {
		/** The view as the change found it. */
		BEFORE,
		/** The view as the change makes it. */
		AFTER;
	}

	/** Configuration of {@link ViewPreviewElement}. */
	@TagName("view-preview")
	public interface Config extends UIElement.Config {

		@Override
		@Hidden
		@ClassDefault(ViewPreviewElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Channel holding the change, a {@code tl.ai.views:ViewChange}. */
		@Name("change")
		@Format(ChannelRefFormat.class)
		@Mandatory
		ChannelRef getChange();

		/** Which text of the change is shown. */
		@Name("side")
		Side getSide();
	}

	/** Properties that run something when the user acts; a preview leaves them out. */
	private static final Set<String> ACTIONS = Set.of("row-commands", "on-activate", "on-remove", "on-move",
		"on-resize", "on-create", "on-submit", "on-reorder", "on-dismiss", "on-link", "update", "drag", "drops", "action", "save-action", "cancel-action",
		"row-edit", "link", "remove", "new-element");

	/** Switches that offer editing; a preview turns them off. */
	private static final Set<String> EDITING = Set.of("mode-switch", "initial-edit-mode", "editable");

	private static final String PREVIEW_REASON =
		"x -> #(\"Preview - nothing is changed here.\"@en, \"Vorschau - hier wird nichts geändert.\"@de)";

	private final Config _config;

	/** Creates a {@link ViewPreviewElement}. */
	@CalledByReflection
	public ViewPreviewElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel change = context.resolveChannel(_config.getChange());
		Preview preview = new Preview(context, build(context, change.get()));
		ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> preview.show(build(context, newValue));
		change.addListener(listener);
		preview.addCleanupAction(() -> change.removeListener(listener));
		return preview;
	}

	private ReactControl build(ViewContext context, Object value) {
		if (!(value instanceof TLObject change)) {
			return null;
		}
		String path = String.valueOf(change.tValueByName("name"));
		Object text = change.tValueByName(_config.getSide() == Side.BEFORE ? "before" : "after");
		try {
			if (text == null) {
				throw new ConfigurationException(_config.getSide() == Side.BEFORE ? path + " did not exist before."
					: "The change deletes " + path + ".");
			}
			ViewElement.Config view =
				ViewLoader.parseConfig(ViewFiles.sourcesWith(ViewFiles.resource(path), text.toString()));
			disarm(view, new IdentityHashMap<>());
			DefaultInstantiationContext instantiation = new DefaultInstantiationContext(ViewPreviewElement.class);
			UIElement element = instantiation.getInstance(view);
			instantiation.checkErrors();
			return (ReactControl) element.createControl(context.withIsolatedChannels());
		} catch (ConfigurationException | RuntimeException ex) {
			Logger.info("Preview of " + path + " failed: " + ex.getMessage(), ViewPreviewElement.class);
			return ViewLoadError.createControl(context, path,
				ex instanceof ConfigurationException config ? config : new ConfigurationException(ex.getMessage()));
		}
	}

	/** Shows one built view and replaces it by the next. */
	private static final class Preview extends ReactCompositeControl {

		/** Shows nothing while {@code shown} is <code>null</code>: no change is selected. */
		Preview(ViewContext context, ReactControl shown) {
			super(context, null, "TLStack", shown == null ? List.of() : List.of(shown));
		}

		void show(ReactControl next) {
			List<ReactControl> old = List.copyOf(getChildren());
			replaceChildren(next == null ? List.of() : List.of(next));
			old.forEach(ContentControls::retire);
		}
	}

	/** Takes out of a view, in place, everything that would change data. */
	private static void disarm(Object value, Map<Object, Boolean> seen) {
		if (value == null || seen.put(value, Boolean.TRUE) != null) {
			return;
		}
		if (value instanceof Collection<?> collection) {
			collection.forEach(entry -> disarm(entry, seen));
		} else if (value instanceof Map<?, ?> map) {
			map.values().forEach(entry -> disarm(entry, seen));
		} else if (value instanceof ViewCommand.Config command) {
			disable(command);
		} else if (value instanceof ConfigurationItem item) {
			for (PropertyDescriptor property : item.descriptor().getProperties()) {
				String name = property.getPropertyName();
				if (ACTIONS.contains(name)) {
					if (item.valueSet(property)) {
						item.reset(property);
					}
				} else if (EDITING.contains(name) && property.getType() == boolean.class) {
					item.update(property, Boolean.FALSE);
				} else if (!property.isAbstract()) {
					switch (property.kind()) {
						case ITEM, LIST, ARRAY, MAP, COMPLEX -> disarm(item.value(property), seen);
						default -> {
							// A plain value runs nothing.
						}
					}
				}
			}
		}
	}

	/** Keeps a command on the toolbar but disabled, saying why, and drops what it would do. */
	private static void disable(ViewCommand.Config command) {
		PropertyDescriptor rules = command.descriptor().getProperty("executability");
		if (rules == null) {
			return;
		}
		DisabledIf.Config disabled = TypedConfiguration.newConfigItem(DisabledIf.Config.class);
		try {
			disabled.update(disabled.descriptor().getProperty(DisabledIf.Config.EXPR),
				ExprFormat.INSTANCE.getValue(DisabledIf.Config.EXPR, PREVIEW_REASON));
		} catch (ConfigurationException ex) {
			throw new IllegalStateException(ex);
		}
		command.update(rules, List.of(disabled));
		PropertyDescriptor actions = command.descriptor().getProperty("actions");
		if (actions != null && command.valueSet(actions)) {
			command.reset(actions);
		}
	}
}
