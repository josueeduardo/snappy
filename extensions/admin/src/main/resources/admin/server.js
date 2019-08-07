var express = require('express');
var cors = require('cors');
var bodyParser = require('body-parser');
var http = require('http');
var url = require("url");

var fs = require('fs');
var path = require('path');

var app = express();
app.use(bodyParser.json());
app.use(cors());

app.get('/', function (req, res) {
    res.send('Hello World')
});

var _metrics = JSON.parse(fs.readFileSync('./json/metrics.json', 'utf8'));

app.get('/metrics', function (req, res) {
    // _metrics.maxMemory = Math.floor(Math.random() * 100000) + 50000;
    // _metrics.totalMemory = _metrics.maxMemory / 2;
    // _metrics.usedMemory = _metrics.totalMemory / Math.floor(Math.random() * 10) + 1;
    res.json(_metrics)
});

app.get('/logs', function (req, res) {
    console.log("-------------------");
    res.writeHead(200, {'Content-Type': 'application/octet-stream'});
    let _log = fs.readFileSync('./json/jmeter.log', 'utf8');
    res.end(_log, 'blob');
    // res.type('application/octet-stream').sendFile('./json/jmeter.log')
});


app.listen(3000);