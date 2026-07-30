/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;

/**
 * UIElement that shows exactly one of several configured content views, chosen by evaluating a
 * TL-Script predicate per {@code <case>} against the value of an {@code input} channel.
 *
 * <p>
 * The cases are tested in configuration order; the first whose {@link CaseConfig#getTest() test}
 * returns {@code true} is shown. If none matches, the {@code <default>} content is shown (or nothing
 * if no default is configured). The switch re-evaluates whenever the input channel changes, and also
 * when the input object itself is changed (a test typically decides by an attribute of that object,
 * which can be edited without the channel value changing) - see
 * {@link Config#getObservedTypes()} for tests reaching beyond the input object. It only swaps the
 * visible view when the matching case actually changes - within a single case the content (bound to
 * its own channels) updates itself.
 * </p>
 *
 * <p>
 * When the matching case changes, the previously shown content is disposed rather than cached, so a
 * hidden case leaves no contributions (e.g. a form's edit/save/cancel commands) behind in the
 * enclosing scope.
 * </p>
 *
 * @see ReactSwitchControl
 */
public class SwitchElement implements UIElement {

	/**
	 * Configuration for {@link SwitchElement}.
	 */
	@TagName("switch")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getCases()}. */
		String CASES = "cases";

		/** Configuration name for {@link #getDefault()}. */
		String DEFAULT = "default";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		@Override
		@ClassDefault(SwitchElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The channel whose current value is passed to every {@code <case>} test predicate to choose
		 * the visible content.
		 */
		@Name(INPUT)
		@Format(ChannelRefFormat.class)
		@Mandatory
		ChannelRef getInput();

		/**
		 * The cases, evaluated in order; the first matching case is shown.
		 *
		 * @implNote Written directly as {@code <case>} children of the {@code <switch>}.
		 */
		@Name(CASES)
		@DefaultContainer
		@TreeProperty
		List<CaseConfig> getCases();

		/**
		 * Content shown when no {@code <case>} matches; empty for no fallback content.
		 */
		@Name(DEFAULT)
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getDefault();

		/**
		 * Types whose object changes (create / update / delete) trigger a re-evaluation of the
		 * {@link #getCases() cases}, in addition to the {@link #getInput() input} object, which is
		 * always observed.
		 *
		 * <p>
		 * Configure this only for a test that navigates beyond the input object, e.g. one deciding by
		 * an attribute of the input's container: a change of that other object is invisible to the
		 * input's own observation. Empty (default) observes just the input object.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();
	}

	/**
	 * Configuration for a single {@code <case>} of a {@link SwitchElement}.
	 */
	@TagName("case")
	public interface CaseConfig extends ConfigurationItem {

		/** Configuration name for {@link #getTest()}. */
		String TEST = "test";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/**
		 * TL-Script predicate called with the current value of the switch's
		 * {@link Config#getInput() input} channel; this case is chosen when it returns {@code true}.
		 */
		@Name(TEST)
		@Mandatory
		@NonNullable
		Expr getTest();

		/**
		 * The content elements shown when this case is chosen.
		 */
		@Name(CONTENT)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getContent();
	}

	private final ChannelRef _inputRef;

	private final List<SwitchCase> _cases;

	private final List<UIElement> _default;

	private final List<TLModelPartRef> _observedTypeRefs;

	/**
	 * Creates a new {@link SwitchElement} from configuration.
	 */
	@CalledByReflection
	public SwitchElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_observedTypeRefs = config.getObservedTypes();
		_cases = new ArrayList<>();
		for (CaseConfig caseConfig : config.getCases()) {
			QueryExecutor test = QueryExecutor.compile(caseConfig.getTest());
			List<UIElement> content = caseConfig.getContent().stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
			_cases.add(new SwitchCase(test, content));
		}
		_default = config.getDefault().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel input = context.resolveChannel(_inputRef);
		return new ReactSwitchControl(context, input, _cases, _default, resolveObservedTypes());
	}

	private Set<TLStructuredType> resolveObservedTypes() {
		if (_observedTypeRefs.isEmpty()) {
			return Set.of();
		}
		Set<TLStructuredType> types = new HashSet<>();
		for (TLModelPartRef ref : _observedTypeRefs) {
			TLStructuredType type = (TLStructuredType) ref.resolveType();
			if (type == null) {
				throw new RuntimeException("Failed to resolve observed type: " + ref.qualifiedName());
			}
			types.add(type);
		}
		return types;
	}

	/**
	 * A compiled case: its predicate and the content shown when it matches.
	 */
	record SwitchCase(QueryExecutor _test, List<UIElement> _content) {
	}
}
