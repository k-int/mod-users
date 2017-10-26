#!/usr/bin/env bash

users_storage_address=${1:-http://localhost:9130/users}
tenant=${2:-demo_tenant}

for f in ./users/*.json; do
    curl -w '\n' -X POST -D - \
         -H "Content-type: application/json" \
         -H "X-Okapi-Tenant: ${tenant}" \
         -d @$f \
         "${users_storage_address}"
done
