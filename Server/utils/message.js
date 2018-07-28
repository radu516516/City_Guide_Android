//Utility functions related to messaging

var moment=require('moment');

var generateMessage=(from,text)=>{
    return{
        from,
        text,
        createdAt:moment().valueOf()
    };
};

var generateLocationMessage=(from,latitude,longitude)=>{
    return{
        from,
        latitude,
        longitude,
        createdAt:moment().valueOf
    };
};

module.exports={generateMessage,generateLocationMessage};