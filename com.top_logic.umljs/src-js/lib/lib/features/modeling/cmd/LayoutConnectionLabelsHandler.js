import inherits from 'inherits';

import { getAbsoluteClosePoint } from '../../../draw/MathUtil';

LayoutConnectionLabelsHandler.$inject = [
  'modeling'
];

export default function LayoutConnectionLabelsHandler(modeling) {
  this._modeling = modeling;
};

LayoutConnectionLabelsHandler.prototype.preExecute = function(context) {
  var modeling = this._modeling;

  setLabels(context.connection);

  function setLabels(connection) {
    moveSourceLabels(modeling, connection);
    moveTargetLabels(modeling, connection);
  };

  function moveSourceLabels(modeling, connection) {
    var sourceLabels = getSourceLabels(connection);
    var existsMarker = existsAnyMarker(connection, 'source');

    var connectionPartSource = connection.waypoints[0];
    var connectionPartTarget = connection.waypoints[1];

    var direction = getStraightLinePartDirection(connectionPartSource, connectionPartTarget);
    var referencePoint = getReferencePoint(connectionPartSource, connectionPartTarget, direction, existsMarker);

    for(var i=0; i<sourceLabels.length; i++) {
      var label = sourceLabels[i];

      fixReferencePoint(direction, referencePoint, label);

      moveShape(modeling, label, referencePoint);

      adjustReferenceLabelPoint(direction, referencePoint, label);
    }
  };

  function moveShape(modeling, shape, point) {
    modeling.moveShape(shape, {
      x: point.x - shape.x,
      y: point.y - shape.y
    });
  };

  function fixReferencePoint(direction, point, label) {
    if(direction === 'left') {
      point.x -= label.width;
    } else if(direction === 'top') {
      point.y -= label.height;
    }
  };

  function adjustReferenceLabelPoint(direction, point, label) {
    if(direction === 'left') {
      point.x -= 5;
    } else if(direction === 'right') {
      point.x += label.width + 5;
    } else if(direction === 'top') {
      point.y -= 5;
    } else if(direction === 'bottom') {
      point.y += label.height + 5;
    }
  };

  function moveTargetLabels(modeling, connection) {
    var targetLabels = getTargetLabels(connection);
    var existsMarker = existsAnyMarker(connection, 'target');

    var connectionPartSource = connection.waypoints[connection.waypoints.length-1];
    var connectionPartTarget = connection.waypoints[connection.waypoints.length-2];

    var direction = getStraightLinePartDirection(connectionPartSource, connectionPartTarget);
    var referencePoint = getReferencePoint(connectionPartSource, connectionPartTarget, direction, existsMarker);

    for(var i=0; i<targetLabels.length; i++) {
      var label = targetLabels[i];

      fixReferencePoint(direction, referencePoint, label);

      moveShape(modeling, label, referencePoint);

      adjustReferenceLabelPoint(direction, referencePoint, label);
    }
  };

  function getReferencePoint(connectionPartSource, connectionPartTarget, direction, existsMarker) {
    var point = getAbsoluteClosePoint(connectionPartSource, connectionPartTarget, getCardinalityDistance(existsMarker));

    if(isHorizontal(direction)) {
      point.y += 5;
    } else {
      point.x += 5;
    }

    return point;
  };

  function getSourceLabels(connection) {
    var sources = connection.labels.filter(function(label) {
      return label.labelType === 'sourceCard' || label.labelType === 'sourceName';
    });

    return sources.sort(function(label1, label2) {
      return label1.labelType.localeCompare(label2.labelType);
    });
  };

  function getTargetLabels(connection) {
    var targets = connection.labels.filter(function(label) {
      return label.labelType === 'targetCard' || label.labelType === 'targetName';
    });

    return targets.sort(function(label1, label2) {
      return label1.labelType.localeCompare(label2.labelType);
    });
  };

  function existsAnyMarker(connection, labelType) {
    var connectionType = connection.connectionType;

    if(labelType === 'source') {
      return connectionType === 'aggregation' || connectionType === 'composition';
    } else {
      return connectionType === 'association' || connectionType === 'inheritance';
    }
  };

  function getStraightLinePartDirection(source, target) {
    if(source.y === target.y) {
      return getHorizontalDirection(source, target);
    } else {
      return getVerticalDirection(source, target);
    }
  };

  function getHorizontalDirection(source, target) {
    if(source.x < target.x) {
      return 'right';
    } else {
      return 'left';
    }
  };

  function getVerticalDirection(source, target) {
    if(source.y < target.y) {
      return 'bottom';
    } else {
      return 'top';
    }
  };

  function isHorizontal(direction) {
    return direction === 'right' || direction === 'left';
  };

  function getCardinalityDistance(existsMarker) {
    if(existsMarker) {
      return 15;
    } else {
      return 5;
    }
  };
};
