// SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
//
// SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
//
// Set-up of the Jenkins of tl-factory-jenkins from the environment of the container, at every
// start: the administrator TL_JENKINS_ADMIN with the password TL_JENKINS_ADMIN_PASSWORD and the API
// token TL_JENKINS_API_TOKEN (version "11" and 32 hex digits), every logged-in user with full
// control, optionally the user name and password credential TL_JENKINS_SCM_CREDENTIALS_ID for
// reading the git hosting (TL_JENKINS_SCM_USER, TL_JENKINS_SCM_PASSWORD), TL_JENKINS_EXECUTORS
// executors on the built-in node (default 2), and optionally the Jenkins URL TL_JENKINS_URL.
//
// Run again on an existing Jenkins home, the script sets the password, keeps an API token that is
// still valid and replaces the SCM credential.

import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration
import jenkins.security.ApiTokenProperty

def env = System.getenv()

def value = { String name -> env[name] }
for (name in ["TL_JENKINS_ADMIN", "TL_JENKINS_ADMIN_PASSWORD", "TL_JENKINS_API_TOKEN"]) {
	if (!value(name)) {
		throw new IllegalStateException("The environment variable " + name + " is not set.")
	}
}

def jenkins = Jenkins.get()

def realm = new HudsonPrivateSecurityRealm(false)
def admin = realm.createAccount(value("TL_JENKINS_ADMIN"), value("TL_JENKINS_ADMIN_PASSWORD"))
jenkins.setSecurityRealm(realm)
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
jenkins.setAuthorizationStrategy(strategy)

def tokens = admin.getProperty(ApiTokenProperty.class)
if (tokens == null) {
	tokens = new ApiTokenProperty()
	admin.addProperty(tokens)
}
def token = value("TL_JENKINS_API_TOKEN")
if (!tokens.matchesPassword(token)) {
	tokens.addFixedNewToken("tl-factory", token)
}
admin.save()

def scmId = value("TL_JENKINS_SCM_CREDENTIALS_ID")
if (scmId) {
	def store = SystemCredentialsProvider.getInstance().getStore()
	def domain = Domain.global()
	def scm = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, scmId, "Read access to the git hosting",
		value("TL_JENKINS_SCM_USER"), value("TL_JENKINS_SCM_PASSWORD"))
	def existing = store.getCredentials(domain).find { it.id == scmId }
	if (existing) {
		store.updateCredentials(domain, existing, scm)
	} else {
		store.addCredentials(domain, scm)
	}
}

def url = value("TL_JENKINS_URL")
if (url) {
	def location = JenkinsLocationConfiguration.get()
	location.setUrl(url)
	location.save()
}

jenkins.setNumExecutors((value("TL_JENKINS_EXECUTORS") ?: "2") as int)
jenkins.save()
