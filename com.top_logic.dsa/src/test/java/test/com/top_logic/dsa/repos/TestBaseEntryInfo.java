/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dsa.repos;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.dsa.DSATestSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dsa.repos.BaseEntryInfo;
import com.top_logic.dsa.repos.EntryInfo;
import com.top_logic.dsa.repos.VersionInfo;
import com.top_logic.dsa.repos.file.FileRepository;

/**
 * Test for {@link BaseEntryInfo} that is shared between threads and between repository instances
 * working on the same storage.
 */
public class TestBaseEntryInfo extends BasicTestCase {

	private static final String USER = "writer";

	private static final String READER_USER = "reader";

	private static final String ENTRY = "entry.txt";

	private static final int CHECK_INS = 300;

	private static final int READERS = 8;

	private File _repositoryDir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_repositoryDir = createdCleanTestDir("entryInfoRepos");
	}

	/**
	 * Reads the latest visible version of an entry from several threads while another thread
	 * checks in new versions of that entry.
	 */
	public void testConcurrentCheckIn() throws Exception {
		FileRepository repository = repository();
		assertEquals(1, repository.create(USER, null, ENTRY, content(1)));
		assertTrue(repository.unlock(USER, ENTRY));

		EntryInfo info = (EntryInfo) repository.getInformation(ENTRY);
		info.getLastVersionInfo();

		List<Throwable> problems = Collections.synchronizedList(new ArrayList<>());
		AtomicBoolean done = new AtomicBoolean();

		List<Thread> readers = new ArrayList<>();
		for (int n = 0; n < READERS; n++) {
			Thread reader = new Thread(() -> {
				while (!done.get()) {
					try {
						int visibleVersions = info.getNumVersions(READER_USER);
						VersionInfo version = info.getVersionInfo(visibleVersions);
						if (version == null) {
							problems.add(new AssertionError("No version info for visible version " + visibleVersions + "."));
						}
					} catch (Throwable ex) {
						problems.add(ex);
					}
				}
			}, "EntryInfoReader-" + n);
			readers.add(reader);
			reader.start();
		}

		int lastVersion = 1;
		try {
			for (int version = 2; version <= CHECK_INS && problems.isEmpty(); version++) {
				assertTrue(repository.lock(USER, ENTRY));
				assertEquals(version, repository.create(USER, null, ENTRY, content(version)));
				assertTrue(repository.unlock(USER, ENTRY));
				lastVersion = version;
			}
		} finally {
			done.set(true);
			for (Thread reader : readers) {
				reader.join();
			}
		}

		if (!problems.isEmpty()) {
			AssertionError error =
				new AssertionError(problems.size() + " readers failed during concurrent check-in.");
			error.initCause(problems.get(0));
			throw error;
		}

		VersionInfo last = info.getLastVersionInfo();
		assertNotNull(last);
		assertEquals(lastVersion, last.getVersion());
		assertEquals(content(lastVersion).available(), info.getSize());
	}

	/**
	 * Re-reads the entry information after another repository instance on the same storage checked
	 * in a new version.
	 */
	public void testReadInfoAfterForeignCheckIn() throws Exception {
		FileRepository repository1 = repository();
		FileRepository repository2 = repository();

		assertEquals(1, repository1.create(USER, null, ENTRY, content(1)));
		assertTrue(repository1.unlock(USER, ENTRY));

		EntryInfo info = (EntryInfo) repository1.getInformation(ENTRY);
		assertEquals(1, info.getLastVersionInfo().getVersion());

		assertTrue(repository2.lock(USER, ENTRY));
		assertEquals(2, repository2.create(USER, null, ENTRY, content(2)));
		assertTrue(repository2.unlock(USER, ENTRY));

		info.readInfo();
		VersionInfo lastVersion = info.getLastVersionInfo();
		assertEquals(2, lastVersion.getVersion());
	}

	private FileRepository repository() {
		FileRepository.Config<?> config = TypedConfiguration.newConfigItem(FileRepository.Config.class);
		config.setPath(_repositoryDir.getAbsolutePath());
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static InputStream content(int version) {
		return new ByteArrayInputStream(("Version " + version).getBytes());
	}

	/**
	 * Suite of all tests in this class.
	 */
	public static Test suite() {
		return DSATestSetup.createDSATestSetup(new TestSuite(TestBaseEntryInfo.class));
	}

}
