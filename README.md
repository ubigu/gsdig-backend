# GSDIG backend

Backend component of GSDIG. Spring Boot web application.

# Building

Build with maven

`mvn clean install`

or run directly with Spring Boot run

`mvn spring-boot:run`

# Installing

First setup a database and a db user for GSDIG, e.g. by:
```
CREATE DATABASE gsdig
   WITH OWNER = postgres
   ENCODING = 'UTF8'
   TABLESPACE = pg_default
   CONNECTION LIMIT = -1;
CREATE USER gsdig WITH PASSWORD 'gsdig';
```
And install `postgis` extension to `gsdig` database, e.g. (in psql client) by:
```
\c gsdig
CREATE EXTENSION postgis;
```
And grant the newly created user `gsdig` all privileges
```
GRANT ALL PRIVILEGES ON DATABASE gsdig to gsdig;
```
Then run the `/src/main/resources/initdb.sql` init script as `gsdig` user to create the tables gsdig-backend requires.

When you're done with that you need configure the db connection parameters in `/src/main/resources/application.properties`

You'll also need to setup a [Keycloak](https://www.keycloak.org/) instance and add the required configuration in `application.properties`.

Feel free to contact us at Ubigu for further instructions

## About GSDIG

The Geospatial Statistical Data Integration Service, GSDIG, will address frequent needs to integrate and aggregate spatially located unit data from different sources and with statistical background information.

GSDIG is an indexing-based process to integrate statistical data from any domain in areal subdivisions; grids, standard or user modified geographies. It enables reusing location data of/for the original unit record data - usually buildings or cadastral parcels - and geographies from SDI using persistent identifiers (or address matching as extension) which provides an integrated geocoding system and data supply with further aggregation capabilities.

A metadata-driven application for statistical data exploration will pilot the use of additional metadata concerning statistical and spatial data in automating and enhancing the user experience. The eventual goal is to elaborate a multilateral (multisource) geospatial-statistical ecosystem to increase the impact of geospatial and statistical information in decision making.
