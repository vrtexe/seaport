/** @type {import('tailwindcss').Config} */
export default {
    content: ['./src/**/*.{html,js,svelte,ts}'],
    theme: {
        extend: {
            colors: {
                primary: "#355C80",
                secondary: "#1B77BA",
                danger: "#d95b5e"
            }
        }
    },
    plugins: []
};