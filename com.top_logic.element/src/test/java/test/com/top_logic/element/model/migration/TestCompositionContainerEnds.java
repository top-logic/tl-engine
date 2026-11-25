/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.LongID;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.TypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;

/**
 * Test of the {@link TLStructuredTypeColumns#SELF_ASSOCIATION_END_NAME own end} of the
 * associations of references created by a migration.
 */
@SuppressWarnings("javadoc")
public class TestCompositionContainerEnds extends AbstractLinkTableMigrationTest {

	private static final String MODEL_MODULE = "test.composition.ends";

	private static final String CONTAINER = MODEL_MODULE + ":Container";

	private static final String PART = MODEL_MODULE + ":Part";

	private static final String CONTAINER_CLASS = "Container";

	private static final String PART_CLASS = "Part";

	private static final String PARTS = "parts";

	private static final String CONTAINER_REFERENCE = "container";

	private static final String REFERENCED = "referenced";

	private static final String REFERRER = "referrer";

	private static final String LINKED = "linked";

	private static final String LINKERS = "linkers";

	private static final String TL_OBJECT = "tl.model:TLObject";

	private static final long TRUNK = TLContext.TRUNK_ID;

	/**
	 * The own end of a composition created by a migration is single and an aggregate, also after
	 * an inverse reference declared as mandatory and multiple is created for it, so that the
	 * inverse reference answers the container of a part. The own end of a non-composite reference
	 * is multiple and takes the multiplicity of its inverse reference.
	 */
	public void testCreateComposition() throws Exception {
		String baseline = readBaseline();
		try {
			migrate(processors(migration(createReferences())), null);

			assertEquals(end(false, false, true, false), selfEnd(CONTAINER_CLASS, PARTS));
			assertEquals(end(false, true, false, false), selfEnd(CONTAINER_CLASS, REFERENCED));
			assertEquals(end(false, true, false, false), selfEnd(CONTAINER_CLASS, LINKED));

			migrate(processors(migration(createInverseReferences())), null);

			assertEquals("Back of a composition takes neither multiplicity nor mandatory from the inverse.",
				end(false, false, true, false), selfEnd(CONTAINER_CLASS, PARTS));
			assertEquals("Inverse of a non-composite reference defines the own end.",
				end(true, false, false, false), selfEnd(CONTAINER_CLASS, REFERENCED));
			assertEquals(end(false, true, false, false), selfEnd(CONTAINER_CLASS, LINKED));

			TLReference container = reference(PART_CLASS, CONTAINER_REFERENCE);
			assertFalse(TLModelUtil.isForwardReference(container));
			assertFalse("Backwards reference of a composition must answer the container.", container.isMultiple());
			assertTrue(container.getEnd().isAggregate());
			assertFalse(container.getEnd().isComposite());

			TLReference referrer = reference(PART_CLASS, REFERRER);
			assertFalse(TLModelUtil.isForwardReference(referrer));
			assertFalse(referrer.isMultiple());
			assertFalse(referrer.getEnd().isAggregate());

			TLReference linkers = reference(PART_CLASS, LINKERS);
			assertTrue(linkers.isMultiple());

			assertValues(reference(CONTAINER_CLASS, PARTS), reference(CONTAINER_CLASS, REFERENCED),
				reference(CONTAINER_CLASS, LINKED), container, referrer, linkers);
		} finally {
			migrate(processors(migration("<delete-module name=\"" + MODEL_MODULE + "\"/>")), null);
			storeBaseline(baseline);
		}
	}

	/**
	 * Creates a container with a part referenced by all forwards references and checks the values
	 * of the inverse references of the part.
	 */
	private void assertValues(TLReference parts, TLReference referenced, TLReference linked,
			TLReference container, TLReference referrer, TLReference linkers) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLObject containerObject;
		TLObject partObject;
		try (Transaction tx = kb.beginTransaction()) {
			containerObject = newObject(kb, parts.getOwner());
			partObject = newObject(kb, parts.getType());
			containerObject.tUpdate(parts, Collections.singleton(partObject));
			containerObject.tUpdate(referenced, Collections.singleton(partObject));
			containerObject.tUpdate(linked, Collections.singleton(partObject));
			tx.commit();
		}
		try {
			assertSame("Inverse of a composition answers the container.", containerObject,
				partObject.tValue(container));
			assertSame("Single inverse of a non-composite reference answers the referrer.", containerObject,
				partObject.tValue(referrer));
			assertEquals("Multiple inverse of a non-composite reference answers a collection.",
				Collections.singletonList(containerObject),
				new ArrayList<>((Collection<?>) partObject.tValue(linkers)));
		} finally {
			try (Transaction tx = kb.beginTransaction()) {
				partObject.tDelete();
				containerObject.tDelete();
				tx.commit();
			}
		}
	}

	/**
	 * Creates an object of the given type in the table of the types without own table.
	 *
	 * <p>
	 * The factory of the model module is not used, since the model module of the test is created
	 * by a migration in the running application.
	 * </p>
	 */
	private static TLObject newObject(KnowledgeBase kb, TLType type) {
		NameValueBuffer values = new NameValueBuffer();
		values.setValue(PersistentObject.TYPE_REF, type.tHandle());
		return WrapperFactory.getWrapper(kb.createKnowledgeObject(TLAnnotations.GENERIC_TABLE_NAME, values));
	}

	private static String createReferences() {
		return "<create-module name=\"" + MODEL_MODULE + "\"/>"
			+ "<create-class name=\"" + CONTAINER + "\" primary-generalization=\"" + TL_OBJECT + "\"/>"
			+ "<create-class name=\"" + PART + "\" primary-generalization=\"" + TL_OBJECT + "\"/>"
			+ "<create-reference name=\"" + CONTAINER + "#" + PARTS + "\" type=\"" + PART + "\""
			+ " composite=\"true\" multiple=\"true\"/>"
			+ "<create-reference name=\"" + CONTAINER + "#" + REFERENCED + "\" type=\"" + PART + "\""
			+ " multiple=\"true\"/>"
			+ "<create-reference name=\"" + CONTAINER + "#" + LINKED + "\" type=\"" + PART + "\""
			+ " multiple=\"true\"/>";
	}

	private static String createInverseReferences() {
		return "<create-inverse-reference name=\"" + PART + "#" + CONTAINER_REFERENCE + "\""
			+ " inverse-reference=\"" + CONTAINER + "#" + PARTS + "\" mandatory=\"true\" multiple=\"true\"/>"
			+ "<create-inverse-reference name=\"" + PART + "#" + REFERRER + "\""
			+ " inverse-reference=\"" + CONTAINER + "#" + REFERENCED + "\" mandatory=\"true\"/>"
			+ "<create-inverse-reference name=\"" + PART + "#" + LINKERS + "\""
			+ " inverse-reference=\"" + CONTAINER + "#" + LINKED + "\" multiple=\"true\"/>";
	}

	/**
	 * Reads a migration with the given processors.
	 */
	private static MigrationConfig migration(String processors) throws ConfigurationException {
		String migrationXML =
			"<" + MigrationUtil.ROOT_TAG_NAME + ">"
				+ "<version name=\"test\" module=\"test\"/>"
				+ "<processors>" + processors + "</processors>"
				+ "</" + MigrationUtil.ROOT_TAG_NAME + ">";
		InstantiationContext context = new DefaultInstantiationContext(TestCompositionContainerEnds.class);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(MigrationConfig.class);
		MigrationConfig migration = (MigrationConfig) new ConfigurationReader(context,
			Collections.singletonMap(MigrationUtil.ROOT_TAG_NAME, descriptor))
				.setSource(CharacterContents.newContent(migrationXML)).read();
		context.checkErrors();
		return migration;
	}

	/**
	 * The flags of the own end of the association of the given forwards reference.
	 */
	private Map<String, Boolean> selfEnd(String ownerClass, String forwardsReference) throws Exception {
		TypePart[] end = { null };
		migrate(noProcessor(), (context, log, connection) -> {
			end[0] = context.getSQLUtils().getTLTypePartOrFail(connection, TRUNK, MODEL_MODULE,
				TLStructuredTypeColumns.syntheticAssociationName(ownerClass, forwardsReference),
				TLStructuredTypeColumns.SELF_ASSOCIATION_END_NAME);
		});
		return endFlags(end[0]);
	}

	private static Map<String, Boolean> end(boolean mandatory, boolean multiple, boolean aggregate,
			boolean composite) {
		Map<String, Boolean> result = new LinkedHashMap<>();
		result.put(TLStructuredTypePart.MANDATORY_ATTR, mandatory);
		result.put(TLStructuredTypePart.MULTIPLE_ATTR, multiple);
		result.put(TLAssociationEnd.AGGREGATE_ATTR, aggregate);
		result.put(TLAssociationEnd.COMPOSITE_ATTR, composite);
		result.put(TLAssociationEnd.BAG_ATTR, false);
		result.put(TLAssociationEnd.ORDERED_ATTR, false);
		return result;
	}

	private static Map<String, Boolean> endFlags(TypePart end) throws SQLException {
		Map<String, Boolean> result = end(false, false, false, false);
		PooledConnection connection = pool().borrowReadConnection();
		try {
			DBHelper sqlDialect = connection.getSQLDialect();
			StringBuilder sql = new StringBuilder("SELECT ");
			boolean first = true;
			for (String flag : result.keySet()) {
				sql.append(first ? "" : ", ").append(sqlDialect.columnRef(SQLH.mangleDBName(flag)));
				first = false;
			}
			sql.append(" FROM ")
				.append(sqlDialect.tableRef(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)))
				.append(" WHERE ").append(sqlDialect.columnRef(BasicTypes.IDENTIFIER_DB_NAME)).append(" = ?")
				.append(" AND ").append(sqlDialect.columnRef(BasicTypes.REV_MAX_DB_NAME)).append(" = ?");
			try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
				statement.setLong(1, ((LongID) end.getID()).longValue());
				statement.setLong(2, Revision.CURRENT_REV);
				try (ResultSet row = statement.executeQuery()) {
					assertTrue("No association end " + end.getPartName() + ".", row.next());
					int column = 1;
					for (String flag : result.keySet()) {
						result.put(flag, row.getBoolean(column++));
					}
				}
			}
		} finally {
			pool().releaseReadConnection(connection);
		}
		return result;
	}

	/**
	 * The reference of the model module of the test as the knowledge base resolves it.
	 */
	private TLReference reference(String ownerClass, String name) throws Exception {
		TypePart[] part = { null };
		migrate(noProcessor(), (context, log, connection) -> {
			Util util = context.getSQLUtils();
			part[0] = util.getTLTypePartOrFail(connection, TRUNK, MODEL_MODULE, ownerClass, name);
		});
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		KnowledgeItem item = kb.resolveObjectKey(KBUtils.createObjectKey(TRUNK, Revision.CURRENT_REV,
			table(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE), part[0].getID()));
		assertNotNull("Reference " + name + " not found in the knowledge base.", item);
		return item.getWrapper();
	}

	public static Test suite() {
		return suite(TestCompositionContainerEnds.class);
	}

}
