/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Map;

import com.top_logic.base.locking.handler.LockHandler;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.edit.CanLock;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRuleManager;
import com.top_logic.util.error.TopLogicException;

/**
 * Base class for {@link CommandHandler}s deleting objects.
 * 
 * @see #deleteObject(LayoutComponent, Object, Map)
 * 
 * @implNote
 *           <p>
 *           Subclasses are expected to implement
 *           {@link #deleteObject(LayoutComponent, Object, Map)}.
 *           </p>
 * 
 *           <p>
 *           The overall process is as follows:
 *           </p>
 * 
 *           <ul>
 *           <li>Update or acquire a lock for the deletion, see {@link LockHandler#updateLock()}, if
 *           the component supports locking.</li>
 *           <li>{@link #beginTransaction(Object)}</li>
 *           <li>{@link #deleteObject(LayoutComponent, Object, Map)}</li>
 *           <li>{@link #commit(Transaction, Object)}</li>
 *           <li>{@link #updateComponent(LayoutComponent, Object)}</li>
 *           </ul>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractDeleteCommandHandler extends AbstractCommandHandler implements TransactionHandler {

	/** Config interface for {@link AbstractDeleteCommandHandler}. */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@BooleanDefault(true)
		boolean getConfirm();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.DELETE_NAME)
		CommandGroupReference getGroup();

	}

	/**
	 * {@link DisplayContext} property to prevent closing the open dialog after deletion.
	 */
	public static final Property<Boolean> PREVENT_DIALOG_CLOSE =
		TypedAnnotatable.property(Boolean.class, "preventDialogClose", Boolean.FALSE);

    /** 
     * Create a Command with {@link SimpleBoundCommandGroup#DELETE} 
     */
    public AbstractDeleteCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
	 * Deletes the given model.
	 * 
	 * <p>
	 * Subclasses should override this method to implement the actual delete handling.
	 * </p>
	 * 
	 * <p>
	 * Note: This method is not abstract for compatibility with legacy code. When overriding this
	 * method, do <b>not</b> call the super implementation.
	 * </p>
     * @param model
	 *        The current model of the calling component, may be <code>null</code>.
	 */
	protected void deleteObject(LayoutComponent component, Object model, Map<String, Object> arguments) {
		// For compatibility with legacy API:
		try {
			FormContext formContext =
				component instanceof FormHandler ? ((FormHandler) component).getFormContext() : null;

			if (!deleteObject(formContext, model, component, arguments)) {
				failCannotDelete();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Deletes the given elements.
	 * 
	 * <p>
	 * Subclasses should override this method to implement the implement a more performant delete
	 * handling.
	 * </p>
	 * 
	 * @param elements
	 *        The elements to delete.
	 */
	protected void deleteObjects(LayoutComponent component, Iterable<?> elements, Map<String, Object> arguments) {
		elements.forEach(element -> deleteObject(component, element, arguments));
	}

    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		// When possible, allocate a token context before delete.
		if (component instanceof EditMode) {
			EditMode editor = (EditMode) component;
			if (editor.isInViewMode()) {
				editor.setEditMode();
				boolean success = editor.isInEditMode();
				if (!success) {
					throw new TopLogicException(I18NConstants.ERROR_CANNOT_LOCK_OBJECT);
				}
				try {
					// Note: The token context must still be updated, even if the component was
					// switched to edit mode just before: A grid can be switched to edit mode, even
					// if the the currently selected row cannot be locked, since changing the row
					// selection keeps the edit mode.
					if (editor instanceof CanLock) {
						((CanLock) editor).getLockHandler().updateLock();
					}
					doApplyChanges(component, model, arguments);
				} finally {
					editor.setViewMode();
				}
			} else {
				if (editor instanceof CanLock) {
					((CanLock) editor).getLockHandler().updateLock();
				}
				doApplyChanges(component, model, arguments);
			}
		} else {
			doApplyChanges(component, model, arguments);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private void doApplyChanges(LayoutComponent component, Object model, Map<String, Object> arguments) {
		try (Transaction tx = beginTransaction(model)) {
			if (model instanceof Iterable) {
				deleteObjects(component, (Iterable<?>) model, arguments);
			} else {
				deleteObject(component, model, arguments);
			}

			commit(tx, model);

			updateComponent(component, model);
		}
    }

	/**
	 * Aborts execution with a generic (not very helpful) error message.
	 * 
	 * @deprecated Replace with custom error handling explaining the reason.
	 */
	@Deprecated
	protected final TopLogicException failCannotDelete() {
		throw new TopLogicException(I18NConstants.ERROR_CANNOT_DELETE);
	}

	/**
	 * The {@link KnowledgeBase} to perform the commit in.
	 * 
	 * @param model
	 *        The component model being deleted.
	 */
	protected KnowledgeBase getKnowledgeBase(Object model) {
		return (model instanceof TLObject) ? ((TLObject) model).tKnowledgeBase() : null;
	}

	/** 
     * Close aComp when {@link FormComponent#openedAsDialog()}.
     * 
     * Subclasses may suppress this behavior by overriding this method.
     */
	protected void closeDialog(LayoutComponent aComp) {
        if(aComp.openedAsDialog()) {
			if (DefaultDisplayContext.getDisplayContext().get(PREVENT_DIALOG_CLOSE)) {
				return;
			}
			aComp.closeDialog();
        }
    }

    @Override
	public ResKey getDefaultI18NKey() {
		return I18NConstants.DELETE;
    }

    /**
     * Update the given component, if the deletion succeeds.
     * @param    component    The component deleting the model, must not be <code>null</code>.
     * @param    model        The model deleted (no longer valid!), must not be <code>null</code>.
     */
	protected void updateComponent(LayoutComponent component, Object model) {
		// Note: The deleted event must be sent *before* setting the new object
		// as model, because the corresponding components must have a chance to 
		// remove the deleted object from their internal state, before a new 
		// object gets set as current object (which normally causes this object
		// to be selected). As the persistent events are sent via the ModelEventForwarder it is
		// necessary to process the global events.
		component.getMainLayout().processGlobalEvents();

		closeDialog(component);
    }

    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return ExecutabilityRuleManager.getRule(ExecutabilityRuleManager.KEY_GENERAL_DELETE);
    }

	/**
	 * @deprecated Implement {@link #deleteObject(LayoutComponent, Object, Map)}. Note: The
	 *             {@link FormContext} can be retrieved from the component, if necessary.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected boolean deleteObject(FormContext aContext, Object aModel) throws Exception {
		return true;
	}

	/**
	 * @deprecated Implement {@link #deleteObject(LayoutComponent, Object, Map)}. Note: The
	 *             {@link FormContext} can be retrieved from the component, if necessary.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected boolean deleteObject(FormContext aContext, LayoutComponent aComponent, Object aModel) throws Exception {
		return deleteObject(aContext, aModel);
	}

	/**
	 * @deprecated Implement {@link #deleteObject(LayoutComponent, Object, Map)}. Note: The
	 *             {@link FormContext} can be retrieved from the component, if necessary.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected boolean deleteObject(FormContext aContext, Object aModel, LayoutComponent aComponent, Map someArguments)
			throws Exception {
		return deleteObject(aContext, aComponent, aModel);
	}

}
