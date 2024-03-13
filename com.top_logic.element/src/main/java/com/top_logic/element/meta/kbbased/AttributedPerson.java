/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Locale;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.service.db2.KnowledgeItemImpl;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.Country;
import com.top_logic.util.Utils;

/**
 * Person implementation that returns the dynamic person type as {@link TLObject#tType()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AttributedPerson extends Person {

	/**
	 * @see KnowledgeItemImpl#createWrapper(KnowledgeItem)
	 */
	@CalledByReflection
	public AttributedPerson(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public TLClass tType() {
		return PersistentObjectImpl.tType(this);
	}

	/**
	 * The list of attribute names defined by the
	 * 
	 * @see AbstractBoundWrapper#getAllAttributeNames()
	 */
	@Override
	public Set<String> getAllAttributeNames() {
		Set<String> theResult = super.getAllAttributeNames();

		for (TLStructuredTypePart part : tType().getAllParts()) {
			theResult.add(part.getName());
		}

		return theResult;
	}

	@Override
	public TLObject tContainer() {
		return PersistentObjectImpl.tContainer(this);
	}

	@Override
	public TLReference tContainerReference() {
		return PersistentObjectImpl.tContainerReference(this);
	}

	@Override
	public Object getValue(String anAttribute) {
		return PersistentObjectImpl.getValue(this, anAttribute);
	}

	@Override
	public Object tValue(TLStructuredTypePart part) {
		return PersistentObjectImpl.getValue(this, part);
	}

	@Override
	public void generateFullText(FullTextBuBuffer buffer) {
		PersistentObjectImpl.generateFullText(buffer, this);
	}

	@Override
	public void setValue(String aKey, Object aValue) {
		PersistentObjectImpl.setValue(this, aKey, aValue);
	}

	@Override
	public Locale getLocale() {
		return Utils.parseLocale((String) tValueByName(Person.LOCALE));
	}

	@Override
	public Locale getLanguage() {
		return (Locale) tValueByName(LANGUAGE_ATTR);
	}

	@Override
	public void setLanguage(Locale newValue) {
		tUpdateByName(LANGUAGE_ATTR, newValue);
	}

	@Override
	public Country getCountry() {
		return (Country) tValueByName(COUNTRY_ATTR);
	}

	@Override
	public void setCountry(Country newValue) {
		tUpdateByName(COUNTRY_ATTR, newValue);
	}

}

