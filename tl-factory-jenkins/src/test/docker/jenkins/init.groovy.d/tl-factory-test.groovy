// SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
//
// SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
//
// Set-up of the Jenkins of the integration tests from the environment of the container: the
// administrator TL_TEST_ADMIN with the password TL_TEST_ADMIN_PASSWORD and the API token
// TL_TEST_API_TOKEN (version "11" and 32 hex digits), every logged-in user with full control,
// optionally the user name and password credential TL_TEST_SCM_CREDENTIALS_ID for reading the git
// hosting (TL_TEST_SCM_USER, TL_TEST_SCM_PASSWORD), and two executors on the built-in node.

import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import jenkins.model.Jenkins
import jenkins.security.ApiTokenProperty

def env = System.getenv()
def jenkins = Jenkins.get()

def realm = new HudsonPrivateSecurityRealm(false)
def admin = realm.createAccount(env.TL_TEST_ADMIN, env.TL_TEST_ADMIN_PASSWORD)
jenkins.setSecurityRealm(realm)
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
jenkins.setAuthorizationStrategy(strategy)

def tokens = admin.getProperty(ApiTokenProperty.class)
if (tokens == null) {
	tokens = new ApiTokenProperty()
	admin.addProperty(tokens)
}
tokens.addFixedNewToken("tl-factory-test", env.TL_TEST_API_TOKEN)
admin.save()

if (env.TL_TEST_SCM_CREDENTIALS_ID) {
	def store = SystemCredentialsProvider.getInstance().getStore()
	store.addCredentials(Domain.global(), new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
		env.TL_TEST_SCM_CREDENTIALS_ID, "Git hosting of the test", env.TL_TEST_SCM_USER, env.TL_TEST_SCM_PASSWORD))
}

jenkins.setNumExecutors(2)
jenkins.save()
