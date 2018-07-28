

var chatModel=require('../models/chatmodel');

exports.chat_groups_pagination=function(req,res){
    var page=parseInt(req.query.page,10);
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
        res.json({message:"Error1"});
    }
    else{
        
        chatModel.getRowCount(city_id,function(err,result){
            if(err){
                console.log(err);
                res.json({message:"Error2"});
            }
            else
            {
                    var count = parseInt(result[0].nrRows, 10);
                    console.log("nr groups:"+count);
                   
                    var totalNrOfPages=Math.ceil(count/limit);
                    
                    if(totalNrOfPages<page)
                    {
                        res.json({message:"Error: Max Pages:"+totalNrOfPages+" requested page:"+page});
                    }
                    else{
                        var offset = (page - 1) * limit;
                        console.log("nr total pagini:"+totalNrOfPages+" limit:"+limit+" offset:"+offset);

                       
                        chatModel.getChatGroups(limit,offset,city_id,function(err,rows){
                            if(err){
                                console.log(err);
                                res.json({message:"Error3"});
                            }
                            else
                            {
                                res.status(200).json({
                                count:rows.length,
                                maxPage:totalNrOfPages,
                                groups:rows});
                            }
                        });
                    }
                }
            });
        }
    }

//pt create ii dai token city,name si langauge
exports.create_group=function(req,res){
    console.log(req.JWTdecodedUserData);
    //check token to see if authenticated
    var groupData=req.body;
    console.log(req.body);

    groupData.creator=req.JWTdecodedUserData.id;

    chatModel.addGroup(groupData,function(err,result){
        if(err){
            res.json({message:"Error,You allready have created a chat group in this city"});
        }
        else{
            //SHOULD RESPOND WITH THIS ID CA DUPAIA SA STIE UNDE SA ADAUGE
            console.log("1 record inserted, ID: " + result.insertId);
            res.json({message:"Succes"});
        }
    });

}
    
        
