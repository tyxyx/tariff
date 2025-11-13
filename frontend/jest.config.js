const nextJest = require('next/jest');

const createJestConfig = nextJest({
  dir: './',
});

const hasJsdom = (() => {
  try {
    require.resolve('jest-environment-jsdom');
    return true;
  } catch (error) {
    return false;
  }
})();

const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  testEnvironment: hasJsdom ? 'jsdom' : 'node',
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/$1',
    '\\.(css|less|sass|scss)$': '<rootDir>/__mocks__/styleMock.js',
  },
  testPathIgnorePatterns: ['<rootDir>/.next/', '<rootDir>/node_modules/'],
  coveragePathIgnorePatterns: [
    '/node_modules/',
    '/.next/',
    '/coverage/',
    'jest.config.js',
    'jest.setup.js',
    'next.config.mjs',
    '/app/layout.js',
    '/styles/colors.js',
  ],
};

module.exports = createJestConfig(customJestConfig);
