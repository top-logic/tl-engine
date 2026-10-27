/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.OptionProvider;
import com.top_logic.element.meta.kbbased.storage.StringSetStorage;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.select.OptionOrder;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.ClassificationDisplay;
import com.top_logic.model.annotate.ui.ReferenceDisplay;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ReactFieldControlProvider} for attributes that are edited by selecting from a set of
 * options.
 *
 * <p>
 * Wraps a {@link SelectFieldModel} (an {@link AttributeSelectFieldModel}) in a
 * {@link ReactDropdownSelectControl}, using {@link MetaResourceProvider} so that options and the
 * current selection are rendered with the label and the image their model registers, in both edit
 * and display mode.
 * </p>
 *
 * <p>
 * When configured for a special model datatype (e.g. {@code tl.util:Country}) via the
 * {@link FieldControlService} type map, an {@link #getConfiguredOptions() option provider} supplies
 * the selectable values. Without a configured option provider, options are derived from the
 * attribute itself (enumeration, reference, enum datatype, or a TL-Script options annotation).
 * </p>
 *
 * <p>
 * The options are offered in the shape the edited field asks for, which for a model attribute
 * follows its {@link ReferenceDisplay} or {@link ClassificationDisplay} annotation and is a list
 * that opens on demand where the attribute says nothing. The options come in the order their
 * source gives them: the classifiers of an enumeration and the result of an options expression
 * keeping its order as given, the instances of a class sorted by their labels. A
 * {@link Config#getDisplay() display}, an {@link Config#getOrientation() orientation} and an
 * {@link Config#getOrder() order} configured here override what the field says, so a single form
 * field deviates from how the attribute is displayed elsewhere.
 * </p>
 *
 * <p>
 * A list that opens on demand can accept {@link Config#hasFreeEntry() free entry}: the user picks
 * an option or types a text that becomes the value, or from which the {@link Config#getCreate()
 * create} script makes the value - a new keyword named by the text, say.
 * </p>
 *
 * @implNote What the field asks for is read from {@link FieldSpec#getSelectDisplay()},
 *           {@link FieldSpec#getSelectOrientation()} and {@link FieldSpec#getOptionOrder()}.
 */
public class SelectControlProvider implements ReactFieldControlProvider {

	/**
	 * Configuration for {@link SelectControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<SelectControlProvider> {

		/** Configuration name for {@link #getOptionProvider()}. */
		String OPTION_PROVIDER = "option-provider";

		/** Configuration name for {@link #getDisplay()}. */
		String DISPLAY = "display";

		/** Configuration name for {@link #getOrientation()}. */
		String ORIENTATION = "orientation";

		/** Configuration name for {@link #getOrder()}. */
		String ORDER = "order";

		/** Configuration name for {@link #hasFilter()}. */
		String FILTER = "filter";

		/** Configuration name for {@link #hasFreeEntry()}. */
		String FREE_ENTRY = "free-entry";

		/** Configuration name for {@link #getCreate()}. */
		String CREATE = "create";

		/** Name of the variable holding the entered text in the {@link #getCreate()} script. */
		String TEXT_VARIABLE = "text";

		/** Name of the variable holding the edited object in the {@link #getCreate()} script. */
		String SELF_VARIABLE = "self";

		@Override
		@ClassDefault(SelectControlProvider.class)
		Class<? extends SelectControlProvider> getImplementationClass();

		/**
		 * The option source for the selectable values.
		 *
		 * <p>
		 * If unset, options are derived from the attribute's type (enumeration, reference, enum
		 * datatype) or its TL-Script options annotation.
		 * </p>
		 */
		@Name(OPTION_PROVIDER)
		@InstanceFormat
		OptionProvider getOptionProvider();

		/**
		 * The shape the options are offered in, or nothing to offer them the way the edited
		 * attribute asks for.
		 *
		 * <p>
		 * A list that opens on demand takes the room of one field whatever the number of options
		 * and is searched by typing, which suits a list of any length. A cloud of toggles and a bar
		 * of segments show every option at all times, so the value and what else could be chosen
		 * are read without opening anything - at the price of the room all options take, which
		 * makes them a choice for a handful of options rather than for a long list. A group of
		 * radio buttons shows every option the way a form marks a choice, with a checkbox for each
		 * option where several values can be chosen.
		 * </p>
		 *
		 * <p>
		 * Stated here, the shape holds for this field alone; stated as the {@link ReferenceDisplay}
		 * or {@link ClassificationDisplay} annotation of an attribute, it holds wherever that
		 * attribute is shown. Where neither states a shape, the options are offered in a list that
		 * opens on demand.
		 * </p>
		 */
		@Name(DISPLAY)
		@Nullable
		@NullDefault
		SelectDisplay getDisplay();

		/**
		 * The direction a group of radio buttons lays its options out in, or nothing to lay them
		 * out the way the edited attribute asks for.
		 *
		 * <p>
		 * One below the other, the options are scanned like a list; side by side, a few short
		 * options take a single line. Where neither this setting nor the annotation of the
		 * attribute states a direction, the options are laid out one below the other. The other
		 * shapes of the {@link #getDisplay() display} have no direction to choose and ignore it.
		 * </p>
		 */
		@Name(ORIENTATION)
		@Nullable
		@NullDefault
		Orientation getOrientation();

		/**
		 * The order the options are offered in, or nothing to offer them in the order their source
		 * gives them.
		 *
		 * <p>
		 * Sorted by label, an option is found where its name puts it, which suits a long list of
		 * names without an order of their own. As given, options with an order of their own - the
		 * days of a week, the steps of a process, a list that starts with "none" - keep that order.
		 * </p>
		 *
		 * <p>
		 * Where nothing is stated, the source of the options decides: the classifiers of an
		 * enumeration and the result of an options expression that is not asked to sort
		 * alphabetically are offered as given, the instances of a class sorted by their labels.
		 * Stated here, the order holds for this field alone.
		 * </p>
		 */
		@Name(ORDER)
		@Nullable
		@NullDefault
		OptionOrder getOrder();

		/**
		 * Whether the list that opens on demand offers an input to filter its options by.
		 *
		 * <p>
		 * Without the input, the open list is operated by the keyboard alone: the arrow keys move
		 * through the options, and typing the beginning of a label jumps to the option it starts.
		 * That suits a short list whose labels the user knows. The shapes of the
		 * {@link #getDisplay() display} that show every option have nothing to filter and ignore
		 * it.
		 * </p>
		 */
		@Name(FILTER)
		@BooleanDefault(true)
		boolean hasFilter();

		/**
		 * Whether the user may enter a text that is none of the options, instead of choosing one.
		 *
		 * <p>
		 * For a value that is mostly one of a known set but now and then a new one: the keywords of
		 * a document, the tags of a task, the place of a meeting. The user types into the list
		 * that opens on demand; a text matching the label of an option, ignoring case, chooses that
		 * option, any other text becomes a value of its own. A field holding several values adds it
		 * to them, a field holding one value takes it as its value. A text entered twice yields one
		 * value, not two.
		 * </p>
		 *
		 * <p>
		 * The value a text becomes is the result of the {@link #getCreate() create} script. Without
		 * a script, only an attribute holding texts accepts free entry, and the text itself becomes
		 * the value; for any other attribute, free entry is switched off and an error is logged. The
		 * values of an attribute stored as one comma separated text cannot contain a comma: such a
		 * text is refused.
		 * </p>
		 *
		 * <p>
		 * Only the list that opens on demand has an input to type into; the shapes of the
		 * {@link #getDisplay() display} that show every option ignore it.
		 * </p>
		 */
		@Name(FREE_ENTRY)
		boolean hasFreeEntry();

		/**
		 * The value a text the user entered becomes, where the field accepts {@link #hasFreeEntry()
		 * free entry}.
		 *
		 * <p>
		 * A TL-Script expression using the variables {@code $text}, the entered text without
		 * leading and trailing white space, and {@code $self}, the edited object (nothing where the
		 * field edits no attribute of an object), without declaring them. Typically it creates a
		 * new object named by the text, which becomes the value of a reference:
		 * </p>
		 *
		 * <pre>
		 * new(`my:Keyword`, transient: true)..set(`my:Keyword#name`, $text)
		 * </pre>
		 *
		 * <p>
		 * The new object is created as transient object, as the editing of a form keeps everything
		 * the user changes: storing the form creates it, cancelling the form drops it. A text
		 * matching the label of an option, of a value of the field, or of a value created before,
		 * ignoring case, chooses that value without the script being evaluated. A result of nothing
		 * leaves the value of the field unchanged.
		 * </p>
		 *
		 * <p>
		 * Without a script, the text itself becomes the value, which suits an attribute holding
		 * texts only.
		 * </p>
		 */
		@Name(CREATE)
		@Supplies(TEXT_VARIABLE)
		@Supplies(SELF_VARIABLE)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getCreate();
	}

	private final OptionProvider _optionProvider;

	private final SelectDisplay _display;

	private final Orientation _orientation;

	private final OptionOrder _order;

	private final boolean _filter;

	private final boolean _freeEntry;

	/**
	 * The compiled {@link Config#getCreate()} script, {@code null} for none.
	 */
	private final QueryExecutor _create;

	/**
	 * The inputs the {@link #_create} script sees ahead of the values it is supplied with.
	 */
	private final List<InputRef> _createInputs;

	/**
	 * Creates a {@link SelectControlProvider} without a configured option source (options are
	 * derived from the attribute), offering them in the shape and the order each field asks for.
	 */
	public SelectControlProvider() {
		_optionProvider = null;
		_display = null;
		_orientation = null;
		_order = null;
		_filter = true;
		_freeEntry = false;
		_create = null;
		_createInputs = List.of();
	}

	/**
	 * Creates a configured {@link SelectControlProvider}.
	 */
	@CalledByReflection
	public SelectControlProvider(InstantiationContext context, Config config) {
		_optionProvider = config.getOptionProvider();
		_display = config.getDisplay();
		_orientation = config.getOrientation();
		_order = config.getOrder();
		_filter = config.hasFilter();
		_freeEntry = config.hasFreeEntry();
		_create = InputScript.compile(config, Config.CREATE);
		_createInputs = _create == null ? List.of()
			: InputScript.inputs(config, config.descriptor().getProperty(Config.CREATE));
	}

	/**
	 * The configured option source, or {@code null} to derive options from the attribute.
	 */
	public OptionProvider getConfiguredOptions() {
		return _optionProvider;
	}

	/**
	 * The shape the options are offered in, or {@code null} to follow what each field says.
	 */
	public SelectDisplay getDisplay() {
		return _display;
	}

	/**
	 * The direction a group of radio buttons lays its options out in, or {@code null} to follow
	 * what each field says.
	 */
	public Orientation getOrientation() {
		return _orientation;
	}

	/**
	 * The order the options are offered in, or {@code null} to follow what each field says.
	 */
	public OptionOrder getOrder() {
		return _order;
	}

	/**
	 * Whether the list that opens on demand offers an input to filter its options by.
	 */
	public boolean hasFilter() {
		return _filter;
	}

	/**
	 * Whether the user may enter a text that is none of the options.
	 *
	 * @see Config#hasFreeEntry()
	 */
	public boolean hasFreeEntry() {
		return _freeEntry;
	}

	/**
	 * A selection is made on one control, however many options it accepts.
	 *
	 * <p>
	 * The selected values are the value of the {@link SelectFieldModel} the control is bound to, so
	 * a multi-valued field is picked from in one dropdown rather than through one dropdown per
	 * value.
	 * </p>
	 */
	@Override
	public boolean editsCollections() {
		return true;
	}

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		SelectFieldModel selectModel = (SelectFieldModel) model;
		// A resource provider rather than a label provider: an option is presented by its label and
		// its image, and the image is what a plain label provider cannot answer - the control drops
		// it for want of one. The two registries of LabelProviderService fall back to each other,
		// so a type registered only for its label is labelled exactly as before.
		LabelProvider labels = MetaResourceProvider.INSTANCE;
		Comparator<?> optionOrder =
			_order == null ? field.getOptionOrder() : AttributeOptions.comparator(_order);
		// An ordered attribute keeps the order the user gives its selection; an unordered one is
		// shown in the order of the options.
		SelectDisplay display = _display == null ? field.getSelectDisplay() : _display;
		Orientation orientation = _orientation == null ? field.getSelectOrientation() : _orientation;
		ReactDropdownSelectControl control = new ReactDropdownSelectControl(context, selectModel, labels,
			optionOrder, field.isOrdered(), display, orientation);
		if (!_filter) {
			control.setFilter(false);
		}
		if (_freeEntry && display == SelectDisplay.DROPDOWN) {
			Function<String, Object> textToValue = textToValue(context, field, model);
			if (textToValue != null) {
				control.setFreeEntry(textToValue);
			}
		}
		return control;
	}

	/**
	 * The function making the value of the given field from a text the user entered, see
	 * {@link Config#hasFreeEntry()}, or {@code null} where the field cannot accept free entry.
	 */
	private Function<String, Object> textToValue(ReactContext context, FieldSpec field, FieldModel model) {
		TLStructuredTypePart part = model instanceof AttributeFieldModel attribute ? attribute.getPart() : null;
		Function<String, Object> conversion;
		if (_create != null) {
			conversion = createScript(context, model);
		} else if (holdsText(part, field)) {
			conversion = text -> text;
		} else {
			Logger.error("The field '" + fieldName(part, field) + "' cannot accept free entry: its values are no texts,"
				+ " and no '" + Config.CREATE + "' script makes a value from an entered text.",
				SelectControlProvider.class);
			return null;
		}
		if (part != null && part.isMultiple()
			&& AttributeOperations.getStorageImplementation(part) instanceof StringSetStorage) {
			return text -> checkNoSeparator(conversion.apply(text));
		}
		return conversion;
	}

	/**
	 * The {@link #_create} script as function of the entered text, evaluated for the object the
	 * given model edits at the time of the entry.
	 */
	private Function<String, Object> createScript(ReactContext context, FieldModel model) {
		List<ViewChannel> inputs;
		if (_createInputs.isEmpty()) {
			inputs = List.of();
		} else if (context instanceof ViewContext viewContext) {
			inputs = ChannelInputs.resolve(viewContext, _createInputs);
		} else {
			throw new IllegalStateException("A select whose '" + Config.CREATE
				+ "' script reads the values of channels must be created in a view, but is created in: " + context);
		}
		return text -> {
			TLObject self = model instanceof AttributeFieldModel attribute ? attribute.getObject() : null;
			return _create.execute(ChannelInputs.arguments(inputs, text, self));
		};
	}

	/**
	 * Refuses a text value containing the separator of the values of an attribute that stores them
	 * as one text, see {@link StringSetStorage}.
	 */
	private static Object checkNoSeparator(Object value) {
		if (value instanceof String text && text.indexOf(StringSetStorage.VALUE_SEPARATOR) >= 0) {
			throw new TopLogicException(I18NConstants.ERROR_ENTRY_CONTAINS_SEPARATOR__TEXT.fill(text));
		}
		return value;
	}

	/**
	 * Whether the values of the field are texts, which an entered text can be taken as.
	 *
	 * @param part
	 *        The edited attribute, deciding by its type; {@code null} where the field edits no
	 *        attribute, and the value type of the field decides.
	 */
	private static boolean holdsText(TLStructuredTypePart part, FieldSpec field) {
		if (part != null) {
			return part.getType() instanceof TLPrimitive primitive && primitive.getKind() == TLPrimitive.Kind.STRING;
		}
		return field.getValueType() == String.class;
	}

	private static String fieldName(TLStructuredTypePart part, FieldSpec field) {
		return part != null ? TLModelUtil.qualifiedName(part) : field.getLabel();
	}

}
