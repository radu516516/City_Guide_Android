var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
const fs=require('fs');
var logger = require('morgan');
var bodyParser = require('body-parser');

const dbConnection=require('./configdb');
var api = require('./routes/api');

var app = express();

//in android maybe conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//String post_data = URLEncoder.encode("name","UTF-8");


// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(logger('dev'));
//DOES NOT HANDLE MULTYPART BODIES
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));//CHANGED WAS FALSE//Extended rich objects and array to be encoded in url, json like experience
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));


dbConnection.connect((err)=>{
  if(err){
    console.log('Unable to connect to MySQL.');
  }
  console.log('Succesfully connected to mysql');
  console.log('connected as id ' + dbConnection.threadId);
  console.log('Config:'+JSON.stringify(dbConnection.config));
});


//May lose Connection to MYSQL SERVER network problem,server crasih etc fatal error
function handleDisconnect(connection) {
  connection.on('error', function(err) {
    var now=new Date().toString();
    var log=`#######(MYSQL CONNECTION ON ERROR)\nTIME: ${now}\n#######\n`;
    fs.appendFile('api.log',log,(err)=>{
      if(err){
        console.log('Unable to write to sever.log');
      }
    });
    if (!err.fatal) {
      return;
    }

    if (err.code !== 'PROTOCOL_CONNECTION_LOST') {
      throw err;
    }
    console.log('Re-connecting lost connection: ' + err.stack);
    connection = mysql.createConnection(connection.config);
    handleDisconnect(connection);
    connection.connect();
    var log=`#######(MYSQL RECONNECTION)\nTIME: ${now}\nCONNECTION: ${connection.threadId}\n#######\n`;
    fs.appendFile('api.log',log,(err)=>{
      if(err){
        console.log('Unable to write to sever.log');
      }
    });
  });
}
handleDisconnect(dbConnection);
//End of mysql Setup

app.use('/api', api);




// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});
console.log("EXPRESS APP INITIATED");
module.exports = app;
