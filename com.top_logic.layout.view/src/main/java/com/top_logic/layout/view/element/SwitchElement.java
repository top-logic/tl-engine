/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
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
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactDeckPaneControl;
import com.top_logic.layout.react.control.layout.ReactDeckPaneControl.ChildFactory;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * UIElement that shows exactly one of several configured content views, chosen by evaluating a
 * TL-Script predicate per {@code <case>} against the value of an {@code input} channel.
 *
 * <p>
 * The cases are tested in configuration order; the first whose {@link CaseConfig#getTest() test}
 * returns {@code true} is shown. If none matches, the {@code <default>} content is shown (or nothing
 * if no default is configured). The switch re-evaluates whenever the input channel changes and only
 * swaps the visible view when the matching case actually changes - within a single case the content
 * (bound to its own channels) updates itself.
 * </p>
 *
 * <p>
 * Backed by {@link ReactDeckPaneControl}, so each case's content is created lazily on first
 * activation and cached afterwards (e.g. a form's edit mode survives switching away and back).
 * </p>
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

	private final List<CaseEntry> _cases;

	private final List<UIElement> _default;

	/**
	 * Creates a new {@link SwitchElement} from configuration.
	 */
	@CalledByReflection
	public SwitchElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_cases = new ArrayList<>();
		for (CaseConfig caseConfig : config.getCases()) {
			QueryExecutor test = QueryExecutor.compile(caseConfig.getTest());
			List<UIElement> content = caseConfig.getContent().stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
			_cases.add(new CaseEntry(test, content));
		}
		_default = config.getDefault().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel input = context.resolveChannel(_inputRef);

		List<ChildFactory> factories = new ArrayList<>();
		for (int i = 0; i < _cases.size(); i++) {
			CaseEntry entry = _cases.get(i);
			int index = i;
			factories.add(() -> buildContent(entry._content, context, index));
		}
		// Trailing fallback pane: the <default> content, or an empty pane when no default is
		// configured. Chosen whenever no case matches.
		int fallbackIndex = factories.size();
		factories.add(() -> buildContent(_default, context, fallbackIndex));

		ReactDeckPaneControl deck =
			new ReactDeckPaneControl(context, factories, selectIndex(input.get(), fallbackIndex));

		// Re-choose the visible case on input change. selectChild() is a no-op when the index is
		// unchanged, so selecting a different object of the same case does not rebuild; and it
		// detaches (does not dispose) the previous pane, so cached case content keeps its state -
		// no ChannelNotificationScope deferral needed here.
		ChannelListener listener =
			(sender, oldValue, newValue) -> deck.selectChild(selectIndex(newValue, fallbackIndex));
		input.addListener(listener);
		deck.addCleanupAction(() -> input.removeListener(listener));

		return deck;
	}

	private int selectIndex(Object value, int fallbackIndex) {
		for (int i = 0; i < _cases.size(); i++) {
			if (Boolean.TRUE.equals(_cases.get(i)._test.execute(value))) {
				return i;
			}
		}
		return fallbackIndex;
	}

	private static ReactControl buildContent(List<UIElement> elements, ViewContext context, int index) {
		ViewContext childContext = context.childContext("switch").withChildSlotPath(String.valueOf(index));
		if (elements.size() == 1) {
			return (ReactControl) elements.get(0).createControl(childContext);
		}
		List<ReactControl> children = elements.stream()
			.map(e -> (ReactControl) e.createControl(childContext))
			.collect(Collectors.toList());
		return new ReactStackControl(childContext, children);
	}

	private record CaseEntry(QueryExecutor _test, List<UIElement> _content) {
	}
}
