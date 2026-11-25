#!/bin/sh
# Scénario de test via l'Ingress Gateway (lancer ./scripts/ingress-forward.sh avant).
BASE=${BASE:-http://localhost:31380}
H="Content-Type: application/json"

echo "== Création d'un produit (stock 10)"
curl -s -H "$H" -X POST -d '{"name":"Clavier","quantity":10}' $BASE/stock-service/api/stock/products; echo
echo "== Liste des produits"
curl -s $BASE/stock-service/api/stock/products; echo
echo "== Réservation de 3 unités"
curl -s -X POST "$BASE/stock-service/api/stock/products/1/reserve?quantity=3" -w "HTTP %{http_code}\n"
echo "== Sondes de santé"
curl -s $BASE/order-service/actuator/health/readiness; echo
curl -s $BASE/stock-service/actuator/health/liveness; echo
