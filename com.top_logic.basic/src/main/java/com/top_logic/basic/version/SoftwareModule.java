/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.basic.Logger;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.basic.version.model.VersionInfo;
import com.top_logic.basic.xml.DOMUtil;

/**
 * The software module (Maven artifact) a class is delivered with.
 *
 * <p>
 * The module is derived from where the class is loaded from: in a deployed application a jar file
 * (its {@code META-INF/maven/<group>/<artifact>/pom.properties}, or the jar file name), or the
 * {@code WEB-INF/classes} folder of the application; during development the {@code target/classes}
 * folder of a module, whose {@code pom.xml} names the artifact. The {@link #label() label} is
 * the human-readable name the {@link Version#getApplicationVersion() version information of the
 * application} gives the artifact, if it has one.
 * </p>
 *
 * @param artifactId
 *        The artifact ID of the module, <code>null</code> if it cannot be determined.
 * @param label
 *        A human-readable name of the module.
 */
public record SoftwareModule(String artifactId, String label) {

	/** Name of the file describing a Maven artifact within its jar. */
	private static final String POM_PROPERTIES = "pom.properties";

	/** Folder of the Maven descriptors within a jar. */
	private static final String MAVEN_FOLDER = "META-INF/maven/";

	/** Property of {@link #POM_PROPERTIES} holding the artifact ID. */
	private static final String ARTIFACT_ID_PROPERTY = "artifactId";

	/** Name of the Maven project descriptor of a module. */
	private static final String POM_XML = "pom.xml";

	/** Element of {@link #POM_XML} holding the artifact ID. */
	private static final String ARTIFACT_ID_ELEMENT = "artifactId";

	/** Folder of the compiled classes of a deployed web application. */
	private static final String WEB_INF_CLASSES = "WEB-INF" + File.separator + "classes";

	/** File name of a jar: the artifact ID followed by a version starting with a digit. */
	private static final Pattern VERSIONED_JAR = Pattern.compile("(.+?)-\\d.*\\.jar");

	/** Suffix of a jar file. */
	private static final String JAR_SUFFIX = ".jar";

	/** The module of a class that is loaded from no file, e.g. a class of the Java runtime. */
	public static final SoftwareModule UNKNOWN = new SoftwareModule(null, "");

	private static final ClassValue<SoftwareModule> MODULES = new ClassValue<>() {
		@Override
		protected SoftwareModule computeValue(Class<?> type) {
			return compute(type);
		}
	};

	/**
	 * The module the given class is delivered with.
	 *
	 * @param type
	 *        The class to look up.
	 * @return The module, {@link #UNKNOWN} if it cannot be determined. The result is computed once
	 *         per class.
	 */
	public static SoftwareModule of(Class<?> type) {
		return MODULES.get(type);
	}

	private static SoftwareModule compute(Class<?> type) {
		File location = location(type);
		if (location == null) {
			return UNKNOWN;
		}
		if (location.isDirectory()) {
			return ofDirectory(location);
		}
		return named(artifactOfJar(location));
	}

	/**
	 * The file or folder the given class is loaded from, <code>null</code> if it is not loaded from
	 * a file.
	 */
	private static File location(Class<?> type) {
		ProtectionDomain domain = type.getProtectionDomain();
		CodeSource source = domain == null ? null : domain.getCodeSource();
		URL url = source == null ? null : source.getLocation();
		if (url == null || !"file".equals(url.getProtocol())) {
			return null;
		}
		try {
			return new File(url.toURI());
		} catch (URISyntaxException | IllegalArgumentException ex) {
			return null;
		}
	}

	private static SoftwareModule ofDirectory(File classes) {
		if (classes.getPath().endsWith(WEB_INF_CLASSES)) {
			return application();
		}
		File moduleDir = canonical(moduleDir(classes));
		if (moduleDir == null) {
			return UNKNOWN;
		}
		String artifactId = artifactOfPom(new File(moduleDir, POM_XML));
		if (moduleDir.equals(applicationDir())) {
			return new SoftwareModule(artifactId, Version.getApplicationVersion().getName());
		}
		return named(artifactId);
	}

	/**
	 * The closest folder above the given one that holds a {@link #POM_XML}, e.g. the module folder
	 * of its {@code target/classes} folder.
	 */
	private static File moduleDir(File classes) {
		for (File dir = classes; dir != null; dir = dir.getParentFile()) {
			if (new File(dir, POM_XML).isFile()) {
				return dir;
			}
		}
		return null;
	}

	private static File canonical(File file) {
		if (file == null) {
			return null;
		}
		try {
			return file.getCanonicalFile();
		} catch (IOException ex) {
			return file.getAbsoluteFile();
		}
	}

	/**
	 * The folder of the module of the running application during development, <code>null</code>
	 * if it is not known.
	 */
	private static File applicationDir() {
		try {
			return Workspace.topLevelProjectDirectory().getCanonicalFile();
		} catch (RuntimeException | IOException ex) {
			return null;
		}
	}

	private static SoftwareModule application() {
		Version version = Version.getApplicationVersion();
		List<VersionInfo> dependencies = version.getDependencies();
		String artifactId = dependencies.get(dependencies.size() - 1).getArtifactId();
		return new SoftwareModule(artifactId.isEmpty() ? null : artifactId, version.getName());
	}

	/**
	 * The module of the given artifact, labeled with the name the version information of the
	 * application gives it.
	 */
	private static SoftwareModule named(String artifactId) {
		if (artifactId == null) {
			return UNKNOWN;
		}
		for (VersionInfo info : Version.getApplicationVersion().getDependencies()) {
			if (artifactId.equals(info.getArtifactId()) && info.hasName() && !info.getName().isEmpty()) {
				return new SoftwareModule(artifactId, info.getName());
			}
		}
		return new SoftwareModule(artifactId, artifactId);
	}

	/**
	 * The artifact ID of the given jar: from its Maven descriptor, if it holds exactly one, otherwise
	 * from its file name.
	 */
	private static String artifactOfJar(File jar) {
		List<String> artifacts = new ArrayList<>();
		try (JarFile file = new JarFile(jar)) {
			Enumeration<JarEntry> entries = file.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.startsWith(MAVEN_FOLDER) && name.endsWith("/" + POM_PROPERTIES)) {
					Properties properties = new Properties();
					try (InputStream in = file.getInputStream(entry)) {
						properties.load(in);
					}
					String artifactId = properties.getProperty(ARTIFACT_ID_PROPERTY);
					if (artifactId != null) {
						artifacts.add(artifactId);
					}
				}
			}
		} catch (IOException ex) {
			Logger.info("Cannot read jar file " + jar + ": " + ex.getMessage(), SoftwareModule.class);
		}
		if (artifacts.size() == 1) {
			return artifacts.get(0);
		}
		return artifactOfFileName(jar.getName());
	}

	/**
	 * The artifact ID a jar file name stands for, the name without version and suffix.
	 */
	public static String artifactOfFileName(String fileName) {
		Matcher matcher = VERSIONED_JAR.matcher(fileName);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		if (fileName.endsWith(JAR_SUFFIX)) {
			return fileName.substring(0, fileName.length() - JAR_SUFFIX.length());
		}
		return fileName;
	}

	/**
	 * The artifact ID the given Maven project descriptor declares for its own project.
	 */
	private static String artifactOfPom(File pom) {
		try {
			DocumentBuilder builder = DOMUtil.getDocumentBuilder();
			Element project = builder.parse(pom).getDocumentElement();
			for (Node child = project.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child instanceof Element element && ARTIFACT_ID_ELEMENT.equals(element.getLocalName() != null
					? element.getLocalName() : element.getTagName())) {
					String artifactId = element.getTextContent().trim();
					return artifactId.isEmpty() ? null : artifactId;
				}
			}
		} catch (Exception ex) {
			Logger.info("Cannot read " + pom + ": " + ex.getMessage(), SoftwareModule.class);
		}
		return null;
	}

}
