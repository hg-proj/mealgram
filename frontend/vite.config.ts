import fs from 'node:fs';
import path from 'node:path';
import { loadEnv, type Plugin } from 'vite';
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

// 개발 서버 백엔드 이미지 제공
function backendImages(): Plugin {
  const root = path.resolve(import.meta.dirname, '../backend/src/main/resources/static/images');
  return {
    name: 'backend-images',
    apply: 'serve',
    configureServer(server) {
      server.middlewares.use('/images', (req, res, next) => {
        const file = path.join(root, decodeURIComponent((req.url ?? '').split('?')[0]));
        if (file.startsWith(root + path.sep) && fs.existsSync(file) && fs.statSync(file).isFile()) {
          res.setHeader('Content-Type', 'image/webp');
          fs.createReadStream(file).pipe(res);
        } else {
          next();
        }
      });
    },
  };
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [react(), tailwindcss(), backendImages()],
    resolve: { alias: { '@': path.resolve(import.meta.dirname, 'src') } },
    server: {
      proxy: { '/api': env.VITE_PROXY_TARGET || 'http://localhost:8080' },
    },
    test: {
      environment: 'jsdom',
      setupFiles: ['./src/test/setup.ts'],
    },
  };
});
