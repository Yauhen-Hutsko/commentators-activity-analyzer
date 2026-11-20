import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The built app is packaged into the WAR under /greatanalyzer/react/.
// In dev (`npm run dev`), API calls are proxied to Tomcat on :8080 (local tomcat7:run or Docker).
export default defineConfig({
  base: '/greatanalyzer/react/',
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/greatanalyzer/api': process.env.API_TARGET ?? 'http://localhost:8080',
    },
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
});
