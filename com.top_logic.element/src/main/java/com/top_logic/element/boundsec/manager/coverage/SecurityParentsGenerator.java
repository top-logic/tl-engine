/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.SecurityParentsConfig;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Turns the security parents proposed by the {@link SecurityCoverageAnalysis} into the
 * configuration that defines them.
 *
 * <p>
 * A type without a role source that is contained in exactly one composition gets a security parent
 * rule navigating that composition backwards. This generator collects those rules, renders them as
 * the XML that belongs into the access manager configuration, and stores them in the configuration
 * of the application, where the developer reviews them like any other configuration.
 * </p>
 *
 * <p>
 * The stored entry is layered onto the access manager configuration of the underlying
 * configuration layers instead of replacing it, so that the rules defined there stay in effect and
 * a generated rule only replaces a rule of the same id. An entry stored as an override before, for
 * example by the service editor, keeps replacing the underlying layers.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityParentsGenerator {

	/** Tag name of a {@link SecurityParentsConfig} in the access manager configuration. */
	public static final String SECURITY_PARENTS_TAG = ElementAccessManager.Config.SECURITY_PARENTS;

	private final File _configFile;

	/**
	 * Creates a {@link SecurityParentsGenerator} storing into the configuration of the running
	 * application.
	 *
	 * @see InAppServiceConfigStore#fileFor(com.top_logic.basic.module.BasicRuntimeModule)
	 */
	public SecurityParentsGenerator() {
		this(InAppServiceConfigStore.fileFor(AccessManager.Module.INSTANCE));
	}

	/**
	 * Creates a {@link SecurityParentsGenerator} storing into the given file.
	 *
	 * @param configFile
	 *        The file holding the stored access manager configuration, existing or not.
	 */
	public SecurityParentsGenerator(File configFile) {
		_configFile = configFile;
	}

	/**
	 * The file the generated rules are stored in.
	 */
	public File getConfigFile() {
		return _configFile;
	}

	/**
	 * Collects the security parent rules proposed by the given analysis result.
	 *
	 * @param coverage
	 *        The result of {@link SecurityCoverageCheck#analyze()}.
	 * @return The proposed rules, ordered by the qualified name of the type they apply to.
	 *
	 * @see FindingKind#SUGGESTED_PARENT
	 */
	public SecurityParentsConfig collect(Collection<TypeCoverage> coverage) {
		SecurityParentsConfig result = TypedConfiguration.newConfigItem(SecurityParentsConfig.class);
		coverage.stream()
			.sorted(Comparator.comparing(entry -> TLModelUtil.qualifiedName(entry.type())))
			.flatMap(entry -> entry.findings(FindingKind.SUGGESTED_PARENT).stream())
			.map(CoverageFinding::getSuggestedRule)
			.forEach(rule -> result.getRules().add(TypedConfiguration.copy(rule)));
		return result;
	}

	/**
	 * Renders the given rules as the XML that belongs into the access manager configuration.
	 *
	 * @param rules
	 *        The rules to render.
	 * @return The {@link #SECURITY_PARENTS_TAG} element, pretty printed.
	 */
	public String toXml(SecurityParentsConfig rules) {
		return InAppServiceConfigStore.toXml(SECURITY_PARENTS_TAG, rules, InAppServiceConfigStore.LAYER_ONTO_BASE);
	}

	/**
	 * Stores the given rules in the access manager configuration of the application.
	 *
	 * <p>
	 * A rule whose id is already defined in the stored configuration is replaced, every other rule
	 * is appended. The rules defined in the other configuration layers are left untouched.
	 * </p>
	 *
	 * @param rules
	 *        The rules to store.
	 * @return The written file.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public File writeToAutoconf(SecurityParentsConfig rules) throws IOException, ConfigurationException {
		ApplicationConfig.Config appConfig = InAppServiceConfigStore.read(_configFile);
		boolean override = InAppServiceConfigStore.isOverriding(_configFile);

		ModuleConfiguration entry = appConfig.getServices().get(AccessManager.class);
		if (entry == null) {
			entry = InAppServiceConfigStore.newServiceEntry(AccessManager.class, newAccessManagerConfig());
			appConfig.getServices().put(AccessManager.class, entry);
		}
		merge(securityParents(entry), rules);

		InAppServiceConfigStore.write(_configFile, appConfig, InAppServiceConfigStore.overrideTypes(override));
		return _configFile;
	}

	/**
	 * The security parents of the given service entry.
	 */
	private SecurityParentsConfig securityParents(ModuleConfiguration entry) {
		ServiceConfiguration<?> instance = entry.getInstance();
		if (!(instance instanceof ElementAccessManager.Config)) {
			throw new IllegalStateException("The access manager configuration does not define security parents: "
				+ instance);
		}
		return ((ElementAccessManager.Config) instance).getSecurityParents();
	}

	/**
	 * Creates the access manager configuration carrying only the generated rules.
	 *
	 * @implNote Both the configuration interface and the implementation class are taken from the
	 *           running access manager: the stored entry then offers the same properties as the
	 *           configuration it is layered onto, and it names the class that is already in effect
	 *           instead of falling back to the default implementation class of the property.
	 */
	private ServiceConfiguration<?> newAccessManagerConfig() {
		AccessManager manager = AccessManager.getInstance();
		ServiceConfiguration<AccessManager> result =
			TypedConfiguration.newConfigItem(configInterface(manager));
		result.setImplementationClass(manager.getClass());
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Class<ServiceConfiguration<AccessManager>> configInterface(AccessManager manager) {
		return (Class<ServiceConfiguration<AccessManager>>) (Class<?>) manager.getConfig()
			.getConfigurationInterface();
	}

	/**
	 * Adds the given rules to the given definition, replacing a rule of the same id.
	 */
	private void merge(SecurityParentsConfig target, SecurityParentsConfig rules) {
		List<NavigationRuleConfig> stored = target.getRules();
		for (NavigationRuleConfig rule : rules.getRules()) {
			NavigationRuleConfig copy = TypedConfiguration.copy(rule);
			int index = indexOf(stored, rule.getId());
			if (index < 0) {
				stored.add(copy);
			} else {
				stored.remove(index);
				stored.add(index, copy);
			}
		}
	}

	private static int indexOf(List<NavigationRuleConfig> rules, String id) {
		for (int n = 0, size = rules.size(); n < size; n++) {
			if (id.equals(rules.get(n).getId())) {
				return n;
			}
		}
		return -1;
	}

}
