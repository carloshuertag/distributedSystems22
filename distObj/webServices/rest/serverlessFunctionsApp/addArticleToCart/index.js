const azureFunctionHandler = require("azure-function-express").createHandler;
const express = require("express");
const mysql = require("mysql");
const app = express();
const GET_ARTICLE_QUANTITY = "select articulos.quantity from articulos where article_id = ?;";
const ADD_ARTICLE_TO_CART = "insert into carrito_compra (article_id, quantity) values (?, ?);";
const UPDATE_ARTICLE_QUANTITY_ADD = "update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity - carrito_compra.quantity where articulos.article_id = ?;";
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
const query = (queryStatement, values) => new Promise((resolve, reject) => {
    connection.query(queryStatement, values, (err, results) => {
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
app.post("/api/addArticleToCart", (req, res) => {
    req.context.log("addArticleToCart posted");
    const { article } = req.body;
    if (!article
        || !article.id || article.id == ''
        || !article.quantity || article.quantity == '') {
        res.status(400).json({ "message": "Invalid article" });
        return;
    }
    req.context.log(JSON.stringify(article));
    res.header("Access-Control-Allow-Origin", "*");
    query(GET_ARTICLE_QUANTITY, [article.id]).then((results) => {
        if (!results[0].quantity
            || results[0].quantity < article.quantity
            || results[0].quantity == 0) {
            res.status(400).json({ "message": "Article is not available" });
            return;
        }
        req.context.log("Article is available");
        beginTransaction().then(() => {
            query(ADD_ARTICLE_TO_CART, [
                article.id,
                article.quantity
            ]).then(() => {
                query(UPDATE_ARTICLE_QUANTITY_ADD, [article.id]).then(() => {
                    commit().then(() => {
                        req.context.log("Article added to cart");
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
    }).catch((err) => {
        req.context.log(JSON.stringify(err));
        res.status(500).json({ "message": err });
    });
});
module.exports = azureFunctionHandler(app);