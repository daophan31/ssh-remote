function WSSHClient() {
};

WSSHClient.prototype._generateEndpoint = function () {
    if (window.location.protocol == 'https:') {
        var protocol = 'wss://';
    } else {
        var protocol = 'ws://';
    }
    var endpoint = protocol+'127.0.0.1:8080/webssh';
    return endpoint;
};

WSSHClient.prototype.connect = function (options) {
    var endpoint = this._generateEndpoint();

    if (window.WebSocket) {
        //if websocket is supported
        this._connection = new WebSocket(endpoint);
    }else {
        // otherwise report an error
        options.onError('WebSocket Not Supported');
        return;
    }

    this._connection.onopen = function () {
        options.onConnect();
    };

    this._connection.onmessage = function (evt) {
        var data = evt.data.toString();
        //data = base64.decode(data);
        options.onData(data);
    };


    this._connection.onclose = function (evt) {
        options.onClose();
    };
};

WSSHClient.prototype.send = function (data) {
    this._connection.send(JSON.stringify(data));
};

WSSHClient.prototype.sendInitData = function (options) {
    //connection parameters
    this._connection.send(JSON.stringify(options));
}

WSSHClient.prototype.sendClientData = function (data) {
    // send command
    this._connection.send(JSON.stringify({"operate": "command", "command": data}))
}

WSSHClient.prototype.closePrevConnect = function () {
    this._connection.close()
}

var client = new WSSHClient();
