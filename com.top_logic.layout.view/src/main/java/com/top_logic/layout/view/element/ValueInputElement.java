/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Function;

import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.view.command.ActionScopeDefinition;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.constraint.MandatoryConstraintCheck;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.select.OptionOrder;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.InputRef;
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
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.FormModelListener;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.annotate.TLValueNormalizer;
import com.top_logic.model.annotate.util.ValueNormalizer;
import com.top_logic.model.config.AttributeConfigBase;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.HandlerResult;
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
 * The value is declared in the words of an attribute of a class in the model: its
 * {@link Config#getType() type}, whether it is {@link Config#getMandatory() mandatory}, whether it
 * holds {@link Config#getMultiple() multiple} values, and the {@link Config#getAnnotations()
 * annotations} that shape its display - a {@code <classification-display>}, a
 * {@code <multi-line>}, a {@code <format>}, an {@code <input-control>} - which act on the input as
 * they act on a field of such an attribute. An annotation the input does not state is taken from
 * the type.
 * </p>
 *
 * <p>
 * A {@link TLValueNormalizer} among the annotations brings a value the user enters into its
 * canonical form before it reaches the channel. Its function sees no object, since the input is not
 * bound to one.
 * </p>
 *
 * <p>
 * A {@link Config#getOnSubmit() submit command} turns the input into an action over the value the
 * user enters: a search field or a jump-to box runs it on Enter, an input whose value is applied at
 * once runs it on every edit the user finishes, as {@link Config#getSubmitOn() stated}. A value
 * that is picked from options runs it with every choice. Whenever the command runs, the value has
 * already reached the channel.
 * </p>
 *
 * <p>
 * Inside a {@code <form>}, the input follows the form's edit mode: the value can be changed only
 * while the form is edited. An {@link Config#getEditMode() edit mode channel} decides instead of
 * the form, and {@link Config#getReadonly() readonly} keeps the input unchangeable in any case.
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
	public interface Config extends UIElement.Config, Inputs, AttributeConfigBase, ActionScopeDefinition {

	/** Access to the default methods of this interface for the configuration proxy. */
	Lookup LOOKUP = MethodHandles.lookup();

		/** Configuration name for {@link #getOnSubmitAs()}. */
		String SUBMIT_AS = "submit-as";

		/** Name of the value the submitted value when no other name is given. */
		String DEFAULT_SUBMIT_AS = "value";


		@Override
		@ClassDefault(ValueInputElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getOptions()}. */
		String OPTIONS = "options";

		/** Configuration name for {@link #getOptionOrder()}. */
		String OPTION_ORDER = "option-order";

		/** Configuration name for {@link #getMultiple()}. */
		String MULTIPLE = "multiple";

		/** Configuration name for {@link #getMandatory()}. */
		String MANDATORY = TLStructuredTypePart.MANDATORY_ATTR;

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

		/** Configuration name for {@link #getEditMode()}. */
		String EDIT_MODE = "edit-mode";

		/** Configuration name for {@link #getLabelPosition()}. */
		String LABEL_POSITION = "label-position";

		/** Configuration name for {@link #getOnSubmit()}. */
		String ON_SUBMIT = "on-submit";

		/** Configuration name for {@link #getSubmitOn()}. */
		String SUBMIT_ON = "submit-on";

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
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names, so that the options can depend on what the
		 * rest of the view shows.
		 * </p>
		 *
		 * <p>
		 * Without an expression, the options are those the type itself offers: the classifiers of an
		 * enumeration, the instances of a class. A value of a primitive type is then entered rather
		 * than chosen.
		 * </p>
		 *
		 * <p>
		 * The options of an expression are offered in the order it computes them, so that the
		 * expression decides where each option stands; the {@link #getOptionOrder() option order}
		 * sorts them by their labels instead.
		 * </p>
		 *
		 * <p>
		 * The options are computed again whenever one of the input channels changes.
		 * </p>
		 */
		@Name(OPTIONS)
		@Nullable
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getOptions();

		/**
		 * The order the options are offered in, or nothing to offer them in the order their source
		 * gives them.
		 *
		 * <p>
		 * Where nothing is stated, the options of an {@link #getOptions() options expression} are
		 * offered in the order it computes them, the options of the type in the order of its
		 * classifiers for an enumeration and sorted by their labels for the instances of a class.
		 * Sorting by label suits options computed without an order of their own, which the
		 * expression would otherwise have to sort itself.
		 * </p>
		 *
		 * <p>
		 * An order stated at the {@link #getInputControl() input control} wins over this one.
		 * </p>
		 */
		@Name(OPTION_ORDER)
		@Nullable
		@NullDefault
		OptionOrder getOptionOrder();

		/**
		 * Whether the channel holds a collection of values rather than a single one.
		 */
		@Name(MULTIPLE)
		boolean getMultiple();

		/**
		 * Whether the value may not be empty.
		 *
		 * <p>
		 * The same statement the mandatory flag makes for an attribute: a selection offers no
		 * empty choice, and an input the user types in reports the missing value once it is
		 * emptied. The empty value still reaches the {@link #getValue() value channel}, but the
		 * {@link #getOnSubmit() submit command} does not run on it.
		 * </p>
		 */
		@Name(MANDATORY)
		boolean getMandatory();

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
		 * The channel deciding whether the value can be changed: it can while the channel holds
		 * {@code true}, and cannot otherwise.
		 *
		 * <p>
		 * Without a channel, an input inside a {@code <form>} can be changed while the form is in
		 * edit mode, and an input outside a form can always be changed. With a channel, the
		 * form's mode is ignored: an input whose value belongs to the view rather than to the
		 * edited object - a filter above a list shown in the form, a choice made while viewing -
		 * names a channel that always holds {@code true}, or the channel of its own edit state.
		 * </p>
		 *
		 * <p>
		 * {@link #getReadonly() Readonly} wins over this channel: a read-only input is never
		 * changed.
		 * </p>
		 */
		@Name(EDIT_MODE)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getEditMode();

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
		 * The command receives the value of the input as its input, after the value has reached the
		 * value channel. In a field the user types in - a text, a number - the
		 * {@link #getSubmitOn() submit gesture} decides when it runs: on Enter, or on every edit the
		 * user finishes. In a field that is picked from - a dropdown, a date, a checkbox - every
		 * choice runs it.
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
		@TreeProperty
		PolymorphicConfiguration<? extends ViewCommand> getOnSubmit();

		/**
		 * The name of the argument holding the submitted value, the main argument of the
		 * {@link #getOnSubmit() command}.
		 *
		 * <p>
		 * The command sees it in addition to its own inputs; its scripts refer to it by this name.
		 * </p>
		 */
		@Name(SUBMIT_AS)
		@StringDefault(DEFAULT_SUBMIT_AS)
		String getOnSubmitAs();

		/**
		 * The {@link #getOnSubmit() command} runs with the submitted value as argument named by
		 * {@link #getOnSubmitAs()}.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			if (part != null && part == getOnSubmit()) {
				return ActionScope.single(getOnSubmitAs());
			}
			return outer;
		}

		/**
		 * When the {@link #getOnSubmit() submit command} runs on a value the user types - a text, a
		 * number.
		 *
		 * <p>
		 * With {@link SubmitOn#ENTER}, the command runs when the user presses Enter, which is what a
		 * search field or a jump-to box does. With {@link SubmitOn#CHANGE}, it runs on each edit the
		 * user finishes, by pressing Enter or by leaving the input after changing its value - once
		 * per edit, which is what an input does whose value is applied at once.
		 * </p>
		 *
		 * <p>
		 * The entered value reaches the {@link #getValue() value channel} and the server in every
		 * case, when the input is left as well as on Enter, whatever is stated here. This property
		 * only decides whether the command runs at that point.
		 * </p>
		 *
		 * <p>
		 * A value that is picked - from a dropdown, a date picker, a checkbox - runs the command
		 * with every choice; this property has no effect there.
		 * </p>
		 */
		@Name(SUBMIT_ON)
		SubmitOn getSubmitOn();

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
		@TreeProperty
		PolymorphicConfiguration<? extends ReactFieldControlProvider> getInputControl();
	}

	private final ChannelRef _valueRef;

	private final TLModelPartRef _typeRef;

	private final QueryExecutor _options;

	private final List<InputRef> _inputRefs;

	private final OptionOrder _optionOrder;

	private final boolean _multiple;

	private final boolean _mandatory;

	private final AnnotationLookup _annotations;

	private final ValueNormalizer _normalizer;

	private final ResKey _label;

	private final ResKey _placeholder;

	private final String _icon;

	private final boolean _clearable;

	private final Long _debounce;

	private final boolean _readonly;

	private final ChannelRef _editModeRef;

	private final LabelPosition _labelPosition;

	private final ViewCommand _submitCommand;

	private final String _submitAs;

	private final ViewCommand.Config _submitCommandConfig;

	private final SubmitOn _submitOn;

	private final PolymorphicConfiguration<? extends ReactFieldControlProvider> _inputControl;

	private final String _cssClass;

	/**
	 * Creates a new {@link ValueInputElement} from configuration.
	 */
	@CalledByReflection
	public ValueInputElement(InstantiationContext context, Config config) {
		_valueRef = config.getValue();
		_typeRef = config.getType();
		_options = InputScript.compile(config, Config.OPTIONS);
		_inputRefs = config.getInputs();
		_optionOrder = config.getOptionOrder();
		_multiple = config.getMultiple();
		_mandatory = config.getMandatory();
		_annotations = config;
		_normalizer = ValueNormalizer.annotated(config);
		_label = config.getLabel();
		_placeholder = config.getPlaceholder();
		_icon = config.getIcon();
		_clearable = config.getClearable();
		_debounce = config.getDebounce();
		_readonly = config.getReadonly();
		_editModeRef = config.getEditMode();
		_labelPosition = config.getLabelPosition();

		PolymorphicConfiguration<? extends ViewCommand> submitConfig = config.getOnSubmit();
		_submitCommandConfig = submitConfig instanceof ViewCommand.Config commandConfig ? commandConfig : null;
		_submitCommand = _submitCommandConfig == null ? null
			: ViewCommands.instantiate(context, submitConfig, config.getOnSubmitAs());
		_submitAs = config.getOnSubmitAs();
		_submitOn = config.getSubmitOn();
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
		ViewChannel editModeChannel = _editModeRef == null ? null : context.resolveChannel(_editModeRef);
		FormModel form = editModeChannel == null ? context.getFormModel() : null;
		field.setEditable(!_readonly && editMode(editModeChannel, form));
		field.setMandatory(_mandatory);

		// Bound before the control is built, so that the control is built over the value the
		// channel already holds instead of over an empty field it has to be told about afterwards.
		ChannelFieldBinding binding = ChannelFieldBinding.bind(context.resolveChannel(_valueRef), field,
			options != null, _multiple);
		// Set after binding: the value the channel holds is shown as it is, only an entered value is
		// normalized.
		field.setNormalizer(_normalizer);
		FieldModelListener mandatoryCheck = _mandatory ? checkMandatory(field) : null;

		Resources resources = Resources.getInstance();
		String label = _label == null ? null : resources.getString(_label);
		FieldSpec spec = FieldControlService.fieldSpec(type, _annotations, label, _multiple, field);
		if (_optionOrder != null) {
			spec.setOptionOrder(AttributeOptions.comparator(_optionOrder));
		} else if (_options != null) {
			// The options expression computes the order the options are offered in.
			spec.setOptionOrder(null);
		}
		if (_placeholder != null) {
			spec.setPlaceholder(resources.getString(_placeholder));
		}
		spec.setIcon(_icon).setClearable(_clearable).setDebounce(_debounce);
		ReactControl input = FieldControlService.getInstance().createFieldControl(context, type, _annotations,
			spec, field, _inputControl);
		input.addCleanupAction(binding::dispose);
		if (mandatoryCheck != null) {
			input.addCleanupAction(() -> field.removeListener(mandatoryCheck));
		}

		if (_options != null && !optionInputs.isEmpty()) {
			followOptions(type, optionInputs, (SelectFieldModel) field, input);
		}

		if (_submitCommand != null) {
			followSubmit(context, input, field, binding);
		}

		if (_label == null && _labelPosition == null) {
			input.setCssClass(_cssClass);
			followEditMode(editModeChannel, form, field, input, null);
			return input;
		}
		boolean editable = field.isEditable();
		ReactFormFieldChromeControl chrome =
			new ReactFormFieldChromeControl(context, label, _mandatory && editable, false, null, null,
				AttributeFieldControl.wirePosition(_labelPosition, editable), false, true, input);
		chrome.setCssClass(_cssClass);
		followEditMode(editModeChannel, form, field, input, chrome);
		return chrome;
	}

	/**
	 * Whether the value can be changed, as far as the edit mode decides.
	 *
	 * @param editModeChannel
	 *        The {@link Config#getEditMode() edit mode channel}, or {@code null} if none is
	 *        configured.
	 * @param form
	 *        The enclosing form, or {@code null} if the input stands outside a form or the edit
	 *        mode channel decides.
	 */
	private static boolean editMode(ViewChannel editModeChannel, FormModel form) {
		if (editModeChannel != null) {
			return Boolean.TRUE.equals(editModeChannel.get());
		}
		if (form != null) {
			return form.isEditMode();
		}
		return true;
	}

	/**
	 * Makes the input follow the edit mode of the {@link Config#getEditMode() edit mode channel}
	 * respectively of the enclosing form.
	 *
	 * <p>
	 * The mandatory mark and an edit-dependent label position follow the editability, as they do
	 * for the field of an attribute. A {@link Config#getReadonly() read-only} input follows
	 * nothing.
	 * </p>
	 *
	 * @param chrome
	 *        The chrome around the input, or {@code null} if the input stands alone.
	 */
	private void followEditMode(ViewChannel editModeChannel, FormModel form, AbstractFieldModel field,
			ReactControl input, ReactFormFieldChromeControl chrome) {
		if (_readonly) {
			return;
		}
		Runnable update = () -> {
			boolean editable = editMode(editModeChannel, form);
			field.setEditable(editable);
			if (chrome != null) {
				chrome.setRequired(_mandatory && editable);
				chrome.setLabelPosition(AttributeFieldControl.wirePosition(_labelPosition, editable));
			}
		};
		if (editModeChannel != null) {
			Registration registration =
				editModeChannel.addListener((sender, oldValue, newValue) -> update.run());
			input.addCleanupAction(registration::dispose);
		} else if (form != null) {
			FormModelListener listener = source -> update.run();
			form.addFormModelListener(listener);
			input.addCleanupAction(() -> form.removeFormModelListener(listener));
		}
	}

	/**
	 * Runs the {@link Config#getOnSubmit() submit command} on each value the user finishes
	 * entering.
	 *
	 * <p>
	 * In a field the user types in, the {@link Config#getSubmitOn() submit gesture} decides: on
	 * {@link SubmitOn#CHANGE}, each edit the user finishes runs the command; on
	 * {@link SubmitOn#ENTER}, the Enter gesture of the field does, and a field without one - a text
	 * area, in which Enter starts a new line - runs no command. In a field that is picked from,
	 * every value the user produces is a finished one, and the binding reports it once it has
	 * reached the channel. The command's executability rule decides per value, so the model needs
	 * no attachment to a channel.
	 * </p>
	 *
	 * <p>
	 * The command's result - its refusal included - is reported to the user: a finished edit and a
	 * submit return it as the result of the client's command. A picked value is reported from
	 * within the value change the client sent, which has no result to return it through, so a
	 * failed result is shown right away.
	 * </p>
	 *
	 * <p>
	 * A {@link Config#getMandatory() mandatory} value that is empty is not handed to the command:
	 * the field shows the missing value, and the finished edit or the submit succeeds without
	 * running anything.
	 * </p>
	 */
	private void followSubmit(ViewContext context, ReactControl input, AbstractFieldModel field,
			ChannelFieldBinding binding) {
		ViewCommandModel model =
			ViewCommandModel.forCommand(context, _submitCommand, _submitCommandConfig, _submitAs);
		if (input instanceof ReactFormFieldControl fieldControl && fieldControl.commitsEdits()) {
			Function<Object, HandlerResult> submit = value -> {
				if (missingValue(value) != null) {
					// The field reports the missing value; the submit itself has nothing to add.
					field.setRevealed(true);
					return HandlerResult.DEFAULT_RESULT;
				}
				return model.execute(context, value);
			};
			switch (_submitOn) {
				case CHANGE:
					fieldControl.setCommitListener(submit);
					break;
				case ENTER:
					if (fieldControl.hasSubmitGesture()) {
						fieldControl.setSubmitListener(submit);
					}
					break;
			}
		} else {
			binding.setCommitListener(value -> {
				if (missingValue(value) != null) {
					// The value change reveals the missing value at the field.
					return;
				}
				HandlerResult result = model.execute(context, value);
				if (!result.isSuccess()) {
					CommandErrors.show(context.getErrorSink(), result);
				}
			});
		}
	}

	/**
	 * Checks the value of a {@link Config#getMandatory() mandatory} field for emptiness, now and on
	 * every change.
	 *
	 * <p>
	 * The check reports at the field only once the field is revealed, which a change of its value
	 * does: an input that appears empty does not complain before the user touched it.
	 * </p>
	 *
	 * @return The registered listener, to be removed when the input goes away.
	 */
	private FieldModelListener checkMandatory(AbstractFieldModel field) {
		field.setModelValidationError(missingValue(field.getValue()));
		FieldModelListener listener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				field.setModelValidationError(missingValue(newValue));
				field.setRevealed(true);
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// The check does not depend on editability.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Reported by the check itself.
			}
		};
		field.addListener(listener);
		return listener;
	}

	/**
	 * The error reporting that the given value is missing, or {@code null} if the value is given or
	 * may be empty.
	 */
	private ResKey missingValue(Object value) {
		return _mandatory ? MandatoryConstraintCheck.INSTANCE.checkValue(value) : null;
	}

	/**
	 * Recomputes the options whenever one of the option inputs changes, so that a selection offered
	 * for what the view currently shows stays in step with it.
	 */
	private void followOptions(TLType type, List<ViewChannel> optionInputs, SelectFieldModel field,
			ReactControl input) {
		ChannelListener listener = (sender, oldValue, newValue) -> field.setOptions(options(type, optionInputs));
		for (ViewChannel channel : optionInputs) {
			Registration registration = channel.addListener(listener);
			input.addCleanupAction(registration::dispose);
		}
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
