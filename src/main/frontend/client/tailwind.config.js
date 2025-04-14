/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          light: '#4A90E2',
          DEFAULT: '#2563EB',
          dark: '#1E40AF',
        },
        secondary: {
          light: '#9CA3AF',
          DEFAULT: '#6B7280',
          dark: '#4B5563',
        },
      },
    },
  },
  plugins: [],
} 