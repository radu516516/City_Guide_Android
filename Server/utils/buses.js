class Buses{//Connected Sockets to SOCKET IO

    //Autobuz se identifica unic prin Socket.ID
    //El se poate afla intr-un singur room at a time
    constructor(){
        this.buses=[];
    }
    addBus(socketId,busName,room){
        var bus={socketId,busName,room};
        this.buses.push(bus);
        return bus;
    }
    removeBus(socketId){
        var bus = this.getBus(socketId);
        if (bus) {//daca exista autobuzul
          this.buses = this.buses.filter((bus) => bus.socketId !== socketId);
        }
        return bus;
    }
    getBus(socketId){
        return this.buses.filter((bus)=>bus.socketId===socketId)[0];//no user undefined, else we get it
    }

   /* getUserList(room){
        var users=this.users.filter((user)=>{//for each item in the array
            return user.room === room;
        });

        var namesArray=users.map((user)=>{
            return user.name;
        });
        console.log('Users:',this.users)
        return namesArray;
    }*/
}
module.exports={Buses};