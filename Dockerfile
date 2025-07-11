FROM fedora:41
WORKDIR /app
COPY target/oda-files-service /app

CMD ["./oda-files-service"]

