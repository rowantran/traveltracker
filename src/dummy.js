var net = require("net");
var fs = require("fs");

var SERVER_PORT = 1337;

function getDateFormatted() {
	var d = new Date();
	return d.getFullYear()+"-"+d.getMonth()+"-"+d.getDate()+"-"+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds();
}
function writeLog(msg) {
	var logString = "("+getDateFormatted()+") [LOG] "+msg;
	fs.appendFile("dummy.log", logString+"\n", function (err) {
		if (err) throw err;
	});
	console.log(logString);
}
function writeWarn(msg) {
	var warnString = "("+getDateFormatted()+") [WARN] "+msg;
	fs.appendFile("dummy.log", warnString+"\n", function (err) {
		if (err) throw err;
	});
	console.log(warnString);
}
function writeError(msg) {
	var errString = "("+getDateFormatted()+") [ERROR] "+msg;
	fs.appendFile("dummy.log", errString+"\n", function (err) {
		if (err) throw err;
	});
	console.log(errString);
}
var server = net.createServer(function (socket) {
    // This server acts as a placeholder for a working server that returns constant values
    // without processing any information: it will be a constant in the test flow.

    // All returned values based on simulated account username/password pair (username, password) sent from client

    socket.on('data', function (data) {
        var request = data.toString("utf-8");
        request = request.split(";");
        switch (request[0]) {
            case "CREATE":
                writeLog("Dummy server received account creation request")
                socket.write("Success;username,286755fad04869ca523320acce0dc6a4\n");
                break;
            case "SIGNIN":
                writeLog("Dummy server received sign in request");
                socket.write("Authenticated;username,286755fad04869ca523320acce0dc6a4\n");
                break;
            case "NEWGROUP":
                writeLog("Dummy server received group creation request");
                socket.write("Success\n");
                break;
            case "STORELOC":
            	writeLog("Dummy server received location storing request");
                break;
            case "SUBMITDESTINATION":
                writeLog("Dummy server received destination submittal request");
                writeLog("Coordinates received were " + request[1].split(",")[3] + "," + request[1].split(",")[4]);
                socket.write("Success\n");
                break;
            case "GETDESTINATION":
                writeLog("Dummy server received destination retrieval request");
                socket.write("39.234754,-121.754087\n");
            case "LOCATIONARRIVAL":
                writeLog("Dummy server received group destination arrival notification");
                socket.write("Success\n");
            case "GETGROUPS":
            	writeLog("Dummy server received group poll request");
                socket.write("1234;testGroup\n");
                break;
            case "GETGROUPMEMBERS":
                writeLog("Dummy server received group member poll request");
                socket.write("snoop420;mlg360;optic420;faze360noscope\n");
            case "ADDMEMBER":
            	writeLog("Dummy server received group member add request");
                socket.write("Success\n");
                break;
            case "CHECKINVITES":
            	writeLog("Dummy server received invite poll request");
                socket.write("f123;1234;username123,g1235;4252;username123\n");
                break;
            case "REPLYINVITE":
                if (request[1].split(",")[3] === "y") {
                    writeLog("Dummy server received invite acceptance request");
                } else {
                    writeLog("Dummy server received invite denial request");
                }
                socket.write("Success\n");
                break;
        }
    });
});

server.listen(SERVER_PORT);
