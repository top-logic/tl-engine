/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external.meta;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.LazyListUnmodifyable;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.contact.external.ExternalContact;
import com.top_logic.contact.external.ExternalContacts;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link ExternalContact}.
 * 
 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public final class ExternalContactFieldProvider extends AbstractFieldProvider {

	private final boolean _multiple;

	/**
	 * Configuration options for {@link ExternalContactFieldProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<FieldProvider> {

		/**
		 * Whether the attribute is a multiple reference.
		 */
		boolean isMultiple();
	}
	
	/**
	 * Creates a {@link ExternalContactFieldProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExternalContactFieldProvider(InstantiationContext context, Config config) {
		_multiple = config.isMultiple();
	}

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		boolean isDisabled  = editContext.isDisabled();
		boolean isSearch    = editContext.isSearchUpdate();
		
		boolean isMultiple = _multiple || isSearch;
		boolean isMandatory = isSearch ? false : editContext.isMandatory();

	    SelectField selectField = FormFactory.newSelectField(fieldName, new LazyListUnmodifyable<ExternalContact>() {
			@Override
			protected List<ExternalContact> initInstance() {
				try {
					return ExternalContacts.getAllContactsCached();
				} catch (SQLException e) {
					Logger.error("Fetching external contacts failed.", e);
					return Collections.emptyList();
				}
			}
			
		}, isMultiple, isDisabled);
	    
	    selectField.setOptionLabelProvider(MetaLabelProvider.INSTANCE);
	    selectField.setMandatory(isMandatory);
	    
	    return selectField;
	}
}