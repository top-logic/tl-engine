/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Configured view-related properties that can be applied to an existing {@link CommandModel}
 * implementation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommandModelCustomization<C extends CommandModelCustomization.Config<?>>
		extends AbstractConfiguredInstance<C> {

	/**
	 * Configuration options for {@link CommandModelCustomization}.
	 */
	public interface Config<I extends CommandModelCustomization<?>> extends PolymorphicConfiguration<I>, Settings {
		// Pure sum interface.
	}

	/**
	 * Configuration for a {@link CommandModel}.
	 * 
	 * @see CommandModelCustomization#applySettings(Settings, CommandModel) Applying configuration
	 *      settings to an existing {@link CommandModel}.
	 */
	public interface Settings extends ConfigurationItem {
		/** @see #getLabelKey() */
		String LABEL_KEY = "labelKey";

		/** @see #getTooltipKey() */
		String TOOLTIP_KEY = "tooltipKey";

		/** @see #getAltTextKey() */
		String ALT_TEXT_KEY = "altTextKey";

		/** @see #getCssClasses() */
		String CSS_CLASSES = "cssClasses";

		/** @see #getImage() */
		String IMAGE_PATH = "image";

		/** @see #getNotExecutableImage() */
		String NOT_EXECUTABLE_IMAGE_PATH = "notExecutableImage";

		/** @see #getActiveImage() */
		String ACTIVE_IMAGE_PATH = "activeImage";

		/** @see CommandModel#getLabel() */
		@Name(LABEL_KEY)
		@InstanceFormat
		ResKey getLabelKey();

		/** @see CommandModel#getCssClasses() */
		@Name(CSS_CLASSES)
		String getCssClasses();

		/** @see CommandModel#getImage() */
		@Name(IMAGE_PATH)
		ThemeImage getImage();

		/** @see CommandModel#getNotExecutableImage() */
		@Name(NOT_EXECUTABLE_IMAGE_PATH)
		ThemeImage getNotExecutableImage();

		/** @see CommandModel#getActiveImage() */
		@Name(ACTIVE_IMAGE_PATH)
		ThemeImage getActiveImage();

		/** @see CommandModel#getTooltip() */
		@Name(TOOLTIP_KEY)
		@InstanceFormat
		ResKey getTooltipKey();

		/** @see CommandModel#getAltText() */
		@Name(ALT_TEXT_KEY)
		@InstanceFormat
		ResKey getAltTextKey();
	}

	/**
	 * Creates a {@link CommandModelCustomization} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CommandModelCustomization(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * Applies the configured {@link CommandModel} properties.
	 */
	public void customizeCommandModel(CommandModel model) {
		applySettings(getConfig(), model);
	}

	/**
	 * Utility for applying the {@link Settings} to the given {@link CommandModel}.
	 */
	public static void applySettings(Settings config, CommandModel model) {
		ResKey labelKey = config.getLabelKey();
		ResKey tooltipKey = config.getTooltipKey();
		ResKey altTextKey = config.getAltTextKey();
		String cssClasses = StringServices.nonEmpty(config.getCssClasses());
		ThemeImage image = config.getImage();
		ThemeImage notExecutableImage = config.getNotExecutableImage();
		ThemeImage activeImage = config.getNotExecutableImage();

		if (labelKey != null) {
			model.setLabel(labelKey);
		}
		if (tooltipKey != null) {
			model.setTooltip(tooltipKey);
		}
		if (altTextKey != null) {
			model.setAltText(altTextKey);
		}
		if (cssClasses != null) {
			model.setCssClasses(cssClasses);
		}
		if (image != null) {
			model.setImage(image);
		}
		if (notExecutableImage != null) {
			model.setNotExecutableImage(notExecutableImage);
		}
		if (activeImage != null) {
			model.setActiveImage(activeImage);
		}
	}

}
