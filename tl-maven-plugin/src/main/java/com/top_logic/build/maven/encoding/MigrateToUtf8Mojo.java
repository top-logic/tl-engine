/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.encoding;

import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven goal that migrates project source and properties files from ISO-8859-1
 * to UTF-8.
 *
 * <p>
 * Intended as a one-shot migration helper for TopLogic-based projects when
 * upgrading to a TopLogic version that uses UTF-8 as the default source
 * encoding. Invoke via
 * {@code mvn com.top-logic:tl-maven-plugin:migrate-to-utf8}.
 * </p>
 *
 * <p>
 * Per file:
 * </p>
 * <ol>
 *   <li>If the bytes are pure ASCII, nothing changes.</li>
 *   <li>If the bytes are valid UTF-8 with at least one non-ASCII sequence, the
 *       file is treated as already UTF-8 and left untouched.</li>
 *   <li>Otherwise the bytes are decoded as ISO-8859-1 and rewritten as
 *       UTF-8.</li>
 * </ol>
 *
 * <p>
 * Files that fall into category 2 are reported separately so callers can
 * decide whether they are genuinely UTF-8 (intentional) or mojibake from a
 * prior incorrect round-trip.
 * </p>
 */
@Mojo(name = "migrate-to-utf8", requiresProject = false)
public class MigrateToUtf8Mojo extends AbstractMojo {

	private static final Set<String> PRUNE_DIRS =
		new HashSet<>(Arrays.asList("target", "build", "node_modules", ".git", "bin"));

	/**
	 * Root directory whose source tree is migrated.
	 *
	 * <p>
	 * Defaults to the current project base directory when run inside a Maven project, or the
	 * working directory when invoked without a project (e.g. as a one-shot CLI migration).
	 * </p>
	 */
	@Parameter(defaultValue = "${project.basedir}", property = "baseDir")
	private File baseDir;

	/**
	 * File extensions to process (without leading dot).
	 *
	 * <p>
	 * Defaults to {@code java,properties}. Pass via
	 * {@code -Dextensions=java,properties,xml} on the command line.
	 * </p>
	 */
	@Parameter(defaultValue = "java,properties", property = "extensions")
	private String extensions;

	/**
	 * If set to {@code true}, report what would change but write nothing.
	 */
	@Parameter(defaultValue = "false", property = "dryRun")
	private boolean dryRun;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		List<String> exts = parseExtensions();
		if (exts.isEmpty()) {
			throw new MojoFailureException("No file extensions configured.");
		}

		Path root = baseDir != null ? baseDir.toPath() : Paths.get(".").toAbsolutePath().normalize();

		Stats stats = new Stats();
		Set<Path> alreadyUtf8 = new TreeSet<>();

		try {
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					if (!dir.equals(root) && PRUNE_DIRS.contains(dir.getFileName().toString())) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (matchesExtension(file, exts)) {
						process(file, stats, alreadyUtf8);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException ex) {
			throw new MojoExecutionException("Failed to walk " + root + ".", ex);
		}

		getLog().info("Scanned:        " + stats.total() + " files");
		getLog().info("Pure ASCII:     " + stats.ascii);
		getLog().info("Already UTF-8:  " + stats.alreadyUtf8 + " (skipped)");
		getLog().info("Converted:      " + stats.converted + " (" + (dryRun ? "dry run" : "written") + ")");

		if (!alreadyUtf8.isEmpty()) {
			getLog().info("Files already containing UTF-8 byte sequences (skipped):");
			for (Path file : alreadyUtf8) {
				getLog().info("  " + root.relativize(file));
			}
		}

		if (stats.converted > 0 && dryRun) {
			getLog().info("Re-run without -DdryRun to rewrite the converted files.");
		}
	}

	private List<String> parseExtensions() {
		List<String> result = new java.util.ArrayList<>();
		for (String ext : extensions.split(",")) {
			String trimmed = ext.trim();
			if (!trimmed.isEmpty()) {
				result.add(trimmed.startsWith(".") ? trimmed.substring(1) : trimmed);
			}
		}
		return result;
	}

	private boolean matchesExtension(Path file, List<String> exts) {
		String name = file.getFileName().toString();
		int dot = name.lastIndexOf('.');
		if (dot < 0) {
			return false;
		}
		String ext = name.substring(dot + 1);
		return exts.contains(ext);
	}

	private void process(Path file, Stats stats, Set<Path> alreadyUtf8) throws IOException {
		byte[] data = Files.readAllBytes(file);
		Kind kind = classify(data);
		switch (kind) {
			case ASCII:
				stats.ascii++;
				break;
			case ALREADY_UTF8:
				stats.alreadyUtf8++;
				alreadyUtf8.add(file);
				break;
			case ISO_8859_1:
				stats.converted++;
				if (!dryRun) {
					String text = new String(data, StandardCharsets.ISO_8859_1);
					Files.write(file, text.getBytes(StandardCharsets.UTF_8));
				}
				break;
		}
	}

	static Kind classify(byte[] data) {
		boolean anyHighBit = false;
		for (byte b : data) {
			if ((b & 0x80) != 0) {
				anyHighBit = true;
				break;
			}
		}
		if (!anyHighBit) {
			return Kind.ASCII;
		}
		if (isValidUtf8(data)) {
			return Kind.ALREADY_UTF8;
		}
		return Kind.ISO_8859_1;
	}

	private static boolean isValidUtf8(byte[] data) {
		Charset utf8 = StandardCharsets.UTF_8;
		try {
			utf8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT)
				.decode(java.nio.ByteBuffer.wrap(data));
			return true;
		} catch (CharacterCodingException ex) {
			return false;
		}
	}

	enum Kind {
		ASCII, ALREADY_UTF8, ISO_8859_1
	}

	private static final class Stats {
		int ascii;
		int alreadyUtf8;
		int converted;

		int total() {
			return ascii + alreadyUtf8 + converted;
		}
	}
}
