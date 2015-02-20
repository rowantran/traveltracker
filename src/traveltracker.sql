CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    user TEXT,
    email TEXT,
    hash TEXT,
    salt TEXT,
    invites TEXT,
    latitude NUMERIC,
    longitude NUMERIC,
    groupsTable TEXT
);

CREATE TABLE groups (
    id INTEGER PRIMARY KEY,
    gName TEXT,
    owner TEXT,
    destLatitude NUMERIC,
    destLongitude NUMERIC,
    usersTable TEXT
);
