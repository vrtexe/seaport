# Seaport Hosting

## Start

### Cluster

There is a script located at [cloud/local/init.sh](cloud/local/init.sh),
that should start up a [kind](https://kind.sigs.k8s.io/) cluster.

### Application

The applications can be run in two modes, and the mode is set based on the spring profile used, `cluster` when running
inside the cluster, `external` when the app is running outside a cluster, in this case a .kubeconfig file needs to be
provided from the cluster to manage.

To configure it to run in a cluster the application requires cluster roles to be set up, take a look
at [cloud/local/development/rback](cloud/local/development/rback) and how the service account is set
in [deployment](cloud/local/development/app/deploy.yaml).

Start spring gradle app in development mode

```sh
   bootRun --args='--spring.profiles.active=external' --continuous -xtest
```

- continuous rebuild the app on change
- -xtest disabled the test from running every time

### UI

The ui is located in [`frontend`](frontend)

Start development mode:

```sh
    pnpm dev
```

Start in production mode:

```sh
    pnpm build
    pnpm preview
```

## Development

Main parts

- [Webdav](https://github.com/vrtexe/webdav)
- [Kubernetes](https://kubernetes.io/)
- [Kaniko](https://github.com/GoogleContainerTools/kaniko)
- [Kind](https://kind.sigs.k8s.io/)
- [Postgres](https://www.postgresql.org/)
- [Registry](https://hub.docker.com/_/registry)

### Local registry

The registry is hosted by itself inside the cluster

- [Kind registry config loader](cloud/local/cluster/load-registry-config.sh)
- [containerd config](cloud/local/cluster/certs.d)
- [Registry name resolution](cloud/local/cluster/kind-start.sh)
- [Static registry service cluster ip](cloud/local/development/registry/service.yaml)
- [Data directory](cloud/local/data/internal-registry)

### Webdav

Webdav is configured in [`cloud/local/development/webdav`](cloud/local/development/webdav)

Locally data would be stored in [`cloud/local/data/webdav`](cloud/local/data/webdav)

### Application docker definition

The resources defined in [cloud/local/development/app/deploy.yaml](cloud/local/development/app/deploy.yaml) are meant
for development where you can start the application and access the api from outside of the cluster, when you make code
changes it should automatically rebuild (because the code itself is mounted inside)

### Adding container images

Container images are kept in the database.

For how to add new container images take a look at
the [data migration script](src/main/resources/db/migrations/1.2_base_image_data.sql)

When it is added to the database the parameters are automatically picked up by the ui, no additional configuration
should be necessary