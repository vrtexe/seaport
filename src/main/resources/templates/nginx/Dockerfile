FROM curlimages/curl:8.8.0 as downloader

ARG HASH
ARG URL

WORKDIR /download

ENV HASH=${HASH}
RUN curl $URL > app.zip

RUN mkdir app
RUN unzip app.zip -d app

RUN if [[ "$(ls -l app | tail -n +2 | wc -l | xargs)" = 1 && "$(ls -ld app/*/ | wc -l | xargs)" = 1 ]]; \
    then                       \
        folder=$(ls app);      \
        mv app/$folder/* app/; \
        rm -r app/$folder;     \
    fi;

FROM nginx:1.27.1-alpine

ARG PORT=80

COPY --from=downloader /download/app /var/www
RUN echo -e "\
server { \\\\n\
    listen $PORT;\\\\n\
    root /var/www;\\\\n\
    index index.html index.htm;\\\\n\
\\\\n\
    location / {\\\\n\
      try_files \$uri.html \$uri \$uri/ index.html;\\\\n\
    }\\\\n\
}" > /etc/nginx/conf.d/default.conf


CMD ["nginx", "-g", "daemon off;"]
