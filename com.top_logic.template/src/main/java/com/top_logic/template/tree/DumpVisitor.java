/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.template.tree.parameter.ListParameterValue;
import com.top_logic.template.tree.parameter.ParameterValue;
import com.top_logic.template.tree.parameter.PrimitiveParameterValue;
import com.top_logic.template.tree.parameter.StructuredParameterValue;

/**
 * {@link TemplateVisitor} that creates a dump of a template syntax tree for debugging.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class DumpVisitor implements TemplateVisitor<Void, StringBuilder> {

	public static StringBuilder toString(TemplateNode node) {
		StringBuilder buffer = new StringBuilder();
		node.visit(new DumpVisitor(), buffer);
		return buffer;
	}

	@Override
	public Void visitAssignStatement(AssignStatement node, StringBuilder arg) {
		arg.append("\nAssignment: ");
		node.getExpression().visit(this, arg);
		arg.append("\n");
		arg.append("  attributes: ");
		if (!CollectionUtil.isEmptyOrNull(node.getAttributes())){
			Set<String> keys = node.getAttributes().keySet();
			for(Iterator<String> theIt = keys.iterator(); theIt.hasNext();) {
				String theKey = theIt.next();
				arg.append(theKey + " = " + node.getAttributes().get(theKey));
				if (theIt.hasNext()) {
					arg.append(", ");
				}
			}
		}
		return none;
	}

	@Override
	public Void visitBinaryExpression(BinaryExpression node, StringBuilder arg) {
		arg.append("B-Expr: ");
		node.getLeftExpression().visit(this, arg);
		arg.append(" " + node.getOperator() + " ");
		node.getRightExpression().visit(this, arg);
		return none;
	}

	@Override
	public Void visitConstantExpression(ConstantExpression node, StringBuilder arg) {
		return none;
	}
	
	@Override
	public Void visitUnaryExpression(UnaryExpression node, StringBuilder arg) {
		return none;
	}

	@Override
	public Void visitForeachStatement(ForeachStatement node, StringBuilder arg) {
		arg.append("\nForeach:\n");
		arg.append("  identifier: ");
		String theKey = node.getVariable();
		arg.append(theKey + " IN ");
		node.getExpression().visit(this, arg);
		arg.append("\n");
		arg.append("  attributes: ");
		Set<String> keys = node.getAttributes().keySet();
		for(Iterator<String> theIt = keys.iterator(); theIt.hasNext();) {
			theKey = theIt.next();
			arg.append(theKey + " = " + node.getAttributes().get(theKey));
			if (theIt.hasNext()) {
				arg.append(", ");
			}
		}
		arg.append("\n");
		arg.append("-- production --\n");
		node.getProductionNode().visit(this, arg);
		arg.append("\n-- end prod --\n");
		return none;
	}

	@Override
	public Void visitFunctionCall(FunctionCall node, StringBuilder arg) {
		arg.append("FunctionCall: \n  ");
		arg.append(node.getName() + "(");
		for (Iterator<Expression> theIt = node.getArguments().iterator(); theIt.hasNext();) {
			theIt.next().visit(this, arg);
			if (theIt.hasNext()) {
				arg.append(", ");
			}
		}
		arg.append(")");
		return none;
	}

	@Override
	public Void visitLiteralText(LiteralText node, StringBuilder arg) {
		arg.append(node.getValue());
		return none;
	}

	@Override
	public Void visitReference(Reference node, StringBuilder arg) {
		arg.append(node.getFullQualifiedName());
		return none;
	}

	@Override
	public Void visitIfStatement(IfStatement node, StringBuilder arg) {
		arg.append("If:\n");
		arg.append("  condition: ");
		node.getCondition().visit(this, arg);
		arg.append("\n");
		arg.append("  attributes: ");
		Set<String> keys = node.getAttributes().keySet();
		for(Iterator<String> theIt = keys.iterator(); theIt.hasNext();) {
			String theKey = theIt.next();
			arg.append(theKey + " = " + node.getAttributes().get(theKey));
			if (theIt.hasNext()) {
				arg.append(", ");
			}
		}
		arg.append("\n");
		TemplateNode thenStm = node.getThenStm();
		if (thenStm != null) {
			arg.append("-- then --\n");
			thenStm.visit(this, arg);
			arg.append("\n-- end then --");
		}
		TemplateNode elseStm = node.getElseStm();
		if (elseStm != null) {
			arg.append("-- else --\n");
			elseStm.visit(this, arg);
			arg.append("\n-- end else --");
		}
		return none;
	}

	@Override
	public Void visitTemplate(Template node, StringBuilder arg) {
		for (TemplateNode n:node.getNodes()) {
			n.visit(this, arg);
		}
		return none;
	}

	@Override
	public Void visitDefineStatement(DefineStatement node, StringBuilder arg) {
		arg.append("\nDefine:\n");
		arg.append("  identifier: ");
		String theKey = node.getVariable();
		arg.append(theKey + " AS ");
		node.getExpression().visit(this, arg);
		arg.append("\n");
		arg.append("  attributes: ");
		Set<String> keys = node.getAttributes().keySet();
		for(Iterator<String> theIt = keys.iterator(); theIt.hasNext();) {
			theKey = theIt.next();
			arg.append(theKey + " = " + node.getAttributes().get(theKey));
			if (theIt.hasNext()) {
				arg.append(", ");
			}
		}
		arg.append("\n");
		return none;
	}

	@Override
	public Void visitInvokeStatement(InvokeStatement node, StringBuilder result) {
		result.append("\n");
		result.append("Template Invocation:\n");
		result.append("  Template: '").append(node.getTemplateSource()).append("'\n");
		result.append("  Parameters:\n");
		dumpParameterValues(node.getParameterValues(), result);
		return none;
	}

	private void dumpParameterValues(Map<String, ParameterValue<TemplateNode>> parameters, StringBuilder result) {
		for (Map.Entry<String, ParameterValue<TemplateNode>> parameter : parameters.entrySet()) {
			ParameterValue<TemplateNode> value = parameter.getValue();
			if (value.isPrimitiveValue()) {
				result.append("    Parameter ").append(parameter.getKey()).append(":\n");
				dumpPrimitiveParameterValue(value.asPrimitiveValue(), result);
			} else if (value.isStructuredValue()) {
				result.append("    Parameter Structure ").append(parameter.getKey()).append(":\n");
				StructuredParameterValue<TemplateNode> parameterValue = value.asStructuredValue();
				dumpStructuredParameterValue(parameterValue, result);
				result.append("    End Parameter Structure ").append(parameter.getKey()).append("\n");
			} else if (value.isListValue()) {
				result.append("    Parameter List ").append(parameter.getKey()).append(":\n");
				ListParameterValue<TemplateNode> parameterValue = value.asListValue();
				dumpListParameterValue(parameterValue, result);
				result.append("    End Parameter List ").append(parameter.getKey()).append("\n");
			} else {
				throw new UnsupportedOperationException("Unknown " + ParameterValue.class.getSimpleName()
					+ " instance: " + StringServices.getObjectDescription(value));
			}
			result.append("\n");
		}
	}

	private void dumpPrimitiveParameterValue(
			PrimitiveParameterValue<TemplateNode> parameterValue, StringBuilder result) {
		TemplateNode value = parameterValue.getPrimitiveValue();
		value.visit(this, result);
	}

	private void dumpStructuredParameterValue(
			StructuredParameterValue<TemplateNode> parameterValue, StringBuilder result) {
		Map<String, ParameterValue<TemplateNode>> structuredValue = parameterValue.getStructuredValue();
		dumpParameterValues(structuredValue, result);
	}

	private void dumpListParameterValue(ListParameterValue<TemplateNode> parameterValue, StringBuilder result) {
		for (ParameterValue<TemplateNode> valueEntry : parameterValue.getListValue()) {
			if (valueEntry.isPrimitiveValue()) {
				result.append("    Parameter:\n");
				dumpPrimitiveParameterValue(valueEntry.asPrimitiveValue(), result);
			} else if (valueEntry.isStructuredValue()) {
				result.append("    Parameter Structure:\n");
				dumpStructuredParameterValue(valueEntry.asStructuredValue(), result);
				result.append("    End Parameter Structure\n");
			} else if (valueEntry.isListValue()) {
				throw new UnreachableAssertion("A list cannot contain another list directly.");
			} else {
				throw new UnsupportedOperationException("Unknown " + ParameterValue.class.getSimpleName()
					+ " instance: " + StringServices.getObjectDescription(valueEntry));
			}
		}
	}

	@Override
	public Void visitAttributeValue(AttributeValue attributeValue, StringBuilder result) {
		result.append("Attribute Value:\n");
		for (TemplateNode node : attributeValue.getContent()) {
			node.visit(this, result);
		}
		result.append("End Attribute Value\n");
		return none;
	}

}
