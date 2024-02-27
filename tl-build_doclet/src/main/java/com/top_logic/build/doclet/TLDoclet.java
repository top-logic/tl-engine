/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import static com.top_logic.build.doclet.TypeUtils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor9;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;

import com.top_logic.tools.resources.ResourceFile;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * JavaScript doclet generating XML containing the class structure and documentation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLDoclet implements Doclet {

	private static final String KNOWN_BUGS_RESOURCE_ENCODING = "utf-8";

	/**
	 * Marker for failures produced by this {@link TLDoclet}: The original message is prefixed with
	 * this string.
	 */
	private static final String TL_DOCLET = "TLDoclet: ";

	private String _destDir = ".";

	private boolean _showSrcLink = false;

	private String _srcBasePath = "";

	private String _srcLink = "";

	private String _acronymProperties = "";

	private String _targetMessages = "";

	private String _knownBugsResource = "";

	private boolean _createBaseLine = false;

	/**
	 * System resources where parts of the documentation is externalized for usage in the runtime
	 * app.
	 */
	private ResourceFile _configDoc = new ResourceFile();

	Properties _acronyms = new Properties();

	private Set<String> _knownBugs = new HashSet<>();

	private List<String> _acronymTokens;

	private Reporter _reporter;

	private WellKnownTypes _wellKnown;

	private Elements _elements;

	private Types _types;

	private DocTrees _docTrees;

	DocTrees docTrees() {
		return _docTrees;
	}

	Elements elements() {
		return _elements;
	}

	Types types() {
		return _types;
	}

	/**
	 * Entry point for the JavaDoc generation.
	 */
	@Override
	public boolean run(DocletEnvironment environment) {
		_elements = environment.getElementUtils();
		_types = environment.getTypeUtils();
		_docTrees = environment.getDocTrees();
		try {
			/* Need to install common language (en), because some errors are internationalised,
			 * especially SAXException. This would prevent detect known bugs. */

			/* Setting the Locale also changes the Locale's for each category. So each Locale must
			 * be saved. */
			Locale defaultLocale = Locale.getDefault();
			Locale displayLocale = Locale.getDefault(Category.DISPLAY);
			Locale formatLocale = Locale.getDefault(Category.FORMAT);
			boolean localeChanged = false;
			try {
				if (!defaultLocale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
					Locale.setDefault(Locale.ENGLISH);
					localeChanged = true;
				}
			} catch (SecurityException ex) {
				printNotice(TL_DOCLET + "Can not change language from "
					+ defaultLocale.getDisplayLanguage(Locale.ENGLISH) + " (" + defaultLocale.getLanguage() + ") to "
					+ Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH) + " (" + Locale.ENGLISH.getLanguage()
					+ "). Known bugs may not be detected correctly. Cause: " + ex.getMessage());
			}
			try {
				generate(environment);
			} finally {
				if (localeChanged) {
					/* Setting default Locale must occur first because it overrides the Locale's for
					 * all categories. */
					Locale.setDefault(defaultLocale);
					Locale.setDefault(Category.DISPLAY, displayLocale);
					Locale.setDefault(Category.FORMAT, formatLocale);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	private void generate(DocletEnvironment environment)
			throws XMLStreamException, IOException, SAXException, ParserConfigurationException {
		new File(_destDir).mkdirs();

		copyFiles();

		loadAcronyms();

		loadKnownBugs();

		PackageTree packageNames = new PackageTree();
		Map<PackageElement, List<TypeElement>> packages = new HashMap<>();
		_wellKnown = new WellKnownTypes(elements(), types());
		List<TypeElement> includedElements = includedClasses(environment);

		indexPackages(specifiedPackages(environment), includedElements, packageNames, packages);
		new ResourcesWriter().collectResources(includedElements);

		if (!_targetMessages.isEmpty()) {
			File messages = new File(_targetMessages);
			if (messages.exists()) {
				File backupFile = new File(_targetMessages + "~");
				if (backupFile.exists()) {
					backupFile.delete();
				}
				messages.renameTo(backupFile);
			}
			if (!_configDoc.isEmpty()) {
				File parentFile = messages.getParentFile();
				if (parentFile != null) {
					parentFile.mkdirs();
				}

				_configDoc.saveAs(messages);
			}
		}

		new DocumentationWriter().writeDocumentation(packageNames, packages, includedElements);

		writeSettings();
		storeKnownBugs();
	}

	private void indexPackages(Collection<PackageElement> specifiedPackages, List<TypeElement> includedTypes,
			PackageTree packageNames, Map<PackageElement, List<TypeElement>> packages) {
		for (PackageElement packageDoc : specifiedPackages) {
			packageNames.add(qName(packageDoc));
			ensureEntry(packages, packageDoc);
		}

		for (TypeElement type : includedTypes) {
			// Consider only top-level classes.
			Element enclosingElement = type.getEnclosingElement();
			if (enclosingElement.getKind() == ElementKind.PACKAGE) {
				PackageElement containingPackage = asPackageElement(enclosingElement);

				packageNames.add(qName(containingPackage));
				addIndex(packages, containingPackage, type);
			}
		}
	}

	private void copyFiles() throws IOException {
		copy("templates.js");
		copy("index.html");
		copy("javadoc.css");
		copy("javadoc.min.js");
		copy("jquery-3.5.1.min.js");
		copy("nunjucks.js");
		copy("toplogic.svg");
	}

	private void loadAcronyms() throws IOException {
		if (_acronymProperties != null && !_acronymProperties.isEmpty()) {
			try (InputStream in = getClass().getClassLoader().getResourceAsStream(_acronymProperties)) {
				if (in == null) {
					throw new IOException("Acronym resource '" + _acronymProperties + "' not found.");
				}
				_acronyms.load(in);
			}
		}

		_acronymTokens = new ArrayList<>();
		for (Object key : _acronyms.keySet()) {
			String acronym = (String) key;

			if (Character.isUpperCase(acronym.charAt(0))) {
				_acronymTokens.add(acronym);
			}
		}

		for (String acronym : _acronymTokens) {
			_acronyms.setProperty(acronym.toLowerCase(), _acronyms.getProperty(acronym));
		}
	}

	private void writeSettings() throws IOException {
		new SettingsWriter()
			.setShowSourceLinks(_showSrcLink)
			.setSourceBasePath(_srcBasePath)
			.writeToJSON(new File(_destDir, "settings.json"));
	}

	private List<TypeElement> includedClasses(DocletEnvironment env) {
		return env.getIncludedElements()
			.stream()
			.filter(elem -> elem.getKind().isClass() || elem.getKind().isInterface())
			.map(TypeElement.class::cast)
			.filter(this::notHiddenType)
			.collect(Collectors.toList());
	}

	private Collection<PackageElement> specifiedPackages(DocletEnvironment env) {
		return ElementFilter.packagesIn(env.getSpecifiedElements());
	}

	private static PackageElement asPackageElement(Element element) {
		return PackageElement.class.cast(element);
	}

	private void printNotice(String message) {
		_reporter.print(Diagnostic.Kind.NOTE, message);
	}

	private void loadKnownBugs() throws IOException {
		if (!_createBaseLine) {
			if (_knownBugsResource != null && !_knownBugsResource.isEmpty()) {
				if (!new File(_knownBugsResource).exists()) {
					printNotice("No known bugs base line found: " + _knownBugsResource);
				} else {
					try (BufferedReader in =
						new BufferedReader(new InputStreamReader(new FileInputStream(_knownBugsResource),
							KNOWN_BUGS_RESOURCE_ENCODING))) {
						String line;
						while ((line = in.readLine()) != null) {
							_knownBugs.add(line);
						}
					}
					printNotice("Read known bug base line with " + _knownBugs.size() + " elements.");
				}
			}
		} else {
			if (_knownBugsResource == null || _knownBugsResource.isEmpty()) {
				throw new IllegalStateException(
					"Can not create base line (-createBaseline), without known bugs file (-knownBugs).");
			}
		}
	}

	private void storeKnownBugs() throws IOException {
		if (!_createBaseLine) {
			return;
		}
		File knownBugs = new File(_knownBugsResource);
		if (_knownBugs.isEmpty()) {
			knownBugs.delete();
			return;
		}
		ArrayList<String> sortedBugs = new ArrayList<>(_knownBugs);
		Collections.sort(sortedBugs);

		if (knownBugs.getParentFile() != null) {
			knownBugs.getParentFile().mkdirs();
		}

		try (Writer out = new OutputStreamWriter(new FileOutputStream(knownBugs), KNOWN_BUGS_RESOURCE_ENCODING)) {
			for (String bug : sortedBugs) {
				out.write(bug);
				out.write("\n");
			}
		}
		printNotice("Created known bug base line with " + _knownBugs.size() + " elements.");
	}

	private void copy(String fileName) throws IOException {
		try (InputStream in = this.getClass().getResourceAsStream(fileName)) {
			copy(in, new File(_destDir, fileName));
		}
	}

	private static void copy(InputStream in, File file) throws IOException {
		byte[] buffer = new byte[4096];
		try (OutputStream out = new FileOutputStream(file)) {
			while (true) {
				int direct = in.read(buffer);
				if (direct < 0) {
					break;
				}

				out.write(buffer, 0, direct);
			}
		}
	}

	private static <K, V> void addIndex(Map<K, List<V>> multiMap, K key, V value) {
		ensureEntry(multiMap, key).add(value);
	}

	private static <V, K> List<V> ensureEntry(Map<K, List<V>> multiMap, K key) {
		List<V> specializations = multiMap.get(key);
		if (specializations == null) {
			specializations = new ArrayList<>();
			multiMap.put(key, specializations);
		}
		return specializations;
	}

	void printWarning(SourcePosition position, String msg) {
		if (msg.startsWith("[Fatal Error]")) {
			// System output from XML parser - unspecific nonsense message. Not even write to
			// log.
			return;
		}

		if (msg.startsWith("No source files for package")) {
			// It's OK to have e.g. a module top-level package with only sub-packages.
			return;
		}

		String bug = toBugString(position, msg);
		if (_createBaseLine) {
			_knownBugs.add(bug);
			return;
		}
		if (_knownBugs.contains(bug)) {
			// Do not report known bugs.
			return;
		}
		_reporter.print(Diagnostic.Kind.WARNING, position + " " + TL_DOCLET + msg);
	}

	private static String toBugString(SourcePosition position, String msg) {
		String absolutePath = position.uri().toString();
		if (File.separatorChar != '/') {
			absolutePath = absolutePath.replace(File.separatorChar, '/');
		}
		String javaMain = "src/main/java/";
		int indexOf = absolutePath.indexOf(javaMain);
		if (indexOf > 0) {
			absolutePath = absolutePath.substring(indexOf + javaMain.length());
		}
		return absolutePath.replace('/', '.') + ": " + msg;
	}

	private PackageElement containingPackage(Element element) {
		return elements().getPackageOf(element);
	}

	String kind(TypeElement type) {
		TypeMirror classType = type.asType();
		switch (type.getKind()) {
			case ANNOTATION_TYPE:
				return "annotation";
			case CLASS:
				if (isSubType(classType, _wellKnown._errorType)) {
					return "error";
				}
				if (isSubType(classType, _wellKnown._exceptionType)) {
					return "exception";
				}
				return "class";
			case ENUM:
				return "enum";
			case INTERFACE:
				if (_wellKnown._configType != null && isSubType(classType, _wellKnown._configType)
					&& classType != _wellKnown._configType) {
					return "config";
				} else {
					return "interface";
				}
			case CONSTRUCTOR:
			case ENUM_CONSTANT:
			case EXCEPTION_PARAMETER:
			case FIELD:
			case INSTANCE_INIT:
			case LOCAL_VARIABLE:
			case METHOD:
			case MODULE:
			case OTHER:
			case PACKAGE:
			case PARAMETER:
			case RESOURCE_VARIABLE:
			case STATIC_INIT:
			case TYPE_PARAMETER:
			default:
				throw new IllegalArgumentException();

		}
	}

	boolean isSubType(TypeMirror subType, TypeMirror superType) {
		TypeMirror subTypeErasure = erasure(subType);
		TypeMirror supTypeErasure = erasure(superType);
		return types().isSubtype(subTypeErasure, supTypeErasure);
	}

	boolean notHiddenType(TypeElement element) {
		return !hiddenType(element);
	}

	private boolean hiddenType(TypeElement element) {
		PackageElement containingPackage = containingPackage(element);
		if (hiddenPackage(containingPackage)) {
			return true;
		}
		return hiddenElement(element);
	}

	TypeMirror erasure(TypeMirror type) {
		return types().erasure(type);
	}

	String qualifiedName(Element type) {
		TypeElement containingClass = containingClass(type);
		if (containingClass == null) {
			return qName(asTypeElement(type));
		} else {
			return qualifiedName(containingClass) + "." + simpleName(type);
		}
	}

	String simpleName(Element clazz) {
		String result = asString(clazz);
		int dotIndex = result.lastIndexOf(".");
		if (dotIndex >= 0) {
			return result.substring(dotIndex + 1);
		}

		return result;
	}

	SourcePosition position(DocTreePath path) {
		return position(path.getTreePath());
	}

	SourcePosition position(Element element) {
		TreePath path = docTrees().getPath(element);
		if (path == null) {
			return null;
		}
		return position(path);
	}

	private SourcePosition position(TreePath path) {
		CompilationUnitTree cu = path.getCompilationUnit();
		DocSourcePositions spos = docTrees().getSourcePositions();
		long pos = spos.getStartPosition(cu, path.getLeaf());
		long lineNumber = cu.getLineMap().getLineNumber(pos);
		JavaFileObject sourceFile = cu.getSourceFile();
		return new SourcePosition(sourceFile, lineNumber);
	}

	List<? extends DocTree> docTree(Element type) {
		DocCommentTree docCommentTree = docTrees().getDocCommentTree(type);
		List<? extends DocTree> comments;
		if (docCommentTree != null) {
			comments = docCommentTree.getFullBody();
		} else {
			comments = Collections.emptyList();
		}
		return comments;
	}

	void printWarning(Element element, String msg) {
		printWarning(position(element), msg);
	}

	private String simpleTypeName(TypeVariable type) {
		return asString(type.asElement());
	}

	String signature(TypeMirror type) {
		return signature(type, false);
	}

	String signature(TypeMirror type, boolean withDimension) {
		switch (type.getKind()) {
			case TYPEVAR:
				return simpleTypeName((TypeVariable) type);
			case BOOLEAN:
				return "boolean";
			case BYTE:
				return "byte";
			case CHAR:
				return "char";
			case DOUBLE:
				return "double";
			case FLOAT:
				return "float";
			case INT:
				return "int";
			case LONG:
				return "long";
			case SHORT:
				return "short";
			case VOID:
				return "void";
			case WILDCARD:
				// Wildcard has "?" representation with old API.
				return "?";
			case ARRAY:
				if (withDimension) {
					return ignoreDimension(type,
						(compType, dims) -> signature(compType, withDimension) + "[]".repeat(dims));
				} else {
					return ignoreDimension(type, (compType, dims) -> signature(compType, withDimension));
				}
			case DECLARED:
			case ERROR:
			case EXECUTABLE:
			case INTERSECTION:
			case MODULE:
			case NONE:
			case NULL:
			case OTHER:
			case PACKAGE:
			case UNION:
				TypeElement clazz = asTypeElement(types().asElement(type));
				TypeElement containingClass = containingClass(clazz);
				if (containingClass == null) {
					return qName(clazz);
				} else {
					return signature(containingClass.asType(), false) + "$" + simpleName(clazz);
				}
		}
		throw new IllegalStateException("All cases covered");
	}

	private <T> T ignoreDimension(TypeMirror arrayType, Function<TypeMirror, T> callback) {
		return ignoreDimension(arrayType, asBiFunction(callback));
	}

	private <T, U, R> BiFunction<T, U, R> asBiFunction(Function<T, R> callback) {
		return (t, u) -> callback.apply(t);
	}

	private <T> T ignoreDimension(TypeMirror arrayType, BiFunction<TypeMirror, Integer, T> callback) {
		int numberDims = 0;
		TypeMirror componentType = arrayType;
		do {
			componentType = ((ArrayType) componentType).getComponentType();
			numberDims++;
		} while (componentType.getKind() == TypeKind.ARRAY);
		return callback.apply(componentType, numberDims);
	}

	/**
	 * Whether the given {@link Element member} requires documentation.
	 */
	protected boolean needsDocumentation(Element member) {
		if (_wellKnown.hasDeprecatedAnnotation(member)) {
			return false;
		}

		TypeElement containingClass = containingClass(member);
		if (containingClass != null) {
			if ("I18NConstants".equals(asString(containingClass))) {
				return false;
			}
			if ("Icons".equals(asString(containingClass))) {
				return false;
			}
		}

		return true;
	}

	ExecutableElement overriddenMethod(ExecutableElement methodDoc, Name simpleName,
			List<? extends VariableElement> parameters) {
		// Only class overrides:
		// ExecutableElement overriddenMethod = methodDoc.overriddenMethod();
		if (_wellKnown.hasOverrideAnnotation(methodDoc)) {
			// System.out.println("Searching override of " + signature(methodDoc.containingClass())
			// + "." + signature);
			ExecutableElement foundMethod =
				searchOverriddenMethod(containingClass(methodDoc), simpleName, parameters);
			return foundMethod;
		}
		return null;
	}

	private ExecutableElement searchOverriddenMethod(TypeElement clazz, Name simpleName,
			List<? extends VariableElement> parameters) {
		TypeMirror type = clazz.asType();

		boolean isInterface = clazz.getKind() == ElementKind.INTERFACE;
		if (!isInterface) {
			// search super classes.
			{
				TypeMirror baseType = directSupertypes(type).get(0);
				while (true) {
					ExecutableElement result =
						searchMethodDefinition(baseType, typeArguments(baseType), simpleName, parameters);
					if (result != null) {
						return result;
					}

					List<? extends TypeMirror> directSupertypes = directSupertypes(baseType);
					if (directSupertypes.isEmpty()) {
						break;
					}
					baseType = directSupertypes.get(0);
				}
			}
		}

		{

			TypeMirror baseType = type;
			while (true) {
				List<? extends TypeMirror> directSupertypes = directSupertypes(baseType);
				if (directSupertypes.isEmpty()) {
					break;
				}
				List<? extends TypeMirror> interfaces;
				if (isInterface) {
					interfaces = directSupertypes;
				} else {
					interfaces = directSupertypes.subList(1, directSupertypes.size());
				}
				ExecutableElement result = searchInterfaces(interfaces, simpleName, parameters);
				if (result != null) {
					return result;
				}
				if (isInterface) {
					break;
				}
				baseType = directSupertypes.get(0);
			}
		}

		return null;
	}

	private List<? extends TypeMirror> directSupertypes(TypeMirror type) {
		return types().directSupertypes(type);
	}

	private ExecutableElement searchInterfaces(List<? extends TypeMirror> interfaces, Name simpleName,
			List<? extends VariableElement> parameters) {
		for (TypeMirror superType : interfaces) {
			ExecutableElement result =
				searchMethodDefinition(superType, typeArguments(superType), simpleName, parameters);
			if (result != null) {
				return result;
			}

			result = searchInterfaces(directSupertypes(superType), simpleName, parameters);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private List<? extends TypeMirror> typeArguments(TypeMirror type) {
		List<? extends TypeMirror> typeArguments;
		if (type instanceof DeclaredType) {
			typeArguments = ((DeclaredType) type).getTypeArguments();
		} else {
			typeArguments = Collections.emptyList();
		}
		return typeArguments;
	}

	private ExecutableElement searchMethodDefinition(TypeMirror clazz,
			List<? extends TypeMirror> typeArguments,
			Name simpleName, List<? extends VariableElement> parameters) {
		ExecutableElement result = searchLocalMethod(clazz, typeArguments, simpleName, parameters);
		if (result != null) {
			ExecutableElement parent = overriddenMethod(result, simpleName, parameters);
			return parent != null ? parent : result;
		}
		return null;
	}

	private ExecutableElement searchLocalMethod(TypeMirror clazz,
			List<? extends TypeMirror> typeArguments, Name simpleName,
			List<? extends VariableElement> parameters) {
		if (clazz == null) {
			return null;
		}

		TypeElement clazzType = asTypeElement(types().asElement(clazz));

		Map<TypeVariable, TypeMirror> concreteTypes;
		List<? extends TypeParameterElement> typeParameters = clazzType.getTypeParameters();
		if (!typeParameters.isEmpty()) {
			concreteTypes = new HashMap<>();
			if (typeArguments.isEmpty()) {
				// Extension of raw type; missing types.
				for (int i = 0; i < typeParameters.size(); i++) {
					concreteTypes.put((TypeVariable) typeParameters.get(i).asType(),
						_wellKnown._objectType.asType());
				}
			} else {
				assert typeParameters.size() == typeArguments.size();
				for (int i = 0; i < typeParameters.size(); i++) {
					concreteTypes.put((TypeVariable) typeParameters.get(i).asType(), typeArguments.get(i));
				}
			}

		} else {
			concreteTypes = Collections.emptyMap();
		}

		methods:
		for (ExecutableElement method : methodsIn(clazzType)) {
			if (!simpleName.contentEquals(method.getSimpleName())) {
				continue;
			}
			List<? extends VariableElement> methodParams = method.getParameters();
			if (parameters.size() != methodParams.size()) {
				continue;
			}
			for (int i = 0; i < methodParams.size(); i++) {
				TypeMirror methodParam = methodParams.get(i).asType();
				if (methodParam instanceof TypeVariable) {
					TypeVariable typeParam = (TypeVariable) methodParam;
					TypeMirror classTypeParam = concreteTypes.get(typeParam);
					if (classTypeParam != null) {
						// method param is a TypeVariable which is bounded by the type, not by the
						// method.
						methodParam = classTypeParam;
					}
				}
				TypeMirror param = parameters.get(i).asType();
				if (!types().isSameType(erasure(methodParam), erasure(param))) {
					continue methods;
				}
			}
			return method;
		}
		// System.out.println(" Nothing found in: " + signature(clazz));
		return null;
	}

	private List<ReturnTree> returnTags(Element elem) {
		return returnTags(docTrees().getDocCommentTree(elem));
	}

	private List<ReturnTree> returnTags(DocCommentTree commentTree) {
		return bockTagsOfKind(commentTree, DocTree.Kind.RETURN, ReturnTree.class);
	}

	private List<ParamTree> paramTags(Element elem) {
		return paramTags(docTrees().getDocCommentTree(elem));
	}

	private List<ParamTree> paramTags(DocCommentTree commentTree) {
		return bockTagsOfKind(commentTree, DocTree.Kind.PARAM, ParamTree.class);
	}

	private <T extends DocTree> List<T> bockTagsOfKind(DocCommentTree commentTree, DocTree.Kind blockTagKind,
			Class<T> tagClass) {
		if (commentTree == null) {
			return Collections.emptyList();
		}
		return commentTree.getBlockTags()
			.stream()
			.filter(docTree -> docTree.getKind() == blockTagKind)
			.map(tagClass::cast)
			.collect(Collectors.toList());
	}

	DocTreePath docTreePathForElement(Element element) {
		TreePath path = docTrees().getPath(element);
		DocCommentTree commentTree = docTrees().getDocCommentTree(element);
		if (commentTree == null) {
			return null;
		}
		return new DocTreePath(path, commentTree);
	}

	String stripTodo(String text) {
		return text.replaceAll("\\bTODO\\b.*", "").trim();
	}

	@Override
	public Set<? extends Option> getSupportedOptions() {
		Option[] options = new Option[] {
			new OptionBuilder()
				.argumentCount(1)
				.addName("-d")
				.processArguments(args -> _destDir = args.get(0))
				.build(),
			new OptionBuilder()
				.argumentCount(1)
				.addName("-srcLink")
				.processArguments(args -> _srcLink = args.get(0))
				.build(),
			new OptionBuilder()
				.argumentCount(1)
				.addName("-srcBasePath")
				.processArguments(args -> _srcBasePath = args.get(0))
				.build(),
			new OptionBuilder()
				.argumentCount(1)
				.addName("-showSrcLink")
				.processArguments(args -> _showSrcLink = Boolean.parseBoolean(args.get(0)))
				.build(),
			new OptionBuilder()
				.argumentCount(1)
				.addName("-acronyms")
				.processArguments(args -> _acronymProperties = args.get(0))
				.build(),
			new OptionBuilder()
				.argumentCount(1)
				.addName("-targetMessages")
				.processArguments(args -> _targetMessages = args.get(0))
				.build(),
			new OptionBuilder()
				.argumentCount(1)
				.addName("-knownBugs")
				.processArguments(args -> _knownBugsResource = args.get(0))
				.build(),
			new OptionBuilder()
				.argumentCount(1)
				.addName("-createBaseline")
				.processArguments(args -> _createBaseLine = Boolean.parseBoolean(args.get(0)))
				.build(),
		};
		return new HashSet<>(Arrays.asList(options));
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_11;
	}

	@Override
	public void init(Locale locale, Reporter reporter) {
		_reporter = reporter;
		printNotice("Doclet using locale: " + locale);
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	/**
	 * Writer creating
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private class ResourcesWriter {

		private final Pattern METHOD_NAME_PATTERN =
			Pattern.compile("^(?:is|should|has|can|must|get|set|)(.*)$");

		void collectResources(List<TypeElement> includedElements) {
			for (TypeElement type : includedElements) {
				String kind = kind(type);
				TypeMirror typeMirror = type.asType();
				if (kind.equals("config")) {
					if (!isPolymorphicConfiguration(typeMirror) && !_wellKnown.hasAbstractAnnotation(type)) {
						// Abstract configurations are not displayed at the UI.
						collectType(type, type);
					}
					collectPropertyDoc(type);
				} else if (kind.equals("enum")) {
					collectType(type, type);
					collectClassifierDoc(type);
				} else if (kind.equals("annotation")) {
					collectType(type, type);
					collectAnnotationElementDoc(type);
				} else {
					// Note: Even abstract classes must be documented, since e.g. services with
					// multiple implementations are abstract, but only the abstract class is shown
					// in the service editor.
					DeclaredType configurationType = getConfigurationType(type);
					if (configurationType != null) {
						collectType(type, asTypeElement(configurationType.asElement()));
					}
					else if (isI18NExtension(typeMirror)) {
						collectI18NConstantDoc(type);
					}
					else if (isThemeConstantsClass(typeMirror)) {
						collectThemeConstantsDoc(type);
					} else if (isWithPropertiesClass(typeMirror)) {
						collectWithPropertiesDoc(type);
					} else if (_wellKnown.hasInAppAnnotation(type)) {
						collectType(type, type);
					}
				}
			}

		}

		/**
		 * The configuration type of a configured class, or <code>null</code> if the given class is
		 * not configured.
		 */
		private DeclaredType getConfigurationType(TypeElement type) {
			for (ExecutableElement constructor : constructorsIn(type)) {
				if (!publicOrProtected(constructor)) {
					continue;
				}
				List<? extends VariableElement> parameters = constructor.getParameters();
				if (parameters.size() != 2) {
					continue;
				}

				if (parameters.get(0).asType() == _wellKnown._instantiationContext) {
					VariableElement configParam = parameters.get(1);
					TypeMirror configParamType = configParam.asType();
					if (isSubType(erasure(configParamType), _wellKnown._configType)) {
						return configParamType.accept(new AbstractTypeVisitor9<DeclaredType, Void>() {

							private DeclaredType errorNoConfigTypeExtension(TypeMirror t) {
								throw new IllegalArgumentException(t + " is not a configuration type.");
							}

							@Override
							public DeclaredType visitPrimitive(PrimitiveType t, Void p) {
								return errorNoConfigTypeExtension(t);
							}

							@Override
							public DeclaredType visitNull(NullType t, Void p) {
								return errorNoConfigTypeExtension(t);
							}

							@Override
							public DeclaredType visitArray(ArrayType t, Void p) {
								return errorNoConfigTypeExtension(t);
							}

							@Override
							public DeclaredType visitDeclared(DeclaredType t, Void p) {
								return t;
							}

							@Override
							public DeclaredType visitError(ErrorType t, Void p) {
								return errorNoConfigTypeExtension(t);
							}

							@Override
							public DeclaredType visitTypeVariable(TypeVariable t, Void p) {
								TypeMirror upperBound = t.getUpperBound();
								if (upperBound != null) {
									return upperBound.accept(this, p);
								}
								return null;
							}

							@Override
							public DeclaredType visitWildcard(WildcardType t, Void p) {
								// A concrete interface is needed.
								return null;
							}

							@Override
							public DeclaredType visitExecutable(ExecutableType t, Void p) {
								return errorNoConfigTypeExtension(t);
							}

							@Override
							public DeclaredType visitNoType(NoType t, Void p) {
								return errorNoConfigTypeExtension(t);
							}

							@Override
							public DeclaredType visitIntersection(IntersectionType t, Void p) {
								return null;
							}

							@Override
							public DeclaredType visitUnion(UnionType t, Void p) {
								return null;
							}
						}, null);
					}
				}
			}
			return null;
		}

		private void collectType(TypeElement type, TypeElement configurationType) {
			String key = signature(type.asType());
			_configDoc.setProperty(key, label(type, true));

			String doc = extractDoc(configurationType, type);
			if (!doc.isEmpty()) {
				_configDoc.setProperty(key + ".tooltip", doc);
			}
		}

		private String extractDoc(TypeElement selfClass, Element element) {
			DocTreePath docTreePathForElement = docTreePathForElement(element);
			if (docTreePathForElement == null) {
				// No comment for element
				return "";
			}
			return extractDoc(selfClass, docTreePathForElement, docTree(element));
		}

		private String extractDoc(TypeElement selfClass, DocTreePath parentPath, List<? extends DocTree> inlineTags) {
			class Buffer implements Appendable {
				private StringBuffer _buffer = new StringBuffer();

				private Consumer<StringBuffer> _callback;

				public void append(String label) {
					if (label.isEmpty()) {
						return;
					}

					if (_callback != null) {
						int n = 0;
						for (int cnt = label.length(); n < cnt; n++) {
							char ch = label.charAt(n);

							// Whether e.g. a plural "s" or a genitive "'s" is appended to the
							// preceeding tag.
							boolean suffix = Character.isAlphabetic(ch)
								|| (ch == '\'' && n < cnt - 1 && Character.isAlphabetic(label.charAt(n + 1)));

							if (!suffix) {
								break;
							}
						}

						if (n > 0) {
							_buffer.append(label.substring(0, n));
							_callback.accept(_buffer);
							_buffer.append(label.substring(n));
						} else {
							_callback.accept(_buffer);
							_buffer.append(label);
						}

						_callback = null;
						return;
					}

					_buffer.append(label);
				}

				public void appendClose(Consumer<StringBuffer> callback) {
					flush();
					_callback = callback;
				}

				private void flush() {
					if (_callback != null) {
						_callback.accept(_buffer);
						_callback = null;
					}
				}

				@Override
				public String toString() {
					flush();
					return _buffer.toString();
				}

				@Override
				public Buffer append(CharSequence csq, int start, int end) {
					append(csq.subSequence(start, end).toString());
					return this;
				}

				@Override
				public Buffer append(char c) {
					append(Character.toString(c));
					return this;
				}

				@Override
				public Buffer append(CharSequence csq) {
					append(csq.toString());
					return this;
				}
			}

			class Scanner extends DocTreePathScanner<Void, Buffer> {

				private boolean _startOfSentence = true;

				@Override
				public Void visitLink(LinkTree linkTag, Buffer buffer) {
					List<? extends DocTree> label = linkTag.getLabel();
					if (!label.isEmpty()) {
						scan(label, buffer);
					} else {
						scan(linkTag.getReference(), buffer);
					}
					return null;
				}

				@Override
				public Void visitReference(ReferenceTree tree, Buffer buffer) {
					boolean found = false;
					Element referencedElement = docTrees().getElement(getCurrentPath());
					if (referencedElement != null) {
						String labelValue = getAnnotatedLabel(referencedElement);
						if (labelValue != null) {
							buffer.append("<i>");
							buffer.append(adjustCase(labelValue, _startOfSentence));
							buffer.appendClose(b -> b.append("</i>"));
							found = true;
						} else {
							switch (referencedElement.getKind()) {
								case CONSTRUCTOR:
								case METHOD:
									TypeElement containingClass = containingClass(referencedElement);
									if ("config".equals(kind(containingClass))) {
										ExecutableElement ref = (ExecutableElement) referencedElement;
										buffer.append("<i>");
										buffer.append(label(ref, propertyName(originalDefinition(ref)), true));
										buffer.appendClose(b -> {
											b.append("</i>");

											if (!isSubType(selfClass.asType(), containingClass.asType())) {
												// Reference to another configuration
												b.append(" (");
												b.append("<i>");
												b.append(labelIncludeEnclosing(containingClass, false));
												b.append("</i>");
												b.append(")");
											}
										});
										found = true;
									} else if ("annotation"
										.equals(kind(containingClass))) {
										buffer.append("<i>");
										buffer.append(label(referencedElement, _startOfSentence));
										buffer.appendClose(b -> b.append("</i>"));
										found = true;
									}
									break;
								case FIELD:
									if (isStatic(referencedElement) && isFinal(referencedElement)) {
										VariableElement field = (VariableElement) referencedElement;
										Object value = field.getConstantValue();
										useConstant:
										if (value != null) {
											String valueLabel;
											if (value instanceof String) {
												valueLabel = "\"" + value + "\"";
											} else if (value instanceof Number) {
												valueLabel = value.toString();
											} else if (value instanceof Boolean) {
												valueLabel = value.toString();
											} else {
												try {
													valueLabel = elements().getConstantExpression(value);
												} catch (IllegalArgumentException ex) {
													// value not primitive or string.
													break useConstant;
												}
											}

											buffer.append("<code>");
											buffer.append(valueLabel);
											buffer.append("</code>");
											found = true;
										}
									}
									break;
								case ENUM:
								case ANNOTATION_TYPE:
								case CLASS:
								case INTERFACE:
								case RECORD:
								case ENUM_CONSTANT:
									buffer.append("<i>");
									buffer.append(label(referencedElement, _startOfSentence));
									buffer.appendClose(b -> b.append("</i>"));
									found = true;
									break;
								case EXCEPTION_PARAMETER:
								case INSTANCE_INIT:
								case LOCAL_VARIABLE:
								case MODULE:
								case OTHER:
								case PACKAGE:
								case PARAMETER:
								case RESOURCE_VARIABLE:
								case STATIC_INIT:
								case TYPE_PARAMETER:
								case RECORD_COMPONENT:
								case BINDING_VARIABLE:
									break;
							}
						}
					}
					if (!found) {
						buffer.append("<code>");
						buffer.append(tree.getSignature());
						buffer.append("</code>");

						printWarning(position(getCurrentPath()), "Invalid configuration reference '"
							+ tree.getSignature()
							+ "'. A configuration property and a configured class may only reference other classes or configuration properties from their JavaDoc without providing a label for the reference. The documentation must be written to be usfull for in-app configuration. Notes to the developer may be given in @see tags.");

					}
					return super.visitReference(tree, buffer);
				}

				@Override
				public Void visitText(TextTree tree, Buffer buffer) {
					internalVisitText(tree, buffer);
					return super.visitText(tree, buffer);
				}

				@Override
				public Void visitErroneous(ErroneousTree tree, Buffer buffer) {
					internalVisitText(tree, buffer);
					return super.visitErroneous(tree, buffer);
				}

				private void internalVisitText(TextTree tree, Buffer buffer) {
					String text = tree.getBody();
					buffer.append(text);
					String trimmed = text.trim();
					if (!trimmed.isEmpty()) {
						_startOfSentence = trimmed.endsWith(".");
					}
				}

				@Override
				public Void visitStartElement(StartElementTree node, Buffer p) {
					p.append('<').append(node.getName());
					Void returnVal = super.visitStartElement(node, p);
					if (node.isSelfClosing()) {
						p.append('/');
					}
					p.append('>');
					return returnVal;
				}

				@Override
				public Void visitEntity(EntityTree node, Buffer p) {
					p.append('&').append(node.getName()).append(";");
					return null;
				}

				@Override
				public Void visitAttribute(AttributeTree node, Buffer p) {
					p.append(' ');
					p.append(node.getName());
					p.append('=');
					switch (node.getValueKind()) {
						case EMPTY:
							p.append("\"null\"");
							break;
						case DOUBLE:
						case SINGLE:
						case UNQUOTED:
							p.append('"');
							Buffer attrBuffer = new Buffer();
							scan(node.getValue(), attrBuffer);
							String attrValue = attrBuffer.toString().replace("\"", "&quot;");
							p.append(attrValue);
							p.append('"');
							break;
					}
					return null;
				}

				@Override
				public Void visitEndElement(EndElementTree node, Buffer p) {
					p.append('<')
						.append('/')
						.append(node.getName())
						.append('>');
					return super.visitEndElement(node, p);
				}

			}

			Scanner scanner = new Scanner();
			Buffer bufferArg = new Buffer();
			for (DocTree tag : inlineTags) {
				scanner.scan(new DocTreePath(parentPath, tag), bufferArg);
			}

			return adjustCase(stripTodo(bufferArg.toString().replaceAll("\\s+", " ")), true);
		}

		private String adjustCase(String value, boolean startOfSentence) {
			if (value.isEmpty()) {
				return value;
			}
			char firstChar = value.charAt(0);
			if (firstChar == (startOfSentence ? Character.toUpperCase(firstChar) : Character.toLowerCase(firstChar))) {
				return value;
			}
			Matcher matcher = CamelCaseIterator.CAMEL_CASE_WORD.matcher(value);
			if (matcher.find()) {
				if (matcher.start() == 0) {
					// Camel case start are not adjusted
					return value;
				}
			}

			if (startOfSentence) {
				return Character.toUpperCase(firstChar) + value.substring(1);
			} else {
				Matcher spaceMatcher = Pattern.compile("[-\\s]").matcher(value);
				if (spaceMatcher.find()) {
					if (isAllUpperCase(value.substring(0, spaceMatcher.start()))) {
						return value;
					}
				} else if (isAllUpperCase(value)) {
					return value;
				}
				return Character.toLowerCase(firstChar) + value.substring(1);
			}
		}

		private String labelIncludeEnclosing(TypeElement element, boolean upperCase) {
			String technicalName = asString(element);
			Element enclosingElement = element.getEnclosingElement();
			while (enclosingElement != null
				&& (enclosingElement.getKind().isClass() || enclosingElement.getKind().isInterface())) {
				technicalName = asString(enclosingElement) + "." + technicalName;
				enclosingElement = enclosingElement.getEnclosingElement();
			}

			return label(element, technicalName, upperCase);
		}

		private String label(Element element, boolean upperCase) {
			return label(element, asString(element), upperCase);
		}

		private String label(Element element, String technicalName, boolean upperCase) {
			String result = getAnnotatedLabel(element);
			if (result != null) {
				return result;
			}

			StringBuilder builder = new StringBuilder();
			boolean first = true;

			boolean isAllUppercase = isAllUpperCase(technicalName);

			for (String part : split(technicalName)) {
				boolean unchanged = false;

				String acronym = _acronyms.getProperty(part.toLowerCase());
				if (acronym != null) {
					if (acronym.isEmpty()) {
						// Part excluded from labels.
						continue;
					}

					unchanged = Character.isUpperCase(acronym.charAt(0));
					part = acronym;
				}

				boolean firstWord = first;
				if (first) {
					first = false;
				} else {
					builder.append(" ");
				}

				if (unchanged || (!isAllUppercase && isAllUpperCase(part))) {
					builder.append(part);
				} else if (firstWord) {
					builder.append(adjustCase(part.toLowerCase(), upperCase));
				} else {
					builder.append(part.toLowerCase());
				}
			}

			String converted = builder.toString();
			if (converted.isEmpty()) {
				return adjustCase(technicalName, upperCase);
			}

			return converted;
		}

		private List<String> split(String technicalName) {
			ArrayList<String> tokens = new ArrayList<>();
			TokenMatcher matcher = new TokenMatcher(_acronyms, _acronymTokens, technicalName);
			while (true) {
				String next = matcher.next();
				if (next == null) {
					break;
				}
				tokens.add(next);
			}

			return tokens;
		}

		private boolean isAllUpperCase(String part) {
			return part.equals(part.toUpperCase());
		}

		private void collectClassifierDoc(TypeElement type) {
			for (VariableElement field : enumConstantsIn(type)) {
				String propertyName = asString(field);
				String key = qualifiedName(type) + "." + propertyName;

				_configDoc.setProperty(key, label(field, propertyName, true));
				String doc = extractDoc(type, field);
				if (!doc.isEmpty()) {
					_configDoc.setProperty(key + ".tooltip", doc);
				}
			}
		}

		private void collectAnnotationElementDoc(TypeElement type) {
			for (ExecutableElement element : methodsIn(type)) {
				String propertyName = asString(element);
				String key = qualifiedName(type) + "." + propertyName;

				_configDoc.setProperty(key, label(element, propertyName, true));
				String doc = extractDoc(type, element);
				if (!doc.isEmpty()) {
					_configDoc.setProperty(key + ".tooltip", doc);
				}
			}
		}

		private void collectPropertyDoc(TypeElement type) {
			for (ExecutableElement method : methodsIn(type)) {
				if (asString(method).startsWith("set")
					|| isStatic(method)
					|| !method.getParameters().isEmpty()) {
					continue;
				}

				ExecutableElement original = originalDefinition(method);

				// Note: The property name must be derived from the original method, because there
				// might
				// exist a @Name annotation.
				String propertyName = propertyName(original);

				String key = qualifiedName(type) + "." + propertyName;
				if (method == original) {
					// Only produce name property for the original method. No not repeat the
					// property
					// name for an overridden property. Only update a potentially updated
					// documentation
					// (tool-tip).
					_configDoc.setProperty(key, label(method, propertyName, true));
				}

				String doc = extractDoc(containingClass(method), method);
				if (!doc.isEmpty()) {
					_configDoc.setProperty(key + ".tooltip", doc);
				}
			}
		}

		boolean isPolymorphicConfiguration(TypeMirror typeElement) {
			return _wellKnown._polymorphicConfigurationType != null
				&& isSubType(typeElement, _wellKnown._polymorphicConfigurationType);
		}

		boolean isI18NExtension(TypeMirror typeElement) {
			return _wellKnown._i18nConstantsType != null
				&& isSubType(typeElement, _wellKnown._i18nConstantsType);
		}

		boolean isThemeConstantsClass(TypeMirror typeElement) {
			return _wellKnown._themeConstantsType != null
				&& isSubType(typeElement, _wellKnown._themeConstantsType);
		}

		boolean isWithPropertiesClass(TypeMirror typeElement) {
			return _wellKnown._withPropertiesType != null
				&& isSubType(typeElement, _wellKnown._withPropertiesType);
		}

		private void collectI18NConstantDoc(TypeElement type) {
			for (VariableElement field : fieldsIn(type)) {
				if (!isReskeyField(field)) {
					continue;
				}

				String propertyName = asString(field);
				String key = "class." + qualifiedName(type) + "." + propertyName;

				DocTreePath pathToField = docTreePathForElement(field);
				if (pathToField == null) {
					// field has no comment.
					continue;
				}
				UnknownBlockTagTree description = firstUnknownTag(field, "@en");
				if (description != null) {
					String doc =
						extractDoc(type, new DocTreePath(pathToField, description), description.getContent());
					if (!doc.isEmpty()) {
						_configDoc.setProperty(key, doc);

						UnknownBlockTagTree tooltip = firstUnknownTag(field, "@tooltip");
						if (tooltip != null) {
							String tooltipDoc =
								extractDoc(type, new DocTreePath(pathToField, tooltip), tooltip.getContent());
							if (!tooltipDoc.isEmpty()) {
								_configDoc.setProperty(key + ".tooltip", tooltipDoc);
							}
						}

						String infoDoc = extractDoc(type, field);
						if (!infoDoc.isEmpty()) {
							_configDoc.setProperty(key + ".help", infoDoc);
						}
					}
				}
			}
		}

		private void collectThemeConstantsDoc(TypeElement type) {
			for (VariableElement field : fieldsIn(type)) {
				String propertyName = asString(field);
				String key = qualifiedName(type) + "." + propertyName;

				DocTreePath pathToField = docTreePathForElement(field);
				if (pathToField == null) {
					// field has no comment.
					continue;
				}

				String doc = extractDoc(type, field);
				_configDoc.setProperty(key, doc);
			}
		}

		private void collectWithPropertiesDoc(TypeElement type) {
			for (ExecutableElement method : methodsIn(type)) {
				Optional<String> templateVariableName = _wellKnown.getTemplateVariableName(method);
				if (templateVariableName.isEmpty()) {
					continue;
				}

				String variableName = templateVariableName.get();
				String key = qualifiedName(type) + "." + variableName;

				DocTreePath pathToMethod = docTreePathForElement(method);
				if (pathToMethod == null) {
					// field has no comment.
					continue;
				}

				String doc = extractDoc(type, method);
				_configDoc.setProperty(key, doc);
			}
		}

		private UnknownBlockTagTree firstUnknownTag(Element elem, String tagName) {
			// @ is not actually needed but makes it easier to find tags in source code.
			assert tagName.startsWith("@");
			UnknownBlockTagTree[] tags = docTrees().getDocCommentTree(elem).getBlockTags()
				.stream()
				.filter(docTree -> docTree.getKind() == DocTree.Kind.UNKNOWN_BLOCK_TAG)
				.map(UnknownBlockTagTree.class::cast)
				.filter(tag -> ("@" + tag.getTagName()).equals(tagName)).toArray(UnknownBlockTagTree[]::new);
			switch (tags.length) {
				case 0:
					return null;
				case 1:
					return tags[0];
				default:
					printWarning(elem, "ERROR: Multiple '" + tagName + "' tags for " + qualifiedName(elem));
					return tags[0];
			}
		}

		private String getAnnotatedLabel(Element element) {
			return _wellKnown.getAnnotatedLabel(element).orElse(null);
		}

		private ExecutableElement originalDefinition(ExecutableElement method) {
			Name methodName = method.getSimpleName();
			List<? extends VariableElement> parameters = method.getParameters();
			ExecutableElement original = method;
			while (true) {
				ExecutableElement overridden = overriddenMethod(original, methodName, parameters);
				if (overridden == null) {
					break;
				}
				original = overridden;
			}
			return original;
		}

		/**
		 * Compute the config name of the given method. Note, the passed value must be the original
		 * method definition of a potentially overridden method.
		 */
		private String propertyName(ExecutableElement originalMethod) {
			return _wellKnown.getAnnotatedName(originalMethod)
				.orElseGet(() -> defaultPropertyName(asString(originalMethod)));
		}

		private String defaultPropertyName(String methodId) {
			Matcher matcher = METHOD_NAME_PATTERN.matcher(methodId);
			matcher.matches();
			String propertyId = matcher.group(1);
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for (String part : split(propertyId)) {
				if (first) {
					first = false;
				} else {
					builder.append("-");
				}
				builder.append(part.toLowerCase());
			}
			return builder.toString();
		}

		private boolean isReskeyField(VariableElement field) {
			TypeMirror fieldType = field.asType();
			for (TypeMirror reskeyType : _wellKnown._reskeyTypes) {
				if (isSubType(fieldType, reskeyType)) {
					return true;
				}
			}
			return false;
		}

	}

	static class TokenMatcher {

		private static final String WORD_END = "(?<=\\p{Lower})(?=\\p{Upper})";

		private static final String ABBREVIATION_END = "(?<=\\p{Upper})(?=\\p{Upper}\\p{Lower})";

		private static final String SEPARATOR = "[-_\\.]";

		private static final Pattern PROPERTY_NAME_SPLITTER =
			Pattern.compile("(?:" + WORD_END + ")|(?:" + ABBREVIATION_END + ")|(?:" + SEPARATOR + "+)");

		private String _str;

		private Matcher _matcher;

		private int _pos;

		private Properties _acronyms;

		private List<String> _acronymTokens;

		/**
		 * Creates a {@link TokenMatcher}.
		 */
		public TokenMatcher(Properties acronyms, List<String> acronymTokens, String str) {
			_acronyms = acronyms;
			_acronymTokens = acronymTokens;
			_str = str;
			_matcher = PROPERTY_NAME_SPLITTER.matcher(str);
			_pos = 0;
		}

		public String next() {
			if (_pos == _str.length()) {
				return null;
			}
			for (String acronym : _acronymTokens) {
				if (_str.startsWith(acronym, _pos)) {
					String result = _acronyms.getProperty(acronym);
					_pos += acronym.length();
					return result;
				}
			}

			if (_matcher.find(_pos + 1)) {
				String result = _str.substring(_pos, _matcher.start());
				_pos = _matcher.end();
				return result;
			}

			String rest = _str.substring(_pos);
			_pos = _str.length();
			return rest;
		}

	}

	@FunctionalInterface
	interface Contents {

		void write() throws IOException, XMLStreamException;

	}

	/**
	 * {@link XMLWriter} creating the documentation from the source files.
	 */
	private class DocumentationWriter extends XMLWriter {

		private static final String SYNTHETIC_ROOT_TAG = "javadoc";

		/** Tags that do not allow "camel case" text content. */
		private final Set<String> NO_CAMEL_CASE_FORCING_TAGS =
			new HashSet<>(Arrays.asList(SYNTHETIC_ROOT_TAG, "p", "div"));

		private final Comparator<Element> BY_SIMPLE_NAME =
			(m1, m2) -> CharSequence.compare(m1.getSimpleName(), m2.getSimpleName());

		private final DocumentBuilder _docBuilder;

		private XMLOutputFactory _outFac;

		DocumentationWriter() throws ParserConfigurationException {
			_outFac = newOutputFactory();
			_docBuilder = newDocumentBuilder();
		}

		private XMLOutputFactory newOutputFactory() throws FactoryConfigurationError {
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
			return factory;
		}

		private DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			return factory.newDocumentBuilder();
		}

		void writeDocumentation(PackageTree packageTree, Map<PackageElement, List<TypeElement>> packages,
				Collection<? extends TypeElement> includedElements)
				throws SAXException, IOException, XMLStreamException {
			File packageListFile = new File(_destDir, "package-list.xml");

			if (packageListFile.exists()) {
				Document packageDoc = _docBuilder.parse(packageListFile);
				packageTree.fromXML(packageDoc);
			}

			// Update with new (merged) contents.
			writeXml(packageListFile, () -> {
				packageTree.save(_out);
			});

			for (Entry<PackageElement, List<TypeElement>> packageContents : packages.entrySet()) {
				PackageElement pkg = packageContents.getKey();
				writeXml(dirName(pkg), "package-info.xml", () -> {
					nl();
					startElement("package");
					attribute("name", qName(pkg));
					attribute("srcLink", _srcLink);

					writeDoc(pkg);

					List<TypeElement> types = packageContents.getValue();
					Collections.sort(types, BY_SIMPLE_NAME);

					Map<String, List<TypeElement>> typesByKind =
						types.stream().collect(Collectors.groupingBy(TLDoclet.this::kind));

					for (Entry<String, List<TypeElement>> typeKinds : typesByKind.entrySet()) {
						String kind = typeKinds.getKey();
						nl();
						startElement(plural(kind));
						for (TypeElement type : typeKinds.getValue()) {
							nl();
							emptyElement(kind);
							attribute("id", asString(type));
							if (isAbstract(type) && isClass(type)) {
								attribute("abstract", "true");
							}
						}
						endElement();
					}

					endElement();
				});
			}

			writeTypes(includedElements);

		}

		private void writeTypes(Collection<? extends TypeElement> includedElements)
				throws XMLStreamException, UnsupportedEncodingException, FileNotFoundException, IOException {
			for (TypeElement type : includedElements) {
				writeXml(dirName(containingPackage(type)), baseName(type) + ".xml", () -> writeType(type));
			}
		}

		private String dirName(PackageElement pkg) {
			return qName(pkg).replace('.', File.separatorChar);
		}

		private String baseName(TypeElement type) {
			return signatureSimple(type);
		}

		private String signatureSimple(Element clazz) {
			TypeElement containingClass = containingClass(clazz);
			if (containingClass == null) {
				return simpleName(clazz);
			} else {
				return baseName(containingClass) + "$" + simpleName(clazz);
			}
		}

		private void validateComment(List<? extends DocTree> commentText, TypeElement doc) {
			DocTree first = commentText.get(0);
			if (first.getKind() == DocTree.Kind.TEXT) {
				char firstChar = ((TextTree) first).getBody().charAt(0);
				if (!Character.isUpperCase(firstChar)) {
					printWarning(doc, "Documentation starts with an uppercase letter: " + qualifiedName(doc));
				}
			}

			boolean hasCommentWithDot = commentText.stream()
				.filter(docTree -> docTree.getKind() == DocTree.Kind.TEXT)
				.map(TextTree.class::cast)
				.map(TextTree::getBody)
				.filter(comment -> comment.indexOf('.') >= 0)
				.findAny()
				.isPresent();

			if (!hasCommentWithDot) {
				printWarning(doc,
					"Documentation contains at least one '.' character (normally at the end of the description): "
						+ qualifiedName(doc));
			}
		}

		private void writeType(TypeElement type) throws XMLStreamException, IOException {
			List<? extends DocTree> comments = docTree(type);
			if (comments.isEmpty()) {
				printWarning(type, "Missing documentation: " + qualifiedName(type));
			} else {
				validateComment(comments, type);
			}

			nl();
			startElement("type");
			{
				attribute("name", signature(type.asType()));
				writeVisibility(type);
				attribute("kind", kind(type));
				if (isAbstract(type) && isClass(type)) {
					attribute("abstract", "true");
				}
				if (isStatic(type) && isClass(type)
					&& containingClass(type) != null) {
					attribute("static", "true");
				}
				writeLineAttr(type);
				writeTypeParameters(type);
				writeExtends(type);
				writeImplements(type);
				writeAnnotations(type);
				writeDoc(type);
				writeInnerTypes(type);
				boolean inInterface = type.getKind().isInterface();
				writeFields(type, inInterface);
				writeConstructors(type, inInterface);
				writeMethods(type, inInterface);
			}
			endElement();
		}

		private void writeTypeParameters(TypeElement type) throws XMLStreamException {
			List<? extends TypeParameterElement> typeParameters = type.getTypeParameters();
			if (typeParameters.isEmpty()) {
				return;
			}
			startElement("args");
			for (TypeParameterElement typeParam : typeParameters) {
				writeVarDef("arg", typeParam);
			}
			endElement();
		}

		private void writeMethods(TypeElement type, boolean inInterface) throws XMLStreamException, IOException {
			ExecutableElement[] methods = methodsIn(type)
				.stream()
				.filter(TypeUtils::notHiddenElement)
				.toArray(ExecutableElement[]::new);
			if (methods.length == 0) {
				return;
			}
			nl();
			startElement("methods");
			for (ExecutableElement method : methods) {
				writeMethod(method, inInterface);
			}
			endElement();
		}

		private void writeConstructors(TypeElement type, boolean inInterface)
				throws XMLStreamException, IOException {
			ExecutableElement[] constructors = constructorsIn(type)
				.stream()
				.filter(TypeUtils::notHiddenElement)
				.toArray(ExecutableElement[]::new);
			if (constructors.length == 0) {
				return;
			}
			nl();
			startElement("constructors");
			for (ExecutableElement method : constructors) {
				writeMethod(method, inInterface);
			}
			endElement();
		}

		private void writeFields(TypeElement type, boolean inInterface) throws XMLStreamException, IOException {
			List<VariableElement> inner;
			if (type.getKind() == ElementKind.ENUM) {
				inner = enumConstantsIn(type);
			} else {
				inner = fieldsIn(type);
			}
			VariableElement[] fields = inner
				.stream()
				.filter(TypeUtils::notHiddenElement)
				.toArray(VariableElement[]::new);
			if (fields.length == 0) {
				return;
			}
			nl();
			startElement("fields");
			for (VariableElement field : fields) {
				writeField(field, inInterface);
			}
			endElement();
		}

		private void writeInnerTypes(TypeElement type) throws XMLStreamException {
			TypeElement[] innerTypes = typesIn(type)
				.stream()
				.filter(TLDoclet.this::notHiddenType)
				.toArray(TypeElement[]::new);
			if (innerTypes.length == 0) {
				return;
			}
			nl();
			startElement("contents");
			for (TypeElement inner : innerTypes) {
				writeTypeRef(inner);
			}
			endElement();
		}

		private void writeImplements(TypeElement type) throws XMLStreamException {
			List<? extends TypeMirror> interfaces = type.getInterfaces();
			if (interfaces.isEmpty()) {
				return;
			}
			nl();
			startElement("implements");
			for (TypeMirror intf : interfaces) {
				if (hiddenType(intf)) {
					continue;
				}

				writeTypeRef(intf);
			}
			endElement();
		}

		private void writeExtends(TypeElement type) throws XMLStreamException {
			TypeMirror superClass = type.getSuperclass();
			if (writeAsExtensions(superClass)) {
				nl();
				writeTypeRef("extends", superClass);
			}
		}

		private void writeField(VariableElement field, boolean inInterface) throws XMLStreamException, IOException {
			checkDocumentation(field);

			nl();
			startElement("field");
			{
				attribute("id", elementSignature(field));
				Object constantValue = field.getConstantValue();
				if (constantValue != null) {
					attribute("value", elements().getConstantExpression(constantValue));
				}
				writeVisibility(field);
				if (inInterface || isStatic(field)) {
					attribute("static", "true");
				}
				if (inInterface || isFinal(field)) {
					attribute("final", "true");
				}
				writeLineAttr(field);

				writeTypeRef(field.asType());
				writeAnnotations(field);

				writeDoc(field);
			}
			endElement();
		}

		private void writeMethod(ExecutableElement method, boolean inInterface) throws XMLStreamException, IOException {
			ElementKind methodKind = method.getKind();

			nl();
			startElement(methodKind == ElementKind.CONSTRUCTOR ? "constructor" : "method");
			{
				String signature = executableSignature(method);

				attribute("id", signature);
				writeLineAttr(method);

				writeVisibility(method);
				if (isStatic(method)) {
					attribute("static", "true");
				}
				if (methodKind == ElementKind.METHOD) {
					if (!inInterface && isAbstract(method)) {
						attribute("abstract", "true");
					}

					if (method.isDefault()) {
						attribute("default", "true");
					}

					ExecutableElement overriddenMethod =
						overriddenMethod(method, method.getSimpleName(), method.getParameters());
					if (overriddenMethod != null) {
						attribute("overrides", signature(containingClass(overriddenMethod).asType()));
					} else {
						checkDocumentation(method);
					}

					List<? extends TypeParameterElement> typeParameters = method.getTypeParameters();
					if (!typeParameters.isEmpty()) {
						startElement("args");
						for (TypeParameterElement arg : typeParameters) {
							writeVarDef("arg", arg);
						}
						endElement();
					}

					writeReturn(method);

					AnnotationValue defaultValue = method.getDefaultValue();
					if (defaultValue != null) {
						startElement("defaultValue");
						writeValue(defaultValue.getValue());
						endElement();
					}
				}
				writeAnnotations(method);

				nl();
				startElement("params");
				for (VariableElement param : method.getParameters()) {
					writeParam(method, param, paramTag(method, param));
				}
				endElement();

				writeDoc(method);
			}
			endElement();
		}

		private void writeVisibility(Element type) throws XMLStreamException {
			attribute("visibility", visibility(type));
		}

		private String visibility(Element type) {
			Set<Modifier> modifiers = type.getModifiers();
			if (modifiers.contains(Modifier.PUBLIC)) {
				return "public";
			}
			if (modifiers.contains(Modifier.PROTECTED)) {
				return "protected";
			}
			if (modifiers.contains(Modifier.PRIVATE)) {
				return "private";
			}
			return "package";
		}

		private ParamTree paramTag(ExecutableElement method, VariableElement param) {
			for (ParamTree tag : paramTags(method)) {
				if (param.getSimpleName().contentEquals(tag.getName().getName())) {
					return tag;
				}
			}
			return null;
		}

		private void writeParam(Element ownerMethod, VariableElement param, ParamTree tag)
				throws XMLStreamException, IOException {
			nl();
			startElement("param");
			attribute("name", asString(param));

			writeTypeRef(param.asType());

			writeAnnotations(param);
			if (tag != null) {
				writeDoc(ownerMethod, tag, () -> tag.getDescription());
			}
			endElement();
		}

		private void writeDoc(Element docOwner, DocTree doc, Supplier<List<? extends DocTree>> inlineTags)
				throws XMLStreamException, IOException {
			if (doc != null && !stripTodo(text(doc)).isEmpty()) {
				DocTreePath parent =
					DocTreePath.getPath(docTrees().getPath(docOwner), docTrees().getDocCommentTree(docOwner), doc);
				writeHtml(position(parent), parent, inlineTags.get());
			}
		}

		private String text(DocTree tag) {
			return DocTreeToString.toString(tag);
		}

		private String text(Collection<? extends DocTree> tags) {
			return DocTreeToString.toString(tags);
		}

		private String stripNewLine(String xml) {
			return xml.replace("\r\n", " ").replace("\r", " ").replace("\n", " ");
		}

		private void checkDocumentation(Element field) {
			DocCommentTree fieldCommentTree = docTrees().getDocCommentTree(field);
			if (fieldCommentTree == null || (fieldCommentTree.getFullBody().isEmpty() && seeTags(field).isEmpty())) {
				if (needsDocumentation(field)) {
					printWarning(field, "Missing documentation: " + qualifiedName(field));
				}
			}
		}

		private void writeAnnotations(Element element) throws XMLStreamException {
			writeAnnotations(element.getAnnotationMirrors());
		}

		private void writeAnnotations(List<? extends AnnotationMirror> annotations)
				throws XMLStreamException {
			if (annotations.isEmpty()) {
				return;
			}

			nl();
			startElement("annotations");
			for (AnnotationMirror annotation : annotations) {
				writeAnnotation(annotation);
			}
			endElement();
		}

		private void writeAnnotation(AnnotationMirror annotation) throws XMLStreamException {
			nl();
			startElement("annotation");
			writeAnnotationValue(annotation);
			endElement();
		}

		private void writeAnnotationValue(AnnotationMirror annotation) throws XMLStreamException {
			attribute("id", signature(annotation.getAnnotationType()));

			Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotation.getElementValues();
			if (elementValues.isEmpty()) {
				return;
			}
			nl();
			startElement("params");
			for (Entry<? extends ExecutableElement, ? extends AnnotationValue> pair : elementValues.entrySet()) {
				startElement("param");
				try {
					attribute("name", asString(pair.getKey()));
				} catch (Exception exception) {
					// An exception seems to be thrown only if an annotation with a property of
					// type
					// array of string is annotated to a class or interface
					attribute("name", "<unknown annotation element name>");
				}

				try {
					writeValue(pair.getValue().getValue());
				} catch (Exception exception) {
					writeValue("<unknown annotation value>");
				}
				endElement();
			}
			endElement();
		}

		private void writeValue(Object obj) throws XMLStreamException {
			if (obj instanceof AnnotationMirror) {
				startElement("value");
				attribute("kind", "annotation");
				writeAnnotationValue((AnnotationMirror) obj);
				endElement();
			} else if (obj instanceof TypeMirror) {
				startElement("value");
				attribute("kind", "type");
				writeTypeRef((TypeMirror) obj);
				endElement();
			} else if (obj instanceof VariableElement) {
				emptyElement("value");
				attribute("kind", "enum");
				VariableElement field = (VariableElement) obj;
				attribute("id", signature(containingClass(field).asType()));
				attribute("member", elementSignature(field));
			} else if (obj.getClass().isArray()) {
				startElement("value");
				attribute("kind", "array");
				startElement("values");
				for (int n = 0, cnt = Array.getLength(obj); n < cnt; n++) {
					AnnotationValue elem = (AnnotationValue) Array.get(obj, n);
					writeValue(elem.getValue());
				}
				endElement();
				endElement();
			} else if (obj instanceof Collection) {
				startElement("value");
				attribute("kind", "array");
				startElement("values");
				for (Object entry : (Collection<?>) obj) {
					AnnotationValue elem = (AnnotationValue) entry;
					writeValue(elem.getValue());
				}
				endElement();
				endElement();
			} else {
				emptyElement("value");
				attribute("kind", valueKind(obj));
				attribute("label", obj.toString());
			}
		}

		private String valueKind(Object obj) {
			if (obj instanceof String) {
				return "string";
			}
			if (obj instanceof Number) {
				return "number";
			}
			if (obj instanceof Character) {
				return "char";
			}
			if (obj instanceof Boolean) {
				return "boolean";
			}
			return "unknown";
		}

		private void writeVarDef(String tagName, TypeParameterElement typeParam) throws XMLStreamException {
			startElement(tagName);
			attribute("id", signature(typeParam.asType()));
			attribute("kind", typeKind(typeParam.asType()));

			TypeMirror[] bounds = typeParam.getBounds()
				.stream()
				.filter(this::writeAsExtensions)
				.toArray(TypeMirror[]::new);
			if (bounds.length > 0) {
				startElement("bounds");
				for (TypeMirror bound : bounds) {
					writeTypeRef("bound", bound);
				}
				endElement();
			}

			endElement();
		}

		private boolean writeAsExtensions(TypeMirror superClass) {
			return superClass.getKind() != TypeKind.NONE
				&& !isSubType(_wellKnown._objectType.asType(), superClass)
				&& !hiddenType(superClass);
		}

		private void writeLineAttr(Element element)
				throws XMLStreamException {
			attribute("line", Long.toString(lineNumber(element)));
		}

		long lineNumber(Element element) {
			return position(element).line();
		}

		private String plural(String kind) {
			return kind.endsWith("s") ? kind + "es" : kind + "s";
		}

		private void writeDoc(Element doc) throws XMLStreamException, IOException {
			DocTreePath docTreePathForElement = docTreePathForElement(doc);
			writeHtml(position(doc), docTreePathForElement, docTree(doc));

			List<SeeTree> seeTags = seeTags(doc);
			if (!seeTags.isEmpty()) {
				nl();
				startElement("sees");
				for (SeeTree tag : seeTags) {
					writeSee(new DocTreePath(docTreePathForElement, tag), tag);
				}
				endElement();
			}
		}

		private List<SeeTree> seeTags(Element elem) {
			return seeTags(docTrees().getDocCommentTree(elem));
		}

		private List<SeeTree> seeTags(DocCommentTree commentTree) {
			return bockTagsOfKind(commentTree, DocTree.Kind.SEE, SeeTree.class);
		}

		private void writeSee(DocTreePath tagPath, SeeTree tag) throws XMLStreamException, IOException {
			List<? extends DocTree> reference = tag.getReference();
			if (reference.isEmpty()) {
				return;
			}
			String text = text(reference);
			boolean hasContent = text != null && !stripTodo(text).isEmpty();

			nl();
			if (hasContent) {
				startElement("see");
			} else {
				emptyElement("see");
				return;
			}
			boolean skipFirstWord = true;
			DocTree firstRef = reference.get(0);
			if (firstRef.getKind() == DocTree.Kind.REFERENCE) {
				Element referencedElement = docTrees().getElement(new DocTreePath(tagPath, firstRef));
				if (referencedElement != null) {
					switch (referencedElement.getKind()) {
						case ANNOTATION_TYPE:
						case CLASS:
						case ENUM:
						case INTERFACE:
						case RECORD:
							attribute("class", signature(referencedElement.asType()));
							break;
						case CONSTRUCTOR:
						case ENUM_CONSTANT:
						case EXCEPTION_PARAMETER:
						case FIELD:
						case INSTANCE_INIT:
						case LOCAL_VARIABLE:
						case METHOD:
						case MODULE:
						case OTHER:
						case PARAMETER:
						case RESOURCE_VARIABLE:
						case STATIC_INIT:
						case TYPE_PARAMETER:
						case BINDING_VARIABLE:
						case RECORD_COMPONENT:
							attribute("class", signature(referencedElement.getEnclosingElement().asType()));
							attribute("member", elementSignature(referencedElement));
							break;
						case PACKAGE:
							attribute("package", asString(referencedElement));
							break;
					}
					reference = reference.subList(1, reference.size());
					skipFirstWord = false;
				}
			}
			if (hasContent) {
				writeHtmlContent(position(tagPath), tagPath, reference, skipFirstWord);
				endElement();
			}
		}

		private void writeHtml(SourcePosition position, DocTreePath parentPath, List<? extends DocTree> tags)
				throws XMLStreamException, IOException {
			writeHtmlContent(position, parentPath, tags, false);
		}

		private void writeHtmlContent(SourcePosition location, DocTreePath parentPath, List<? extends DocTree> tags,
				boolean skipFirstWord) throws XMLStreamException, IOException {
			class Scanner extends DocTreePathScanner<Void, StringBuilder> {

				boolean _skipFirstWord = skipFirstWord;

				Consumer<StringBuilder> _referenceContent = null;

				Consumer<StringBuilder> _referenceAttribute = null;

				@Override
				public Void visitLink(LinkTree tree, StringBuilder raw) {
					Consumer<StringBuilder> formerContent = _referenceContent;
					Consumer<StringBuilder> formerAttribute = _referenceAttribute;
					try {
						List<? extends DocTree> label = tree.getLabel();
						if (!label.isEmpty()) {
							_referenceContent = out -> {
								scan(label, out);
							};
						}
						if (tree.getKind() == DocTree.Kind.LINK_PLAIN) {
							_referenceAttribute = out -> {
								out.append("plain=\"");
								out.append("true");
								out.append("\" ");
							};
						}
						scan(tree.getReference(), raw);
					} finally {
						_referenceAttribute = formerAttribute;
						_referenceContent = formerContent;
					}
					return null;
				}

				@Override
				public Void visitReference(ReferenceTree tree, StringBuilder raw) {
					Element referencedElement = docTrees().getElement(getCurrentPath());
					if (referencedElement != null) {
						raw.append("<ref ");
						if (_referenceAttribute != null) {
							_referenceAttribute.accept(raw);
						}
						switch (referencedElement.getKind()) {
							case ANNOTATION_TYPE:
							case CLASS:
							case ENUM:
							case INTERFACE:
							case RECORD:
								raw.append("class=\"");
								raw.append(signature(referencedElement.asType()));
								raw.append("\" ");
								break;
							case CONSTRUCTOR:
							case ENUM_CONSTANT:
							case EXCEPTION_PARAMETER:
							case FIELD:
							case INSTANCE_INIT:
							case LOCAL_VARIABLE:
							case METHOD:
							case MODULE:
							case OTHER:
							case PARAMETER:
							case RESOURCE_VARIABLE:
							case STATIC_INIT:
							case TYPE_PARAMETER:
							case BINDING_VARIABLE:
							case RECORD_COMPONENT:
								raw.append("class=\"");
								raw.append(signature(referencedElement.getEnclosingElement().asType()));
								raw.append("\" ");

								raw.append("member=\"");
								raw.append(elementSignature(referencedElement));
								raw.append("\" ");
								break;
							case PACKAGE:
								raw.append("package=\"");
								raw.append(asString(referencedElement));
								raw.append("\" ");
								break;
						}
						raw.append(">");
						if (_referenceContent != null) {
							_referenceContent.accept(raw);
						}
						raw.append("</ref>");
					}
					return super.visitReference(tree, raw);
				}

				@Override
				public Void visitText(TextTree tree, StringBuilder raw) {
					internalVisitText(tree, raw);
					return super.visitText(tree, raw);
				}

				@Override
				public Void visitErroneous(ErroneousTree tree, StringBuilder raw) {
					internalVisitText(tree, raw);
					return super.visitErroneous(tree, raw);
				}

				private void internalVisitText(TextTree tree, StringBuilder raw) {
					String text = tree.getBody();

					if (_skipFirstWord && !(text.startsWith("<") || text.startsWith("\""))) {
						int start = findTextStart(text);
						if (start >= 0) {
							text = text.substring(start);
						} else {
							// Plain reference.
							text = "";
						}
					}
					raw.append(text);
				}

				/**
				 * Find the start of the documentation text in a "see" param.
				 * 
				 * <p>
				 * The JavaDoc tool reports the reference to the target together with the
				 * description text.
				 * </p>
				 */
				private int findTextStart(String text) {
					int nesting = 0;
					for (int n = 0, cnt = text.length(); n < cnt; n++) {
						char ch = text.charAt(n);
						switch (ch) {
							case '(':
							case '<':
								nesting++;
								break;
							case ')':
							case '>':
								nesting--;
								break;
							default:
								if (Character.isWhitespace(ch)) {
									if (nesting == 0) {
										return n + 1;
									}
								}
								break;
						}
					}

					return text.length();
				}

				@Override
				public Void visitStartElement(StartElementTree node, StringBuilder p) {
					p.append('<').append(node.getName());
					Void returnVal = super.visitStartElement(node, p);
					if (node.isSelfClosing()) {
						p.append('/');
					}
					p.append('>');
					return returnVal;
				}

				@Override
				public Void visitEntity(EntityTree node, StringBuilder p) {
					p.append("&").append(node.getName()).append(";");
					return null;
				}

				@Override
				public Void visitAttribute(AttributeTree node, StringBuilder p) {
					p.append(' ');
					p.append(node.getName());
					p.append('=');
					switch (node.getValueKind()) {
						case EMPTY:
							p.append("\"null\"");
							break;
						case DOUBLE:
						case SINGLE:
						case UNQUOTED:
							p.append('"');
							StringBuilder attrValue = new StringBuilder();
							scan(node.getValue(), attrValue);
							int quote = attrValue.indexOf("\"");
							while (quote >= 0) {
								attrValue = attrValue.replace(quote, quote + 1, "&quot;");
								quote = attrValue.indexOf("\"", quote + 6);
							}
							p.append(attrValue);
							p.append('"');
							break;
					}
					return null;
				}

				@Override
				public Void visitEndElement(EndElementTree node, StringBuilder p) {
					p.append('<')
						.append('/')
						.append(node.getName())
						.append('>');
					return super.visitEndElement(node, p);
				}

				@Override
				public Void visitLiteral(LiteralTree node, StringBuilder p) {
					return null; // Skip. We don't supported this, yet.
				}

			}

			StringBuilder raw = new StringBuilder();

			Scanner scanner = new Scanner();
			for (DocTree tag : tags) {
				scanner.scan(new DocTreePath(parentPath, tag), raw);
				scanner._skipFirstWord = false;
			}

			String text = stripTodo(raw.toString());

			if (text.isEmpty()) {
				return;
			}

			// Catch common quoting mistakes.
			text = text.replaceAll("<(?![!/a-zA-Z])", "&lt;");
			text = text.replaceAll("&(?![#a-zA-Z])", "&amp;");
			text = text.replaceAll("<xmp>", "<xmp><![CDATA[");
			text = text.replaceAll("</xmp>", "]]></xmp>");

			String xml = "<" + SYNTHETIC_ROOT_TAG + ">" + text + "</" + SYNTHETIC_ROOT_TAG + ">";

			Document document;
			try {
				InputSource source = new InputSource(new StringReader(xml));
				document = _docBuilder.parse(source);
			} catch (SAXException ex) {
				printWarning(location,
					"Invalid XML: " + stripNewLine(ex.getMessage()) + " Content: " + stripNewLine(xml));

				startElement("title");
				_out.writeCData(text.replace("]]>", "]]]]><![CDATA[>"));
				endElement();
				return;
			}

			new DocWriter(location, _out).writeDoc(document);
		}

		private String elementSignature(Element member) {
			if (member instanceof ExecutableElement) {
				return executableSignature((ExecutableElement) member);
			} else {
				return asString(member);
			}
		}

		private String executableSignature(ExecutableElement executable) {
			String s;
			if (executable.getKind() == ElementKind.CONSTRUCTOR) {
				// simple name is <init>
				s = asString(executable.getEnclosingElement());
			} else {
				s = asString(executable);
			}
			return s
				+ "(" + executable.getParameters()
					.stream()
					.map(p -> signature(p.asType(), true))
					.collect(Collectors.joining(","))
				+ ")";
		}

		private void writeReturn(ExecutableElement method) throws XMLStreamException, IOException {
			nl();
			startElement("return");
			writeTypeRef(method.getReturnType());
			ReturnTree returnTag = returnTag(method);
			writeDoc(method, returnTag, () -> returnTag.getDescription());
			endElement();
		}

		private ReturnTree returnTag(ExecutableElement method) {
			List<ReturnTree> tags = returnTags(method);
			switch (tags.size()) {
				case 0:
					return null;
				case 1:
					return tags.get(0);
				default:
					printWarning(method, "ERROR: Multiple return tags for " + qualifiedName(method));
					return tags.get(0);
			}
		}

		private void writeTypeRef(TypeElement type) throws XMLStreamException {
			writeTypeRef(type.asType());
		}

		private void writeTypeRef(TypeMirror type) throws XMLStreamException {
			writeTypeRef("type", type);
		}

		private void writeTypeRef(String tag, TypeMirror type) throws XMLStreamException {
			startElement(tag);
			attribute("id", signature(type));
			attribute("kind", typeKind(type));
			if (hiddenType(type)) {
				attribute("hidden", "true");
			}

			int dim = 0;
			TypeMirror elementType = type;
			while (true) {
				if (elementType.getKind() != TypeKind.ARRAY) {
					break;
				}
				elementType = ((ArrayType) elementType).getComponentType();
				dim++;
			}

			if (dim > 0) {
				attribute("dim", Integer.toString(dim));
			}

			if (type.getKind() == TypeKind.WILDCARD) {
				WildcardType wildcard = (WildcardType) type;
				List<? extends TypeMirror> extendsBounds = extendsBounds(wildcard);
				if (!extendsBounds.isEmpty()) {
					startElement("extendsBounds");
					for (TypeMirror bound : extendsBounds) {
						writeTypeRef("bound", bound);
					}
					endElement();
				}
				List<? extends TypeMirror> superBounds = superBounds(wildcard);
				if (!superBounds.isEmpty()) {
					startElement("superBounds");
					for (TypeMirror bound : superBounds) {
						writeTypeRef("bound", bound);
					}
					endElement();
				}
			}

			if (type.getKind() == TypeKind.DECLARED) {
				List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
				if (!typeArguments.isEmpty()) {
					startElement("args");
					for (TypeMirror argType : typeArguments) {
						writeTypeRef("arg", argType);
					}
					endElement();
				}
			}

			endElement();
		}

		private List<? extends TypeMirror> extendsBounds(WildcardType wildcard) {
			TypeMirror bound = wildcard.getExtendsBound();
			if (bound == null) {
				return Collections.emptyList();
			}
			switch (bound.getKind()) {
				case INTERSECTION:
					return ((IntersectionType) bound).getBounds();
				default:
					return Collections.singletonList(bound);

			}
		}

		private List<? extends TypeMirror> superBounds(WildcardType wildcard) {
			TypeMirror bound = wildcard.getSuperBound();
			if (bound == null) {
				return Collections.emptyList();
			}
			switch (bound.getKind()) {
				case INTERSECTION:
					return ((IntersectionType) bound).getBounds();
				default:
					return Collections.singletonList(bound);

			}
		}

		private boolean hiddenType(TypeMirror element) {
			switch (element.getKind()) {
				case BOOLEAN:
				case BYTE:
				case CHAR:
				case DOUBLE:
				case FLOAT:
				case INT:
				case LONG:
				case SHORT:
					return false;
				case ARRAY:
					return ignoreDimension(element, this::hiddenType);
				case INTERSECTION:
				case NONE:
				case TYPEVAR:
				case UNION:
				case WILDCARD:
				case VOID:
					return false;
				default:
					break;
			}
			Element doc = types().asElement(element);
			if (doc == null) {
				return false;
			}
			if (hiddenPackage(containingPackage(doc))) {
				return true;
			}
			return hiddenElement(doc);
		}

		private String typeKind(TypeMirror type) {
			switch (type.getKind()) {
				case BOOLEAN:
				case BYTE:
				case DOUBLE:
				case FLOAT:
				case LONG:
				case SHORT:
				case INT:
				case CHAR:
				case VOID:
					return "primitive";
				case WILDCARD:
					return "wildcard";
				case TYPEVAR:
					return "typevar";
				case DECLARED:
					if (((DeclaredType) type).getTypeArguments().isEmpty()) {
						return "raw";
					}
					return "generic";
				case ARRAY:
					return ignoreDimension(type, this::typeKind);
				case ERROR:
				case EXECUTABLE:
				case INTERSECTION:
				case MODULE:
				case NONE:
				case NULL:
				case OTHER:
				case PACKAGE:
				case UNION:
					throw new IllegalArgumentException("Unexpected type of kind " + type.getKind() + ": " + type);
			}
			throw new IllegalArgumentException("Type of unknown kind " + type.getKind() + ": " + type);
		}

		private File writeXml(String dirName, String fileName, Contents contents)
				throws XMLStreamException, IOException {
			File dir = new File(_destDir, dirName);
			dir.mkdirs();
			File file = new File(dir, fileName);
			writeXml(file, contents);
			return dir;
		}

		private void writeXml(File file, Contents contents) throws XMLStreamException, IOException {
			try (Writer w = new OutputStreamWriter(new FileOutputStream(file), "utf-8")) {
				_out = _outFac.createXMLStreamWriter(w);
				_out.writeStartDocument();

				contents.write();

				_out.writeEndDocument();
			}
		}

		class DocWriter extends XMLWriter {

			private SourcePosition _location;

			/**
			 * Creates a {@link DocWriter}.
			 */
			public DocWriter(SourcePosition location, XMLStreamWriter out) {
				super(out);
				_location = location;
			}

			public void writeDoc(Document document) throws XMLStreamException {
				writeDoc(document.getDocumentElement());
			}

			private void writeDoc(org.w3c.dom.Element root) throws XMLStreamException {
				Node node = root.getFirstChild();

				startElement("title");
				node = writeTitle(node);
				endElement();

				if (node != null) {
					String text = node.getTextContent();
					int titleEnd = getTitleEnd(text);
					while (titleEnd < text.length() && Character.isWhitespace(text.charAt(titleEnd))) {
						titleEnd++;
					}

					String remaining = text.substring(titleEnd);
					node = node.getNextSibling();

					if (remaining.isEmpty() && node == null) {
						return;
					}

					startElement("doc");
					checkForInvalidRefs(remaining);
					_out.writeCharacters(remaining);
					while (node != null) {
						writeDocNode(node);
						node = node.getNextSibling();
					}
					endElement();
				}
			}

			private int getTitleEnd(String text) {
				int length = text.length();

				int index = 0;
				while (true) {
					int dotIndex = text.indexOf('.', index);
					if (dotIndex < 0) {
						return -1;
					}
					if (dotIndex == length - 1) {
						return length;
					}
					if (Character.isWhitespace(text.charAt(dotIndex + 1))) {
						// Fix common mistakes.
						if (!containsAt(text, dotIndex, "e.g.")) {
							return dotIndex + 2;
						}
					}

					index = dotIndex + 1;
				}
			}

			/**
			 * Whether the given text contains the given pattern at the position described by
			 * <code>dotIndex</code>.
			 *
			 * @param text
			 *        The text to search the pattern in.
			 * @param lastCharIndex
			 *        The character index of the potential end character of the given pattern in the
			 *        given text.
			 * @param pattern
			 *        The pattern to search for.
			 * @return Whether the given text contains the given pattern at the given end position.
			 */
			private boolean containsAt(String text, int lastCharIndex, String pattern) {
				int firstCharIndex = lastCharIndex - pattern.length() + 1;
				if (firstCharIndex < 0) {
					return false;
				}
				for (int textIndex = firstCharIndex, patternIndex =
					0; textIndex <= lastCharIndex; textIndex++, patternIndex++) {
					if (text.charAt(textIndex) != pattern.charAt(patternIndex)) {
						return false;
					}
				}
				return true;
			}

			private Node writeTitle(Node node) throws DOMException, XMLStreamException {
				while (node != null) {
					switch (node.getNodeType()) {
						case Node.TEXT_NODE:
							String text = node.getTextContent();

							int titleEnd = getTitleEnd(text);
							if (titleEnd >= 0) {
								String title = text.substring(0, titleEnd);
								// Just check title. Remaining content is checked later.
								checkForInvalidRefs(title);
								_out.writeCharacters(title);
								return node;
							} else {
								checkForInvalidRefs(text);
								_out.writeCharacters(text);
								break;
							}
						case Node.CDATA_SECTION_NODE:
							_out.writeCData(node.getTextContent());
							break;
						case Node.ELEMENT_NODE:
							writeDocElement((org.w3c.dom.Element) node);
							break;
						default:
							break;
					}

					node = node.getNextSibling();
				}
				return null;
			}

			private void checkForInvalidRefs(String text) {
				Iterator<String> it = CamelCaseIterator.newIterator(text);
				while (it.hasNext()) {
					String camelCaseWord = it.next();
					if (_acronyms.get(camelCaseWord.toLowerCase()) != null) {
						continue;
					}

					printWarning(_location, "Invalid camel case word '" + camelCaseWord
						+ "', this may only be used within JavaDoc links or HTML code tags.");
				}
			}

			private void writeDocElement(org.w3c.dom.Element element) throws XMLStreamException {
				startElement(element.getLocalName());
				{
					NamedNodeMap attributes = element.getAttributes();
					for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
						Node attribute = attributes.item(n);
						attribute(attribute.getNodeName(), attribute.getNodeValue());
					}

					writeDocContets(element);
				}
				endElement();
			}

			private void writeDocContets(org.w3c.dom.Element element) throws DOMException, XMLStreamException {
				for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
					writeDocNode(child);
				}
			}

			private void writeDocNode(Node node) throws XMLStreamException {
				switch (node.getNodeType()) {
					case Node.TEXT_NODE:
						String text = node.getTextContent();
						String enclosingTagName = node.getParentNode().getNodeName();
						if (SYNTHETIC_ROOT_TAG.equals(enclosingTagName)) {
							// Apply wiki formatting for empty lines.
							String[] parts = text.trim().split("\\n\\s*\\n");
							if (parts.length > 1) {
								for (String part : parts) {
									startElement("p");
									_out.writeCharacters(part);
									endElement();
								}
							}
						}
						if (NO_CAMEL_CASE_FORCING_TAGS.contains(enclosingTagName)) {
							checkForInvalidRefs(text);
						}
						_out.writeCharacters(text);
						break;
					case Node.CDATA_SECTION_NODE:
						_out.writeCData(node.getTextContent());
						break;
					case Node.ELEMENT_NODE:
						writeDocElement((org.w3c.dom.Element) node);
						break;
					default:
						break;
				}
			}
		}

	}

}
