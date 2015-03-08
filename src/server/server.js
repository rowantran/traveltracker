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
var jwt = require("jsonwebtoken");

var fs = require("fs");


// Define constants
// ==================
var connectionString = "pg://rj:Mw88itbg@localhost/traveltracker";
var SERVER_PORT = 1337;
var LOG_FILE = "server.log";
var ENCRYPTION_KEY = fs.readFileSync("tokenSecretPassword.key");


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

function getUnixTime() {
    var d = new Date();
    return d.getTime();
}

function checkJWT(token) {
    var tokenEncodedUsername = jwt.decode(token).username;
    
    pg.connect(connectionString, function(err, client, done) {
        if (err == null) {
            var fetchSecretForUser = client.query("SELECT token FROM users WHERE username = $1", [tokenEncodedUsername]);
            
            fetchSecretForUser.on('err', function(err, result) {
                writeErr(err.toString());
                return 2;
            });

            fetchSecretForUser.on('row', function(row, result) {
                result.addRow(row);
            });

            fetchDetailsForUser.on('done', function(result) {
                var secret = result.rows[0].token;
                var secretDecrypter = crypto.createDecipher('aes192', ENCRYPTION_KEY);
                secretDecrypter.update(secret, 'hex');
                secret = secretDecrypter.final();
                try {
                    var payload = jwt.verify(token, secret);
                    return payload.username;
                } catch (err) {
                    return 0;
                }
            });
        } else {
            writeErr(err);
            return 1;
        }
    });
}

// Define router middleware
// ===========================
router.use(function (req, res, next) {

    // Log all requests to console
    writeLog(req.method + " " + req.url);

    next();
});


// Define unauthenticated routing for server
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
                        
                        var insertUser = client.query('INSERT INTO users (username, email, hash, salt, token) VALUES ($1, $2, $3, $4, $5);', 
                                                      [username, email, hashedPass, randomSalt, ""]);

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
                            var secret = crypto.randomBytes(256).toString('base64');
                            var secretEncrypter = crypto.createCipher('aes192', ENCRYPTION_KEY);
                            secretEncrypter.update(secret);
                            var encryptedSecret = secretEncrypter.final('hex');

                            var insertTokenIntoDatabase = client.query("UPDATE users SET token = $1 WHERE username = $2", 
                                                                       [encryptedSecret, username]);
                            
                            insertTokenIntoDatabase.on('error', function (err, result) {
                                writeError(err.toString());
                                res.sendStatus(500);
                                done();
                            });

                            insertTokenIntoDatabase.on('end', function (result) {
                                var token = jwt.sign({'username': username}, secret);
                                res.status(200).send(token);
                                done();
                            });
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


// Define authenticated routing
// ====================================
var authRouter = express.Router();
authRouter.use(function (req, res, next) {
    if (req.headers.authorization === undefined) {
        res.sendStatus(401);
    } else {
        next();
    }
authRouter.get('/invites', function (req, res) {
    var token = req.headers.authorization.split(" ")[1];
    
    var authenticated = checkJWT(token);
    if (authenticated === 1) {
        res.sendStatus(403);
    } else if (authenticated === 2) {
        res.sendStatus(500);
    } else {
    }
});
    
router.use("/auth/", authRouter);


// Use router
// ==================
app.use("/", router);  // Configure router object as final middleware for root path


// Start server
// ==================
app.listen(SERVER_PORT);
writeLog("Listening on port " + SERVER_PORT);


// Add listener for SIGINT
// ===========================
process.on('SIGINT', function() {
    writeLog("Received SIGINT, shutting down");
    process.exit();
});
