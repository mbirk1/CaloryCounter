// eslint.config.mjs
import { FlatCompat } from '@eslint/eslintrc';

const compat = new FlatCompat({
  baseDirectory: import.meta.url,
});

export default [
  ...compat.config({
    overrides: [
      {
        files: ['*.ts'],
        extends: [
          'plugin:@angular-eslint/recommended',
          'plugin:prettier/recommended',
        ],
        parserOptions: {
          project: ['tsconfig.json'],
          createDefaultProgram: true,
        },
        rules: {
          'prettier/prettier': 'error',
        },
      },
      {
        files: ['*.html'],
        extends: ['plugin:@angular-eslint/template/recommended'],
        rules: {},
      },
    ],
  }),
  {
    ignores: ['node_modules', 'dist', 'build'],
  },
];
