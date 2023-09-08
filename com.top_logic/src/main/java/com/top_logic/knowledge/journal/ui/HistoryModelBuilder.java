/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} that retrieves all versions of an object.
 * 
 * <p>
 * Each change of an object in the knowledge base produces a separate version. Those versions are
 * retrieved by this builder.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HistoryModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link HistoryModelBuilder} instance.
	 */
	public static final HistoryModelBuilder INSTANCE = new HistoryModelBuilder();

	private HistoryModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		Wrapper model = (Wrapper) businessModel;
		if (model == null) {
			return Collections.emptyList();
		}
		return getAllVersions(model);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return (aModel == null) || (aModel instanceof Wrapper);
	}

	@Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
		return (anObject instanceof Wrapper)
			&& aComponent.getModel() == WrapperHistoryUtils.getCurrent((Wrapper) anObject);
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		return WrapperHistoryUtils.getCurrent((Wrapper) anObject);
	}

	/**
	 * Retrieve a list of all relevant predecessor versions of the given object.
	 * 
	 * @param object
	 *        Start version, must not be a new uncommitted object.
	 * @return List of all versions of the given object, in which a change to the object occurred.
	 */
	public static ArrayList<Wrapper> getAllVersions(Wrapper object) {
		ArrayList<Wrapper> versions = new ArrayList<>();
	
		Revision versionRevision = WrapperHistoryUtils.getLastUpdate(object);
		Wrapper version = WrapperHistoryUtils.getWrapper(versionRevision, object);
		if (version == null) {
			throw new AssertionError("Object does not exist in its last update revision.");
		}
		while (true) {
			versions.add(version);
	
			Wrapper beforeLastUpdate =
				WrapperHistoryUtils.getWrapper(HistoryUtils.getRevision(versionRevision.getCommitNumber() - 1), version);
			if (beforeLastUpdate == null) {
				// No predecessor version.
				break;
			}
	
			versionRevision = WrapperHistoryUtils.getLastUpdate(beforeLastUpdate);
			version = WrapperHistoryUtils.getWrapper(versionRevision, beforeLastUpdate);
		}
		return versions;
	}

}
