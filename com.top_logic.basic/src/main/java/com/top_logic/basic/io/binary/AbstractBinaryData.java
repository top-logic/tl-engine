/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.StreamUtilities;

/**
 * Base class for {@link BinaryData} implementations that provides generic
 * implementations for the general contract of {@link #hashCode()} and
 * {@link #equals(Object)} of {@link BinaryData}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBinaryData implements BinaryData {

    @Override
    public boolean equals(Object anOther) {
		return AbstractBinaryData.equalsBinaryData(this, anOther);
    }
    
    @Override
    public int hashCode() {
        return AbstractBinaryData.hashCodeBinaryData(this);
    }

	/**
	 * General implementation of hash code for {@link BinaryData} as defined by
	 * {@link BinaryData#hashCode()}.
	 * 
	 * @param binary
	 *        The {@link BinaryData} to compute the hash code for.
	 * 
	 * @return The hash code of the given {@link BinaryData}.
	 */
	public static int hashCodeBinaryData(BinaryData binary) {
		long size = binary.getSize();
		int result = (int) (size ^ (size >>> 32));
		
		try {
			try (InputStream in = binary.getStream()) {
				readContents:
				for (int n = 0; n < 64; n++) {
					for (int offset = 0; offset <= 24; offset += 8) {
						int data = in.read();
						if (data < 0) {
							break readContents;
						}
						
						result ^= (0xFF & data) << offset;
					}
				}
			}
		} catch (IOException ex) {
			// Ignore, stop reading at the location, where the error occurs.
		}
		
		return result;
	}

	/**
	 * General implementation of equals for {@link BinaryData} as defined by
	 * {@link BinaryData#equals(Object)}.
	 * 
	 * @param self
	 *        The {@link BinaryData} to compare with the other given
	 *        {@link BinaryData} instance.
	 * @param other
	 *        The other {@link BinaryData} instance to compare with the first.
	 * 
	 * @return Whether both given {@link BinaryData} instances have equal
	 *         contents.
	 */
	public static boolean equalsBinaryData(BinaryData self, Object other) {
		if (self == other) {
		    return true;
		}
		if (!(other instanceof BinaryData)) {
			return false;
		}
		
		BinaryData otherData = (BinaryData) other;

		long selfSize = self.getSize();
		long otherSize = otherData.getSize();
		if (selfSize > 0 && otherSize > 0 && selfSize != otherSize) {
		    return false;
		}
		
		try {
			try (InputStream in1 = self.getStream(); InputStream in2 = otherData.getStream()) {
				return StreamUtilities.equalsStreamContents(in1, in2);
			}
		} catch (IOException e) {
		    Logger.error("Failed to check streams for equality", e, self);
		    return false;
		}
	}

	/**
	 * The given content type, or {@link #CONTENT_TYPE_OCTET_STREAM}, if the given value was
	 * <code>null</code> or empty.
	 */
	protected static String nonNullContentType(String contentType) {
		return StringServices.isEmpty(contentType) ? CONTENT_TYPE_OCTET_STREAM : contentType;
	}

	@Override
	public String toString() {
		return getName();
	}

}
