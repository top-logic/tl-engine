/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.setup.config.KeyAttributes;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.ItemEventReader;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * Test for {@link KeyAttributes}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestKeyAttributes extends AbstractDBKnowledgeBaseTest {

	static final String B_NAME_EXT = "b_extension";

	static final String C_NAME_EXT = "C_extension";

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseTestSetup localSetup = (DBKnowledgeBaseTestSetup) super.createSetup(self);
		localSetup.addAdditionalTypes(new TypeProvider() {

			@Override
			public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
				MOKnowledgeItemImpl bExtension = addBExtension(log, typeRepository);
				addCExtension(log, typeRepository, bExtension);
			}

			private MOKnowledgeItemImpl addCExtension(Log log, MORepository typeRepository,
					MOKnowledgeItemImpl superClass) {
				MOKnowledgeItemImpl cExtension = new MOKnowledgeItemImpl(C_NAME_EXT);
				cExtension.setSuperclass(superClass);
				KeyAttributes keyAttributes = TypedConfiguration.newConfigItem(KeyAttributes.class);
				keyAttributes.getAttributes().addAll(set("b1", "b2"));
				cExtension.addAnnotation(keyAttributes);
				addType(log, typeRepository, cExtension);
				return cExtension;
			}

			private MOKnowledgeItemImpl addBExtension(Log log, MORepository typeRepository) {
				MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(B_NAME_EXT);
				type.setSuperclass(new DeferredMetaObject(B_NAME));
				KeyAttributes keyAttributes = TypedConfiguration.newConfigItem(KeyAttributes.class);
				keyAttributes.getAttributes().add("b1");
				type.addAnnotation(keyAttributes);
				addType(log, typeRepository, type);
				return type;
			}

			private void addType(Log log, MORepository typeRepository, MOKnowledgeItemImpl type) {
				try {
					typeRepository.addMetaObject(type);
				} catch (DuplicateTypeException ex) {
					log.error("Duplicate type " + type.getName(), ex);
				}
			}
		});
		return localSetup;
	}

	public void testCorrectDataSetup() {
		assertTrue("Subtypeing is needed for correct test of more special key attributes",
			type(C_NAME_EXT).isSubtypeOf(type(B_NAME_EXT)));
	}

	public void testKeyAttributeInEvent() throws SQLException, DataObjectException {
		final KnowledgeObject b1;
		{
			/* Create an Object which is modified later */
			Transaction tx = begin();
			b1 = newA(B_NAME_EXT, "b");
			setB1(b1, "b1Value");
			setB2(b1, "b2Value");
			tx.commit();
		}

		Revision r1;
		{
			Transaction tx = begin();
			setA1(b1, "bNew");
			tx.commit();
			r1 = tx.getCommitRevision();
		}

		ItemEventReader reader = detailedEventReader(r1, lastRevision());
		try {
			ItemEvent evt = reader.readEvent();
			assertInstanceof(evt, ItemUpdate.class);
			ItemUpdate update = (ItemUpdate) evt;
			Map<String, Object> newValues = update.getValues();
			Map<String, Object> oldValues = update.getOldValues();
			assertEquals("Key property is contained also on no change.", "b1Value", newValues.get(B1_NAME));
			assertEquals("Key property is contained also on no change.", "b1Value", oldValues.get(B1_NAME));
			assertFalse("Non key attribute has not changed", newValues.containsKey(B2_NAME));
			assertFalse("Non key attribute has not changed", oldValues.containsKey(B2_NAME));
			assertNull(reader.readEvent());
		} finally {
			reader.close();
		}
	}

	public void testMoreSpecialKeyAttributeInEvent() throws SQLException, DataObjectException {
		final KnowledgeObject c1;
		{
			/* Create an Object which is modified later */
			Transaction tx = begin();
			c1 = newA(C_NAME_EXT, "c");
			setB1(c1, "b1Value");
			setB2(c1, "b2Value");
			tx.commit();
		}
		
		Revision r1;
		{
			Transaction tx = begin();
			setA1(c1, "cNew");
			tx.commit();
			r1 = tx.getCommitRevision();
		}
		
		ItemEventReader reader = detailedEventReader(r1, lastRevision());
		try {
			ItemEvent evt = reader.readEvent();
			assertInstanceof(evt, ItemUpdate.class);
			ItemUpdate update = (ItemUpdate) evt;
			Map<String, Object> newValues = update.getValues();
			Map<String, Object> oldValues = update.getOldValues();
			assertEquals("Key property is contained also on no change.", "b2Value", newValues.get(B2_NAME));
			assertEquals("Key property is contained also on no change.", "b2Value", oldValues.get(B2_NAME));
			assertNull(reader.readEvent());
		} finally {
			reader.close();
		}
	}

	private ItemEventReader detailedEventReader(Revision startRev, Revision stopRev) throws SQLException {
		return new ItemEventReader(kb(), true, startRev.getCommitNumber(), stopRev.getCommitNumber() + 1);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestKeyAttributes}.
	 */
	public static Test suite() {
		return suite(TestKeyAttributes.class);
	}

}

