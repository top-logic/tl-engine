import inherits from 'inherits';

import UpdateLabelHandler from '../label-editing/cmd/UpdateLabelHandler';
import LayoutConnectionLabelsHandler from './cmd/LayoutConnectionLabelsHandler';
import VisibilityHandler from './cmd/VisibilityHandler';

import { isShape, isLabel, isConnection } from '../../util/ModelUtil';

import BaseModeling from 'diagram-js/lib/features/modeling/Modeling';

import { assign } from 'min-dash';

inherits(Modeling, BaseModeling);

Modeling.$inject = [
  'eventBus',
  'elementFactory',
  'elementRegistry',
  'commandStack',
  'textRenderer',
  'canvas',
  'selection',
  'graphicsFactory',
  'layouter'
];

export default function Modeling(eventBus, elementFactory, elementRegistry, commandStack, textRenderer, canvas, selection, graphicsFactory, layouter) {
  this.canvas = canvas;
  this._textRenderer = textRenderer;
  this._graphicsFactory = graphicsFactory;
  this._elementRegistry = elementRegistry;
  this._selection = selection;
  this._layouter = layouter;

  BaseModeling.call(this, eventBus, elementFactory, commandStack);
}

Modeling.prototype.getHandlers = function() {
  var handlers = BaseModeling.prototype.getHandlers.call(this);

  handlers['element.updateLabel'] = UpdateLabelHandler;
  handlers['layout.connection.labels'] = LayoutConnectionLabelsHandler;
  handlers['elements.visibility'] = VisibilityHandler;

  return handlers;
};

Modeling.prototype.updateLabel = function(element, newLabel) {
  this._commandStack.execute('element.updateLabel', {
    element: element,
    newLabel: newLabel
  });
};

Modeling.prototype.connect = function(source, target, connectionType, attrs, hints) {
  return this.createConnection(source, target, assign({
    connectionType: connectionType
  }, attrs), source.parent);
};

Modeling.prototype.toggleVisibility = function(element, isVisible) {
  this.handleVisibilityContext = {
    elements: [],
    visibility: isVisible
  };
  
  if(isVisible) {
    this.showHiddenParents(element);
  }

  this.setVisibility(element, isVisible);
  
  if(isConnection(element)) {
    if(isVisible) {
      this._setVisibility(element.source, true);
      this.updateGraphics(element.source, true);
      this._setVisibility(element.target, true);
      this.updateGraphics(element.target, true);
    }
  }
  
  this.handleVisibilityContext.elements.forEach(element => {
    if(element.hidden) {
      this._selection.deselect(element);
    }
  });

  this._commandStack.execute('elements.visibility', this.handleVisibilityContext);
};

Modeling.prototype.showHiddenParents = function(element) {
    var parent = element.parent;
    
    if(!parent.isVisible) {
      if(isConnection(parent)) {
        this._setVisibility( parent.source, true);
        this.updateGraphics(parent.source, true);
        this._setVisibility(parent.target, true);
        this.updateGraphics(parent.target, true);
      }
      
      this._setVisibility( parent, true);
      this.updateGraphics(parent, true);
    }
}

Modeling.prototype.setVisibility = function(element, isVisible) {
  this._setVisibility(element, isVisible);
  
  element.hidden = !isVisible && !this._layouter.showHiddenElements;
  
  if(isShape(element)) {
    if(isLabel(element)) {
      this.setLabelVisibility(element, isVisible);
    } else {
      this.setShapeVisibility(element, isVisible);
    }
  } else {
    this.setConnectionVisibility(element, isVisible);
  }
};

Modeling.prototype._setVisibility = function(element, isVisible) {
  if(element.isVisible !== isVisible) {
    if(!this.handleVisibilityContext.elements.includes(element)) {
      this.handleVisibilityContext.elements.push(element);
    }
    
    element.isVisible = isVisible;
  }
};

Modeling.prototype.setLabelVisibility = function(label, visibility) {
  this.setChildrenVisibility(label, visibility);
  
  this.updateGraphics(label, false);
};

Modeling.prototype.setConnectionVisibility = function(connection, isVisible) {
  this.setChildrenVisibility(connection, isVisible);

  this.updateGraphics(connection, false);
};

Modeling.prototype.setShapeVisibility = function(shape, visibility) {
  this.setAttachedEdgesVisibility(shape, visibility);
  this.setChildrenVisibility(shape, visibility);

  this.updateGraphics(shape, false);
};

Modeling.prototype.setAttachedEdgesVisibility = function(shape, visibility) {
  this.setIncomingAttachedEdgesVisibility(shape, visibility);
  this.setOutgoingAttachedEdgesVisibility(shape, visibility);
};

Modeling.prototype.setIncomingAttachedEdgesVisibility = function(shape, isVisible) {
  var modeling = this;

  if('incoming' in shape) {
    shape.incoming.forEach(function(edge) {
      if(isVisible) {
        if(edge.source.isVisible) {
          modeling.setVisibility(edge, isVisible);
        }
      } else {
        modeling.setVisibility(edge, isVisible);
      }
    });
  }
};

Modeling.prototype.setOutgoingAttachedEdgesVisibility = function(shape, isVisible) {
  var modeling = this;

  if('outgoing' in shape) {
    shape.outgoing.forEach(function(edge) {
      if(isVisible) {
        if(edge.target.isVisible) {
          modeling.setVisibility(edge, isVisible);
        }
      } else {
        modeling.setVisibility(edge, isVisible);
      }
    });
  }
};

Modeling.prototype.setChildrenVisibility = function(element, visibility) {
  var modeling = this;

  if('children' in element) {
    element.children.forEach(function(child) {
      modeling.setVisibility(child, visibility);
    });
  }
};

Modeling.prototype.updateGraphics = function(element, updateChildren) {
  if(isConnection(element)) {
    this.updateGfx(element, 'connection');
  } else {
    this.updateGfx(element, 'shape');
  }
  
  if(updateChildren) {
    this.updateGraphicChildren(element);
  }
};

Modeling.prototype.updateGraphicChildren = function(element) {
  var modeling = this;
  
  if('children' in element) {
	element.children.forEach(function(child) {
	  modeling.updateGraphics(child, false);
	});
  }
};

Modeling.prototype.updateGfx = function(element, type) {
  var gfx = this._elementRegistry.getGraphics(element, false);

  this._graphicsFactory.update(type, element, gfx);
};
