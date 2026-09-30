/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.knowledge.gui.layout.upload;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.values.edit.editor.AcceptedTypesChecker;

/**
 * Configuration of accepted file types.
 */
@Abstract
public interface AcceptedFileTypesConfig extends ConfigurationItem {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/** Configuration name for {@link #getAcceptedTypes()}. */
	String ACCEPTED_TYPES_NAME = "accepted-types";

	/**
	 * Comma separated list of accepted file types.
	 * 
	 * <p>
	 * An accepted file type can either be a MIME type like <code>image/*</code> or
	 * <code>image/jpeg</code>, or a file pattern such as <code>*.xml</code>.
	 * </p>
	 * 
	 * @see DataField#getAcceptedTypes()
	 */
	@Name(ACCEPTED_TYPES_NAME)
	@Label("Accepted file types")
	String getAcceptedTypes();

	/**
	 * Applies the {@link #getAcceptedTypes()} to the given {@link DataField}
	 */
	default void applyAcceptedTypes(DataField field) {
		Set<String> acceptedTypesSet = StringServices.toSet(getAcceptedTypes(), ',');
		if (!acceptedTypesSet.isEmpty()) {
			AcceptedTypesChecker checker = new AcceptedTypesChecker(acceptedTypesSet);
			FileNameConstraint constraint = new FileNameConstraint(checker);
			field.setFileNameConstraint(constraint);
			field.setAcceptedTypes(checker.getAcceptedTypesAsString(true));
		}
	}
}
