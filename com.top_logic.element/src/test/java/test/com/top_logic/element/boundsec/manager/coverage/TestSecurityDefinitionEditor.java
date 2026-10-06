/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.SecurityDefinitionEditor;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.security.AccessGrant;
import com.top_logic.model.annotate.security.AccessRule;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.security.AccessParentDefinition;
import com.top_logic.model.security.ContainerAccessParent;
import com.top_logic.model.security.SelfAccessParent;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.security.SecurityConfigurationService.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.model.security.SecurityConfigurationService.TLModuleAccessRights;
import com.top_logic.model.security.SecurityConfigurationService.TypeBasedAccessRights;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Test for the {@link SecurityDefinitionEditor} against the model module
 * {@code TestSecurityCoverage} of the element test application.
 *
 * <p>
 * The editor works on two temporary files instead of the autoconf folder of the test application,
 * so that a test never changes the configuration the running services were started with.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSecurityDefinitionEditor extends BasicTestCase {

	/** Name of the analyzed test model module. */
	private static final String MODULE = "TestSecurityCoverage";

	/** Type contained in exactly one composition. */
	private static final String SINGLE_CONTAINED = MODULE + ":SingleContained";

	/** Type marked internal. */
	private static final String INTERNAL = MODULE + ":Internal";

	/** Module marked internal. */
	private static final String INTERNAL_MODULE = "TestSecurityCoverageInternal";

	/** Type of {@link #INTERNAL_MODULE}. */
	private static final String IN_INTERNAL_MODULE = INTERNAL_MODULE + ":InModule";

	/** Type without a role source and without a container. */
	private static final String ORPHAN = MODULE + ":Orphan";

	/** The composition a role parent rule of this test navigates backwards. */
	private static final String CONTAINER_REFERENCE = MODULE + ":Container#singles";

	/** Id of the role parent rule this test stores for {@link #SINGLE_CONTAINED}. */
	private static final String PARENT_RULE_ID = "SingleContained_roleParent";

	/** Id of the role parent rule the test application defines. */
	private static final String BASE_PARENT_ID = "TestSecurityCoverage_childOfCovered";

	/** The type {@link #BASE_PARENT_ID} applies to. */
	private static final String CHILD_OF_COVERED = MODULE + ":ChildOfCovered";

	/** Id of the role rule the test application defines. */
	private static final String BASE_ROLE_RULE_ID = "TestSecurityCoverage_covered";

	/** The role {@link #BASE_ROLE_RULE_ID} delivers. */
	private static final String READER_ROLE = MODULE + ".Reader";

	/** The access manager the element test application runs. */
	private static final String ACCESS_MANAGER_CLASS =
		"com.top_logic.element.boundsec.manager.StorageAccessManager";

	/**
	 * The access manager configuration of the underlying configuration layers, in the same shape as
	 * {@code element.test.config.xml} defines it.
	 */
	private static final String BASE_CONFIG = """
			<application>
				<services>
					<config service-class="com.top_logic.tool.boundsec.manager.AccessManager">
						<instance class="%s">
							<security-parents>
								<rule
									id="%s"
									inherit="true"
									meta-element="%s"
								>
									<path>
										<step
											attribute="%s"
											inverse="true"
										/>
									</path>
								</rule>
							</security-parents>
						</instance>
					</config>
				</services>
			</application>
			""".formatted(ACCESS_MANAGER_CLASS, BASE_PARENT_ID, CHILD_OF_COVERED, MODULE + ":Covered#children");

	/**
	 * The access rights of the underlying configuration layers, a module marked internal, in the
	 * same shape as {@code element.test.config.xml} defines them.
	 */
	private static final String BASE_GRANTS = """
			<application>
				<services>
					<config service-class="com.top_logic.model.security.SecurityConfigurationService">
						<instance class="com.top_logic.model.security.SecurityConfigurationService">
							<security-config>
								<module name="%s"
									internal="true"
								/>
							</security-config>
						</instance>
					</config>
				</services>
			</application>
			""";

	private SecurityDefinitionEditor _editor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		File dir = createdCleanTestDir(getName());
		_editor = new SecurityDefinitionEditor(
			new File(dir, InAppServiceConfigStore.fileName(AccessManager.Module.INSTANCE)),
			new File(dir, InAppServiceConfigStore.fileName(SecurityConfigurationService.Module.INSTANCE)));
	}

	@Override
	protected void tearDown() throws Exception {
		_editor = null;

		super.tearDown();
	}

	public void testEmptyStore() throws Exception {
		assertEquals(Collections.emptyList(), _editor.storedRoleParentRules());
		assertEquals(Collections.emptyList(), _editor.storedRoleRules());
		assertEquals(Collections.emptyList(), _editor.storedAccessRights());
		assertEquals(0, _editor.storedRuleCount());
		assertEquals(0, _editor.storedAccessRightsCount());
		assertFalse("Nothing is stored yet.", _editor.isStoredRoleParentRule(PARENT_RULE_ID));
		assertFalse("Nothing is stored yet.", _editor.isStoredRoleRule(BASE_ROLE_RULE_ID));
	}

	public void testPutReplaceRemoveRoleParentRule() throws Exception {
		NavigationRuleConfig rule = newParentRule();
		_editor.putRoleParentRule(rule);

		assertEquals(List.of(PARENT_RULE_ID), parentIds());
		assertTrue(_editor.isStoredRoleParentRule(PARENT_RULE_ID));
		assertEquals(1, _editor.storedRuleCount());

		rule.setInherit(false);
		_editor.putRoleParentRule(rule);
		assertEquals("A rule is replaced by its id, not appended a second time.",
			List.of(PARENT_RULE_ID), parentIds());
		assertFalse("The stored rule carries the edited state.",
			_editor.storedRoleParentRules().get(0).isInherit());

		assertTrue(_editor.removeRoleParentRule(PARENT_RULE_ID));
		assertEquals(Collections.emptyList(), parentIds());
		assertFalse("A rule that is not stored cannot be removed twice.",
			_editor.removeRoleParentRule(PARENT_RULE_ID));
	}

	public void testRemoveBaseRule() throws Exception {
		assertFalse("A rule of the base configuration is not stored, so it cannot be removed.",
			_editor.isStoredRoleParentRule(BASE_PARENT_ID));
		assertFalse("A rule of the base configuration cannot be removed by layering.",
			_editor.removeRoleParentRule(BASE_PARENT_ID));
		assertFalse("A rule of the base configuration cannot be removed by layering.",
			_editor.removeRoleRule(BASE_ROLE_RULE_ID));
	}

	public void testEditBaseRoleParentRule() throws Exception {
		NavigationRuleConfig rule = _editor.editableRoleParentRule(BASE_PARENT_ID);
		assertNotNull("The rule of the base configuration is in effect.", rule);
		assertEquals(CHILD_OF_COVERED, rule.getMetaElement());

		rule.setInherit(false);
		_editor.putRoleParentRule(rule);

		assertEquals("The edited rule replaces the base rule under its id.",
			List.of(BASE_PARENT_ID), parentIds());
		assertTrue("Now the rule is stored and can be removed again.",
			_editor.isStoredRoleParentRule(BASE_PARENT_ID));
		assertNull("An unknown rule has no editable copy.", _editor.editableRoleParentRule("noSuchRule"));
	}

	public void testEditBaseRoleRule() throws Exception {
		RoleRuleConfig rule = _editor.editableRoleRule(BASE_ROLE_RULE_ID);
		assertNotNull("The role rule of the base configuration is in effect.", rule);
		assertEquals(List.of(READER_ROLE), rule.getRole());

		rule.setId("TestSecurityCoverage_coveredCopy");
		_editor.putRoleRule(rule);

		assertEquals(List.of("TestSecurityCoverage_coveredCopy"), roleRuleIds());
		assertEquals(1, _editor.storedRuleCount());
		assertNull("An unknown rule has no editable copy.", _editor.editableRoleRule("noSuchRule"));

		assertTrue(_editor.removeRoleRule("TestSecurityCoverage_coveredCopy"));
		assertEquals(Collections.emptyList(), roleRuleIds());
	}

	public void testAccessRightsRoundTrip() throws Exception {
		TLClass orphan = type(ORPHAN);
		TLClassAccessRights entry = _editor.editableAccessRights(orphan);
		assertEquals(ORPHAN, entry.getName());
		assertFalse("A fresh entry has no flags set.", entry.isInternal());
		assertEquals(Collections.emptyList(), entry.getGrants());

		entry.getGrants().add(newGrant(SimpleBoundCommandGroup.READ_NAME, READER_ROLE));
		_editor.putAccessRights(entry);

		TLClassAccessRights stored = _editor.editableAccessRights(orphan);
		assertEquals(1, stored.getGrants().size());
		AccessRule grant = stored.getGrants().get(0);
		assertEquals(SimpleBoundCommandGroup.READ_NAME, grant.getOperation().id());
		assertEquals(List.of(READER_ROLE), grant.getRoles().stream().map(RoleConfig::getName).toList());
		assertEquals(1, _editor.storedAccessRightsCount());

		_editor.setInternal(orphan, true);
		stored = _editor.editableAccessRights(orphan);
		assertTrue("The flag is stored on the entry of the type.", stored.isInternal());
		assertEquals("Storing the flag keeps the grants of the entry.", 1, stored.getGrants().size());

		_editor.setWithoutSecurity(orphan, true);
		stored = _editor.editableAccessRights(orphan);
		assertTrue(stored.isWithoutSecurity());
		assertFalse("Excluding the type from access control drops the internal mark, the two contradicting each other.",
			stored.isInternal());
		assertEquals("The entry is replaced by name, not appended.", 1, _editor.storedAccessRightsCount());

		_editor.setInternal(orphan, true);
		stored = _editor.editableAccessRights(orphan);
		assertTrue(stored.isInternal());
		assertFalse("Marking the type internal drops the exclusion from access control.", stored.isWithoutSecurity());
		assertEquals("Storing the marks keeps the grants of the entry.", 1, stored.getGrants().size());

		ModelAccessRights onlyEntry = _editor.storedAccessRights().get(0);
		assertEquals(ORPHAN, onlyEntry.getName());
	}

	public void testEditShowsTheMarkOfTheBaseConfiguration() throws Exception {
		TLModule module = type(IN_INTERNAL_MODULE).getModule();
		TLModuleAccessRights entry = _editor.editableAccessRights(module);
		assertTrue("The mark of the underlying configuration is in effect, though not stored.", entry.isInternal());
		assertFalse(entry.isWithoutSecurity());

		assertTrue(_editor.editableAccessRights(type(INTERNAL)).isInternal());
	}

	public void testEditDoesNotSetDefaults() throws Exception {
		TLClassAccessRights entry = _editor.editableAccessRights(type(ORPHAN));
		assertFalse("A value in effect by default is not set explicitly.",
			entry.valueSet(entry.descriptor().getProperty(TypeBasedAccessRights.INTERNAL)));
		assertFalse(entry.valueSet(entry.descriptor().getProperty(TLClassAccessRights.ACCESS_PARENT)));
	}

	public void testDroppedMarkOverridesTheBaseConfiguration() throws Exception {
		TLModule module = type(IN_INTERNAL_MODULE).getModule();
		TLModuleAccessRights entry = _editor.editableAccessRights(module);
		entry.setInternal(false);
		_editor.putAccessRights(entry);

		assertFalse("The stored value takes precedence over the one in effect.",
			_editor.editableAccessRights(module).isInternal());

		String base = BASE_GRANTS.formatted(INTERNAL_MODULE);
		ModelAccessRights merged = grantsConfig(overlay(base, _editor.getGrantsFile())).getSecurityConfig()
			.get(INTERNAL_MODULE);
		assertFalse("The stored entry drops the mark of the underlying layers.",
			((TLModuleAccessRights) merged).isInternal());
	}

	public void testAccessParentRoundTrip() throws Exception {
		TLClass contained = type(SINGLE_CONTAINED);
		TLClassAccessRights entry = _editor.editableAccessRights(contained);
		entry.getGrants().add(newGrant(SimpleBoundCommandGroup.READ_NAME, READER_ROLE));
		entry.setInternal(true);
		_editor.putAccessRights(entry);

		_editor.setAccessParent(contained, container(CONTAINER_REFERENCE));
		TLClassAccessRights stored = _editor.editableAccessRights(contained);
		assertEquals(CONTAINER_REFERENCE,
			((ContainerAccessParent.Config) stored.getAccessParent().getDefinition()).getReference().qualifiedName());
		assertEquals("A type with an access parent has no grants of its own.", 0, stored.getGrants().size());
		assertFalse("A type with an access parent has no marks of its own.", stored.isInternal());

		_editor.setInternal(contained, true);
		stored = _editor.editableAccessRights(contained);
		assertNull("Marking the type internal drops its access parent.", stored.getAccessParent());

		_editor.setAccessParent(contained, container(CONTAINER_REFERENCE));
		_editor.setAccessParent(contained, null);
		assertNull(_editor.editableAccessRights(contained).getAccessParent());
	}

	public void testSelfKeepsOwnDefinition() throws Exception {
		TLClass contained = type(SINGLE_CONTAINED);
		TLClassAccessRights entry = _editor.editableAccessRights(contained);
		entry.getGrants().add(newGrant(SimpleBoundCommandGroup.READ_NAME, READER_ROLE));
		_editor.putAccessRights(entry);

		_editor.setAccessParent(contained, self());
		TLClassAccessRights stored = _editor.editableAccessRights(contained);
		assertTrue(stored.getAccessParent().getDefinition() instanceof SelfAccessParent.Config);
		assertEquals("A type deciding for itself keeps its grants.", 1, stored.getGrants().size());
	}

	/**
	 * The written file is layered onto a base configuration in the same way as an application
	 * layers its configuration onto the one of the framework.
	 */
	public void testLayerOntoBaseConfiguration() throws Exception {
		_editor.putRoleParentRule(newParentRule());

		ElementAccessManager.Config merged = accessManagerConfig(overlay(BASE_CONFIG, _editor.getAccessManagerFile()));
		assertEquals("The implementation class of the base configuration stays in effect.",
			ACCESS_MANAGER_CLASS, merged.getImplementationClass().getName());
		assertEquals("The rules of both configuration layers are kept.",
			List.of(BASE_PARENT_ID, PARENT_RULE_ID),
			merged.getSecurityParents().getRules().stream().map(NavigationRuleConfig::getId).toList());
	}

	/**
	 * A role parent rule for {@link #SINGLE_CONTAINED} navigating {@link #CONTAINER_REFERENCE}
	 * backwards.
	 */
	private static NavigationRuleConfig newParentRule() {
		NavigationRuleConfig rule = TypedConfiguration.newConfigItem(NavigationRuleConfig.class);
		rule.setId(PARENT_RULE_ID);
		rule.setMetaElement(SINGLE_CONTAINED);
		rule.setInherit(true);
		PathElementConfig step = TypedConfiguration.newConfigItem(PathElementConfig.class);
		step.setAttribute(TLModelPartRef.ref(CONTAINER_REFERENCE));
		step.setInverse(true);
		rule.getPathElements().add(step);
		return rule;
	}

	private List<String> parentIds() throws Exception {
		return _editor.storedRoleParentRules().stream().map(NavigationRuleConfig::getId).toList();
	}

	private List<String> roleRuleIds() throws Exception {
		return _editor.storedRoleRules().stream().map(NavigationRuleConfig::getId).toList();
	}

	private static AccessGrant newGrant(String operation, String role) {
		AccessGrant result = TypedConfiguration.newConfigItem(AccessGrant.class);
		result.update(result.descriptor().getProperty(AccessRule.OPERATION), new CommandGroupReference(operation));
		RoleConfig roleConfig = TypedConfiguration.newConfigItem(RoleConfig.class);
		roleConfig.setName(role);
		result.getRoles().add(roleConfig);
		return result;
	}

	private static TLClass type(String qualifiedTypeName) {
		return (TLClass) TLModelUtil.findType(qualifiedTypeName);
	}

	/**
	 * Reads the given file on top of the given base configuration, in the same way as an
	 * application layers its configuration onto the one of the framework.
	 */
	private static ApplicationConfig.Config overlay(String base, File increment) throws Exception {
		Protocol log = new BufferingProtocol();
		ApplicationConfig.Config baseConfig = (ApplicationConfig.Config) newAppConfigReader(log)
			.setSource(CharacterContents.newContent(base))
			.read();
		ApplicationConfig.Config result = (ApplicationConfig.Config) newAppConfigReader(log)
			.setBaseConfig(baseConfig)
			.setSource(BinaryDataFactory.createBinaryData(increment))
			.read();
		log.checkErrors();
		return result;
	}

	private static ConfigurationReader newAppConfigReader(Protocol log) {
		Map<String, ConfigurationDescriptor> descriptors =
			Collections.singletonMap(InAppServiceConfigStore.APPLICATION_TAG,
				TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class));
		return new ConfigurationReader(new DefaultInstantiationContext(log), descriptors);
	}

	private static SecurityConfigurationService.Config grantsConfig(ApplicationConfig.Config config) {
		ModuleConfiguration entry = config.getServices().get(SecurityConfigurationService.class);
		assertNotNull("The access rights are configured.", entry);
		return (SecurityConfigurationService.Config) entry.getInstance();
	}

	private static ElementAccessManager.Config accessManagerConfig(ApplicationConfig.Config config) {
		ModuleConfiguration entry = config.getServices().get(AccessManager.class);
		assertNotNull("The access manager is configured.", entry);
		return (ElementAccessManager.Config) entry.getInstance();
	}

	private static PolymorphicConfiguration<? extends AccessParentDefinition> container(String composition) {
		ContainerAccessParent.Config config = TypedConfiguration.newConfigItem(ContainerAccessParent.Config.class);
		config.setReference(TLModelPartRef.ref(composition));
		return config;
	}

	private static PolymorphicConfiguration<? extends AccessParentDefinition> self() {
		return TypedConfiguration.newConfigItem(SelfAccessParent.Config.class);
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestSecurityDefinitionEditor.class),
			SecurityCoverageCheck.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
