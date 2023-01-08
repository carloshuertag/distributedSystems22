const azureFunctionHandler = require("azure-function-express").createHandler;
const express = require("express");
const mysql = require("mysql");
const UPDATE_ARTCILE_QUANTITY_RESET = "update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity + carrito_compra.quantity;";
const DELETE_CART = "delete from carrito_compra;";
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
const beginTransaction = () => new Promise((resolve, reject) => {
    connection.beginTransaction((err) => {
        if (err) reject(err);
        else resolve();
    });
});
const commit = () => new Promise((resolve, reject) => {
    connection.commit((err) => {
        if (err) reject(err);
        else resolve();
    });
});
const rollback = () => new Promise((resolve, reject) => {
    connection.rollback((err) => {
        if (err) reject(err);
        else resolve();
    });
});
app.post("/api/emptyCart", (req, res) => {
    req.context.log("emptyCart posted");
    res.header("Access-Control-Allow-Origin", "*");
    beginTransaction().then(() => {
        query(UPDATE_ARTCILE_QUANTITY_RESET).then(() => {
            query(DELETE_CART).then(() => {
                commit().then(() => {
                    req.context.log("Cart emptied");
                    res.status(200).end();
                }).catch((err) => {
                    rollback().then(() => {
                        req.context.log(JSON.stringify(err));
                        res.status(500).json({ "message": err });
                    }).catch((err) => {
                        req.context.log(JSON.stringify(err));
                        res.status(500).json({ "message": err });
                    });
                });
            }).catch((err) => {
                rollback().then(() => {
                    req.context.log(JSON.stringify(err));
                    res.status(500).json({ "message": err });
                }).catch((err) => {
                    req.context.log(JSON.stringify(err));
                    res.status(500).json({ "message": err });
                });
            });
        }).catch((err) => {
            rollback().then(() => {
                req.context.log(JSON.stringify(err));
                res.status(500).json({ "message": err });
            }).catch((err) => {
                req.context.log(JSON.stringify(err));
                res.status(500).json({ "message": err });
            });
        });
    }).catch((err) => {
        req.context.log(JSON.stringify(err));
        res.status(500).json({ "message": err });
    });
});
module.exports = azureFunctionHandler(app);