/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.ProxyExchange;
import com.top_logic.factory.proxy.ProxyForwarding;
import com.top_logic.factory.proxy.ProxyRoute;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ProxyRoute} of the {@link WorkspaceProxyService workspace proxy} through which the coding
 * agent of a workspace builds the application of its project on the CI server, without a
 * credential of the CI server in the workspace.
 *
 * <p>
 * The route offers an API of its own, independent of the CI server, on the CI job of the project
 * of the workspace the token of the request was issued for. Below the path of the route (by
 * default {@link #DEFAULT_PATH}):
 * </p>
 * <ul>
 * <li>{@code POST /}{@link #BUILDS} starts a build and answers with a JSON object: the
 * {@link #FIELD_JOB job}, the {@link #FIELD_NUMBER number} of the build ({@code null} while it is
 * {@link #FIELD_QUEUED queued}) and its {@link #FIELD_URL page}.</li>
 * <li>{@code GET /}{@link #BUILDS}{@code /}{@link #LAST} answers with the last build as a JSON object:
 * {@link #FIELD_JOB job}, {@link #FIELD_NUMBER number}, {@link #FIELD_STATE state} (a
 * {@link CIBuildState}), {@link #FIELD_URL page}, {@link #FIELD_START_TIME start time} (ISO 8601),
 * {@link #FIELD_DURATION duration} in milliseconds; 404 if the job has no build.</li>
 * <li>{@code GET} of {@link #BUILDS}, the number of the build and {@link #LOG}, separated by
 * slashes, answers with the end of the console output of the build as plain text, at most
 * {@link Config#getMaxLogSize()} bytes; an output that is cut starts with the line
 * {@link #TRUNCATED_MARK}.</li>
 * </ul>
 *
 * <p>
 * Every other request - another path or method -, every request of a workspace whose project has no
 * CI job, and every request while no CI server is configured is answered with 404. A failure of the
 * CI server is answered with 502 and a fixed text; the details go to the log of the control
 * application. The requests are authenticated with the token of the workspace like those of every
 * route, see {@link ProxyTokens#ENV_PROXY_TOKEN}.
 * </p>
 *
 * @implNote The CI job of a project is {@link FactoryModel#getCIJob(TLObject)}.
 */
public class CIRoute implements ProxyRoute {

	/**
	 * Configuration of {@link CIRoute}.
	 */
	public interface Config<I extends CIRoute> extends ProxyRoute.Config<I> {

		/**
		 * Configuration property name of {@link #getMaxLogSize()}.
		 */
		String MAX_LOG_SIZE = "max-log-size";

		@Override
		@ClassDefault(CIRoute.class)
		Class<? extends I> getImplementationClass();

		@Override
		@StringDefault(DEFAULT_PATH)
		String getPath();

		/**
		 * The largest part of the console output of a build the route answers with, in bytes: of a
		 * longer output, only its last lines.
		 *
		 * @see #MAX_LOG_SIZE
		 */
		@Name(MAX_LOG_SIZE)
		@IntDefault(DEFAULT_MAX_LOG_SIZE)
		int getMaxLogSize();

	}

	/**
	 * Default of {@link Config#getPath()}.
	 */
	public static final String DEFAULT_PATH = "/ci";

	/**
	 * Default of {@link Config#getMaxLogSize()}.
	 */
	public static final int DEFAULT_MAX_LOG_SIZE = 64 * 1024;

	/**
	 * Environment variable of a workspace container naming the URL of the route.
	 */
	public static final String ENV_CI_URL = "TL_FACTORY_CI_URL";

	/**
	 * Path segment of the builds of the job, below the route.
	 */
	public static final String BUILDS = "builds";

	/**
	 * Path segment of the last build, below {@link #BUILDS}.
	 */
	public static final String LAST = "last";

	/**
	 * Path segment of the console output, below a build.
	 */
	public static final String LOG = "log";

	/**
	 * Field of a JSON answer: the name of the CI job.
	 */
	public static final String FIELD_JOB = "job";

	/**
	 * Field of a JSON answer: the number of the build.
	 */
	public static final String FIELD_NUMBER = "number";

	/**
	 * Field of a JSON answer: whether the started build waits for an executor.
	 */
	public static final String FIELD_QUEUED = "queued";

	/**
	 * Field of a JSON answer: the state of the build, the name of a {@link CIBuildState}.
	 */
	public static final String FIELD_STATE = "state";

	/**
	 * Field of a JSON answer: the page of the build in the web interface of the CI server.
	 */
	public static final String FIELD_URL = "url";

	/**
	 * Field of a JSON answer: when the build started, in ISO 8601.
	 */
	public static final String FIELD_START_TIME = "startTime";

	/**
	 * Field of a JSON answer: how long the build took, or runs so far, in milliseconds.
	 */
	public static final String FIELD_DURATION = "durationMillis";

	/**
	 * The first line of a console output the route has cut.
	 */
	public static final String TRUNCATED_MARK = "[... earlier output omitted ...]";

	/**
	 * Content type of the JSON answers.
	 */
	public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

	private static final String GET = "GET";

	private static final String POST = "POST";

	private static final char PATH_SEPARATOR = '/';

	private static final char NEW_LINE = '\n';

	private static final Pattern BUILD_NUMBER = Pattern.compile("[1-9][0-9]{0,8}");

	private static final int SC_OK = 200;

	private static final int SC_NOT_FOUND = 404;

	private static final int SC_BAD_GATEWAY = 502;

	private static final String NO_BUILD = "No build.";

	private static final String CI_FAILED = "The CI server failed to answer the request.";

	private final String _path;

	private final Supplier<? extends CIProvider> _provider;

	private final Function<String, String> _jobOfWorkspace;

	private final int _maxLogSize;

	/**
	 * Creates a {@link CIRoute} from configuration, asking the provider of the {@link CIService}
	 * for the CI jobs of the stored projects.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public CIRoute(InstantiationContext context, Config<?> config) {
		this(config.getPath(), CIService::configuredProvider, CIRoute::workspaceJob, config.getMaxLogSize());
	}

	/**
	 * Creates a {@link CIRoute}.
	 *
	 * @param path
	 *        See {@link #getPath()}.
	 * @param provider
	 *        Supplier of the CI server asked for each request; it may supply {@code null} when none
	 *        is configured, which answers requests with 404.
	 * @param jobOfWorkspace
	 *        The {@link CIJob#name() name} of the CI job a workspace has access to, by the ID of
	 *        the workspace; {@code null} for none.
	 * @param maxLogSize
	 *        See {@link Config#getMaxLogSize()}.
	 */
	public CIRoute(String path, Supplier<? extends CIProvider> provider, Function<String, String> jobOfWorkspace,
			int maxLogSize) {
		_path = path;
		_provider = provider;
		_jobOfWorkspace = jobOfWorkspace;
		_maxLogSize = maxLogSize;
	}

	@Override
	public String getPath() {
		return _path;
	}

	@Override
	public Map<String, String> containerEnv(String routeUrl) {
		return Map.of(ENV_CI_URL, routeUrl);
	}

	/**
	 * What the coding agent of a workspace is told about building its application on the CI server
	 * through this route, in addition to its instructions.
	 */
	public static String agentInstructions() {
		String auth = "-H \"Authorization: Bearer $" + ProxyTokens.ENV_PROXY_TOKEN + "\" ";
		String builds = "\"$" + ENV_CI_URL + PATH_SEPARATOR + BUILDS;
		return "The CI server builds the default branch of the repository of the application. After a push, "
			+ "start a build with `curl -X POST " + auth + builds + "\"` and check it with `curl " + auth + builds
			+ PATH_SEPARATOR + LAST + "\"` (JSON with " + FIELD_NUMBER + ", " + FIELD_STATE + ", " + FIELD_URL
			+ "); `curl " + auth + builds + "/<number>/" + LOG + "\"` shows the end of the console output of a build.";
	}

	@Override
	public void handle(ProxyExchange exchange, String path, ProxyAdmission admission) throws IOException {
		CIRequest request = CIRequest.parse(exchange.method(), path);
		if (request == null) {
			notFound(exchange);
			return;
		}
		CIProvider provider = _provider.get();
		if (provider == null) {
			notFound(exchange);
			return;
		}
		String job = _jobOfWorkspace.apply(admission.workspaceId());
		if (job == null) {
			notFound(exchange);
			return;
		}
		try {
			switch (request.kind()) {
				case START:
					startBuild(exchange, provider, job);
					break;
				case LAST_BUILD:
					lastBuild(exchange, provider, job);
					break;
				case CONSOLE:
					console(exchange, provider, job, request.number());
					break;
			}
		} catch (TopLogicException ex) {
			if (isError(ex, I18NConstants.ERROR_CI_JOB_NOT_FOUND__JOB)
				|| isError(ex, I18NConstants.ERROR_CI_BUILD_NOT_FOUND__JOB_BUILD)) {
				notFound(exchange);
				return;
			}
			Logger.warn("CI proxy: request of workspace '" + admission.workspaceId() + "' on job '" + job
				+ "' failed: " + ex.getMessage(), ex, CIRoute.class);
			ProxyForwarding.sendText(exchange, SC_BAD_GATEWAY, CI_FAILED);
		}
	}

	/**
	 * Whether the given exception reports the given error.
	 */
	public static boolean isError(TopLogicException ex, ResKeyTemplate error) {
		ResKey key = ex.getErrorKey();
		return key != null && error.getKey().equals(key.plain().getKey());
	}

	private static void startBuild(ProxyExchange exchange, CIProvider provider, String job) throws IOException {
		CIBuildStart build = provider.startBuild(job);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(FIELD_JOB, job);
		result.put(FIELD_NUMBER, build.number());
		result.put(FIELD_QUEUED, Boolean.valueOf(build.isQueued()));
		result.put(FIELD_URL, build.webUrl());
		sendJson(exchange, result);
	}

	private static void lastBuild(ProxyExchange exchange, CIProvider provider, String job) throws IOException {
		CIBuild build = provider.lastBuild(job);
		if (build == null) {
			ProxyForwarding.sendText(exchange, SC_NOT_FOUND, NO_BUILD);
			return;
		}
		sendJson(exchange, toJson(job, build));
	}

	/**
	 * The JSON object describing the given build, as {@link #LAST} answers it.
	 */
	public static Map<String, Object> toJson(String job, CIBuild build) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(FIELD_JOB, job);
		result.put(FIELD_NUMBER, Integer.valueOf(build.number()));
		result.put(FIELD_STATE, build.state().name());
		result.put(FIELD_URL, build.webUrl());
		result.put(FIELD_START_TIME, build.startTime().toString());
		result.put(FIELD_DURATION, Long.valueOf(build.duration().toMillis()));
		return result;
	}

	private void console(ProxyExchange exchange, CIProvider provider, String job, int number) throws IOException {
		CIConsole console = provider.console(job, number, _maxLogSize);
		String text = console.truncated() ? TRUNCATED_MARK + NEW_LINE + console.text() : console.text();
		ProxyForwarding.send(exchange, SC_OK, ProxyForwarding.TEXT_CONTENT_TYPE, text.getBytes(StandardCharsets.UTF_8));
	}

	private static void sendJson(ProxyExchange exchange, Map<String, Object> value) throws IOException {
		ProxyForwarding.send(exchange, SC_OK, JSON_CONTENT_TYPE, JSON.toString(value).getBytes(StandardCharsets.UTF_8));
	}

	private static void notFound(ProxyExchange exchange) throws IOException {
		ProxyForwarding.sendText(exchange, SC_NOT_FOUND, com.top_logic.factory.proxy.ProxyError.NOT_FOUND.message());
	}

	/**
	 * The CI job of the project of the stored workspace with the given ID.
	 *
	 * @return The {@link CIJob#name() name} of the job, {@code null} if there is no such workspace
	 *         or its project has no CI job.
	 */
	public static String workspaceJob(String workspaceId) {
		return ThreadContext.<String, RuntimeException, RuntimeException> inSystemContext(CIRoute.class, () -> {
			TLObject workspace = FactoryModel.findWorkspace(workspaceId);
			return workspace == null ? null : FactoryModel.getCIJob(FactoryModel.getProject(workspace));
		});
	}

	/**
	 * What a request asks for.
	 */
	private enum Kind {
		/** {@code POST /builds} */
		START,

		/** {@code GET /builds/last} */
		LAST_BUILD,

		/** {@code GET /builds/<number>/log} */
		CONSOLE,
	}

	/**
	 * A request the route answers.
	 *
	 * @param kind
	 *        What it asks for.
	 * @param number
	 *        For {@link Kind#CONSOLE}, the number of the build; {@code 0} otherwise.
	 */
	private record CIRequest(Kind kind, int number) {

		/**
		 * The request of the given method and path below the route.
		 *
		 * @return The request, {@code null} if it is none the route answers.
		 */
		static CIRequest parse(String method, String path) {
			if (path.isEmpty() || path.charAt(0) != PATH_SEPARATOR) {
				return null;
			}
			String[] segments = path.substring(1).split(String.valueOf(PATH_SEPARATOR), -1);
			if (!BUILDS.equals(segments[0])) {
				return null;
			}
			switch (segments.length) {
				case 1:
					return POST.equals(method) ? new CIRequest(Kind.START, 0) : null;
				case 2:
					return GET.equals(method) && LAST.equals(segments[1]) ? new CIRequest(Kind.LAST_BUILD, 0) : null;
				case 3:
					if (GET.equals(method) && LOG.equals(segments[2]) && BUILD_NUMBER.matcher(segments[1]).matches()) {
						return new CIRequest(Kind.CONSOLE, Integer.parseInt(segments[1]));
					}
					return null;
				default:
					return null;
			}
		}
	}

}
