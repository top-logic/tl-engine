/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.internal;

import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;

/**
 * Command to load and display another main layout. The current main layout is left.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SwitchLayoutCommand extends AbstractConfiguredInstance<SwitchLayoutCommand.Config> implements Command {

	/**
	 * Typed configuration interface definition for {@link SwitchLayoutCommand}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<SwitchLayoutCommand> {

		/** Configuration name of the value of {@link #getLayoutName()}. */
		String LAYOUT_NAME_NAME = "layoutName";

		/**
		 * Name the layout file which contains the main layout to load.
		 * 
		 * <p>
		 * The layout name is the path to the layout file, starting from the layouts folder, e.g.
		 * <code>otherMain/masterFrame.xml</code> references to the file
		 * <code>WEB-INF/layouts/otherMain/masterFrame.xml</code>.
		 * </p>
		 */
		@Mandatory
		@Name(LAYOUT_NAME_NAME)
		String getLayoutName();

		/**
		 * Setter for {@link #getLayoutName()}.
		 */
		void setLayoutName(String layout);

	}

	/**
	 * Create a {@link SwitchLayoutCommand}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SwitchLayoutCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {

		storePersonalConfiguration();

		String layoutName = getConfig().getLayoutName();
		WindowId subsessionId = new WindowId();
		TLSessionContext session = context.getSessionContext();
		TLSubSessionContext currentSubsession = context.getSubSessionContext();
		Locale locale = currentSubsession.getCurrentLocale();
		TimeZone timeZone = currentSubsession.getCurrentTimeZone();
		Person person = currentSubsession.getPerson();
		SubsessionHandler newLayoutContext =
			session.getHandlersRegistry().startLogin(session, layoutName, subsessionId, person, locale, timeZone);

		TLSubSessionContext subSession = TLContextManager.getSession().getSubSession(subsessionId.getWindowName());
		TLContextManager.initLayoutContext(subSession, newLayoutContext);

		context.getWindowScope().getTopLevelFrameScope().addClientAction(changeToSubSession(subsessionId));

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Stores the current {@link PersonalConfiguration}.
	 * 
	 * <p>
	 * The {@link PersonalConfiguration} is currently part of the {@link SubSessionContext}, and
	 * made persistent when the {@link SessionContext} is closed. Therefore after creating a new
	 * {@link SubSessionContext} there are two different {@link PersonalConfiguration}s.
	 * 
	 * When the {@link SessionContext} is closed, both {@link PersonalConfiguration} are made
	 * persistent, so the second {@link PersonalConfiguration} overrides the changes of the first.
	 * </p>
	 */
	private void storePersonalConfiguration() {
		PersonalConfiguration.storePersonalConfiguration();
	}

	private JSSnipplet changeToSubSession(WindowId subsessionId) {
		return new JSSnipplet((context, out) -> {
			out.append("window.name = ");
			TagUtil.writeJsString(out, subsessionId.getWindowName());
			out.append(";");

			out.append("window.location = ");
			TagUtil.beginJsString(out);
			LayoutUtils.appendFullLayoutServletURL(context, out);
			out.append('/');
			out.append(subsessionId.getEncodedForm());
			TagUtil.endJsString(out);
			out.append(';');
		});
	}

}

