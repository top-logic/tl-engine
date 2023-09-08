/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.util;

import static com.top_logic.basic.col.FilterUtil.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.element.model.PersistentTLModel;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.TLCssClass;
import com.top_logic.model.cache.TLModelCacheEntry;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.model.cache.TLModelOperations;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * {@link Test}s for {@link TLModelUtil} with the {@link PersistentTLModel}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTLModelUtilPersistent extends AbstractTestTLModelUtil {

	private Revision _preSetUpRevision;

	@Override
	protected void extendModel(BinaryContent modelXml) {
		_preSetUpRevision = HistoryUtils.getSessionRevision();
		try (Transaction transaction = getKnowledgeBase().beginTransaction()) {
			super.extendModel(modelXml);
			transaction.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		revertChanges();
		super.tearDown();
	}

	private void revertChanges() {
		KBUtils.revert(getKnowledgeBase(), _preSetUpRevision);
	}

	/**
	 * Tests
	 * {@link TLModelUtil#isCompatibleType(com.top_logic.model.TLType, com.top_logic.model.TLType)}
	 * with types in different history contexts.
	 * 
	 * @see test.com.top_logic.element.model.util.AbstractTestTLModelUtil#testIsCompatibleType()
	 */
	@Override
	public void testIsCompatibleType() {
		super.testIsCompatibleType();

		TLClass typeA = type("ComplexTypeHierarchy:A");
		TLClass typeB = type("ComplexTypeHierarchy:B");
		TLClass typeC = type("ComplexTypeHierarchy:C");
		assertNull("Must be null to ensure consistency after cleanup.", typeA.getAnnotation(TLCssClass.class));
		assertNull("Must be null to ensure consistency after cleanup.", typeB.getAnnotation(TLCssClass.class));
		assertNull("Must be null to ensure consistency after cleanup.", typeC.getAnnotation(TLCssClass.class));

		Transaction tx1 = getKnowledgeBase().beginTransaction();
		TLCssClass cssAnnotation = TypedConfiguration.newConfigItem(TLCssClass.class);
		cssAnnotation.setValue("testCssClass");
		typeA.setAnnotation(cssAnnotation);
		typeB.setAnnotation(cssAnnotation);
		typeC.setAnnotation(cssAnnotation);
		tx1.commit();

		Transaction tx2 = getKnowledgeBase().beginTransaction();
		typeA.removeAnnotation(TLCssClass.class);
		typeB.removeAnnotation(TLCssClass.class);
		typeC.removeAnnotation(TLCssClass.class);
		tx2.commit();

		Revision tx1Revision = tx1.getCommitRevision();
		Revision tx2Revision = tx2.getCommitRevision();

		assertTrue("Class is assignment compatible to itself in different context.",
			TLModelUtil.isCompatibleType(inRevision(typeA, tx1Revision), inRevision(typeA, tx2Revision)));
		assertTrue("Class is assignment compatible to itself in different context.",
			TLModelUtil.isCompatibleType(inRevision(typeA, tx2Revision), inRevision(typeA, tx1Revision)));
		assertTrue("Class is assignment compatible to itself in different context.",
			TLModelUtil.isCompatibleType(inRevision(typeA, tx1Revision), typeA));
		assertTrue("Class is assignment compatible to itself in different context.",
			TLModelUtil.isCompatibleType(typeA, inRevision(typeA, tx2Revision)));
		assertTrue("Class is assignment compatible to itself.",
			TLModelUtil.isCompatibleType(inRevision(typeA, tx2Revision), inRevision(typeA, tx2Revision)));

		assertTrue("Class is assignment compatible to direct supertype in same history contexts.",
			TLModelUtil.isCompatibleType(inRevision(typeA, tx2Revision), inRevision(typeB, tx2Revision)));
		assertTrue("Class is assignment compatible to direct supertype in different contexts.",
			TLModelUtil.isCompatibleType(inRevision(typeA, tx2Revision), inRevision(typeB, tx1Revision)));
		assertTrue("Class is assignment compatible to supertype of supertype in different contexts.",
			TLModelUtil.isCompatibleType(inRevision(typeA, tx1Revision), inRevision(typeC, tx2Revision)));
	}

	/* The computations in the TLModelCache are already tested in AbstractTestTLModelUtil. As long
	 * as the TLModelCacheService is active during these tests, there is no need to test that again.
	 * And that is what these tests do: They make sure the cache is active and actually caching. */

	public void testCacheSuperClasses() {
		TLClass exampleType = type("test1:H");
		Set<TLClass> firstResult = cache().getSuperClasses(exampleType);
		Set<TLClass> secondResult = cache().getSuperClasses(exampleType);
		assertSame("The TLModel cache is either not active, not caching this method or creates unnecessary objects.",
			firstResult, secondResult);
		/* Test that the collections above are not just the collections from
		 * "exampleType.getGeneralizations()". */
		assertTrue("The test needs to be updated, as there needs to be indirect super types of the test type.",
			exampleType.getGeneralizations().size() < firstResult.size());
	}

	public void testCacheSuperClassesAfterUpdate() {
		TLClass baseType = type("test1:X");
		TLClass firstSuperType = type("test1:Y");
		TLClass secondSuperType = type("test1:Z");

		try (Transaction transaction = getKnowledgeBase().beginTransaction()) {
			List<TLClass> generalizations = baseType.getGeneralizations();

			assertTrue(generalizations.isEmpty());
			assertTrue(generalizations.add(firstSuperType));
			assertEquals(set(baseType, firstSuperType), cache().getSuperClasses(baseType));
			assertTrue(generalizations.remove(firstSuperType));
			assertTrue(generalizations.add(secondSuperType));
			assertEquals(set(baseType, secondSuperType), cache().getSuperClasses(baseType));
			assertTrue(generalizations.remove(secondSuperType));

			transaction.commit();
		}
	}

	public void testCacheSubClasses() {
		TLClass exampleType = type("test1:A");
		Set<TLClass> firstResult = cache().getSubClasses(exampleType);
		Set<TLClass> secondResult = cache().getSubClasses(exampleType);
		assertSame("The TLModel cache is either not active, not caching this method or creates unnecessary objects.",
			firstResult, secondResult);
		/* Test that the collections above are not just the collections from
		 * "exampleType.getSpecializations()". */
		assertTrue("The test needs to be updated, as there needs to be indirect sub types of the test type.",
			exampleType.getSpecializations().size() < firstResult.size());
	}

	public void testCachePotentialTables() {
		TLClass exampleType = type("test1:A");
		Map<String, ? extends Set<TLClass>> firstResult = cache().getPotentialTables(exampleType);
		Map<String, ? extends Set<TLClass>> secondResult = cache().getPotentialTables(exampleType);
		assertSame("The TLModel cache is either not active, not caching this method or creates unnecessary objects.",
			firstResult, secondResult);
		assertFalse(firstResult.isEmpty());
		/* No need to test for indirect subtypes like above, as there is no field or getter for the
		 * potential tables of the direct subtypes that might cache the result. */
	}

	public void testCacheAttributesOfSubClasses() {
		TLClass exampleType = type("ComplexTypeHierarchy:A");
		Set<TLClassPart> firstResult = cache().getAttributesOfSubClasses(exampleType);
		Set<TLClassPart> secondResult = cache().getAttributesOfSubClasses(exampleType);
		assertSame("The TLModel cache is either not active, not caching this method or creates unnecessary objects.",
			firstResult, secondResult);
		assertFalse(firstResult.isEmpty());
		/* No need to test for indirect subtypes like above, as there is no field or getter for the
		 * attributes of the direct subtypes that might cache the result. */
	}

	public void testAttributesOfSubClasses() {
		TLClass exampleType = type("testGetClassParts:B");
		assertTrue("Test of #26309 need local attributes.", exampleType.getLocalParts() != null);
		Set<TLClassPart> firstResult = TLModelCacheService.getOperations().getAttributesOfSubClasses(exampleType);
		Collection<TLStructuredTypePart> attributesOfSubClasses = localParts(computeSubClasses(exampleType));
		assertEquals(toSet(attributesOfSubClasses), firstResult);
	}

	private Collection<TLClass> computeSubClasses(TLClass type) {
		Collection<TLClass> result = set();
		Collection<TLClass> specializations = type.getSpecializations();
		result.addAll(specializations);
		specializations.forEach(specialisation -> result.addAll(computeSubClasses(specialisation)));
		return result;
	}

	private Collection<TLStructuredTypePart> localParts(Collection<TLClass> types) {
		Collection<TLStructuredTypePart> result = set();
		types.forEach(type -> result.addAll(type.getLocalParts()));
		return result;
	}

	public void testSuperClassesAreUnmodifiable() {
		Set<TLClass> firstResult = cache().getSuperClasses(type("test1:H"));
		assertUnmodifiable(firstResult);
	}

	public void testSubClassesAreUnmodifiable() {
		Set<TLClass> firstResult = cache().getSubClasses(type("test1:A"));
		assertUnmodifiable(firstResult);
	}

	public void testPotentialTablesAreUnmodifiable() {
		Map<String, ? extends Set<TLClass>> firstResult = cache().getPotentialTables(type("test1:A"));
		assertUnmodifiable(firstResult);
		assertUnmodifiable(firstResult.entrySet());
		assertUnmodifiable(firstResult.keySet());
		assertUnmodifiable(firstResult.values());
		Set<Set<TLClass>> nonTrivialValueSets = filterSet(valueSet -> valueSet.size() > 1, firstResult.values());
		Set<TLClass> valueSet = CollectionUtil.getFirst(nonTrivialValueSets);
		assertUnmodifiable(valueSet);
	}

	public void testAttributesOfSubClassesAreUnmodifiable() {
		Set<TLClass> firstResult = cache().getSubClasses(type("ComplexTypeHierarchy:A"));
		assertUnmodifiable(firstResult);
	}

	public void testCacheInitializationBeforeTransactionStart() {
		testCacheUpdateOnRelevantChange(true, true, true);
	}

	public void testCacheInitializationAfterTransactionStart() {
		testCacheUpdateOnRelevantChange(false, true, true);
	}

	public void testCacheInitializationAfterChange() {
		testCacheUpdateOnRelevantChange(false, false, true);
	}

	public void testCacheInitializationBeforeTransactionStartButRollback() {
		testCacheUpdateOnRelevantChange(true, true, false);
	}

	public void testCacheInitializationAfterTransactionStartButRollback() {
		testCacheUpdateOnRelevantChange(false, true, false);
	}

	public void testCacheInitializationAfterChangeButRollback() {
		testCacheUpdateOnRelevantChange(false, false, false);
	}

	private void testCacheUpdateOnRelevantChange(boolean initBeforeTransaction, boolean initBeforeChange,
			boolean commit) {
		String newClassName = "DynamicSubClass";
		TLClass superClass = type("ComplexTypeHierarchy:A");
		Set<TLClass> originalSubClasses = set(
			type("ComplexTypeHierarchy:A"),
			type("ComplexTypeHierarchy:B"),
			type("ComplexTypeHierarchy:C"),
			type("ComplexTypeHierarchy:D"),
			type("ComplexTypeHierarchy:E"),
			type("ComplexTypeHierarchy:F"),
			type("ComplexTypeHierarchy:G"));
		if (initBeforeTransaction) {
			assertFalse("The super class needs sub classes, or the test cannot detect a value that is falsely empty.",
				cache().getSubClasses(superClass).isEmpty());
		} else {
			/* The cache might already have been initialized. Undo that. */
			((TLModelCacheEntry) cache()).clear();
		}
		try (Transaction transaction = getKnowledgeBase().beginTransaction()) {
			if (initBeforeChange) {
				assertEquals("The cache value is wrong after beginning a transaction.",
					originalSubClasses, cache().getSubClasses(superClass));
			}
			TLClass newClass = TLModelUtil.addClass(module("ComplexTypeHierarchy"), newClassName);
			newClass.getGeneralizations().add(superClass);
			assertTrue("The cache value is wrong after changes were being made in the current transaction.",
				cache().getSubClasses(superClass).containsAll(originalSubClasses));
			assertTrue("The cache value does not contain the new changes from the current transaction.",
				cache().getSubClasses(superClass).contains(newClass));
			if (commit) {
				transaction.commit();
			} else {
				transaction.rollback();
			}
		}
		assertTrue("The cache value is wrong after changes were being made and committed.",
			cache().getSubClasses(superClass).containsAll(originalSubClasses));
		if (commit) {
			assertTrue("The cache value does not contain the new changes after they were committed.",
				cache().getSubClasses(superClass).contains(type("ComplexTypeHierarchy", newClassName)));
		} else {
			/* It is not possible to use "contains" here, as the new type does no longer exist after
			 * the rollback. */
			assertEquals("The cache value contains the new changes, even though they were rolled back.",
				originalSubClasses.size(), cache().getSubClasses(superClass).size());
		}
	}

	private TLModelOperations cache() {
		return TLModelCacheService.getOperations();
	}

	private void assertUnmodifiable(Collection<?> collection) {
		assertTrue("There have to be at least two entries in the result"
			+ " to make sure this is not one of the optimized Collections (empty and singleton)"
			+ " and unmodifiable only for size 0 or 1.",
			collection.size() > 1);
		try {
			collection.clear();
		} catch (UnsupportedOperationException exception) {
			/* Correct */
			return;
		}
		fail("The cache values have to be unmodifiable to prevent side effect changes from corrupting the cache.");
	}

	/**
	 * This is a copy of {@link #assertUnmodifiable(Collection)}, as there is no super type of
	 * {@link Map} and {@link Collection} in which "size()" and "clear()" are declared.
	 */
	private void assertUnmodifiable(Map<?, ?> map) {
		assertTrue("There have to be at least two entries in the result"
			+ " to make sure this is not one of the optimized Collections (empty and singleton)"
			+ " and unmodifiable only for size 0 or 1.",
			map.size() > 1);
		try {
			map.clear();
		} catch (UnsupportedOperationException exception) {
			/* Correct */
			return;
		}
		fail("The cache values have to be unmodifiable to prevent side effect changes from corrupting the cache.");
	}

	@Override
	protected TLModel setUpModel() {
		return TLModelTestUtil.createTLModelInTransaction(getKnowledgeBase());
	}

	@Override
	protected TLFactory setUpFactory() {
		return new DefaultModelFactory();
	}

	@Override
	protected void tearDownModel() {
		/* nothing to do. Reverting the changes in the KnowledgeBase does not work, as reverting
		 * type-creations is not possible. Additionally, reverting slows down the test from 2
		 * seconds to 30 seconds and causes NPEs during the teardown, caused by MetaElements without
		 * scope. */
	}

	private KnowledgeBase getKnowledgeBase() {
		return PersistencyLayer.getKnowledgeBase();
	}

	public static Test suite() {
		Test modelTest = ServiceTestSetup.createSetup(TestTLModelUtilPersistent.class, ModelService.Module.INSTANCE);
		Test kbTest = KBSetup.getSingleKBTest(modelTest);
		return TestUtils.doNotMerge(kbTest);
	}

}
