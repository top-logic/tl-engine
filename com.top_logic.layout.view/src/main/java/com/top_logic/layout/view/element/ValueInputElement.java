/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
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
 * - and needs no form and no object to hold it. Standing outside a form, it is placed in a
 * {@link FieldsElement} for the grid a form gives its fields.
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
 *
 * <p>
 * A {@link Config#getOnSubmit() submit command} turns the input into an action: the value the user
 * finishes entering is written to the channel and then run through the command, which is what a
 * search field or a jump-to box is - one input and one command over what was entered.
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
	public interface Config extends UIElement.Config, Inputs {

		@Override
		@ClassDefault(ValueInputElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getOptions()}. */
		String OPTIONS = "options";

		/** Configuration name for {@link #getMultiple()}. */
		String MULTIPLE = "multiple";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getPlaceholder()}. */
		String PLACEHOLDER = "placeholder";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/** Configuration name for {@link #getClearable()}. */
		String CLEARABLE = "clearable";

		/** Configuration name for {@link #getDebounce()}. */
		String DEBOUNCE = "debounce";

		/** Configuration name for {@link #getReadonly()}. */
		String READONLY = "readonly";

		/** Configuration name for {@link #getLabelPosition()}. */
		String LABEL_POSITION = "label-position";

		/** Configuration name for {@link #getOnSubmit()}. */
		String ON_SUBMIT = "on-submit";

		/** Configuration name for {@link #getInputControl()}. */
		String INPUT_CONTROL = "input-control";

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
		 *
		 * <p>
		 * The options are computed again whenever one of the input channels changes.
		 * </p>
		 */
		@Name(OPTIONS)
		@Nullable
		Expr getOptions();

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
		 * The text shown in the input while it is empty.
		 *
		 * <p>
		 * What the user is expected to enter, said inside the input itself: "Search" in a search
		 * box, "name@example.com" in a mail address. It states the purpose of an input that stands
		 * without a visible label - in a toolbar, above a list - and it disappears as soon as a
		 * value is entered.
		 * </p>
		 */
		@Name(PLACEHOLDER)
		@Nullable
		ResKey getPlaceholder();

		/**
		 * The icon shown inside the input, ahead of what is typed.
		 *
		 * <p>
		 * What kind of input this is, said as a picture: the magnifier of a search box, the
		 * envelope of a mail address. The icon is decoration - nothing happens when it is clicked,
		 * and a screen reader passes over it - so the input is still named by its label or its
		 * placeholder.
		 * </p>
		 *
		 * <p>
		 * An icon font class such as {@code css:fa-solid fa-magnifying-glass} or a path to an
		 * image. Shown by an input over a text; an input over a number, a date, a truth value or a
		 * selection ignores it.
		 * </p>
		 */
		@Name(ICON)
		@Nullable
		String getIcon();

		/**
		 * Whether the input offers a button that empties it.
		 *
		 * <p>
		 * For a value that is taken back as often as it is given - the term a table is searched by,
		 * the text a list is narrowed to - where emptying the input is a step of its own rather
		 * than the accident of deleting every character. The button is shown only while the input
		 * holds something and while it can be changed, and it writes the empty value at once rather
		 * than after the delay.
		 * </p>
		 *
		 * <p>
		 * Offered by an input over a text; an input over a number, a date, a truth value or a
		 * selection ignores it.
		 * </p>
		 */
		@Name(CLEARABLE)
		boolean getClearable();

		/**
		 * How long the input waits after the last keystroke before the typed value reaches the
		 * channel, written as a duration ({@code 300ms}, {@code 1s}).
		 *
		 * <p>
		 * A shorter wait makes whatever is computed from the value - the rows a search narrows to -
		 * follow the typing more closely, at the price of one round-trip per pause; a longer one
		 * waits for the user to stop. Empty for the wait a typed input uses by default.
		 * </p>
		 *
		 * <p>
		 * Only a value that is typed waits at all; a value that is picked - from a dropdown, a date
		 * picker, a checkbox - reaches the channel with the choice. An input whose value the server
		 * rewrites as it is stored, a number for instance, holds the value back until the input is
		 * left and ignores the wait altogether.
		 * </p>
		 */
		@Name(DEBOUNCE)
		@Nullable
		@Format(MillisFormat.class)
		Long getDebounce();

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

		/**
		 * The command run on the value the user submits.
		 *
		 * <p>
		 * A value the user finishes entering is written to the value channel and then handed to
		 * this command as its input. In a field the user types in - a text, a number - the value is
		 * finished by pressing Enter; in a field that is picked from - a dropdown, a date, a
		 * checkbox - every choice finishes it.
		 * </p>
		 *
		 * <p>
		 * A {@link GenericViewCommand} unless another command is named, so the actions to run on
		 * the value stand directly inside this element.
		 * </p>
		 */
		@Name(ON_SUBMIT)
		@Nullable
		@ImplementationClassDefault(GenericViewCommand.class)
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends ViewCommand> getOnSubmit();

		/**
		 * The control the value is entered in, overriding the one its type implies.
		 *
		 * <p>
		 * The same choice a {@code <field>} makes for the attribute it displays, made for a value
		 * that belongs to the view instead: a selection offered as a cloud of toggles rather than
		 * as a list that opens on demand, a number dragged along a track rather than typed, a truth
		 * value flipped on a switch rather than ticked in a box.
		 * </p>
		 *
		 * <p>
		 * Left unset, the control is the one the {@link #getType() type} of the value leads to.
		 * </p>
		 */
		@Name(INPUT_CONTROL)
		@Nullable
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends ReactFieldControlProvider> getInputControl();
	}

	private final ChannelRef _valueRef;

	private final TLModelPartRef _typeRef;

	private final QueryExecutor _options;

	private final List<ChannelRef> _inputRefs;

	private final boolean _multiple;

	private final ResKey _label;

	private final ResKey _placeholder;

	private final String _icon;

	private final boolean _clearable;

	private final Long _debounce;

	private final boolean _readonly;

	private final LabelPosition _labelPosition;

	private final ViewCommand _submitCommand;

	private final ViewCommand.Config _submitCommandConfig;

	private final PolymorphicConfiguration<? extends ReactFieldControlProvider> _inputControl;

	private final String _cssClass;

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
		_placeholder = config.getPlaceholder();
		_icon = config.getIcon();
		_clearable = config.getClearable();
		_debounce = config.getDebounce();
		_readonly = config.getReadonly();
		_labelPosition = config.getLabelPosition();

		PolymorphicConfiguration<? extends ViewCommand> submitConfig = config.getOnSubmit();
		_submitCommandConfig = submitConfig instanceof ViewCommand.Config commandConfig ? commandConfig : null;
		_submitCommand = _submitCommandConfig == null ? null : context.getInstance(submitConfig);
		_inputControl = config.getInputControl();
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		TLType type = _typeRef.resolveType();

		List<ViewChannel> optionInputs = ChannelInputs.resolve(context, _inputRefs);
		List<?> options = options(type, optionInputs);

		AbstractFieldModel field = options == null
			? new AbstractFieldModel(null)
			: new SimpleSelectFieldModel(null, options, _multiple);
		field.setEditable(!_readonly);

		// Bound before the control is built, so that the control is built over the value the
		// channel already holds instead of over an empty field it has to be told about afterwards.
		ChannelFieldBinding binding = ChannelFieldBinding.bind(context.resolveChannel(_valueRef), field,
			options != null, _multiple);

		Resources resources = Resources.getInstance();
		String label = _label == null ? null : resources.getString(_label);
		FieldSpec spec = FieldControlService.fieldSpec(type, type, label, _multiple, field);
		if (_placeholder != null) {
			spec.setPlaceholder(resources.getString(_placeholder));
		}
		spec.setIcon(_icon).setClearable(_clearable).setDebounce(_debounce);
		ReactControl input =
			FieldControlService.getInstance().createFieldControl(context, type, spec, field, _inputControl);
		input.addCleanupAction(binding::dispose);

		if (_options != null && !optionInputs.isEmpty()) {
			followOptions(type, optionInputs, (SelectFieldModel) field, input);
		}

		if (_submitCommand != null) {
			followSubmit(context, input, binding);
		}

		if (_label == null && _labelPosition == null) {
			input.setCssClass(_cssClass);
			return input;
		}
		ReactFormFieldChromeControl chrome =
			new ReactFormFieldChromeControl(context, label, field.isMandatory(), false, null, null,
				AttributeFieldControl.wirePosition(_labelPosition, !_readonly), false, true, input);
		chrome.setCssClass(_cssClass);
		return chrome;
	}

	/**
	 * Runs the {@link Config#getOnSubmit() submit command} on each value the user finishes
	 * entering.
	 *
	 * <p>
	 * Where the control has a submit gesture of its own, the submit is that gesture; otherwise
	 * every value the user produces is a finished one, and the binding reports it once it has
	 * reached the channel. The command's executability rule decides per value, so the model needs
	 * no attachment to a channel.
	 * </p>
	 */
	private void followSubmit(ViewContext context, ReactControl input, ChannelFieldBinding binding) {
		ViewCommandModel model = ViewCommandModel.forCommand(context, _submitCommand, _submitCommandConfig);
		if (input instanceof ReactFormFieldControl fieldControl && fieldControl.hasSubmitGesture()) {
			fieldControl.setSubmitListener(value -> model.execute(context, value));
		} else {
			binding.setCommitListener(value -> model.execute(context, value));
		}
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
