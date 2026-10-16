// Used only by the Jest tests (tests/js); apps compile the package with their own Metro/Babel config.
module.exports = {
  presets: [['@babel/preset-env', { targets: { node: 'current' } }]],
  plugins: ['@babel/plugin-transform-class-properties'],
};
