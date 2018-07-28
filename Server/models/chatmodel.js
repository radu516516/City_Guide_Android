
const connection=require('../configdb');


exports.getChatGroups=function(limit,offset,city_id,callback){
    //Later add order by ?o=mr parameter
    var sql="SELECT * FROM chat_room WHERE city=? LIMIT ? OFFSET ?";
    connection.query(sql,[city_id,limit,offset],function(err,results){
        if(err){
            callback(true);
            return;
        }
        callback(false,results);
    });
};


exports.addGroup=function(groupData,callback){
    var sql="INSERT INTO chat_room(creator,city,created_at,name,language) VALUES(?,?,NOW(),?,?)";
    connection.query(sql,[groupData.creator,groupData.city,groupData.name,groupData.language],function(err,results){
        if(err){
            console.log(err);
            callback(true);//create error poate doar unique key are deja un grup in acest oras
            return;
        }
        callback(false,results);
    });
}

exports.getRowCount=function(city_id,callback){
    var sql = 'SELECT count(1) AS nrRows FROM chat_room WHERE city=?';
    connection.query(sql,[city_id],function(err,result){
        if(err){
            console.log(err);
            callback(true);
            return;
        }
        callback(false,result);
    });
}
