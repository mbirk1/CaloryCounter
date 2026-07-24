/** @type {import('tailwindcss').Config} */

// The brand blue used by the logo (src/app/static/logo.svg). primary/button.primary/navbar.background
// used to be a slightly different, uncoordinated blue (#173083) - they now derive from the same
// brand color so the logo and the rest of the UI are visually consistent.
const brand = '#1E40AF'

module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: brand,
          light: '#EEF2FF',
        },
        primary: {
          DEFAULT: brand,
          dark: '#1E4A7A',
          light: '#E5E9F2',
        },
        secondary: {
          DEFAULT: '#B85C00',
          hover: '#944900',
          light: '#D58033',
        },
        tertiary: {
          DEFAULT: '#146074',
          dark: '#0E4854',
          light: '#1D7D91',
        },
        neutral: {
          white: '#FFFFFF',
          lightGray: '#CCCCCC',
          darkGray: '#333333',
        },
        button: {
          primary: {
            DEFAULT: brand,
            hover: '#1E4A7A',
          },
          secondary: {
            DEFAULT: '#B85C00',
            hover: '#944900',
          },
          tertiary: {
            DEFAULT: '#146074',
            hover: '#0E4854',
          },
          disabled: '#E5E9F2',
        },
        navbar: {
          background: brand,
          link: '#FFFFFF',
          linkHover: '#146074',
        },
      },
    },
  },
  plugins: [],
};
