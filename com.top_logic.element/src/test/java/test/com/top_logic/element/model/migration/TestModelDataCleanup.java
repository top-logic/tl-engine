/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import static com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil.*;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.element.model.migration.model.ModelDataCleanup;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.model.migration.data.BranchIdType;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;

/**
 * Test of {@link ModelDataCleanup}.
 */
@SuppressWarnings("javadoc")
public class TestModelDataCleanup extends AbstractLinkTableMigrationTest {

	private static final String OBJECT_TABLE = "GenericObject";

	private static final long TRUNK = 1L;

	public void testDeleteReferenceValues() throws Exception {
		long reference = FIRST_ID + 100;
		long override = FIRST_ID + 101;
		long property = FIRST_ID + 102;
		long otherReference = FIRST_ID + 103;

		insertLink(WRAPPER_ATTRIBUTE_ASSOCIATION, FIRST_ID + 1, 1, MAX, 1, reference, 100, 0);
		insertLink(WRAPPER_ATTRIBUTE_ASSOCIATION, FIRST_ID + 2, 1, MAX, 1, override, 100, 0);
		insertLink(WRAPPER_ATTRIBUTE_ASSOCIATION, FIRST_ID + 3, 1, MAX, 1, property, 100, 0);
		insertLink(WRAPPER_ATTRIBUTE_ASSOCIATION, FIRST_ID + 4, 1, MAX, 1, otherReference, 100, 0);
		// Values of the reference are found in all link tables.
		insertLink(WRAPPER_ATTRIBUTE_BAG_ASSOCIATION, FIRST_ID + 5, 1, MAX, 1, reference, 100, 0);

		Type owner = BranchIdType.newInstance(Type.class, TRUNK, LongID.valueOf(FIRST_ID + 200), META_ELEMENT_OBJECT_TYPE);
		List<TypePart> parts = List.of(
			part(owner, reference, TypePart.Kind.REFERENCE, reference),
			// Values of an override are stored with the identifier of its definition.
			part(owner, override, TypePart.Kind.REFERENCE, otherReference),
			part(owner, property, TypePart.Kind.CLASS_PROPERTY, property));

		BufferingProtocol log = migrate((context, contextLog, connection) -> {
			try {
				new ModelDataCleanup(context, contextLog, connection).deleteReferenceValues(TRUNK, parts);
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
		}, TestModelDataCleanup::revalidate);

		assertEquals(
			List.of(row(FIRST_ID + 2, 1, MAX), row(FIRST_ID + 3, 1, MAX), row(FIRST_ID + 4, 1, MAX)),
			rows(WRAPPER_ATTRIBUTE_ASSOCIATION));
		assertEquals(List.of(), rows(WRAPPER_ATTRIBUTE_BAG_ASSOCIATION));
		assertInfo(log,
			"Deleted 1 values of deleted references from table '" + WRAPPER_ATTRIBUTE_ASSOCIATION + "'.");
		assertInfo(log,
			"Deleted 1 values of deleted references from table '" + WRAPPER_ATTRIBUTE_BAG_ASSOCIATION + "'.");
	}

	public void testDeleteInstances() throws Exception {
		long deletedType = FIRST_ID + 500;
		long otherType = FIRST_ID + 501;
		long deleted = FIRST_ID + 1;
		long other = FIRST_ID + 2;
		long attribute = FIRST_ID + 600;

		insertObject(deleted, deletedType);
		insertObject(other, otherType);
		insertFlexData(deleted);
		insertFlexData(other);

		String metaElement = META_ELEMENT_OBJECT_TYPE;
		insertLink(WRAPPER_ATTRIBUTE_ASSOCIATION, FIRST_ID + 10, 1, MAX, OBJECT_TABLE, deleted, attribute,
			metaElement, 7, 0);
		insertLink(WRAPPER_ATTRIBUTE_ASSOCIATION, FIRST_ID + 11, 1, MAX, metaElement, 7, attribute,
			OBJECT_TABLE, deleted, 0);
		insertLink(WRAPPER_ATTRIBUTE_ASSOCIATION, FIRST_ID + 12, 1, MAX, OBJECT_TABLE, other, attribute,
			OBJECT_TABLE, deleted, 0);
		insertLink(WRAPPER_ATTRIBUTE_ASSOCIATION, FIRST_ID + 13, 1, MAX, OBJECT_TABLE, other, attribute,
			OBJECT_TABLE, other, 0);

		int[] deletedObjects = { -1 };
		BufferingProtocol log = migrate((context, contextLog, connection) -> {
			try {
				Set<TLID> types = Set.of(LongID.valueOf(deletedType));
				deletedObjects[0] = new ModelDataCleanup(context, contextLog, connection)
					.deleteInstances(TRUNK, OBJECT_TABLE, types);
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
		}, TestModelDataCleanup::revalidate);

		assertEquals(1, deletedObjects[0]);
		assertEquals(List.of(row(other, 1, MAX)), rows(OBJECT_TABLE));
		assertEquals(List.of(row(other, 1, MAX)), rows(AbstractFlexDataManager.FLEX_DATA));
		assertEquals(List.of(row(FIRST_ID + 13, 1, MAX)), rows(WRAPPER_ATTRIBUTE_ASSOCIATION));
		assertInfo(log, "Deleted 1 flex attribute values of objects in table '" + OBJECT_TABLE + "'.");
	}

	private static void revalidate(MigrationContext context, BufferingProtocol log, PooledConnection connection)
			throws SQLException {
		// Rebuilds the revision index of the tables the cleanup invalidated.
		context.revalidateXRef(log, connection);
	}

	private static TypePart part(Type owner, long id, TypePart.Kind kind, long definition) {
		return TypePart.newInstance(TRUNK, LongID.valueOf(id), "MetaAttribute", kind, owner, "part" + id,
			LongID.valueOf(definition), 0);
	}

	private void insertObject(long id, long type) throws SQLException {
		MOReference typeRef = (MOReference) table(OBJECT_TABLE).getAttribute(PersistentObject.TYPE_REF);

		Map<String, Object> values = new LinkedHashMap<>();
		values.put(BasicTypes.IDENTIFIER_DB_NAME, id);
		values.put(BasicTypes.REV_MIN_DB_NAME, 1L);
		values.put(BasicTypes.REV_MAX_DB_NAME, MAX);
		values.put(BasicTypes.REV_CREATE_DB_NAME, 1L);
		values.put(BasicTypes.BRANCH_DB_NAME, TRUNK);
		putReference(values, typeRef, META_ELEMENT_OBJECT_TYPE, type, 0);
		insertRow(OBJECT_TABLE, values);
	}

	private void insertFlexData(long id) throws SQLException {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put(AbstractFlexDataManager.BRANCH_DBNAME, TRUNK);
		values.put(AbstractFlexDataManager.TYPE_DBNAME, OBJECT_TABLE);
		values.put(AbstractFlexDataManager.IDENTIFIER_DBNAME, id);
		values.put(BasicTypes.REV_MIN_DB_NAME, 1L);
		values.put(BasicTypes.REV_MAX_DB_NAME, MAX);
		values.put(AbstractFlexDataManager.ATTRIBUTE_DBNAME, "value");
		values.put(AbstractFlexDataManager.DATA_TYPE_DBNAME, AbstractFlexDataManager.LONG_TYPE);
		values.put(AbstractFlexDataManager.LONG_DATA_DBNAME, 42L);
		insertRow(AbstractFlexDataManager.FLEX_DATA, values);
	}

	public static Test suite() {
		return suite(TestModelDataCleanup.class);
	}

}
