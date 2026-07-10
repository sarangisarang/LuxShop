-- Add an image URL to products and refresh the seed into a real luxury catalog,
-- keeping H2 (data.sql) and Postgres in sync.
ALTER TABLE product ADD COLUMN image_url VARCHAR(1000);

-- Categories
UPDATE category SET name='Watches',     description='Luxury and heritage timepieces', image='https://loremflickr.com/600/400/luxury,watch?lock=101' WHERE id='1';
UPDATE category SET name='Laptops',     description='High-performance notebooks',     image='https://loremflickr.com/600/400/laptop?lock=102' WHERE id='2';
UPDATE category SET name='Smartphones', description='Flagship mobile devices',        image='https://loremflickr.com/600/400/smartphone?lock=103' WHERE id='3';
INSERT INTO category (id, name, image, description) VALUES
    ('4','Audio','https://loremflickr.com/600/400/headphones?lock=104','Headphones, earbuds and speakers'),
    ('5','Accessories','https://loremflickr.com/600/400/luxury,accessory?lock=105','Bags, pens and eyewear');

-- Refresh the three existing products (kept so the sample order still references them)
UPDATE product SET product_name='Rolex Submariner Date', product_desc='Iconic 41mm Oystersteel dive watch with a Cerachrom bezel and self-winding Perpetual movement.', image_url='https://loremflickr.com/600/400/rolex,watch?lock=1', price=38500, stock=3, category_id='1' WHERE id='1';
UPDATE product SET product_name='Omega Speedmaster Moonwatch', product_desc='The legendary manual-wind chronograph, the first watch worn on the Moon.', image_url='https://loremflickr.com/600/400/omega,watch?lock=2', price=21900, stock=5, category_id='1' WHERE id='2';
UPDATE product SET product_name='MacBook Pro 16" M3 Max', product_desc='36GB unified memory, 1TB SSD and a stunning Liquid Retina XDR display.', image_url='https://loremflickr.com/600/400/macbook,laptop?lock=3', price=18999, stock=8, category_id='2' WHERE id='3';

-- Add the rest of the catalog
INSERT INTO product (id, product_name, product_desc, image_url, price, stock, category_id) VALUES
    ('4','Dell XPS 15 OLED','15.6-inch 3.5K OLED, Intel Core i9, 32GB RAM and NVIDIA RTX 4070.','https://loremflickr.com/600/400/laptop,ultrabook?lock=4',9499,10,'2'),
    ('5','iPhone 15 Pro Max','Aerospace-grade titanium, A17 Pro chip, 512GB and a 5x telephoto camera.','https://loremflickr.com/600/400/iphone,smartphone?lock=5',4299,15,'3'),
    ('6','Samsung Galaxy S24 Ultra','200MP camera, built-in S Pen and a 6.8-inch Dynamic AMOLED 2X display.','https://loremflickr.com/600/400/samsung,smartphone?lock=6',3799,12,'3'),
    ('7','Sony WH-1000XM5','Industry-leading noise-cancelling wireless over-ear headphones.','https://loremflickr.com/600/400/headphones?lock=7',1099,20,'4'),
    ('8','Apple AirPods Pro 2','Active noise cancellation, USB-C charging and adaptive audio.','https://loremflickr.com/600/400/earbuds?lock=8',749,30,'4'),
    ('9','Bose SoundLink Flex','Rugged, waterproof portable Bluetooth speaker with surprisingly deep bass.','https://loremflickr.com/600/400/speaker?lock=9',649,18,'4'),
    ('10','Louis Vuitton Keepall 55','Monogram canvas weekender travel bag, an enduring symbol of luxury travel.','https://loremflickr.com/600/400/handbag,luxury?lock=10',8900,4,'5'),
    ('11','Montblanc Meisterstück 149','Gold-coated fountain pen, handcrafted from precious black resin.','https://loremflickr.com/600/400/pen,luxury?lock=11',1450,9,'5'),
    ('12','Ray-Ban Aviator Classic','Timeless gold-frame sunglasses with crystal G-15 lenses.','https://loremflickr.com/600/400/sunglasses?lock=12',520,25,'5');

-- Keep the sample order consistent with the refreshed prices
UPDATE orders SET order_no=1001, order_total=79399, order_date=DATE '2026-06-11', shipping_date=DATE '2026-06-14' WHERE id='1';
UPDATE order_details SET qty=1, price=38500, subtotal=38500 WHERE id='1';
UPDATE order_details SET qty=1, price=21900, subtotal=21900 WHERE id='2';
UPDATE order_details SET qty=1, price=18999, subtotal=18999 WHERE id='3';
