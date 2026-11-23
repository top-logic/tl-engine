/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.credential;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;

/**
 * A {@link CredentialProvider#isPersonal() personal} {@link CredentialProvider} for the tests of a
 * consumer that never acts for a person and therefore must refuse it.
 *
 * <p>
 * It adds {@link #AUTHORIZATION_VALUE} to each request it is asked for, so that a test can see that
 * the refusing consumer sent no request with it.
 * </p>
 *
 * <pre>
 * &lt;credentials class="test.com.top_logic.basic.credential.PersonalTestCredentials"/&gt;
 * </pre>
 */
public class PersonalTestCredentials extends AbstractConfiguredInstance<PersonalTestCredentials.Config>
		implements CredentialProvider {

	/** The value this provider sets in the header {@link #AUTHORIZATION}. */
	public static final String AUTHORIZATION_VALUE = BEARER + " personal-test-token";

	/**
	 * Configuration of {@link PersonalTestCredentials}.
	 */
	public interface Config extends PolymorphicConfiguration<PersonalTestCredentials> {
		// No properties.
	}

	/**
	 * Creates a {@link PersonalTestCredentials} from configuration.
	 */
	public PersonalTestCredentials(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void authenticate(RequestAuthentication request) {
		request.setHeader(AUTHORIZATION, AUTHORIZATION_VALUE);
	}

	@Override
	public boolean isPersonal() {
		return true;
	}

}
