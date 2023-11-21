import SelectionModule from 'diagram-js/lib/features/selection';
import MoveModule from 'diagram-js/lib/features/move';
import OutlineModule from 'diagram-js/lib/features/outline';
import LassoToolModule from 'diagram-js/lib/features/lasso-tool';
import PaletteModule from 'diagram-js/lib/features/palette';
import CreateModule from 'diagram-js/lib/features/create';
import ContextPadModule from 'diagram-js/lib/features/context-pad';
import RulesModule from 'diagram-js/lib/features/rules';
import BendpointsModule from 'diagram-js/lib/features/bendpoints';
import LabelSupportModule from 'diagram-js/lib/features/label-support';

import ChangeSupportModule from 'diagram-js/lib/features/change-support';
import ResizeModule from 'diagram-js/lib/features/resize';
import DirectEditingModule from 'diagram-js-direct-editing';

import ConnectModule from './features/connect';
import ConnectionPreviewModule from './features/connection-preview';
import UmlModelingModule from './features/modeling';
import PaletteProviderModule from './features/palette';
import ContextPadProviderModule from './features/context-pad';
import UmlRulesProviderModule from './features/rules-provider';

import Viewer from './Viewer';

import inherits from 'inherits';

inherits(Modeler, Viewer);

export default function Modeler(options) {
  Viewer.call(this, options);
}

Modeler.prototype._modelingModules = [
  SelectionModule,
  MoveModule,
  OutlineModule,
  LassoToolModule,
  PaletteModule,
  CreateModule,
  ContextPadModule,
  ConnectModule,
  RulesModule,
  PaletteProviderModule,
  ContextPadProviderModule,
  UmlRulesProviderModule,
  UmlModelingModule,
  LabelSupportModule,
  ChangeSupportModule,
  ResizeModule,
  DirectEditingModule,
  ConnectionPreviewModule,
  BendpointsModule
];

Modeler.prototype._modules = [].concat(
  Modeler.prototype._modules,
  Modeler.prototype._modelingModules
);
