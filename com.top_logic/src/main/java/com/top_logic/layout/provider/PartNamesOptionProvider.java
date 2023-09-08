/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.layout.editor.I18NConstants;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.provider.AllColumnOptions;
import com.top_logic.layout.table.provider.ColumnOption;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.config.FullQualifiedName;
import com.top_logic.model.config.TypeRef;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericFunction} retrieving all part names of a given {@link TLStructuredType}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PartNamesOptionProvider extends Function0<Collection<ColumnOption>> {

	TLModelPart _modelPart;

	TLType _type;

	/**
	 * Creates a {@link PartNamesOptionProvider}.
	 */
	@CalledByReflection
	public PartNamesOptionProvider(DeclarativeFormOptions options) {
		ConfigurationItem formModel = options.get(DeclarativeFormBuilder.FORM_MODEL);

		try {
			_modelPart = getContextModelPart((FullQualifiedName) formModel);
			_type = getTargetType((TypeRef) formModel);
		} catch (ConfigurationException exception) {
			throw new TopLogicException(I18NConstants.MODEL_TYPE_NOT_RESOLVED_ERROR, exception);
		}
	}

	private TLType getTargetType(TypeRef typeReference) throws ConfigurationException {
		String targetType = typeReference.getTypeSpec();

		if (StringServices.isEmpty(targetType)) {
			return null;
		}

		return TLModelUtil.findType(targetType);
	}

	private TLModelPart getContextModelPart(FullQualifiedName qualifiedName) throws ConfigurationException {
		String fullQualifiedName = qualifiedName.getFullQualifiedName();

		if (StringServices.isEmpty(fullQualifiedName)) {
			return null;
		}

		return TLModelUtil.resolveModelPart(fullQualifiedName);
	}

	@Override
	public Collection<ColumnOption> apply() {
		if (_modelPart instanceof TLStructuredType) {
			return TableUtil.createColumnOptions(TLModelUtil.getMetaAttributes((TLClass) _type, true, false));
		} else if (_modelPart instanceof TLStructuredTypePart) {
			return AllColumnOptions.INSTANCE.apply(Collections.singleton(TLModelPartRef.ref(_type)));
		}

		return Collections.emptyList();
	}

}
