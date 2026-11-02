/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.control.overlay.ReactPopoverControl;
import com.top_logic.layout.react.reveal.ChildRevealer;
import com.top_logic.layout.react.state.PopoverState.Mode;
import com.top_logic.layout.react.state.PopoverState.Placement;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ClosePopoverAction;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.RevealRegistry;
import com.top_logic.util.Resources;

/**
 * A button that opens a surface of arbitrary content beside it.
 *
 * <p>
 * The surface is non-modal: the page around it stays usable, and a click outside or the Escape key
 * closes it. Use it for content that belongs to one place of the page but would crowd it when shown
 * permanently - the filter of a list, the details of a value, a small settings form. A choice
 * between commands is a menu instead.
 * </p>
 *
 * <p>
 * The content is created when the popover opens for the first time and kept while it is closed, so
 * it shows what the user left there when it opens again. It is part of the view the popover is
 * written in and shares its channels: a form in the content edits the object of a channel of the
 * view, and what it writes to a channel is seen by the rest of the view. A
 * {@link ClosePopoverAction &lt;close-popover&gt;} in a command of the content closes the popover.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;popover label="Filter" image="css:fa-solid fa-filter" placement="bottom-end"&gt;
 *   &lt;content&gt;
 *     &lt;form input="filter"&gt;
 *       &lt;field attribute="status"/&gt;
 *     &lt;/form&gt;
 *     &lt;button&gt;
 *       &lt;action class="com.top_logic.layout.view.command.GenericViewCommand" label="Done"&gt;
 *         &lt;close-popover/&gt;
 *       &lt;/action&gt;
 *     &lt;/button&gt;
 *   &lt;/content&gt;
 * &lt;/popover&gt;
 * </pre>
 */
@InApp
public class PopoverElement implements UIElement {

	/**
	 * The key under which the popover addresses its content, see {@link ChildGroup#key()}.
	 *
	 * <p>
	 * Displaying something that lives in the content of a popover opens the popover.
	 * </p>
	 */
	public static final String CONTENT_KEY = "content";

	/** Personalization key segment of the content of a popover. */
	private static final String POPOVER_SEGMENT = "popover";

	/** Slot path segment of the trigger content. */
	private static final String TRIGGER_SEGMENT = "trigger";

	/**
	 * Configuration for {@link PopoverElement}.
	 */
	@TagName(Config.TAG_NAME)
	@TypeIcon("css:bi bi-chat-square-dots")
	public interface Config extends UIElement.Config {

		/** Configuration tag of a {@link PopoverElement}. */
		String TAG_NAME = "popover";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getPlacement()}. */
		String PLACEMENT = "placement";

		/** Configuration name for {@link #getOpenOn()}. */
		String OPEN_ON = "open-on";

		/** Configuration name for {@link #getTrigger()}. */
		String TRIGGER = "trigger";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		@Override
		@ClassDefault(PopoverElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The label of the button opening the popover.
		 *
		 * <p>
		 * The label is shown when the button has neither {@link #getTrigger() trigger content} nor
		 * an {@link #getImage() image}. In every case it is the name by which assistive technology
		 * announces the button and the opened surface, so a popover should have one.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * The icon of the button opening the popover, shown alone when the button has no
		 * {@link #getTrigger() trigger content}.
		 */
		@Name(IMAGE)
		@Nullable
		ThemeImage getImage();

		/**
		 * Where the surface stands at the button: on which side, and aligned with which edge of
		 * the button.
		 *
		 * <p>
		 * Where the surface does not fit on the given side, it moves to the opposite one.
		 * </p>
		 */
		@Name(PLACEMENT)
		@FormattedDefault("bottom-start")
		Placement getPlacement();

		/**
		 * The gesture opening the popover: a click on the button, or resting the pointer on it.
		 */
		@Name(OPEN_ON)
		@FormattedDefault("click")
		Mode getOpenOn();

		/**
		 * What the button opening the popover shows instead of its {@link #getLabel() label} and
		 * {@link #getImage() image} - a text, an icon, a badge, or several of them side by side.
		 *
		 * <p>
		 * The content is presentational only. The button itself is what the user acts on, so an
		 * element that is operated itself - a button, an input field, a link - does not belong
		 * here: it would be a control inside a control, which can be neither reached nor operated.
		 * </p>
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address
		 *           an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(TRIGGER)
		@EntryTag("child")
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getTrigger();

		/**
		 * The content of the surface: any elements of the view, created when the popover opens for
		 * the first time.
		 *
		 * <p>
		 * The content shares the channels of the view the popover is written in.
		 * </p>
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address
		 *           an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(CONTENT)
		@EntryTag("child")
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getContent();
	}

	private final Config _config;

	private final List<UIElement> _trigger;

	private final List<UIElement> _content;

	/**
	 * Creates a new {@link PopoverElement} from configuration.
	 */
	@CalledByReflection
	public PopoverElement(InstantiationContext context, Config config) {
		_config = config;
		_trigger = instantiate(context, config.getTrigger());
		_content = instantiate(context, config.getContent());
	}

	private static List<UIElement> instantiate(InstantiationContext context,
			List<UIElement.Config> configs) {
		return configs.stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(_trigger), ChildGroup.keyed(CONTENT_KEY, _content));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		RevealPath here = RevealPath.of(context);

		// The content is created only when the popover opens for the first time, so its context
		// must already say where that content will sit.
		ViewContext contentContext = context.childContext(POPOVER_SEGMENT)
			.withChildSlotPath(CONTENT_KEY)
			.withScope(RevealPath.class, here.append(this, CONTENT_KEY));
		PopoverContent content = new PopoverContent(_content, contentContext);

		ReactPopoverControl popover = new ReactPopoverControl(context, createTrigger(context), content,
			_config.getPlacement(), _config.getOpenOn());
		content.setPopover(popover);

		ResKey label = _config.getLabel();
		if (label != null) {
			popover.setLabel(Resources.getInstance().getString(label));
		}
		popover.setIcon(_config.getImage());
		popover.setCssClass(_config.getCssClass());

		RevealRegistry registry = context.getRevealRegistry();
		if (registry != null) {
			ChildRevealer revealer = key -> {
				if (!CONTENT_KEY.equals(key)) {
					throw new IllegalArgumentException("A popover has no content '" + key + "'.");
				}
				popover.open();
			};
			popover.addCleanupAction(registry.registerContainer(this, here, revealer));
		}

		return popover;
	}

	/**
	 * The content of the trigger button, {@code null} for a button showing label and image.
	 *
	 * <p>
	 * Several elements sit side by side, since the button reads as one control.
	 * </p>
	 */
	private ReactControl createTrigger(ViewContext context) {
		if (_trigger.isEmpty()) {
			return null;
		}
		ViewContext triggerContext = context.withChildSlotPath(TRIGGER_SEGMENT);
		List<ReactControl> controls = new ArrayList<>(_trigger.size());
		for (int i = 0; i < _trigger.size(); i++) {
			controls.add((ReactControl) _trigger.get(i).createControl(triggerContext.withChildSlotPath(Integer.toString(i))));
		}
		if (controls.size() == 1) {
			return controls.get(0);
		}
		return new ReactStackControl(context, StackDirection.ROW, StackGap.COMPACT, StackAlign.CENTER, false,
			controls);
	}

	/**
	 * Creates the content of a popover when it opens for the first time.
	 *
	 * <p>
	 * The content is created in a context that knows the popover, so that a
	 * {@link ClosePopoverAction} in it finds the popover to close.
	 * </p>
	 */
	private static final class PopoverContent implements Supplier<ReactControl> {

		private final List<UIElement> _elements;

		private final ViewContext _context;

		private ReactPopoverControl _popover;

		PopoverContent(List<UIElement> elements, ViewContext context) {
			_elements = elements;
			_context = context;
		}

		/**
		 * Sets the popover the content is created for, before it first opens.
		 */
		void setPopover(ReactPopoverControl popover) {
			_popover = popover;
		}

		@Override
		public ReactControl get() {
			return ContentControls.toControl(_elements,
				_context.withScope(ReactPopoverControl.class, _popover));
		}
	}
}
