/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.util.Utils.*;
import static java.util.Comparator.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.core.log.Log;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.layout.security.AccessChecker;
import com.top_logic.layout.security.LiberalAccessChecker;
import com.top_logic.model.DerivedTLTypePart;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLAssociationProperty;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelFactory;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLModuleSingletons;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.TLTypeVisitor;
import com.top_logic.model.TransientObject;
import com.top_logic.model.access.IdentityMapping;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLSortOrder;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.v5.transform.ModelLayout;
import com.top_logic.model.visit.DefaultTLModelVisitor;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.model.TL5Types;

/**
 * Utilities for programmatic manipulation of a {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLModelUtil {

	private static final char SCOPE_ID_PART_SEPARATOR = '/';

	/** Separator of of type and the part of its type in a qualified name. */
	public static final char QUALIFIED_NAME_PART_SEPARATOR = '#';

	/** Separator of module and type in the qualified name of {@link TLType}. */
	public static final char QUALIFIED_NAME_SEPARATOR = ':';

	/** {@link Pattern} for splitting a qualified type name into its parts. */
	public static final Pattern SPLIT_PATTERN = Pattern.compile("" + QUALIFIED_NAME_SEPARATOR);

	/**
	 * {@link Pattern Regular expression} source for a name (part) of a {@link TLModelPart} name.
	 * 
	 * @see #TYPE_PART_NAME_PATTERN_SRC
	 */
	public static final String NAME_PATTERN_SRC = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

	/**
	 * {@link Pattern Regular expression} source of a dot-separated list of
	 * {@value #NAME_PATTERN_SRC names}.
	 */
	public static final String QNAME_PATTERN_SRC = NAME_PATTERN_SRC + "(?:" + "\\." + NAME_PATTERN_SRC + ")*";

	/**
	 * {@link Pattern Regular expression} source of a {@link TLModule} name.
	 */
	public static final String MODULE_NAME_PATTERN_SRC = QNAME_PATTERN_SRC;

	/**
	 * {@link Pattern Regular expression} source of a {@link TLType} name.
	 */
	public static final String TYPE_NAME_PATTERN_SRC =
		MODULE_NAME_PATTERN_SRC + "(?:" + SCOPE_ID_PART_SEPARATOR + "\\d+" + SCOPE_ID_PART_SEPARATOR + "\\d+" + ")?"
				+ QUALIFIED_NAME_SEPARATOR + QNAME_PATTERN_SRC;

	/**
	 * {@link Pattern Regular expression} source of a {@link TLTypePart} name.
	 */
	public static final String TYPE_PART_NAME_PATTERN_SRC =
		TYPE_NAME_PATTERN_SRC + QUALIFIED_NAME_PART_SEPARATOR + NAME_PATTERN_SRC;

	/**
	 * {@link Pattern Regular expression} source of a singleton {@link TLObject} name.
	 */
	public static final String SINGLETON_NAME_PATTERN_SRC =
		MODULE_NAME_PATTERN_SRC + QUALIFIED_NAME_PART_SEPARATOR + NAME_PATTERN_SRC;

	/**
	 * {@link Pattern Regular expression} source of a {@link TLModelPart}.
	 * <p>
	 * This can be one of: {@link TLModule}, {@link TLType}, {@link TLStructuredTypePart} or
	 * {@link TLModuleSingleton}.
	 * </p>
	 */
	public static final String MODEL_PART_NAME_PATTERN_SRC =
		MODULE_NAME_PATTERN_SRC + "(?:" + SCOPE_ID_PART_SEPARATOR + "\\d+" + SCOPE_ID_PART_SEPARATOR + "\\d+" + ")?" // Module
			+ "(?:" + QUALIFIED_NAME_SEPARATOR + QNAME_PATTERN_SRC + ")?" // Type
			+ "(?:" + QUALIFIED_NAME_PART_SEPARATOR + NAME_PATTERN_SRC + ")?"; // Attribute or Singleton

	private static final class QualifiedNameVisitor extends DefaultTLModelVisitor<Void, StringBuilder> {

		/** Singleton {@link QualifiedNameVisitor} instance. */
		public static final QualifiedNameVisitor INSTANCE = new QualifiedNameVisitor();

		private QualifiedNameVisitor() {
			// singleton instance
		}

		@Override
		protected Void visitType(TLType model, StringBuilder arg) {
			TLModule module = model.getModule();
			TLScope scope = model.getScope();
			String moduleName = module == null ? "<no-module>" : module.getName();
			if (scope != module) {
				String scopeName;
				TLModuleSingleton moduleAndName = model.getModel().getQuery(TLModuleSingletons.class).getModuleAndName(scope);
				if (moduleAndName == null || moduleAndName.getModule() != module) {
					KnowledgeItem scopeHandle = scope.tHandle();
					ObjectKey scopeId = scopeHandle.tId();
					scopeName = scopeId.getObjectType().getName() + SCOPE_ID_PART_SEPARATOR + scopeId.getBranchContext()
						+ SCOPE_ID_PART_SEPARATOR + IdentifierUtil.toExternalForm(scopeId.getObjectName());
					appendQualifiedName(arg, scopeName, model.getName());
				} else {
					scopeName = moduleAndName.getName();
					appendQualifiedName(arg, moduleName, scopeName, model.getName());
				}
			} else {
				appendQualifiedName(arg, moduleName, model.getName());
			}
			return null;
		}

		@Override
		protected Void visitTypePart(TLTypePart model, StringBuilder arg) {
			TLType owner = model.getOwner();
			if (owner == null) {
				arg.append("<unknown-type>");
			} else {
				owner.visit(this, arg);
			}
			arg.append(QUALIFIED_NAME_PART_SEPARATOR);
			arg.append(model.getName());
			return null;
		}

		@Override
		public Void visitModel(TLModel model, StringBuilder arg) {
			// TLModels have no name
			return null;
		}

		@Override
		public Void visitModule(TLModule model, StringBuilder arg) {
			arg.append(model.getName());
			return null;
		}

	}

	/**
	 * Visitor that removes the visited model part from the {@link TLModelPart} hierarchy
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class UnlinkPartVisitor extends DefaultTLModelVisitor<Void, Object> {

		/** Singleton {@link TLModelUtil.UnlinkPartVisitor} instance. */
		public static final UnlinkPartVisitor INSTANCE = new UnlinkPartVisitor();

		private UnlinkPartVisitor() {
			// singleton instance
		}

		@Override
		protected Void visitStructuredTypePart(TLStructuredTypePart model, Object arg) {
			model.getOwner().getLocalParts().remove(model);
			return super.visitStructuredTypePart(model, arg);
		}

		@Override
		public Void visitClassifier(TLClassifier model, Object arg) {
			model.getOwner().getClassifiers().remove(model);
			return super.visitClassifier(model, arg);
		}

		@Override
		public Void visitClass(TLClass model, Object arg) {
			model.getScope().getClasses().remove(model);
			return super.visitClass(model, arg);
		}

		@Override
		public Void visitAssociation(TLAssociation model, Object arg) {
			model.getScope().getAssociations().remove(model);
			return super.visitAssociation(model, arg);
		}

		@Override
		public Void visitEnumeration(TLEnumeration model, Object arg) {
			model.getScope().getEnumerations().remove(model);
			return super.visitEnumeration(model, arg);
		}

		@Override
		public Void visitPrimitive(TLPrimitive model, Object arg) {
			model.getScope().getDatatypes().remove(model);
			return super.visitPrimitive(model, arg);
		}

		@Override
		public Void visitModule(TLModule model, Object arg) {
			model.getModel().getModules().remove(model);
			return super.visitModule(model, arg);
		}

	}

	/**
	 * Visitor that deletes the visited model part and all "parts" of it from the
	 * {@link KnowledgeBase} of the part.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class DeleteVisitor extends DefaultTLModelVisitor<Void, Object> {
	
		/** Singleton {@link TLModelUtil.DeleteVisitor} instance. */
		public static final DeleteVisitor INSTANCE = new DeleteVisitor();
	
		private DeleteVisitor() {
			// singleton instance
		}
	
		@Override
		protected Void visitModelPart(TLModelPart model, Object arg) {
			deleteTLObject(model);
			return none;
		}

		@Override
		public Void visitReference(TLReference model, Object arg) {
			TLAssociationEnd end = model.getEnd();

			// Note: The end may be null, during a model refactoring operation, if the association
			// is already deleted.
			if (end != null && end.tValid()) {
				if (isForwardReference(model)) {
					return removeWholeReference(model, arg, end.getOwner());
				} else {
					return super.visitReference(model, arg);
				}
			}

			return none;
		}

		private Void removeWholeReference(TLReference model, Object arg, TLAssociation owner) {
			TLReference inverseReference = TLModelUtil.getForeignName(model);

			if (inverseReference != null) {
				super.visitReference(inverseReference, arg);
			}

			super.visitReference(model, arg);

			return visitValid(owner, arg);
		}

		private void deleteTLObject(TLObject model) {
			model.tHandle().delete();
		}

		@Override
		protected Void visitStructuredType(TLStructuredType model, Object arg) {
			for (TLModelPart part : copy(model.getLocalParts())) {
				visitValid(part, arg);
			}
			return super.visitStructuredType(model, arg);
		}
	
		@Override
		public Void visitClass(TLClass model, Object arg) {
			for (TLModelPart part : copy(model.getSpecializations())) {
				visitValid(part, arg);
			}
			return super.visitClass(model, arg);
		}
	
		@Override
		public Void visitEnumeration(TLEnumeration model, Object arg) {
			for (TLModelPart part : copy(model.getClassifiers())) {
				visitValid(part, arg);
			}
			return super.visitEnumeration(model, arg);
		}
	
		@Override
		public Void visitModel(TLModel model, Object arg) {
			for (TLModelPart part : copy(model.getModules())) {
				visitValid(part, arg);
			}
			return super.visitModel(model, arg);
		}
	
		@Override
		public Void visitModule(TLModule model, Object arg) {
			// Note: The singletons must be deleted before the modules types, because the singleton
			// normally uses a type of its module.
			for (TLModuleSingleton link : model.getSingletons()) {
				// Note: The singleton link must be destroyed before deleting the singleton object,
				// because registered singletons cannot be deleted.
				TLObject oldSingleton = link.getSingleton();
				link.tDelete();
				deleteTLObject(oldSingleton);
			}
			for (TLModelPart part : copy(model.getDatatypes())) {
				visitValid(part, arg);
			}
			for (TLModelPart part : copy(model.getClasses())) {
				visitValid(part, arg);
			}
			for (TLModelPart part : copy(model.getAssociations())) {
				visitValid(part, arg);
			}
			for (TLModelPart part : copy(model.getEnumerations())) {
				visitValid(part, arg);
			}

			return super.visitModule(model, arg);
		}

		private Void visitValid(TLModelPart part, Object arg) {
			if (part.tValid()) {
				return part.visit(this, arg);
			}

			return none;
		}

		TLModelPart[] copy(Collection<? extends TLModelPart> orig) {
			return orig.toArray(new TLModelPart[orig.size()]);
		}
	
	}

	/**
	 * Visitor that checks whether the visited type is compatible to the argument {@link TLType}.
	 * 
	 * @see #isCompatibleType(TLType, TLType)
	 */
	private static final TLTypeVisitor<Boolean, TLType> COMPATIBILITY_VISITOR = new TLTypeVisitor<>() {

		@Override
		public Boolean visitPrimitive(TLPrimitive model, TLType arg) {
			return equals(model, arg);
		}

		@Override
		public Boolean visitEnumeration(TLEnumeration model, TLType arg) {
			if (isTLObjectTable(arg)) {
				return true;
			}
			return equals(model, arg);
		}

		@Override
		public Boolean visitClass(TLClass model, TLType arg) {
			if (!(arg instanceof TLClass)) {
				return Boolean.FALSE;
			}
			if (isTLObjectTable(arg)) {
				return Boolean.TRUE;
			}
			return isGeneralization((TLClass) arg, model);
		}

		@Override
		public Boolean visitAssociation(TLAssociation model, TLType arg) {
			if (!(arg instanceof TLAssociation)) {
				return Boolean.FALSE;
			}
			return isInheritedSubset(model, arg);
		}

		private Boolean isInheritedSubset(TLAssociation model, TLType arg) {
			if (equals(model, arg)) {
				return Boolean.TRUE;
			}
			for (TLAssociation union : model.getUnions()) {
				if (Boolean.TRUE == isInheritedSubset(union, arg)) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		}

		private boolean isTLObjectTable(TLType type) {
			return equals(type, TLModelUtil.tlObjectType(type.getModel()));
		}

		private boolean equals(TLType t1, TLType t2) {
			return t1.equals(t2);
		}
	};

	/**
	 * Filter that only accepts abstract classes.
	 */
	public static final Filter<TLClass> IS_ABSTRACT = new Filter<>() {
		@Override
		public boolean accept(TLClass clazz) {
			return clazz.isAbstract();
		}
	};

	/**
	 * Filter that only accepts non-abstract classes.
	 */
	public static final Filter<TLClass> IS_CONCRETE = new Filter<>() {
		@Override
		public boolean accept(TLClass clazz) {
			return !clazz.isAbstract();
		}
	};

	/**
	 * Mapping that computes {@link TLClass#getGeneralizations()} for a given {@link TLClass}.
	 */
	public static final Mapping<TLClass, Iterable<TLClass>> GET_GENERALIZATIONS =
		new Mapping<>() {
			@Override
			public Iterable<TLClass> map(TLClass input) {
				return input.getGeneralizations();
			}
		};

	/**
	 * Retrieves the module with the given name from the given model, or creates a new one, if no
	 * module with that name does yet exist in the given model.
	 */
	public static TLModule makeModule(TLModel model, String name) {
		TLModule result = model.getModule(name);
		if (result != null) {
			return result;
		}
		return addModule(model, name);
	}

	/**
	 * Adds a new module to the given model.
	 * 
	 * @param model
	 *        The model to create the module in.
	 * @param name
	 *        the name of the new module.
	 * @return The newly created module.
	 */
	public static TLModule addModule(TLModel model, String name) {
		return model.addModule(model, name);
	}

	/**
	 * Retrieves the class with the given name from the given module, or creates a new one, if no
	 * class with that name does yet exist in the given module.
	 */
	public static TLClass makeClass(TLModule module, String name) {
		TLType result = module.getType(name);
		if (result != null) {
			return (TLClass) result;
		}
		return addClass(module, name);
	}

	/**
	 * Adds a new class to the given module.
	 * 
	 * @param module
	 *        The module to create the class in.
	 * @param name
	 *        the name of the new class.
	 * @return The newly created class.
	 */
	public static TLClass addClass(TLModule module, String name) {
		return addClass(module, module, name);
	}

	/**
	 * @see TLModelFactory#addClass(TLModule, TLScope, String)
	 */
	public static TLClass addClass(TLModule module, TLScope scope, String name) {
		assert name != null : "No anonymous classes.";
		return module.getModel().addClass(module, scope, name);
	}

	/**
	 * @see TLModelFactory#addEnumeration(TLModule, TLScope, String)
	 */
	public static TLEnumeration addEnumeration(TLModule module, TLScope scope, String name) {
		assert name != null : "No anonymous enumerations.";
		return module.getModel().addEnumeration(module, scope, name);
	}

	/**
	 * Adds a new enumeration to the given module.
	 * 
	 * @param module
	 *        The module to create the enumeration in.
	 * @param name
	 *        the name of the new enumeration.
	 * @return The newly created enumeration.
	 */
	public static TLEnumeration addEnumeration(TLModule module, String name) {
		return addEnumeration(module, module, name);
	}

	/**
	 * Retrieves the {@link TLEnumeration} with the given name from the given module, or creates a
	 * new one, if no {@link TLEnumeration} with that name does yet exist in the given module.
	 * 
	 * @see #addEnumeration(TLModule, TLScope, String)
	 */
	public static TLEnumeration makeEnumeration(TLModule module, String name) {
		TLType enumeration = module.getType(name);
		if (enumeration != null) {
			return (TLEnumeration) enumeration;
		}
		return addEnumeration(module, module, name);
	}

	/**
	 * Adds a new {@link TLPrimitive} to the given {@link TLModule}.
	 * 
	 * @param module
	 *        The module to create the data type in.
	 * @param name
	 *        the name of the new data type.
	 * @param mapping
	 *        The {@link StorageMapping} to convert application values back and forth to storage
	 *        values.
	 * @return The newly created data type.
	 */
	public static TLPrimitive addDatatype(TLModule module, String name, StorageMapping<?> mapping) {
		return addDatatype(module, module, name, Kind.CUSTOM, mapping);
	}

	/**
	 * @see #addDatatype(TLModule, TLScope, String, Kind, StorageMapping)
	 */
	public static TLPrimitive addDatatype(TLModule module, TLScope scope, String name, Kind kind) {
		return addDatatype(module, scope, name, kind, IdentityMapping.INSTANCE);
	}

	/**
	 * Adds a new {@link TLPrimitive} to the given {@link TLModule}.
	 * 
	 * @param module
	 *        The module to create the data type in.
	 * @param name
	 *        The name of the new data type.
	 * @param mapping
	 *        The {@link StorageMapping} to convert application values back and forth to storage
	 *        values.
	 * 
	 * @return The newly created data type.
	 */
	public static TLPrimitive addDatatype(TLModule module, TLScope scope, String name, Kind kind,
			StorageMapping<?> mapping) {
		assert name != null : "No anonymous data types.";
		return module.getModel().addDatatype(module, scope, name, kind, mapping);
	}

	/**
	 * Adds a new {@link TLClassifier} to the given {@link TLEnumeration}.
	 * 
	 * @param enumeration
	 *        The enumeration to create the classifier in.
	 * @param name
	 *        the name of the new classifier.
	 * @return The newly created classifier.
	 */
	public static TLClassifier addClassifier(TLEnumeration enumeration, String name) {
		TLClassifier result = enumeration.getModel().createClassifier(name);
		enumeration.getClassifiers().add(result);
		return result;
	}

	/**
	 * Adds a new named {@link TLAssociation} to the given {@link TLModule}.
	 * 
	 * @param module
	 *        The module to create the association in.
	 * @param name
	 *        the name of the new association.
	 * @return The newly created association.
	 */
	public static TLAssociation addAssociation(TLModule module, String name) {
		return addAssociation(module.getModel(), module, module, name);
	}

	/**
	 * @see TLModelFactory#addAssociation(TLModule, TLScope, String)
	 */
	public static TLAssociation addAssociation(TLModule module, TLScope scope, String name) {
		return addAssociation(module.getModel(), scope, module, name);
	}

	/**
	 * Adds a new named {@link TLAssociation} to the given {@link TLScope}.
	 * 
	 * @param factory
	 *        The factory to create the association with.
	 * @param scope
	 *        The scope to create the association in.
	 * @param module
	 *        The module of the new association.
	 * @param name
	 *        the name of the new association.
	 * @return The newly created association.
	 */
	public static TLAssociation addAssociation(TLModelFactory factory, TLScope scope, TLModule module, String name) {
		return factory.addAssociation(module, scope, name);
	}

	/**
	 * Adds a new named {@link TLAssociationEnd} to the given {@link TLAssociation}.
	 * 
	 * @param association
	 *        The association to create the end in.
	 * @param name
	 *        the name of the new association end.
	 * @return The newly created association end.
	 */
	public static TLAssociationEnd addEnd(TLAssociation association, String name, TLType targetType) {
		return association.getModel().addAssociationEnd(association, name, targetType);
	}

	/**
	 * Adds a new named {@link TLReference} to the given {@link TLClass}.
	 * 
	 * @param clazz
	 *        The class to create the reference in.
	 * @param name
	 *        the name of the new reference.
	 * @return The newly created reference.
	 */
	public static TLReference addReference(TLClass clazz, String name, TLAssociationEnd impl) {
		return clazz.getModel().addReference(clazz, name, impl);
	}

	/**
	 * Adds a new bidirectional reference between the given {@link TLClass}es.
	 * 
	 * @param sourceClass
	 *        The source class of the reference.
	 * @param sourceName
	 *        the reference name in the source class.
	 * @param destClass
	 *        The destination class of the reference.
	 * @param destName
	 *        The name of the backward reference in the destination class.
	 * @return The newly created association implementing the bidirectional reference.
	 */
	public static TLAssociation addReference(TLType sourceClass, String sourceName, TLType destClass, String destName) {
		StringBuilder associationName = new StringBuilder();
		if (sourceName != null) {
			associationName.append(sourceClass.getName());
			associationName.append('$');
			associationName.append(sourceName);
		} else {
			associationName.append(destClass.getName());
			associationName.append('$');
			associationName.append(destName);
		}
		TLAssociation association =
			addAssociation(sourceClass.getModule(), sourceClass.getScope(), associationName.toString());

		TLAssociationEnd source;
		if (destName != null) {
			source = addEnd(association, destName, sourceClass);
		} else {
			source = addEnd(association, "source", sourceClass);
		}
		TLAssociationEnd destination;
		if (sourceName != null) {
			destination = addEnd(association, sourceName, destClass);
		} else {
			destination = addEnd(association, "dest", destClass);
		}

		if (sourceName != null) {
			addReference((TLClass) sourceClass, sourceName, destination);
		}
		if (destName != null) {
			addReference((TLClass) destClass, destName, source);
		}

		return association;
	}

	/**
	 * Adds a new {@link TLClassProperty property} to the given class.
	 * 
	 * @param type
	 *        The class to add a property to.
	 * @param name
	 *        The name of the new property.
	 * @param valueType
	 *        The type of the new property.
	 * 
	 * @return the newly created property.
	 */
	public static TLClassProperty addProperty(TLClass type, String name, TLType valueType) {
		return type.getModel().addClassProperty(type, name, valueType);
	}

	/**
	 * Adds a new {@link TLAssociationProperty property} to the given association.
	 * 
	 * @param type
	 *        The association to add a property to.
	 * @param name
	 *        The name of the new property.
	 * @param valueType
	 *        The type of the new property.
	 * 
	 * @return the newly created property.
	 */
	public static TLAssociationProperty addProperty(TLAssociation type, String name, TLType valueType) {
		return type.getModel().addAssociationProperty(type, name, valueType);
	}

	/**
	 * Adds a new {@link TLProperty property} to the given type depending on the concrete
	 * {@link TLStructuredType}.
	 * 
	 * @see TLModelUtil#addProperty(TLClass, String, TLType)
	 * @see TLModelUtil#addProperty(TLAssociation, String, TLType)
	 */
	public static TLProperty addProperty(TLStructuredType type, String name, TLType valueType) {
		// Do not check for instance of TLClass because the concrete implementation is always a
		// TLClass, but not always a TLAssocation.
		if (type instanceof TLAssociation) {
			return addProperty((TLAssociation) type, name, valueType);
		} else {
			return addProperty((TLClass) type, name, valueType);
		}
	}

	/**
	 * An {@link TLAssociation} is private, if it has no name, two ends and implements only one
	 * {@link TLReference}.
	 */
	public static boolean isPrivateAssociation(TLAssociation model) {
		Iterable<TLAssociationEnd> ends = TLModelUtil.getEnds(model);
		if (model.getName() == null) {
			Iterator<TLAssociationEnd> iterator = ends.iterator();
			if (!iterator.hasNext()) {
				return false;
			}
			TLReference reference1 = iterator.next().getReference();
			if (!iterator.hasNext()) {
				return false;
			}
			TLReference reference2 = iterator.next().getReference();
			return (reference1 != null) ^ (reference2 != null);
		}
		return false;
	}

	/**
	 * The unique {@link TLReference} that is implemented by the given
	 * {@link #isPrivateAssociation(TLAssociation) private association}.
	 */
	public static TLReference getOwningReference(TLAssociation model) {
		Iterator<TLAssociationEnd> ends = TLModelUtil.getEnds(model).iterator();
		TLReference reference1 = ends.next().getReference();
		TLReference reference2 = ends.next().getReference();
		if (reference1 != null) {
			return reference1;
		} else {
			return reference2;
		}
	}

	/**
	 * Compute, whether the first class is a generalization of the second one.
	 * <p>
	 * Returns true, if c1 and c2 are equal.
	 * </p>
	 * 
	 * @param c1
	 *        The potential generalization.
	 * @param c2
	 *        The potential specialization.
	 * @return Whether <code>c1</code> is a generalization of <code>c2</code>.
	 */
	public static boolean isGeneralization(TLStructuredType c1, TLStructuredType c2) {
		if (c1.getModelKind() == ModelKind.CLASS && c2.getModelKind() == ModelKind.CLASS) {
			return isGeneralization((TLClass) c1, (TLClass) c2);
		} else {
			return c1.equals(c2);
		}
	}

	/**
	 * Compute, whether the first class is a generalization of the second one.
	 * <p>
	 * Returns true, if c1 and c2 are equal.
	 * </p>
	 * 
	 * @param c1
	 *        The potential generalization.
	 * @param c2
	 *        The potential specialization.
	 * @return Whether <code>c1</code> is a generalization of <code>c2</code>.
	 */
	public static boolean isGeneralization(TLClass c1, TLClass c2) {
		if (c1.equals(c2)) {
			return true;
		}
		/* The "reflexive" part is not necessary due to the equality check above. But that method
		 * directly returns the cache value without constructing a new Collection. */
		return getReflexiveTransitiveGeneralizations(c2).contains(c1);
	}

	/**
	 * Resolve a {@link Collection} of qualified {@link TLClass} names.
	 * 
	 * @param protocol
	 *        The {@link Protocol} to report errors to.
	 * @param qualifiedNames
	 *        The qualified name of each {@link TLClass}. See {@link #findType(TLModel, String)}.
	 * @return A new, mutable and resizable {@link Set}. For each name which can not be resolved or
	 *         resolves to something else than a {@link TLClass}, an error will be reported to the
	 *         {@link Protocol}.
	 */
	public static Set<TLClass> findClasses(Protocol protocol, Collection<String> qualifiedNames) {
		Set<TLType> types = findTypes(protocol, ModelService.getApplicationModel(), qualifiedNames);
		return reportNonTLClasses(protocol, types);
	}

	private static Set<TLClass> reportNonTLClasses(Protocol protocol, Set<TLType> types) {
		Set<TLClass> classes = new HashSet<>();

		for (TLType type : types) {
			if (type instanceof TLClass) {
				classes.add((TLClass) type);
			} else {
				protocol.error("Type " + type.getName() + " is not a TLClass.");
			}
		}

		return classes;
	}

	/**
	 * Resolve a collection of qualified type names against a given model.
	 * 
	 * @param protocol
	 *        The {@link Protocol} to report errors to.
	 * @param model
	 *        The model to resolve types from.
	 * @param qualifiedTypeNames
	 *        A collection of qualified type names to find in the given model. See
	 *        {@link #findType(TLModel, String)}.
	 * @return A set of all found types. If some types could not be found, an error has been
	 *         reported to the given {@link Protocol} for each name that could not be found.
	 */
	public static Set<TLType> findTypes(Log protocol, TLModel model, Collection<String> qualifiedTypeNames) {
		Set<TLType> types = new HashSet<>();
		for (String name : qualifiedTypeNames) {
			TLType type;
			try {
				type = findType(model, name);
			} catch (TopLogicException ex) {
				protocol.error(ex.getMessage());
				continue;
			}
			types.add(type);
		}
		return types;
	}

	/**
	 * Lookup the {@link TLModule} with the given name in the global application model.
	 *
	 * @param moduleName
	 *        Name of the {@link TLModule} to search.
	 * @return The {@link TLModule} with the given name.
	 * @throws TopLogicException
	 *         If no such {@link TLModule} exists.
	 */
	public static TLModule findModule(String moduleName) throws TopLogicException {
		return findModule(model(), moduleName);
	}

	/**
	 * Lookup the {@link TLModule} with the given name in the given {@link TLModel}.
	 *
	 * @param model
	 *        The {@link TLModel} to inspect.
	 * @param moduleName
	 *        Name of the {@link TLModule} to search.
	 * @return The {@link TLModule} with the given name.
	 * @throws TopLogicException
	 *         If no such {@link TLModule} exists.
	 */
	public static TLModule findModule(TLModel model, String moduleName) throws TopLogicException {
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_MODULE__NAME.fill(moduleName));
		}
		return module;
	}

	/**
	 * Lookup the default singleton in the {@link TLModule} with the given name.
	 *
	 * @param moduleName
	 *        Name of the {@link TLModule} to search.
	 * @return The singleton of the given {@link TLModule}.
	 * @throws TopLogicException
	 *         If no such singleton exists.
	 */
	public static TLObject findSingleton(String moduleName) throws TopLogicException {
		return findSingleton(moduleName, TLModule.DEFAULT_SINGLETON_NAME);
	}

	/**
	 * Lookup the singleton with the given name in the {@link TLModule} with the given name.
	 *
	 * @param moduleName
	 *        Name of the {@link TLModule} to search.
	 * @param singletonName
	 *        The name of the singleton {@link TLObject} to resolve.
	 * @return The singleton of the given {@link TLModule}.
	 * @throws TopLogicException
	 *         If no such singleton exists.
	 */
	public static TLObject findSingleton(String moduleName, String singletonName) throws TopLogicException {
		return findSingleton(findModule(moduleName), singletonName);
	}

	/**
	 * Lookup the default singleton of the given {@link TLModule}.
	 *
	 * @param module
	 *        The {@link TLModule} to search.
	 * @return The singleton of the given {@link TLModule}.
	 * @throws TopLogicException
	 *         If no such singleton exists.
	 */
	public static TLObject findSingleton(TLModule module) throws TopLogicException {
		return findSingleton(module, TLModule.DEFAULT_SINGLETON_NAME);
	}

	/**
	 * Lookup the singleton with the given name in the given {@link TLModule}.
	 *
	 * @param module
	 *        The {@link TLModule} to search.
	 * @param singletonName
	 *        The name of the singleton {@link TLObject} to resolve.
	 * @return The singleton of the given {@link TLModule}.
	 * @throws TopLogicException
	 *         If no such singleton exists.
	 */
	public static TLObject findSingleton(TLModule module, String singletonName) throws TopLogicException {
		TLObject singleton = module.getSingleton(singletonName);
		if (singleton == null) {
			throw new TopLogicException(
				I18NConstants.ERROR_NO_SUCH_SINGLETON__MODULE_NAME.fill(module.getName(), singletonName));
		}
		return singleton;
	}

	/**
	 * Resolve a collection of qualified part names against a given model.
	 * 
	 * @param protocol
	 *        The {@link Protocol} to report errors to.
	 * @param model
	 *        The model to resolve type parts from.
	 * @param qualifiedPartNames
	 *        A collection of qualified part names to find in the given model.
	 * @return A set of all found type parts. If some parts could not be found, an error has been
	 *         reported to the given {@link Protocol} for each name that could not be found.
	 * 
	 * @see #findPart(TLModel, String)
	 */
	public static Set<TLTypePart> findParts(Protocol protocol, TLModel model, Collection<String> qualifiedPartNames) {
		Set<TLTypePart> parts = new HashSet<>();
		for (String name : qualifiedPartNames) {
			TLTypePart type;
			try {
				type = findPart(model, name);
			} catch (TopLogicException ex) {
				protocol.error(ex.getMessage());
				continue;
			}
			parts.add(type);
		}
		return parts;
	}

	/**
	 * Resolve the given name against the given module.
	 * 
	 * <p>
	 * If the type is qualified, the methods act likes {@link #findPart(TLModel, String)}.
	 * </p>
	 * 
	 * <p>
	 * If not the part is resolved in the given module.
	 * </p>
	 * 
	 * @param module
	 *        The module to resolve type parts from.
	 * @param name
	 *        The name of the part to resolve.
	 * 
	 * @throws TopLogicException
	 *         if the name is not a qualified name or the name does not denote an existing part.
	 */
	public static <T extends TLTypePart> T findPart(TLModule module, String name) throws TopLogicException {
		return findPart(module.getModel(), module, name);
	}

	/**
	 * Resolve the given qualified name against the application model.
	 * 
	 * @param name
	 *        The qualified name of the part to resolve.
	 * 
	 * @throws TopLogicException
	 *         if the name is not a qualified name or the name does not denote an existing part.
	 */
	public static <T extends TLTypePart> T findPart(String name) throws TopLogicException {
		return findPart(model(), name);
	}

	/**
	 * Resolve the given qualified name against the given model.
	 * 
	 * @param model
	 *        The model to resolve type parts from.
	 * @param name
	 *        The qualified name of the part to resolve.
	 * 
	 * @throws TopLogicException
	 *         if the name is not a qualified name or the name does not denote an existing part.
	 */
	public static <T extends TLTypePart> T findPart(TLModel model, String name) throws TopLogicException {
		return findPart(model, null, name);
	}

	private static <T extends TLTypePart> T findPart(TLModel model, TLModule module, String name)
			throws TopLogicException {
		int partSeparatorIndex = name.lastIndexOf(QUALIFIED_NAME_PART_SEPARATOR);
		if (partSeparatorIndex < 0) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_QUALIFIED_NAME__NAME.fill(name));
		}
		String typeName = name.substring(0, partSeparatorIndex);
		String partName = name.substring(partSeparatorIndex + 1);
		
		TLType type = findType(model, module, typeName);
		
		return resolvePart(type, partName);
	}

	/**
	 * Resolves a {@link TLTypePart} from a type in a module of a given model.
	 */
	public static <T extends TLTypePart> T findPart(TLModel model, String moduleName, String typeName,
			String partName) throws TopLogicException {
		TLScope module = resolveScopeOrModule(model, moduleName);
		TLType type = module.getType(typeName);
		if (type == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_TYPE__MODULE_NAME.fill(moduleName, typeName));
		}
		return resolvePart(type, partName);
	}

	/**
	 * Resolves the given local part name in the context of the given {@link TLType}.
	 *
	 * @param type
	 *        The context type.
	 * @param partName
	 *        The name of the part to resolve.
	 * @return The resolved part
	 * @throws TopLogicException
	 *         If no corresponding part is found.
	 */
	public static <T extends TLTypePart> T resolvePart(TLType type, String partName) throws TopLogicException {
		if (!(type instanceof TLStructuredType)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_STRUCTURED_TYPE__TYPE.fill(qualifiedName(type)));
		}
		@SuppressWarnings("unchecked")
		T part = (T) ((TLStructuredType) type).getPart(partName);
		if (part != null) {
			return part;
		}
		if (type instanceof TLEnumeration) {
			@SuppressWarnings("unchecked")
			T classifier = (T) ((TLEnumeration) type).getClassifier(partName);
			if (classifier != null) {
				return classifier;
			}
		}
		throw new TopLogicException(
			I18NConstants.ERROR_NO_SUCH_PART_IN_TYPE__TYPE_NAME.fill(qualifiedName(type), partName));
	}

	/**
	 * Resolves the type with the given name in the {@link TLModule} with the given name.
	 */
	public static TLType findType(String moduleName, String typeName) {
		TLModel model = model();
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			return null;
		}
		return module.getType(typeName);
	}

	/**
	 * Computes the set of all super classes of the given class and their super classes.
	 * 
	 * @param clazz
	 *        The class to find generalizations of.
	 * @return Set of all generalizations of the given class.
	 */
	public static LinkedHashSet<TLClass> getTransitiveGeneralizations(TLClass clazz) {
		return addTransitiveGeneralizations(new LinkedHashSet<>(), clazz);
	}

	/**
	 * Computes the set of the given class, all super classes of the given class, and their super
	 * classes.
	 * <p>
	 * The classes are sorted depth-first. The result contains no duplicates: Only the first
	 * occurrence in the type hierarchy is returned.
	 * </p>
	 * 
	 * @param clazz
	 *        The class to find generalizations of.
	 * @return A {@link Set} of all generalizations of the given class including the class itself.
	 *         It is unmodifiable.
	 */
	public static Set<TLClass> getReflexiveTransitiveGeneralizations(TLClass clazz) {
		return TLModelCacheService.getOperations().getSuperClasses(clazz);
	}

	/**
	 * Computes the set of the given classes, all super classes of the given classes, and their
	 * super classes.
	 * 
	 * @param classes
	 *        The classes to find generalizations of.
	 * @return Set of all generalizations of the given class including the class itself.
	 */
	public static LinkedHashSet<TLClass> getReflexiveTransitiveGeneralizations(Iterable<? extends TLClass> classes) {
		LinkedHashSet<TLClass> result = new LinkedHashSet<>();
		addReflexiveTransitiveGeneralizations(result, classes);
		return result;
	}

	/**
	 * Adds all transitive generalizations of the given class to the given collection in DFS
	 * pre-order.
	 * 
	 * @param <C>
	 *        The concrete collection type.
	 * @param result
	 *        The collection to fill.
	 * @param clazz
	 *        The class to find transitive generalizations of.
	 * @return The given collection for call chaining.
	 */
	public static <C extends Collection<TLClass>> C addTransitiveGeneralizations(C result, TLClass clazz) {
		addReflexiveTransitiveGeneralizations(result, clazz.getGeneralizations());
		return result;
	}

	/**
	 * Find all {@link TLClassPart}s of the given {@link TLClass} including inherited ones.
	 * <p>
	 * Use {@link TLClass#getAllParts()} instead, as that is cached.
	 * </p>
	 * 
	 * @param tlClass
	 *        The {@link TLClass} to find {@link TLClassPart}s of.
	 * @return The list of all {@link TLClassPart}s ordered by their {@link TLSortOrder}.
	 */
	public static List<TLClassPart> calcAllPartsUncached(TLClass tlClass) {
		/* The *Linked*HashMap causes attributes without sort-order to be sorted by their owner
		 * class: Attributes from a type X all come before those defined in super-types. */
		LinkedHashMap<String, TLClassPart> parts = linkedMap();
		for (TLClass superClass : TLModelUtil.getReflexiveTransitiveGeneralizations(tlClass)) {
			for (TLClassPart part : superClass.getLocalClassParts()) {
				String partName = part.getName();
				// Filter out overridden parts:
				if (!parts.containsKey(partName)) {
					parts.put(partName, part);
				}
			}
		}
		List<TLClassPart> result = list(parts.values());
		Collections.sort(result, TLStructuredTypePartComparator.INSTANCE);
		return result;
	}

	/**
	 * Find all properties of the given class including inherited properties.
	 * 
	 * @param clazz
	 *        The class to find properties of.
	 * @return The list of all {@link TLProperty}s ordered by their {@link TLSortOrder}.
	 */
	public static List<TLProperty> getAllProperties(TLClass clazz) {
		return FilterUtil.filterList(TLProperty.class, clazz.getAllClassParts());
	}

	/**
	 * Adds all reflexive transitive generalizations of the given classes to the given collection.
	 * 
	 * @param result
	 *        The collection to fill.
	 * @param classes
	 *        The class to find reflexive transitive generalizations of.
	 */
	public static void addReflexiveTransitiveGeneralizations(Collection<TLClass> result, Iterable<? extends TLClass> classes) {
		for (TLClass generalization : classes) {
			result.addAll(getReflexiveTransitiveGeneralizations(generalization));
		}
	}

	/**
	 * Find the other end of a two-ended {@link TLAssociation}.
	 * 
	 * @param end
	 *        The one end of a two-ended association.
	 * @return The opposite end.
	 */
	public static final TLAssociationEnd getOtherEnd(TLAssociationEnd end) {
		List<TLAssociationEnd> allEnds = CollectionUtil.toListIterable(TLModelUtil.getEnds(end.getOwner()));
		int endIndex = allEnds.indexOf(end);
		TLAssociationEnd otherEnd = allEnds.get(1 - endIndex);
		return otherEnd;
	}

	/**
	 * Find the index of an {@link TLAssociationEnd}.
	 * 
	 * @param end
	 *        The end to find its index within all ends of its {@link TLAssociation}.
	 * 
	 * @return The index in {@link TLModelUtil#getEnds(TLAssociation)}.
	 */
	public static int getEndIndex(TLAssociationEnd end) {
		return getEnds(end.getOwner()).indexOf(end);
	}

	/**
	 * Compute the minimal set of common generalizations of the given types.
	 * 
	 * @param types
	 *        The types of which the common generalizations are computed, must not be empty.
	 * 
	 * @return A set of types, where each member is a common super type of all given types. The
	 *         returned set is minimal in the sense that no member of the returned set is a super
	 *         type of any other member in the result. In a type hierarchy with single inheritance,
	 *         the result is either empty or has exactly one element. In such hierarchy with a
	 *         common super type of all types, the result has always exactly one element.
	 */
	public static Set<TLClass> getCommonGeneralizations(Iterable<? extends TLClass> types) {
		Iterator<? extends TLClass> it = types.iterator();
		TLClass first = it.next();

		Set<TLClass> firstReflexiveGeneralizations = getReflexiveTransitiveGeneralizations(first);

		Set<TLClass> commonGeneralizations = set(firstReflexiveGeneralizations);
		while (it.hasNext()) {
			TLClass next = it.next();
			Set<TLClass> reflexiveGeneralizations = getReflexiveTransitiveGeneralizations(next);

			commonGeneralizations.retainAll(reflexiveGeneralizations);
		}

		// Find the best generalization(s).
		for (TLClass commonGeneralization : new ArrayList<>(commonGeneralizations)) {
			commonGeneralizations =
				CollectionUtil.difference(commonGeneralizations, getTransitiveGeneralizations(commonGeneralization));
		}

		return commonGeneralizations;
	}

	/**
	 * Finds all classes that are concrete sub-classes of the given class and all super types
	 * thereof.
	 * 
	 * @param modelIndex
	 *        The {@link TLModel} for the model.
	 * @param baseClass
	 *        The base class.
	 * @return All concrete types that are assignment compatible to the given type and their super
	 *         classes.
	 */
	public static Set<TLClass> getGeneralizationsOfConcreteSpecializations(TLModel modelIndex, TLClass baseClass) {
		return getGeneralizationsOfConcreteSpecializations(modelIndex, Collections.singletonList(baseClass));
	}

	/**
	 * Finds all classes that are concrete sub-classes of the given classes and all super types
	 * thereof.
	 * 
	 * @param modelIndex
	 *        The {@link TLModel} for the model.
	 * @param baseClasses
	 *        The base class.
	 * @return All concrete types that are assignment compatible to the given type and their super
	 *         classes.
	 */
	public static Set<TLClass> getGeneralizationsOfConcreteSpecializations(TLModel modelIndex,
			Iterable<? extends TLClass> baseClasses) {
		Set<TLClass> result = getConcreteSubclasses(baseClasses);

		// Add all generalizations of concrete types found so far.
		for (TLClass specialization : new ArrayList<>(result)) {
			addTransitiveGeneralizations(result, specialization);
		}

		return result;
	}

	/**
	 * All concrete classes from the reflexive transitive specializations of the given class.
	 */
	public static Set<TLClass> getConcreteSpecializations(TLClass baseClass) {
		return FilterUtil.filterSet(IS_CONCRETE, getReflexiveTransitiveSpecializations(baseClass));
	}

	/**
	 * All non-abstract classes that are assignment compatible to the given base type.
	 * @param baseClasses
	 *        The base classes.
	 * 
	 * @return All types of assignment instances that are assignment compatible to the given class.
	 */
	public static Set<TLClass> getConcreteSubclasses(Iterable<? extends TLClass> baseClasses) {
		return TLModelUtil.getReflexiveTransitiveSpecializations(TLModelUtil.IS_CONCRETE, baseClasses);
	}

	/**
	 * The direct subclasses in a stable order.
	 * <p>
	 * Subclasses have no inherent order. Therefore, they are sorted by their name.
	 * </p>
	 * <p>
	 * Getting the subclasses in a stable order is necessary when something is derived from them
	 * which needs a stable order and there is no better order in that situation.
	 * </p>
	 * 
	 * @return A new, mutable and resizable {@link List}.
	 */
	public static List<TLClass> getSpecializationsOrdered(TLClass baseClass) {
		List<TLClass> result = list(baseClass.getSpecializations());
		result.sort(comparing(TLClass::getName));
		return result;
	}

	/**
	 * The {@link TLReference} of the {@link TLReference#getType()} that implements the inverse of
	 * the reference.
	 * 
	 * @return The inverse {@link TLReference}, or <code>null</code>, if there is no inverse.
	 */
	public static TLReference getForeignName(TLReference tlReference) {
		TLAssociationEnd myEnd = tlReference.getEnd();
		TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(myEnd);
		return otherEnd.getReference();
	}

	/**
	 * List of all reference attributes of given class including those defined by super classes.
	 * 
	 * @return The list of all {@link TLReference}s ordered by their {@link TLSortOrder}.
	 */
	public static List<TLReference> getAllReferences(TLClass tlClass) {
		return FilterUtil.filterList(TLReference.class, tlClass.getAllClassParts());
	}

	/**
	 * Enumeration of primitive attributes declared locally in the {@link TLStructuredType}.
	 * 
	 * <p>
	 * Changing {@link TLStructuredType#getLocalParts()} during iterating through the enumeration,
	 * may cause {@link ConcurrentModificationException}.
	 * </p>
	 * 
	 * @return The list of all {@link TLProperty}s ordered by their {@link TLSortOrder}.
	 */
	public static List<TLProperty> getLocalProperties(TLStructuredType tlStructuredType) {
		List<TLProperty> result = FilterUtil.filterList(TLProperty.class, tlStructuredType.getLocalParts());
		Collections.sort(result, TLStructuredTypePartComparator.INSTANCE);
		return result;
	}

	/**
	 * Enumeration of reference attributes declared locally in the class.
	 * 
	 * <p>
	 * Changing {@link TLStructuredType#getLocalParts()} during iterating through the enumeration,
	 * may cause {@link ConcurrentModificationException}.
	 * </p>
	 * 
	 * @see TLModelUtil#getAllReferences(TLClass)
	 * 
	 * @return The list of all {@link TLProperty}s ordered by their {@link TLSortOrder}.
	 */
	public static List<TLReference> getLocalReferences(TLClass tlClass) {
		List<TLReference> result = FilterUtil.filterList(TLReference.class, tlClass.getLocalParts());
		Collections.sort(result, TLStructuredTypePartComparator.INSTANCE);
		return result;
	}

	/**
	 * A view of {@link TLClass#getLocalParts()} that returns a {@link Collection} of
	 * {@link TLClassPart}s.
	 * <p>
	 * This is just a view, not a copy.
	 * </p>
	 */
	public static Collection<TLClassPart> getLocalParts(TLClass owner) {
		return CollectionUtil.dynamicCastView(TLClassPart.class, owner.getLocalParts());
	}

	/**
	 * Enumeration of key association ends.
	 * 
	 * <p>
	 * An association must have at least two ends, where the first one is a {@link TLAssociationEnd}
	 * .
	 * </p>
	 * 
	 * <p>
	 * Changing {@link TLStructuredType#getLocalParts()} during iterating through the enumeration,
	 * may cause {@link ConcurrentModificationException}.
	 * </p>
	 */
	public static List<TLAssociationEnd> getEnds(TLAssociation tlAssociation) {
		return FilterUtil.filterList(TLAssociationEnd.class, tlAssociation.getLocalParts());
	}

	/**
	 * Creates a modifiable list of the ends of the given {@link TLAssociation}.
	 * 
	 * <p>
	 * Changing the returned list does not reflect to the {@link TLAssociation}.
	 * </p>
	 */
	public static List<TLAssociationEnd> getCopiedEnds(TLAssociation tlAssociation) {
		return CollectionUtil.toListIterable(getEnds(tlAssociation));
	}

	/**
	 * The {@link TLClassifier} from {@link TLEnumeration#getClassifiers()} that serves as default
	 * value of this {@link TLEnumeration}.
	 * 
	 * @return The default {@link TLClassifier}, or <code>null</code>, if this {@link TLEnumeration}
	 *         has no default classifier.
	 */
	public static TLClassifier getDefaultClassifier(TLEnumeration tlEnumeration) {
		for (TLClassifier classifier : tlEnumeration.getClassifiers()) {
			if (classifier.isDefault()) {
				return classifier;
			}
		}
		return null;
	}

	private static TLType lookupType(TLModel model, String moduleName, String scopeName, String typeName)
			throws TopLogicException {
		TLScope scope;
		if (scopeName != null) {
			scope = resolveScope(model, scopeName);
			if (scope == null) {
				scope = resolveSingletonScope(model, moduleName, scopeName);
			}
		} else {
			scope = resolveScopeOrModule(model, moduleName);
		}
		TLType type = scope.getType(typeName);
		if (type == null) {
			if (scopeName == null) {
				throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_TYPE__MODULE_NAME.fill(moduleName, typeName));
			} else {
				throw new TopLogicException(
					I18NConstants.ERROR_NO_SUCH_TYPE__SCOPE_MODULE_NAME.fill(scopeName, moduleName, typeName));
			}
		}
		return type;
	}

	private static TLScope resolveScopeOrModule(TLModel model, String moduleName) throws TopLogicException {
		TLScope scope = resolveScope(model, moduleName);
		if (scope == null) {
			scope = findModule(model, moduleName);
		}
		return scope;
	}

	private static TLScope resolveScope(TLModel model, String scopeName)
			throws TopLogicException {
		int tableIndex = scopeName.indexOf(SCOPE_ID_PART_SEPARATOR);
		if (tableIndex < 0) {
			return null;
		} else {
			String tableName = scopeName.substring(0, tableIndex);
			int branchIndex = scopeName.indexOf(SCOPE_ID_PART_SEPARATOR, tableIndex + 1);
			if (branchIndex < 0) {
				throw new TopLogicException(I18NConstants.ERROR_INVALID_SCOPE_REFERENCE__VALUE.fill(scopeName));
			}
			long branch = Long.parseLong(scopeName.substring(tableIndex + 1, branchIndex));
			String externalId = scopeName.substring(branchIndex + 1, scopeName.length());
			TLID id = IdentifierUtil.fromExternalForm(externalId);
			KnowledgeBase kb = getKnowledgeBase(model);
			MetaObject table;
			try {
				table = kb.getMORepository().getMetaObject(tableName);
			} catch (UnknownTypeException ex) {
				throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_TABLE__SCOPE_TABLE.fill(scopeName, tableName));
			}
			DefaultObjectKey scopeId = new DefaultObjectKey(branch, Revision.CURRENT_REV, table, id);
			KnowledgeItem scopeHandle = kb.resolveObjectKey(scopeId);
			if (scopeHandle == null) {
				throw new TopLogicException(
					I18NConstants.ERROR_NO_SUCH_SCOPE_OBJECT__SCOPE_ID.fill(scopeName, scopeId));
			}
			TLObject scope = scopeHandle.getWrapper();
			if (!(scope instanceof TLScope)) {
				throw new TopLogicException(I18NConstants.ERROR_NOT_A_SCOPE__SCOPE_OBJ.fill(scopeName, scope));
			}
			return (TLScope) scope;
		}
	}

	private static TLScope resolveSingletonScope(TLModel model, String moduleName, String name)
			throws TopLogicException {
		TLModule module = findModule(model, moduleName);
		TLObject singleton = findSingleton(module, name);
		if (!(singleton instanceof TLScope)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_TYPE_SCOPE__MODULE_SINGLETON.fill(moduleName, name));
		}
		return (TLScope) singleton;
	}

	private static TLModel model() {
		return ModelService.getApplicationModel();
	}

	/**
	 * The {@link TLNamedPart#getName() local names} of the given {@link TLModelPart}s.
	 * 
	 * @param parts
	 *        Is allowed to be null. Is not allowed to contain null.
	 * @return Never null. A mutable, resizable {@link List}.
	 */
	public static List<String> localNames(Iterable<? extends TLNamedPart> parts) {
		List<String> names = list();
		for (TLNamedPart part : CollectionUtil.nonNull(parts)) {
			names.add(part.getName());
		}
		return names;
	}

	/**
	 * The {@link #qualifiedName(TLModelPart) qualified names} of the given {@link TLModelPart}s.
	 * <p>
	 * This method requires {@link TLNamedPart}s instead of {@link TLModelPart}s like
	 * {@link #qualifiedName(TLModelPart)} to guarantee that the result does not contain null
	 * entries.
	 * </p>
	 * 
	 * @param parts
	 *        Is allowed to be null. Is not allowed to contain null.
	 * @return Never null. A mutable, resizable {@link List}.
	 */
	public static List<String> qualifiedNames(Iterable<? extends TLNamedPart> parts) {
		List<String> names = list();
		for (TLNamedPart part : CollectionUtil.nonNull(parts)) {
			names.add(qualifiedName(part));
		}
		return names;
	}

	/**
	 * The fully qualified name of a {@link TLType} type.
	 * 
	 * @return Null, if there is no name. (The {@link TLModel} itself has no name.)
	 */
	public static String qualifiedName(TLModelPart type) {
		StringBuilder buffer = new StringBuilder();
		type.visit(QualifiedNameVisitor.INSTANCE, buffer);
		return buffer.toString();
	}

	/**
	 * Fully qualified name of the {@link TLType} with the given typeName in the module with the
	 * given module name defined within the given global scope.
	 */
	public static String qualifiedName(String moduleName, String scopeName, String typeName) {
		StringBuilder buffer = new StringBuilder();
		appendQualifiedName(buffer, moduleName, scopeName, typeName);
		return buffer.toString();
	}

	static void appendQualifiedName(StringBuilder out, String moduleName, String scopeName, String typeName) {
		out.append(moduleName);
		out.append(QUALIFIED_NAME_SEPARATOR);
		out.append(scopeName);
		out.append(QUALIFIED_NAME_SEPARATOR);
		out.append(typeName);
	}

	/**
	 * Fully qualified name of the globally defined {@link TLType} with the given typeName in the
	 * module with the given module name.
	 */
	public static String qualifiedName(String moduleName, String typeName) {
		StringBuilder buffer = new StringBuilder();
		appendQualifiedName(buffer, moduleName, typeName);
		return buffer.toString();
	}

	static void appendQualifiedName(StringBuilder out, String moduleName, String typeName) {
		out.append(moduleName);
		out.append(QUALIFIED_NAME_SEPARATOR);
		out.append(typeName);
	}

	/**
	 * Variant of a {@link TLClass}es qualified name using solely '.' characters as separators.
	 * 
	 * <p>
	 * The name is only configuration-unique. Dynamically allocated local types use the same
	 * qualified name.
	 * </p>
	 */
	public static String qualifiedNameDotted(TLType type) {
		TLModule module = type.getModule();
		if (module == null) {
			return type.getName();
		}
		return qualifiedNameDotted(module.getName(), type.getName());
	}

	/**
	 * @see #qualifiedNameDotted(TLType)
	 */
	public static String qualifiedNameDotted(String structureName, String elementName) {
		return structureName + '.' + elementName;
	}

	/**
	 * Resolves the {@link TLModelPart} with the given qualified name.
	 *
	 * @see #resolveQualifiedName(String)
	 */
	public static TLModelPart resolveModelPart(String qualifiedName) throws TopLogicException {
		TLObject object = TLModelUtil.resolveQualifiedName(qualifiedName);

		if (object instanceof TLModelPart) {
			return (TLModelPart) object;
		}

		throw new ConfigurationError(I18NConstants.ERROR_INVALID_PART_REFERENCE__VALUE.fill(qualifiedName));
	}

	/**
	 * Resolves the {@link TLModelPart} or singleton {@link TLObject} with given qualified name
	 * within the {@link ModelService#getApplicationModel() application model}.
	 * 
	 * @see #resolveQualifiedName(TLModel, String)
	 */
	public static TLObject resolveQualifiedName(String qualifiedName) throws TopLogicException {
		return resolveQualifiedName(model(), qualifiedName);
	}

	/**
	 * Resolves the {@link TLModelPart} or singleton {@link TLObject} with given qualified name
	 * within the given {@link TLModel}.
	 * 
	 * @param model
	 *        The {@link TLModel} to resolve the qualified name in.
	 * @param qualifiedName
	 *        The qualified name of the {@link TLModelPart} to resolve.
	 * 
	 * @return The {@link TLModelPart} of the given {@link TLModel} with the given qualified name.
	 * 
	 * @throws TopLogicException
	 *         iff the qualified name has invalid format, or can not be resolved to a valid
	 *         {@link TLModelPart} within the given model.
	 */
	public static TLObject resolveQualifiedName(TLModel model, String qualifiedName)
			throws TopLogicException {
		int partSeparatorIndex = qualifiedName.lastIndexOf(QUALIFIED_NAME_PART_SEPARATOR);
		if (partSeparatorIndex < 0) {
			return resolveModuleOrType(model, qualifiedName);
		}
		String scopeName = qualifiedName.substring(0, partSeparatorIndex);
		String partName = qualifiedName.substring(partSeparatorIndex + 1);

		int moduleSep = scopeName.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (moduleSep >= 0) {
			TLType type = TLModelUtil.findType(model, scopeName);
			return resolvePart(type, partName);
		} else {
			TLModule module = findModule(model, scopeName);
			return findSingleton(module, partName);
		}
	}

	private static TLModelPart resolveModuleOrType(TLModel model, String qualifiedName) throws TopLogicException {
		int moduleSep = qualifiedName.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (moduleSep >= 0) {
			return TLModelUtil.findType(model, qualifiedName);
		} else {
			TLModule module = model.getModule(qualifiedName);
			if (module == null) {
				throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_MODULE__NAME.fill(qualifiedName));
			}
			return module;
		}
	}

	/**
	 * Returns the {@link TLObject#TL_OBJECT_TYPE} type which is compatible to all {@link TLClass}es
	 * in the given {@link TLModel}.
	 */
	public static TLClass tlObjectType(TLModel model) {
		TLModule module = model.getModule(TlModelFactory.TL_MODEL_STRUCTURE);
		if (module == null) {
			return null;
		}
		TLType tlObjectType = module.getType(TLObject.TL_OBJECT_TYPE);
		assert tlObjectType instanceof TLClass : "There should be a type for all TLObject's with name "
			+ TLObject.TL_OBJECT_TYPE;
		return (TLClass) tlObjectType;
	}

	/**
	 * Whether the given instance can be assigned to a {@link TLStructuredTypePart} of the given
	 * {@link TLType}.
	 */
	public static boolean isCompatibleInstance(TLType type, Object instance) {
		return isCompatibleInstance(type, instance, false);
	}

	/**
	 * Whether the given instance can be assigned to a {@link TLStructuredTypePart} of the given
	 * {@link TLType}.
	 * 
	 * <p>
	 * When the given type and instance have different history contexts, it is checked whether the
	 * given type exists in the history context of the instance and whether the type of the instance
	 * is assignment compatible to the type in that history context.
	 * </p>
	 * 
	 * @param ignoreMissingValueType
	 *        When <code>true</code>, activate a special hack that treats objects without type as
	 *        assignment compatible to all object types. This is necessary when a new
	 *        StructuredElement is created, to break through a cyclic dependency that occurs when
	 *        creating instances of local types (for example "DemoTypes:C"). Without this hack,
	 *        initializing the <code>parent</code> reference of a new node would require the type of
	 *        the node being created to be set, but resolving this type (a local type defined within
	 *        its parent) requires the scope of the new node, and resolving this scope requires the
	 *        parent to be initialized, which completes the cycle.
	 */
	public static boolean isCompatibleInstance(TLType type, Object instance, boolean ignoreMissingValueType) {
		switch (type.getModelKind()) {
			case ENUMERATION:
				return checkCompatibleEnum((TLEnumeration) type, instance);
			case CLASS:
			case ASSOCIATION:
				return checkCompatibleClass(type, instance, ignoreMissingValueType);
			case DATATYPE:
				return ((TLPrimitive) type).getStorageMapping().isCompatible(instance);
			default:
				throw new UnreachableAssertion("Not a type: " + type);
		}
	}

	private static boolean checkCompatibleClass(TLType type, Object value,
			boolean ignoreMissingValueType) {
		if (!(value instanceof TLObject)) {
			return false;
		}
		TLObject instance = (TLObject) value;
		TLType valueType = instance.tType();
		if (valueType == null) {
			return ignoreMissingValueType;
		}
		return isCompatibleType(type, valueType);
	}

	private static boolean checkCompatibleEnum(TLEnumeration enumType, Object instance) {
		if (!isClassifier(instance)) {
			return false;
		}

		TLClassifier classifier = (TLClassifier) instance;
		return isCompatibleType(enumType, classifier.getOwner());
	}

	private static boolean isClassifier(Object instance) {
		return instance instanceof TLClassifier;
	}

	/**
	 * Checks whether the <code>actualType</code> is compatible to the <code>expectedType</code>.
	 * 
	 * <p>
	 * <ol>
	 * <li>If the <code>expectedType</code> is a {@link TLClass} it checks whether the
	 * <code>actualType</code> is either the same or a subtype (hereditarily).</li>
	 * <li>If the <code>expectedType</code> is a {@link TLAssociation} it checks whether the
	 * <code>actualType</code> is either the same or (if the association is a union) one of its part
	 * (hereditarily).</li>
	 * <li>If the <code>expectedType</code> is a {@link TLPrimitive} or a {@link TLEnumeration} it
	 * checks whether the <code>actualType</code> is the same.</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>
	 * If <code>expectedType</code> and <code>actualType</code> are present in different history
	 * contexts, it is checked whether the <code>expectedType</code>, when transfered to the
	 * revision of the <code>actualType</code>, is compatible with <code>actualType</code> in the
	 * above sense.
	 * </p>
	 */
	public static boolean isCompatibleType(TLType expectedType, TLType actualType) {
		if (actualType == expectedType) {
			return true;
		}
		long expectedHC = expectedType.tHistoryContext();
		long actualHC = actualType.tHistoryContext();
		if (expectedHC == actualHC) {
			// Same history type: check for equality
			return actualType.visitType(COMPATIBILITY_VISITOR, expectedType).booleanValue();
		} else {
			/* Revision of the `expectedType` does not match the revision of the `actualType`. The
			 * method is designed to check whether the `actualType` is assignment compatible to the
			 * `expectedType`. Therefore the `actualType` is the "main" type: transfer the
			 * `expectedType` in the history context of the `actualType` and check whether they are
			 * compatible in that history context. */

			KnowledgeItem expectedKI = expectedType.tHandle();
			HistoryManager hm = HistoryUtils.getHistoryManager(expectedKI);
			KnowledgeItem expectedTypeInContext = hm.getKnowledgeItem(hm.getRevision(actualHC), expectedKI);
			if (expectedTypeInContext == null) {
				/* `expectedType` does not exist in the revision of the `actualType`. */
				return false;
			}
			return actualType.visitType(COMPATIBILITY_VISITOR, expectedTypeInContext.getWrapper()).booleanValue();
		}

	}

	/**
	 * Resolves a given type description in the application model.
	 * 
	 * <p>
	 * The type description is either {@link #qualifiedName(TLModelPart) qualified} or:
	 * </p>
	 * 
	 * <ul>
	 * <li><code>me:</code> followed by the type name that defined the type, e.g.
	 * <code>me:projElement.All</code></li>
	 * 
	 * <li><code>node:</code> followed by name of the structure followed by <code>:</code> followed
	 * by the name of the node type, e.g. <code>node:projElement:Project</code></li>
	 * 
	 * <li><code>enum:</code> followed by name of the fast list defining the enum type, e.g.
	 * <code>enum:tl.yesno</code></li>
	 * </ul>
	 * 
	 * @param typeDescription
	 *        See above.
	 * 
	 * @return The resolved type.
	 * 
	 * @throws TopLogicException
	 *         iff the typeDescription could not be parsed or the required type could not be found.
	 */
	public static TLType findType(String typeDescription) throws TopLogicException {
		return findType(model(), typeDescription);
	}

	/**
	 * Resolves the given type description in the given {@link TLModule}.
	 * 
	 * <p>
	 * If the type description is full qualified, the methods acts like
	 * {@link #findType(TLModel, String)} with {@link TLModule#getModel() model} of the module.
	 * </p>
	 * 
	 * <p>
	 * If is not qualified, the corresponding type in the module is returned.
	 * </p>
	 * 
	 * @param module
	 *        Holder of the desired type.
	 * @param typeDescription
	 *        Description of the desired type.
	 * @return The {@link TLType} described by the given type description. Not <code>null</code>.
	 * @throws TopLogicException
	 *         Iff the type description has wrong format, or there is no such type.
	 */
	public static TLType findType(TLModule module, String typeDescription) throws TopLogicException {
		return findType(module.getModel(), module, typeDescription);
	}

	private static TLType findType(TLModel model, TLScope scope, String typeDescription)
			throws TopLogicException {
		int moduleSep = typeDescription.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (moduleSep >= 0) {
			return TLModelUtil.findType(model, typeDescription);
		}
		if (scope == null) {
			throw new TopLogicException(I18NConstants.ERROR_UNQUALIFIED_TYPE_NAME_WITHOUT_SCOPE__NAME.fill(typeDescription));
		}
		TLType result = scope.getType(typeDescription);
		if (result == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_TYPE__MODULE_NAME.fill(scope, typeDescription));
		}
		return result;
	}

	/**
	 * Resolves a given type description in the given model.
	 * 
	 * @see TLModelUtil#findType(String)
	 */
	public static TLType findType(TLModel model, String typeDescription) throws TopLogicException {
		if (typeDescription == null || typeDescription.isBlank()) {
			throw new TopLogicException(I18NConstants.ERROR_TYPE_NAME_MUST_NOT_BE_EMPTY);
		}
		String[] parts = SPLIT_PATTERN.split(typeDescription);
		int length = parts.length;
		if (length < 2) {
			throw new TopLogicException(I18NConstants.ERROR_UNQUALIFIED_TYPE_NAME_WITHOUT_SCOPE__NAME.fill(typeDescription));
		}
		String structure = parts[0];
		if (TL5Types.CLASS_PROTOCOL.equals(structure)) {
			if (parts.length != 2) {
				throw new TopLogicException(
					I18NConstants.ERROR_INVALID_LEGACY_TYPE_FORMAT__EXPECTED_ACTUAL.fill(TL5Types.CLASS_PROTOCOL
							+ QUALIFIED_NAME_SEPARATOR + "<meName>",
						typeDescription));
			}
			return lookupGlobalType(model, parts[1]);
		}
		if (TL5Types.NODE_PROTOCOL.equals(structure)) {
			if (parts.length != 3) {
				throw new TopLogicException(I18NConstants.ERROR_INVALID_LEGACY_TYPE_FORMAT__EXPECTED_ACTUAL.fill(TL5Types.NODE_PROTOCOL
						+ QUALIFIED_NAME_SEPARATOR + "<structureName>" + QUALIFIED_NAME_SEPARATOR + "<meName>",
					typeDescription));
			}
			return lookupType(model, parts[1], parts[2]);
		}
		if (TL5Types.ENUM_PROTOCOL.equals(structure)) {
			if (parts.length != 2) {
				throw new TopLogicException(
					I18NConstants.ERROR_INVALID_LEGACY_TYPE_FORMAT__EXPECTED_ACTUAL.fill(TL5Types.ENUM_PROTOCOL
							+ QUALIFIED_NAME_SEPARATOR + "<enumerationName>",
						typeDescription));
			}
			return lookupType(model, ModelLayout.TL5_ENUM_MODULE, parts[1]);
		}
		if (parts.length == 2) {
			return lookupType(model, structure, parts[1]);
		}
		if (parts.length == 3) {
			return lookupType(model, structure, parts[1], parts[2]);
		}
		throw new TopLogicException(I18NConstants.ERROR_INVALID_LEGACY_TYPE_FORMAT__EXPECTED_ACTUAL
			.fill("<moduleName>" + QUALIFIED_NAME_SEPARATOR + "<rootElement>"
					+ QUALIFIED_NAME_SEPARATOR + "<interface>",
				typeDescription));
	}

	private static TLType lookupType(TLModel model, String moduleName, String typeName) throws TopLogicException {
		return lookupType(model, moduleName, null, typeName);
	}

	private static TLType lookupGlobalType(TLModel model, String typeName) throws TopLogicException {
		for (TLModule module : model.getModules()) {
			TLType type = module.getType(typeName);
			if (type != null) {
				return type;
			}
			for (TLModuleSingleton link : model.getQuery(TLModuleSingletons.class).getAllSingletons()) {
				TLObject singleton = link.getSingleton();
				if (!(singleton instanceof TLScope)) {
					continue;
				}
				type = ((TLScope) singleton).getType(typeName);
				if (type != null) {
					return type;
				}
			}
		}
		throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_GLOBAL_TYPE__NAME.fill(typeName));
	}

	/**
	 * All concrete classes that are type-compatible with the given class.
	 */
	public static Set<TLClass> getConcreteReflexiveTransitiveSpecializations(TLClass classType) {
		return getReflexiveTransitiveSpecializations(IS_CONCRETE, classType);
	}

	/**
	 * All classes that are type-compatible with the given class and accepted by the given
	 * {@link Filter}.
	 */
	public static Set<TLClass> getReflexiveTransitiveSpecializations(Filter<? super TLClass> filter, TLClass baseClass) {
		return FilterUtil.filterSet(filter, getReflexiveTransitiveSpecializations(baseClass));
	}

	/**
	 * All true sub-classes of the given class that match the given {@link Filter}.
	 * 
	 * <p>
	 * The given class itself is not part of the result.
	 * </p>
	 */
	public static Set<TLClass> getTransitiveSpecializations(Filter<? super TLClass> filter, TLClass clazz) {
		return FilterUtil.filterInline(filter, getTransitiveSpecializations(clazz));
	}

	/**
	 * All true sub-classes of the given class.
	 * 
	 * <p>
	 * The given class itself is not part of the result.
	 * </p>
	 */
	public static Set<TLClass> getTransitiveSpecializations(TLClass classType) {
		HashSet<TLClass> result = new HashSet<>();
		addTransitiveSpecializations(result, classType);
		return result;
	}

	/**
	 * All classes that are type-compatible with at least one of the given classes and accepted by
	 * the given {@link Filter}.
	 */
	public static Set<TLClass> getReflexiveTransitiveSpecializations(Filter<? super TLClass> filter,
			Iterable<? extends TLClass> baseClasses) {
		return FilterUtil.filterInline(filter, getReflexiveTransitiveSpecializations(baseClasses));
	}

	/**
	 * All classes that are type-compatible with the given class.
	 * 
	 * <p>
	 * The result includes the given {@link TLClass} itself.
	 * </p>
	 * 
	 * <p>
	 * The result has no specific order.
	 * </p>
	 * 
	 * @return An unmodifiable {@link Set}.
	 */
	public static Set<TLClass> getReflexiveTransitiveSpecializations(TLClass classType) {
		return TLModelCacheService.getOperations().getSubClasses(classType);
	}

	/**
	 * All classes that are type-compatible with at least one of the given classes.
	 */
	public static Set<TLClass> getReflexiveTransitiveSpecializations(Iterable<? extends TLClass> classTypes) {
		HashSet<TLClass> result = new HashSet<>();
		addReflexiveTransitiveSpecializations(result, classTypes);
		return result;
	}

	/**
	 * Adds all classes that are type-compatible with the given class to the given result
	 * collection.
	 */
	public static void addReflexiveTransitiveSpecializations(Collection<TLClass> result, TLClass classType) {
		result.addAll(getReflexiveTransitiveSpecializations(classType));
	}

	/**
	 * Adds all true sub-classes of the given class to the given result collection.
	 * 
	 * <p>
	 * The given class itself is not added.
	 * </p>
	 */
	public static void addTransitiveSpecializations(Collection<TLClass> result, TLClass clazz) {
		addReflexiveTransitiveSpecializations(result, clazz.getSpecializations());
	}

	/**
	 * Adds all classes that are type-compatible with at least one of the given classes to the given
	 * collection.
	 */
	public static void addReflexiveTransitiveSpecializations(Collection<TLClass> result, Iterable<? extends TLClass> specializations) {
		for (TLClass specialization : specializations) {
			result.addAll(getReflexiveTransitiveSpecializations(specialization));
		}
	}

	/**
	 * All global {@link TLType}s in the given {@link TLModel}.
	 * 
	 * @param model
	 *        Is not allowed to be null.
	 * @return Never null. Always a new, mutable, extendible {@link List}.
	 */
	public static List<TLType> getAllGlobalTypes(TLModel model) {
		List<TLType> result = new ArrayList<>();
		for (TLModule module : model.getModules()) {
			result.addAll(module.getTypes());
		}
		for (TLModuleSingleton link : model.getQuery(TLModuleSingletons.class).getAllSingletons()) {
			TLObject singleton = link.getSingleton();
			if (singleton instanceof TLScope) {
				result.addAll(((TLScope) singleton).getTypes());
			}
		}
		return result;
	}

	/**
	 * The global {@link TLClass}es in the given {@link TLModel}.
	 * <p>
	 * "Global" means, it is either defined directly in the scope of a {@link TLModule} or
	 * {@link TLModule#getSingletons() singleton}.
	 * </p>
	 * 
	 * @return An unmodifiable {@link Set}.
	 */
	public static Set<TLClass> getAllGlobalClasses(TLModel model) {
		return TLModelCacheService.getOperations().getGlobalClasses(model);
	}

	/** Use {@link #getAllGlobalClasses(TLModel)} instead. */
	@FrameworkInternal
	public static Set<TLClass> getAllGlobalClassesUncached(TLModel model) {
		Set<TLClass> result = set();
		for (TLModule module : model.getModules()) {
			result.addAll(module.getClasses());
		}
		for (TLModuleSingleton link : model.getQuery(TLModuleSingletons.class).getAllSingletons()) {
			TLObject singleton = link.getSingleton();
			if (singleton instanceof TLScope) {
				result.addAll(((TLScope) singleton).getClasses());
			}
		}
		return result;
	}

	/**
	 * All usages of the given type as content type in properties and references of the given model.
	 * 
	 * @see TLTypeUsage
	 */
	public static Collection<TLStructuredTypePart> getUsage(TLModel model, TLType type) {
		return model.getQuery(TLTypeUsage.class).getUsage(type);
	}

	/**
	 * Creates a list of {@link TLPrimitive} known in the given {@link TLModel}.
	 */
	public static List<TLPrimitive> getDataTypes(TLModel model) {
		List<TLPrimitive> dataTypes = new ArrayList<>();
		for (TLModule module : model.getModules()) {
			dataTypes.addAll(module.getDatatypes());
		}
		return dataTypes;
	}

	/**
	 * Deletes the given model part and all its "parts".
	 */
	public static void deleteRecursive(TLModelPart part) throws KnowledgeBaseException {
		if (!(part instanceof TransientObject)) {
			part.visit(DeleteVisitor.INSTANCE, null);
		} else {
			// Unlinking part, i.e. removing from TLModel hierarchy should actually also be done for
			// persistent elements, but this is currently not possible because the "owner" of the
			// part delivers unmodifiable list from which no deletion is possible. But this is
			// currently also not necessary because the lists are computed from KB state which is up
			// to date after deletion.
			part.visit(UnlinkPartVisitor.INSTANCE, null);
		}
	}

	/**
	 * The table the given concrete {@link TLStructuredType} stores its instances.
	 * 
	 * <p>
	 * No subtypes of the given type is considered. If the given type is abstract, the result
	 * determines the table in which concrete subtypes of the given abstract type would be stored by
	 * default (without further configuration).
	 * </p>
	 * 
	 * @see #potentialTables(TLClass, boolean)
	 */
	public static MOStructure getTable(TLStructuredType type) {
		String tableName = CompatibilityService.getInstance().getTableFor(type);
		if (tableName == null) {
			return null;
		}
		return (MOStructure) type.tKnowledgeBase().getMORepository().getMetaObject(tableName);
	}

	/**
	 * Determines for a given {@link TLClass} and all specialisations the names of the tables which
	 * are used to store data.
	 * 
	 * <p>
	 * The returned {@link Map} maps a table name to all {@link TLClass} (specialisations of the
	 * given one) that uses that table to store data.
	 * </p>
	 * 
	 * @param type
	 *        The type to get tables for.
	 * @param excludeSubtypes
	 *        If <code>true</code>, only the given type without specialisations is used.
	 * 
	 * @see #getTable(TLStructuredType)
	 */
	public static Map<String, ? extends Collection<TLClass>> potentialTables(TLClass type, boolean excludeSubtypes) {
		if (excludeSubtypes) {
			/* This is a special case which is rarely used. No need to add a cache for that. */
			return potentialTablesUncached(type, excludeSubtypes);
		}
		return TLModelCacheService.getOperations().getPotentialTables(type);
	}

	/** @see #potentialTables(TLClass, boolean) */
	@FrameworkInternal
	public static Map<String, Set<TLClass>> potentialTablesUncached(TLClass type, boolean excludeSubtypes) {
		Set<TLClass> specializations;
		if (excludeSubtypes) {
			if (type.isAbstract()) {
				StringBuilder abstractTypeError = new StringBuilder();
				abstractTypeError.append("Invalid target specification: Abstract type '");
				abstractTypeError.append(type.getName());
				abstractTypeError.append("' excluding subtypes.");
				throw new IllegalArgumentException(abstractTypeError.toString());
			}
			specializations = Collections.<TLClass> singleton(type);
		} else {
			specializations = TLModelUtil.getConcreteReflexiveTransitiveSpecializations(type);
		}

		CompatibilityService compatibilityService = CompatibilityService.getInstance();
		HashMap<String, Set<TLClass>> result = new HashMap<>();
		for (TLClass specialization : specializations) {
			String tableName = compatibilityService.getTableFor(specialization);

			MultiMaps.add(result, tableName, specialization);
		}
		return result;
	}

	/**
	 * Whether the given {@link TLTypePart} may have security, i.e. whether there are separate
	 * access rights for values of that part.
	 * 
	 * @param part
	 *        The part to check.
	 * 
	 * @see TLModelUtil#getAccessAware(TLTypePart)
	 */
	public static boolean isAccessAware(TLTypePart part) {
		return getAccessAware(part) != LiberalAccessChecker.INSTANCE;
	}

	/**
	 * Returns an {@link AccessChecker} for the given {@link TLTypePart}.
	 * 
	 * @param part
	 *        The part to get {@link AccessChecker} for.
	 * @return An {@link AccessChecker} for the given part, or {@link LiberalAccessChecker} if no
	 *         special access is needed.
	 * 
	 * @see TLModelUtil#isAccessAware(TLTypePart)
	 */
	public static AccessChecker getAccessAware(TLTypePart part) {
		if (!(part instanceof TLStructuredTypePart)) {
			return LiberalAccessChecker.INSTANCE;
		}
		return ((TLStructuredTypePart) part).getAccessChecker();
	}

	/**
	 * Sets the generalization of for the given type.
	 * 
	 * @param type
	 *        The type to set generalizations.
	 * @param newGeneralizations
	 *        The new generalizations of the the given type.
	 */
	public static void setGeneralizations(TLClass type, List<? extends TLClass> newGeneralizations) {
		List<TLClass> generalizations = type.getGeneralizations();
		generalizations.clear();
		generalizations.addAll(newGeneralizations);
	}

	/**
	 * Variant of {@link TLStructuredTypePart#getDefinition()} that follows all overridden parts to
	 * their definitions.
	 * <p>
	 * {@link TLStructuredTypePart}s in the {@link TLModel} have exactly one definition. This method
	 * is used to check whether a {@link TLStructuredTypePart} obeys that rule.
	 * </p>
	 * <p>
	 * Returns a {@link Set} with exactly the given part itself, if it is not overriding anything.
	 * </p>
	 * 
	 * @param typePart
	 *        Is not allowed to be null.
	 * @return Never null. A new, mutable and resizable {@link Set}.
	 */
	public static Set<? extends TLStructuredTypePart> getDefinitions(TLStructuredTypePart typePart) {
		if (typePart instanceof TLAssociationPart) {
			return set(typePart);
		}
		TLClassPart classPart = (TLClassPart) typePart;
		Set<TLClassPart> definitions = set(getOverriddenParts(classPart));
		if (definitions.isEmpty()) {
			return set(classPart);
		}
		boolean change = true;
		while ((definitions.size() > 1) && change) {
			change = false;
			for (TLClassPart superPart : definitions) {
				Set<TLClassPart> inheritedParts = getOverriddenParts(superPart.getOwner(), classPart.getName());
				if (!inheritedParts.isEmpty()) {
					definitions.remove(superPart);
					definitions.addAll(inheritedParts);
					change = true;
					/* The iterator of the inner loop is broken, as the collection has changed.
					 * Therefore, the inner loop has to end. */
					break;
				}
			}
		}
		return definitions;
	}

	/**
	 * Checks, whether the given {@link TLModelPart} is a {@link TLProperty}.
	 * 
	 * <p>
	 * If this check returns <code>true</code>, it is safe to cast the given part to
	 * {@link TLProperty}.
	 * </p>
	 * 
	 * <p>
	 * Note: It is not safe to use the <code>instanceof</code> operator for model interfaces,
	 * because implementations may choose to implement more interfaces than actually supported.
	 * </p>
	 * 
	 * @param part
	 *        The part to check.
	 * @return Whether the given part of of kind {@link ModelKind#REFERENCE}.
	 */
	public static boolean isProperty(TLModelPart part) {
		return part != null && part.getModelKind() == ModelKind.PROPERTY;
	}

	/**
	 * Checks, whether the given {@link TLModelPart} is a {@link TLReference}.
	 * 
	 * <p>
	 * If this check returns <code>true</code>, it is safe to cast the given part to
	 * {@link TLReference}.
	 * </p>
	 * 
	 * <p>
	 * Note: It is not safe to use the <code>instanceof</code> operator for model interfaces,
	 * because implementations may choose to implement more interfaces than actually supported.
	 * </p>
	 * 
	 * @param part
	 *        The part to check.
	 * @return Whether the given part of of kind {@link ModelKind#REFERENCE}.
	 */
	public static boolean isReference(TLModelPart part) {
		return part != null && part.getModelKind() == ModelKind.REFERENCE;
	}

	/**
	 * Whether a {@link TLClassPart} with the given name in the given {@link TLClass} would be an
	 * override.
	 * 
	 * @param tlClass
	 *        Is not allowed to be null.
	 * @param partName
	 *        Is not allowed to be null.
	 */
	public static boolean isOverride(TLClass tlClass, String partName) {
		return !getOverriddenParts(tlClass, partName).isEmpty();
	}

	/**
	 * The {@link TLClassPart}s overridden by the given {@link TLClassPart}.
	 * 
	 * @param overridingPart
	 *        Is not allowed to be null.
	 * @return The {@link TLClassPart}s <em>directly</em> overridden by the given part. Never null.
	 *         A new, mutable and resizable {@link Set}.
	 * 
	 * @see #getOverriddenParts(TLClass, String)
	 * @see #getPredecessors(Pair)
	 */
	public static LinkedHashSet<TLClassPart> getOverriddenParts(TLClassPart overridingPart) {
		return getOverriddenParts(overridingPart.getOwner(), overridingPart.getName());
	}

	/**
	 * The {@link TLClassPart}s that a {@link TLClassPart} with the given name in the given
	 * {@link TLClass} would override.
	 * 
	 * @param tlClass
	 *        Is not allowed to be null.
	 * @param partName
	 *        Is not allowed to be null.
	 * @return The {@link TLClassPart}s <em>directly</em> overridden by the given part. Never null.
	 *         A new, mutable and resizable {@link Set}.
	 * 
	 * @see #getOverriddenParts(TLClassPart)
	 * @see #getPredecessors(Pair)
	 */
	public static LinkedHashSet<TLClassPart> getOverriddenParts(TLClass tlClass, String partName) {
		LinkedHashSet<TLClassPart> result = linkedSet();
		for (TLClass superClass : tlClass.getGeneralizations()) {
			TLClassPart part = (TLClassPart) superClass.getPart(partName);
			if (part != null) {
				result.add(part);
			}
		}
		return result;
	}

	/**
	 * The {@link TLClassPart}s that override the given {@link TLClassPart}.
	 * 
	 * @param classPart
	 *        Is not allowed to be null.
	 * @return The {@link TLClassPart}s <em>directly</em> overriding the given part. Never null. A
	 *         new, mutable and resizable {@link Set}.
	 * 
	 * @see #getOverridingParts(TLClass, String)
	 * @see #getSuccessors(Pair)
	 */
	public static Set<TLClassPart> getOverridingParts(TLClassPart classPart) {
		return getOverridingParts(classPart.getOwner(), classPart.getName());
	}

	/**
	 * The {@link TLClassPart}s that would override a {@link TLClassPart} with the given name in the
	 * given owner.
	 * 
	 * @param owner
	 *        Is not allowed to be null.
	 * @param partName
	 *        Is not allowed to be null.
	 * @return The {@link TLClassPart}s <em>directly</em> overriding the given part. Never null. A
	 *         new, mutable and resizable {@link Set}.
	 * 
	 * @see #getOverridingParts(TLClassPart)
	 * @see #getSuccessors(Pair)
	 */
	public static Set<TLClassPart> getOverridingParts(TLClass owner, String partName) {
		Set<TLClassPart> overridingParts = set();
		for (TLClass subtype : owner.getSpecializations()) {
			TLClassPart overridingPart = getLocalPart(subtype, partName);
			if (overridingPart != null) {
				overridingParts.add(overridingPart);
			} else {
				overridingParts.addAll(getOverridingParts(subtype, partName));
			}
		}
		return overridingParts;
	}

	private static TLClassPart getLocalPart(TLClass owner, String partName) {
		for (TLStructuredTypePart part : owner.getLocalParts()) {
			if (part.getName().equals(partName)) {
				return (TLClassPart) part;
			}
		}
		return null;
	}

	/**
	 * Whether the given {@link TLType} is global.
	 * 
	 * @param type
	 *        Is not allowed to be null.
	 */
	public static boolean isGlobal(TLType type) {
		TLScope scope = type.getScope();
		if (scope instanceof TLModule) {
			assert scope == type.getModule() : "Scope of the type is a foreign module. Type: " + debug(type)
				+ ". Module: " + debug(type.getModule()) + ". Scope: " + debug(scope);
			return true;
		}
		if ((type instanceof TLClass) && TLModelCacheService.Module.INSTANCE.isActive()) {
			/* This is an optimization when the cache is active. But when it is not active, this
			 * would take much longer than the unoptimized variant. It is therefore only used when
			 * the cache is active. */
			return isGlobalCached((TLClass) type);
		}
		TLModuleSingleton moduleAndName = type.getModel().getQuery(TLModuleSingletons.class).getModuleAndName(scope);
		if (moduleAndName == null) {
			return false;
		}
		return moduleAndName.getModule() == type.getModule();
	}

	private static boolean isGlobalCached(TLClass tlClass) {
		TLModel tlModel = tlClass.getModel();
		return TLModelCacheService.getOperations().getGlobalClasses(tlModel).contains(tlClass);
	}

	/**
	 * Checks whether the given part is a {@link DerivedTLTypePart} which is
	 * {@link DerivedTLTypePart#isDerived() derived}.
	 */
	public static boolean isDerived(TLModelPart part) {
		return part instanceof DerivedTLTypePart && ((DerivedTLTypePart) part).isDerived();
	}

	/**
	 * Returns the primary (actually the first) generalization of the given type.
	 * 
	 * @param type
	 *        The type to get primary supertype of.
	 * @return The first generalization of the given type, or <code>null</code> when there is no
	 *         generalization.
	 */
	public static TLClass getPrimaryGeneralization(TLType type) {
		if (type.getModelKind() == ModelKind.CLASS) {
			return getPrimaryGeneralization((TLClass) type);
		} else {
			return null;
		}
	}

	/**
	 * Returns the primary (actually the first) generalization of the given type.
	 * 
	 * @param type
	 *        The type to get primary supertype of.
	 * @return The first generalization of the given type, or <code>null</code> when there is no
	 *         generalization.
	 */
	public static TLClass getPrimaryGeneralization(TLClass type) {
		List<TLClass> generalizations = type.getGeneralizations();
		if (generalizations.isEmpty()) {
			return null;
		}
		return generalizations.get(0);
	}

	/**
	 * Adds the names of {@link TLClass#getAllParts() all parts} of the given type to the given
	 * collection of names.
	 * 
	 * @param names
	 *        The collection to add names to.
	 * @param type
	 *        The type the get part names from.
	 * @return The given collection for chaining.
	 */
	public static <T extends Collection<? super String>> T addPartNames(T names, TLClass type) {
		for (TLStructuredTypePart part : type.getAllParts()) {
			names.add(part.getName());
		}
		return names;
	}

	/**
	 * Gets the value stored in the given {@link TLObject} under the property with the given name.
	 * <p>
	 * Throws an exception if the property does not exist.
	 * </p>
	 * 
	 * @param self
	 *        The object from which the value should be retrieved. Is not allowed to be null.
	 * @param propertyName
	 *        Is not allowed to be null.
	 * @return Null, if that is the stored value.
	 */
	public static Object getValue(TLObject self, String propertyName) {
		TLStructuredTypePart property = getProperty(self, propertyName);
		return self.tValue(property);
	}

	/**
	 * Sets the value in the given {@link TLObject} under the property with the given name.
	 * <p>
	 * Throws an exception if the property does not exist.
	 * </p>
	 * 
	 * @param self
	 *        The object in which the value should be stored. Is not allowed to be null.
	 * @param propertyName
	 *        Is not allowed to be null.
	 * @param newValue
	 *        Is allowed to be null, if the property allows null.
	 */
	public static void setValue(TLObject self, String propertyName, Object newValue) {
		TLStructuredTypePart property = getProperty(self, propertyName);
		self.tUpdate(property, newValue);
	}

	/**
	 * The property with the given name from the given {@link TLObject}.
	 * 
	 * @param self
	 *        The {@link TLObject} which has the given property.
	 * @param propertyName
	 *        Is not allowed to be null.
	 * @return See: {@link TLStructuredType#getPart(String)}
	 */
	public static TLStructuredTypePart getProperty(TLObject self, String propertyName) {
		return self.tType().getPart(propertyName);
	}

	/**
	 * Convenience getter for the "name" attribute, which exists in almost every {@link TLObject}.
	 * <p>
	 * Throws an (unspecified) exception if the object has no "name" property.
	 * </p>
	 * 
	 * @param tlObject
	 *        Is not allowed to be null.
	 */
	public static String getName(TLObject tlObject) {
		return (String) TLModelUtil.getValue(tlObject, AbstractWrapper.NAME_ATTRIBUTE);
	}

	/**
	 * Returns the {@link KnowledgeBase} in which the given {@link TLObject} is stored.
	 * 
	 * @param tlObjectModel
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static KnowledgeBase getKnowledgeBase(TLObject tlObjectModel) {
		return tlObjectModel.tHandle().getKnowledgeBase();
	}

	/**
	 * Returns a {@link Pair} of the owner and the name of the attribute.
	 * <p>
	 * This is used in APIs that need to represent attributes which are inherited but are not
	 * explicitly declared and are therefore no overrides. Such attributes have no representation in
	 * the {@link TLModel}. As a substitute, a {@link Pair} of a {@link TLClass} (the attribute
	 * owner) and a {@link String} (the attribute name) are used, if a single object is needed.
	 * </p>
	 * 
	 * @param attribute
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Pair<TLClass, String> toPair(TLClassPart attribute) {
		return new Pair<>(attribute.getOwner(), attribute.getName());
	}

	/**
	 * Convenience variant of {@link #toPair(TLClassPart)} that converts a whole {@link Collection}
	 * at once.
	 * 
	 * @param attributes
	 *        Null is equivalent to an empty {@link Collection}.
	 * @return Never null. A new, mutable and resizable {@link List}.
	 */
	public static List<Pair<TLClass, String>> toPairs(Collection<? extends TLClassPart> attributes) {
		List<Pair<TLClass, String>> result = list();
		for (TLClassPart attribute : CollectionUtilShared.nonNull(attributes)) {
			result.add(toPair(attribute));
		}
		return result;
	}

	/**
	 * The reverse of {@link #toPair(TLClassPart)}.
	 * <p>
	 * {@link Optional#empty()} is returned if:
	 * <ul>
	 * <li>The given {@link Pair} is null.</li>
	 * <li>There is no such {@link TLClassPart}, {@link Optional#empty()} is returned.</li>
	 * <li>The attribute is inherited but not explicitly declared. I.e. if it has no representation
	 * in the {@link TLModel}.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param attribute
	 *        If the {@link Pair} is null, {@link Optional#empty()} is returned. The {@link Pair} is
	 *        not allowed to contain null.
	 * @return Never null.
	 */
	public static Optional<TLClassPart> toClassPart(Pair<TLClass, String> attribute) {
		if (attribute == null) {
			return Optional.empty();
		}
		TLClassPart classPart = (TLClassPart) attribute.getFirst().getPart(attribute.getSecond());
		if (classPart == null) {
			return Optional.empty();
		}
		if (!classPart.getOwner().equals(attribute.getFirst())) {
			/* The part is inherited but not an override. That means there is no TLClassPart with
			 * the correct owner. */
			return Optional.empty();
		}
		return Optional.of(classPart);
	}

	/**
	 * The {@link Pair}s of all the direct subclasses and the attribute name.
	 * <p>
	 * {@link #getOverridingParts(TLClassPart)} is similar, but includes only {@link TLClass}es on
	 * which the attribute is explicitly declared. It also collects the attributes from the indirect
	 * subtypes. This method returns only the direct subclasses, and all of them.
	 * </p>
	 * 
	 * @param attribute
	 *        Is not allowed to be or contain null.
	 * @return Never null.
	 */
	public static Collection<Pair<TLClass, String>> getSuccessors(Pair<TLClass, String> attribute) {
		List<Pair<TLClass, String>> results = list();
		for (TLClass subtype : attribute.getFirst().getSpecializations()) {
			String attributeName = attribute.getSecond();
			results.add(new Pair<>(subtype, attributeName));
		}
		return results;
	}

	/**
	 * The {@link Pair}s of the direct superclasses and the attribute name.
	 * <p>
	 * Superclasses in which the attribute is not defined, are not returned.
	 * </p>
	 * 
	 * @param attribute
	 *        Is not allowed to be or contain null.
	 * @return Never null.
	 */
	public static Collection<Pair<TLClass, String>> getPredecessors(Pair<TLClass, String> attribute) {
		List<Pair<TLClass, String>> results = list();
		for (TLClass subtype : attribute.getFirst().getGeneralizations()) {
			String attributeName = attribute.getSecond();
			if (subtype.getPart(attributeName) != null) {
				results.add(new Pair<>(subtype, attributeName));
			}
		}
		return results;
	}

	/**
	 * The {@link TLType} of the given object.
	 */
	public static TLType type(Object obj) {
		if (obj instanceof TLClassifier) {
			return ((TLClassifier) obj).getOwner();
		}
		if (obj instanceof TLObject) {
			return ((TLObject) obj).tType();
		}
		return null;
	}

	/**
	 * Updates the given {@link TLAnnotation}s.
	 */
	public static void updateAnnotations(TLModelPart part, Collection<? extends TLAnnotation> annotations) {
		Set<Class<?>> types = new HashSet<>();
		for (TLAnnotation annotation : annotations) {
			part.setAnnotation(annotation);
			types.add(annotation.getConfigurationInterface());
		}
		for (TLAnnotation annotation : new ArrayList<>(part.getAnnotations())) {
			Class<? extends TLAnnotation> type = annotation.getConfigurationInterface();
			if (!types.contains(type)) {
				part.removeAnnotation(type);
			}
		}
	}

	/**
	 * True if the given {@link TLReference} is forward, otherwise false.
	 */
	public static boolean isForwardReference(TLReference reference) {
		return TLModelUtil.getEndIndex(reference.getEnd()) > 0;
	}

	/**
	 * Compute the complete set of all {@link TLStructuredTypePart}s of the given type and all of
	 * its subtypes.
	 * <p>
	 * If an attribute is overridden in the given {@link TLClass} or its super-types, the overridden
	 * definitions are not returned, only the overriding definition. But all definitions in
	 * sub-types of the given {@link TLClass} are returned, overridden or not. If an attribute is
	 * not defined in the given {@link TLClass}, the definitions from all subtypes are returned,
	 * even if they have no common root and define for example incompatible types.
	 * </p>
	 * 
	 * @param metaElement
	 *        The type that builds the root of the hierarchy.
	 * @return All {@link TLStructuredTypePart} in the hierarchy. The {@link List} is potentially
	 *         unmodifiable.
	 * 
	 */
	public static List<TLStructuredTypePart> getMetaAttributesInHierarchy(TLClass metaElement) {
		return getMetaAttributes(metaElement, true, true);
	}

	/**
	 * Compute the complete set of all {@link TLStructuredTypePart}s of the given type and all of
	 * its sup- and subtypes if the given arguments are true.
	 * 
	 * @return A potentially unmodifiable {@link List}.
	 */
	public static List<TLStructuredTypePart> getMetaAttributes(TLClass metaElement, boolean includeSuperMetaElements,
			boolean includeSubMetaElements) {
		List<TLStructuredTypePart> base;
		if (includeSuperMetaElements) {
			base = getMetaAttributes(metaElement);
		} else {
			base = getLocalMetaAttributes(metaElement);
		}

		if (includeSubMetaElements) {
			List<TLStructuredTypePart> result = new ArrayList<>(base);
			result.addAll(getAttributesOfSubClasses(metaElement));
			return result;
		} else {
			return base;
		}
	}

	private static Set<TLClassPart> getAttributesOfSubClasses(TLClass tlClass) {
		return TLModelCacheService.getOperations().getAttributesOfSubClasses(tlClass);
	}

	/**
	 * All {@link TLStructuredTypePart}s of this type (including inherited attributes).
	 * <p>
	 * In a stable, but unspecified order.
	 * </p>
	 * 
	 * @return A potentially unmodifiable {@link List}.
	 * 
	 * @see TLModelUtil#getMetaAttributesInHierarchy(TLClass)
	 */
	@SuppressWarnings("unchecked")
	public static List<TLStructuredTypePart> getMetaAttributes(TLClass metaElement) {
		return (List<TLStructuredTypePart>) metaElement.getAllParts();
	}

	/**
	 * The {@link TLStructuredTypePart}s defined locally on this type (excluding inherited
	 * attributes).
	 */
	@SuppressWarnings("unchecked")
	public static List<TLStructuredTypePart> getLocalMetaAttributes(TLClass metaElement) {
		return (List<TLStructuredTypePart>) metaElement.getLocalParts();
	}

	/**
	 * Determines the best match of the type of the given object.
	 * 
	 * @param obj
	 *        The object to get target for.
	 * 
	 * @see #getBestMatch(TLType, Map)
	 */
	public static <T> T getBestMatch(Object obj, Map<TLType, T> matches) {
		TLType targetType;
		if (obj instanceof TLObject) {
			targetType = ((TLObject) obj).tType();
		} else {
			return null;
		}
		return getBestMatch(targetType, matches);
	}

	/**
	 * Determines the target for that type in the given {@link Map}, which is equal to the given
	 * type or the nearest generalisation of it.
	 * 
	 * @param type
	 *        The {@link TLType} to get best match for.
	 * @param matches
	 *        {@link Map} to search for target.
	 * @return The element for that type in the map, that is the "smallest" generalisation (or the
	 *         type itself) of the given type. May be <code>null</code>, when the map is empty or
	 *         neither the type itself nor a generalisation is contained in the map..
	 */
	public static <T> T getBestMatch(TLType type, Map<TLType, T> matches) {
		switch (matches.size()) {
			case 0:
				return null;
			case 1:
				Entry<TLType, T> singleEntry = matches.entrySet().iterator().next();
				if (isCompatibleType(singleEntry.getKey(), type)) {
					return singleEntry.getValue();
				} else {
					return null;
				}
			default:
				T target = matches.get(type);
				if (target == null && type instanceof TLClass) {
					for (TLClass generalization : getTransitiveGeneralizations((TLClass) type)) {
						T generalizationTarget = matches.get(generalization);
						if (generalizationTarget != null) {
							target = generalizationTarget;
							break;
						}
					}
				}
				return target;
		}
	}

	/**
	 * A {@link Predicate} selecting only those implementation classes that have at least one of the
	 * given classifiers in their {@link InApp#classifiers()} list.
	 * 
	 * @param unclassified
	 *        Whether unclassified implementations are considered to match.
	 */
	public static Predicate<Class<?>> classifierPredicate(String[] requiredClassifiers, boolean unclassified) {
		return impl -> {
			String[] givenClassifiers = impl.getAnnotation(InApp.class).classifiers();
			if (givenClassifiers.length == 0) {
				return unclassified;
			}
			for (String requiredClassifier : requiredClassifiers) {
				for (String givenClassifier : givenClassifiers) {
					if (givenClassifier.equals(requiredClassifier)) {
						return true;
					}
				}
			}
			return false;
		};
	}

	/**
	 * Returns true if the given {@link TLClass} has a generalization in the same module as itself,
	 * otherwise false.
	 * 
	 * @param clazz
	 *        A generalization of this class in the same module is searched.
	 */
	public static boolean hasGeneralizationsInSameModule(TLClass clazz) {
		TLModule module = clazz.getModule();

		for (TLClass superType : clazz.getGeneralizations()) {
			if (superType.getModule() == module) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the local name of the given (full qualified) technical name.
	 * 
	 * <p>
	 * It handles <code>.</code> as name part separator.
	 * </p>
	 * 
	 * <p>
	 * Example: The name <code>my.foo.bar</code> results in the local name <code>bar</code>.
	 * </p>
	 */
	public static String getLocalName(String name) {
		int lastIndexOf = name.lastIndexOf(".");

		if (lastIndexOf != -1 && lastIndexOf != name.length() - 1) {
			return name.substring(lastIndexOf + 1);
		} else {
			return name;
		}
	}

}
