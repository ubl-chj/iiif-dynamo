## iiif-dynamo

[![Build Status](https://travis-ci.org/ub-leipzig/iiif-dynamo.png?branch=master)](https://travis-ci.org/ub-leipzig/iiif-dynamo)

An Apache Camel Jetty implementation that queries web annotations with SPARQL and dynamically constructs 
IIIF Manifests and Collections.  It also uses Redis to cache query results to produce instantaneous responses.

## Configuration
 * `de.ubleipzig.dynamo.cfg` 
 * `de.ubleipzig.webanno.Constants`

## Building
This requires JDK10 or higher.
To build run
```bash
./buildtools/src/install/install-jpms.sh
```
## Running Docker Composition
`docker-compose up`
* This starts three containers `redis`, `dynamo` and `dynamo-search` on the `110_default` network (which is where the JDK10 
[Trellis deployment](https://github.com/trellis-ldp/trellis-deployment/tree/master/trellis-compose/trellis-app-triplestore/1.10) lives)

## Endpoint
The test query endpoint is exposed at `http://localhost:9095/dynamo`

## Example Endpoint Metadata Type Query
This example requests canvases with resources that have either metadata value `1676` or metadata value `1670`

```bash
$ http://localhost:9095/dynamo?type=meta&v1=1676&v2=1670
```

## Example Endpoint Collection Type Query
This example builds a collection of dynamic manifest identifiers by evaluating all possible metadata query values.

```bash
$ http://localhost:9095/dynamo?type=collection
```

## Dependencies
* Start [trellis-compose](https://github.com/trellis-ldp/trellis-deployment/blob/master/trellis-compose/trellis-app/1.9/docker-compose.yml) 

## Resource Aggregation with SPARQL at Fuseki Endpoint
The main use case of this pipeline is to enable repository resources to be grouped as typed collections in named graphs.    

See [Systematik Catalogue Builder](https://github.com/ub-leipzig/systematik-catalogue-builder) for a Web Annotation Builder.
