DELETE FROM goods;
DELETE FROM type;

SET @catalog_business_id = (
    SELECT id
    FROM business
    WHERE status = 'APPROVED'
    ORDER BY id
    LIMIT 1
);

INSERT INTO type (name, description, img)
VALUES
    ('Computers & Tablets', 'Laptops, monitors and accessories for productive work and study.', '/images/catalog/categories/computers-tablets.webp'),
    ('Phones & Wearables', 'Connected essentials for communication, activity and charging.', '/images/catalog/categories/phones-wearables.webp'),
    ('TV & Audio', 'Entertainment products for clear pictures and balanced sound at home.', '/images/catalog/categories/tv-audio.webp'),
    ('Gaming', 'Responsive accessories for comfortable everyday gaming.', '/images/catalog/categories/gaming.webp'),
    ('Kitchen Appliances', 'Practical countertop appliances for daily cooking and coffee breaks.', '/images/catalog/categories/kitchen-appliances.webp'),
    ('Smart Home', 'Simple connected devices for lighting, awareness and home comfort.', '/images/catalog/categories/smart-home.webp');

INSERT INTO goods (name, description, img, price, unit, count, type_id, business_id)
VALUES
    (
        'NordBook Air 14 Laptop',
        'A lightweight 14-inch laptop with a bright display, quiet keyboard and all-day battery for study, office work and travel.',
        '/images/catalog/products/nordbook-air-14.webp',
        10990.00, '', 14,
        (SELECT id FROM type WHERE name = 'Computers & Tablets'),
        @catalog_business_id
    ),
    (
        'FjordView 27 QHD Monitor',
        'A 27-inch QHD monitor with a height-adjustable stand and crisp detail for productive home-office setups.',
        '/images/catalog/products/fjordview-27-monitor.webp',
        3490.00, '', 22,
        (SELECT id FROM type WHERE name = 'Computers & Tablets'),
        @catalog_business_id
    ),
    (
        'Birch Wireless Keyboard',
        'A compact low-profile wireless keyboard with comfortable keys and easy switching between two devices.',
        '/images/catalog/products/birch-wireless-keyboard.webp',
        899.00, '', 35,
        (SELECT id FROM type WHERE name = 'Computers & Tablets'),
        @catalog_business_id
    ),
    (
        'Aurora 5G Smartphone',
        'A balanced 5G smartphone with a vivid display, dependable cameras and enough battery for a full day.',
        '/images/catalog/products/aurora-phone.webp',
        7490.00, '', 18,
        (SELECT id FROM type WHERE name = 'Phones & Wearables'),
        @catalog_business_id
    ),
    (
        'Pulse Active Smartwatch',
        'Track daily activity, heart rate and notifications on a bright round display with a comfortable woven strap.',
        '/images/catalog/products/pulse-smartwatch.webp',
        2290.00, '', 26,
        (SELECT id FROM type WHERE name = 'Phones & Wearables'),
        @catalog_business_id
    ),
    (
        'Pocket 65W USB-C Charger',
        'A compact dual-port USB-C charger that can power a laptop, phone or tablet without taking over your bag.',
        '/images/catalog/products/pocket-usbc-charger.webp',
        549.00, '', 48,
        (SELECT id FROM type WHERE name = 'Phones & Wearables'),
        @catalog_business_id
    ),
    (
        'Horizon 55 4K Smart TV',
        'A slim 55-inch 4K television with straightforward streaming features and a clear picture for everyday viewing.',
        '/images/catalog/products/horizon-55-tv.webp',
        8990.00, '', 10,
        (SELECT id FROM type WHERE name = 'TV & Audio'),
        @catalog_business_id
    ),
    (
        'QuietWave Wireless Headphones',
        'Comfortable over-ear headphones with active noise reduction, clear calls and long wireless listening time.',
        '/images/catalog/products/quietwave-headphones.webp',
        1990.00, '', 31,
        (SELECT id FROM type WHERE name = 'TV & Audio'),
        @catalog_business_id
    ),
    (
        'RoomBeat Portable Speaker',
        'A compact splash-resistant speaker with balanced sound and a useful battery for the kitchen, balcony or park.',
        '/images/catalog/products/roombeat-speaker.webp',
        1190.00, '', 29,
        (SELECT id FROM type WHERE name = 'TV & Audio'),
        @catalog_business_id
    ),
    (
        'Arcade Pro Wireless Controller',
        'An ergonomic wireless controller with responsive triggers and a familiar layout for relaxed gaming sessions.',
        '/images/catalog/products/arcade-controller.webp',
        849.00, '', 24,
        (SELECT id FROM type WHERE name = 'Gaming'),
        @catalog_business_id
    ),
    (
        'Velocity Surround Gaming Headset',
        'A lightweight over-ear gaming headset with spatial sound, soft cushions and a clear adjustable microphone.',
        '/images/catalog/products/velocity-headset.webp',
        1290.00, '', 17,
        (SELECT id FROM type WHERE name = 'Gaming'),
        @catalog_business_id
    ),
    (
        'Glide Precision Gaming Mouse',
        'A lightweight precision mouse with adjustable sensitivity and six practical controls for work and play.',
        '/images/catalog/products/glide-gaming-mouse.webp',
        699.00, '', 40,
        (SELECT id FROM type WHERE name = 'Gaming'),
        @catalog_business_id
    ),
    (
        'Compact Air Fryer 5L',
        'A five-litre air fryer with simple controls and a removable basket for quick weekday meals and easy cleaning.',
        '/images/catalog/products/compact-air-fryer.webp',
        1590.00, '', 19,
        (SELECT id FROM type WHERE name = 'Kitchen Appliances'),
        @catalog_business_id
    ),
    (
        'Barista Filter Coffee Maker',
        'A compact filter coffee maker with a glass carafe, reusable filter and automatic keep-warm function.',
        '/images/catalog/products/barista-coffee-maker.webp',
        1290.00, '', 16,
        (SELECT id FROM type WHERE name = 'Kitchen Appliances'),
        @catalog_business_id
    ),
    (
        'Nordic Temperature Kettle 1.7L',
        'A stainless-steel kettle with selectable temperatures for tea, coffee and everyday boiling.',
        '/images/catalog/products/nordic-kettle.webp',
        799.00, '', 27,
        (SELECT id FROM type WHERE name = 'Kitchen Appliances'),
        @catalog_business_id
    ),
    (
        'Glow Smart Bulb Starter Set',
        'Three dimmable smart bulbs and a compact hub for simple schedules, warm light and app control.',
        '/images/catalog/products/glow-smart-bulbs.webp',
        649.00, '', 42,
        (SELECT id FROM type WHERE name = 'Smart Home'),
        @catalog_business_id
    ),
    (
        'HomeGuard Indoor Camera',
        'A compact indoor camera with motion alerts, night view and a privacy mode for everyday home awareness.',
        '/images/catalog/products/homeguard-camera.webp',
        999.00, '', 21,
        (SELECT id FROM type WHERE name = 'Smart Home'),
        @catalog_business_id
    ),
    (
        'Climate Mini Sensor',
        'A small wireless sensor that tracks room temperature and humidity and helps you understand indoor comfort.',
        '/images/catalog/products/climate-sensor.webp',
        449.00, '', 38,
        (SELECT id FROM type WHERE name = 'Smart Home'),
        @catalog_business_id
    );
