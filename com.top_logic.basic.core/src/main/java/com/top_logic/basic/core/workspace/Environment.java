/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.workspace;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Description of the environment in which the application runs.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Environment {

	/** Namespace (relative to {@link #JAVA_COMP_ENV}) to get TL variables. */
	public static final String JNDI_NAMESPACE_TL = "tl";

	/** Base namespace to get JNDI properties */
	public static final String JAVA_COMP_ENV = "java:comp/env";

	private static final String TL_ENV = JAVA_COMP_ENV + "/" + JNDI_NAMESPACE_TL;

	private static final Logger LOG = Logger.getLogger(Environment.class.getName());

	/**
	 * Marker property to enable startup in the IDE with a modular resource path.
	 *
	 * @deprecated This property no longer influences {@link #isDeployed()}. The prod/dev
	 *             distinction is now driven solely by the {@link #OPERATION_MODE} variable. To run
	 *             an installation as a non-deployed developer workspace, set
	 *             <code>{@value #OPERATION_MODE}={@value #OPERATION_MODE_DEVELOPMENT}</code>.
	 */
	@Deprecated
	public static final String DEVELOPER_MODE = "tl_developerMode";

	/**
	 * Name of the system property or environment variable selecting the operational environment
	 * (see {@code com.top_logic.base.operation.OperationMode}).
	 *
	 * <p>
	 * This is the single low-level driver of the prod/dev distinction. It is shared with the
	 * high-level {@code OperationModeService}, which resolves the same variable through the
	 * <code>%OPERATION_MODE%</code> configuration alias.
	 * </p>
	 *
	 * <p>
	 * The two answer <em>different</em> questions and therefore need not always coincide.
	 * {@link #isDeployed()} is a two-way flag ("developer workspace" vs. "everything else"): it is
	 * only ever {@code false} when this variable is explicitly set to
	 * {@value #OPERATION_MODE_DEVELOPMENT}. The {@code OperationModeService}, by contrast,
	 * resolves the full three-way environment axis (development / test / production) and can derive
	 * {@code TEST} from the surrounding test container even when this variable is unset. So in an
	 * automated test install {@link #isDeployed()} reports {@code true} (not a developer workspace)
	 * while the service reports {@code TEST}: consistent, because they classify along different
	 * axes. Code that must distinguish test from production has to consult the service, not this
	 * flag.
	 * </p>
	 *
	 * <p>
	 * The variable name is defined here, at the {@code basic.core} layer, because {@link Environment}
	 * lives below the core module and must not depend on the {@code OperationModeService}.
	 * </p>
	 */
	public static final String OPERATION_MODE = "tl_operation_mode";

	/**
	 * Value of {@link #OPERATION_MODE} that marks a non-deployed developer workspace.
	 *
	 * <p>
	 * Must equal the external name of {@code OperationMode.DEVELOPMENT}; a test in
	 * {@code TestOperationModeService} guards this agreement.
	 * </p>
	 */
	public static final String OPERATION_MODE_DEVELOPMENT = "development";

	/**
	 * Determines whether the application is deployed.
	 *
	 * <p>
	 * An installation is considered deployed (a production install) by default. It is only treated
	 * as not deployed when the {@link #OPERATION_MODE} variable is explicitly set to
	 * {@value #OPERATION_MODE_DEVELOPMENT} (a developer workspace or IDE run).
	 * </p>
	 *
	 * @return <code>true</code> unless {@link #OPERATION_MODE} is set to
	 *         {@value #OPERATION_MODE_DEVELOPMENT}.
	 */
	public static boolean isDeployed() {
		return !isDevelopmentMode();
	}

	private static boolean isDevelopmentMode() {
		return OPERATION_MODE_DEVELOPMENT.equals(getSystemPropertyOrEnvironmentVariable(OPERATION_MODE, null));
	}

	/**
	 * Returns the value of the given JNDI, system or environment property. If property is nowhere
	 * set, the <code>defaultValue</code> is returned.
	 * 
	 * @implNote Although both are essentially maps that provide String values for String keys,
	 *           let's look at a few differences:
	 *           <ol>
	 *           <li>We can update Properties at runtime while Environment Variables are an
	 *           immutable copy of the Operating System's variables.</li>
	 *           <li>Properties are contained only within Java platform while Environment Variables
	 *           are global at the Operating System level available to all applications running on
	 *           the same machine.</li>
	 *           <li>Properties must exist when packaging the application but we can create
	 *           Environment Variables on the Operating System at almost any point.</li>
	 *           </ol>
	 * 
	 * @param property
	 *        The property to check.
	 * @param defaultValue
	 *        Return value when no system or environment property is set, or a security manager
	 *        doesn't allow access to the property.
	 * 
	 * @return Either the JNDI value if not <code>null</code>, or the value of
	 *         {@link System#getProperty(String)} if not <code>null</code>, or
	 *         {@link System#getenv(String)} if not <code>null</code>, or the given default value.
	 */
	public static String getSystemPropertyOrEnvironmentVariable(String property, String defaultValue) {
		if (property.indexOf('.') >= 0) {
			LOG.warning(
				"For portability reasons, environment variables must not contain the '.' character: " + property);
		}
		try {
			String ctxValue = lookupJNDIProperty(property);
			if (ctxValue != null) {
				return ctxValue;
			}
			String propValue = lookupSystemProperty(property);
			if (propValue != null) {
				return propValue;
			}
			String envValue = lookupEnvironmentVariable(property);
			if (envValue != null) {
				return envValue;
			}
			return defaultValue;
		} catch (SecurityException ex) {
			return defaultValue;
		}
	}

	/**
	 * Searches for a JNDI property with the given name in context {@value #TL_ENV}.
	 * 
	 * @param property
	 *        Name of the property to lookup.
	 * @return The value for the property or <code>null</code>.
	 * 
	 * @throws ClassCastException
	 *         when the configured value is not of type {@link String}.
	 * 
	 * @see #getSystemPropertyOrEnvironmentVariable(String, boolean) General access to a TL
	 *      variable.
	 */
	public static String lookupJNDIProperty(String property) {
		try {
			Context envCtx = getTLEnvironment();
			if (envCtx == null) {
				return null;
			}

			Object value = envCtx.lookup(property);
			if (value == null || value instanceof String) {
				return (String) value;
			}
			throw new ClassCastException("JNDI property tl/" + property + " must be of type " + String.class.getName()
				+ ", but is " + value.getClass().getName());
		} catch (NamingException ex) {
			// Exception is thrown when nothing is set.
			return null;
		}
	}

	/**
	 * Determines all TL variables defined in the context.
	 * 
	 * @return Context found under {@value #TL_ENV} or <code>null</code> if the value is not defined
	 *         or not a context.
	 * @throws NamingException
	 *         Iff accessing {@link Context} failed.
	 * 
	 * @see #getSystemPropertyOrEnvironmentVariable(String, boolean) General access to a TL
	 *      variable.
	 */
	public static Context getTLEnvironment() throws NamingException {
		Object tlContext = InitialContext.doLookup(TL_ENV);
		if (tlContext instanceof Context) {
			return (Context) tlContext;
		}
		return null;
	}

	/**
	 * Searches for a {@link System} property with the given name.
	 * 
	 * @param property
	 *        Name of the property to lookup.
	 * @return The value for the property or <code>null</code>.
	 * 
	 * @see #getSystemPropertyOrEnvironmentVariable(String, boolean) General access to a TL
	 *      variable.
	 */
	public static String lookupSystemProperty(String property) {
		try {
			return System.getProperty(property);
		} catch (SecurityException ex) {
			// May be thrown when security manager is installed and access to
			// system properties is not allowed.
			LOG.log(Level.INFO, "System property variable '" + property + "' not accessible.", ex);
			return null;
		}
	}

	/**
	 * Searches for an environment property with the given name.
	 * 
	 * @param property
	 *        Name of the property to lookup.
	 * @return The value for the property or <code>null</code>.
	 */
	public static String lookupEnvironmentVariable(String property) {
		// May be defined as environment variable
		try {
			return System.getenv(property);
		} catch (SecurityException ex) {
			// May be thrown when security manager is installed and access to
			// system environment is not allowed.
			LOG.log(Level.INFO, "Environment variable '" + property + "' not accessible.", ex);
			return null;
		}
	}

	/**
	 * Checks whether the given property is set to "true". If property is not set, the
	 * <code>defaultValue</code> is returned.
	 * 
	 * @param property
	 *        The system property to check.
	 * @param defaultValue
	 *        Return value when no system or environment property is set, or a security manager
	 *        doesn't allow access to the property.
	 * 
	 * @return <code>true</code> when {@link System#getenv(String) system property} is "true", or
	 *         system property is not set and {@link System#getenv(String) environment variable} is
	 *         <code>true</code>.
	 * 
	 * @see #getSystemPropertyOrEnvironmentVariable(String, String)
	 */
	public static boolean getSystemPropertyOrEnvironmentVariable(String property, boolean defaultValue) {
		return String.valueOf(true)
			.equals(getSystemPropertyOrEnvironmentVariable(property, String.valueOf(defaultValue)));
	}

	/**
	 * Whether this files runs within a jar file.
	 *
	 * @deprecated This method no longer participates in deployment detection. The prod/dev
	 *             distinction is now driven solely by the {@link #OPERATION_MODE} variable via
	 *             {@link #isDeployed()}.
	 */
	@Deprecated
	public static boolean isJarFile() {
		URL location = Environment.class.getProtectionDomain().getCodeSource().getLocation();
		if (location == null) {
			// Happens in Tomcat 11.0 with jdk21-openjdk
			return true;
		}
		String base = location.getFile();

		return (base.endsWith(".jar") || base.indexOf(".jar!") > 0);
	}

}

