/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.list;

import static com.top_logic.basic.StringServices.*;

import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.util.Resources;

/**
 * {@link ModelNamingScheme} for identifying {@link FastListElement}s by their translation and that
 * of their {@link FastList}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class FastListNaming extends AbstractModelNamingScheme<FastListElement, FastListNaming.FastListName> {

	/**
	 * The separator between the {@link FastList} and the {@link FastListElement}.
	 */
	public static final String SEPARATOR = "/";

	/**
	 * The ModelName of the {@link FastListNaming}.
	 */
	public interface FastListName extends ModelName {

		/**
		 * The combination of the {@link FastList} translation and the {@link FastListElement}
		 * translation, separated by {@link #getSeparator()}.
		 */
		String getTranslation();

		/**
		 * @see #getTranslation()
		 */
		void setTranslation(String translation);

		/**
		 * The separator between the {@link FastList} and the {@link FastListElement}.
		 */
		@StringDefault(SEPARATOR)
		String getSeparator();

		/**
		 * @see #getSeparator()
		 */
		void setSeparator(String separator);
	}

	@Override
	public Class<FastListName> getNameClass() {
		return FastListName.class;
	}

	@Override
	public Class<FastListElement> getModelClass() {
		return FastListElement.class;
	}

	@Override
	public FastListElement locateModel(ActionContext context, FastListName name) {
		String[] parts = split(name.getTranslation(), name.getSeparator());
		String listName = parts[0];
		String elementName = parts[1];
		FastList list = findList(listName);
		return findElement(elementName, list);
	}

	private String[] split(String translation, String separator) {
		int index = translation.indexOf(separator);
		if (index < 0) {
			throw new IllegalArgumentException("Expected the translation of the FastList and the FastListElement,"
				+ " separated by '" + separator + "', but got: '" + translation + "'");
		}
		return new String[] {
			translation.substring(0, index),
			translation.substring(index + separator.length()) };
	}

	private FastList findList(String listName) {
		SearchResult<FastList> searchResult = new SearchResult<>();
		for (FastList fastList : FastList.getAllLists()) {
			String translation = getTranslation(fastList);
			searchResult.addCandidate(translation);
			if (StringServices.equals(translation, listName)) {
				searchResult.add(fastList);
			}
		}
		return searchResult.getSingleResult(
			"Failed to resolve FastList with the translation '" + listName + "'.");
	}

	private FastListElement findElement(String elementName, FastList fastList) {
		SearchResult<FastListElement> searchResult = new SearchResult<>();
		for (FastListElement fastListElement : getElements(fastList)) {
			String label = getTranslation(fastListElement);
			searchResult.addCandidate(label);
			if (StringServices.equals(label, elementName)) {
				searchResult.add(fastListElement);
			}
		}
		return searchResult.getSingleResult(
			"Failed to resolve FastListElement with the translation '" + elementName + "' in " + debug(fastList) + ".");
	}

	private List<FastListElement> getElements(FastList fastList) {
		return fastList.elements();
	}

	@Override
	protected void initName(FastListName name, FastListElement model) {
		name.setTranslation(getTranslation(model.getList()) + name.getSeparator() + getTranslation(model));
	}

	private String getTranslation(FastList classification) {
		return Resources.getInstance().getString(TLModelNamingConvention.enumKey(classification));
	}

	private String getTranslation(FastListElement classifier) {
		return Resources.getInstance().getString(TLModelNamingConvention.classifierKey(classifier));
	}

}
