
const jwt=require('jsonwebtoken');

module.exports=(req,res,next)=>{
    try{
        const token=req.headers.authorization.split(" ")[1];
        console.log(token);
        const decoded=jwt.verify(token,"secretkey");
        req.JWTdecodedUserData=decoded;//pune data decodata pe request
        next();
    }catch(error){
        return res.status(401).json({message:'Auth failed'});
        //poate a expirat, poate nu e bun, poate nu a trimis token
    }
};
//postman
//Authorization(header) Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NjEsIm5hbWUiOiJyYWR1NTE0MSIsInR5cGUiOiJ0dXJpc3QiLCJpYXQiOjE1MjAyNzg1MjEsImV4cCI6MTUyMDMxNDUyMX0.ZsfHr117dwHxXHBdtq6fgu4JhD4I431KHXg_YgxC-fo