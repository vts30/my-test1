#!/bin/bash
mvn clean install
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
docker build --build-arg APP_VERSION="$VERSION" -t bsp-forum-backup-monitor:"$VERSION" .
docker tag bsp-forum-backup-monitor:"$VERSION" registry-int.rz.bankenit.de/bsp/baseimages/bsp-forum-backup-monitor:"$VERSION"
docker push registry-int.rz.bankenit.de/bsp/baseimages/bsp-forum-backup-monitor:"$VERSION"
