/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.schema;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} for the static types table in the DB schema monitor.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StaticTypesBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link StaticTypesBuilder} instance.
	 */
	public static final StaticTypesBuilder INSTANCE = new StaticTypesBuilder();

	private StaticTypesBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		MORepository moRepository = PersistencyLayer.getKnowledgeBase().getMORepository();
		ArrayList<MOClass> result = new ArrayList<>();
		for (MetaObject type : moRepository.getMetaObjects()) {
			if (type instanceof MOClass) {
				MOClass classType = (MOClass) type;
				result.add(classType);
			}
		}
		return result;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof MOClass;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return null;
	}

	/**
	 * {@link Accessor} for the internal name column.
	 */
	public static class Name extends ReadOnlyAccessor<MOClass> {

		/**
		 * Singleton {@link StaticTypesBuilder.Name} instance.
		 */
		public static final Name INSTANCE = new Name();

		private Name() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(MOClass object, String property) {
			return object.getName();
		}

	}

	/**
	 * {@link Accessor} for the name column.
	 */
	public static class DbName extends ReadOnlyAccessor<MOClass> {

		/**
		 * Singleton {@link StaticTypesBuilder.DbName} instance.
		 */
		public static final DbName INSTANCE = new DbName();

		private DbName() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(MOClass object, String property) {
			return object.getDBMapping().getDBName();
		}

	}

	/**
	 * {@link Accessor} for the "is template" column.
	 */
	public static class IsAbstract extends ReadOnlyAccessor<MOClass> {

		/**
		 * Singleton {@link StaticTypesBuilder.DbName} instance.
		 */
		public static final IsAbstract INSTANCE = new IsAbstract();

		private IsAbstract() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(MOClass object, String property) {
			return object.isAbstract();
		}

	}

	/**
	 * {@link Accessor} for the template column.
	 */
	public static class SuperType extends ReadOnlyAccessor<MOClass> {

		/**
		 * Singleton {@link StaticTypesBuilder.DbName} instance.
		 */
		public static final SuperType INSTANCE = new SuperType();

		private SuperType() {
			// Singleton constructor.
		}

		@Override
		public Object getValue(MOClass object, String property) {
			return object.getSuperclass();
		}

	}

	/**
	 * {@link RowClassProvider} for selecting the CSS class for abstract types.
	 */
	public static class TypeClasses implements RowClassProvider {

		/**
		 * Singleton {@link StaticTypesBuilder.TypeClasses} instance.
		 */
		public static final TypeClasses INSTANCE = new TypeClasses();

		private TypeClasses() {
			// Singleton constructor.
		}

		@Override
		public String getTRClass(TableControl view, int rowOptions, int displayedRow, int row) {
			Object rowObject = view.getViewModel().getRowObject(row);

			String tableRowCssClass = DefaultTableRenderer.TABLE_ROW_CSS_CLASS;

			if ((!(rowObject instanceof MOClass)) || ((MOClass) rowObject).isAbstract()) {
				return tableRowCssClass + " moAbstract";
			} else {
				return tableRowCssClass + " moConcrete";
			}
		}
	}

}
