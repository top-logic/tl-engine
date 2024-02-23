/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter;

import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.model.config.annotation.MainProperties;
import com.top_logic.model.impl.TLUnionType;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.All;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.ArithmeticExpr;
import com.top_logic.model.search.expr.AssociationNavigation;
import com.top_logic.model.search.expr.At;
import com.top_logic.model.search.expr.Block;
import com.top_logic.model.search.expr.Call;
import com.top_logic.model.search.expr.Compare;
import com.top_logic.model.search.expr.CompareOp;
import com.top_logic.model.search.expr.ContainsAll;
import com.top_logic.model.search.expr.ContainsElement;
import com.top_logic.model.search.expr.ContainsSome;
import com.top_logic.model.search.expr.Desc;
import com.top_logic.model.search.expr.Filter;
import com.top_logic.model.search.expr.Flatten;
import com.top_logic.model.search.expr.Foreach;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.GetDay;
import com.top_logic.model.search.expr.IfElse;
import com.top_logic.model.search.expr.InstanceOf;
import com.top_logic.model.search.expr.Intersection;
import com.top_logic.model.search.expr.IsEmpty;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.IsStringEqual;
import com.top_logic.model.search.expr.IsStringGreater;
import com.top_logic.model.search.expr.KBQuery;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.Length;
import com.top_logic.model.search.expr.ListExpr;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.Not;
import com.top_logic.model.search.expr.Or;
import com.top_logic.model.search.expr.Recursion;
import com.top_logic.model.search.expr.Referers;
import com.top_logic.model.search.expr.Round;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SingleElement;
import com.top_logic.model.search.expr.Singleton;
import com.top_logic.model.search.expr.Size;
import com.top_logic.model.search.expr.Sort;
import com.top_logic.model.search.expr.StringContains;
import com.top_logic.model.search.expr.StringEndsWith;
import com.top_logic.model.search.expr.StringStartsWith;
import com.top_logic.model.search.expr.TupleExpression;
import com.top_logic.model.search.expr.TupleExpression.Coord;
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.html.AttributeMacro;
import com.top_logic.model.search.expr.html.HtmlMacro;
import com.top_logic.model.search.expr.html.TagMacro;
import com.top_logic.model.search.expr.visit.DescendingVisitor;
import com.top_logic.model.search.expr.visit.Visitor;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link Visitor} computing {@link SearchExpression} types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeResolver extends DescendingVisitor<TLType, TLType> {

	private static final TypedAnnotatable.Property<TLType> TYPE = TypedAnnotatable.property(TLType.class, "type");

	private TLModel _model;

	private TLType _any;

	/**
	 * The {@link TLType} of the evaluation result of this {@link SearchExpression}.
	 */
	public static TLType getType(SearchExpressionPart part) {
		return part.get(TYPE);
	}

	/**
	 * @see #getType(SearchExpressionPart)
	 */
	private TLType setType(SearchExpressionPart part, TLType type) {
		if (type == null) {
			type = any();
		}
		part.set(TYPE, type);
		return type;
	}

	private TLType any() {
		if (_any == null) {
			TLModule utilModule = _model.getModule("tl.util");
			if (utilModule != null) {
				_any = utilModule.getType("Any");
			}
		}
		return _any;
	}

	/**
	 * Creates a {@link TypeResolver}.
	 */
	public TypeResolver(TLModel model) {
		_model = model;
	}

	@Override
	protected TLType composeTuple(TupleExpression expr, TLType arg, List<TLType> results) {
		for (int cnt = expr.getCoords().length, n = 0; n < cnt; n++) {
			setType(expr.getCoords()[n], results.get(n));
		}
		return setType(expr, tupleType(expr, results));
	}

	private TLType tupleType(TupleExpression expr, List<TLType> coordTypes) {
		TLClass result = TransientModelFactory.createTransientClass(_model, tupleTypeName(coordTypes));

		MainProperties mainProperties = TypedConfiguration.newConfigItem(MainProperties.class);

		StringBuilder buffer = new StringBuilder();
		buffer.append("(");
		boolean first = true;
		Iterator<TLType> types = coordTypes.iterator();
		for (Coord coord : expr.getCoords()) {
			String name = coord.getName().toString();

			mainProperties.getProperties().add(name);

			if (first) {
				first = false;
			} else {
				buffer.append(", ");
			}
			buffer.append(name);

			TLType coordType = types.next();
			if (coordType == null) {
				return null;
			}
			TLClassProperty property = TransientModelFactory.addClassProperty(result, name, coordType);
			setLabel(property, name);
		}
		buffer.append(")");
		result.setAnnotation(mainProperties);
		setLabel(result, buffer.toString());
		return result;
	}

	private void setLabel(TLModelPart part, String label) {
		TLI18NKey key = TypedConfiguration.newConfigItem(TLI18NKey.class);
		key.setValue(ResKey.text(label));
		part.setAnnotation(key);
	}

	private String tupleTypeName(List<TLType> coordTypes) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Tuple<");
		boolean first = true;
		for (TLType type : coordTypes) {
			if (first) {
				first = false;
			} else {
				buffer.append(",");
			}
			buffer.append(type.toString());
		}
		buffer.append(">");
		return buffer.toString();
	}

	@Override
	protected TLType composeNot(Not expr, TLType arg, TLType argumentResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeArithmetic(ArithmeticExpr expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setType(expr, leftResult);
	}

	@Override
	protected TLType composeEquals(IsEqual expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeCompareOp(CompareOp expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeCompare(Compare expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setIntType(expr);
	}

	@Override
	protected TLType composeRound(Round expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setFloatType(expr);
	}

	@Override
	protected TLType composeGetDay(GetDay expr, TLType arg, TLType argumentResult) {
		return setDateType(expr);
	}

	@Override
	protected TLType composeStringEquals(IsStringEqual expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeStringGreater(IsStringGreater expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeStringContains(StringContains expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeStringStartsWith(StringStartsWith expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeStringEndsWith(StringEndsWith expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeContainsAll(ContainsAll expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeContainsElement(ContainsElement expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeContainsSome(ContainsSome expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeInstanceOf(InstanceOf expr, TLType arg, TLType valueResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeAccess(Access expr, TLType arg, TLType selfResult) {
		return setType(expr, expr.getPart().getType());
	}

	@Override
	protected TLType composeAt(At expr, TLType arg, TLType self, TLType index) {
		return setType(expr, self);
	}

	@Override
	protected TLType composeAssociationNavigation(AssociationNavigation expr, TLType arg, TLType sourceResult) {
		return setType(expr, expr.getDestinationEnd().getType());
	}

	@Override
	protected TLType composeLambda(Lambda expr, TLType arg, TLType bodyResult) {
		return setType(expr, bodyResult);
	}

	@Override
	protected TLType composeVar(Var expr, TLType arg) {
		return setType(expr, TypeResolver.getType(expr.getDef()));
	}

	@Override
	protected TLType composeUnion(Union expr, TLType arg, TLType leftResult, TLType rightResult) {
		if (leftResult == null || rightResult == null) {
			return setNoType(expr);
		}
		if (leftResult.getModelKind() == ModelKind.CLASS && rightResult.getModelKind() == ModelKind.CLASS) {
			return setType(expr, new TLUnionType(_model, (TLClass) leftResult, (TLClass) rightResult));
		} else {
			return setNoType(expr);
		}
	}

	private TLType setBooleanType(SearchExpression expr) {
		return setType(expr, booleanType());
	}

	private TLType setIntType(SearchExpression expr) {
		return setType(expr, intType());
	}

	private TLType setFloatType(SearchExpression expr) {
		return setType(expr, floatType());
	}

	private TLType setDateType(SearchExpression expr) {
		return setType(expr, dateType());
	}

	private TLType booleanType() {
		return TLModelUtil.findType(_model, TypeSpec.BOOLEAN_TYPE);
	}

	private TLType intType() {
		return TLModelUtil.findType(_model, TypeSpec.INTEGER_TYPE);
	}

	@Override
	protected TLType composeUpdate(Update expr, TLType arg, TLType selfResult, TLType valueResult) {
		return setNoType(expr);
	}

	/**
	 * @param expr
	 *        The expression for which no type was found.
	 */
	private TLType setNoType(SearchExpression expr) {
		return null;
	}

	@Override
	protected TLType composeCall(Call expr, TLType arg, TLType functionResult, TLType argumentResult) {
		return setType(expr, functionResult);
	}

	@Override
	protected TLType composeRecursion(Recursion expr, TLType arg, TLType startResult, TLType functionResult,
			TLType minDepthResult, TLType maxDepthResult) {
		return setType(expr, startResult);
	}

	@Override
	protected TLType composeIntersection(Intersection expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setType(expr, leftResult);
	}

	@Override
	protected TLType composeFilter(Filter expr, TLType arg, TLType baseResult, TLType functionResult) {
		return setType(expr, baseResult);
	}

	@Override
	protected TLType composeGenericMethod(GenericMethod expr, TLType arg, List<TLType> argumentsResult) {
		return setType(expr, expr.getType(argumentsResult));
	}

	@Override
	protected TLType composeAnd(And expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeOr(Or expr, TLType arg, TLType leftResult, TLType rightResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeIfElse(IfElse expr, TLType arg, TLType conditionResult, TLType ifResult,
			TLType elseResult) {
		return setType(expr, ifResult);
	}

	@Override
	protected TLType composeForeach(Foreach expr, TLType arg, TLType baseResult, TLType functionResult) {
		return setType(expr, functionResult);
	}

	@Override
	protected TLType composeAll(All expr, TLType arg) {
		return setType(expr, expr.getInstanceType());
	}

	@Override
	protected TLType composeSource(KBQuery expr, TLType arg) {
		return setType(expr, expr.getClassType());
	}

	@Override
	protected TLType composeBlock(Block expr, TLType arg, List<TLType> results) {
		return setNoType(expr);
	}

	@Override
	protected TLType composeReferers(Referers expr, TLType arg, TLType targetResult) {
		return setType(expr, expr.getReference().getOwner());
	}

	@Override
	protected TLType composeLiteral(Literal expr, TLType arg) {
		Object value = expr.getValue();
		if (value == null) {
			return setNoType(expr);
		} else if (value instanceof Boolean) {
			return setBooleanType(expr);
		} else if (value instanceof String) {
			return setType(expr, stringType());
		} else if (value instanceof Number) {
			return setType(expr, floatType());
		}
		return setNoType(expr);
	}

	@Override
	protected TLType composeHtml(HtmlMacro expr, TLType arg, List<TLType> contentsResult) {
		return setNoType(expr);
	}

	@Override
	protected TLType composeTag(TagMacro expr, TLType arg, List<TLType> attributesResult) {
		return setNoType(expr);
	}

	@Override
	protected TLType composeAttr(AttributeMacro expr, TLType arg, TLType valueResult) {
		return setNoType(expr);
	}

	private TLType stringType() {
		return TLModelUtil.findType(_model, TypeSpec.STRING_TYPE);
	}

	private TLType floatType() {
		return TLModelUtil.findType(_model, "tl.core:Float");
	}

	private TLType dateType() {
		return TLModelUtil.findType(_model, TypeSpec.DATE_TYPE);
	}

	@Override
	protected TLType composeIsEmpty(IsEmpty expr, TLType arg, TLType argumentResult) {
		return setBooleanType(expr);
	}

	@Override
	protected TLType composeFlatten(Flatten expr, TLType arg, TLType argumentResult) {
		return setType(expr, argumentResult);
	}

	@Override
	protected TLType composeSingleton(Singleton expr, TLType arg, TLType argumentResult) {
		return setType(expr, argumentResult);
	}

	@Override
	protected TLType composeSingleElement(SingleElement expr, TLType arg, TLType argumentResult) {
		return setType(expr, argumentResult);
	}

	@Override
	protected TLType composeDesc(Desc expr, TLType arg, TLType keyResult) {
		return setType(expr, keyResult);
	}

	@Override
	protected TLType composeLength(Length expr, TLType arg, TLType stringResult) {
		return setIntType(expr);
	}

	@Override
	protected TLType composeSize(Size expr, TLType arg, TLType listResult) {
		return setIntType(expr);
	}

	@Override
	protected TLType composeList(ListExpr expr, TLType arg, List<TLType> elementsResult) {
		return elementsResult.isEmpty() ? setNoType(expr) : setType(expr, elementsResult.get(0));
	}

	@Override
	protected TLType composeSort(Sort expr, TLType arg, TLType listResult, TLType keyFunResult) {
		return setType(expr, listResult);
	}

}