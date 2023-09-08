/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRuleManager;

/**
 * Handler for generically deleting {@link TLObject persistent objects}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Delete command")
public class GenericDeleteCommandHandler extends AbstractCommandHandler {

	/** Config interface for {@link GenericDeleteCommandHandler}. */
	public interface Config extends AbstractCommandHandler.Config, WithCloseDialog {

		@Override
		@FormattedDefault("class.com.top_logic.layout.form.values.edit.editor.I18NConstants.DELETE_OBJECT")
		ResKey getResourceKey();

		@Override
		@FormattedDefault("theme:ICONS_DELETE_MENU")
		ThemeImage getImage();

		@Override
		@FormattedDefault("theme:ICONS_DELETE_MENU_DISABLED")
		ThemeImage getDisabledImage();

		@Override
		@BooleanDefault(BoundCommand.NEEDS_CONFIRM)
		boolean getConfirm();

		@Override
		@FormattedDefault(CommandHandlerFactory.DELETE_CLIQUE)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.DELETE_NAME)
		CommandGroupReference getGroup();

	}

	/**
	 * The ID this handler is/must referenced in the {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "genericDelete";

	private boolean _closeDialog;
	
    /**
     * Creates a {@link GenericDeleteCommandHandler}.
     */
    public GenericDeleteCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
		_closeDialog = config.getCloseDialog();
    }
    
    @Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		if (model == null) {
			return HandlerResult.error("object.selection", GenericDeleteCommandHandler.class);
        }
        
		tryExecute(component, model);

		HandlerResult result = new HandlerResult();
		result.setCloseDialog(_closeDialog);
		return result;
	}

	private void tryExecute(LayoutComponent component, Object model) throws DataObjectException {
		boolean locked = lock(component);
		try {
			withEditLock(component, model);
		} finally {
			if (locked) {
				unlock(component);
			}
		}
	}

	private void withEditLock(LayoutComponent component, Object model)
			throws DataObjectException, KnowledgeBaseException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			inTransaction(component, model);
			tx.commit();
		}
	}

	private void inTransaction(LayoutComponent component, Object model) throws DataObjectException {
		if (model instanceof Collection<?>) {
			deleteObjects(component, CollectionUtil.dynamicCastView(TLObject.class, (Collection<?>) model));
		} else {
			deleteObject(component, (TLObject) model);
		}
	}

	private boolean lock(LayoutComponent aComponent) {
		if (!(aComponent instanceof EditMode)) {
			return false;
		}

		EditMode editor = (EditMode) aComponent;
		if (editor.isInEditMode()) {
			return false;
		}

		editor.setEditMode();
		return editor.isInEditMode();
	}

	private void unlock(LayoutComponent aComponent) {
		if (!(aComponent instanceof EditMode)) {
			return;
		}

		EditMode editor = (EditMode) aComponent;
		if (!editor.isInEditMode()) {
			return;
		}

		editor.setEditMode(false);
	}

	/**
	 * Actually perform the deletion.
	 * 
	 * @param component
	 *        The context component.
	 * @param element
	 *        The object to be deleted.
	 * @throws DataObjectException
	 *         If deletion fails.
	 * 
	 * @see #deleteObjects(LayoutComponent, Collection)
	 */
	protected void deleteObject(LayoutComponent component, TLObject element) throws DataObjectException {
		element.tDelete();
	}

	/**
	 * Actually perform the deletion of multiple elements.
	 * 
	 * @param component
	 *        The context component.
	 * @param elements
	 *        The objects to be deleted.
	 * @throws DataObjectException
	 *         If deletion fails.
	 * @see #deleteObject(LayoutComponent, TLObject)
	 */
	protected void deleteObjects(LayoutComponent component, Collection<? extends TLObject> elements)
			throws DataObjectException {
		KBUtils.deleteAll(elements);
	}

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.GENERIC_DELETE;
    }

    @Override
	protected ExecutabilityRule intrinsicExecutability() {
		return ExecutabilityRuleManager.getRule(ExecutabilityRuleManager.KEY_GENERAL_DELETE);
    }

	@Override
	protected ResKey getDefaultConfirmKey(LayoutComponent component, Map<String, Object> arguments,
			Object targetModel) {
		return CommandHandlerUtil.defaultDeletionConfirmKey(targetModel);
	}

}
