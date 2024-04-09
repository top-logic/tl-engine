/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config.visit;

import com.top_logic.element.model.diff.config.AddAnnotations;
import com.top_logic.element.model.diff.config.AddGeneralization;
import com.top_logic.element.model.diff.config.CreateClassifier;
import com.top_logic.element.model.diff.config.CreateModule;
import com.top_logic.element.model.diff.config.CreateRole;
import com.top_logic.element.model.diff.config.CreateSingleton;
import com.top_logic.element.model.diff.config.CreateStructuredTypePart;
import com.top_logic.element.model.diff.config.CreateType;
import com.top_logic.element.model.diff.config.Delete;
import com.top_logic.element.model.diff.config.DeleteRole;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.element.model.diff.config.MakeAbstract;
import com.top_logic.element.model.diff.config.MakeConcrete;
import com.top_logic.element.model.diff.config.MoveClassifier;
import com.top_logic.element.model.diff.config.MoveGeneralization;
import com.top_logic.element.model.diff.config.MoveStructuredTypePart;
import com.top_logic.element.model.diff.config.RemoveAnnotation;
import com.top_logic.element.model.diff.config.RemoveGeneralization;
import com.top_logic.element.model.diff.config.RenamePart;
import com.top_logic.element.model.diff.config.SetAnnotations;
import com.top_logic.element.model.diff.config.UpdateMandatory;
import com.top_logic.element.model.diff.config.UpdatePartType;
import com.top_logic.element.model.diff.config.UpdateStorageMapping;

/**
 * Visitor interface for the {@link DiffElement} hierarchy.
 * 
 * @param <R>
 *        The result of the visit.
 * @param <A>
 *        The argument to the visit.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DiffVisitor<R, A, E extends Throwable> {

	/** Visit case for {@link CreateModule}. */
	R visit(CreateModule diff, A arg) throws E;

	/** Visit case for {@link CreateSingleton}. */
	R visit(CreateSingleton diff, A arg) throws E;

	/** Visit case for {@link CreateRole}. */
	R visit(CreateRole diff, A arg) throws E;

	/** Visit case for {@link DeleteRole}. */
	R visit(DeleteRole diff, A arg) throws E;

	/** Visit case for {@link CreateType}. */
	R visit(CreateType diff, A arg) throws E;

	/** Visit case for {@link CreateStructuredTypePart}. */
	R visit(CreateStructuredTypePart diff, A arg) throws E;

	/** Visit case for {@link CreateClassifier}. */
	R visit(CreateClassifier diff, A arg) throws E;

	/** Visit case for {@link Delete}. */
	R visit(Delete diff, A arg) throws E;

	/** Visit case for {@link AddAnnotations}. */
	R visit(AddAnnotations diff, A arg) throws E;

	/** Visit case for {@link SetAnnotations}. */
	R visit(SetAnnotations diff, A arg) throws E;

	/** Visit case for {@link RemoveAnnotation}. */
	R visit(RemoveAnnotation diff, A arg) throws E;

	/** Visit case for {@link AddGeneralization}. */
	R visit(AddGeneralization diff, A arg) throws E;

	/** Visit case for {@link RemoveGeneralization}. */
	R visit(RemoveGeneralization diff, A arg) throws E;

	/** Visit case for {@link MoveGeneralization}. */
	R visit(MoveGeneralization diff, A arg) throws E;

	/** Visit case for {@link MakeAbstract}. */
	R visit(MakeAbstract diff, A arg) throws E;

	/** Visit case for {@link MakeConcrete}. */
	R visit(MakeConcrete diff, A arg) throws E;

	/** Visit case for {@link UpdateMandatory}. */
	R visit(UpdateMandatory diff, A arg) throws E;

	/** Visit case for {@link MoveClassifier}. */
	R visit(MoveClassifier diff, A arg) throws E;

	/** Visit case for {@link MoveStructuredTypePart}. */
	R visit(MoveStructuredTypePart diff, A arg) throws E;

	/** Visit case for {@link RenamePart}. */
	R visit(RenamePart diff, A arg) throws E;

	/** Visit case for {@link UpdatePartType}. */
	R visit(UpdatePartType diff, A arg) throws E;

	/** Visit case for {@link UpdateStorageMapping}. */
	R visit(UpdateStorageMapping diff, A arg) throws E;

}
