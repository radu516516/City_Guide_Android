const connection=require('../configdb');

//insert user Register user
exports.addUser=function(userData,callback){
    var sql="INSERT INTO USER(username,pass) VALUES(?,?)";
    connection.query(sql,[userData.name,userData.pass],function(err,results){
        if(err){
            console.log("Nu am putut sa inserez userul in baza de date");
            console.log(err);
            callback(true);
            return;
        }
        callback(false,results);//pot fi multe rezultate
    });
}
//for login
//Check login user(by name, daca exista userul)
exports.getUser=function(name,callback){
    var sql="SELECT * FROM USER WHERE username=?;";//name should be unique
    connection.query(sql,[name],function(err,result){
        if(err){
            callback(true);//error
            return;
        }
        console.log(result);
        callback(false,result);
    })
}

exports.deleteUser=function(id,callback){
    var sql="DELETE FROM USER WHERE id=?";
    connection.query(sql,[id],function(err,results){
        console.log('connected as id ' + connection.threadId);
        if(err){
            console.log("Nu am putut sa sterg userul cu id:"+id+" din baza de date!");
            console.log(err);
            callback(true);
            return;
        }
        //daca avem datele
        callback(false,results);//pot fi multe rezultate
    });
}