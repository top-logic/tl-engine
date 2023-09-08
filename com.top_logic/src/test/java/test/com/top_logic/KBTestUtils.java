/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * Utilities to abstract testing-only features of different
 * {@link KnowledgeBase} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KBTestUtils {

	public static Object switchThreadContext(KnowledgeBase knowledgeBase, Object o1) {
		DBKnowledgeBase dbKb = (DBKnowledgeBase) knowledgeBase;

		return dbKb.switchThreadContext(o1);
	}

	/**
	 * {@link Mapping Map} {@link KnowledgeAssociation}s to their destination objects.
	 */
	public static final Mapping GET_TARGET = new Mapping() {
		@Override
		public Object map(Object input) {
			KnowledgeAssociation link = (KnowledgeAssociation) input;
			
			try {
				KnowledgeObject destinationObject = link.getDestinationObject();
				
				TestCase.assertTrue("Invalid target object: " + destinationObject, destinationObject.isAlive());
				
				return destinationObject;
			} catch (InvalidLinkException e) {
				throw new AssertionError(e);
			}
		}
		
	};

	/**
	 * {@link Mapping Map} {@link KnowledgeAssociation}s to their destination objects.
	 */
	public static final Mapping GET_SOURCE = new Mapping() {
		@Override
		public Object map(Object input) {
			KnowledgeAssociation link = (KnowledgeAssociation) input;
			
			try {
				KnowledgeObject sourceObject = link.getSourceObject();
				
				TestCase.assertTrue("Invalid source object: " + sourceObject, sourceObject.isAlive());
				
				return sourceObject;
			} catch (InvalidLinkException e) {
				throw new AssertionError(e);
			}
		}
		
	};
	
	public static void assertTargets(String msg, KnowledgeObject[] expectedTargetsArray, Iterator foundAssociations) {
		KBTestUtils.assertTargets(msg, Arrays.asList(expectedTargetsArray), foundAssociations);
	}
	
	public static void assertSources(String msg, KnowledgeObject[] expectedSourcesArray, Iterator foundAssociations) {
		KBTestUtils.assertSources(msg, Arrays.asList(expectedSourcesArray), foundAssociations);
	}

	public static void assertTargets(String msg, List expectedTargetList, Iterator foundAssociations) {
		assertTargets(msg, new HashSet(expectedTargetList), foundAssociations);
	}
	
	public static void assertSources(String msg, List expectedSourcesList, Iterator foundAssociations) {
		assertSources(msg, new HashSet(expectedSourcesList), foundAssociations);
	}

	public static void assertTargets(String msg, Set expectedTargets, Iterator foundAssociations) {
		assertSetEquals(msg, expectedTargets, new HashSet(getTargets(foundAssociations)));
	}
	
	public static void assertSources(String msg, Set expectedSources, Iterator foundAssociations) {
		assertSetEquals(msg, expectedSources, new HashSet(getSources(foundAssociations)));
	}

	protected static void assertSetEquals(String msg, Set expectedTargets,
			HashSet foundTargets) {
		boolean subset1 = expectedTargets.containsAll(foundTargets);
		boolean subset2 = foundTargets.containsAll(expectedTargets);
		
		if (subset1 && (! subset2)) {
			// Missing values.
			HashSet missingValues = new HashSet(expectedTargets);
			missingValues.removeAll(foundTargets);
			TestCase.fail((StringServices.isEmpty(msg) ? "" : msg + ": ") + "Missing values: " + missingValues);
		} else if (subset2 && (! subset1)) {
			// More than expected.
			HashSet unexpectedTargets = new HashSet(foundTargets);
			unexpectedTargets.removeAll(expectedTargets);
			TestCase.fail((StringServices.isEmpty(msg) ? "" : msg + ": ") + "Unexpected values: " + unexpectedTargets);
		} else {
			// Default check and default error message.
			TestCase.assertEquals(msg, expectedTargets, foundTargets);
		}
	}

	public static List getTargets(Iterator foundAssociations) {
		return getTargets(CollectionUtil.toList(foundAssociations));
	}

	public static List getTargets(List associationList) {
		return Mappings.map(GET_TARGET, associationList);
	}

	public static Set getTargetsSet(Iterator foundAssociations) {
		return new HashSet(getTargets(foundAssociations));
	}

	public static Set getTargetsSet(List foundAssociations) {
		return new HashSet(getTargets(foundAssociations));
	}
	
	public static List getSources(Iterator foundAssociations) {
		return getSources(CollectionUtil.toList(foundAssociations));
	}
	
	public static List getSources(List associationList) {
		return Mappings.map(GET_SOURCE, associationList);
	}
	
	public static Set getSourcesSet(Iterator foundAssociations) {
		return new HashSet(getSources(foundAssociations));
	}
	
	public static Set getSourcesSet(List foundAssociations) {
		return new HashSet(getSources(foundAssociations));
	}

	/**
	 * Resets the cache of the {@link KnowledgeBase}.
	 * 
	 * <p>
	 * This method removes all objects stored in the {@link KnowledgeBase} internal cache. If there
	 * is a reference to such an object outside the {@link KnowledgeBase}, the reference may seem
	 * correct but in fact it isn't because the {@link KnowledgeBase} will create a new instance for
	 * it.
	 * </p>
	 */
	public static void clearCache(KnowledgeBase kb) {
		ReflectionUtils.executeMethod(kb, "clearCacheForTests", new Class[0], ArrayUtil.EMPTY_OBJECT_ARRAY);
	}

}
