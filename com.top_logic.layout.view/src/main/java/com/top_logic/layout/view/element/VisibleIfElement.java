/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
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
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.SwitchElement.SwitchCase;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;

/**
 * UIElement that shows its content while a TL-Script condition over the value of an {@code input}
 * channel holds, and nothing while it does not.
 *
 * <p>
 * The condition is re-evaluated whenever the input channel changes, and also when the input object
 * itself is changed - a condition typically decides by an attribute of that object, which can be
 * edited without the channel value changing. A condition reaching beyond the input object states
 * the types it navigates to in {@link Config#getObservedTypes()}.
 * </p>
 *
 * <p>
 * Content that is hidden is disposed rather than kept alive, so it leaves no contributions (e.g. a
 * form's edit/save/cancel commands) behind in the enclosing scope. While the condition keeps
 * holding, the content stays as it is - bound to its own channels, it updates itself.
 * </p>
 *
 * <p>
 * This is the short form of a {@link SwitchElement} with a single case and no default. A
 * {@link SwitchElement} is what several alternatives are written with, where one of them is shown
 * at a time.
 * </p>
 *
 * <p>
 * The condition of an element is not to be confused with the condition of a command: whether a
 * command can be executed on its input is decided by the executability rule
 * {@link com.top_logic.layout.view.command.VisibleIf}, which carries the same tag name inside a
 * command's {@code <executability>}.
 * </p>
 *
 * @implNote {@link #createControl(ViewContext)} builds the {@link ReactSwitchControl} of a
 *           {@link SwitchElement} with a single {@link SwitchCase} and empty default content, so
 *           that the re-evaluation, the input object observation and the disposal semantics are
 *           literally those of a {@code <switch>}.
 */
@InApp
public class VisibleIfElement implements UIElement {

	/**
	 * Configuration for {@link VisibleIfElement}.
	 */
	@TagName("visible-if")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		@Override
		@ClassDefault(VisibleIfElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The channel whose current value is passed to the condition.
		 */
		@Name(INPUT)
		@Format(ChannelRefFormat.class)
		@Mandatory
		ChannelRef getInput();

		/**
		 * TL-Script predicate called with the current value of the {@link #getInput() input}
		 * channel; the content is shown while it returns {@code true}.
		 */
		@Name(EXPR)
		@Mandatory
		@NonNullable
		Expr getExpr();

		/**
		 * The content elements shown while the condition holds.
		 *
		 * @implNote Written directly as children of the {@code <visible-if>}.
		 */
		@Name(CONTENT)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getContent();

		/**
		 * Types whose object changes (create / update / delete) trigger a re-evaluation of the
		 * condition, in addition to the {@link #getInput() input} object, which is always observed.
		 *
		 * <p>
		 * Configure this only for a condition that navigates beyond the input object, e.g. one
		 * deciding by an attribute of the input's container: a change of that other object is
		 * invisible to the input's own observation. Empty (default) observes just the input object.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();
	}

	private final ChannelRef _inputRef;

	private final SwitchCase _case;

	private final List<TLModelPartRef> _observedTypeRefs;

	/**
	 * Creates a new {@link VisibleIfElement} from configuration.
	 */
	@CalledByReflection
	public VisibleIfElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_observedTypeRefs = config.getObservedTypes();

		QueryExecutor test = QueryExecutor.compile(config.getExpr());
		List<UIElement> content = config.getContent().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
		_case = new SwitchCase(test, content);
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(_case._content()));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel input = context.resolveChannel(_inputRef);
		return new ReactSwitchControl(context, input, List.of(_case), List.of(),
			ObservedTypes.resolve(_observedTypeRefs));
	}
}
