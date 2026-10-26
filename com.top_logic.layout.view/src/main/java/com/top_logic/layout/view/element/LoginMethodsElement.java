/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethods;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that renders one redirect button per external {@link LoginMethod} contributed
 * through {@link LoginMethods} (e.g. SSO providers).
 *
 * <p>
 * Each button is a full-page browser redirect to the method's
 * {@link LoginMethod#getInitiationUrl(String) initiation URL}, returning to the page the visitor
 * asked for. Renders nothing when no login method is configured, so it can be placed in any login
 * view unconditionally.
 * </p>
 *
 * <p>
 * The buttons are determined each time the element is rendered, also when a reloaded page renders
 * the control tree its window keeps, so that only the methods {@link LoginMethod#isAvailable()
 * available} at that moment are offered.
 * </p>
 */
public class LoginMethodsElement implements UIElement {

	/**
	 * Configuration for {@link LoginMethodsElement}.
	 */
	@TagName("login-methods")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(LoginMethodsElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	private final String _cssClass;

	private final Supplier<? extends List<? extends LoginMethod>> _methods;

	/**
	 * Creates a new {@link LoginMethodsElement} from configuration.
	 */
	@CalledByReflection
	public LoginMethodsElement(InstantiationContext context, Config config) {
		this(config.getCssClass(), LoginMethods::all);
	}

	/**
	 * Creates a {@link LoginMethodsElement}.
	 *
	 * @param cssClass
	 *        The CSS class of the stack of buttons, or <code>null</code>.
	 * @param methods
	 *        The login methods to offer, asked each time the buttons are rendered.
	 */
	public LoginMethodsElement(String cssClass, Supplier<? extends List<? extends LoginMethod>> methods) {
		_cssClass = cssClass;
		_methods = methods;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		LoginMethodsControl result = new LoginMethodsControl(context, _methods);
		result.setCssClass(_cssClass);
		return result;
	}

	/**
	 * Stack of the buttons of the login methods available when it is rendered.
	 *
	 * <p>
	 * A window keeps its control tree across page reloads, so the buttons are not fixed when the
	 * control is created: a login method that becomes available or unavailable later - e.g. when the
	 * secret of an identity provider is provided or removed - shows up or disappears with the next
	 * rendering, without restarting the application.
	 * </p>
	 */
	private static class LoginMethodsControl extends ReactStackControl {

		private final ViewContext _context;

		private final Supplier<? extends List<? extends LoginMethod>> _methods;

		/**
		 * The IDs of the methods and the return path the current buttons were built for,
		 * <code>null</code> before the first rendering.
		 */
		private List<String> _builtFor;

		LoginMethodsControl(ViewContext context, Supplier<? extends List<? extends LoginMethod>> methods) {
			super(context, StackDirection.COLUMN, StackGap.COMPACT, StackAlign.STRETCH, false, List.of());
			_context = context;
			_methods = methods;
		}

		@Override
		protected void onBeforeWrite() {
			super.onBeforeWrite();

			String returnTo = returnPath(_context);
			List<? extends LoginMethod> methods = _methods.get();
			List<String> builtFor = new ArrayList<>(methods.size() + 1);
			builtFor.add(returnTo);
			for (LoginMethod method : methods) {
				builtFor.add(method.getId());
			}
			if (builtFor.equals(_builtFor)) {
				return;
			}

			List<ReactControl> previous = childControls();
			List<ReactControl> buttons = new ArrayList<>(methods.size());
			for (LoginMethod method : methods) {
				buttons.add(createButton(method, returnTo));
			}
			setChildren(buttons);
			for (ReactControl button : previous) {
				button.cleanupTree();
			}
			_builtFor = builtFor;
		}

		private ReactButtonControl createButton(LoginMethod method, String returnTo) {
			String label = Resources.getInstance().getString(method.getLabel());
			ReactButtonControl button = new ReactButtonControl(_context, label, ctx -> HandlerResult.DEFAULT_RESULT);
			// Navigate the browser directly to the external login (no server/SSE round-trip).
			button.setNavigateUrl(method.getInitiationUrl(returnTo));
			ThemeImage icon = method.getIcon();
			if (icon != null) {
				button.setImage(icon);
			}
			return button;
		}

	}

	/**
	 * The page the external login returns to: the one the visitor asked for.
	 *
	 * <p>
	 * The requested route is the URL the {@link RouteManager} carries - the login view takes none of
	 * it up, so it is still the one the visitor entered - and is prefixed with
	 * {@link ViewServlet#ROOT_PATH} to name a page of the view application. Context-relative: the
	 * authentication servlet's redirect prepends the context path itself, so including it here would
	 * double it. A window that routes nothing returns to the application's root.
	 * </p>
	 */
	private static String returnPath(ViewContext context) {
		RouteManager routeManager = context.getRouteManager();
		String route = routeManager == null ? null : routeManager.currentUrl();
		if (route == null || route.isEmpty()) {
			return ViewServlet.ROOT_PATH;
		}
		return ViewServlet.ROOT_PATH + route;
	}

}
