/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.diff;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import junit.framework.Test;

import test.com.top_logic.KBTestUtils;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.ErrorIgnoringProtocol;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.annotation.ConfigType;
import com.top_logic.element.meta.kbbased.storage.LinkStorage;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.element.model.ModelCopy;
import com.top_logic.element.model.ModelResolver;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.compare.CreateModelPatch;
import com.top_logic.element.model.diff.config.AddAnnotations;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.element.model.export.ModelConfigExtractor;
import com.top_logic.element.model.i18n.I18NAttributeStorageMapping;
import com.top_logic.element.model.migration.model.DeleteTLModuleProcessor;
import com.top_logic.element.model.migration.model.UpdateTLClassProcessor;
import com.top_logic.element.model.migration.model.refactor.MoveLinksProcessor;
import com.top_logic.element.model.migration.model.refactor.RemoveDuplicatesProcessor;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.IdentityMapping;
import com.top_logic.model.config.ModelPartConfig;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link CreateModelPatch} and {@link ApplyModelPatch}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestModelPatch extends AbstractModelPatchTest {

	private static final String BAG_TEST_TYPE = "A";

	private static final String BAG_TEST_REFERENCE = "refs";

	public void testPatch() throws SQLException {
		doTestMigrate("test1-left.model.xml", "test1-right.model.xml");
	}

	private void doTestMigrate(String leftResource, String rightResource) throws SQLException {
		doTestMigrate(leftResource, rightResource, null, migrated -> {
			// No additional checks.
		});
	}

	/**
	 * Migrates a persistent model created from the left fixture to the right fixture with the
	 * migration processors created while patching a transient copy.
	 *
	 * @param moduleName
	 *        The name to give the single module of both fixtures, see
	 *        {@link #fixture(String, String)}.
	 * @param check
	 *        Additional checks on the migrated persistent model.
	 */
	private void doTestMigrate(String leftResource, String rightResource, String moduleName,
			Consumer<TLModel> check) throws SQLException {
		ModelConfig leftConfig = fixture(leftResource, moduleName);
		ModelConfig rightConfig = fixture(rightResource, moduleName);

		TLModel left = setupModel(leftConfig);

		TLModel base = loadModelTransient(leftConfig);
		TLModel right = loadModelTransient(rightConfig);

		// Compute patch.
		List<DiffElement> diff = createPatch(base, right);

		// Compute migration while applying patch to transient model.
		List<MigrationProcessor> processors = applyPatch(base, new DefaultModelFactory(), diff);

		// Check that patch removes all differences between transient models.
		assertEmpty(createPatch(base, right));
		assertEqualsConfig(right, base);

		// Perform migration at database level.
		left = migrate(left, processors);

		// Check that migration removes all differences between persistent and target model.
		assertEmpty(createPatch(left, right));
		assertEqualsConfig(right, left);
		check.accept(left);
	}

	/**
	 * Runs the given processors on the database and reloads the given model afterwards.
	 */
	private TLModel migrate(TLModel model, List<MigrationProcessor> processors) throws SQLException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		ConnectionPool connectionPool = KBUtils.getConnectionPool(kb);
		PooledConnection connection = connectionPool.borrowWriteConnection();
		try {
			Log log = new AssertProtocol();
			MigrationContext context = new MigrationContext(log, connection);

			for (MigrationProcessor processor : processors) {
				processor.doMigration(context, log, connection);

				connection.commit();
			}
		} finally {
			connectionPool.releaseWriteConnection(connection);
		}

		KBTestUtils.clearCache(kb);
		return reload(kb, model);
	}

	/**
	 * Applies the patch from the left to the right fixture directly to a persistent model, the
	 * way the model service upgrades the application model at startup.
	 *
	 * @param moduleName
	 *        The name to give the single module of both fixtures, see
	 *        {@link #fixture(String, String)}.
	 * @param check
	 *        Additional checks on the patched persistent model.
	 */
	private void doTestPersistent(String leftResource, String rightResource, String moduleName,
			Consumer<TLModel> check) {
		TLModel left = setupModel(fixture(leftResource, moduleName));
		TLModel right = loadModelTransient(fixture(rightResource, moduleName));

		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			applyDiff(left, right);
			tx.commit();
		}

		assertEmpty(createPatch(left, right));
		assertEqualsConfig(right, left);
		check.accept(left);
	}

	/**
	 * Reads a model fixture.
	 *
	 * <p>
	 * Migration processors identify modules by name in the database, and all tests share a single
	 * knowledge base that keeps the rows of deleted modules in its history. Therefore, each test
	 * creating a persistent model from a fixture used by another test must give the module of
	 * the fixture a name of its own.
	 * </p>
	 *
	 * @param resource
	 *        The name of the fixture resource.
	 * @param moduleName
	 *        The new name of the single module of the fixture, <code>null</code> to keep the
	 *        name. Types in a renamed fixture must reference each other by their local names.
	 */
	private ModelConfig fixture(String resource, String moduleName) {
		ModelConfig config = readModelConfig(resource);
		if (moduleName != null) {
			assertEquals(resource, 1, config.getModules().size());
			ModuleConfig module = TypedConfiguration.copy(config.getModules().iterator().next());
			module.setName(moduleName);
			config.getModules().clear();
			config.getModules().add(module);
		}
		return config;
	}

	private <T extends TLObject> T reload(KnowledgeBase kb, T left) {
		left = kb.resolveObjectKey(new DefaultObjectKey(left.tId().getBranchContext(),
			left.tId().getHistoryContext(), left.tId().getObjectType(), left.tId().getObjectName())).getWrapper();
		return left;
	}

	private TLModel setupModel(ModelConfig config) {
		TLModel left;
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			left = loadModel(config);
			tx.commit();
		}
		return left;
	}

	public void testPatchAddGeneralizationOverride() throws SQLException {
		doTestMigrate("test-add-generalization-override-left.model.xml",
			"test-add-generalization-override-right.model.xml", "addGeneralizationOverride", migrated -> {
				// No additional checks.
			});
	}

	public void testPatchBecomesOverride() throws SQLException {
		doTestMigrate("test-becomes-override-left.model.xml", "test-becomes-override-right.model.xml",
			"becomesOverride", migrated -> assertBecameOverride(migrated, "becomesOverride"));
	}

	public void testPatchBecomesOwnPart() throws SQLException {
		doTestMigrate("test-becomes-override-right.model.xml", "test-becomes-override-left.model.xml",
			"becomesOwnPart", migrated -> assertBecameOwnPart(migrated, "becomesOwnPart"));
	}

	public void testPatchBecomesOverridePersistent() {
		doTestPersistent("test-becomes-override-left.model.xml", "test-becomes-override-right.model.xml",
			"becomesOverridePersistent", patched -> assertBecameOverride(patched, "becomesOverridePersistent"));
	}

	public void testPatchBecomesOwnPartPersistent() {
		doTestPersistent("test-becomes-override-right.model.xml", "test-becomes-override-left.model.xml",
			"becomesOwnPartPersistent", patched -> assertBecameOwnPart(patched, "becomesOwnPartPersistent"));
	}

	private void assertBecameOverride(TLModel model, String module) {
		TLStructuredTypePart baseA = localPart(model, module, "Base", "a");
		assertDefinition(localPart(model, module, "Ext", "a"), baseA);
		assertDefinition(localPart(model, module, "Ext2", "a"), baseA);
	}

	private void assertBecameOwnPart(TLModel model, String module) {
		TLStructuredTypePart extA = localPart(model, module, "Ext", "a");
		assertDefinition(extA, extA);
		assertDefinition(localPart(model, module, "Ext2", "a"), extA);
	}

	private void assertDefinition(TLStructuredTypePart part, TLStructuredTypePart expectedDefinition) {
		String partName = TLModelUtil.qualifiedName(part);
		assertEquals(partName, expectedDefinition, part.getDefinition());
		assertEquals(partName, part != expectedDefinition, part.isOverride());
	}

	/**
	 * Navigates to a local part of a class.
	 *
	 * <p>
	 * Navigates the same way as the {@link ModelConfigExtractor} to observe the parts of a model
	 * reloaded after a cache reset, not wrappers kept by lookup indexes.
	 * </p>
	 */
	private static TLStructuredTypePart localPart(TLModel model, String moduleName, String typeName,
			String partName) {
		TLModule module = model.getModules().stream()
			.filter(m -> m.getName().equals(moduleName))
			.findFirst().orElseThrow();
		TLClass type = (TLClass) module.getTypes().stream()
			.filter(t -> t.getName().equals(typeName))
			.findFirst().orElseThrow();
		return type.getLocalParts().stream()
			.filter(p -> p.getName().equals(partName))
			.findFirst().orElseThrow();
	}

	public void testUpdateClassMovesAssociations() throws SQLException {
		String sourceModule = "updateClassMoveSource";
		String targetModule = "updateClassMoveTarget";
		ModelConfig config = fixture("test-update-class.model.xml", sourceModule);
		ModuleConfig target = TypedConfiguration.newConfigItem(ModuleConfig.class);
		target.setName(targetModule);
		config.getModules().add(target);
		TLModel model = setupModel(config);

		UpdateTLClassProcessor.Config update = updateClass(sourceModule, "A", targetModule, "A");
		DeleteTLModuleProcessor.Config delete = TypedConfiguration.newConfigItem(DeleteTLModuleProcessor.Config.class);
		delete.setName(sourceModule);
		delete.setFailOnExistingTypes(true);
		delete.setSkipModelBaselineChange(true);

		TLModel migrated = migrate(model, processors(update, delete));

		assertFalse(migrated.getModules().stream().anyMatch(m -> m.getName().equals(sourceModule)));
		assertAssociation(localPart(migrated, targetModule, "A", "ref"), targetModule, "A$ref");
	}

	public void testUpdateClassRenamesAssociations() throws SQLException {
		String module = "updateClassRename";
		TLModel model = setupModel(fixture("test-update-class.model.xml", module));

		TLModel migrated = migrate(model, processors(updateClass(module, "A", module, "B")));

		assertAssociation(localPart(migrated, module, "B", "ref"), module, "B$ref");
		assertFalse(migrated.getModules().stream()
			.filter(m -> m.getName().equals(module))
			.flatMap(m -> m.getTypes().stream())
			.anyMatch(t -> t.getName().equals("A$ref")));
	}

	private static UpdateTLClassProcessor.Config updateClass(String module, String type, String newModule,
			String newType) {
		UpdateTLClassProcessor.Config config = TypedConfiguration.newConfigItem(UpdateTLClassProcessor.Config.class);
		config.setName(qTypeName(module, type));
		config.setNewName(qTypeName(newModule, newType));
		config.setSkipModelBaselineChange(true);
		return config;
	}

	private static QualifiedTypeName qTypeName(String module, String type) {
		QualifiedTypeName result = TypedConfiguration.newConfigItem(QualifiedTypeName.class);
		result.setName(TLModelUtil.qualifiedName(module, type));
		return result;
	}

	@SafeVarargs
	private static List<MigrationProcessor> processors(
			PolymorphicConfiguration<? extends MigrationProcessor>... configs) {
		return TypedConfiguration.getInstanceList(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Arrays.asList(configs));
	}

	private static void assertAssociation(TLStructuredTypePart reference, String expectedModule,
			String expectedName) {
		TLAssociation association = ((TLReference) reference).getEnd().getOwner();
		assertEquals(expectedName, association.getName());
		assertEquals(expectedModule, association.getModule().getName());
		assertTrue(association.getModule().getTypes().contains(association));
	}

	/**
	 * Checks that the links of a reference are moved to the bag table, when the reference becomes
	 * a bag.
	 */
	public void testPatchMakesBag() throws SQLException {
		doTestLinkMove("test-bag-left.model.xml", "test-bag-right.model.xml", "makeBag",
			(a, b) -> List.of(a, b), (a, b) -> List.of(a, b));
	}

	/**
	 * Checks that the links of a bag reference are moved to the table of references that are no
	 * bag, when the reference stops being a bag, and that duplicate values are removed.
	 */
	public void testPatchMakesUnique() throws SQLException {
		doTestLinkMove("test-bag-right.model.xml", "test-bag-left.model.xml", "makeUnique",
			(a, b) -> List.of(a, b, a, b), (a, b) -> List.of(a, b));
	}

	/**
	 * Checks that the links of a reference are moved to the table of its new deletion policy.
	 */
	public void testPatchDeletionPolicy() throws SQLException {
		doTestLinkMove("test-bag-left.model.xml", "test-veto-right.model.xml", "makeVeto",
			(a, b) -> List.of(a, b), (a, b) -> List.of(a, b));
	}

	/**
	 * Migrates a persistent model with a value in the reference {@link #BAG_TEST_REFERENCE} from
	 * the left to the right fixture, which differ in a property of the reference that determines
	 * its link table.
	 *
	 * @param before
	 *        The value of the reference before the migration, given two objects.
	 * @param expected
	 *        The expected value of the reference after the migration, given the same two objects.
	 */
	private void doTestLinkMove(String leftResource, String rightResource, String moduleName,
			BiFunction<TLObject, TLObject, List<TLObject>> before,
			BiFunction<TLObject, TLObject, List<TLObject>> expected) throws SQLException {
		ModelConfig leftConfig = fixture(leftResource, moduleName);
		ModelConfig rightConfig = fixture(rightResource, moduleName);

		TLModel left = setupModel(leftConfig);
		TLStructuredTypePart leftRef = localPart(left, moduleName, BAG_TEST_TYPE, BAG_TEST_REFERENCE);
		String sourceTable = linkTable(leftRef);

		TLObject owner;
		TLObject a;
		TLObject b;
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLClass type = (TLClass) leftRef.getOwner();
			DefaultModelFactory factory = new DefaultModelFactory();
			owner = factory.createObject(type);
			a = factory.createObject(type);
			b = factory.createObject(type);
			owner.tUpdate(leftRef, before.apply(a, b));
			tx.commit();
		}
		assertEquals(before.apply(a, b), owner.tValue(leftRef));
		assertEquals(before.apply(a, b).size(), countLinks(owner, sourceTable));

		TLModel base = loadModelTransient(leftConfig);
		TLModel right = loadModelTransient(rightConfig);
		List<MigrationProcessor> processors = applyPatch(base, new DefaultModelFactory(), createPatch(base, right));
		assertTrue(processors.stream().anyMatch(MoveLinksProcessor.class::isInstance));
		assertEquals(leftRef.isBag() && !localPart(right, moduleName, BAG_TEST_TYPE, BAG_TEST_REFERENCE).isBag(),
			processors.stream().anyMatch(RemoveDuplicatesProcessor.class::isInstance));

		TLModel migrated = migrate(left, processors);
		assertEmpty(createPatch(migrated, right));

		TLStructuredTypePart migratedRef = localPart(migrated, moduleName, BAG_TEST_TYPE, BAG_TEST_REFERENCE);
		String destTable = linkTable(migratedRef);
		assertFalse(sourceTable.equals(destTable));

		TLObject migratedOwner = reload(kb(), owner);
		TLObject migratedA = reload(kb(), a);
		TLObject migratedB = reload(kb(), b);
		List<TLObject> expectedValue = expected.apply(migratedA, migratedB);
		assertEquals(0, countLinks(migratedOwner, sourceTable));
		assertEquals(expectedValue.size(), countLinks(migratedOwner, destTable));
		// Note: The value is read through the storage of the reloaded reference. The type of the
		// owner is resolved through lookup indexes that still hold the wrappers from before the
		// SQL migration, whose storage reads the old table.
		assertEquals(expectedValue,
			migratedRef.getStorageImplementation().getAttributeValue(migratedOwner, migratedRef));
	}

	private static String linkTable(TLStructuredTypePart reference) {
		return ((LinkStorage<?>) reference.getStorageImplementation()).getTable();
	}

	private static int countLinks(TLObject owner, String table) {
		int result = 0;
		Iterator<KnowledgeAssociation> it = ((KnowledgeObject) owner.tHandle()).getOutgoingAssociations(table);
		while (it.hasNext()) {
			it.next();
			result++;
		}
		return result;
	}

	public void testPatchBecomesOverrideTransient() {
		doTestTransient("test-becomes-override-left.model.xml", "test-becomes-override-right.model.xml");
	}

	public void testPatchBecomesOwnPartTransient() {
		doTestTransient("test-becomes-override-right.model.xml", "test-becomes-override-left.model.xml");
	}

	public void testPatchTransient() {
		doTestTransient("test1-left.model.xml", "test1-right.model.xml");
	}

	public void testPatchOverrideTransient() {
		doTestTransient("test-override-left.model.xml", "test-override-right.model.xml");
	}

	public void testPatchAddGeneralizationOverrideTransient() {
		doTestTransient("test-add-generalization-override-left.model.xml",
			"test-add-generalization-override-right.model.xml");
	}

	public void testPatchRefDeleteTransient() {
		doTestTransient("test-refdelete-left.model.xml", "test-refdelete-right.model.xml");
	}

	private void doTestTransient(String leftFixture, String rightFixture) {
		TLModel left = loadModelTransient(leftFixture);
		TLModel right = loadModelTransient(rightFixture);

		applyDiff(left, right);

		assertEmpty(createPatch(left, right));
		assertEqualsConfig(right, left);
	}

	public void testUpgradeLog() {
		TLModel left = loadModelTransient("test-upgrade-log-left.model.xml");
		TLModel right = loadModelTransient("test-upgrade-log-right.model.xml");

		BufferingProtocol log = new BufferingProtocol();
		ApplyModelPatch.applyPatch(log, left, TransientObjectFactory.INSTANCE, createPatch(left, right));
		log.checkErrors();

		assertEmpty(createPatch(left, right));
		assertContainsInfo(log,
			"Changing type of reference 'm0:A#r' from 'm0:B' to 'm0:I', existing values are kept.");
		assertContainsInfo(log,
			"Changing type of reference 'm0:A#s' from 'm0:B' to 'm0:C', existing values are kept, "
				+ "but values that are not of type 'm0:C' no longer match the reference.");
		assertContainsInfo(log, "Updating storage mapping of 'm0:D' from '"
			+ I18NAttributeStorageMapping.class.getName() + "' to '" + DirectMapping.class.getName() + "'.");
	}

	private void assertContainsInfo(BufferingProtocol log, String expected) {
		assertTrue("Missing '" + expected + "' in: " + log.getInfos(),
			log.getInfos().stream().anyMatch(info -> info.contains(expected)));
	}

	public void testCopy() {
		assertCopyWithoutDiff("test1-left.model.xml");
		assertCopyWithoutDiff("test1-right.model.xml");
		assertCopyWithoutDiff("test-override-left.model.xml");
		assertCopyWithoutDiff("test-add-generalization-override-left.model.xml");
		assertCopyWithoutDiff("test-add-generalization-override-right.model.xml");
		assertCopyWithoutDiff("test-becomes-override-left.model.xml");
		assertCopyWithoutDiff("test-becomes-override-right.model.xml");
	}

	private void assertCopyWithoutDiff(String fixture) {
		TLModel model = loadModelTransient(fixture);
		TLModel copy = ModelCopy.copy(model);
		TLModel copy2 = copyDumpLoad(model);

		assertEmpty(createPatch(model, copy));
		assertEqualsConfig(model, copy);

		assertEmpty(createPatch(model, copy2));
		assertEqualsConfig(model, copy2);
	}

	private TLModel copyDumpLoad(TLModel model) {
		TLModelImpl result = new TLModelImpl();

		ModelConfig config = (ModelConfig) model.visit(new ModelConfigExtractor(), null);
		ModelResolver modelResolver = new TestingModelResolver(new ErrorIgnoringProtocol(), result, null);
		modelResolver.createModel(config);
		modelResolver.complete();

		return result;
	}

	private void assertEqualsConfig(TLModel expected, TLModel actual) {
		assertEqualConfig(config(expected), config(actual));
	}

	private void assertEqualConfig(ModelPartConfig expected, ModelPartConfig actual) {
		boolean equals = ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(expected, actual);
		if (!equals) {
			assertEquals(expected.toString(), actual.toString());
			fail("Expected configuration '" + expected + "' but was '" + actual + "'.");
		}
	}

	private ModelPartConfig config(TLModelPart model) {
		return model.visit(new ModelConfigExtractor(), null);
	}

	private void assertEmpty(Collection<?> values) {
		if (!values.isEmpty()) {
			fail("Expected collection to be empty but was: " + values);
		}
	}

	public void testAnnotationUpdate() {
		TLModelImpl leftModel = annotationUpdateModel();
		
		TLModelImpl rightModel = annotationUpdateModel();
		TLType type = rightModel.getModule("m1").getType("c1");
		ConfigType configType = TypedConfiguration.newConfigItem(ConfigType.class);
		configType.setValue("C1_TYPE");
		type.setAnnotation(configType);
		
		AddAnnotations addAnnotations = TypedConfiguration.newConfigItem(AddAnnotations.class);
		addAnnotations.setPart("m1:c1");
		addAnnotations.getAnnotations().add(TypedConfiguration.copy(configType));

		List<DiffElement> patch = createPatch(leftModel, rightModel);
		assertConfigEquals(list(addAnnotations), patch);
	}

	private TLModelImpl annotationUpdateModel() {
		TLModelImpl model = new TLModelImpl();
		TLModule m1 = TLModelUtil.addModule(model, "m1");
		TLModelUtil.addDatatype(m1, "c1", IdentityMapping.INSTANCE);
		return model;
	}

	public static Test suite() {
		// Note: Since the test resets KB caches to observe the changes after a SQL-based migration,
		// the test must be executed separately. Otherwise, caches of other services would also
		// become inconsistent.
		return TestUtils.doNotMerge(KBSetup.getSingleKBTest(TestModelPatch.class));
	}

}
