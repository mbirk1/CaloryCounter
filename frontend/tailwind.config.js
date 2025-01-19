/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/**/*.{js,jsx,ts,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        navigationBackground: '#173083',
        navigationButton: '#451eaf',
        navigationButtonHover: '#13286e',
      },
    },
  },
  plugins: [],
}
