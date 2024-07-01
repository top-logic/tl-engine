/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.processor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

import com.top_logic.common.json.adapt.ReaderR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.xref.model.AnnotationInfo;
import com.top_logic.xref.model.FloatValue;
import com.top_logic.xref.model.IndexFile;
import com.top_logic.xref.model.IntValue;
import com.top_logic.xref.model.ListValue;
import com.top_logic.xref.model.StringValue;
import com.top_logic.xref.model.TypeInfo;


/**
 * {@link Processor} generating an index of public classes and interfaces with their
 * generalizations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public final class TypeIndexer extends AbstractProcessor
		implements AnnotationValueVisitor<com.top_logic.xref.model.Value, Void> {

	Map<String, TypeInfo> _types;

	private Elements _elementUtils;

	private Types _typeUtils;

	private TypeElement _instantiationContextType;

	private TypeMirror _configurationItemRaw;

	private TypeElement _polymorphicConfigurationElement;

	private JavaTypeModelUtil _boundFinder;

	private TypeMirror _polymorphicConfigurationRaw;

	@Override
	public synchronized void init(ProcessingEnvironment newEnv) {
		super.init(newEnv);

		_elementUtils = processingEnv.getElementUtils();
		_typeUtils = processingEnv.getTypeUtils();

		_boundFinder = new JavaTypeModelUtil(_typeUtils, _elementUtils);

		_types = new HashMap<>();
	}

	private void lookupBaseTypes() {
		_instantiationContextType = _elementUtils.getTypeElement("com.top_logic.basic.config.InstantiationContext");
		if (_instantiationContextType == null) {
			// System.err.println("No instantiation context type found.");
		}

		TypeElement configurationItemType =
			_elementUtils.getTypeElement("com.top_logic.basic.config.ConfigurationItem");
		if (configurationItemType != null) {
			_configurationItemRaw = raw(configurationItemType.asType());
		} else {
			// System.err.println("No configuration item type found.");
		}

		_polymorphicConfigurationElement =
			_elementUtils.getTypeElement("com.top_logic.basic.config.PolymorphicConfiguration");
		if (_polymorphicConfigurationElement != null) {
			_polymorphicConfigurationRaw = raw(_polymorphicConfigurationElement.asType());
		} else {
			// System.err.println("No polymorphic configuration type found.");
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		lookupBaseTypes();

		try {
			CharSequence pkg = "";
			Location location = StandardLocation.CLASS_OUTPUT;
			Filer filer = processingEnv.getFiler();

			processConfigurationItems(roundEnv.getRootElements());

			if (roundEnv.processingOver()) {
				CharSequence indexName = "META-INF/com.top_logic.basic.reflect.TypeIndex.json";
				try {
					FileObject oldResource = filer.getResource(location, pkg, indexName);
					try (InputStream in = oldResource.openInputStream()) {
						JsonReader reader = new JsonReader(new ReaderR(new InputStreamReader(in, "utf-8")));
						IndexFile typeIndex = IndexFile.readIndexFile(reader);

						for (Entry<String, TypeInfo> entry : typeIndex.getTypes().entrySet()) {
							String binaryName = entry.getKey();
							if (!_types.containsKey(binaryName)) {
								TypeElement existingType =
									_elementUtils.getTypeElement(binaryName.replace('$', '.'));
								if (existingType != null) {
									_types.put(binaryName, entry.getValue());
								}
							}
						}
					}
				} catch (UnsupportedOperationException ex) {
					// Javac does not support reading resources, which is not necessary in non-incremental builds.
				} catch (IOException ex) {
					// Does not yet exist.
				}

				IndexFile typeIndex = IndexFile.create();
				ArrayList<String> sorted = new ArrayList<>(_types.keySet());
				Collections.sort(sorted);
				for (String name : sorted) {
					typeIndex.getTypes().put(name, _types.get(name));
				}

				FileObject resource = filer.createResource(location, pkg, indexName);
				try (Writer writer = resource.openWriter()) {
					typeIndex.writeContent(new JsonWriter(writer));
				}
			}

			return false;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void processConfigurationItems(Collection<? extends Element> elements) {
		for (Element element : elements) {
			ElementKind kind = element.getKind();
			if (kind.isInterface() || kind == ElementKind.CLASS) {
				TypeElement type = (TypeElement) element;

				if (isStatic(type)) {
					TypeInfo info = TypeInfo.create();

					if (isInterface(type)) {
						info.setInterface(true);

						info.setImplementation(findImplementationType(type));
						info.setConfiguration(null);
					} else {
						info.setImplementation(null);
						info.setConfiguration(findConfigurationType(type));
					}

					if (isAbstract(type)) {
						info.setAbstract(true);
					}

					if (isPublic(type)) {
						info.setPublic(true);
					}

					TypeMirror superClassMirror = type.getSuperclass();
					if (superClassMirror.getKind() != TypeKind.NONE) {
						addTypeName(superClassMirror, info);
					}
					for (TypeMirror superInterfaceMirror : type.getInterfaces()) {
						addTypeName(superInterfaceMirror, info);
					}

					List<? extends AnnotationMirror> annotations = type.getAnnotationMirrors();
					for (AnnotationMirror annotation : annotations) {
						DeclaredType annotationType = annotation.getAnnotationType();
						TypeElement annotationTypeElement = (TypeElement) annotationType.asElement();
						Retention retention = annotationTypeElement.getAnnotation(Retention.class);
						if (retention == null || retention.value() != RetentionPolicy.RUNTIME) {
							continue;
						}

						AnnotationInfo annotationInfo = toAnnotationInfo(annotation);
						Name annotationName = _elementUtils.getBinaryName(annotationTypeElement);
						info.getAnnotations().put(annotationName.toString(), annotationInfo);
					}

					_types.put(_elementUtils.getBinaryName(type).toString(), info);
				}

				if (isConfigurationItem(type)) {
					if (!type.getModifiers().contains(Modifier.PUBLIC)) {
						if (!hasAnnotation(type, "com.top_logic.basic.annotation.FrameworkInternal")) {
							processingEnv.getMessager().printMessage(Kind.ERROR,
								"Configuration interfaces must be public.", type);
						}
					}
				}
			}

			List<? extends Element> enclosedElements = element.getEnclosedElements();
			if (enclosedElements.contains(element)) {
				enclosedElements = new ArrayList<>(enclosedElements);
				processingEnv.getMessager().printMessage(Kind.ERROR,
					TypeIndexer.class.getName() + ": Element contains itself:" + element, element);
				enclosedElements.remove(element);
			}
			processConfigurationItems(enclosedElements);
		}
	}

	private String findImplementationType(TypeElement type) {
		if (isPolymorphicConfiguration(type)) {
			TypeMirror implType = _boundFinder.getTypeBound(type, _polymorphicConfigurationElement, 0);
			if (implType != null) {
				return _elementUtils.getBinaryName((TypeElement) _typeUtils.asElement(implType)).toString();
			} else {
				// System.err.println("No implementation type found for polymorphic configuration " + type + ".");
			}
		}
		// System.err.println("Type " + type.asType() + " is not " + _polymorphicConfigurationRaw + ".");
		return null;
	}

	private String findConfigurationType(TypeElement type) {
		for (Element member : type.getEnclosedElements()) {
			if (member.getKind() != ElementKind.CONSTRUCTOR) {
				continue;
			}
			ExecutableElement constructor = (ExecutableElement) member;
			
			if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
				continue;
			}
			
			List<? extends VariableElement> parameters = constructor.getParameters();
			if (parameters.size() != 2) {
				continue;
			}
			
			VariableElement p1 = parameters.get(0);
			if (!isInstantiationContextType(p1.asType())) {
				continue;
			}
			
			VariableElement p2 = parameters.get(1);
			TypeMirror p2TypeErasure = _typeUtils.erasure(p2.asType());
			TypeElement p2Type = (TypeElement) _typeUtils.asElement(p2TypeErasure);
			
			return _elementUtils.getBinaryName(p2Type).toString();
		}
		return null;
	}

	private AnnotationInfo toAnnotationInfo(AnnotationMirror annotation) {
		AnnotationInfo result = AnnotationInfo.create();
		putAnnotationValues(result.getProperties(), annotation.getElementValues());
		return result;
	}

	private void putAnnotationValues(Map<String, com.top_logic.xref.model.Value> properties,
			Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues) {
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
			com.top_logic.xref.model.Value valueInfo = toValueInfo(entry.getValue());
			if (valueInfo == null) {
				continue;
			}
			properties.put(entry.getKey().getSimpleName().toString(), valueInfo);
		}
	}

	private com.top_logic.xref.model.Value toValueInfo(AnnotationValue value) {
		return value.accept(this, null);
	}


	private boolean isStatic(TypeElement type) {
		return type.getEnclosingElement().getKind() == ElementKind.PACKAGE
			|| type.getModifiers().contains(Modifier.STATIC);
	}

	private void addTypeName(TypeMirror superClassMirror, TypeInfo info) {
		TypeElement superTypeElement =
			(TypeElement) _typeUtils.asElement(superClassMirror);
		if (superTypeElement == null) {
			/* Can happen when the workspace contains compile errors, for example if the super type
			 * does not exist or is too broken. */
			return;
		}
		String binaryName = _elementUtils.getBinaryName(superTypeElement).toString();
		if (!"java.lang.Object".equals(binaryName)) {
			info.getGeneralizations().add(binaryName);
		}
	}

	private boolean isPublic(TypeElement type) {
		return type.getModifiers().contains(Modifier.PUBLIC);
	}

	private boolean isAbstract(TypeElement type) {
		if (isInterface(type)) {
			return hasAnnotation(type, "com.top_logic.basic.config.annotation.Abstract");
		} else {
			return type.getModifiers().contains(Modifier.ABSTRACT);
		}
	}

	private boolean hasAnnotation(TypeElement type, String annotationTypeName) {
		for (AnnotationMirror mirror : type.getAnnotationMirrors()) {
			TypeElement typeElement = (TypeElement) mirror.getAnnotationType().asElement();
			if (typeElement.getQualifiedName().toString().equals(annotationTypeName)) {
				return true;
			}
		}
		return false;
	}

	private boolean isInterface(TypeElement type) {
		return type.getKind().isInterface();
	}

	private boolean isInstantiationContextType(TypeMirror type) {
		if (_instantiationContextType == null) {
			return false;
		}
		TypeMirror configurationItemType = _instantiationContextType.asType();
		return _typeUtils.isSameType(type, configurationItemType);
	}
	
	private boolean isConfigurationItem(TypeElement typeElement) {
		return isRawInterface(_configurationItemRaw, typeElement);
	}

	private boolean isPolymorphicConfiguration(TypeElement typeElement) {
		return isRawInterface(_polymorphicConfigurationRaw, typeElement);
	}

	private boolean isRawInterface(TypeMirror rawInterface, TypeElement typeElement) {
		if (rawInterface == null) {
			return false;
		}
		if (typeElement.getKind() != ElementKind.INTERFACE) {
			return false;
		}
		return _typeUtils.isSubtype(raw(typeElement.asType()), rawInterface);
	}

	private TypeMirror raw(TypeMirror type) {
		return _typeUtils.erasure(type);
	}

	@Override
	public com.top_logic.xref.model.Value visit(AnnotationValue av, Void p) {
		return null;
	}

	@Override
	public com.top_logic.xref.model.Value visit(AnnotationValue av) {
		return null;
	}

	@Override
	public com.top_logic.xref.model.Value visitBoolean(boolean b, Void p) {
		return null;
	}

	@Override
	public com.top_logic.xref.model.Value visitByte(byte b, Void p) {
		return IntValue.create().setValue(b);
	}

	@Override
	public com.top_logic.xref.model.Value visitChar(char c, Void p) {
		return StringValue.create().setValue(Character.toString(c));
	}

	@Override
	public com.top_logic.xref.model.Value visitDouble(double d, Void p) {
		return FloatValue.create().setValue(d);
	}

	@Override
	public com.top_logic.xref.model.Value visitFloat(float f, Void p) {
		return FloatValue.create().setValue(f);
	}

	@Override
	public com.top_logic.xref.model.Value visitInt(int i, Void p) {
		return IntValue.create().setValue(i);
	}

	@Override
	public com.top_logic.xref.model.Value visitLong(long i, Void p) {
		return IntValue.create().setValue(i);
	}

	@Override
	public com.top_logic.xref.model.Value visitShort(short s, Void p) {
		return IntValue.create().setValue(s);
	}

	@Override
	public com.top_logic.xref.model.Value visitString(String s, Void p) {
		return StringValue.create().setValue(s);
	}

	@Override
	public com.top_logic.xref.model.Value visitType(TypeMirror t, Void p) {
		return StringValue.create()
			.setValue(_elementUtils.getBinaryName((TypeElement) _typeUtils.asElement(t)).toString());
	}

	@Override
	public com.top_logic.xref.model.Value visitEnumConstant(VariableElement c, Void p) {
		return StringValue.create().setValue(c.getSimpleName().toString());
	}

	@Override
	public com.top_logic.xref.model.Value visitAnnotation(AnnotationMirror a, Void p) {
		com.top_logic.xref.model.AnnotationValue result = com.top_logic.xref.model.AnnotationValue.create()
			.setType(_elementUtils.getBinaryName((TypeElement) a.getAnnotationType().asElement()).toString());

		putAnnotationValues(result.getProperties(), a.getElementValues());
		a.getElementValues();

		return result;
	}

	@Override
	public com.top_logic.xref.model.Value visitArray(List<? extends AnnotationValue> vals, Void p) {
		ListValue result = ListValue.create();
		for (AnnotationValue value : vals) {
			result.getValues().add(toValueInfo(value));
		}
		return result;
	}

	@Override
	public com.top_logic.xref.model.Value visitUnknown(AnnotationValue av, Void p) {
		return null;
	}

}
