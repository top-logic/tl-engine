/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.azure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.azure.storage.blob.BlobContainerClient;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.storage.azure.AzureRepositoryContentReader;

/**
 * Test of {@link AzureRepositoryContentReader} against blobs written in the layout of a document
 * repository of former versions, stored in the storage emulator Azurite.
 *
 * <p>
 * The test is skipped if no Docker environment is available.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAzureRepositoryContentReader extends BasicTestCase {

	private static final String BASE = "base/";

	private static final String ROOT = BASE + "repository/";

	private static final String ATTIC = BASE + "attic/";

	private BlobContainerClient _container;

	private AzureRepositoryContentReader _reader;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_container = AzuriteSetup.createContainer();

		putBlob(ROOT + "__folder.txt", "FOLDER: repository");
		putBlob(ROOT + "folder/__folder.txt", "FOLDER: folder");
		putBlob(ROOT + "folder/_fdoc one.txt/__folder.txt", "FOLDER: doc one.txt");
		putBlob(ROOT + "folder/_fdoc one.txt/_0_version", "2;false;false;");
		putBlob(ROOT + "folder/_fdoc one.txt/_1_n_root", "version 1");
		putBlob(ROOT + "folder/_fdoc one.txt/_2_n_Some User", "version 2");
		putBlob(ROOT + "folder/_fdoc one.txt/_10_n_root", "version 10");
		putBlob(ROOT + "folder/_fdoc one.txt/_3_d_root", "");
		putBlob(ROOT + "__escaped/sub/_f_x.pdf/_1_n_root", "escaped");
		putBlob(ROOT + "_ftop.txt/_1_n_root", "top");
		putBlob(ATTIC + "old/gone.txt/_1_n_root", "deleted");

		_reader = newReader("attic");
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			_container.deleteIfExists();
		} finally {
			_container = null;
			_reader = null;
			super.tearDown();
		}
	}

	private AzureRepositoryContentReader newReader(String attic) {
		AzureRepositoryContentReader.Config<?> config =
			TypedConfiguration.newConfigItem(AzureRepositoryContentReader.Config.class);
		config.setConnectionString(AzuriteSetup.connectionString());
		config.setContainerName(_container.getBlobContainerName());
		config.setDirectoryName("base");
		config.setAttic(attic);
		return (AzureRepositoryContentReader) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(config);
	}

	public void testReadVersions() throws IOException {
		assertContent("version 1", "folder/doc one.txt", 1);
		assertContent("version 2", "/folder/doc one.txt", 2);
		assertContent("version 10", "folder/doc one.txt", 10);
		assertContent("top", "top.txt", 1);
	}

	public void testSize() throws IOException {
		assertEquals("version 10".length(), _reader.read("folder/doc one.txt", 10).getSize());
	}

	public void testEscapedDirectories() throws IOException {
		assertContent("escaped", "_escaped/sub/_x.pdf", 1);
	}

	public void testMissing() throws IOException {
		assertNull("Deleted in place.", _reader.read("folder/doc one.txt", 3));
		assertNull("No such version.", _reader.read("folder/doc one.txt", 4));
		assertNull("Version 0 has no content.", _reader.read("folder/doc one.txt", 0));
		assertNull("No such document.", _reader.read("folder/other.txt", 1));
		assertNull("No such directory.", _reader.read("missing/doc one.txt", 1));
		assertNull(_reader.read("", 1));
	}

	public void testAttic() throws IOException {
		assertContent("deleted", "old/gone.txt", 1);
		assertNull("The attic is only used if configured.", newReader(null).read("old/gone.txt", 1));
	}

	private void assertContent(String expected, String path, int version) throws IOException {
		BinaryData data = _reader.read(path, version);
		assertNotNull("No content for '" + path + "' version " + version, data);
		try (InputStream in = data.getStream()) {
			assertEquals(expected, new String(in.readAllBytes(), StandardCharsets.UTF_8));
		}
	}

	private void putBlob(String name, String content) {
		_container.getBlobClient(name).upload(com.azure.core.util.BinaryData.fromString(content));
	}

	/**
	 * The test suite, empty if no Docker environment is available.
	 */
	public static Test suite() {
		return AzuriteSetup.suite(TestAzureRepositoryContentReader.class);
	}

}
