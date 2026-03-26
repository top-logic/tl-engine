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
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Bounds;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.security.selfservice.model.TlSecuritySelfserviceFactory;

/**
 * {@link ConfiguredManagedClass} for elements in
 * {@value TlSecuritySelfserviceFactory#TL_SECURITY_SELFSERVICE_STRUCTURE}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Service module for invitations")
public class InvitationModule extends KBBasedManagedClass<InvitationModule.Config> {

	/**
	 * Typed configuration interface definition for {@link InvitationModule}.
	 */
	@DisplayOrder({
		Config.INVITATION_MAIL,
		Config.VERIFICATION_MAIL,
		Config.RESET_PASSWORD_MAIL,
		Config.RESET_MFA_MAIL,
		Config.CODE_VALIDITY,
		Config.VERIFICATION_CODE_SIZE,
		Config.PASSWORD_RESET_TIMEOUT,
	})
	public interface Config extends KBBasedManagedClass.Config<InvitationModule> {

		/** Configuration name for {@link #getInvitationMail()}. */
		String INVITATION_MAIL = "invitation-mail";

		/** Configuration name for {@link #getVerificationMail()}. */
		String VERIFICATION_MAIL = "verification-mail";

		/** Configuration name for {@link #getResetPasswordMail()}. */
		String RESET_PASSWORD_MAIL = "reset-password-mail";

		/** Configuration name for {@link #getResetMFAMail()}. */
		String RESET_MFA_MAIL = "reset-mfa-mail";

		/** Configuration name for {@link #getCodeValidity()}. */
		String CODE_VALIDITY = "code-validity";

		/** Configuration name for {@link #getPasswortResetTimeOut()}. */
		String PASSWORD_RESET_TIMEOUT = "password-reset-timeout";

		/** Configuration name for {@link #getVerificationCodeSize()}. */
		String VERIFICATION_CODE_SIZE = "verification-code-size";

		/**
		 * The TL-Script expression sending the invitation mail.
		 *
		 * <p>
		 * The expression is called with the {@link Invitation} object as first argument, the
		 * application title as second argument, and the invitation link as third argument.
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
		 * application title as second argument, and the verification code as third argument.
		 * </p>
		 */
		@Name(VERIFICATION_MAIL)
		@Mandatory
		Expr getVerificationMail();

		/**
		 * The TL-Script expression that sends an email when a password reset is requested.
		 *
		 * <p>
		 * The expression is called with the email as first argument, the application title as
		 * second argument, and the reset code as third argument.
		 * </p>
		 */
		@Name(RESET_PASSWORD_MAIL)
		@Mandatory
		Expr getResetPasswordMail();

		/**
		 * The TL-Script expression that sends an email when reset of the multi-factor
		 * authentication is requested.
		 *
		 * <p>
		 * The expression is called with the email as first argument, the application title as
		 * second argument, and the reset code as third argument.
		 * </p>
		 */
		@Name(RESET_MFA_MAIL)
		@Mandatory
		@Label("Reset multi-factor-authentication mail")
		Expr getResetMFAMail();

		/**
		 * How long a verification code remains valid after it was created.
		 */
		@Format(MillisFormat.class)
		@Name(CODE_VALIDITY)
		@Mandatory
		long getCodeValidity();

		/**
		 * Time in milliseconds how long a password reset request is valid.
		 */
		@Format(MillisFormat.class)
		@Name(PASSWORD_RESET_TIMEOUT)
		@Mandatory
		long getPasswortResetTimeOut();

		/**
		 * The number of digits that are used to create a verification code.
		 */
		@Mandatory
		@Bounds({
			@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 4),
			@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = 18)
		})
		@Name(VERIFICATION_CODE_SIZE)
		int getVerificationCodeSize();
	}

	private final QueryExecutor _verificationMail;

	private final QueryExecutor _invitationMail;

	private final QueryExecutor _resetPasswordMail;

	private final QueryExecutor _resetMFAMail;

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
		_resetPasswordMail = QueryExecutor.compile(config.getResetPasswordMail());
		_resetMFAMail = QueryExecutor.compile(config.getResetMFAMail());
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
	 * Compiled executor for {@link Config#getResetPasswordMail()}.
	 */
	public QueryExecutor getResetPasswordMail() {
		return _resetPasswordMail;
	}

	/**
	 * Compiled executor for {@link Config#getResetMFAMail()}.
	 */
	public QueryExecutor getResetMFAMail() {
		return _resetMFAMail;
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

