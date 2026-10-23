/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.secdemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.service.migration.StartupAction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.config.AccessConfiguration;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig.ViewConfig;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig.ViewConfig.CommandGroupRef;
import com.top_logic.tool.boundsec.config.AccessConfigurationSetup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.util.StartupHooks;
import com.top_logic.util.model.ModelService;

/**
 * Seeds the fixtures of the agent security test bed: one account per persona, its group memberships,
 * the example projects and tickets, the component (layout) access every persona needs to reach the
 * agent chat - and a boot-time check that the declarative role rules produced the roles the persona
 * matrix claims.
 *
 * <p>
 * The setup runs as {@link StartupHooks startup hook} only when the opt-in aspect
 * {@code WEB-INF/conf/secdemo.config.xml} is listed in {@code WEB-INF/conf/metaConf.txt}. All steps
 * are idempotent, so restarting the application does not duplicate anything.
 * </p>
 *
 * <p>
 * <b>It assigns no roles.</b> That is the difference from the version of this fixture that preceded
 * issue #290, and it is the point: roles arrive from the {@code <role-rules>} of the same aspect - a
 * {@code <singleton-rule>} per tier, reading group membership, and a {@code <rule type="reference">}
 * deriving the scope from {@code pm:Project#partners}. Assigning them from Java taught no
 * configuration idiom and, worse, hid the failure the tier/scope split makes possible: a role rule
 * that produces nothing. So the matrix states which roles each persona is <em>expected</em> to hold
 * and this setup checks, after the seed data is committed, whether the rules delivered them.
 * </p>
 *
 * <p>
 * What has no declarative counterpart in the engine, and is therefore still here: creating accounts,
 * joining them to groups, creating example instances, and applying component access. The engine's
 * {@link AccessConfigurationSetup} reads component access declaratively, but only from one fixed
 * path, and its import <em>resets</em> the access of every view in the application before applying
 * that file. An opt-in aspect must not do either, so this setup applies the very same
 * {@link AccessConfiguration} format additively. The engine's import is a startup hook as well,
 * this setup must therefore be configured after it.
 * </p>
 *
 * @see com.top_logic.model.security.SecurityConfigurationService
 * @see Config#getPersonas()
 * @see Config#getProjects()
 * @see Config#getComponentAccess()
 */
public class SecDemoSetup extends AbstractConfiguredInstance<SecDemoSetup.Config<?>> implements StartupAction {

	/** Qualified name of the type the scope rule reads its partners from. */
	public static final String PROJECT_TYPE = "pm:Project";

	/** Qualified name of the type whose security parent is its project. */
	public static final String TICKET_TYPE = "pm:Ticket";

	/** Qualified name of the attribute the scope rule navigates. */
	public static final String PARTNERS_ATTRIBUTE = "pm:Project#partners";

	/** Qualified name of the composition a ticket lives in. */
	public static final String TICKETS_ATTRIBUTE = "pm:Project#tickets";

	/**
	 * Profile name in {@link Config#getComponentAccess()} that stands for every persona role.
	 *
	 * @see Config#getComponentAccess()
	 */
	private static final String EVERY_PERSONA_ROLE = "*";

	/**
	 * Configuration options for {@link SecDemoSetup}.
	 */
	public interface Config<I extends SecDemoSetup> extends PolymorphicConfiguration<I> {

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
		 * Configuration property name for the projects.
		 *
		 * @see #getProjects()
		 */
		String PROJECTS = "projects";

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
		 * the matrix a tester has to hold in their head - which login joins which group, which role
		 * that should produce, and what each one demonstrates - is readable in one place, next to the
		 * &lt;role-rules&gt;, the &lt;tool-access&gt; policy and the &lt;security-config&gt; that give
		 * those roles their meaning.
		 * </p>
		 */
		@Name(PERSONAS)
		@Key(PersonaConfig.LOGIN)
		@EntryTag("persona")
		List<PersonaConfig> getPersonas();

		/**
		 * The example projects and their tickets.
		 *
		 * <p>
		 * A project's partners are what the scope role rule reads, so this list is where the fixture's
		 * per-object security actually comes from: it is ordinary business data, and the rule derives
		 * the role from it.
		 * </p>
		 */
		@Name(PROJECTS)
		@Key(ProjectConfig.NAME)
		@EntryTag("project")
		List<ProjectConfig> getProjects();

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
		 * A profile named {@code "*"} is applied to every role a configured {@link #getPersonas()
		 * persona} is expected to hold. That is how the test bed states that all personas see the same
		 * UI: the only thing distinguishing two of them is then the policy, never what they can reach,
		 * which is what makes an allow/deny pair on one prompt comparable. A profile named after a
		 * single role still works, for a persona that deliberately needs different views.
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
	 * One persona of the test bed: an account, the groups it joins, and the roles the aspect's role
	 * rules are expected to give it.
	 */
	public interface PersonaConfig extends ConfigurationItem {

		/** Configuration property name for {@link #getLogin()}. */
		String LOGIN = "login";

		/** Configuration property name for {@link #getGroups()}. */
		String GROUPS = "groups";

		/** Configuration property name for {@link #getRoles()}. */
		String ROLES = "roles";

		/** Configuration property name for {@link #getPartnerOf()}. */
		String PARTNER_OF = "partner-of";

		/** Configuration property name for {@link #getPurpose()}. */
		String PURPOSE = "purpose";

		/**
		 * Login name of the account to create.
		 */
		@Name(LOGIN)
		@Label("Login name")
		@Mandatory
		String getLogin();

		/**
		 * Names of the groups this account joins.
		 *
		 * <p>
		 * Must be declared on {@link InitialGroupManager} in the same aspect. Group membership is the
		 * only thing an administrator maintains by hand here; every role follows from it through a
		 * rule.
		 * </p>
		 */
		@Name(GROUPS)
		@Format(CommaSeparatedStrings.class)
		@Mandatory
		List<String> getGroups();

		/**
		 * Names of the roles the aspect's tier rules are expected to give this account <b>on the
		 * security root</b>.
		 *
		 * <p>
		 * Not an assignment. Nothing in this setup grants a role - the {@code <singleton-rule>}
		 * entries of the aspect do, from {@link #getGroups() group membership}. This is the claim the
		 * matrix makes, and {@link SecDemoSetup} checks it after boot so that a rule which
		 * produces nothing is a warning naming the persona rather than a silently narrow agent.
		 * </p>
		 */
		@Name(ROLES)
		@Format(CommaSeparatedStrings.class)
		@Mandatory
		List<String> getRoles();

		/**
		 * Names of the projects this account is expected to hold the scope role on.
		 *
		 * <p>
		 * Also not an assignment: it follows from the account being listed in those projects'
		 * {@code partners}, which is configured on {@link Config#getProjects()}. Stated here for the
		 * same reason {@link #getRoles()} is - so the check has something to check against.
		 * </p>
		 */
		@Name(PARTNER_OF)
		@Format(CommaSeparatedStrings.class)
		List<String> getPartnerOf();

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
	 * One example project: its name, the logins it lists as partners, and the tickets it contains.
	 */
	public interface ProjectConfig extends ConfigurationItem {

		/** Configuration property name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration property name for {@link #getPartners()}. */
		String PARTNERS = "partners";

		/** Configuration property name for {@link #getTickets()}. */
		String TICKETS = "tickets";

		/**
		 * Name of the project.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * Logins of the accounts this project lists in {@code pm:Project#partners}.
		 *
		 * <p>
		 * The input of the scope role rule, and therefore the whole of this fixture's per-object
		 * security. It is ordinary business data; the rule derives the role from it.
		 * </p>
		 */
		@Name(PARTNERS)
		@Format(CommaSeparatedStrings.class)
		List<String> getPartners();

		/**
		 * The tickets contained in this project.
		 */
		@Name(TICKETS)
		@EntryTag("ticket")
		List<TicketConfig> getTickets();

	}

	/**
	 * One example ticket.
	 */
	public interface TicketConfig extends ConfigurationItem {

		/** Configuration property name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration property name for {@link #getBody()}. */
		String BODY = "body";

		/**
		 * The ticket's title, readable by everybody who may read the ticket.
		 */
		@Name(TITLE)
		@Mandatory
		String getTitle();

		/**
		 * The ticket's body, readable by {@code projectPartner} only through a {@code <part>} grant.
		 */
		@Name(BODY)
		String getBody();

	}

	/**
	 * Creates a {@link SecDemoSetup} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SecDemoSetup(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * What the last check of the persona matrix found, <code>null</code> before the first check.
	 */
	private volatile List<String> _lastProblems;

	/**
	 * What the last check of the persona matrix found and logged.
	 *
	 * @return One sentence per problem; empty when the rules produced the whole matrix;
	 *         <code>null</code> if this setup has not checked yet - when it runs inside an enclosing
	 *         transaction, it checks only once that transaction has committed.
	 */
	public List<String> lastProblems() {
		return _lastProblems;
	}

	@Override
	public void perform() {
		ThreadContext.inSystemContext(SecDemoSetup.class, () -> setup());
	}

	private void setup() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		boolean enclosed;
		Set<ObjectKey> written = new HashSet<>();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Setting up the security demo fixtures."))) {
			enclosed = tx.isNested();
			setupAccounts(written);
			grantComponentAccess();
			seedProjects(written);
			tx.commit();
		} catch (RuntimeException ex) {
			throw new RuntimeException("Failed to set up the security demo fixtures: " + ex.getMessage(), ex);
		}

		if (enclosed && !written.isEmpty()) {
			// The startup hook runs this setup inside its own transaction, so the commit above is nested
			// and persists nothing yet: checked now, every persona and project would be reported
			// missing (#626). Checked instead once a commit has persisted what this setup wrote.
			kb.addUpdateListener(new UpdateListener() {
				@Override
				public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
					if (event.isRemote() || !persists(event, written)) {
						return;
					}
					sender.removeUpdateListener(this);
					checkMatrix();
				}
			});
		} else {
			checkMatrix();
		}
	}

	/**
	 * Whether the given commit created or changed one of the given objects.
	 */
	private static boolean persists(UpdateEvent event, Set<ObjectKey> written) {
		for (ObjectKey key : written) {
			if (event.getCreatedObjectKeys().contains(key) || event.getUpdatedObjectKeys().contains(key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Logs every problem {@link #matrixProblems()} finds, and how many personas it checked.
	 */
	private void checkMatrix() {
		List<String> problems = matrixProblems();
		_lastProblems = problems;
		for (String problem : problems) {
			Logger.warn(problem, SecDemoSetup.class);
		}
		Logger.info("Security demo: checked the roles of " + getConfig().getPersonas().size()
			+ " personas against the aspect's role rules.", SecDemoSetup.class);
	}

	/**
	 * Ensures every persona's account exists, can log in and is a member of its groups.
	 *
	 * @param written
	 *        Gets the accounts this method created and the groups it added a member to.
	 */
	private void setupAccounts(Set<ObjectKey> written) {
		for (PersonaConfig persona : getConfig().getPersonas()) {
			String login = persona.getLogin();
			Person account = Person.byName(login);
			if (account == null) {
				AuthenticationDevice device = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice();
				account = Person.create(PersistencyLayer.getKnowledgeBase(), login, device);
				setInitialPassword(device, account);
				Logger.info("Created security demo persona '" + login + "' (expects role(s) "
					+ persona.getRoles() + " from the tier rules): " + purposeOf(persona),
					SecDemoSetup.class);
				written.add(account.tId());
			}

			for (String groupName : persona.getGroups()) {
				Group group = group(groupName);
				if (!group.getMembers().contains(account)) {
					group.addMember(account);
					written.add(group.tId());
				}
			}
		}
	}

	/**
	 * Every persona whose roles the aspect's rules did not produce.
	 *
	 * <p>
	 * The check the previous fixture could not have: it assigned the roles itself, so they were there
	 * by construction and a broken rule was invisible. Two questions are asked, one per assignment:
	 * does the persona hold its role <b>on the security root</b> (the tier, which is all
	 * {@code <tool-access>} can see), and does it hold the scope role <b>on the projects it partners
	 * on</b>.
	 * </p>
	 *
	 * <p>
	 * Logged as warnings at start-up rather than failing the boot. The fixture is a demo, and an
	 * application that fails to start says less about what went wrong than a log line naming the
	 * persona and the role does. The deterministic form is {@code SecDemoRoleRuleTest}.
	 * </p>
	 *
	 * @return One sentence per problem, in persona order; empty when the rules produced the whole
	 *         matrix.
	 */
	public List<String> matrixProblems() {
		List<String> problems = new ArrayList<>();
		BoundObject securityRoot = BoundHelper.getInstance().getDefaultObject();
		if (securityRoot == null) {
			problems.add("No security root available; cannot check the persona roles.");
			return problems;
		}

		for (PersonaConfig persona : getConfig().getPersonas()) {
			Person account = Person.byName(persona.getLogin());
			if (account == null) {
				problems.add("Persona '" + persona.getLogin() + "' has no account.");
				continue;
			}

			Set<String> onRoot = roleNames(account, securityRoot);
			for (String expected : persona.getRoles()) {
				if (!onRoot.contains(expected)) {
					problems.add("The role rules did not give '" + persona.getLogin() + "' the role '"
						+ expected + "' on the security root, which is where <tool-access> reads roles."
						+ " It holds " + onRoot + " there instead, so its tool grants reach nothing."
						+ " Check the <singleton-rule target=\"SecurityStructure#ROOT\"> entries and the"
						+ " group membership " + persona.getGroups() + " in 'secdemo.config.xml'.");
				}
			}

			for (String projectName : persona.getPartnerOf()) {
				TLObject project = projectByName(projectName);
				if (project == null) {
					problems.add("Persona '" + persona.getLogin() + "' is declared a partner of project '"
						+ projectName + "', which does not exist.");
					continue;
				}
				Set<String> onProject = roleNames(account, (BoundObject) project);
				if (onProject.isEmpty()) {
					problems.add("The scope rule gave '" + persona.getLogin() + "' no role on project '"
						+ projectName + "', although the project lists it as a partner. Per-object"
						+ " scoping is not working; check the <rule type=\"reference\"> over '"
						+ PARTNERS_ATTRIBUTE + "' in 'secdemo.config.xml'.");
				}
			}
		}
		return problems;
	}

	private static Set<String> roleNames(Person account, BoundObject object) {
		Set<String> names = new TreeSet<>();
		for (BoundRole role : AccessManager.getInstance().getRoles(account, object)) {
			names.add(role.getName());
		}
		return names;
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
	void grantComponentAccess() {
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
			roleNames.addAll(persona.getRoles());
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
					+ role.getName() + "'.", SecDemoSetup.class);
			}
		}
	}

	private static String purposeOf(PersonaConfig persona) {
		String purpose = persona.getPurpose();
		return purpose == null || purpose.isBlank() ? "(no purpose documented)" : purpose;
	}

	private void setInitialPassword(AuthenticationDevice device, Person account) {
		if (device == null || !device.allowPwdChange()) {
			Logger.warn("The authentication device does not allow setting a password; the security demo account '"
				+ account.getName() + "' needs a password assigned manually.", SecDemoSetup.class);
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
	 * Creates the example projects and their tickets, unless projects already exist.
	 *
	 * <p>
	 * Setting {@code partners} is what gives the scope rule something to derive a role from, so it is
	 * the security-relevant line of this method even though it looks like ordinary seed data. That is
	 * the property the fixture exists to show.
	 * </p>
	 *
	 * @param written
	 *        Gets the projects this method created.
	 */
	private void seedProjects(Set<ObjectKey> written) {
		TLClass projectType = type(PROJECT_TYPE);
		if (!MetaElementUtil.getAllInstancesOf(projectType, TLObject.class).isEmpty()) {
			return;
		}

		TLClass ticketType = type(TICKET_TYPE);
		int ticketCount = 0;
		for (ProjectConfig projectConfig : getConfig().getProjects()) {
			TLObject project = ModelService.getInstance().getFactory().createObject(projectType);
			written.add(project.tId());
			project.tUpdateByName("name", projectConfig.getName());

			List<Person> partners = new ArrayList<>();
			for (String login : projectConfig.getPartners()) {
				Person partner = Person.byName(login);
				if (partner == null) {
					throw new RuntimeException("Project '" + projectConfig.getName() + "' names '" + login
						+ "' as a partner, but no such account exists. Add it to <personas> in"
						+ " 'secdemo.config.xml'.");
				}
				partners.add(partner);
			}
			project.tUpdateByName("partners", partners);

			List<TLObject> tickets = new ArrayList<>();
			for (TicketConfig ticketConfig : projectConfig.getTickets()) {
				TLObject ticket = ModelService.getInstance().getFactory().createObject(ticketType);
				ticket.tUpdateByName("title", ticketConfig.getTitle());
				ticket.tUpdateByName("body", ticketConfig.getBody());
				tickets.add(ticket);
				ticketCount++;
			}
			project.tUpdateByName("tickets", tickets);

			Logger.info("Created project '" + projectConfig.getName() + "' with partners "
				+ projectConfig.getPartners() + " and " + tickets.size() + " ticket(s).",
				SecDemoSetup.class);
		}
		Logger.info("Created " + getConfig().getProjects().size() + " example projects with "
			+ ticketCount + " tickets.", SecDemoSetup.class);
	}

	private static TLClass type(String qualifiedName) {
		return (TLClass) TLModelUtil.findType(qualifiedName);
	}

	/**
	 * The example project of the given name, or <code>null</code> if there is none.
	 *
	 * <p>
	 * Looked up by name rather than kept in a field, because the fixture is idempotent: on a restart
	 * against an existing database nothing is created and the projects are the committed ones.
	 * </p>
	 */
	public static TLObject projectByName(String name) {
		for (TLObject project : MetaElementUtil.getAllInstancesOf(type(PROJECT_TYPE), TLObject.class)) {
			if (name.equals(project.tValueByName("name"))) {
				return project;
			}
		}
		return null;
	}

}
