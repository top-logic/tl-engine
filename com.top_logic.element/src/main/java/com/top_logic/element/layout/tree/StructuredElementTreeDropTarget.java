/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.tree;

import static com.top_logic.basic.util.Utils.*;
import static com.top_logic.element.structured.util.StructuredElementUtil.*;

import java.util.Collection;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.tree.dnd.BusinessObjectTreeDrop;
import com.top_logic.layout.tree.dnd.TreeDropEvent;
import com.top_logic.layout.tree.dnd.TreeDropEvent.Position;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link TreeDropTarget} for dropping a {@link StructuredElement}.
 * <p>
 * The {@link StructuredElement} is removed from its parents and added to the new tree at the target
 * location.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@InApp(classifiers = { "tree" })
@Label("Structure element drop")
public class StructuredElementTreeDropTarget extends BusinessObjectTreeDrop {

	/**
	 * Is executed in a {@link KnowledgeBase} transaction.
	 * 
	 * @param event
	 *        Is not allowed to be null.
	 */
	@Override
	protected void handleDrop(TreeDropEvent event, Collection<?> droppedObjects) {
		Position dropPosition = event.getPos();
		Object destinationObject = TLTreeModelUtil.getInnerBusinessObject(event.getRefNode());

		if (destinationObject instanceof StructuredElement) {
			try (Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				droppedObjects
					.forEach(droppedObject -> handleDropInternal(dropPosition, destinationObject, droppedObject));
				transaction.commit();
			}
		} else {
			showMessage(
				I18NConstants.DESTINATION_IS_NO_STRUCTURED_ELEMENT__DESTINATION.fill(getLabel(destinationObject)));
		}
	}

	private void handleDropInternal(Position dropPosition, Object destinationObject, Object droppedObject) {
		if (!(droppedObject instanceof StructuredElement)) {
			showMessage(I18NConstants.OBJECT_IS_NO_STRUCTURED_ELEMENT__OBJECT.fill(getLabel(droppedObject)));
			return;
		}
		StructuredElement droppedElement = (StructuredElement) droppedObject;
		if (droppedElement.isRoot()) {
			showMessage(I18NConstants.ROOT_CANNOT_BE_MOVED);
			return;
		}
		StructuredElement destinationElement = (StructuredElement) destinationObject;
		StructuredElement newParent = getParent(destinationElement, dropPosition);
		if (!isCompatibleChildInstance(newParent, droppedElement)) {
			showMessage(I18NConstants.WRONG_CHILD_TYPE__DROPPED_DESTINATION
				.fill(getLabel(droppedElement), getLabel(destinationElement)));
			return;
		}
		if (isInOwnSubtree(newParent, droppedElement)) {
			showMessage(I18NConstants.MOVE_INTO_OWN_SUBTREE__DROPPED_DESTINATION
				.fill(getLabel(droppedElement), getLabel(destinationElement)));
			return;
		}
		move(destinationElement, droppedElement, newParent, dropPosition);
	}

	/**
	 * Calculates and returns the new parent.
	 * <p>
	 * If this method is changed or overridden, getIndex(StructuredElement, Position) needs to be
	 * adapted, too.
	 * </p>
	 */
	protected StructuredElement getParent(StructuredElement target, Position position) {
		switch (position) {
			case ABOVE:
			case BELOW: {
				StructuredElement parent = target.getParent();
				if (parent == null) {
					return target;
				}
				return parent;
			}
			case ONTO:
			case WITHIN: {
				return target;
			}
			default: {
				throw new UnreachableAssertion("Missing case: " + debug(position));
			}
		}
	}

	/**
	 * Whether new dropped element can be a child of the new parent.
	 * 
	 * @param newParent
	 *        Is not allowed to be null.
	 * @param droppedElement
	 *        Is not allowed to be null.
	 */
	protected boolean isCompatibleChildInstance(StructuredElement newParent, StructuredElement droppedElement) {
		for (TLClass childType : newParent.getChildrenTypes()) {
			if (TLModelUtil.isCompatibleInstance(childType, droppedElement)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the dropped element was dropped into its own subtree.
	 * <p>
	 * I.e. whether the dropped element should be inserted as an (indirect) child of itself.
	 * </p>
	 */
	protected boolean isInOwnSubtree(StructuredElement newParent, StructuredElement droppedElement) {
		return newParent.equals(droppedElement) || getParents(newParent).contains(droppedElement);
	}

	/**
	 * Calculates and returns the index in the new parent.
	 * <p>
	 * If this method is changed or overridden, getParent(StructuredElement, Position) needs to be
	 * adapted, too.
	 * </p>
	 */
	protected int getIndex(StructuredElement target, Position position) {
		switch (position) {
			case ABOVE: {
				StructuredElement parent = target.getParent();
				if (parent == null) {
					return 0;
				}
				return parent.getChildren().indexOf(target);
			}
			case BELOW: {
				StructuredElement parent = target.getParent();
				if (parent == null) {
					return target.getChildren().size();
				}
				return parent.getChildren().indexOf(target) + 1;
			}
			case ONTO:
			case WITHIN: {
				return target.getChildren().size();
			}
			default: {
				throw new UnreachableAssertion("Missing case: " + debug(position));
			}
		}
	}

	/**
	 * Removes the dropped element from its current parent and adds it to the new parent.
	 * 
	 * @param destinationElement
	 *        Is not allowed to be null.
	 * @param droppedElement
	 *        Is not allowed to be null.
	 * @param newParent
	 *        Is not allowed to be null.
	 */
	protected void move(StructuredElement destinationElement, StructuredElement droppedElement, StructuredElement newParent, Position pos) {
		int newIndex = getIndex(destinationElement, pos);
		ResKey2 veto = checkVeto(droppedElement, newParent, newIndex);
		if (veto != null) {
			showMessage(veto.fill(droppedElement, newParent));
		}
		removeFromOldParent(droppedElement);
		newIndex = getIndex(destinationElement, pos);
		newParent.getChildrenModifiable().add(newIndex, droppedElement);
	}

	/**
	 * Hook for subclasses to prevent moves that would violate the business logic.
	 * <p>
	 * The types have already been checked when this method is called. I.e. whether the new parent
	 * is allowed to have the dropped element as a child.
	 * </p>
	 * 
	 * @param droppedElement
	 *        Never null.
	 * @param newParent
	 *        Never null.
	 * @param newIndex
	 *        The index at which the dropped element will be inserted.
	 * @return Null, if there is no veto. Otherwise, a {@link ResKey} to explain why that move is
	 *         not allowed. The {@link ResKey} gets two arguments: The dropped element and the new
	 *         parent.
	 */
	protected ResKey2 checkVeto(StructuredElement droppedElement, StructuredElement newParent, int newIndex) {
		return null;
	}

	private void removeFromOldParent(StructuredElement droppedElement) {
		if (droppedElement.getParent() != null) {
			droppedElement.getParent().getChildrenModifiable().remove(droppedElement);
		}
	}

	/** Returns the label of the given object. */
	protected String getLabel(Object object) {
		return MetaLabelProvider.INSTANCE.getLabel(object);
	}

	/**
	 * Show a message to the user, explaining why the move is not possible.
	 * 
	 * @param message
	 *        Never null.
	 */
	protected void showMessage(ResKey message) {
		InfoService.showInfo(message);
	}

}
