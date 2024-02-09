# p8e-cee-api

`p8e-cee-api` stands for the Provenance Contract Execution Environment API.

`p8e-cee-api` allows for operations against [BlockVault](https://developer.provenance.io/docs/pb/p8e/overview/) (formerly nicknamed "p8e"), with included support for replication across multiple object stores along with other miscellaneous utilities to facilitate creating and broadcasting scoped transmissions to the Provenance Blockchain.

## Status

[![stability-release-candidate](https://img.shields.io/badge/stability-pre--release-48c9b0.svg)](https://github.com/mkenney/software-guides/blob/master/STABILITY-BADGES.md#release-candidate)
[![Latest Release][release-badge]][release-latest]
[![Docker Hub badge][dockerhub-badge]][dockerhub]
[![License][license-badge]][license-url]
[![LOC][loc-badge]][loc-report]

[release-badge]: https://img.shields.io/github/v/tag/provenance-io/p8e-cee-api.svg
[release-latest]: https://github.com/provenance-io/p8e-cee-api/releases/latest

[dockerhub-badge]: https://img.shields.io/docker/pulls/provenanceio/p8e-cee-api
[dockerhub]: https://hub.docker.com/r/provenanceio/p8e-cee-api/

[license-badge]: https://img.shields.io/github/license/provenance-io/p8e-cee-api.svg
[license-url]: https://github.com/provenance-io/p8e-cee-api/blob/main/LICENSE

[loc-badge]: https://tokei.rs/b1/github/provenance-io/p8e-cee-api
[loc-report]: https://github.com/provenance-io/p8e-cee-api

## Overview

The [BlockVault integration guide](https://developer.provenance.io/docs/pb/integrating/integrating-with-p8e/) provides contextual support for the varied use cases supported by this API. Having a fundamental understanding of the Provenance Blockchain & BlockVault is recommended.

## Deploying/Hosting
[Instructions for how to deploy the service](DEPLOYING.md)

## Local Development
To run this service locally, be sure to have [Docker](https://www.docker.com/) installed:

```
brew install docker
```
You can also install [Vault](https://www.vaultproject.io/) if you wish to use a local installation over the provided Docker container:
```
brew tap hashicorp/tap
brew install hashicorp/tap/vault
```
If you plan on running smart contracts for asset classification, you may need the following packages:
```
brew install rust
brew install jq
```

You'll additionally need `CoreUtils` if your system is missing it:
```
brew install coreutils
```
**Note: it's possible to install all dependencies with the following command:**
```
./dc.sh setup
```

The default configuration assumes that the following ports are available:

| **Container**  |    **Port(s)**    |
|:--------------:|:-----------------:|
|   PostgreSQL   |       5432        |
| Object Store 1 |       5001        |
| Object Store 2 |       5002        |
|     Vault      |       8200        |
|   Provenance   | 1317, 9090, 26657 |

If any are taken on your local machine, feel free to update the default values in the `/service/local-docker/dependencies.yaml` file and associated `/service/local-docker/*.env` files.

Once ready, all you need to do is run the included docker setup script from the root directory:

```
./dc.sh up
```

Then, run the service - either via an Intellij run configuration or via the command line with the following command:

```
./gradlew bootRun
```

### Swagger Documentation

once the service is running, try out some local calls using Swagger!

http://localhost:8080/p8e-cee-api/secure/docs/api.html


### Publishing p8e Contracts Locally

As a convenience, you can publish contracts from another repository without leaving this project in the command line.

```
./dc.sh -p <path to contracts repository> -v <version to publish> publish
```

For example:

```
./dc.sh -p ../../provenance-io/loan-package-contracts -v 0.3.2 publish
```

Note: This convenience method uses preset environment variables found in `/service/local-docker/bootstrap.env` and assumes the address used to sign the transactions associated with publishing the contracts is listed in the genesis block (`/service/local-docker/prov-init/config/genesis.json`).
