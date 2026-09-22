/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.SortColumn;

/**
 * The {@code <embedded-columns>} of a table: the columns of an object the row points to, shown as
 * columns of the row.
 *
 * <p>
 * A table of tickets shows the name and the mail address of the assignee without saying a second
 * time how a person is displayed: it embeds the columns of the assignee, and each of them reads its
 * value from the person the row points to. The embedded object is reached either by naming the
 * {@link Config#getReference() reference} leading to it - a whole path of references, where it is
 * further away - or by a {@link Config#getObject() function} computing it, for an object no single
 * reference leads to.
 * </p>
 *
 * <p>
 * What is embedded are column declarations of any kind, resolved against the type of the embedded
 * object exactly as the columns of a table over that object would be - including the columns the
 * embedded type offers in addition, and another embedding one step further. An embedding declaring
 * nothing shows the main properties of the embedded type, so naming the reference is all it takes.
 * </p>
 *
 * <p>
 * The embedded columns are named and labelled after the path leading to them, so that the name of
 * the row and the name of the assignee stay apart in the header and in the column selection. Where
 * a row reaches several objects - over a reference holding more than one, or through a function
 * yielding a collection of them - a cell shows the value of each of them.
 * </p>
 */
@InApp
public class EmbeddedColumns implements ColumnDeclaration {

	/** The tag the columns of a referenced object are embedded with. */
	public static final String TAG_NAME = "embedded-columns";

	/** Separates the path of an embedding from the name of an embedded column. */
	private static final char NAME_SEPARATOR = '.';

	/**
	 * Configuration of an {@link EmbeddedColumns}.
	 */
	@TagName(TAG_NAME)
	public interface Config extends ColumnDeclaration.Config<EmbeddedColumns>, ColumnsConfig, Inputs {

		/** Configuration name for {@link #getReference()}. */
		String REFERENCE = "reference";

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getObject()}. */
		String OBJECT = "object";

		/** Configuration name for {@link #getMultiple()}. */
		String MULTIPLE = "multiple";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		@Override
		@ClassDefault(EmbeddedColumns.class)
		Class<? extends EmbeddedColumns> getImplementationClass();

		/**
		 * The reference leading from the row to the embedded object.
		 *
		 * <p>
		 * A path of reference names separated by dots reaches an object further away, each step
		 * resolved in the type the step before it leads to. A step holding several objects makes
		 * every embedded column show the value of each of them.
		 * </p>
		 *
		 * <p>
		 * Either the reference leading to the embedded object is named here, or the object is
		 * computed by a function and its type declared.
		 * </p>
		 */
		@Name(REFERENCE)
		@Nullable
		String getReference();

		/**
		 * The name the embedded columns are named after, or unset to name them after the reference
		 * leading to them.
		 *
		 * <p>
		 * It is what the names of the embedded columns start with, so that a column of the embedded
		 * object and one of the row itself stay apart. A computed embedding has no reference to
		 * take it from and declares it.
		 * </p>
		 */
		@Name(NAME)
		@Nullable
		String getName();

		/**
		 * The type of the computed object, deciding which columns it is embedded with.
		 *
		 * <p>
		 * Declared together with the function computing the object; an embedding naming a reference
		 * takes the type from that reference.
		 * </p>
		 */
		@Name(TYPE)
		@Nullable
		TLModelPartRef getType();

		/**
		 * The function computing the embedded object, receiving the row as its last argument.
		 *
		 * <p>
		 * The values of the declared inputs come first, in declaration order. The function yields
		 * the one object whose columns are embedded, of the declared type.
		 * </p>
		 */
		@Name(OBJECT)
		@Nullable
		Expr getObject();

		/**
		 * Whether the {@link #getObject()} function yields a collection of objects rather than a
		 * single one.
		 *
		 * <p>
		 * Every embedded column then shows the value of each of them, exactly as it does where a
		 * reference on the path holds several objects. An embedding naming a reference takes this
		 * from the path.
		 * </p>
		 */
		@Name(MULTIPLE)
		boolean getMultiple();

		/**
		 * What the header of every embedded column starts with, or unset to take it from the labels
		 * of the references leading to the embedded object.
		 *
		 * <p>
		 * The label of an embedded column is this prefix and the column's own label, so that the
		 * name of the row and the name of the assignee read differently. A computed embedding has
		 * no reference to take the prefix from and declares it.
		 * </p>
		 */
		@Name(LABEL)
		ResKey getLabel();

	}

	/**
	 * The object an {@link EmbeddedColumns} embeds the columns of, as far as it is known before the
	 * table has rows.
	 *
	 * @param type
	 *        The type of the embedded object, or {@code null} when it is unknown - the row type is
	 *        then unknown as well and nothing can be resolved about the embedded columns.
	 * @param navigation
	 *        Reads the embedded object from a row, yielding {@code null} where the row leads
	 *        nowhere and a collection where the path leads over a reference holding several
	 *        objects.
	 * @param multiple
	 *        Whether a row reaches several embedded objects, so that every embedded column shows a
	 *        collection of values.
	 * @param prefix
	 *        What the header of every embedded column starts with.
	 */
	private record Target(TLStructuredType type, Function<Object, Object> navigation, boolean multiple,
			ResKey prefix) {
		// Pure value type.
	}

	private final List<String> _path;

	private final String _name;

	private final TLModelPartRef _typeRef;

	private final QueryExecutor _object;

	private final boolean _multiple;

	private final List<ChannelRef> _inputs;

	private final ResKey _label;

	private final List<ColumnDeclaration> _columns;

	/**
	 * Creates an {@link EmbeddedColumns} from configuration.
	 */
	@CalledByReflection
	public EmbeddedColumns(InstantiationContext context, Config config) {
		String reference = config.getReference();
		_path = reference == null ? List.of() : List.of(reference.split("\\" + NAME_SEPARATOR));
		_typeRef = config.getType();
		_object = QueryExecutor.compileOptional(config.getObject());
		_multiple = config.getMultiple();
		_inputs = config.getInputs();
		_label = config.getLabel();
		_columns = ColumnDeclarations.instantiate(context, config);
		_name = config.getName() != null ? config.getName() : reference;

		checkTarget(context, config, reference);
	}

	/**
	 * Reports an embedding that does not say which object it embeds the columns of: the reference
	 * leading to it, or a function computing it together with its type - one of the two, and a name
	 * and a label for the computed one, which has no reference to take them from.
	 */
	private void checkTarget(InstantiationContext context, Config config, String reference) {
		boolean computed = _typeRef != null || _object != null;
		if (reference != null) {
			if (computed) {
				context.error("Embedded columns are reached either through a reference or through a"
					+ " computed object, not both: '" + reference + "'.");
			}
			if (_multiple) {
				context.error("The path says how many objects it reaches, so '" + Config.MULTIPLE
					+ "' is declared only by a computed embedding: '" + reference + "'.");
			}
			return;
		}
		if (!computed) {
			context.error("Embedded columns must say which object they show: name the '"
				+ Config.REFERENCE + "' leading to it, or compute it with '" + Config.OBJECT
				+ "' and declare its '" + Config.TYPE + "'.");
			return;
		}
		if (_typeRef == null || _object == null) {
			context.error("A computed embedding needs both the function computing the object ('"
				+ Config.OBJECT + "') and its type ('" + Config.TYPE + "').");
		}
		if (config.getName() == null) {
			context.error("A computed embedding has no reference to name its columns after and needs a '"
				+ Config.NAME + "'.");
		}
		if (_label == null) {
			context.error("A computed embedding has no reference to label its columns after and needs a '"
				+ Config.LABEL + "'.");
		}
	}

	@Override
	public List<String> declaredNames() {
		List<String> result = new ArrayList<>();
		for (String name : ColumnDeclarations.declaredNames(_columns)) {
			result.add(embeddedName(name));
		}
		return result;
	}

	@Override
	public List<SortColumn> defaultSort() {
		List<SortColumn> result = new ArrayList<>();
		for (ColumnDeclaration column : _columns) {
			for (SortColumn sort : column.defaultSort()) {
				result.add(new SortColumn(embeddedName(sort.column()), sort.ascending()));
			}
		}
		return result;
	}

	@Override
	public List<ColumnSetup> resolve(ColumnResolution scope) {
		Target target = target(scope);
		ColumnResolution embedded = new ColumnResolution(target.type(), scope.context());
		List<ColumnDeclaration> displayed =
			_columns.isEmpty() ? ColumnDeclarations.mainColumns(target.type()) : _columns;

		List<ColumnSetup> result = new ArrayList<>();
		for (ColumnSetup setup : ColumnDeclarations.resolve(displayed, embedded)) {
			result.add(embed(setup, target));
		}
		List<ColumnDeclaration> offered =
			ColumnDeclarations.offeredColumns(ColumnDeclarations.declaredNames(displayed), target.type());
		for (ColumnSetup setup : ColumnDeclarations.resolve(offered, embedded)) {
			result.add(embed(setup, target));
		}
		return result;
	}

	/**
	 * The column showing what the given column of the embedded object shows, as a column of the
	 * row: named and labelled after the path leading to the embedded object, reading its value
	 * through that path, and displayed but not edited - an embedded object is shown through the
	 * row, and what it holds is changed where that object itself is edited.
	 *
	 * @param setup
	 *        The column as the embedded object's own table would show it.
	 * @param target
	 *        The embedded object the column reads through.
	 */
	private ColumnSetup embed(ColumnSetup setup, Target target) {
		Function<Object, Object> navigation = target.navigation();
		Function<Object, Object> embedded = setup.value();
		Function<List<Object>, Object> aggregate = setup.aggregate();

		return new ColumnSetup(
			embeddedName(setup.name()),
			I18NConstants.EMBEDDED_COLUMN_LABEL__PREFIX_COLUMN.fill(target.prefix(), setup.label()),
			target.multiple() ? setup.type().collected() : setup.type(),
			row -> values(navigation.apply(row), embedded),
			setup.viewContext(),
			setup.binding(),
			setup.width(),
			null,
			aggregate == null ? null : rows -> aggregate.apply(objects(rows, navigation)),
			setup.hiddenByDefault());
	}

	/** The name of an embedded column, under the path leading to the object it shows. */
	private String embeddedName(String name) {
		return _name + NAME_SEPARATOR + name;
	}

	/**
	 * What the given embedded object holds for an embedded column: the value the column reads, the
	 * collection of those values where the row reaches several objects, and nothing where it
	 * reaches none.
	 *
	 * @param target
	 *        The embedded object of one row, a collection of them, or {@code null}.
	 * @param value
	 *        Reads the cell value from the embedded object.
	 */
	private static Object values(Object target, Function<Object, Object> value) {
		if (target == null) {
			return null;
		}
		if (target instanceof Collection<?> targets) {
			List<Object> result = new ArrayList<>(targets.size());
			for (Object object : targets) {
				if (object != null) {
					collect(result, value.apply(object));
				}
			}
			return result;
		}
		return value.apply(target);
	}

	/**
	 * The embedded objects of the given rows, which an embedded column aggregates over: what its
	 * own table would aggregate over, reached through the path.
	 *
	 * @param rows
	 *        The member rows of the group the column computes its cell for.
	 * @param navigation
	 *        Reads the embedded object from a row.
	 */
	private static List<Object> objects(List<Object> rows, Function<Object, Object> navigation) {
		List<Object> result = new ArrayList<>(rows.size());
		for (Object row : rows) {
			collect(result, navigation.apply(row));
		}
		return result;
	}

	/**
	 * The object an embedding shows the columns of, resolved against the rows of the table.
	 *
	 * @param scope
	 *        What the columns are resolved against.
	 */
	private Target target(ColumnResolution scope) {
		if (_path.isEmpty()) {
			return computedTarget(scope);
		}
		return navigatedTarget(scope);
	}

	/**
	 * The object the declared path of references leads to, and the navigation reading it from a
	 * row.
	 *
	 * <p>
	 * A step naming something the type at hand does not hold, or holds as something other than a
	 * reference, is reported: such a path would show empty cells and nothing would say why.
	 * </p>
	 */
	private Target navigatedTarget(ColumnResolution scope) {
		TLStructuredType type = scope.rowType();
		Function<Object, Object> navigation = Function.identity();
		boolean multiple = false;
		ResKey prefix = null;
		for (String step : _path) {
			ResKey stepLabel = ResKey.text(step);
			if (type != null) {
				TLStructuredTypePart part = type.getPart(step);
				if (part == null) {
					throw new IllegalStateException("The embedded columns of '" + _name
						+ "' navigate over '" + step + "', which " + type + " does not hold.");
				}
				if (!(part instanceof TLReference reference)) {
					throw new IllegalStateException("The embedded columns of '" + _name
						+ "' navigate over '" + step + "', which is not a reference.");
				}
				TLType referenced = reference.getType();
				if (!(referenced instanceof TLStructuredType referencedType)) {
					throw new IllegalStateException("The embedded columns of '" + _name
						+ "' navigate over '" + step + "', which does not lead to an object.");
				}
				multiple |= reference.isMultiple();
				type = referencedType;
				stepLabel = TLModelNamingConvention.resourceKey(reference);
			}
			prefix = prefix == null ? stepLabel
				: I18NConstants.EMBEDDED_COLUMN_LABEL__PREFIX_COLUMN.fill(prefix, stepLabel);
			Function<Object, Object> before = navigation;
			String name = step;
			navigation = row -> step(before.apply(row), name);
		}
		return new Target(type, navigation, multiple, _label != null ? _label : prefix);
	}

	/** The object the declared function computes, of the declared type. */
	private Target computedTarget(ColumnResolution scope) {
		TLType type = _typeRef == null ? null : _typeRef.resolveType(scope.model());
		if (type != null && !(type instanceof TLStructuredType)) {
			throw new IllegalStateException("The embedded columns of '" + _name + "' declare the type "
				+ type + ", which holds no columns.");
		}
		List<ViewChannel> inputs = ChannelInputs.resolve(scope.context(), _inputs);
		QueryExecutor object = _object;
		Function<Object, Object> navigation =
			object == null ? row -> null : row -> object.execute(ChannelInputs.arguments(inputs, row));
		return new Target((TLStructuredType) type, navigation, _multiple, _label);
	}

	/**
	 * One step of the navigation: what the given object holds under the given reference.
	 *
	 * <p>
	 * A step over a collection of objects is taken for each of them, so that a path leading over a
	 * reference holding several objects yields all the objects it reaches.
	 * </p>
	 *
	 * @param object
	 *        What the steps before this one reached, possibly {@code null}.
	 * @param name
	 *        The name of the reference to follow.
	 */
	private static Object step(Object object, String name) {
		if (object == null) {
			return null;
		}
		if (object instanceof Collection<?> objects) {
			List<Object> result = new ArrayList<>(objects.size());
			for (Object element : objects) {
				collect(result, step(element, name));
			}
			return result;
		}
		if (object instanceof TLObject model) {
			TLStructuredType type = model.tType();
			TLStructuredTypePart part = type == null ? null : type.getPart(name);
			return part == null ? null : model.tValue(part);
		}
		return null;
	}

	/**
	 * Adds the given value to the given list, a collection of values with each of its elements, so
	 * that a path over several multi-valued steps yields one flat collection.
	 */
	private static void collect(List<Object> result, Object value) {
		if (value == null) {
			return;
		}
		if (value instanceof Collection<?> values) {
			result.addAll(values);
		} else {
			result.add(value);
		}
	}

}
