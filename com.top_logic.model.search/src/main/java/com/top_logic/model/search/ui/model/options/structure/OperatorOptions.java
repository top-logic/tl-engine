/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options.structure;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function3;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.CombinedOperator;
import com.top_logic.model.search.ui.model.operator.Empty;
import com.top_logic.model.search.ui.model.operator.NotEmpty;
import com.top_logic.model.search.ui.model.operator.Operator;

/**
 * Option provider for {@link Operator}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class OperatorOptions extends Function3<List<Operator<?>>, TLType, Boolean, String> {

	/** The {@link OperatorOptions} instance. */
	public static final OperatorOptions INSTANCE = new OperatorOptions();

	@Override
	public List<Operator<?>> apply(TLType type, Boolean multiplicity, String searchName) {
		List<Operator<?>> allOperators = new ArrayList<>();
		if (!containsNull(type, multiplicity, searchName)) {
			addComparisonsForType(allOperators, type, multiplicity, searchName);
			allOperators.add(TypedConfiguration.newConfigItem(CombinedOperator.class));
		}
		return allOperators;
	}

	/**
	 * Add available {@link Operator}s for a given type.
	 * 
	 * @param result
	 *        List to append to.
	 * @param type
	 *        The type of object to compare.
	 * @param multiple
	 *        Whether multiple values are compared.
	 */
	private static void addComparisonsForType(List<Operator<?>> result, TLType type, boolean multiple,
			String searchName) {
		result.addAll(getUniversalOperators(type, multiple));
		result.addAll(typeSpecificComparisons(type, multiple, searchName));
	}

	private static List<Operator<?>> getUniversalOperators(TLType type, boolean multiple) {
		List<Operator<?>> genericOperators = new ArrayList<>();
		genericOperators.add(isEmpty());
		genericOperators.add(notEmpty());
		if ((!multiple) && !isCollectionCompareBroken(type)) {
			// ContainedIn for TLPrimitive values
		}
		return genericOperators;
	}

	/**
	 * Temporary workaround for floating point numbers and {@link Date}s: Comparing them requires
	 * special code: Rounding (for floats) or normalizing to noon (for dates). As there is no such
	 * special code for collections of primitive values yet, they are excluded here.
	 */
	private static boolean isCollectionCompareBroken(TLType type) {
		if (!(type instanceof TLPrimitive)) {
			return false;
		}
		TLPrimitive primitiveType = (TLPrimitive) type;
		return (primitiveType.getKind() == TLPrimitive.Kind.DATE)
			|| (primitiveType.getKind() == TLPrimitive.Kind.FLOAT);
	}

	private static List<Operator<?>> typeSpecificComparisons(TLType type, boolean multiple, String searchName) {
		return type.visitType(new TypeSpecificOperatorOptions(multiple, searchName), null);
	}

	private static Empty isEmpty() {
		return TypedConfiguration.newConfigItem(Empty.class);
	}

	private static NotEmpty notEmpty() {
		return TypedConfiguration.newConfigItem(NotEmpty.class);
	}

}
