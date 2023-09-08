/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.util.Resources;

/**
 * Adaption of an annotated {@link ConstraintAlgorithm} implementation to a {@link Constraint}
 * algorithm of the {@link FormMember} hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class DeclarativeConstraint implements Constraint {

	private final PropertyEditModel[] _models;

	private final int _index;

	private final ConstraintAlgorithm _constraint;

	public DeclarativeConstraint(PropertyEditModel[] models, int index, ConstraintAlgorithm constraint) {
		_models = models;
		_index = index;
		_constraint = constraint;
	}

	@Override
	public boolean check(Object value) throws CheckException {
		for (PropertyEditModel editModel : _models) {

			FormField field = editModel.getConstraintField();
			if (field != null && !field.hasValue()) {
				return false;
			}
		}

		_models[_index].setProblemDescription(null);

		_constraint.check(_models);

		ResKey problem = _models[_index].getProblemDescription();
		if (problem != null) {
			throw new CheckException(Resources.getInstance().getString(problem));
		}

		return true;
	}

	@Override
	public Collection<FormField> reportDependencies() {
		int length = _models.length;
		if (length == 1) {
			return Collections.emptyList();
		}
		ArrayList<FormField> result = new ArrayList<>(length - 1);
		for (int k = 0, cnt = length; k < cnt; k++) {
			if (k == _index) {
				continue;
			}
			FormField member = _models[k].getConstraintField();
			if (member != null) {
				result.add(member);
			}
		}
		return result;
	}
}