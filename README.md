# p8e-cee-api

The p8e-cee-api allows for operations against the encrypted object store, with included support for multi-store replication, and creating and broadcasting scoped transmissions to the Provenance Blockchain.

`p8e-cee-api` stands for the Provenance Contract Execution Environment API.

## Status

[![stability-release-candidate](https://img.shields.io/badge/stability-pre--release-48c9b0.svg)](https://github.com/mkenney/software-guides/blob/master/STABILITY-BADGES.md#release-candidate)
[![Latest Release][release-badge]][release-latest]
[![License][license-badge]][license-url]
[![LOC][loc-badge]][loc-report]

[release-badge]: https://img.shields.io/github/v/tag/provenance-io/p8e-cee-api.svg
[release-latest]: https://github.com/provenance-io/p8e-cee-api/releases/latest

[license-badge]: https://img.shields.io/github/license/provenance-io/p8e-cee-api.svg
[license-url]: https://github.com/provenance-io/p8e-cee-api/blob/main/LICENSE

[loc-badge]: https://tokei.rs/b1/github/provenance-io/p8e-cee-api
[loc-report]: https://github.com/provenance-io/p8e-cee-api

## Overview

The [Asset Originator's Guide](https://docs.provenance.io/integrating/asset-originators-guide) provides contextual support for the varied use cases supported by this API. Having a fundamental understanding of the Provenance Blockchain is recommended.

## Local Setup
To run this service locally, be sure to have [Docker](https://www.docker.com/) and [Vault by Hashicorp](https://www.vaultproject.io/) installed:

```
brew install docker
```

```
brew tap hashicorp/tap
brew install hashicorp/tap/vault
```
If you plan on running smart contracts for asset classification you'll need the following:
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

If any are taken on your local machine, feel free to update the default values in the `/service/docker/dependencies.yaml` file and associated `/service/docker/*.env` files.

Once ready, all you need to do is run the included docker setup script from the root directory:

```
./dc.sh up
```

Then, run the service - either via an Intellij run configuration or via the command line with the following command:

```
./gradlew bootRun
```

## Swagger Documentation

once the service is running, try out some local calls using Swagger!

http://localhost:8080/p8e-cee-api/secure/docs/api.html


## Publishing p8e Contracts Locally

As a convenience, you can publish contracts from another repository without leaving this project in the command line.

```
./dc.sh -p <path to contracts repository> -v <version to publish> publish
```

For example:

```
./dc.sh -p ../../provenance-io/loan-package-contracts -v 0.3.2 publish
```

Note: This convenience method uses preset environment variables found in `/service/docker/bootstrap.env` and assumes the address used to sign the transactions associated with publishing the contracts is listed in the genesis block (`/service/docker/prov-init/config/genesis.json`).
