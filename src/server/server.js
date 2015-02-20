var restify = require("restify");
var bodyParser = require("body-parser");

var pg = require("pg");

var bcrypt = require("bcrypt-nodejs");
var crypto = require("crypto");

var fs = require("fs");

var connectionString = "pg://rj:Mw88itbg@localhost/traveltracker";

function getDateFormatted() {
	var d = new Date();
	return d.getFullYear()+"-"+d.getMonth()+"-"+d.getDate()+" "+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds();
}

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

function sendResponseToClient(serverResponse, responseBody) {
    var responsePayload = JSON.stringify(responseBody);

    serverResponse.writeHead(responseBody.result, {
        'Content-Length': responsePayload.length,
        'Content-Type': 'application/json'
    });
    serverResponse.write(responsePayload);
}


var SERVER_PORT = 1337;
var LOG_FILE = "ttREST.log";

var server = restify.createServer();

server.post('/create/:username', function (req, res) {
});

server.post('/signin/:username', function (req, res) {
});

var server = http.createServer(function (request, response) {
    var method = request.method;
    var url = request.url;

    var slicedUrl = url.slice(1)

    var slicedUrlPieces = slicedUrl.split("/");
    var body = '';
    
    request.on('data', function (data) {
        body += data;
    });

    request.on('end', function() {
        switch (slicedUrlPieces[0]) {
        case "create":
            if (method === 'POST') {
                if (body === '') {
                    var responsePayload = JSON.stringify(
                        {"result": 200}
                    );
                    response.writeHead(400, {
                        'Content-Length': responsePayload.length,
                        'Content-Type': 'application/json'
                    });
                    response.write(responsePayload);
                } else {
                    var parsedBody = qs.parse(body);

                    var username = slicedUrlPieces[1]; 
                    var email = parsedBody["email"];
                    var password = parsedBody["password"];
                    writeLog("Received POST request to /create with details " + username + "," + email);
                    
                    var responseRaw = {
                        "result": 500
                    };

                    pg.connect(connectionString, function(err, client, done) {
                        if (err == null) {
                            var searchForExistingUser = client.query('SELECT * FROM users WHERE username = $1', [username]);

                            searchForExistingUser.on('error', function(err, result) {
                                writeError(err);
                                sendResponseToClient(response, responseRaw);
                                done();
                            });

                            searchForExistingUser.on('row', function(row, result) {
                                result.addRow(row);
                            });

                            searchForExistingUser.on('end', function(result) {
                                if (result.rowCount > 0) {
                                    responseRaw.result = 208;
                                    sendResponseToClient(response, responseRaw);
                                    done();
                                } else {
                                    var randomSalt = crypto.randomBytes(16);
                                    var hashedPass = bcrypt.hashSync(password, randomSalt);

                                    var insertUser = client.query('INSERT INTO users (username, email, hash, salt) VALUES ($1, $2, $3, $4);', 
                                                                  [username, email, hashedPass, randomSalt]);
                                    insertUser.on('error', function(err, result) {
                                        writeError(err.toString());
                                        sendResponseToClient(response, responseRaw);
                                        done();
                                    });
                                    
                                    insertUser.on('end', function(result) {
                                        writeLog(result.rowCount + ' rows inserted');
                                        responseRaw.result = 201;
                                        sendResponseToClient(response, responseRaw);
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
            }
            break;

          case "signin":
            if (body === '') {
                  var responsePayload = JSON.stringify(
                      {"result": 200}
                  );
                response.writeHead(400, {
                    'Content-Length': responsePayload.length,
                    'Content-Type': 'application/json'
                });
                response.write(responsePayload);
            } else {
                var parsedBody = qs.parse(body);
                var username = slicedUrlPieces[1];
                
                var password = parsedBody["password"];

                var responseRaw = {
                    "result": 500
                };

                pg.connect(connectionString, function(err, client, done) {
                    if (err == null) {
                        var fetchDetailsForUser = client.query('SELECT (hash, salt) FROM users WHERE user = $1', [username]);

                        fetchDetailsForUser.on('error', function(err, result) {
                            writeError(err.toString());
                            sendResponseToClient(response, responseRaw);
                            done();
                        });
                        fetchDetailsForUser.on('row', function(row, result) {
                            result.addRow(row);
                        });
                        fetchDetailsForUser.on('end', function(result) {
                            if (result.rows.length > 0) {
                                var enteredHash = bcrypt.hashSync(password, result.rows[0].salt);
                                if (enteredHash === result.rows[0].hash) {
                                    responseRaw.result = 200;
                                    sendResponseToClient(response, responseRaw);
                                } else {
                                    responseRaw.result = 403;
                                    sendResponseToClient(response, responseRaw);
                                    done();
                                }
                            } else {
                                responseRaw.result = 404;
                                sendResponseToClient(response, responseRaw);
                                done();
                            }
                        });
                        
                    } else {
                        writeError(err);
                        sendResponseToClient(response, responseRaw);
                        done();
                    }
                });
                
            }
            break;
        }
    });
});

server.listen(SERVER_PORT);

process.on('SIGINT', function() {
    writeLog("Received SIGINT, gracefully shutting down");
    process.exit();
});
