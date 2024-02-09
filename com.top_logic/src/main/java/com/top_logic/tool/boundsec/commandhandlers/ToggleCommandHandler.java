/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.css.CssUtil;

/**
 * Base class for {@link CommandHandler}s that have an active/pressed state with an alternative
 * image and label.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ToggleCommandHandler extends AbstractCommandHandler {

	/**
	 * Default CSS class for activated {@link ToggleCommandHandler}s.
	 */
	public static final String ACTIVE_CSS = "active";

	/**
	 * Configuration options for {@link ToggleCommandHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * @see #getActiveResourceKey()
		 */
		String ACTIVE_RESOURCE_KEY = "activeResourceKey";

		/**
		 * @see #getActiveImage()
		 */
		String ACTIVE_IMAGE = "activeImage";

		/**
		 * @see #getActiveCssClasses()
		 */
		String ACTIVE_CSS_CLASSES = "activeCssClasses";

		/**
		 * Label to use, if activated.
		 * 
		 * <p>
		 * If no special label key is configured, the default one is used even in activated state.
		 * </p>
		 * 
		 * @see #getResourceKey()
		 */
		@Name(ACTIVE_RESOURCE_KEY)
		@Nullable
		ResKey getActiveResourceKey();

		@Override
		@FormattedDefault("theme:com.top_logic.layout.form.control.Icons.TRISTATE_NULL")
		public ThemeImage getImage();

		@Override
		@FormattedDefault("theme:com.top_logic.layout.form.control.Icons.TRISTATE_NULL_DISABLED")
		public ThemeImage getDisabledImage();

		/**
		 * Icon to use, if activated.
		 * 
		 * <p>
		 * If no special image is configured, the default one is used even in activated state.
		 * </p>
		 * 
		 * @see #getImage()
		 */
		@Name(ACTIVE_IMAGE)
		@Nullable
		@FormattedDefault("theme:com.top_logic.layout.form.control.Icons.TRISTATE_TRUE")
		ThemeImage getActiveImage();

		/**
		 * CSS classes to set in active state.
		 */
		@Name(ACTIVE_CSS_CLASSES)
		@StringDefault(ACTIVE_CSS)
		String getActiveCssClasses();

	}

	/**
	 * Creates a {@link ToggleCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ToggleCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {

		boolean state = !getState(aContext, aComponent);
		setState(aContext, aComponent, state);

		CommandModel commandModel = getCommandModel(someArguments);
		if (commandModel != null) {
			// Update view. Make sure that the view is optional to allow simplified testing.
			commandModel.setImage(getImage(aComponent, state));
			Resources resources = aContext.getResources();
			ResKey labelKey = getResourceKey(aComponent, state);
			commandModel.setLabel(resources.getString(labelKey));
			commandModel.setCssClasses(getCssClasses(aComponent, state));
			if (labelKey != null) {
				commandModel.setTooltip(resources.getString(labelKey.tooltipOptional()));
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Determines, whether this button is pressed.
	 * 
	 * <p>
	 * The state must be determined independently of the command's component, because
	 * </p>
	 * 
	 * @param component
	 *        The context component displaying this command.
	 */
	protected abstract boolean getState(DisplayContext context, LayoutComponent component);

	/**
	 * Updates the state that remembers that this button is pressed.
	 * 
	 * @param component
	 *        The context component displaying this command.
	 */
	protected abstract void setState(DisplayContext context, LayoutComponent component, boolean newValue);

	@Override
	public ThemeImage getImage(LayoutComponent component) {
		return getImage(component, getState(DefaultDisplayContext.getDisplayContext(), component));
	}

	private ThemeImage getImage(LayoutComponent component, boolean state) {
		return state ? getActiveImage(component) : getDefaultImage(component);
	}

	private ThemeImage getActiveImage(LayoutComponent component) {
		ThemeImage activeImage = config().getActiveImage();
		if (activeImage != null) {
			return activeImage;
		}
		return getDefaultImage(component);
	}

	private ThemeImage getDefaultImage(LayoutComponent component) {
		return super.getImage(component);
	}

	@Override
	public ResKey getResourceKey(LayoutComponent component) {
		return getResourceKey(component, getState(DefaultDisplayContext.getDisplayContext(), component));
	}

	private ResKey getResourceKey(LayoutComponent component, boolean state) {
		return state ? getActiveResourceKey(component) : getDefaultResourceKey(component);
	}

	private ResKey getActiveResourceKey(LayoutComponent component) {
		ResKey activeResourceKey = config().getActiveResourceKey();
		if (activeResourceKey != null) {
			return activeResourceKey;
		}
		return getDefaultResourceKey(component);
	}

	private ResKey getDefaultResourceKey(LayoutComponent component) {
		return super.getResourceKey(component);
	}

	@Override
	public String getCssClasses(LayoutComponent component) {
		return getCssClasses(component, getState(DefaultDisplayContext.getDisplayContext(), component));
	}

	private String getCssClasses(LayoutComponent component, boolean state) {
		String defaultCssClasses = getDefaultCssClasses(component);
		return state ? CssUtil.joinCssClasses(getActiveCssClasses(), defaultCssClasses) : defaultCssClasses;
	}

	private String getActiveCssClasses() {
		return config().getActiveCssClasses();
	}

	private String getDefaultCssClasses(LayoutComponent component) {
		return super.getCssClasses(component);
	}
	
	private Config config() {
		return (Config) getConfig();
	}

}
