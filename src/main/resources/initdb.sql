CREATE TABLE areal_division (
    uuid uuid NOT NULL PRIMARY KEY,
    created_by uuid NOT NULL,
    title text NOT NULL,
    description text,    
    organization text,
    publicity boolean,
    min_x double precision NOT NULL,
    min_y double precision NOT NULL,
    max_x double precision NOT NULL,
    max_y double precision NOT NULL,
    attributes json NOT NULL
);

CREATE TABLE background_layer (
    uuid uuid NOT NULL PRIMARY KEY,
    type text NOT NULL,
    title text NOT NULL,    
    options json NOT NULL
);

CREATE TABLE downloads (
    uuid uuid NOT NULL PRIMARY KEY,
    created_by uuid NOT NULL,
    collection_id uuid NOT NULL,
    format text NOT NULL,
    path text NOT NULL,
    content_type text NOT NULL,
    filename text NOT NULL,
    length bigint NOT NULL
);

CREATE TABLE job (
    uuid uuid NOT NULL PRIMARY KEY,
    created_by uuid NOT NULL,
    title text NOT NULL,
    description text,
    areal_division uuid NOT NULL,
    area_attributes json NOT NULL,
    unit_dataset uuid NOT NULL,
    data_attributes json NOT NULL,
    additional_grouping_property text,
    status text NOT NULL,
    message text,
    created timestamp without time zone NOT NULL,
    updated timestamp without time zone NOT NULL,
    started timestamp without time zone,
    finished timestamp without time zone
);
CREATE INDEX ON job (unit_dataset);

CREATE TABLE unitdata (
    uuid uuid NOT NULL PRIMARY KEY,
    created_by uuid NOT NULL,
    title text NOT NULL,
    description text,
    organization text,
    publicity boolean,
    min_x double precision NOT NULL,
    min_y double precision NOT NULL,
    max_x double precision NOT NULL,
    max_y double precision NOT NULL,
    attributes json NOT NULL,
    remote boolean NOT NULL,
    sensitivity_setting json
);

CREATE TABLE permission (
    resource_id uuid NOT NULL,
    role text NOT NULL,
    permissions integer NOT NULL
);
ALTER TABLE permission ADD PRIMARY KEY (resource_id, role);

CREATE TABLE uploads (
    uuid uuid NOT NULL PRIMARY KEY,
    created_by uuid NOT NULL,
    typename text NOT NULL,
    min_x double precision NOT NULL,
    min_y double precision NOT NULL,
    max_x double precision NOT NULL,
    max_y double precision NOT NULL,
    srid integer,
    attributes json NOT NULL
);

CREATE TABLE workspace (
    uuid uuid NOT NULL PRIMARY KEY,
    created_by uuid NOT NULL,
    title text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    last_modified timestamp without time zone DEFAULT now() NOT NULL,
    center_x double precision NOT NULL,
    center_y double precision NOT NULL,
    zoom double precision NOT NULL,
    background_layers json NOT NULL,
    data_layer json
);

CREATE TABLE workspace_defaults (
    title text NOT NULL,
    center_x double precision NOT NULL,
    center_y double precision NOT NULL,
    zoom double precision NOT NULL,
    background_layer uuid,
    opacity double precision
);
