/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.accesscontrol.login.LoginMethod;
import com.top_logic.base.accesscontrol.login.LoginMethods;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that renders one redirect button per external {@link LoginMethod} contributed
 * through {@link LoginMethods} (e.g. SSO providers).
 *
 * <p>
 * Each button is a full-page browser redirect to the method's
 * {@link LoginMethod#getInitiationUrl(String) initiation URL}, returning to the React view after
 * authentication. Renders nothing when no login method is configured, so it can be placed in any
 * login view unconditionally.
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

	/**
	 * Creates a new {@link LoginMethodsElement} from configuration.
	 */
	@CalledByReflection
	public LoginMethodsElement(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		String returnTo = DefaultDisplayContext.getDisplayContext().asRequest().getContextPath() + "/view/";

		List<LoginMethod> methods = LoginMethods.all();
		List<ReactControl> buttons = new ArrayList<>(methods.size());
		for (LoginMethod method : methods) {
			String label = Resources.getInstance().getString(method.getLabel());
			String url = method.getInitiationUrl(returnTo);
			ReactButtonControl button = new ReactButtonControl(context, label, ctx -> redirect(ctx, url));
			ThemeImage icon = method.getIcon();
			if (icon != null) {
				button.setImage(icon);
			}
			buttons.add(button);
		}
		return new ReactStackControl(context, StackDirection.COLUMN, StackGap.COMPACT, StackAlign.STRETCH, false,
			buttons);
	}

	private static HandlerResult redirect(com.top_logic.layout.react.ReactContext context, String url) {
		String js = "window.location.assign('" + url + "');";
		context.getSSEQueue().enqueue(JSSnipplet.create().setCode(js));
		return HandlerResult.DEFAULT_RESULT;
	}

}
