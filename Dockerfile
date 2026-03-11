### Das eigentliche Image zusammenbauen ###
FROM xxx/jre-21:latest

# Installation
USER root
WORKDIR /bsp-forum-backup-monitor

# Define version as a build-time argument (default = 0.0.6)
ARG APP_VERSION=0.0.6
ENV APP_VERSION=${APP_VERSION}

COPY ../target/bsp-forum-backup-monitor-${APP_VERSION}.jar /bsp-forum-backup-monitor/bsp-forum-backup-monitor.jar
RUN chmod 555 /bsp-forum-backup-monitor/bsp-forum-backup-monitor.jar

COPY ./entrypoint.sh /bsp-forum-backup-monitor/entrypoint.sh
EXPOSE 8080
ENTRYPOINT [ "/bin/bash","/bsp-forum-backup-monitor/entrypoint.sh" ]
