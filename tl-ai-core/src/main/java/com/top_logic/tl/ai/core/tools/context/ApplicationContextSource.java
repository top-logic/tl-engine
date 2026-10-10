/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.top_logic.tl.ai.core.agent.TextPromptContributor;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.tools.context.XmlSpans.Element;

/**
 * The application context in the application's source configuration: the
 * {@link TextPromptContributor} named {@value TextPromptContributor.Config#APPLICATION_CONTEXT}
 * among the prompt contributors of {@link LLMService}, in a file of the application's
 * {@code WEB-INF/autoconf} folder.
 *
 * <p>
 * Which file it writes:
 * </p>
 * <ol>
 * <li>Every file of the folder that already holds the entry: its text is replaced there. Normally that
 * is one file; a second one appears when the service editor saved {@code LLMService} after the entry
 * was written, because its snapshot carries the entry too.</li>
 * <li>Otherwise the file that configures {@code LLMService} and is read last - files of the folder are
 * read in the order of their names. The entry is added to it: when that file replaces the service's
 * configuration ({@code config:override="true"}), an entry in any earlier file would be dropped.</li>
 * <li>Otherwise a new file, {@value #NEW_FILE}, that adds the entry without {@code config:override}.
 * The prompt contributors are a list without a key, and the engine appends the entries of a later
 * configuration layer to that list, so the file adds one entry and leaves everything else tl-ai and
 * the application configured. It deliberately does not have the name the service editor uses: a file
 * of the same name in the application hides the file tl-ai-core ships with that name, and with it
 * tl-ai's own settings of the service. Its name sorts before that one, so a later snapshot of the
 * service editor - which then contains the entry - replaces this file's entry rather than adding it a
 * second time.</li>
 * </ol>
 *
 * <p>
 * A file is changed in place: only the entry is written anew, every other character of the file stays
 * as the developer wrote it. The text goes into every language the application supports, in a CDATA
 * section, so that a user of any language is given it and no fallback language decides.
 * </p>
 */
final class ApplicationContextSource {

	/** The folder below the web application that holds the configuration files read automatically. */
	static final String AUTOCONF = "WEB-INF/autoconf";

	/** The file written when the application configures {@link LLMService} in no file of its own. */
	static final String NEW_FILE = "ai-application-context.config.xml";

	private static final String FILE_SUFFIX = ".config.xml";

	private static final String SERVICE = LLMService.class.getName();

	private static final String CONTRIBUTOR = TextPromptContributor.class.getName();

	private static final String NAME = TextPromptContributor.Config.APPLICATION_CONTEXT;

	private final Path _webapp;

	/**
	 * Creates an {@link ApplicationContextSource} for the web application folder of an application's
	 * source.
	 */
	ApplicationContextSource(Path webapp) {
		_webapp = webapp;
	}

	/**
	 * The folder the files are in.
	 */
	Path folder() {
		return _webapp.resolve(AUTOCONF);
	}

	/**
	 * Where a file is, relative to the application's project, as a developer finds it.
	 */
	String display(Path file) {
		Path sources = Path.of("src", "main", "webapp");
		Path project = _webapp.endsWith(sources) ? _webapp.getParent().getParent().getParent() : _webapp;
		return project.relativize(file).toString().replace('\\', '/');
	}

	/**
	 * The application context the source configuration holds.
	 *
	 * @param file
	 *        The file it is in.
	 * @param texts
	 *        Its text per language.
	 */
	record Found(Path file, Map<String, String> texts) {
		// A plain result.
	}

	/**
	 * The application context the source configuration holds, from the first file that has the
	 * entry; <code>null</code> when none has.
	 */
	Found read() throws IOException {
		for (Path file : serviceFiles()) {
			Map<String, String> context = contextIn(Files.readString(file, StandardCharsets.UTF_8));
			if (context != null) {
				return new Found(file, context);
			}
		}
		return null;
	}

	/**
	 * What {@link #save(String, List)} did.
	 *
	 * @param files
	 *        The files written.
	 * @param created
	 *        Whether the one file was created, because the application configured {@link LLMService}
	 *        in no file of the folder.
	 * @param added
	 *        Whether the entry was added rather than its text replaced.
	 */
	record Saved(List<Path> files, boolean created, boolean added) {
		// A plain result.
	}

	/**
	 * Writes the text as the application context into the source configuration.
	 *
	 * @param text
	 *        The application context.
	 * @param languages
	 *        The languages to write it in, e.g. {@code de, en}.
	 */
	Saved save(String text, List<String> languages) throws IOException {
		List<Path> serviceFiles = serviceFiles();
		List<Path> holding = new ArrayList<>();
		for (Path file : serviceFiles) {
			if (contextIn(Files.readString(file, StandardCharsets.UTF_8)) != null) {
				holding.add(file);
			}
		}
		if (!holding.isEmpty()) {
			for (Path file : holding) {
				rewrite(file, text, languages);
			}
			return new Saved(holding, false, false);
		}
		if (!serviceFiles.isEmpty()) {
			Path last = serviceFiles.get(serviceFiles.size() - 1);
			rewrite(last, text, languages);
			return new Saved(List.of(last), false, true);
		}
		Path created = folder().resolve(NEW_FILE);
		Files.createDirectories(created.getParent());
		Files.writeString(created, newFile(text, languages), StandardCharsets.UTF_8);
		return new Saved(List.of(created), true, true);
	}

	private static void rewrite(Path file, String text, List<String> languages) throws IOException {
		String xml = Files.readString(file, StandardCharsets.UTF_8);
		Files.writeString(file, withContext(xml, text, languages), StandardCharsets.UTF_8);
	}

	/**
	 * The files of the folder that configure {@link LLMService}, in the order the engine reads them.
	 *
	 * @throws IOException
	 *         When a file that names the service cannot be read as XML: writing next to a file nobody
	 *         understands could put the entry where it has no effect.
	 */
	List<Path> serviceFiles() throws IOException {
		Path folder = folder();
		if (!Files.isDirectory(folder)) {
			return List.of();
		}
		List<Path> files;
		try (Stream<Path> listing = Files.list(folder)) {
			files = listing
				.filter(file -> file.getFileName().toString().endsWith(FILE_SUFFIX) && Files.isRegularFile(file))
				.sorted((a, b) -> a.getFileName().toString().compareTo(b.getFileName().toString()))
				.toList();
		}
		List<Path> result = new ArrayList<>();
		for (Path file : files) {
			String xml = Files.readString(file, StandardCharsets.UTF_8);
			if (!xml.contains(SERVICE)) {
				continue;
			}
			try {
				if (serviceEntry(XmlSpans.parse(xml)) != null) {
					result.add(file);
				}
			} catch (IllegalArgumentException ex) {
				throw new IOException("Cannot read " + file.getFileName() + ": " + ex.getMessage(), ex);
			}
		}
		return result;
	}

	/**
	 * The application context the given configuration file holds, per language; <code>null</code>
	 * when it has no such entry.
	 */
	static Map<String, String> contextIn(String xml) {
		Element entry = serviceEntry(XmlSpans.parse(xml));
		for (Element contributor : contributors(entry)) {
			Element text = contributor.child(TextPromptContributor.Config.TEXT);
			Map<String, String> result = new LinkedHashMap<>();
			if (text != null) {
				for (Element language : text.children()) {
					result.put(language.name(), language.text(xml).strip());
				}
			}
			return result;
		}
		return null;
	}

	/**
	 * The given configuration file with the text as its application context.
	 *
	 * @param xml
	 *        A configuration file that configures {@link LLMService}.
	 * @param text
	 *        The application context.
	 * @param languages
	 *        The languages to write it in.
	 * @return The file with the entry replaced, or added to the service's prompt contributors; every
	 *         other character as it was.
	 */
	static String withContext(String xml, String text, List<String> languages) {
		Element entry = serviceEntry(XmlSpans.parse(xml));
		if (entry == null) {
			throw new IllegalArgumentException("The file does not configure " + SERVICE + ".");
		}
		Layout layout = Layout.of(xml);
		List<Element> existing = contributors(entry);
		if (!existing.isEmpty()) {
			String result = xml;
			// From the last to the first, so that the positions of the ones before stay valid.
			for (int n = existing.size() - 1; n >= 0; n--) {
				Element contributor = existing.get(n);
				String indent = indentOf(xml, contributor);
				result = result.substring(0, contributor.start())
					+ layout.contributor(indent, contributor.attribute(TextPromptContributor.Config.AGENTS), text,
						languages)
					+ result.substring(contributor.end());
			}
			return result;
		}
		Element instance = entry.child("instance");
		if (instance == null) {
			return layout.append(xml, entry, indent -> layout.instance(indent, text, languages));
		}
		Element contributors = instance.child(LLMService.Config.PROMPT_CONTRIBUTORS);
		if (contributors == null) {
			return layout.append(xml, instance, indent -> layout.promptContributors(indent, text, languages));
		}
		return layout.append(xml, contributors, indent -> layout.contributor(indent, null, text, languages));
	}

	/**
	 * A configuration file that adds the text as the application context to {@link LLMService}, and
	 * nothing else.
	 */
	static String newFile(String text, List<String> languages) {
		Layout layout = new Layout("\t", "\n");
		return "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"
			+ "\n"
			+ "<!--\n"
			+ "\tThe application context: what every agent of this application is told about it in its system\n"
			+ "\tprompt. Written with Albert's TL_init skill; revise it there or here. A change takes effect\n"
			+ "\twhen the LLM service is restarted.\n"
			+ "\n"
			+ "\tNo config:override: this file adds one entry to the prompt contributors of LLMService and\n"
			+ "\tleaves the rest of the service's configuration as tl-ai and this application set it.\n"
			+ "-->\n"
			+ "<application config:interface=\"com.top_logic.basic.config.ApplicationConfig$Config\"\n"
			+ "\txmlns:config=\"http://www.top-logic.com/ns/config/6.0\"\n"
			+ ">\n"
			+ "\t<services>\n"
			+ "\t\t<config service-class=\"" + SERVICE + "\">\n"
			+ "\t\t\t" + layout.instance("\t\t\t", text, languages) + "\n"
			+ "\t\t</config>\n"
			+ "\t</services>\n"
			+ "</application>\n";
	}

	private static Element serviceEntry(Element root) {
		if (!"application".equals(root.name())) {
			return null;
		}
		Element services = root.child("services");
		if (services == null) {
			return null;
		}
		for (Element config : services.children()) {
			if (SERVICE.equals(config.attribute("service-class"))) {
				return config;
			}
		}
		return null;
	}

	private static List<Element> contributors(Element entry) {
		if (entry == null) {
			return List.of();
		}
		Element instance = entry.child("instance");
		Element list = instance == null ? null : instance.child(LLMService.Config.PROMPT_CONTRIBUTORS);
		if (list == null) {
			return List.of();
		}
		return list.children().stream()
			.filter(contributor -> NAME.equals(contributor.attribute(TextPromptContributor.Config.NAME)))
			.toList();
	}

	private static String indentOf(String xml, Element element) {
		String indent = XmlSpans.indentBefore(xml, element.start());
		return indent == null ? "" : indent;
	}

	/**
	 * How a file indents and ends its lines, so that what is written looks like the rest of it.
	 */
	private record Layout(String unit, String newline) {

		static Layout of(String xml) {
			String newline = xml.contains("\r\n") ? "\r\n" : "\n";
			String unit = null;
			int spaces = Integer.MAX_VALUE;
			for (String line : xml.split("\n")) {
				if (line.startsWith("\t")) {
					unit = "\t";
					break;
				}
				int count = 0;
				while (count < line.length() && line.charAt(count) == ' ') {
					count++;
				}
				if (count > 0 && count < line.length() && count < spaces) {
					spaces = count;
				}
			}
			if (unit == null) {
				unit = spaces == Integer.MAX_VALUE ? "\t" : " ".repeat(spaces);
			}
			return new Layout(unit, newline);
		}

		/**
		 * Adds a child element at the end of the given element's content.
		 */
		String append(String xml, Element parent, Function<String, String> child) {
			String parentIndent = indentOf(xml, parent);
			String childIndent = parentIndent + unit;
			String rendered = child.apply(childIndent);
			if (parent.isEmpty()) {
				String tag = xml.substring(parent.start(), parent.end());
				String open = tag.substring(0, tag.length() - 2);
				open = (open.substring(open.lastIndexOf('\n') + 1).isBlank() ? open : open.stripTrailing()) + ">";
				return xml.substring(0, parent.start())
					+ open + newline + childIndent + rendered + newline + parentIndent + "</" + parent.name() + ">"
					+ xml.substring(parent.end());
			}
			int close = parent.contentEnd();
			if (XmlSpans.indentBefore(xml, close) != null) {
				int at = XmlSpans.lineStart(xml, close);
				return xml.substring(0, at) + childIndent + rendered + newline + xml.substring(at);
			}
			return xml.substring(0, close) + newline + childIndent + rendered + newline + parentIndent
				+ xml.substring(close);
		}

		String instance(String indent, String text, List<String> languages) {
			String inner = indent + unit;
			return "<instance class=\"" + SERVICE + "\">" + newline
				+ inner + promptContributors(inner, text, languages) + newline
				+ indent + "</instance>";
		}

		String promptContributors(String indent, String text, List<String> languages) {
			String inner = indent + unit;
			return "<" + LLMService.Config.PROMPT_CONTRIBUTORS + ">" + newline
				+ inner + contributor(inner, null, text, languages) + newline
				+ indent + "</" + LLMService.Config.PROMPT_CONTRIBUTORS + ">";
		}

		/**
		 * The entry, beginning at its {@code <}; every following line indented from the given
		 * indentation, except the lines of the text itself, which stay as written.
		 */
		String contributor(String indent, String agents, String text, List<String> languages) {
			String inner = indent + unit;
			StringBuilder result = new StringBuilder();
			result.append("<contributor class=\"").append(CONTRIBUTOR).append('"').append(newline);
			result.append(inner).append(TextPromptContributor.Config.NAME).append("=\"").append(NAME).append('"')
				.append(newline);
			if (agents != null && !agents.isBlank()) {
				result.append(inner).append(TextPromptContributor.Config.AGENTS).append("=\"")
					.append(XmlSpans.attributeValue(agents)).append('"').append(newline);
			}
			result.append(indent).append('>').append(newline);
			result.append(inner).append('<').append(TextPromptContributor.Config.TEXT).append('>').append(newline);
			String lines = text.replace("\r\n", "\n").replace("\n", newline);
			for (String language : languages) {
				result.append(inner).append(unit).append('<').append(language).append('>')
					.append(XmlSpans.cdata(lines)).append("</").append(language).append('>').append(newline);
			}
			result.append(inner).append("</").append(TextPromptContributor.Config.TEXT).append('>').append(newline);
			result.append(indent).append("</contributor>");
			return result.toString();
		}
	}

}
