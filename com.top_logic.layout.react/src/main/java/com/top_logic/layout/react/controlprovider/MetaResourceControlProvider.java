/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.controlprovider;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.ReactResourceCellControl;

/**
 * {@link ReactControlProvider} that renders any object by its icon and its label.
 *
 * <p>
 * Icon, label, tooltip and CSS class are resolved through the {@link MetaResourceProvider}, so an
 * object is displayed the way its type registers it.
 * </p>
 *
 * <p>
 * Which of these parts the display shows is configured: the icon, the label, and whether the
 * display leads to the place the application shows the object at.
 * </p>
 *
 * @implNote The display is a {@link ReactResourceCellControl} created with the three flags
 *           {@link Config#getImage()}, {@link Config#getLabel()} and {@link Config#getLink()}.
 */
public class MetaResourceControlProvider implements ReactControlProvider {

	/**
	 * Configuration options for {@link MetaResourceControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<MetaResourceControlProvider> {

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getLink()}. */
		String LINK = "link";

		@Override
		@ClassDefault(MetaResourceControlProvider.class)
		Class<? extends MetaResourceControlProvider> getImplementationClass();

		/**
		 * Whether the icon of the displayed object is shown before its label.
		 *
		 * <p>
		 * The icon tells the type of the object apart at a glance, so it is shown unless the
		 * display has no room for it, or the type it would name is the same for every displayed
		 * object anyway.
		 * </p>
		 */
		@Name(IMAGE)
		@BooleanDefault(true)
		boolean getImage();

		/** @see #getImage() */
		void setImage(boolean value);

		/**
		 * Whether the label of the displayed object is shown.
		 *
		 * <p>
		 * The label is what names the object to the user, so it is shown unless the display stands
		 * for the object by its icon alone.
		 * </p>
		 */
		@Name(LABEL)
		@BooleanDefault(true)
		boolean getLabel();

		/** @see #getLabel() */
		void setLabel(boolean value);

		/**
		 * Whether the display leads to the place the application shows the object at.
		 *
		 * <p>
		 * A link is off by default, because the display of an object is commonly the object's own
		 * place in the view - a node of a tree - and there a click selects it. A link that leaves
		 * the view on that click is in the way.
		 * </p>
		 *
		 * <p>
		 * Where the display is a value pointing at an object shown elsewhere, this option turns the
		 * value into a link to it.
		 * </p>
		 */
		@Name(LINK)
		@BooleanDefault(false)
		boolean getLink();

		/** @see #getLink() */
		void setLink(boolean value);
	}

	private final boolean _useImage;

	private final boolean _useLabel;

	private final boolean _useLink;

	/**
	 * Creates a {@link MetaResourceControlProvider} from configuration.
	 */
	@CalledByReflection
	public MetaResourceControlProvider(InstantiationContext context, Config config) {
		_useImage = config.getImage();
		_useLabel = config.getLabel();
		_useLink = config.getLink();
	}

	@Override
	public ReactControl createControl(ReactContext context, Object model) {
		return new ReactResourceCellControl(context, model, MetaResourceProvider.INSTANCE, _useImage, _useLabel,
			_useLink);
	}
}
