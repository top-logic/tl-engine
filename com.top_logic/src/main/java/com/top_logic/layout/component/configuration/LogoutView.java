/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.View;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.link.Link;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * The view displays the command to log the user out or to log in if the user is not yet logged in.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LogoutView extends AbstractConfiguredInstance<LogoutView.Config>
		implements ViewConfiguration, View, HTMLConstants, Link {

	private String cssClass;

    /**
     * Configuration interface for {@link LogoutView}.
     */
	public interface Config extends PolymorphicConfiguration<LogoutView> {

		/** Configuration name for {@link #getImage()}. */
		String LOGOUT_IMAGE = "image";

		/** Configuration name for {@link #getLoginImage()} */
		String LOGIN_IMAGE = "login-image";

		/** Configuration name for {@link #getCssClass()} */
		String CSS_CLASS = "cssClass";

		/**
		 * The icon to display to log out the user.
		 */
		@Name(LOGOUT_IMAGE)
		ThemeImage getImage();

		/**
		 * The icon to display when the user is not yet logged in.
		 */
		@Name(LOGIN_IMAGE)
		ThemeImage getLoginImage();

		/**
		 * The CSS class to add to the view.
		 */
		@Name(CSS_CLASS)
		String getCssClass();
		
		/**
		 * The {@link Renderer} to create the visual representation.
		 */
		@InstanceFormat
		@InstanceDefault(ToolRowCommandRenderer.class)
		Renderer<? super LogoutView> getRenderer();
	}

	/**
	 * Creates a {@link LogoutView} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LogoutView(InstantiationContext context, LogoutView.Config config) {
		super(context, config);
		
		this.cssClass = StringServices.nonEmpty(config.getCssClass());
	}

	@Override
	public String getID() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	/**
	 * It is necessary for this method that {@link DisplayContext#getExecutionScope()} of the given
	 * {@link DisplayContext context} is not <code>null</code> to compute a correct frame
	 * reference.
	 * 
	 * @see com.top_logic.layout.View#write(com.top_logic.layout.DisplayContext,
	 *      com.top_logic.basic.xml.TagWriter)
	 */
	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		getConfig().getRenderer().write(context, out, this);
	}

	private boolean isNotLoggedIn() {
		return TLContext.getContext().getCurrentPersonWrapper().equals(PersonManager.getManager().getAnonymous());
	}

	@Override
	public void writeCssClassesContent(Appendable out) throws IOException {
		out.append(cssClass);
	}

	@Override
	public String getTooltip() {
		return getLabel();
	}

	@Override
	public String getTooltipCaption() {
		return null;
	}

	@Override
	public String getLabel() {
		ResKey key;
		if (isNotLoggedIn()) {
			key = I18NConstants.LOGIN;
		} else {
			key = I18NConstants.LOGOUT;
		}
		return Resources.getInstance().getString(key);
	}

	@Override
	public String getAltText() {
		return getLabel();
	}

	@Override
	public ThemeImage getImage() {
		return isNotLoggedIn() ? getConfig().getLoginImage() : getConfig().getImage();
	}

	@Override
	public boolean isDisabled() {
		return false;
	}

	@Override
	public String getOnclick() {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		if (isNotLoggedIn()) {
			StringBuilder out = new StringBuilder();
			try {
				out.append("services.ajax.logout('");
				out.append(ApplicationPages.getInstance().getLoginPage());
				out.append("');");
				context.getWindowScope().appendCloseAllWindowsCommand(out, context.getExecutionScope().getFrameScope());
			} catch (IOException ex) {
				throw new IOError(ex);
			}
			out.append("return false;");
			return out.toString();
		} else {
			StringBuilder out = new StringBuilder();
			try {
				appendLogoutAction(context, out, context.getExecutionScope().getFrameScope());
			} catch (IOException ex) {
				throw new IOError(ex);
			}
			out.append("return false;");
			return out.toString();
		}
	}

	/**
	 * Appends js code to execute on client side to logout from the session.
	 * 
	 * @param out
	 *        the Appendable to append code to.
	 * @param source
	 *        the server side representation of the document in which the code
	 *        will be executed.
	 * 
	 * @throws IOException
	 *         iff the given {@link Appendable} throws someone
	 */
	public static void appendLogoutAction(DisplayContext context, Appendable out, FrameScope source) throws IOException {
		out.append("services.ajax.logout('");
		out.append(ApplicationPages.getInstance().getTriggerLogoutPage());
		out.append("');");
		context.getWindowScope().appendCloseAllWindowsCommand(out, source);
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		return this;
	}

	/**
	 * Brings an action to the client which logs out the current user.
	 * 
	 * @param scope
	 *        the server side representation of some browser window. must not be
	 *        <code>null</code>.
	 */
	public static void logout(WindowScope scope) {
		ClientAction logoutAction = new JSSnipplet(new AbstractDisplayValue() {
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				LogoutView.appendLogoutAction(context, out, context.getWindowScope().getTopLevelFrameScope());
			}
		});
		WindowScope mainWindow = scope;
		while (mainWindow.getOpener() != null) {
			// search the main window as the logout action must be executed there
			mainWindow = mainWindow.getOpener();
		}
		mainWindow.getTopLevelFrameScope().addClientAction(logoutAction);
	}

}
