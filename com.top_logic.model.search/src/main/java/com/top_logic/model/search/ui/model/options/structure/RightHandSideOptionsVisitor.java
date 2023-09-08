/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options.structure;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLTypeVisitor;
import com.top_logic.model.search.annotate.TLSearchOptions;
import com.top_logic.model.search.ui.model.NavigationValue;
import com.top_logic.model.search.ui.model.QueryValue;
import com.top_logic.model.search.ui.model.SubQuery;
import com.top_logic.model.search.ui.model.literal.LiteralBooleanValue;
import com.top_logic.model.search.ui.model.literal.LiteralDateValue;
import com.top_logic.model.search.ui.model.literal.LiteralFloatValue;
import com.top_logic.model.search.ui.model.literal.LiteralIntegerValue;
import com.top_logic.model.search.ui.model.literal.LiteralObjectSet;
import com.top_logic.model.search.ui.model.literal.LiteralObjectValue;
import com.top_logic.model.search.ui.model.literal.LiteralStringValue;
import com.top_logic.model.search.ui.model.structure.RightHandSide;

/**
 * A {@link TLTypeVisitor} that calculates the {@link RightHandSideOptions}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class RightHandSideOptionsVisitor implements TLTypeVisitor<List<RightHandSide>, Boolean> {

	/** Returns the {@link RightHandSideOptionsVisitor} instance. */
	public static final RightHandSideOptionsVisitor INSTANCE = new RightHandSideOptionsVisitor();

	@Override
	public List<RightHandSide> visitPrimitive(TLPrimitive type, Boolean multiple) {
		List<RightHandSide> result = new ArrayList<>();
		if (multiple) {
			result.add(newConfigItem(NavigationValue.class));
			result.add(newConfigItem(QueryValue.class));
		} else {
			result.addAll(getPrimitiveLiterals(type));
			result.add(newConfigItem(NavigationValue.class));
		}
		return result;
	}

	private List<RightHandSide> getPrimitiveLiterals(TLPrimitive type) {
		TLSearchOptions annotation = type.getAnnotation(TLSearchOptions.class);
		if (annotation != null) {
			return annotation.getLiterals().get();
		}
		switch (type.getKind()) {
			case BOOLEAN:
			case TRISTATE:
				return singletonList(newConfigItem(LiteralBooleanValue.class));
			case DATE:
				return singletonList(newConfigItem(LiteralDateValue.class));
			case FLOAT:
				return singletonList(newConfigItem(LiteralFloatValue.class));
			case INT:
				return singletonList(newConfigItem(LiteralIntegerValue.class));
			case STRING:
				return singletonList(newConfigItem(LiteralStringValue.class));
			case CUSTOM:
			case BINARY:
				return emptyList();
			default: {
				throw new UnreachableAssertion("Unknown TLPrimitive: " + type);
			}
		}
	}

	@Override
	public List<RightHandSide> visitEnumeration(TLEnumeration type, Boolean multiple) {
		List<RightHandSide> result = getObjectOptions(multiple);
		if (multiple) {
			result.add(newConfigItem(QueryValue.class));
		}
		return result;
	}

	@Override
	public List<RightHandSide> visitClass(TLClass type, Boolean multiple) {
		List<RightHandSide> result = getObjectOptions(multiple);
		if (multiple) {
			result.add(newConfigItem(SubQuery.class));
		}
		return result;
	}

	private List<RightHandSide> getObjectOptions(Boolean multiple) {
		List<RightHandSide> result = new ArrayList<>();
		if (multiple) {
			result.add(newConfigItem(LiteralObjectSet.class));
		} else {
			result.add(newConfigItem(LiteralObjectValue.class));
		}
		result.add(newConfigItem(NavigationValue.class));
		return result;
	}

	@Override
	public List<RightHandSide> visitAssociation(TLAssociation type, Boolean multiple) {
		throw new UnsupportedOperationException("Associations are not yet supported in the search.");
	}

}
