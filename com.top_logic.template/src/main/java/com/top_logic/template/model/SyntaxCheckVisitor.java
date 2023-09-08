/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.template.I18NConstants;
import com.top_logic.template.model.TemplateProblem.Type;
import com.top_logic.template.tree.AssignStatement;
import com.top_logic.template.tree.AttributeValue;
import com.top_logic.template.tree.BinaryExpression;
import com.top_logic.template.tree.ConstantExpression;
import com.top_logic.template.tree.DefineStatement;
import com.top_logic.template.tree.Expression;
import com.top_logic.template.tree.ForeachStatement;
import com.top_logic.template.tree.FunctionCall;
import com.top_logic.template.tree.IfStatement;
import com.top_logic.template.tree.InvokeStatement;
import com.top_logic.template.tree.LiteralText;
import com.top_logic.template.tree.Reference;
import com.top_logic.template.tree.Template;
import com.top_logic.template.tree.TemplateNode;
import com.top_logic.template.tree.TemplateVisitor;
import com.top_logic.template.tree.UnaryExpression;
import com.top_logic.template.tree.parameter.ParameterValue;

/**
 * This visitor performs a second level syntax check. After the parser verified, that the template
 * is syntactically correct this visitor checks whether the used references and variable declarations
 * are correct. After the performed check each {@link Reference} will have a reference to the used
 * {@link VarSymbol}. 
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class SyntaxCheckVisitor implements TemplateVisitor<Void, VarScope> {
	
	private static final Set<String> GENERAL_ATTRIBUTES = new HashSet<>(
			Arrays.asList("id"));
	
	private static final Set<String> FOREACH_ATTRIBUTES = CollectionUtil.union(GENERAL_ATTRIBUTES, 
			Arrays.asList("start", "end", "separator"));

	private ErrorReport report;
	private Stack<VarScope> scopes;
	
	private Set<String> ids;

	/**
	 * Creates a new {@link SyntaxCheckVisitor}.
	 */
	public SyntaxCheckVisitor() {
		this.report   = new ErrorReport();
		this.scopes   = new Stack<>();
		this.ids      = new HashSet<>();
	}

	@Override
	public Void visitAssignStatement(AssignStatement aNode, VarScope aScope) {
		checkIDs(aNode, GENERAL_ATTRIBUTES, aNode.getAttributes());
		return aNode.getExpression().visit(this, aScope);
	}

	@Override
	public Void visitDefineStatement(DefineStatement aNode, VarScope aScope) {
		String       theVariable     = aNode.getVariable();
		Expression   theExpression   = aNode.getExpression();
		
		checkIDs(aNode, GENERAL_ATTRIBUTES, aNode.getAttributes());
		if (isInScope(theVariable)) {
			addError(new TemplateProblem(I18NConstants.REFERENCE_ALREADY_IN_SCOPE__NAME_ROW_COL, theVariable, Type.SYNTAX_ERROR, aNode.getRowBegin(), aNode.getColBegin(), aNode.getRowEnd(), aNode.getColEnd()));
			return none;
		}
		theExpression.visit(this, aScope);
		aScope.declare(theVariable, theExpression.getVarSymbol());
		
		return none;
	}

	@Override
	public Void visitBinaryExpression(BinaryExpression aNode, VarScope aScope) {
		aNode.getLeftExpression().visit(this, aScope);
		aNode.getRightExpression().visit(this, aScope);
		aNode.setVarSymbol(new VarSymbol());
		
		return none;
	}

	/**
	 * The SyntaxCheck for a {@link ConstantExpression} is not necessary. (If it passed
	 * the parser its is syntactically correct.)
	 */
	@Override
	public Void visitConstantExpression(ConstantExpression aNode, VarScope aScope) {
		aNode.setVarSymbol(new VarSymbol());
		return none;
	}
	
	@Override
	public Void visitUnaryExpression(UnaryExpression aNode, VarScope aScope) {
		aNode.getExpression().visit(this, aScope);
		aNode.setVarSymbol(new VarSymbol());
		return none;
	}

	@Override
	public Void visitForeachStatement(ForeachStatement aNode, VarScope aScope) {
		String                 theVariable   = aNode.getVariable();
		Expression             theExpression = aNode.getExpression();
		
		checkIDs(aNode, FOREACH_ATTRIBUTES, aNode.getAttributes());

		VarScope theInnerScope = new VarScope();
		this.scopes.push(theInnerScope);
		theExpression.visit(this, theInnerScope);
		theInnerScope.declare(theVariable, new VarSymbol());
		
		aNode.getProductionNode().visit(this, theInnerScope);
		
		this.scopes.pop();
		return none;
	}

	@Override
	public Void visitFunctionCall(FunctionCall aNode, VarScope aScope) {
		for (Expression e:aNode.getArguments()) {
			e.visit(this, aScope);
		}
		aNode.setVarSymbol(new VarSymbol());
		return none;
	}

	@Override
	public Void visitIfStatement(IfStatement aNode, VarScope aScope) {
		Expression             condition     = aNode.getCondition();
		TemplateNode           thenStm       = aNode.getThenStm();
		TemplateNode           elseStm       = aNode.getElseStm();
		
		checkIDs(aNode, GENERAL_ATTRIBUTES, aNode.getAttributes());
		
		condition.visit(this, aScope);
		
		if (thenStm != null) {
			VarScope theInnerScope = new VarScope();
			this.scopes.push(theInnerScope);
			thenStm.visit(this, theInnerScope);
			this.scopes.pop();
		}

		if (elseStm != null) {
			VarScope theInnerScope = new VarScope();
			this.scopes.push(theInnerScope);
			elseStm.visit(this, theInnerScope);
			this.scopes.pop();
		}
		
		return none;
	}

	/**
	 * The SyntaxCheck for a {@link LiteralText} is not necessary. (Its not part of the
	 * template language)
	 */
	@Override
	public Void visitLiteralText(LiteralText aNode, VarScope aScope) {
		return none;
	}

	@Override
	public Void visitReference(Reference aNode, VarScope aScope) {
		String       theName       = aNode.getPath().get(0);
		
		if (StringServices.isEmpty(theName)) {
			addError(new TemplateProblem(I18NConstants.REFERENCE_INVALID__NAME_ROW_COL, theName, Type.SYNTAX_ERROR, aNode.getRowBegin(), aNode.getColBegin(), aNode.getRowEnd(), aNode.getColEnd()));
			return none;
		}
		
		String key = aNode.getFullQualifiedName();
		// Check whether the ModelObject was already used. If not, create a new one and add it
		// to the scope.
		if (isInScope(key)) {
			aNode.setVarSymbol(getVariable(key));
		} else {
			VarSymbol newVarSymbol = new VarSymbol();
			aScope.declare(key, newVarSymbol);
			aNode.setVarSymbol(newVarSymbol);
		}
		
		return none;
	}

	@Override
	public Void visitTemplate(Template aNode, VarScope aScope) {
		VarScope innerScope = new VarScope();
		this.scopes.push(innerScope);
		for (TemplateNode n:aNode.getNodes()) {
			n.visit(this, innerScope);
		}
		this.scopes.pop();
		return none;
	}

	@Override
	public Void visitInvokeStatement(InvokeStatement node, VarScope scope) {
		checkStructuredParameterValue(node.getParameterValues());
		return none;
	}

	private void checkPrimitiveParameterValue(TemplateNode value) {
		VarScope innerScope = new VarScope();
		this.scopes.push(innerScope);
		value.visit(this, innerScope);
		this.scopes.pop();
	}

	private void checkStructuredParameterValue(Map<String, ParameterValue<TemplateNode>> structuredValue) {
		for (ParameterValue<TemplateNode> value : structuredValue.values()) {
			checkParameterValue(value);
		}
	}

	private void checkListParameterValue(List<ParameterValue<TemplateNode>> listValue) {
		for (ParameterValue<TemplateNode> valueEntry : listValue) {
			checkParameterValue(valueEntry);
		}
	}

	private void checkParameterValue(ParameterValue<TemplateNode> value) {
		if (value.isPrimitiveValue()) {
			checkPrimitiveParameterValue(value.asPrimitiveValue().getPrimitiveValue());
		} else if (value.isStructuredValue()) {
			checkStructuredParameterValue(value.asStructuredValue().getStructuredValue());
		} else if (value.isListValue()) {
			checkListParameterValue(value.asListValue().getListValue());
		} else {
			throw new UnsupportedOperationException("Unknown " + ParameterValue.class.getSimpleName()
				+ " instance: " + StringServices.getObjectDescription(value));
		}
	}

	@Override
	public Void visitAttributeValue(AttributeValue attributeValue, VarScope scope) {
		VarScope innerScope = new VarScope();
		scopes.push(innerScope);
		for (TemplateNode node : attributeValue.getContent()) {
			node.visit(this, innerScope);
		}
		scopes.pop();
		return none;
	}

	/**
	 * Result of this check.
	 */
	public ErrorReport getResult() {
		return report;
	}
	
	/** 
	 * Adds the given {@link TemplateProblem} to the list of encountered errors.
	 */
	protected void addError(TemplateProblem anError) {
		report.addError(anError);
	}

	/** 
	 * Adds the given {@link TemplateProblem} to the list of encountered warnings.
	 */
	protected void addWarning(TemplateProblem aWarning) {
		report.addWarning(aWarning);
	}
	
	/**
	 * A syntax check checks whether the given template is semantically correct. This check
	 * verifies, that all variables used in the template are properly defined and only used in their
	 * respective scopes.
	 * 
	 * @param aNode the root node of a syntax tree to check
	 * @return returns the node with added information that was collected during the check process.
	 * @throws TemplateException if anything goes wrong during the expansion process
	 */
	public static TemplateNode checkSyntax(TemplateNode aNode) throws TemplateException {
		return checkSyntax(new SyntaxCheckVisitor(), aNode);
	}

	/**
	 * Parses the syntax tree and checks each node.
	 * 
	 * @param v a visitor to be used for parsing the syntax tree
	 * @param aNode the root node of the tree
	 * @return the given root node. The tree is now prepared for a model check.
	 * @throws TemplateException in case any errors or warnings occur during the parsing.
	 */
	public static TemplateNode checkSyntax(SyntaxCheckVisitor v, TemplateNode aNode) throws TemplateException {
		VarScope             theScope = new VarScope();

		aNode.visit(v, theScope);
		
		ErrorReport result = v.getResult();
		if (result.isOk()) {
			return aNode;
		}
		else {
			throw new TemplateException(result);
		}
	}

	/**
	 * Checks whether the (optional) ID attribute of a given node was already used. If that is the
	 * case, a {@link Type#SYNTAX_ERROR} will be {@link #addError(TemplateProblem) added to the
	 * errors list}.
	 * 
	 * @param aNode
	 *        the current node to check
	 * @param expectedAttributes
	 *        Expected attributes in the current context.
	 * @param givenAttributes
	 *        a {@link Map} of attributes and their values
	 */
	private void checkIDs(TemplateNode aNode, Set<String> expectedAttributes, Map<String, String> givenAttributes) {
		if (givenAttributes.containsKey(TemplateNode.ATTR_ID)) {
			String theID = givenAttributes.get(TemplateNode.ATTR_ID);
			
			if (!this.ids.add(theID)){
				addError(new TemplateProblem(I18NConstants.ID_EXISTS__NAME_ROW_COL, theID, Type.SYNTAX_ERROR, aNode.getRowBegin(), aNode.getColBegin(), aNode.getRowEnd(), aNode.getColEnd()));
			}
		}
		
		if (!expectedAttributes.containsAll(givenAttributes.keySet())) {
			List<String> invalidNames = new ArrayList<>(givenAttributes.keySet());
			invalidNames.removeAll(expectedAttributes);
			Collections.sort(invalidNames);
			
			addError(new TemplateProblem(I18NConstants.INVALID_ATTRIBUTES__NAME_ROW_COL, StringServices.join(invalidNames, ", "), Type.SYNTAX_ERROR, aNode.getRowBegin(), aNode.getColBegin(), aNode.getRowEnd(), aNode.getColEnd()));
		}
	}

	/**
	 * Checks if the scope stack contains variable of the given name, which means it was already
	 * assigned.
	 * 
	 * @param aVariableName a name to check
	 * @return <code>true</code> if one of the scopes contains the given name, <code>false</code>
	 *         otherwise.
	 */
	private boolean isInScope(String aVariableName) {
		int size = this.scopes.size() - 1;
		for (int i = size; i >= 0; i--) {
			VarScope theScope = this.scopes.get(i);
			if (theScope == null) {
				continue;
			}
			else if (theScope.isDeclared(aVariableName)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the value for a given variable name from the stack of scopes.
	 * 
	 * @param aVariableName a name to retrieve
	 * @return the {@link VarSymbol} for the first occurrence for given name, or <code>null</code>
	 *         if the name was not found.
	 */
	private VarSymbol getVariable(String aVariableName) {
		int size = this.scopes.size() - 1;
		for (int i = size; i >= 0; i--) {
			VarScope theScope = this.scopes.get(i);
			if (theScope == null) {
				continue;
			}
			else if (theScope.isDeclared(aVariableName)){
				return theScope.lookup(aVariableName);
			}
		}
		return null;
	}

}
