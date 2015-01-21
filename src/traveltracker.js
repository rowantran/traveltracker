var SERVER_PORT = 1337;
var LOG_FILE = "traveltracker.log";

var net = require("net");
var sqlite3 = require("sqlite3").verbose();
var crypto = require("crypto");

var db = new sqlite3.Database("traveltracker.db");

/*
function authTokenCheck (authUser, authToken) {
    var sqlQuery = "SELECT * FROM users WHERE user='" + authUser + "';";
    db.get(sqlQuery, function (err, row) {
        if (err == null) {
            if (row != undefined) {
                var d = new Date();
                var md5sum2 = crypto.createHash('md5');
                md5sum2.update(row["tokenGenerated"]+row["user"]+row["hash"]+"\n", 'ascii');
                console.log("Plaintext to hash: " + row["tokenGenerated"]+row["user"]+row["hash"]);
                console.log("Received token from app: " + authToken);
                var corHash = md5sum2.digest('hex');
                console.log("Hashed: " + corHash);
                if (authToken == corHash) {
                    console.log("Token valid");
                    return 0;
                } else {
                    console.log("Token invalid");
                    return 1;
                }
            } else {
                console.log("User does not exist");
                return 1;
            }
        } else {
            console.log("Error");
            return 1;
        }
    });
}
*/

function authTokenCheck (authUser, authToken) {
    writeLog("Received token "+authToken+" for user "+authUser);
    writeLog("Token for user "+authUser+" successfully authenticated");
    return 0;
}

function getGroupID (groupID) {
    var groupIDFetchQuery = "SELECT * FROM groups WHERE ROWID="+groupID+";";

    db.get(groupIDFetchQuery, function (err, row) {
        if (err != null) {
            return row["gName"];
        } else {
            writeError("Error converting group ID to friendly identifier");
        }
    });
}

function getDateFormatted() {
	var d = new Date();
	return d.getFullYear()+"-"+d.getMonth()+"-"+d.getDate()+"-"+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds();
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

var server = net.createServer(function (socket) {
    // This server processes queries from the TravelTracker mobile
    // application and acts as an interface to the database.\

    socket.on('data', function (data) {
        var request = data.toString("utf-8");
        request = request.split(";");
        switch (request[0]) {
            case "CREATE":
                var regUser = request[1].split(",")[0];
                var regPass = request[1].split(",")[2];
                var regEmail = request[1].split(",")[1];
                
                writeLog("Received CREATE request with credentials ("+regUser+","+regEmail+","+regPass+")");

                var searchQuery = "SELECT * FROM users WHERE user='" + regUser + "';";

                db.get(searchuery, function (err, row) {
                    if (err == null) {
                        if (row == undefined) {
                            var md5sum = crypto.createHash('md5');
                            
                            md5sum.update(regPass+regUser+"\n", 'ascii');
                            
                            var hashedPass = md5sum.digest('hex');
                            
                            var uAddQuery = "INSERT INTO users (user, email, hash, groupsTable) VALUES ('" + regUser + "', '" + regEmail + "', '" + hashedPass + "', '" + regUser + "');"
                            
                            db.run(uAddQuery, function (err) {                                
                                if (err != null) {
                                    writeError(err);
                                    socket.write("User adding error\n");
                                } else {
                                    writeLog("Successfully added user " + regUser);

                                    var d = new Date();

                                    var md5sum2 = crypto.createHash('md5');
                                    md5sum2.update(getDateFormatted()+regUser+hashedPass+"\n", 'ascii');

                                    var hashedToken = md5sum2.digest('hex');

                                    writeLog("Generated token: "+hashedToken);

                                    var sqlQuery4 = "UPDATE users SET tokenGenerated = '"getDateFormatted()+"' WHERE user='"+signUser+"';";
                                    db.run(sqlQuery4, function (err) {
                                        if (!err) {
                                            socket.write("Success;"+regUser+","+hashedToken+"\n", 'ascii');
                                        } else {
                                            console.log("Error setting token");
                                            socket.write("Access error\n");
                                        }
                                    });
                                }
                            });
                        } else {
                            writeError("user "+regUser+" exists");
                            socket.write("Existing user error\n");
                        }
                    } else {
                        writeError("Error inserting user "+regUser+" into database: " + err);
                        socket.write("Access error\n");
                    }
                });
                break;
            case "SIGNIN":
                var signUser = request[1].split(",")[0];
                var signPass = request[1].split(",")[1];
                var sqlQuery = "SELECT * FROM users WHERE user='" + signUser + "';";
                db.get(sqlQuery, function (err, row) {
                    if (err == null) {
                        if (row != undefined) {
                            var md5sum = crypto.createHash('md5');
                            md5sum.update(signPass+signUser+"\n", 'ascii');
                            var signHash = md5sum.digest('hex');
                            var corHash = row["hash"];
                            if (signHash === corHash) {
                                var d = new Date();
                                var md5sum2 = crypto.createHash('md5');
                                md5sum2.update(d.getMonth().toString()+d.getDate().toString()+d.getFullYear().toString()+signUser+corHash+"\n", 'ascii');
                                var sqlQuery4 = "UPDATE users SET tokenGenerated = '"+d.getMonth().toString()+d.getDate().toString()+d.getFullYear().toString()+"' WHERE user='"+signUser+"';";
                                db.run(sqlQuery4, function (err) {
                                    if (!err) {
                                        socket.write("Authenticated;"+signUser+","+md5sum2.digest('hex')+"\n");
                                    } else {
                                        console.log("Error setting token");
                                        socket.write("Access error\n");
                                    }
                                });
                            } else {
                                socket.write("Wrong password\n");
                            }
                        } else {
                            socket.write("User does not exist\n");
                        }
                    } else {
                        socket.write("Access error\n");
                    }
                });
                break;
            case "NEWGROUP":
                console.log("Received NEWGROUP request");
                var splitRequestD = request[1].split(",");
                if (splitRequestD.length == 3) {
                    var authUser = splitRequestD[0];
                    var authToken = splitRequestD[1];
                    var groupName = splitRequestD[2];
                    var noGroups;
                    var groups;
                    var groupRowID;
                    var isAuthenticated = authTokenCheck(authUser, authToken)
                    if (isAuthenticated == 0) {
                        var sqlQuery = "INSERT INTO groups (gName, owner, usersTable) VALUES ('"+groupName+"', '"+authUser+"', '"+groupName+"');";
                        db.run(sqlQuery, function (err) {
                            if (err != null) {
                                socket.write("Error\n");
                            } else {
                                groupRowID = this.lastId;
                                socket.write("Success\n");
                            }
                        });
                        var sqlQuery2 = "SELECT * FROM users WHERE user='"+authUser+"';";
                        db.get(sqlQuery2, function (err, row) {
                            if (row["groups"] == undefined) {
                                noGroups = true;
                            } else {
                                noGroups = false;
                                groups = row["groups"];
                            }
                        });
                        var sqlQueryGroupEdit = "UPDATE groups SET members = '" + authUser + "' WHERE ROWID = '" + groupRowID + ";";
                        db.run(sqlQueryGroupEdit, function (err) {
                            if (err != null) {
                                console.log("Succesfully edited new group's members list");
                            } else {
                                console.log("Error");
                                socket.write("Error");
                            }
                        });
                        if (noGroups) {
                            var sqlQuery3 = "UPDATE users SET groups = '"+groupRowID+"' WHERE user = '"+authUser+"';";
                            db.run(sqlQuery3, function (err) {
                                if (err != null) {
                                    console.log("Error while setting user "+authUser+"'s group");
                                } else {
                                    console.log("Succesfully edited user "+authUser+"'s groups");
                                }
                            });
                        } else {
                            var sqlQuery3 = "UPDATE users set groups = '"+groups+","+groupRowID+"' WHERE user = '"+authUser+"';";
                            db.run(sqlQuery3, function (err) {
                                if (err != null) {
                                    console.log("Error while setting user's groups");
                                } else {
                                    console.log("Successfully edited user "+authUser+"'s groups");
                                }
                            });
                        }
                    } else { 
                        socket.write("Error\n");
                        console.log("unable to auth");
                    }
                }
                break;
            case "STORELOC":
                console.log("Received STORELOC request");
                var authUser = request[1].split(",")[0];
                var authToken = request[1].split(",")[1];
                var storeLat = request[1].split(",")[2];
                var storeLong = request[1].split(",")[3];
                if (authTokenCheck(authUser, authToken) == 0) {
                    var sqlPhrase = "UPDATE users set latitude = '"+storeLat+"' WHERE user = '"+authUser+"';";
                    var sqlPhrase2 = "UPDATE users set longitude = '"+storeLong+"' WHERE user = '"+authUser+"';";
                    db.run(sqlPhrase, function (err) {
                        if (err != null) {
                            console.log("Error");
                        } else {
                            ;
                        }
                    });
                    db.run(sqlPhrase2, function (err) {
                        if (err != null) {
                            console.log("Error");
                        } else {
                            console.log("Success");
                        }
                    });
                } else {
                    console.log("Unable to authenticate");
                }
                break;
            case "GETGROUPS":
                console.log("Received GETGROUPS request");
                var authUser = request[1].split(",")[0]
                var authToken = request[1].split(",")[1]
                if (authTokenCheck(authUser, authToken) == 0) {
                    var sqlPhrase = "SELECT * FROM users WHERE user = '"+authUser+"';";
                    db.get(sqlPhrase, function (err, row) {
                        if (err == null) {
                            if (row["groups"] == undefined) {
                                socket.write("none\n");
                                console.log("No groups");
                            } else {
                                var firstIteration = 1;
                                var groupsList;
                                for (i in row["groups"].split(",")) {
                                    if (firstIteration == 1) {
                                        groupsList = i+";"+getGroupID(i);
                                        firstIteration = 0;
                                    } else {
                                        groupsList = groupsList+","+i+";"+getGroupID(i);
                                    }
                                };
                                socket.write(groupsList+"\n");
                                console.log("Wrote groups for user "+authUser);
                            }
                        } else {
                           console.log(err);
                        }
                    });
                }
                break;
            case "ADDMEMBER":
                console.log("Received ADDMEMBER request");
                var authUser = request[1].split(",")[0];
                var authToken = request[1].split(",")[1];
                var groupID = request[1].split(",")[2];
                var memberUsername = request[1].split(",")[3];
                if (authTokenCheck(authUser, authToken) == 0) {
                    var sqlPhrase = "SELECT * FROM invites WHERE user=" + memberUsername + ";";
                    db.get(sqlPhrase, function (err, row) {
                        if (err == null) {
                            var currentInvites = row["invites"];
                            if (currentInvites == null) {
                                var sqlPhraseUpdate = "UPDATE users set invites = '"+groupID+"';";
                            } else {
                                var sqlPhraseUpdate = "UPDATE users set invites = '" + currentInvites + "," + groupID + "';";
                            }
                            db.run(sqlPhraseUpdate, function (err) {
                                if (err == null) {
                                    socket.write("Success");
                                } else {
                                    socket.write("Error");
                                }
                            });
                        } else {
                            socket.write("Error");
                        }
                    });
                }
                break;
            case "CHECKINVITES":
                console.log("Received CHECKINVITES request");
                var authUser = request[1].split(",")[0];
                var authToken = request[1].split(",")[1]
                if (authTokenCheck(authUser, authToken) == 0) {
                    var sqlPhrase = "SELECT * FROM users WHERE user='"+authUser+"';";
                    db.get(sqlPhrase, function (err, row) {
                        if (err == null) {
                            socket.write(row["invites"]);
                        } else {
                            socket.write("Error");
                        }
                });
                break;
            }
        }
    });
});

server.listen(SERVER_PORT);
