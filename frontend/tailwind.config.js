/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [ "./src/**/*.{html,ts}",],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#173083',
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
            DEFAULT: '#173083',
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
          background: '#173083',
          link: '#FFFFFF',
          linkHover: '#146074',
        },
      },
    },
  },
  plugins: [],
}

