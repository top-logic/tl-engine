/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.util.List;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.field.FieldRef;

/**
 * Base interface for {@link ModelName}s referencing {@link FormMember}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface FormMemberBasedName extends ModelName {

	/** The path to the {@link FormMember} in descending order. (Outermost parent first) */
	@EntryTag("node")
	List<FieldRef> getPath();

	/** @see #getPath() */
	void setPath(List<FieldRef> path);

	/**
	 * The {@link ModelName} for the {@link FormHandler} responsible for the {@link FormMember}
	 * identified by this {@link FormMemberBasedName}.
	 */
	ModelName getFormHandlerName();

	/** @see #getFormHandlerName() */
	void setFormHandlerName(ModelName formHandlerName);

}
