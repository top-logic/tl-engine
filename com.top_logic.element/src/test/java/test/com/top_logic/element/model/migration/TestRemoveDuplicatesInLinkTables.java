/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import static com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil.*;
import static java.util.Arrays.*;

import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.model.migration.model.refactor.RemoveDuplicatesProcessor;

/**
 * Test of {@link RemoveDuplicatesProcessor} on the link tables of references, which store the
 * reference of a link in its {@link com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil#META_ATTRIBUTE_ATTR}
 * column.
 *
 * <p>
 * The test uses the bag tables, since they accept duplicate links.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestRemoveDuplicatesInLinkTables extends AbstractLinkTableMigrationTest {

	private static final long OWNER = 1;

	private static final long ATTRIBUTE = 10;

	private static final long OTHER_ATTRIBUTE = 11;

	private static final long VALUE = 100;

	private static final long OTHER_VALUE = 101;

	private static final long VALUE_REV = 5;

	/**
	 * Duplicates within one attribute are removed over the history, equal values in different
	 * attributes and different revisions of the same value are kept.
	 */
	public void testRemoveDuplicates() throws Exception {
		String links = WRAPPER_ATTRIBUTE_BAG_ASSOCIATION;
		insertLink(links, FIRST_ID + 1, 1, 10, OWNER, ATTRIBUTE, VALUE, 0);
		// Covered completely by link 1.
		insertLink(links, FIRST_ID + 2, 3, 5, OWNER, ATTRIBUTE, VALUE, 0);
		// Starts during link 1: Cut to start after link 1 (not after the deleted link 2).
		insertLink(links, FIRST_ID + 3, 7, MAX, OWNER, ATTRIBUTE, VALUE, 0);
		// Same value in another attribute.
		insertLink(links, FIRST_ID + 4, 1, MAX, OWNER, OTHER_ATTRIBUTE, VALUE, 0);
		// Other value in the same attribute.
		insertLink(links, FIRST_ID + 5, 2, MAX, OWNER, ATTRIBUTE, OTHER_VALUE, 0);
		// Same value and attribute of another owner.
		insertLink(links, FIRST_ID + 6, 2, MAX, OWNER + 1, ATTRIBUTE, VALUE, 0);

		String historic = HISTORIC_WRAPPER_ATTRIBUTE_BAG_ASSOCIATION;
		insertLink(historic, FIRST_ID + 11, 1, MAX, OWNER, ATTRIBUTE, VALUE, VALUE_REV);
		// The current revision of the same value.
		insertLink(historic, FIRST_ID + 12, 1, MAX, OWNER, ATTRIBUTE, VALUE, MAX);
		// Duplicate of link 11.
		insertLink(historic, FIRST_ID + 13, 2, MAX, OWNER, ATTRIBUTE, VALUE, VALUE_REV);

		RemoveDuplicatesProcessor.Config<?> config =
			TypedConfiguration.newConfigItem(RemoveDuplicatesProcessor.Config.class);
		config.setSubtypesOf(asList("hasWrapperAttValueBagBaseAssociation"));
		config.setTables(asList(historic));
		BufferingProtocol log = migrate(TypedConfigUtil.createInstance(config), null);

		assertEquals(List.of(
			row(FIRST_ID + 1, 1, 10),
			row(FIRST_ID + 3, 11, MAX),
			row(FIRST_ID + 4, 1, MAX),
			row(FIRST_ID + 5, 2, MAX),
			row(FIRST_ID + 6, 2, MAX)), rows(links));
		assertEquals(List.of(
			row(FIRST_ID + 11, 1, MAX),
			row(FIRST_ID + 12, 1, MAX)), rows(historic));

		assertInfo(log, "Deleted assignment of value " + VALUE + " of attribute " + ATTRIBUTE + " to owner " + OWNER
			+ " in '" + links + "' in time range [3, 5].");
		assertInfo(log, "Updated assignment of value " + VALUE + " of attribute " + ATTRIBUTE + " to owner " + OWNER
			+ " in '" + links + "' from time range [7, " + MAX + "] to [11, " + MAX + "].");
		assertInfo(log, "Deleted assignment of value " + VALUE + "@" + VALUE_REV + " of attribute " + ATTRIBUTE
			+ " to owner " + OWNER + " in '" + historic + "'");
		assertInfo(log, "Deleted 1 assignments and adjusted 1 assignments in '" + links + "'.");
		assertInfo(log, "Deleted 1 assignments and adjusted 0 assignments in '" + historic + "'.");
		assertInfo(log, "Deleted 0 assignments and adjusted 0 assignments in '" + WRAPPER_VETO_BAG_ASSOCIATION + "'.");
	}

	public static Test suite() {
		return suite(TestRemoveDuplicatesInLinkTables.class);
	}

}
