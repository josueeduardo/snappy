var express = require('express');
var cors = require('cors');
var bodyParser = require('body-parser');
var http = require('http');
var url = require("url");

var fs = require('fs');
var path = require('path');

var multer = require('multer'); //file upload
var upload = multer({ dest: 'uploads/' });

var fileUpload = require('express-fileupload');

var app = express();
app.use(bodyParser.json());
app.use(cors());

app.get('/', function (req, res) {
    res.send('Hello World')
});

var _videos = JSON.parse(fs.readFileSync('./json/videos.json', 'utf8'));

app.post('/v1/videos', function (req, res) {
    var video = req.body;

    if (!video.title) {
        console.log("No video title");
        res.status(400).send();
        return;
    }

    video.username = "manolo";
    video.likes = 0;
    video.commentCount = 0;
    video.date = new Date();
    video.id = Date.now().toString();

    _videos.unshift(video);
    res.status(201).json(video)
});

app.get('/v1/videos', function (req, res) {
    res.json(_videos)
});

app.get('/v1/videos/latest', function (req, res) {
    if (_videos.length > 3) {
        res.json(_videos.slice(0, 3));
        return;
    }
    res.json(_videos);

});

app.get('/v1/videos/:videoId', function (req, res) {
    var videoId = req.params.videoId;
    _videos.forEach(function (video) {
        if (video.id === videoId) {
            res.json(video);
        }
    });
    res.status(404).send();
});

var comments = {};

app.get('/v1/videos/:videoId/comments', function (req, res) {
    var videoId = req.params.videoId;
    var videoComments = comments[videoId] ? comments[videoId] : [];
    res.json(videoComments)
});

var updateCommentCount = function (videoId) {
    _videos.forEach(function (video) {
        if (video.id.toString() === videoId) {
            video.commentCount++;
        }
    });
};

app.post('/v1/videos/:videoId/comments', function (req, res) {
    var videoId = req.params.videoId;
    var comment = req.body;

    comment.likes = 0;
    comment.id = Date.now();
    comment.replies = [];

    updateCommentCount(videoId);

    if (!comments[videoId]) {
        comments[videoId] = [];
    }
    comments[videoId].unshift(comment);
    res.json(comment)
});

//reply
app.post('/v1/videos/:videoId/comments/:commentId/replies', function (req, res) {
    var videoId = req.params.videoId;
    var commentId = req.params.commentId;
    var reply = req.body;

    reply.id = Date.now();
    reply.date = new Date();
    reply.likes = 0;

    if (!comments[videoId]) {
        res.json(404).send();
        return;
    }

    updateCommentCount(videoId);

    var videoComment = comments[videoId];
    if (videoComment.length > 0) {
        videoComment.map(function (comment) {//only high level comment
            if (comment.id.toString() === commentId) {
                comment.replies.unshift(reply);
            }
        });
    }
    res.json(reply)
});

var videoCategories = JSON.parse(fs.readFileSync('./json/categories.json', 'utf8'));
app.get('/v1/video/categories', function (req, res) {
    res.json(videoCategories);
});

app.post('/v1/videos', function (req, res) {
    var videoId = req.params.videoId;
    var comment = req.body;
    comment.likes = 0;
    comment.commentCount = 0;

    comments.unshift(comment);
    res.json(comment)
});


//file upload
app.post('/video-upload', upload.array('fileToUpload'), function (req, res, next) {

    console.log("Uploading: " + JSON.stringify(req.files));

    // req.files is array of `photos` files
    // req.body will contain the text fields, if there were any
})



app.listen(3000);