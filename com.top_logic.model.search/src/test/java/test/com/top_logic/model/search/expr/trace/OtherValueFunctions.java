/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.trace;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
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
	 * Reads the given attribute of the object referenced by the given object, through
	 * {@link TLObject#tValue(TLStructuredTypePart)}.
	 *
	 * @param self
	 *        The object whose referenced object is read.
	 * @param attribute
	 *        The name of the attribute to read.
	 * @return The value of the attribute.
	 */
	public static Object value(@Mandatory TLObject self, @Mandatory String attribute) {
		TLObject other = (TLObject) self.tValue(part(self, AbstractScriptTracerTest.OTHER));
		if (other == null) {
			return null;
		}
		return other.tValue(part(other, attribute));
	}

	/**
	 * Reads the given attribute of the object referenced by the given object, through
	 * {@link TLObject#tValueByName(String)}.
	 *
	 * @param self
	 *        The object whose referenced object is read.
	 * @param withSecurity
	 *        Whether the call is evaluated with security. Does not influence the read.
	 * @param attribute
	 *        The name of the attribute to read.
	 * @return The value of the attribute.
	 */
	public static Object valueByName(@Mandatory TLObject self, @UsesSecurity boolean withSecurity,
			@Mandatory String attribute) {
		TLObject other = (TLObject) self.tValueByName(AbstractScriptTracerTest.OTHER);
		if (other == null) {
			return null;
		}
		return other.tValueByName(attribute);
	}

	private static TLStructuredTypePart part(TLObject self, String attribute) {
		return self.tType().getPartOrFail(attribute);
	}

}
