/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation.script;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.mail.Address;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.base.mail.script.SendMail;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.version.Version;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.URLPathBuilder;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.security.selfservice.invitation.CreateLoginHoook;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.security.selfservice.model.TlSecuritySelfserviceFactory;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script function to send an invitation mail.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SendInvitation extends GenericMethod {

	/**
	 * Creates a new {@link SendInvitation}.
	 */
	protected SendInvitation(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new SendInvitation(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLObject arg = asTLObjectNonNull(getArguments()[0], arguments[0]);
		if (!TLModelUtil.isCompatibleInstance(TlSecuritySelfserviceFactory.getInvitationType(), arg)) {
			throw new TopLogicException(
				I18NConstants.ERROR_NOT_AN_INVITATION__VAL_EXPR.fill(arg, getArguments()[0]));
		}

		Invitation invitation = (Invitation) arg;

		Address to = SendMail.toAddress(invitation.getEmail());
		MailSenderService mailService = MailSenderService.getInstance();
		MimeMessage message = mailService.createEmptyMessage();
		try {
			message.addRecipient(RecipientType.TO, to);

			String applicationName = Version.getApplicationName();
			message.setSubject(getSubject(invitation, applicationName), StringServices.UTF8);
			message.setDataHandler(SendMail.toDataHandler(getBody(invitation, applicationName), ToString::toString));

			return mailService.send(message, new ArrayList<>(), false);
		} catch (MessagingException ex) {
			throw new TopLogicException(
				com.top_logic.base.mail.script.I18NConstants.ERROR_SENDING_MAIL__TO_MSG.fill(to, ex.getMessage()), ex);
		}
	}

	private String getSubject(Invitation invitation, String applicationName) {
		return ToString.toString(InvitationModule.getInstance().getInvitationSubject().execute(invitation, applicationName));
	}

	private Object getBody(Invitation invitation, String applicationName) {
		String link = invitationLink(DefaultDisplayContext.getDisplayContext(), invitation);
		return InvitationModule.getInstance().getInvitationBody().execute(invitation, applicationName, link);
	}

	private String invitationLink(DisplayContext context, Invitation invitation) {
		StringBuilder urlBuilder = new StringBuilder(128);
		try {
			LayoutUtils.appendHostURL(context, urlBuilder);
			URLPathBuilder.newLayoutServletURLBuilder(context)
				.appendParameter(CreateLoginHoook.INVITATION_ID, invitation.getId())
				.appendTo(urlBuilder);

		} catch (IOException ex) {
			// StringBuilder does not throw IOException.
			throw new IOError(ex);
		}
		return urlBuilder.toString();
	}

	/**
	 * {@link MethodBuilder} for {@link SendInvitation}s.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<SendInvitation> {

		/** Description of parameters for a {@link SendInvitation}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("invitation")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public SendInvitation build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new SendInvitation(getName(), args);
		}

	}

}

