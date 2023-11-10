/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;

/**
 * Template parameter definition to provide a type and a in this type contained multiple
 * {@link TLReference} parameter.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypeWithReferenceTemplateParameters extends TypeTemplateParameters {

	/**
	 * @see #getReference()
	 */
	String REFERENCE = "reference";

	/**
	 * {@link TLReference} of {@link #getType()}.
	 */
	@Options(fun = MultipleReferencesOfType.class, args = { @Ref(TYPE) }, mapping = TLModelPartRef.PartMapping.class)
	@Mandatory
	@Name(REFERENCE)
	TLModelPartRef getReference();

	/**
	 * All multiple {@link TLReference}s of a type given by a {@link TLModelPartRef}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	class MultipleReferencesOfType extends Function1<List<TLModelPart>, TLModelPartRef> {
		@Override
		public List<TLModelPart> apply(TLModelPartRef typeRef) {
			if (typeRef == null) {
				return Collections.emptyList();
			}
			TLClass type;
			try {
				type = typeRef.resolveClass();
			} catch (ConfigurationException ex) {
				return Collections.emptyList();
			}

			return getAllStructureReferences(type);
		}

		private List<TLModelPart> getAllStructureReferences(TLClass type) {
			Set<TLModelPart> structureReferences = new HashSet<>();

			for (TLTypePart typePart : TLModelUtil.getMetaAttributes(type, true, true)) {
				if (isStructureReference(typePart)) {
					structureReferences.add(((TLReference) typePart).getDefinition());
				}
			}

			return new ArrayList<>(structureReferences);
		}

		private boolean isStructureReference(TLTypePart typePart) {
			return typePart.getModelKind() == ModelKind.REFERENCE && ((TLReference) typePart).isMultiple();
		}
	}

}
