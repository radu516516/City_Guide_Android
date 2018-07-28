
const connection=require('../configdb');


exports.getAllLists=function(callback){//Aici ia listele ca nume , title si thumbnail
    var sql="SELECT * FROM list;";
    connection.query(sql,function(err,results){
            if(err){
            console.log(err);
            callback(true);
            return;
        }
        callback(false,results);
    });
}
exports.getListById=function(id,callback){
    var sql="SELECT * FROM list WHERE id=?";
    connection.query(sql,[id],function(err,results){
        if(err){
            console.log(err);
            callback(true);
            return;
        }
        callback(false,results);
    });
}
//CHECK BEFORE ADDING AN ELEMENT OR DELETING THE LIST
//MAI INTAI SE CREAZA LISTA,DUPAIA SE ADUGA ELEMENTE UNA CATE UNA, DACA NU SA ADAUGAT NIMIC STERG LISTA
exports.checkListCreator=function(creator,id,callback){
    var sql="SELECT * FROM list WHERE creator=? AND id=?";
    connection.query(sql,[creator,id],function(err,results){
        if(err){
            console.log(err);
            callback(true);
            return;
        }
        callback(false,results);
    });
}






//REST API  PAGINATIONS ( NU DAU TOATE LISTELE DEODATA)
//Limit 10,Offset 10(all rows starting at 10)
//LIMIT DATA SELECTION
//1.First we need the client to be able to tell us which records they want. This is implemented with query strings. 
//2.We can allow the user to specify two parameters: Page and Limit.
//(Limit the number of rows returned)

//# GET LISTE
exports.getListsPaginationTest=function(limit,offset,city_id,callback){
    //Later add order by ?o=mr parameter
    var sql="SELECT * FROM list WHERE id IN (SELECT DISTINCT id_list FROM (list_details ld INNER JOIN location l ON ld.id_location=l.id ) INNER JOIN city c ON c.id=l.city_id WHERE c.id=?) LIMIT ? OFFSET ?;";
    connection.query(sql,[city_id,limit,offset],function(err,results){
        if(err){
            callback(true);
            return;
        }
        callback(false,results);//10 liste 20 etc
    });
};
//# GET LIST ITEMS OF LIST
exports.getListItems=function(list_id,callback){
    connection.query("SELECT location.id,name,description,lat,lng,thumbnail FROM location INNER JOIN list_details detail ON location.id = detail.id_location WHERE id_list=?;"
    ,[list_id],function(err,results){
        if(err){
            callback(true);
            return;
        }
        callback(false,results);
    });
}


//# GET NR DE LISTE (userd for pagination) (cate liste poate sa vada max)
exports.getRowCount=function(city_id,callback){
    var sql = 'SELECT COUNT(DISTINCT id_list) as nrRows FROM (list_details ld INNER JOIN location l ON ld.id_location=l.id ) INNER JOIN city c ON c.id=l.city_id WHERE c.id=?';
    connection.query(sql,[city_id],function(err,result){
        if(err){
            console.log(err);
            callback(true);
            return;
        }
        callback(false,result);
    });
}
exports.deleteList=function(id,callback){
    var sql="DELETE FROM LIST WHERE id=?";
    connection.query(sql,[id],function(err,results){
        if(err){
            console.log(err);
            callback(true);
            return;
        }
        callback(false,results);
    });
}
//For upload
//ADD LIST ,CREARE O LISTA GOALA
//TODO ADAUGARE LOCATII MAI INTAI,SERVERU RETURNEAZA ID LA FIECARE LOCATIE , DUPAIA CREEARE LISTA
exports.addList=function(listData,callback){
    var sql="INSERT INTO LIST(title,description,thumbnail,creator,created_at) VALUES(?,?,?,?,NOW())";
    connection.query(sql,[listData.listName,listData.listDesc,listData.thumbnail,listData.userId],function(err,results){
        if(err){
            console.log(err);
            callback(true,"List Create Error!");
            return;
        }

        callback(false,results);
    });
}

//For Upload (Get duplicate locations id he sent)
exports.getDuplicateLocations=function(latlngs,callback){
    //test data
    //latlngs=[[44.17892720,28.65181330],[28.65181330,44.17892720],[44.17868600,28.64668700],[44.206724, 28.631996],[44.16685750,28.60858984
    //]]
    console.log("getting duplicate locations2");
    console.log(latlngs);
    connection.query("SELECT id FROM LOCATION WHERE(lat,lng) IN (?)",[latlngs],function(err,results){
        if(err){
            console.log(err);
            callback(true,"Get duplicate location error!");
            return;
        }
        else{
            console.log(results);
            callback(false,results);
        }
    });
    //duplicates: yes(muzeu),no,yes(tomis),no(tara piticiilor),yes(aula b)
};

//INSERT DATA
//TODO NUSH CUM SA II AFISEZ CE LOCATII  NUS UNICE
//INSERT ALL ITEMS AT ONCE( CAN BE 10 20 etc)
exports.uploadList=function(listItems,listData,latlngs,callback){
    //console.log(listData);
    //console.log(listItems);
    //sau on duplicate key update lat=lat lng=lng //INSERT IGNORE
    //console.log(listItems);
    //console.log(listData);
    console.log(latlngs);
    var sql="INSERT IGNORE INTO LOCATION(name,description,lat,lng,thumbnail,city_id) VALUES ?;"
    //ignore pt daca exista duplicate le ignora si insereaza doar ce e unic
    connection.beginTransaction(function(err){
        if(err){
            console.log(err);
            callback(true,"Transaction Error");//Nu A fost creata lista din fiserente motive

        }
        //# CREATE LOCATIONS Execute querry
        connection.query(sql,[listItems],function(err,result) {
            if (err) //daca e o eroare inafara de duplicate keys
            {
                console.log(err);
                //callback(true);
                return connection.rollback(function(){
                    callback(true,"Error Creating Locations");
                });
            }
            //var keptImages=[];
            var rowIds = [];//locatii inserate care nu sunt duplicate
            //get all the adder rows ids, non duplicates(works only if autoincrement by 1)

            for (var i = result.insertId; i < result.insertId + result.affectedRows; i++) {
                rowIds.push(i);
            }
            console.log("unique ids:")
            console.log(rowIds)
            //console.log(result)

            //Verificare Daca Au Fost Introduse Minim 3 locatii unice
            //Daca iese cu callback(true) am dat rollback si nu s-a inserat nimic deci sterg toate pozele
            if(rowIds.length<3){
                //rollback
                console.log("Rolling Back Not Enough Unique Locations Inserted!");
                var uniques="";
                //show user what rows are unique
                connection.query("SELECT * FROM location WHERE id IN (?)",[rowIds],function(err,result){
                    if(result!=undefined){
                        for(var i=0;i<result.length;i++){
                            uniques=uniques+result[i].name+",";
                        }
                    }
                    return connection.rollback(function(){
                        callback(true,"Not enought unique locations!\n Minimum:3, Yours:"+rowIds.length+"\n Unique Locations: "+uniques);
                    });
                });
            }
            else//MORE THAN 3 UNIQUE LOCATIONS INSERTED
            {
              

                //get the inserted rows so i can determines what images to delete
               connection.query("SELECT * FROM location WHERE id IN (?)",[rowIds],function(err,result){
                    if(err){
                        console.log("Getting Location Error");
                        return connection.rollback(function(){
                            callback(true,"Error");
                        });
                    }
                    else{
                        var keptImages=[];
                        var insertedLocations=[];
                        for(var i=0;i<result.length;i++){
                            keptImages.push(result[i].thumbnail);
                            //imagini pe care nu le sterg dupa creare lista
                            insertedLocations.push(result[i].name);
                        }
                        console.log(result);
                        listData.thumbnail=result[0].thumbnail;//image of first uploaded location
                        //## Create The List
                        exports.addList(listData,function(err,results){
                            if(err){
                                //List Not Inserted Rollback
                                console.log("List Create Error");
                                return connection.rollback(function(){
                                    callback(true,results);
                                });
                            }
                            else{
                                //List Was Created Now Fill The many to many table
                                    console.log(results);
                                    var listId=results.insertId;
                                    //### Insert into List Details Table
                                    var listDetailData=[];
                                    for(var i=0;i<rowIds.length;i++){
                                        var listDetailItem=[];
                                        listDetailItem.push(listId);
                                        listDetailItem.push(rowIds[i]);
                                        listDetailData.push(listDetailItem);
                                    }
                                    console.log(listDetailData);
                                    connection.query("INSERT INTO list_details(id_list,id_location) VALUES ?",[listDetailData],function(err,result){
                                        if(err){
                                            console.log(err);
                                            return connection.rollback(function(){
                                                callback(true,"Error");
                                            });
                                        }
                                        else{
                                            //Everything Inserted Went Well
                                            //Succes Dupa Toate commit
                                            connection.commit(function(err){
                                                console.log("COMMITING CHANGES!");
                                                if(err){
                                                    return connection.rollback(function(){
                                                        callback(true,"Error Commiting Changes!");
                                                    });
                                                }
                                                console.log("SUCCES ADDED LIST COMPLETELY!");
                                                var r=new Object();
                                                r.keptImages=keptImages;
                                                r.insertedLocations=insertedLocations;
                                                callback(err,r);
                                            });
                                        }
                                    });
                                }
                            });
                    }
                });
            } 
        });
    });
}


//POST LOCATION AND ADD IT TO THE LIST
exports.addListItem=function(listItemData,callback){
    //?? in loc de ? to escape ids and values
   // var sql = "SELECT * FROM ?? WHERE ?? = ?";
   // var inserts = ['users', 'id', userId];
   // sql = mysql.format(sql, inserts);
    var sql="INSERT INTO LOCATION(name,description,lat,lng,thumbnail) VALUES(?,?,?,?,?);"
    connection.query(sql,[listItemData.titlu,listItemData.descriere,listItemData.lat,listItemData.lng,listItemData.thumbnail],function(err,results){
        if(err){
            console.log(err);
            callback(true);
            return;
        }
        console.log(results);
        //insert into list detail table
        connection.query("INSERT INTO list_details(id_list,id_location) VALUES(?,?);",[listItemData.listId,results.insertId],function(err,rows){
                if(err){
                    console.log(err);
                    callback(true);
                    return;
                }
                //dau results de mai sus sa stiu inserted id for some reason i guess
                callback(false,results); //Inserted location, and in list details table
        });
    });
}

//exports test=function(callback){//
  //  var sql="SELECT FROM LOCATION WHERE lat=? AND lng=?";
   // connection.query(sql,[listData.listName,listData.listDesc,listData.thumbnail,listData.userId,listData.cityId],function(err,results){
    //    if(err){
        //    console.log(err);
        //    callback(true,"List Create Error!");
        //    return;
       // }
        //callback(false,results);
    //});

    //in upload
  //get the duplicate rows so i get get their ids
               /* console.log("getting duplicate locations1");
                exports.getDuplicateLocations(latlngs,function(err,result)
                {
                    if(err){
                        console.log("Getting Duplicate locations error Error");
                        //rollback
                        return connection.rollback(function(){
                            callback(true,result);
                        });
                    }
                    else{
                        console.log(result);
                        for (var i = 0; i < result.length; i++) {
                            rowIds.push(result[i].id);
                        }
                        console.log(rowIds);

                        //CREATE LIST AND INSERT IN LIST DETAILS
                    }

                });*/
                