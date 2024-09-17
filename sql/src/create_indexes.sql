DROP INDEX IF EXISTS idx_user_login;
DROP INDEX IF EXISTS idx_catalog_price;
DROP INDEX IF EXISTS idx_catalog_genre;
DROP INDEX IF EXISTS idx_games_id;
DROP INDEX IF EXISTS idx_rental_id;
DROP INDEX IF EXISTS idx_tracking_id;
-- User Table
CREATE INDEX idx_user_login ON users USING BTREE (login);

-- Catalog Table
CREATE INDEX idx_catalog_price ON catalog USING BTREE (price);
CREATE INDEX idx_catalog_genre ON catalog USING BTREE (genre);

-- GamesInOrder Table
CREATE INDEX idx_games_id ON gamesinorder USING BTREE (gameid);

-- RentalOrder Table
CREATE INDEX idx_rental_id ON rentalorder USING BTREE (rentalorderid);

-- TrackingInfo Table
CREATE INDEX idx_tracking_id ON trackinginfo USING BTREE (rentalorderid);