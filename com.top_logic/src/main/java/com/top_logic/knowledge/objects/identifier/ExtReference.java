/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.identifier;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.TLID;

/**
 * Object representing a reference to an external object.
 * 
 * <p>
 * Variant of {@link ObjectBranchId} using an {@link ExtID} instead of a {@link TLID}.
 * </p>
 * 
 * @see ObjectBranchId
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExtReference {

	private final long _branchId;

	private final String _objectType;

	private final ExtID _objectName;

	/**
	 * Creates a new {@link ExtReference}.
	 * 
	 * @param branchId
	 *        The branch where the object lives.
	 * @param externalType
	 *        The external type of the object.
	 * @param objectName
	 *        The external Id of the object.
	 */
	public ExtReference(long branchId, String externalType, ExtID objectName) {
		_branchId = branchId;
		_objectType = externalType;
		_objectName = objectName;
	}

	/**
	 * The branch where the object lives.
	 */
	public long getBranchId() {
		return _branchId;
	}

	/**
	 * The external type of the object.
	 */
	public String getObjectType() {
		return _objectType;
	}

	/**
	 * The external Id of the object.
	 */
	public ExtID getObjectName() {
		return _objectName;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(this, obj);
	}

	static boolean equals(ExtReference self, Object other) {
		if (other == self) {
			return true;
		}

		if (!(other instanceof ExtReference)) {
			return false;
		}

		ExtReference otherId = (ExtReference) other;

		return equalsObjectId(self, otherId);
	}

	static boolean equalsObjectId(ExtReference self, ExtReference otherId) {
		boolean result =
			self.getObjectName().equals(otherId.getObjectName()) &&
				self.getBranchId() == otherId.getBranchId() &&
				self.getObjectType().equals(otherId.getObjectType());

		return result;
	}

	@Override
	public int hashCode() {
		return hashCode(this);
	}
	static int hashCode(ExtReference self) {
		int result = self.getObjectType().hashCode();
		result += 44641 * self.getObjectName().hashCode();
		result += 16661 * self.getBranchId();
		return result;
	}

	@Override
	public final String toString() {
		return ExtReferenceFormat.INSTANCE.format(this);
	}

}

