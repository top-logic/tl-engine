/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.Collection;

import org.w3c.dom.Document;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.common.webfolder.ui.NotExecutableListener;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link FormField} representing a single document with template proposal.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DocumentField extends CompositeField {

	/**
	 * Name of the internal {@link #getDocumentField()}.
	 */
	public static final String DOCUMENT_FIELD = "document";

	/**
	 * Name of the {@link #getTemplateField()}.
	 */
	public static final String TEMPLATE_FIELD = "template";

	/**
	 * Name of the {@link #getLockField()}.
	 */
	public static final String LOCK_FIELD = "lock";

	/**
	 * Name of the {@link #getUnlockField()}.
	 */
	public static final String UNLOCK_FIELD = "unlock";

	private static final ResourceView RESOURCES = I18NConstants.DOCUMENT_FIELD;

	/**
	 * Creates a {@link DocumentField}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param lockAction
	 *        The action to lock the document.
	 * @param unlockAction
	 *        The action to unlock the document.
	 */
	public DocumentField(String name, Command lockAction, Command unlockAction) {
		super(name, RESOURCES);

		DataField documentField = FormFactory.newDataField(DOCUMENT_FIELD);
		documentField.setCssClasses(FormConstants.FLEXIBLE_CSS_CLASS);
		addMember(documentField);

		DataField template = FormFactory.newDataField(TEMPLATE_FIELD);
		template.setCssClasses(FormConstants.FIXED_RIGHT_CSS_CLASS);
		addMember(template);
		template.setImmutable(true);

		CommandField lock = FormFactory.newCommandField(LOCK_FIELD, lockAction);
		lock.setLabel((String) null);
		lock.setImage(Icons.DOC_LOCKED);
		lock.setNotExecutableImage(Icons.DOC_LOCKED_DISABLED);
		lock.setCssClasses(FormConstants.FIXED_RIGHT_CSS_CLASS);
		addMember(lock);

		CommandField unlock = FormFactory.newCommandField(UNLOCK_FIELD, unlockAction);
		unlock.setLabel((String) null);
		unlock.setImage(Icons.DOC_UNLOCK);
		unlock.setNotExecutableImage(Icons.DOC_UNLOCK_DISABLED);
		unlock.setCssClasses(FormConstants.FIXED_RIGHT_CSS_CLASS);
		addMember(unlock);
		
		NotExecutableListener.createNotExecutableReasonKey(
			com.top_logic.common.webfolder.ui.I18NConstants.FIELD_DISABLED, lock, unlock).addAsListener(this);
	}

	/**
	 * The template document.
	 * 
	 * @see #getValue() The document content.
	 */
	public BinaryDataSource getTemplate() {
		return (BinaryDataSource) getTemplateField().getValue();
	}

	/**
	 * Sets the {@link #getTemplate()} property.
	 */
	public void setTemplate(BinaryDataSource dataItem) {
		FormField template = getTemplateField();
		if (dataItem != null) {
			template.setValue(dataItem);
			template.setVisible(true);
		} else {
			template.setValue(null);
			template.setVisible(false);
		}
	}

	@Override
	public Object getValue() {
		Object value = super.getValue();
		// Value of a datafield is a list of elements, also when the field is actually not multiple
		if (!getDocumentField().isMultiple() && value instanceof Collection<?>) {
			value = CollectionUtil.getSingleValueFrom(value);
		}
		return value;
	}

	@Override
	public void checkDependency() {
		getDocumentField().checkDependency();
	}

	@Override
	public boolean addDependant(FormField dependant) {
		return getDocumentField().addDependant(dependant);
	}

	@Override
	public boolean removeDependant(FormField dependant) {
		return getDocumentField().removeDependant(dependant);
	}

	/**
	 * The field holding the document value.
	 * 
	 * <p>
	 * Note: This {@link DocumentField} is a proxy for the internal document field.
	 * </p>
	 */
	public final DataField getDocumentField() {
		return (DataField) getMember(DOCUMENT_FIELD);
	}

	/**
	 * The field holding the template content.
	 */
	public final DataField getTemplateField() {
		return (DataField) getMember(TEMPLATE_FIELD);
	}

	/**
	 * The field holding the lock action.
	 */
	public final CommandField getLockField() {
		return (CommandField) getMember(LOCK_FIELD);
	}

	/**
	 * The field holding the unlock action.
	 */
	public final CommandField getUnlockField() {
		return (CommandField) getMember(UNLOCK_FIELD);
	}

	@Override
	protected FormField getProxy() {
		return getDocumentField();
	}

	@Override
	public ControlProvider getControlProvider() {
		ControlProvider result = super.getControlProvider();
		if (result == null) {
			return DocumentControlProvider.INSTANCE;
		}
		return result;
	}

	/**
	 * Creates the view for a {@link DocumentField}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class DocumentControlProvider implements ControlProvider {

		/**
		 * Singleton {@link DocumentControlProvider} instance.
		 */
		public static final DocumentControlProvider INSTANCE = new DocumentControlProvider();

		// @formatter:off
		private static final Document TEMPLATE = DOMUtil.parseThreadSafe(
				"<span"
	    	+		" xmlns='" + HTMLConstants.XHTML_NS + "'"
	    	+  		" xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
	    	+		" xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
	    	+		" class='" + "cDecoratedCell" + "'"
	    	+	">"
	    	+		"<p:field name='" + DocumentField.DOCUMENT_FIELD + "' />"
	    	+		"<p:field name='" + DocumentField.TEMPLATE_FIELD + "' />"
	    	+		"<p:field name='" + DocumentField.LOCK_FIELD + "' />"
	    	+		"<p:field name='" + DocumentField.UNLOCK_FIELD + "' />"
			+ 		"<p:field name='" + DocumentField.DOCUMENT_FIELD + "'"
			+			" style='" + FormTemplateConstants.STYLE_ERROR_VALUE + "' />"
	        +	"</span>");
		// @formatter:on

		private static FormTemplate newTemplate() {
			return new FormTemplate(I18NConstants.DOCUMENT_FIELD, DefaultFormFieldControlProvider.INSTANCE, false,
				TEMPLATE);
		}

		private DocumentControlProvider() {
			// Singleton constructor.
		}

		@Override
		public Control createControl(Object model, String style) {
			return new FormGroupControl(((FormGroup) model), newTemplate());
		}

	}

}
