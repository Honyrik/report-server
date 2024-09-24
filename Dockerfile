FROM gradle:7.6.4-jdk17 as builder

COPY . /tmp/app

RUN cd /tmp/app; \
    gradle --no-daemon clean build; \
    cp -rv /tmp/app/build/distributions/essence-report-boot-*.tar /tmp/.; \
    cd /tmp; \
    tar -xvf essence-report-boot-*.tar; \
    mv -v $(find /tmp -type d -name 'essence-report-boot*') /tmp/service

FROM eclipse-temurin:17-ubi9-minimal

ARG GROUP_UID=1001
ARG USER_UID=1001

RUN mkdir -p /opt/service; \
    groupadd -g $GROUP_UID app; \
    useradd --home-dir /opt/service --shell /bin/bash -g $GROUP_UID -u $USER_UID service;

COPY --from=builder --chown=$USER_UID:$GROUP_UID /tmp/service /opt/service

EXPOSE 8080

USER $USER_UID

WORKDIR /opt/service

CMD ["/opt/service/bin/essence-report"]