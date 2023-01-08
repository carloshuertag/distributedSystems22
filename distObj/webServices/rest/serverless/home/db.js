const mysql = require("mysql");
const config = {
    host: process.env.Server,
    user: process.env.User,
    password: process.env.Password,
    database: process.env.Database,
    port: process.env.Port,
    ssl: {
        ca: '../DigiCertGlobalRootCA.crt.pem',
        rejectUnauthorized: false
    }
}
const connection = mysql.createConnection();
const query = (queryStatement) => new Promise((resolve, reject) => {
    connection.connect((err) => {
        if (err) {
            reject(err);
            return;
        }
        connection.query(queryStatement, (err, results) => {
            if (err) reject(err);
            else resolve(results);
            connection.end();
        });
    });
});
module.exports = query;