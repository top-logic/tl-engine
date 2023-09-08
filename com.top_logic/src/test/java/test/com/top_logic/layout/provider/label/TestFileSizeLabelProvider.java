/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.provider.label;

import java.text.NumberFormat;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.provider.label.FileSizeLabelProvider;
import com.top_logic.mig.html.HTMLFormatter;

/**
 * {@link BasicTestCase} for {@link FileSizeLabelProvider}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestFileSizeLabelProvider extends BasicTestCase {

	@SuppressWarnings("javadoc")
	public void testUseNoUnitPrefix() {
		long size = 1023; // B
		String expectedSizeLabel = "1023 B";
		String actualSizeLabel = FileSizeLabelProvider.INSTANCE.getLabel(size);
		assertEquals(expectedSizeLabel, actualSizeLabel);
	}

	@SuppressWarnings("javadoc")
	public void testUseKiloPrefix() {
		long size = 3 * 1024; // 3 KB
		String expectedSizeLabel = "3 KB";
		String actualFileSizeLabel = FileSizeLabelProvider.INSTANCE.getLabel(size);
		assertEquals(expectedSizeLabel, actualFileSizeLabel);
	}

	@SuppressWarnings("javadoc")
	public void testDefaultTwoMaxFractionDigits() {
		long size = 3 * 1024 + 112; // ~3,109 KB
		String expectedSizeLabel = getFormat().format(3.11) + " KB";
		String actualSizeLabel = FileSizeLabelProvider.INSTANCE.getLabel(size);
		assertEquals(expectedSizeLabel, actualSizeLabel);
	}

	@SuppressWarnings("javadoc")
	public void testZeroBytesForNull() {
		String expectedSizeLabel = "0 B";
		String actualSizeLabel = FileSizeLabelProvider.INSTANCE.getLabel(null);
		assertEquals(expectedSizeLabel, actualSizeLabel);
	}

	@SuppressWarnings("javadoc")
	public void testExceedMaximumUnitPrefix() {
		long size = 3L * 1024 * 1024 * 1024 * 1024 * 1024 * 1024; // 3 EB
		String expectedSizeLabel = getFormat().format(3072) + " PB";
		String actualSizeLabel = FileSizeLabelProvider.INSTANCE.getLabel(size);
		assertEquals(expectedSizeLabel, actualSizeLabel);
	}

	private NumberFormat getFormat() {
		NumberFormat format = (NumberFormat) HTMLFormatter.getInstance().getNumberFormat().clone();
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(2);
		return format;
	}

	@SuppressWarnings("javadoc")
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestFileSizeLabelProvider.class);
	}

}
