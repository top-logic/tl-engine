/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.security.AllSecurityRoles;

/*
 * Issue #347. Every JavaDoc comment in this file is exported to META-INF/messages_en.properties by
 * the build's doclet and shown as the label and tooltip of the corresponding field in the service
 * editor, so its audience is an administrator, not a developer: short sentences saying what a
 * setting does and what happens when it is left out. The design rationale belongs in comments like
 * this one, which the doclet does not export.
 *
 * WHY THIS IS NOT the browser tool's allowed-domains/blocked-domains. Those answer "which origins
 * may the BROWSER reach", as one list for the whole installation. This answers "which origins may
 * THIS USER'S agent run reach", for every route out of the application - browser, webFetch and
 * webSearch - and it varies per security role. The two compose: both have to permit an origin, and
 * neither can relax UrlSafety, which is the inner SSRF check and is not configurable at all.
 *
 * WHY THE PATTERN SYNTAX DIFFERS from allowed-domains. There an entry matches the host and every
 * sub-domain of it, with no way to say "this host only". Here "docs.example.com" is the host and
 * nothing else, and "*.example.com" is the sub-domains. An origin policy is written to carve narrow
 * exceptions out of a deny-by-default, and a syntax whose only form silently includes every
 * sub-domain cannot express one.
 *
 * The full shape an administrator writes:
 *
 * <config service-class="com.top_logic.tl.ai.core.tools.web.WebToolsService">
 *     <instance>
 *         <browsing default="research">
 *             <policies>
 *                 <policy name="open"/>
 *                 <policy name="research" access="deny">
 *                     <origins>
 *                         <origin host="docs.oracle.com"/>
 *                         <origin host="*.top-logic.com" downloads="deny"/>
 *                     </origins>
 *                 </policy>
 *             </policies>
 *             <roles>
 *                 <role name="SalesAssistantUser" policy="open"/>
 *             </roles>
 *         </browsing>
 *     </instance>
 * </config>
 */

/**
 * Which origins the agents of this application may browse, per security role.
 *
 * <p>
 * Configure nothing and nothing changes: every origin that passes the SSRF check is reachable, as
 * before. Name a policy and its holders reach exactly the origins that policy permits.
 * </p>
 *
 * <p>
 * This governs all three routes out of the application - the browser, {@code webFetch} and
 * {@code webSearch}. It is the ceiling, so a domain list an agent passes to {@code webSearch} can
 * only narrow it further, never widen it.
 * </p>
 */
public interface BrowsingPolicyConfig extends ConfigurationItem {

	/**
	 * Configuration property name for {@link #getDefault()}.
	 *
	 * @see #getDefault()
	 */
	String DEFAULT = "default";

	/**
	 * Configuration property name for {@link #getPolicies()}.
	 *
	 * @see #getPolicies()
	 */
	String POLICIES = "policies";

	/**
	 * Configuration property name for {@link #getRoles()}.
	 *
	 * @see #getRoles()
	 */
	String ROLES = "roles";

	/**
	 * The policy that applies to a user holding none of the roles named below.
	 *
	 * <p>
	 * Leave it out and such a user browses every origin the SSRF check permits - which is what an
	 * application that configures nothing here has always done. Name a policy to make it the floor
	 * for everybody.
	 * </p>
	 *
	 * @see #DEFAULT
	 */
	@Name(DEFAULT)
	@Nullable
	String getDefault();

	/**
	 * Setter for {@link #getDefault()}.
	 */
	void setDefault(String value);

	/**
	 * The policies this application knows, each under the name a role refers to.
	 *
	 * @see #POLICIES
	 */
	@Name(POLICIES)
	@Key(Policy.NAME_ATTRIBUTE)
	@EntryTag("policy")
	Map<String, Policy> getPolicies();

	/**
	 * Which security role browses under which policy.
	 *
	 * <p>
	 * A user holding several of these roles is bound by <b>all</b> of their policies at once: an
	 * origin is reachable only if every one of them permits it. Holding one more role can therefore
	 * never widen what a user reaches.
	 * </p>
	 *
	 * @see #ROLES
	 */
	@Name(ROLES)
	@Key(RolePolicy.NAME_ATTRIBUTE)
	@EntryTag("role")
	Map<String, RolePolicy> getRoles();

	/**
	 * Whether something is permitted or refused.
	 */
	enum Access implements ExternallyNamed {

		/** Permitted. */
		ALLOW("allow"),

		/** Refused. */
		DENY("deny");

		private final String _externalName;

		private Access(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Whether a file download is permitted, or left to whatever the access setting says.
	 */
	enum DownloadAccess implements ExternallyNamed {

		/**
		 * Whatever the access setting next to this one says: an origin that may be browsed may
		 * download. This is what an entry that does not mention downloads means.
		 */
		AS_ACCESS("as-access"),

		/** Permitted, as long as the origin may be reached at all. */
		ALLOW("allow"),

		/** Refused: the origin may be read, but a file from it is never saved. */
		DENY("deny");

		private final String _externalName;

		private DownloadAccess(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * One named browsing policy: what it permits by default, and the origins it treats differently.
	 */
	interface Policy extends NamedConfigMandatory {

		/**
		 * Configuration property name for {@link #getAccess()}.
		 *
		 * @see #getAccess()
		 */
		String ACCESS = "access";

		/**
		 * Configuration property name for {@link #getDownloads()}.
		 *
		 * @see #getDownloads()
		 */
		String DOWNLOADS = "downloads";

		/**
		 * Configuration property name for {@link #getOrigins()}.
		 *
		 * @see #getOrigins()
		 */
		String ORIGINS = "origins";

		/**
		 * The name roles refer to this policy by.
		 *
		 * @see NamedConfigMandatory#getName()
		 */
		@Override
		String getName();

		/**
		 * What happens at an origin no rule below names.
		 *
		 * <p>
		 * {@code allow} makes the rules exceptions that forbid; {@code deny} makes them the only
		 * origins this policy reaches.
		 * </p>
		 *
		 * @see #ACCESS
		 */
		@Name(ACCESS)
		@FormattedDefault("allow")
		Access getAccess();

		/**
		 * Setter for {@link #getAccess()}.
		 */
		void setAccess(Access value);

		/**
		 * Whether a page may start a file download at an origin no rule below names.
		 *
		 * <p>
		 * Left out, it follows the setting above: an origin that may be browsed may download. Set it
		 * to refused for a policy that reads the web but never saves a file from it.
		 * </p>
		 *
		 * @see #DOWNLOADS
		 */
		@Name(DOWNLOADS)
		DownloadAccess getDownloads();

		/**
		 * Setter for {@link #getDownloads()}.
		 */
		void setDownloads(DownloadAccess value);

		/**
		 * The origins this policy treats differently from the settings above.
		 *
		 * @see #ORIGINS
		 */
		@Name(ORIGINS)
		@EntryTag("origin")
		@DefaultContainer
		List<OriginRule> getOrigins();
	}

	/**
	 * What one policy says about one origin.
	 */
	interface OriginRule extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getHost()}.
		 *
		 * @see #getHost()
		 */
		String HOST = "host";

		/**
		 * Configuration property name for {@link #getAccess()}.
		 *
		 * @see #getAccess()
		 */
		String ACCESS = "access";

		/**
		 * Configuration property name for {@link #getDownloads()}.
		 *
		 * @see #getDownloads()
		 */
		String DOWNLOADS = "downloads";

		/**
		 * The host this rule is about.
		 *
		 * <p>
		 * {@code docs.example.com} is that host and no other. {@code *.example.com} is every
		 * sub-domain of {@code example.com} but not {@code example.com} itself - write both entries
		 * to cover a domain and everything under it.
		 * </p>
		 *
		 * @see #HOST
		 */
		@Name(HOST)
		@Mandatory
		String getHost();

		/**
		 * Setter for {@link #getHost()}.
		 */
		void setHost(String value);

		/**
		 * Whether this origin may be reached at all.
		 *
		 * @see #ACCESS
		 */
		@Name(ACCESS)
		@FormattedDefault("allow")
		Access getAccess();

		/**
		 * Setter for {@link #getAccess()}.
		 */
		void setAccess(Access value);

		/**
		 * Whether a page at this origin may start a file download.
		 *
		 * <p>
		 * Left out, it follows the setting above: an origin this rule permits may download. An
		 * origin this rule refuses never downloads, whatever is written here.
		 * </p>
		 *
		 * @see #DOWNLOADS
		 */
		@Name(DOWNLOADS)
		DownloadAccess getDownloads();

		/**
		 * Setter for {@link #getDownloads()}.
		 */
		void setDownloads(DownloadAccess value);
	}

	/**
	 * Which policy one security role browses under.
	 */
	interface RolePolicy extends NamedConfigMandatory {

		/**
		 * Configuration property name for {@link #getPolicy()}.
		 *
		 * @see #getPolicy()
		 */
		String POLICY = "policy";

		/**
		 * The security role this entry is about.
		 *
		 * @implNote Redeclared from {@link NamedConfigMandatory} only to carry the option
		 *           annotation; the property itself is inherited.
		 *
		 * @see AllSecurityRoles
		 */
		@Override
		@Options(fun = AllSecurityRoles.class)
		String getName();

		/**
		 * The policy this role browses under. A name no policy above declares is a boot error.
		 *
		 * @see #POLICY
		 */
		@Name(POLICY)
		@Mandatory
		String getPolicy();

		/**
		 * Setter for {@link #getPolicy()}.
		 */
		void setPolicy(String value);
	}
}
