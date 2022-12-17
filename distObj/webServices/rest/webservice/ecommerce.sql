create database ecommerce;
use ecommerce;
create table articulos ( 
    article_id integer auto_increment primary key, 
    article_name nvarchar(100) not null, 
    article_description nvarchar(256) not null, 
    price decimal(10,2) not null, 
    quantity integer not null 
);
create table article_photos ( 
    article_photo_id integer auto_increment primary key, 
    photo longblob, 
    article_id integer not null 
);
create table carrito_compra ( 
    article_id integer primary key, 
    quantity integer not null 
);
alter table article_photos add foreign key (article_id) references articulos(article_id); 
alter table carrito_compra add foreign key (article_id) references articulos(article_id); 
/*
-- SQL statements to be executed example
insert into articulos (article_name, article_description, price, quantity) values ('Cable USB C a USB C', 'Cable de USB tipo C a USB tipo C', 10.00, 10); 
insert into articulos (article_name, article_description, price, quantity) values ('Cable USB C a USB A', 'Cable de USB tipo C a USB tipo A', 20.00, 20); 
insert into articulos (article_name, article_description, price, quantity) values ('Jabón Aurrera', 'Jabón de manos Aurrera', 30.00, 30); 
insert into articulos (article_name, article_description, price, quantity) values ('Lácteo Aurrera', 'Producto sustituto de lácteo Aurrera', 40.00, 40); 
insert into article_photos (photo, article_id) values ('photo1', 1); 
insert into article_photos (photo, article_id) values ('photo2', 2); 
insert into article_photos (photo, article_id) values ('photo3', 3); 
insert into article_photos (photo, article_id) values ('photo4', 4); 
select articulos.article_id, article_photos.photo, articulos.article_name, articulos.article_description, articulos.price from articulos inner join article_photos on articulos.article_id = article_photos.article_id where article_name like '%Cable%' or article_description like '%Cable%'; 
select articulos.article_id, article_photos.photo, articulos.article_name, articulos.article_description, articulos.price from articulos inner join article_photos on articulos.article_id = article_photos.article_id where article_name like '%Aurrera%' or article_description like '%Aurrera%'; 
select articulos.quantity from articulos where article_id = 1; 
select articulos.quantity from articulos where article_id = 3; 
-- begin transaction 
insert into carrito_compra (article_id, quantity) values (1, 1); 
update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity - carrito_compra.quantity where articulos.article_id = 1;
-- commit transaction 

-- begin transaction 
insert into carrito_compra (article_id, quantity) values (3, 3); 
update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity - carrito_compra.quantity where articulos.article_id = 3;
-- commit transaction 

select carrito_compra.article_id, article_photos.photo, articulos.article_name, carrito_compra.quantity, articulos.price from carrito_compra inner join article_photos on carrito_compra.article_id = article_photos.article_id inner join articulos on carrito_compra.article_id = articulos.article_id; 

-- begin transaction 
update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity + carrito_compra.quantity where articulos.article_id = 1;
delete from carrito_compra where article_id = 1; 
-- commit transaction 

-- begin transaction 
update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity + carrito_compra.quantity where articulos.article_id = 3;-- commit transaction 
delete from carrito_compra where article_id = 3; 

-- begin transaction 
insert into carrito_compra (article_id, quantity) values (1, 1); 
update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity - carrito_compra.quantity where articulos.article_id = 1;
-- commit transaction 

-- begin transaction 
insert into carrito_compra (article_id, quantity) values (3, 3); 
update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity - carrito_compra.quantity where articulos.article_id = 3;
-- commit transaction 

select carrito_compra.article_id, article_photos.photo, articulos.article_name, carrito_compra.quantity, articulos.price from carrito_compra inner join article_photos on carrito_compra.article_id = article_photos.article_id inner join articulos on carrito_compra.article_id = articulos.article_id; 

-- begin transaction, n updates opt1
-- delete from carrito_compra;antity + 1 where article_id = 1;
-- update articulos set quantity = quantity + 3 where article_id = 3;
-- update articulos set quantity = qu
-- commit transaction

-- begin transaction, n updates op2
update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity + carrito_compra.quantity;
delete from carrito_compra;
-- commit transaction

select * from articulos;
select * from carrito_compra;
*/