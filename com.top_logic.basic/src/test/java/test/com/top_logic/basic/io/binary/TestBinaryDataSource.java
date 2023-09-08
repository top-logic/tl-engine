/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sched.SchedulerService;

/**
 * Test case for {@link BinaryDataSource} upgrade to {@link BinaryData}.
 */
@SuppressWarnings("javadoc")
public class TestBinaryDataSource extends TestCase {

	public void testUpgradeMini() throws IOException {
		BinaryDataSource source = new BinaryDataSource() {
			@Override
			public String getName() {
				return "foo";
			}

			@Override
			public long getSize() {
				return -1;
			}

			@Override
			public String getContentType() {
				return "application/foo";
			}

			@Override
			public void deliverTo(OutputStream out) throws IOException {
				out.write(65);
				out.write(66);
				out.write(67);
				out.write(new byte[] { 68, 69, 70, 71, 72 });
				out.write(new byte[] { 0, 0, 73, 74, 75, 76, 0, 0 }, 2, 4);
			}
		};

		assertEquals("ABC" + "DEFGH" + "IJKL", StreamUtilities.readAllFromStream(source.toData()));
	}

	public void testUpgradeLarge() throws IOException {
		Random rnd = new Random(42);
		byte[] input = BasicTestCase.randomString(rnd, 1000000, true, false, false, false).getBytes();

		BinaryDataSource source = new BinaryDataSource() {
			@Override
			public String getName() {
				return "foo";
			}

			@Override
			public long getSize() {
				return -1;
			}

			@Override
			public String getContentType() {
				return "application/foo";
			}

			@Override
			public void deliverTo(OutputStream out) throws IOException {
				int n = 0;
				int avail;

				byte[] buffer = new byte[8512];
				while ((avail = input.length - n) > 0) {
					int cnt;
					float choice = (rnd.nextFloat());

					if (choice < 0.1) {
						cnt = 1;
						out.write(input[n]);
					} else if (choice < 0.9) {
						cnt = Math.min(avail, rnd.nextInt(256));
						System.arraycopy(input, n, buffer, 0, cnt);
						out.write(buffer, 0, cnt);
					} else {
						cnt = Math.min(avail, 512 + rnd.nextInt(8000));
						System.arraycopy(input, n, buffer, 0, cnt);
						out.write(buffer, 0, cnt);
					}
					n += cnt;
				}
			}
		};

		// Read efficient.
		ByteArrayOutputStream buffer1 = new ByteArrayOutputStream();
		StreamUtilities.copyStreamContents(source.toData().getStream(), buffer1);
		assertTrue(Arrays.equals(input, buffer1.toByteArray()));

		// Read each single byte.
		ByteArrayOutputStream buffer2 = new ByteArrayOutputStream();
		try (InputStream in = source.toData().getStream()) {
			while (true) {
				int b = in.read();
				if (b < 0) {
					break;
				}
				buffer2.write(b);
			}
		}
		assertTrue(Arrays.equals(input, buffer2.toByteArray()));

		// Read partial.
		ByteArrayOutputStream buffer3 = new ByteArrayOutputStream();
		try (InputStream in = source.toData().getStream()) {
			for (int n = 0; n < 10000; n++) {
				buffer3.write(in.read());
			}
		}
		assertTrue(Arrays.equals(ArrayUtil.copy(input, 0, 10000), buffer3.toByteArray()));
	}

	public static Test suite() {
		return ModuleTestSetup
			.setupModule(ServiceTestSetup.createSetup(TestBinaryDataSource.class, SchedulerService.Module.INSTANCE));
	}
}
