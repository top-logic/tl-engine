/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.migration.data;

import com.top_logic.model.util.TLModelUtil;

/**
 * Readable <code>toString</code> computation for {@link BranchIdType}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ToString implements BranchIdTypeVisitor<Void, StringBuilder> {

	/** Singleton {@link ToString} instance. */
	public static final ToString INSTANCE = new ToString();

	/**
	 * Creates a new {@link ToString}.
	 */
	protected ToString() {
		// singleton instance
	}

	@Override
	public Void visitBranchIdType(BranchIdType self, StringBuilder arg) {
		arg.append(self.toString());
		return null;
	}

	@Override
	public Void visitModule(Module self, StringBuilder arg) {
		arg.append(self.getModuleName());
		appendID(arg, self);
		return null;
	}

	void appendID(StringBuilder out, BranchIdType bit) {
		out.append(" (").append(bit.getID()).append(")");
	}

	@Override
	public Void visitType(Type self, StringBuilder arg) {
		arg.append(self.getModule().getModuleName());
		arg.append(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		arg.append(self.getTypeName());
		appendID(arg, self);
		return null;
	}

	@Override
	public Void visitTypePart(TypePart self, StringBuilder arg) {
		arg.append(self.getOwner().getModule().getModuleName());
		arg.append(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		arg.append(self.getOwner().getTypeName());
		arg.append(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR);
		arg.append(self.getPartName());
		appendID(arg, self);
		return null;
	}

}

