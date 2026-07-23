/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Declarative {@link UIElement} that edits a list of model objects through a per-object content
 * template (the {@code <object-list>} tag) - e.g. the comments of a ticket, rendered as a
 * conversation.
 *
 * <p>
 * The element is model-agnostic: it is bound to a container object (from the
 * {@link Config#getInput() input} channel) plus three functions over that container - the
 * {@link Config#getItems() element lookup}, the {@link Config#getLink() link function} that
 * attaches a newly created element, and the {@link Config#getRemove() remove function} that
 * detaches an element. Any containment style works: a composite reference, a back-reference on the
 * element, or an association - the functions decide.
 * </p>
 *
 * <p>
 * For each element, the {@link Config#getItem() item content} is instantiated with the element
 * published on a local channel ({@link Config#getElementChannel()}), so the content typically binds
 * a {@code <form input="...">} over the element. Below the items, the
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
 * The displayed list follows the model: changes to the container channel, to displayed elements,
 * or to objects of the {@link Config#getObservedTypes() observed types} re-run the element lookup;
 * unchanged elements keep their controls (including edit state).
 * </p>
 */
public class ObjectListElement implements UIElement {

	/**
	 * Configuration for {@link ObjectListElement}.
	 */
	@TagName("object-list")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

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

		@Override
		@ClassDefault(ObjectListElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Reference to the channel providing the container object whose elements are listed.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * TL-Script function computing the list elements from the container:
		 * {@code container -> elements}.
		 */
		@Name(ITEMS)
		@Mandatory
		Expr getItems();

		/**
		 * TL-Script function linking a new element to the container:
		 * {@code container -> element -> ...}.
		 *
		 * <p>
		 * Run by {@link LinkElementAction &lt;link-element&gt;} in a transaction. The element
		 * passed is the new-element template's transient object; the function is responsible for
		 * making it persistent (e.g. via a non-transient copy) and attaching it to the container.
		 * </p>
		 */
		@Name(LINK)
		Expr getLink();

		/**
		 * TL-Script function detaching an element from the container:
		 * {@code container -> element -> ...}.
		 *
		 * <p>
		 * Run by {@link RemoveElementAction &lt;remove-element&gt;} in a transaction.
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
		 */
		@Name(ITEM)
		@Mandatory
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getItem();

		/**
		 * The content instantiated once below the items for entering a new element, with the fresh
		 * transient element published on the {@link #getNewElementChannel() new-element channel};
		 * omitted for a read-only list.
		 *
		 * <p>
		 * Ordinary view content, like {@link #getItem()}.
		 * </p>
		 */
		@Name(NEW_ELEMENT)
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getNewElement();

		/**
		 * Text displayed instead of items when the list is empty.
		 */
		@Name(EMPTY_TEXT)
		ResKey getEmptyText();
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
			List<PolymorphicConfiguration<? extends UIElement>> content) {
		if (content == null) {
			return Collections.emptyList();
		}
		return content.stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel container = context.resolveChannel(_config.getInput());

		ObjectListScope scope = new ObjectListScope(container, _linkExecutor, _removeExecutor);
		ViewContext templateContext = context.withScope(ObjectListScope.class, scope);

		ObjectListControl control = new ObjectListControl(templateContext, scope, container,
			_itemContent, _newElementContent,
			_config.getElementChannel(), _config.getNewElementChannel(),
			resolveElementType(), _config.getEmptyText());

		QueryExecutor itemsExecutor = _itemsExecutor;
		List<Object> initialElements = computeElements(itemsExecutor, container.get());
		control.showElements(initialElements);

		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			initialElements,
			args -> computeElements(itemsExecutor, args[0]),
			resolveObservedTypes(),
			List.of(container),
			control::showElements);
		control.addBeforeWriteAction(() -> observer.attach(context.getModelScope()));
		control.addCleanupAction(observer::detach);

		return control;
	}

	private static List<Object> computeElements(QueryExecutor itemsExecutor, Object container) {
		if (container == null) {
			return Collections.emptyList();
		}
		Object result = itemsExecutor.execute(container);
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

	private Set<TLStructuredType> resolveObservedTypes() {
		List<TLModelPartRef> refs = _config.getObservedTypes();
		Set<TLStructuredType> types = new HashSet<>();
		if (refs == null || refs.isEmpty()) {
			TLClass elementType = resolveElementType();
			if (elementType != null) {
				types.add(elementType);
			}
			return types;
		}
		for (TLModelPartRef ref : refs) {
			TLStructuredType type = (TLStructuredType) ref.resolveType();
			if (type == null) {
				throw new RuntimeException("Failed to resolve observed type: " + ref.qualifiedName());
			}
			types.add(type);
		}
		return types;
	}

}
