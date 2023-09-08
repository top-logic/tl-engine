/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.MetaObject.Kind;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOFunction;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.template.I18NConstants;
import com.top_logic.template.model.TemplateProblem.Type;
import com.top_logic.template.tree.AssignStatement;
import com.top_logic.template.tree.AttributeValue;
import com.top_logic.template.tree.BinaryExpression;
import com.top_logic.template.tree.ConstantExpression;
import com.top_logic.template.tree.DefineStatement;
import com.top_logic.template.tree.Expression;
import com.top_logic.template.tree.Expression.Operator;
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
 * Checks the variable definition in a scope map that was produced by a {@link SyntaxCheckVisitor}.
 * The {@link VarSymbol} will be enriched with the respective objects from {@link ExpansionModel}. 
 * 
 * Before a ModelChecker can be used the {@link SyntaxCheckVisitor} must have parsed the syntax
 * tree successfully.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class ModelCheckVisitor implements TemplateVisitor<Void, TypeScope> {
	
	private ErrorReport report;
	
	private List<String>                  references;
	private Map<String, String>           ids;
	private Map<String, TemplateArea>     templateAreas;

	private Stack<TypeScope> scopes;
	
	private final ExpansionModel      expansionModel;

	public ModelCheckVisitor(ExpansionModel anExpansionModel) {
		this.expansionModel = anExpansionModel;
		this.report         = new ErrorReport();
		this.references     = new ArrayList<>();
		this.ids            = new HashMap<>();
		this.templateAreas  = new HashMap<>();
		this.scopes = new Stack<>();
	}
	
	public void checkModel(TemplateNode aNode, TypeScope aScope) {
		aNode.visit(this, aScope);
	}
	
	@Override
	public Void visitAssignStatement(AssignStatement aNode, TypeScope aScope) {
		addTemplateAreaForID(aNode.getAttributes(), aNode);
		Expression theExpr = aNode.getExpression();
		theExpr.visit(this, aScope);
		
		if (theExpr instanceof Reference) {
			addIDs(aNode.getAttributes(), (Reference)theExpr);
		}
		
		postVisitAssignmentStatement(aNode, theExpr);
		
		return none;
	}
	
	@Override
	public Void visitDefineStatement(DefineStatement aNode, TypeScope aScope) {
		String       theVariable     = aNode.getVariable();
		Expression   theExpression   = aNode.getExpression();
		
		addTemplateAreaForID(aNode.getAttributes(), aNode);
		
		String       theRefPath      = null;
		
		
		if (theExpression instanceof Reference) {
			Reference theReference = (Reference)theExpression;
			addIDs(aNode.getAttributes(), theReference);
			theRefPath = theReference.getFullQualifiedName();
		}
		int        theErrorSize = errorCount();
		theExpression.visit(this, aScope);
		VarSymbol theVS = theExpression.getVarSymbol();
		
		if (theErrorSize != errorCount()) {
			theVS.setType(MetaObject.INVALID_TYPE);
		}
		
		if (theVS == null){
			// This error was already collected while parsing the expression node
		}
		else {
			MetaObject theMO = theVS.getType();

			if (isError(theMO)) {
				// This error was already collected while parsing the reference node
			}
			else {
				aScope.declare(theVariable, theMO);
				if (!StringServices.isEmpty(theRefPath)) {
					this.expandReferences(theVariable, theRefPath);
				}
			}
		}
		return none;
	}

	/**
	 * Hook for subclasses to implement special handling after an assignment statement was visited.
	 * 
	 * @param aNode
	 *        the node the Expression belongs to
	 * @param aVisitedExpression
	 *        the Expression that was visited in
	 *        {@link #visitAssignStatement(AssignStatement, TypeScope)}
	 */
	protected void postVisitAssignmentStatement(TemplateNode aNode, Expression aVisitedExpression) {
		// only a hook
	}

	@Override
	public Void visitBinaryExpression(BinaryExpression aNode, TypeScope aScope) {
		Expression theLE       = aNode.getLeftExpression();
		Expression theRE       = aNode.getRightExpression();
		Operator   theOperator = aNode.getOperator();
		
		int        theErrorSize = errorCount();
		
		theLE.visit(this, aScope);
		theRE.visit(this, aScope);
		
		if (theErrorSize != errorCount()) {
			// one of the inner Expressions produced an error, so return at here without logging it again.
			return none;
		}

		VarSymbol theLEVS = theLE.getVarSymbol();
		VarSymbol theREVS = theRE.getVarSymbol();
		
		if (theLEVS == null || theREVS == null) {
			// something went wrong in the expressions, an error was generated, so just stop
			// processing at this points
			return none;
		}
		MetaObject theLEType = theLEVS.getType();
		MetaObject theREType = theREVS.getType();

		// check whether the given types fit to the given operator and to each other
		switch (theOperator) {
			case GT: // >
			case GE: // >=
			case LT: // <
			case LE: // <=
				if (! this.expansionModel.getTypeSystem().isComparableTo(theLEType, theREType)) {
					addError(I18NConstants.EXPRESSION_OPERATOR_MATCHING_TYPE__NAME_ROW_COL, "", Type.MODEL_ERROR, aNode);
				}
				if (theLEType.equals(MOPrimitive.BOOLEAN) ||  theREType.equals(MOPrimitive.BOOLEAN)) {
					addError(I18NConstants.EXPRESSION_OPERATOR_WRONG_TYPE__NAME_ROW_COL, "", Type.MODEL_ERROR, aNode);
				}
				break;
			case EQ:  // ==
			case NE:  // !=
			case AND: // &&
			case OR:  // ||
				if(!this.expansionModel.getTypeSystem().hasCommonInstances(theLEType, theREType)) {
					addError(I18NConstants.EXPRESSION_OPERATOR_MATCHING_TYPE__NAME_ROW_COL, "", Type.MODEL_ERROR, aNode);
				}
				break;
			default:
				addError(I18NConstants.EXPRESSION_OPERATOR_NOT_SUPPORTED__NAME_ROW_COL, theOperator.name(),
					Type.SYNTAX_ERROR, aNode);
		}
		
		MetaObject theType  = MOPrimitive.BOOLEAN;
		VarSymbol  theVS    = aNode.getVarSymbol();
		theVS.setType(theType);
		return none;
	}

	@Override
	public Void visitConstantExpression(ConstantExpression aNode, TypeScope aScope) {
		VarSymbol  theVS = aNode.getVarSymbol();
		if (aNode.isBoolean()) {
			theVS.setType(MOPrimitive.BOOLEAN);
			theVS.setValue(aNode.getBooleanValue());
		}
		else {
			theVS.setType(MOPrimitive.STRING);
			theVS.setValue(aNode.getStringValue());
		}
		return none;
	}
	
	@Override
	public Void visitUnaryExpression(UnaryExpression aNode, TypeScope aScope) {
		Expression theExpr     = aNode.getExpression();
		Operator   theOperator = aNode.getOperator();
		
		theExpr.visit(this, aScope);
		
		// need check for null in case something went wrong while visiting the inner expression. An
		// extra error is not needed, because the reason was already added.
		VarSymbol exprSymbol = theExpr.getVarSymbol();
		if (exprSymbol != null) {
			MetaObject theExprType = exprSymbol.getType();

			switch (theOperator) {
				case NOT:
					if (!this.expansionModel.getTypeSystem().isAssignmentCompatible(MOPrimitive.BOOLEAN, theExprType)) {
						addError(I18NConstants.EXPRESSION_OPERATOR_WRONG_TYPE__NAME_ROW_COL, "", Type.MODEL_ERROR, theExpr);
					}
					aNode.getVarSymbol().setType(MOPrimitive.BOOLEAN);
					break;
				default:
					addError(I18NConstants.EXPRESSION_OPERATOR_NOT_SUPPORTED__NAME_ROW_COL, theOperator.name(),
						Type.SYNTAX_ERROR, aNode);
					break;
			}
		}
		return none;
	}

	private void addError(ResKey3 key, String name, Type type, TemplateNode node) {
		addError(new TemplateProblem(key, name, type, node.getRowBegin(), node.getColBegin(), node.getRowEnd(),
			node.getColEnd()));
	}

	@Override
	public Void visitForeachStatement(ForeachStatement aNode, TypeScope aScope) {
		MetaObject   theOldValue     = null;
		String       theVariable     = aNode.getVariable();
		Expression   theExpression   = aNode.getExpression();
		
		addTemplateAreaForID(aNode.getAttributes(), aNode);
		
		String       theRefPath      = null;
		
		
		if (theExpression instanceof Reference) {
			Reference theReference = (Reference)theExpression;
			addIDs(aNode.getAttributes(), theReference);
			theRefPath = theReference.getFullQualifiedName();
		}
		
		theExpression.visit(this, aScope);
		VarSymbol theVS = theExpression.getVarSymbol();
		
		if (theVS == null){
			// This error was already collected while parsing the expression node
		}
		else {
			MetaObject theMO = theVS.getType();

			if (isError(theMO)) {
				// This error was already collected while parsing the reference node
			}
			else if (theMO.getKind() != MetaObject.Kind.collection) {
				addError(I18NConstants.FOREACH_WRONG_TYPE__NAME_ROW_COL, theMO.getName(), Type.MODEL_ERROR, aNode);
			}
			else {
				TypeScope innerScope = new TypeScope();
				scopes.push(innerScope);
				MetaObject elementType = ((MOCollection) theMO).getElementType();
				theOldValue = innerScope.declare(theVariable, elementType);
				aNode.getProductionNode().visit(this, innerScope);
				if (!StringServices.isEmpty(theRefPath)) {
					this.expandReferences(theVariable, theRefPath);
				}

				if (theOldValue != null) {
					innerScope.declare(theVariable, theOldValue);
				}
				else {
					innerScope.remove(theVariable);
				}
				scopes.pop();
			}
		}
		return none;
	}

	private void expandReferences(String theVariable, String theRefPath) {
		List<String> toRemove = new ArrayList<>();
		List<String> toAdd    = new ArrayList<>();
		for(String thePath : this.references) {
			// beware of variable names if the one starts with another (f.e. "group" and "groupName")
			if (thePath.startsWith(theVariable + ".")) {
				String replace = theRefPath + ".*" + thePath.substring(theVariable.length());
				toAdd.add(replace);
				toRemove.add(thePath);
			}
		}
		this.references.removeAll(toRemove);
		this.references.addAll(toAdd);
		
	}

	@Override
	public Void visitFunctionCall(FunctionCall aNode, TypeScope aScope) {
		String     theFunctionName = aNode.getName();
		MOFunction theFunction     = this.expansionModel.getTypeForFunction(theFunctionName);
		
		if (theFunction == null) {
			addError(new TemplateProblem(I18NConstants.FUNCTION_NOT_EXISTS__NAME_ROW_COL, theFunctionName, Type.MODEL_ERROR, aNode.getRowBegin(), aNode.getColBegin(), aNode.getRowEnd(), aNode.getColEnd()));
			// need to reset the VarSymbol so that no further model checks will be performed. The
			// function does not exist in the context of the given model, so its return type and
			// outer type checks are useless.
			aNode.getVarSymbol().setType(MetaObject.INVALID_TYPE);
//			aNode.setVarSymbol(null);
			return none;
		}
		
		MetaObject       theReturnType     = theFunction.getReturnType();
		List<MetaObject> theParameterTypes = theFunction.getArgumentTypes();
		boolean          isVarArg          = theFunction.isVarArg();
		List<Expression> theArguments      = aNode.getArguments();
		
		// check number of required arguments for the used function with the number of passed in arguments
		int theArgSize        = theArguments.size();
		int theParameterCount = theParameterTypes.size();
		
		if ((isVarArg && theArgSize < theParameterCount - 1) || (!isVarArg && theParameterCount != theArgSize)) {
			addError(new TemplateProblem(I18NConstants.FUNCTION_ARGUMENTS_SIZE__NAME_ROW_COL, theFunctionName, Type.SYNTAX_ERROR, aNode.getRowBegin(), aNode.getColBegin(), aNode.getRowEnd(), aNode.getColEnd()));
			aNode.getVarSymbol().setType(MetaObject.INVALID_TYPE);
			return none;
		}
		
		// check each attribute type with the defined types of the used function. Only if all passed
		// in types match their respective definitions the function call can be successful.
		for (int i = 0; i < theArgSize; i++) {
			Expression theExpr      = theArguments.get(i);
			int        theErrorSize = errorCount();
			
			theExpr.visit(this, aScope);
			if (theErrorSize != errorCount()) {
				// inner Expression produced an error, so return at here without logging it again.
				aNode.getVarSymbol().setType(MetaObject.INVALID_TYPE);
				return none;
				
			}
			
			VarSymbol theVS = theExpr.getVarSymbol();

			MetaObject theExpectedType;
			if (isVarArg && i >= theParameterCount) {
				theExpectedType = theParameterTypes.get(theParameterCount - 1);
			}
			else {
				theExpectedType = theParameterTypes.get(i);
			}
			MetaObject theGivenType = theVS.getType();
			if (!this.expansionModel.getTypeSystem().isAssignmentCompatible(theExpectedType, theGivenType)) {
				addError(new TemplateProblem(
					I18NConstants.FUNCTION_MATCHING_TYPE__NAME_ROW_COL_EXPECTED_ENCOUNTERED.fill(theFunctionName,
						aNode.getRowBegin(), aNode.getColBegin(), theExpectedType.getName(), theGivenType.getName()),
					theFunctionName, Type.MODEL_ERROR, aNode.getRowBegin(), aNode.getColBegin(), aNode.getRowEnd(),
					aNode.getColEnd()));
			}
			
		}
		
		VarSymbol theVS = aNode.getVarSymbol();
		theVS.setType(theReturnType);
		return none;
	}

	@Override
	public Void visitIfStatement(IfStatement aNode, TypeScope aScope) {
		Expression   theCondition = aNode.getCondition();
		TemplateNode theThenStm   = aNode.getThenStm();
		TemplateNode theElseStm   = aNode.getElseStm();
		
		addTemplateAreaForID(aNode.getAttributes(), aNode);
		
		theCondition.visit(this, aScope);
		
		if (theThenStm != null) {
			TypeScope innerScope = new TypeScope();
			scopes.push(innerScope);
			theThenStm.visit(this, innerScope);
			scopes.pop();
		}

		if (theElseStm != null) {
			TypeScope innerScope = new TypeScope();
			scopes.push(innerScope);
			theElseStm.visit(this, innerScope);
			scopes.pop();
		}
		
		return none;
	}

	/**
	 * The ModelCheck for a {@link LiteralText} is not necessary. (Its not part of the
	 * template language)
	 */
	@Override
	public Void visitLiteralText(LiteralText aNode, TypeScope aScope) {
		return none;
	}

	@Override
	public Void visitReference(Reference aNode, TypeScope aScope) {
		boolean      isModelRef    = aNode.isModelRef();
		String       theNameSpace  = aNode.getNameSpace();
		List<String> theNodePath   = aNode.getPath();
		String       theName       = theNodePath.get(0);
		MetaObject   theMO         = MetaObject.INVALID_TYPE;
		boolean      pathIsValid   = true;
		
		// if the name is null, the template is invalid.
		if (StringServices.isEmpty(theName)) {
			addError(I18NConstants.REFERENCE_INVALID__NAME_ROW_COL, theName, Type.SYNTAX_ERROR, aNode);
//			return none;
		}
		
		
		if (isModelRef) {
			// 1.  fetch the MetaObject for the current nameSpace and path.
			// 1a. each MetaObject for the path except the last MUST be of type MOStructure
			// 2.  if the returned MetaObject is null, the type does not exist in the model which
			//     means we encountered a model error
			theMO = this.expansionModel.getTypeForVariable(theNameSpace, theName);
			
		}
		// This reference is (partly) defined via a model reference that must be in the scope. The
		// first part of the path-array is the assigned name. The rest of the path must be used to
		// fetch the new MetaObject.
		else {
			// 1. check whether the referencing MO is in the scope
			if(!isInScope(theName)) {
				addError(I18NConstants.REFERENCE_NOT_IN_SCOPE__NAME_ROW_COL, theName, Type.SYNTAX_ERROR, aNode);
				pathIsValid = false;
			}
			theMO = getVariable(theName);
		}
		// 2. fetch the MetaObject for the current path;
		if (isError(theMO)) {
			String theCombinedName = (theNameSpace != null) ? (theNameSpace + ":" + theName) : theName;
			addError(I18NConstants.REFERENCE_INVALID__NAME_ROW_COL, theCombinedName, Type.MODEL_ERROR, aNode);
			theMO = MetaObject.INVALID_TYPE;
		} else {
			if (theNodePath.size() > 1) {
				for (int i = 1; i < theNodePath.size(); i++) {
					try {
						theMO = this.getDerivedMO(theMO, theNodePath.get(i));
					} catch (NoSuchAttributeException e) {
						// Expected, errors will be collected in local error list.
						addError(I18NConstants.MISSING_ATTRIBUTE__NAME_ROW_COL,
							StringServices.join(theNodePath.subList(0, i + 1), "."),
							Type.SYNTAX_ERROR,
							aNode);
						pathIsValid = false;
						theMO = MetaObject.INVALID_TYPE;
						break;
					}
				}
			}
		}
		if (pathIsValid && !isError(theMO)) {
			this.references.add(aNode.getFullQualifiedName());
		}
		// 3. add the MO to the VarSymbol of the reference node
		VarSymbol varSymbol = aNode.getVarSymbol();
		if (varSymbol.getType() == null) {
			varSymbol.setType(theMO);
		} else {
			if (!varSymbol.getType().equals(theMO)) {
				throw new RuntimeException(
					"Expression has multiple types: '" + varSymbol.getType() + "' and '" + theMO + "'");
			}
		}
		return none;
	}

	protected MetaObject getDerivedMO(MetaObject aMO, String anAttr) throws NoSuchAttributeException {
		if (!(aMO instanceof MOStructure)) {
			throw new NoSuchAttributeException("wrong meta object type");
		}
		MOStructure theStruct    = (MOStructure) aMO;
		MOAttribute theAttribute = theStruct.getAttribute(anAttr);
		
		return theAttribute.getMetaObject();
	}

	@Override
	public Void visitTemplate(Template aNode, TypeScope outerScope) {
		TypeScope innerScope = new TypeScope();
		scopes.push(innerScope);
		for (TemplateNode theNode:aNode.getNodes()) {
			theNode.visit(this, innerScope);
		}
		scopes.pop();
		return none;
	}

	@Override
	public Void visitInvokeStatement(InvokeStatement node, TypeScope scope) {
		checkStructuredParameterValue(node.getParameterValues());
		return none;
	}

	private void checkPrimitiveParameterValue(TemplateNode value) {
		TypeScope innerScope = new TypeScope();
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
	public Void visitAttributeValue(AttributeValue attributeValue, TypeScope scope) {
		TypeScope innerScope = new TypeScope();
		scopes.push(innerScope);
		for (TemplateNode node : attributeValue.getContent()) {
			node.visit(this, innerScope);
		}
		scopes.pop();
		return none;
	}

	/**
	 * Result of the check.
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
	
	private int errorCount() {
		return report.getErrors().size();
	}
	
	/** 
	 * Adds the given {@link TemplateProblem} to the list of encountered warnings.
	 */
	protected void addWarning(TemplateProblem aWarning) {
		report.addWarning(aWarning);
	}
	
	/**
	 * the list of expanded references found during the model check process.
	 */
	public List<String> getReferences() {
		return this.references;
	}
	
	/**
	 * If the passed in {@link Reference} is model reference ( {@link Reference#isModelRef()} ==
	 * <code>true</code>)and has ID set this id will be added to {@link #ids} with the
	 * {@link Reference}s complete path.
	 * 
	 * @param someAttributes a {@link Map} of attributes and their values
	 * @param aReference the current Reference to check
	 */
	private void addIDs(Map<String, String> someAttributes, Reference aReference) {
		
		boolean isModelRef = aReference.isModelRef();
		if (isModelRef && someAttributes.containsKey(TemplateNode.ATTR_ID)) {
			String theID = someAttributes.get(TemplateNode.ATTR_ID);
			
			this.ids.put(theID, aReference.getFullQualifiedName());
		}
	}
	
	/**
	 * Returns a {@link Map} with the IDs found in the template as keys and the
	 * path object they define as values. Never <code>null</code>, might be empty.
	 */
	public Map<String, String> getIds() {
		return this.ids;
	}
	
	
	/**
	 * Adds a new {@link TemplateArea} to list of areas if the given {@link TemplateNode} has an ID
	 * attribute.
	 * 
	 * @param someAttributes the Map of attributes
	 * @param aNode the {@link TemplateNode} that defines the area.
	 */
	protected void addTemplateAreaForID(Map<String, String> someAttributes, TemplateNode aNode) {
		if (someAttributes.containsKey(TemplateNode.ATTR_ID)) {
			String theID = someAttributes.get(TemplateNode.ATTR_ID);
			
			TemplateArea theTemplateArea = new TemplateArea(aNode.getColBegin(), aNode.getColEnd(), aNode.getRowBegin(), aNode.getRowEnd());
			this.templateAreas.put(theID, theTemplateArea);
		}
	}
	
	/**
	 * Returns a {@link Map} with the IDs found in the template as keys and the
	 * {@link TemplateArea}s they define as values. Never <code>null</code>, might be empty.
	 */
	public final Map<String, TemplateArea> getTemplateAreas() {
		return this.templateAreas;
	}

	/**
	 * Checks whether the used references into the model and the variables/attributes match the
	 * given {@link ExpansionModel} .
	 * 
	 * @param aNode the root node of a syntax tree
	 * @param anExpansionModel the {@link ExpansionModel} to be used for the model check
	 * @return a {@link CheckResult} filled with the information gathered during the check process
	 * 
	 * @throws TemplateException if warnings or errors occurred during the check process
	 */
	public static CheckResult checkModel(TemplateNode aNode, ExpansionModel anExpansionModel) throws TemplateException {
		return checkModel(new ModelCheckVisitor(anExpansionModel), aNode);
	}

	/**
	 * Checks the syntax tree defined by the given {@link TemplateException} using the given
	 * {@link ModelCheckVisitor}.
	 * 
	 * @param theMCV the visitor to be used for the model check
	 * @param aNode the root node of the syntax tree that shall be checked
	 * @return a {@link CheckResult} containing the information that was collected during the model
	 *         check.
	 * @throws TemplateException if anything goes wrong during the model check
	 */
	public static CheckResult checkModel(ModelCheckVisitor theMCV, TemplateNode aNode) throws TemplateException {
		TypeScope            theMOScope  = new TypeScope();
		theMCV.checkModel(aNode, theMOScope);
		
		ErrorReport result = theMCV.getResult();
		if (result.isOk()) {
			return new CheckResult(aNode, theMCV.getReferences(), theMCV.getIds(), theMCV.getTemplateAreas());
		}
		else {
			throw new TemplateException(result);
		}
	}
	
	/**
	 * Returns the value for a given variable name from the stack of scopes.
	 * 
	 * @param aVariableName a name to retrieve
	 * @return the {@link VarSymbol} for the first occurrence for given name, or <code>null</code>
	 *         if the name was not found.
	 */
	protected MetaObject getVariable(String aVariableName) {
		int size = this.scopes.size() - 1;
		for (int i = size; i >= 0; i--) {
			TypeScope theScope = this.scopes.get(i);
			if (theScope == null) {
				continue;
			}
			else if (theScope.isDeclared(aVariableName)){
				return theScope.lookup(aVariableName);
			}
		}
		return null;
	}

	/**
	 * Checks the scope stack if a variable of the given name was already assigned.
	 * 
	 * @param aVariableName a name to check
	 * @return <code>true</code> if one of the scopes contains the given name, <code>false</code>
	 *         otherwise.
	 */
	protected boolean isInScope(String aVariableName) {
		int size = this.scopes.size() - 1;
		for (int i = size; i >= 0; i--) {
			TypeScope theScope = this.scopes.get(i);
			if (theScope == null) {
				continue;
			}
			else if (theScope.isDeclared(aVariableName)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isError(MetaObject type) {
		return type == null || type.getKind() == Kind.INVALID;
	}

}
