FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21

ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS='-XX:MaxRAMPercentage=90 -Duser.language=nb'

COPY build/libs/*.jar /app/

WORKDIR /app

CMD ["-jar", "app.jar"]
