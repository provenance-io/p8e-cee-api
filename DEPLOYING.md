# Deploying

This guide will provide a high-level outline of the steps required to stand up a new instance of `p8e-cee-api` **for use against an [actual Provenance network](https://github.com/provenance-io/provenance#active-networks)**.

It is highly recommended one first goes through the process of standing up a local environment for `p8e-cee-api` per the instructions in the [README](README.md) to understand the various processes involved and how they communicate with each other.

## Services

A connection will be needed to the following services, which can be either an existing deployment you have permission to connect to or a self-hosted instance:

- `p8e-cee-api`
  - Use one of the [published Docker images](https://hub.docker.com/r/provenanceio/p8e-cee-api/), or build an image from source if you need to make your own customizations
- [Object store](https://github.com/provenance-io/object-store)s which contains or replicates the data in the [metadata scopes](https://developer.provenance.io/docs/pb/modules/metadata-module#scope-data-structures) you will be operating on
  - See the [README](https://github.com/provenance-io/object-store#backends) for the currently supported storage backends
  - See an [example of an object store gateway](https://github.com/FigureTechnologies/object-store-gateway/) as a possible way to access an object store
- Key Management System (KMS)
  - `p8e-cee-api` consumes the [provenance-io/kms-connector](https://github.com/provenance-io/kms-connector) library to allow for fetching of Provenance account keys from any plugin defined there (as of writing, there is default support for [Vault](https://www.vaultproject.io/) & tentative support for Fortanix and Keystone)

## Common Pitfalls
### Environment Variables
`p8e-cee-api` is a Spring application which sets various application properties based on [definitions determined by the active Spring profile](https://github.com/provenance-io/p8e-cee-api/tree/174f066aa91b510cdb777f1b09010693b34fe838/service/src/main/resources).
Any environment variable defined in the active Spring profile's `*.properties` file will need to be defined in order for the service to run.

Data types should be inferrable from the corresponding Kotlin classes annotated with `@ConfigurationProperties`.

Some environment variables, e.g. `DART_UUID` and `PORTFOLIO_MANAGER_ADDRESS`, exist only to tailor to common users of `p8e-cee-api`, and as such, can be deleted if not used.
  - ⚠️ Note that doing this deletion requires modification to Kotlin/Spring code and then building the Docker image from the modified source.
