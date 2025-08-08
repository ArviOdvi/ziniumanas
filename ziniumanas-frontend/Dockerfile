FROM node:20-alpine AS build
WORKDIR /app
COPY ziniumanas-frontend/package*.json ./
RUN npm ci --only=production
COPY ziniumanas-frontend ./
RUN npm run build

FROM nginx:alpine
RUN apk add --no-cache curl

# TAVO nginx.conf padedam kaip default vhost
COPY ziniumanas-frontend/nginx.conf /etc/nginx/conf.d/default.conf

RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /app/build /usr/share/nginx/html

# SVARBU: jokių non-root user
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
