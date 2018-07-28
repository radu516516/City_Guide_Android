var express = require('express');
var router = express.Router();

var busTracking= require('./busTracking');
var users=require('./users');
var liste=require('./liste');
var chat=require('./chat');

router.use('/bus-tracking',busTracking);
router.use('/lists',liste);
router.use('/users',users);
router.use('/chat',chat)
module.exports=router;


