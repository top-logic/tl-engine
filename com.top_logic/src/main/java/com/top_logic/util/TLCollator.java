/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.text.Collator;
import java.util.Comparator;

/**
 * A null-safe {@link Comparator} based on a {@link Collator} with the locale of the current
 * {@link TLContext}.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public final class TLCollator implements Comparator<String> {
    
	private Collator baseCollator;

    /** 
     * Create a TLCollator based on the {@link TLContext#getLocale()}
     */
    public TLCollator() {
        baseCollator = Collator.getInstance(TLContext.getLocale());
    }
    
    /**
     * Delegate to {@link Collator#compare(String, String)}.
     */
	@Override
	public int compare(String o1, String o2) {
		return baseCollator.compare(nonNull(o1), nonNull(o2));
    }

	@Override
	public int hashCode() {
		return 37 * 3 + baseCollator.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TLCollator other = (TLCollator) obj;
		if (!baseCollator.equals(other.baseCollator))
			return false;
		return true;
	}

}

