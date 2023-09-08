/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.addons.loginmessages.layout;

import java.util.Collection;

import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.shared.string.StringServicesShared;
import com.top_logic.element.layout.meta.DefaultFormContextModificator;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.Resources;

/**
 * A {@link DefaultFormContextModificator} for {@link LoginMessage}s.
 *
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public class LoginMessageFormContextModifier extends DefaultFormContextModificator {

	/** Singleton {@link LoginMessageFormContextModifier} instance. */
	@SuppressWarnings("hiding")
	public static final LoginMessageFormContextModifier INSTANCE = new LoginMessageFormContextModifier();

	/**
	 * Creates a new {@link LoginMessageFormContextModifier}.
	 * 
	 */
	protected LoginMessageFormContextModifier() {
		// singleton instance
	}

	@Override
	public void postModify(LayoutComponent component, TLClass type, TLObject anAttributed, AttributeFormContext aContext,
			FormContainer currentGroup) {
		super.postModify(component, type, anAttributed, aContext, currentGroup);
		FormField messageField = findField(currentGroup, anAttributed, type, LoginMessage.MESSAGE_ATTR);
		FormField activeField = findField(currentGroup, anAttributed, type, LoginMessage.ACTIVE_ATTR);
		activeField.addWarningConstraint(new LoginMessageNotEmptyConstraint(messageField, (LoginMessage) anAttributed));
	}

	/**
	 * Returns the field for the given attribute in the given group.
	 */
	private FormField findField(FormContainer currentGroup, TLObject anAttributed, TLStructuredType type,
			String attributeName) {
		TLStructuredTypePart part = type.getPart(attributeName);
		if (part == null) {
			return null;
		}
		String fieldName = MetaAttributeGUIHelper.getAttributeID(part, anAttributed);
		return currentGroup.hasMember(fieldName) ? currentGroup.getField(fieldName) : null;
	}

	/**
	 * A {@link Constraint} ensuring that the message ({@link LoginMessage#MESSAGE_ATTR} 
	 * of a {@link LoginMessage} is not empty.
	 *
	 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
	 */
	public class LoginMessageNotEmptyConstraint implements Constraint {

		private final FormField _messageField;

		private final LoginMessage _loginMessage;

		/**
		 * Creates a new {@link LoginMessageNotEmptyConstraint} for the given {@link FormField} or
		 * {@link LoginMessage}.
		 */
		public LoginMessageNotEmptyConstraint(FormField messageField, LoginMessage loginMessage) {
			super();
			_messageField = messageField;
			_loginMessage = loginMessage;
		}

		@Override
		public boolean check(Object value) throws CheckException {
			boolean active = FormFieldHelper.getbooleanValue(value);
			String message = null;
			if (_messageField != null) {
				message = FormFieldHelper.getStringValue(_messageField.getValue());
			} else if (_loginMessage != null) {
				message = _loginMessage.getMessage().localizeSourceCode();
			}
			if (active && StringServicesShared.nonNull(message).isEmpty()) {
				throw new CheckException(
					Resources.getInstance().getMessage(I18NConstants.LOGIN_MESSAGE_WARNING_EMPTY_MESSAGE, value));
			}
			return true;
		}

		@Override
		public Collection<FormField> reportDependencies() {
			return CollectionUtil.intoListNotNull(_messageField);
		}
	}
}
