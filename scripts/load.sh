#!/bin/bash
# Génère du trafic (utile pour Kiali / Grafana et pour déclencher le circuit breaker).
BASE=${BASE:-http://localhost:31380}
for i in $(seq 1 1000); do
  curl -s -o /dev/null -w "%{http_code} " -X POST "$BASE/stock-service/api/stock/products/1/restock?quantity=1"
done
echo
