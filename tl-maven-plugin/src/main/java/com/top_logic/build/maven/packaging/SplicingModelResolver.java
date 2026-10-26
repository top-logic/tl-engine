/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.packaging;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;

/**
 * {@link ModelResolver} passing the resolved POMs through a {@link ParentSplicer}.
 */
public class SplicingModelResolver implements ModelResolver {

	private final ModelResolver _delegate;

	private final ParentSplicer _splicer;

	/**
	 * Creates a {@link SplicingModelResolver}.
	 *
	 * @param delegate
	 *        The resolver locating the POMs.
	 * @param splicer
	 *        The splicer to pass the resolved POMs through.
	 */
	public SplicingModelResolver(ModelResolver delegate, ParentSplicer splicer) {
		_delegate = delegate;
		_splicer = splicer;
	}

	@Override
	public ModelSource resolveModel(String groupId, String artifactId, String version)
			throws UnresolvableModelException {
		return wrap(_delegate.resolveModel(groupId, artifactId, version));
	}

	@Override
	public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
		return wrap(_delegate.resolveModel(parent));
	}

	@Override
	public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
		return wrap(_delegate.resolveModel(dependency));
	}

	@Override
	public void addRepository(Repository repository) throws InvalidRepositoryException {
		_delegate.addRepository(repository);
	}

	@Override
	public void addRepository(Repository repository, boolean replace) throws InvalidRepositoryException {
		_delegate.addRepository(repository, replace);
	}

	@Override
	public ModelResolver newCopy() {
		return new SplicingModelResolver(_delegate.newCopy(), _splicer);
	}

	private ModelSource wrap(ModelSource source) {
		if (source instanceof FileModelSource) {
			return new SplicingModelSource(((FileModelSource) source).getFile(), _splicer);
		}
		return source;
	}
}
