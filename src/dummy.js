var net = require("net");
var fs = require("fs");

var SERVER_PORT = 1337;

function getDateFormatted() {
	var d = new Date();
	return d.getFullYear()+"-"+d.getMonth()+"-"+d.getDate()+"-"+getHours()+":"+getMinutes()+":"+getSeconds();
}
function writeLog(msg) {
	var logString = "("+getDateFormatted()+") [LOG] "+msg;
	fs.appendFile("dummy.log", logString, function (err) {
		if (err) throw err;
	});
	console.log(logString);
}
function writeWarn(msg) {
	var warnString = "("+getDateFormatted()+") [WARN] "+msg;
	fs.appendFile("dummy.log", warnString, function (err) {
		if (err) throw err;
	});
	console.log(warnString);
}
function writeError(msg) {
	var errString = "("+getDateFormatted()+") [ERROR] "+msg;
	fs.appendFile("dummy.log", errString, function (err) {
		if (err) throw err;
	});
	console.log(errString);
}

var server = net.createServer(function (socket) {
    // This server acts as a placeholder for a working server that returns constant values
    // without processing any information: it will be a constant in the test flow.
    socket.on('data', function (data) {
        var request = data.toString("utf-8");
        request = request.split(";");
        switch (request[0]) {
            case "CREATE":
                writeLog("Dummy server received account creation request")
                socket.write("Success;username,286755fad04869ca523320acce0dc6a4\n", 'ascii');
                break;
            case "SIGNIN":
                writeLog("Dummy server received sign in request");
                socket.write("Authenticated;username,286755fad04869ca523320acce0dc6a4\n", 'ascii');
                break;
            case "NEWGROUP":
                writeLog("Dummy server received group creation request");
                socket.write("Success\n");
                break;
            case "STORELOC":
            	writeLog("Dummy server received location storing request");
                break;
            case "GETGROUPS":
            	writeLog("Dummy server received group poll request");
                socket.write("1234;testGroup");
                break;
            case "ADDMEMBER":
            	writeLog("Dummy server received group member add request");
                socket.write("Success");
                break;
            case "CHECKINVITES":
            	writeLog("Dummy server received invite poll request");
                socket.write("f123,g1235");
                break;
        }
    });
});

server.listen(SERVER_PORT);
