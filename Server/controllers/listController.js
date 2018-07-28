var listModel=require('../models/listsModel');
var fs = require('fs');


function deleteFiles(files,callback){
    var i = files.length;
    files.forEach(function(filepath){
      fs.unlink(filepath, function(err) {
        i--;
        if (err) {
          callback(err);
          return;
        } else if (i <= 0) {
          callback(null);
        }
      });
    });
}
//todo   REZISE IMAGES SO THEY ARE SMALLER MAYBE??
//upload whole list at once
exports.upload_list=function(req,res){
    //console.log(req.body);
  //  console.log(req.body.listItemName);//array of
    //console.log(req.body.listItemName[1]);
    //console.log(req.files);
    //Add Locations
    //Create List
    var listItemsData=[];
    var locationsLatLngs=[];
    //arrays
    var listItemName=req.body.listItemName;
    var listItemDescription=req.body.listItemDesc;
    var listItemLat=req.body.listItemLat;
    var listItemLng=req.body.listItemLng;

    var thumbnails=[];
    for(var i=0;i<listItemName.length;i++){
    
    var listItem=[];
    listItem.push(listItemName[i]);
    listItem.push(listItemDescription[i]);
    listItem.push(listItemLat[i]);
    listItem.push(listItemLng[i]);

    var latlng=[];
    latlng.push(listItemLat[i]);
    latlng.push(listItemLng[i]);


    thumbnail=req.files[i].path;
    thumbnail=thumbnail.replace("public","");
    thumbnail=thumbnail.replace(/\\/g,"/");
    thumbnails.push(thumbnail);
    listItem.push(thumbnail);

    listItem.push(req.body.cityId);//city for location //list items are locations
    
    // listItem.name=listItemName[i]; listItem={}
    //  listItem.description=listItemDescription[i];
    // listItem.lat=listItemLat[i];
    // listItem.lng=listItemLng[i];
    //  listItem.thumbnail=req.files[i].path;
    //console.log(listItem);
    listItemsData.push(listItem);
    locationsLatLngs.push(latlng);
    
    }
   // console.log(listItemsData);
   // console.log(locationsLatLngs);
  
    //
    var listdata=new Object();
    listdata.listName=req.body.listName;
    listdata.listDesc=req.body.listDesc;
    //listdata.cityId=parseInt(req.body.cityId);
    listdata.userId=parseInt(req.body.userId);
    //thumbnail=req.files[0].path;
    //thumbnail=thumbnail.replace("public","");
    //thumbnail=thumbnail.replace(/\\/g,"/");
    //listdata.thumbnail=thumbnail;
    
    //insert into mysql
    //Nested arrays are turned into grouped lists (for bulk inserts), e.g. [['a', 'b'], ['c', 'd']] turns into ('a', 'b'), ('c', 'd')
    listModel.uploadList(listItemsData,listdata,locationsLatLngs,function(err,result){
        if(err){
            //NOTHING INSERTED, REMOVE ALL  IMAGES
            for(var i = 0 ; i<thumbnails.length;i++)
            {
                thumbnails[i]="public"+thumbnails[i];
            }
            console.log(thumbnails);
            //daca nu am pastrat nimic sterg toate imaginile
            deleteFiles(thumbnails,function(err){
                if(err){
                    console.log(err);
                }
                else{
                    console.log('all files removed');
                }
            })
            console.log(result);
            res.status(200).json({message:"error",error:result});//error mesage if it didnt upload
        }
        else{
        console.log(result);
         //REMOVE IMAGES THAT I DONT NEED TO KEEP(imaginile care nu se afla in result.keptImages)
        let difference=thumbnails.filter(x=>!result.keptImages.includes(x));
        //Keep only the immages i need
        for(var i = 0 ; i<difference.length;i++)
        {
            difference[i]="public"+difference[i];
        }
        console.log(difference);
        deleteFiles(difference,function(err){
            if(err){
                console.log(err);
            }
            else{
                console.log('non needed files removed');
            }
        })
        //console.log("Affected Rows:"+result.affectedRows);
        //console.log("InsertedID:"+result.insertId)
        res.status(200).json({message:"good"});//List inserted GOOD
        }
    });

   
}


//MULTER SE OCUPA DE MULTYPART BODY
exports.create_list=function(req,res){
    console.log(req.file);//uploadded file from multer middleware

    //NORMAL CREATOR ID ERA LUAT DIN TOKEN CA PUNEAM TOKENU IN REQUEST

    var listData=req.body;//ii dau ce trebuie
    //city id, creator , title ,description etc

    if (typeof req.file != "undefined") {// || !req.file //daca nu a uploadat, sau sa intamplat o problema
        listData.thumbnail=req.file.path;
     }
     else{//nu a trimis thumbnail lista
         listData.thumbnail="/images/noimage.jpg";
     }

     listData.thumbnail=listData.thumbnail.replace("public","");//Sa imi fie usor sa fac request
     listData.thumbnail=listData.thumbnail.replace(/\\/g,"/");//works gucci
     listData.titlu+=":"+Date.now();//La insert saimi fie usor
     listModel.addList(listData,function(err,result){
        if(err){
            res.json({message:"Error"});
        }
        else{
            res.json(result.insertId);
            //SHOULD RESPOND WITH THIS ID CA DUPAIA SA STIE UNDE SA ADAUGE
            console.log("1 record inserted, ID: " + result.insertId);
        }
     });
}

//We can allow the user to specify two parameters: Page and Limit.
//SCOP: load 10 or 5 or whatever at a time, not all!!!!!!!!
exports.liste_list_pagination_test=function(req,res){
    var page=parseInt(req.query.page,10);//base 10
    //page ce pagina
    //page = offset de unde incepe, Limit = limit cate randuri sa ia
    if(isNaN(page)||page<1)//IsNan not a number
    {
        page=1;//Default incepe de la 1 daca nu ii zice sau daca e eroare de conversie
    }

    var limit=parseInt(req.query.limit,10);
    if (isNaN(limit)) {
        limit = 10;//Default 10
    } else if (limit > 200) {//Max 50 de odata
        limit = 200;
    } else if (limit < 1) {//Minim 1
        limit = 1;
    }

    var city_id=parseInt(req.query.city_id,10);
    if(isNaN(city_id)){
        res.json({message:"Error"});
    }
    else{
         //!! REMBER CALCULATE THE TOTAL NUMBER OF PAGES , USING rowCOUNT CA SA STIU DACA FAC QUERRY IN CONTINUARE SAU NU 
        listModel.getRowCount(city_id,function(err,result){
            if(err){
                console.log(err);
                res.json({message:"Error"});
            }
            else//Dc fac total nr of pages? Pt optimizare ca e mai rapid sa calculez cate rows are tabelu decat sa select * din el si dupaia sa selectez in fct de offset si limit
            {
                    var count = parseInt(result[0].nrRows, 10);
                    console.log("nr liste:"+count);
                    //TOTAL NUMBER OF PAGES
                    //A PAGE HAS (LIMIT) ROWS( limit=10 , count=100, 10 pagini maxime/limit=5,count=100,20 de pagini maxime)
                    var totalNrOfPages=Math.ceil(count/limit);//Ex count=12 , nrPg=1.2 => 2 pagini, 1 cu 10 , a 2 o sa aiba doar 2 elemente
                    
                    if(totalNrOfPages<page)//Daca ar vrea ceva ce nu e in baza de date
                    {
                        res.json({message:"Error: Max Pages:"+totalNrOfPages+" requested page:"+page});
                    }
                    else{
                        var offset = (page - 1) * limit;
                        console.log("nr total pagini:"+totalNrOfPages+" limit:"+limit+" offset:"+offset);

                        //Get the requested data
                        listModel.getListsPaginationTest(limit,offset,city_id,function(err,rows){
                            if(err){
                                console.log(err);
                                res.json({message:"Error"});
                            }
                            else
                            {
                                res.status(200).json({
                                count:rows.length,
                                maxPage:totalNrOfPages,
                                liste:rows});
                            }
                        });
                    }
                    //console.log(count);
                    //Calculate the offset
                    //ex page=1,limit=10 => offset=0,limit=10 , arata primele 10
                    //ex page=2,limit=10 => offset=10,limit=10, arata urmatoarele 10
                    //Deci arata LIMIT ELEMENTE per PAGE
                    //O pagina are Limit Liste
                }
                //per page default 10
                //page default 1
            });
    }
}

exports.get_list_items=function(req,res){
    listModel.getListItems(req.params.id,function(err,rows){
        if(err){
            res.json({message:"Error"});
        }
        else{
            res.status(200).json({listItems:rows});
        }
    });
}
//TEST FUNCTION ONLY FOR TESTING ON UPLOAD TEST TEST
/*exports.test_duplicates=function(req,res){
    latlngs=[[44.17892720,28.65181330],[28.65181330,44.17892720],[44.17868600,28.64668700],[44.206724, 28.631996],[44.16685750,28.60858984
    ],[44.18256660,28.64747820]];
    //duplicates: yes(muzeu),no,yes(tomis),no(tara piticiilor),yes(aula b)
    console.log("got request");
    listModel.getDuplicateLocations(latlngs,function(err,result){
        if(err)
        {
            res.json({message:"Error"});
        }
        else{
            res.json(result);
        }
    });
}*/

//daca as adauga 1 cate 1
exports.create_list_item=function(req,res){
    console.log(req.file);//uploadded file from multer middleware
    var listItemData=req.body;
    if (typeof req.file != "undefined") {
        listItemData.thumbnail=req.file.path;
     }
     else{
         listItemData.thumbnail="/images/noimage.jpg";
     }
     listItemData.thumbnail=listItemData.thumbnail.replace("public","");//Sa imi fie usor sa fac request
     listItemData.thumbnail=listItemData.thumbnail.replace(/\\/g,"/");//works gucci
     listItemData.titlu+=":"+Date.now();//La insert saimi fie usor
     listItemData.listId=req.params.id;
     console.log(listItemData);
     listModel.addListItem(listItemData,function(err,result){
        if(err){
            res.json({message:"Error"});
        }
        else{
            res.json(result.insertId);//ID INSERERAT DIN LOCATION
            //SHOULD RESPOND WITH THIS ID CA DUPAIA SA STIE UNDE SA ADAUGE
            console.log("1 record inserted, ID: " + result.insertId);
        }
     });
}
