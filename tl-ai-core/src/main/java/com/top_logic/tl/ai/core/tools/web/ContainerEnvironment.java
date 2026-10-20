/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Detects whether this JVM runs inside an OS container (Docker, Podman, containerd/Kubernetes, LXC).
 *
 * <p>
 * Consulted for exactly one decision: whether Chromium's OS-level sandbox has to be given up (see
 * {@code BrowserToolsService.Config#getBrowserSandbox()}). The sandbox is what keeps attacker-
 * controlled page JavaScript inside its renderer, so it must stay on wherever it can work; the
 * container case is the one where the default runtime profile blocks the sandbox's own
 * {@code clone}/{@code unshare} calls and Chromium refuses to start without {@code --no-sandbox}.
 * Getting this answer wrong in the permissive direction (reporting a container that is not one)
 * would silently disarm a security control on an ordinary host, so every signal below is one only a
 * container sets.
 * </p>
 *
 * <p>
 * <b>Why several signals and not one.</b> No single check covers the field:
 * </p>
 * <ul>
 * <li>{@code /.dockerenv} is written by Docker only - Podman, containerd/CRI (so plain Kubernetes)
 * and LXC do not create it.</li>
 * <li>{@code /run/.containerenv} is the Podman/libpod counterpart.</li>
 * <li>{@code /proc/1/cgroup} naming a {@code docker}/{@code kubepods}/{@code containerd}/{@code lxc}
 * /{@code libpod} path is the classic check and covers runtimes that write neither marker file - but
 * it is a <b>cgroup v1</b> artefact. Under the unified (v2) hierarchy that current distributions
 * default to, a container's pid 1 typically reads back a bare {@code 0::/}, which says nothing.
 * That is precisely why it cannot be the only signal.</li>
 * <li>{@code /run/systemd/container} is what systemd itself writes when it booted as a container's
 * pid 1 (systemd-nspawn, LXC).</li>
 * </ul>
 *
 * <p>
 * The remaining canonical signal - {@code container=} in pid 1's environment, which
 * {@code systemd-detect-virt} reads - is deliberately <b>not</b> used: {@code /proc/1/environ} is
 * readable by root only, and the application does not run as root, so it would contribute nothing
 * but a stack trace.
 * </p>
 *
 * <p>
 * Non-Linux hosts short-circuit to "not a container": all of the above live in the Linux
 * {@code /proc} and {@code /run} filesystems, and the container deployments this exists for are
 * Linux.
 * </p>
 */
final class ContainerEnvironment {

	/**
	 * System property that, when set to {@code true}/{@code false}, overrides the detection.
	 *
	 * <p>
	 * For tests of the sandbox decision, which have to be able to exercise both branches on whatever
	 * host they happen to run on. It is <em>not</em> the operator escape hatch - that is the typed
	 * configuration property, which can also force the sandbox on inside a container.
	 * </p>
	 */
	static final String FORCE_PROPERTY = "tl.ai.browser.container";

	/** Marker file Docker writes into every container's root filesystem. */
	private static final Path DOCKER_MARKER = Path.of("/.dockerenv");

	/** Marker file Podman/libpod writes into every container. */
	private static final Path PODMAN_MARKER = Path.of("/run/.containerenv");

	/** Marker file systemd writes when it booted as the pid 1 of a container. */
	private static final Path SYSTEMD_CONTAINER_MARKER = Path.of("/run/systemd/container");

	/** The cgroup path of pid 1, which under cgroup v1 names the runtime that started the container. */
	private static final Path PID1_CGROUP = Path.of("/proc/1/cgroup");

	/** Container-runtime path segments as they appear in a cgroup v1 hierarchy of pid 1. */
	private static final List<String> CGROUP_RUNTIME_MARKERS =
		List.of("/docker", "/docker-", "/kubepods", "/containerd", "/lxc", "/libpod", "/crio-", "/podman");

	private ContainerEnvironment() {
		// Static utility.
	}

	/**
	 * Whether this JVM runs inside an OS container.
	 *
	 * @return {@code true} when any container signal is present; {@code false} when none is, when
	 *         the host is not Linux, or when {@code /proc} is not readable.
	 */
	static boolean isContainer() {
		String forced = System.getProperty(FORCE_PROPERTY);
		if (forced != null && !forced.isBlank()) {
			return Boolean.parseBoolean(forced.trim());
		}
		if (!isLinux()) {
			return false;
		}
		return Files.exists(DOCKER_MARKER)
			|| Files.exists(PODMAN_MARKER)
			|| Files.exists(SYSTEMD_CONTAINER_MARKER)
			|| pid1IsInARuntimeCgroup();
	}

	private static boolean isLinux() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
	}

	/**
	 * Whether pid 1's cgroup path names a container runtime - the cgroup v1 signal. Silently
	 * {@code false} when {@code /proc/1/cgroup} cannot be read (a hardened {@code /proc} mount) or
	 * when the unified hierarchy leaves it uninformative, because the marker files above are what
	 * carry the answer in those cases.
	 */
	private static boolean pid1IsInARuntimeCgroup() {
		String cgroups;
		try {
			cgroups = Files.readString(PID1_CGROUP, StandardCharsets.UTF_8);
		} catch (IOException | RuntimeException unreadable) {
			return false;
		}
		String normalized = cgroups.toLowerCase(Locale.ROOT);
		for (String marker : CGROUP_RUNTIME_MARKERS) {
			if (normalized.contains(marker)) {
				return true;
			}
		}
		return false;
	}
}
