/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.field;

import java.util.List;

import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * A reference to a {@link FormField} in an Tree. The field is identified by the path (of parents)
 * to it.
 * 
 * Root is not contained in the path and represented by an empty path.
 */
public interface TreeFieldRef extends FieldRef {

	@EntryTag("node")
	List<ModelName> getPath();

	void setPath(List<? extends ModelName> treePath);

}
