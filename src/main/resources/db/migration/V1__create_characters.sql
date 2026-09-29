-- The application generates each id in Java (see CharacterEntity), so this
-- column has no DEFAULT - every insert always supplies one.
CREATE TABLE characters (
    id         UUID PRIMARY KEY,
    name       VARCHAR(255),
    class_id   VARCHAR(100),
    race       VARCHAR(100),
    level      INTEGER,
    data       JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- No user/owner column yet - that arrives in a later migration once auth exists.
