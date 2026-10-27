/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.ci;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.factory.ci.CIBuild;
import com.top_logic.factory.ci.CIBuildStart;
import com.top_logic.factory.ci.CIBuildState;
import com.top_logic.factory.ci.CIConsole;
import com.top_logic.factory.ci.CIJob;
import com.top_logic.factory.ci.CIProvider;
import com.top_logic.factory.ci.I18NConstants;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CIProvider} without CI server: it keeps the jobs and their builds in memory and records
 * the calls.
 *
 * <p>
 * A job is named {@link #FOLDER}{@code /<owner>/<name>} after its repository and has its page below
 * {@link #BASE_URL}. A started build runs until a test {@link #finish(String, CIBuildState) finishes}
 * it; its console output is {@link #setConsole(String, int, String) set} by the test. A test lets the
 * next calls fail with {@link #setFailure(TopLogicException)}.
 * </p>
 */
public class RecordingCIProvider implements CIProvider {

	/**
	 * The URL of the simulated CI server.
	 */
	public static final String BASE_URL = "https://ci.test/";

	/**
	 * The folder of the jobs.
	 */
	public static final String FOLDER = "factory-jobs";

	/**
	 * The {@link #pipelineFile() pipeline file}.
	 */
	public static final String PIPELINE_FILE = "Jenkinsfile";

	/**
	 * Entry of {@link #calls()} for {@link #ensureJob(GitRepository, String)}.
	 */
	public static final String ENSURE = "ensure";

	/**
	 * Entry of {@link #calls()} for {@link #startBuild(String)}.
	 */
	public static final String START = "start";

	/**
	 * Entry of {@link #calls()} for {@link #lastBuild(String)}.
	 */
	public static final String LAST = "last";

	/**
	 * Entry of {@link #calls()} for {@link #console(String, int, int)}.
	 */
	public static final String CONSOLE = "console";

	private static final Instant START_TIME = Instant.parse("2026-10-27T10:15:30Z");

	private final Map<String, GitRepository> _jobs = new LinkedHashMap<>();

	private final Map<String, List<CIBuild>> _builds = new LinkedHashMap<>();

	private final Map<String, String> _consoles = new LinkedHashMap<>();

	private final List<String> _calls = new ArrayList<>();

	private boolean _queued;

	private TopLogicException _failure;

	@Override
	public String pipelineFile() {
		return PIPELINE_FILE;
	}

	/**
	 * Lets every following call fail with the given exception, {@code null} for none.
	 */
	public synchronized void setFailure(TopLogicException failure) {
		_failure = failure;
	}

	/**
	 * Lets the following started builds wait in the queue, without number.
	 */
	public synchronized void setQueued(boolean queued) {
		_queued = queued;
	}

	@Override
	public synchronized CIJob ensureJob(GitRepository repository, String description) {
		_calls.add(ENSURE + " " + repository.getFullName());
		checkFailure();
		String name = jobName(repository);
		_jobs.put(name, repository);
		_builds.computeIfAbsent(name, x -> new ArrayList<>());
		return new CIJob(name, jobUrl(name));
	}

	/**
	 * The name of the job of the given repository.
	 */
	public static String jobName(GitRepository repository) {
		return FOLDER + "/" + repository.getFullName();
	}

	/**
	 * The page of the given job.
	 */
	public static String jobUrl(String job) {
		return BASE_URL + "job/" + job + "/";
	}

	@Override
	public synchronized CIBuildStart startBuild(String job) {
		_calls.add(START + " " + job);
		checkFailure();
		List<CIBuild> builds = builds(job);
		if (_queued) {
			return new CIBuildStart(null, "q" + builds.size(), jobUrl(job));
		}
		int number = builds.size() + 1;
		CIBuild build = new CIBuild(number, CIBuildState.RUNNING, jobUrl(job) + number + "/", START_TIME,
			Duration.ofSeconds(number));
		builds.add(build);
		return new CIBuildStart(Integer.valueOf(number), "q" + number, build.webUrl());
	}

	/**
	 * Ends the last build of the given job in the given state.
	 */
	public synchronized void finish(String job, CIBuildState state) {
		List<CIBuild> builds = builds(job);
		CIBuild last = builds.remove(builds.size() - 1);
		builds.add(new CIBuild(last.number(), state, last.webUrl(), last.startTime(), last.duration()));
	}

	/**
	 * Sets the console output of a build.
	 */
	public synchronized void setConsole(String job, int number, String text) {
		_consoles.put(job + "#" + number, text);
	}

	@Override
	public synchronized CIBuild lastBuild(String job) {
		_calls.add(LAST + " " + job);
		checkFailure();
		List<CIBuild> builds = builds(job);
		return builds.isEmpty() ? null : builds.get(builds.size() - 1);
	}

	@Override
	public synchronized CIConsole console(String job, int number, int maxBytes) {
		_calls.add(CONSOLE + " " + job + " " + number);
		checkFailure();
		List<CIBuild> builds = builds(job);
		if (number > builds.size()) {
			throw new TopLogicException(I18NConstants.ERROR_CI_BUILD_NOT_FOUND__JOB_BUILD.fill(job, number));
		}
		String text = _consoles.getOrDefault(job + "#" + number, "");
		byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
		if (bytes.length <= maxBytes) {
			return new CIConsole(text, false);
		}
		String tail = new String(bytes, bytes.length - maxBytes, maxBytes, StandardCharsets.UTF_8);
		return new CIConsole(tail.substring(tail.indexOf('\n') + 1), true);
	}

	/**
	 * The jobs by name with the repositories they build.
	 */
	public synchronized Map<String, GitRepository> jobs() {
		return new LinkedHashMap<>(_jobs);
	}

	/**
	 * The calls of the operations, each as {@code <operation> <argument>...}, in the order of the
	 * calls.
	 */
	public synchronized List<String> calls() {
		return new ArrayList<>(_calls);
	}

	private List<CIBuild> builds(String job) {
		List<CIBuild> builds = _builds.get(job);
		if (builds == null) {
			throw new TopLogicException(I18NConstants.ERROR_CI_JOB_NOT_FOUND__JOB.fill(job));
		}
		return builds;
	}

	private void checkFailure() {
		if (_failure != null) {
			throw _failure;
		}
	}

}
