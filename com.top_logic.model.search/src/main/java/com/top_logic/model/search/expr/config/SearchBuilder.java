/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config;

import static com.top_logic.mig.html.HTMLConstants.*;
import static com.top_logic.model.search.expr.SearchExpressionFactory.*;
import static com.top_logic.model.search.expr.SearchExpressions.*;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.basic.treexf.FactoryResolver;
import com.top_logic.basic.treexf.TreeMaterializer;
import com.top_logic.basic.treexf.TreeMaterializer.Factory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.search.expr.DynamicAll;
import com.top_logic.model.search.expr.DynamicGet;
import com.top_logic.model.search.expr.DynamicReferers;
import com.top_logic.model.search.expr.DynamicSet;
import com.top_logic.model.search.expr.Recursion;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.TupleExpression.Coord;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Access;
import com.top_logic.model.search.expr.config.dom.Expr.Add;
import com.top_logic.model.search.expr.config.dom.Expr.And;
import com.top_logic.model.search.expr.config.dom.Expr.Apply;
import com.top_logic.model.search.expr.config.dom.Expr.Arg;
import com.top_logic.model.search.expr.config.dom.Expr.Assign;
import com.top_logic.model.search.expr.config.dom.Expr.At;
import com.top_logic.model.search.expr.config.dom.Expr.Attribute;
import com.top_logic.model.search.expr.config.dom.Expr.AttributeContent;
import com.top_logic.model.search.expr.config.dom.Expr.AttributeContents;
import com.top_logic.model.search.expr.config.dom.Expr.Block;
import com.top_logic.model.search.expr.config.dom.Expr.Chain;
import com.top_logic.model.search.expr.config.dom.Expr.Cmp;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.config.dom.Expr.Div;
import com.top_logic.model.search.expr.config.dom.Expr.EmbeddedExpression;
import com.top_logic.model.search.expr.config.dom.Expr.EndTag;
import com.top_logic.model.search.expr.config.dom.Expr.Eq;
import com.top_logic.model.search.expr.config.dom.Expr.False;
import com.top_logic.model.search.expr.config.dom.Expr.Html;
import com.top_logic.model.search.expr.config.dom.Expr.HtmlContent;
import com.top_logic.model.search.expr.config.dom.Expr.Method;
import com.top_logic.model.search.expr.config.dom.Expr.Mod;
import com.top_logic.model.search.expr.config.dom.Expr.ModuleLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Mul;
import com.top_logic.model.search.expr.config.dom.Expr.Not;
import com.top_logic.model.search.expr.config.dom.Expr.Null;
import com.top_logic.model.search.expr.config.dom.Expr.NumberLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Operation;
import com.top_logic.model.search.expr.config.dom.Expr.Or;
import com.top_logic.model.search.expr.config.dom.Expr.PartLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.ResKeyLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.ResKeyLiteral.LangStringConfig;
import com.top_logic.model.search.expr.config.dom.Expr.ResKeyReference;
import com.top_logic.model.search.expr.config.dom.Expr.SingletonLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.StartTag;
import com.top_logic.model.search.expr.config.dom.Expr.StringLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Sub;
import com.top_logic.model.search.expr.config.dom.Expr.TextContent;
import com.top_logic.model.search.expr.config.dom.Expr.True;
import com.top_logic.model.search.expr.config.dom.Expr.Tuple;
import com.top_logic.model.search.expr.config.dom.Expr.TypeLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Var;
import com.top_logic.model.search.expr.config.dom.Expr.Wrapped;
import com.top_logic.model.search.expr.config.dom.ExprVisitor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.search.expr.html.AttributeMacro;
import com.top_logic.model.search.ui.help.HelpPageIndex;
import com.top_logic.model.search.ui.help.HelpPageIndex.Page;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link ExprVisitor} that creates operational {@link SearchExpression} from an {@link Expr} parse
 * tree.
 * 
 * @see #toSearchExpression(TLModel, Expr)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SearchBuilder<C extends SearchBuilder.Config<?>> extends ConfiguredManagedClass<C>
		implements ExprVisitor<SearchExpression, TLModel, ConfigurationException>
{

	/** CSS class for the documentation of a TL script function. */
	public static final String DOCUMENTATION_CSS_CLASS = "tlScriptDoc";

	static final SearchExpression[] EMPTY_EXPR_ARRAY = new SearchExpression[0];

	/**
	 * Configuration options for {@link SearchBuilder}.
	 */
	public interface Config<I extends SearchBuilder<?>> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Method plug-ins.
		 */
		@Name("methods")
		@Key(MethodBuilder.Config.NAME_ATTRIBUTE)
		List<MethodBuilder.Config<?>> getMethods();

		/**
		 * Resolvers for dynamically defined methods.
		 */
		@Name("method-resolvers")
		List<PolymorphicConfiguration<MethodResolver>> getMethodResolvers();

	}

	private Map<String, MethodBuilder<?>> _builders = new HashMap<>();

	private final TreeMaterializer _treeMaterializer;

	private List<MethodResolver> _resolvers;

	/**
	 * Creates a {@link SearchBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SearchBuilder(InstantiationContext context, C config) {
		super(context, config);
		
		List<MethodResolver> resolvers = TypedConfiguration.getInstanceList(context, config.getMethodResolvers());
		_resolvers = resolvers;

		Map<Object, Factory> factories = TreeMaterializer.buildFactoryMap(SearchExpressionFactory.class);
		
		for (MethodBuilder.Config<?> builderConfig : config.getMethods()) {
			createBuilder(context, builderConfig, factories);
		}

		/* Added Builder for DynamicGet and DynamicSet programmatically. These expressions are just
		 * a different implementation of "get" and "set" but need a builder. It is not configured
		 * because it is internal and must not be offered to the user when creating an expression
		 * within the application. */
		createBuilder(context, TypedConfiguration.newConfigItem(DynamicGet.Builder.Config.class), factories);
		createBuilder(context, TypedConfiguration.newConfigItem(DynamicSet.Builder.Config.class), factories);
		createBuilder(context, TypedConfiguration.newConfigItem(DynamicReferers.Builder.Config.class), factories);
		createBuilder(context, TypedConfiguration.newConfigItem(DynamicAll.Builder.Config.class), factories);
		
		FactoryResolver factoryResolver = new FactoryResolver() {
			@Override
			public Factory getFactory(Object type) {
				Factory result = factories.get(type);
				if (result != null) {
					return result;
				}

				for (MethodResolver resolver : resolvers) {
					Factory dynamicFactory = resolver.getFactory(type);
					if (dynamicFactory != null) {
						return dynamicFactory;
					}
				}

				return null;
			}
		};

		_treeMaterializer = new TreeMaterializer(factoryResolver);
	}

	private void createBuilder(InstantiationContext context, MethodBuilder.Config<?> builderConfig,
			Map<Object, Factory> factories) {
		MethodBuilder<?> builder = context.getInstance(builderConfig);
		_builders.put(builderConfig.getName(), builder);
		
		Object id = builder.getId();
		if (id != null) {
			factories.put(id, new BuilderFactory(builder));
		} else {
			Class<?> binding = JavaTypeUtil.getTypeBound(builder.getClass(), MethodBuilder.class, 0);
			if (!Modifier.isAbstract(binding.getModifiers())) {
				factories.put(binding, new BuilderFactory(builder));
			}
		}
	}

	/**
	 * The list of all defined function names.
	 */
	public List<String> getMethodNames() {
		List<String> result = internalMethodNames();
		for (MethodResolver resolver : _resolvers) {
			result.addAll(resolver.getMethodNames());
		}
		return result;
	}

	private List<String> internalMethodNames() {
		return getConfig().getMethods()
			.stream()
			.map(NamedConfiguration::getName)
			.collect(Collectors.toList());
	}

	/**
	 * Retrieved the documentation for the given method with the given function. The returned value
	 * is an HTML snipplet describing the function and its parameters.
	 * 
	 * @param context
	 *        Rendering context.
	 * @param functionName
	 *        Name of the function to get documentation for.
	 * 
	 * @return Documentation for the specified function, if any, regardless of whether there is no
	 *         function with that name or the function exists but has no documentation.
	 */
	public Optional<String> getDocumentation(DisplayContext context, String functionName) {
		Optional<String> documentation = internalDocumentation(context, functionName);
		if (documentation.isPresent()) {
			return documentation;
		}
		for (MethodResolver resolver : _resolvers) {
			documentation = resolver.getDocumentation(context, functionName);
			if (documentation.isPresent()) {
				return documentation;
			}
		}

		return documentation;
	}

	private Optional<String> internalDocumentation(DisplayContext context, String method) {
		String language = context.getResources().getLocale().getLanguage();
		Page page = HelpPageIndex.getPage(language, method);
		if (page != null) {
			try {
				StringWriter out = new StringWriter();
				try (TagWriter writer = new TagWriter(out)) {
					writeDocumentation(writer, page);
				}
				return Optional.of(out.toString());
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("deprecation")
	private void writeDocumentation(TagWriter out, Page page) throws IOException {
		out.beginTag(DIV, CLASS_ATTR, DOCUMENTATION_CSS_CLASS);

		out.beginTag(H2);
		out.writeText(page.getTitle());
		out.endTag(H2);

		Path content = page.getContent();
		Document document;
		try (InputStream in = Files.newInputStream(content)) {
			document = Jsoup.parse(in, StringServices.UTF8, content.toUri().toString());
		}
		// content is valid as Jsoup generates it.
		out.writeContent(document.body().html());

		out.endTag(DIV);
	}

	/**
	 * Function name defining a {@link Recursion}.
	 */
	public static final String RECURSION_FUN = "recursion";

	@Override
	public SearchExpression visit(Wrapped expr, TLModel arg) throws ConfigurationException {
		return descend(expr.getExpr(), arg);
	}

	@Override
	public SearchExpression visit(And expr, TLModel arg) throws ConfigurationException {
		SearchExpression result = null;
		for (Expr operand : expr.getOperands()) {
			if (result == null) {
				result = descend(operand, arg);
			} else {
				result = and(result, descend(operand, arg));
			}
		}
		if (result == null) {
			return trueLiteral();
		} else {
			return result;
		}
	}

	@Override
	public SearchExpression visit(Or expr, TLModel arg) throws ConfigurationException {
		SearchExpression result = null;
		for (Expr operand : expr.getOperands()) {
			if (result == null) {
				result = descend(operand, arg);
			} else {
				result = or(result, descend(operand, arg));
			}
		}
		if (result == null) {
			return falseLiteral();
		} else {
			return result;
		}
	}

	@Override
	public SearchExpression visit(Add expr, TLModel arg) throws ConfigurationException {
		return add(descendLeft(expr, arg), descendRight(expr, arg));
	}

	@Override
	public SearchExpression visit(Sub expr, TLModel arg) throws ConfigurationException {
		return sub(descendLeft(expr, arg), descendRight(expr, arg));
	}

	@Override
	public SearchExpression visit(Mul expr, TLModel arg) throws ConfigurationException {
		return mul(descendLeft(expr, arg), descendRight(expr, arg));
	}

	@Override
	public SearchExpression visit(Div expr, TLModel arg) throws ConfigurationException {
		return div(descendLeft(expr, arg), descendRight(expr, arg));
	}

	@Override
	public SearchExpression visit(Mod expr, TLModel arg) throws ConfigurationException {
		return mod(descendLeft(expr, arg), descendRight(expr, arg));
	}

	private SearchExpression descendLeft(Operation expr, TLModel arg) throws ConfigurationException {
		return descend(expr.getOperands().get(0), arg);
	}

	private SearchExpression descendRight(Operation expr, TLModel arg) throws ConfigurationException {
		return descend(expr.getOperands().get(1), arg);
	}

	@Override
	public SearchExpression visit(Not expr, TLModel arg) throws ConfigurationException {
		return not(descend(expr.getExpr(), arg));
	}

	@Override
	public SearchExpression visit(Eq expr, TLModel arg) throws ConfigurationException {
		return isEqual(descend(expr.getOperands().get(0), arg), descend(expr.getOperands().get(1), arg));
	}

	@Override
	public SearchExpression visit(StringLiteral expr, TLModel arg) throws ConfigurationException {
		return literal(expr.getValue());
	}

	@Override
	public SearchExpression visit(Html expr, TLModel arg) throws ConfigurationException {
		SearchExpression[] contents = new SearchExpression[expr.getContents().size()];
		int idx = 0;
		for (HtmlContent content : expr.getContents()) {
			contents[idx++] = content.visit(this, arg);
		}
		return html(contents);
	}

	@Override
	public SearchExpression visit(StartTag node, TLModel arg) throws ConfigurationException {
		AttributeMacro[] attributes = new AttributeMacro[node.getAttributes().size()];
		int idx = 0;
		for (Attribute attr : node.getAttributes()) {
			attributes[idx++] = (AttributeMacro) descend(attr, arg);
		}
		return SearchExpressionFactory.tag(node.getTag(), node.isEmpty(), attributes);
	}

	@Override
	public SearchExpression visit(Attribute expr, TLModel arg) throws ConfigurationException {
		return SearchExpressionFactory.attr(expr.getName(), expr.getValue().visit(this, arg));
	}

	@Override
	public SearchExpression visit(EndTag node, TLModel arg) throws ConfigurationException {
		return SearchExpressions.endTag(node.getTag());
	}

	@Override
	public SearchExpression visit(EmbeddedExpression node, TLModel arg) throws ConfigurationException {
		return node.getExpr().visit(this, arg);
	}

	@Override
	public SearchExpression visit(TextContent node, TLModel arg) throws ConfigurationException {
		return SearchExpressions.text(node.getValue());
	}

	@Override
	public SearchExpression visit(AttributeContents value, TLModel arg) throws ConfigurationException {
		SearchExpression[] contents = new SearchExpression[value.getValues().size()];
		int idx = 0;
		for (AttributeContent content : value.getValues()) {
			contents[idx++] = content.visit(this, arg);
		}
		return SearchExpressionFactory.html(contents);
	}

	@Override
	public SearchExpression visit(ResKeyReference expr, TLModel arg) throws ConfigurationException {
		return literal(ResKey.internalModel(expr.getKey()));
	}

	@Override
	public SearchExpression visit(ResKeyLiteral expr, TLModel arg) throws ConfigurationException {
		Builder builder = ResKey.builder(expr.getKey());
		fillLiteralBuilder(builder, expr);
		return literal(builder.build());
	}

	private void fillLiteralBuilder(Builder builder, ResKeyLiteral expr) {
		for (LangStringConfig translation : expr.getValues().values()) {
			builder.add(translation.getLang(), translation.getText());
		}
		for (Entry<String, ResKeyLiteral> suffixes : expr.getSuffixes().entrySet()) {
			Builder suffixBuilder = builder.suffix(suffixes.getKey());
			fillLiteralBuilder(suffixBuilder, suffixes.getValue());
		}
	}

	@Override
	public SearchExpression visit(NumberLiteral expr, TLModel arg) throws ConfigurationException {
		return literal(SearchExpression.toNumber(expr.getValue()));
	}

	@Override
	public SearchExpression visit(ModuleLiteral expr, TLModel arg) throws ConfigurationException {
		return literal(resolveModule(expr, arg));
	}

	@Override
	public SearchExpression visit(SingletonLiteral expr, TLModel arg) throws ConfigurationException {
		return literal(resolveSingleton(expr, arg));
	}

	@Override
	public SearchExpression visit(TypeLiteral expr, TLModel arg) throws ConfigurationException {
		return literal(resolveType(expr, arg));
	}

	@Override
	public SearchExpression visit(PartLiteral expr, TLModel arg) throws ConfigurationException {
		return literal(resolvePart(expr, arg));
	}

	@Override
	public SearchExpression visit(Var expr, TLModel arg) throws ConfigurationException {
		return var(expr.getName());
	}

	@Override
	public SearchExpression visit(Define expr, TLModel arg) throws ConfigurationException {
		return lambda(expr.getName(), descend(expr.getExpr(), arg));
	}

	@Override
	public SearchExpression visit(Apply expr, TLModel arg) throws ConfigurationException {
		return call(descend(expr.getFun(), arg), descend(expr.getArg(), arg));
	}

	@Override
	public SearchExpression visit(At expr, TLModel arg) throws ConfigurationException {
		return at(descend(expr.getSelf(), arg), descend(expr.getIndex(), arg));
	}

	@Override
	public SearchExpression visit(Assign expr, TLModel arg) throws ConfigurationException {
		return descend(expr.getExpr(), arg);
	}

	@Override
	public SearchExpression visit(Tuple expr, TLModel arg) throws ConfigurationException {
		return tuple(coords(expr.getCoords(), arg));
	}

	private Coord[] coords(List<com.top_logic.model.search.expr.config.dom.Expr.Tuple.Coord> coords, TLModel arg)
			throws ConfigurationException {
		int size = coords.size();
		Coord[] result = new Coord[size];
		for (int n = 0; n < size; n++) {
			com.top_logic.model.search.expr.config.dom.Expr.Tuple.Coord coord = coords.get(n);
			result[n] = coord(coord.isOptional(), coord.getName(), coord.getExpr().visit(this, arg));
		}
		return result;
	}

	@Override
	public SearchExpression visit(Block expr, TLModel arg) throws ConfigurationException {
		List<Expr> contents = expr.getContents();
		return nestAssigns(contents, 0, contents.size(), arg);
	}

	private SearchExpression nestAssigns(List<Expr> contents, int start, int stop, TLModel arg)
			throws ConfigurationException {
		List<SearchExpression> result = new ArrayList<>();
		for (int n = start; n < stop; n++) {
			Expr expr = contents.get(n);
			if (expr instanceof Assign) {
				Assign assign = (Assign) expr;

				SearchExpression body = nestAssigns(contents, n + 1, stop, arg);
				result.add(let(assign.getName(), descend(assign.getExpr(), arg), body));
				break;
			} else {
				result.add(descend(expr, arg));
			}
		}
		return toBlock(result);
	}

	private SearchExpression toBlock(List<SearchExpression> result) {
		if (result.size() == 1) {
			return result.get(0);
		}
		return block(result.toArray(EMPTY_EXPR_ARRAY));
	}

	@Override
	public SearchExpression visit(Cmp expr, TLModel arg) throws ConfigurationException {
		List<Expr> operands = expr.getOperands();
		if (operands.size() != 2) {
			throw error("Expecting exactly two operands in the compare operation: " + expr);
		}
		return compareOp(expr.getKind(), descend(operands.get(0), arg), descend(operands.get(1), arg));
	}

	@Override
	public SearchExpression visit(Access expr, TLModel arg) throws ConfigurationException {
		SearchExpression self = descend(expr.getExpr(), arg);

		Method method = expr.getMethod();
		Argument[] args = descendArgs(self, method.getArgs(), arg);

		MethodBuilder<?> builder = getBuilder(method);
		return builder.build(method, args);
	}

	@Override
	public SearchExpression visit(Chain expr, TLModel arg) throws ConfigurationException {
		SearchExpression self = descend(expr.getExpr(), arg);

		Object anonymous = new NamedConstant("<chainvar>");

		Method method = expr.getMethod();
		Argument[] args = descendArgs(var(anonymous), method.getArgs(), arg);

		MethodBuilder<?> builder = getBuilder(method);
		return call(lambda(anonymous, block(builder.build(method, args), var(anonymous))), self);
	}

	@Override
	public SearchExpression visit(Method expr, TLModel arg) throws ConfigurationException {
		Method method = expr;
		Argument[] args = descendArgs(method.getArgs(), arg);

		MethodBuilder<?> builder = getBuilder(method);
		return builder.build(method, args);
	}

	private ConfigurationException error(String message) throws ConfigurationException {
		throw new ConfigurationException(message);
	}

	private MethodBuilder<?> getBuilder(Method expr) throws ConfigurationException {
		String methodName = expr.getName();
		MethodBuilder<?> builder = getBuilder(methodName);
		if (builder == null) {
			throw error("Unknown method '" + methodName + "' in " + ExprPrinter.toString(expr));
		}
		return builder;
	}

	/**
	 * The {@link MethodBuilder} for the given method name, or <code>null</code> if no such method
	 * is defined.
	 */
	public MethodBuilder<?> getBuilder(String methodName) {
		MethodBuilder<?> result = _builders.get(methodName);

		if (result != null) {
			return result;
		}

		return getDynamicBuilder(methodName);
	}

	private MethodBuilder<?> getDynamicBuilder(String methodName) {
		for (MethodResolver resolver : _resolvers) {
			MethodBuilder<?> result = resolver.getMethodBuilder(methodName);
			if (result != null) {
				return result;
			}
		}

		// Not found.
		return null;
	}

	@Override
	public SearchExpression visit(Null expr, TLModel arg) throws ConfigurationException {
		return literal(null);
	}

	@Override
	public SearchExpression visit(True expr, TLModel arg) throws ConfigurationException {
		return trueLiteral();
	}

	private SearchExpression trueLiteral() {
		return literal(Boolean.TRUE);
	}

	@Override
	public SearchExpression visit(False expr, TLModel arg) throws ConfigurationException {
		return falseLiteral();
	}

	private SearchExpression falseLiteral() {
		return literal(Boolean.FALSE);
	}

	private Object resolveModule(ModuleLiteral expr, TLModel arg) throws ConfigurationException {
		return TLModelUtil.findModule(arg, expr.getName());
	}

	private Object resolveSingleton(SingletonLiteral expr, TLModel arg) throws ConfigurationException {
		return TLModelUtil.findSingleton(TLModelUtil.findModule(arg, expr.getModule()), expr.getName());
	}

	private TLType resolveType(Expr expr, TLModel arg) throws ConfigurationException {
		if (expr instanceof TypeLiteral) {
			TypeLiteral literal = (TypeLiteral) expr;
			return TLModelUtil.findType(arg, literal.getModule() + ":" + literal.getName());
		}
		throw error("Expecting a type literal, found: " + expr);
	}

	private <T extends TLTypePart> T resolvePart(Expr expr, TLModel arg) throws ConfigurationException {
		if (expr instanceof PartLiteral) {
			PartLiteral literal = (PartLiteral) expr;
			String moduleName = literal.getModule();
			String typeName = literal.getType();
			String partName = literal.getName();
			return TLModelUtil.findPart(arg, moduleName, typeName, partName);
		}
		throw error("Expecting a type part literal, found: " + expr);
	}

	private SearchExpression descend(Expr expr, TLModel arg) throws ConfigurationException {
		return expr.visit(this, arg);
	}

	private Argument descend(Arg argument, TLModel arg) throws ConfigurationException {
		return new Argument(argument.getName(), argument.getValue().visit(this, arg));
	}

	private Argument[] descendArgs(SearchExpression self, List<Arg> args, TLModel arg) throws ConfigurationException {
		Argument[] result = new Argument[args.size() + 1];
		result[0] = new Argument(null, self);
		for (int n = 0, cnt = args.size(); n < cnt; n++) {
			result[n + 1] = descend(args.get(n), arg);
		}
		return result;
	}

	private Argument[] descendArgs(List<Arg> args, TLModel arg) throws ConfigurationException {
		Argument[] result = new Argument[args.size()];
		for (int n = 0, cnt = args.size(); n < cnt; n++) {
			result[n] = descend(args.get(n), arg);
		}
		return result;
	}

	private Argument[] descendExceptFirst(List<Arg> exprs, TLModel arg) throws ConfigurationException {
		int cnt = exprs.size();
		Argument[] result = new Argument[cnt - 1];
		for (int n = 1; n < cnt; n++) {
			result[n - 1] = descend(exprs.get(n), arg);
		}
		return result;
	}

	/**
	 * Convenience method for converting an {@link Expr} configuration to an operational
	 * {@link SearchExpression}.
	 */
	public static SearchExpression toSearchExpression(TLModel model, Expr expr) {
		SearchExpression search;
		try {
			search = expr.visit(getInstance(), model);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		return search;
	}

	/**
	 * The {@link TreeMaterializer} for transforming {@link SearchExpression} trees.
	 */
	public TreeMaterializer getTreeMaterializer() {
		return _treeMaterializer;
	}

	/**
	 * Singleton instance of this service.
	 */
	public static SearchBuilder<?> getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	private static final class BuilderFactory implements Factory {
	
		private final MethodBuilder<?> _builder;
	
		/** 
		 * Creates a {@link BuilderFactory}.
		 */
		public BuilderFactory(MethodBuilder<?> builder) {
			_builder = builder;
		}
	
		@Override
		public Object create(Object type, Object[] children) {
			int argCnt = children.length;
			SearchExpression[] args = new SearchExpression[argCnt];
			copy(children, 0, args, 0, argCnt);
			try {
				return _builder.build(null, args);
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}
	
		private void copy(Object[] result, int fromIndex, SearchExpression[] target, int toIndex, int length) {
			for (int n = 0; n < length; n++) {
				target[toIndex + n] = asExpr(result[fromIndex + n]);
			}
		}

		private SearchExpression asExpr(Object object) {
			if (object instanceof SearchExpression) {
				return (SearchExpression) object;
			} else {
				return literal(object);
			}
		}

		@Override
		public String toString() {
			return "Factory(" + _builder.getClass().getName() + ")";
		}
	}

	/**
	 * Singleton reference for {@link SearchBuilder}.
	 */
	public static final class Module extends TypedRuntimeModule<SearchBuilder<?>> {

		/**
		 * Singleton {@link SearchBuilder.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<SearchBuilder<?>> getImplementation() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Class<SearchBuilder<?>> result = (Class) SearchBuilder.class;
			return result;
		}

	}

}
