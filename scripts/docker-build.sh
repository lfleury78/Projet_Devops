#!/bin/sh
# Construit les JAR puis les images Docker (tag :1).
# Pour minikube, exécuter d'abord : eval $(minikube docker-env)
set -e
cd "$(dirname "$0")/.."
mvn -B -DskipTests package
docker build --tag=stock-service:1 stock-service
docker build --tag=order-service:1 order-service
