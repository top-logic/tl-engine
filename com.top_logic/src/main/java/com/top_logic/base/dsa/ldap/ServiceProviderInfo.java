/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.dsa.ldap;

import java.util.Hashtable;

import javax.naming.Context;

import com.top_logic.base.security.util.CryptSupport;
import com.top_logic.base.security.util.CryptSupportSecretSourceFormat;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.credential.SecretSource;

/**
 * Configuration object for instantiating {@link LDAPAccessService}
 * 
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class ServiceProviderInfo extends Hashtable {
	
	/**
	 * Configuration for a {@link ServiceProviderInfo}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		@StringDefault(DEFAULT_AUTHENTICATION_TYPE)
		String getAuthenticationType();

		@StringDefault(DEFAULT_CONTEXT_FACTORY)
		String getContextFactory();

		/**
		 * @see #getMasterCredential()
		 */
		String MASTER_CREDENTIAL = "master-credential";

		/**
		 * The password of the master account used to access the directory.
		 * 
		 * <p>
		 * Recommended is a reference to the password by its secret name, so that the password is
		 * not part of the configuration, e.g. <code>&lt;master-credential
		 * name="ldap_credential"/&gt;</code>, which the default secret store reads from the
		 * environment variable of that name holding the plain password. An attribute value is the
		 * password encoded by {@link CryptSupport}, or the password in plain text.
		 * </p>
		 * 
		 * <p>
		 * The password is read each time a connection with the master account is opened.
		 * </p>
		 */
		@Name(MASTER_CREDENTIAL)
		@Format(CryptSupportSecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getMasterCredential();

		/**
		 * @see #getMasterCredential()
		 */
		void setMasterCredential(PolymorphicConfiguration<? extends SecretSource> value);

		String getMasterPrincipal();

		String getReferral();

		String getProviderUrl();

		String getBaseDn();
		
		String getSecurityProtocolType();

	}

	/**
	 * Default values if not explicitly given
	 */
	public static final String DEFAULT_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	public static final String DEFAULT_AUTHENTICATION_TYPE = "simple";
	
	// Manadatory configuration values.
    
	public static final String KEY_LDAP_HOST           = Context.PROVIDER_URL;
	public static final String KEY_SEARCH_BASE         = "BaseDN";
	public static final String KEY_CONTEXT_FACTORY     = Context.INITIAL_CONTEXT_FACTORY;
	public static final String KEY_AUTHENTICATION_TYPE = Context.SECURITY_AUTHENTICATION;
	public static final String KEY_PRINCIPAL           = Context.SECURITY_PRINCIPAL;
	public static final String KEY_CREDENTIALS         = Context.SECURITY_CREDENTIALS;
	public static final String KEY_SECURITY_PROTOCOL   = Context.SECURITY_PROTOCOL;
	
	/**
	 * Configuration access keys...
	 */
	public static final String	XML_PARAM_AUTH_TYPE			= "authentication.type";
	public static final String	XML_PARAM_CTXT_FACTORY		= "contextFactory.class";
	public static final String	XML_PARAM_MASTER_CREDENTIAL	= "masterCredential.value";
	public static final String	XML_PARAM_MASTER_PRINCIPAL	= "masterPrincipal.value";
	private static final String XML_PARAM_REFERRAL          = "referral";
	public static final String	XML_PARAM_URL				= "provider.url";
	public static final String	XML_PARAM_SEARCHBASE		= "base.dn";
	public static final String  XML_PARAM_SSL               = "securityProtocol.type";

	/**
	 * Create a new ServiceProviderInfo
	 * @param host 		- full url too host of the LDAP system (e.g. ldap://myhost.de:389) 
	 * @param factory	- optional, class of the context factory. If not given, default is used
	 * @param authType	- optional, type of authentication. If not given, default is used
	 * @param principal	- login name of master account used to access the directory system
	 * @param credentials - password of master account used to access the directory system
	 * @param searchBase - root node of the directory
	 */
	public ServiceProviderInfo(String host, String factory, String authType, String principal, String credentials, String searchBase){
		super();
		this.put(KEY_LDAP_HOST            ,host);
		this.put(KEY_CONTEXT_FACTORY      ,factory);
		this.put(KEY_AUTHENTICATION_TYPE  ,authType);
		this.put(KEY_PRINCIPAL            ,principal);
		this.put(KEY_CREDENTIALS          ,credentials);
		this.put(KEY_SEARCH_BASE          ,searchBase);
	}
	
	/**
	 * Create a new instance, using an existing one as base
	 */
	public ServiceProviderInfo(ServiceProviderInfo aTemplate){
		super(aTemplate);
	}
	
	/**
	 * Creates the {@link ServiceProviderInfo} described by the given configuration.
	 * 
	 * <p>
	 * The result contains no {@link #KEY_CREDENTIALS credentials}, since the
	 * {@link Config#getMasterCredential() master credential} is read each time a connection is
	 * opened, see {@link #withCredentials(String)}.
	 * </p>
	 */
	public static ServiceProviderInfo createFromConfig(Config providerConfig) {
		String searchBaseDN = providerConfig.getBaseDn();
		String masterPrincipal = providerConfig.getMasterPrincipal();
		String url = providerConfig.getProviderUrl();
		String authType = providerConfig.getAuthenticationType();
		String contextFactory = providerConfig.getContextFactory();
		String referral = providerConfig.getReferral();
		String sslType = providerConfig.getSecurityProtocolType();

		ServiceProviderInfo ldapAccessCfg =
			new ServiceProviderInfo(url, contextFactory, authType, masterPrincipal, StringServices.EMPTY_STRING,
				searchBaseDN);
		ldapAccessCfg.remove(KEY_CREDENTIALS);
		if (!StringServices.isEmpty(referral)) {
			ldapAccessCfg.put(Context.REFERRAL, referral);
		}

		if (!StringServices.isEmpty(sslType)) {
			ldapAccessCfg.put(ServiceProviderInfo.KEY_SECURITY_PROTOCOL, sslType);
		}

		return ldapAccessCfg;
	}

	/**
	 * A copy of this {@link ServiceProviderInfo} with the given {@link #KEY_CREDENTIALS
	 * credentials}.
	 */
	public ServiceProviderInfo withCredentials(String credentials) {
		ServiceProviderInfo result = new ServiceProviderInfo(this);
		result.put(KEY_CREDENTIALS, credentials);
		return result;
	}

}
