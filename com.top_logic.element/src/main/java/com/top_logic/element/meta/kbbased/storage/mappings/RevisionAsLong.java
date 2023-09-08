/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.access.StorageMapping;

/**
 * {@link StorageMapping} mapping a {@link Revision} to its {@link Revision#getCommitNumber() commit
 * number}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionAsLong implements StorageMapping<Revision> {

	/** Singleton {@link RevisionAsLong} instance. */
	public static final RevisionAsLong INSTANCE = new RevisionAsLong();

	/**
	 * Creates a new {@link RevisionAsLong}.
	 */
	protected RevisionAsLong() {
		// singleton instance
	}

	@Override
	public Class<? extends Revision> getApplicationType() {
		return Revision.class;
	}

	@Override
	public Revision getBusinessObject(Object aStorageObject) {
		if (aStorageObject == null) {
			return null;
		}
		return HistoryUtils.getRevision((((Number) aStorageObject).longValue()));
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		if (aBusinessObject == null) {
			return null;
		}
		return ((Revision) aBusinessObject).getCommitNumber();
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof Revision;
	}

}

