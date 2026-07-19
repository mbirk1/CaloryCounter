// eslint.config.js
import angular from 'angular-eslint';
import prettierPlugin from 'eslint-plugin-prettier';
import prettierConfig from 'eslint-config-prettier';

const withFiles = (files, configs) => configs.map((c) => ({ ...c, files }));

export default [
  ...withFiles(['**/*.ts'], angular.configs.tsRecommended),
  {
    files: ['**/*.ts'],
    languageOptions: {
      parserOptions: {
        project: ['tsconfig.json'],
        createDefaultProgram: true,
      },
    },
    plugins: {
      prettier: prettierPlugin,
    },
    rules: {
      ...prettierConfig.rules,
      'prettier/prettier': 'error',
    },
  },
  ...withFiles(['**/*.html'], angular.configs.templateRecommended),
  {
    ignores: ['node_modules', 'dist', 'build'],
  },
];
