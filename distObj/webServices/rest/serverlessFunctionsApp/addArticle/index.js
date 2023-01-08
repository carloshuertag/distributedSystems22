const azureFunctionHandler = require("azure-function-express").createHandler;
const express = require("express");
const mysql = require("mysql");
const app = express();
const ADDARTSTTMNT = "insert into articulos (article_name, article_description, price, quantity) values (?, ?, ?, ?);";
const ADDARTPHTSTTMNT = "insert into article_photos (photo, article_id) values (?, (select article_id from articulos where article_name=?));"
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
app.post("/api/addArticle", (req, res) => {
    req.context.log("addArticle posted");
    const { article } = req.body;
    if (!article
        || !article.name || article.name == ''
        || !article.description || article.description == ''
        || !article.price || article.price == ''
        || !article.quantity || article.quantity == '') {
        res.status(400).json({ "message": "Invalid parameters" });
        return;
    }
    req.context.log(JSON.stringify(article));
    res.contentType("application/json");
    res.header("Access-Control-Allow-Origin", "*");
    beginTransaction().then(() => {
        query(ADDARTSTTMNT, [
            article.name,
            article.description,
            article.price,
            article.quantity
        ]).then((results) => {
            req.context.log("Article added successfully");
            if (article.photo && article.photo != '') {
                query(ADDARTPHTSTTMNT, [
                    article.photo,
                    article.name
                ]).then((results) => {
                    req.context.log("Article photo added successfully");
                }).catch((err) => {
                    rollback().then(() => {
                        req.context.log(JSON.stringify(err));
                        res.status(500).json({ "message": err });
                    }).catch((err) => {
                        req.context.log(JSON.stringify(err));
                        res.status(500).json({ "message": err });
                    });
                });
            }
            commit().then(() => {
                req.context.log("Committed successfully");
                res.status(200).end();
            }).catch((err) => {
                req.context.log(JSON.stringify(err));
                res.status(500).json({ "message": err });
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