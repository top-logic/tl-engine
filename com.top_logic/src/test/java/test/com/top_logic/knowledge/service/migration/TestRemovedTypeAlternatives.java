/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import org.w3c.dom.Document;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.schema.config.AlternativeConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.RemovedTypeAlternatives;

/**
 * Test case for {@link RemovedTypeAlternatives}.
 */
public class TestRemovedTypeAlternatives extends TestCase {

	/**
	 * Stored schema with alternatives over the types <code>Folder</code>, <code>Item</code> and
	 * <code>Other</code>.
	 *
	 * <ul>
	 * <li><code>FolderContent</code> lists only types removed in the test.</li>
	 * <li><code>Mixed</code> lists a removed and a remaining type.</li>
	 * <li><code>Nested</code> lists only the alternative <code>FolderContent</code>.</li>
	 * <li><code>Outer</code> lists the alternative <code>FolderContent</code> and a remaining
	 * type.</li>
	 * </ul>
	 */
	private static final String SCHEMA = """
		<schema xmlns:config="http://www.top-logic.com/ns/config/6.0"
			config:interface="com.top_logic.basic.db.schema.setup.config.SchemaConfiguration"
		>
			<meta-objects>
				<metaobject object_name="Folder"/>
				<metaobject object_name="Item" abstract="true"/>
				<metaobject object_name="Other"/>
				<alternative object_name="FolderContent">
					<specialisation name="Folder"/>
					<specialisation name="Item"/>
				</alternative>
				<alternative object_name="Mixed">
					<specialisation name="Folder"/>
					<specialisation name="Other"/>
				</alternative>
				<alternative object_name="Nested">
					<specialisation name="FolderContent"/>
				</alternative>
				<alternative object_name="Outer">
					<specialisation name="FolderContent"/>
					<specialisation name="Other"/>
				</alternative>
			</meta-objects>
		</schema>
		""";

	private static final Set<String> REMOVED = Set.of("Folder", "Item");

	/**
	 * Cleanup of the typed schema.
	 */
	public void testTypedSchema() throws ConfigurationException {
		SchemaConfiguration schema = KBSchemaUtil.parseSchema(SCHEMA);
		Map<String, MetaObjectName> types = schema.getMetaObjects().getTypes();
		types.keySet().removeAll(REMOVED);

		Set<String> removedAlternatives = RemovedTypeAlternatives.cleanup(schema.getMetaObjects(), REMOVED, log());

		assertEquals(Set.of("FolderContent", "Nested"), removedAlternatives);
		assertEquals(Set.of("Other", "Mixed", "Outer"), types.keySet());
		assertEquals(Set.of("Other"), specialisations(types, "Mixed"));
		assertEquals(Set.of("Other"), specialisations(types, "Outer"));
	}

	/**
	 * Cleanup of the XML of the schema, as stored in the database.
	 */
	public void testStoredSchemaXml() throws ConfigurationException {
		SchemaConfiguration typed = KBSchemaUtil.parseSchema(SCHEMA);
		typed.getMetaObjects().getTypes().keySet().removeAll(REMOVED);
		Document schema = DOMUtil.parse(KBSchemaUtil.serializeSchema(typed));

		Set<String> removedAlternatives = RemovedTypeAlternatives.cleanup(schema, REMOVED, log());

		assertEquals(Set.of("FolderContent", "Nested"), removedAlternatives);
		SchemaConfiguration cleaned = KBSchemaUtil.parseSchema(DOMUtil.toString(schema));
		Map<String, MetaObjectName> types = cleaned.getMetaObjects().getTypes();
		assertEquals(Set.of("Other", "Mixed", "Outer"), types.keySet());
		assertEquals(Set.of("Other"), specialisations(types, "Mixed"));
		assertEquals(Set.of("Other"), specialisations(types, "Outer"));
	}

	/**
	 * Alternatives not referencing a removed type are left unchanged.
	 */
	public void testNothingRemoved() throws ConfigurationException {
		SchemaConfiguration schema = KBSchemaUtil.parseSchema(SCHEMA);

		Set<String> removedAlternatives = RemovedTypeAlternatives.cleanup(schema.getMetaObjects(), Set.of(), log());

		assertEquals(Set.of(), removedAlternatives);
		Map<String, MetaObjectName> types = schema.getMetaObjects().getTypes();
		assertEquals(Set.of("Folder", "Item"), specialisations(types, "FolderContent"));
		assertEquals(Set.of("FolderContent"), specialisations(types, "Nested"));
	}

	private static Set<String> specialisations(Map<String, MetaObjectName> types, String alternative) {
		return ((AlternativeConfig) types.get(alternative)).getSpecialisations()
			.stream()
			.map(AlternativeConfig.TypeChoice::getName)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	private static Log log() {
		return new LogProtocol(TestRemovedTypeAlternatives.class);
	}

	/**
	 * Suite of {@link TestRemovedTypeAlternatives}.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestRemovedTypeAlternatives.class, TypeIndex.Module.INSTANCE));
	}

}
