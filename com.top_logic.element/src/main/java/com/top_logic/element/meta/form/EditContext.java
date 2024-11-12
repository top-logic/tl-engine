/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdate.DefaultStorageAlgorithm;
import com.top_logic.element.meta.AttributeUpdate.StoreAlgorithm;
import com.top_logic.element.meta.AttributeUpdate.UpdateCheck;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.form.overlay.FormObjectOverlay;
import com.top_logic.element.meta.form.overlay.ObjectCreation;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.element.meta.form.tag.TagProviderAnnotation;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormContextStoreAlgorithm;
import com.top_logic.model.form.EditContextBase;

/**
 * Description of an atomic model edit operation such as editing a single attribute of some object.
 * 
 * <p>
 * A form presented to the user is composed of multiple edit contexts each of them representing a
 * single input field.
 * </p>
 * 
 * <p>
 * The process of editing a persistent object through a form allowing user input is as follows:
 * </p>
 * 
 * <ul>
 * <li>An {@link AttributeFormContext} is created.</li>
 * 
 * <li>For each attribute of the edited object:
 * <ul>
 * <li>A {@link FieldProvider} is determined from the attribute's configuration, see
 * {@link FieldProviderAnnotation}.</li>
 * <li>The {@link FieldProvider} creates a form field through
 * {@link FieldProvider#getFormField(EditContext, String)}. This process includes the initialization
 * of the created field with the base value of the attribute, see
 * {@link AbstractFieldProvider#initValue(EditContext, com.top_logic.layout.form.FormMember)}. By
 * default, this process takes the value to edit from {@link EditContext#getCorrectValues()},
 * potentially converts it to a value that can be displayed in a form field by
 * {@link AttributeFormFactory#toFieldValue(EditContext, com.top_logic.layout.form.FormField, Object)},
 * and initializes the field value through {@link FormField#initializeField(Object)}.</li>
 * <li>A {@link DisplayProvider} is determined from the attribute's configuration, see
 * {@link TagProviderAnnotation}</li>
 * <li>The {@link DisplayProvider} creates a {@link Control} through
 * {@link DisplayProvider#createDisplay(EditContext, com.top_logic.layout.form.FormMember)} for the
 * attribute's {@link FormMember} created in the previous step.</li>
 * <li>The attribute's control is rendered to the UI, interacts with the browser and updates the
 * form field's value based on user input.</li>
 * </ul>
 * </li>
 * 
 * <li>The user requests to save the form values.</li>
 * 
 * <li>The input is checked for consistency by calling {@link FormContext#checkAll()}. This in turn
 * performs the following steps for all edited fields:
 * <ul>
 * <li>The {@link FormField}'s input is checked for syntactical errors.</li>
 * <li>Potential dependencies that are attached to {@link FormField}s are checked.</li>
 * <li>The {@link FormField}s new value is converted through
 * {@link AttributeUpdate#fieldToAttributeValue(FormField)} to a value suitable for storage.</li>
 * <li>The converted value is set to the {@link EditContext#getCorrectValues()} and the
 * {@link EditContext} is marked as {@link AttributeUpdate#isChanged() changed}, if the new value
 * differs from the value the edit operation started with.</li>
 * <li>The {@link UpdateCheck} {@link AttributeUpdate#setUpdateCheck(UpdateCheck) registered} for
 * the {@link EditContext} is {@link UpdateCheck#checkUpdate(AttributeUpdate) tested}.</li>
 * </ul>
 * </li>
 * 
 * <li>A transaction is started in the persistency layer.</li>
 * <li>The new values from {@link AttributeUpdate#isChanged() changed} {@link EditContext}s are
 * stored back to the persistency layer by a call to {@link FormContext#store()}. This in turn
 * performs the following steps on each field:
 * <ul>
 * <li>All {@link FormContextStoreAlgorithm}s
 * {@link FormContext#addStoreAlgorithm(FormContextStoreAlgorithm) registered} in the context are
 * called.</li>
 * <li>All object creations (objects transiently allocated during the edit operation, e.g. new
 * composite values) are performed: {@link ObjectCreation#create()}.</li>
 * <li>All updates are performed:
 * {@link FormObjectOverlay#store(com.top_logic.element.meta.AttributeUpdateContainer)}. This calls
 * the {@link StoreAlgorithm} for all {@link EditContext}s, see
 * {@link EditContext#setStoreAlgorithm(StoreAlgorithm)}. The storage algorithm by default calls
 * {@link StorageImplementation#update(AttributeUpdate)} on the storage implementation of the
 * attribute, which in turn calls
 * {@link StorageImplementation#setAttributeValue(com.top_logic.model.TLObject, com.top_logic.model.TLStructuredTypePart, Object)}
 * with the {@link EditContext#getCorrectValues() updated value} from the {@link EditContext}. This
 * method is finally responsible to perform an operation on the persistent object, e.g. setting a
 * value on the underlying {@link KnowledgeObject}, see
 * {@link KnowledgeObject#setAttributeValue(String, Object)}.</li>
 * <li>All deletions (objects marked for deletion during the edit operation, e.g. removed composite
 * values) are performed: {@link Modification#execute()}.</li>
 * </ul>
 * </li>
 * 
 * <li>The transaction in the persistency layer is committed.</li>
 * <li>The UI is updated.</li>
 * </ul>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface EditContext extends EditContextBase {

	/**
	 * The {@link TLFormObject} this {@link EditContext} stores an attribute value for.
	 */
	TLFormObject getOverlay();

	/**
	 * Option provider relevant for the current context.
	 */
	Generator getOptions();

	/**
	 * Sets the algorithm that persists the edited value.
	 * 
	 * @see DefaultStorageAlgorithm
	 */
	@FrameworkInternal
	void setStoreAlgorithm(StoreAlgorithm storeAlgorithm);

	/**
	 * {@link AttributedValueFilter} relevant for the current context.
	 */
	@FrameworkInternal
	@Deprecated
	default AttributedValueFilter getFilter() {
		return null;
	}

}
