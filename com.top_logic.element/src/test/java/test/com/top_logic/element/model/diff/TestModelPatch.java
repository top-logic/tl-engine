/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.diff;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.KBTestUtils;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.ErrorIgnoringProtocol;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.element.config.DefinitionReader;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.annotation.ConfigType;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.element.model.ModelCopy;
import com.top_logic.element.model.ModelResolver;
import com.top_logic.element.model.PersistentTLModel;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.compare.CreateModelPatch;
import com.top_logic.element.model.diff.config.AddAnnotations;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.element.model.export.ModelConfigExtractor;
import com.top_logic.element.model.migration.model.TLModelBaseLineMigrationProcessor;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.access.IdentityMapping;
import com.top_logic.model.config.ModelPartConfig;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link CreateModelPatch} and {@link ApplyModelPatch}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestModelPatch extends BasicTestCase {

	public void testPatch() throws SQLException {
		doTestMigrate("test1-left.model.xml", "test1-right.model.xml");
	}

	private void doTestMigrate(String leftResource, String rightResource) throws SQLException {
		TLModel left = setupModel(leftResource);

		TLModel base = loadModelTransient(leftResource);
		TLModel right = loadModelTransient(rightResource);

		// Compute patch.
		List<DiffElement> diff = createPatch(base, right);

		// Compute migration while applying patch to transient model.
		List<MigrationProcessor> processors = applyPatch(base, new DefaultModelFactory(), diff);

		// Check that patch removes all differences between transient models.
		assertEmpty(createPatch(base, right));
		assertEqualsConfig(right, base);

		// Perform migration at database level.
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
		left = reload(kb, left);

		// Check that migration removes all differences between persistent and target model.
		assertEmpty(createPatch(left, right));
		assertEqualsConfig(right, left);
	}

	private <T extends TLObject> T reload(KnowledgeBase kb, T left) {
		left = kb.resolveObjectKey(new DefaultObjectKey(left.tId().getBranchContext(),
			left.tId().getHistoryContext(), left.tId().getObjectType(), left.tId().getObjectName())).getWrapper();
		return left;
	}

	private TLModel setupModel(String configResource) {
		TLModel left;
		try (Transaction tx = kb().beginTransaction()) {
			left = loadModel(configResource);
			tx.commit();
		}
		return left;
	}

	public void testPatchTransient() {
		doTestTransient("test1-left.model.xml", "test1-right.model.xml");
	}

	public void testPatchOverrideTransient() {
		doTestTransient("test-override-left.model.xml", "test-override-right.model.xml");
	}

	public void testPatchRefDeleteTransient() {
		doTestTransient("test-refdelete-left.model.xml", "test-refdelete-right.model.xml");
	}

	private void doTestTransient(String leftFixture, String rightFixture) {
		TLModel left = loadModelTransient(leftFixture);
		TLModel right = loadModelTransient(rightFixture);

		List<DiffElement> patch = createPatch(left, right);
		applyPatch(left, new DefaultModelFactory(), patch);

		assertEmpty(createPatch(left, right));
		assertEqualsConfig(right, left);
	}

	public void testCopy() {
		assertCopyWithoutDiff("test1-left.model.xml");
		assertCopyWithoutDiff("test1-right.model.xml");
		assertCopyWithoutDiff("test-override-left.model.xml");
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

	private List<MigrationProcessor> applyPatch(TLModel left, TLFactory factory, List<DiffElement> patch) {
		AssertProtocol log = new AssertProtocol() {
			@Override
			public void localInfo(String message, int verbosityLevel) {
				if (verbosityLevel < Log.INFO && message.contains("conflict")) {
					// Note: Delete-delete conflicts are normal, since deleting a type deletes all
					// parts using that type.
					fail(message);
				}
				super.localInfo(message, verbosityLevel);
			}
		};

		ArrayList<PolymorphicConfiguration<? extends MigrationProcessor>> processors = new ArrayList<>();
		ApplyModelPatch.applyPatch(log, left, factory, patch, processors);

		MigrationConfig migration = TypedConfiguration.newConfigItem(MigrationConfig.class);
		migration.setProcessors(processors);

		System.out.println(migration);

		// Skip baseline, since this does not exist for models dynamically created during test.
		for (PolymorphicConfiguration<? extends MigrationProcessor> config : processors) {
			if (config instanceof TLModelBaseLineMigrationProcessor.Config<?> skip) {
				skip.setSkipModelBaselineChange(true);
			}
		}

		return TypedConfiguration.getInstanceList(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			processors);
	}

	private List<DiffElement> createPatch(TLModel left, TLModel right) {
		CreateModelPatch patchCreator = new CreateModelPatch();
		patchCreator.addPatch(left, right);
		List<DiffElement> patch = patchCreator.getPatch();
		return patch;
	}

	private TLModel loadModel(String name) {
		return loadModel(PersistentTLModel.newInstance(kb()), new DefaultModelFactory(), name);
	}

	private TLModel loadModelTransient(String name) {
		return loadModel(new TLModelImpl(), TransientObjectFactory.INSTANCE, name);
	}

	private TLModel loadModel(TLModel model, TLFactory factory, String name) {
		ModelConfig config =
			DefinitionReader.readElementConfig(new ClassRelativeBinaryContent(TestModelPatch.class, name));
		AssertProtocol log = new AssertProtocol();
		ModelResolver modelResolver = new TestingModelResolver(log, model, factory);
		modelResolver.createModel(config);
		modelResolver.complete();
		return model;
	}

	private KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
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

	private final class TestingModelResolver extends ModelResolver {
		/** 
		 * Creates a {@link TestingModelResolver}.
		 */
		private TestingModelResolver(Protocol log, TLModel model, TLFactory factory) {
			super(log, model, factory);
		}
	
		@Override
		protected void autoExtendTLObject(ModuleConfig moduleConf) {
			// Don't do it, there is no type tl.model:TLObject in the test models.
		}
	}

	public static Test suite() {
		// Note: Since the test resets KB caches to observe the changes after a SQL-based migration,
		// the test must be executed separately. Otherwise, caches of other services would also
		// become inconsistent.
		return TestUtils.doNotMerge(KBSetup.getSingleKBTest(TestModelPatch.class));
	}

}
