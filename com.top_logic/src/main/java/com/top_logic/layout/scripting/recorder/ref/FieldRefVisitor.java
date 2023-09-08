/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.layout.scripting.recorder.ref.field.AttributeFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.BusinessObjectFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.FieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.LabeledFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.NamedFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.TableFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.TreeFieldRef;

/**
 * Visitor interface for {@link FieldRef}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FieldRefVisitor {

	/**
	 * Visit case for {@link NamedFieldRef}.
	 */
	Object visitNamedFieldRef(NamedFieldRef ref, Object arg);

	/**
	 * Visit case for {@link AttributeFieldRef}.
	 */
	Object visitAttributeFieldRef(AttributeFieldRef ref, Object arg);

	/**
	 * Visit case for {@link TreeFieldRef}.
	 */
	Object visitTreeFieldRef(TreeFieldRef ref, Object arg);

	/**
	 * Visit case for {@link TableFieldRef}.
	 */
	Object visitTableFieldRef(TableFieldRef value, Object arg);

	/**
	 * Visit case for {@link BusinessObjectFieldRef}.
	 */
	Object visitSubstituteIdFieldRef(BusinessObjectFieldRef value, Object arg);
	
	/**
	 * Visit case for {@link LabeledFieldRef}.
	 */
	Object visitLabeledFieldRef(LabeledFieldRef ref, Object arg);

}
