/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.event.convert;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.DBKnowledgeBaseTestSetup;

import com.top_logic.basic.Log;
import com.top_logic.basic.StringID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.CachingEventWriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.BranchModificator;
import com.top_logic.knowledge.event.convert.BranchModificator.BranchAttributes;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * The class {@link TestBranchModificator} tests {@link BranchModificator}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestBranchModificator extends AbstractDBKnowledgeBaseTest {

	/**
	 * Name of a type with an attribute {@link #BRANCH_ATTR} representing a branch
	 */
	protected static final String WITH_BRANCH_ATTR = "withBranchAttr";

	/**
	 * Name of an attribute representing a branch
	 */
	protected static final String BRANCH_ATTR = "branchAttr";

	/** @see BranchModificator */
	private static final BranchAttributes BRANCH_ATTRS = new BranchAttributes() {

		@Override
		public Set<String> map(String type) {
			if (WITH_BRANCH_ATTR.equals(type)) {
				return Collections.singleton(BRANCH_ATTR);
			}
			return null;
		}
	};

	MetaObject getTypeWithBranchAttr() {
		try {
			return kb().getMORepository().getMetaObject(WITH_BRANCH_ATTR);
		} catch (UnknownTypeException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseTestSetup setup = new DBKnowledgeBaseTestSetup(self);
		setup.addAdditionalTypes(additionalTypeProviders());
		return setup;
	}

	private static TypeProvider additionalTypeProviders() {
		return new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				MOClass type = new MOKnowledgeItemImpl(WITH_BRANCH_ATTR);
				type.setSuperclass(BasicTypes.getKnowledgeObjectType(typeRepository));
				try {
					type.addAttribute(new MOAttributeImpl(BRANCH_ATTR, MOPrimitive.LONG, false));
				} catch (DuplicateAttributeException ex) {
					throw new UnreachableAssertion("Single attribute " + BRANCH_ATTR);
				}
				try {
					typeRepository.addMetaObject(type);
				} catch (DuplicateTypeException ex) {
					log.error("Unable to add " + type.getName(), ex);
				}
			}
		};
	}

	/**
	 * Tests that the old values of {@link ItemUpdate} with "branch attributes" are correct
	 */
	public void testCorrectOldValues() {
		
		BranchModificator brcMod = new BranchModificator(BRANCH_ATTRS);
		
		CachingEventWriter out = new CachingEventWriter();
		ObjectBranchId id = new ObjectBranchId(1, getTypeWithBranchAttr(), StringID.valueOf("someName"));
		
		brcMod.rewrite(new ChangeSet(2).add(new ObjectCreation(2, id)), out);
		brcMod.increaseBranchID();
		brcMod.rewrite(new ChangeSet(3).add(new BranchEvent(3, 2, 1, 2)), out);
		
		ItemUpdate updateWithoutOldValues = new ItemUpdate(3, id, false);
		updateWithoutOldValues.setValue(BRANCH_ATTR, null, 2L);
		brcMod.rewrite(new ChangeSet(3).add(updateWithoutOldValues), out);

		ItemUpdate updateWithOldValues = new ItemUpdate(3, id, true);
		updateWithOldValues.setValue(BRANCH_ATTR, 2L, 1L);
		brcMod.rewrite(new ChangeSet(3).add(updateWithOldValues), out);

		// Inserted branch between 1 and 2
		final List<ChangeSet> allEvents = out.getAllEvents();
		assertEquals(1L, allEvents.get(0).getCreations().get(0).getOwnerBranch());

		assertEquals(2 + 1L, allEvents.get(1).getBranchEvents().get(0).getBranchId());
		assertEquals(1L, allEvents.get(1).getBranchEvents().get(0).getBaseBranchId());

		assertEquals(2 + 1L, allEvents.get(2).getUpdates().get(0).getValues().get(BRANCH_ATTR));
		assertEquals(null, allEvents.get(2).getUpdates().get(0).getOldValues());

		assertEquals(1L, allEvents.get(3).getUpdates().get(0).getValues().get(BRANCH_ATTR));
		assertEquals(2 + 1L, allEvents.get(3).getUpdates().get(0).getOldValues().get(BRANCH_ATTR));
	}

	/**
	 * Creates a test running all tests in {@link TestBranchModificator}
	 */
	public static Test suite() {
		return suite(TestBranchModificator.class);
	}
}

