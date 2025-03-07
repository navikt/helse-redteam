FROM gcr.io/distroless/java21-debian12:nonroot

ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS='-XX:MaxRAMPercentage=90 -Duser.language=nb'

COPY build/libs/*.jar /app/

WORKDIR /app

CMD ["app.jar"]
