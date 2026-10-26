/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.theme.UIThemeService;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that puts a density of the UI into effect, {@code normal} or
 * {@code compact}.
 *
 * <p>
 * Stores the density as the user's preference and applies it on the client instantly, without a
 * page reload.
 * </p>
 */
@InApp
public class SetDensityCommand implements ViewCommand {

	/**
	 * Configuration for {@link SetDensityCommand}.
	 */
	@TagName("set-density")
	public interface Config extends ViewCommand.Config {

		/** Configuration name for {@link #getDensity()}. */
		String DENSITY = "density";

		@Override
		@ClassDefault(SetDensityCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * Name of the density to activate, {@code normal} or {@code compact}.
		 */
		@Name(DENSITY)
		@Mandatory
		String getDensity();

	}

	private final String _density;

	/**
	 * Creates a {@link SetDensityCommand} from configuration.
	 */
	@CalledByReflection
	public SetDensityCommand(InstantiationContext context, Config config) {
		_density = config.getDensity();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		return applyDensity(context, _density);
	}

	/**
	 * Stores the given density as the current user's preference and applies it on the client.
	 *
	 * @param context
	 *        The context whose update queue carries the change to the browser.
	 * @param density
	 *        Name of the density to activate. A name that is none of the densities changes nothing.
	 * @return The result of the activation.
	 */
	public static HandlerResult applyDensity(ReactContext context, String density) {
		if (!UIThemeService.isDensity(density)) {
			// Neither stored nor put into effect, so that the page keeps the density the user sees.
			return HandlerResult.DEFAULT_RESULT;
		}
		UIThemeService.getInstance().setSelectedDensity(density);

		SSEUpdateQueue queue = context.getSSEQueue();
		if (queue != null) {
			queue.enqueue(JSSnipplet.create().setCode(clientCode(density)));
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The script putting the given density into effect in the browser.
	 *
	 * @param density
	 *        Name of the density, see {@link UIThemeService#isDensity(String)}.
	 */
	public static String clientCode(String density) {
		StringBuilder code = new StringBuilder();
		code.append("window.");
		code.append(UIThemeService.CLIENT_API);
		code.append(".");
		code.append(UIThemeService.DENSITY_FUNCTION);
		code.append("(");
		TagUtil.writeJsString(code, density);
		code.append(");");
		return code.toString();
	}

}
