/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactGridControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.GridOptions;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.SelectionMode;

/**
 * Declarative {@link UIElement} repeating a content template over a computed list of model objects
 * (the {@code <object-list>} tag) - e.g. the comments of a ticket rendered as a conversation, or
 * the products of a catalogue rendered as a card grid.
 *
 * <p>
 * The element is model-agnostic: it is bound to any number of {@link Config#getInputs() inputs} plus
 * three functions over their values - the {@link Config#getItems() element lookup}, the
 * {@link Config#getLink() link function} that attaches a newly created element, and the
 * {@link Config#getRemove() remove function} that detaches an element. Any containment style works:
 * a composite reference, a back-reference on the element, an association, or a plain query - the
 * functions decide.
 * </p>
 *
 * <p>
 * For each element, the {@link Config#getItem() item content} is instantiated with the element
 * published on a local channel ({@link Config#getElementChannel()}), so the content typically binds
 * a {@code <form input="...">} over the element. Behind the items, the
 * {@link Config#getNewElement() new-element content} is instantiated once, bound to a channel
 * ({@link Config#getNewElementChannel()}) holding a fresh transient element of
 * {@link Config#getElementType()}; a command chain with {@code <store-form-state/>} and
 * {@link LinkElementAction &lt;link-element&gt;} persists it. Inside the item content,
 * {@link RemoveElementAction &lt;remove-element&gt;} detaches the item.
 * </p>
 *
 * <p>
 * Both slots hold ordinary view content, so the per-element layout is not a bespoke template but a
 * regular {@link UIElement}: typically a {@link com.top_logic.layout.view.ReferenceElement
 * &lt;view-ref&gt;} to an external {@code .view.xml} that binds the element channel and declares its
 * own display channels (e.g. author, date), or any element inline.
 * </p>
 *
 * <p>
 * The elements are arranged as the {@link Config#getLayout() layout} says, a column by default and
 * a responsive grid where a card per element is wanted. The arrangement covers the elements alone:
 * the {@link Config#getEmptyText() empty text} and the new-element content follow it as a whole,
 * rather than taking a place among the elements.
 * </p>
 *
 * <pre>
 * &lt;object-list
 *   inputs="catalogue"
 *   items="catalogue -&gt; $catalogue.get(`demo:Catalogue#products`)"
 *   layout="grid"
 *   max-columns="4"
 *   min-column-width="18rem"
 * &gt;
 *   &lt;item&gt;
 *     &lt;view-ref view="catalogue/product-card.view.xml"&gt;
 *       &lt;bind channel="product" to="element"/&gt;
 *     &lt;/view-ref&gt;
 *   &lt;/item&gt;
 * &lt;/object-list&gt;
 * </pre>
 *
 * <p>
 * With a {@link Config#getSelection() selection channel}, the elements are selectable like the
 * entries of a list box: clicking an element writes its object to the channel, and the element of
 * the object the channel holds is displayed as selected. Without one, the list only displays its
 * elements.
 * </p>
 *
 * <pre>
 * &lt;object-list
 *   inputs="catalogue"
 *   items="catalogue -&gt; $catalogue.get(`demo:Catalogue#products`)"
 *   selection="product"
 * &gt;
 *   &lt;item&gt;
 *     &lt;text input="element"/&gt;
 *   &lt;/item&gt;
 * &lt;/object-list&gt;
 * </pre>
 *
 * <p>
 * The displayed list follows the model: changes to an input, to displayed elements, or to objects of
 * the {@link Config#getObservedTypes() observed types} re-run the element lookup; unchanged elements
 * keep their controls (including edit state).
 * </p>
 */
@InApp
public class ObjectListElement implements UIElement {

	/**
	 * CSS class of the element the client places around each item, carrying its 0-based position as
	 * the CSS custom property {@code --item-index}.
	 *
	 * <p>
	 * An application styles the items of every list through this class, a staggered entrance
	 * animation composed from the position being the case it is meant for.
	 * </p>
	 */
	public static final String ITEM_CSS_CLASS = "tlObjectList__item";

	/**
	 * How an {@link ObjectListElement} arranges its elements.
	 */
	public enum Layout implements ExternallyNamed {

		/** One element below the other, each of them as wide as the list. */
		LIST("list"),

		/** As many elements next to each other as fit, reflowing with the available width. */
		GRID("grid");

		private final String _externalName;

		Layout(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

		/**
		 * Creates the control arranging the elements of a list this way, with each element wrapped
		 * in an item of the class {@link ObjectListElement#ITEM_CSS_CLASS}.
		 *
		 * <p>
		 * The container holds the repeated elements and nothing else, so that only they take a
		 * place in the arrangement. It starts out empty: what it displays is built while the list
		 * follows its model.
		 * </p>
		 *
		 * @param context
		 *        The context the control is created in.
		 * @param options
		 *        The options of the arrangement; of those, a list takes the space between the
		 *        elements and the width they are bounded to, the column options being a grid's.
		 */
		public ReactLayoutControl createContainer(ReactContext context, GridOptions options) {
			ReactLayoutControl result = switch (this) {
				case GRID -> new ReactGridControl(context, options.getMinColumnWidth(), options.getMaxColumns(),
					options.getGap(), List.of());
				case LIST -> new ReactStackControl(context, StackDirection.COLUMN, options.getGap(),
					StackAlign.STRETCH, false, List.of());
			};
			result.setItemClass(ITEM_CSS_CLASS);
			result.setMaxWidth(options.getMaxWidth());
			return result;
		}
	}

	/**
	 * Configuration for {@link ObjectListElement}.
	 */
	@TagName("object-list")
	public interface Config extends UIElement.Config, Inputs, GridOptions {

		/** Configuration name for {@link #getLayout()}. */
		String LAYOUT = "layout";

		/** Configuration name for {@link #getItems()}. */
		String ITEMS = "items";

		/** Configuration name for {@link #getLink()}. */
		String LINK = "link";

		/** Configuration name for {@link #getRemove()}. */
		String REMOVE = "remove";

		/** Configuration name for {@link #getElementType()}. */
		String ELEMENT_TYPE = "element-type";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getElementChannel()}. */
		String ELEMENT_CHANNEL = "element-channel";

		/** Configuration name for {@link #getNewElementChannel()}. */
		String NEW_ELEMENT_CHANNEL = "new-element-channel";

		/** Configuration name for {@link #getItem()}. */
		String ITEM = "item";

		/** Configuration name for {@link #getNewElement()}. */
		String NEW_ELEMENT = "new-element";

		/** Configuration name for {@link #getEmptyText()}. */
		String EMPTY_TEXT = "empty-text";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getSelectionMode()}. */
		String SELECTION_MODE = "selection-mode";

		@Override
		@ClassDefault(ObjectListElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * How the elements are arranged.
		 *
		 * <p>
		 * A list places one element below the other, a grid places as many next to each other as
		 * fit. The grid options apply to a grid only, except for the space between the elements,
		 * which separates them in either arrangement.
		 * </p>
		 */
		@Name(LAYOUT)
		Layout getLayout();

		/**
		 * TL-Script function computing the displayed elements from the values of the inputs:
		 * {@code ...inputs -> elements}.
		 *
		 * <p>
		 * Applied again whenever an input changes, so the displayed elements follow what the inputs
		 * hold. An input whose object was deleted meanwhile is passed as nothing.
		 * </p>
		 */
		@Name(ITEMS)
		@Mandatory
		Expr getItems();

		/**
		 * TL-Script function attaching a newly composed element:
		 * {@code ...inputs -> element -> ...}.
		 *
		 * <p>
		 * Run by {@link LinkElementAction &lt;link-element&gt;} in a transaction. The element passed
		 * behind the input values is the new-element template's transient object; the function is
		 * responsible for making it persistent (e.g. via a non-transient copy) and attaching it
		 * where it belongs.
		 * </p>
		 */
		@Name(LINK)
		Expr getLink();

		/**
		 * TL-Script function detaching an element: {@code ...inputs -> element -> ...}.
		 *
		 * <p>
		 * Run by {@link RemoveElementAction &lt;remove-element&gt;} in a transaction, with the
		 * element passed behind the input values.
		 * </p>
		 */
		@Name(REMOVE)
		Expr getRemove();

		/**
		 * The type of the transient element created for the new-element template.
		 */
		@Name(ELEMENT_TYPE)
		TLModelPartRef getElementType();

		/**
		 * Types whose object changes (create / update / delete) trigger a re-evaluation of the
		 * {@link #getItems() element lookup}, so the list refreshes automatically.
		 *
		 * <p>
		 * When empty, the {@link #getElementType() element type} is observed.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * Name of the channel publishing the current element to the item template.
		 */
		@Name(ELEMENT_CHANNEL)
		@StringDefault("element")
		String getElementChannel();

		/**
		 * Name of the channel publishing the transient element to the new-element template.
		 */
		@Name(NEW_ELEMENT_CHANNEL)
		@StringDefault("new-element")
		String getNewElementChannel();

		/**
		 * The content instantiated once per list element, with the element published on the
		 * {@link #getElementChannel() element channel}.
		 *
		 * <p>
		 * Ordinary view content: typically a single {@link com.top_logic.layout.view.ReferenceElement
		 * &lt;view-ref&gt;} binding the element channel, or elements written inline. Multiple entries
		 * are stacked vertically.
		 * </p>
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address
		 *           an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(ITEM)
		@Mandatory
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getItem();

		/**
		 * The content instantiated once behind the items for entering a new element, with the fresh
		 * transient element published on the {@link #getNewElementChannel() new-element channel};
		 * omitted for a read-only list.
		 *
		 * <p>
		 * Displayed while every input holds a value, because an element is composed to be attached
		 * somewhere. Ordinary view content, like {@link #getItem()}.
		 * </p>
		 *
		 * @implNote Keyed by {@link UIElement.Config#ID} so that a same-path overlay can address
		 *           an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(NEW_ELEMENT)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getNewElement();

		/**
		 * Text displayed instead of items when the list is empty.
		 */
		@Name(EMPTY_TEXT)
		ResKey getEmptyText();

		/**
		 * Optional {@link ViewChannel} the object of the selected element is written to.
		 *
		 * <p>
		 * The element of the object the channel holds is displayed as selected. Without a selection
		 * channel, the elements are not selectable.
		 * </p>
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Whether the user may select one element at a time, or any number of them.
		 *
		 * <p>
		 * {@link SelectionMode#SINGLE} (the default) replaces the selection with every click, and a
		 * click on the selected element with {@code Ctrl} gives it up again.
		 * </p>
		 *
		 * <p>
		 * {@link SelectionMode#MULTI} adds an element to the selection, or takes it out again, by a
		 * click with {@code Ctrl} or a long press on a touch screen; a click with {@code Shift} adds the elements from the one clicked
		 * last up to the clicked one.
		 * </p>
		 *
		 * <p>
		 * The {@link #getSelection() selection channel} holds the object of the selected element
		 * while exactly one is selected, the set of the selected objects while there are several,
		 * and nothing while there is none.
		 * </p>
		 */
		@Name(SELECTION_MODE)
		@ComplexDefault(SelectionMode.SingleDefault.class)
		SelectionMode getSelectionMode();
	}

	private final Config _config;

	private final QueryExecutor _itemsExecutor;

	private final QueryExecutor _linkExecutor;

	private final QueryExecutor _removeExecutor;

	private final List<UIElement> _itemContent;

	private final List<UIElement> _newElementContent;

	/**
	 * Creates an {@link ObjectListElement} from configuration.
	 */
	@CalledByReflection
	public ObjectListElement(InstantiationContext context, Config config) {
		_config = config;
		_itemsExecutor = QueryExecutor.compile(config.getItems());
		_linkExecutor = config.getLink() == null ? null : QueryExecutor.compile(config.getLink());
		_removeExecutor = config.getRemove() == null ? null : QueryExecutor.compile(config.getRemove());
		_itemContent = instantiate(context, config.getItem());
		_newElementContent = instantiate(context, config.getNewElement());
	}

	private static List<UIElement> instantiate(InstantiationContext context,
			List<UIElement.Config> content) {
		if (content == null) {
			return Collections.emptyList();
		}
		return content.stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(
			ChildGroup.elements(_itemContent),
			ChildGroup.elements(_newElementContent));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputs = ChannelInputs.resolve(context, _config.getInputs());

		ObjectListScope scope = new ObjectListScope(inputs, _linkExecutor, _removeExecutor);
		ViewContext templateContext = context.withScope(ObjectListScope.class, scope);

		ObjectListItems items = new ObjectListItems(templateContext, scope, _config.getLayout(), _config, inputs,
			_itemContent, _newElementContent,
			_config.getElementChannel(), _config.getNewElementChannel(),
			resolveElementType(), _config.getEmptyText());

		ChannelRef selection = _config.getSelection();
		if (selection != null) {
			items.bindSelection(_config.getSelectionMode(), context.resolveChannel(selection));
		}

		QueryExecutor itemsExecutor = _itemsExecutor;
		List<Object> initialElements = computeElements(itemsExecutor, ChannelInputs.arguments(inputs));
		items.showElements(initialElements);

		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			initialElements,
			args -> computeElements(itemsExecutor, args),
			resolveObservedTypes(),
			inputs,
			items::showElements);

		ReactLayoutControl display = items.display();
		display.setCssClass(_config.getCssClass());
		display.addAttachListener(() -> observer.attach(context.getModelScope()));
		display.addDetachListener(observer::detach);

		return display;
	}

	/**
	 * The elements the element function yields for the given input values.
	 *
	 * @param inputValues
	 *        The values of the input channels, in declaration order; an object that was deleted is
	 *        passed on as nothing.
	 */
	private static List<Object> computeElements(QueryExecutor itemsExecutor, Object[] inputValues) {
		Object result = itemsExecutor.execute(InputValues.alive(inputValues));
		if (result instanceof Collection<?> collection) {
			return new ArrayList<>(collection);
		}
		return result == null ? Collections.emptyList() : Collections.singletonList(result);
	}

	private TLClass resolveElementType() {
		TLModelPartRef ref = _config.getElementType();
		if (ref == null) {
			return null;
		}
		return (TLClass) ref.resolveType();
	}

	/**
	 * The types whose object changes refresh the list: the configured
	 * {@link Config#getObservedTypes() observed types}, or the
	 * {@link Config#getElementType() element type} where none is configured, so that a created
	 * element of that type reaches the list.
	 */
	private Set<TLStructuredType> resolveObservedTypes() {
		List<TLModelPartRef> refs = _config.getObservedTypes();
		if (refs != null && !refs.isEmpty()) {
			return ObservedTypes.resolve(refs);
		}
		TLClass elementType = resolveElementType();
		return elementType == null ? Set.of() : Set.of(elementType);
	}

}
