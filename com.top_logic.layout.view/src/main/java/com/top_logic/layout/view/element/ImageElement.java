/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.image.ImageFit;
import com.top_logic.layout.react.control.image.ReactImageControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that displays a picture through the {@link ReactImageControl}.
 *
 * <p>
 * The picture is either a resource of the web application, or it is read from an input channel
 * (e.g. a QR-code PNG that a {@link com.top_logic.layout.view.command.ViewAction} placed on a
 * dialog channel) and kept in sync with that channel: when the channel value changes, the displayed
 * picture is updated. Where both are configured, the resource is the placeholder shown while the
 * channel holds no picture.
 * </p>
 *
 * <p>
 * The box the picture is shown in is described by the proportions, the width and the height; where
 * none of them is given, the picture keeps its natural size, limited to the width available.
 * </p>
 */
@InApp
public class ImageElement implements UIElement {

	/**
	 * Configuration for {@link ImageElement}.
	 */
	@TagName("image")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getResource()}. */
		String RESOURCE = "resource";

		/** Configuration name for {@link #getAlt()}. */
		String ALT = "alt";

		/** Configuration name for {@link #getAspectRatio()}. */
		String ASPECT_RATIO = "aspect-ratio";

		/** Configuration name for {@link #getFit()}. */
		String FIT = "fit";

		/** Configuration name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Configuration name for {@link #getHeight()}. */
		String HEIGHT = "height";

		/** Configuration name for {@link #getLazy()}. */
		String LAZY = "lazy";

		/** Configuration name for {@link #getCssClass()}. */
		String CSS_CLASS = "css-class";

		/**
		 * Pattern a value of {@link #getAspectRatio()} matches: two numbers separated by a slash.
		 */
		String ASPECT_RATIO_PATTERN = "\\s*\\d+(\\.\\d+)?\\s*/\\s*\\d+(\\.\\d+)?\\s*";

		@Override
		@ClassDefault(ImageElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel whose value is displayed as a picture.
		 *
		 * <p>
		 * The value is either picture data (content type "image/...") or a text naming the address
		 * the picture is loaded from. Any other value - binary data the browser cannot render as a
		 * picture, or an unrelated object - shows the {@link #getResource()} instead, and nothing
		 * where there is none.
		 * </p>
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Path of a static resource of the web application, e.g. "/images/logo.svg".
		 *
		 * <p>
		 * On its own, this is the picture the element shows. Together with {@link #getInput()} it
		 * is the placeholder shown as long as that channel holds no picture.
		 * </p>
		 */
		@Name(RESOURCE)
		@Nullable
		String getResource();

		/**
		 * What the picture shows, for a reader who cannot see it.
		 *
		 * <p>
		 * Worth saying wherever the picture carries information rather than decoration - a QR code
		 * enrolling an authenticator, a chart, a scan. Left unset, it is announced as a picture and
		 * nothing more.
		 * </p>
		 */
		@Name(ALT)
		@Nullable
		ResKey getAlt();

		/**
		 * Proportions of the box the picture is shown in, as width and height separated by a slash,
		 * e.g. "16/9".
		 *
		 * <p>
		 * With proportions the box keeps its shape as the available width changes, so that a row of
		 * pictures of differing originals stays even. Left unset, the box takes its shape from the
		 * picture.
		 * </p>
		 */
		@Name(ASPECT_RATIO)
		@Nullable
		@RegexpConstraint(ASPECT_RATIO_PATTERN)
		String getAspectRatio();

		/**
		 * How a picture whose proportions differ from the box fills it.
		 *
		 * <p>
		 * Only of consequence where the box has a shape of its own, through
		 * {@link #getAspectRatio()}, {@link #getWidth()} or {@link #getHeight()}.
		 * </p>
		 */
		@Name(FIT)
		ImageFit getFit();

		/**
		 * Width of the box the picture is shown in, as a CSS length such as "12rem".
		 */
		@Name(WIDTH)
		@Nullable
		String getWidth();

		/**
		 * Height of the box the picture is shown in, as a CSS length such as "12rem".
		 */
		@Name(HEIGHT)
		@Nullable
		String getHeight();

		/**
		 * Whether the browser may postpone loading the picture until it comes close to the visible
		 * part of the page.
		 *
		 * <p>
		 * Worth switching on for a picture far down a long page, and wrong for one the user sees at
		 * once, which then arrives later than it had to.
		 * </p>
		 */
		@Name(LAZY)
		@BooleanDefault(false)
		boolean getLazy();

		/**
		 * Additional CSS class appended to the classes of the picture's box.
		 */
		@Name(CSS_CLASS)
		@Nullable
		String getCssClass();
	}

	private final Config _config;

	/**
	 * Creates a new {@link ImageElement} from configuration.
	 */
	@CalledByReflection
	public ImageElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ReactImageControl control = new ReactImageControl(context);

		ResKey alt = _config.getAlt();
		if (alt != null) {
			control.setAlt(Resources.getInstance().getString(alt));
		}
		String resource = _config.getResource();
		if (resource != null && !resource.isEmpty()) {
			control.setFallbackUrl(context.getContextPath() + resource);
		}
		control.setFit(_config.getFit());
		control.setAspectRatio(_config.getAspectRatio());
		control.setWidth(_config.getWidth());
		control.setHeight(_config.getHeight());
		control.setLazy(_config.getLazy());
		control.setCssClass(_config.getCssClass());

		ChannelRef inputRef = _config.getInput();
		if (inputRef != null) {
			ViewChannel channel = context.resolveChannel(inputRef);
			control.setValue(channel.get());
			ChannelListener listener = (sender, oldValue, newValue) -> control.setValue(newValue);
			channel.addListener(listener);
			control.addCleanupAction(() -> channel.removeListener(listener));
		}
		return control;
	}

}
