/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.List;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.UpdateWriter;
import com.top_logic.layout.basic.component.AJAXComponent;
import com.top_logic.layout.basic.component.ControlComponent;

/**
 * A {@link RevalidationVisitor} generates the necessary {@link ClientAction}s
 * that bring a client-side page in sync with the current server model.
 * 
 * <p>
 * The generation is performed by
 * {@link LayoutComponent#visitChildrenRecursively(LayoutComponentVisitor) recursively visiting}
 * a component tree starting with the {@link MainLayout} (see
 * {@link LayoutComponent#getMainLayout()}).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RevalidationVisitor extends DefaultDescendingLayoutVisitor {

    /**
	 * The {@link UpdateQueue} used to collect {@link ClientAction}s during a visit.
	 */
    private UpdateWriter actions;

    /**
	 * Generate a new {@link RevalidationVisitor} that generates
	 * {@link #getActions() revalidation actions} in the given
	 * {@link com.top_logic.layout.DisplayContext}. 
	 * 
	 * <p>
	 * Note: A {@link RevalidationVisitor} is not intended to be used more than
	 * once.
	 * </p>
     * @param actions the {@link UpdateWriter} to collect the {@link ClientAction}s
	 */
    public RevalidationVisitor(UpdateWriter actions) {
		this.actions = actions;
	}

    /**
	 * The list of {@link ClientAction actions} that was generated
	 *         during a visit.
	 */
    public UpdateWriter getActions() {
    	return actions;
    }
    
    /**
	 * General fallback for revalidating a {@link LayoutComponentVisitor} by
	 * reloading its document completely.
	 * 
	 * @see LayoutComponentVisitor#visitLayoutComponent(LayoutComponent)
	 */
	@Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
		if (! aComponent.isVisible()) {
			// Invisible components cannot be reloaded.
			return false;
		}
		
		// The following check is not necessary during an AJAX update, because
		// the component that sends the update is not being redrawn completely
		// by default. If a component cannot be incrementally updated, it must
		// be explicitly reloaded, even if it is the component that is currently
		// sending the AJAX update.
		//
		//	if (aComponent == current) {
		//		visitDialogs(aComponent);
		//
		//		// The component that issues the redraw does not need an
		//		// *explicit* reload, because it is the component for which the
		//		// browser already has requested a redraw.
		//
		//		aComponent.resetForReload();
		//		aComponent.markAsValid();
		//
		//		return false;
		//	}

		// The following checks isInvalid(), wasReloadScriptWritten() and the
		// creation of a reload action and marking the component as being
		// reloaded must be synchronized to prevent concurrent threads create
		// multiple updates for the same component.
		//
		boolean shouldVisitDialogs = false;
		boolean result;
		synchronized (aComponent) {
			if (aComponent.isInvalid()) {
				if (! aComponent.wasReloadScriptWritten()) {
					if (aComponent instanceof LayoutContainer) {
						aComponent.markAsValid();
						aComponent.resetForReload();
						return !(aComponent instanceof MainLayout);
					}
					shouldVisitDialogs = true;
					
					aComponent.resetForReload();
				}
				
				// Stop descending into child components, because theses are
				// automatically reloaded, if the browser requests a redraw for
				// the parent frame.
				result = false;
			} else {
				result = true;
			}
		}
		
		if (shouldVisitDialogs) {
			// Visiting the dialogs of the visited component must not be
			// done while being synchronized on the component, because this
			// would result in the acquisition of multiple nested locks and
			// it's not sure that these locks are properly ordered.
			visitDialogs(aComponent);
		}
		return result;
	}
	
	/**
	 * Revalidation functionality for {@link ControlComponent}s.
	 * 
	 * @see LayoutComponentVisitor#visitAJAXComponent(AJAXComponent)
	 */
	@Override
	public boolean visitAJAXComponent(AJAXComponent aComponent) {
		if (! aComponent.isVisible()) {
			// Invisible components cannot be reloaded.
			return false;
		}

//		int firstAddedActionIndex = actions.size();

		// Give the control component a chance to incrementally update its
		// display.  
		aComponent.revalidate(getActions().getDisplayContext(), actions);
		if (aComponent.isInvalid()) {
			return super.visitAJAXComponent(aComponent);
		}
		return true;
		
		
//		// If revalidation succeeded, the component is now valid.
//		if (aComponent.isInvalid()) {
//			// Drop potentially added revalidation actions.
//			for (int n = actions.size() - 1; n >= firstAddedActionIndex; n--)  {
//				actions.remove(n);
//			}
//			
//			// If revalidation did not succeed (e.g. because an explicit reload
//			// was requested), the component is reloaded by the default mechanism.
//			return super.visitAJAXComponent(aComponent);
//		} else {
//			int actionCount = actions.size();
//			if (firstAddedActionIndex < actionCount) {
//				// Add a context to the generated revalidation actions. 
//				String actionContext = LayoutUtils.createActionContext(aComponent);
//				for (int n = firstAddedActionIndex; n < actionCount; n++) {
//					ClientAction generatedAction = (ClientAction) actions.get(n);
//					generatedAction.setContext(actionContext);
//				}
//			}
//
//			// Descend to potential invalid children. Those must be revalidated,
//			// because the currently visited component is not being reloaded.
//			return true;
//		}
	}

	/**
	 * This method is called to unconditionally visit all dialogs of a
	 * component. This is necessary even for components that are reloaded
	 * themselves, because the client-side dialog representations are not
	 * children of the client-side representation of the reloaded component
	 * (dialogs are children of the outermost frame). Dialogs of a reloaded
	 * component therefore need a chance for an explicit reload.
	 */
	protected void visitDialogs(LayoutComponent aComponent) {
		visitAllRecursively(aComponent.getDialogs());
	}

	/**
	 * Helper method for explicitly descending into a list of components.
	 */
	protected final void visitAllRecursively(List components) {
		if (components == null) return;
		
		for (int cnt = components.size(), n = 0; n < cnt; n++) {
			((LayoutComponent) components.get(n)).acceptVisitorRecursively(this);
		}
	}

	/**
	 * Validates the given component using the given {@link UpdateWriter} to put the revalidation
	 * actions into.
	 * 
	 * @param component
	 *        the component to revalidate.
	 * @param writer
	 *        the {@link UpdateWriter} to deliver update actions to.
	 */
	public static void runValidation(LayoutComponent component, UpdateWriter writer) {
		RevalidationVisitor visitor =  new RevalidationVisitor(writer);
		component.acceptVisitorRecursively(visitor);
	}
}
