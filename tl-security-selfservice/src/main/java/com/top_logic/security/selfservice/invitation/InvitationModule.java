/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.security.selfservice.model.Invitation;

/**
 * {@link ConfiguredManagedClass} for {@link Invitation}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Service module for invitations")
public class InvitationModule extends ConfiguredManagedClass<InvitationModule.Config> {

	/**
	 * Typed configuration interface definition for {@link InvitationModule}.
	 */
	@DisplayOrder({
		Config.INVITATION_MAIL,
		Config.VERIFICATION_MAIL,
		Config.TOKEN_VALIDITY,
	})
	public interface Config extends ConfiguredManagedClass.Config<InvitationModule> {

		/** Configuration name for {@link #getInvitationMail()}. */
		String INVITATION_MAIL = "invitation-mail";

		/** Configuration name for {@link #getVerificationMail()}. */
		String VERIFICATION_MAIL = "verification-mail";

		/** Configuration name for {@link #getTokenValidity()}. */
		String TOKEN_VALIDITY = "token-validity";

		/** Configuration name for {@link #getTokenResendDelay()}. */
		String TOKEN_RESEND_DELAY = "token-resend-delay";

		/**
		 * The TL-Script expression sending the invitation mail.
		 *
		 * <p>
		 * The expression is called with the {@link Invitation} object as first argument, the
		 * application name as second argument, and the invitation link as third argument.
		 * </p>
		 */
		@Name(INVITATION_MAIL)
		@Mandatory
		Expr getInvitationMail();

		/**
		 * The TL-Script expression sending the verification mail.
		 *
		 * <p>
		 * The expression is called with the {@link Invitation} object as first argument, the
		 * application name as second argument, and the verification token as third argument.
		 * </p>
		 */
		@Name(VERIFICATION_MAIL)
		@Mandatory
		Expr getVerificationMail();

		/**
		 * How long a verification token remains valid after it was created.
		 */
		@Format(MillisFormat.class)
		@Name(TOKEN_VALIDITY)
		@Mandatory
		long getTokenValidity();

		/**
		 * The delay a user must wait before requesting a new verification token is possible.
		 *
		 * <p>
		 * The actual minimum wait time grows with each successive attempt: after the N-th request
		 * the user must wait at least N * {@link #getTokenResendDelay()} since the last token was
		 * issued.
		 * </p>
		 */
		@Format(MillisFormat.class)
		@Name(TOKEN_RESEND_DELAY)
		@Mandatory
		long getTokenResendDelay();

	}

	private QueryExecutor _verificationMail;

	private QueryExecutor _invitationMail;

	/**
	 * Create a {@link InvitationModule}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public InvitationModule(InstantiationContext context, Config config) {
		super(context, config);
		_verificationMail = QueryExecutor.compile(config.getVerificationMail());
		_invitationMail = QueryExecutor.compile(config.getInvitationMail());
	}

	/**
	 * Compiled executor for {@link Config#getInvitationMail()}.
	 */
	public QueryExecutor getInvitationMail() {
		return _invitationMail;
	}

	/**
	 * Compiled executor for {@link Config#getVerificationMail()}.
	 */
	public QueryExecutor getVerificationMail() {
		return _verificationMail;
	}

	/**
	 * Access to the single instance of {@link InvitationModule}.
	 */
	public static InvitationModule getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to the {@link InvitationModule}.
	 */
	public static final class Module extends TypedRuntimeModule<InvitationModule> {

		/**
		 * Singleton {@link InvitationModule.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<InvitationModule> getImplementation() {
			return InvitationModule.class;
		}

	}
}

