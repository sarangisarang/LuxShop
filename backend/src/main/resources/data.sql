insert into Category(id,name,image,description) values ('1','book','literatura','beletresica');
insert into Category(id,name,image,description) values ('2','Computer',' ','Computer');
insert into Category(id,name,image,description) values ('3','Phone',' ','SmartPhone');
insert into Customer (id,email,first_Name,last_Name,Password,Address,Postcode,City,Phone) values (1,'bekakikalishvili@gmail.com','lilian','mircos','dushqu','birkestrase50', 40233, 'dusseldorf', 015434232);
insert into Customer (id,email,first_Name,last_Name,Password,Address,Postcode,City,Phone) values (2,'quli2007@mail.ru','Sofio','abuladze','Secreto','birkestrase50', 40233, 'dusseldorf', 01543423232);
insert into Customer (id,email,first_Name,last_Name,Password,Address,Postcode,City,Phone) values (3,'quli2007@mail.ru','björn','seiffert','Secreto','birkestrase178', 42344, 'witten', 0127777777);
insert into Orders (id,order_No,order_Date,order_Total,shipping_Date,is_Delivered,Customer_id,order_status) values ('1',10,'2015-10-11',30,'2013-02-02','Waiting','1','Pending');
insert into Product(id,product_Name,product_Desc,image1,image2,image3,Price,Stock,Category_id) values ('1','Book','Dostoyevsky','dushqu1','dushqu2','dushqu3',40,200,'1');
insert into Product(id,product_Name,product_Desc,image1,image2,image3,Price,Stock,Category_id) values ('2','Apple','MacBook16','dushqu1','dushqu2','dushqu3',2000,100,'3');
insert into Product(id,product_Name,product_Desc,image1,image2,image3,Price,Stock,Category_id) values ('3','Apple','MacBook13','dushqu1','dushqu2','dushqu3',1000,100,'2');
insert into Order_Details(id,Qty,Price,Subtotal,Order_id,Product_id) values ('1',1,20,20,'1','1');
insert into Order_Details(id,Qty,Price,Subtotal,Order_id,Product_id) values ('2',2,2000,4000,'1','2');
insert into Order_Details(id,Qty,Price,Subtotal,Order_id,Product_id) values ('3',2,1000,2000,'1','3');
-- Password is the BCrypt hash of "1234" (see Project #5: Fix PasswordEncoder / Fix data.sql seed).
insert into Service_User (id, username, password) values ('1', 'admin', '$2a$10$Req4kJOSY0g8pYA352BrUeWzkn6PGJh4jZs3t0W6SR0ujFIt16Y7K');
insert into User_Role (id, role_name, user_id) values ('1', 'ROLE_ADMIN', '1');