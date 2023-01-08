const azureFunctionHandler = require("azure-function-express").createHandler;
const express = require("express");
const mysql = require("mysql");
const GET_ARTICLES_IN_CART = "select carrito_compra.article_id as id, article_photos.photo, articulos.article_name as name, carrito_compra.quantity, articulos.price from carrito_compra inner join article_photos on carrito_compra.article_id = article_photos.article_id inner join articulos on carrito_compra.article_id = articulos.article_id;";
const app = express();
const connection = mysql.createConnection({
    host: process.env.DBSERVER,
    user: process.env.DBUSER,
    password: process.env.DBUSRPSSWRD,
    database: process.env.DBNAME,
    port: process.env.DBPORT,
    ssl: {
        ca: process.env.DBCA,
        rejectUnauthorized: false
    }
});
const query = (queryStatement) => new Promise((resolve, reject) => {
    connection.query(queryStatement, (err, results) => {
        if (err) reject(err);
        else resolve(results);
    });
});
app.post("/api/getCart", (req, res) => {
    req.context.log("getCart posted");
    res.header("Access-Control-Allow-Origin", "*");
    query(GET_ARTICLES_IN_CART).then((results) => {
        results.forEach((result) => {
            result.photo = result.photo.toString();
        });
        req.context.log(JSON.stringify(results).substring(0, 80));
        res.json(results);
    }).catch((err) => {
        req.context.log(JSON.stringify(err));
        res.status(500).json({ "message": err });
    });
});
module.exports = azureFunctionHandler(app);