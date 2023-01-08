const azureFunctionHandler = require("azure-function-express").createHandler;
const express = require("express");
const mysql = require("mysql");
const SRCHARTSTTMNT = "select articulos.article_id as id, article_photos.photo, articulos.article_name as name, articulos.article_description as description, articulos.price from articulos inner join article_photos on articulos.article_id = article_photos.article_id where article_name like ? or article_description like ?;";
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
const query = (queryStatement, values) => new Promise((resolve, reject) => {
    connection.query(queryStatement, values, (err, results) => {
        if (err) reject(err);
        else resolve(results);
    });
});
app.post("/api/searchArticles", (req, res) => {
    req.context.log("searchArticles posted");
    const search = req.body;
    req.context.log(JSON.stringify(search));
    if (!search || !search.keyword) {
        res.status(400).json("Invalid keyword");
        return;
    }
    const keyword = '%' + search.keyword + '%';
    res.contentType("application/json");
    res.header("Access-Control-Allow-Origin", "*");
    query(SRCHARTSTTMNT, [
        keyword,
        keyword
    ]).then((results) => {
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