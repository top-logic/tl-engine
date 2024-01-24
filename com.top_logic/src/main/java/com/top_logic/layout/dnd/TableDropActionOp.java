/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dnd;

import static com.top_logic.basic.util.Utils.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.dnd.BusinessObjectTableDrop;
import com.top_logic.layout.table.dnd.TableDropTarget;

/**
 * The {@link ApplicationActionOp} for dropping an object in a {@link TableData table}.
 * <p>
 * The drop handler has to be a {@link BusinessObjectTableDrop}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableDropActionOp extends DropActionOp<TableDropActionOp.TableDropAction> {

	/** {@link ApplicationAction} for the {@link TableDropActionOp}. */
	public interface TableDropAction extends DropActionOp.DropAction {

		@Override
		@ClassDefault(TableDropActionOp.class)
		Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

		/**
		 * Builds a {@link TableDropAction}.
		 * <p>
		 * See
		 * {@link DropActionOp.DropAction#fill(DropActionOp.DropAction, Object, Object, Object, Function)}
		 * for the parameters.
		 * </p>
		 */
		static TableDropAction create(TableData dropView, Object dragView, Object dropPosition,
				Function<Object, ModelName> function) {
			TableDropAction action = TypedConfiguration.newConfigItem(TableDropAction.class);
			DropActionOp.DropAction.fill(action, dropView, dragView, dropPosition, function);
			return action;
		}

	}

	/** {@link TypedConfiguration} constructor for {@link TableDropActionOp}. */
	public TableDropActionOp(InstantiationContext context, TableDropAction config) {
		super(context, config);
	}

	@Override
	protected void processDrop(ActionContext context, Object dropView, Object dropPosition, Object droppedObject) {
		TableData dropTable = (TableData) dropView;
		TableDropTarget dropHandler = dropTable.getDropTarget();
		processDrop(dropTable, dropHandler, dropPosition, droppedObject);
	}

	/** Performs the drop into the given table. */
	protected void processDrop(TableData dropTable, TableDropTarget javaDropHandler, Object dropPosition,
			Object droppedObject) {
		BusinessObjectTableDrop tlScriptDropHandler = assertTLScriptDrop(javaDropHandler);
		assertDropEnabled(dropTable, tlScriptDropHandler);
		List<?> droppedObjects = CollectionUtilShared.asList(droppedObject);
		assertDropPossible(tlScriptDropHandler, dropPosition, droppedObjects);

		tlScriptDropHandler.handleDrop(droppedObjects, dropPosition);
	}

	private BusinessObjectTableDrop assertTLScriptDrop(TableDropTarget dropHandler) {
		if (dropHandler instanceof BusinessObjectTableDrop) {
			return (BusinessObjectTableDrop) dropHandler;
		}
		throw fail("The drop handler has to be TL-Script based, i.e. it has to be a "
			+ BusinessObjectTableDrop.class.getSimpleName() + ". But it is: " + debug(dropHandler));
	}

	private void assertDropEnabled(TableData dropTable, BusinessObjectTableDrop dropHandler) {
		if (!dropHandler.dropEnabled(dropTable)) {
			fail("Dropping is here not possible. Context:" + dropTable.getOwner());
		}
	}

	private void assertDropPossible(BusinessObjectTableDrop dropHandler, Object dropPosition,
			Collection<?> droppedObjects) {
		if (!dropHandler.canDrop(droppedObjects, dropPosition)) {
			fail("The object cannot be droppped here."
				+ " Dropped object: " + droppedObjects + ". Drop position: " + dropPosition);
		}
	}

}
