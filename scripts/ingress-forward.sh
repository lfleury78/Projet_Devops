#!/bin/sh
echo "Ingress ouvert sur http://localhost:31380/"
kubectl -n istio-system port-forward deployment/istio-ingressgateway 31380:8080
