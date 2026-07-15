/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ViewCommand} that pushes a new {@link TileFrame} onto the enclosing {@link TileStackScope
 * tile stack}.
 *
 * <p>
 * Resolves the target stack from the {@link ViewContext} - the command must be
 * executed from within a frame that was mounted by a {@link TileStackElement &lt;tile-stack&gt;}.
 * Captured at execution time: the command's {@link Config#getInput() input} (mapped onto a named
 * channel of the new frame, if {@link Config#getBindInputTo() bind-input-to} is set) and the
 * current value of every channel referenced by a {@code <bind>}.
 * </p>
 *
 * <p>
 * Example - drilling into a product detail view from a button next to a product table:
 * </p>
 *
 * <pre>
 * &lt;button&gt;
 *   &lt;action class="com.top_logic.layout.view.tiles.NavigatePushCommand"
 *     view="products/detail.view.xml"
 *     input="selectedProduct"
 *     bind-input-to="product"
 *     frame-label="product.detail.crumb"/&gt;
 *   &lt;label&gt;&lt;en&gt;Open&lt;/en&gt;&lt;/label&gt;
 * &lt;/button&gt;
 * </pre>
 *
 * @implNote Resolves the target stack via {@link ViewContext#getTileStackScope()}.
 */
public class NavigatePushCommand implements ViewCommand {

	/**
	 * Configuration for {@link NavigatePushCommand}.
	 */
	@TagName("navigate-push")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(NavigatePushCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Configuration name for {@link #getView()}. */
		String VIEW = "view";

		/** Configuration name for {@link #getBindings()}. */
		String BINDINGS = "bindings";

		/** Configuration name for {@link #getBindInputTo()}. */
		String BIND_INPUT_TO = "bind-input-to";

		/** Configuration name for {@link #getFrameLabel()}. */
		String FRAME_LABEL = "frame-label";

		/**
		 * Path of the view file to mount as the new frame, relative to {@code /WEB-INF/views/}.
		 */
		@Name(VIEW)
		@Mandatory
		String getView();

		/**
		 * Channel bindings from the calling scope to the new frame's channels.
		 *
		 * <p>
		 * Each binding captures the <em>current value</em> of the referenced parent channel at
		 * push time and stores it under {@link ChannelBindingConfig#getChannel()} in the pushed
		 * {@link TileFrame#getParams() frame params}. The mounted view's channel of that name
		 * receives the captured value as its initial state.
		 * </p>
		 */
		@Name(BINDINGS)
		@DefaultContainer
		List<ChannelBindingConfig> getBindings();

		/**
		 * Name of the channel in the pushed view that should receive the command's
		 * {@link #getInput() input} value.
		 *
		 * <p>
		 * Convenience for the common case where the drill-down target is a single business object
		 * (e.g. the row selected in a table). If {@code null}, the input is not propagated - use
		 * explicit {@code <bind>}s instead.
		 * </p>
		 */
		@Name(BIND_INPUT_TO)
		@Nullable
		String getBindInputTo();

		/**
		 * Provider for the pushed frame's breadcrumb label.
		 *
		 * <p>
		 * Resolved exactly once at push time; the resulting {@link ResKey} is captured as the
		 * pushed frame's label. Use {@link StaticTileLabel &lt;static&gt;} for fixed
		 * text, {@link ScriptedTileLabel &lt;scripted&gt;} for a label derived from caller-scope
		 * channels at the push moment.
		 * </p>
		 *
		 * @implNote The resolved {@link ResKey} is captured into {@link TileFrame#getLabel()}.
		 */
		@Name(FRAME_LABEL)
		@Nullable
		PolymorphicConfiguration<? extends TileLabelProvider> getFrameLabel();
	}

	private final String _view;

	private final List<ChannelBindingConfig> _bindings;

	private final String _bindInputTo;

	private final TileLabelProvider _frameLabel;

	/**
	 * Creates a new {@link NavigatePushCommand}.
	 */
	@CalledByReflection
	public NavigatePushCommand(InstantiationContext context, Config config) {
		_view = config.getView();
		_bindings = config.getBindings();
		_bindInputTo = config.getBindInputTo();
		PolymorphicConfiguration<? extends TileLabelProvider> labelConfig = config.getFrameLabel();
		_frameLabel = labelConfig != null ? context.getInstance(labelConfig) : null;
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext viewContext)) {
			throw new IllegalStateException(
				"<navigate-push> requires a ViewContext, got " + context.getClass().getName());
		}
		TileStackScope scope = viewContext.getTileStackScope();
		if (scope == null) {
			throw new IllegalStateException(
				"<navigate-push> executed outside of any enclosing <tile-stack>.");
		}

		Map<String, Object> params = new LinkedHashMap<>();
		if (_bindInputTo != null && input != null) {
			params.put(_bindInputTo, input);
		}
		for (ChannelBindingConfig binding : _bindings) {
			String parentName = binding.getTo().getChannelName();
			if (!viewContext.hasChannel(parentName)) {
				Logger.warn("Channel '" + parentName + "' not found in caller scope, skipping binding.",
					NavigatePushCommand.class);
				continue;
			}
			ViewChannel parentChannel = viewContext.resolveChannel(binding.getTo());
			params.put(binding.getChannel(), parentChannel.get());
		}

		ResKey label = _frameLabel != null ? _frameLabel.compute(viewContext) : null;
		scope.push(_view, label, Collections.unmodifiableMap(params));
		return HandlerResult.DEFAULT_RESULT;
	}
}
