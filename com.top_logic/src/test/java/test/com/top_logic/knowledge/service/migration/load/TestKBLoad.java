/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration.load;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.migration.dump.KBDump;
import com.top_logic.knowledge.service.migration.load.KBLoad;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of {@link KBLoad} converting a dump written by {@link KBDump}.
 */
@SuppressWarnings("javadoc")
public class TestKBLoad extends BasicTestCase {

	public void testOpenDump() throws IOException {
		BinaryData dump = dump();
		KBLoad load = KBLoad.open(log(), dump);

		assertTrue(load.hasDataVersion());
		assertFalse(load.getDataVersions().isEmpty());
		// The dump was written by this application, no migration is necessary.
		assertTrue(load.getMigratedVersions().isEmpty());
		assertTrue(KBLoad.toText(load.getDataVersions()).contains(": "));

		String expectedBase = dump.getName().substring(0,
			dump.getName().length() - (KBDump.DUMP_EXT + KBDump.GZ_EXT).length());
		assertEquals(expectedBase, load.baseName());
	}

	public void testDumpLeavesOutInstallationState() throws IOException {
		String xml;
		try (InputStream in = new GZIPInputStream(dump().getStream())) {
			xml = StreamUtilities.readAllFromStream(in, StandardCharsets.UTF_8);
		}
		for (String table : KBDump.INSTALLATION_TABLES) {
			assertFalse("Dump contains installation table " + table + ".",
				xml.contains("\"" + table + "\""));
		}
		assertFalse("Dump contains the security storage.", xml.contains("\"" + SecurityStorage.TABLE_NAME + "\""));
	}

	public void testCreateSqlScript() throws IOException {
		KBLoad load = KBLoad.open(log(), dump());
		BinaryData script = load.createSqlScript(log());

		assertEquals(load.baseName() + KBLoad.SQL_EXT, script.getName());
		String sql;
		try (InputStream in = new GZIPInputStream(script.getStream())) {
			sql = StreamUtilities.readAllFromStream(in, StandardCharsets.UTF_8);
		}
		assertTrue("Script contains no insert statements.", sql.toUpperCase().contains("INSERT INTO"));
		for (String table : KBDump.INSTALLATION_TABLES) {
			// The stored schema in the properties table names the table, too.
			Pattern insert = Pattern.compile("INSERT INTO\\s+\"?" + table + "\\b", Pattern.CASE_INSENSITIVE);
			assertFalse("Script fills installation table " + table + ".", insert.matcher(sql).find());
		}
	}

	public void testCreateLoaderScript() throws IOException {
		KBLoad load = KBLoad.open(log(), dump());
		BinaryData archive = load.createLoaderScript(log());

		assertEquals(load.baseName() + KBLoad.LOADER_SUFFIX + KBLoad.ZIP_EXT, archive.getName());
		int entries = 0;
		try (ZipInputStream in = new ZipInputStream(archive.getStream())) {
			for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
				assertTrue(entry.getName().startsWith(load.baseName() + KBLoad.LOADER_SUFFIX));
				entries++;
			}
		}
		assertTrue("Archive is empty.", entries > 0);
	}

	public void testNoDump() {
		BinaryData noDump = BinaryDataFactory.createBinaryData("no dump".getBytes(StandardCharsets.UTF_8),
			"text/plain", "no" + KBDump.DUMP_EXT);
		try {
			KBLoad.open(log(), noDump);
			fail("A file that is no dump must be rejected.");
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	private static BinaryData dump() throws IOException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		BinaryData lazyDump = KBDump.newDump(kb, KBDump.DEFAULT_EXCLUDE_TYPES);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try (InputStream in = lazyDump.getStream()) {
			StreamUtilities.copyStreamContents(in, buffer);
		}
		return BinaryDataFactory.createBinaryData(buffer.toByteArray(), BinaryData.CONTENT_TYPE_OCTET_STREAM,
			lazyDump.getName());
	}

	private static Log log() {
		return new LogProtocol(TestKBLoad.class);
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestKBLoad.class,
			ServiceTestSetup.createStarterFactory(TypeIndex.Module.INSTANCE));
	}

}
