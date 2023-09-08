/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.compile.transform;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.compile.SearchExpressionCompiler;
import com.top_logic.model.search.expr.interpreter.transform.Transformations;
import com.top_logic.model.search.expr.parser.ParseException;

/**
 * Test case for {@link Transformations}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTransformUtil extends AbstractSearchExpressionTest {

	public void testPulloutUnionFromFilter() throws ConfigurationException, ParseException {
		SearchExpression expr = search(
			"union(all(`TestTransformUtil:A`), all(`TestTransformUtil:B`)).filter(x -> $x.get(`TestTransformUtil:Common#name`) == 'foo')");

		SearchExpression transformed = SearchExpressionCompiler.applyTransform(expr, Transformations.pulloutUnionFromFilter());

		SearchExpression expected = search(
			"union(all(`TestTransformUtil:A`).filter(x -> $x.get(`TestTransformUtil:Common#name`) == 'foo'), all(`TestTransformUtil:B`).filter(x -> $x.get(`TestTransformUtil:Common#name`) == 'foo'))");

		assertEquals(expected.toString(), transformed.toString());
	}

	public void testPulloutNoneUnionFromFilter() throws ConfigurationException, ParseException {
		SearchExpression expr = search(
			"union(none(), all(`TestTransformUtil:A`), none()).filter(x -> $x.get(`TestTransformUtil:Common#name`) == 'foo')");

		SearchExpression transformed = SearchExpressionCompiler.applyTransform(expr,
			Transformations.inlineCalls(),

			Transformations.emptyUnion1(),
			Transformations.emptyUnion2(),
			Transformations.emptyIntersection1(),
			Transformations.emptyIntersection2(),
			Transformations.trueFilter(),
			Transformations.falseFilter(),
			Transformations.emptyFilter(),

			Transformations.trueIfElse(),
			Transformations.falseIfElse(),
			Transformations.booleanIfElse(),
			Transformations.andFalse1(),
			Transformations.andFalse2(),
			Transformations.andTrue1(),
			Transformations.andTrue2(),
			Transformations.orFalse1(),
			Transformations.orFalse2(),
			Transformations.orTrue1(),
			Transformations.orTrue2(),
			Transformations.literalNot(),
			Transformations.distributeNotAnd(),
			Transformations.distributeNotOr(),
			Transformations.distributeOrAnd1(),
			Transformations.distributeOrAnd2(),

			Transformations.pulloutUnionFromFilter());

		SearchExpression expected = search(
			"all(`TestTransformUtil:A`).filter(x -> $x.get(`TestTransformUtil:Common#name`) == 'foo')");

		assertEquals(expected.toString(), transformed.toString());
	}

	public static Test suite() {
		return suite(TestTransformUtil.class);
	}

}
