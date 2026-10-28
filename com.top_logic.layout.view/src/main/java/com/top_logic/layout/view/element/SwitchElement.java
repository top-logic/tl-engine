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
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.GlobalKey;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputRefFormat;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.SingleInput;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
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
 * its own channels) updates itself, unless the switch is configured to {@link Config#getRebuild()
 * rebuild} it for every new value.
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
	@TypeIcon("css:bi bi-signpost")
	public interface Config extends UIElement.Config, SingleInput {

		/** Configuration name for {@link #getCases()}. */
		String CASES = "cases";

		/** Configuration name for {@link #getDefault()}. */
		String DEFAULT = "default";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getRebuild()}. */
		String REBUILD = "rebuild";

		@Override
		@ClassDefault(SwitchElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The channel whose current value every {@code <case>} test predicate decides on to choose
		 * the visible content.
		 *
		 * <p>
		 * The predicates see the value as a variable named after the channel, or by the name given
		 * with {@code as}: {@code input="selection as ticket"}.
		 * </p>
		 */
		@Override
		@Name(INPUT)
		@Format(InputRefFormat.class)
		@Mandatory
		InputRef getInput();

		/**
		 * The cases, evaluated in order; the first matching case is shown.
		 *
		 * @implNote Written directly as {@code <case>} children of the {@code <switch>}. Keyed by
		 *           {@link CaseConfig#ID} so that a same-path overlay can address a case.
		 */
		@Name(CASES)
		@Key(CaseConfig.ID)
		@DefaultContainer
		@TreeProperty
		List<CaseConfig> getCases();

		/**
		 * Content shown when no {@code <case>} matches; empty for no fallback content.
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address
		 *           an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(DEFAULT)
		@TreeProperty
		List<UIElement.Config> getDefault();

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

		/**
		 * Whether the shown content is built anew whenever the {@link #getInput() input} channel
		 * delivers another value, also when the matching case stays the same.
		 *
		 * <p>
		 * By default, the content of a case updates itself from the channels it is bound to. Some
		 * content is shaped by a value when it is built and keeps that shape: the columns a table
		 * computes from its inputs, for instance. A switch over that value with this option
		 * rebuilds such content for every new value; a switch without any case shows its
		 * {@link #getDefault() default} content that way. A change of the input object itself
		 * does not rebuild the content.
		 * </p>
		 */
		@Name(REBUILD)
		boolean getRebuild();
	}

	/**
	 * Configuration for a single {@code <case>} of a {@link SwitchElement}.
	 */
	@TagName("case")
	public interface CaseConfig extends ConfigPart {

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getTest()}. */
		String TEST = "test";

		/**
		 * The name an overlay of the view addresses this case by, unique in the whole view, see
		 * {@link UIElement.Config#getId()}.
		 *
		 * <p>
		 * A case without id is anonymous: an overlay adds cases beside it, but cannot address the
		 * case itself.
		 * </p>
		 */
		@Name(ID)
		@GlobalKey
		String getId();

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/**
		 * TL-Script predicate over the current value of the switch's {@link Config#getInput() input}
		 * channel; this case is chosen when it returns {@code true}.
		 *
		 * <p>
		 * The predicate sees the value as a variable named after the channel, or by its {@code as}
		 * name, e.g. {@code input="selectedDemo as demo"} with {@code test="$demo == 'table'"}.
		 * </p>
		 */
		@Name(TEST)
		@Mandatory
		@NonNullable
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getTest();

		/**
		 * The content elements shown when this case is chosen.
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address
		 *           an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(CONTENT)
		@DefaultContainer
		@TreeProperty
		List<UIElement.Config> getContent();
	}

	private final InputRef _input;

	private final List<SwitchCase> _cases;

	private final List<UIElement> _default;

	private final List<TLModelPartRef> _observedTypeRefs;

	private final String _cssClass;

	private final boolean _rebuild;

	/**
	 * Creates a new {@link SwitchElement} from configuration.
	 */
	@CalledByReflection
	public SwitchElement(InstantiationContext context, Config config) {
		_input = config.getInput();
		_observedTypeRefs = config.getObservedTypes();
		_cases = new ArrayList<>();
		for (CaseConfig caseConfig : config.getCases()) {
			QueryExecutor test = InputScript.compile(caseConfig, CaseConfig.TEST);
			List<UIElement> content = caseConfig.getContent().stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
			_cases.add(new SwitchCase(test, content));
		}
		_default = config.getDefault().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
		_cssClass = config.getCssClass();
		_rebuild = config.getRebuild();
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		List<ChildGroup> result = new ArrayList<>();
		for (SwitchCase switchCase : _cases) {
			result.add(ChildGroup.elements(switchCase._content()));
		}
		result.add(ChildGroup.elements(_default));
		return result;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel input = context.resolveChannel(_input.getChannel());
		ReactSwitchControl result =
			new ReactSwitchControl(context, input, _cases, _default, ObservedTypes.resolve(_observedTypeRefs),
				_rebuild);
		result.setCssClass(_cssClass);
		return result;
	}

	/**
	 * A compiled case: its predicate and the content shown when it matches.
	 */
	record SwitchCase(QueryExecutor _test, List<UIElement> _content) {
	}
}
