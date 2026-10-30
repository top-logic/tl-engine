/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.CombinedViewExecutabilityRule;
import com.top_logic.layout.view.command.DeleteObjectAction;
import com.top_logic.layout.view.command.TypeObservingRule;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.login.DisableMfaAction;
import com.top_logic.layout.view.login.SecondFactor;
import com.top_logic.layout.view.login.SecondFactorStep;
import com.top_logic.security.passkey.PasskeyCredentialRepository;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.security.passkey.model.TlSecurityPasskeyFactory;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link DeleteObjectAction} deleting a {@link Passkey} on behalf of the account it belongs to,
 * which keeps an account required to use a second factor from deleting its last one.
 *
 * <p>
 * An account whose {@link MfaRequirement} is {@link MfaRequirement#REQUIRED} cannot switch its
 * second factor off (see {@link DisableMfaAction}), so it cannot delete the passkey that is its
 * only second factor either: the deletion is refused unless the account keeps another passkey or
 * another {@link SecondFactorStep#configured() configured} method of the login it has set up, e.g.
 * an authenticator app. A command with this action is disabled for such a passkey and tells why.
 * </p>
 *
 * <p>
 * An administrator removing a passkey of somebody else deletes it with {@link DeleteObjectAction}:
 * a required account without a second factor sets one up at its next login.
 * </p>
 *
 * <p>
 * Example: delete the selected passkey of the current user.
 * </p>
 *
 * <pre>
 * &lt;generic-command inputs="selectedPasskey"&gt;
 *   &lt;executability&gt;
 *     &lt;null-input-disabled/&gt;
 *   &lt;/executability&gt;
 *   &lt;confirm expr="..."/&gt;
 *   &lt;verify-identity/&gt;
 *   &lt;delete-passkey/&gt;
 *   &lt;write-channel name="selectedPasskey" clear="true"/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 */
@InApp
public class DeletePasskeyAction extends DeleteObjectAction {

	/**
	 * Configuration for {@link DeletePasskeyAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends DeleteObjectAction.Config {

		/** Tag name of a {@link DeletePasskeyAction} in an action chain. */
		String TAG_NAME = "delete-passkey";

		@Override
		@ClassDefault(DeletePasskeyAction.class)
		Class<? extends DeleteObjectAction> getImplementationClass();
	}

	/**
	 * Disables the deletion of a passkey that is the last second factor of an account required to
	 * use one.
	 */
	private static final ViewExecutabilityRule LAST_SECOND_FACTOR = input -> {
		if (input instanceof Passkey passkey) {
			ResKey refusal = refusal(passkey);
			if (refusal != null) {
				return ExecutableState.createDisabledState(refusal);
			}
		}
		return ExecutableState.EXECUTABLE;
	};

	/**
	 * Creates a {@link DeletePasskeyAction} from configuration.
	 */
	@CalledByReflection
	public DeletePasskeyAction(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * The rule of the {@link DeleteObjectAction}, and that the passkey is not the last second
	 * factor of an account required to use one.
	 *
	 * <p>
	 * Whether it is the last one depends on the other passkeys of the account and on the other
	 * second factors stored with the account, so the rule decides anew whenever a passkey or an
	 * account changes, not only when another passkey is selected.
	 * </p>
	 */
	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		return CombinedViewExecutabilityRule.combine(List.of(super.getIntrinsicRule(),
			new TypeObservingRule(LAST_SECOND_FACTOR,
				Set.of(TlSecurityPasskeyFactory.getPasskeyType(), Person.getPersonType()))));
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (input == null) {
			return null;
		}
		if (!(input instanceof Passkey passkey)) {
			throw new IllegalArgumentException("Not a passkey: " + input);
		}
		ResKey refusal = refusal(passkey);
		if (refusal != null) {
			throw new TopLogicException(refusal);
		}
		return super.execute(context, input);
	}

	/**
	 * Why the given passkey must not be deleted by its account, <code>null</code> if it may, with
	 * the {@link SecondFactorStep#configured() configured} methods of the login.
	 *
	 * @see #refusal(Passkey, List)
	 */
	public static ResKey refusal(Passkey passkey) {
		return refusal(passkey, SecondFactorStep.configured());
	}

	/**
	 * Why the given passkey must not be deleted by its account, <code>null</code> if it may.
	 *
	 * @param passkey
	 *        The passkey to delete.
	 * @param methods
	 *        The second factor methods of the login. Methods for passkeys are not asked: whether
	 *        the account keeps a passkey is decided by its passkeys other than the given one.
	 * @return The reason shown to the user if the passkey is the last second factor of an account
	 *         that is {@link MfaRequirement#REQUIRED required} to use one, <code>null</code>
	 *         otherwise.
	 */
	public static ResKey refusal(Passkey passkey, List<? extends SecondFactor> methods) {
		Person account = PasskeyCredentialRepository.getOwner(passkey);
		if (account == null || account.getMFARequirement() != MfaRequirement.REQUIRED) {
			return null;
		}
		for (Passkey other : PasskeyCredentialRepository.getPasskeys(account)) {
			if (!other.equals(passkey)) {
				return null;
			}
		}
		for (SecondFactor method : methods) {
			if (!(method instanceof PasskeySecondFactor) && method.isEnrolled(account)) {
				return null;
			}
		}
		return I18NConstants.ERROR_LAST_SECOND_FACTOR__ACCOUNT.fill(account.getName());
	}

}
