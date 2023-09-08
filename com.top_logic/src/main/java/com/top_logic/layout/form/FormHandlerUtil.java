/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.util.Collection;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.component.VisibleComponentFilter;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.ComponentCollector;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link FormHandlerUtil} is a static class of util methods for {@link FormHandler}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormHandlerUtil {
	
	/** a {@link Filter} to use in a {@link ComponentCollector} */
	private static final Filter<LayoutComponent> CHANGE_CHECK_FILTER = new Filter<>() {

		/**
		 * Assumes that the given Object is a {@link LayoutComponent} and accepts it iff it is
		 * visible, is necessary for change handling, implements {@link ChangeHandler}, and has an
		 * discard command.
		 * 
		 * If the form has no {@link ChangeHandler#getDiscardClosure()}, the form must not have an
		 * {@link ChangeHandler#getApplyClosure()} so the changes can neither be applied nor
		 * discarded so the change dialog does not make sense.
		 * 
		 * @see Filter#accept(Object)
		 */
		@Override
		public boolean accept(LayoutComponent anObject) {
			LayoutComponent component = anObject;
			
			if (!component.isVisible() || !component.getUseChangeHandling()) {
				return false;
			}
			
			return anObject instanceof ChangeHandler && ((ChangeHandler) anObject).getDiscardClosure() != null;
		}
		
	};

	private static final Property<Integer> DEACTIVATED = TypedAnnotatable.property(Integer.class, "deactivated");

	/**
	 * Sets the immutable state of the given {@link FormHandler}.
	 * 
	 * <p>
	 * This method only operates on {@link FormHandler}s which have a {@link FormContext}. Multiple
	 * calls of this method will be covered, i.e. calling this method twice with
	 * <code>immutable == true</code> needs to call this method twice with
	 * <code>immutable == false</code> to get a not immutable context (if no one else changes the
	 * immutable state of the context).
	 * </p>
	 * 
	 * @param formHandler
	 *        must not be <code>null</code>
	 * @param immutable
	 *        whether the {@link FormContext} must be immutable
	 * @return whether this method changed the immutable state of the {@link FormContext}
	 */
	public static boolean setImmutable(FormHandler formHandler, boolean immutable) {
		if (!formHandler.hasFormContext()) {
			return false;
		}

		FormContext theContext = formHandler.getFormContext();

		if (theContext == null) {
			return false;
		}

		Integer coveringCount = theContext.get(FormHandlerUtil.DEACTIVATED);

		if (immutable) {
			if (coveringCount != null) {
				// The context was already deactivated by another thing.
				int newCoveringDialogCount = coveringCount.intValue() + 1;
				theContext.set(FormHandlerUtil.DEACTIVATED, Integer.valueOf(newCoveringDialogCount));
				return false;
			}

			if (theContext.isImmutable()) {
				// The context is by definition immutable.
				return false;
			}

			theContext.setImmutable(true);
			theContext.set(FormHandlerUtil.DEACTIVATED, Integer.valueOf(1));
			return true;
		} else {
			if (coveringCount == null) {
				// The context was not deactivated.
				return false;
			}

			int newCoveringDialogCount = coveringCount.intValue() - 1;
			if (newCoveringDialogCount > 0) {
				// The context is still deactivated by something.
				theContext.set(FormHandlerUtil.DEACTIVATED, Integer.valueOf(newCoveringDialogCount));
				return false;
			}

			theContext.reset(FormHandlerUtil.DEACTIVATED);
			theContext.setImmutable(false);
			return true;
		}
	}
	
	/**
	 * a {@link ComponentCollector} which has
	 *         {@link #getNeedsChangeCheckFilter() a filter} to accept
	 *         {@link LayoutComponent}s and descents as long as the current
	 *         component is visible
	 */
	public static final ComponentCollector createNeedsChangeCheckVisitor() {
		return new ComponentCollector(getNeedsChangeCheckFilter(), VisibleComponentFilter.INSTANCE);
	}
	
	/**
	 * Returns a {@link Filter} which can be used to filter a {@link Collection}
	 * of {@link LayoutComponent}s to get only those components which needs a
	 * change check.
	 */
	public static final Filter<LayoutComponent> getNeedsChangeCheckFilter() {
		return CHANGE_CHECK_FILTER;
	}

}
