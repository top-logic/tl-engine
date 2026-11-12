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
 * The element fills the space it is given. Behind the content stands a grid of dots in the house
 * color. A subtle highlight follows the mouse pointer, a click on the background sends a ripple
 * across the grid, and repeated clicks trigger a hidden shape change. Nothing moves at rest, and
 * with "reduce motion" set in the system the grid stands still.
 * </p>
 *
 * <p>
 * The content is centered above the backdrop. To arrange several children, wrap them in a
 * {@code <stack>}.
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

		@Override
		@ClassDefault(LoginBackdropElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Path of a static resource of the web application, e.g. "/images/logo.svg".
		 *
		 * <p>
		 * The logo stands above the content. Without it, no logo is shown.
		 * </p>
		 */
		@Name(LOGO)
		@Nullable
		String getLogo();

		/**
		 * Path of a static resource of the web application showing the logo in dark mode, e.g.
		 * "/images/logo-dark.svg".
		 *
		 * <p>
		 * In dark mode this picture is shown instead of the <i>logo</i>. Without it, the <i>logo</i>
		 * is shown in both modes.
		 * </p>
		 */
		@Name(LOGO_DARK)
		@Nullable
		String getLogoDark();

		/**
		 * What the logo shows, for a reader who cannot see it.
		 *
		 * <p>
		 * Left unset, the logo is announced as a picture and nothing more.
		 * </p>
		 */
		@Name(LOGO_ALT)
		@Nullable
		ResKey getLogoAlt();
	}

	private final String _logo;

	private final String _logoDark;

	private final ResKey _logoAlt;

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
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<IReactControl> childControls = createChildControls(context);

		ReactControl content = ContentControls.combine(context, childControls);

		ReactLoginBackdropControl result = new ReactLoginBackdropControl(context, content);
		result.setLogo(url(context, _logo));
		result.setLogoDark(url(context, _logoDark));
		if (_logoAlt != null) {
			result.setLogoAlt(Resources.getInstance().getString(_logoAlt));
		}
		result.setCssClass(_cssClass);
		return result;
	}

	private static String url(ViewContext context, String resource) {
		if (resource == null || resource.isEmpty()) {
			return null;
		}
		return context.getContextPath() + resource;
	}
}
