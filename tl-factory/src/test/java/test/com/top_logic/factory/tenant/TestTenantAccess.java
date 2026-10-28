/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.tenant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageAnalysis;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.factory.agent.StoredConversation;
import com.top_logic.factory.project.FactoryFunctions;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.WorkspaceStatus;
import com.top_logic.factory.tenant.I18NConstants;
import com.top_logic.factory.tenant.TenantMembers;
import com.top_logic.factory.tenant.TenantMembers.NewAccount;
import com.top_logic.factory.tenant.TenantRole;
import com.top_logic.factory.tenant.Tenants;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test of the access to the tenants of the control application, their members, their projects and
 * the workspaces of the projects, with the access configuration of the module, and of the
 * management of the members of a tenant.
 *
 * <p>
 * Tenant A has an administrator, a developer and a reader; tenant B a developer. The operator holds
 * the operator role on the security root, a further account is a member of no tenant.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTenantAccess extends BasicTestCase {

	private static final String PASSWORD = "Initial-Pass-2026";

	private static final String OPERATOR_ROLE = "tl.factory.Operator";

	private static final String SECURITY_STRUCTURE = "SecurityStructure";

	private static final BoundCommandGroup READ = SimpleBoundCommandGroup.READ;

	private static final BoundCommandGroup WRITE = SimpleBoundCommandGroup.WRITE;

	private static final BoundCommandGroup DELETE = SimpleBoundCommandGroup.DELETE;

	private final List<TLObject> _created = new ArrayList<>();

	private final List<Person> _persons = new ArrayList<>();

	/** The login names of the accounts the tests create through {@link TenantMembers}. */
	private final List<String> _logins = new ArrayList<>();

	private Person _adminA;

	private Person _developerA;

	private Person _readerA;

	private Person _developerB;

	private Person _operator;

	private Person _noTenant;

	private TLObject _tenantA;

	private TLObject _tenantB;

	private TLObject _projectA;

	private TLObject _projectB;

	private TLObject _workspaceA;

	private TLObject _conversationA;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		inTransaction(() -> {
			_adminA = createPerson("tenantAdminA");
			_developerA = createPerson("tenantDeveloperA");
			_readerA = createPerson("tenantReaderA");
			_developerB = createPerson("tenantDeveloperB");
			_operator = createPerson("tenantOperator");
			_noTenant = createPerson("tenantNone");

			_tenantA = created(TestTenants.newTenant("Tenant A"));
			_tenantA.tUpdateByName(FactoryModel.TENANT_ADMINISTRATORS, Set.of(_adminA));
			_tenantA.tUpdateByName(FactoryModel.TENANT_DEVELOPERS, Set.of(_developerA));
			_tenantA.tUpdateByName(FactoryModel.TENANT_READERS, Set.of(_readerA));

			_tenantB = created(TestTenants.newTenant("Tenant B"));
			_tenantB.tUpdateByName(FactoryModel.TENANT_DEVELOPERS, Set.of(_developerB));

			_projectA = created(project(_tenantA, "project-a"));
			_projectB = created(project(_tenantB, "project-b"));

			_workspaceA = FactoryModel.createWorkspace(_projectA, "tenant-access-a", "secret", WorkspaceStatus.CREATED);
			_conversationA = FactoryModel.createConversation(_workspaceA, "A task.");

		});
		// The representative group of the operator, which holds the role, is found once committed.
		inTransaction(() -> BoundedRole.assignRole(securityRoot(), _operator, BoundedRole.getRoleByName(OPERATOR_ROLE)));
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> {
			Collections.reverse(_created);
			for (TLObject object : _created) {
				if (object.tValid()) {
					object.tDelete();
				}
			}
			for (String login : _logins) {
				Person person = Person.byName(login);
				if (person != null) {
					_persons.add(person);
				}
			}
			for (Person person : _persons) {
				Group group = person.getRepresentativeGroup();
				person.tDelete();
				if (group != null && group.tValid()) {
					group.tDelete();
				}
			}
		});
		_created.clear();
		_persons.clear();
		_logins.clear();
		super.tearDown();
	}

	/**
	 * Runs the test body with checked security: the accesses of explicitly given persons are only
	 * checked outside a system context.
	 */
	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(null, () -> {
			super.runTest();
			return null;
		});
	}

	public void testTenant() {
		for (Person member : List.of(_adminA, _developerA, _readerA, _operator)) {
			assertTrue(member.getName(), allowed(member, _tenantA, READ));
		}
		assertFalse(allowed(_developerB, _tenantA, READ));
		assertFalse(allowed(_noTenant, _tenantA, READ));

		assertTrue("The administrator manages the tenant.", allowed(_adminA, _tenantA, WRITE));
		assertTrue(allowed(_operator, _tenantA, WRITE));
		assertFalse("A developer may not change the tenant.", allowed(_developerA, _tenantA, WRITE));
		assertFalse(allowed(_readerA, _tenantA, WRITE));
		assertFalse(allowed(_developerB, _tenantA, WRITE));

		assertTrue("Only an operator deletes a tenant.", allowed(_operator, _tenantA, DELETE));
		assertFalse(allowed(_adminA, _tenantA, DELETE));
		assertFalse(allowed(_developerA, _tenantA, DELETE));

		assertTrue("Only an operator creates a tenant.",
			rights().isAllowedCreate(_operator, FactoryModel.tenantType(), (TLObject) null));
		assertFalse(rights().isAllowedCreate(_adminA, FactoryModel.tenantType(), _tenantA));
		assertFalse(rights().isAllowedCreate(_adminA, FactoryModel.tenantType(), (TLObject) null));
	}

	public void testProject() {
		for (Person member : List.of(_adminA, _developerA, _readerA, _operator)) {
			assertTrue(member.getName(), allowed(member, _projectA, READ));
		}
		for (Person member : List.of(_adminA, _developerA, _operator)) {
			assertTrue(member.getName(), allowed(member, _projectA, WRITE));
			assertTrue(member.getName(), allowed(member, _projectA, DELETE));
		}
		assertFalse("A reader only reads.", allowed(_readerA, _projectA, WRITE));
		assertFalse(allowed(_readerA, _projectA, DELETE));

		for (Person other : List.of(_developerB, _noTenant)) {
			assertFalse(other.getName(), allowed(other, _projectA, READ));
			assertFalse(other.getName(), allowed(other, _projectA, WRITE));
			assertFalse(other.getName(), allowed(other, _projectA, DELETE));
		}
		assertTrue("A developer works on the projects of the own tenant.", allowed(_developerB, _projectB, WRITE));
		assertFalse(allowed(_developerA, _projectB, READ));
	}

	public void testCreateProject() {
		assertTrue(rights().isAllowedCreate(_developerA, FactoryModel.projectType(), _tenantA));
		assertTrue(rights().isAllowedCreate(_adminA, FactoryModel.projectType(), _tenantA));
		assertFalse("A reader creates no project.",
			rights().isAllowedCreate(_readerA, FactoryModel.projectType(), _tenantA));
		assertFalse("No project in another tenant.",
			rights().isAllowedCreate(_developerB, FactoryModel.projectType(), _tenantA));
		assertFalse("Without a tenant, the right is checked on the security root.",
			rights().isAllowedCreate(_developerA, FactoryModel.projectType(), (TLObject) null));
		assertFalse(rights().isAllowedCreate(_noTenant, FactoryModel.projectType(), (TLObject) null));
		assertTrue(rights().isAllowedCreate(_operator, FactoryModel.projectType(), (TLObject) null));
		assertTrue(rights().isAllowedCreate(_operator, FactoryModel.projectType(), _tenantA));
	}

	public void testWorkspaceFollowsProject() {
		for (TLObject part : List.of(_workspaceA, _conversationA)) {
			assertTrue(allowed(_readerA, part, READ));
			assertFalse("A reader only reads.", allowed(_readerA, part, WRITE));
			assertTrue(allowed(_developerA, part, WRITE));
			assertTrue("Deleting a part is writing its project.", allowed(_developerA, part, DELETE));
			assertTrue(allowed(_operator, part, WRITE));
			assertFalse(allowed(_developerB, part, READ));
			assertFalse(allowed(_noTenant, part, READ));
		}
		assertTrue("Creating a workspace is writing the project.",
			rights().isAllowedCreate(_developerA, _projectA, part(FactoryModel.projectType().getName(),
				FactoryModel.PROJECT_WORKSPACE)));
		assertFalse(rights().isAllowedCreate(_readerA, _projectA, part(FactoryModel.projectType().getName(),
			FactoryModel.PROJECT_WORKSPACE)));
	}

	/**
	 * The initial password of the application of a workspace logs into it as root, so only the
	 * members working on the project and the operators read it.
	 */
	public void testInitialPasswordNotForReaders() {
		TLStructuredTypePart password = part(_workspaceA.tType().getName(), FactoryModel.WORKSPACE_INITIAL_PASSWORD);
		for (Person person : List.of(_adminA, _developerA, _operator)) {
			assertTrue(person.getName(), rights().isAllowed(person, _workspaceA, password, READ));
		}
		assertFalse("A reader reads the workspace but not the password.",
			rights().isAllowed(_readerA, _workspaceA, password, READ));
		assertTrue(allowed(_readerA, _workspaceA, READ));
		assertFalse(rights().isAllowed(_developerB, _workspaceA, password, READ));
		assertFalse(rights().isAllowed(_noTenant, _workspaceA, password, READ));
	}

	public void testOperationsNeedWrite() {
		for (Person person : List.of(_readerA, _developerB)) {
			try {
				as(person, () -> {
					FactoryFunctions.start(_projectA);
					return null;
				});
				fail(person.getName() + " may not start the workspace.");
			} catch (TopLogicException ex) {
				assertEquals(com.top_logic.factory.project.I18NConstants.ERROR_NO_WRITE_ACCESS__OBJECT.fill(_projectA),
					ex.getErrorKey());
			}
			try {
				as(person, () -> FactoryFunctions.setUpCI(_projectA));
				fail(person.getName() + " may not set up the CI.");
			} catch (TopLogicException ex) {
				assertEquals(com.top_logic.factory.project.I18NConstants.ERROR_NO_WRITE_ACCESS__OBJECT.fill(_projectA),
					ex.getErrorKey());
			}
			try {
				as(person, () -> FactoryFunctions.createConversation(_workspaceA, "Another task."));
				fail(person.getName() + " may not give the agent a task.");
			} catch (TopLogicException ex) {
				assertEquals(com.top_logic.factory.project.I18NConstants.ERROR_NO_WRITE_ACCESS__OBJECT.fill(_workspaceA),
					ex.getErrorKey());
			}
		}
	}

	public void testContinueConversation() {
		StoredConversation conversation = new StoredConversation(_conversationA);
		conversation.checkContinue(_developerA);
		conversation.checkContinue(_adminA);
		for (Person person : List.of(_readerA, _developerB, _noTenant)) {
			try {
				// The user's security decides, also on a thread running in a system context.
				ThreadContext.inSystemContext(TestTenantAccess.class, () -> conversation.checkContinue(person));
				fail(person.getName() + " may not talk to the agent.");
			} catch (TopLogicException ex) {
				assertEquals(com.top_logic.factory.project.I18NConstants.ERROR_NO_WRITE_ACCESS__OBJECT.fill(_conversationA),
					ex.getErrorKey());
			}
		}
	}

	public void testOneTenantPerUser() {
		assertNull("A user may hold several roles of the own tenant.",
			Tenants.checkMembers(_tenantA, List.of(_adminA, _developerA)));
		assertNull("A member of no tenant may join one.", Tenants.checkMembers(_tenantB, List.of(_noTenant)));

		ResKey refused = Tenants.checkMembers(_tenantB, List.of(_noTenant, _readerA));
		assertEquals(I18NConstants.ERROR_MEMBER_OF_OTHER_TENANT__USER_TENANT.fill(_readerA, _tenantA), refused);

		ResKey constraint = constraint(FactoryModel.TENANT_READERS).check(_tenantB,
			part(FactoryModel.TENANT, FactoryModel.TENANT_READERS));
		assertNull("Tenant B has no members of another tenant.", constraint);

		try {
			inTransaction(() -> _tenantB.tUpdateByName(FactoryModel.TENANT_READERS, Set.of(_developerA)));
			fail("A member of tenant A cannot become a member of tenant B.");
		} catch (TopLogicException ex) {
			// expected: vetoed by the constraint of the model.
		}
		assertEquals(Set.of(_tenantA), Tenants.tenantsOf(_developerA));
	}

	public void testUniqueName() {
		try {
			inTransaction(() -> created(TestTenants.newTenant("Tenant A")));
			fail("The name of a tenant is unique.");
		} catch (TopLogicException ex) {
			// expected: vetoed by the constraint of the model.
		}
	}

	public void testScriptFunctions() throws ConfigurationException {
		QueryExecutor tenant = script("factoryCurrentTenant()");
		assertEquals(_tenantA, as(_developerA, () -> tenant.execute()));
		assertEquals(_tenantA, as(_readerA, () -> tenant.execute()));
		assertEquals(_tenantB, as(_developerB, () -> tenant.execute()));
		assertNull("The operator is a member of no tenant.", as(_operator, () -> tenant.execute()));
		assertNull(as(_noTenant, () -> tenant.execute()));

		QueryExecutor projects = script("factoryProjects()");
		assertEquals(List.of(_projectA), list(as(_readerA, () -> projects.execute())));
		assertEquals(List.of(_projectB), list(as(_developerB, () -> projects.execute())));
		assertEquals(List.of(), list(as(_noTenant, () -> projects.execute())));
		List<?> all = list(as(_operator, () -> projects.execute()));
		assertTrue("The operator sees the projects of all tenants.",
			all.contains(_projectA) && all.contains(_projectB));
	}

	public void testMembersSeeEachOther() {
		for (Person member : List.of(_adminA, _developerA, _readerA)) {
			assertTrue(member.getName(), allowed(member, _developerA, READ));
			assertTrue(member.getName(), allowed(member, _adminA, READ));
		}
		assertTrue(allowed(_operator, _developerA, READ));
		assertFalse("The accounts of another tenant are invisible.", allowed(_developerB, _developerA, READ));
		assertFalse(allowed(_developerB, _adminA, READ));
		assertFalse(allowed(_noTenant, _developerA, READ));
		assertFalse("A member reads the accounts of the tenant only.", allowed(_adminA, _developerA, WRITE));
	}

	public void testCreateMember() {
		Person member = (Person) as(_adminA,
			() -> TenantMembers.createMember(_tenantA, TenantRole.DEVELOPER, newAccount("tenantNewMember")));
		assertEquals(member, Person.byName("tenantNewMember"));
		assertEquals(Set.of(_tenantA), Tenants.tenantsOf(member));
		assertEquals(TenantRole.DEVELOPER, TenantMembers.roleOf(_tenantA, member));
		assertTrue(TenantMembers.members(_tenantA).contains(member));
		assertTrue("The member logs in with the initial password.", member.getAuthenticationDevice()
			.authentify(LoginCredentials.fromUserAndPassword(member, PASSWORD.toCharArray())));
		assertFalse(member.getAuthenticationDevice()
			.authentify(LoginCredentials.fromUserAndPassword(member, "wrong".toCharArray())));
		assertTrue("The new developer works on the projects of the tenant.", allowed(member, _projectA, WRITE));
		assertFalse(allowed(member, _projectB, READ));
		assertTrue("The administrator sees the new member.", allowed(_adminA, member, READ));
		assertFalse(allowed(_developerB, member, READ));
	}

	public void testOperatorCreatesMember() {
		Person member = (Person) as(_operator,
			() -> TenantMembers.createMember(_tenantB, TenantRole.ADMINISTRATOR, newAccount("tenantNewAdminB")));
		assertEquals(TenantRole.ADMINISTRATOR, TenantMembers.roleOf(_tenantB, member));
		assertTrue(allowed(member, _tenantB, WRITE));
	}

	public void testManageOtherTenantRefused() {
		assertNoWriteAccess(_tenantB,
			() -> TenantMembers.createMember(_tenantB, TenantRole.DEVELOPER, newAccount("tenantIntruder")));
		assertNull("No account was created.", Person.byName("tenantIntruder"));
		assertNoWriteAccess(_tenantB, () -> {
			TenantMembers.changeRole(_tenantB, _developerB, TenantRole.READER);
			return null;
		});
		assertNoWriteAccess(_tenantB, () -> {
			TenantMembers.removeMember(_tenantB, _developerB);
			return null;
		});
		assertEquals(TenantRole.DEVELOPER, TenantMembers.roleOf(_tenantB, _developerB));
		assertTrue(_developerB.tValid());
	}

	public void testDeveloperCannotManageMembers() {
		for (Person person : List.of(_developerA, _readerA)) {
			try {
				as(person, () -> TenantMembers.createMember(_tenantA, TenantRole.READER, newAccount("tenantNotCreated")));
				fail(person.getName() + " may not create members.");
			} catch (TopLogicException ex) {
				assertEquals(com.top_logic.factory.project.I18NConstants.ERROR_NO_WRITE_ACCESS__OBJECT.fill(_tenantA),
					ex.getErrorKey());
			}
			try {
				as(person, () -> {
					TenantMembers.changeRole(_tenantA, _readerA, TenantRole.ADMINISTRATOR);
					return null;
				});
				fail(person.getName() + " may not change roles.");
			} catch (TopLogicException ex) {
				assertEquals(com.top_logic.factory.project.I18NConstants.ERROR_NO_WRITE_ACCESS__OBJECT.fill(_tenantA),
					ex.getErrorKey());
			}
		}
		assertNull(Person.byName("tenantNotCreated"));
		assertEquals(TenantRole.READER, TenantMembers.roleOf(_tenantA, _readerA));
	}

	public void testLoginInUse() {
		try {
			as(_adminA, () -> TenantMembers.createMember(_tenantA, TenantRole.READER, newAccount("tenantDeveloperB")));
			fail("The login name is used by an account of another tenant.");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.knowledge.wrap.person.I18NConstants.ERROR_DUPLICATE_ACCOUNT_NAME__NAME
				.fill("tenantDeveloperB"), ex.getErrorKey());
		}
		assertEquals(Set.of(_tenantB), Tenants.tenantsOf(_developerB));
	}

	public void testChangeRole() {
		as(_adminA, () -> {
			TenantMembers.changeRole(_tenantA, _developerA, TenantRole.READER);
			return null;
		});
		assertEquals(TenantRole.READER, TenantMembers.roleOf(_tenantA, _developerA));
		assertFalse("A reader only reads.", allowed(_developerA, _projectA, WRITE));
		assertTrue(allowed(_developerA, _projectA, READ));

		try {
			as(_adminA, () -> {
				TenantMembers.changeRole(_tenantA, _adminA, TenantRole.DEVELOPER);
				return null;
			});
			fail("The last administrator keeps the role.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_LAST_ADMINISTRATOR__TENANT.fill(_tenantA), ex.getErrorKey());
		}
		try {
			as(_adminA, () -> {
				TenantMembers.changeRole(_tenantA, _developerB, TenantRole.READER);
				return null;
			});
			fail("A member of another tenant is not changed.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_NOT_A_MEMBER__USER_TENANT.fill(_developerB, _tenantA), ex.getErrorKey());
		}
	}

	public void testRemoveMember() {
		Person member = (Person) as(_adminA,
			() -> TenantMembers.createMember(_tenantA, TenantRole.DEVELOPER, newAccount("tenantRemoved")));
		assertTrue(allowed(member, _projectA, WRITE));
		as(_adminA, () -> {
			TenantMembers.removeMember(_tenantA, member);
			return null;
		});
		assertFalse(TenantMembers.members(_tenantA).contains(member));
		assertNull("The account of the member is deleted.", Person.byName("tenantRemoved"));
		assertNull(Group.getGroupByName(kb(), "tenantRemoved"));

		try {
			as(_adminA, () -> {
				TenantMembers.removeMember(_tenantA, _adminA);
				return null;
			});
			fail("Nobody removes himself.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_REMOVE_SELF, ex.getErrorKey());
		}
		try {
			as(_operator, () -> {
				TenantMembers.removeMember(_tenantA, _adminA);
				return null;
			});
			fail("The last administrator is not removed.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_LAST_ADMINISTRATOR__TENANT.fill(_tenantA), ex.getErrorKey());
		}
		assertTrue(_adminA.tValid());
	}

	public void testCreateTenant() {
		TLObject tenant = (TLObject) as(_operator, () -> TenantMembers.createTenant("Tenant C", newAccount("tenantAdminC")));
		_created.add(tenant);
		Person administrator = Person.byName("tenantAdminC");
		assertEquals(TenantRole.ADMINISTRATOR, TenantMembers.roleOf(tenant, administrator));
		assertTrue(allowed(administrator, tenant, WRITE));
		assertFalse(allowed(administrator, _tenantA, READ));

		try {
			as(_adminA, () -> TenantMembers.createTenant("Tenant D", newAccount("tenantAdminD")));
			fail("Only an operator creates a tenant.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_NO_TENANT_CREATION, ex.getErrorKey());
		}
		assertNull(Person.byName("tenantAdminD"));
	}

	public void testMemberScriptFunctions() throws ConfigurationException {
		QueryExecutor members = script("t -> factoryMembers($t)");
		assertEquals(Set.of(_adminA, _developerA, _readerA),
			new HashSet<>(list(as(_readerA, () -> members.execute(_tenantA)))));
		assertEquals("A script sees only the accounts the user may read.", List.of(),
			list(as(_developerB, () -> members.execute(_tenantA))));
		QueryExecutor role = script("t -> p -> factoryMemberRole($t, $p)");
		assertEquals(TenantRole.READER.classifier(), as(_adminA, () -> role.execute(_tenantA, _readerA)));
		assertNull(as(_operator, () -> role.execute(_tenantA, _developerB)));

		QueryExecutor create = script(
			"t -> factoryCreateMember($t, `tl.factory:TenantRole#READER`, 'tenantScripted', 'Ada', 'Lovelace', 'ada@example.com', '"
				+ PASSWORD + "')");
		Person member = (Person) as(_adminA, () -> create.execute(_tenantA));
		_logins.add("tenantScripted");
		assertEquals(TenantRole.READER, TenantMembers.roleOf(_tenantA, member));
	}

	private void assertNoWriteAccess(TLObject tenant, Supplier<Object> job) {
		try {
			as(_adminA, job);
			fail("The administrator of tenant A may not manage the members of " + tenant + ".");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.factory.project.I18NConstants.ERROR_NO_WRITE_ACCESS__OBJECT.fill(tenant),
				ex.getErrorKey());
		}
	}

	private NewAccount newAccount(String login) {
		_logins.add(login);
		return new NewAccount(login, "First", "Last", login + "@example.com", PASSWORD);
	}

	public void testCoverage() {
		Set<TLClass> analysed = new HashSet<>();
		for (TypeCoverage coverage : SecurityCoverageAnalysis.newInstance(Set.of()).analyze()) {
			if (!FactoryModel.MODULE.equals(coverage.type().getModule().getName())) {
				continue;
			}
			analysed.add(coverage.type());
			List<CoverageFinding> findings = coverage.findings();
			assertTrue(TLModelUtil.qualifiedName(coverage.type()) + ": "
				+ findings.stream().map(f -> f + " " + f.getOperation() + " " + f.getRoles()).toList(),
				findings.isEmpty());
		}
		assertEquals(Set.of(FactoryModel.tenantType(), FactoryModel.projectType(), FactoryModel.workspaceType(),
			FactoryModel.conversationType(), FactoryModel.gitConnectionType()), analysed);
	}

	/**
	 * The security root, on which the operators hold their role.
	 */
	private static TLObject securityRoot() {
		return TLModelUtil.findModule(SECURITY_STRUCTURE).getSingleton(TLModule.DEFAULT_SINGLETON_NAME);
	}

	private static List<?> list(Object value) {
		return value == null ? List.of() : new ArrayList<>((Collection<?>) value);
	}

	private static QueryExecutor script(String expr) throws ConfigurationException {
		return QueryExecutor.compile(ExprFormat.INSTANCE.getValue("expr", expr));
	}

	private static Object as(Person person, Supplier<Object> job) {
		return TLContextManager.<Object, RuntimeException, RuntimeException> inPersonContext(person, job::get);
	}

	private static ConstraintCheck constraint(String members) {
		TLConstraints annotation = part(FactoryModel.TENANT, members).getAnnotation(TLConstraints.class);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(annotation.getConstraints().get(0));
	}

	private static TLStructuredTypePart part(String type, String name) {
		return ((TLClass) TLModelUtil.findType(FactoryModel.MODULE, type)).getPartOrFail(name);
	}

	private static TLObject project(TLObject tenant, String artifactId) {
		TLObject result = TestTenants.newProject(tenant);
		result.tUpdateByName(FactoryModel.PROJECT_NAME, artifactId);
		result.tUpdateByName(FactoryModel.PROJECT_GROUP_ID, "com.example");
		result.tUpdateByName(FactoryModel.PROJECT_ARTIFACT_ID, artifactId);
		result.tUpdateByName(FactoryModel.PROJECT_ENGINE_VERSION, "8.0.0");
		return result;
	}

	private TLObject created(TLObject object) {
		_created.add(object);
		return object;
	}

	private Person createPerson(String name) {
		Person result = Person.create(kb(), name, PersonManager.getManager().getRoot().getAuthenticationDevice());
		_persons.add(result);
		return result;
	}

	private static boolean allowed(Person person, TLObject object, BoundCommandGroup operation) {
		return rights().isAllowed(person, object, operation);
	}

	private static ModelAccessRights rights() {
		return ModelAccessRights.getInstance();
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static void inTransaction(Runnable modification) {
		ThreadContext.withoutSecurityChecks(() -> {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				modification.run();
				tx.commit();
			}
		});
	}

	/**
	 * The tests of this class, with a knowledge base holding the model of the control application
	 * and the access services with the configuration of the module.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestTenantAccess.class),
				SearchBuilder.Module.INSTANCE,
				ModelService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				InitialRolesManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE,
				// The default engine version of a project; starting it contacts no container platform.
				WorkspaceService.Module.INSTANCE));
	}

}
