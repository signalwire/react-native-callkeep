module.exports = {
  watchman: false,
  testMatch: ['<rootDir>/tests/js/**/*.test.js'],
  moduleNameMapper: { '^react-native$': '<rootDir>/tests/js/__mocks__/react-native.js' },
  transform: { '\\.js$': 'babel-jest' },
};
