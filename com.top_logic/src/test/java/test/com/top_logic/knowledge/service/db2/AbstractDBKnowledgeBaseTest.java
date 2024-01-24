/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.KBTestUtils;
import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.base.cluster.ClusterManagerSetup;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.ExpectedError;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.SimpleDecoratedTestSetup;
import test.com.top_logic.basic.SingleTestFactory;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestSetupDecorator;
import test.com.top_logic.basic.db.schema.MultipleBranchSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.service.AbstractKnowledgeBaseTest;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.objects.identifier.ObjectId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.CommittableAdapter;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * Utility method for {@link KnowledgeBase} testing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDBKnowledgeBaseTest extends AbstractKnowledgeBaseTest implements
		KnowledgeBaseTestScenarioConstants {

	public static final Committable COMMIT_FAILURE = new CommittableAdapter() {
		@Override
		public boolean commit(CommitContext context) {
			return false;
		}
	};
	
	public static final Committable COMMIT_ABORT = new CommittableAdapter() {
		@Override
		public boolean commit(CommitContext context) {
			throw new ExpectedFailure("Commit aborted");
		}
	};

	public static final Committable COMMIT_ERROR = new CommittableAdapter() {
		@Override
		public boolean commit(CommitContext context) {
			throw new ExpectedError("Commit crashed");
		}
	};
	
	public static final Mapping GET_TARGET_NAME = new Mapping() {
		@Override
		public Object map(Object input) {
			KnowledgeObject targetObject = (KnowledgeObject) KBTestUtils.GET_TARGET.map(input);
			try {
				return targetObject.getAttributeValue(A1_NAME);
			} catch (NoSuchAttributeException e) {
				throw new UnreachableAssertion(e);
			}
		}
	};
	
	private LocalTestSetup localSetup;

	public static final KnowledgeObject[] NO_OBJECTS = new KnowledgeObject[0];

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.localSetup = createSetup(this);
		this.localSetup.setUp();

		// Resets the revision of the current session to the last local revision; actually like
		// there is no revision when test starts.
		updateSessionRevision();
	}

	protected LocalTestSetup createSetup(Test self) {
		return new DBKnowledgeBaseTestSetup(self);
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.localSetup.tearDown();
		this.localSetup = null;
		
		super.tearDown();
	}
	
	
	/**
	 * Creates a new object of given type (sub type of
	 * {@link KnowledgeBaseTestScenarioConstants#A_NAME A}) and sets value of attribute
	 * {@link KnowledgeBaseTestScenarioConstants#A1_NAME A1}.
	 */
	protected KnowledgeObject newA(String concreteTypeName, String a1) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject(concreteTypeName);
		setA1(result, a1);
		return result;
	}

	protected KnowledgeObject newB(String a1) throws DataObjectException {
		return newA(B_NAME, a1);
	}

	protected KnowledgeObject newU(String a1) throws DataObjectException {
		return newA(U_NAME, a1);
	}
	
	protected KnowledgeObject newC(String a1) throws DataObjectException {
		return newA(C_NAME, a1);
	}

	protected KnowledgeObject newE(String a1) throws DataObjectException {
		return newA(E_NAME, a1);
	}

	protected KnowledgeObject newF(String a1) throws DataObjectException {
		return newA(F_NAME, a1);
	}

	protected KnowledgeObject newG(String a1) throws DataObjectException {
		return newA(G_NAME, a1);
	}

	protected KnowledgeObject newH(String a1) throws DataObjectException {
		return newA(H_NAME, a1);
	}

	protected Object setReference(KnowledgeItem e, KnowledgeItem reference, boolean monomorphic,
			HistoryType historyType, boolean branchGlobal) throws DataObjectException {
		return e.setAttributeValue(getReferenceAttr(monomorphic, historyType, branchGlobal), reference);
	}

	protected String getReferenceAttr(boolean monomorphic, HistoryType historyType, boolean branchGlobal) {
		if (monomorphic) {
			switch (historyType) {
				case HISTORIC:
					if (branchGlobal) {
						return REFERENCE_MONO_HIST_GLOBAL_NAME;
					} else {
						return REFERENCE_MONO_HIST_LOCAL_NAME;
					}
				case CURRENT:
					if (branchGlobal) {
						return REFERENCE_MONO_CUR_GLOBAL_NAME;
					} else {
						return REFERENCE_MONO_CUR_LOCAL_NAME;
					}
				case MIXED:
					if (branchGlobal) {
						return REFERENCE_MONO_MIXED_GLOBAL_NAME;
					} else {
						return REFERENCE_MONO_MIXED_LOCAL_NAME;
					}
			}
			throw HistoryType.noSuchType(historyType);
		} else {
			switch (historyType) {
				case HISTORIC:
					if (branchGlobal) {
						return REFERENCE_POLY_HIST_GLOBAL_NAME;
					} else {
						return REFERENCE_POLY_HIST_LOCAL_NAME;
					}
				case CURRENT:
					if (branchGlobal) {
						return REFERENCE_POLY_CUR_GLOBAL_NAME;
					} else {
						return REFERENCE_POLY_CUR_LOCAL_NAME;
					}
				case MIXED:
					if (branchGlobal) {
						return REFERENCE_POLY_MIXED_GLOBAL_NAME;
					} else {
						return REFERENCE_POLY_MIXED_LOCAL_NAME;
					}
			}
			throw HistoryType.noSuchType(historyType);
		}
	}

	protected KnowledgeItem getReference(KnowledgeItem e, boolean monomorphic, HistoryType historyType,
			boolean branchGlobal) throws DataObjectException {
		Object attributeValue = e.getAttributeValue(getReferenceAttr(monomorphic, historyType, branchGlobal));
		if (attributeValue != null) {
			assertInstanceof(attributeValue, KnowledgeItem.class);
		}
		return (KnowledgeItem) attributeValue;
	}

	protected void setA1(KnowledgeItem result, String a1) throws DataObjectException {
		result.setAttributeValue(A1_NAME, a1);
	}
	
	protected void setA2(KnowledgeItem result, String a2) throws DataObjectException {
		result.setAttributeValue(A2_NAME, a2);
	}
	
	protected void setB1(KnowledgeItem result, String b1) throws DataObjectException {
		result.setAttributeValue(B1_NAME, b1);
	}
	
	protected void setB2(KnowledgeItem result, String b2) throws DataObjectException {
		result.setAttributeValue(B2_NAME, b2);
	}
	
	protected KnowledgeObject newD(String a1) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject(D_NAME);
		setA1(result, a1);
		return result;
	}

	/**
	 * Creates a new object that can be used as value for a monomorphic reference.
	 */
	protected KnowledgeObject newReference(String a1) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject(REFERENCE_TYPE_NAME);
		setA1(result, a1);
		return result;
	}

	protected KnowledgeObject newX(String x10) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject(X_NAME);
		setX10(result, x10);
		return result;
	}
	
	protected KnowledgeObject newX(boolean x1, byte x2, char x3, String x4, double x5, float x6, int x7, long x8, short x9, String x10) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject(X_NAME);
		updateX(result, x1, x2, x3, x4, x5, x6, x7, x8, x9, x10);
		return result;
	}

	protected void updateX(KnowledgeObject x, boolean x1, byte x2, char x3, String x4,
			double x5, float x6, int x7, long x8, short x9,
			String x10) throws DataObjectException, AssertionError {
		setX1(x, x1);
		setX2(x, x2);
		setX3(x, x3);
		setX4Date(x, x4);
		setX5(x, x5);
		setX6(x, x6);
		setX7(x, x7);
		setX8(x, x8);
		setX9(x, x9);
		setX10(x, x10);
	}

	protected void setX4Date(KnowledgeObject x, String x4) throws DataObjectException, AssertionError {
		try {
			x.setAttributeValue(X4_NAME, x4Format().parse(x4));
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Format that is used to parse attribute {@link KnowledgeBaseTestScenarioConstants#X4_NAME}.
	 */
	protected SimpleDateFormat x4Format() {
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm");
	}

	protected void setX10(KnowledgeObject x, String x10) throws DataObjectException {
		x.setAttributeValue(X10_NAME, x10);
	}

	protected void setX9(KnowledgeObject x, short x9) throws DataObjectException {
		x.setAttributeValue(X9_NAME, Short.valueOf(x9));
	}

	protected void setX8(KnowledgeObject x, long x8) throws DataObjectException {
		x.setAttributeValue(X8_NAME, Long.valueOf(x8));
	}

	protected void setX7(KnowledgeObject x, int x7) throws DataObjectException {
		x.setAttributeValue(X7_NAME, Integer.valueOf(x7));
	}

	protected void setX6(KnowledgeObject x, float x6) throws DataObjectException {
		x.setAttributeValue(X6_NAME, Float.valueOf(x6));
	}

	protected void setX5(KnowledgeObject x, double x5) throws DataObjectException {
		x.setAttributeValue(X5_NAME, Double.valueOf(x5));
	}

	protected void setX3(KnowledgeObject x, char x3) throws DataObjectException {
		x.setAttributeValue(X3_NAME, Character.valueOf(x3));
	}

	protected void setX2(KnowledgeObject x, byte x2) throws DataObjectException {
		x.setAttributeValue(X2_NAME, Byte.valueOf(x2));
	}

	protected void setX1(KnowledgeObject x, boolean x1) throws DataObjectException {
		x.setAttributeValue(X1_NAME, Boolean.valueOf(x1));
	}
	
	protected KnowledgeObject newY(int x1, long x2, String x3) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject(Y_NAME);
		updateY(result, x1, x2, x3);
		return result;
	}
	
	protected void updateY(KnowledgeObject x, int x1, long x2, String x3) throws DataObjectException, AssertionError {
		x.setAttributeValue(Y1_NAME, Integer.valueOf(x1));
		x.setAttributeValue(Y2_NAME, Long.valueOf(x2));
		x.setAttributeValue(Y3_NAME, x3);
	}

	protected KnowledgeObject newZ(String z1, double z2) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject(Z_NAME);
		updateZ(result, z1, z2);
		return result;
	}
	
	protected void updateZ(KnowledgeObject z, String z1, double z2) throws DataObjectException, AssertionError {
		z.setAttributeValue(Z1_NAME, z1);
		z.setAttributeValue(Z2_NAME, Double.valueOf(z2));
	}
	
	protected KnowledgeAssociation newAB(KnowledgeObject b1, KnowledgeObject d1) throws DataObjectException {
		KnowledgeAssociation result = b1.getKnowledgeBase().createAssociation(b1, d1, AB_NAME);
		result.setAttributeValue(AB1_NAME,
			b1.getAttributeValue(A1_NAME) + "-" + d1.getAttributeValue(A1_NAME));
		return result;
	}

	protected KnowledgeAssociation newBC(KnowledgeObject b1, KnowledgeObject c1) throws DataObjectException {
		KnowledgeAssociation result = b1.getKnowledgeBase().createAssociation(b1, c1, BC_NAME);
		String value = b1.getAttributeValue(A1_NAME) + "-" + c1.getAttributeValue(A1_NAME);
		setBC1(result, value);
		return result;
	}

	protected KnowledgeAssociation newUA(KnowledgeObject b1, KnowledgeObject c1) throws DataObjectException {
		KnowledgeAssociation result = b1.getKnowledgeBase().createAssociation(b1, c1, UA_NAME);
		String value = b1.getAttributeValue(A1_NAME) + "-" + c1.getAttributeValue(A1_NAME);
		setUA1(result, value);
		return result;
	}

	/**
	 * Sets the attribute {@link KnowledgeBaseTestScenarioConstants#BC1_NAME}.
	 */
	protected void setBC1(KnowledgeAssociation result, String value) throws DataObjectException {
		result.setAttributeValue(BC1_NAME, value);
	}

	/**
	 * Sets the attribute {@link KnowledgeBaseTestScenarioImpl#BC2}.
	 */
	protected void setBC2(KnowledgeAssociation result, String value) throws DataObjectException {
		result.setAttributeValue(BC2_NAME, value);
	}

	/**
	 * Sets the attribute {@link KnowledgeBaseTestScenarioImpl#UA1}.
	 */
	protected void setUA1(KnowledgeAssociation result, String value) throws DataObjectException {
		result.setAttributeValue(UA1_NAME, value);
	}

	/**
	 * Sets the attribute {@link KnowledgeBaseTestScenarioImpl#UA2}.
	 */
	protected void setUA2(KnowledgeAssociation result, String value) throws DataObjectException {
		result.setAttributeValue(UA2_NAME, value);
	}

	/**
	 * Sets the attribute {@link KnowledgeBaseTestScenarioImpl#AB2}.
	 */
	protected void setAB2(KnowledgeAssociation result, String value) throws DataObjectException {
		result.setAttributeValue(AB2_NAME, value);
	}
	
	protected Set navigateForward(KnowledgeObject baseObject, String associationType) {
		return toSet(Mappings.map(KBTestUtils.GET_TARGET, baseObject.getOutgoingAssociations(associationType)));
	}
	
	protected Set navigateBackward(KnowledgeObject baseObject, String associationType) {
		return toSet(Mappings.map(KBTestUtils.GET_SOURCE, baseObject.getOutgoingAssociations(associationType)));
	}

	public static void assertTargetNames(String msg, String[] expectedTargetNamesArray, Iterator foundAssociations) {
		ArrayList expectedTargetNames = new ArrayList(Arrays.asList(expectedTargetNamesArray));
		ArrayList foundTargets = new ArrayList(Mappings.map(GET_TARGET_NAME, CollectionUtil.toList(foundAssociations)));
		
		// Compare sorted lists instead of sets to make potential error message
		// readable.
		Collections.sort(expectedTargetNames);
		Collections.sort(foundTargets);
		
		TestCase.assertEquals(msg, expectedTargetNames, foundTargets);
	}

	/**
	 * Short-cut for {@link DBKnowledgeBaseTestSetup#kb()}.
	 */
	@Override
	public DBKnowledgeBase kb() {
		return DBKnowledgeBaseTestSetup.kb();
	}
	
	public Set<? extends MetaObject> types(String... names) {
		HashSet<MetaObject> result = new HashSet<>();
		for (String name : names) {
			result.add(type(name));
		}
		return result;
	}

	public MOClass type(String name) {
		try {
			return (MOClass) kb().getMORepository().getMetaObject(name);
		} catch (UnknownTypeException ex) {
			throw fail("Type not found.", ex);
		}
	}

	/** 
	 * Returns the {@link TypeSystem} of the current {@link KnowledgeBase}
	 */
	protected TypeSystem typeSystem() {
		return (TypeSystem) kb().getMORepository();
	}

	/**
	 * Whether the {@link TypeSystem} does not have multiple branches.
	 */
	protected boolean noMultipleBranches() {
		return !typeSystem().multipleBranches();
	}

	protected static ObjectBranchId getObjectID(KnowledgeItem ko) {
		return new ObjectBranchId(ko.getBranchContext(), ko.tTable(), ko.getObjectName());
	}

	protected static ObjectBranchId getObjectID(Wrapper b1) {
		return new ObjectBranchId(b1.tHandle().getBranchContext(), b1.tTable(), KBUtils.getWrappedObjectName(b1));
	}

	protected static Test runOneTest(Class<? extends AbstractDBKnowledgeBaseTest> testClass,
			String testName) {
		return runOneTest(testClass, testName, DatabaseTestSetup.DEFAULT_DB);
	}

	protected static Test runOneTest(Class<? extends AbstractDBKnowledgeBaseTest> testClass,
			String testName, DBType database) {
		return runOneTest(testClass, Pattern.compile(Pattern.quote(testName)), database);
	}
	
	/**
	 * Runs all tests in the given test class that matches the given {@link Pattern}.
	 * 
	 * @param testClass
	 *        the class containing the tests.
	 * @param testNamePattern
	 *        the pattern to find the test to execute.
	 * @param database
	 *        the database to use.
	 */
	protected static Test runOneTest(final Class<? extends AbstractDBKnowledgeBaseTest> testClass,
			final Pattern testNamePattern, DBType database) {
		return suite(testClass, database, new SingleTestFactory(testNamePattern));
	}
	
	protected static Test suite(Class<? extends AbstractDBKnowledgeBaseTest> testCase, TestFactory f) {
		return suite(testCase, f, false);
	}

	protected static Test suite(Class<? extends AbstractDBKnowledgeBaseTest> testCase, TestFactory f,
			boolean needsBranches) {
		Test actualTest = DatabaseTestSetup.getDBTest(testCase, wrap(f));
		if (!DatabaseTestSetup.useOnlyDefaultDB() && !needsBranches) {
			TestSuite suite = new TestSuite();
			DBType db = DatabaseTestSetup.DEFAULT_DB;
			Test noBranchSetup = DatabaseTestSetup.getDBTest(testCase, db, wrap(f), db.toString() + " no branches");
			noBranchSetup = new MultipleBranchSetup(noBranchSetup, Decision.FALSE);
			suite.addTest(noBranchSetup);
			suite.addTest(actualTest);
			actualTest = suite;
		}
		return defaultTestSetup(testCase, actualTest);
	}

	protected static Test suite(Class<? extends AbstractDBKnowledgeBaseTest> testCase, DBType dbType, TestFactory f) {
		return defaultTestSetup(testCase, DatabaseTestSetup.getDBTest(testCase, dbType, wrap(f)));
	}

	@SuppressWarnings("unchecked")
	private static Test defaultTestSetup(Class<? extends AbstractDBKnowledgeBaseTest> testCase, Test dbTest) {
		Test test = ModuleLicenceTestSetup.wrap(dbTest);
		List<Class<?>> classes = new ArrayList<>();
		while (true) {
			classes.add(testCase);
			if (testCase == AbstractDBKnowledgeBaseTest.class) {
				break;
			}
			testCase = (Class<? extends AbstractDBKnowledgeBaseTest>) testCase.getSuperclass();
		}
		Class<?>[] customClassesArray = classes.toArray(new Class<?>[classes.size()]);
		ArrayUtil.reverse(customClassesArray);
		TestSetupDecorator decorator = CustomPropertiesDecorator.newDecorator(customClassesArray, true);
		test = new SimpleDecoratedTestSetup(decorator, test);
		return ModuleLicenceTestSetup.setupModuleWithoutAdditionalServices(test);
	}
	
	protected static Test suiteDefaultDB(Class<? extends AbstractDBKnowledgeBaseTest> testCase) {
		return suite(testCase, DatabaseTestSetup.DEFAULT_DB);
	}

	protected static Test suite(Class<? extends AbstractDBKnowledgeBaseTest> testCase, DBType dbType) {
		return suite(testCase, dbType, DefaultTestFactory.INSTANCE);
	}
	
	protected static Test suite(Class<? extends AbstractDBKnowledgeBaseTest> testCase) {
		return suite(testCase, DefaultTestFactory.INSTANCE);
	}
	
	protected static Test suiteNeedsBranches(Class<? extends AbstractDBKnowledgeBaseTest> testCase) {
		return suite(testCase, DefaultTestFactory.INSTANCE, true);
	}

	private static TestFactory wrap(TestFactory factory) {
		factory = ServiceTestSetup.createStarterFactoryForModules(factory, neededModules());
		factory = ClusterManagerSetup.setupClusterManager(factory);
		return factory;
	}

	/**
	 * the {@link BasicRuntimeModule}s needed to start a {@link KnowledgeBase}
	 */
	protected static BasicRuntimeModule<?>[] neededModules() {
		/* The correct version for getting the dependencies does not work here:
		 * ModuleUtil.INSTANCE.getDependencies(KnowledgeBaseFactory.Module.INSTANCE) If this is
		 * called, KnowledgeBaseFactory.Module.initModule() will be called, which calls
		 * ModuleUtil.inModuleContext(...), which starts the XMLProperties, which fails, as
		 * XMLProperties.Module.config is not set. */
		return getDependenciesFromAnnotation(KnowledgeBaseFactory.class);
	}

	private static BasicRuntimeModule<?>[] getDependenciesFromAnnotation(Class<? extends ManagedClass> module) {
		ServiceDependencies dependenciesAnnotation = module.getAnnotation(ServiceDependencies.class);
		if (dependenciesAnnotation == null) {
			throw new RuntimeException("Annotation for dependencies (" + ServiceDependencies.class.getSimpleName()
				+ ") not found on class " + module.getSimpleName() + ".");
		}
		return getSingletons(dependenciesAnnotation.value());
	}

	private static BasicRuntimeModule<?>[] getSingletons(Class<? extends BasicRuntimeModule<?>>[] dependencyClasses) {
		BasicRuntimeModule<?>[] moduleInstances = new BasicRuntimeModule[dependencyClasses.length];
		int i = 0;
		for (Class<? extends BasicRuntimeModule<?>> moduleClass : dependencyClasses) {
			moduleInstances[i] = getSingleton(moduleClass);
			i++;
		}
		return moduleInstances;
	}

	private static BasicRuntimeModule<?> getSingleton(Class<? extends BasicRuntimeModule<?>> moduleClass) {
		try {
			return ConfigUtil.getSingleton(moduleClass);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to get singleton instance of " + moduleClass + ". Cause: "
				+ ex.getMessage(), ex);
		}
	}
	/**
	 * asserts that the data on the given branches are the same
	 */
	protected static void assertBranchEquality(DBKnowledgeBase kb, Branch b1, Branch b2) {
		final Branch contextBranch = HistoryUtils.getHistoryManager(kb).getContextBranch();
		try {
			kb.setContextBranch(b1);
			Map<ObjectId, KnowledgeObject> objOn1 = getObjectsById(kb.getAllKnowledgeObjects());

			kb.setContextBranch(b2);
			Map<ObjectId, KnowledgeObject> objOn2 = getObjectsById(kb.getAllKnowledgeObjects());

			assertEquals(objOn1.keySet(), objOn2.keySet());

			for (ObjectId id : objOn2.keySet()) {
				KnowledgeObject koOn1 = objOn1.get(id);
				KnowledgeObject koOn2 = objOn2.get(id);

				final MetaObject correspondingMO = koOn1.tTable();
				assertEquals(correspondingMO, koOn2.tTable());

				Set<String> moAttributes = new HashSet<>();
				for (MOAttribute currentAttr : koOn1.getAttributes()) {
					String attr = currentAttr.getName();
					moAttributes.add(attr);
					if (currentAttr.isSystem()) {
						continue;
					}
					try {
						assertEquals("Attribute '" + attr + "' on '" + koOn1 + "' and '" + koOn2 + "'", 
								koOn1.getAttributeValue(attr), 
								koOn2.getAttributeValue(attr));
					} catch (NoSuchAttributeException ex) {
						fail(ex.getLocalizedMessage());
					}
				}

				{
					final Wrapper wrapper1 = WrapperFactory.getWrapper(koOn1);
					final Wrapper wrapper2 = WrapperFactory.getWrapper(koOn2);
					final Set<String> allAttributeNames = wrapper1.tHandle().getAllAttributeNames();
					for (String attr : allAttributeNames) {
						if (moAttributes.contains(attr)) {
							continue;
						}
						assertEquals("Attribute '" + attr + "' on '" + wrapper1 + "' and '" + wrapper2 + "'",
							wrapper1.getValue(attr),
							wrapper2.getValue(attr));
					}
					assertEquals(wrapper1.getClass(), wrapper2.getClass());
				}
			}

		} finally {
			kb.setContextBranch(contextBranch);
		}
	}

	protected static Map<ObjectId, KnowledgeObject> getObjectsById(Collection<?> knowledgeObjects) {
		Map<ObjectId, KnowledgeObject> objectsById = new HashMap<>();
		for (Object object: knowledgeObjects) {
			if (!(object instanceof KnowledgeObject)) {
				fail(object + " should be a KnowledgeObject");
			}
			KnowledgeObject ko = (KnowledgeObject) object;
			objectsById.put(new ObjectId(ko.tTable(), ko.getObjectName()), ko);
		}
		return objectsById;
	}

	/**
	 * Fetches row data from the database for the object identified by the given {@link ObjectKey}.
	 * Flex values are not reported.
	 * 
	 * <p>
	 * This method directly fetches data from the database, i.e. this method can be used to detect
	 * inconsistency in the {@link KnowledgeBase}.
	 * </p>
	 * 
	 * @param connection
	 *        The connection to connect to database.
	 * @param key
	 *        The Key identifying the object in a specific branch and history context to get data
	 *        for.
	 * 
	 * @see #getFlexData(PooledConnection, ObjectKey)
	 */
	public static ResultSet getRowData(PooledConnection connection, ObjectKey key) throws SQLException {
		String alias = "r";
		DBTableMetaObject type = (DBTableMetaObject) key.getObjectType();
		SQLExpression branchEQ;
		if (type.multipleBranches()) {
			branchEQ = eq(notNullColumn(alias, BasicTypes.BRANCH_DB_NAME), literalLong(key.getBranchContext()));
		} else {
			branchEQ = literalBooleanLogical(Boolean.TRUE);
		}
		SQLSelect select = select(
			columnDefs(alias, type),
			table(type, alias),
			and(
				branchEQ,
				eq(notNullColumn(alias, BasicTypes.IDENTIFIER_DB_NAME), literalID(key.getObjectName())),
				ge(notNullColumn(alias, BasicTypes.REV_MAX_DB_NAME), literalLong(key.getHistoryContext())),
				le(notNullColumn(alias, BasicTypes.REV_MIN_DB_NAME), literalLong(key.getHistoryContext()))));
		CompiledStatement sql = query(select).toSql(connection.getSQLDialect());
		return sql.executeQuery(connection);
	}

	/**
	 * Fetches flex data from the database for the object identified by the given {@link ObjectKey}.
	 * Row values are not reported.
	 * 
	 * <p>
	 * This method directly fetches data from the database, i.e. this method can be used to detect
	 * inconsistency in the {@link KnowledgeBase}.
	 * </p>
	 * 
	 * @param connection
	 *        The connection to connect to database.
	 * @param flexDataType
	 *        Type defining the flex data table.
	 * @param key
	 *        The Key identifying the object in a specific branch and history context to get data
	 *        for.
	 * 
	 * @see #getRowData(PooledConnection, ObjectKey)
	 */
	public static ResultSet getFlexData(PooledConnection connection, DBTableMetaObject flexDataType, ObjectKey key) throws SQLException {
		String alias = "f";
		SQLExpression branchEQ;
		if (flexDataType.multipleBranches()) {
			branchEQ = eq(notNullColumn(alias, AbstractFlexDataManager.BRANCH_DBNAME),
				literalLong(key.getBranchContext()));
		} else {
			branchEQ = literalBooleanLogical(Boolean.TRUE);
		}
		SQLSelect select = select(
			columnDefs(alias, flexDataType),
			table(flexDataType, alias),
			and(
				branchEQ,
				eq(notNullColumn(alias, AbstractFlexDataManager.TYPE_DBNAME),
					literalString(key.getObjectType().getName())),
				eq(notNullColumn(alias, AbstractFlexDataManager.IDENTIFIER_DBNAME),
					literalID(key.getObjectName())),
				ge(notNullColumn(alias, BasicTypes.REV_MAX_DB_NAME),
					literalLong(key.getHistoryContext())),
				le(notNullColumn(alias, BasicTypes.REV_MIN_DB_NAME),
					literalLong(key.getHistoryContext()))));
		CompiledStatement sql = query(select).toSql(connection.getSQLDialect());
		return sql.executeQuery(connection);
	}

	/**
	 * Returns the {@link ObjectKey} which is known by the given {@link KnowledgeBase}
	 */
	protected ObjectKey getKnownKey(KnowledgeBase kb, ObjectKey original) {
		KnowledgeItem cachedItem = kb.resolveCachedObjectKey(original);
		if (cachedItem == null) {
			return original;
		}
		return cachedItem.tId();
	}

	/**
	 * Fetches flex data from the database for the object identified by the given {@link ObjectKey},
	 * with the {@link AbstractFlexDataManager#FLEX_DATA} type from the default KnowledgeBase.
	 * 
	 * @see #getFlexData(PooledConnection, DBTableMetaObject, ObjectKey)
	 */
	public ResultSet getFlexData(PooledConnection connection, ObjectKey key) throws SQLException {
		MOKnowledgeItemImpl flexDataType = kb().lookupType(AbstractFlexDataManager.FLEX_DATA);
		return getFlexData(connection, flexDataType, key);
	}

	/**
	 * Checks whether the object identified by the given {@link ObjectKey} is loaded in the given
	 * {@link KnowledgeBase}.
	 */
	protected boolean isNotLoaded(KnowledgeBase kb, ObjectKey original) {
		return kb.resolveCachedObjectKey(original) == null;
	}

}
