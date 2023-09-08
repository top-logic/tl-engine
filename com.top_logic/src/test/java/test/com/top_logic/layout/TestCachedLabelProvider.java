/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.text.Collator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.CachedLabelProvider;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;

/**
 * Test case for {@link CachedLabelProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestCachedLabelProvider extends TestCase {

	/**
	 * Tests that the underlying label provider is accessed only once per object when sorting by
	 * label and using a {@link CachedLabelProvider} proxy.
	 */
	public void testSort() {
		Collections.sort(
			BasicTestCase.list(1, 2, 3, 4, 5, 10, 9, 8, 7, 6),
			LabelComparator.newInstance(
				CachedLabelProvider.newInstance(labelsOnlyOnce()),
				Collator.getInstance(Locale.GERMANY)));
	}

	private LabelProvider labelsOnlyOnce() {
		LabelProvider onlyOnceLabelProvider = new LabelProvider() {
			Set<Object> _seen = new HashSet<>();

			@Override
			public String getLabel(Object object) {
				// Label for each object requested only once.
				assertTrue("Already seen: " + object, _seen.add(object));

				return object.toString();
			}
		};
		return onlyOnceLabelProvider;
	}

}
