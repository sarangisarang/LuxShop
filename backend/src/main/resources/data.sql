-- Categories
insert into Category(id,name,image,description) values ('1','Watches','https://loremflickr.com/600/400/luxury,watch?lock=101','Luxury and heritage timepieces');
insert into Category(id,name,image,description) values ('2','Laptops','https://loremflickr.com/600/400/laptop?lock=102','High-performance notebooks');
insert into Category(id,name,image,description) values ('3','Smartphones','https://loremflickr.com/600/400/smartphone?lock=103','Flagship mobile devices');
insert into Category(id,name,image,description) values ('4','Audio','https://loremflickr.com/600/400/headphones?lock=104','Headphones, earbuds and speakers');
insert into Category(id,name,image,description) values ('5','Accessories','https://loremflickr.com/600/400/luxury,accessory?lock=105','Bags, pens and eyewear');

-- Customers
insert into Customer (id,email,first_Name,last_Name,Address,Postcode,City,Phone) values (1,'bekakikalishvili@gmail.com','lilian','mircos','birkestrase50', 40233, 'dusseldorf', 015434232);
insert into Customer (id,email,first_Name,last_Name,Address,Postcode,City,Phone) values (2,'quli2007@mail.ru','Sofio','abuladze','birkestrase50', 40233, 'dusseldorf', 01543423232);
insert into Customer (id,email,first_Name,last_Name,Address,Postcode,City,Phone) values (3,'quli2007@mail.ru','björn','seiffert','birkestrase178', 42344, 'witten', 0127777777);

-- Products (real luxury catalog with images, descriptions and GEL prices)
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('1','Rolex Submariner Date','Iconic 41mm Oystersteel dive watch with a Cerachrom bezel and self-winding Perpetual movement.','https://loremflickr.com/600/400/rolex,watch?lock=1',38500,3,'1');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('2','Omega Speedmaster Moonwatch','The legendary manual-wind chronograph, the first watch worn on the Moon.','https://loremflickr.com/600/400/omega,watch?lock=2',21900,5,'1');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('3','MacBook Pro 16" M3 Max','36GB unified memory, 1TB SSD and a stunning Liquid Retina XDR display.','https://loremflickr.com/600/400/macbook,laptop?lock=3',18999,8,'2');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('4','Dell XPS 15 OLED','15.6-inch 3.5K OLED, Intel Core i9, 32GB RAM and NVIDIA RTX 4070.','https://loremflickr.com/600/400/laptop,ultrabook?lock=4',9499,10,'2');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('5','iPhone 15 Pro Max','Aerospace-grade titanium, A17 Pro chip, 512GB and a 5x telephoto camera.','https://loremflickr.com/600/400/iphone,smartphone?lock=5',4299,15,'3');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('6','Samsung Galaxy S24 Ultra','200MP camera, built-in S Pen and a 6.8-inch Dynamic AMOLED 2X display.','https://loremflickr.com/600/400/samsung,smartphone?lock=6',3799,12,'3');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('7','Sony WH-1000XM5','Industry-leading noise-cancelling wireless over-ear headphones.','https://loremflickr.com/600/400/headphones?lock=7',1099,20,'4');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('8','Apple AirPods Pro 2','Active noise cancellation, USB-C charging and adaptive audio.','https://loremflickr.com/600/400/earbuds?lock=8',749,30,'4');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('9','Bose SoundLink Flex','Rugged, waterproof portable Bluetooth speaker with surprisingly deep bass.','https://loremflickr.com/600/400/speaker?lock=9',649,18,'4');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('10','Louis Vuitton Keepall 55','Monogram canvas weekender travel bag, an enduring symbol of luxury travel.','https://loremflickr.com/600/400/handbag,luxury?lock=10',8900,4,'5');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('11','Montblanc Meisterstück 149','Gold-coated fountain pen, handcrafted from precious black resin.','https://loremflickr.com/600/400/pen,luxury?lock=11',1450,9,'5');
insert into Product(id,product_Name,product_Desc,image_url,Price,Stock,Category_id) values ('12','Ray-Ban Aviator Classic','Timeless gold-frame sunglasses with crystal G-15 lenses.','https://loremflickr.com/600/400/sunglasses?lock=12',520,25,'5');

-- Sample order for customer 1
insert into Orders (id,order_No,order_Date,order_Total,shipping_Date,is_Delivered,Customer_id,order_status) values ('1',1001,'2026-06-11',79399,'2026-06-14',false,'1','Pending');
insert into Order_Details(id,Qty,Price,Subtotal,Order_id,Product_id) values ('1',1,38500,38500,'1','1');
insert into Order_Details(id,Qty,Price,Subtotal,Order_id,Product_id) values ('2',1,21900,21900,'1','2');
insert into Order_Details(id,Qty,Price,Subtotal,Order_id,Product_id) values ('3',1,18999,18999,'1','3');

-- Admin login (BCrypt hash of "1234")
insert into Service_User (id, username, password) values ('1', 'admin', '$2a$10$Req4kJOSY0g8pYA352BrUeWzkn6PGJh4jZs3t0W6SR0ujFIt16Y7K');
insert into User_Role (id, role_name, user_id) values ('1', 'ROLE_ADMIN', '1');
