/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.addons.loginmessages.layout;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Date;

import com.top_logic.addons.loginmessages.model.LoginMessagesUtil;
import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.gui.layout.TLMainLayout;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBoxShortcuts;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControl;
import com.top_logic.layout.wysiwyg.ui.StructuredTextFieldFactory;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link TLMainLayout} able to show {@link LoginMessage}s.
 *
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public class LoginMessagesMainLayout extends TLMainLayout {

	/**
	 * Configuration for {@link LoginMessagesMainLayout}.
	 */
	public interface GlobalConfig extends ConfigurationItem {
		/**
		 * Whether to show {@link LoginMessage}s on application start. See
		 * {@link GlobalConfig#getShowLoginMessages}.
		 */
		String SHOW_LOGIN_MESSAGES = "showLoginMessages";

		/** Getter for {@link GlobalConfig#SHOW_LOGIN_MESSAGES}. */
		@Name(SHOW_LOGIN_MESSAGES)
		boolean getShowLoginMessages();
	}

	/**
	 * Default message window width in px. Can be overridden for each individual message.
	 */
	private static final Integer DEFAULT_MESSAGE_WINDOW_WIDTH = 550;

	/**
	 * Default message window width in px. Can be overridden for each individual message.
	 */
	private static final Integer DEFAULT_MESSAGE_WINDOW_HEIGHT = 300;

	private static final String MESSAGE_FIELD_NAME = "loginMessage";

	private final boolean _showLoginMessages;

	/**
	 * Creates a new {@link LoginMessagesMainLayout}.
	 */
	public LoginMessagesMainLayout(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		_showLoginMessages = ApplicationConfig.getInstance().getConfig(GlobalConfig.class).getShowLoginMessages();
	}

	/**
	 * Append the goto handling to the start page.
	 */
	@Override
	protected void initialValidateModel(DisplayContext aContext) {
		super.initialValidateModel(aContext);
		if (_showLoginMessages) {
			showLoginMessages(aContext);
		}
	}

	private void showLoginMessages(DisplayContext aContext) {
		final PersonalConfiguration configuration = PersonalConfiguration.getPersonalConfiguration();
		for (LoginMessage loginMessage : LoginMessagesUtil.getLoginMessagesSortedByNameDescending()) {
			showLoginMessage(aContext, configuration, loginMessage);
		}
	}

	private void showLoginMessage(DisplayContext aContext, final PersonalConfiguration configuration, LoginMessage loginMessage) {
		if (loginMessage.getActive()
			&& LoginMessagesUtil.isInTimeInterval(loginMessage)
			&& LoginMessagesUtil.isConfirmExpired(configuration, loginMessage)) {
			showLoginMessageDialog(aContext, configuration, loginMessage);
		}
	}

	private void showLoginMessageDialog(DisplayContext aContext, final PersonalConfiguration configuration,
			LoginMessage loginMessage) {
		final DefaultDialogModel dialogModel = createLoginMessageDialogModel(loginMessage);
		HTMLFragment dialogContent = createLoginMessageDialogContent(loginMessage);
		String confirmName = LoginMessagesUtil.getConfirmName(loginMessage);
		CommandModel dialogButton = createLoginMessageDialogButton(configuration, confirmName, dialogModel);
		MessageBoxShortcuts.open(aContext, dialogModel, dialogContent, CollectionUtil.intoList(dialogButton));
	}


	private HTMLFragment createLoginMessageDialogContent(LoginMessage loginMessage) {
		return (context, out) -> {
			I18NStructuredText message = loginMessage.getMessage();
			out.beginBeginTag(DIV);
			out.writeAttribute(STYLE_ATTR, "margin-left: 10px; margin-bottom: 10px; margin-right: 10px;");
			out.endBeginTag();
			if (message != null) {
				writeMessage(context, out, message);
			} else {
				out.beginTag(PARAGRAPH);
				out.writeText(loginMessage.getName());
				out.endTag(PARAGRAPH);
			}
			out.endTag(DIV);
		};
	}

	void writeMessage(DisplayContext context, TagWriter out, I18NStructuredText message) throws IOException {
		StructuredText localizedMessage = message.localize();
		FormField field = createField(localizedMessage);
		StructuredTextControl control = new StructuredTextControl(field);
		control.write(context, out);
	}

	private FormField createField(StructuredText message) {
		FormField structuredTextField = StructuredTextFieldFactory.create(MESSAGE_FIELD_NAME, message);
		structuredTextField.setImmutable(true);
		return structuredTextField;
	}

	private DefaultDialogModel createLoginMessageDialogModel(LoginMessage loginMessage) {
		Integer window_width = loginMessage.getWindowWidth();
		if (window_width == null) {
			window_width = DEFAULT_MESSAGE_WINDOW_WIDTH;
		}
		Integer window_height = loginMessage.getWindowHeight();
		if (window_height == null) {
			window_height = DEFAULT_MESSAGE_WINDOW_HEIGHT;
		}
		DisplayDimension width = DisplayDimension.dim(window_width, DisplayUnit.PIXEL);
		DisplayDimension height = DisplayDimension.dim(window_height, DisplayUnit.PIXEL);
		LayoutData layoutData = new DefaultLayoutData(width, 100, height, 100, Scrolling.AUTO);
		ResourceText dialogTitle = new ResourceText(I18NConstants.LOGIN_MESSAGE_DIALOG_TITLE);
		return new DefaultDialogModel(layoutData, dialogTitle, true, false, null);
	}

	private CommandModel createLoginMessageDialogButton(final PersonalConfiguration configuration,
			final String confirmName, final DefaultDialogModel dialogModel) {
		Command command = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				// store flag in personal configuration that login-message were confirmed by user
				configuration.setValue(confirmName, new Date());
				// close the message box
				dialogModel.getCloseAction().executeCommand(context);

				return HandlerResult.DEFAULT_RESULT;
			}
		};

		CommandModel dialogButton = MessageBox.button(ButtonType.OK, command);

		return dialogButton;
	}
}
