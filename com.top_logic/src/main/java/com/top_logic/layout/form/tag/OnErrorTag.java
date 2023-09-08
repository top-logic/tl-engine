/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.util.Iterator;

import javax.servlet.jsp.tagext.Tag;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.HasErrorChanged;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.model.FormContext;

/**
 * {@link Tag} for conditionally rendering parts of a form.
 * 
 * <p>
 * The visibility of the body of this view depends on the existance of an error
 * in the referenced {@link FormMember}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OnErrorTag extends CompositeControlTag {
	
	private final class ErrorListener implements HasErrorChanged {
		private final FormMember targetMember;

		private final BlockControl onErrorDisplay;

		ErrorListener(FormMember targetMember, BlockControl onErrorDisplay) {
			this.targetMember = targetMember;
			this.onErrorDisplay = onErrorDisplay;
		}

		@Override
		public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
			onErrorDisplay.setVisible(currentVisibility(targetMember));
			return Bubble.BUBBLE;
		}
	}

	private String name;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	@Override
	protected Control createControl() {
		final FormMember targetMember = 
			FormContext.getMemberByRelativeName(FormTagUtil.findParentFormContainer(this), name);

		final BlockControl result = new BlockControl() {
			private ErrorListener errorListener = new ErrorListener(targetMember, this);

			@Override
			protected void attachRevalidated() {
				super.attachRevalidated();
				targetMember.addListener(FormField.HAS_ERROR_PROPERTY, errorListener);
			}
			
			@Override
			protected void detachInvalidated() {
				super.detachInvalidated();
				targetMember.removeListener(FormField.HAS_ERROR_PROPERTY, errorListener);
			}
		};
		
		initCompositeControl(result);
		result.setVisible(currentVisibility(targetMember));
		
		return result;
	}

	private static boolean currentVisibility(final FormMember targetMember) {
		boolean hasError;
		if (targetMember instanceof FormField) {
			hasError = ((FormField) targetMember).hasError();
		} else {
			hasError = false;
			for (Iterator it = ((FormContainer) targetMember).getDescendantFields(); it.hasNext(); ) {
				if (((FormField) it.next()).hasError()) {
					hasError = true;
					break;
				}
			}
		}
		return hasError;
	}
}
