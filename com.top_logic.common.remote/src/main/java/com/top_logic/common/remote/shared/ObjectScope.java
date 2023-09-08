/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.common.remote.json.JsonSerializable;
import com.top_logic.common.remote.json.JsonUtil;
import com.top_logic.common.remote.json.Ref;
import com.top_logic.common.remote.update.Change;
import com.top_logic.common.remote.update.Changes;
import com.top_logic.common.remote.update.Create;
import com.top_logic.common.remote.update.Delete;
import com.top_logic.common.remote.update.Update;

/**
 * Pool of interconnected {@link SharedObject}s that can be synchronized with a corresponding pool
 * through a network protocol.
 * 
 * <p>
 * Objects in an {@link ObjectScope} store their properties and references to other objects in
 * {@link ObjectData data containers} managed by the pool. These data containers are observed for
 * manipulation by the {@link ObjectScope} and changes are recorded for future transmission and
 * replay in an associated {@link ObjectScope} that is potentially stored in another machine.
 * </p>
 * 
 * @see SharedObject
 * @see #popChanges()
 * @see #update(Changes)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObjectScope implements JsonSerializable {

	private final Map<String, ObjectData> _objById = new LinkedHashMap<>();

	private final Map<ObjectData, Operation> _operations = new LinkedHashMap<>();

	private final Map<ObjectData, Set<String>> _changes = new HashMap<>();

	private IdSource _idSource = new IntIdSource();

	private final HandleFactory _factory;

	private TypeNaming _typeNaming = ClassTypeNaming.INSTANCE;

	private boolean _replay;

	private ScopeListeners _listeners = new ScopeListeners();

	private DataMapping _dataMapping = DefaultDataMapping.INSTANCE;

	/**
	 * Creates a {@link ObjectScope}.
	 *
	 * @param factory
	 *        See {@link #getFactory()}.
	 */
	public ObjectScope(HandleFactory factory) {
		_factory = factory;
	}

	/**
	 * A {@link HandleFactory} that translates network type names to application type instance
	 * constructions.
	 */
	public HandleFactory getFactory() {
		return _factory;
	}

	/**
	 * {@link TypeNaming} strategy that translates application object instances into network type
	 * names.
	 */
	public TypeNaming getTypeNaming() {
		return _typeNaming;
	}

	/**
	 * @see #getTypeNaming()
	 */
	public void setTypeNaming(TypeNaming typing) {
		_typeNaming = typing;
	}

	/**
	 * Factory for network IDs that allow identifying {@link ObjectData}s in different
	 * {@link ObjectScope}s across the network.
	 */
	public IdSource getIdSource() {
		return _idSource;
	}

	/**
	 * @see #getIdSource()
	 */
	public void setIdSource(IdSource idSource) {
		_idSource = idSource;
	}

	/**
	 * The {@link DataMapping} strategy in use.
	 */
	public DataMapping getDataMapping() {
		return _dataMapping;
	}

	/**
	 * @see #getDataMapping()
	 */
	public void setDataMapping(DataMapping dataMapping) {
		_dataMapping = dataMapping;
	}

	/**
	 * All objects in this {@link ObjectScope}.
	 */
	public final Collection<ObjectData> objects() {
		return _objById.values();
	}

	/**
	 * The object with the given network ID.
	 * 
	 * @param id
	 *        The ID to resolve.
	 * @return The {@link ObjectData} with the given ID, or <code>null</code> if no such object
	 *         exists in this {@link ObjectScope}.
	 */
	public final ObjectData obj(String id) {
		return _objById.get(id);
	}

	/**
	 * The network type name of the given object.
	 */
	public String networkType(ObjectData obj) {
		return _typeNaming.networkType(obj);
	}

	/**
	 * Callback that is triggered for each object creation in this {@link ObjectScope}.
	 * 
	 * @param obj
	 *        The newly created object.
	 */
	public void notifyCreate(ObjectData obj) {
		if (_replay) {
			return;
		}
		checkCreate(obj);
		String id = _idSource.newId();
		index(obj, id);
		recordCreate(obj);

		_listeners.notifyCreate(this, obj.handle());
	}

	private void checkCreate(ObjectData obj) {
		if ((!_operations.containsKey(obj)) && !_changes.containsKey(obj)) {
			return;
		}
		if (_operations.get(obj) == Operation.DELETE) {
			throw new UnsupportedOperationException("Re-creating a deleted object is not supported.");
		}
		throw new IllegalStateException("The object has already been created.");
	}

	private void recordCreate(ObjectData obj) {
		_operations.put(obj, Operation.CREATE);
	}

	/**
	 * Callback that is triggered for each object deletion in this {@link ObjectScope}.
	 * 
	 * @param obj
	 *        The deleted object.
	 */
	public void notifyDelete(ObjectData obj) {
		if (_replay) {
			return;
		}
		checkDelete(obj);
		recordDelete(obj);

		_listeners.notifyDelete(this, obj.handle());
		delete(obj.id());
	}

	private void checkDelete(ObjectData obj) {
		if (obj.id() == null) {
			throw new IllegalArgumentException("Object is not part of this scope.");
		}
	}

	private void recordDelete(ObjectData obj) {
		if (_operations.get(obj) == Operation.CREATE) {
			_operations.remove(obj);
			return;
		}
		_operations.put(obj, Operation.DELETE);
		_changes.remove(obj);
	}

	/**
	 * Callback that is triggered for each property manipulation of every object in this
	 * {@link ObjectScope}.
	 * 
	 * @param obj
	 *        The modified object.
	 * @param property
	 *        The touched property.
	 */
	public void notifyUpdate(ObjectData obj, String property) {
		if (_replay) {
			return;
		}
		recordUpdate(obj, property);
		sendUpdateEvent(obj, property);
	}

	private void sendUpdateEvent(ObjectData obj, String property) {
		_listeners.notifyUpdate(this, obj.handle(), property);
		obj.handleAttributeUpdate(property);
	}

	private void recordUpdate(ObjectData obj, String property) {
		if (_operations.containsKey(obj)) {
			return;
		}
		Set<String> changedProperties = _changes.get(obj);
		if (changedProperties == null) {
			changedProperties = new HashSet<>();
			_changes.put(obj, changedProperties);
		}
		changedProperties.add(property);
	}

	/**
	 * Whether {@link #popChanges()} would return an empty set of changes.
	 */
	public boolean hasUpdates() {
		return (!_operations.isEmpty()) || !_changes.isEmpty();
	}

	/**
	 * Externalizes recorded changes in this {@link ObjectScope} and resets the change set to empty.
	 * 
	 * @return A description of changes that have happened since the last call to this method, or
	 *         the creation of this {@link ObjectScope}.
	 * 
	 * @see #update(Changes)
	 */
	public Changes popChanges() {
		Changes result = new Changes();
		do {
			for (Entry<ObjectData, Operation> entry : popOperations()) {
				ObjectData obj = entry.getKey();
				switch (entry.getValue()) {
					case CREATE: {
						result.getCreates().add(new Create(obj.id(), networkType(obj)));
						Map<String, Object> properties = obj.properties();
						if (!properties.isEmpty()) {
							result.getUpdates().add(new Update(obj.id(), inspect(properties)));
						}
						break;
					}
					case DELETE: {
						result.getDeletes().add(new Delete(obj.id()));
						break;
					}
				}
			}
			for (Entry<ObjectData, Set<String>> entry : _changes.entrySet()) {
				ObjectData obj = entry.getKey();
				result.getUpdates().add(new Update(obj.id(), inspect(extract(obj, entry.getValue()))));
			}
			_changes.clear();
		} while (!_operations.isEmpty());

		return result;
	}

	private Map<String, Object> inspect(Map<String, Object> values) {
		for (Object value : values.keySet()) {
			if (value instanceof Collection<?>) {
				for (Object element : ((Collection<?>) value)) {
					data(element);
				}
			} else {
				data(value);
			}
		}
		return values;
	}

	private Collection<Entry<ObjectData, Operation>> popOperations() {
		List<Entry<ObjectData, Operation>> result = new ArrayList<>(_operations.entrySet());
		_operations.clear();
		return result;
	}

	private static Map<String, Object> extract(ObjectData obj, Set<String> properties) {
		Map<String, Object> values = new HashMap<>();
		for (String property : properties) {
			values.put(property, obj.getDataRaw(property));
		}
		return values;
	}

	/**
	 * Replays the given changes in this {@link ObjectScope}.
	 * 
	 * @param changes
	 *        Recorded changes from another {@link ObjectScope}. Is not allowed to be null.
	 * 
	 * @see #popChanges()
	 */
	public final void update(Changes changes) {
		firePrepareEvent();

		fireDeleteEvents(changes.getDeletes());
		startReplay();
		try {
			updateObjectData(changes);
			createHandles(changes.getCreates());
			updateHandles(changes.getUpdates());
		} finally {
			stopReplay();
		}
		fireCreateEvents(changes.getCreates());
		fireUpdateEvents(changes.getUpdates());

		firePostProcessEvent();
	}

	private void firePostProcessEvent() {
		_listeners.notifyPostProcess(this);
	}

	private void firePrepareEvent() {
		_listeners.notifyPrepare(this);
	}

	private void fireDeleteEvents(List<Delete> deletes) {
		for (Delete delete : deletes) {
			_listeners.notifyDelete(this, getHandle(delete));
		}
	}

	private void fireCreateEvents(List<Create> creates) {
		for (Create create : creates) {
			_listeners.notifyCreate(this, getHandle(create));
		}
	}

	private void fireUpdateEvents(List<Update> updates) {
		for (Update update : updates) {
			for (String property : update.getValues().keySet()) {
				ObjectData obj = obj(update.getId());
				sendUpdateEvent(obj, property);
			}
		}
	}

	private Object getHandle(Change change) {
		return obj(change.getId()).handle();
	}

	/** @see #startReplay() */
	protected void stopReplay() {
		_replay = false;
	}

	/** Has to be called when changes from the other side (client vs server) are being applied. */
	protected void startReplay() {
		if (_replay) {
			throw new IllegalStateException("Already updating.");
		}
		_replay = true;
	}

	/** @see #startReplay() */
	public boolean isReplaying() {
		return _replay;
	}

	private void updateObjectData(Changes changes) {
		sort(changes).forEach(this::updateObjectDataUnsorted);
	}

	private List<Change> sort(Changes changes) {
		return ChangeDependencySorter.sort(changes);
	}

	private void updateObjectDataUnsorted(Change change) {
		if (change instanceof Create) {
			updateObjectData((Create) change);
		}
		if (change instanceof Update) {
			updateObjectData((Update) change);
		}
		if (change instanceof Delete) {
			handleObjectData((Delete) change);
		}
	}

	private void updateObjectData(Create create) {
		ObjectData obj = _factory.createHandle(create.getNetworkType(), this);
		String id = create.getId();
		index(obj, id);
		getIdSource().setLastId(id);
	}

	private void updateObjectData(Update update) {
		String id = update.getId();
		ObjectData obj = obj(id);
		if (obj == null) {
			throw new IllegalArgumentException("Updated object not found: " + id);
		}
		for (Entry<String, Object> entry : update.getValues().entrySet()) {
			obj.setData(entry.getKey(), resolve(entry.getValue()));
		}
	}

	private void handleObjectData(Delete delete) {
		delete(delete.getId());
	}

	private void delete(String id) {
		ObjectData obj = _objById.get(id);
		if (obj == null) {
			throw new IllegalArgumentException("Dropping undefined ID: " + id);
		}
		obj.onDelete();
		_objById.remove(id);
	}

	private Object resolve(Object value) {
		if (value instanceof Ref) {
			String id = ((Ref) value).id();
			ObjectData result = obj(id);
			if (result == null) {
				throw new IllegalArgumentException("Unresolvable object ID: " + id);
			}
			return result;
		}
		if (value instanceof List<?>) {
			return resolveList((List<?>) value);
		}
		return value;
	}

	private Object resolveList(List<?> list) {
		List<Object> result = new ArrayList<>(list.size());
		for (Object entry : list) {
			result.add(resolve(entry));
		}
		return result;
	}

	private void index(ObjectData obj, String id) {
		String existingId = obj.id();
		if (existingId != null) {
			throw new IllegalArgumentException("Object '" + obj + "' is already part of a scope.");
		}
		obj.initId(id);
		ObjectData clashObj = _objById.put(id, obj);
		if (clashObj != null) {
			_objById.put(id, clashObj);
			throw new IllegalArgumentException("ID '" + id + "' is already used in this scope.");
		}
	}

	/**
	 * Hook for subclasses that is called after the {@link #update(Changes)} have been applied but
	 * before events are fired.
	 * 
	 * @param creates
	 *        The {@link Create}s to apply. Never null. Never contains null.
	 */
	protected void createHandles(List<Create> creates) {
		// Hook for subclasses.
	}

	/**
	 * Hook for subclasses that is called after the {@link #update(Changes)} have been applied but
	 * before events are fired.
	 * 
	 * @param updates
	 *        The {@link Update}s to apply. Never null. Never contains null.
	 */
	protected void updateHandles(List<Update> updates) {
		// Hook for subclasses.
	}

	@Override
	public void writeTo(JsonWriter writer) throws IOException {
		writer.beginObject();
		{
			writer.name("objects");
			writer.beginArray();

			List<ObjectData> objects = new ArrayList<>(objects());

			writeObjects(writer, objects);

			// Note: While serializing objects, new ones are discovered that also must be
			// serialized.
			while (!_operations.isEmpty()) {
				List<ObjectData> created = new ArrayList<>();
				for (Entry<ObjectData, Operation> entry : _operations.entrySet()) {
					if (entry.getValue() == Operation.CREATE) {
						created.add(entry.getKey());
					}
				}
				_operations.clear();
				writeObjects(writer, created);
			}
			_changes.clear();

			writer.endArray();
		}
		writer.endObject();
	}

	private void writeObjects(JsonWriter writer, List<ObjectData> objects) throws IOException {
		for (ObjectData obj : objects) {
			String id = obj.id();

			writer.beginObject();
			{
				writer.name("id");
				writer.value(id);
				writer.name("type");
				writer.value(networkType(obj));
				writer.name("properties");
				writer.beginObject();
				{
					for (Entry<String, Object> value : obj.properties().entrySet()) {
						writer.name(value.getKey());
						JsonUtil.writeValue(writer, value.getValue());
					}
				}
				writer.endObject();
			}
			writer.endObject();
		}
	}

	/**
	 * Loads the initial state of this {@link ObjectScope} from a JSON description read from the
	 * given reader.
	 * 
	 * @param reader
	 *        The JSON reader to read from.
	 * @throws IOException
	 *         If reading failed.
	 */
	public final void readFrom(JsonReader reader) throws IOException {
		startReplay();
		try {
			readObjectData(reader);
			initHandles();
		} finally {
			stopReplay();
		}
		fireEvents();
	}

	private void fireEvents() {
		firePrepareEvent();

		for (ObjectData object : objects()) {
			_listeners.notifyCreate(this, object.handle());
		}

		firePostProcessEvent();
	}

	/**
	 * Hook for subclasses that is called after {@link #readFrom(JsonReader) reading the data} but
	 * before events are fired.
	 */
	protected void initHandles() {
		// Hook for subclasses.
	}

	private void readObjectData(JsonReader reader) throws IOException {
		Map<String, Map<String, Object>> objectProperties = new HashMap<>();

		reader.beginObject();
		while (reader.hasNext()) {
			String property = reader.nextName();
			switch (property) {
				case "objects": {
					reader.beginArray();
					while (reader.hasNext()) {
						String id = null;
						String type = null;
						Map<String, Object> values = new HashMap<>();
						
						reader.beginObject();
						while (reader.hasNext()) {
							String name = reader.nextName();
							switch (name) {
								case "id": {
									id = reader.nextString();
									break;
								}
								case "type": {
									type = reader.nextString();
									break;
								}
								case "properties": {
									JsonUtil.readMap(reader, values);
									break;
								}
								default: {
									throw new IllegalArgumentException("Unexpected property: " + name);
								}
							}
						}
						reader.endObject();
						
						ObjectData obj = obj(id);
						if (obj == null) {
							obj = _factory.createHandle(type, this);
							index(obj, id);
							getIdSource().setLastId(id);
						}

						objectProperties.put(obj.id(), values);
					}
					reader.endArray();
					break;
				}
				
				default: {
					throw new IllegalArgumentException("Unexpected property: " + property);
				}
			}
		}
		reader.endObject();

		// Resolve references.
		for (Map<String, Object> data : objectProperties.values()) {
			for (Entry<String, Object> value : data.entrySet()) {
				value.setValue(resolve(value.getValue()));
			}
		}

		// Update data.
		for (Entry<String, Map<String, Object>> objData : objectProperties.entrySet()) {
			obj(objData.getKey()).constructorProperties(objData.getValue());
		}
		for (Entry<String, Map<String, Object>> objData : objectProperties.entrySet()) {
			obj(objData.getKey()).updateProperties(objData.getValue());
		}
	}

	/**
	 * Resets this {@link ObjectScope} to its initial empty state.
	 */
	public void clear() {
		_objById.clear();
		_operations.clear();
		_changes.clear();
	}

	/**
	 * Adds an observer to this {@link ObjectScope}.
	 * 
	 * @param listener
	 *        The {@link ScopeListener} to add.
	 */
	public void addListener(ScopeListener listener) {
		_listeners.add(listener);
	}

	/**
	 * Adds an observer from this {@link ObjectScope}.
	 * 
	 * @param listener
	 *        The {@link ScopeListener} to remove.
	 * 
	 * @see #addListener(ScopeListener)
	 */
	public void removeListener(ScopeListener listener) {
		_listeners.remove(listener);
	}

	/**
	 * Converts shared application objects to their corresponding {@link ObjectData} containers.
	 * 
	 * @param value
	 *        The original value, either a {@link SharedObject}, a generalized {@link SharedObject},
	 *        or any primitive value.
	 * @return For a {@link SharedObject} or generalized {@link SharedObject} the corresponding
	 *         {@link ObjectData}. For any other value, the unchanged value.
	 * 
	 * @see #data(Object)
	 */
	public final Object toData(Object value) {
		Object data = data(value);
		if (data != null) {
			return data;
		}
		return value;
	}

	/**
	 * Looks up the {@link ObjectData} container for the given {@link SharedObject} or generalized
	 * {@link SharedObject}.
	 * 
	 * <p>
	 * This strategy allows to use generalized {@link SharedObject}s as application objects. A
	 * generalized {@link SharedObject} is an application object with managed instance data that
	 * cannot implement the {@link SharedObject} interface for some reason.
	 * </p>
	 * 
	 * @param value
	 *        The generalized {@link SharedObject} to look up it's {@link ObjectData} for.
	 * @return The {@link ObjectData} associated to the given shared object, or <code>null</code>,
	 *         if the given value is not a shared object.
	 */
	public ObjectData data(Object value) {
		return _dataMapping.data(this, value);
	}

}
