import BaseElementFactory from 'diagram-js/lib/core/ElementFactory';

import { assign } from 'min-dash';

import inherits from 'inherits';

inherits(ElementFactory, BaseElementFactory);

export default function ElementFactory() {
  BaseElementFactory.call(this);
}

ElementFactory.prototype.createEnumeration = function(attrs) {
  var newAttrs = {
    shapeType: 'enumeration',
    businessObject: {}
  };

  assign(newAttrs, attrs);

  return this.createShape(newAttrs);
};

ElementFactory.prototype.createClass = function(attrs) {
  var newAttrs = {
    shapeType: 'class',
    businessObject: {}
  };

  assign(newAttrs, attrs);

  return this.createShape(newAttrs);
};

ElementFactory.prototype.createUmlLabel = function(attrs) {
  var newAttrs = {
    shapeType: 'label'
  };

  assign(newAttrs, attrs);

  return this.createLabel(newAttrs);
};

ElementFactory.prototype.createInheritance = function(attrs) {
  var newAttrs = {
    connectionType: 'inheritance'
  };

  assign(newAttrs, attrs);

  return this.createConnection(newAttrs);
}

ElementFactory.prototype.createAssociation = function(attrs) {
  var newAttrs = {
    connectionType: 'association'
  };

  assign(newAttrs, attrs);

  return this.createConnection(newAttrs);
}

ElementFactory.prototype.createAggregation = function(attrs) {
  var newAttrs = {
    connectionType: 'aggregation'
  };

  assign(newAttrs, attrs);

  return this.createConnection(newAttrs);
}

ElementFactory.prototype.createComposition = function(attrs) {
  var newAttrs = {
    connectionType: 'composition'
  };

  assign(newAttrs, attrs);

  return this.createConnection(newAttrs);
}

ElementFactory.prototype.createDependency = function(attrs) {
  var newAttrs = {
    connectionType: 'dependency'
  };

  assign(newAttrs, attrs);

  return this.createConnection(newAttrs);
}

ElementFactory.prototype.createRealization = function(attrs) {
  var newAttrs = {
    connectionType: 'realization'
  };

  assign(newAttrs, attrs);

  return this.createConnection(newAttrs);
}
