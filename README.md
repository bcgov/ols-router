# Route Planner API - Build and OpenShift Deployment

## Build Instructions

The Following Jenkins Job is used to Build the artifacts:

 [OLS Router Build](https://cis.apps.gov.bc.ca/int/view/LOC/job/ols/job/OLS%20OSS%20Jobs/job/OLS%20Router%20Build/)

Artifacts are managed in Artifactory:

https://delivery.apps.gov.bc.ca/artifactory/

`lib-snapshot-repo` and   `lib-release-repo` folders.


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

This template provides a parameterized BuildConfig for both sidedar containers.
The resulting BuildConfig, when run, created an imageStream containing the indicated version of the ols-router-admin WAR file.

  * When the associated Deployment runs, this imageStream is used as an initContainer in a pod.
  * It's purpose is to "copy the ols-router-admin.war to /app/ROOT.war"
  * The long-running container then launches tomcat which subsequently loads the WAR, thus starting the application.

For details on the pattern being used see:
  https://github.com/kubernetes/examples/tree/master/staging/javaweb-tomcat-sidecar

### Example  
```bash
oc process -f router.buildconfigs.template.yaml -o yaml \
 | oc apply -f - -n 1475a9-tools
```
#### Remove
```bash
# use get first to check
oc get all -l template=router-sidecar-buildconfigs -n 1475a9-tools
...
# use delete
oc delete all -l template=router-sidecar-buildconfigs -n 1475a9-tools
```

## `router.template.yaml`

This provision all the objects relevant to the Route Planner API.  This includes

* Route Planner API web app
* Data (or) Config Admin Web App
* Cassandra Cluster
* necessary services and routes
* necessary NetworPolcies.

### Example

```bash
#!/bin/bash

NS=1475a9-dev
TOOLS=1475a9-tools
DOCKER_CFG_SECRET=default-dockercfg-XXXXX

# oc get all -l app=ols-router-web -n ${NS}
# oc delete  all -l app=ols-router-web -n ${NS}

oc process -f router-template.yaml \
    -p TOOLS_NAMESPACE=${TOOLS} \
    -p ENV=dev \
    -p DEFAULT_DOCKERCFG=${DOCKER_CFG_SECRET} \
    -o yaml \
    | oc apply -f - -n ${NS} #\
    #--dry-run=client
    #| yq -C - r
```
#### Alternatively  
```bash
$ cat dev.env
TOOLS_NAMESPACE=1475a9-tools
ENV=dev
# change this to your configured secret
DEFAULT_DOCKERCFG=default-dockercfg-XXXXX
ROUTER_IS_TAG=latest
DATA_ADMIN_IS_TAG=latest
$
$
$ oc process -f router.template.yaml --param-file=dev.env -o yaml

```

#### Remove

```bash
# use get first to check
oc get all -n 1475a9-dev -l template=route-planner-template-databc
# delete
oc delete all -n 1475a9-dev -l template=route-planner-template-databc
```
