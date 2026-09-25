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
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.AvatarSize;
import com.top_logic.layout.react.control.common.ReactAvatarControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Declarative {@link UIElement} displaying a channel's value (e.g. a person) as a circular avatar.
 *
 * <p>
 * The circle shows the picture of the one it represents. Where there is none, it is filled with the
 * initials of the value's label over a background color derived from that label.
 * </p>
 */
@InApp
public class AvatarElement implements UIElement {

	/**
	 * Configuration for {@link AvatarElement}.
	 */
	@TagName("avatar")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(AvatarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getSize()}. */
		String SIZE = "size";

		/**
		 * Channel whose value is displayed; the avatar derives initials and category color from the value's
		 * label.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Channel whose value is the picture shown instead of the initials.
		 *
		 * <p>
		 * The value is either picture data (content type "image/...") or a text naming the address
		 * the picture is loaded from. Any other value - a person without a photo, an unrelated
		 * object - leaves the initials. The picture follows the channel, so a photo replaced
		 * elsewhere is shown without the avatar being built anew.
		 * </p>
		 */
		@Name(IMAGE)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getImage();

		/**
		 * Diameter of the circle: {@code sm}, {@code md} (the default), {@code lg} or {@code xl}.
		 */
		@Name(SIZE)
		AvatarSize getSize();
	}

	private final Config _config;

	/**
	 * Creates a new {@link AvatarElement} from configuration.
	 */
	@CalledByReflection
	public AvatarElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel channel = context.resolveChannel(_config.getInput());
		ReactAvatarControl control = new ReactAvatarControl(context, label(channel.get()));
		control.setSize(_config.getSize());
		control.setCssClass(_config.getCssClass());

		ChannelListener listener = (sender, oldValue, newValue) -> control.setName(label(newValue));
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));

		ChannelRef imageRef = _config.getImage();
		if (imageRef != null) {
			ViewChannel imageChannel = context.resolveChannel(imageRef);
			control.setImage(imageChannel.get());
			ChannelListener imageListener = (sender, oldValue, newValue) -> control.setImage(newValue);
			imageChannel.addListener(imageListener);
			control.addCleanupAction(() -> imageChannel.removeListener(imageListener));
		}
		return control;
	}

	private static String label(Object value) {
		return value == null ? null : MetaLabelProvider.INSTANCE.getLabel(value);
	}
}
