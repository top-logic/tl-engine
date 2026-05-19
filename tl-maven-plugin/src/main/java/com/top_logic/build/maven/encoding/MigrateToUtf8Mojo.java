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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven goal that migrates a TopLogic-based project from ISO-8859-1 to UTF-8.
 *
 * <p>
 * Intended as a one-shot migration helper when upgrading to a TopLogic version
 * that uses UTF-8 as the default source encoding. Invoke via
 * {@code mvn com.top-logic:tl-maven-plugin:migrate-to-utf8}.
 * </p>
 *
 * <p>
 * The goal handles three concerns:
 * </p>
 * <ol>
 *   <li><b>Source files</b> (default: {@code .java} and {@code .properties}):
 *       genuine ISO-8859-1 byte streams are rewritten as UTF-8. Pure ASCII
 *       files are skipped silently; files that are already valid UTF-8 are
 *       skipped and listed so callers can spot accidental mojibake.</li>
 *   <li><b>Eclipse encoding preferences</b>
 *       ({@code .settings/org.eclipse.core.resources.prefs}): the project
 *       default and the Java source folders are flipped to UTF-8, and
 *       obsolete per-path {@code =ISO-8859-1} overrides are removed.</li>
 *   <li><b>Maven POMs</b>: {@code <project.build.sourceEncoding>ISO-8859-1}
 *       declarations are rewritten to {@code UTF-8}.</li>
 * </ol>
 *
 * <p>
 * Each part can be skipped via the {@code skipSources}, {@code skipEclipse}
 * and {@code skipPoms} parameters.
 * </p>
 */
@Mojo(name = "migrate-to-utf8", requiresProject = false)
public class MigrateToUtf8Mojo extends AbstractMojo {

	private static final Set<String> PRUNE_DIRS =
		new HashSet<>(Arrays.asList("target", "build", "node_modules", ".git", "bin"));

	private static final String ECLIPSE_PREFS_NAME = "org.eclipse.core.resources.prefs";

	private static final Set<String> ECLIPSE_JAVA_KEYS =
		new HashSet<>(Arrays.asList("<project>", "src", "/src/main/java", "/src/test/java"));

	private static final Pattern ECLIPSE_ENCODING_LINE =
		Pattern.compile("^(encoding/)(.+?)=(.+)$");

	private static final Pattern POM_SOURCE_ENCODING =
		Pattern.compile(
			"<project\\.build\\.sourceEncoding>\\s*ISO-8859-1\\s*</project\\.build\\.sourceEncoding>");

	private static final String POM_SOURCE_ENCODING_REPLACEMENT =
		"<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>";

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
	 * File extensions to process during the source-file step (without leading dot).
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

	/**
	 * Skip the source-file conversion step.
	 */
	@Parameter(defaultValue = "false", property = "skipSources")
	private boolean skipSources;

	/**
	 * Skip the Eclipse {@code .settings/org.eclipse.core.resources.prefs} update.
	 */
	@Parameter(defaultValue = "false", property = "skipEclipse")
	private boolean skipEclipse;

	/**
	 * Skip the {@code <project.build.sourceEncoding>} update in {@code pom.xml} files.
	 */
	@Parameter(defaultValue = "false", property = "skipPoms")
	private boolean skipPoms;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Path root = baseDir != null ? baseDir.toPath() : Paths.get(".").toAbsolutePath().normalize();

		try {
			if (!skipSources) {
				migrateSources(root);
			}
			if (!skipEclipse) {
				migrateEclipsePrefs(root);
				migrateEclipsePropertiesFileOverrides(root);
			}
			if (!skipPoms) {
				migratePoms(root);
			}
		} catch (IOException ex) {
			throw new MojoExecutionException("Failed to walk " + root + ".", ex);
		}

		if (dryRun) {
			getLog().info("Re-run without -DdryRun to apply the changes above.");
		}
	}

	// --- Source files ---

	private void migrateSources(Path root) throws IOException, MojoFailureException {
		List<String> exts = parseExtensions();
		if (exts.isEmpty()) {
			throw new MojoFailureException("No file extensions configured.");
		}

		SourceStats stats = new SourceStats();
		Set<Path> alreadyUtf8 = new TreeSet<>();

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
					processSource(file, stats, alreadyUtf8);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		getLog().info("Source files (" + String.join(", ", exts) + "):");
		getLog().info("  Scanned:        " + stats.total() + " files");
		getLog().info("  Pure ASCII:     " + stats.ascii);
		getLog().info("  Already UTF-8:  " + stats.alreadyUtf8 + " (skipped)");
		getLog().info("  Converted:      " + stats.converted + " (" + (dryRun ? "dry run" : "written") + ")");

		if (!alreadyUtf8.isEmpty()) {
			getLog().info("  Files already containing UTF-8 byte sequences (skipped):");
			for (Path file : alreadyUtf8) {
				getLog().info("    " + root.relativize(file));
			}
		}
	}

	private List<String> parseExtensions() {
		List<String> result = new ArrayList<>();
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

	private void processSource(Path file, SourceStats stats, Set<Path> alreadyUtf8) throws IOException {
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

	// --- Eclipse prefs ---

	private void migrateEclipsePrefs(Path root) throws IOException {
		List<Path> prefs = new ArrayList<>();
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				if (!dir.equals(root) && PRUNE_DIRS.contains(dir.getFileName().toString())) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (file.getFileName().toString().equals(ECLIPSE_PREFS_NAME)) {
					prefs.add(file);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		int flipped = 0;
		int dropped = 0;
		int filesChanged = 0;
		for (Path file : prefs) {
			int[] outcome = updatePrefsFile(file);
			if (outcome[0] + outcome[1] > 0) {
				filesChanged++;
				flipped += outcome[0];
				dropped += outcome[1];
			}
		}

		getLog().info("Eclipse preferences:");
		getLog().info("  Files scanned:        " + prefs.size());
		getLog().info("  Files touched:        " + filesChanged + " ("
			+ (dryRun ? "dry run" : "written") + ")");
		getLog().info("  Entries flipped to UTF-8: " + flipped);
		getLog().info("  Stale ISO-8859-1 overrides removed: " + dropped);
	}

	/** Returns int[2] = { flipped, dropped }. */
	private int[] updatePrefsFile(Path file) throws IOException {
		List<String> input = Files.readAllLines(file, StandardCharsets.UTF_8);
		List<String> output = new ArrayList<>(input.size());
		int flipped = 0;
		int dropped = 0;
		for (String line : input) {
			java.util.regex.Matcher m = ECLIPSE_ENCODING_LINE.matcher(line);
			if (m.matches()) {
				String key = m.group(2);
				String value = m.group(3).trim();
				if (ECLIPSE_JAVA_KEYS.contains(key)) {
					String flippedLine = "encoding/" + key + "=UTF-8";
					if (!flippedLine.equals(line)) {
						flipped++;
					}
					output.add(flippedLine);
					continue;
				}
				if ("ISO-8859-1".equalsIgnoreCase(value)) {
					dropped++;
					continue;
				}
			}
			output.add(line);
		}

		if (flipped == 0 && dropped == 0) {
			return new int[] { 0, 0 };
		}

		if (!dryRun) {
			byte[] originalBytes = Files.readAllBytes(file);
			boolean trailingNewline = originalBytes.length > 0 && originalBytes[originalBytes.length - 1] == '\n';
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < output.size(); i++) {
				buf.append(output.get(i));
				if (i < output.size() - 1 || trailingNewline) {
					buf.append('\n');
				}
			}
			Files.write(file, buf.toString().getBytes(StandardCharsets.UTF_8));
		}
		return new int[] { flipped, dropped };
	}

	// --- Eclipse .properties per-file overrides ---

	private void migrateEclipsePropertiesFileOverrides(Path root) throws IOException {
		List<Path> moduleDirs = new ArrayList<>();
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				if (!dir.equals(root) && PRUNE_DIRS.contains(dir.getFileName().toString())) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (file.getFileName().toString().equals("pom.xml")) {
					moduleDirs.add(file.getParent());
				}
				return FileVisitResult.CONTINUE;
			}
		});

		int modulesTouched = 0;
		int entriesAdded = 0;
		for (Path module : moduleDirs) {
			List<String> propertiesFiles = collectPropertiesFiles(module);
			if (propertiesFiles.isEmpty()) {
				continue;
			}
			Path prefs = module.resolve(".settings").resolve(ECLIPSE_PREFS_NAME);
			int added = addPropertiesFileEntries(prefs, propertiesFiles);
			if (added > 0) {
				modulesTouched++;
				entriesAdded += added;
			}
		}

		getLog().info("Eclipse .properties per-file overrides:");
		getLog().info("  Modules scanned: " + moduleDirs.size());
		getLog().info("  Modules touched: " + modulesTouched);
		getLog().info("  Entries added:   " + entriesAdded + " ("
			+ (dryRun ? "dry run" : "written") + ")");
	}

	private List<String> collectPropertiesFiles(Path module) throws IOException {
		List<String> out = new ArrayList<>();
		Path moduleAbs = module.toAbsolutePath().normalize();
		Files.walkFileTree(module, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				if (dir.equals(module)) {
					return FileVisitResult.CONTINUE;
				}
				String name = dir.getFileName().toString();
				if (PRUNE_DIRS.contains(name) || ".settings".equals(name)) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				// Prune nested Maven modules.
				if (Files.isRegularFile(dir.resolve("pom.xml"))) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (file.getFileName().toString().endsWith(".properties")) {
					Path rel = moduleAbs.relativize(file.toAbsolutePath().normalize());
					String posix = rel.toString().replace(File.separatorChar, '/');
					out.add("/" + posix);
				}
				return FileVisitResult.CONTINUE;
			}
		});
		java.util.Collections.sort(out);
		return out;
	}

	private int addPropertiesFileEntries(Path prefs, List<String> propertiesFiles) throws IOException {
		List<String> lines;
		if (Files.exists(prefs)) {
			lines = new ArrayList<>(Files.readAllLines(prefs, StandardCharsets.UTF_8));
		} else {
			lines = new ArrayList<>();
			lines.add("eclipse.preferences.version=1");
		}
		Set<String> keys = new HashSet<>();
		for (String line : lines) {
			int eq = line.indexOf('=');
			if (eq > 0) {
				keys.add(line.substring(0, eq));
			}
		}
		int added = 0;
		for (String path : propertiesFiles) {
			String key = "encoding/" + path;
			if (keys.contains(key)) {
				continue;
			}
			lines.add(key + "=UTF-8");
			added++;
		}
		if (added == 0) {
			return 0;
		}
		if (!dryRun) {
			Files.createDirectories(prefs.getParent());
			StringBuilder buf = new StringBuilder();
			for (String line : lines) {
				buf.append(line).append('\n');
			}
			Files.write(prefs, buf.toString().getBytes(StandardCharsets.UTF_8));
		}
		return added;
	}

	// --- POMs ---

	private void migratePoms(Path root) throws IOException {
		List<Path> poms = new ArrayList<>();
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				if (!dir.equals(root) && PRUNE_DIRS.contains(dir.getFileName().toString())) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (file.getFileName().toString().equals("pom.xml")) {
					poms.add(file);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		int changed = 0;
		for (Path pom : poms) {
			String text = new String(Files.readAllBytes(pom), StandardCharsets.UTF_8);
			String updated = POM_SOURCE_ENCODING.matcher(text).replaceAll(POM_SOURCE_ENCODING_REPLACEMENT);
			if (!updated.equals(text)) {
				changed++;
				if (!dryRun) {
					Files.write(pom, updated.getBytes(StandardCharsets.UTF_8));
				}
			}
		}

		getLog().info("Maven POMs:");
		getLog().info("  Files scanned:                            " + poms.size());
		getLog().info("  project.build.sourceEncoding flipped:     " + changed
			+ " (" + (dryRun ? "dry run" : "written") + ")");
	}

	enum Kind {
		ASCII, ALREADY_UTF8, ISO_8859_1
	}

	private static final class SourceStats {
		int ascii;
		int alreadyUtf8;
		int converted;

		int total() {
			return ascii + alreadyUtf8 + converted;
		}
	}
}
