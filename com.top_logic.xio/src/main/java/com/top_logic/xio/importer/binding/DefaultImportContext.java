/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.xio.importer.I18NConstants;
import com.top_logic.xio.importer.handlers.Handler;
import com.top_logic.xio.importer.handlers.ImportPart;

/**
 * {@link ImportContext} implementation based on a custom {@link ModelBinding}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultImportContext extends LazyTypedAnnotatable implements ImportContext {

	private final ModelBinding _binding;

	private VariableScope _vars = NoVariables.INSTANCE;

	private final Map<String, Ref> _resolvers = new HashMap<>();

	private int _depth = 0;

	private XMLStreamLog _log;

	/**
	 * The import source currently being read (for finding the current location during error
	 * reporting).
	 */
	private XMLStreamReader _source;

	private boolean _logCreations;

	/**
	 * Creates a {@link DefaultImportContext}.
	 */
	public DefaultImportContext(I18NLog log, ModelBinding binding) {
		super();
		_log = XMLStreamLog.fromI18NLog(log);
		_binding = binding;
	}
	
	/**
	 * Whether to log create operations to the application log.
	 */
	public boolean getLogCreations() {
		return _logCreations;
	}
	
	/**
	 * @see #getLogCreations()
	 */
	public void setLogCreations(boolean logCreations) {
		_logCreations = logCreations;
	}

	@Override
	public Object importXml(Handler handler, XMLStreamReader in) throws XMLStreamException {
		_source = in;
		try {
			return ImportContext.super.importXml(handler, in);
		} finally {
			_source = null;
		}
	}

	@Override
	public Object withVar(String var, Object value, XMLStreamReader in, Handler inner) throws XMLStreamException {
		if (var == null) {
			return inner.importXml(this, in);
		}
		if (var.isEmpty()) {
			throw new IllegalArgumentException("Variable must not be empty.");
		}
		_vars = _vars.assign(var, value);
		_depth++;
		try {
			return inner.importXml(this, in);
		} finally {
			_vars = _vars.drop();
			_depth--;
		}
	}

	@Override
	public Object getVar(String name) {
		Object result = getVarOrNull(name);
		if (result == null && name != null && !_vars.containsKey(name)) {
			throw new IllegalArgumentException("Variable '" + name + "' not defined.");
		}
		return result;
	}

	@Override
	public Object getVarOrNull(String name) {
		return _vars.get(name);
	}

	abstract class Resolver implements ImportContext.Resolution {

		private final ImportPart _handler;

		private final Location _location;

		private final String _id;

		public Resolver(ImportPart handler, Location location, String id) {
			_handler = handler;
			_location = location;
			_id = id;
		}

		public void errorUnresolved() {
			error(_location, I18NConstants.ERROR_UNRESOLVED_ID__HANDLER_ID.fill(_handler.location(), _id));
		}

		public String getId() {
			return _id;
		}

	}

	class Ref extends Resolver {

		private List<ImportContext.Resolution> _resolutions = new ArrayList<>();

		private Object _target;

		public Ref(ImportPart handler, Location location, String id) {
			super(handler, location, id);
		}

		void deref(ImportContext.Resolution fun) {
			if (_resolutions == null) {
				fun.resolve(_target);
			} else {
				_resolutions.add(fun);
			}
		}

		@Override
		public void resolve(Object target) {
			// In case, resolve is called from extern.
			dropResolver(getId());

			setTarget(target).forEach(fun -> fun.resolve(target));
		}

		private List<ImportContext.Resolution> setTarget(Object target) {
			List<ImportContext.Resolution> result = _resolutions;
			// Note: Atomically set target and remove resolution functions to prevent duplicate
			// resolution.
			_target = target;
			_resolutions = null;
			return result;
		}

	}

	private Resolver addRef(ImportPart handler, Location location, String id) {
		Ref ref = _resolvers.get(id);
		if (ref == null) {
			ref = new Ref(handler, location, id);
			_resolvers.put(id, ref);
		}
		return ref;
	}

	@Override
	public boolean fillForward(Object ref, Object value) {
		if (ref instanceof Resolver) {
			_binding.assignId(value, ((Resolver) ref).getId());
			((Resolver) ref).resolve(value);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setProperty(ImportPart handler, Object obj, String name, Object value) {
		deref(obj, x -> internalSetProperty(handler, x, name, value));
	}

	@Override
	public void setReference(ImportPart handler, Object obj, String name, Object value) {
		deref(obj, x -> deref(value, y -> internalSetReference(handler, x, name, y)));
	}

	@Override
	public void deref(Object obj, Resolution fun) {
		if (obj instanceof Ref) {
			((Ref) obj).deref(keepVars(_vars, fun));
		} else {
			fun.resolve(obj);
		}
	}

	private Resolution keepVars(VariableScope vars, Resolution fun) {
		return result -> {
			VariableScope before = _vars;
			_vars = vars;
			try {
				fun.resolve(result);
			} finally {
				_vars = before;
			}
		};
	}

	@Override
	public Object resolveObject(ImportPart handler, Location location, String id) {
		Object result = resolveObjectBackwards(handler, location, id);
		if (result != null) {
			return result;
		}

		return addRef(handler, location, id);
	}

	@Override
	public Object resolveObjectBackwards(ImportPart handler, Location location, String id) {
		return _binding.resolveObject(handler, location, id);
	}

	@Override
	public Object createObject(ImportPart handler, Location location, String modelType, String id) {
		if (id != null) {
			Object existing = _binding.resolveObject(handler, location, id);
			if (existing != null) {
				error(location, I18NConstants.ERROR_DUPLICATE_IDENTIFIER__HANDLER_ID.fill(handler.location(), id));
				return existing;
			}
		}
		
		if (_logCreations) {
			logCreation(modelType, id);
		}
		
		Object result = _binding.createObject(handler, location, modelType, id);
		resolveReferences(id, result);
		return result;
	}

	private void logCreation(String modelType, String id) {
		StringBuilder indent = new StringBuilder();
		for (int n = 0; n < _depth; n++) {
			indent.append(' ');
		}
		if (id == null) {
			Logger.info(indent + "Creating '" + modelType + "' anonymously.", DefaultImportContext.class);
		} else {
			Logger.info(indent + "Creating '" + modelType + "' with ID '" + id + "'.", DefaultImportContext.class);
		}
	}

	@Override
	public void assignId(Object value, String id) {
		if (value == null) {
			return;
		}
		_binding.assignId(value, id);
		resolveReferences(id, value);
	}

	private void resolveReferences(String id, Object result) {
		if (id != null) {
			Ref ref = dropResolver(id);
			if (ref != null) {
				ref.resolve(result);
			}
		}
	}

	Ref dropResolver(String id) {
		return _resolvers.remove(id);
	}

	@Override
	public void isInstanceOf(Object obj, String type, Runnable yes, Runnable no) {
		deref(obj, x -> {
			if (_binding.isInstanceOf(x, type)) {
				yes.run();
			} else {
				no.run();
			}
		});
	}

	@Override
	public void addValue(ImportPart handler, Object obj, String part, Object element) {
		deref(obj, x -> deref(element, y -> internalAddValue(handler, x, part, y)));
	}

	private void internalSetProperty(ImportPart handler, Object obj, String name, Object value) {
		try {
			_binding.setProperty(handler, obj, name, value);
		} catch (RuntimeException ex) {
			error(location(), I18NConstants.ERROR_SETTING_PROPERTY_FAILED__HANDLER_OBJ_PROP_VALUE_MESSAGE.fill(handler,
				obj, name, value, ex.getMessage()), ex);
		}
	}

	private void internalSetReference(ImportPart handler, Object obj, String name, Object value) {
		try {
			_binding.setReference(handler, obj, name, value);
		} catch (RuntimeException ex) {
			error(location(), I18NConstants.ERROR_SETTING_PROPERTY_FAILED__HANDLER_OBJ_PROP_VALUE_MESSAGE.fill(handler,
				obj, name, value, ex.getMessage()), ex);
		}
	}

	private void internalAddValue(ImportPart handler, Object obj, String name, Object value) {
		try {
			_binding.addValue(handler, obj, name, value);
		} catch (RuntimeException ex) {
			error(location(),
				I18NConstants.ERROR_SETTING_PROPERTY_FAILED__HANDLER_OBJ_PROP_VALUE_MESSAGE.fill(handler.location(),
					obj, name, value, ex.getMessage()), ex);
		}
	}

	private Location location() {
		return _source == null ? null : _source.getLocation();
	}

	@Override
	public void close() {
		_resolvers.values().forEach(r -> r.errorUnresolved());
	}

	@Override
	public Object eval(Expr expr, Object... args) {
		return _binding.eval(expr, args);
	}

	@Override
	public void log(Level level, Location location, ResKey message, Throwable ex) {
		_log.log(level, location, message, ex);
	}
}
