/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.version.intf;

import java.util.Date;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;

/**
 * Interface for {@link #TAG_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceTemplateGenerator}
 */
public interface Tag extends com.top_logic.element.version.TagBase {

	/**
	 * Return the date this tag represents.
	 * 
	 * <p>
	 * This method is used to calculate the value of {@link Tag#getDate()}.
	 * </p>
	 * 
	 * @return The date this tag has been created.
	 */
	@CalledByReflection
	default Date getRevisionDate() {
        return new Date(this.getRevision().getDate());
    }

	/**
	 * Return the create revision of this tag.
	 * 
	 * @return The revision of this tag, never <code>null</code>.
	 * @throws WrapperRuntimeException
	 *         If this wrapper is invalid.
	 */
	default Revision getRevision() {
		return HistoryUtils.getCreateRevision(this.tHandle());
    }

	/** 
     * Return the revision of the given wrapper matching to this tag.
     * 
     * @param     aWrapper    The wrapper to get the historical version from, may be <code>null</code>.
     * @return    The historical version of the given wrapper, may be <code>null</code>.
     */
	default Wrapper getWrapper(Wrapper aWrapper) {
        return (aWrapper == null) ? null : WrapperHistoryUtils.getWrapper(this.getRevision(), aWrapper);
    }

	/**
	 * Create a new tag.
	 * 
	 * @param aName
	 *        The name of the new tag, must not be <code>null</code> or empty.
	 * @return The requested tag, never <code>null</code>.
	 */
	public static Tag createTag(String aName) {
		if (StringServices.isEmpty(aName)) {
			throw new IllegalArgumentException("A tag must have a name");
		}

		Tag theTag = TagFactory.getInstance().createTag();

		theTag.setName(aName);

		return theTag;
	}

	/**
	 * Return an instance of a {@link Tag} for the given knowledge object.
	 * 
	 * @param aKO
	 *        The requested KO, must not be <code>null</code>.
	 * @return The requested instance, <code>null</code> when given item has no
	 *         {@link KnowledgeBase} (deleted or result of a roll back).
	 */
	public static Tag getInstance(KnowledgeObject aKO) {
		return (Tag) WrapperFactory.getWrapper(aKO);
	}

	/**
	 * Return an instance of a {@link Tag} for the given KO-ID.
	 * 
	 * @param anID
	 *        The requested KO-ID, must not be <code>null</code>.
	 * @return The requested instance, may be <code>null</code> if no such {@link Tag} can be found.
	 */
	public static Tag getInstance(TLID anID) {
		return (Tag) WrapperFactory.getWrapper(anID, TagFactory.KO_NAME_TAG);
	}

}
