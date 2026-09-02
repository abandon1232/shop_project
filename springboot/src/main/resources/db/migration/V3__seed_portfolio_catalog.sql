-- A small, brand-neutral catalogue for local development and portfolio demos.
-- The application stores product metadata and image URLs in MySQL; the image
-- binaries themselves are served from Spring Boot's static resources folder.

INSERT INTO type (name, description, img)
SELECT 'Computers', 'Laptops and everyday computing devices.', 'http://localhost:9090/products/slim-laptop.png'
WHERE NOT EXISTS (SELECT 1 FROM type WHERE name = 'Computers');

INSERT INTO type (name, description, img)
SELECT 'Audio', 'Headphones and personal audio equipment.', 'http://localhost:9090/products/wireless-headphones.png'
WHERE NOT EXISTS (SELECT 1 FROM type WHERE name = 'Audio');

INSERT INTO type (name, description, img)
SELECT 'TV & Entertainment', 'Displays for films, games and home entertainment.', 'http://localhost:9090/products/smart-television.png'
WHERE NOT EXISTS (SELECT 1 FROM type WHERE name = 'TV & Entertainment');

INSERT INTO type (name, description, img)
SELECT 'Smart Home', 'Connected devices that simplify everyday routines.', 'http://localhost:9090/products/robot-vacuum.png'
WHERE NOT EXISTS (SELECT 1 FROM type WHERE name = 'Smart Home');

INSERT INTO goods (name, description, img, price, unit, count, type_id, business_id)
SELECT
    'Nord 14 Slim Laptop',
    '<p>A lightweight 14-inch notebook with a crisp display, quiet performance and all-day portability.</p>',
    'http://localhost:9090/products/slim-laptop.png',
    8990.00,
    'each',
    28,
    (SELECT id FROM type WHERE name = 'Computers'),
    NULL
WHERE NOT EXISTS (SELECT 1 FROM goods WHERE name = 'Nord 14 Slim Laptop');

INSERT INTO goods (name, description, img, price, unit, count, type_id, business_id)
SELECT
    'Auralis Wireless Headphones',
    '<p>Comfortable over-ear headphones with active noise reduction and balanced everyday sound.</p>',
    'http://localhost:9090/products/wireless-headphones.png',
    1490.00,
    'each',
    42,
    (SELECT id FROM type WHERE name = 'Audio'),
    NULL
WHERE NOT EXISTS (SELECT 1 FROM goods WHERE name = 'Auralis Wireless Headphones');

INSERT INTO goods (name, description, img, price, unit, count, type_id, business_id)
SELECT
    'Vista 55 Smart Television',
    '<p>A slim 55-inch smart television with a vivid 4K panel and a clean, modern design.</p>',
    'http://localhost:9090/products/smart-television.png',
    6990.00,
    'each',
    19,
    (SELECT id FROM type WHERE name = 'TV & Entertainment'),
    NULL
WHERE NOT EXISTS (SELECT 1 FROM goods WHERE name = 'Vista 55 Smart Television');

INSERT INTO goods (name, description, img, price, unit, count, type_id, business_id)
SELECT
    'Orbit Smart Robot Vacuum',
    '<p>A low-profile robot vacuum with room mapping, obstacle detection and scheduled cleaning.</p>',
    'http://localhost:9090/products/robot-vacuum.png',
    4290.00,
    'each',
    34,
    (SELECT id FROM type WHERE name = 'Smart Home'),
    NULL
WHERE NOT EXISTS (SELECT 1 FROM goods WHERE name = 'Orbit Smart Robot Vacuum');
