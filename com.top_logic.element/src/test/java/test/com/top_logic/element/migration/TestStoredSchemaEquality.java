/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationService;

/**
 * Test that the configured application schema equals itself after it has been stored and read
 * back, in the way {@link MigrationService} compares the types of the stored and the configured
 * schema.
 *
 * <p>
 * The application schema of this module contains types with an attribute whose type is given by
 * name (e.g. <code>tType</code> with <code>att_type="MetaElement"</code>). Reading such a schema
 * creates new placeholder objects for those names, which have no value equality.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestStoredSchemaEquality extends TestCase {

	public void testStoredSchemaEqualsConfiguredSchema() throws Exception {
		SchemaConfiguration applicationSchema = applicationSchema();
		SchemaConfiguration storedSchema = KBSchemaUtil.parseSchema(KBSchemaUtil.serializeSchema(applicationSchema));

		Map<String, MetaObjectName> applicationTypes = applicationSchema.getMetaObjects().getTypes();
		Map<String, MetaObjectName> storedTypes = storedSchema.getMetaObjects().getTypes();

		assertTrue("Application schema has no types.", !applicationTypes.isEmpty());
		assertTrue("Application schema has no type 'MetaElement'.", applicationTypes.containsKey("MetaElement"));
		assertEquals(applicationTypes.keySet(), storedTypes.keySet());

		List<String> differentTypes = new ArrayList<>();
		for (Entry<String, MetaObjectName> entry : applicationTypes.entrySet()) {
			MetaObjectName applicationType = entry.getValue();
			MetaObjectName storedType = storedTypes.get(entry.getKey());
			if (!ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(storedType, applicationType)
				|| ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(storedType) != ConfigEquality.INSTANCE_ALL_BUT_DERIVED
					.hashCode(applicationType)) {
				differentTypes.add(entry.getKey());
			}
		}
		assertEquals("Types that differ after storing the schema.", List.of(), differentTypes);
	}

	private static SchemaConfiguration applicationSchema() throws Exception {
		KnowledgeBaseFactoryConfig factoryConfig = (KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
			.getServiceConfiguration(KnowledgeBaseFactory.class);
		KnowledgeBaseConfiguration kbConfig =
			factoryConfig.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		return KBUtils.getSchemaConfigResolved(kbConfig).getConfig();
	}

	/**
	 * A cumulative {@link Test} for all tests in {@link TestStoredSchemaEquality}.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestStoredSchemaEquality.class, DataAccessService.Module.INSTANCE));
	}

}
