/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstanceAccess;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.Utils;
import com.top_logic.element.config.AssociationConfig;
import com.top_logic.element.config.AssociationConfig.EndConfig;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.element.config.AttributedTypeConfig;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.ExtendsConfig;
import com.top_logic.element.config.InterfaceConfig;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.ObjectTypeConfig;
import com.top_logic.element.config.PartConfig;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.config.ReferenceConfig.ReferenceKind;
import com.top_logic.element.config.SingletonConfig;
import com.top_logic.element.config.annotation.TLSingletons;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.DatatypeConfig;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;
import com.top_logic.model.config.ModelPartConfig;
import com.top_logic.model.config.PartAspect;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link TLModelVisitor} creating a {@link ModelConfig} from a run-time {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelConfigExtractor implements TLModelVisitor<ModelPartConfig, Void> {

	@Override
	public ModelConfig visitModel(TLModel model, Void arg) {
		ModelConfig config = create(ModelConfig.class);
		extractModelConfig(config, model);
		return config;
	}

	private void extractModelConfig(ModelConfig modelConfig, TLModel model) {
		for (TLModule module : sorted(model.getModules())) {
			modelConfig.getModules().add(createModuleConfig(module));
		}
	}

	@Override
	public ModuleConfig visitModule(TLModule model, Void arg) {
		return createModuleConfig(model);
	}

	private ModuleConfig createModuleConfig(TLModule module) {
		ModuleConfig moduleConfig = create(ModuleConfig.class);
		extractModuleConfig(moduleConfig, module);
		return moduleConfig;
	}

	private void extractModuleConfig(ModuleConfig moduleConfig, TLModule module) {
		copyIfDifferent(moduleConfig::getName, moduleConfig::setName, module.getName());
		for (TLType type : sorted(module.getTypes())) {
			TypeConfig typeConfig = createTypeConfig(type);
			if (typeConfig == null) {
				continue;
			}
			moduleConfig.getTypes().add(typeConfig);
		}
		extractAnnotations(moduleConfig, module);

		// Synchronize singletons annotation.
		TLSingletons singletonsAnnotation = moduleConfig.getAnnotation(TLSingletons.class);
		for (TLModuleSingleton singleton : module.getSingletons()) {
			if (singletonsAnnotation == null) {
				singletonsAnnotation = create(TLSingletons.class);
				moduleConfig.getAnnotations().add(singletonsAnnotation);
			}
			if (singletonsAnnotation.getSingleton(singleton.getName()) == null) {
				singletonsAnnotation.getSingletons().add(extractSingletonConfig(singleton));
			}
		}
	}

	private TypeConfig createTypeConfig(TLType type) {
		return (TypeConfig) type.visitType(this, null);
	}

	private SingletonConfig extractSingletonConfig(TLModuleSingleton singleton) {
		SingletonConfig singletonConfig = create(SingletonConfig.class);
		fill(singletonConfig, singleton);
		return singletonConfig;
	}

	private void fill(SingletonConfig singletonConfig, TLModuleSingleton singleton) {
		copyIfDifferent(singletonConfig::getName, singletonConfig::setName, singleton.getName());
	}

	private static <T extends ConfigurationItem> T create(Class<T> type) {
		return TypedConfiguration.newConfigItem(type);
	}

	@Override
	public TypeConfig visitPrimitive(TLPrimitive model, Void arg) {
		DatatypeConfig config = create(DatatypeConfig.class);
		extractTypeConfig(config, model);
		// Mandatory property
		config.setKind(model.getKind());
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<StorageMapping<?>> storageConfig =
			(PolymorphicConfiguration<StorageMapping<?>>) InstanceAccess.INSTANCE.getConfig(model.getStorageMapping());
		copyIfDifferent(config::getStorageMapping, config::setStorageMapping, storageConfig);
		copyIfDifferent(config::isBinary, config::setBinary, model.isBinary());
		copyIfDifferent(config::getDBType, config::setDBType, model.getDBType());
		copyIfDifferent(config::getDBPrecision, config::setDBPrecision, model.getDBPrecision());
		copyIfDifferent(config::getDBSize, config::setDBSize, model.getDBSize());
		return config;
	}

	@Override
	public TypeConfig visitEnumeration(TLEnumeration model, Void arg) {
		EnumConfig config = create(EnumConfig.class);
		extractTypeConfig(config, model);
		for (TLClassifier classifier : model.getClassifiers()) {
			config.getClassifiers().add(createClassifierConfig(classifier));
		}
		return config;
	}

	@Override
	public ClassifierConfig visitClassifier(TLClassifier model, Void arg) {
		return createClassifierConfig(model);
	}

	private ClassifierConfig createClassifierConfig(TLClassifier model) {
		ClassifierConfig config = create(ClassifierConfig.class);
		extractClassifierConfig(config, model);
		return config;
	}

	private void extractClassifierConfig(ClassifierConfig config, TLClassifier model) {
		copyIfDifferent(config::getName, config::setName, model.getName());
		extractAnnotations(config, model);
	}

	@Override
	public TypeConfig visitClass(TLClass model, Void arg) {
		ObjectTypeConfig config;
		if (model.isAbstract()) {
			config = create(InterfaceConfig.class);
		} else {
			ClassConfig classConfig = create(ClassConfig.class);
			copyIfDifferent(classConfig::isFinal, classConfig::setFinal, model.isFinal());
			config = classConfig;
		}
		extractAttributedTypeConfig(config, model);
		extractGeneralizationConfig(config, model);
		return config;
	}

	private void extractGeneralizationConfig(ObjectTypeConfig config, TLClass model) {
		TLModule module = model.getModule();
		for (TLClass generalization : model.getGeneralizations()) {
			ExtendsConfig generalizationConfig = create(ExtendsConfig.class);
			generalizationConfig.setQualifiedTypeName(typeRef(module, generalization));
			config.getGeneralizations().add(generalizationConfig);
		}
	}

	@Override
	public TypeConfig visitAssociation(TLAssociation model, Void arg) {
		List<TLAssociationEnd> ends = TLModelUtil.getEnds(model);
		if (ends.size() == 2) {
			for (TLAssociationEnd end : ends) {
				if (end.getReference() != null) {
					// Only represented by its references.
					return null;
				}
			}
		}
		AssociationConfig config = create(AssociationConfig.class);
		extractAttributedTypeConfig(config, model);
		return config;
	}

	private void extractAttributedTypeConfig(AttributedTypeConfig config, TLStructuredType model) {
		extractTypeConfig(config, model);
		for (TLStructuredTypePart part : model.getLocalParts()) {
			config.getAttributes().add((PartConfig) part.visitStructuredTypePart(this, null));
		}
	}

	private void extractTypeConfig(TypeConfig config, TLType model) {
		extractTypeName(config, model);
		extractAnnotations(config, model);
	}

	private void extractTypeName(TypeConfig config, TLNamedPart model) {
		copyIfDifferent(config::getName, config::setName, model.getName());
	}

	private <A extends TLAnnotation> void extractAnnotations(AnnotatedConfig<A> config, TLModelPart model) {
		for (TLAnnotation annotation : model.getAnnotations()) {
			@SuppressWarnings("unchecked")
			A typedAnnotation = (A) annotation;
			config.getAnnotations().add(TypedConfiguration.copy(typedAnnotation));
		}
	}

	@Override
	public PartConfig visitProperty(TLProperty model, Void arg) {
		AttributeConfig config = create(AttributeConfig.class);
		extractPartConfig(config, model);
		return config;
	}

	@Override
	public PartConfig visitReference(TLReference model, Void arg) {
		ReferenceConfig config = create(ReferenceConfig.class);
		boolean override = extractPartConfig(config, model);
		if (!override) {
			copyIfDifferent(config::canNavigate, config::setNavigate, model.getEnd().canNavigate());
			copyIfDifferent(config::isAggregate, config::setAggregate, model.getEnd().isAggregate());
			copyIfDifferent(config::isComposite, config::setComposite, model.getEnd().isComposite());
			copyIfDifferent(config::getHistoryType, config::setHistoryType, model.getEnd().getHistoryType());
			copyIfDifferent(config::getKind, config::setKind, kind(model));
			if (TLModelUtil.getEnds(model.getEnd().getOwner()).size() == 2) {
				TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(model.getEnd());
				TLReference inverseReference = otherEnd.getReference();
				if (inverseReference != null) {
					copyIfDifferent(config::getInverseReference, config::setInverseReference,
						inverseReference.getName());
				}
			} else {
				copyIfDifferent(config::getEndName, config::setEndName, TLModelUtil.qualifiedName(model.getEnd()));
			}
		} else {
			if (kind(model) == ReferenceKind.BACKWARDS) {
				copyIfDifferent(config::getKind, config::setKind, ReferenceKind.BACKWARDS);
			}
		}
		return config;
	}

	private ReferenceKind kind(TLReference model) {
		TLAssociationEnd end = model.getEnd();
		List<TLAssociationEnd> allEnds = TLModelUtil.getEnds(end.getOwner());
		if (allEnds.size() != 2) {
			return ReferenceKind.NONE;
		}
		int endIndex = allEnds.indexOf(end);
		switch (endIndex) {
			case 0:
				return ReferenceKind.BACKWARDS;
			case 1:
				return ReferenceKind.FORWARDS;
			default:
				return ReferenceKind.NONE;
		}
	}

	@Override
	public PartConfig visitAssociationEnd(TLAssociationEnd model, Void arg) {
		EndConfig config = create(EndConfig.class);
		boolean override = extractPartConfig(config, model);
		if (!override) {
			copyIfDifferent(config::canNavigate, config::setNavigate, model.canNavigate());
			copyIfDifferent(config::isAggregate, config::setAggregate, model.isAggregate());
			copyIfDifferent(config::isComposite, config::setComposite, model.isComposite());
			copyIfDifferent(config::getHistoryType, config::setHistoryType, model.getHistoryType());
		}
		return config;
	}

	private boolean extractPartConfig(PartConfig config, TLStructuredTypePart model) {
		extractAnnotations(config, model);
		extractPartAspectConfig(config, model);
		boolean override = model.isOverride();
		copyIfDifferent(config::isOverride, config::setOverride, override);
		copyIfDifferent(config::isAbstract, config::setAbstract, model.isAbstract());

		TLType valueType = model.getType();
		TLModule module = model.getOwner().getModule();
		config.setTypeSpec(typeRef(module, valueType));

		if (!override) {
			copyIfDifferent(config::getMandatory, config::setMandatory, model.isMandatory());
			copyIfDifferent(config::isMultiple, config::setMultiple, model.isMultiple());
			copyIfDifferent(config::isOrdered, config::setOrdered, model.isOrdered());
			copyIfDifferent(config::isBag, config::setBag, model.isBag());
		}
		return override;
	}

	private String typeRef(TLModule ownerModule, TLType type) {
		return type.getModule() == ownerModule ? type.getName() : TLModelUtil.qualifiedName(type);
	}

	private void extractPartAspectConfig(PartAspect config, TLNamedPart model) {
		copyIfDifferent(config::getName, config::setName, model.getName());
	}

	private static <T extends TLNamedPart> List<T> sorted(Collection<T> collection) {
		List<T> result = new ArrayList<>(collection);
		Collections.sort(result, new Comparator<TLNamedPart>() {
			@Override
			public int compare(TLNamedPart o1, TLNamedPart o2) {
				if (o1 == o2) {
					return 0;
				}

				// Sort data types and enumerations to the top of the file.
				int kindResult = compareKind(o1.getModelKind(), o2.getModelKind());
				if (kindResult != 0) {
					return kindResult;
				}

				// Sort generalizations to the top of the file.
				if (o1.getModelKind() == ModelKind.CLASS && o2.getModelKind() == ModelKind.CLASS) {
					TLClass c1 = (TLClass) o1;
					TLClass c2 = (TLClass) o2;
					return compareGeneralization(c1, c2);
				} else {
					return compareName(o1, o2);
				}
			}

			private int compareName(TLNamedPart o1, TLNamedPart o2) {
				return o1.getName().compareTo(o2.getName());
			}

			private int compareGeneralization(TLClass c1, TLClass c2) {
				List<TLClass> path1 = pathToRootInModule(c1);
				List<TLClass> path2 = pathToRootInModule(c2);
				
				int n1 = path1.size() - 1;
				int n2 = path2.size() - 1;
				while (n1 >= 0 && n2 >= 0) {
					TLClass e1 = path1.get(n1--);
					TLClass e2 = path2.get(n2--);
					if (e1 != e2) {
						return compareName(e1, e2);
					}
				}

				if (n1 < 0) {
					// c1 is a generalization of c2
					return -1;
				}
				if (n2 < 0) {
					// c2 is a generalization of c1
					return 1;
				}
				return 0;
			}

			private List<TLClass> pathToRootInModule(TLClass t) {
				TLModule module = t.getModule();

				List<TLClass> path = new ArrayList<>();
				while (true) {
					path.add(t);
					TLClass generalization = primaryGeneralizationIn(module, t);
					if (generalization == null) {
						return path;
					}

					t = generalization;
				}
			}

			private TLClass primaryGeneralizationIn(TLModule module, TLClass t) {
				for (TLClass generalization : t.getGeneralizations()) {
					if (generalization.getModule() == module) {
						return generalization;
					}
				}
				return null;
			}

			private int compareKind(ModelKind k1, ModelKind k2) {
				int order1 = order(k1);
				int order2 = order(k2);
				return Integer.compare(order1, order2);
			}

			private int order(ModelKind kind) {
				switch (kind) {
					case DATATYPE:
						return 1;

					case ENUMERATION:
						return 2;

					case CLASS:
						return 3;

					case ASSOCIATION:
						return 4;

					default:
						return 5;
				}
			}
		});
		return result;
	}

	private static void copyIfDifferent(Supplier<String> getter, Consumer<String> setter, String value) {
		if (Utils.equals(StringServices.nonEmpty(value), StringServices.nonEmpty(getter.get()))) {
			return;
		}
		setter.accept(value);
	}

	private static <T> void copyIfDifferent(Supplier<T> getter, Consumer<T> setter, T value) {
		if (Utils.equals(value, getter.get())) {
			return;
		}
		setter.accept(value);
	}

	private static void copyIfDifferent(BooleanSupplier getter, BooleanConsumer setter, boolean value) {
		if (value == getter.getAsBoolean()) {
			return;
		}
		setter.accept(value);
	}

	@FunctionalInterface
	private interface BooleanConsumer {
		void accept(boolean value);
	}
}
