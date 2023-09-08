/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import static com.top_logic.model.search.expr.query.QueryExecutor.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLModel;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.schema.ObjectConf;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.SearchExpressionParserTokenManager;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * Utilities for tests operating on {@link SearchExpression}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractSearchExpressionTest extends BasicTestCase {

	protected Object eval(String script, Object... args) throws ParseException {
		SearchExpression expr = search(script);
		return eval(expr, args);
	}

	protected Object eval(SearchExpression expr, Object... args) {
		Object resultDirect = execute(expr, args);
		Object resultCompiled = executeCompiled(expr, args);
		assertEquals(resultDirect, resultCompiled);
		return resultCompiled;
	}

	protected static SearchExpression search(String expr) throws ParseException {
		return build(parse(expr));
	}

	protected static SearchExpression build(Expr search) {
		return SearchBuilder.toSearchExpression(model(), search);
	}

	protected static Expr parse(String expr) throws ParseException {
		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(expr));
		Expr result = parser.expr();

		try {
			ExprFormat.checkFullyRead("", expr, parser);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		return result;
	}

	protected static SearchExpression textWithEmbeddedExpressions(String expr) throws ParseException {
		return build(parseTextWithEmbeddedExpressions(expr));
	}

	protected static Expr parseTextWithEmbeddedExpressions(String expr) throws ParseException {
		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(expr));
		parser.token_source.SwitchTo(SearchExpressionParserTokenManager.TEXT);
		Expr result = parser.textWithEmbeddedExpressions();
		try {
			ExprFormat.checkFullyRead("", expr, parser);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		return result;
	}

	protected static Object executeAsSet(SearchExpression search, Object... args) {
		return asSet(execute(search, args));
	}

	protected static Object execute(SearchExpression search, Object... args) {
		return executeCompiled(search, args);
	}

	protected static Object execute(DisplayContext displayContext, TagWriter out, SearchExpression search, Object... args) {
		return executeCompiled(displayContext, out, search, args);
	}

	protected static Object executeCompiledAsSet(SearchExpression search, Object... args) {
		return asSet(executeCompiled(search, args));
	}

	protected static Object executeCompiled(SearchExpression search, Object... args) {
		return executeCompiled(null, null, search, args);
	}

	protected static Object executeCompiled(DisplayContext displayContext, TagWriter out, SearchExpression search, Object... args) {
		QueryExecutor executor = compile(kb(), model(), search);
		return execute(displayContext, out, executor, args);
	}

	protected static Object execute(DisplayContext displayContext, TagWriter out, QueryExecutor executor, Object... args) {
		return executor.executeWith(displayContext, out, Args.some(args));
	}

	protected static Set<?> asSet(Object result) {
		return toSet((Iterable<?>) result);
	}

	protected static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	protected XMLInstanceImporter scenario(String name) throws ConfigurationException {
		XMLInstanceImporter importer;
		try (Transaction tx = kb().beginTransaction()) {
			List<ObjectConf> configs = XMLInstanceImporter.loadConfigs(resource(name));
			importer = new XMLInstanceImporter(model(), ModelService.getInstance().getFactory());
			importer.setLog(new AssertProtocol());
			importer.importInstances(configs);

			tx.commit();
		}
		return importer;
	}

	protected ClassRelativeBinaryContent resource(String name) {
		return new ClassRelativeBinaryContent(getClass(), name);
	}

	protected static TLModel model() {
		return ModelService.getApplicationModel();
	}

	protected static Test suite(Class<? extends Test> test, BasicRuntimeModule<?>... additionalModules) {
		BasicRuntimeModule<?>[] modules = getModules(additionalModules);

		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(test),
				modules));
	}

	private static BasicRuntimeModule<?>[] getModules(BasicRuntimeModule<?>... additionalModules) {
		ArrayList<BasicRuntimeModule<?>> modules = new ArrayList<>();

		modules.add(SearchBuilder.Module.INSTANCE);
		modules.add(ModelService.Module.INSTANCE);
		modules.add(LabelProviderService.Module.INSTANCE);
		
		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}

}
