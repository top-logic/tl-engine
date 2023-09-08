/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.translate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.top_logic.tools.resources.ResourceFile;
import com.top_logic.tools.resources.translate.deepl.DeepLTranslator;

/**
 * Maven goal to translate resource files.
 */
@Mojo(name = "translate")
public class ResourceTranslator extends AbstractTranslateMojo {

	private static final String CODE_OPEN = "<code>";
	private static final String CODE_CLOSE = "</code>";

	private static final char BRACE_OPEN = '{';
	private static final char BRACE_CLOSE = '}';

	private static final String XML_OPEN = "<";
	private static final String XML_CLOSE = ">";
	private static final String XML_SLASH = "/";
	private static final String XML_BRACE_NAME = "tl";
	private static final String XML_BRACE_OPEN = XML_OPEN + XML_BRACE_NAME;
	private static final String XML_BRACE_CLOSE = XML_OPEN + XML_SLASH + XML_BRACE_NAME;

	private static final Pattern SUFFIX_PATTERN = Pattern.compile("(.*/)?([^_/]+)_([A-Za-z_]+).properties");

	/**
	 * Base directory to resolve {@link #sourcePath} against.
	 */
	@Parameter(defaultValue = "${project.basedir}", property = "baseDir")
	private File baseDir;

	/** The resource file to translate. */
	@Parameter(property = "sourcePath", required = true)
	private String sourcePath;

	/**
	 * The target file to save translations to. Either {@link #targetPath} or
	 * {@link #targetLanguages} must be given.
	 */
	@Parameter(property = "targetPath")
	private String targetPath;

	/** The second resource file used as reference to find changed resources. */
	@Parameter(property = "referencePath")
	private String referencePath;

	/** The source language, if it cannot be determined from the {@link #sourcePath}. */
	@Parameter(property = "sourceLanguage")
	private String sourceLanguage;

	/**
	 * The target language(s) to translate to. Must be given, if it cannot be determined from the
	 * {@link #targetPath}, or no {@link #targetPath} is given. Either {@link #targetPath} or
	 * {@link #targetLanguages} must be specified.
	 */
	@Parameter(property = "tl.deepl.targetLanguages")
	private String targetLanguages;

	/**
	 * When set to <code>true</code>, no translation is performed.
	 */
	@Parameter(defaultValue = "false", property = "tl.deepl.skip")
	private boolean skip;

	/**
	 * Parameter from the DeepL API, see https://www.deepl.com/docs-api.
	 */
	@Parameter(defaultValue = "true", property = "tl.deepl.outlineDetection")
	private boolean outlineDetection;

	/**
	 * Parameter from the DeepL API, see https://www.deepl.com/docs-api.
	 */
	@Parameter(defaultValue = "nonewlines", property = "tl.deepl.splitSentences")
	private String splitSentences;

	/**
	 * Parameter from the DeepL API, see https://www.deepl.com/docs-api.
	 */
	@Parameter(defaultValue = "true", property = "tl.deepl.tagHandling")
	private boolean tagHandling;

	/**
	 * Parameter from the DeepL API, see https://www.deepl.com/docs-api.
	 */
	@Parameter(defaultValue = "div,p,td,li", property = "tl.deepl.splittingTags")
	private String splittingTags;

	/**
	 * Parameter from the DeepL API, see https://www.deepl.com/docs-api.
	 */
	@Parameter(defaultValue = "", property = "tl.deepl.nonSplittingTags")
	private String nonSplittingTags = "";

	/**
	 * Parameter from the DeepL API, see https://www.deepl.com/docs-api.
	 */
	@Parameter(defaultValue = "", property = "tl.deepl.ignoreTags")
	private String ignoreTags = "";

	/**
	 * Glossary names to use for translation directions.
	 * 
	 * <p>
	 * A semicolon-separated list of glossary specifications in the form
	 * <code>de:en:glossary-name</code>.
	 * </p>
	 */
	@Parameter(property = "glossaries")
	private String glossaries;

	private File _sourceFile;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Skipped translation.");
			return;
		}

		if (isEmpty(sourcePath)) {
			getLog().info("Skipping translation, no source path.");
			return;
		}

		_sourceFile = resolvePath(sourcePath);
		if (!_sourceFile.exists()) {
			getLog().info("Nothing to translate, does not exist: " + _sourceFile);
			return;
		}
		
		if (isEmpty(getApiKey())) {
			getLog().info("Skipping translation, no API key provided.");
			return;
		}

		initTranslator();

		if (isEmpty(targetPath)) {
			// Target files are detected automatically.

			int slashIndex = sourcePath.lastIndexOf('/');
			int localeIndex = sourcePath.indexOf('_', slashIndex + 1);
			if (localeIndex < 0) {
				localeIndex = sourcePath.indexOf('.', slashIndex + 1);
			}
			if (isEmpty(targetLanguages)) {
				// Translate to all existing target resources.
				String prefix = sourcePath.substring(slashIndex + 1, localeIndex + 1);
				String sourceName = sourcePath.substring(slashIndex + 1);
				File[] targetFiles = _sourceFile.getParentFile()
					.listFiles(f -> f.getName().startsWith(prefix) && !f.getName().equals(sourceName));
				if (targetFiles.length == 0) {
					getLog().info("No target resources found for: " + sourcePath);
					return;
				}

				for (File targetFile : targetFiles) {
					translate(targetFile, null);
				}
			} else {
				for (String targetLanguage : split(targetLanguages)) {
					File targetFile =
						resolvePath(sourcePath.substring(0, localeIndex) + "_" + targetLanguage + ".properties");
					translate(targetFile, targetLanguage);
				}
			}
		} else {
			if (!isEmpty(targetLanguages)) {
				List<String> languages = split(targetLanguages);
				if (languages.size() > 1) {
					throw new MojoExecutionException(
						"When translating to a single target file, at most a single target language must be given.");
				}
			}

			// Translate to a single file.
			translate(resolvePath(targetPath), targetLanguages);
		}
	}

	private File resolvePath(String path) {
		return Paths.get(path).isAbsolute() ? new File(path) : new File(baseDir, path);
	}

	private void translate(File targetFile, String targetLanguage) throws MojoExecutionException {
		try {
			translateFile(targetFile, targetLanguage);
		} catch (IOException ex) {
			throw new MojoExecutionException("Translation failed.", ex);
		}
	}

	@Override
	protected void initTranslator(DeepLTranslator.Builder builder) {
		super.initTranslator(builder);

		builder.setIgnoreTags(split(ignoreTags));
		builder.setNonSplittingTags(split(nonSplittingTags));
		builder.setSplittingTags(split(splittingTags));
		builder.setOutlineDetection(outlineDetection);
		builder.setTagHandling(tagHandling);
		builder.setSplitSentences(splitSentences);
		builder.setGlossaries(indexGlossaries());
	}

	private Map<String, Map<String, String>> indexGlossaries() {
		Map<String, Map<String, String>> glossaryIndex = new HashMap<>();
		if (glossaries != null) {
			for (String glossary : glossaries.trim().split("\\s*;\\s*")) {
				String glossarySpec = glossary.trim();
				if (glossarySpec.isEmpty()) {
					continue;
				}
				String[] parts = glossarySpec.split("\\s*:\\s*");
				if (parts.length == 3) {
					String source = parts[0];
					String target = parts[1];
					String name = parts[2];
					glossaryIndex.computeIfAbsent(source, x -> new HashMap<>()).put(target, name);
				} else {
					getLog().warn("Invalid glossary specification: " + glossary);
				}
			}
		}
		return glossaryIndex;
	}

	List<String> split(String str) {
		return Arrays.asList(str.split(",")).stream().map(String::trim).collect(Collectors.toList());
	}

	/**
	 * Translates the given resource file from the source {@link Locale} to the target
	 * {@link Locale}.
	 *
	 * @throws IOException
	 *         If saving changes has failed.
	 */
	public void translateFile(File targetFile, String targetLanguage) throws IOException, MojoExecutionException {
		String srcLang = getLanguage(sourcePath, sourceLanguage);
		String targetLang = getLanguage(targetFile.getName(), targetLanguage);

		if (isEmpty(targetLang)) {
			throw new MojoExecutionException("Cannot determine target language for file: " + targetFile);
		}

		ResourceFile sourceResources = new ResourceFile(_sourceFile);
		ResourceFile targetResources = new ResourceFile(targetFile);
		List<XMLObject> objects = getResourcesToTranslate(sourceResources, targetResources);
		long toTranslate = objects.size();
		long translated = 0;
		try {
			getLog().info("Translating " + sourceResources.getFile() + " to " + targetResources.getFile() + ".");
			if (toTranslate > 0) {
				for (XMLObject object : objects) {
					boolean canProceed = translate(srcLang, targetLang, object);
					if (!canProceed) {
						break;
					}
					translated++;
				}
			}
		} catch (Exception ex) {
			getLog().error(ex.getMessage());
			throw ex;
		} finally {
			setTranslations(objects, targetResources);
			targetResources.save();
			getLog().info(translated + " of " + toTranslate + " lines are translated.");
		}
	}

	private String getLanguage(String resourcePath, String lang) {
		if (isEmpty(lang)) {
			Matcher srcMather = SUFFIX_PATTERN.matcher(resourcePath);
			if (srcMather.matches()) {
				lang = srcMather.group(3);
			}
		}
		return lang;
	}

	/**
	 * Translates the given {@link XMLObject} from the source {@link Locale} to the target
	 * {@link Locale}.
	 * 
	 * @param srcLang
	 *        The source language.
	 * @param targetLang
	 *        The target language for the translation.
	 * 
	 * @return <code>true</code> if translation went well and <code>false</code> otherwise.
	 */
	private boolean translate(String srcLang, String targetLang, XMLObject object) {
		String sourceXML = object.getSourceXML();
		String targetXML = _translator.translate(sourceXML, srcLang, targetLang);
		String correctedXML = autocorrectXML(targetXML);
		object.setTargetXML(correctedXML);
		return true;
	}

	/**
	 * Corrects the translated text - if it is a sentence, it should start with a capital letter.
	 */
	private String autocorrectXML(String xml) {
		if (xml != null && xml.length() > 0 && xml.contains(".")) {
			if(Character.isLetter(xml.charAt(0))) {
				String firstLetter = xml.substring(0, 1).toUpperCase();
				return firstLetter + xml.substring(1);
			}
		}
		return xml;
	}

	private static void setTranslations(List<XMLObject> objects, ResourceFile targetFile) {
		for (XMLObject object : objects) {
			String key = object.getKey();
			if (object.isTranslated()) {
				String targetTranslation = convertXMLToText(object);
				setTranslation(key, targetTranslation, targetFile);
			} else {
				targetFile.removeProperty(key);
			}
		}
	}

	/**
	 * Provides all {@link XMLObject}s that have keys from the source {@link ResourceFile} that are
	 * to update in the sense of {@link #isKeyToUpdate}.
	 */
	private List<XMLObject> getResourcesToTranslate(ResourceFile sourceFile, ResourceFile targetFile) {
		ResourceFile reference = loadReference();

		List<XMLObject> objects = new ArrayList<>();
		Collection<String> sourceKeys = sourceFile.getKeys();
		if (reference != null) {
			for (String key : reference.getKeys()) {
				if (!sourceKeys.contains(key)) {
					// Key does no longer exist.
					targetFile.removeProperty(key);
				}
			}
		}
		HashSet<String> targetKeysBeforeRemove = new HashSet<>(targetFile.getKeys());
		for (String key : targetKeysBeforeRemove) {
			if (!sourceKeys.contains(key)) {
				targetFile.removeProperty(key);
			}
		}
		for (String key : sourceKeys) {
			if (isKeyToUpdate(key, sourceFile, targetFile, reference)) {
				XMLObject object = convertTextToXML(key, sourceFile);
				if (isToTranslate(object)) {
					objects.add(object);
				} else {
					setTranslation(key, getTranslation(key, sourceFile), targetFile);
				}
			}
		}
		return objects;
	}

	/**
	 * Using the given {@link Map} as a reference, decide whether the given resource key from the
	 * source {@link ResourceFile} should be updated in the target {@link ResourceFile}.
	 * 
	 * @param reference
	 *        equals <code>null</code>, if and only if there is no reference.
	 */
	private static boolean isKeyToUpdate(String key, ResourceFile sourceFile, ResourceFile targetFile,
			ResourceFile reference) {
		// Is the key missing in target?
		String targetResource = targetFile.getProperty(key);
		if (targetResource == null) {
			return true;
		}
		// Is the key empty in target, while not empty in source?
		String sourceTranslation = getTranslation(key, sourceFile);
		if (isEmpty(targetResource)) {
			if (!isEmpty(sourceTranslation)) {
				return true;
			}
		}
		// Is there any reference?
		if (reference != null) {
			// Is the key new in source?
			if (reference.getProperty(key) == null) {
				return true;
			}
			// Is the translation of the key new in source?
			String referenceTranslation = getTranslation(key, reference);
			return !equalsEmpty(sourceTranslation, referenceTranslation);
		}
		return false;
	}

	private static String getTranslation(String key, ResourceFile file) {
		return file.getProperty(key);
	}

	private static void setTranslation(String key, String text, ResourceFile file) {
		file.setProperty(key, text);
	}

	private ResourceFile loadReference() {
		if (referencePath == null || referencePath.isEmpty()) {
			getLog().info("No reference file as base line given.");
			return null;
		}

		File referenceFile = resolvePath(referencePath);
		if (!referenceFile.exists()) {
			getLog().info("No reference file does not exist: " + referenceFile);
			return null;
		}

		getLog().info("Using reference file: " + referenceFile);
		return new ResourceFile(referenceFile);
	}

	private static boolean isToTranslate(XMLObject object) {
		String text = object.getSourceXML();
		String textWithoutSpaces = removeWhiteSpace(text);
		return !isPureXML(textWithoutSpaces);
	}

	/**
	 * Removes white spaces, (' ', '\t', '\f', '\n', and '\r') from a string.
	 * 
	 * @param aString
	 *        the string to remove white space from.
	 * @return the given string without white spaces
	 */
	private static String removeWhiteSpace(String aString) {
		if (aString == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(aString);
		for (int i = 0; i < sb.length(); i++) {
			switch (sb.charAt(i)) {
				case ' ':
				case '\t':
				case '\f':
				case '\n':
				case '\r':
					sb.deleteCharAt(i--);
			}
		}
		return sb.toString();
	}

	private static boolean isPureXML(String text) {
		String textWithoutXML = removeXML(text);
		return !containsLetter(textWithoutXML);
	}

	private static boolean containsLetter(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (Character.isLetter(text.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	private static String removeXML(String xml) {
		String text = xml;
		int openIndex = text.indexOf(XML_BRACE_OPEN);
		while (openIndex == 0) {
			int closeIndex = text.indexOf(XML_BRACE_CLOSE, openIndex);
			closeIndex = text.indexOf(XML_CLOSE, closeIndex);
			text = text.substring(closeIndex + XML_CLOSE.length());
			openIndex = text.indexOf(XML_BRACE_OPEN);
		}
		return text;
	}

	private static String convertXMLToText(XMLObject xmlObject) {
		String xml = xmlObject.getTargetXML();
		List<String> arguments = xmlObject.getArguments();
		StringBuilder text = new StringBuilder();
		int start = 0;
		int openIndex = xml.indexOf(XML_BRACE_OPEN);
		while (openIndex > -1) {
			int closeIndex = xml.indexOf(XML_CLOSE, openIndex);
			String openTag = xml.substring(openIndex, closeIndex + XML_CLOSE.length());
			int argumentIndex = Integer.parseInt(openTag.replace(XML_BRACE_OPEN, "").replace(XML_CLOSE, ""));
			text.append(xml.substring(start, openIndex))
				.append(BRACE_OPEN)
				.append(arguments.get(argumentIndex))
				.append(BRACE_CLOSE);
			String closeTag = XML_BRACE_CLOSE + argumentIndex + XML_CLOSE;
			closeIndex = xml.indexOf(closeTag, closeIndex);
			start = closeIndex + closeTag.length();
			openIndex = xml.indexOf(XML_BRACE_OPEN, start);
		}
		text.append(xml.substring(start));
		return unwrapCodeTags(text.toString());
	}

	private static XMLObject convertTextToXML(String key, ResourceFile file) {
		String text = wrapCodeTags(getTranslation(key, file));
		StringBuilder xml = new StringBuilder();
		List<String> arguments = new ArrayList<>();
		int argumentIndex = 0;
		int bracesOpened = 0;
		StringBuilder argument = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char token = text.charAt(i);
			if (token == BRACE_OPEN) {
				bracesOpened++;
				if (bracesOpened == 1) {
					argument = new StringBuilder();
					continue;
				}
			}
			if (token == BRACE_CLOSE) {
				bracesOpened--;
				if (bracesOpened == 0) {
					arguments.add(argument.toString());
					xml.append(XML_BRACE_OPEN)
						.append(argumentIndex)
						.append(XML_CLOSE)
						.append(argumentIndex)
						.append(XML_BRACE_CLOSE)
						.append(argumentIndex)
						.append(XML_CLOSE);
					argumentIndex++;
					continue;
				}
			}
			if (bracesOpened > 0) {
				argument.append(token);
			} else {
				xml.append(token);
			}
		}
		return new XMLObject(key, xml.toString(), arguments);
	}

	private static String wrapCodeTags(String unwrapped) {
		String wrapped = unwrapped.replace(CODE_OPEN, BRACE_OPEN + CODE_OPEN);
		wrapped = wrapped.replace(CODE_CLOSE, CODE_CLOSE + BRACE_CLOSE);
		return wrapped;
	}

	private static String unwrapCodeTags(String wrapped) {
		String unwrapped = wrapped.replace(BRACE_OPEN + CODE_OPEN, CODE_OPEN);
		unwrapped = unwrapped.replace(CODE_CLOSE + BRACE_CLOSE, CODE_CLOSE);
		return unwrapped;
	}

	/**
	 * Provides the name pattern of {@link ResourceFile} for the given suffix.
	 */
	public static String getFileSuffix(String suffix) {
		return "_" + suffix + ".properties";
	}

	/**
	 * For the given resource key, this object encapsulates its
	 * <ul>
	 * <li>XML representation of the text to translate
	 * <li>arguments extracted from the text in order to keep them hidden from the translation
	 * service
	 * <li>XML representation of the translated text
	 * <li>state flag confirming successful translation
	 * </ul>
	 */
	private static class XMLObject {
		private String _key;

		private String _sourceXML;

		private List<String> _arguments;

		private String _targetXML;

		private boolean _translated;

		public XMLObject(String key, String xml, List<String> arguments) {
			setKey(key);
			setSourceXML(xml);
			setArguments(arguments);
			setTranslated(false);
		}

		public String getKey() {
			return _key;
		}

		private void setKey(String key) {
			_key = key;
		}

		public String getSourceXML() {
			return _sourceXML;
		}

		private void setSourceXML(String xml) {
			_sourceXML = xml;
		}

		public List<String> getArguments() {
			return _arguments;
		}

		private void setArguments(List<String> arguments) {
			_arguments = arguments;
		}

		public String getTargetXML() {
			return _targetXML;
		}

		public void setTargetXML(String xml) {
			_targetXML = xml;
			setTranslated(true);
		}

		public boolean isTranslated() {
			return _translated;
		}

		private void setTranslated(boolean translated) {
			_translated = translated;
		}
	}

}
