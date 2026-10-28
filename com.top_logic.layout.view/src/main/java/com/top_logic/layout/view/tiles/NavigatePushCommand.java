/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ContextDependentRule;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewCommand} that pushes a new {@link TileFrame} onto the enclosing {@link TileStackScope
 * tile stack}.
 *
 * <p>
 * Resolves the target stack from the {@link ViewContext} - the command must be executed from within
 * a frame that was mounted by a {@link TileStackElement &lt;tile-stack&gt;}. The frame is named by
 * the {@link Config#getFrame() id} of its {@link FrameRouteConfig &lt;frame&gt;} declaration in that
 * stack, which supplies the view, the parameters and the label of the frame. Captured at execution
 * time: the command's main argument (mapped onto a named channel of the new frame, if
 * {@link Config#getBindInputTo() bind-input-to} is set) and the current value of every channel
 * referenced by a {@code <bind>}.
 * </p>
 *
 * <p>
 * The push is checked when the frame displaying it is built: it must name a
 * {@link FrameRouteConfig#getSuccessors() successor} of the declaration that frame was built for -
 * a declaration directly inside the stack for a push from its initial view - and hand over exactly
 * the {@link FrameRoute#paramNames() parameters} of the named frame, no more and no less. A
 * violation is a {@link ConfigurationError} that fails the build of the frame.
 * </p>
 *
 * <p>
 * Example - drilling into a product detail frame from a button next to a product table:
 * </p>
 *
 * <pre>
 * &lt;button&gt;
 *   &lt;action class="com.top_logic.layout.view.tiles.NavigatePushCommand"
 *     frame="product"
 *     inputs="selectedProduct"
 *     bind-input-to="product"/&gt;
 *   &lt;label&gt;&lt;en&gt;Open&lt;/en&gt;&lt;/label&gt;
 * &lt;/button&gt;
 * </pre>
 *
 * @implNote Resolves the target stack via {@link TileStackScope#lookup(ReactContext, String)}.
 */
@InApp
public class NavigatePushCommand implements ViewCommand {

	/**
	 * Configuration for {@link NavigatePushCommand}.
	 */
	@TagName(Config.TAG_NAME)
	@TypeIcon("css:bi bi-box-arrow-right")
	public interface Config extends ViewCommand.Config {

		/** Configuration tag of a {@link NavigatePushCommand}. */
		String TAG_NAME = "navigate-push";

		@Override
		@ClassDefault(NavigatePushCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Configuration name for {@link #getFrame()}. */
		String FRAME = "frame";

		/** Configuration name for {@link #getBindings()}. */
		String BINDINGS = "bindings";

		/** Configuration name for {@link #getBindInputTo()}. */
		String BIND_INPUT_TO = "bind-input-to";

		/** Configuration name for {@link #getFrameLabel()}. */
		String FRAME_LABEL = "frame-label";

		/**
		 * The {@link FrameRouteConfig#getId() id} of the declaration of the frame to push, in the
		 * stack the command is displayed in.
		 *
		 * <p>
		 * The declaration must be a {@link FrameRouteConfig#getSuccessors() successor} of the one the
		 * frame displaying the command was built for.
		 * </p>
		 */
		@Name(FRAME)
		@Mandatory
		String getFrame();

		/**
		 * Channel bindings from the calling scope to the new frame's channels.
		 *
		 * <p>
		 * Each binding captures the <em>current value</em> of the referenced parent channel at
		 * push time and stores it under {@link ChannelBindingConfig#getChannel()} in the pushed
		 * {@link TileFrame#getParams() frame params}. The mounted view's channel of that name
		 * receives the captured value as its initial state.
		 * </p>
		 *
		 * <p>
		 * Together with {@link #getBindInputTo() bind-input-to}, the bindings name exactly the
		 * {@link FrameRoute#paramNames() parameters} of the pushed frame.
		 * </p>
		 */
		@Name(BINDINGS)
		@DefaultContainer
		List<ChannelBindingConfig> getBindings();

		/**
		 * Name of the channel in the pushed view that should receive the command's
		 * main argument.
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

	private final String _frame;

	private final String _location;

	private final List<ChannelBindingConfig> _bindings;

	private final String _bindInputTo;

	private final TileLabelProvider _frameLabel;

	/**
	 * Creates a new {@link NavigatePushCommand}.
	 */
	@CalledByReflection
	public NavigatePushCommand(InstantiationContext context, Config config) {
		_frame = config.getFrame();
		_location = String.valueOf(config.location());
		_bindings = config.getBindings();
		_bindInputTo = config.getBindInputTo();
		PolymorphicConfiguration<? extends TileLabelProvider> labelConfig = config.getFrameLabel();
		_frameLabel = labelConfig != null ? context.getInstance(labelConfig) : null;
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		TileStackScope scope = TileStackScope.lookup(context, Config.TAG_NAME);
		// The lookup succeeded, so the context is the ViewContext whose channels the bindings read.
		ViewContext viewContext = (ViewContext) context;

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
		scope.push(_frame, label, Collections.unmodifiableMap(params));
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * A rule that checks the push against the frame declarations of the stack it is displayed in,
	 * once it is built there.
	 */
	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		return new PushCheck();
	}

	/**
	 * The names of the frame parameters the push hands over.
	 */
	private Set<String> handedOver() {
		Set<String> result = new LinkedHashSet<>();
		if (_bindInputTo != null) {
			result.add(_bindInputTo);
		}
		for (ChannelBindingConfig binding : _bindings) {
			result.add(binding.getChannel());
		}
		return result;
	}

	/**
	 * Checks the push in the given context.
	 *
	 * @throws ConfigurationError
	 *         If the push names no successor of the declaration the frame displaying it was built
	 *         for, or does not hand over exactly the parameters of the frame it names.
	 */
	void check(ViewContext context) {
		TileStackScope stack = context.getScope(TileStackScope.class);
		FrameRoute here = context.getScope(FrameRoute.class);
		if (stack == null || here == null) {
			// Displayed outside of a stack, e.g. in a preview of the frame view: nothing to push onto.
			return;
		}
		FrameRoute target = stack.declaration(_frame);
		if (target == null) {
			throw new ConfigurationError("The " + Config.TAG_NAME + " at " + _location + " names the frame '"
				+ _frame + "', which the tile stack does not declare; declared are "
				+ stack.declarations().stream().map(FrameRoute::id).toList() + ".");
		}
		if (!here.successors().contains(target)) {
			throw new ConfigurationError("The " + Config.TAG_NAME + " at " + _location + " in "
				+ here.describe() + " names the frame '" + _frame
				+ "', which is no successor of that declaration; its successors are "
				+ here.successors().stream().map(FrameRoute::id).toList() + ".");
		}
		Set<String> handedOver = handedOver();
		Set<String> expected = target.paramNames();
		if (!handedOver.equals(expected)) {
			throw new ConfigurationError("The " + Config.TAG_NAME + " at " + _location + " in "
				+ here.describe() + " hands over the parameters " + handedOver + " to " + target.describe()
				+ ", which holds the parameters " + expected + ": a push binds exactly the parameters of"
				+ " the frame it names.");
		}
	}

	/**
	 * The {@link #getIntrinsicRule() intrinsic rule} of the push, checking it where it is built and
	 * allowing it from then on.
	 */
	private final class PushCheck implements ViewExecutabilityRule, ContextDependentRule {

		@Override
		public void bind(ViewContext context) {
			check(context);
		}

		@Override
		public ExecutableState isExecutable(Object input) {
			return ExecutableState.EXECUTABLE;
		}
	}
}
