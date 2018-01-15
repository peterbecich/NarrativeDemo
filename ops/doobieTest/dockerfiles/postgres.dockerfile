FROM library/postgres:10

MAINTAINER Peter Becich "peterbecich@gmail.com"

ADD dockerfiles/init.sql /docker-entrypoint-initdb.d/

# https://stackoverflow.com/questions/40717975/extend-docker-postgres-image-to-create-extra-database

# https://hub.docker.com/_/postgres/
