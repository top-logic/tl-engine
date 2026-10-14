/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Checks that a ZIP archive is a gap-free sequence of entries, as LibreOffice requires.
 *
 * <p>
 * LibreOffice rejects an archive with unaccounted bytes between its entries as corrupt ("Zip file
 * has holes!"). Apache POI and Java's ZIP readers accept such an archive, so a test reading an
 * export back with POI does not see the defect.
 * </p>
 */
public class ZipGaps {

	/**
	 * Walks the entries in the order of the central directory and checks that each entry (local
	 * header, data and data descriptor) ends exactly where the next one starts, and the last one
	 * where the central directory starts.
	 *
	 * <p>
	 * A data descriptor has 8-byte sizes only if the local header of its entry carries the ZIP64
	 * extra field (APPNOTE 4.3.9.2), else 4-byte sizes.
	 * </p>
	 *
	 * @return {@code null} if the archive has no gap, else a description of the first gap.
	 */
	public static String findGap(byte[] zip) {
		ByteBuffer buffer = ByteBuffer.wrap(zip).order(ByteOrder.LITTLE_ENDIAN);
		int endRecord = zip.length - 22;
		while (buffer.getInt(endRecord) != 0x06054b50) {
			endRecord--;
		}
		int entryCount = Short.toUnsignedInt(buffer.getShort(endRecord + 10));
		int directoryStart = buffer.getInt(endRecord + 16);
		int directoryEntry = directoryStart;
		long expected = 0;
		for (int n = 0; n < entryCount; n++) {
			int compressedSize = buffer.getInt(directoryEntry + 20);
			int nameLength = Short.toUnsignedInt(buffer.getShort(directoryEntry + 28));
			int directoryExtra = Short.toUnsignedInt(buffer.getShort(directoryEntry + 30));
			int commentLength = Short.toUnsignedInt(buffer.getShort(directoryEntry + 32));
			int local = buffer.getInt(directoryEntry + 42);
			String name = new String(zip, directoryEntry + 46, nameLength, StandardCharsets.UTF_8);
			if (compressedSize == 0xFFFFFFFF) {
				return "ZIP64 sizes (not supported by this check) for " + name;
			}
			if (local != expected) {
				return "gap of " + (local - expected) + " bytes before " + name;
			}

			int flags = Short.toUnsignedInt(buffer.getShort(local + 6));
			int localName = Short.toUnsignedInt(buffer.getShort(local + 26));
			int localExtra = Short.toUnsignedInt(buffer.getShort(local + 28));
			int data = local + 30 + localName + localExtra;
			boolean zip64 = false;
			for (int field = local + 30 + localName; field < data;) {
				zip64 |= Short.toUnsignedInt(buffer.getShort(field)) == 0x0001;
				field += 4 + Short.toUnsignedInt(buffer.getShort(field + 2));
			}
			long end = data + Integer.toUnsignedLong(compressedSize);
			if ((flags & 0x08) != 0) {
				// Data descriptor: optional signature, CRC, compressed and uncompressed size.
				end += (buffer.getInt((int) end) == 0x08074b50 ? 4 : 0) + 4 + (zip64 ? 16 : 8);
			}
			expected = end;
			directoryEntry += 46 + nameLength + directoryExtra + commentLength;
		}
		return expected == directoryStart ? null
			: "gap of " + (directoryStart - expected) + " bytes before the central directory";
	}

}
