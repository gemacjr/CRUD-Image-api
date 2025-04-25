# Use the official PostgreSQL image from Docker Hub
FROM postgres:14-alpine

# Set environment variables for PostgreSQL
ENV POSTGRES_DB=imagecrud
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres

# Create directories for persisting data and for initialization scripts
VOLUME /var/lib/postgresql/data
RUN mkdir -p /docker-entrypoint-initdb.d

# Copy initialization scripts
COPY init-db.sql /docker-entrypoint-initdb.d/

# Expose PostgreSQL port
EXPOSE 5432

# Add PostgreSQL configuration customizations (optional)
# For production, you might want to customize the postgresql.conf
# COPY custom-postgres.conf /etc/postgresql/postgresql.conf
# CMD ["postgres", "-c", "config_file=/etc/postgresql/postgresql.conf"]