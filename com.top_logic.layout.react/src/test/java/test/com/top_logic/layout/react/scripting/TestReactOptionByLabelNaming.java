/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.scripting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.react.scripting.ReactOptionByLabelNaming;
import com.top_logic.layout.react.scripting.ReactOptionByLabelNaming.Name;
import com.top_logic.layout.react.scripting.ReactOptionScope;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;

/**
 * Tests the round-trip of {@link ReactOptionByLabelNaming}: a select option is named by its label
 * relative to its {@link ReactOptionScope option scope} and resolved back to the same object.
 *
 * <p>
 * This exercises the scheme directly — its {@code buildName}/{@code locateModel} contract — without
 * the {@code ModelResolver} service, since the scheme resolves purely from the value context and
 * ignores the {@code ActionContext}.
 * </p>
 */
public class TestReactOptionByLabelNaming extends TestCase {

	/**
	 * An option object distinct from its label, so resolution must designate the object identity, not
	 * a string equal to the label.
	 */
	private static final class Option {
		final String _label;

		Option(String label) {
			_label = label;
		}
	}

	private ReactOptionByLabelNaming _scheme;

	private Option _alpha;

	private Option _beta;

	private Option _duplicate1;

	private Option _duplicate2;

	private ReactOptionScope _scope;

	@Override
	protected void setUp() {
		_scheme = new ReactOptionByLabelNaming();
		_alpha = new Option("Alpha");
		_beta = new Option("Beta");
		_duplicate1 = new Option("Same");
		_duplicate2 = new Option("Same");

		Map<Object, String> labels = new HashMap<>();
		labels.put(_alpha, _alpha._label);
		labels.put(_beta, _beta._label);
		labels.put(_duplicate1, _duplicate1._label);
		labels.put(_duplicate2, _duplicate2._label);
		LabelProvider labelProvider = labels::get;

		_scope = new ReactOptionScope(List.of(_alpha, _beta, _duplicate1, _duplicate2), labelProvider);
	}

	/**
	 * A uniquely-labeled option names and resolves back to the very same object.
	 */
	public void testRoundTripUniqueLabel() {
		Maybe<Name> name = _scheme.buildName(_scope, _alpha);
		assertTrue("Unique label must produce a name.", name.hasValue());
		assertEquals("Alpha", name.get().getLabel());

		Object resolved = _scheme.locateModel(null, _scope, name.get());
		assertSame("Must resolve back to the same option object.", _alpha, resolved);

		assertSame(_beta, _scheme.locateModel(null, _scope, _scheme.buildName(_scope, _beta).get()));
	}

	/**
	 * An ambiguous label (shared by more than one option) cannot identify an option, so no name is
	 * built — identity falls back to a global scheme.
	 */
	public void testAmbiguousLabelDeclinesNaming() {
		assertFalse("Ambiguous label must not produce a name.",
			_scheme.buildName(_scope, _duplicate1).hasValue());
	}

	/**
	 * An option that is not part of the scope cannot be named.
	 */
	public void testForeignOptionDeclinesNaming() {
		assertFalse(_scheme.buildName(_scope, new Option("Gamma")).hasValue());
	}

	/**
	 * Resolving a label that is not (uniquely) present fails loudly rather than silently returning the
	 * wrong object.
	 */
	public void testLocateUnknownLabelFails() {
		Name name = _scheme.buildName(_scope, _alpha).get();
		name.setLabel("Nonexistent");
		try {
			_scheme.locateModel(null, _scope, name);
			fail("Expected a failure for a label with no unique option.");
		} catch (ApplicationAssertion expected) {
			// Expected: no unique option labeled "Nonexistent".
		}
	}
}
