/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

import java.util.EmptyStackException;

/**
 * {@link SuperUserState} implementation based on a thread local.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SuperUserStateImpl extends ThreadLocal<Integer> implements SuperUserState {

	private static final SuperUserStateImpl INSTANCE = new SuperUserStateImpl();

	private SuperUserStateImpl() {
		// singleton instance
	}

	/**
	 * The sole {@link SuperUserState}.
	 */
	public static SuperUserState getInstance() {
		return INSTANCE;
	}

	@Override
	public void resetSuperUser() {
		remove();
	}

	@Override
	public void pushSuperUser() {
		Integer count = get();
		if (count == null) {
			set(Integer.valueOf(1));
		} else {
			set(count.intValue() + 1);
		}
	}

	@Override
	public boolean isSuperUser() {
		Integer count = get();
		return count != null;
	}

	@Override
	public void popSuperUser() {
		Integer count = get();
		if (count == null) {
			throw new EmptyStackException();
		} else {
			int iCount = count.intValue();
			switch (iCount) {
				case 0:
					throw new EmptyStackException();
				case 1:
					remove();
					break;
				default:
					int newCount = iCount - 1;
					assert newCount > 0;
					set(newCount);
			}
		}

	}

}
