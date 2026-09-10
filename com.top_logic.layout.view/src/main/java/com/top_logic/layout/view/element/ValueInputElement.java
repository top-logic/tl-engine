/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.AttributeOptions;
import com.top_logic.layout.view.form.ChannelFieldBinding;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that lets the user enter the value of a channel.
 *
 * <p>
 * The input is bound to the {@link Config#getValue() value channel} in both directions: what the
 * user enters becomes the value of the channel, and a value the channel receives from elsewhere
 * appears in the input. It is the counterpart of a {@code <field>} for a value that belongs to the
 * view rather than to a model object - the term a table filters by, the state a list is narrowed to
 * - and needs no form and no object to hold it.
 * </p>
 *
 * <p>
 * The {@link Config#getType() type} of the value decides the input: a text field for a text, a
 * number field for a number, a date picker for a point in time, a checkbox for a boolean, a
 * dropdown for an enumeration or a class. It is resolved by the same
 * {@link com.top_logic.layout.view.form.FieldControlService} that picks the control for an
 * attribute, so a value of a given type is entered the same way wherever it appears.
 * </p>
 *
 * <p>
 * A value of an enumeration or a class is chosen from options: the classifiers respectively the
 * instances of the type, or whatever the {@link Config#getOptions() options expression} computes.
 * With {@link Config#getMultiple() multiple} set, the channel holds a collection of such values
 * instead of a single one.
 * </p>
 */
@InApp
public class ValueInputElement implements UIElement {

	/**
	 * The type of a value whose {@link Config#getType() type} is not stated.
	 */
	public static final String DEFAULT_TYPE = "tl.core:String";

	/**
	 * Configuration for {@link ValueInputElement}.
	 */
	@TagName("value-input")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ValueInputElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getOptions()}. */
		String OPTIONS = "options";

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getMultiple()}. */
		String MULTIPLE = "multiple";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getReadonly()}. */
		String READONLY = "readonly";

		/** Configuration name for {@link #getLabelPosition()}. */
		String LABEL_POSITION = "label-position";

		/**
		 * The channel carrying the value the user enters.
		 */
		@Name(VALUE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getValue();

		/**
		 * The type of the value, deciding how it is entered.
		 *
		 * <p>
		 * A primitive type such as {@code tl.core:String}, {@code tl.core:Integer},
		 * {@code tl.core:Date} or {@code tl.core:Boolean} is entered, an enumeration or a class is
		 * chosen from options.
		 * </p>
		 */
		@Name(TYPE)
		@FormattedDefault(DEFAULT_TYPE)
		TLModelPartRef getType();

		/**
		 * The values to choose from.
		 *
		 * <p>
		 * Called with the values of the {@link #getInputs() input channels} as its arguments, so
		 * that the options can depend on what the rest of the view shows; without inputs, an
		 * expression computing the options directly.
		 * </p>
		 *
		 * <p>
		 * Without an expression, the options are those the type itself offers: the classifiers of an
		 * enumeration, the instances of a class. A value of a primitive type is then entered rather
		 * than chosen.
		 * </p>
		 */
		@Name(OPTIONS)
		@Nullable
		Expr getOptions();

		/**
		 * References to the {@link ViewChannel}s whose current values become the arguments of
		 * {@link #getOptions()}, in declaration order.
		 *
		 * <p>
		 * The options are computed again whenever one of these channels changes.
		 * </p>
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * Whether the channel holds a collection of values rather than a single one.
		 */
		@Name(MULTIPLE)
		boolean getMultiple();

		/**
		 * The label shown beside the input.
		 *
		 * <p>
		 * Without one, the input stands alone, which is what an input in a toolbar or above a list
		 * does, where the surrounding display says what it is for.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * Whether the value is displayed but cannot be changed here.
		 */
		@Name(READONLY)
		boolean getReadonly();

		/**
		 * Where the input renders its label relative to itself, e.g. {@code hide-label} for a
		 * label-less input.
		 *
		 * <p>
		 * If not set, the position falls back to the responsive default of the enclosing layout.
		 * </p>
		 */
		@Name(LABEL_POSITION)
		@Nullable
		LabelPosition getLabelPosition();
	}

	private final ChannelRef _valueRef;

	private final TLModelPartRef _typeRef;

	private final QueryExecutor _options;

	private final List<ChannelRef> _inputRefs;

	private final boolean _multiple;

	private final ResKey _label;

	private final boolean _readonly;

	private final LabelPosition _labelPosition;

	/**
	 * Creates a new {@link ValueInputElement} from configuration.
	 */
	@CalledByReflection
	public ValueInputElement(InstantiationContext context, Config config) {
		_valueRef = config.getValue();
		_typeRef = config.getType();
		_options = QueryExecutor.compileOptional(config.getOptions());
		_inputRefs = config.getInputs();
		_multiple = config.getMultiple();
		_label = config.getLabel();
		_readonly = config.getReadonly();
		_labelPosition = config.getLabelPosition();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		TLType type = _typeRef.resolveType();

		List<ViewChannel> optionInputs = new ArrayList<>(_inputRefs.size());
		for (ChannelRef ref : _inputRefs) {
			optionInputs.add(context.resolveChannel(ref));
		}
		List<?> options = options(type, optionInputs);

		AbstractFieldModel field = options == null
			? new AbstractFieldModel(null)
			: new SimpleSelectFieldModel(null, options, _multiple);
		field.setEditable(!_readonly);

		// Bound before the control is built, so that the control is built over the value the
		// channel already holds instead of over an empty field it has to be told about afterwards.
		ChannelFieldBinding binding = ChannelFieldBinding.bind(context.resolveChannel(_valueRef), field,
			options != null, _multiple);

		String label = _label == null ? null : Resources.getInstance().getString(_label);
		FieldSpec spec = FieldControlService.fieldSpec(type, type, label, _multiple, field);
		ReactControl input = FieldControlService.getInstance().createFieldControl(context, type, spec, field);
		input.addCleanupAction(binding::dispose);

		if (_options != null && !optionInputs.isEmpty()) {
			followOptions(type, optionInputs, (SelectFieldModel) field, input);
		}

		if (_label == null && _labelPosition == null) {
			return input;
		}
		return new ReactFormFieldChromeControl(context, label, field.isMandatory(), false, null, null,
			AttributeFieldControl.wirePosition(_labelPosition, !_readonly), false, true, input);
	}

	/**
	 * Recomputes the options whenever one of the option inputs changes, so that a selection offered
	 * for what the view currently shows stays in step with it.
	 */
	private void followOptions(TLType type, List<ViewChannel> optionInputs, SelectFieldModel field,
			ReactControl input) {
		ChannelListener listener = (sender, oldValue, newValue) -> field.setOptions(options(type, optionInputs));
		for (ViewChannel channel : optionInputs) {
			channel.addListener(listener);
		}
		input.addCleanupAction(() -> {
			for (ViewChannel channel : optionInputs) {
				channel.removeListener(listener);
			}
		});
	}

	/**
	 * The values to choose from, or {@code null} if the value is entered rather than chosen.
	 */
	private List<?> options(TLType type, List<ViewChannel> optionInputs) {
		if (_options == null) {
			return AttributeOptions.optionsFor(type);
		}
		Object[] args = new Object[optionInputs.size()];
		for (int n = 0, cnt = args.length; n < cnt; n++) {
			args[n] = optionInputs.get(n).get();
		}
		return SearchExpression.asList(_options.execute(args));
	}

}
