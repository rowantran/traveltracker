// server.js

// Express setup
// ==================
var express = require('express');
var bodyParser = require('body-parser');

var app = express();
app.use(bodyParser.json());                        // Parse JSON body
app.use(bodyParser.urlencoded({extended: true}));  // Parse POST data

var router = express.Router();


// Other requires
// ==================
var pg = require("pg");

var bcrypt = require("bcrypt-nodejs");
var crypto = require("crypto");

var fs = require("fs");


// Define constants
// ==================
var connectionString = "pg://rj:Mw88itbg@localhost/traveltracker";
var SERVER_PORT = 1337;
var LOG_FILE = "server.log";


// Logging functions
// ==================
function writeLog(msg) {
	var logString = "("+getDateFormatted()+") [LOG] "+msg;
	fs.appendFile(LOG_FILE, logString+"\n", function (err) {
		if (err) throw err;
	});
	console.log(logString);
}

function writeWarn(msg) {
	var warnString = "("+getDateFormatted()+") [WARN] "+msg;
	fs.appendFile(LOG_FILE, warnString+"\n", function (err) {
		if (err) throw err;
	});
	console.log(warnString);
}

function writeError(msg) {
	var errString = "("+getDateFormatted()+") [ERROR] "+msg;
	fs.appendFile(LOG_FILE, errString+"\n", function (err) {
		if (err) throw err;
	});
	console.log(errString);
}


// Various functions
// ==================
function getDateFormatted() {
	var d = new Date();
	return d.toLocaleString();
}


// Define router middleware
// ===========================
router.use(function (req, res, next) {

    // Log all requests to console
    writeLog(req.method + " " + req.url);

    next();
});


// Define account-related routing for server
// =============================================
router.post('/create/:username', function (req, res) {
    if (req.body.email === undefined || req.body.password === undefined) {
        res.sendStatus(400);
    } else {
        var username = req.params.username;
        var email = req.body.email;
        var password = req.body.password;
        
        pg.connect(connectionString, function(err, client, done) {
            if (err == null) {
                var searchForExistingUser = client.query('SELECT * FROM users WHERE username = $1', [username]);
                
                searchForExistingUser.on('error', function(err, result) {
                    writeError(err);
                    res.sendStatus(500);
                    done();
                });
                
                searchForExistingUser.on('row', function(row, result) {
                    result.addRow(row);
                });
                
                searchForExistingUser.on('end', function(result) {
                    if (result.rowCount > 0) {
                        res.sendStatus(208);
                        done();
                    } else {
                        var randomSalt = bcrypt.genSaltSync(8);
                        var hashedPass = bcrypt.hashSync(password, randomSalt);
                        
                        var insertUser = client.query('INSERT INTO users (username, email, hash, salt) VALUES ($1, $2, $3, $4);', 
                                                      [username, email, hashedPass, randomSalt]);

                        insertUser.on('error', function(err, result) {
                            writeError(err.toString());
                            res.sendStatus(500);
                            done();
                        });
                        
                        insertUser.on('end', function(result) {
                            writeLog(result.rowCount + ' rows inserted');
                            res.sendStatus(201);
                            done();
                        });
                    }
                });
            } else {
                writeError(err);
                sendResponseToClient(response, responseRaw);
            }
        });
    }
});

router.post('/signin/:username', function (req, res) {
    if (req.body.password === undefined) {
        res.sendStatus(400);
    } else {
        pg.connect(connectionString, function(err, client, done) {
            var username = req.params.username;
            var password = req.body.password;

            if (err == null) {
                var fetchDetailsForUser = client.query("SELECT hash, salt FROM users WHERE username = $1", [username]);
                
                fetchDetailsForUser.on('error', function(err, result) {
                    writeError(err.toString());
                    res.sendStatus(500);
                    done();
                });

                fetchDetailsForUser.on('row', function(row, result) {
                    result.addRow(row);
                });

                fetchDetailsForUser.on('end', function(result) {
                    if (result.rows.length > 0) {
                        var enteredHash = bcrypt.hashSync(password, result.rows[0].salt);
                        if (enteredHash === result.rows[0].hash) {
                            res.sendStatus(200);
                            done();
                        } else {
                            res.sendStatus(403);
                            done();
                        }
                    } else {
                        res.sendStatus(404);
                        done();
                    }
                });
                
            } else {
                writeError(err);
                res.sendStatus(500);
                done();
            }
        });
    }
});

app.use("/", router);


// Start server
// ==================
app.listen(SERVER_PORT);
writeLog("Listening on port " + SERVER_PORT);


// Add listener for SIGINT
// ===========================
process.on('SIGINT', function() {
    writeLog("Received SIGINT, gracefully shutting down");
    process.exit();
});
