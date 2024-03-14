FROM gcr.io/distroless/java21-debian12:nonroot

ENV TZ="Europe/Oslo"
ENV LANG="nb_NO.UTF-8"
ENV JAVA_OPTS='-XX:MaxRAMPercentage=90'

COPY build/libs/*.jar /app/

WORKDIR /app

CMD ["app.jar"]
