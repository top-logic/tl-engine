/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.UnmodifiableIterator;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.I18NConstants;
import com.top_logic.element.meta.form.overlay.DefaultObjectConstructor;
import com.top_logic.element.meta.form.overlay.FormObjectOverlay;
import com.top_logic.element.meta.form.overlay.ObjectConstructor;
import com.top_logic.element.meta.form.overlay.ObjectCreation;
import com.top_logic.element.meta.form.overlay.ObjectEditing;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.mig.html.Media;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.util.Resources;

/**
 * Container for attribute updates. May contain - simple values for SimpleMetaAttributes -
 * add/remove/set specifications for CollectionMetaAttributes - search range specifications for
 * searches (simple, from/to, collections of possible values)
 *
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class AttributeUpdateContainer {
	/** Separates IDs of attribute and object. */
	public static final String ID_SEPARATOR = "_";

	private static final int FIRST_ID = 1;

	private AttributeFormContext _form;

	final Map<TLObject, ObjectEditing> _edits;

	final Map<String, ObjectCreation> _creates;

	private int _nextObjId = FIRST_ID;

	private int _nextCreateId = FIRST_ID;

	/**
	 * Default CTor. Set up fields.
	 * 
	 * @param form
	 *        The {@link AttributeFormContext} containing UI values.
	 */
	public AttributeUpdateContainer(AttributeFormContext form) {
		_form = form;
		_edits = new HashMap<>();
		_creates = new HashMap<>();
	}

	/**
	 * The associated {@link AttributeFormContext} containing the input fields.
	 */
	public AttributeFormContext getFormContext() {
		return _form;
	}

	/**
	 * Where the form is written to.
	 */
	public Media getOutputMedia() {
		return _form.getOutputMedia();
	}

	/**
	 * Get the update for an attribute of an object.
	 *
	 * @param attribute
	 *        the attribute. Must no be <code>null</code>.
	 * @param object
	 *        the object. Must no be <code>null</code>.
	 * @return the AttributeUpdate or <code>null</code> if there's no update available. Remark: with
	 *         this definition an update with a value of <code>null</code> can be differentiated
	 *         from no update.
	 */
	public final AttributeUpdate getAttributeUpdate(TLStructuredTypePart attribute, TLObject object) {
		return getAttributeUpdate(attribute, object, null);
    }

	/**
	 * Get the update for an attribute of an object.
	 *
	 * @param attribute
	 *        the attribute. Must no be <code>null</code>.
	 * @param object
	 *        The object. May be be <code>null</code> for a create update.
	 * @param domain
	 *        See {@link AttributeUpdate#getDomain()}.
	 * @return the AttributeUpdate or <code>null</code> if there's no update available. Remark: with
	 *         this definition an update with a value of <code>null</code> can be differentiated
	 *         from no update.
	 */
	public AttributeUpdate getAttributeUpdate(TLStructuredTypePart attribute, TLObject object, String domain) {
		return getUpdate(overlay(object, domain), attribute);
	}

	/**
	 * Requests an object creation.
	 * 
	 * <p>
	 * Note: If a creation with the same parameters (type and domain) was already requested before,
	 * the existing instance is returned.
	 * </p>
	 * 
	 * @param type
	 *        The type of object to create.
	 * @param domain
	 *        The identifier of the object being created. See {@link TLFormObject#getDomain()}.
	 * @return A transient overlay object. The object represents the current values currently being
	 *         edited in the form. Through the {@link UpdateFactory} aspect, it allows to create
	 *         {@link AttributeUpdate}s for its attributes to display.
	 * 
	 * @see #newObject(TLStructuredType)
	 */
	public TLFormObject createObject(TLStructuredType type, String domain) {
		return createObject(type, domain, null);
	}

	/**
	 * Requests an object creation in a containment context.
	 * 
	 * <p>
	 * Note: If a creation with the same parameters (type and domain) was already requested before,
	 * the existing instance is returned.
	 * </p>
	 * 
	 * @param type
	 *        The type of object to create.
	 * @param domain
	 *        The identifier of the object being created. See {@link TLFormObject#getDomain()}.
	 * @param container
	 *        The object owning the the object being created.
	 * @return A transient overlay object. The object represents the current values currently being
	 *         edited in the form. Through the {@link UpdateFactory} aspect, it allows to create
	 *         {@link AttributeUpdate}s for its attributes to display.
	 * 
	 * @see #newObject(TLStructuredType, TLObject)
	 */
	public TLFormObject createObject(TLStructuredType type, String domain, TLObject container) {
		return createObject(type, domain, container, DefaultObjectConstructor.INSTANCE);
	}

	/**
	 * Requests an object creation in a containment context.
	 * 
	 * <p>
	 * Note: If a creation with the same parameters (type and domain) was already requested before,
	 * the existing instance is returned.
	 * </p>
	 * 
	 * @param type
	 *        The type of object to create.
	 * @param domain
	 *        The identifier of the object being created. See {@link TLFormObject#getDomain()}.
	 * @param container
	 *        The object owning the the object being created.
	 * @param constructor
	 *        A custom constructor function.
	 * @return A transient overlay object. The object represents the current values currently being
	 *         edited in the form. Through the {@link UpdateFactory} aspect, it allows to create
	 *         {@link AttributeUpdate}s for its attributes to display.
	 * 
	 * @see #newObject(TLStructuredType, TLObject, ObjectConstructor)
	 */
	public TLFormObject createObject(TLStructuredType type, String domain, TLObject container,
			ObjectConstructor constructor) {
		ObjectCreation existingOverlay = _creates.get(domain);
		if (existingOverlay != null) {
			return existingOverlay;
		}

		return allocateCreateOverlay(type, domain, container, constructor);
	}

	/**
	 * Allocates a new object creation.
	 * 
	 * <p>
	 * Note: In contrast to the {@link #createObject(TLStructuredType, String)} signatures with
	 * domain, each call creates a new object (never retrieves a creation requested before).
	 * </p>
	 *
	 * @param type
	 *        The type of object to create.
	 * @return A transient overlay object. The object represents the current values currently being
	 *         edited in the form. Through the {@link UpdateFactory} aspect, it allows to create
	 *         {@link AttributeUpdate}s for its attributes to display.
	 * 
	 * @see #createObject(TLStructuredType, String)
	 */
	public TLFormObject newObject(TLStructuredType type) {
		return newObject(type, null);
	}

	/**
	 * Allocates a new object creation in a containment context.
	 * 
	 * <p>
	 * Note: In contrast to the {@link #createObject(TLStructuredType, String)} signatures with
	 * domain, each call creates a new object (never retrieves a creation requested before).
	 * </p>
	 *
	 * @param type
	 *        The type of object to create.
	 * @param container
	 *        The object owning the the object being created.
	 * @return A transient overlay object. The object represents the current values currently being
	 *         edited in the form. Through the {@link UpdateFactory} aspect, it allows to create
	 *         {@link AttributeUpdate}s for its attributes to display.
	 * 
	 * @see #createObject(TLStructuredType, String, TLObject)
	 */
	public TLFormObject newObject(TLStructuredType type, TLObject container) {
		return newObject(type, container, DefaultObjectConstructor.INSTANCE);
	}

	/**
	 * Allocates a new object creation in a containment context.
	 * 
	 * <p>
	 * Note: In contrast to the {@link #createObject(TLStructuredType, String)} signatures with
	 * domain, each call creates a new object (never retrieves a creation requested before).
	 * </p>
	 *
	 * @param type
	 *        The type of object to create.
	 * @param container
	 *        The object owning the the object being created.
	 * @param constructor
	 *        A custom constructor function.
	 * @return A transient overlay object. The object represents the current values currently being
	 *         edited in the form. Through the {@link UpdateFactory} aspect, it allows to create
	 *         {@link AttributeUpdate}s for its attributes to display.
	 * 
	 * @see #createObject(TLStructuredType, String, TLObject, ObjectConstructor)
	 */
	public TLFormObject newObject(TLStructuredType type, TLObject container, ObjectConstructor constructor) {
		while (true) {
			String domain = newCreateID();
			if (_creates.get(domain) != null) {
				continue;
			}

			return allocateCreateOverlay(type, domain, container, constructor);
		}
	}

	/**
	 * Creates a new local object ID for usage in forms.
	 */
	public String newObjectID() {
		return "obj_" + Integer.toString(_nextObjId++);
	}

	/**
	 * Creates a new local create object ID for usage in forms.
	 * 
	 * <p>
	 * Note: The IDs are separate from object IDs to keep compatibility with recorded test scripts.
	 * </p>
	 */
	public String newCreateID() {
		return "create_" + Integer.toString(_nextCreateId++);
	}

	private TLFormObject allocateCreateOverlay(TLStructuredType type, String domain, TLObject container,
			ObjectConstructor constructor) {
		ObjectCreation newOverlay = new ObjectCreation(this, type, domain, constructor);
		_creates.put(domain, newOverlay);
		newOverlay.initContainer(container);

		TLFactory.setupDefaultValues(container, newOverlay, type);

		return newOverlay;
	}

	/**
	 * Requests an object edit operation.
	 * 
	 * @param object
	 *        The object to edit.
	 * @return A transient overlay object. The object represents the current values currently being
	 *         edited in the form. Through the {@link UpdateFactory} aspect, it allows to create
	 *         {@link AttributeUpdate}s for its attributes to display.
	 */
	public TLFormObject editObject(TLObject object) {
		if (object instanceof TLFormObject) {
			return (TLFormObject) object;
		}

		FormObjectOverlay existingOverlay = _edits.get(object);
		if (existingOverlay != null) {
			return existingOverlay;
		}
		
		ObjectEditing newOverlay = new ObjectEditing(this, object);
		_edits.put(object, newOverlay);
		return newOverlay;
	}

	/**
	 * The transient form overlay object that contains the value represented by the given
	 * {@link AttributeUpdate}.
	 */
	public TLFormObject getOverlay(AttributeUpdate update) {
		return overlay(update.getObject(), update.getDomain());
	}

	/**
	 * Cancels an edit or create operation for the given overlay.
	 * 
	 * <p>
	 * Note: This removes all associated fields in the corresponding {@link #getFormContext()}.
	 * </p>
	 *
	 * @param overlay
	 *        The overlay created with {@link #createObject(TLStructuredType, String)} or
	 *        {@link #editObject(TLObject)}.
	 * 
	 * @see #clear()
	 */
	public void removeOverlay(TLFormObject overlay) {
		checkScope(overlay);

		dropFields(overlay);

		if (overlay.getEditedObject() != null) {
			_edits.remove(overlay.getEditedObject());
		} else {
			_creates.remove(overlay.getDomain());
		}
	}

	/**
	 * Removes all overlays.
	 * 
	 * @see #removeOverlay(TLFormObject)
	 */
	public void clear() {
		for (FormObjectOverlay overlay : allOverlays()) {
			dropFields(overlay);
		}

		_edits.clear();
		_creates.clear();
		_nextObjId = FIRST_ID;
		_nextCreateId = FIRST_ID;
	}

	private void dropFields(TLFormObject overlay) {
		for (AttributeUpdate update : overlay.getUpdates()) {
			FormMember field = update.getField();
			if (field != null) {
				field.getParent().removeMember(field);
			}
		}

		FormContainer container = overlay.getFormContainer();
		if (container != null) {
			FormContainer parent = container.getParent();
			if (parent != null) {
				parent.removeMember(container);
			}
		}
	}

	/**
	 * The transient form overlay object that contains the value represented by the given
	 * {@link AttributeUpdate}.
	 */
	public TLFormObject getOverlay(TLObject object, String domain) {
		return overlay(object, domain);
	}

	private FormObjectOverlay overlay(TLObject object) {
		if (object == null) {
			return null;
		}
		if (object instanceof FormObjectOverlay) {
			return ((FormObjectOverlay) object);
		} else {
			return _edits.get(object);
		}
	}

	private FormObjectOverlay overlay(TLObject object, String domain) {
		if (object instanceof FormObjectOverlay) {
			return (FormObjectOverlay) object;
		}
		FormObjectOverlay overlay;
		if (object == null) {
			overlay = _creates.get(domain);
		} else {
			assert domain == null;
			overlay = _edits.get(object);
		}
		return overlay;
	}

	private static AttributeUpdate getUpdate(FormObjectOverlay object, TLStructuredTypePart attribute) {
		if (object == null) {
			return null;
		}
		return object.getUpdate(attribute);
	}

	/**
	 * Remove the update for an attribute of an object and return it.
	 *
	 * @param attribute
	 *        the attribute. Must no be <code>null</code>.
	 * @param object
	 *        the object. Must no be <code>null</code>.
	 * @return the AttributeUpdate or <code>null</code> if there's no update available.
	 */
	public final AttributeUpdate removeAttributeUpdate(TLStructuredTypePart attribute, TLObject object) {
		return removeAttributeUpdate(attribute, object, null);
    }

	/**
	 * Removes all {@link AttributeUpdate}s for the given object.
	 *
	 * @param object
	 *        The base object being edited.
	 * 
	 * @return The form overlay object that was removed from this container, or <code>null</code> if
	 *         no such object existed before.
	 */
	public TLObject removeAllUpdatesFor(TLObject object) {
		FormObjectOverlay overlay = overlay(object);
		if (overlay != null) {
			TLObject baseObject = overlay.getEditedObject();
			if (baseObject != null) {
				return _edits.remove(baseObject);
			} else {
				return _creates.remove(overlay.getDomain());
			}
		} else {
			return null;
		}
	}

	/**
	 * Removes all {@link AttributeUpdate}s for the given object.
	 *
	 * @param object
	 *        The base object being edited.
	 * @param domain
	 *        The identifier of the object being created.
	 * 
	 * @return The form overlay object that was removed from this container, or <code>null</code> if
	 *         no such object existed before.
	 */
	public TLObject removeAllUpdatesFor(TLObject object, String domain) {
		if (object == null) {
			return _creates.remove(domain);
		} else {
			assert domain == null;
			return _edits.remove(object);
		}
	}

	/**
	 * Remove the update for an attribute of an object and return it.
	 *
	 * @param attribute
	 *        the attribute. Must no be <code>null</code>.
	 * @param object
	 *        The object. May be <code>null</code> for a create update.
	 * @param domain
	 *        See {@link AttributeUpdate#getDomain()}.
	 * @return the AttributeUpdate or <code>null</code> if there's no update available.
	 */
	public AttributeUpdate removeAttributeUpdate(TLStructuredTypePart attribute, TLObject object, String domain) {
		if (object == null) {
			return removeUpdate(_creates.get(domain), attribute);
		} else {
			assert domain == null;
			return removeUpdate(_edits.get(object), attribute);
		}
    }

	/**
	 * Removes the given {@link AttributeUpdate} from this container.
	 */
	public AttributeUpdate removeAttributeUpdate(AttributeUpdate update) {
		TLFormObject overlay = update.getOverlay();
		checkScope(overlay);
		removeUpdate(overlay, update.getAttribute());
		return update;
	}

	private void checkScope(TLFormObject overlay) {
		if (overlay.getScope() != this) {
			throw new IllegalArgumentException("Not an update of this container.");
		}
	}

	private static AttributeUpdate removeUpdate(TLFormObject overlay, TLStructuredTypePart attribute) {
		if (overlay == null) {
			return null;
		}
		return overlay.removeUpdate(attribute);
	}

	/**
	 * All {@link AttributeUpdate}s of the given overlay object.
	 * 
	 * @param object
	 *        A form overlay object, see {@link #getOverlay(TLObject, String)}.
	 * 
	 * @return All {@link AttributeUpdate}s belonging to the given overlay object.
	 */
	public Iterable<AttributeUpdate> getAllUpdates(TLObject object) {
		FormObjectOverlay overlay = overlay(object);
		if (overlay == null) {
			return null;
		}
		return overlay.getUpdates();
	}

	/**
	 * All {@link TLFormObject}s in this {@link AttributeUpdateContainer}.
	 */
	public final Iterable<? extends TLFormObject> getAllOverlays() {
		return allOverlays();
	}

	private Iterable<FormObjectOverlay> allOverlays() {
		return new Iterable<>() {
			@Override
			public Iterator<FormObjectOverlay> iterator() {
				return overlayIterator();
			}
		};
	}

	final Iterator<FormObjectOverlay> overlayIterator() {
		return new UnmodifiableIterator<>() {

			Iterator<ObjectCreation> _createIt = _creates.values().iterator();

			Iterator<ObjectEditing> _editIt = _edits.values().iterator();

			FormObjectOverlay _next;

			@Override
			public boolean hasNext() {
				while (_next == null) {
					if (_createIt.hasNext()) {
						_next = _createIt.next();
					} else if (_editIt.hasNext()) {
						_next = _editIt.next();
					} else {
						return false;
					}
				}
				return true;
			}

			@Override
			public FormObjectOverlay next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				FormObjectOverlay result = _next;
				_next = null;
				return result;
			}
		};
	}

	/**
	 * All {@link AttributeUpdate}s of this container.
	 */
	public Iterable<AttributeUpdate> getAllUpdates() {
		return new Iterable<>() {
			@Override
			public Iterator<AttributeUpdate> iterator() {
				return new UnmodifiableIterator<>() {

					Iterator<FormObjectOverlay> _overlays = overlayIterator();
					Iterator<AttributeUpdate> _updates;

					@Override
					public boolean hasNext() {
						while (true) {
							if (_updates == null) {
								FormObjectOverlay nextOverlay;
								if (_overlays.hasNext()) {
									nextOverlay = _overlays.next();
								} else {
									return false;
								}
								_updates = nextOverlay.getUpdates().iterator();
							}
							if (_updates.hasNext()) {
								return true;
							} else {
								_updates = null;
							}
						}
					}

					@Override
					public AttributeUpdate next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						return _updates.next();
					}
				};
			}
		};
	}

	/**
	 * Transfers values from the form to the {@link AttributeUpdate}s.
	 */
	public void update() {
		for (AttributeUpdate update : getAllUpdates()) {
			update.update();
		}
	}

    /**
	 * {@link AttributeUpdate#checkUpdate() Checks} all updates and show potential problems at
	 * associated form fields.
	 * 
	 * @return Whether all updates are OK.
	 */
	public boolean check() {
		boolean ok = true;
		for (AttributeUpdate update : getAllUpdates()) {
			try {
				update.checkUpdate();
			} catch (I18NRuntimeException ex) {
				FormMember field = update.getField();
				if (field instanceof FormField) {
					((FormField) field).setError(
						Resources.getInstance().getString(
							I18NConstants.CONSTRAINT_FAILURE__ATTRIBUTE_MESSAGE
								.fill(update.getLabelKey(), ex.getErrorKey())));
					ok = false;
				}
			}
		}
		return ok;
	}

	/**
	 * Store all {@link AttributeUpdate}s in this container.
	 * 
	 * @see AttributeUpdate#store()
	 */
    public void store() {
		for (ObjectCreation create : _creates.values()) {
			create.create();
		}
		for (FormObjectOverlay overlay : allOverlays()) {
			overlay.store(this);
		}
    }

	/**
	 * @deprecated Use {@link #store()}
	 */
	@Deprecated
	public void performUpdate() {
		store();
	}

	/**
	 * Adds a value listener to the form field displaying the given object's property.
	 */
	public Handle addValueListener(TLObject object, TLStructuredTypePart attribute, ValueListener listener) {
		return new Handle() {
			private FormField _field;

			private boolean _released;

			Handle install() {
				TLFormObject overlay = editObject(object);
				overlay.withUpdate(attribute, u -> {
					if (_released) {
						return;
					}
					u.withField(f -> {
						if (_released) {
							return;
						}

						if (f instanceof FormField) {
							_field = (FormField) f;
							_field.addValueListener(listener);
						}
					});
				});

				return this;
			}

			@Override
			public void release() {
				if (_field != null) {
					_field.removeValueListener(listener);
				}
				_released = true;
			}
		}.install();
	}

	/**
	 * Handle that allows to release a registration done before.
	 * 
	 * @see AttributeUpdateContainer#addValueListener(TLObject, TLStructuredTypePart, ValueListener)
	 */
	public interface Handle {
		/**
		 * Releases the former registration.
		 */
		void release();
	}
}
