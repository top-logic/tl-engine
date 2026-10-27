/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

import java.util.Objects;
import java.util.function.Consumer;

import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.RepositorySource;

/**
 * {@link RepositorySource.Pipeline} of a project repository built by a {@link CIProvider}.
 *
 * <p>
 * A new application gets the configured pipeline definition (see {@link CIService#pipelineTemplate()})
 * as the {@link CIProvider#pipelineFile() pipeline file} of its first commit. When the repository is
 * ready, the {@link CIProvider#ensureJob(GitRepository, String) job} of the repository is set up,
 * recorded for the project, and its first build is started.
 * </p>
 */
public class CIPipeline implements RepositorySource.Pipeline {

	private final CIProvider _provider;

	private final String _content;

	private final String _description;

	private final Consumer<CIJob> _record;

	/**
	 * Creates a {@link CIPipeline}.
	 *
	 * @param provider
	 *        The CI server.
	 * @param content
	 *        The pipeline definition of a new application.
	 * @param description
	 *        The description of the job, e.g. the name of the project; {@code null} for none.
	 * @param record
	 *        Stores the job of the project, in a transaction of its own.
	 */
	public CIPipeline(CIProvider provider, String content, String description, Consumer<CIJob> record) {
		_provider = Objects.requireNonNull(provider, "provider");
		_content = Objects.requireNonNull(content, "content");
		_description = description;
		_record = Objects.requireNonNull(record, "record");
	}

	@Override
	public String file() {
		return _provider.pipelineFile();
	}

	@Override
	public String content() {
		return _content;
	}

	@Override
	public void setUp(GitRepository repository, Consumer<String> progress) {
		progress.accept("Setting up the CI job of " + repository.getFullName());
		CIJob job = _provider.ensureJob(repository, _description);
		_record.accept(job);
		progress.accept("CI job " + job.name() + ": " + job.webUrl());
		CIBuildStart build = _provider.startBuild(job.name());
		if (build.isQueued()) {
			progress.accept("First CI build queued: " + build.webUrl());
		} else {
			progress.accept("First CI build " + build.number() + " started: " + build.webUrl());
		}
	}

}
