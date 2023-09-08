/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.Command.CommandChain;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.fragments.Tag;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractFormDialogBase} to fill a {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractCreateConfigurationDialog<C> extends AbstractFormDialogBase {

	private ResKey _headerKey = ResKey.NONE;

	private ResKey _messageKey = ResKey.NONE;

	private Function<? super C, HandlerResult> _okHandle = null;

	/**
	 * Creates a new {@link AbstractCreateConfigurationDialog}.
	 * 
	 * @see AbstractFormDialogBase#AbstractFormDialogBase(DialogModel)
	 */
	public AbstractCreateConfigurationDialog(DialogModel dialogModel) {
		super(dialogModel);
	}

	/**
	 * {@link ResKey} providing the header text of the dialog. {@link ResKey#NONE} means no header
	 * text.
	 */
	public ResKey getHeaderKey() {
		return _headerKey;
	}

	/**
	 * Setter for {@link #getHeaderKey()}.
	 * 
	 * @see #getHeaderKey()
	 */
	public void setHeaderKey(ResKey headerKey) {
		_headerKey = Objects.requireNonNull(headerKey, "Null not allowed. Use ResKey#NONE for no header.");
	}

	/**
	 * {@link ResKey} providing an additional message of the dialog. {@link ResKey#NONE} means no
	 * message text.
	 */
	public ResKey getMessageKey() {
		return _messageKey;
	}

	/**
	 * Setter for {@link #getMessageKey()}.
	 * 
	 * @see #getMessageKey()
	 */
	public void setMessageKey(ResKey messageKey) {
		_messageKey = Objects.requireNonNull(messageKey, "Null not allowed. Use ResKey#NONE for no message.");
		
	}

	@Override
	protected HTMLFragment createView() {
		Tag content = div("mboxInput", DefaultFormFieldControlProvider.INSTANCE.createControl(getFormContext()));
		HTMLFragment header;
		if (getHeaderKey() != ResKey.NONE) {
			header = div("mboxHeader", message(getHeaderKey()));
		} else {
			header = empty();
		}
		HTMLFragment message;
		if (getMessageKey() != ResKey.NONE) {
			message = div("mboxMessage", message(getMessageKey()));
		} else {
			message = empty();
		}
		return div("mboxContent",
			header,
			message,
			content
			);
	}

	/**
	 * Setter for {@link #getOkHandle()}.
	 */
	public void setOkHandle(Function<? super C, HandlerResult> okHandle) {
		_okHandle = okHandle;
	}

	/**
	 * {@link Function} to call with the newly created {@link ConfigurationItem}.
	 * 
	 * @return May be <code>null</code>.
	 */
	public Function<? super C, HandlerResult> getOkHandle() {
		return _okHandle;
	}

	/**
	 * Returns the configuration model.
	 */
	public abstract C getModel();

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(getOkButton());
		buttons.add(getCancelButton());
	}

	/**
	 * Canceling the dialog and discard the configuration.
	 * 
	 * Note: Hook to enable subclasses to create a custom cancel button.
	 */
	protected CommandModel getCancelButton() {
		return MessageBox.button(ButtonType.CANCEL, getDiscardClosure());
	}

	/**
	 * Apply the configuration.
	 * 
	 * Note: Hook to enable subclasses to create a custom apply button.
	 */
	protected CommandModel getOkButton() {
		return MessageBox.button(ButtonType.OK, getApplyClosure());
	}

	@Override
	public Command getApplyClosure() {
		return new CommandChain(checkContextCommand(), this::beforeSave, setResult(), close());
	}

	/**
	 * Hook being called before the newly created configuration is passed to the
	 * {@link #getOkHandle()}.
	 * 
	 * @param context
	 *        The current command context, see {@link Command#executeCommand(DisplayContext)}.
	 */
	protected HandlerResult beforeSave(DisplayContext context) {
		return HandlerResult.DEFAULT_RESULT;
	}

	private Command close() {
		return getDialogModel().getCloseAction();
	}

	private Command setResult() {
		Function<? super C, HandlerResult> okHandle = getOkHandle();
		if (okHandle == null) {
			return Command.DO_NOTHING;
		} else {
			return (DisplayContext context) -> {
				return okHandle.apply(getModel());
			};
		}
	}
}

