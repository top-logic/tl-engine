/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;

/**
 * A {@link UIElement} that embeds a view whose configuration is a value: the result of a TL-Script
 * expression over the {@link Config#getInputs() input channels}, typically a view stored in an
 * attribute of a model object (datatype {@code tl.view:ViewDefinition}).
 *
 * <p>
 * Like {@link ReferenceElement &lt;view-ref&gt;}, the embedded view is an isolated component with a
 * channel namespace of its own; {@code <bind>} children share channels of the enclosing view with
 * it. The expression is evaluated again whenever an input channel gets a new value, and when one of
 * the input objects itself is changed - the stored view is an attribute of that object, which can be
 * edited without the channel value changing. A changed view configuration replaces the embedded
 * view. While the expression yields {@code null}, nothing is shown. A configuration that cannot be
 * instantiated is shown as the {@link ViewLoadError} placeholder.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;dynamic-view inputs="task" view="$task.get(`my.app:Task#view`)"&gt;
 *   &lt;bind channel="item" to="task"/&gt;
 * &lt;/dynamic-view&gt;
 * </pre>
 *
 * @see DynamicViewControl
 */
@InApp
public class DynamicViewElement implements UIElement {

	/**
	 * Configuration for {@link DynamicViewElement}.
	 */
	@TagName(Config.TAG_NAME)
	@TypeIcon("css:bi bi-box-arrow-in-down-left")
	public interface Config extends UIElement.Config, Inputs, ViewBindings {

		/** The name of the element in a view file. */
		String TAG_NAME = "dynamic-view";

		/** Configuration name for {@link #getView()}. */
		String VIEW = "view";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		@Override
		@ClassDefault(DynamicViewElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * TL-Script expression computing the configuration of the embedded view.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names. It yields a view configuration, the value of
		 * an attribute of the datatype {@code tl.view:ViewDefinition}, or {@code null} to show
		 * nothing.
		 * </p>
		 */
		@Name(VIEW)
		@Mandatory
		@NonNullable
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getView();

		/**
		 * Types whose object changes (create / update / delete) trigger a new evaluation of the
		 * {@link #getView() view} expression, in addition to the objects of the {@link #getInputs()
		 * input channels}, which are always observed.
		 *
		 * <p>
		 * Configure this only for an expression that navigates beyond the input objects, e.g. one
		 * reading the view from the type of an input object: a change of that other object is
		 * invisible to the observation of the inputs. Empty (default) observes just the input
		 * objects.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();
	}

	private final Config _config;

	private final QueryExecutor _view;

	/**
	 * Creates a new {@link DynamicViewElement} from configuration.
	 */
	@CalledByReflection
	public DynamicViewElement(InstantiationContext context, Config config) {
		_config = config;
		_view = InputScript.compile(config, Config.VIEW);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputs = ChannelInputs.resolve(context, _config.getInputs());
		DynamicViewControl result = new DynamicViewControl(context, inputs, _view, _config.getBindings(),
			ObservedTypes.resolve(_config.getObservedTypes()), source());
		result.setCssClass(_config.getCssClass());
		return result;
	}

	/**
	 * The name of this element in a message about an embedded view that cannot be shown.
	 */
	private String source() {
		String id = _config.getId();
		String element = "<" + Config.TAG_NAME + (id == null ? "" : " id=\"" + id + "\"") + ">";
		return element + " (" + _config.location() + ")";
	}
}
