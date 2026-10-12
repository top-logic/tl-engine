/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.trace;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.model.search.expr.config.operations.UsesSecurity;

/**
 * Test TL-Script functions reading an attribute of the object referenced by the
 * {@link AbstractScriptTracerTest#OTHER} reference of their argument.
 *
 * @see TestScriptTracerJavaFunctions
 */
@ScriptPrefix(OtherValueFunctions.PREFIX)
public class OtherValueFunctions extends TLScriptFunctions {

	/**
	 * The {@link ScriptPrefix} of the functions in this class.
	 */
	public static final String PREFIX = "testOther";

	/**
	 * Name of the {@link EvalContext} parameter of the functions in this class.
	 */
	public static final String CONTEXT_PARAM = "definitions";

	/**
	 * Reads the given attribute of the object referenced by the given object, through the
	 * evaluation context received as first parameter.
	 *
	 * @param definitions
	 *        The context of the evaluation.
	 * @param self
	 *        The object whose referenced object is read.
	 * @param attribute
	 *        The name of the attribute to read.
	 * @return The value of the attribute.
	 *
	 * @implNote The function is declared to be evaluable at compile time to test that a function
	 *           receiving the evaluation context is evaluated at runtime nevertheless.
	 */
	@SideEffectFree(canEvaluateAtCompileTime = true)
	public static Object value(EvalContext definitions, @Mandatory TLObject self, @Mandatory String attribute) {
		TLObject other = (TLObject) definitions.access(self, part(self, AbstractScriptTracerTest.OTHER), false);
		if (other == null) {
			return null;
		}
		return definitions.access(other, part(other, attribute), false);
	}

	/**
	 * Reads the given attribute of the object referenced by the given object, through the
	 * evaluation context received as last parameter, with the security flag of the call.
	 *
	 * @param self
	 *        The object whose referenced object is read.
	 * @param withSecurity
	 *        Whether the read access is checked.
	 * @param attribute
	 *        The name of the attribute to read.
	 * @param definitions
	 *        The context of the evaluation.
	 * @return The value of the attribute.
	 */
	public static Object valueSecured(@Mandatory TLObject self, @UsesSecurity boolean withSecurity,
			@Mandatory String attribute, EvalContext definitions) {
		TLObject other =
			(TLObject) definitions.access(self, part(self, AbstractScriptTracerTest.OTHER), withSecurity);
		if (other == null) {
			return null;
		}
		return definitions.access(other, part(other, attribute), withSecurity);
	}

	/**
	 * Reads the given attribute of the object referenced by the given object, through
	 * {@link TLObject#tValue(TLStructuredTypePart)}.
	 *
	 * @param self
	 *        The object whose referenced object is read.
	 * @param attribute
	 *        The name of the attribute to read.
	 * @return The value of the attribute.
	 */
	public static Object valuePlain(@Mandatory TLObject self, @Mandatory String attribute) {
		TLObject other = (TLObject) self.tValue(part(self, AbstractScriptTracerTest.OTHER));
		if (other == null) {
			return null;
		}
		return other.tValue(part(other, attribute));
	}

	private static TLStructuredTypePart part(TLObject self, String attribute) {
		return self.tType().getPartOrFail(attribute);
	}

}
