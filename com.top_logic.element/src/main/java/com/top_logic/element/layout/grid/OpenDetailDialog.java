/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * {@link Command} opening an object in a dialog (containing an edit component)
 * and activates the edit mode.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class OpenDetailDialog extends AbstractCommandModel {

    private final FormContainer row;
    protected final GridComponent grid;

    public OpenDetailDialog(GridComponent aGrid, FormContainer row) {
		this.grid = aGrid;
		this.row = row;
    }

    @Override
    public String toString() {
		return this.getClass().getSimpleName() + " "
			+ (getEditComponentName() == null ? "" : getEditComponentName().qualifiedName() + ' ') + "in " + grid.getName()
			+ " with " + getObject();
    }

	@Override
	protected HandlerResult internalExecuteCommand(DisplayContext context) {
        Object selected = this.grid.getSelected();
		Collection<?> selection;
		if (selected instanceof Collection) {
			selection = (Collection<?>) selected;
		} else {
			// Grid is in single selection mode
			selection = CollectionUtil.singletonOrEmptySet(selected);
		}
		if (!selection.isEmpty()) {
            // implies EditComponenent.releaseTokenContext
			HandlerResult result =
				this.grid.storeRowValues(selection, AbstractApplyCommandHandler.warningsDisabledTemporarily());
			if (!result.isSuccess()) {
				return result;
            }
        }

        LayoutComponent theGoto = this.gotoLayout();

        if (theGoto != null) {
            this.prepareComponent(context, theGoto);
        }

        return HandlerResult.DEFAULT_RESULT;
    }

    /** 
     * Remove TokenContext from {@link #grid} and care for aGoto.{@link GridComponent#openInEdit()}.
     * @param    aGoto        The component displaying the object, never <code>null</code>.
     */
    public void prepareComponent(DisplayContext aContext, final LayoutComponent aGoto) {
        
        if (!this.grid.openInEdit()) {
            this.grid.validateModel(aContext); // validate here to avoid #5474
        } else if ((aGoto instanceof EditComponent)) {
            EditComponent theEdit = (EditComponent) aGoto;

            // set edit mode if item is not finished
            // should use edit command handler of component to do the switch
            // but the handler is not accessible
            if (this.checkAllowEdit(this.grid, this.getObject())) {
                if (!theEdit.isInEditMode()) {
                    theEdit.setEditMode();
                }
            }
            else {
                if (!theEdit.isInViewMode()) {
                    theEdit.setViewMode();
                }
            }
        } else {
           
        }
        Object selected = this.grid.getSelected();
        if (selected != null) {
			if (this.grid.getLockHandler().hasLock()) {
				AttributeFormContext theContext = grid.getFormContext();
                FormGroup                theGroup     = this.grid.getRowGroup(selected);
                AttributeUpdateContainer theContainer = theContext.getAttributeUpdateContainer();
                
                this.grid.removeFields(selected, theGroup, theContainer);
            }
        }
    }

    /**
	 * Jump to the layout defined in this class and display the defined object.
	 * 
	 * @return The component now displaying the object, may be <code>null</code> when goto was not
	 *         successful.
	 * 
	 * @see GotoHandler#gotoLayout(LayoutComponent, Object, ComponentName)
	 * @see #canShow()
	 */
    public LayoutComponent gotoLayout() {
		return grid.gotoTarget(this.getObject(), this.getEditComponentName());
    }

	/**
	 * Checks whether {@link #gotoLayout()} may be successful.
	 * 
	 * @see #gotoLayout()
	 */
	public boolean canShow() {
		LayoutComponent component;
		ComponentName editComponentName = getEditComponentName();
		if (editComponentName == null) {
			component = null;
		} else {
			component = grid.getMainLayout().getComponentByName(editComponentName);
		}
		return GotoHandler.canShow(getObject(), component);
	}

    /** 
     * Delegate the question of allow edit to {@link GridComponent#allowEdit(Object)}.
     * 
     * @param    aComp     The grid component to check, must not be <code>null</code>.
     * @param    rowObject    The model to be checked, may be <code>null</code>.
     */
    protected boolean checkAllowEdit(GridComponent aComp, Object rowObject) {
        return aComp.allowEdit(rowObject);
    }

	protected Object getObject() {
		return row.get(GridComponent.PROP_ATTRIBUTED);
	}

	/**
	 * @see GridComponent#getEditComponentName()
	 */
	protected ComponentName getEditComponentName() {
		return grid.getEditComponentName();
	}

	/**
	 * Initialises certain properties of this {@link CommandModel}.
	 */
	public void initialize(DisplayContext context) {
		setImage(Icons.OPEN_DETAIL);
		setNotExecutableImage(Icons.OPEN_DETAIL_DISABLED);
		setLabel(context.getResources().getString(I18NConstants.GRID_GOTO));
		if (!canShow()) {
			setNotExecutable(I18NConstants.NO_GOTO_NO_DEFAULT_LAYOUT);
		}
	}

	/**
	 * {@link ModelNamingScheme} for {@link OpenDetailDialog} commands that are created during rendering.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Naming extends AbstractModelNamingScheme<OpenDetailDialog, Naming.Name> {
		
		/**
		 * {@link ModelName} for {@link OpenDetailDialog} commands in the {@link Naming} scheme.
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		public interface Name extends ModelName {

			/**
			 * Description of the {@link OpenDetailDialog#row row} the detail dialog is opened for.
			 */
			ModelName getRow();
			
			/**
			 * @see #getRow()
			 */
			void setRow(ModelName referenceValue);

			/**
			 * Description of the {@link OpenDetailDialog#grid component} displaying the table the
			 * detail dialog is opened for.
			 */
			ModelName getComponent();
			
			/**
			 * @see #getComponent()
			 */
			void setComponent(ModelName referenceValue);

		}

		@Override
		public Class<Name> getNameClass() {
			return Name.class;
		}

		@Override
		public Class<OpenDetailDialog> getModelClass() {
			return OpenDetailDialog.class;
		}

		@Override
		public OpenDetailDialog locateModel(ActionContext context, Name name) {
			GridComponent grid = (GridComponent) context.resolve(name.getComponent());
			FormGroup row = (FormGroup) context.resolve(name.getRow());
			OpenDetailDialog command = new OpenDetailDialog(grid, row);
			command.initialize(context.getDisplayContext());
			return command;
		}

		@Override
		protected void initName(Name name, OpenDetailDialog model) {
			name.setRow(ModelResolver.buildModelName(model.row));
			name.setComponent(ModelResolver.buildModelName(model.grid));
		}
	}
	
}

