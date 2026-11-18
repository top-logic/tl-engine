/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactLoginBackdropControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that shows its content in front of a halftone backdrop, for the login display.
 *
 * <p>
 * The element fills the space it is given. Behind the content stands a fine grid of dots in the
 * brand color. A subtle highlight follows the mouse pointer, and a click on the background sends
 * a ripple across the grid. Nothing moves at rest, and with "reduce motion" set in the system the
 * grid stands still.
 * </p>
 *
 * <p>
 * The content is centered above the backdrop. To arrange several children, wrap them in a stack
 * element. Above the content stands the logo, or without one the {@link Config#getTitle() title}
 * of the application, and at the bottom of the page "Powered by TopLogic".
 * </p>
 */
public class LoginBackdropElement extends ContainerElement {

	/**
	 * Configuration for {@link LoginBackdropElement}.
	 */
	@TagName("login-backdrop")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getLogo()}. */
		String LOGO = "logo";

		/** Configuration name for {@link #getLogoDark()}. */
		String LOGO_DARK = "logo-dark";

		/** Configuration name for {@link #getLogoAlt()}. */
		String LOGO_ALT = "logo-alt";

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		@Override
		@ClassDefault(LoginBackdropElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The logo, e.g. an image file of the web application "/images/logo.svg" or a theme image
		 * "theme:IMAGES_APP_LOGO".
		 *
		 * <p>
		 * The logo stands above the content. Without it, the title of the application is shown
		 * there.
		 * </p>
		 */
		@Name(LOGO)
		@Nullable
		ThemeImage getLogo();

		/**
		 * The logo in dark mode, e.g. "/images/logo-dark.svg".
		 *
		 * <p>
		 * In dark mode this picture is shown instead of the <i>logo</i>. Without it, the <i>logo</i>
		 * is shown in both modes.
		 * </p>
		 */
		@Name(LOGO_DARK)
		@Nullable
		ThemeImage getLogoDark();

		/**
		 * What the logo shows, for a reader who cannot see it.
		 *
		 * <p>
		 * Left unset, the logo is described by the title of the application.
		 * </p>
		 */
		@Name(LOGO_ALT)
		@Nullable
		ResKey getLogoAlt();

		/**
		 * The name of the application, shown above the content where there is no logo, and
		 * describing a logo without a {@link #getLogoAlt() description of its own}.
		 *
		 * <p>
		 * Left unset, the {@link com.top_logic.layout.I18NConstants#APPLICATION_TITLE application
		 * title} is shown, which every application sets in its resource bundle.
		 * </p>
		 */
		@Name(TITLE)
		@Nullable
		ResKey getTitle();
	}

	private final ThemeImage _logo;

	private final ThemeImage _logoDark;

	private final ResKey _logoAlt;

	private final ResKey _title;

	private final String _cssClass;

	/**
	 * Creates a new {@link LoginBackdropElement} from configuration.
	 */
	@CalledByReflection
	public LoginBackdropElement(InstantiationContext context, Config config) {
		super(context, config);
		_logo = config.getLogo();
		_logoDark = config.getLogoDark();
		_logoAlt = config.getLogoAlt();
		_title = config.getTitle();
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<IReactControl> childControls = createChildControls(context);

		ReactControl content = ContentControls.combine(context, childControls);

		ReactLoginBackdropControl result = new ReactLoginBackdropControl(context, content);
		result.setLogo(logo(context, _logo));
		result.setLogoDark(logo(context, _logoDark));
		if (_logoAlt != null) {
			result.setLogoAlt(Resources.getInstance().getString(_logoAlt));
		}
		result.setAppTitle(Resources.getInstance()
			.getString(_title != null ? _title : com.top_logic.layout.I18NConstants.APPLICATION_TITLE));
		result.setCssClass(_cssClass);
		return result;
	}

	/**
	 * The encoded form of the given logo for the client, {@code null} for none - also for the
	 * invisible image, which leaves the place to the title of the application.
	 */
	private static String logo(ViewContext context, ThemeImage image) {
		String encoded = ReactImages.encode(context, image);
		return ThemeImage.NONE_ICON.equals(encoded) ? null : encoded;
	}
}
