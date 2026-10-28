/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.git;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.tenant.TestTenants;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.git.GitCapabilities;
import com.top_logic.factory.git.GitConnections;
import com.top_logic.factory.git.GitConnections.OwnConnection;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitHostingService;
import com.top_logic.factory.git.GitRoute;
import com.top_logic.factory.git.I18NConstants;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.project.FactoryFunctions;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.WorkspaceStatus;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test of the git connections of the control application ({@link GitConnections}) with the
 * {@link RecordingGitProviderType} and the built-in connection of the test configuration: the
 * built-in connection offered to every tenant with an organization per tenant, the own connections
 * of a tenant with their access token in the writable secret store, who may manage and read them,
 * the deletion refused while projects live on a connection, and the git route resolving the
 * provider of the connection of the project of a workspace.
 *
 * <p>
 * Tenant A has an administrator, a developer and a reader; tenant B an administrator and a
 * developer. The operator holds the operator role on the security root.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestGitConnections extends BasicTestCase {

	private static final String BUILT_IN = "test-builtin";

	private static final String BUILT_IN_URL = "https://git.test/";

	private static final String BUILT_IN_OWNER = "factory";

	private static final String OWN_URL = "https://own-git.test/";

	private static final String OWN_OWNER = "acme";

	private static final String TOKEN = "own-access-token";

	private static final String OPERATOR_ROLE = "tl.factory.Operator";

	private static final BoundCommandGroup READ = SimpleBoundCommandGroup.READ;

	private static final BoundCommandGroup WRITE = SimpleBoundCommandGroup.WRITE;

	private final List<TLObject> _created = new ArrayList<>();

	private final List<Person> _persons = new ArrayList<>();

	private Person _adminA;

	private Person _developerA;

	private Person _readerA;

	private Person _adminB;

	private Person _developerB;

	private Person _operator;

	private TLObject _tenantA;

	private TLObject _tenantB;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RecordingGitProviderType.reset();
		inTransaction(() -> {
			_adminA = createPerson("gitAdminA");
			_developerA = createPerson("gitDeveloperA");
			_readerA = createPerson("gitReaderA");
			_adminB = createPerson("gitAdminB");
			_developerB = createPerson("gitDeveloperB");
			_operator = createPerson("gitOperator");

			_tenantA = created(TestTenants.newTenant("Git tenant A"));
			_tenantA.tUpdateByName(FactoryModel.TENANT_ADMINISTRATORS, Set.of(_adminA));
			_tenantA.tUpdateByName(FactoryModel.TENANT_DEVELOPERS, Set.of(_developerA));
			_tenantA.tUpdateByName(FactoryModel.TENANT_READERS, Set.of(_readerA));

			_tenantB = created(TestTenants.newTenant("Git tenant B"));
			_tenantB.tUpdateByName(FactoryModel.TENANT_ADMINISTRATORS, Set.of(_adminB));
			_tenantB.tUpdateByName(FactoryModel.TENANT_DEVELOPERS, Set.of(_developerB));
		});
		inTransaction(() -> BoundedRole.assignRole(securityRoot(), _operator, BoundedRole.getRoleByName(OPERATOR_ROLE)));
		// The tenants are created after the startup: offer them the built-in connection.
		GitConnections.synchronizeBuiltIn();
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
		super.tearDown();
	}

	/**
	 * Runs the test body with checked security, see {@link TLContextManager#inPersonContext}.
	 */
	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(null, () -> {
			super.runTest();
			return null;
		});
	}

	public void testBuiltIn() {
		TLObject builtIn = builtIn();
		assertTrue(GitConnections.isBuiltIn(builtIn));
		assertEquals(RecordingGitProviderType.NAME,
			builtIn.tValueByName(FactoryModel.GIT_CONNECTION_PROVIDER_TYPE));
		assertEquals(BUILT_IN_URL, builtIn.tValueByName(FactoryModel.GIT_CONNECTION_URL));
		assertEquals(BUILT_IN_OWNER, builtIn.tValueByName(FactoryModel.GIT_CONNECTION_OWNER));
		assertNull("No secret of its own.", builtIn.tValueByName(FactoryModel.GIT_CONNECTION_SECRET_NAME));
		assertEquals(List.of(builtIn), GitConnections.choices(_tenantA));
		assertEquals(List.of(builtIn), GitConnections.choices(null));
		assertEquals(builtIn, GitConnections.defaultChoice(_tenantB));

		for (Person member : List.of(_adminA, _developerA, _readerA, _adminB, _developerB, _operator)) {
			assertTrue(member.getName() + " reads the built-in connection.", allowed(member, builtIn, READ));
		}
		assertFalse("Only the configuration changes it.", allowed(_adminA, builtIn, WRITE));
		assertTrue(allowed(_operator, builtIn, WRITE));

		assertManageRefused(I18NConstants.ERROR_BUILT_IN_CONNECTION__CONNECTION.fill(BUILT_IN), _operator,
			() -> GitConnections.update(builtIn, new OwnConnection("x", RecordingGitProviderType.NAME, OWN_URL, "x")));
		assertManageRefused(I18NConstants.ERROR_BUILT_IN_CONNECTION__CONNECTION.fill(BUILT_IN), _operator,
			() -> GitConnections.delete(builtIn));
	}

	public void testOrganizationPerTenant() {
		TLObject builtIn = builtIn();
		String ownerA = GitConnections.ownerFor(builtIn, _tenantA);
		String ownerB = GitConnections.ownerFor(builtIn, _tenantB);
		assertTrue(ownerA, ownerA.startsWith(BUILT_IN_OWNER + GitConnections.OWNER_SEPARATOR));
		assertFalse("Each tenant has an organization of its own.", ownerA.equals(ownerB));
		assertTrue(ownerA, ownerA.matches("[a-z0-9-]+") && ownerA.length() <= GitConnections.MAX_OWNER_LENGTH);
		assertEquals("Stable for the tenant.", ownerA, GitConnections.tenantOwner(BUILT_IN_OWNER, _tenantA));
		assertTrue(GitConnections.tenantOwner("a-very-long-start-of-the-organization-names", _tenantA)
			.length() <= GitConnections.MAX_OWNER_LENGTH);

		GitHostingProvider providerA = GitConnections.provider(builtIn, _tenantA);
		assertEquals(ownerA, providerA.getOwner());
		assertSame("The provider is kept.", providerA, GitConnections.provider(builtIn, _tenantA));
		assertEquals(ownerB, GitConnections.provider(builtIn, _tenantB).getOwner());
		RecordingGitProviderType.Created created = RecordingGitProviderType.created(BUILT_IN_URL, ownerA);
		assertTrue("The provider creates the organization of the tenant.", created.settings().createOwner());
		assertNull("No organization without tenant.", GitConnections.provider(builtIn, null));
	}

	public void testCreateOwnConnection() {
		TLObject connection = createOwn(_adminA, "Own A");
		String secretName = (String) connection.tValueByName(FactoryModel.GIT_CONNECTION_SECRET_NAME);
		assertTrue(secretName, secretName.startsWith(GitConnections.SECRET_PREFIX));
		assertEquals("The token is in the writable secret store.", TOKEN, SecretStoreService.secret(secretName));
		assertFalse("The token is in the model nowhere.", modelValues(connection).contains(TOKEN));
		assertEquals(_tenantA, GitConnections.tenantOf(connection));
		assertFalse(GitConnections.isBuiltIn(connection));

		assertEquals(List.of(builtIn(), connection), GitConnections.choices(_tenantA));
		assertEquals("Not offered to another tenant.", List.of(builtIn()), GitConnections.choices(_tenantB));
		assertEquals(List.of(connection), GitConnections.ownConnections(_tenantA));

		GitHostingProvider provider = GitConnections.provider(connection, _tenantA);
		assertEquals(OWN_OWNER, provider.getOwner());
		assertEquals(OWN_OWNER, GitConnections.ownerFor(connection, _tenantB));
		RecordingGitProviderType.Created created = RecordingGitProviderType.created(OWN_URL, OWN_OWNER);
		assertFalse("The owner of an own connection exists.", created.settings().createOwner());
		assertEquals("The provider sends the token.", TOKEN, sentToken(provider));
	}

	public void testCreateRefused() {
		for (Person person : List.of(_developerA, _readerA, _adminB, _developerB)) {
			assertManageRefused(
				com.top_logic.factory.project.I18NConstants.ERROR_NO_WRITE_ACCESS__OBJECT.fill(_tenantA), person,
				() -> GitConnections.create(_tenantA, own("Intruder"), TOKEN));
		}
		assertEquals(List.of(), GitConnections.ownConnections(_tenantA));
		assertManageRefused(I18NConstants.ERROR_UNKNOWN_PROVIDER_TYPE__TYPE.fill("missing"), _adminA,
			() -> GitConnections.create(_tenantA, new OwnConnection("Bad", "missing", OWN_URL, OWN_OWNER), TOKEN));
		assertManageRefused(I18NConstants.ERROR_MISSING_TOKEN, _adminA,
			() -> GitConnections.create(_tenantA, own("No token"), " "));
		assertManageRefused(I18NConstants.ERROR_MISSING_CONNECTION_URL, _adminA,
			() -> GitConnections.create(_tenantA,
				new OwnConnection("No URL", RecordingGitProviderType.NAME, "", OWN_OWNER), TOKEN));

		TLObject byOperator = createOwn(_operator, "By the operator");
		assertEquals(_tenantA, GitConnections.tenantOf(byOperator));
	}

	public void testReadOwnConnection() {
		TLObject connection = createOwn(_adminA, "Own A");
		for (Person member : List.of(_adminA, _developerA, _readerA, _operator)) {
			assertTrue(member.getName(), allowed(member, connection, READ));
		}
		for (Person other : List.of(_adminB, _developerB)) {
			assertFalse(other.getName() + " does not see a connection of another tenant.",
				allowed(other, connection, READ));
		}
		assertFalse("Changed through the functions only.", allowed(_adminA, connection, WRITE));
	}

	public void testManageOtherTenantRefused() {
		TLObject connection = createOwn(_adminA, "Own A");
		ResKey refused = com.top_logic.factory.project.I18NConstants.ERROR_NO_WRITE_ACCESS__OBJECT.fill(_tenantA);
		for (Person person : List.of(_adminB, _developerA)) {
			assertManageRefused(refused, person,
				() -> GitConnections.update(connection, own("Renamed")));
			assertManageRefused(refused, person, () -> GitConnections.replaceToken(connection, "stolen"));
			assertManageRefused(refused, person, () -> GitConnections.delete(connection));
		}
		assertTrue(connection.tValid());
		assertEquals("Own A", GitConnections.nameOf(connection));
		assertEquals(TOKEN, SecretStoreService.secret(secretName(connection)));
	}

	public void testReplaceToken() {
		TLObject connection = createOwn(_adminA, "Own A");
		String before = secretName(connection);
		GitHostingProvider old = GitConnections.provider(connection, _tenantA);

		as(_adminA, () -> GitConnections.replaceToken(connection, "replaced-token"));
		String after = secretName(connection);
		assertFalse("A new secret.", before.equals(after));
		assertEquals("replaced-token", SecretStoreService.secret(after));
		assertFalse("The old secret is removed.", MemorySecretStore.names().contains(before));
		GitHostingProvider provider = GitConnections.provider(connection, _tenantA);
		assertNotSame("A new provider for the new token.", old, provider);
		assertEquals("replaced-token", sentToken(provider));
	}

	public void testUpdate() {
		TLObject connection = createOwn(_adminA, "Own A");
		GitHostingProvider old = GitConnections.provider(connection, _tenantA);
		as(_adminA, () -> GitConnections.update(connection,
			new OwnConnection("Moved", RecordingGitProviderType.NAME, OWN_URL, "other-owner")));
		assertEquals("Moved", GitConnections.nameOf(connection));
		GitHostingProvider provider = GitConnections.provider(connection, _tenantA);
		assertNotSame(old, provider);
		assertEquals("other-owner", provider.getOwner());
	}

	public void testDeleteRefusedWhileUsed() {
		TLObject connection = createOwn(_adminA, "Own A");
		String secretName = secretName(connection);
		TLObject project = inTransaction(() -> created(project(_tenantA, "on-own", connection)));

		assertManageRefused(I18NConstants.ERROR_CONNECTION_IN_USE__CONNECTION_PROJECTS.fill("Own A", "on-own"),
			_adminA, () -> GitConnections.delete(connection));
		assertManageRefused(I18NConstants.ERROR_CONNECTION_SERVER_IN_USE__CONNECTION_PROJECTS.fill("Own A", "on-own"),
			_adminA, () -> GitConnections.update(connection,
				new OwnConnection("Own A", RecordingGitProviderType.NAME, "https://elsewhere.test/", OWN_OWNER)));
		as(_adminA, () -> GitConnections.update(connection, own("Renamed")));
		assertEquals("The name changes anyway.", "Renamed", GitConnections.nameOf(connection));
		assertTrue(connection.tValid());
		assertEquals(TOKEN, SecretStoreService.secret(secretName));

		inTransaction(() -> project.tDelete());
		as(_adminA, () -> GitConnections.delete(connection));
		assertFalse(connection.tValid());
		assertFalse("The secret is removed with the connection.", MemorySecretStore.names().contains(secretName));
		assertEquals(List.of(builtIn()), GitConnections.choices(_tenantA));
	}

	public void testRouteResolution() {
		TLObject connection = createOwn(_adminA, "Own A");
		TLObject builtIn = builtIn();
		Map<String, TLObject> workspaces = new HashMap<>();
		inTransaction(() -> {
			TLObject onOwn = created(project(_tenantA, "route-own", connection));
			onOwn.tUpdateByName(FactoryModel.PROJECT_REPOSITORY, OWN_OWNER + "/route-own");
			TLObject onBuiltIn = created(project(_tenantB, "route-builtin", builtIn));
			onBuiltIn.tUpdateByName(FactoryModel.PROJECT_REPOSITORY,
				GitConnections.ownerFor(builtIn, _tenantB) + "/route-builtin");
			TLObject withoutConnection = created(project(_tenantA, "route-none", null));
			withoutConnection.tUpdateByName(FactoryModel.PROJECT_REPOSITORY, OWN_OWNER + "/route-none");
			TLObject unavailable = created(project(_tenantA, "route-unavailable", connection));
			unavailable.tUpdateByName(FactoryModel.PROJECT_REPOSITORY, OWN_OWNER + "/route-unavailable");
			workspaces.put("own", FactoryModel.createWorkspace(onOwn, "route-own-ws", "pw", WorkspaceStatus.RUNNING));
			workspaces.put("builtin",
				FactoryModel.createWorkspace(onBuiltIn, "route-builtin-ws", "pw", WorkspaceStatus.RUNNING));
			workspaces.put("none",
				FactoryModel.createWorkspace(withoutConnection, "route-none-ws", "pw", WorkspaceStatus.RUNNING));
			return null;
		});

		GitRoute.Target own = GitRoute.workspaceTarget("route-own-ws");
		assertSame(GitConnections.provider(connection, _tenantA), own.provider());
		assertEquals(new RepositoryName(OWN_OWNER, "route-own"), own.repository());
		assertEquals(OWN_URL + OWN_OWNER + "/route-own.git", own.provider().gitUrl(own.repository()));

		GitRoute.Target onBuiltIn = GitRoute.workspaceTarget("route-builtin-ws");
		assertSame(GitConnections.provider(builtIn, _tenantB), onBuiltIn.provider());
		assertEquals(GitConnections.ownerFor(builtIn, _tenantB), onBuiltIn.provider().getOwner());
		assertEquals(BUILT_IN_URL, ((RecordingGitHostingProvider) onBuiltIn.provider()).getGitBaseUrl());

		assertNull("No connection, no access.", GitRoute.workspaceTarget("route-none-ws"));
		assertNull("Unknown workspace.", GitRoute.workspaceTarget("route-unknown-ws"));

		// A connection of a type that is no longer registered.
		inTransaction(() -> {
			connection.tUpdateByName(FactoryModel.GIT_CONNECTION_PROVIDER_TYPE, "removed-type");
			return null;
		});
		assertFalse(GitConnections.isAvailable(connection));
		assertNull("Unavailable connection, no access.", GitRoute.workspaceTarget("route-own-ws"));
		assertEquals(List.of(builtIn), GitConnections.choices(_tenantA));
	}

	public void testCapabilities() {
		TLObject connection = createOwn(_adminA, "Own A");
		RecordingGitHostingProvider provider =
			(RecordingGitHostingProvider) GitConnections.provider(connection, _tenantA);
		assertEquals(GitCapabilities.ALL, GitConnections.capabilities(connection, _tenantA));
		provider.setCapabilities(new GitCapabilities(true, false, null));
		Map<String, Object> shown = as(_adminA, () -> FactoryFunctions.gitCapabilities(connection, null));
		assertEquals(Boolean.TRUE, shown.get(FactoryFunctions.GIT_CREATE_REPOSITORIES));
		assertEquals(Boolean.FALSE, shown.get(FactoryFunctions.GIT_PROTECT_BRANCHES));
		assertEquals(OWN_OWNER, shown.get(FactoryFunctions.GIT_OWNER));
		assertNull(shown.get(FactoryFunctions.GIT_PROBLEM));

		Map<String, Object> builtIn = as(_developerB, () -> FactoryFunctions.gitCapabilities(builtIn(), null));
		assertEquals("The organization of the tenant of the user.", GitConnections.ownerFor(builtIn(), _tenantB),
			builtIn.get(FactoryFunctions.GIT_OWNER));
	}

	public void testProjectConnectionCheck() {
		TLObject connection = createOwn(_adminA, "Own A");
		TLObject draft = as(_developerA, () -> TransientObjectFactory.INSTANCE.createObject(FactoryModel.projectType()));
		assertEquals("The built-in connection is the default.", builtIn(), FactoryModel.getGitConnection(draft));
		draft.tUpdateByName(FactoryModel.PROJECT_TENANT, _tenantA);
		assertNull(GitConnections.checkProjectConnection(draft, connection));
		assertNull(GitConnections.checkProjectConnection(draft, builtIn()));
		assertEquals(I18NConstants.ERROR_NO_GIT_CONNECTION, GitConnections.checkProjectConnection(draft, null));

		draft.tUpdateByName(FactoryModel.PROJECT_TENANT, _tenantB);
		assertEquals("Not a connection of the tenant of the project.",
			I18NConstants.ERROR_GIT_CONNECTION_NOT_AVAILABLE__CONNECTION.fill(connection),
			GitConnections.checkProjectConnection(draft, connection));

		TLObject stored = inTransaction(() -> created(project(_tenantB, "stored", null)));
		assertNull("A stored project is not checked again.", GitConnections.checkProjectConnection(stored, null));
	}

	private TLObject builtIn() {
		for (TLObject connection : GitConnections.choices(null)) {
			if (BUILT_IN.equals(GitConnections.nameOf(connection))) {
				return connection;
			}
		}
		throw new AssertionError("No built-in connection " + BUILT_IN);
	}

	private TLObject createOwn(Person person, String name) {
		TLObject connection = as(person, () -> GitConnections.create(_tenantA, own(name), TOKEN));
		_created.add(connection);
		return connection;
	}

	private static OwnConnection own(String name) {
		return new OwnConnection(name, RecordingGitProviderType.NAME, OWN_URL, OWN_OWNER);
	}

	private static String secretName(TLObject connection) {
		return (String) connection.tValueByName(FactoryModel.GIT_CONNECTION_SECRET_NAME);
	}

	/**
	 * The token the credentials of the given provider send.
	 */
	private static String sentToken(GitHostingProvider provider) {
		Map<String, String> headers = new HashMap<>();
		provider.gitCredentials().authenticate(headers::put);
		return headers.get(RecordingGitProviderType.TOKEN_HEADER);
	}

	private static List<Object> modelValues(TLObject object) {
		List<Object> result = new ArrayList<>();
		for (var part : object.tType().getAllParts()) {
			Object value = object.tValue(part);
			if (value instanceof Collection<?> collection) {
				result.addAll(collection);
			} else {
				result.add(value);
			}
		}
		return result;
	}

	private void assertManageRefused(ResKey expected, Person person, Runnable job) {
		try {
			as(person, () -> {
				job.run();
				return null;
			});
			fail(person.getName() + ": expected " + expected);
		} catch (TopLogicException ex) {
			assertEquals(expected, ex.getErrorKey());
		}
	}

	private static TLObject project(TLObject tenant, String artifactId, TLObject connection) {
		TLObject result = TestTenants.newProject(tenant);
		result.tUpdateByName(FactoryModel.PROJECT_NAME, artifactId);
		result.tUpdateByName(FactoryModel.PROJECT_GROUP_ID, "com.example");
		result.tUpdateByName(FactoryModel.PROJECT_ARTIFACT_ID, artifactId);
		result.tUpdateByName(FactoryModel.PROJECT_ENGINE_VERSION, "8.0.0");
		result.tUpdateByName(FactoryModel.PROJECT_GIT_CONNECTION, connection);
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

	private static TLObject securityRoot() {
		return com.top_logic.model.util.TLModelUtil.findModule("SecurityStructure")
			.getSingleton(com.top_logic.model.TLModule.DEFAULT_SINGLETON_NAME);
	}

	private static boolean allowed(Person person, TLObject object, BoundCommandGroup operation) {
		return ModelAccessRights.getInstance().isAllowed(person, object, operation);
	}

	private static <T> T as(Person person, Supplier<T> job) {
		return TLContextManager.<T, RuntimeException, RuntimeException> inPersonContext(person, job::get);
	}

	private static void as(Person person, Runnable job) {
		as(person, () -> {
			job.run();
			return null;
		});
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static void inTransaction(Runnable modification) {
		inTransaction(() -> {
			modification.run();
			return null;
		});
	}

	private static <T> T inTransaction(Supplier<T> modification) {
		return ThreadContext.<T, RuntimeException, RuntimeException> withoutSecurityChecks(() -> {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				T result = modification.get();
				tx.commit();
				return result;
			}
		});
	}

	/**
	 * The tests of this class, with a knowledge base holding the model of the control application,
	 * the access services and the git hosting service of the test configuration.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestGitConnections.class),
				SearchBuilder.Module.INSTANCE,
				ModelService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				InitialRolesManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE,
				SecretStoreService.Module.INSTANCE,
				GitHostingService.Module.INSTANCE,
				// The default engine version of a project; starting it contacts no container platform.
				WorkspaceService.Module.INSTANCE));
	}

}
