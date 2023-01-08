const azureFunctionHandler = require("azure-function-express").createHandler;
const express = require("express");
const mysql = require("mysql");
const app = express();
const connection = mysql.createConnection({
    host: process.env.Server,
    user: process.env.User,
    password: process.env.Password,
    database: process.env.Database,
    port: process.env.Port,
    ssl: {
        ca: '../DigiCertGlobalRootCA.crt.pem',
        rejectUnauthorized: false
    }
});
const query = (queryStatement) => new Promise((resolve, reject) => {
    connection.query(queryStatement, (err, results) => {
        if (err) reject(err);
        else resolve(results);
    });
});
app.get("/api/home/:name", (req, res) => {
    let message = 'Default message.';
    const queryStatement = 'show tables';
    query(queryStatement).then((results) => {
        message = results.length + ' rows returned.';
        res.send("Hello " + req.params.name + "! " + message);
    }).catch((err) => {
        message = 'Error: ' + err;
        res.send("Hello " + req.params.name + "! " + message);
    });

});
app.get("*", (request, response) => {
    response.status(404).end();
});
module.exports = azureFunctionHandler(app);