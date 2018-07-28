
//PT A TINE IN EVIDENTA CE SOCKETURI SUNT CONNECTATE LA SOCKET IO
//PT A IMI USORA LUCRUL CU ELE

//Nu e stocare permanenta, when server goes down, the chatting goes down aswell

//ES6 CLASSES!


//functile care sunt folosite cu new start with UPPER CASE Person not person

//Clases are special function 

class Users{//Connected Sockets to SOCKET IO

    constructor(){
        this.users=[];
    }
    addUser(socketId,name,room,type){
        var user={socketId,name,room,type};
        this.users.push(user);
        return user;
    }
    removeUser(socketId){
        var user = this.getUser(socketId);
        if (user) {//daca exista userul
          this.users = this.users.filter((user) => user.socketId !== socketId);
        }
        return user;
    }
    getUser(socketId){
        return this.users.filter((user)=>user.socketId===socketId)[0];//no user undefined, else we get it
    }
    getUserList(room){
        var users=this.users.filter((user)=>{//for each item in the array
            return user.room === room;
        });
        var namesArray=users.map((user)=>{
            return user.name;
        });
        console.log('Users:',this.users)
        return namesArray;
    }
    getNrUsers(room){
        var users=this.users.filter((user)=>{//for each item in the array
            return user.room === room;
        });
        var namesArray=users.map((user)=>{
            return user.name;
        });
        return namesArray.length;
    }
}
module.exports={Users};