# 1) Build Stage
FROM node:20-alpine AS builder
WORKDIR /app

# 빌드 인자 선언 (기본값 /api)
ARG REACT_APP_API_URL=/api
ENV REACT_APP_API_URL=$REACT_APP_API_URL

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN REACT_APP_API_URL=$REACT_APP_API_URL npm run build

# 2) Serve with nginx
FROM nginx:alpine
WORKDIR /

COPY nginx/default.conf /etc/nginx/nginx.conf

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
