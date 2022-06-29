# Route Planner API - Build and OpenShift Deployment


## OpenShift Deployment Instructions

1. Setup image pull secrets
2. optional - setup Service Accounts for remote automation
3. Provision Data Providers
4. Create BuildConfigs in tools namespace for router-web and admin app.
5. Provision the Route Planner in destination namespace
6. post provision steps
   a. copy relevant data into data Providers
   b. update config with relevant configurations.
7. Configure or update Kong Gateway routes

## Templates

## `caddy-minio-pvc-template.yaml`

This Template provisions a Caddy server and a MinIO sever that share the same PVC.
* Minio is used to transport/upate the data files.
* Caddy is used to server the data files to the route planner API - when the pod startes it reads source data files from the caddy server.

### Example
```bash
#!/bin/bash

namespace=1475a9-dev
tools_namespace=1475a9-tools

echo "Provisioning Router Datastore DLV"
oc \
    -n ${namespace} \
    process \
    -p ROUTE_SUBDOMAIN=apps.silver.devops.gov.bc.ca \
    -p APPLICATION_NAME=router-datastore-dlv \
    -p TOOLS_NAMESPACE=${tools_namespace} \
    -f caddy-minio-pvc-template.yaml \
    -o yaml \
    | oc apply \
      -n ${namespace} -f - # --dry-run=client
```

## `router.buildconfigs.template.yaml`

This contains the build config for:  
`ols-router-admin-sidecar`  
`ols-router-sidecar`

This template provides a parameterized BuildConfig for both images.
The resulting BuildConfig, when run, created an imageStream containing the indicated version of the ols-router-admin WAR file.

### Example  
```bash
oc process -f router.buildconfigs.template.yaml --param-file=env.build -o yaml | oc apply -f - -n 1475a9-tools
```
#### Remove
```bash
# use get first to check
oc get all -l template=router-sidecar-buildconfigs -n 1475a9-tools
...
# use delete
oc delete all -l template=router-sidecar-buildconfigs -n 1475a9-tools
```
## `cfg-maps.yaml`

Provision config files:

```
oc process -f cfg-maps.yaml | oc apply -f -
```

## `router.template.yaml`

This provision all the objects relevant to the Route Planner API.  This includes

* Route Planner API web app
* Data (or) Config Admin Web App
* necessary services and routes
* necessary NetworPolcies.

#### Using env. files

```bash
$ oc process -f router.template.yaml --param-file=env.dev -o yaml | oc apply -f - -n 1475a9-dev
```

#### Remove

```bash
# use get first to check
oc get all -n 1475a9-dev -l template=route-planner-template-databc
# delete
oc delete all -n 1475a9-dev -l template=route-planner-template-databc
```
