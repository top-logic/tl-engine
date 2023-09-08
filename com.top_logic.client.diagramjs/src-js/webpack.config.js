const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
  entry: {
    bundle: ['./binding/Diagram.js']
  },
  output: {
    path: __dirname + '/../webapp/script',
    filename: 'Diagram.js'
  },
  plugins: [
    new CopyWebpackPlugin([
      { from: '**/*', to: 'css', context: 'assets' },
      { from: '**/*.{html,css}', context: 'binding' }
    ])
  ],
  mode: 'development',
  devtool: 'source-map'
};
