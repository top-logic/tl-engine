import CommandInterceptor from 'diagram-js/lib/command/CommandInterceptor';

import { Label } from 'diagram-js/lib/model';

import inherits from 'inherits';

inherits(UmlUpdater, CommandInterceptor);

UmlUpdater.$inject = [
  'eventBus', 'connectionDocking', 'commandStack'
];

export default function UmlUpdater(eventBus, connectionDocking, commandStack) {
  CommandInterceptor.call(this, eventBus);

  var self = this;

  function cropConnection(e) {
    var context = e.context,
    connection;

    if (!context.cropped) {
      connection = context.connection;
      connection.waypoints = connectionDocking.getCroppedWaypoints(connection);
      context.cropped = true;
    }
  }

  this.executed([
    'connection.layout',
    'connection.create',
    'connection.reconnectEnd',
    'connection.reconnectStart'
  ], cropConnection);

  this.postExecuted([
    'connection.layout',
    'connection.reconnectEnd',
    'connection.reconnectStart',
    'connection.updateWaypoints',
    'connection.move'
  ], function(event) {
    commandStack.execute('layout.connection.labels', event.context);
  });

  this.reverted([ 'connection.layout' ], function(event) {
    delete event.context.cropped;
  });

  eventBus.on('element.hover', 1500, function(event) {
    if(event.element instanceof Label && event.element.labelType === 'classifier') {
      return false;
    }
  });

  var delegatedClassifierEvents = ['element.click', 'element.mousedown', 'element.mouseup', 'element.contextmenu', 'element.dblclick' ]

  eventBus.on(delegatedClassifierEvents, 1500, function(event) {
    if(event.element instanceof Label && event.element.labelType === 'classifier') {
      event.stopPropagation();

      eventBus.fire(event.type, {
        element: event.element.parent,
        originalEvent: event.originalEvent,
        srcEvent: event.srcEvent
      });
    }
  });
}
