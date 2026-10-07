/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import java.util.List;
import java.util.Optional;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.search.expr.documentation.DocumentationConstants;
import com.top_logic.model.search.ui.CodeCompletion;
import com.top_logic.model.search.ui.TLScriptCompletionService;
import com.top_logic.model.search.ui.TLScriptDocumentation;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for the documentation of script functions and model elements delivered by
 * {@link TLScriptDocumentation} and attached to completions by {@link TLScriptCompletionService}.
 */
public class TestTLScriptDocumentation extends AbstractSearchExpressionTest {

	private static final String FUNCTION = "mathRandom";

	/**
	 * A completion of a model type carries documentation naming the type.
	 */
	public void testModelTypeCompletionHasDocumentation() {
		TLType type = objectType();
		String module = type.getModule().getName();

		CodeCompletion completion = completion(TLScriptCompletionService.computeCompletions(null,
			"`" + module + ":" + type.getName(), type.getName(), false), type.getName());

		assertDocumentsPart(completion.getDocHTML(), TLModelUtil.qualifiedName(type));
	}

	/**
	 * A completion of an attribute carries documentation naming the attribute.
	 */
	public void testAttributeCompletionHasDocumentation() {
		TLStructuredTypePart part = attribute();
		String qualifiedName = TLModelUtil.qualifiedName(part);

		CodeCompletion completion = completion(TLScriptCompletionService.computeCompletions(null,
			"`" + qualifiedName, part.getName(), false), part.getName());

		assertDocumentsPart(completion.getDocHTML(), qualifiedName);
	}

	/**
	 * A quoted model reference is documented like the completion of the element it references.
	 */
	public void testModelReferenceDocumentation() {
		TLType type = objectType();
		String qualifiedName = TLModelUtil.qualifiedName(type);

		Optional<String> doc = TLScriptDocumentation.documentation(null, "`" + qualifiedName + "`");

		assertDocumentsPart(doc.orElse(null), qualifiedName);
		assertEquals(TLScriptDocumentation.modelPartDocumentation(type), doc);
	}

	/**
	 * A qualified attribute name is resolved without quotes, too.
	 */
	public void testUnquotedAttributeReferenceDocumentation() {
		String qualifiedName = TLModelUtil.qualifiedName(attribute());

		assertDocumentsPart(TLScriptDocumentation.documentation(null, qualifiedName).orElse(null), qualifiedName);
	}

	/**
	 * A name that denotes no model element, also one that is incomplete while being typed, has no
	 * documentation.
	 */
	public void testUnknownModelReferenceHasNoDocumentation() {
		assertEquals(Optional.empty(), TLScriptDocumentation.documentation(null, "`no.such.module:NoType`"));
		assertEquals(Optional.empty(), TLScriptDocumentation.documentation(null, "`no.such.module`"));
		assertEquals(Optional.empty(), TLScriptDocumentation.documentation(null, "`tl.core:"));
		assertEquals(Optional.empty(), TLScriptDocumentation.documentation(null,
			"`" + TLModelUtil.qualifiedName(objectType()) + "#noSuchAttribute`"));
	}

	/**
	 * A variable reference has no documentation.
	 */
	public void testVariableHasNoDocumentation() {
		assertEquals(Optional.empty(), TLScriptDocumentation.documentation(displayContext(), "$" + FUNCTION));
		assertEquals(Optional.empty(), TLScriptDocumentation.documentation(displayContext(), "$x"));
	}

	/**
	 * Without a display context, neither a function completion nor a function token is documented.
	 */
	public void testFunctionWithoutContextHasNoDocumentation() {
		CodeCompletion completion =
			completion(TLScriptCompletionService.computeCompletions(null, FUNCTION, FUNCTION, false), FUNCTION);

		assertEquals("", completion.getDocHTML());
		assertEquals(Optional.empty(), TLScriptDocumentation.documentation(null, FUNCTION));
	}

	/**
	 * With a display context, a function completion and a function token are documented alike.
	 */
	public void testFunctionDocumentation() {
		DisplayContext context = displayContext();
		CodeCompletion completion =
			completion(TLScriptCompletionService.computeCompletions(context, FUNCTION, FUNCTION, false), FUNCTION);

		String doc = completion.getDocHTML();
		assertFalse("No documentation for function '" + FUNCTION + "'.", doc.isBlank());
		assertEquals(Optional.of(doc), TLScriptDocumentation.documentation(context, FUNCTION));
	}

	/**
	 * An unknown function has no documentation.
	 */
	public void testUnknownFunctionHasNoDocumentation() {
		assertEquals(Optional.empty(), TLScriptDocumentation.documentation(displayContext(), "noSuchFunction"));
	}

	private static void assertDocumentsPart(String doc, String qualifiedName) {
		assertNotNull("No documentation for '" + qualifiedName + "'.", doc);
		assertFalse("No documentation for '" + qualifiedName + "'.", doc.isEmpty());
		assertTrue(doc, doc.contains(DocumentationConstants.DOCUMENTATION_CSS_CLASS));
		assertTrue(doc, doc.contains(qualifiedName));
	}

	private static CodeCompletion completion(List<CodeCompletion> completions, String name) {
		for (CodeCompletion completion : completions) {
			if (name.equals(completion.getName())) {
				return completion;
			}
		}
		fail("No completion '" + name + "' in " + completions.stream().map(CodeCompletion::getName).toList());
		return null;
	}

	private static TLType objectType() {
		return TLModelUtil.findType(TlModelFactory.TL_MODEL_STRUCTURE, TLObject.TL_OBJECT_TYPE);
	}

	private static TLStructuredTypePart attribute() {
		TLStructuredType type = (TLStructuredType) TLModelUtil.findType(TlModelFactory.TL_MODEL_STRUCTURE, "TLClass");
		return type.getPart("abstract");
	}

	private static DisplayContext displayContext() {
		return DummyDisplayContext.newInstance();
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return suite(TestTLScriptDocumentation.class);
	}
}
