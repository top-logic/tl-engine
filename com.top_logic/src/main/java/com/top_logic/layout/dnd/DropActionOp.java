/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dnd;

import java.util.function.Function;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.tree.TreeData;

/**
 * The {@link ApplicationActionOp} for dropping an object with a TL-Script based drop handler.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class DropActionOp<C extends DropActionOp.DropAction> extends AbstractApplicationActionOp<C> {

	/** {@link ApplicationAction} for the {@link DropActionOp}. */
	public interface DropAction extends ApplicationAction {

		/** Property name of {@link #getDropView()}. */
		String DROP_VIEW = "drop-view";

		/** Property name of {@link #getDragView()}. */
		String DRAG_VIEW = "drag-view";

		/** Property name of {@link #getDropPosition()}. */
		String DROP_POSITION = "drop-position";

		/** Property name of {@link #getDroppedObject()}. */
		String DROPPED_OBJECT = "dropped-object";

		/**
		 * The view into which the object was dropped.
		 * <p>
		 * For example a {@link TableData} or {@link TreeData}.
		 * </p>
		 */
		@Name(DROP_VIEW)
		ModelName getDropView();

		/** @see #getDropView() */
		void setDropView(ModelName view);

		/**
		 * The view from which the object was dragged.
		 * <p>
		 * For example a {@link TableData} or {@link TreeData}. This is only used as the value
		 * context for recording and resolving the {@link #getDroppedObject()}. It is therefore not
		 * limited to {@link TableData} and {@link TreeData}. Everything that can be used as a value
		 * context is okay. If a value context is not necessary or should not be used, this is
		 * allowed to be null.
		 * </p>
		 */
		@Name(DRAG_VIEW)
		ModelName getDragView();

		/** @see #getDragView() */
		void setDragView(ModelName dragView);

		/** The business object that marks the drop position. */
		@Name(DROP_POSITION)
		ModelName getDropPosition();

		/** @see #getDropPosition() */
		void setDropPosition(ModelName dropPosition);

		/** The dropped business object. */
		@Name(DROPPED_OBJECT)
		ModelName getDroppedObject();

		/** @see #getDroppedObject() */
		void setDroppedObject(ModelName droppedObject);

		/**
		 * FIlls the {@link DropAction} with {@link ModelName} for the given values.
		 * 
		 * @param view
		 *        See: {@link #getDropView()}
		 * @param dragView
		 *        See: {@link #getDragView()}
		 * @param dropPosition
		 *        See: {@link #getDropPosition()}
		 * @param dragData
		 *        Algorithm computing from the name of the drag view a name for the dragged objects.
		 *        See {@link DropEvent#getDragDataName()}.
		 */
		static void fill(DropAction action, Object view, Object dragView, Object dropPosition,
				Function<ModelName, ModelName> dragData) {
			action.setDropView(ModelResolver.buildModelName(view));
			/* Don't use the dragView when it cannot be recorded. This should rarely be the case.
			 * But if it is the case, it is better to use only value context free NamingSchemes for
			 * the dropped object than failing to record anything. */
			Maybe<? extends ModelName> dragViewName = ModelResolver.buildModelNameIfAvailable(dragView);
			if (dragViewName.hasValue()) {
				action.setDragView(dragViewName.get());
			}
			action.setDroppedObject(dragData.apply(dragViewName.getElse(null)));
			action.setDropPosition(ModelResolver.buildModelName(view, dropPosition));
		}

	}

	/** {@link TypedConfiguration} constructor for {@link DropActionOp}. */
	public DropActionOp(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) {
		Object dropView = context.resolve(getConfig().getDropView());
		Object dragView = context.resolve(getConfig().getDragView());
		Object dropPosition = context.resolve(getConfig().getDropPosition(), dropView);
		Object droppedObject = context.resolve(getConfig().getDroppedObject(), dragView);
		processDrop(context, dropView, dropPosition, droppedObject);
		return argument;
	}

	/** The parameters are the resolved values from the {@link DropAction}. */
	protected abstract void processDrop(ActionContext context, Object dropView, Object dropPosition, Object droppedObject);

}
