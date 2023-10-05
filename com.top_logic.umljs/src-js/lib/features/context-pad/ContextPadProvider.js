import { isShape, isLabel } from '../../util/ModelUtil';

import { assign } from 'min-dash';

ContextPadProvider.$inject = [
  'connect',
  'contextPad',
  'modeling',
  'eventBus'
];

export default function ContextPadProvider(connect, contextPad, modeling, eventBus) {
  this._connect = connect;
  this._modeling = modeling;
  this._eventBus = eventBus;
  this._contextPad = contextPad;

  contextPad.registerProvider(this);
}

ContextPadProvider.prototype.getContextPadEntries = function(element) {
  var modeling = this._modeling,
      connect = this._connect,
      eventBus = this._eventBus,
      contextPad = this._contextPad;

  function removeElement(event, element) {
    eventBus.fire('delete.element', {
      context: {
        element: element
      }
    });
  }

  function startConnect(event, element, type) {
    connect.start(event, element, type);
  }

  function addProperty(event, element) {
    assign(event, {
      context: {
        shape: element
      }
    });

    eventBus.fire('create.class.property', event);
  }

  function hide(event, element) {
    modeling.hide(element);

    contextPad.close();
  }

  function getConnectionPadEntry(type, title) {
    return  {
      group: 'add',
      className: 'context-pad-icon-' + type,
      title: title,
      action: {
        click: function(event, element) {
          startConnect(event, element, type);
        },
        dragstart: function(event, element) {
          startConnect(event, element, type);
        }
      }
    }
  }

  function getRemoveShapePadEntry() {
    return {
      group: 'admin',
      className: 'context-pad-icon-delete',
      title: 'Remove',
      action: {
        click: removeElement,
        dragstart: removeElement
      }
    };
  }

  function getCreatePropertyPadEntry() {
    return {
      group: 'add',
      className: 'context-pad-icon-create-property',
      title: 'Create Property',
      action: {
        click: addProperty
      }
    };
  }

  function getHidePadEntry() {
    return {
      group: 'admin',
      className: 'context-pad-icon-hide',
      title: 'Hide Part',
      action: {
        click: hide
      }
    };
  }

  function getGoToPadEntry() {
    return {
      group: 'admin',
      className: 'context-pad-icon-goto',
      title: 'Go to',
      action: {
        click: function(event, element) {
          eventBus.fire('element.goto', {
            context: {
              element: element
            }
          });
        }
      }
    };
  }

  var contextPadEntries = {
    'delete': getRemoveShapePadEntry(),
    'hide': getHidePadEntry()
  };

  if(isShape(element) && !isLabel(element)) {
    if(!('stereotypes' in element && element.stereotypes.indexOf('enumeration') != -1)) {
      assign(contextPadEntries, {
        'inheritance-connection': getConnectionPadEntry('inheritance', 'Inheritance Connection'),
        'composition-connection': getConnectionPadEntry('composition', 'Composition Connection'),
        'association-connection': getConnectionPadEntry('association', 'Association Connection'),
        'create-property': getCreatePropertyPadEntry()
      });
    }

    if(element.isImported) {
      assign(contextPadEntries, {
        'goto': getGoToPadEntry()
      });
    }
  }

  return contextPadEntries;
};
