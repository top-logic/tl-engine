/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.Settings;
import com.top_logic.basic.XMain;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.ProtocolOutputStream;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLPrettyPrinter.Config;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.XsltUtil;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocatorFactory;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.element.meta.schema.HolderType;
import com.top_logic.model.binding.xml.TLModelBindingConstants;
import com.top_logic.model.config.JavaClass;
import com.top_logic.model.config.ScopeConfig;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.config.annotation.TableName;

/**
 * Tool that rewrites a model definition from 5.7 to 6.0 syntax.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpgradeModel extends XMain {

	private static final Pattern PLAIN_NAME_PATTERN = Pattern.compile("([^\\.:/\\\\]*)(?:\\.xml)");

	private static final boolean PRETTY = false;

	private static final Transformer UPGRADE_FACTORY_TRANSFORMER;

	private static final Transformer UPGRADE_FACTORY_POST_TRANSFORMER;

	private static final Transformer IMPORT_TL_MODEL_TRANSFORMER;

	private static final ErrorListener LOGGING_LISTENER = new ErrorListener() {

		@Override
		public void warning(TransformerException exception) throws TransformerException {
			Logger.warn(exception.getMessageAndLocation(), exception, DefinitionReader.class);
		}

		@Override
		public void fatalError(TransformerException exception) throws TransformerException {
			error(exception);
			throw exception;
		}

		@Override
		public void error(TransformerException exception) throws TransformerException {
			Logger.error(exception.getMessageAndLocation(), exception, DefinitionReader.class);
		}
	};

	private static final boolean RESOLVE = true;

	static {
		try (PrintStream errStream =
			new PrintStream(new ProtocolOutputStream(new LogProtocol(DefinitionReader.class), Protocol.WARN))) {

			/* Compiler warnings are dumped to the error stream. Better log to the logger if
			 * possible. */
			PrintStream oldErr = System.err;
			boolean errorStreamSuccessfullyChanged = false;
			try {
				System.setErr(errStream);
				errorStreamSuccessfullyChanged = true;
			} catch (SecurityException ex) {
				// Unable to replace errorStream as SecurityManager does not allow.
			}
			try {
				try {
					UPGRADE_FACTORY_TRANSFORMER = createTransformer("upgrade-factory.xslt");
					UPGRADE_FACTORY_POST_TRANSFORMER = createTransformer("upgrade-factory-post.xslt");
					IMPORT_TL_MODEL_TRANSFORMER = createTransformer("import-tl-model.xslt");
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			} finally {
				if (errorStreamSuccessfullyChanged) {
					// reinstall origin error stream.
					System.setErr(oldErr);
				}
			}
		}
	}

	private static BinaryContent upgradeModel(InstantiationContext context, BinaryContent content, ModelConfig base,
			boolean resolve) throws IOException, FileNotFoundException, UnsupportedEncodingException {
		String plainName = getPlainName(content);

		BinaryContent in;
		String documentNamespace = getDocumentNamespace(content);
		if (documentNamespace == null) {
			File target = transformRaw(plainName, content);

			String inputLocation = target.getAbsolutePath() + " (" + content + ")";
			in = new TransformedFile(target, inputLocation);

			if (resolve) {
				ModelConfig rawModel = DefinitionReader.parse(context, in, null);
				transform(rawModel);
				File modelFile = File.createTempFile(plainName, ".v6.xml", Settings.getInstance().getTempDir());
				writeModel(modelFile, rawModel);

				inputLocation = modelFile.getAbsolutePath() + " (" + inputLocation + ")";
				in = new TransformedFile(modelFile, inputLocation);
			}
		} else if (TLModelBindingConstants.NS_MODEL.equals(documentNamespace)) {
			File target = File.createTempFile(plainName, ".v6.xml", Settings.getInstance().getTempDir());

			transform(content, target, IMPORT_TL_MODEL_TRANSFORMER, "Migrating '" + content + "' failed.");
			String inputLocation = target.getAbsolutePath() + " (" + content + ")";
			in = new TransformedFile(target, inputLocation);
		} else if (ElementSchemaConstants.MODEL_6_NS.equals(documentNamespace)) {
			in = content;
		} else {
			throw new IllegalArgumentException("Invalid model namespace version '" + documentNamespace
				+ "', expected: " + ElementSchemaConstants.MODEL_6_NS);
		}
		return in;
	}

	public static File transformRaw(String plainName, BinaryContent content) throws IOException {
		File phase1Result = File.createTempFile(plainName, ".v55.xml", Settings.getInstance().getTempDir());
		Transformer phase1Transformer = UPGRADE_FACTORY_TRANSFORMER;
		String phase1Message =
			"Migrating (phase 1) of '" + content + "' to '" + phase1Result.getAbsolutePath() + "' failed.";
		transform(content, phase1Result, phase1Transformer, phase1Message);

		File target = File.createTempFile(plainName, ".v56.xml", Settings.getInstance().getTempDir());
		Transformer phase2Transformer = UPGRADE_FACTORY_POST_TRANSFORMER;
		String phase2Message =
			"Migrating (phase 2) of '" + content + "' failed, see '" + phase1Result.getAbsolutePath() + "'.";
		transform(new StreamSource(phase1Result), new StreamResult(target), phase2Transformer, phase2Message);
		return target;
	}

	private static void transform(BinaryContent input, File result, Transformer transformer, String errorMessage)
			throws IOException {

		try (InputStream inputStream = input.getStream();
				FileOutputStream outputStream = new FileOutputStream(result)) {
			transform(new StreamSource(inputStream), new StreamResult(outputStream), transformer,
				errorMessage);
		}
	}

	private static void transform(StreamSource source, StreamResult result, Transformer transformer,
			String errorMessage) throws IOException {
		try {
			synchronized (transformer) {
				transformer.setErrorListener(LOGGING_LISTENER);
				try {
					transformer.transform(source, result);
				} finally {
					transformer.reset();
				}
			}
		} catch (TransformerException ex) {
			throw new IOException(errorMessage, ex);
		}
	}

	private static void writeModel(File modelFile, ModelConfig model) throws FileNotFoundException,
			UnsupportedEncodingException, IOException {
		writeModel(modelFile, model, PRETTY);
	}

	static void writeModel(File modelFile, ModelConfig model, boolean pretty) throws FileNotFoundException,
			UnsupportedEncodingException, IOException {

		FileOutputStream out = new FileOutputStream(modelFile);
		try {
			if (pretty) {
				TypedConfiguration.minimize(model);
				CharArrayWriter buffer = new CharArrayWriter();
				writeModel(buffer, model);
				String xml = buffer.toString();
				Document dom = DOMUtil.parse(xml);
				Config config = XMLPrettyPrinter.newConfiguration();
				try (XMLPrettyPrinter printer = new XMLPrettyPrinter(out, config)) {
					printer.write(dom);
				}
			} else {
				OutputStreamWriter writer = new OutputStreamWriter(out, "utf-8");
				writeModel(writer, model);
			}
		} catch (XMLStreamException ex) {
			throw new IOException("Writing '" + modelFile.getAbsolutePath() + "' failed.", ex);
		} finally {
			out.close();
		}
	}

	private static void writeModel(Writer out, ModelConfig model) throws XMLStreamException, IOException {
		ConfigurationWriter writer = new ConfigurationWriter(out);
		writer.setNamespace("", ElementSchemaConstants.MODEL_6_NS);
		writer.write(ElementSchemaConstants.ROOT_ELEMENT, ModelConfig.class, model);
		out.flush();
	}

	private static void transform(ModelConfig rawModel) {
		ensureGlobalTypesExtendNoLocalTypes(rawModel);
		ChildAttributeTransformer.INSTANCE.transform(rawModel);
	}

	private static void ensureGlobalTypesExtendNoLocalTypes(ModelConfig rawModel) {
		// Make sure that global types only refer to other global types in their extends clause. If
		// this property is violated, reorder the generalization chain.
		for (ModuleConfig module : rawModel.getModules()) {
			Map<String, ObjectTypeConfig> typesByName = typesByName(module);
			Map<String, ScopeConfig> scopeByName = scopeByName(module);
			for (TypeConfig type : module.getTypes()) {
				if (type instanceof ObjectTypeConfig) {
					ObjectTypeConfig objectType = (ObjectTypeConfig) type;
					ExtendsConfig primaryGeneralization = primaryGeneralization(objectType);
					if (primaryGeneralization != null) {
						if (primaryGeneralization.getTypeName().trim().isEmpty()) {
							throw new RuntimeException("Missing super type name in '" + objectType.getName() + "'.");
						}
						if (!isGlobal(primaryGeneralization.getScopeRef())) {
							assert primaryGeneralization
								.getModuleName() == null : "No cross-module inheritance in legacy model definitions: "
									+ primaryGeneralization.getQualifiedTypeName();
							// Global type with local generalization.
							ObjectTypeConfig firstLocalGeneralization =
								typesByName.get(primaryGeneralization.getTypeName());
							ObjectTypeConfig lastLocalGeneralization =
								lastLocalSuperType(typesByName, primaryGeneralization.getTypeName());

							List<ExtendsConfig> lastLocalGeneralizations = lastLocalGeneralization.getGeneralizations();
							if (!lastLocalGeneralizations.isEmpty()) {
								ExtendsConfig primaryLastLocalGeneralization = lastLocalGeneralizations.get(0);

								String firstGlobalType = primaryLastLocalGeneralization.getTypeName();
								ObjectTypeConfig firstGlobalGeneralization = typesByName.get(firstGlobalType);

								primaryGeneralization.setQualifiedTypeName(firstGlobalGeneralization.getName());
								primaryGeneralization.setScopeRef(HolderType.GLOBAL);
							} else {
								objectType.getGeneralizations().clear();
							}

							if (objectType instanceof ClassConfig) {
								((ClassConfig) objectType).setAbstract(true);
							}

							ScopeConfig localScope = scopeByName.get(firstLocalGeneralization.getName());
							ClassConfig concreteType = TypedConfiguration.newConfigItem(ClassConfig.class);
							concreteType.setName(objectType.getName());

							concreteType.getGeneralizations().add(
								newLocalExtends(firstLocalGeneralization.getName()));
							concreteType.getGeneralizations().add(newExtends(objectType.getName()));

							// Move table annotation to concrete type.
							if (objectType instanceof ClassConfig) {
								ClassConfig classType = (ClassConfig) objectType;

								// Note: The annotations are also kept at the abstract base classes,
								// since only those are considered during wrapper generation.
								copyAnnotation(classType, concreteType, TableName.class);
								copyAnnotation(classType, concreteType, JavaClass.class);
							}

							// Move local types to new concrete type.
							if (objectType instanceof ClassConfig) {
								for (Iterator<TypeConfig> types =
									((ClassConfig) objectType).getTypes().iterator(); types.hasNext();) {

									TypeConfig typeConfig = types.next();
									types.remove();

									concreteType.getTypes().add(typeConfig);
									scopeByName.put(typeConfig.getName(), concreteType);
								}
							}

							localScope.getTypes().add(concreteType);
						}
					}
				}
			}
		}
	}

	private static void copyAnnotation(ClassConfig classType, ClassConfig concreteType,
			Class<? extends TLTypeAnnotation> annotationType) {
		TLTypeAnnotation annotation = classType.getAnnotation(annotationType);
		if (annotation != null) {
			concreteType.getAnnotations().add(TypedConfiguration.copy(annotation));
		}
	}

	private static ExtendsConfig primaryGeneralization(ObjectTypeConfig objectType) {
		List<ExtendsConfig> generalizations = objectType.getGeneralizations();
		if (generalizations.isEmpty()) {
			return null;
		}
		return generalizations.get(0);
	}

	private static ExtendsConfig newLocalExtends(String typeName) {
		ExtendsConfig result = newExtends(typeName);
		result.setScopeRef(HolderType.THIS);
		return result;
	}

	private static ExtendsConfig newExtends(String typeName) {
		ExtendsConfig result = TypedConfiguration.newConfigItem(ExtendsConfig.class);
		result.setQualifiedTypeName(typeName);
		return result;
	}

	private static ObjectTypeConfig lastLocalSuperType(Map<String, ObjectTypeConfig> typesByName,
			String localTypeName) {
		ObjectTypeConfig type = typesByName.get(localTypeName);
		if (type.getGeneralizations().isEmpty()) {
			return type;
		}
		ExtendsConfig primaryGeneralization = type.getGeneralizations().get(0);
		if (isGlobal(primaryGeneralization.getScopeRef())) {
			return type;
		}
		return lastLocalSuperType(typesByName, primaryGeneralization.getTypeName());
	}

	private static boolean isGlobal(String scopeRef) {
		return HolderType.GLOBAL.equals(scopeRef);
	}

	private static Map<String, ObjectTypeConfig> typesByName(ModuleConfig module) {
		Map<String, ObjectTypeConfig> result = new HashMap<>();
		addTypes(result, module);
		return result;
	}

	private static void addTypes(Map<String, ObjectTypeConfig> result, ScopeConfig module) {
		for (TypeConfig type : module.getTypes()) {
			if (type instanceof ObjectTypeConfig) {
				ObjectTypeConfig clash = result.put(type.getName(), (ObjectTypeConfig) type);
				if (clash != null) {
					throw new ConfigurationError("Type names not unique: '" + type.getName() + "' in '"
						+ module.location() + "'.");
				}
			}
			if (type instanceof ScopeConfig) {
				addTypes(result, (ScopeConfig) type);
			}
		}
	}

	private static Map<String, ScopeConfig> scopeByName(ModuleConfig module) {
		Map<String, ScopeConfig> result = new HashMap<>();
		addScopes(result, module);
		return result;
	}

	private static void addScopes(Map<String, ScopeConfig> result, ScopeConfig scope) {
		for (TypeConfig type : scope.getTypes()) {
			ScopeConfig clash = result.put(type.getName(), scope);
			if (clash != null) {
				throw new ConfigurationError("Type defined in multiple scopes: '" + type.getName() + "' in '"
					+ scope.location() + "'.");
			}

			if (type instanceof ScopeConfig) {
				addScopes(result, (ScopeConfig) type);
			}
		}
	}

	private static String getDocumentNamespace(BinaryContent content) throws IOException {
		InputStream in = content.getStream();
		try {
			XMLStreamReader reader = XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(in);
			XMLStreamUtil.nextStartTag(reader);
			return reader.getNamespaceURI();
		} catch (XMLStreamException ex) {
			throw new IOException("Parsing document failed.", ex);
		} finally {
			in.close();
		}
	}

	private static String getPlainName(BinaryContent content) {
		Matcher matcher = PLAIN_NAME_PATTERN.matcher(content.toString());
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "factory";
		}
	}

	private static Transformer createTransformer(String resourceName) throws IOException,
			TransformerConfigurationException, TransformerFactoryConfigurationError {
		try (InputStream xslt = UpgradeModel.class.getResourceAsStream(resourceName)) {
			return XsltUtil.unsafeTransformerFactory().newTransformer(new StreamSource(xslt));
		}
	}

	private List<String> _files = new ArrayList<>();

	@Override
	protected int parameter(String[] args, int i) throws Exception {
		_files.add(args[i]);
		return i + 1;
	}

	@Override
	protected void doActualPerformance() throws Exception {
		super.doActualPerformance();

		setupModuleContext(
			Settings.Module.INSTANCE,
			AttributeValueLocatorFactory.Module.INSTANCE,
			AttributeSettings.Module.INSTANCE);

		for (String oldName : _files) {
			File oldFile = new File(oldName);

			DefaultInstantiationContext context = new DefaultInstantiationContext(DefinitionReader.class);
			BinaryContent upgradedModel =
				upgradeModel(context, BinaryDataFactory.createBinaryData(oldFile), null, RESOLVE);

			ModelConfig config = DefinitionReader.readElementConfig(upgradedModel);

			File modelDir = new File(oldFile.getParentFile().getParentFile(), "model");
			modelDir.mkdirs();

			for (ModuleConfig module : new ArrayList<>(config.getModules())) {
				File newFile = new File(modelDir, module.getName() + ".model.xml");

				ModelConfig newConfig = TypedConfiguration.newConfigItem(ModelConfig.class);
				config.getModules().remove(module);
				newConfig.getModules().add(module);

				System.out.println("Creating model configuration '" + newFile.getAbsolutePath() + "' from '"
					+ oldFile.getName() + "'.");
				writeModel(newFile, newConfig, true);
			}
		}
	}

	/**
	 * Upgrades the given file.
	 */
	public static void main(String[] args) throws Exception {
		new UpgradeModel().runMain(args);
	}

}
