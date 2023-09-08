import UmlRenderer from './UmlRenderer';
import TextRenderer from './TextRenderer'

export default {
  __init__: [ 'umlRenderer'],
  umlRenderer: ['type', UmlRenderer],
  textRenderer: [ 'type', TextRenderer]
};
