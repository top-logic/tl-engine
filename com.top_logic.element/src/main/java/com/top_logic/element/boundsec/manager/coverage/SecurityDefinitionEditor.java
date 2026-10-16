/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.FileBasedBinaryData;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.element.boundsec.manager.rule.config.SecurityParentsConfig;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.security.AccessParentConfig;
import com.top_logic.model.security.AccessParentDefinition;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.security.SecurityConfigurationService.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.model.security.SecurityConfigurationService.TLModuleAccessRights;
import com.top_logic.model.security.SecurityConfigurationService.TypeBasedAccessRights;
import com.top_logic.model.security.SelfAccessParent;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Edit access to the model based access definition of the running application.
 *
 * <p>
 * The definition consists of the role rules and role parent rules of the {@link AccessManager}
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
 * definition differ: {@link #storedRoleParentRules()} answers what the files hold,
 * {@link #editableRoleParentRule(String)} what is currently in effect.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityDefinitionEditor {

	/**
	 * Value of the list operation attribute of an entry removing the entry of the same key of the
	 * underlying configuration layers.
	 */
	private static final String OPERATION_REMOVE = "remove";

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
	 * @see #storedRoleParentRules()
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
	 * The role parent rules the stored configuration defines.
	 *
	 * @return A snapshot of the rules of {@link #getAccessManagerFile()}, empty when the file
	 *         defines none.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 *
	 * @see #editableRoleParentRule(String) The rules that are in effect, which also include the
	 *      rules of the underlying configuration layers.
	 */
	public List<NavigationRuleConfig> storedRoleParentRules() throws ConfigurationException {
		ElementAccessManager.Config config = storedAccessManagerConfig(readAccessManagerFile().config());
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
		ElementAccessManager.Config config = storedAccessManagerConfig(readAccessManagerFile().config());
		return config == null ? Collections.emptyList() : List.copyOf(config.getRoleRules().getRules());
	}

	/**
	 * The number of rules {@link #getAccessManagerFile()} defines.
	 *
	 * @return The number of stored role parent rules plus the number of stored role rules.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public int storedRuleCount() throws ConfigurationException {
		ElementAccessManager.Config config = storedAccessManagerConfig(readAccessManagerFile().config());
		if (config == null) {
			return 0;
		}
		return config.getSecurityParents().getRules().size() + config.getRoleRules().getRules().size();
	}

	/**
	 * Whether the role parent rule with the given id is defined by the stored configuration.
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} to look for.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 *
	 * @see #removeRoleParentRule(String) Only a stored rule can be removed.
	 */
	public boolean isStoredRoleParentRule(String id) throws ConfigurationException {
		return indexOf(storedRoleParentRules(), id) >= 0;
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
	 * Stores the given role parent rule, replacing a stored rule of the same id.
	 *
	 * @param rule
	 *        The rule to store. A copy is taken, the given configuration is left untouched.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public void putRoleParentRule(NavigationRuleConfig rule) throws IOException, ConfigurationException {
		StoredRules stored = readAccessManagerFile();
		putRule(accessManagerConfig(stored.config()).getSecurityParents().getRules(), rule);
		// A removal entry would remove the stored rule again.
		stored.removedRoleParentRules().remove(rule.getId());
		writeAccessManagerFile(stored);
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
		StoredRules stored = readAccessManagerFile();
		putRule(accessManagerConfig(stored.config()).getRoleRules().getRules(), rule);
		// A removal entry would remove the stored rule again.
		stored.removedRoleRules().remove(rule.getId());
		writeAccessManagerFile(stored);
	}

	/**
	 * Removes the role parent rule with the given id.
	 *
	 * <p>
	 * A rule stored before is dropped from the stored configuration. A rule of the underlying
	 * configuration layers is removed by a removal entry in the stored configuration, see
	 * {@link #removedRoleParentRules()}. A rule of the underlying layers that is stored in an edited
	 * form is dropped from the stored configuration and removed from the underlying layers.
	 * </p>
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} of the rule to remove.
	 * @return Whether the rule was removed, <code>false</code> when neither the stored
	 *         configuration nor the underlying layers define it, or it is removed already.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public boolean removeRoleParentRule(String id) throws IOException, ConfigurationException {
		StoredRules stored = readAccessManagerFile();
		ElementAccessManager.Config config = storedAccessManagerConfig(stored.config());
		boolean dropped = config != null && removeRule(config.getSecurityParents().getRules(), id);
		boolean removed = indexOf(baseConfig().getSecurityParents().getRules(), id) >= 0
			&& stored.removedRoleParentRules().add(id);
		if (!dropped && !removed) {
			return false;
		}
		writeAccessManagerFile(stored);
		return true;
	}

	/**
	 * Removes the role rule with the given id.
	 *
	 * <p>
	 * A rule stored before is dropped from the stored configuration. A rule of the underlying
	 * configuration layers is removed by a removal entry in the stored configuration, see
	 * {@link #removedRoleRules()}. A rule of the underlying layers that is stored in an edited form
	 * is dropped from the stored configuration and removed from the underlying layers.
	 * </p>
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} of the rule to remove.
	 * @return Whether the rule was removed, <code>false</code> when neither the stored
	 *         configuration nor the underlying layers define it, or it is removed already.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public boolean removeRoleRule(String id) throws IOException, ConfigurationException {
		StoredRules stored = readAccessManagerFile();
		ElementAccessManager.Config config = storedAccessManagerConfig(stored.config());
		boolean dropped = config != null && removeRule(config.getRoleRules().getRules(), id);
		boolean removed = indexOf(baseConfig().getRoleRules().getRules(), id) >= 0
			&& stored.removedRoleRules().add(id);
		if (!dropped && !removed) {
			return false;
		}
		writeAccessManagerFile(stored);
		return true;
	}

	/**
	 * The ids of the role parent rules of the underlying configuration layers the stored
	 * configuration removes.
	 *
	 * @see #removeRoleParentRule(String)
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public Set<String> removedRoleParentRules() throws ConfigurationException {
		return Collections.unmodifiableSet(readAccessManagerFile().removedRoleParentRules());
	}

	/**
	 * The ids of the role rules of the underlying configuration layers the stored configuration
	 * removes.
	 *
	 * @see #removeRoleRule(String)
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public Set<String> removedRoleRules() throws ConfigurationException {
		return Collections.unmodifiableSet(readAccessManagerFile().removedRoleRules());
	}

	/**
	 * A copy of the role parent rule with the given id, as it is stored or, if it is not stored,
	 * as it is currently in effect.
	 *
	 * <p>
	 * The copy is meant to be edited and handed back to
	 * {@link #putRoleParentRule(NavigationRuleConfig)}, which stores it under its id. A rule of
	 * the underlying configuration layers is thereby replaced by the edited one. A stored rule is
	 * edited in its stored form, which differs from the one in effect until the stored definition
	 * is {@link #apply() applied}.
	 * </p>
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} of the rule to edit.
	 * @return The copy, or <code>null</code> when no such rule is stored or in effect.
	 */
	public NavigationRuleConfig editableRoleParentRule(String id) {
		NavigationRuleConfig stored = storedRule(this::storedRoleParentRules, id);
		return stored != null ? stored : copyRule(effectiveConfig().getSecurityParents().getRules(), id);
	}

	/**
	 * A copy of the role rule with the given id, as it is stored or, if it is not stored, as it
	 * is currently in effect.
	 *
	 * <p>
	 * The copy is meant to be edited and handed back to {@link #putRoleRule(RoleRuleConfig)}, which
	 * stores it under its id. A rule of the underlying configuration layers is thereby replaced by
	 * the edited one. A stored rule is edited in its stored form, which differs from the one in
	 * effect until the stored definition is {@link #apply() applied}.
	 * </p>
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} of the rule to edit.
	 * @return The copy, or <code>null</code> when no such rule is stored or in effect.
	 */
	public RoleRuleConfig editableRoleRule(String id) {
		RoleRuleConfig stored = storedRule(this::storedRoleRules, id);
		return stored != null ? stored : copyRule(effectiveConfig().getRoleRules().getRules(), id);
	}

	/**
	 * The role parent rule with the given id as it is currently in effect.
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} of the rule.
	 * @return The rule of the running definition, not to be modified, or <code>null</code> when no
	 *         such rule is in effect.
	 */
	public NavigationRuleConfig effectiveRoleParentRule(String id) {
		return findRule(effectiveConfig().getSecurityParents().getRules(), id);
	}

	/**
	 * The role rule with the given id as it is currently in effect.
	 *
	 * @param id
	 *        The {@link NavigationRuleConfig#getId()} of the rule.
	 * @return The rule of the running definition, not to be modified, or <code>null</code> when no
	 *         such rule is in effect.
	 */
	public RoleRuleConfig effectiveRoleRule(String id) {
		return findRule(effectiveConfig().getRoleRules().getRules(), id);
	}

	/**
	 * A copy of the stored rule with the given id, or <code>null</code> when no such rule is
	 * stored or the stored configuration cannot be read.
	 */
	private static <R extends NavigationRuleConfig> R storedRule(StoredQuery<R> stored, String id) {
		List<R> rules;
		try {
			rules = stored.run();
		} catch (ConfigurationException ex) {
			Logger.error("Cannot read the stored access definition.", ex, SecurityDefinitionEditor.class);
			return null;
		}
		return copyRule(rules, id);
	}

	/**
	 * A query for the stored rules of one kind.
	 */
	private interface StoredQuery<R extends NavigationRuleConfig> {

		/**
		 * Answers the query.
		 */
		List<R> run() throws ConfigurationException;
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
	 * here. The marks and the access parent however are those in effect, also when an underlying
	 * layer sets them.
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
	 * here. The marks and the access parent however are those in effect, also when an underlying
	 * layer sets them.
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
	 * <p>
	 * An entry of a type without an access parent drops one an underlying configuration layer
	 * sets: an unset access parent is not written at all and would leave that one in effect, so
	 * the stored entry says that the type {@link SelfAccessParent decides for itself} instead.
	 * </p>
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
		Map<String, ModelAccessRights> stored = grantsConfig(appConfig).getSecurityConfig();
		ModelAccessRights copy = TypedConfiguration.copy(entry);
		if (copy instanceof TLClassAccessRights classCopy && classCopy.getAccessParent() == null
			&& inheritsDelegation(stored.get(entry.getName()), effectiveAccessRights(entry.getName()))) {
			classCopy.setAccessParent(selfAccessParent());
		}
		stored.put(entry.getName(), copy);
		writeGrantsFile(appConfig);
	}

	/**
	 * Whether the access parent in effect delegates and comes from an underlying configuration
	 * layer, not from the stored entry.
	 *
	 * <p>
	 * An access parent the stored entry sets is dropped by storing the entry without it: the
	 * underlying layers then decide again.
	 * </p>
	 */
	private static boolean inheritsDelegation(ModelAccessRights stored, ModelAccessRights effective) {
		if (stored instanceof TLClassAccessRights storedClass && storedClass.getAccessParent() != null) {
			return false;
		}
		return effective instanceof TLClassAccessRights effectiveClass
			&& AccessParentConfig.delegates(effectiveClass.getAccessParent());
	}

	/**
	 * A new access parent setting saying that the type decides for itself.
	 */
	private static AccessParentConfig selfAccessParent() {
		AccessParentConfig result = TypedConfiguration.newConfigItem(AccessParentConfig.class);
		result.setDefinition(TypedConfiguration.newConfigItem(SelfAccessParent.Config.class));
		return result;
	}

	/**
	 * Marks the given type as used by the application's own code only, or drops that mark.
	 *
	 * <p>
	 * An internal type is not excluded from access control at the same time, the two marks
	 * contradicting each other, and it has no access parent, so setting the mark drops an
	 * exclusion and an access parent the stored configuration holds.
	 * </p>
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
		if (value) {
			entry.setWithoutSecurity(false);
			dropDelegation(entry);
		}
		putAccessRights(entry);
	}

	/**
	 * Excludes the given type from access control, or drops that exclusion.
	 *
	 * <p>
	 * A type without access control is not internal at the same time, the two marks contradicting
	 * each other, and it has no access parent, so excluding the type drops an internal mark and an
	 * access parent the stored configuration holds.
	 * </p>
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
		if (value) {
			entry.setInternal(false);
			dropDelegation(entry);
		}
		putAccessRights(entry);
	}

	/**
	 * Sets the access parent of the given type, or drops that setting.
	 * <p>
	 * A type with a delegating access parent has no grants and no marks of its own, so setting one
	 * drops the grants and marks the stored configuration holds for the type.
	 * </p>
	 * 
	 * @param type
	 *        The type whose access parent is set.
	 * @param definition
	 *        The definition to store, e.g. a
	 *        {@link com.top_logic.model.security.ContainerAccessParent.Config}; <code>null</code>
	 *        to drop the setting.
	 * @throws IOException
	 *         When the file cannot be written.
	 * @throws ConfigurationException
	 *         When the stored configuration cannot be parsed.
	 */
	public void setAccessParent(TLClass type, PolymorphicConfiguration<? extends AccessParentDefinition> definition)
			throws IOException, ConfigurationException {
		TLClassAccessRights entry = editableAccessRights(type);
		if (definition == null) {
			entry.setAccessParent(null);
		} else {
			AccessParentConfig setting = TypedConfiguration.newConfigItem(AccessParentConfig.class);
			setting.setDefinition(definition);
			entry.setAccessParent(setting);
			if (AccessParentConfig.delegates(setting)) {
				entry.getGrants().clear();
				entry.setInternal(false);
				entry.setWithoutSecurity(false);
			}
		}
		putAccessRights(entry);
	}

	/**
	 * Drops a delegating access parent of the given entry, which contradicts a definition of its
	 * own.
	 */
	private static void dropDelegation(TLClassAccessRights entry) {
		if (AccessParentConfig.delegates(entry.getAccessParent())) {
			entry.setAccessParent(null);
		}
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

	/**
	 * The content of {@link #getAccessManagerFile()}: the stored rules and the ids of the rules of
	 * the underlying configuration layers the file removes.
	 *
	 * @param config
	 *        The stored configuration, without the removal entries.
	 * @param removedRoleParentRules
	 *        The ids of the removed role parent rules, modifiable.
	 * @param removedRoleRules
	 *        The ids of the removed role rules, modifiable.
	 */
	private record StoredRules(ApplicationConfig.Config config, Set<String> removedRoleParentRules,
			Set<String> removedRoleRules) {
		// Pure data.
	}

	/**
	 * Reads {@link #getAccessManagerFile()}.
	 *
	 * <p>
	 * A removal entry refers to a rule of the underlying configuration layers, so reading it
	 * without these layers would fail. The removal entries are therefore taken out before the file
	 * is read and kept by their ids, so that writing the file back keeps them.
	 * </p>
	 */
	private StoredRules readAccessManagerFile() throws ConfigurationException {
		Set<String> removedParents = new LinkedHashSet<>();
		Set<String> removedRoleRules = new LinkedHashSet<>();
		if (!_accessManagerFile.exists()) {
			return new StoredRules(TypedConfiguration.newConfigItem(ApplicationConfig.Config.class),
				removedParents, removedRoleRules);
		}
		Document document;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			document = factory.newDocumentBuilder().parse(_accessManagerFile);
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			throw new ConfigurationException("Cannot parse '" + _accessManagerFile + "'.", ex);
		}
		takeRemovals(document, ElementAccessManager.Config.SECURITY_PARENTS, removedParents);
		takeRemovals(document, RoleRulesConfig.XML_TAG_ROLE_RULES, removedRoleRules);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			InAppServiceConfigStore.APPLICATION_TAG,
			TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class));
		ConfigurationReader reader =
			new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptors);
		reader.setSource(CharacterContents.newContent(DOMUtil.toString(document), _accessManagerFile.getPath()));
		ApplicationConfig.Config config = (ApplicationConfig.Config) reader.read();
		return new StoredRules(config, removedParents, removedRoleRules);
	}

	/**
	 * Drops the removal entries from the elements of the given name and adds the ids they remove to
	 * the given set.
	 */
	private static void takeRemovals(Document document, String containerTag, Set<String> removedIds) {
		NodeList containers = document.getElementsByTagName(containerTag);
		for (int n = 0, cnt = containers.getLength(); n < cnt; n++) {
			Node child = containers.item(n).getFirstChild();
			while (child != null) {
				Node next = child.getNextSibling();
				if (child instanceof Element entry && OPERATION_REMOVE.equals(
					entry.getAttributeNS(ConfigurationSchemaConstants.CONFIG_NS,
						ConfigurationSchemaConstants.LIST_OPERATION))) {
					removedIds.add(entry.getAttribute(NavigationRuleConfig.ID));
					entry.getParentNode().removeChild(entry);
				}
				child = next;
			}
		}
	}

	/**
	 * The access manager configuration of the underlying configuration layers: the configuration of
	 * the application without {@link #getAccessManagerFile()}.
	 */
	private ElementAccessManager.Config baseConfig() throws ConfigurationException {
		List<BinaryContent> layers = new ArrayList<>();
		for (BinaryContent layer : XMLProperties.Module.INSTANCE.config().getTypedConfigs()) {
			if (!isAccessManagerFile(layer)) {
				layers.add(layer);
			}
		}
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			InAppServiceConfigStore.APPLICATION_TAG,
			TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class));
		ConfigurationReader reader =
			new ConfigurationReader(new DefaultInstantiationContext(new BufferingProtocol()), descriptors);
		ApplicationConfig.Config config = (ApplicationConfig.Config) reader.setSources(layers)
			.setVariableExpander(AliasManager.getInstance().getExpander())
			.read();
		ModuleConfiguration entry = config.getServices().get(AccessManager.class);
		if (entry == null || !(entry.getInstance() instanceof ElementAccessManager.Config base)) {
			// The underlying layers define no rules.
			return TypedConfiguration.newConfigItem(ElementAccessManager.Config.class);
		}
		return base;
	}

	/**
	 * Whether the given configuration layer is {@link #getAccessManagerFile()}.
	 */
	private boolean isAccessManagerFile(BinaryContent layer) {
		if (!(layer instanceof FileBasedBinaryData file)) {
			return false;
		}
		try {
			return file.getFile().getCanonicalFile().equals(_accessManagerFile.getCanonicalFile());
		} catch (IOException ex) {
			return file.getFile().getAbsoluteFile().equals(_accessManagerFile.getAbsoluteFile());
		}
	}

	/**
	 * Writes the given configuration to {@link #getAccessManagerFile()}, layered onto the underlying
	 * configuration.
	 *
	 * <p>
	 * A stored rule replaces the rule of the same id of the underlying layers instead of being
	 * merged into it: the path of an edited rule would otherwise be appended to the path of the
	 * rule it was copied from.
	 * </p>
	 */
	private void writeAccessManagerFile(StoredRules stored) throws IOException {
		ApplicationConfig.Config appConfig = stored.config();
		Map<PropertyDescriptor, Set<String>> removedEntries = new HashMap<>();
		if (!stored.removedRoleParentRules().isEmpty() || !stored.removedRoleRules().isEmpty()) {
			ElementAccessManager.Config config = accessManagerConfig(appConfig);
			if (!stored.removedRoleParentRules().isEmpty()) {
				removedEntries.put(markWritten(config.getSecurityParents(), SecurityParentsConfig.RULES),
					stored.removedRoleParentRules());
			}
			if (!stored.removedRoleRules().isEmpty()) {
				removedEntries.put(markWritten(config.getRoleRules(), RoleRulesConfig.XML_TAG_RULES),
					stored.removedRoleRules());
			}
		}
		InAppServiceConfigStore.write(_accessManagerFile, appConfig, InAppServiceConfigStore.LAYER_ONTO_BASE,
			Set.of(NavigationRuleConfig.class), removedEntries);
	}

	/**
	 * Sets the value of the given list property of the given item, so that the list is written
	 * even if it is empty: the removal entries are written into it.
	 *
	 * @return The list property.
	 */
	private static PropertyDescriptor markWritten(ConfigurationItem item, String listProperty) {
		PropertyDescriptor property = item.descriptor().getProperty(listProperty);
		item.update(property, new ArrayList<>((List<?>) item.value(property)));
		return property;
	}

	private ApplicationConfig.Config readGrantsFile() throws ConfigurationException {
		return InAppServiceConfigStore.read(_grantsFile);
	}

	/**
	 * Writes the given configuration to {@link #getGrantsFile()}, layered onto the underlying
	 * configuration.
	 *
	 * <p>
	 * The definition of an access parent replaces the one of the underlying layers instead of being
	 * merged into it: a definition of another kind - <code>&lt;self/&gt;</code> over
	 * <code>&lt;container/&gt;</code> - would otherwise be dropped silently.
	 * </p>
	 */
	private void writeGrantsFile(ApplicationConfig.Config appConfig) throws IOException {
		InAppServiceConfigStore.write(_grantsFile, appConfig, Set.of(AccessParentConfig.class));
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
	 * name, completed by the marks and the access parent currently in effect.
	 *
	 * <p>
	 * A mark or an access parent an underlying configuration layer sets is in effect without being
	 * stored. The copy shows it nevertheless, so that the user sees what is in effect and can drop
	 * it: a value handed back to {@link #putAccessRights(ModelAccessRights)} is stored explicitly
	 * and overrides the one of the underlying layers. A value the stored entry sets takes
	 * precedence, even before it is {@link #apply() applied}.
	 * </p>
	 */
	private <R extends TypeBasedAccessRights> R editableTypeBasedRights(String name, Class<R> entryType)
			throws ConfigurationException {
		SecurityConfigurationService.Config config = storedGrantsConfig(readGrantsFile());
		ModelAccessRights stored = config == null ? null : config.getSecurityConfig().get(name);
		R result;
		if (entryType.isInstance(stored)) {
			result = TypedConfiguration.copy(entryType.cast(stored));
		} else {
			result = TypedConfiguration.newConfigItem(entryType);
			result.setName(name);
		}
		ModelAccessRights effective = effectiveAccessRights(name);
		if (entryType.isInstance(effective)) {
			addEffectiveSettings(result, entryType.cast(effective));
		}
		return result;
	}

	/**
	 * The access rights entry for the given model element that is currently in effect, base
	 * configuration and stored configuration merged; <code>null</code> when there is none.
	 */
	private static ModelAccessRights effectiveAccessRights(String name) {
		if (!SecurityConfigurationService.Module.INSTANCE.isActive()) {
			return null;
		}
		return SecurityConfigurationService.Module.INSTANCE.getImplementationInstance().getConfig()
			.getSecurityConfig().get(name);
	}

	/**
	 * Sets the marks and the access parent of the given effective entry that the given copy does
	 * not set of its own.
	 *
	 * <p>
	 * Only a value differing from the default is taken over, so that a copy handed back unchanged
	 * does not store a value that merely repeats the default.
	 * </p>
	 */
	private static void addEffectiveSettings(TypeBasedAccessRights copy, TypeBasedAccessRights effective) {
		if (!isSet(copy, TypeBasedAccessRights.INTERNAL) && effective.isInternal()) {
			copy.setInternal(true);
		}
		if (!isSet(copy, TypeBasedAccessRights.WITHOUT_SECURITY) && effective.isWithoutSecurity()) {
			copy.setWithoutSecurity(true);
		}
		if (copy instanceof TLClassAccessRights classCopy
			&& effective instanceof TLClassAccessRights classEffective
			&& !isSet(copy, TLClassAccessRights.ACCESS_PARENT)
			&& classEffective.getAccessParent() != null) {
			classCopy.setAccessParent(TypedConfiguration.copy(classEffective.getAccessParent()));
		}
	}

	/**
	 * Whether the given entry sets the property with the given name of its own.
	 */
	private static boolean isSet(ConfigurationItem entry, String property) {
		return entry.valueSet(entry.descriptor().getProperty(property));
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
		R rule = findRule(rules, id);
		return rule == null ? null : TypedConfiguration.copy(rule);
	}

	/**
	 * The rule with the given id of the given definition, or <code>null</code> when it has none.
	 */
	private static <R extends NavigationRuleConfig> R findRule(List<R> rules, String id) {
		int index = indexOf(rules, id);
		return index < 0 ? null : rules.get(index);
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
