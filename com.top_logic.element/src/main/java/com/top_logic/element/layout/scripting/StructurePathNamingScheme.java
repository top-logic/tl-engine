/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * A {@link ModelNamingScheme} for identifying a {@link StructuredElement} with a
 * {@link BreadcrumbStrings}.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class StructurePathNamingScheme
		extends AbstractModelNamingScheme<StructuredElement, StructurePathNamingScheme.StructurePathName> {

	/**
	 * The {@link ModelName} of the {@link StructurePathNamingScheme}.
	 */
	public interface StructurePathName extends ModelName {

		/** Property name of {@link #getLabelPath()}. */
		String LABEL_PATH = "label-path";

		/** Property name of {@link #getStructureName()}. */
		String STRUCTURE_NAME = "structure-name";

		/**
		 * The {@link BreadcrumbStrings path} to the element.
		 * <p>
		 * Root is not part of the path.
		 * </p>
		 */
		@Format(BreadcrumbStrings.class)
		@Name(LABEL_PATH)
		List<String> getLabelPath();

		/** @see #getLabelPath() */
		void setLabelPath(List<String> labelPath);

		/**
		 * The technical name of the structure.
		 */
		@Name(STRUCTURE_NAME)
		String getStructureName();

		/** @see #getStructureName() */
		void setStructureName(String structureName);

	}

	@Override
	public Class<StructurePathName> getNameClass() {
		return StructurePathName.class;
	}

	@Override
	public Class<StructuredElement> getModelClass() {
		return StructuredElement.class;
	}

	@Override
	public StructuredElement locateModel(ActionContext context, StructurePathName name) {
		StructuredElement root = getStructureRoot(name);
		return findElement(name, root);
	}

	private StructuredElement getStructureRoot(StructurePathName name) {
		StructuredElementFactory factory = getFactory(name.getStructureName());
		return factory.getRoot();
	}

	private StructuredElementFactory getFactory(String structureName) {
		return (StructuredElementFactory) DynamicModelService.getFactoryFor(structureName);
	}

	private StructuredElement findElement(StructurePathName name, StructuredElement parent) {
		List<String> labelPath = name.getLabelPath();
		for (String childLabel : labelPath) {
			parent = searchChild(parent, childLabel);
		}
		return parent;
	}

	private StructuredElement searchChild(StructuredElement parent, String label) {
		SearchResult<StructuredElement> searchResult = new SearchResult<>();
		for (StructuredElement child : parent.getChildren()) {
			String childLabel = getLabel(child);
			searchResult.addCandidate(childLabel);
			if (StringServices.equals(label, childLabel)) {
				searchResult.add(child);
			}
		}
		String message = "Failed to find the child labeled '" + label + "' in '" + getLabel(parent) + "'";
		return searchResult.getSingleResult(message);
	}

	@Override
	protected void initName(StructurePathName name, StructuredElement model) {
		name.setLabelPath(createLabelPath(model));
		name.setStructureName(model.getStructureName());
	}

	private List<String> createLabelPath(StructuredElement model) {
		List<String> labelPath = new ArrayList<>();
		StructuredElement iterator = model;
		// Root is not added to the path
		while (iterator.getParent() != null) {
			labelPath.add(getLabel(iterator));
			iterator = iterator.getParent();
		}
		Collections.reverse(labelPath);
		return labelPath;
	}

	private String getLabel(StructuredElement element) {
		String label = MetaLabelProvider.INSTANCE.getLabel(element);
		if (isEmpty(label)) {
			return element.getName();
		}
		return label;
	}

	@Override
	protected boolean isCompatibleModel(StructuredElement model) {
		return false;
	}

}
