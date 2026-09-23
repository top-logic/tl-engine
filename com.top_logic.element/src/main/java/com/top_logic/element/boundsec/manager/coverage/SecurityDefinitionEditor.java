/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.SecurityParentsConfig;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.security.SecurityConfigurationService.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.model.security.SecurityConfigurationService.TLModuleAccessRights;
import com.top_logic.model.security.SecurityConfigurationService.TypeBasedAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Edit access to the model based access definition of the running application.
 *
 * <p>
 * The definition consists of the role rules and security parent rules of the {@link AccessManager}
 * and of the grants of the {@link SecurityConfigurationService}. Each of the two services keeps its
 * stored configuration in a file of the autoconf folder of the application, and every operation of
 * this editor reads that file, applies the change and writes the file back. The file is layered
 * onto the configuration of the underlying layers instead of replacing it: a rule is merged by its
 * id, an access rights entry by the name of the model element it applies to, and everything the
 * underlying layers define stays in effect.
 * </p>
 *
 * <p>
 * A change becomes effective in the running application with {@link #apply()}, which reloads the
 * application configuration and restarts the two services. Until then the files and the running
 * definition differ: {@link #storedSecurityParentRules()} answers what the files hold,
 * {@link #editableSecurityParentRule(String)} what is currently in effect.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityDefinitionEditor {

	private final File _accessManagerFile;

	private final File _grantsFile;

	/**
	 * Creates a {@link SecurityDefinitionEditor} working on the autoconf files of the running
	 * application.
	 *
	 * @see InAppServiceConfigStore#fileFor(com.top_logic.basic.module.BasicRuntimeModule)
	 */
	public SecurityDefinitionEditor() {
		this(InAppServiceConfigStore.fileFor(AccessManager.Module.INSTANCE),
			InAppServiceConfigStore.fileFor(SecurityConfigurationService.Module.INSTANCE));
	}

	/**
	 * Creates a {@link SecurityDefinitionEditor} working on the given files.
	 *
	 * @param accessManagerFile
	 *        The file holding the stored {@link AccessManager} configuration, existing or not.
	 * @param grantsFile
	 *        The file holding the stored {@link SecurityConfigurationService} configuration,
	 *        existing or not.
	 */
	public SecurityDefinitionEditor(File accessManagerFile, File grantsFile) {
		_accessManagerFile = accessManagerFile;
		_grantsFile = grantsFile;
	}

	/**
	 * The file holding the stored rules.
	 *
	 * @see #storedSecurityParentRules()
	 * @see #storedRoleRules()
	 */
	public File getAccessManagerFile() {
		return _accessManagerFile;
	}

	/**
	 * The file holding the stored grants.
	 *
	 * @see #storedAccessRights()
	 */
	public File getGrantsFile() {
		return _grantsFile;
	}

	/**
	 * The security parent rules the stored configuration defines.
	 *
	 * @return A snapshot of the rules of {@link #getAccessManagerFile()}, empty when the file
	 *         defines none.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 *
	 * @see #editableSecurityParentRule(String) The rules that are in effect, which also include the
	 *      rules of the underlying configuration layers.
	 */
	public List<NavigationRuleConfig> storedSecurityParentRules() throws ConfigurationException {
		ElementAccessManager.Config config = storedAccessManagerConfig(readAccessManagerFile());
		return config == null ? Collections.emptyList() : List.copyOf(config.getSecurityParents().getRules());
	}

	/**
	 * The role rules the stored configuration defines.
	 *
	 * @return A snapshot of the rules of {@link #getAccessManagerFile()}, empty when the file
	 *         defines none.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 *
	 * @see #editableRoleRule(String) The rules that are in effect, which also include the rules of
	 *      the underlying configuration layers.
	 */
	public List<RoleRuleConfig> storedRoleRules() throws ConfigurationException {
		ElementAccessManager.Config config = storedAccessManagerConfig(readAccessManagerFile());
		return config == null ? Collections.emptyList() : List.copyOf(config.getRoleRules().getRules());
	}

	/**
	 * The number of rules {@link #getAccessManagerFile()} defines.
	 *
	 * @return The number of stored security parent rules plus the number of stored role rules.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public int storedRuleCount() throws ConfigurationException {
		ElementAccessManager.Config config = storedAccessManagerConfig(readAccessManagerFile());
		if (config == null) {
			return 0;
		}
		return config.getSecurityParents().getRules().size() + config.getRoleRules().getRules().size();
	}

	/**
	 * Whether the security parent rule with the given id is defined by the stored configuration.
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} to look for.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 *
	 * @see #removeSecurityParentRule(String) Only a stored rule can be removed.
	 */
	public boolean isStoredSecurityParentRule(String id) throws ConfigurationException {
		return indexOf(storedSecurityParentRules(), id) >= 0;
	}

	/**
	 * Whether the role rule with the given id is defined by the stored configuration.
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} to look for.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 *
	 * @see #removeRoleRule(String) Only a stored rule can be removed.
	 */
	public boolean isStoredRoleRule(String id) throws ConfigurationException {
		return indexOf(storedRoleRules(), id) >= 0;
	}

	/**
	 * Stores the given security parent rule, replacing a stored rule of the same id.
	 *
	 * @param rule
	 *        The rule to store. A copy is taken, the given configuration is left untouched.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public void putSecurityParentRule(NavigationRuleConfig rule) throws IOException, ConfigurationException {
		ApplicationConfig.Config appConfig = readAccessManagerFile();
		putRule(accessManagerConfig(appConfig).getSecurityParents().getRules(), rule);
		writeAccessManagerFile(appConfig);
	}

	/**
	 * Stores the given role rule, replacing a stored rule of the same id.
	 *
	 * @param rule
	 *        The rule to store. A copy is taken, the given configuration is left untouched.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public void putRoleRule(RoleRuleConfig rule) throws IOException, ConfigurationException {
		ApplicationConfig.Config appConfig = readAccessManagerFile();
		putRule(accessManagerConfig(appConfig).getRoleRules().getRules(), rule);
		writeAccessManagerFile(appConfig);
	}

	/**
	 * Removes the security parent rule with the given id from the stored configuration.
	 *
	 * <p>
	 * A rule of the underlying configuration layers cannot be removed by layering a file onto them,
	 * so only a rule this editor stored before can be dropped again.
	 * </p>
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} of the rule to remove.
	 * @return Whether the rule was removed, <code>false</code> when the stored configuration does
	 *         not define it.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 *
	 * @see #isStoredSecurityParentRule(String)
	 */
	public boolean removeSecurityParentRule(String id) throws IOException, ConfigurationException {
		ApplicationConfig.Config appConfig = readAccessManagerFile();
		ElementAccessManager.Config config = storedAccessManagerConfig(appConfig);
		if (config == null || !removeRule(config.getSecurityParents().getRules(), id)) {
			return false;
		}
		writeAccessManagerFile(appConfig);
		return true;
	}

	/**
	 * Removes the role rule with the given id from the stored configuration.
	 *
	 * <p>
	 * A rule of the underlying configuration layers cannot be removed by layering a file onto them,
	 * so only a rule this editor stored before can be dropped again.
	 * </p>
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} of the rule to remove.
	 * @return Whether the rule was removed, <code>false</code> when the stored configuration does
	 *         not define it.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 *
	 * @see #isStoredRoleRule(String)
	 */
	public boolean removeRoleRule(String id) throws IOException, ConfigurationException {
		ApplicationConfig.Config appConfig = readAccessManagerFile();
		ElementAccessManager.Config config = storedAccessManagerConfig(appConfig);
		if (config == null || !removeRule(config.getRoleRules().getRules(), id)) {
			return false;
		}
		writeAccessManagerFile(appConfig);
		return true;
	}

	/**
	 * A copy of the security parent rule with the given id, as it is currently in effect.
	 *
	 * <p>
	 * The copy is meant to be edited and handed back to
	 * {@link #putSecurityParentRule(NavigationRuleConfig)}, which stores it under its id. A rule of
	 * the underlying configuration layers is thereby replaced by the edited one.
	 * </p>
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} of the rule to edit.
	 * @return The copy, or <code>null</code> when no such rule is in effect.
	 */
	public NavigationRuleConfig editableSecurityParentRule(String id) {
		return copyRule(effectiveConfig().getSecurityParents().getRules(), id);
	}

	/**
	 * A copy of the role rule with the given id, as it is currently in effect.
	 *
	 * <p>
	 * The copy is meant to be edited and handed back to {@link #putRoleRule(RoleRuleConfig)}, which
	 * stores it under its id. A rule of the underlying configuration layers is thereby replaced by
	 * the edited one.
	 * </p>
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} of the rule to edit.
	 * @return The copy, or <code>null</code> when no such rule is in effect.
	 */
	public RoleRuleConfig editableRoleRule(String id) {
		return copyRule(effectiveConfig().getRoleRules().getRules(), id);
	}

	/**
	 * The access rights entries the stored configuration defines.
	 *
	 * @return A snapshot of the entries of {@link #getGrantsFile()}, empty when the file defines
	 *         none.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public List<ModelAccessRights> storedAccessRights() throws ConfigurationException {
		SecurityConfigurationService.Config config = storedGrantsConfig(readGrantsFile());
		return config == null ? Collections.emptyList() : List.copyOf(config.getSecurityConfig().values());
	}

	/**
	 * The number of access rights entries {@link #getGrantsFile()} defines.
	 *
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public int storedAccessRightsCount() throws ConfigurationException {
		return storedAccessRights().size();
	}

	/**
	 * A copy of the access rights the stored configuration defines for the given type.
	 *
	 * <p>
	 * The copy is meant to be edited and handed back to
	 * {@link #putAccessRights(ModelAccessRights)}, which stores it under the name of the type.
	 * Since the grants of all configuration layers are appended to one sequence, the copy holds the
	 * stored grants only: the grants of the underlying layers stay in effect without being repeated
	 * here.
	 * </p>
	 *
	 * @param type
	 *        The type to edit the access rights of.
	 * @return The copy of the stored entry, or a fresh entry carrying only the name of the type.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public TLClassAccessRights editableAccessRights(TLClass type) throws ConfigurationException {
		return editableTypeBasedRights(TLModelUtil.qualifiedName(type), TLClassAccessRights.class);
	}

	/**
	 * A copy of the access rights the stored configuration defines for the given module.
	 *
	 * <p>
	 * The copy is meant to be edited and handed back to
	 * {@link #putAccessRights(ModelAccessRights)}, which stores it under the name of the module.
	 * Since the grants of all configuration layers are appended to one sequence, the copy holds the
	 * stored grants only: the grants of the underlying layers stay in effect without being repeated
	 * here.
	 * </p>
	 *
	 * @param module
	 *        The module to edit the access rights of.
	 * @return The copy of the stored entry, or a fresh entry carrying only the name of the module.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public TLModuleAccessRights editableAccessRights(TLModule module) throws ConfigurationException {
		return editableTypeBasedRights(TLModelUtil.qualifiedName(module), TLModuleAccessRights.class);
	}

	/**
	 * Stores the given access rights entry, replacing a stored entry of the same name.
	 *
	 * @param entry
	 *        The entry to store. A copy is taken, the given configuration is left untouched.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public void putAccessRights(ModelAccessRights entry) throws IOException, ConfigurationException {
		ApplicationConfig.Config appConfig = readGrantsFile();
		grantsConfig(appConfig).getSecurityConfig().put(entry.getName(), TypedConfiguration.copy(entry));
		writeGrantsFile(appConfig);
	}

	/**
	 * Marks the given type as used by the application's own code only, or drops that mark.
	 *
	 * @param type
	 *        The type to mark.
	 * @param value
	 *        The value of {@link TypeBasedAccessRights#isInternal()} to store.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public void setInternal(TLClass type, boolean value) throws IOException, ConfigurationException {
		TLClassAccessRights entry = editableAccessRights(type);
		entry.setInternal(value);
		putAccessRights(entry);
	}

	/**
	 * Excludes the given type from access control, or drops that exclusion.
	 *
	 * @param type
	 *        The type to exclude.
	 * @param value
	 *        The value of {@link TypeBasedAccessRights#isWithoutSecurity()} to store.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public void setWithoutSecurity(TLClass type, boolean value) throws IOException, ConfigurationException {
		TLClassAccessRights entry = editableAccessRights(type);
		entry.setWithoutSecurity(value);
		putAccessRights(entry);
	}

	/**
	 * A copy of the security parent rule the analysis proposes for the given type.
	 *
	 * @param coverage
	 *        An entry of the result of {@link SecurityCoverageCheck#analyze()}.
	 * @return The proposed rule, or <code>null</code> when the type has no
	 *         {@link FindingKind#SUGGESTED_PARENT} finding.
	 *
	 * @see CoverageFinding#getSuggestedRule()
	 */
	public NavigationRuleConfig proposedRule(TypeCoverage coverage) {
		List<CoverageFinding> proposals = coverage.findings(FindingKind.SUGGESTED_PARENT);
		if (proposals.isEmpty()) {
			return null;
		}
		return TypedConfiguration.copy(proposals.get(0).getSuggestedRule());
	}

	/**
	 * Stores the security parent rule the analysis proposes for the given type.
	 *
	 * <p>
	 * A type the analysis has no proposal for is left alone.
	 * </p>
	 *
	 * @param coverage
	 *        An entry of the result of {@link SecurityCoverageCheck#analyze()}.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 *
	 * @see #proposedRule(TypeCoverage)
	 */
	public void acceptProposal(TypeCoverage coverage) throws IOException, ConfigurationException {
		NavigationRuleConfig rule = proposedRule(coverage);
		if (rule == null) {
			return;
		}
		putSecurityParentRule(rule);
	}

	/**
	 * Makes the stored definition effective in the running application.
	 *
	 * <p>
	 * The application configuration is read again, so that the files this editor wrote take part in
	 * it, and the two services carrying the access definition are restarted with it. Since the
	 * {@link SecurityCoverageCheck} depends on both of them, it is restarted as well and
	 * {@link SecurityCoverageCheck#analyze()} reports the definition of the files afterwards.
	 * </p>
	 *
	 * @implNote The grants are restarted before the access manager: neither service depends on the
	 *           other, and the access manager is restarted last because rebuilding the security
	 *           storage is the expensive step that must see the final state.
	 */
	public void apply() {
		TLServiceUtils.reloadConfigurations();
		TLServiceUtils.restartService(SecurityConfigurationService.Module.INSTANCE);
		TLServiceUtils.restartService(AccessManager.Module.INSTANCE);
	}

	/**
	 * The access manager configuration that is currently in effect, base configuration and stored
	 * configuration merged.
	 */
	private ElementAccessManager.Config effectiveConfig() {
		AccessManager manager = AccessManager.getInstance();
		if (!(manager instanceof ElementAccessManager)) {
			throw new IllegalStateException("The access manager does not define rules: " + manager);
		}
		return (ElementAccessManager.Config) manager.getConfig();
	}

	private ApplicationConfig.Config readAccessManagerFile() throws ConfigurationException {
		return InAppServiceConfigStore.read(_accessManagerFile);
	}

	private void writeAccessManagerFile(ApplicationConfig.Config appConfig) throws IOException {
		InAppServiceConfigStore.write(_accessManagerFile, appConfig, InAppServiceConfigStore.LAYER_ONTO_BASE);
	}

	private ApplicationConfig.Config readGrantsFile() throws ConfigurationException {
		return InAppServiceConfigStore.read(_grantsFile);
	}

	private void writeGrantsFile(ApplicationConfig.Config appConfig) throws IOException {
		InAppServiceConfigStore.write(_grantsFile, appConfig, InAppServiceConfigStore.LAYER_ONTO_BASE);
	}

	/**
	 * The access manager entry of the given stored configuration, or <code>null</code> when it has
	 * none.
	 */
	private ElementAccessManager.Config storedAccessManagerConfig(ApplicationConfig.Config appConfig) {
		ModuleConfiguration entry = appConfig.getServices().get(AccessManager.class);
		return entry == null ? null : accessManagerConfig(entry);
	}

	/**
	 * The access manager entry of the given stored configuration, created on demand.
	 */
	private ElementAccessManager.Config accessManagerConfig(ApplicationConfig.Config appConfig) {
		ModuleConfiguration entry = appConfig.getServices().get(AccessManager.class);
		if (entry == null) {
			entry = InAppServiceConfigStore.newServiceEntry(AccessManager.class, newAccessManagerConfig());
			appConfig.getServices().put(AccessManager.class, entry);
		}
		return accessManagerConfig(entry);
	}

	private static ElementAccessManager.Config accessManagerConfig(ModuleConfiguration entry) {
		ServiceConfiguration<?> instance = entry.getInstance();
		if (!(instance instanceof ElementAccessManager.Config)) {
			throw new IllegalStateException("The stored access manager configuration defines no rules: " + instance);
		}
		return (ElementAccessManager.Config) instance;
	}

	/**
	 * The grants entry of the given stored configuration, or <code>null</code> when it has none.
	 */
	private SecurityConfigurationService.Config storedGrantsConfig(ApplicationConfig.Config appConfig) {
		ModuleConfiguration entry = appConfig.getServices().get(SecurityConfigurationService.class);
		return entry == null ? null : grantsConfig(entry);
	}

	/**
	 * The grants entry of the given stored configuration, created on demand.
	 */
	private SecurityConfigurationService.Config grantsConfig(ApplicationConfig.Config appConfig) {
		ModuleConfiguration entry = appConfig.getServices().get(SecurityConfigurationService.class);
		if (entry == null) {
			entry = InAppServiceConfigStore.newServiceEntry(SecurityConfigurationService.class, newGrantsConfig());
			appConfig.getServices().put(SecurityConfigurationService.class, entry);
		}
		return grantsConfig(entry);
	}

	private static SecurityConfigurationService.Config grantsConfig(ModuleConfiguration entry) {
		ServiceConfiguration<?> instance = entry.getInstance();
		if (!(instance instanceof SecurityConfigurationService.Config)) {
			throw new IllegalStateException("The stored configuration defines no access rights: " + instance);
		}
		return (SecurityConfigurationService.Config) instance;
	}

	/**
	 * Creates the access manager entry of a stored configuration that holds no rules yet.
	 *
	 * @implNote Both the configuration interface and the implementation class are taken from the
	 *           running access manager: the stored entry then offers the same properties as the
	 *           configuration it is layered onto, and it names the class that is already in effect
	 *           instead of falling back to the default implementation class of the property.
	 */
	private ServiceConfiguration<?> newAccessManagerConfig() {
		AccessManager manager = AccessManager.getInstance();
		ServiceConfiguration<AccessManager> result = TypedConfiguration.newConfigItem(configInterface(manager));
		result.setImplementationClass(manager.getClass());
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Class<ServiceConfiguration<AccessManager>> configInterface(AccessManager manager) {
		return (Class<ServiceConfiguration<AccessManager>>) (Class<?>) manager.getConfig()
			.getConfigurationInterface();
	}

	/**
	 * Creates the grants entry of a stored configuration that holds no access rights yet.
	 */
	private ServiceConfiguration<?> newGrantsConfig() {
		SecurityConfigurationService.Config result =
			TypedConfiguration.newConfigItem(SecurityConfigurationService.Config.class);
		result.setImplementationClass(SecurityConfigurationService.class);
		return result;
	}

	/**
	 * A copy of the stored entry for the given model element, or a fresh entry carrying only its
	 * name.
	 */
	private <R extends TypeBasedAccessRights> R editableTypeBasedRights(String name, Class<R> entryType)
			throws ConfigurationException {
		SecurityConfigurationService.Config config = storedGrantsConfig(readGrantsFile());
		ModelAccessRights stored = config == null ? null : config.getSecurityConfig().get(name);
		if (entryType.isInstance(stored)) {
			return TypedConfiguration.copy(entryType.cast(stored));
		}
		R result = TypedConfiguration.newConfigItem(entryType);
		result.setName(name);
		return result;
	}

	/**
	 * Adds a copy of the given rule to the given definition, replacing a rule of the same id at its
	 * position.
	 */
	private static <R extends NavigationRuleConfig> void putRule(List<R> rules, R rule) {
		R copy = TypedConfiguration.copy(rule);
		int index = indexOf(rules, rule.getId());
		if (index < 0) {
			rules.add(copy);
		} else {
			rules.remove(index);
			rules.add(index, copy);
		}
	}

	/**
	 * Drops the rule with the given id from the given definition.
	 *
	 * @return Whether a rule was dropped.
	 */
	private static boolean removeRule(List<? extends NavigationRuleConfig> rules, String id) {
		int index = indexOf(rules, id);
		if (index < 0) {
			return false;
		}
		rules.remove(index);
		return true;
	}

	/**
	 * A copy of the rule with the given id, or <code>null</code> when the given definition has
	 * none.
	 */
	private static <R extends NavigationRuleConfig> R copyRule(List<R> rules, String id) {
		int index = indexOf(rules, id);
		return index < 0 ? null : TypedConfiguration.copy(rules.get(index));
	}

	/**
	 * The position of the rule with the given id in the given definition, or <code>-1</code>.
	 *
	 * @see SecurityParentsConfig#getRules() The rules are keyed by their id.
	 */
	private static int indexOf(List<? extends NavigationRuleConfig> rules, String id) {
		for (int n = 0, size = rules.size(); n < size; n++) {
			if (rules.get(n).getId().equals(id)) {
				return n;
			}
		}
		return -1;
	}

}
