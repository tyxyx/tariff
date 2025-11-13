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
    'app/layout\\.js$',
    'styles/colors\\.js$',
    // Exclude page routes without tests (routing/layout pages)
    'app/page\\.js$',
    'app/login/page\\.js$',
    'app/signup/page\\.js$',
    'app/dashboard/page\\.js$',
    'app/dashboard-admin/page\\.js$',
    'app/profile/page\\.js$',
    'app/crud/page\\.js$',
    'app/data/page\\.js$',
    // Exclude simple UI components without logic
    'components/ui/header\\.js$',
    'components/ui/input\\.js$',
    'components/ui/PageHeader\\.js$',
    // Exclude utility/API wrapper files
    'utils/apiClient\\.js$',
    'utils/useApiData\\.js$',
    // Exclude admin-specific components
    'components/confirm-dialog\\.js$',
    'components/user-table\\.js$',
  ],
};

module.exports = createJestConfig(customJestConfig);
