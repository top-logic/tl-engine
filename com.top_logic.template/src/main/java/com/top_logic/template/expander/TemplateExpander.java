/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.expander;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.dob.data.Function;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.template.I18NConstants;
import com.top_logic.template.TemplateParseResult;
import com.top_logic.template.model.CheckResult;
import com.top_logic.template.model.ExpansionModel;
import com.top_logic.template.model.ModelCheckVisitor;
import com.top_logic.template.model.SyntaxCheckVisitor;
import com.top_logic.template.model.TemplateException;
import com.top_logic.template.model.TemplateProblem;
import com.top_logic.template.model.TemplateProblem.Type;
import com.top_logic.template.model.VarScope;
import com.top_logic.template.model.VarSymbol;
import com.top_logic.template.model.function.EqualsFunction;
import com.top_logic.template.parser.ParseException;
import com.top_logic.template.parser.TemplateParser;
import com.top_logic.template.parser.TokenMgrError;
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
import com.top_logic.template.writer.PlainTextQuoter;
import com.top_logic.template.writer.Quoter;
import com.top_logic.template.writer.Quoter.State;
import com.top_logic.template.writer.TemplateStringBufferWriter;
import com.top_logic.template.writer.TemplateWriter;
import com.top_logic.template.writer.XmlQuoter;
import com.top_logic.template.xml.ConfigurableExpansionModel;
import com.top_logic.template.xml.TemplateSettings.OutputFormat;
import com.top_logic.template.xml.TemplateXMLParser;
import com.top_logic.template.xml.source.TemplateSource;

/**
 * This class expands a template given in form of a {@link Reader} using an {@link ExpansionModel}
 * and a {@link TemplateWriter}. Before the expansion can be done the template needs to be checked
 * for correct syntax and correctness in the context of the used {@link ExpansionModel}.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TemplateExpander implements TemplateVisitor<Void, TemplateWriter> {

	private final ExpansionModel                expansionModel;

	private final Stack<VarScope> scopes;

	private final List<TemplateProblem> _problems;
	
	/**
	 * Creates a {@link TemplateExpander}.
	 * 
	 * @param anExpansionModel
	 *        See {@link #getExpansionModel()}.
	 */
	protected TemplateExpander(ExpansionModel anExpansionModel) {
		this.expansionModel = anExpansionModel;
		this.scopes = new Stack<>();
		_problems = new ArrayList<>();
	}
	
	/**
	 * Encountered problems during template expansion.
	 */
	public List<TemplateProblem> getProblems() {
		return _problems;
	}

	/**
	 * Expands a template "in" a given reader using the given {@link ExpansionModel} and
	 * {@link TemplateWriter}. If errors occur during the parsing or syntax and model check a
	 * {@link TemplateException} will be thrown.
	 * 
	 * @param anExpansionModel the {@link ExpansionModel} for the communication with the object
	 *            model
	 * @param aTemplateReader the template to expand
	 * @param aWriter a {@link TemplateWriter} to write the expanded template to
	 * @throws TemplateException if anything goes wrong during the expansion process
	 */
	public static void expandTemplate(ExpansionModel anExpansionModel, Reader aTemplateReader, TemplateWriter aWriter) throws TemplateException{
		expandTemplate(anExpansionModel, aWriter, 
			ModelCheckVisitor.checkModel(
				SyntaxCheckVisitor.checkSyntax(
					TemplateExpander.parseTemplate(aTemplateReader)), anExpansionModel).getNode());
	}
	
	/** 
	 * Expands the template that is represented by the given node using the given writer.
	 * 
	 * @param aWriter
	 *        a {@link TemplateWriter} to write the expanded template
	 * @param aNode
	 *        a root node of a parsed template
	 * @throws TemplateException
	 *         If errors are detected during template expansion.
	 */
	public static void expandTemplate(ExpansionModel anExpansionModel, TemplateWriter aWriter, TemplateNode aNode)
			throws TemplateException {
		TemplateExpander expander = new TemplateExpander(anExpansionModel);
		expandTemplate(expander, aWriter, aNode);
		if (!expander.getProblems().isEmpty()) {
			throw new TemplateException(expander.getProblems(), Collections.<TemplateProblem> emptyList());
		}
	}
	
	public static void expandTemplate(TemplateExpander expander, TemplateWriter aWriter, TemplateNode aNode) {
		aNode.visit(expander, aWriter);
	}

	/**
	 * Parses the given reader and returns the root of the template in form of a
	 * {@link TemplateNode}.
	 * 
	 * @param aTemplateReader
	 *        a {@link Reader} that contains the template to parse
	 * @return a {@link TemplateNode} if no problems occurred.
	 * @throws TemplateException
	 *         with one error in case something goes wrong
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static TemplateNode parseTemplate(Reader aTemplateReader) throws TemplateException {
		try {
			TemplateParser theParser = new TemplateParser(aTemplateReader);
			TemplateNode   theNode   = theParser.Start();
			return theNode;
		}
		catch (ParseException e) {
			TemplateProblem theProblem = new TemplateProblem(e);
			throw new TemplateException(Collections.singletonList(theProblem), Collections.EMPTY_LIST);
		}
		catch (TokenMgrError err) {
			TemplateProblem theProblem = new TemplateProblem(err);
			throw new TemplateException(Collections.singletonList(theProblem), Collections.EMPTY_LIST);
		}
	}

	/**
	 * Returns the expansionModel.
	 */
	public final ExpansionModel getExpansionModel() {
		return expansionModel;
	}


	@Override
	public Void visitTemplate(Template aNode, TemplateWriter aWriter) {
		VarScope theScope = new VarScope();
		this.scopes.push(theScope);
		for (TemplateNode theNode:aNode.getNodes()) {
			theNode.visit(this, aWriter);
		}
		this.scopes.pop();
		return none;
	}

	@Override
	public Void visitAssignStatement(AssignStatement aNode, TemplateWriter aWriter) {
		Expression theExpression = aNode.getExpression();
		theExpression.visit(this, aWriter);
		Object value = theExpression.getVarSymbol().getValue();
		Maybe<String> formattedValue = expansionModel.format(value);
		if (formattedValue.hasValue()) {
			aWriter.writeLiteral(formattedValue.get());
		} else {
			aWriter.writeValue(value);
		}
		return none;
	}

	@Override
	public Void visitDefineStatement(DefineStatement aNode, TemplateWriter aWriter) {
		String                 theVariable     = aNode.getVariable();
		Expression             theExpression   = aNode.getExpression();
		
		theExpression.visit(this, aWriter);
		
		VarSymbol theVS        = theExpression.getVarSymbol();
		scopes.peek().declare(theVariable, theVS);
		
		return none;
	}

	@Override
	public Void visitBinaryExpression(BinaryExpression aNode, TemplateWriter aWriter) {
		Expression theLeftExpr  = aNode.getLeftExpression();
		Expression theRightExpr = aNode.getRightExpression();
		
		theLeftExpr.visit(this, aWriter);
		theRightExpr.visit(this, aWriter);
		
		Object       theLEVal         = theLeftExpr.getVarSymbol().getValue();
		Object       theREVal         = theRightExpr.getVarSymbol().getValue();
		Operator     theOperator      = aNode.getOperator();
		List<Object> theArgumentsList = new ArrayList<>();
		
		theArgumentsList.add(theLEVal);
		theArgumentsList.add(theREVal);
		
		Boolean  theResult = true;
		String   theFunctionName;
		Function theFunction;

		switch(theOperator) {
			case GT:  // >
			case GE:  // >=
			case LT:  // <
			case LE:  // <=
				break;
			case NE:  // !=
				theFunctionName = EqualsFunction.FUNCTION_NAME;
				theFunction = this.expansionModel.getImplementationForFunction(theFunctionName);
				theResult = !(Boolean) theFunction.apply(theArgumentsList);
				break;
			case AND: // &&
				theResult = (Boolean)theLEVal && (Boolean)theREVal;
				break;
			case OR:  // ||
				theResult = (Boolean)theLEVal || (Boolean)theREVal;
				break;
			case EQ:  // ==
			default:
				theFunctionName = EqualsFunction.FUNCTION_NAME;
				theFunction = this.expansionModel.getImplementationForFunction(theFunctionName);
				theResult = (Boolean) theFunction.apply(theArgumentsList);
		}
		
		VarSymbol theVS = aNode.getVarSymbol();
		theVS.setValue(theResult);
		return none;
	}

	@Override
	public Void visitConstantExpression(ConstantExpression aNode, TemplateWriter aWriter) {
		return none;
	}
	
	@Override
	public Void visitUnaryExpression(UnaryExpression aNode, TemplateWriter aWriter) {
		Expression theExpr  = aNode.getExpression();
		
		theExpr.visit(this, aWriter);
		
		Object   theLEVal    = theExpr.getVarSymbol().getValue();
		Operator theOperator = aNode.getOperator();
		
		Boolean  theResult = (Boolean) theLEVal;
		switch(theOperator) {
			case NOT:
				theResult = !theResult;
				break;
			case AND:
			case EQ:
			case GE:
			case GT:
			case LE:
			case LT:
			case NE:
			case OR:
				throw new IllegalArgumentException(theOperator + " is not a unary operator.");
		}
		
		VarSymbol theVS = aNode.getVarSymbol();
		theVS.setValue(theResult);
		return none;
	}

	@Override
	public Void visitForeachStatement(ForeachStatement aNode, TemplateWriter aWriter) {
		String                 theVariable     = aNode.getVariable();
		Expression             theExpression   = aNode.getExpression();
		Map<String, String>    theAttributes   = aNode.getAttributes();
		
		theExpression.visit(this, aWriter);
		
		VarSymbol theVS        = theExpression.getVarSymbol();
		Object    theValue     = theVS.getValue();
		boolean   hasValue     = theValue != null;
		String    theSeparator = theAttributes.get(TemplateNode.ATTR_SEPARATOR);
		boolean   hasSeparator = theSeparator != null;

		if (hasValue && theAttributes.containsKey(TemplateNode.ATTR_START)) {
			aWriter.writeLiteral(theAttributes.get(TemplateNode.ATTR_START));
		}
		
		Iterator<?> theIt = this.expansionModel.getIteratorForObject(theValue);
		
		while(theIt.hasNext()) {
			VarScope theScope = new VarScope();
			this.scopes.push(theScope);
			Object theObject   = theIt.next();
			VarSymbol  theTemp = new VarSymbol();

			theTemp.setValue(theObject);
			theScope.declare(theVariable, theTemp);
			aNode.getProductionNode().visit(this, aWriter);

			if (hasSeparator && theIt.hasNext()) {
				aWriter.writeLiteral(theSeparator);
			}
			this.scopes.pop();
		}
		if (hasValue && theAttributes.containsKey(TemplateNode.ATTR_END)) {
			aWriter.writeLiteral(theAttributes.get(TemplateNode.ATTR_END));
		}

		return none;
	}

	@Override
	public Void visitFunctionCall(FunctionCall aNode, TemplateWriter aWriter) {
		String   theFunctionName = aNode.getName();
		Function theFunction     = this.expansionModel.getImplementationForFunction(theFunctionName);
		
		List<Object> theArguments = new ArrayList<>();
		for (Expression theExpr:aNode.getArguments()) {
			theExpr.visit(this, aWriter);
			theArguments.add(theExpr.getVarSymbol().getValue());
		}
		
		Object theResult = theFunction.apply(theArguments);
		
		aNode.getVarSymbol().setValue(theResult);
		return none;
	}

	@Override
	public Void visitIfStatement(IfStatement aNode, TemplateWriter aWriter) {
		Expression             theCondition  = aNode.getCondition();
		TemplateNode           theThenStm    = aNode.getThenStm();
		TemplateNode           theElseStm    = aNode.getElseStm();
		
		theCondition.visit(this, aWriter);
		
		Boolean theResult = (Boolean) theCondition.getVarSymbol().getValue();
		
		if (theResult && theThenStm != null) {
			this.scopes.push(new VarScope());
			theThenStm.visit(this, aWriter);
			this.scopes.pop();
		}

		if (!theResult && theElseStm != null) {
			this.scopes.push(new VarScope());
			theElseStm.visit(this, aWriter);
			this.scopes.pop();
		}
		
		return none;
	}

	@Override
	public Void visitLiteralText(LiteralText aNode, TemplateWriter aWriter) {
		aWriter.writeLiteral(aNode.getValue());
		return none;
	}

	@Override
	public Void visitReference(Reference aNode, TemplateWriter aWriter) {
		boolean                isModelRef   = aNode.isModelRef();
		String                 theNameSpace = aNode.getNameSpace();
		List<String>           theNodePath  = aNode.getPath();
		String                 theName      = theNodePath.get(0);
		VarSymbol              theVS        = aNode.getVarSymbol();
		
		// If this Reference is a model reference a new MetaObject must be fetched using the full
		// nameSpace and name
		if (isModelRef) {
			// 1.  fetch the MetaObject for the current nameSpace and path.
			// 1a. each MetaObject for the path except the last MUST be of type MOStructure
			Object theValue = this.expansionModel.getValueForVariable(theNameSpace, theName);
			theValue = resolveValue(theValue, theNodePath);

			theVS.setValue(theValue);
			// 2. add the new MO to the current scope
			String key = aNode.getFullQualifiedName();
			if (isInScope(key)) {
				VarSymbol varSymbol = getVariable(key);
				assert varSymbol.getValue() == theVS.getValue();
				assert varSymbol.getType() == theVS.getType();
			} else {
				scopes.peek().declare(key, theVS);
			}
		}
		// This reference is (partly) defined via a model reference that must be in the scope. The
		// first part of the path-array is the assigned name. The rest of the path must be used to
		// fetch the new MetaObject.
		else {
			// 1. check whether the referencing MO is in the scope
			if(!isInScope(theName)) {
				return none;
			}
			// 2. fetch the MetaObject for the current path;
			Object attributeValue = getVariable(theName).getValue();
			attributeValue = resolveValue(attributeValue, theNodePath);
			theVS.setValue(attributeValue);
		}

		return none;
	}

	private Object resolveValue(Object object, List<String> attributePath) {
		if (attributePath.size() > 1) {
			try {
				for (int i = 1; i < attributePath.size(); i++) {
					object = this.expansionModel.getValueForAttribute(object, attributePath.get(i));
				}
			} catch (NoSuchAttributeException e) {
				throw new IllegalStateException("Illegal template state due to undetected model error."
					+ " Was the model check executed before the attempt to expand the template?", e);
			}
		}
		return object;
	}

	@Override
	public Void visitInvokeStatement(InvokeStatement node, TemplateWriter outerWriter) {
		switch (node.getTemplateFormat()) {
			case XML: {
				Map<String, Object> expandedParameterValues =
					expandStructuredParameterValue(node.getParameterValues());
				String expansion;
				try {
					expansion = expandXmlTemplate(node.getTemplateSource(), expandedParameterValues);
				} catch (IOException | XMLStreamException ex) {
					expansion = "[ERROR: " + ex.toString() + "]";
					String name = node
						.getTemplateSource().toString();
					int rowBegin = node.getRowBegin();
					int colBegin = node.getColBegin();
					_problems.add(new TemplateProblem(
						I18NConstants.ERROR_TEMPLATE_EXPANSION_FAILED__NAME_ROW_COL_ERROR.fill(name, rowBegin, colBegin,
							ex.getMessage()),
						name, Type.SYNTAX_ERROR, rowBegin, colBegin, node.getRowEnd(), node.getColEnd()));
				}
				outerWriter.writeLiteral(expansion);
				return none;
			}
			case COMS: {
				throw new UnsupportedOperationException(
					"The COMS syntax for the template language does not include the parameter model in the template itself."
						+ " It can therefore not be invoked by another template, as the parameter model is unknown.");
			}
			default: {
				throw new RuntimeException("Unknown template syntax type: " + node.getTemplateFormat());
			}
		}
	}

	private Map<String, Object> expandStructuredParameterValue(Map<String, ParameterValue<TemplateNode>> parameters) {
		Map<String, Object> expandedValues = new HashMap<>();
		for (Map.Entry<String, ParameterValue<TemplateNode>> parameter : parameters.entrySet()) {
			Object expandedValue = expandValue(parameter.getValue());
			expandedValues.put(parameter.getKey(), expandedValue);
		}
		return expandedValues;
	}

	private Object expandValue(ParameterValue<TemplateNode> value) {
		if (value.isPrimitiveValue()) {
			return expandPrimitiveParameterValue(value.asPrimitiveValue().getPrimitiveValue());
		}
		if (value.isStructuredValue()) {
			return expandStructuredParameterValue(value.asStructuredValue().getStructuredValue());
		}
		if (value.isListValue()) {
			return expandListParameterValue(value.asListValue().getListValue());
		}
		throw new UnsupportedOperationException("Unknown " + ParameterValue.class.getSimpleName()
			+ " instance: " + StringServices.getObjectDescription(value));
	}

	private Object expandPrimitiveParameterValue(TemplateNode value) {
		TemplateStringBufferWriter parameterValueWriter = new TemplateStringBufferWriter();
		this.scopes.push(new VarScope());
		// If the only thing in the attribute is an object reference,
		// get the object directly without serializing and parsing it.
		Maybe<Object> directValue = tryGetValueDirectly(value);
		if (directValue.hasValue()) {
			return directValue.get();
		} else {
			value.visit(this, parameterValueWriter);
		}
		this.scopes.pop();
		return parameterValueWriter.getBufferAsString();
	}

	private Maybe<Object> tryGetValueDirectly(TemplateNode templateNode) {
		if (!(templateNode instanceof AttributeValue)) {
			return Maybe.none();
		}
		List<TemplateNode> attributeNode = ((AttributeValue) templateNode).getContent();
		if (attributeNode.size() != 1) {
			return Maybe.none();
		}
		TemplateNode singleAttributeContent = attributeNode.get(0);
		if (!(singleAttributeContent instanceof AssignStatement)) {
			return Maybe.none();
		}
		Expression assignExpression = ((AssignStatement) singleAttributeContent).getExpression();
		if (!(assignExpression instanceof Reference)) {
			return Maybe.none();
		}
		assignExpression.visit(this, null);
		Object directValue = ((Reference) assignExpression).getVarSymbol().getValue();
		return Maybe.toMaybeButTreatNullAsValidValue(directValue);
	}

	private Object expandListParameterValue(List<ParameterValue<TemplateNode>> listValue) {
		List<Object> expandedValues = new ArrayList<>();
		for (ParameterValue<TemplateNode> valueEntry : listValue) {
			expandedValues.add(expandValue(valueEntry));
		}
		return expandedValues;
	}

	@Override
	public Void visitAttributeValue(AttributeValue attributeValue, TemplateWriter writer) {
		scopes.push(new VarScope());
		setQuoterState(writer, State.ATTRIBUTE_VALUE);
		for (TemplateNode node : attributeValue.getContent()) {
			node.visit(this, writer);
		}
		scopes.pop();
		setQuoterState(writer, State.NO_QUOTING);
		return none;
	}

	/** Setter for the {@link State} of the {@link TemplateWriter#getQuoter() Quoter}. */
	protected void setQuoterState(TemplateWriter writer, State newState) {
		if (writer.getQuoter() == null) {
			return;
		}
		writer.getQuoter().setState(newState);
	}

	/**
	 * Checks the scope stack if a variable of the given name was already assigned.
	 * 
	 * @param aVariableName a name to check
	 * @return <code>true</code> if one of the scopes contains the given name, <code>false</code>
	 *         otherwise.
	 */
	private boolean isInScope(String aVariableName) {
		int size = this.scopes.size() - 1;
		for (int i = size; i >= 0; i--) {
			VarScope theScope = this.scopes.get(i);
			if (theScope.isDeclared(aVariableName)) {
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
			if (theScope.isDeclared(aVariableName)) {
				return theScope.lookup(aVariableName);
			}
		}
		return null;
	}

	/**
	 * Expands the xml template read via the {@link Reader}.
	 * <p>
	 * The model is expected to be declared in the template file itself.
	 * </p>
	 * 
	 * @param templateSource
	 *        Must not be <code>null</code>.
	 * @param parameterValues
	 *        Is allowed to be <code>null</code>.
	 * @throws XMLStreamException
	 *         If parsing the template fails.
	 * @throws IOException
	 *         If reading accessing the template source fails.
	 */
	public static String expandXmlTemplate(TemplateSource templateSource, Map<String, ?> parameterValues)
			throws IOException, XMLStreamException {
		TemplateParseResult parseResult = new TemplateXMLParser().parse(templateSource);
		ConfigurableExpansionModel expansionModel = new ConfigurableExpansionModel(parseResult, parameterValues);
		return expand(parseResult.getTemplate(), expansionModel);
	}

	/**
	 * Expands the given XML {@link Template}.
	 */
	public static String expand(TemplateNode template, ConfigurableExpansionModel expansionModel) {
		TemplateStringBufferWriter writer = buildWriter(expansionModel);
		expandXMLToWriter(template, expansionModel, writer);
		return writer.getBufferAsString();
	}

	private static TemplateStringBufferWriter buildWriter(ConfigurableExpansionModel expansionModel) {
		TemplateStringBufferWriter writer = new TemplateStringBufferWriter();
		OutputFormat outputFormat = expansionModel.getParseResult().getSettings().getOutputFormat();
		writer.setQuoter(buildQuoter(outputFormat));
		return writer;
	}

	private static Quoter buildQuoter(OutputFormat outputFormat) {
		switch (outputFormat) {
			case TEXT: {
				return new PlainTextQuoter();
			}
			case XML: {
				return new XmlQuoter(State.NO_QUOTING);
			}
		}
		throw new UnsupportedOperationException("Output format " + outputFormat + " is not supported.");
	}

	private static void expandXMLToWriter(TemplateNode template, ExpansionModel expansionModel, TemplateWriter writer) {
		try {
			TemplateNode templateRoot = SyntaxCheckVisitor.checkSyntax(template);
			CheckResult checkResult = ModelCheckVisitor.checkModel(templateRoot, expansionModel);
			expandTemplate(expansionModel, writer, checkResult.getNode());
		} catch (TemplateException ex) {
			throw new RuntimeException(ex);
		}
	}

}
