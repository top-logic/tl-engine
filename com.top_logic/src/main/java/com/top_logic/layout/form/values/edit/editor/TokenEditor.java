/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.func.Identity;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.MandatoryChangedListener;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Editor for displaying and managing token values in a secure manner.
 *
 * <p>
 * This editor creates a specialized field that displays token values in an encrypted/masked format.
 * The actual token value is hidden from the user interface for security purposes. Users can generate
 * new tokens via a dialog or clear existing token values using dedicated commands.
 * </p>
 *
 * <p>
 * The editor provides three main interactions:
 * </p>
 * <ul>
 * <li>Display the token field with a masked/encrypted representation of the value</li>
 * <li>A button to open a dialog for generating and setting a new token</li>
 * <li>A button to clear the current token value (only visible when field is not mandatory)</li>
 * </ul>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TokenEditor extends AbstractEditor {

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {
		
		StringField field = Fields.line(container, fieldName);

		init(editorFactory, model, field, Identity.getInstance(), Identity.getInstance());

		Resources resources = Resources.getInstance();

		CommandModel dialogOpener = createDialogOpener(field, resources);
		CommandModel clearCommand = createClearCommand(field, resources);

		template(field, horizontalBox(
			span(css(FormConstants.FLEXIBLE_CSS_CLASS),
				htmlTemplate(new DisplayValueControl(field))),
			span(Templates.css(FormConstants.FIXED_RIGHT_CSS_CLASS),
				htmlTemplate(new ButtonControl(dialogOpener)),
				htmlTemplate(new ButtonControl(clearCommand)),
				error())));

		return field;
	}

	/**
	 * Creates a command that opens a dialog for generating and setting a new token value.
	 *
	 * <p>
	 * The dialog opener button is only visible when the field is not immutable. It automatically
	 * updates its visibility when the field's immutable state changes.
	 * </p>
	 *
	 * @param tokenField
	 *        The string field that will receive the new token value.
	 * @param resources
	 *        The resources for internationalization.
	 * @return A command model configured with the dialog opener functionality.
	 */
	private CommandModel createDialogOpener(StringField tokenField, Resources resources) {
		CommandModel dialogOpener = new AbstractCommandModel() {

			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				ResKey dialogTitle = I18NConstants.UPDATE_TOKEN_DIALOG_TITLE;
				DisplayDimension width = DisplayDimension.dim(600, DisplayUnit.PIXEL);
				DisplayDimension height = DisplayDimension.dim(250, DisplayUnit.PIXEL);
				DisplayTokenDialog dialog =
					new DisplayTokenDialog(dialogTitle, width, height, tokenField::setAsString);
				return dialog.open(context);
			}
		};
		dialogOpener.setImage(Icons.UPDATE_TOKEN);
		dialogOpener.setLabel(resources.getString(I18NConstants.UPDATE_TOKEN_DIALOG_TITLE));

		tokenField.addListener(FormMember.IMMUTABLE_PROPERTY, new ImmutablePropertyListener() {
			
			@Override
			public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				dialogOpener.setVisible(!newValue.booleanValue());
				return Bubble.BUBBLE;
			}
		});
		dialogOpener.setVisible(!tokenField.isImmutable());

		return dialogOpener;
	}

	/**
	 * Creates a command for clearing the token field value.
	 *
	 * <p>
	 * The clear button is only visible when the field is not mandatory and not immutable. It is
	 * only executable when the field actually contains a value. The button automatically updates
	 * its visibility and executability based on the field's state changes.
	 * </p>
	 *
	 * @param tokenField
	 *        The string field whose value will be cleared.
	 * @param resources
	 *        The resources for internationalization.
	 * @return A command model configured with the clear functionality.
	 */
	private CommandModel createClearCommand(StringField tokenField, Resources resources) {
		CommandModel clearCommand = new AbstractCommandModel() {

			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				tokenField.setAsString(null);
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		clearCommand.setLabel(resources.getString(I18NConstants.CLEAR_TOKEN_FIELD_LABEL));
		clearCommand.setImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON);
		clearCommand.setNotExecutableImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED);

		class ClearExecutableListener implements ImmutablePropertyListener, MandatoryChangedListener {

			@Override
			public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
				updateCommandExecutability(newValue.booleanValue(), sender.isImmutable());
				return Bubble.BUBBLE;
			}

			@Override
			public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				updateCommandExecutability(((FormField) sender).isMandatory(), newValue.booleanValue());
				return Bubble.BUBBLE;
			}

			void updateCommandExecutability(boolean mandatory, boolean immutable) {
				clearCommand.setVisible(!mandatory && !immutable);
			}

		}
		ClearExecutableListener visibilityListener = new ClearExecutableListener();
		tokenField.addListener(FormMember.IMMUTABLE_PROPERTY, visibilityListener);
		tokenField.addListener(FormField.MANDATORY_PROPERTY, visibilityListener);
		visibilityListener.updateCommandExecutability(tokenField.isMandatory(), tokenField.isImmutable());

		class HasValueListener implements ValueListener {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				updateExecutability(newValue);
			}

			void updateExecutability(Object value) {
				if (StringServices.isEmpty(value)) {
					clearCommand.setNotExecutable(I18NConstants.TOKEN_FIELD_NO_VALUE);
				} else {
					clearCommand.setExecutable();
				}
			}
		}
		HasValueListener executabilityListener = new HasValueListener();
		// Initialize executability based on current field value
		executabilityListener.updateExecutability(tokenField.getValue());
		tokenField.addValueListener(executabilityListener);

		return clearCommand;
	}

	/**
	 * Control for displaying the token field value in a secure, masked format.
	 *
	 * <p>
	 * This control never displays the actual token value for security reasons. Instead, it shows a
	 * placeholder text indicating whether a token is present or not. When a token exists, it
	 * displays a generic "blocked value" message. When no token is present, it shows a "no value"
	 * message.
	 * </p>
	 *
	 * <p>
	 * The control automatically updates when the field's value changes by listening to value
	 * change events.
	 * </p>
	 */
	private static class DisplayValueControl extends AbstractVisibleControl implements ValueListener {

		private FormField _field;

		/**
		 * Creates a new {@link DisplayValueControl}.
		 *
		 * @param field
		 *        The form field whose value should be displayed in masked format.
		 */
		public DisplayValueControl(FormField field) {
			_field = field;
		}

		@Override
		public FormField getModel() {
			return _field;
		}

		@Override
		protected String getTypeCssClass() {
			return "cTokenFieldValue";
		}

		@Override
		protected void writeControlClassesContent(Appendable out) throws IOException {
			super.writeControlClassesContent(out);
			if (StringServices.isEmpty(getModel().getValue())) {
				out.append("noValue");
			}
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(SPAN);
			writeControlAttributes(context, out);
			out.endBeginTag();

			ResKey text;
			if (StringServices.isEmpty(getModel().getValue())) {
				text = I18NConstants.TOKEN_FIELD_NO_VALUE;
			} else {
				text = com.top_logic.layout.form.I18NConstants.BLOCKED_VALUE_TEXT;
			}
			out.writeText(context.getResources().getString(text));
			out.endTag(SPAN);
		}

		@Override
		protected void internalAttach() {
			super.internalAttach();
			getModel().addValueListener(this);
		}

		@Override
		protected void internalDetach() {
			getModel().removeValueListener(this);
			super.internalDetach();
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			requestRepaint();
		}
	}

	/**
	 * Dialog for generating and displaying a new secure token.
	 *
	 * <p>
	 * This dialog presents a newly generated token to the user in a read-only field. The token is
	 * generated using a secure random string generator. Users can accept the generated token (which
	 * applies it to the target field) or cancel to discard it.
	 * </p>
	 *
	 * <p>
	 * The token field in the dialog is immutable to prevent user modification, ensuring the token
	 * remains in its securely generated form.
	 * </p>
	 */
	public static class DisplayTokenDialog extends AbstractTemplateDialog {

		private static final String TOKEN_FIELD = "token";

		private Supplier<String> _tokenGenerator = SecureRandomService.getInstance()::getRandomString;

		private Consumer<String> _out;

		/**
		 * Creates a new {@link DisplayTokenDialog}.
		 *
		 * @param dialogTitle
		 *        The title to display in the dialog header.
		 * @param width
		 *        The width of the dialog window.
		 * @param height
		 *        The height of the dialog window.
		 * @param out
		 *        Consumer that receives the generated token when the user accepts the dialog. Must
		 *        not be <code>null</code>.
		 * @throws NullPointerException
		 *         if the consumer is <code>null</code>.
		 */
		public DisplayTokenDialog(ResKey dialogTitle, DisplayDimension width, DisplayDimension height,
				Consumer<String> out) {
			super(dialogTitle, width, height);
			setTokenConsumer(out);
		}

		/**
		 * Creates a new {@link DisplayTokenDialog}.
		 *
		 * @param dialogModel
		 *        The dialog model defining the dialog's appearance and behavior.
		 * @param out
		 *        Consumer that receives the generated token when the user accepts the dialog. Must
		 *        not be <code>null</code>.
		 * @throws NullPointerException
		 *         if the consumer is <code>null</code>.
		 */
		public DisplayTokenDialog(DialogModel dialogModel, Consumer<String> out) {
			super(dialogModel);
			setTokenConsumer(out);
		}

		/**
		 * Sets the consumer that receives the generated token.
		 *
		 * @param out
		 *        The consumer to call when the user accepts the dialog. Must not be
		 *        <code>null</code>.
		 * @throws NullPointerException
		 *         if the consumer is <code>null</code>.
		 */
		public void setTokenConsumer(Consumer<String> out) {
			_out = Objects.requireNonNull(out, "Token consumer must not be null.");
		}

		@Override
		protected TagTemplate getTemplate() {
			return div(
				resource(I18NConstants.UPDATE_TOKEN_DIALOG_MESSAGE),
				fieldBox(TOKEN_FIELD));
		}

		@Override
		protected void fillFormContext(FormContext context) {
			StringField tokenField = FormFactory.newStringField(TOKEN_FIELD);
			tokenField.setLabel(Resources.getInstance().getString(I18NConstants.UPDATE_TOKEN_DIALOG_TOKEN_FIELD_LABEL));
			tokenField.setMandatory(true);
			tokenField.initializeField(_tokenGenerator.get());
			context.addMember(tokenField);
		}

		@Override
		protected void fillButtons(List<CommandModel> buttons) {
			buttons.add(MessageBox.button(ButtonType.OK, getApplyClosure()));
			addCancel(buttons);
		}

		@Override
		public Command getApplyClosure() {
			return closeDialogAfter(checkContextCommand().andThen(dc -> {
				String token = ((StringField) getFormContext().getField(TOKEN_FIELD)).getAsString();
				_out.accept(token);
				return HandlerResult.DEFAULT_RESULT;
			}));
		}

	}

}

