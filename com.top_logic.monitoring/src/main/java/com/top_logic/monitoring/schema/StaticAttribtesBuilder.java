/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MOAlternative;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LinkGenerator;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ListModelBuilder} for the static attributes table of the DB schema monitor.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StaticAttribtesBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link StaticAttribtesBuilder} instance.
	 */
	public static final StaticAttribtesBuilder INSTANCE = new StaticAttribtesBuilder();

	private StaticAttribtesBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		MOClass type = (MOClass) businessModel;
		if (type == null) {
			return Collections.emptyList();
		}
		ArrayList<DBAttribute> attributes = new ArrayList<>();
		for (MOAttribute attr : type.getAttributes()) {
			attributes.addAll(Arrays.asList(attr.getDbMapping()));
		}
		return attributes;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof MOClass;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof DBAttribute;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return ((DBAttribute) listElement).getAttribute().getOwner();
	}

	/**
	 * {@link Accessor} for the internal name column.
	 */
	public static class AttributeName extends ReadOnlyAccessor<DBAttribute> {

		/**
		 * Singleton {@link StaticTypesBuilder.Name} instance.
		 */
		public static final AttributeName INSTANCE = new AttributeName();

		private AttributeName() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(DBAttribute object, String property) {
			return object.getAttribute().getName();
		}

	}

	/**
	 * {@link Accessor} for the name column.
	 */
	public static class DbName extends ReadOnlyAccessor<DBAttribute> {

		/**
		 * Singleton {@link StaticTypesBuilder.Name} instance.
		 */
		public static final DbName INSTANCE = new DbName();

		private DbName() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(DBAttribute object, String property) {
			return object.getDBName();
		}

	}

	/**
	 * {@link Accessor} for the column index column.
	 */
	public static class Index extends ReadOnlyAccessor<DBAttribute> {

		/**
		 * Singleton {@link StaticTypesBuilder.Name} instance.
		 */
		public static final Index INSTANCE = new Index();

		private Index() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(DBAttribute object, String property) {
			return object.getDBColumnIndex();
		}

	}

	/**
	 * {@link Accessor} for the size column.
	 */
	public static class DbSize extends ReadOnlyAccessor<DBAttribute> {

		/**
		 * Singleton {@link StaticTypesBuilder.Name} instance.
		 */
		public static final DbSize INSTANCE = new DbSize();

		private DbSize() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(DBAttribute object, String property) {
			return object.getSQLSize();
		}

	}

	/**
	 * {@link Accessor} for the type column.
	 */
	public static class DbType extends ReadOnlyAccessor<DBAttribute> {

		/**
		 * Singleton {@link StaticTypesBuilder.Name} instance.
		 */
		public static final DbType INSTANCE = new DbType();

		private DbType() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(DBAttribute object, String property) {
			DBType sqlType = object.getSQLType();
			if (sqlType == DBType.ID) {
				return IdentifierUtil.SHORT_IDS ? DBType.LONG : DBType.STRING;
			}
			return sqlType;
		}

	}

	/**
	 * {@link Accessor} for the precision column.
	 */
	public static class DbPrecision extends ReadOnlyAccessor<DBAttribute> {

		/**
		 * Singleton {@link StaticTypesBuilder.Name} instance.
		 */
		public static final DbPrecision INSTANCE = new DbPrecision();

		private DbPrecision() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(DBAttribute object, String property) {
			return object.getSQLPrecision();
		}

	}

	/**
	 * {@link Accessor} for the reference target type column.
	 */
	public static class TargetType extends ReadOnlyAccessor<DBAttribute> {

		/**
		 * Singleton {@link StaticTypesBuilder.Name} instance.
		 */
		public static final TargetType INSTANCE = new TargetType();

		private TargetType() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(DBAttribute object, String property) {
			MOAttribute attribute = object.getAttribute();
			if (attribute instanceof MOReference) {
				if (((MOReference) attribute).getPart(object) == ReferencePart.name) {
					MetaObject targetType = attribute.getMetaObject();
					if (targetType instanceof MOAlternative) {
						MOAlternative alternative = (MOAlternative) targetType;
						return resolve(alternative);
					} else {
						return Collections.singletonList(targetType);
					}
				}
			}
			return null;
		}

		private List<MOClass> resolve(MOAlternative alternative) {
			ArrayList<MOClass> result = new ArrayList<>();
			resolve(result, alternative);
			return result;
		}

		private void resolve(List<MOClass> result, MOAlternative alternative) {
			for (MetaObject specialization : alternative.getSpecialisations()) {
				if (specialization instanceof MOAlternative) {
					resolve(result, (MOAlternative) specialization);
				} else {
					if (!result.contains(specialization)) {
						result.add((MOClass) specialization);
					}
				}
			}
		}

	}

	/**
	 * {@link ResourceProvider} rendering links to static types.
	 */
	public static class TypeResources extends DefaultResourceProvider {
		
		private ComponentName _targetComponent;

		/**
		 * Configuration options for {@link TypeResources}.
		 */
		public interface Config extends PolymorphicConfiguration<TypeResources> {

			/**
			 * Name of the component to select types in upon click.
			 */
			@Name("targetComponent")
			@Mandatory
			ComponentName getTargetComponent();

		}
		
		/**
		 * Creates a {@link StaticAttribtesBuilder.TypeResources} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public TypeResources(InstantiationContext context, Config config) {
			_targetComponent = config.getTargetComponent();
		}


		@Override
		public String getLabel(Object object) {
			if (object == null) {
				return null;
			}
			if (object instanceof MOStructure) {
				return ((MOStructure) object).getDBMapping().getDBName();
			}
			return ((MetaObject) object).getName();
		}

		@Override
		public String getCssClass(Object object) {
			if (object == null) {
				return null;
			}
			return (!(object instanceof MOClass)) || ((MOClass) object).isAbstract() ? "moAbstract" : "moConcrete";
		}

		@Override
		public String getLink(DisplayContext context, Object object) {
			if (object == null) {
				return null;
			}
			if (!(object instanceof MOClass)) {
				return null;
			}
			return LinkGenerator.createLink(context, new GotoSelection(_targetComponent, object));
		}

		private static class GotoSelection implements Command {

			private final Object _object;

			private final ComponentName _component;

			public GotoSelection(ComponentName componentName, Object object) {
				_component = componentName;
				_object = object;
			}

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				LayoutComponent targetComponent =
					context.getSubSessionContext().getLayoutContext().getMainLayout().getComponentByName(_component);
				((Selectable) targetComponent).setSelected(_object);
				targetComponent.makeVisible();
				return HandlerResult.DEFAULT_RESULT;
			}

		}
	}
}
