-- liquibase formatted sql
-- changeset vangel:1.2_base_image_data splitStatements:false

do
$$
    declare
        base_image_id         base_image.id%type;
        maven_image_ref_id    base_image_ref.id%type;
        gradle_image_ref_id   base_image_ref.id%type;
        java_exe_image_ref_id base_image_ref.id%type;
    begin
        if exists (select id from base_image) then
            return;
        end if;

        insert into base_image(language, version)
        values ('java', '21')
        returning id into base_image_id;

        insert into base_image_ref default
        values
        returning id into maven_image_ref_id;

        insert into base_image_ref default
        values
        returning id into gradle_image_ref_id;

        insert into base_image_ref default
        values
        returning id into java_exe_image_ref_id;

        insert into base_image_git(build_tool, version, base_id, ref_id, value)
        values ('maven', '3.9.8', base_image_id, maven_image_ref_id, 'FROM alpine/git:2.45.2 as src

WORKDIR /data

ARG HASH
ARG URL

ENV HASH=${HASH}
RUN git clone $URL src


FROM maven:3.9.8-eclipse-temurin-21-alpine as builder

ARG JAR_PATH
ARG BASE_DIRECTORY="."
ARG BUILD_ARGS=""

WORKDIR /data

COPY --from=src /data/src src

WORKDIR /data/src/$BASE_DIRECTORY

RUN mvn $BUILD_ARGS clean install
RUN mv $JAR_PATH /data/app.jar


FROM eclipse-temurin:21-alpine as jrebuilder

WORKDIR build

RUN jlink \
     --add-modules ALL-MODULE-PATH \
     --strip-debug \
     --no-man-pages \
     --no-header-files \
     --output jre


FROM alpine:3.20.2

COPY --from=jrebuilder /build/jre jre
COPY --from=builder  /data/app.jar app.jar

ENV JAVA_ARGS ""
ENV JAR_ARGS ""

ENV JAVA_HOME /jre
ENV PATH $JAVA_HOME/bin:$PATH

CMD java $JAVA_ARGS -jar $JAR_ARGS app.jar'),
               ('gradle', '8.9.0', base_image_id, gradle_image_ref_id, 'FROM alpine/git:2.45.2 as src

WORKDIR /data

ARG HASH
ARG URL

ENV HASH=${HASH}
RUN git clone $URL src


FROM gradle:8.9.0-jdk21-alpine as builder

ARG JAR_PATH
ARG BASE_DIRECTORY="."
ARG BUILD_ARGS=""

WORKDIR /data

COPY --from=src /data/src src

WORKDIR /data/src/$BASE_DIRECTORY

RUN gradle $BUILD_ARGS clean build
RUN mv $JAR_PATH /data/app.jar


FROM eclipse-temurin:21-alpine as jrebuilder

WORKDIR build

RUN jlink \
     --add-modules ALL-MODULE-PATH \
     --strip-debug \
     --no-man-pages \
     --no-header-files \
     --output jre


FROM alpine:3.20.2

COPY --from=jrebuilder /build/jre jre
COPY --from=builder  /data/app.jar app.jar

ENV JAVA_ARGS ""
ENV JAR_ARGS ""

ENV JAVA_HOME /jre
ENV PATH $JAVA_HOME/bin:$PATH

CMD java $JAVA_ARGS -jar $JAR_ARGS app.jar');

        insert into base_image_exe(base_id, ref_id, file_type, value)
        values (base_image_id, java_exe_image_ref_id, 'jar', 'FROM curlimages/curl:8.8.0 as downloader

ARG HASH
ARG URL

WORKDIR /download

ENV HASH=${HASH}
RUN curl $URL > app.jar


FROM eclipse-temurin:21-alpine as jrebuilder

WORKDIR build

RUN jlink \
     --add-modules ALL-MODULE-PATH \
     --strip-debug \
     --no-man-pages \
     --no-header-files \
     --output jre


FROM alpine:3.20.2

COPY --from=jrebuilder /build/jre jre
COPY --from=downloader  /download/app.jar .

ENV JAVA_ARGS ""
ENV JAR_ARGS ""

ENV JAVA_HOME /jre
ENV PATH $JAVA_HOME/bin:$PATH

CMD java $JAVA_ARGS -jar $JAR_ARGS app.jar
');

        insert into base_image_arg(name, type, stage, description, base_image_ref_id)
        values ('FILE', 'file', 'build', 'Jar file', java_exe_image_ref_id),
               ('JAVA_ARGS', 'string', 'runtime', 'Arguments passed to the java command', java_exe_image_ref_id),
               ('JAR_ARGS', 'string', 'runtime', 'Arguments passed to the application', java_exe_image_ref_id),

               ('URL', 'string', 'build', 'Url of the code repository (public)', maven_image_ref_id),
               ('JAR_PATH', 'string', 'build', 'Path to the jar file building the project', maven_image_ref_id),
               ('BUILD_DIRECTORY', 'string', 'build',
                'The base project directory within the repository, default is the base directory of the project (optional)',
                maven_image_ref_id),
               ('BUILD_ARGS', 'string', 'build', 'Arguments passed to the maven command', maven_image_ref_id),
               ('JAVA_ARGS', 'string', 'runtime', 'Arguments passed to the java command', maven_image_ref_id),
               ('JAR_ARGS', 'string', 'runtime', 'Arguments passed to the application', maven_image_ref_id),

               ('URL', 'string', 'build', 'Url of the code repository (public)', gradle_image_ref_id),
               ('JAR_PATH', 'string', 'build', 'Path to the jar file building the project', gradle_image_ref_id),
               ('BUILD_DIRECTORY', 'string', 'build',
                'The base project directory within the repository, default is the base directory of the project (optional)',
                gradle_image_ref_id),
               ('BUILD_ARGS', 'string', 'build', 'Arguments passed to the gradle command', gradle_image_ref_id),
               ('JAVA_ARGS', 'string', 'runtime', 'Arguments passed to the java command', gradle_image_ref_id),
               ('JAR_ARGS', 'string', 'runtime', 'Arguments passed to the application', gradle_image_ref_id);
    end
$$;


do
$$
    declare
        base_image_id base_image.id%type;
        image_ref_id  base_image_ref.id%type;
    begin


        insert into base_image(language, version)
        values ('nginx', '1.27.1')
        returning id into base_image_id;

        insert into base_image_ref default
        values
        returning id into image_ref_id;


        insert into base_image_exe(base_id, ref_id, file_type, value)
        values (base_image_id, image_ref_id, 'zip', 'FROM curlimages/curl:8.8.0 as downloader

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
');

        insert into base_image_arg(name, type, description, base_image_ref_id)
        values ('FILE', 'file', 'Zip file', image_ref_id);
    end
$$;
