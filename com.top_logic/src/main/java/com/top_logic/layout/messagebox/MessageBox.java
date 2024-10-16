/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.View;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelAdapter;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ResourceImageView;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.buttonbar.ButtonBarFactory;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.FixedFlowLayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Utility to produce simple confirm dialogs.
 * 
 * @see SimpleFormDialog Dialogs that allow user input.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MessageBox extends MessageBoxShortcuts {

	/**
	 * Configuration options for {@link MessageBox}.
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/**
		 * Default width of a message box.
		 * 
		 * @implNote Default value must remain the same as in top-logic.config.xml.
		 */
		@IntDefault(500)
		int getWidth();

		/**
		 * Default height of a message box.
		 * 
		 * @implNote Default value must remain the same as in top-logic.config.xml.
		 */
		@IntDefault(250)
		int getHeight();

		/**
		 * Whether the default message box can be resized.
		 */
		@BooleanDefault(true)
		boolean getResizable();

	}

	/**
	 * CSS class for the image DIV in the message box.
	 */
	static final String CSS_CLASS_MBOX_IMAGE = "mboxImage";

	/**
	 * CSS class for the content DIV in the message box.
	 */
	static final String CSS_CLASS_MBOX_CONTENT = "mboxContent";

	/**
	 * Type of a {@link MessageBox}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static enum MessageType {
		
		/**
		 * Type of a {@link MessageBox} that displays an information.
		 */
		INFO, 
		
		/**
		 * Type of a {@link MessageBox} that displays an error.
		 */
		ERROR, 

		/**
		 * Type of a {@link MessageBox} that displays a system failure.
		 */
		SYSTEM_FAILURE,
		
		/**
		 * Type of a {@link MessageBox} that displays an warning.
		 */
		WARNING, 
		
		/**
		 * Type of a {@link MessageBox} that asks a question.
		 */
		CONFIRM;

		/**
		 * The default name of this {@link MessageType}.
		 */
		public ResKey getTypeNameKey() {
			switch (this) {
				case INFO:
					return I18NConstants.INFO_TYPE_NAME;
				case ERROR:
					return I18NConstants.ERROR_TYPE_NAME;
				case SYSTEM_FAILURE:
					return I18NConstants.SYSTEM_FAILURE_TYPE_NAME;
				case WARNING:
					return I18NConstants.WARNING_TYPE_NAME;
				case CONFIRM:
					return I18NConstants.CONFIRM_TYPE_NAME;
			}
			throw new UnreachableAssertion("No such type: " + this);
		}
		
		/**
		 * The default icon for this {@link MessageType}.
		 */
		public View getTypeImage() {
			switch (this) {
				case INFO:
					return new ResourceImageView(I18NConstants.INFO_TYPE,
						com.top_logic.util.error.Icons.INFO);
				case ERROR:
					return new ResourceImageView(I18NConstants.ERROR_TYPE,
						com.top_logic.util.error.Icons.ERROR);
				case SYSTEM_FAILURE:
					return new ResourceImageView(I18NConstants.SYSTEM_FAILURE_TYPE,
						com.top_logic.util.error.Icons.ERROR);
				case WARNING:
					return new ResourceImageView(I18NConstants.WARNING_TYPE,
						com.top_logic.util.error.Icons.WARNING);
				case CONFIRM:
					return new ResourceImageView(I18NConstants.CONFIRM_TYPE,
						com.top_logic.util.error.Icons.QUESTION);
			}
			throw new UnreachableAssertion("No such type: " + this);
		}

		/**
		 * I18N constant for the title of the {@link MessageType}.
		 */
		public ResKey getTitleKey() {
			switch (this) {
				case INFO:
					return I18NConstants.INFO_TITLE;
				case ERROR:
					return I18NConstants.ERROR_TITLE;
				case SYSTEM_FAILURE:
					return I18NConstants.SYSTEM_FAILURE_TITLE;
				case WARNING:
					return I18NConstants.WARNING_TITLE;
				case CONFIRM:
					return I18NConstants.CONFIRM_TITLE;
			}
			throw new UnreachableAssertion("No such type: " + this);
		}
	}
	
	/**
	 * Type of a generic button displayed in a {@link MessageBox}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static enum ButtonType {
		/**
		 * Ok button.
		 */
		OK, 
		
		/**
		 * Yes button.
		 */
		YES, 
		
		/**
		 * Continue button.
		 */
		CONTINUE,
		
		/**
		 * Cancel button.
		 * 
		 * @implNote You may want to use {@link AbstractFormDialogBase#getDiscardClosure()} as
		 *           command.
		 */
		CANCEL, 
		
		/**
		 * No button.
		 */
		NO, 
		
		/**
		 * Close button.
		 * 
		 * @implNote You may want to use {@link AbstractFormDialogBase#getDiscardClosure()} as
		 *           command.
		 */
		CLOSE;

		/**
		 * I18N constant for the label of the button of given {@link ButtonType}.
		 */
		public ResKey getButtonLabelKey() {
			switch (this) {
			case OK:
				return I18NConstants.BUTTON_OK_LABEL;
			case YES:
				return I18NConstants.BUTTON_YES_LABEL;
			case CONTINUE:
				return I18NConstants.BUTTON_CONTINUE_LABEL;
			case CANCEL:
				return I18NConstants.BUTTON_CANCEL_LABEL;
			case CLOSE:
				return I18NConstants.BUTTON_CLOSE_LABEL;
			case NO:
				return I18NConstants.BUTTON_NO_LABEL;
			default:
				throw new UnreachableAssertion("No such type: " + this);
			}
		}
		
		/**
		 * Image of the button of given {@link ButtonType}.
		 */
		public ThemeImage getButtonImage() {
			switch (this) {
			case OK:
				return Icons.BUTTON_OK_IMAGE;
			case YES:
				return Icons.BUTTON_YES_IMAGE;
			case CONTINUE:
				return Icons.BUTTON_CONTINUE_IMAGE;
			case CANCEL:
				return Icons.BUTTON_CANCEL_IMAGE;
			case CLOSE:
				return Icons.BUTTON_CLOSE_IMAGE;
			case NO:
				return Icons.BUTTON_NO_IMAGE;
			default:
				throw new UnreachableAssertion("No such type: " + this);
			}
		}

		/**
		 * Disabled image of the button of given {@link ButtonType}.
		 */
		public ThemeImage getDisabledButtonImage() {
			switch (this) {
				case OK:
					return Icons.BUTTON_OK_IMAGE_DISABLED;
				case YES:
					return Icons.BUTTON_YES_IMAGE_DISABLED;
				case CONTINUE:
					return Icons.BUTTON_CONTINUE_IMAGE_DISABLED;
				case CANCEL:
					return Icons.BUTTON_CANCEL_IMAGE_DISABLED;
				case CLOSE:
					return Icons.BUTTON_CLOSE_IMAGE_DISABLED;
				case NO:
					return Icons.BUTTON_NO_IMAGE_DISABLED;
				default:
					throw new UnreachableAssertion("No such type: " + this);
			}
		}
	}

	/**
	 * Builder class to create a new message box of a given {@link MessageType}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class Builder {

		/** Empty {@link CommandModel} array. */
		protected static final CommandModel[] NO_BUTTONS = new CommandModel[0];

		/** The {@link MessageType} to create confirm message for. */
		protected final MessageType _type;

		/**
		 * The {@link View} displaying in the image area of the confirm dialog. May be
		 * <code>null</code>.
		 */
		protected View _image;

		/**
		 * The {@link HTMLFragment} displaying the title in the confirm dialog. May be
		 * <code>null</code>.
		 */
		protected HTMLFragment _title;

		/**
		 * The {@link HTMLFragment} displaying the title in the confirm dialog. May be
		 * <code>null</code>.
		 */
		protected HTMLFragment _content;

		/**
		 * The {@link CommandModel} to be displayed in the button area of the dialog. Must not be
		 * <code>null</code>.
		 */
		protected CommandModel[] _buttons = NO_BUTTONS;

		/**
		 * Definition of the layout of the result dialog. May be <code>null</code>.
		 */
		protected LayoutData _layout;

		/**
		 * Whether the dialog is resizable. May be <code>null</code>.
		 */
		protected Boolean _resizable;

		/**
		 * Creates a new {@link Builder} for the given {@link MessageType}.
		 * 
		 * @param type
		 *        The {@link MessageType} to create a confirm box for.
		 */
		protected Builder(MessageType type) {
			_type = type;
		}
		
		/**
		 * Sets the image in the result box.
		 * 
		 * @return This {@link Builder}.
		 */
		public Builder image(View image) {
			_image = image;
			return this;
		}

		/**
		 * Sets the title of the result dialog.
		 * 
		 * @return This {@link Builder}.
		 */
		public Builder title(HTMLFragment title) {
			_title = title;
			return this;
		}

		/**
		 * Sets the title of the result dialog.
		 * 
		 * @return This {@link Builder}.
		 */
		public Builder title(String title) {
			return title(new ConstantDisplayValue(title));
		}

		/**
		 * Sets the title of the result dialog.
		 * 
		 * @return This {@link Builder}.
		 */
		public Builder title(ResKey title) {
			return title(new ResourceText(title));
		}

		/**
		 * Sets the message of the confirm dialog.
		 * 
		 * @return This {@link Builder}.
		 */
		public Builder message(String message) {
			return message(new ConstantDisplayValue(message));
		}

		/**
		 * Sets the message of the confirm dialog.
		 * 
		 * @return This {@link Builder}.
		 */
		public Builder message(ResKey message) {
			return message(new ResourceText(message));
		}

		/**
		 * Sets the message of the confirm dialog.
		 * 
		 * @return This {@link Builder}.
		 */
		public Builder message(HTMLFragment message) {
			_content = message;
			return this;
		}

		/**
		 * Sets the buttons to display in the button bar of the confirm dialog.
		 * <p>
		 * The first button is used as the {@link DialogModel#getDefaultCommand()}.
		 * </p>
		 * 
		 * @return This {@link Builder}.
		 */
		public Builder buttons(CommandModel... buttons) {
			_buttons = Objects.requireNonNull(buttons, "Buttons must not be null.");
			return this;
		}

		/**
		 * Defines the layout of the confirm dialog.
		 * 
		 * @return This {@link Builder}.
		 */
		public Builder layout(LayoutData layout) {
			_layout = layout;
			return this;
		}

		/**
		 * Defines the dialog size.
		 */
		public Builder layout(DisplayDimension width, DisplayDimension height) {
			return layout(new DefaultLayoutData(width, 100, height, 100, Scrolling.AUTO));
		}

		/**
		 * Defines whether the confirm dialog is resizable.
		 * 
		 * @return This {@link Builder}.
		 */
		public Builder resizable(boolean resizable) {
			_resizable = Boolean.valueOf(resizable);
			return this;
		}

		/**
		 * Opens a confirm dialog displaying the informations given in this {@link Builder}.
		 * 
		 * @param context
		 *        The current display context.
		 * 
		 * @return {@link HandlerResult} returned by the opening process.
		 */
		public HandlerResult confirm(DisplayContext context) {
			return confirm(context.getWindowScope());
		}

		/**
		 * Opens a confirm dialog displaying the informations given in this {@link Builder}.
		 * 
		 * @param window
		 *        The window in which the dialog must be opened.
		 * 
		 * @return {@link HandlerResult} returned by the opening process.
		 */
		public HandlerResult confirm(WindowScope window) {
			window.openDialog(toDialog());
			return HandlerResult.DEFAULT_RESULT;
		}

		/**
		 * Creates the dialog displaying the informations given in this {@link Builder}.
		 */
		public DialogWindowControl toDialog() {
			LayoutData layout = _layout != null ? _layout : MessageBox.getDefaultLayout();
			boolean resizable = _resizable != null ? _resizable.booleanValue() : MessageBox.isDefaultResizable();
			HTMLFragment title = _title != null ? _title : new ResourceText(_type.getTitleKey());
			HTMLFragment image = _image != null ? _image : _type.getTypeImage();
			HTMLFragment content = _content != null ? _content : Fragments.empty();
			HTMLFragment message = new MessageArea(image) {

				@Override
				protected void writeMessage(DisplayContext context, TagWriter out) throws IOException {
					content.write(context, out);
				}
			};
			return MessageBox.confirmDialog(layout, resizable, title, message, _buttons);
		}

	}

	/**
	 * Creates a new {@link Builder} for a confirm message box.
	 * 
	 * @param type
	 *        The {@link MessageType} for the confirm message.
	 */
	public static Builder newBuilder(MessageType type) {
		return new Builder(type);
	}

	/**
	 * Create a button of the given type that performs no action at all.
	 * 
	 * @param type
	 *        The {@link ButtonType} of the button.
	 * @return The button to be used in e.g.
	 *         {@link #confirm(WindowScope, MessageType, String, CommandModel...)}
	 */
	public static CommandModel button(ButtonType type) {
		return button(type, Command.DO_NOTHING);
	}

	/**
	 * Create a button of the given type that executes the given {@link Command}.
	 * 
	 * @param type
	 *        The {@link ButtonType} of the button.
	 * @param continuation
	 *        A {@link Command} that should be executed, if the user presses the button.
	 * @return The button to be used in e.g.
	 *         {@link #confirm(WindowScope, MessageType, String, CommandModel...)}
	 */
	public static CommandModel button(ButtonType type, Command continuation) {
		return button(type.getButtonLabelKey(), type.getButtonImage(), type.getDisabledButtonImage(), continuation);
	}
	
	/**
	 * Create a button with given properties.
	 * 
	 * @param labelKey
	 *        The resource key that generates the label.
	 * @param continuation
	 *        A {@link Command} that should be executed, if the user presses the button.
	 * @return The button to be used in e.g.
	 *         {@link #confirm(WindowScope, MessageType, String, CommandModel...)}
	 */
	public static CommandModel button(ResKey labelKey, Command continuation) {
		return button(labelKey, null, continuation);
	}

	/**
	 * Create a button with given properties, styled with forward image.
	 * 
	 * @param labelKey
	 *        The resource key that generates the label.
	 * @param continuation
	 *        A {@link Command} that should be executed, if the user presses the button.
	 * @return The button to be used in e.g.
	 *         {@link #confirm(WindowScope, MessageType, String, CommandModel...)}
	 */
	public static CommandModel forwardStyleButton(ResKey labelKey, Command continuation) {
		CommandModel button = button(labelKey, continuation);
		return button;
	}

	/**
	 * Create a button with given properties.
	 * 
	 * @param labelKey
	 *        The resource key that generates the label.
	 * @param image
	 *        The button image.
	 * @param continuation
	 *        A {@link Command} that should be executed, if the user presses the button.
	 * @return The button to be used in e.g.
	 *         {@link #confirm(WindowScope, MessageType, String, CommandModel...)}
	 */
	public static CommandModel button(ResKey labelKey, ThemeImage image, Command continuation) {
		return button(labelKey, image, image, continuation);
	}

	/**
	 * Create a button with given properties.
	 * 
	 * @param labelKey
	 *        The resource key that generates the label.
	 * @param image
	 *        The button image.
	 * @param continuation
	 *        A {@link Command} that should be executed, if the user presses the button.
	 * @return The button to be used in e.g.
	 *         {@link #confirm(WindowScope, MessageType, String, CommandModel...)}
	 */
	public static CommandModel button(ResKey labelKey, ThemeImage image, ThemeImage disabledImage, Command continuation) {
		CommandModel result = CommandModelFactory.commandModel(continuation);
		Resources resources = Resources.getInstance();
		result.setLabel(resources.getString(labelKey));
		result.setTooltip(resources.getString(labelKey.tooltipOptional()));
		result.setImage(image);
		result.setNotExecutableImage(disabledImage);
		return result;
	}

	/**
	 * Opens a confirm dialog that displays the given message and buttons.
	 * 
	 * @param windowScope
	 *        The window to open the message box in.
	 * @param type
	 *        The {@link MessageType} of the confirm dialog.
	 * @param message
	 *        The message displayed in the dialog.
	 * @param buttons
	 *        The buttons to be shown. Must not be empty. All buttons implicitly close the
	 *        {@link MessageBox} no matter what the inner {@link Command} does. The first button is
	 *        used as the {@link DialogModel#getDefaultCommand()}.
	 * @return The result that must be immediately returned from the invoking command handler that
	 *         opens the {@link MessageBox}.
	 * 
	 * @see #button(ButtonType, Command) Creating simple buttons with predefined type.
	 * @see #confirm(WindowScope, MessageType, String, String, CommandModel...) Displaying a custom
	 *      dialog title.
	 */
	public static HandlerResult confirm(WindowScope windowScope,
			final MessageType type, final String message,
			final CommandModel... buttons) {
		return newBuilder(type).message(message).buttons(buttons).confirm(windowScope);
	}
	
	/**
	 * Opens a confirm dialog that displays the given message and buttons.
	 * 
	 * @param windowScope
	 *        The window to open the message box in.
	 * @param type
	 *        The {@link MessageType} of the confirm dialog.
	 * @param title
	 *        The dialog title.
	 * @param message
	 *        The message displayed in the dialog.
	 * @param buttons
	 *        The buttons to be shown. Must not be empty. All buttons implicitly close the
	 *        {@link MessageBox} no matter what the inner {@link Command} does. The first button is
	 *        used as the {@link DialogModel#getDefaultCommand()}.
	 * @return The result that must be immediately returned from the invoking command handler that
	 *         opens the {@link MessageBox}.
	 * 
	 * @see #button(ButtonType, Command) Creating simple buttons with predefined type.
	 * @see #confirm(WindowScope, MessageType, String, CommandModel...) Simple variant with default
	 *      title.
	 */
	public static HandlerResult confirm(WindowScope windowScope,
			final MessageType type, String title, final String message,
			final CommandModel... buttons) {
		return newBuilder(type).title(title).message(message).buttons(buttons).confirm(windowScope);
	}

	/**
	 * Open a confirm dialog that displays the given message and buttons.
	 * 
	 * @param windowScope
	 *        The window to open the message box in.
	 * @param layout
	 *        A custom size of the dialog.
	 * @param resizable
	 *        Whether the dialog should be resizable.
	 * @param type
	 *        The {@link MessageType} of the confirm dialog.
	 * @param title
	 *        The dialog title.
	 * @param message
	 *        The message displayed in the dialog.
	 * @param buttons
	 *        The buttons to be shown. Must not be empty. All buttons implicitly close the
	 *        {@link MessageBox} no matter what the inner {@link Command} does. The first button is
	 *        used as the {@link DialogModel#getDefaultCommand()}.
	 * @return The result that must be immediately returned from the invoking command handler that
	 *         opens the {@link MessageBox}.
	 * 
	 * @see #button(ButtonType, Command) Creating simple buttons with predefined type.
	 * @see #confirm(WindowScope, MessageType, String, CommandModel...) Simple variant with default
	 *      title.
	 */
	public static HandlerResult confirm(WindowScope windowScope,
			LayoutData layout, boolean resizable, final MessageType type, String title, final String message,
			final CommandModel... buttons) {
		return newBuilder(type)
			.layout(layout)
			.resizable(resizable)
			.title(title)
			.message(message)
			.buttons(buttons)
			.confirm(windowScope);
	}

	/**
	 * Open a confirm dialog that displays the given message and buttons.
	 * 
	 * @param windowScope
	 *        The window to open the message box in.
	 * @param layout
	 *        A custom size of the dialog.
	 * @param resizable
	 *        Whether the dialog should be resizable.
	 * @param title
	 *        The dialog title value.
	 * @param message
	 *        The structured text message displayed in the dialog.
	 * @param buttons
	 *        The buttons to be shown. Must not be empty. All buttons implicitly close the
	 *        {@link MessageBox} no matter what the inner {@link Command} does.
	 * @return The result that must be immediately returned from the invoking command handler that
	 *         opens the {@link MessageBox}.
	 * 
	 * @see #button(ButtonType, Command) Creating simple buttons with predefined type.
	 * @see #confirm(WindowScope, LayoutData, boolean, MessageType, String, String, CommandModel...)
	 */
	public static HandlerResult confirm(WindowScope windowScope, 
			LayoutData layout, boolean resizable, HTMLFragment title,
			HTMLFragment message, final CommandModel... buttons) {
		
		DialogWindowControl dialog = confirmDialog(layout, resizable, title, message, buttons);

		windowScope.openDialog(dialog);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Creates a confirm dialog that displays the given message and buttons.
	 * 
	 * @param layout
	 *        A custom size of the dialog.
	 * @param resizable
	 *        Whether the dialog should be resizable.
	 * @param title
	 *        The dialog title value.
	 * @param message
	 *        The structured text message displayed in the dialog.
	 * @param buttons
	 *        The buttons to be shown. Must not be empty. All buttons implicitly close the
	 *        {@link MessageBox} no matter what the inner {@link Command} does. The first button is
	 *        taken as the {@link DialogModel#getDefaultCommand() default command}.
	 * 
	 * @return The dialog that can be opened.
	 * 
	 * @see #button(ButtonType, Command) Creating simple buttons with predefined type.
	 * @see #confirm(WindowScope, LayoutData, boolean, MessageType, String, String, CommandModel...)
	 */
	public static DialogWindowControl confirmDialog(LayoutData layout, boolean resizable, HTMLFragment title,
			HTMLFragment message, CommandModel... buttons) {
		assert buttons != null && buttons.length > 0 : "No buttons given.";
		
		final DialogModel dialogModel = new DefaultDialogModel(layout, title, resizable, true, null);
		dialogModel.setDefaultCommand(buttons[0]);
		// Wrap actions so that the dialog is closed after each action. 
		final List<CommandModel> wrappedButtons = new ArrayList<>(buttons.length);
		for (CommandModel button : buttons) {
			wrappedButtons.add(new ClosingCommand(dialogModel, button));
		}
		DialogWindowControl dialog = createDialog(dialogModel, new MessageBoxContentView(message), wrappedButtons);
		return dialog;
	}

	/**
	 * Creates and opens a simple dialog with content area and list of buttons.
	 * 
	 * @param windowScope
	 *        The window to open the message box in.
	 * @param dialogModel
	 *        see {@link #createDialog(DialogModel, HTMLFragment, List)}
	 * @param content
	 *        see {@link #createDialog(DialogModel, HTMLFragment, List)}
	 * @param buttons
	 *        see {@link #createDialog(DialogModel, HTMLFragment, List)}
	 * @return The result that must be immediately returned from the invoking command handler that
	 *         opens the {@link MessageBox}.
	 * 
	 * @see #button(ButtonType, Command) Creating simple buttons with predefined type.
	 * 
	 * @see #createDialog(DialogModel, HTMLFragment, List) The dialog created to be opened
	 */
	public static HandlerResult open(WindowScope windowScope, DialogModel dialogModel, HTMLFragment content, List<CommandModel> buttons) {
		final DialogWindowControl dialog = createDialog(dialogModel, content, buttons);
		
		windowScope.openDialog(dialog);
		
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Creates a simple dialog with content area and list of buttons.
	 * 
	 * @param dialogModel
	 *        The model describing the dialog to open.
	 * @param content
	 *        The content to be rendered within the content area of the dialog.
	 * @param buttons
	 *        The buttons to be shown. Must not be empty. The button does not implicitly close the
	 *        dialog. To close the dialog, the user {@link Command} must forward execution to
	 *        {@link DialogModel#getCloseAction()} of the given {@link DialogModel}. If there is
	 *        just one button, it is also used as the {@link DialogModel#getDefaultCommand() default
	 *        command}.
	 * 
	 * @return The dialog to open
	 */
	public static DialogWindowControl createDialog(DialogModel dialogModel, HTMLFragment content,
			List<CommandModel> buttons) {
		if (buttons.size() == 1) {
			dialogModel.setDefaultCommand(buttons.get(0));
		}
		final DialogWindowControl dialog = new DialogWindowControl(dialogModel);
		
		// Dialog layout
		{
			FixedFlowLayoutControl dialogLayout = new FixedFlowLayoutControl(Orientation.VERTICAL);
			dialog.setChildControl(dialogLayout);
			
			// Content.
			{
				LayoutControlAdapter contentLayout = new LayoutControlAdapter(content);
				contentLayout.setConstraint(new DefaultLayoutData(DisplayDimension.HUNDERED_PERCENT, 100,
					DisplayDimension.HUNDERED_PERCENT, 100, Scrolling.AUTO));
				dialogLayout.addChild(contentLayout);
			}
			
			// Button bar.
			{
				LayoutControlAdapter buttonLayout =
					new LayoutControlAdapter(ButtonBarFactory.createButtonBar(buttons));
				
				DisplayDimension buttonBarHeight =
					ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.BUTTON_COMP_HEIGHT);
				
				buttonLayout.setConstraint(new DefaultLayoutData(DisplayDimension.HUNDERED_PERCENT, 100,
					buttonBarHeight, 100, Scrolling.NO));
				dialogLayout.addChild(buttonLayout);
			}
		}
		return dialog;
	}

	/**
	 * @see GlobalConfig#getResizable()
	 */
	public static boolean isDefaultResizable() {
		return ApplicationConfig.getInstance().getConfig(GlobalConfig.class).getResizable();
	}

	/**
	 * The default size of a message box.
	 * 
	 * @see GlobalConfig#getWidth()
	 * @see GlobalConfig#getHeight()
	 */
	public static LayoutData getDefaultLayout() {
		GlobalConfig config = ApplicationConfig.getInstance().getConfig(GlobalConfig.class);

		int width = config.getWidth();
		int height = config.getHeight();

		return new DefaultLayoutData(
			DisplayDimension.dim(width, DisplayUnit.PIXEL), 100,
			DisplayDimension.dim(height, DisplayUnit.PIXEL), 100, Scrolling.AUTO);
	}

	/**
	 * {@link CommandModelAdapter} that closes a dialog after executing the inner {@link Command}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static final class ClosingCommand extends CommandModelAdapter {
		
		final DialogModel dialogModel;

		public ClosingCommand(DialogModel dialogModel, CommandModel action) {
			super(action);
			this.dialogModel = dialogModel;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			// Close message box (unconditionally). Before the actual command execution,
			// because this might crash with a RuntimeException.
			dialogModel.getCloseAction().executeCommand(context);
			
			// Execute custom command.
			return super.executeCommand(context);
		}
	}

}
