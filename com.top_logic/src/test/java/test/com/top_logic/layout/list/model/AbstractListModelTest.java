/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.top_logic.basic.col.Filter;

/**
 * Utilities for testing {@link ListModel}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractListModelTest extends TestCase {

	public static final Filter<Integer> LESS_THAN_80 = new Filter<>() {
		@Override
		public boolean accept(Integer anObject) {
			return anObject.intValue() < 80;
		}
	};

	public static final Filter<Integer> LESS_THAN_40 = new Filter<>() {
		@Override
		public boolean accept(Integer anObject) {
			return anObject.intValue() < 40;
		}
	};

	public static final Filter<Integer> GREATER_THAN_60 = new Filter<>() {
		@Override
		public boolean accept(Integer anObject) {
			return anObject.intValue() > 60;
		}
	};

	public static final Filter<Integer> ODD = new Filter<>() {
		@Override
		public boolean accept(Integer anObject) {
			return anObject.intValue() % 2 == 1;
		}
	};

	public static final Filter<Integer> EVEN = new Filter<>() {
		@Override
		public boolean accept(Integer anObject) {
			return anObject.intValue() % 2 == 0;
		}
	};

	/**
	 * Creates a {@link DefaultListModel} with {@link Integer}s from 0,...99.
	 */
	protected DefaultListModel newBaseModel() {
		DefaultListModel baseModel = new DefaultListModel();
		for (int n = 0; n < 100; n++) {
			baseModel.addElement(Integer.valueOf(n));
		}
		return baseModel;
	}

	protected Set<Integer> newBase(Filter<? super Integer> filter) {
		Set<Integer> base = new HashSet<>();
		for (int n = 0; n < 100; n++) {
			Integer value = Integer.valueOf(n);
			if (filter.accept(value)) {
				base.add(value);
			}
		}
		return base;
	}

	protected void checkContents(Filter<? super Object> predicate, ListModel model) {
		Set<Integer> all = newBase(predicate);
		for (int cnt = model.getSize(), n = 0; n < cnt; n++) {
			Object value = model.getElementAt(n);
			Assert.assertTrue("Value " + value + " found in model but it was not accepted by the given predicate.", predicate.accept(value));
			all.remove(value);
		}
		Assert.assertEquals("Model contains all values matched by predicate.", 0, all.size());
	}
	

}
