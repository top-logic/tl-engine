/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;

/**
 * Transient {@link TLClass} type representing the undeclared union of types.
 * 
 * <p>
 * Instead of marking the types in the union explicitly to have the union type as generalization,
 * the union type simply collects the types the union is built from.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLUnionType extends TLClassImpl {

	/**
	 * Creates a {@link TLUnionType}.
	 *
	 * @param specializations
	 *        Explicit specializations. Note: A union type never occurs within the generalizations
	 *        of its specializations.
	 */
	public TLUnionType(TLModel model, TLClass... specializations) {
		super(model, unionName(specializations));

		specializationsInternal.addAll(Arrays.asList(specializations));
	}

	private static String unionName(TLClass[] specializations) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Union<");
		boolean first = true;
		for (TLClass specialization : specializations) {
			if (first) {
				first = false;
			} else {
				buffer.append(",");
			}
			buffer.append(specialization);
		}
		buffer.append(">");
		return buffer.toString();
	}

	@Override
	public Collection<TLClass> getSpecializations() {
		HashSet<TLClass> result = new HashSet<>();
		fillTypes(result);
		return Collections.unmodifiableCollection(result);
	}

	private void fillTypes(Collection<TLClass> result) {
		for (TLClass subtype : specializationsInternal) {
			if (subtype instanceof TLUnionType) {
				((TLUnionType) subtype).fillTypes(result);
			} else {
				result.add(subtype);
			}
		}
	}

}
