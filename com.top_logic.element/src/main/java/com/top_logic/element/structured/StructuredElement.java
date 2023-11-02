/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured;

import static com.top_logic.basic.StringServices.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.util.Utils;
import com.top_logic.element.core.CreateElementException;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.wrap.WrapperTLElement;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.util.StructuredElementUtil;
import com.top_logic.element.structured.wrap.StructuredElementWrapperFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * Base interface for the creation of hierarchical structures.
 * <p>
 * The interface can be used to generate a tree like structure of elements, where the elements
 * themselves are able to create new children of theirselves.
 * </p>
 * 
 * <p>
 * {@link StructuredElement} defines a 'plain' structure. Issues like security must be handled in
 * sub classes or via filters and visitors.
 * </p>
 * 
 * <p>
 * The interface does not define the isLeaf() method (as mentioned in
 * {@link javax.swing.tree.TreeNode}), because the answer could be ambiguous e.g. if security
 * restrictions prevent delivery of children.
 * </p>
 * 
 * @see TraversalFactory#traverse(StructuredElement, com.top_logic.element.core.TLElementVisitor,
 *      int)
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public interface StructuredElement extends Wrapper, TLScope {

	/** KO type of a simple structured element. */
	String KO_TYPE = "StructuredElement";

	/** The name of the {@link #getParent()} attribute. */
	String PARENT_ATTR = "parent";

	/** The name of the {@link #getChildren()} attribute. */
	String CHILDREN_ATTR = "children";

	/**
	 * Name of the {@link TLType} representing {@link StructuredElement}s.
	 */
	String TL_TYPE_NAME = "StructuredElement";

	/**
	 * Name of the {@link TLType} representing {@link StructuredElement}s that can have children.
	 * <p>
	 * The differentiation between "StructuredElement" and "StructuredElementContainer" is necessary
	 * to support the pre TL 5.8 feature of declaring a StructureElement-type that cannot have
	 * children.
	 * </p>
	 */
	String CONTAINER_TL_TYPE_NAME = "StructuredElementContainer";

	/**
	 * Name of the {@link TLModule} containing the {@link StructuredElement} types like
	 * {@link StructuredElement}.
	 */
	String MODULE_NAME = "tl.element";

	/**
	 * The suffix of the synthetic "FooChildren" {@link TLType}s.
	 */
	String CHILDREN_TYPE_SUFFIX = "Child";

	/**
	 * Check if this element has any children.
	 * 
	 * @return <code>true</code> if this node has any children, <code>false</code> otherwise.
	 * 
	 * @since TL 5.7
	 */
	default boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	/**
	 * Check if this element has children using the given filter.
	 * 
	 * @param filter
	 *        A filter, may be <code>null</code> (in that case no filtering of children will be
	 *        done).
	 * @return <code>true</code> if this node has children matching the filter, <code>false</code>
	 *         otherwise.
	 */
	default boolean hasChildren(Filter<? super StructuredElement> filter) {
		if (filter == null || filter == FilterFactory.trueFilter()) {
			return hasChildren();
		}
		if (filter == FilterFactory.falseFilter()) {
			// Filter accept nothing
			return false;
		}
		Collection<? extends StructuredElement> unsortedChildren = getChildren();
		for (StructuredElement child : unsortedChildren) {
			if (filter.accept(child)) {
				return true;
			}
		}

		// There are elements but no match.
		return false;
	}

	/**
	 * Get all children of this element.
	 * 
	 * @return A list of all children of the element. A new, mutable and resizable {@link List}.
	 * 
	 * @see StructuredElement#getChildrenModifiable()
	 * 
	 * @since TL 5.7
	 */
	@SuppressWarnings("unchecked")
	default List<? extends StructuredElement> getChildren() {
		if (getChildrenTypes().isEmpty()) {
			// Compatibility for types not implementing the dynamic type StructuredElement, but
			// inheriting this method by accident.
			return Collections.emptyList();
		}
		return (List<? extends StructuredElement>) getValue(CHILDREN_ATTR);
	}

	/**
	 * Get the children of the element that match the given Filter
	 * 
	 * @param filter
	 *        A filter, may be <code>null</code>. In that case all children are returned.
	 * @return A list of children of the element that match the given Filter. A modification of the
	 *         result list must not affect the internal representation of the children.
	 */
	default List<? extends StructuredElement> getChildren(Filter<? super StructuredElement> filter) {
		List<? extends StructuredElement> result = getChildren();
		if (filter != null && filter != FilterFactory.trueFilter()) {
			FilterUtil.filterInline(filter, result);
		}
		return result;
	}

	/**
	 * Set the name of the element.
	 * 
	 * @param name
	 *        The name of the element. Must not be <code>null</code> or empty.
	 * @throws IllegalArgumentException
	 *         If the given name is <code>null</code> or empty.
	 */
	default void setElementName(String name) throws IllegalArgumentException {
		setValue(NAME_ATTRIBUTE, name);
	}

	/**
	 * Get the type of the element.
	 * 
	 * @return The type of the element, never <code>null</code> or empty.
	 */
	default String getElementType() {
		return tType().getName();
	}

	/**
	 * The allowed types for a child under this {@link StructuredElement}.
	 * 
	 * @return A new, mutable and resizable {@link Set}.
	 */
	default Set<TLClass> getChildrenTypes() {
		TLStructuredTypePart childrenAttribute = tType().getPart(CHILDREN_ATTR);
		if (childrenAttribute == null) {
			return set();
		}
		TLClass childrenType = (TLClass) childrenAttribute.getType();
		return StructuredElementUtil.getSubclassesForInstantiation(this, childrenType);
	}

	/**
	 * The name of structure this element belongs to.
	 */
	default String getStructureName() {
		return tType().getModule().getName();
	}

	/**
	 * Live view of the {@link List} of children.
	 * <p>
	 * Changes to this {@link List} change directly the attribute value. The caller has to take care
	 * of the transaction handling.
	 * </p>
	 * 
	 * @return A mutable and resizable {@link List}, if and only if this {@link StructuredElement}
	 *         can have children. Otherwise, an unmodifiable empty {@link List} is returned.
	 * @see #getChildren()
	 */
	default List<StructuredElement> getChildrenModifiable() {
		if (getChildrenTypes().isEmpty()) {
			return Collections.emptyList();
		}
		TLStructuredTypePart attribute = tType().getPart(CHILDREN_ATTR);
		StorageImplementation storage = AttributeOperations.getStorageImplementation(this, attribute);
		@SuppressWarnings("unchecked")
		List<StructuredElement> result = (List<StructuredElement>) storage.getLiveCollection(this, attribute);
		return result;
	}

	/**
	 * Adds an element to the {@link #getChildren()} list.
	 */
	default void addChild(StructuredElement newChild) {
		if (getChildrenTypes().isEmpty()) {
			throw new UnsupportedOperationException("No children supported.");
		}
		TLStructuredTypePart attribute = tType().getPart(CHILDREN_ATTR);
		StorageImplementation storage = AttributeOperations.getStorageImplementation(this, attribute);
		storage.addAttributeValue(this, attribute, newChild);
	}

	/**
	 * Removes an element from the {@link #getChildren()} list.
	 */
	default void removeChild(Object oldChild) {
		if (getChildrenTypes().isEmpty()) {
			return;
		}
		TLStructuredTypePart attribute = tType().getPart(CHILDREN_ATTR);
		StorageImplementation storage = AttributeOperations.getStorageImplementation(this, attribute);
		storage.removeAttributeValue(this, attribute, oldChild);
	}

	/**
	 * Convenience short-cut for {@link #createChild(String, TLClass)}, for an unresolved local type
	 * name instead of the {@link TLClass} object.
	 * <p>
	 * <em>Use {@link #createChild(String, TLClass)} directory, if the {@link TLClass} is already
	 * known.</em>
	 * </p>
	 */
	default StructuredElement createChild(String name, String typeName)
			throws IllegalArgumentException, CreateElementException {
		TLClass type = StructuredElementUtil.resolveChildType(this, typeName);
		return createChild(name, type);
	}

	/**
	 * Create a child element and add it to the list of children.
	 * <p>
	 * The child will become the last element of the list returned by getChildren() (if not filtered
	 * somehow).
	 * </p>
	 * 
	 * @param name
	 *        The name for the element. Must not be <code>null</code>.
	 * @param type
	 *        The desired type of the child. Implementation is free for sub classes but type must
	 *        neither be <code>null</code> nor empty.
	 * @return The created child, never <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If one the params is <code>null</code> or empty.
	 * @throws CreateElementException
	 *         If creation fails for reasons like: is leaf node, no access rights, failure in the
	 *         system (db)...
	 * @see #getChildrenTypes()
	 */
	default StructuredElement createChild(String name, TLClass type)
			throws IllegalArgumentException, CreateElementException {
		StructuredElementWrapperFactory factory =
			(StructuredElementWrapperFactory) DynamicModelService.getFactoryFor(getStructureName());
		return factory.createChild(getStructureName(), this, null, name, type);
	}

	/**
	 * Check if the given element is a child of this one.
	 * 
	 * This method will only care for direct children, for other cases use
	 * {@link TraversalFactory#traverse(StructuredElement, com.top_logic.element.core.TLElementVisitor, int, boolean, int)}.
	 * 
	 * @param child
	 *        An element, may be <code>null</code>.
	 * @return <code>true</code> if it is a child, <code>false</code> otherwise (e.g. if the given
	 *         child is <code>null</code>).
	 */
	default boolean isChild(StructuredElement child) {
		try {
			return getChildren().contains(child);
		} catch (Exception exp) {
			Logger.error("Failed to check for isChild", exp, this);
		}
		return false;
	}

	/**
	 * Move an existing child to a new position in the list of children.
	 * 
	 * <p>
	 * Depending on the second parameter, the element will be placed in the list of the children.
	 * The second element defines the next sibling of the one to be inserted. If the second
	 * parameter is <code>null</code>, the element will be placed on last position.
	 * </p>
	 * 
	 * <p>
	 * If the given child is not contained in the list of children, this call will return without
	 * changing the list.
	 * </p>
	 * 
	 * @param child
	 *        The child, which has to be placed in the list of children of this element, must not be
	 *        <code>null</code>.
	 * @param before
	 *        The sibling, which will become the next one in the list of children, may be
	 *        <code>null</code>, which will place the child at the end of the list of children of
	 *        this element.
	 * @return <code>true</code>, if calling this method changed the list of children.
	 * @throws IllegalArgumentException
	 *         If one the parameters is <code>null</code>.
	 */
	default boolean move(StructuredElement child, StructuredElement before) throws IllegalArgumentException {
		if (Utils.equals(child, before)) {
			throw new IllegalArgumentException("An element cannot be moved before itself. Element: " + debug(child));
		}
		if (!(child instanceof WrapperTLElement)) {
			throw new IllegalArgumentException(
				"Child to be moved is null or no WrapperTLElement. Child: " + debug(child));
		}

		List<? extends StructuredElement> oldChildren = getChildren();
		if (oldChildren.isEmpty()) {
			return false;
		}
		if ((before != null) && !oldChildren.contains(before)) {
			return (false);
		}
		if (!oldChildren.contains(child)) {
			return false;
		}
		List<StructuredElement> newChildren = list(oldChildren);
		newChildren.remove(child);
		if (before == null) {
			newChildren.add(child);
		} else {
			int newIndex = newChildren.indexOf(before);
			newChildren.add(newIndex, child);
		}
		setValue(CHILDREN_ATTR, newChildren);
		return !newChildren.equals(oldChildren);
	}

	/**
	 * Return the parent element.
	 * 
	 * @return The parent element or <code>null</code> if this element is the type root element of
	 *         the structure, the element was removed or is otherwise invalid.
	 * @see #getStructureContext()
	 * @see #getRoot()
	 */
	default StructuredElement getParent() {
		return (StructuredElement) getValue(PARENT_ATTR);
	}

	/**
	 * Get the root of the StructureElement tree.
	 * 
	 * It has no parent.
	 * 
	 * @return The root of the StructureElement tree, never <code>null</code>.
	 */
	default StructuredElement getRoot() {
		return (StructuredElement) tType().getModule().getSingleton(TLModule.DEFAULT_SINGLETON_NAME);
	}

	/**
	 * Get the context this instance lives in.
	 * 
	 * E.g. if this element is a work package the project will be returned.
	 * 
	 * @return The context this instance lives in, never <code>null</code>.
	 * @throws IllegalStateException
	 *         If the object has no structure context, see {@link #getStructureContextOrNull()}.
	 */
	default StructuredElement getStructureContext() throws IllegalStateException {
		StructuredElement context = getStructureContextOrNull();

		if (context == null) {
			throw new IllegalStateException("Element '" + this + "' has no structure context.");
		}

		return context;
	}

	/**
	 * Check if this is the root of the StructureElement tree.
	 * 
	 * @return <code>true</code> if this is the root of the StructureElement tree.
	 */
	default boolean isRoot() {
		return getRoot() == this;
	}

	/**
	 * Check if this is the context of a structure (e.g. a project).
	 *
	 * @return <code>true</code> if this is the context of a structure.
	 */
	default boolean isStructureContext() {
		return getStructureContextOrNull() == this;
	}

	/**
	 * Same as {@link #getStructureContext()} returning <code>null</code> instead of throwing an
	 * exception.
	 */
	default StructuredElement getStructureContextOrNull() {
		StructuredElement context;

		StructuredElement parent = getParent();
		if (parent == null) {
			context = null;
		} else if (parent.isRoot()) {
			context = this;
		} else {
			context = parent.getStructureContext();
		}
		return context;
	}

}
