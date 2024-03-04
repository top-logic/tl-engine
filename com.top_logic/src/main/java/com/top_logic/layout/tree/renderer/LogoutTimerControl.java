/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Control} showing a session-timeout count-down.
 * 
 * <p>
 * After the count-down reaches zero, an active logout is performed.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LogoutTimerControl extends AbstractControlBase {

	/**
	 * Configuration options for {@link LogoutTimerControl}.
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * Amount of time in seconds the logout count-down is shown at the UI.
		 * 
		 * <p>
		 * A negative value means that the logout count-down is displayed after the given number of
		 * seconds (independently of the actual session timeout).
		 * </p>
		 * 
		 * @see HttpSession#getMaxInactiveInterval()
		 */
		@IntDefault(300)
		int getCountingSeconds();

	}

	private static final Property<Boolean> FIRST_VALIDATION = TypedAnnotatable.property(Boolean.class, "firstValidation", true);

	private static final Map<String, ControlCommand> COMMANDS =
		Collections.<String, ControlCommand> singletonMap(RefreshCommand.COMMAND_NAME, new RefreshCommand());

	/**
	 * Creates a {@link LogoutTimerControl}.
	 */
	public LogoutTimerControl() {
		super(COMMANDS);
	}

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected boolean hasUpdates() {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		return context.get(FIRST_VALIDATION).booleanValue();
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		markValid(context);
		actions.add(new JSSnipplet("services.form.LogoutTimerControl.update('" + getID() + "')"));
	}

	@Override
	protected String getTypeCssClass() {
		return "cLogoutTimer";
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		markValid(context);

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			String placeHolder = "{}";
			String text = context.getResources().getString(I18NConstants.SESSION_TIMEOUT_IN__TIME.fill(placeHolder));
			int idx = text.indexOf(placeHolder);
			if (idx < 0) {
				idx = text.length();
			}
			out.writeText(text.substring(0, idx));
			out.beginBeginTag(SPAN);
			out.writeAttribute(ID_ATTR, timerId());
			out.endBeginTag();
			out.endTag(SPAN);
			if (idx < text.length()) {
				out.writeText(text.substring(idx + placeHolder.length()));
			}
			out.writeText(' ');
			XMLTag tag = Icons.REFRESH_SMALL.toButton();
			tag.beginBeginTag(context, out);
			HTMLUtil.writeImageTooltip(context, out, context.getResources().getString(I18NConstants.SESSION_REFRESH));
			tag.endEmptyTag(context, out);

			out.beginScript();
			out.write("services.form.LogoutTimerControl.init(");
			out.writeJsString(getID());
			out.write(", ");
			out.writeInt(context.asRequest().getSession(false).getMaxInactiveInterval());
			out.write(", ");
			out.writeInt(ApplicationConfig.getInstance().getConfig(Config.class).getCountingSeconds());
			out.write(", ");
			out.writeJsString(context.getContextPath() + ApplicationPages.getInstance().getLogoutPage());
			out.write(");");
			out.endScript();
		}
		out.endTag(SPAN);
	}

	private void markValid(DisplayContext context) {
		context.set(FIRST_VALIDATION, Boolean.FALSE);
	}

	private String timerId() {
		return getID() + "-timer";
	}

	static class RefreshCommand extends ControlCommand {

		static final String COMMAND_NAME = "refresh";

		/**
		 * Creates a {@link RefreshCommand}.
		 */
		public RefreshCommand() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			// Just a ping.
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.SESSION_REFRESH_COMMAND;
		}

	}

}
