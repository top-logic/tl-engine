/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

/**
 * Test for anonymous entries of keyed lists and for addressing list entries by
 * {@link ConfigurationSchemaConstants#LIST_INDEX} when reading stacked sources with the
 * {@link ConfigurationReader}.
 */
@SuppressWarnings("javadoc")
public class TestKeyedListAnonymousEntries extends AbstractTypedConfigurationTestCase {

	private static final String OWNER_TAG = "owner";

	private static final String INDEX = "config:" + ConfigurationSchemaConstants.LIST_INDEX;

	private static final String OPERATION = "config:" + ConfigurationSchemaConstants.LIST_OPERATION;

	private static final String POSITION = "config:" + ConfigurationSchemaConstants.LIST_POSITION;

	private static final String REFERENCE = "config:" + ConfigurationSchemaConstants.LIST_REFERENCE_ATTR_NAME;

	private static final String OVERRIDE = "config:" + ConfigurationSchemaConstants.CONFIG_OVERLOADING_OVERRIDE;

	public interface Entry extends ConfigurationItem {

		String NAME = "name";

		String VALUE = "value";

		String OTHER = "other";

		@Name(NAME)
		String getName();

		@Name(VALUE)
		String getValue();

		@Name(OTHER)
		String getOther();

	}

	public interface Owner extends ConfigurationItem {

		String KEYED = "keyed";

		String PLAIN = "plain";

		String MAP = "map";

		@Name(KEYED)
		@Key(Entry.NAME)
		@EntryTag("entry")
		List<Entry> getKeyed();

		@Indexed(collection = KEYED)
		Entry getKeyedEntry(String name);

		@Name(PLAIN)
		@EntryTag("entry")
		List<Entry> getPlain();

		@Name(MAP)
		@Key(Entry.NAME)
		@EntryTag("entry")
		Map<String, Entry> getMap();

	}

	private static final String BASE_KEYED = owner(
		"<keyed>"
			+ "<entry value='a1'/>"
			+ "<entry value='a2'/>"
			+ "<entry name='k' value='k1'/>"
			+ "<entry value='a3'/>"
			+ "</keyed>");

	private static final String BASE_PLAIN = owner(
		"<plain>"
			+ "<entry value='p1' other='o1'/>"
			+ "<entry value='p2' other='o2'/>"
			+ "<entry value='p3' other='o3'/>"
			+ "</plain>");

	public void testAnonymousEntriesKept() throws ConfigurationException {
		Owner owner = readOwner(BASE_KEYED);
		assertValues(owner.getKeyed(), "a1", "a2", "k1", "a3");
		assertNull(owner.getKeyedEntry(null));
		assertNull(owner.getKeyedEntry(""));
		assertEquals("k1", owner.getKeyedEntry("k").getValue());
	}

	public void testOverlayAppendsAnonymousAndUpdatesKeyed() throws ConfigurationException {
		Owner owner = readOwner(BASE_KEYED, owner(
			"<keyed>"
				+ "<entry value='a4'/>"
				+ "<entry name='k' other='changed'/>"
				+ "</keyed>"));
		List<Entry> keyed = owner.getKeyed();
		assertValues(keyed, "a1", "a2", "k1", "a3", "a4");
		assertEquals("changed", keyed.get(2).getOther());
		assertEquals("", keyed.get(0).getOther());
		assertEquals("", keyed.get(1).getOther());
		assertEquals("", keyed.get(3).getOther());
	}

	public void testAnonymousPlacedBeforeReference() throws ConfigurationException {
		Owner owner = readOwner(BASE_KEYED, owner(
			"<keyed>"
				+ "<entry value='a4' " + POSITION + "='before' " + REFERENCE + "='k'/>"
				+ "</keyed>"));
		assertValues(owner.getKeyed(), "a1", "a2", "a4", "k1", "a3");
	}

	public void testUpdateWithoutKeyFails() {
		assertFails("must have a key value", BASE_KEYED, owner(
			"<keyed>"
				+ "<entry " + OPERATION + "='update' value='x'/>"
				+ "</keyed>"));
	}

	public void testRemoveWithoutKeyFails() {
		assertFails("must have a key value", BASE_KEYED, owner(
			"<keyed>"
				+ "<entry " + OPERATION + "='remove'/>"
				+ "</keyed>"));
	}

	public void testReferenceToUnknownKeyFails() {
		assertFails("No corresponding ConfigurationItem found for reference 'unknown'", BASE_KEYED, owner(
			"<keyed>"
				+ "<entry value='x' " + POSITION + "='before' " + REFERENCE + "='unknown'/>"
				+ "</keyed>"));
	}

	public void testIndexUpdatesUnkeyedList() throws ConfigurationException {
		Owner owner = readOwner(BASE_PLAIN, owner(
			"<plain>"
				+ "<entry " + INDEX + "='1' other='changed'/>"
				+ "</plain>"));
		List<Entry> plain = owner.getPlain();
		assertValues(plain, "p1", "p2", "p3");
		assertEquals("o1", plain.get(0).getOther());
		assertEquals("changed", plain.get(1).getOther());
		assertEquals("o3", plain.get(2).getOther());
	}

	public void testIndexMovesUnkeyedEntry() throws ConfigurationException {
		Owner owner = readOwner(BASE_PLAIN, owner(
			"<plain>"
				+ "<entry " + INDEX + "='2' " + POSITION + "='begin' other='changed'/>"
				+ "</plain>"));
		List<Entry> plain = owner.getPlain();
		assertValues(plain, "p3", "p1", "p2");
		assertEquals("changed", plain.get(0).getOther());
	}

	public void testIndexUpdatesAnonymousEntryOfKeyedList() throws ConfigurationException {
		Owner owner = readOwner(BASE_KEYED, owner(
			"<keyed>"
				+ "<entry " + INDEX + "='1' other='changed'/>"
				+ "</keyed>"));
		List<Entry> keyed = owner.getKeyed();
		assertValues(keyed, "a1", "a2", "k1", "a3");
		assertEquals("", keyed.get(0).getOther());
		assertEquals("changed", keyed.get(1).getOther());
		assertEquals("", keyed.get(2).getOther());
	}

	public void testIndexCountsOverlayEntriesReadBefore() throws ConfigurationException {
		Owner owner = readOwner(BASE_KEYED, owner(
			"<keyed>"
				+ "<entry value='a0' " + POSITION + "='begin'/>"
				+ "<entry " + INDEX + "='1' other='changed'/>"
				+ "</keyed>"));
		List<Entry> keyed = owner.getKeyed();
		assertValues(keyed, "a0", "a1", "a2", "k1", "a3");
		assertEquals("changed", keyed.get(1).getOther());
	}

	public void testIndexWithOverrideReplaces() throws ConfigurationException {
		Owner owner = readOwner(BASE_PLAIN, owner(
			"<plain>"
				+ "<entry " + INDEX + "='1' " + OVERRIDE + "='true' value='new'/>"
				+ "</plain>"));
		List<Entry> plain = owner.getPlain();
		assertValues(plain, "p1", "new", "p3");
		assertEquals("", plain.get(1).getOther());
	}

	public void testIndexSetsKey() throws ConfigurationException {
		Owner owner = readOwner(BASE_KEYED,
			owner(
				"<keyed>"
					+ "<entry " + INDEX + "='0' name='first'/>"
					+ "</keyed>"),
			owner(
				"<keyed>"
					+ "<entry name='first' other='by-key'/>"
					+ "<entry value='a4' " + POSITION + "='after' " + REFERENCE + "='first'/>"
					+ "</keyed>"));
		List<Entry> keyed = owner.getKeyed();
		assertValues(keyed, "a1", "a4", "a2", "k1", "a3");
		assertEquals("first", keyed.get(0).getName());
		assertEquals("by-key", keyed.get(0).getOther());
		assertSame(keyed.get(0), owner.getKeyedEntry("first"));
	}

	public void testIndexSettingExistingKeyFails() {
		assertFails("Duplicate key 'k'", BASE_KEYED, owner(
			"<keyed>"
				+ "<entry " + INDEX + "='0' name='k'/>"
				+ "</keyed>"));
	}

	public void testIndexOutOfRangeFails() {
		assertFails("out of range", BASE_PLAIN, owner(
			"<plain>"
				+ "<entry " + INDEX + "='3' other='x'/>"
				+ "</plain>"));
	}

	public void testNegativeIndexFails() {
		assertFails("must not be negative", BASE_PLAIN, owner(
			"<plain>"
				+ "<entry " + INDEX + "='-1' other='x'/>"
				+ "</plain>"));
	}

	public void testNonNumericIndexFails() {
		assertFails("is not a number", BASE_PLAIN, owner(
			"<plain>"
				+ "<entry " + INDEX + "='first' other='x'/>"
				+ "</plain>"));
	}

	public void testIndexWithRemoveFails() {
		assertFails("must not be combined", BASE_PLAIN, owner(
			"<plain>"
				+ "<entry " + INDEX + "='0' " + OPERATION + "='remove'/>"
				+ "</plain>"));
	}

	public void testIndexWithAddFails() {
		assertFails("must not be combined", BASE_KEYED, owner(
			"<keyed>"
				+ "<entry " + INDEX + "='0' " + OPERATION + "='add'/>"
				+ "</keyed>"));
	}

	public void testIndexInSingleSource() throws ConfigurationException {
		Owner owner = readOwner(owner(
			"<plain>"
				+ "<entry value='p1'/>"
				+ "<entry " + INDEX + "='0' other='changed'/>"
				+ "</plain>"));
		List<Entry> plain = owner.getPlain();
		assertValues(plain, "p1");
		assertEquals("changed", plain.get(0).getOther());
	}

	public void testMapStillNeedsDistinctKeys() throws ConfigurationException {
		Owner owner = readOwner(
			owner("<map><entry name='x' value='1'/><entry name='y' value='2'/></map>"),
			owner("<map><entry name='x' other='changed'/></map>"));
		assertEquals(2, owner.getMap().size());
		assertEquals("changed", owner.getMap().get("x").getOther());
		assertEquals("1", owner.getMap().get("x").getValue());
	}

	private static String owner(String content) {
		return XML_DECLARATION + "<" + OWNER_TAG + XML_CONFIG_NAMESPACE_DECLARATION + ">" + content + "</" + OWNER_TAG
			+ ">";
	}

	private Owner readOwner(String... sources) throws ConfigurationException {
		return read(sources);
	}

	private void assertFails(String expectedMessagePart, String... sources) {
		try {
			readOwner(sources);
		} catch (AssertionFailedError | ConfigurationException | RuntimeException ex) {
			BasicTestCase.assertErrorMessage("Unexpected error message.",
				Pattern.compile(Pattern.quote(expectedMessagePart)), ex);
			return;
		}
		fail("Expected failure containing '" + expectedMessagePart + "'.");
	}

	private static void assertValues(List<Entry> entries, String... expectedValues) {
		List<String> values = new ArrayList<>();
		for (Entry entry : entries) {
			values.add(entry.getValue());
		}
		assertEquals(List.of(expectedValues), values);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(OWNER_TAG, TypedConfiguration.getConfigurationDescriptor(Owner.class));
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestKeyedListAnonymousEntries.class));
	}

}
