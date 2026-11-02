/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import static com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil.*;

import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.LongID;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.model.migration.model.refactor.MoveLinksProcessor;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test of {@link MoveLinksProcessor}.
 */
@SuppressWarnings("javadoc")
public class TestMoveLinksProcessor extends AbstractLinkTableMigrationTest {

	/**
	 * A reference whose links are not stored in the tables used by the test.
	 */
	private static final String REFERENCE = "tl.model:TLClass#generalizations";

	public void testMoveLinks() throws Exception {
		TLStructuredTypePart reference = (TLStructuredTypePart) TLModelUtil.findPart(REFERENCE);
		long attribute = ((LongID) reference.tId().getObjectName()).longValue();

		String source = WRAPPER_ATTRIBUTE_BAG_ASSOCIATION;
		String dest = WRAPPER_DELETE_REFERER_BAG_ASSOCIATION;
		insertLink(source, FIRST_ID + 1, 1, MAX, 1, attribute, 100, 0);
		insertLink(source, FIRST_ID + 2, 1, MAX, 1, attribute, 101, 0);
		// A link of another reference stays in the source table.
		insertLink(source, FIRST_ID + 3, 1, MAX, 1, attribute + 1_000_000, 100, 0);

		MoveLinksProcessor.Config<?> config = TypedConfiguration.newConfigItem(MoveLinksProcessor.Config.class);
		config.setSourceTable(source);
		config.setDestTable(dest);
		config.setReference(
			new QualifiedPartName.Format().getValue(MoveLinksProcessor.Config.REFERENCE, REFERENCE));

		BufferingProtocol log = migrate(TypedConfigUtil.createInstance(config),
			(MigrationContext context, BufferingProtocol contextLog, PooledConnection connection) ->
				// Rebuilds the revision index of the tables the processor invalidated.
				context.revalidateXRef(contextLog, connection));

		assertEquals(List.of(row(FIRST_ID + 3, 1, MAX)), rows(source));
		assertEquals(List.of(row(FIRST_ID + 1, 1, MAX), row(FIRST_ID + 2, 1, MAX)), rows(dest));
		assertInfo(log, "Copied 2 rows from table '" + source + "' to '" + dest + "'.");
		assertInfo(log, "Re-added ");
		assertInfo(log, " rows for table '" + dest + "' to ");
		assertInfo(log, " rows for table '" + source + "' to ");
	}

	public static Test suite() {
		return suite(TestMoveLinksProcessor.class);
	}

}
