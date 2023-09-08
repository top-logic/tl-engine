import inherits from 'inherits';

import {
  connectPoints,
  withoutRedundantPoints
} from 'diagram-js/lib/layout/ManhattanLayout';

import { assign } from 'min-dash';

UmlImporter.$inject = ['elementFactory', 'canvas', 'textRenderer', 'modeling', 'layouter', 'commandStack'];

export default function UmlImporter(elementFactory, canvas, textRenderer, modeling, layouter, commandStack) {
  this.textRenderer = textRenderer;
  this.elementFactory = elementFactory;
  this.canvas = canvas;
  this.modeling = modeling;
  this.layouter = layouter;
  this.commandStack = commandStack;
}

UmlImporter.prototype.import = function(graphData) {
  var textRenderer = this.textRenderer;
  var elementFactory = this.elementFactory;
  var canvas = this.canvas;
  var modeling = this.modeling;
  var layouter = this.layouter;
  var commandStack = this.commandStack;

  var root = elementFactory.createRoot();
  canvas.setRootElement(root);

  var nodeMap = createNodes(graphData.graph.nodes, elementFactory, canvas, textRenderer, root, modeling);

  createEdges(graphData.graph.edges, nodeMap, elementFactory, canvas, commandStack, root, textRenderer, modeling);
};

function createNodes(nodes, elementFactory, canvas, textRenderer, parent, modeling) {
  return new Map(nodes.map(function(node) {
    var businessObject = node.businessObject || {};
    var id = node.id;

    return [id, createClass(canvas, parent, elementFactory, node, businessObject, textRenderer, modeling)];
  }));
};

function createClass(canvas, parent, elementFactory, node, businessObject, textRenderer, modeling) {
  delete node.id;

  return modeling.createClass(node, parent);
};

function createEdges(edges, nodeMap, elementFactory, canvas, commandStack, root, textRenderer, modeling) {
  return edges.forEach(function(edge) {
    var attrs = {
      connectionType: edge.type,
      waypoints: getWaypoints(edge, nodeMap)
    };

    assign(attrs, getEdgeLabels(edge));

    var connection = modeling.createConnection(nodeMap.get(edge.source), nodeMap.get(edge.target), attrs, root);
  });
};

function addLabel(labelName, fromObject, toObject) {
  if(labelName in fromObject) {
    toObject[labelName] = fromObject[labelName];
  }
};

function getEdgeLabels(edge) {
  var labels = {};

  addLabel('sourceName', edge, labels);
  addLabel('targetName', edge, labels);
  addLabel('sourceCardinality', edge, labels);
  addLabel('targetCardinality', edge, labels);

  return labels;
};

function getWaypoints(edge, nodeMap) {
  var waypoints;

  if('waypoints' in edge) {
    waypoints = withoutRedundantPoints(edge.waypoints);
  } else {
    waypoints = connectPoints(getRectangleCenterPoint(nodeMap.get(edge.source)), getRectangleCenterPoint(nodeMap.get(edge.target)));
  }

  return roundPoints(waypoints);
};

function getRectangleCenterPoint(point) {
  return {
    x: point.x + point.width / 2,
    y: point.y + point.height / 2
  };
};

function roundPoints(points) {
  points.forEach(function(point) {
    point.x = Math.round(point.x),
    point.y = Math.round(point.y)
  });

  return points;
};
