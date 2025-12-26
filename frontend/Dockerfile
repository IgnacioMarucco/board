# ════════════════════════════════════════════════════════════════════════════
# FRONTEND DOCKERFILE - Multi-stage build
# ════════════════════════════════════════════════════════════════════════════
# Stage 1: Build Angular app using Node.js
# Stage 2: Serve static files using Nginx
# ════════════════════════════════════════════════════════════════════════════

# ─────────────────────────────────────────────────────────────────────────────
# STAGE 1: BUILD
# Uses Node.js to install dependencies and build the Angular app
# ─────────────────────────────────────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files first to cache npm install
# This layer is cached unless package.json/package-lock.json change
COPY package*.json ./
RUN npm ci

# Copy source code and build
COPY . .
RUN npm run build

# ─────────────────────────────────────────────────────────────────────────────
# STAGE 2: RUNTIME
# Uses Nginx Alpine to serve static files (~25MB image)
# ─────────────────────────────────────────────────────────────────────────────
FROM nginx:1.27-alpine

# Copy built Angular app to Nginx html directory
# Angular 17+ outputs to dist/<project-name>/browser/
COPY --from=build /app/dist/board-frontend/browser /usr/share/nginx/html

# Copy custom Nginx config for SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Nginx runs as non-root by default in Alpine image
CMD ["nginx", "-g", "daemon off;"]
