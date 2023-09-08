/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import java.util.List;
import java.util.Map.Entry;

import com.top_logic.basic.config.template.TemplateExpression.Alternative;
import com.top_logic.basic.config.template.TemplateExpression.Choice;
import com.top_logic.basic.config.template.TemplateExpression.CollectionAccess;
import com.top_logic.basic.config.template.TemplateExpression.ConfigExpression;
import com.top_logic.basic.config.template.TemplateExpression.Foreach;
import com.top_logic.basic.config.template.TemplateExpression.FunctionCall;
import com.top_logic.basic.config.template.TemplateExpression.LiteralInt;
import com.top_logic.basic.config.template.TemplateExpression.LiteralText;
import com.top_logic.basic.config.template.TemplateExpression.PropertyAccess;
import com.top_logic.basic.config.template.TemplateExpression.SelfAccess;
import com.top_logic.basic.config.template.TemplateExpression.Tag;
import com.top_logic.basic.config.template.TemplateExpression.Template;
import com.top_logic.basic.config.template.TemplateExpression.TemplateReference;
import com.top_logic.basic.config.template.TemplateExpression.TemplateSequence;
import com.top_logic.basic.config.template.TemplateExpression.VariableAccess;
import com.top_logic.basic.config.template.parser.ConfigTemplateParser;

/**
 * {@link TemplateVisitor} that creates a textual representation for the template compatible with
 * {@link ConfigTemplateParser}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToString implements TemplateVisitor<StringBuilder, StringBuilder, RuntimeException> {

	boolean _literalContext = true;

	private TemplateVisitor<StringBuilder, StringBuilder, RuntimeException> _inner =
		new TemplateVisitor<>() {

		@Override
		public StringBuilder visitSelfAccess(SelfAccess expr, StringBuilder arg) {
			arg.append("this");
			return arg;
		}

		@Override
		public StringBuilder visitPropertyAccess(PropertyAccess expr, StringBuilder arg) {
			visitContents(expr.getTarget(), arg);
			arg.append('.');
			arg.append(expr.getPropertyName());
			return arg;
		}

		@Override
		public StringBuilder visitVariableAccess(VariableAccess expr, StringBuilder arg) {
			arg.append('$');
			arg.append(expr.getVariableName());
			return arg;
		}

		@Override
		public StringBuilder visitFunctionCall(FunctionCall expr, StringBuilder arg) {
			arg.append('#');
			arg.append(expr.getName());
			arg.append('(');
			List<ConfigExpression> arguments = expr.getArgs();
			for (int n = 0, cnt = arguments.size(); n < cnt; n++) {
				if (n > 0) {
					arg.append(", ");
				}
				arguments.get(n).visit(this, arg);
			}
			arg.append(')');
			return arg;
		}

		@Override
		public StringBuilder visitLiteralText(LiteralText expr, StringBuilder arg) {
			arg.append('\'');
			arg.append(expr.getText());
			arg.append('\'');
			return arg;
		}

		@Override
		public StringBuilder visitLiteralInt(LiteralInt expr, StringBuilder arg) {
			arg.append(expr.getValue());
			return arg;
		}

		@Override
		public StringBuilder visitCollectionAccess(CollectionAccess expr, StringBuilder arg) {
			visitContents(expr.getExpr(), arg);
			arg.append('[');
			visitContents(expr.getIndex(), arg);
			arg.append(']');
			return arg;
		}

		@Override
		public StringBuilder visitAlternative(Alternative expr, StringBuilder arg) {
			visitContents(expr.getExpr(), arg);
			arg.append(" | ");
			visitContents(expr.getFallback(), arg);
			return arg;
		}

		@Override
		public StringBuilder visitChoice(Choice expr, StringBuilder arg) {
			visitContents(expr.getTest(), arg);
			arg.append(" ? ");
			visitContents(expr.getPositive(), arg);
			TemplateExpression negative = expr.getNegative();
			if (negative != null) {
				arg.append(" : ");
				visitContents(negative, arg);
			}
			return arg;
		}

		@Override
		public StringBuilder vistTemplateReference(TemplateReference expr, StringBuilder arg) {
			arg.append("->");
			expr.getTemplateName().visit(this, arg);
			return arg;
		}

		@Override
		public StringBuilder visitTemplate(Template expr, StringBuilder arg) {
			return visitSequence(expr, arg);
		}

		@Override
		public StringBuilder visitTag(Tag expr, StringBuilder arg) {
			return visitSequence(expr, arg);
		}

		private StringBuilder visitSequence(TemplateSequence expr, StringBuilder arg) {
			for (TemplateExpression part : expr.getExprs()) {
				visitContents(part, arg);
			}
			return arg;
		}

		@Override
		public StringBuilder visitForeach(Foreach expr, StringBuilder arg) {
			arg.append("foreach(");
			String varName = expr.getVarName();
			if (varName != null) {
				arg.append(varName);
				arg.append(" : ");
			}
			visitContents(expr.getCollection(), arg);
			appendOptionalArgs(arg, expr.getSeparator(), expr.getIterator(), expr.getStart(), expr.getStop());
			arg.append(")");
			return arg;
		}

		private void appendOptionalArgs(StringBuilder arg, TemplateExpression... exprs) {
			for (int n = 0, cnt = nonEmptyPrefixLenth(exprs); n < cnt; n++) {
				arg.append(", ");
				visitContents(exprs[n], arg);
			}
		}

		private int nonEmptyPrefixLenth(TemplateExpression[] exprs) {
			int result = exprs.length;
			while (result > 0 && isEmptyTemplate(exprs[result - 1])) {
				result--;
			}
			return result;
		}

		private boolean isEmptyTemplate(TemplateExpression expr) {
			return expr == null || (expr instanceof LiteralText && ((LiteralText) expr).getText().isEmpty());
		}

		private void visitContents(TemplateExpression expr, StringBuilder arg) {
			expr.visit(ToString.this, arg);
		}
	};

	@Override
	public StringBuilder visitLiteralText(LiteralText expr, StringBuilder arg) {
		if (_literalContext) {
			arg.append(quoteSpecials(expr.getText()));
		} else {
			arg.append("'");
			arg.append(quoteString(expr.getText()));
			arg.append("'");
		}
		return arg;
	}

	private Object quoteSpecials(String text) {
		return text.replaceAll("([<>{}!])", "!$1");
	}

	private String quoteString(String text) {
		return text.replace("'", "\\'");
	}

	@Override
	public StringBuilder visitLiteralInt(LiteralInt expr, StringBuilder arg) {
		arg.append(expr.getValue());
		return arg;
	}

	@Override
	public StringBuilder visitPropertyAccess(PropertyAccess expr, StringBuilder arg) {
		return inExpressionContext(expr, arg);
	}

	@Override
	public StringBuilder visitVariableAccess(VariableAccess expr, StringBuilder arg) {
		return inExpressionContext(expr, arg);
	}

	@Override
	public StringBuilder visitFunctionCall(FunctionCall expr, StringBuilder arg) {
		return inExpressionContext(expr, arg);
	}

	@Override
	public StringBuilder visitCollectionAccess(CollectionAccess expr, StringBuilder arg) {
		return inExpressionContext(expr, arg);
	}

	@Override
	public StringBuilder visitAlternative(Alternative expr, StringBuilder arg) {
		return inExpressionContext(expr, arg);
	}

	@Override
	public StringBuilder visitChoice(Choice expr, StringBuilder arg) {
		return inExpressionContext(expr, arg);
	}

	@Override
	public StringBuilder visitForeach(Foreach expr, StringBuilder arg) {
		return inExpressionContext(expr, arg);
	}

	@Override
	public StringBuilder visitSelfAccess(SelfAccess expr, StringBuilder arg) {
		return inExpressionContext(expr, arg);
	}

	@Override
	public StringBuilder vistTemplateReference(TemplateReference expr, StringBuilder arg) {
		return inExpressionContext(expr, arg);
	}

	@Override
	public StringBuilder visitTemplate(Template expr, StringBuilder arg) {
		return inLiteralContext(expr, arg);
	}

	@Override
	public StringBuilder visitTag(Tag expr, StringBuilder arg) {
		boolean literalContextBefore = _literalContext;
		if (!literalContextBefore) {
			arg.append("}");
			_literalContext = true;
		}

		arg.append('<');
		arg.append(expr.getName());
		
		for (Entry<String, TemplateExpression> entry : expr.getAttributes().entrySet()) {
			arg.append(' ');
			arg.append(entry.getKey());
			arg.append('=');
			arg.append('"');

			inLiteralContext(entry.getValue(), arg);

			if (!_literalContext) {
				arg.append('{');
				_literalContext = true;
			}

			arg.append('"');
		}

		if (expr.isEmpty()) {
			arg.append("/>");
		} else {
			arg.append('>');

			inLiteralContext(expr, arg);

			if (!_literalContext) {
				arg.append('{');
				_literalContext = true;
			}

			arg.append("</");
			arg.append(expr.getName());
			arg.append('>');
		}

		if (!literalContextBefore) {
			arg.append('{');
		}
		_literalContext = literalContextBefore;

		return arg;
	}

	private StringBuilder inLiteralContext(TemplateExpression expr, StringBuilder arg) {
		return inContext(expr, arg, true);
	}

	private StringBuilder inExpressionContext(TemplateExpression expr, StringBuilder arg) {
		return inContext(expr, arg, false);
	}

	private StringBuilder inContext(TemplateExpression expr, StringBuilder arg, boolean literalContextExpected) {
		boolean literalContextBefore = _literalContext;
		boolean wrongContext = literalContextBefore != literalContextExpected;

		if (wrongContext) {
			arg.append("{");
			_literalContext = literalContextExpected;
		}

		expr.visit(_inner, arg);

		if (wrongContext) {
			arg.append("}");
			_literalContext = literalContextBefore;
		}
		return arg;
	}

}
