/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.ApplicableOperations;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Test for the operations declared in the <code>&lt;security-config&gt;</code> of the
 * {@link SecurityConfigurationService}: which operations apply to a type or singleton, the
 * operations implied by others, the delegation of a custom operation to an access parent, and the
 * configuration errors.
 *
 * <p>
 * The declarations under test are those of the test application of
 * {@link TestTLScriptSecurity}: <code>TestTLScriptSecurity-test.config.xml</code> and the
 * application layer <code>TestTLScriptSecurity-overlay.config.xml</code>.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestOperationDeclarations extends AbstractSearchExpressionTest {

	private static final String MODULE = "TestTLScriptSecurity";

	private static final String READER_ROLE = MODULE + ".ProjectReader";

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	/** Holds ProjectResponsible on {@link #_project} and {@link #_special}. */
	private Person _responsible;

	/** Holds ProjectReader on {@link #_project} and {@link #_special}. */
	private Person _reader;

	/** Holds no role at all. */
	private Person _other;

	private TLObject _project;

	/** A SpecialProject, on which the read right of the reader is revoked. */
	private TLObject _special;

	/** A task delegating to {@link #_project}, the project its assignee is responsible for. */
	private TLObject _task;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		try (Transaction tx = beginTx()) {
			_responsible = TestPerson.createPerson("operationsResponsible");
			_reader = TestPerson.createPerson("operationsReader");
			_other = TestPerson.createPerson("operationsOther");

			TLObject responsible = newObject("Employee");
			responsible.tUpdateByName("account", _responsible);
			TLObject member = newObject("Employee");
			member.tUpdateByName("account", _reader);

			_project = newObject("Project");
			_project.tUpdateByName("name", "p");
			_project.tUpdateByName("responsible", responsible);
			_project.tUpdateByName("members", Collections.singletonList(member));

			// Another responsible employee, so that the task delegates to _project alone.
			TLObject specialResponsible = newObject("Employee");
			specialResponsible.tUpdateByName("account", _responsible);
			_special = newObject("SpecialProject");
			_special.tUpdateByName("name", "sp");
			_special.tUpdateByName("responsible", specialResponsible);
			_special.tUpdateByName("members", Collections.emptyList());
			_special.tUpdateByName("responsibleAccount", _responsible);
			_special.tUpdateByName("readerAccount", _reader);

			_task = newObject("ProjectTask");
			_task.tUpdateByName("name", "t");
			_task.tUpdateByName("assignee", responsible);
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = beginTx()) {
			// The singleton outlives the test, its values must not keep test objects.
			singleton("HOME").tUpdateByName("editors", Collections.emptyList());
			// Clearing the editors of the singleton already removed the link to the editor.
			for (KnowledgeItem item : _toDelete.values()) {
				if (item.isAlive()) {
					_kb.delete(item);
				}
			}
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	@Override
	protected boolean checksSecurity() {
		return true;
	}

	public void testBuiltInOperationsApplyToEveryType() {
		ApplicableOperations operations = rights().getOperations(type("PathA"));
		for (BoundCommandGroup builtIn : ApplicableOperations.BUILT_IN) {
			assertTrue(builtIn.getID(), operations.appliesTo(builtIn));
		}
		assertFalse(operations.appliesTo(group("Show")));
		assertFalse("A registered command group applies only where it is declared.",
			operations.appliesTo(group("Approve")));
	}

	public void testClassDeclarationAppliesToTypeAndSpecializations() {
		for (String name : new String[] { "Project", "SpecialProject" }) {
			TLClass type = type(name);
			assertTrue(name, rights().appliesTo(type, group("Show")));
			assertTrue(name, rights().appliesTo(type, group("Approve")));
			assertTrue("Declared in the application layer: " + name, rights().appliesTo(type, group("Inspect")));
		}
		assertFalse(rights().appliesTo(type("Employee"), group("Show")));
		assertFalse("The declaration of a singleton does not apply to its type.",
			rights().appliesTo(type("PathHome"), group("Review")));
	}

	public void testModuleDeclarationAppliesToEveryClassOfTheModule() {
		for (String name : new String[] { "Employee", "Project", "SpecialProject", "PathHome", "ProjectTask" }) {
			assertTrue(name, rights().appliesTo(type(name), group("Audit")));
		}
	}

	public void testSingletonDeclaration() {
		ApplicableOperations home = rights().getObjectOperations(singleton("HOME"));
		assertTrue(home.appliesTo(group("Review")));
		assertTrue("The operations of the type apply to the singleton.", home.appliesTo(group("Audit")));
		assertEquals(Set.of(SimpleBoundCommandGroup.WRITE), home.getImpliedBy(group("Review")));

		assertFalse(rights().getObjectOperations(singleton("OFFICE")).appliesTo(group("Review")));
		assertSame("An object that is no singleton has the operations of its type.",
			rights().getOperations(type("Project")), rights().getObjectOperations(_project));
	}

	public void testImpliedByDirect() {
		TLClass project = type("Project");
		assertEquals(Set.of(SimpleBoundCommandGroup.READ), rights().getOperations(project).getImpliedBy(group("Show")));
		assertEquals("Whoever is granted Read holds Show.",
			rights().getAllowedRoles(project, SimpleBoundCommandGroup.READ),
			rights().getAllowedRoles(project, group("Show")));
		assertTrue(rights().getAllowedRoles(project, group("Show")).contains(role(READER_ROLE)));

		assertTrue(rights().isAllowed(_reader, _project, group("Show")));
		assertFalse(rights().isAllowed(_reader, _project, group("Approve")));
		assertTrue(rights().isAllowed(_responsible, _project, group("Approve")));
		assertFalse(rights().isAllowed(_other, _project, group("Show")));
	}

	public void testImpliedByTransitive() {
		TLClass project = type("Project");
		ApplicableOperations operations = rights().getOperations(project);
		assertEquals(Set.of(group("Show")), operations.getImpliedBy(group("Inspect")));
		assertEquals(Set.of(group("Show"), SimpleBoundCommandGroup.READ), operations.getImplying(group("Inspect")));
		assertEquals(rights().getAllowedRoles(project, SimpleBoundCommandGroup.READ),
			rights().getAllowedRoles(project, group("Inspect")));

		assertTrue(rights().isAllowed(_reader, _project, group("Inspect")));
		assertFalse(rights().isAllowed(_other, _project, group("Inspect")));
	}

	public void testImplicationFollowsTheRightsOfTheSpecialization() {
		assertFalse("The read right of the reader is revoked on a special project.",
			rights().isAllowed(_reader, _special, SimpleBoundCommandGroup.READ));
		assertFalse(rights().isAllowed(_reader, _special, group("Show")));
		assertTrue(rights().isAllowed(_responsible, _special, group("Show")));
	}

	public void testAccessParentDelegatesCustomOperation() {
		assertTrue("Approve on the task is Approve on its project.",
			rights().isAllowed(_responsible, _task, group("Approve")));
		assertFalse(rights().isAllowed(_reader, _task, group("Approve")));

		assertTrue("Show on the task is Show on its project.", rights().isAllowed(_reader, _task, group("Show")));
		assertFalse(rights().isAllowed(_other, _task, group("Show")));
	}

	public void testAccessParentChecksImplyingOperation() {
		assertFalse("The project does not declare Review.", rights().appliesTo(type("Project"), group("Review")));
		assertTrue("Review on the task is implied by Write on the task, which is Write on its project.",
			rights().isAllowed(_responsible, _task, group("Review")));
		assertFalse(rights().isAllowed(_reader, _task, group("Review")));
	}

	public void testSingletonImpliedBy() throws Exception {
		TLObject home = singleton("HOME");
		assertFalse(rights().isAllowed(_other, home, group("Review")));
		try (Transaction tx = beginTx()) {
			TLObject editor = newObject("Employee");
			editor.tUpdateByName("account", _other);
			home.tUpdateByName("editors", Collections.singletonList(editor));
			tx.commit();
		}
		assertTrue(rights().isAllowed(_other, home, SimpleBoundCommandGroup.WRITE));
		assertTrue("Write on the home implies Review.", rights().isAllowed(_other, home, group("Review")));
		assertFalse(rights().isAllowed(_reader, home, group("Review")));
	}

	public void testValidDeclarations() throws Exception {
		assertNoErrors(
			config()
				.add("module", "<module name='TestTLScriptSecurity'>"
					+ "<operations><operation name='Audit'/></operations>"
					+ "<grant operation='Audit' roles='" + READER_ROLE + "'/></module>")
				.add("class", "<class name='TestTLScriptSecurity:Project'>"
					+ "<operations><operation name='Show' implied-by='Read'/>"
					+ "<operation name='Inspect' implied-by='Show'/></operations>"
					+ "<grant operation='Show' roles='" + READER_ROLE + "'/></class>")
				.add("class", "<class name='TestTLScriptSecurity:SpecialProject'>"
					+ "<grant operation='Inspect' roles='" + READER_ROLE + "'/></class>")
				.add("class", "<class name='TestTLScriptSecurity:Employee'>"
					+ "<grant operation='Audit' roles='" + READER_ROLE + "'/></class>")
				.add("part", "<part name='TestTLScriptSecurity:Project#secret'>"
					+ "<grant operation='Show' roles='" + READER_ROLE + "'/></part>")
				.add("singleton", "<singleton name='TestTLScriptSecurity#HOME'>"
					+ "<operations><operation name='Review' implied-by='Audit'/></operations>"
					+ "<grant operation='Review' roles='" + READER_ROLE + "'/></singleton>"));
	}

	public void testGrantOfUndeclaredOperation() throws Exception {
		assertError("does not apply",
			config().add("class", "<class name='TestTLScriptSecurity:Employee'>"
				+ "<grant operation='Approve' roles='" + READER_ROLE + "'/></class>"));
	}

	public void testRevokeOfUndeclaredOperation() throws Exception {
		assertError("does not apply",
			config().add("class", "<class name='TestTLScriptSecurity:Employee'>"
				+ "<revoke operation='Approve' roles='" + READER_ROLE + "'/></class>"));
	}

	public void testModuleGrantOfOperationDeclaredOnAClass() throws Exception {
		assertError("does not apply",
			config()
				.add("class", "<class name='TestTLScriptSecurity:Project'>"
					+ "<operations><operation name='Show'/></operations></class>")
				.add("module", "<module name='TestTLScriptSecurity'>"
					+ "<grant operation='Show' roles='" + READER_ROLE + "'/></module>"));
	}

	public void testPartGrantOfOperationNotApplyingToTheOwner() throws Exception {
		assertError("does not apply",
			config().add("part", "<part name='TestTLScriptSecurity:Employee#salary'>"
				+ "<grant operation='Show' roles='" + READER_ROLE + "'/></part>"));
	}

	public void testSingletonGrantOfUndeclaredOperation() throws Exception {
		assertError("does not apply",
			config().add("singleton", "<singleton name='TestTLScriptSecurity#OFFICE'>"
				+ "<grant operation='Review' roles='" + READER_ROLE + "'/></singleton>"));
	}

	public void testImpliedByNotApplying() throws Exception {
		assertError("does not apply to",
			config().add("class", "<class name='TestTLScriptSecurity:Employee'>"
				+ "<operations><operation name='Show' implied-by='Approve'/></operations></class>"));
	}

	public void testImpliedByDeclaredOnAClassOfAModule() throws Exception {
		assertError("does not apply to",
			config()
				.add("class", "<class name='TestTLScriptSecurity:Project'>"
					+ "<operations><operation name='Show'/></operations></class>")
				.add("module", "<module name='TestTLScriptSecurity'>"
					+ "<operations><operation name='Audit' implied-by='Show'/></operations></module>"));
	}

	public void testImplicationCycle() throws Exception {
		assertError("form a cycle",
			config().add("class", "<class name='TestTLScriptSecurity:Project'>"
				+ "<operations><operation name='Show' implied-by='Inspect'/>"
				+ "<operation name='Inspect' implied-by='Review'/>"
				+ "<operation name='Review' implied-by='Show'/></operations></class>"));
	}

	public void testImplicationCycleAcrossModuleAndClass() throws Exception {
		assertError("form a cycle",
			config()
				.add("module", "<module name='TestTLScriptSecurity'>"
					+ "<operations><operation name='Show'/><operation name='Inspect' implied-by='Show'/></operations></module>")
				.add("class", "<class name='TestTLScriptSecurity:Project'>"
					+ "<operations><operation name='Show' implied-by='Inspect'/></operations></class>"));
	}

	public void testBuiltInDeclaration() throws Exception {
		assertError("is built-in",
			config().add("class", "<class name='TestTLScriptSecurity:Project'>"
				+ "<operations><operation name='Read' implied-by='Write'/></operations></class>"));
	}

	public void testUnknownOperation() throws Exception {
		assertError("does not exist",
			config().add("class", "<class name='TestTLScriptSecurity:Project'>"
				+ "<operations><operation name='NoSuchOperation'/></operations></class>"));
		assertError("does not exist",
			config().add("class", "<class name='TestTLScriptSecurity:Project'>"
				+ "<operations><operation name='Show' implied-by='NoSuchOperation'/></operations></class>"));
	}

	private static void assertNoErrors(ConfigBuilder config) {
		BufferingProtocol log = config.instantiate();
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
	}

	private static void assertError(String expectedMessagePart, ConfigBuilder config) {
		BufferingProtocol log = config.instantiate();
		String errors = log.getErrors().toString();
		assertTrue("Expected an error containing '" + expectedMessagePart + "': " + errors,
			errors.contains(expectedMessagePart));
	}

	private static ConfigBuilder config() {
		return new ConfigBuilder();
	}

	/**
	 * Builds a configuration of the {@link SecurityConfigurationService} from XML entries and
	 * instantiates the service, collecting the configuration errors.
	 */
	private static class ConfigBuilder {

		private final SecurityConfigurationService.Config _config =
			TypedConfiguration.newConfigItem(SecurityConfigurationService.Config.class);

		ConfigBuilder add(String tag, String xml) throws ConfigurationException {
			Class<? extends SecurityConfigurationService.ModelAccessRights> type = switch (tag) {
				case "class" -> SecurityConfigurationService.TLClassAccessRights.class;
				case "module" -> SecurityConfigurationService.TLModuleAccessRights.class;
				case "singleton" -> SecurityConfigurationService.TLSingletonAccessRights.class;
				case "part" -> SecurityConfigurationService.TLPartAccessRights.class;
				default -> throw new IllegalArgumentException(tag);
			};
			SecurityConfigurationService.ModelAccessRights entry =
				TypedConfiguration.parse(tag, type, CharacterContents.newContent(xml));
			_config.getSecurityConfig().put(entry.getName(), entry);
			return this;
		}

		BufferingProtocol instantiate() {
			BufferingProtocol log = new BufferingProtocol();
			new SecurityConfigurationService(new DefaultInstantiationContext(log), _config);
			return log;
		}
	}

	private Transaction beginTx() {
		return _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	private static TLObject newObject(String className) {
		TLClass type = type(className);
		return DynamicModelService.getFactoryFor(MODULE).createObject(type);
	}

	private static TLClass type(String className) {
		return (TLClass) TLModelUtil.findType(MODULE + ":" + className);
	}

	private static TLObject singleton(String name) {
		return type("PathHome").getModule().getSingleton(name);
	}

	private static BoundCommandGroup group(String name) {
		BoundCommandGroup result = CommandGroupRegistry.resolve(name);
		assertNotNull(name, result);
		return result;
	}

	private static BoundedRole role(String name) {
		return BoundedRole.getRoleByName(PersistencyLayer.getKnowledgeBase(), name);
	}

	private static ModelAccessRights rights() {
		return ModelAccessRights.getInstance();
	}

	public static Test suite() {
		return suite(TestOperationDeclarations.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}
