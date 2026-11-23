/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.ActingPerson;

/**
 * Runs the checks of {@link AbstractPersonalCredentialProviderTest} on a small provider that keeps
 * the rules, so that the checks themselves are tested.
 */
public class TestPersonalCredentialProviderRules extends AbstractPersonalCredentialProviderTest {

	private TokenPerPerson _tokens;

	@Override
	protected CredentialProvider createProvider() {
		_tokens = new TokenPerPerson();
		return _tokens;
	}

	@Override
	protected void connect(Person person) {
		_tokens.connect(person);
	}

	/**
	 * A personal provider with one token for each connected person. The token gets a new version
	 * when it is invalidated.
	 */
	private static final class TokenPerPerson implements CredentialProvider {

		private final Map<Person, Integer> _versions = new ConcurrentHashMap<>();

		void connect(Person person) {
			_versions.put(person, Integer.valueOf(1));
		}

		@Override
		public void authenticate(RequestAuthentication request) {
			Person person = ActingPerson.require();
			Integer version = _versions.get(person);
			if (version == null) {
				throw new I18NRuntimeException(ResKey.text("Connect your account in the settings first."));
			}
			request.setHeader(AUTHORIZATION, BEARER + " " + person.getName() + "-" + version);
		}

		@Override
		public void invalidate() {
			_versions.computeIfPresent(ActingPerson.require(), (person, version) -> version + 1);
		}

		@Override
		public boolean isPersonal() {
			return true;
		}
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestPersonalCredentialProviderRules.class));
	}

}
