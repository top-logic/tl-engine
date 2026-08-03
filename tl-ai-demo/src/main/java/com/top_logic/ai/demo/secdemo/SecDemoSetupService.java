/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.secdemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.config.AccessConfiguration;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig.ViewConfig;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig.ViewConfig.CommandGroupRef;
import com.top_logic.tool.boundsec.config.AccessConfigurationSetupService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.util.LayoutBasedSecurity;
import com.top_logic.util.model.ModelService;

/**
 * Seeds the fixtures of the agent security test bed: one account per persona, its group membership,
 * the role that group carries on the security root, the component (layout) access every persona
 * needs to reach the agent chat, and a handful of example instances.
 *
 * <p>
 * The service is only started when the opt-in aspect {@code WEB-INF/conf/secdemo.config.xml} is
 * listed in {@code WEB-INF/conf/metaConf.txt}. All steps are idempotent, so restarting the
 * application does not duplicate anything.
 * </p>
 *
 * <p>
 * The roles themselves are created by {@link InitialRolesManager}, the groups by
 * {@link InitialGroupManager}; both are configured in the same aspect. What is left - assigning a
 * role to a group <em>on the security root</em> and creating accounts - has no declarative
 * counterpart in the engine, hence this service. It reads which accounts to create from
 * {@link Config#getPersonas()}, so the whole persona matrix lives in the aspect file rather than
 * half of it here.
 * </p>
 *
 * <p>
 * The same is true for component access. The engine reads it declaratively from
 * {@code WEB-INF/conf/security.xml} ({@link AccessConfigurationSetupService}), but that path is
 * fixed and the import <em>resets</em> the access of every view in the application before applying
 * the file. An opt-in aspect must not do that, so this service applies the very same
 * {@link AccessConfiguration} format additively.
 * </p>
 *
 * @see com.top_logic.model.security.SecurityConfigurationService
 * @see Config#getPersonas()
 * @see Config#getComponentAccess()
 */
@ServiceDependencies({
	ModelService.Module.class,
	PersonManager.Module.class,
	TLSecurityDeviceManager.Module.class,
	InitialRolesManager.Module.class,
	InitialGroupManager.Module.class,
	AccessManager.Module.class,
	BoundHelper.Module.class,
	CommandGroupRegistry.Module.class,
	SecurityComponentCache.Module.class,
	LayoutBasedSecurity.Module.class,
	AccessConfigurationSetupService.Module.class,
})
public class SecDemoSetupService extends ConfiguredManagedClass<SecDemoSetupService.Config<?>> {

	/** Qualified name of the type the personas may work with. */
	private static final String NOTE_TYPE = "secdemo:Note";

	/** Qualified name of the type no persona may touch. */
	private static final String SECRET_TYPE = "secdemo:Secret";

	/**
	 * Profile name in {@link Config#getComponentAccess()} that stands for every persona role.
	 *
	 * @see Config#getComponentAccess()
	 */
	private static final String EVERY_PERSONA_ROLE = "*";

	/**
	 * Configuration options for {@link SecDemoSetupService}.
	 */
	public interface Config<I extends SecDemoSetupService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for the initial password.
		 *
		 * @see #getInitialPassword()
		 */
		String INITIAL_PASSWORD = "initial-password";

		/**
		 * Configuration property name for the personas.
		 *
		 * @see #getPersonas()
		 */
		String PERSONAS = "personas";

		/**
		 * Configuration property name for the component access.
		 *
		 * @see #getComponentAccess()
		 */
		String COMPONENT_ACCESS = "component-access";

		/**
		 * Password given to the persona accounts when they are created.
		 *
		 * <p>
		 * Must satisfy the password policy of the configured authentication device (by default: at
		 * least 8 characters from at least two character classes).
		 * </p>
		 */
		@Name(INITIAL_PASSWORD)
		@StringDefault("Sec-Demo-1")
		String getInitialPassword();

		/**
		 * The accounts to seed, indexed by login.
		 *
		 * <p>
		 * One entry per persona of the test bed. Kept in configuration rather than in code so that
		 * the matrix a tester has to hold in their head - which login holds which role, and what
		 * each one demonstrates - is readable in one place, next to the {@code <tool-access>} policy
		 * and the {@code <security-config>} that give those roles their meaning.
		 * </p>
		 */
		@Name(PERSONAS)
		@Key(PersonaConfig.LOGIN)
		@EntryTag("persona")
		List<PersonaConfig> getPersonas();

		/**
		 * The views (layout components) the persona roles may open, in the format of the engine's
		 * {@code WEB-INF/conf/security.xml}.
		 *
		 * <p>
		 * Model access rights alone do not make an application usable: a role that may read a type
		 * still sees no tab until it holds a command group on the enclosing
		 * {@link com.top_logic.tool.boundsec.compound.CompoundSecurityLayout}. This is where the
		 * persona roles get that.
		 * </p>
		 *
		 * <p>
		 * A profile named {@code "*"} is applied to the role of every configured
		 * {@link #getPersonas() persona}. That is how the test bed states that all personas see the
		 * same UI: the only thing distinguishing two of them is then the policy, never what they can
		 * reach, which is what makes an allow/deny pair on one prompt comparable. A profile named
		 * after a single role still works, for a persona that deliberately needs different views.
		 * </p>
		 *
		 * <p>
		 * In contrast to the engine's import, the entries here are applied <em>additively</em>: no
		 * access outside this configuration is touched, so the shipped demo is unaffected while the
		 * aspect is switched off.
		 * </p>
		 */
		@Name(COMPONENT_ACCESS)
		AccessConfiguration getComponentAccess();

	}

	/**
	 * One persona of the test bed: an account, the group it joins, and the role that group carries.
	 */
	public interface PersonaConfig extends ConfigurationItem {

		/**
		 * Configuration property name for the login.
		 *
		 * @see #getLogin()
		 */
		String LOGIN = "login";

		/**
		 * Configuration property name for the group.
		 *
		 * @see #getGroup()
		 */
		String GROUP = "group";

		/**
		 * Configuration property name for the role.
		 *
		 * @see #getRole()
		 */
		String ROLE = "role";

		/**
		 * Configuration property name for the purpose.
		 *
		 * @see #getPurpose()
		 */
		String PURPOSE = "purpose";

		/**
		 * Login name of the account to create.
		 */
		@Name(LOGIN)
		@Mandatory
		String getLogin();

		/**
		 * Name of the group this account joins.
		 *
		 * <p>
		 * Must be declared on {@link InitialGroupManager} in the same aspect.
		 * </p>
		 */
		@Name(GROUP)
		@Mandatory
		String getGroup();

		/**
		 * Name of the role the {@link #getGroup() group} is granted on the security root.
		 *
		 * <p>
		 * Must be declared on {@link InitialRolesManager} in the same aspect. This is the name the
		 * {@code <security-config>} and the {@code <tool-access>} policy are written against.
		 * </p>
		 */
		@Name(ROLE)
		@Mandatory
		String getRole();

		/**
		 * One line saying what this persona is in the fixture to demonstrate.
		 *
		 * <p>
		 * Logged at boot, so the boot log doubles as an index of the test bed.
		 * </p>
		 */
		@Name(PURPOSE)
		String getPurpose();

	}

	/**
	 * Creates a {@link SecDemoSetupService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SecDemoSetupService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();

		ThreadContextManager.inSystemInteraction(SecDemoSetupService.class, () -> setup());
	}

	private void setup() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Setting up the security demo fixtures."))) {
			setupPersonas();
			grantComponentAccess();
			seedInstances();
			tx.commit();
		} catch (RuntimeException ex) {
			throw new RuntimeException("Failed to set up the security demo fixtures: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Creates every configured persona: its account, its group membership and the role its group
	 * holds on the security root.
	 *
	 * <p>
	 * The security root is the common security parent of every model object, so a role assigned
	 * there is visible for the instance checks of
	 * {@link com.top_logic.model.security.SecurityConfigurationService} as well as for its
	 * type-level {@code CREATE} check, which falls back to the security root as context. It is also
	 * where {@code AiToolAccessService} reads the roles it authorizes tool calls against.
	 * </p>
	 */
	private void setupPersonas() {
		BoundObject securityRoot = BoundHelper.getInstance().getDefaultObject();
		if (securityRoot == null) {
			throw new RuntimeException("No security root available, cannot assign the persona roles.");
		}

		for (PersonaConfig persona : getConfig().getPersonas()) {
			assignRole(securityRoot, persona.getGroup(), persona.getRole());
			setupAccount(persona);
		}
	}

	private void assignRole(BoundObject securityRoot, String groupName, String roleName) {
		BoundedRole.assignRole(securityRoot, group(groupName), role(roleName));
	}

	/**
	 * Grants the configured command groups on the configured views to the configured roles.
	 *
	 * <p>
	 * This is the component-security half of the setup: without it, the persona accounts hold model
	 * access rights and tool grants but no role may open any view, so the application shows no tab
	 * at all.
	 * </p>
	 *
	 * @see Config#getComponentAccess()
	 */
	private void grantComponentAccess() {
		for (ProfileConfig profile : getConfig().getComponentAccess().getProfiles()) {
			for (String roleName : rolesOfProfile(profile)) {
				BoundedRole role = role(roleName);
				for (ViewConfig view : profile.getViews()) {
					grantViewAccess(role, view);
				}
			}
		}
	}

	/**
	 * The roles a {@code <profile>} applies to: the wildcard expands to every persona role.
	 */
	private Set<String> rolesOfProfile(ProfileConfig profile) {
		String profileName = profile.getRole();
		if (!EVERY_PERSONA_ROLE.equals(profileName)) {
			return Set.of(profileName);
		}
		// LinkedHashSet: stable log order, and two personas may share a role.
		Set<String> roleNames = new LinkedHashSet<>();
		for (PersonaConfig persona : getConfig().getPersonas()) {
			roleNames.add(persona.getRole());
		}
		return roleNames;
	}

	private void grantViewAccess(BoundedRole role, ViewConfig view) {
		ComponentName viewName = view.getComponent();
		PersBoundComp securityComponent = SecurityComponentCache.getSecurityComponent(viewName);
		if (securityComponent == null) {
			throw new RuntimeException("View '" + viewName + "' is not a security component. Only a"
				+ " 'securityLayout' (CompoundSecurityLayout) can hold access rights; check the '"
				+ Config.COMPONENT_ACCESS + "' configuration in 'secdemo.config.xml'.");
		}

		for (CommandGroupRef commandGroupRef : view.getCommandGroups()) {
			String commandGroupName = commandGroupRef.getName();
			BoundCommandGroup commandGroup = CommandGroupRegistry.getInstance().getGroup(commandGroupName);
			if (commandGroup == null) {
				throw new RuntimeException("Command group '" + commandGroupName + "' does not exist, check the '"
					+ Config.COMPONENT_ACCESS + "' configuration in 'secdemo.config.xml'.");
			}

			if (securityComponent.addAccess(commandGroup, role)) {
				Logger.info("Granted '" + commandGroupName + "' on view '" + viewName + "' to role '"
					+ role.getName() + "'.", SecDemoSetupService.class);
			}
		}
	}

	/**
	 * Ensures that the persona's account exists, can log in and is a member of its group.
	 */
	private void setupAccount(PersonaConfig persona) {
		String login = persona.getLogin();
		Person account = Person.byName(login);
		if (account == null) {
			AuthenticationDevice device = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice();
			account = Person.create(PersistencyLayer.getKnowledgeBase(), login, device);
			setInitialPassword(device, account);
			Logger.info("Created security demo persona '" + login + "' (role '" + persona.getRole() + "'): "
				+ purposeOf(persona), SecDemoSetupService.class);
		}

		group(persona.getGroup()).addMember(account);
	}

	private static String purposeOf(PersonaConfig persona) {
		String purpose = persona.getPurpose();
		return purpose == null || purpose.isBlank() ? "(no purpose documented)" : purpose;
	}

	private void setInitialPassword(AuthenticationDevice device, Person account) {
		if (device == null || !device.allowPwdChange()) {
			Logger.warn("The authentication device does not allow setting a password; the security demo account '"
				+ account.getName() + "' needs a password assigned manually.", SecDemoSetupService.class);
			return;
		}

		char[] password = getConfig().getInitialPassword().toCharArray();
		try {
			device.setPassword(account, password);
		} finally {
			Arrays.fill(password, (char) 0);
		}
	}

	private static Group group(String groupName) {
		Group group = Group.getGroupByName(groupName);
		if (group == null) {
			throw new RuntimeException("Group '" + groupName + "' does not exist, check the InitialGroupManager"
				+ " configuration in 'secdemo.config.xml'.");
		}
		return group;
	}

	private static BoundedRole role(String roleName) {
		BoundedRole role = BoundedRole.getRoleByName(roleName);
		if (role == null) {
			throw new RuntimeException("Role '" + roleName + "' does not exist, check the InitialRolesManager"
				+ " configuration in 'secdemo.config.xml'.");
		}
		return role;
	}

	/**
	 * Creates the example data, unless instances of the demo types already exist.
	 */
	private void seedInstances() {
		createIfEmpty(NOTE_TYPE, "title", "body", List.of(
			new String[] { "Release checklist", "Cut the branch, run the tests, tag the build." },
			new String[] { "Team offsite", "Agenda for the next offsite: retro, roadmap, dinner." },
			new String[] { "Open questions", "Which of the staged enforcement layers ships first?" }));

		createIfEmpty(SECRET_TYPE, "label", "value", List.of(
			new String[] { "Payroll export", "salary-2026-Q3: 4711" },
			new String[] { "Deploy key", "ssh-ed25519 AAAA-not-a-real-key" }));
	}

	private void createIfEmpty(String typeName, String firstAttribute, String secondAttribute,
			List<String[]> values) {
		TLClass type = (TLClass) TLModelUtil.findType(typeName);
		if (!MetaElementUtil.getAllInstancesOf(type, TLObject.class).isEmpty()) {
			return;
		}

		for (String[] value : values) {
			TLObject instance = ModelService.getInstance().getFactory().createObject(type);
			instance.tUpdateByName(firstAttribute, value[0]);
			instance.tUpdateByName(secondAttribute, value[1]);
		}
		Logger.info("Created " + values.size() + " example instances of '" + typeName + "'.",
			SecDemoSetupService.class);
	}

	/**
	 * The logins of the configured personas, in configuration order.
	 *
	 * <p>
	 * For tests that want to walk the whole matrix without repeating it: the fixture is the aspect
	 * file, and a test that hard-codes the list can silently stop covering a persona that was added
	 * there.
	 * </p>
	 *
	 * @return The logins, never <code>null</code>.
	 */
	public static List<String> personaLogins() {
		List<String> logins = new ArrayList<>();
		for (PersonaConfig persona : getInstance().getConfig().getPersonas()) {
			logins.add(persona.getLogin());
		}
		return logins;
	}

	/**
	 * The role each configured persona holds, indexed by login.
	 *
	 * @return The mapping, never <code>null</code>.
	 */
	public static Map<String, String> personaRoles() {
		Map<String, String> roles = new LinkedHashMap<>();
		for (PersonaConfig persona : getInstance().getConfig().getPersonas()) {
			roles.put(persona.getLogin(), persona.getRole());
		}
		return roles;
	}

	/**
	 * The singleton {@link SecDemoSetupService} instance.
	 */
	public static SecDemoSetupService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link SecDemoSetupService}.
	 */
	public static final class Module extends TypedRuntimeModule<SecDemoSetupService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<SecDemoSetupService> getImplementation() {
			return SecDemoSetupService.class;
		}
	}

}
