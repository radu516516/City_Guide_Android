var userModel=require('../models/userModel');
const bcrypt=require('bcrypt');//hash password on register
const jwt=require('jsonwebtoken');//for tokens to prottect routes, we give token on login
const { check, validationResult } = require('express-validator/check');

//register
exports.user_create_post=function(req,res){//cod 200 register succes
    //Validate Using Express-Validator Middleware
    const errors = validationResult(req);
    var userData=req.body;
    console.log(userData);
    if (!errors.isEmpty()) {
      return res.status(422).json({ message: errors.mapped() });
    }
  
    //Hash password
    //cant reverse Its a one way opperation NON REVERSIBLE
    //Salting =add random string to that plain text password before we hash it so its more secure
    bcrypt.hash(userData.pass,5,(err,hash)=>{
        if(err){
            return res.json({message:"Error Hashing password"});//couldnt hash the password
        }
        else{
            userData.pass=hash;
            console.log(userData);
            //Use model to add user in the database
            userModel.addUser(userData,function(err,result){//callback
                if(err){
                    res.json({message:"Error,Username taken!"});
                }
                else{
                    //CREARE TOKEN
                    const token=jwt.sign({//run sincronous
                        id:result.insertId,
                        name:userData.name
                        //type:userData.type
                    },"secretkey",
                    {
                        expiresIn:"10h"
                    });
                   res.status(200).json({message:"Succes Register",token:token,id:result.insertId});//dai token pe care il pastreazea si foloseste pe rute protejate

                   //daca token gol = register failed, print message
                }
            });
            //only if we can hask the password we create the user
        }
    });
}



exports.user_delete=function(req,res){
    userModel.deleteUser(req.params.id,function(err,count){
        if(err){
            res.json({message:"Error"});
        }
        else{
            res.json(count.affectedRows);
        }
    });
}


//GET TOKEN THAT WE ATTACHE TO USER SIGN IN THAT WE USE TO PROTECT ROUTES
exports.check_login=function(req,res){
    //remember validate data 
    console.log(req.body);
    userModel.getUser(req.body.name,function(err,result){
        if(err){
            res.status(401).json({message:"Error"});
        }
        else{
           console.log(result);
            if(result.length>0){//login bun user exist
                
                //now check if password of username is the same
                bcrypt.compare(req.body.pass,result[0].pass,(err,check)=>{
                    if(err){
                        res.status(401).json({message:"Error Wrong Password"});
                    }
                    console.log(check);
                    if(check){ //!!! LA FIECARE LOGIN II DAU UN TOKEN CARE EXPIRA IN 10 ORE,DECI DACA NU SE LOGHEAZA 10 ORE , NO TOKEN, U HAVE TO LOGIN AGAIN
                        //Login Succes
                        //Generate and give a JSON webtoken, so we can secure api routes
                        const token=jwt.sign({//run sincronous
                            id:result[0].id,
                            name:result[0].username
                           // type:result[0].type
                        },"secretkey",
                        {
                            expiresIn:"10h"
                        });
                        console.log(token);
                        res.status(200).json({message:"Succes Login",token:token,id:result[0].id});//return token

                        //IF RETURNS TOKEN LOGGIN SUCCES else print message


                        //we will send token with requests to resources we want to protect and verify the token
                        //middleware will check for a valid token to be there on requests i want to protect
                    }else{
                        res.status(401).json({message:"Error Wrong Password"});
                    }
                });
            }
            else{
                res.json({message:"Error Didnt Find user"});
            }
        }
    });
}
