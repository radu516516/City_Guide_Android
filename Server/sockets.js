var sockets = {};

const {Buses}=require('./utils/buses');//busses tracking
const {Users}=require('./utils/users');//users in chat groups
const {isRealString}=require('./utils/validation');
const {generateMessage,generateLocationMessage}=require('./utils/message');
var numSockets= 0;
var buses=new Buses();//all the buses to be tracked
var users=new Users();//users chatting
var nrSocketsChat=0;



sockets.init = function (server) {
    // socket.io setup
    var io = require('socket.io')(server, {
        wsEngine: 'ws'
    });


    var trackingNsp=io.of("/bus-tracking");
        
    trackingNsp.on('connection',function(socket){
        console.log('someone connected bus tracking');
        socket.emit('socketID',{id:socket.id});
        numSockets++;
        console.log("Nr of Sockets Connected to tracking namespace:"+numSockets);
        
    
    
        //Logic,Events
        socket.on('joinBusTrackingRoom',function(params){
            //A user or A bus can join a tracking room
            //Bus emits position , User listens
            //1 Room Per each BUS ROUTE
            //todo Remove and add User/Bus to list of connected sockets
            console.log(params);
    
            if(params.type==='bus'){
                socket.join(params.room);
                //UN AUTOBUZ POATE FI INTR-UN ROOT AT ONCE
                console.log("Bus joined:"+params.busName+" has joined room "+params.room);
                buses.removeBus(socket.id);//sa fiu sigur ca nu e in alt room
                buses.addBus(socket.id,params.busName,params.room);
                //trackingNsp.to(params.room).emit('serverMessage',generateMessage('Server', `BUS :${params.busName} has joined :${params.room}`));
                socket.broadcast.to(params.room).emit('serverMessage',generateMessage('Server', `BUS :${params.busName} has joined :${params.room}`));
            }
            if(params.type==='user'){
                //UN USER POATE FI IN MAI MULTE ROOMS
                for(room in params.rooms){
                    console.log(params.rooms[room]);
                    socket.join(params.rooms[room]);//intra in mai multe rooms
                }

                console.log("User joined: has joined rooms: "+params.rooms);
               // trackingNsp.to(params.room).emit('serverMessage',generateMessage('Server', `A user has joined :${params.room}`));
               socket.broadcast.to(params.room).emit('serverMessage',generateMessage('Server', `A user has joined :${params.room}`));
            }
    
            socket.emit('serverMessage', generateMessage('Server', `Welcome to the bus tracking room :${params.room}`));
        });
        socket.on('leaveBusTrackingRoom',function(params,callback){
            socket.leave(params.room);
            var bus=buses.getBus(socket.id);//get bus that is trying to leave
            if(bus){
                //daca socketul respectiv e bus si iese din camera
                buses.removeBus(socket.id);//remove from array of tracking buses
                console.log("Bus :"+bus.busName+" left the room "+bus.room);
               // socket.broadcast.to(bus.room).emit('serverMessage',generateMessage('Server', `BUS :${bus.busName} left:${bus.room}`));
                socket.broadcast.to(bus.room).emit('busLeftRoom',generateMessage(bus.busName,`has left room ${bus.room}`));//Tell other people in tracking room that this bus has left so they can remove it from map
                callback(bus.busName+" have left the room "+bus.room);
                //inca e conectat la server doar ca nu e intr-un room acum
            }
            else{//Daca e user si iese dintr-o camera
                console.log("User left room "+params.room);
                socket.broadcast.to(params.room).emit('serverMessage',generateMessage('Server', `A user has left :${params.room}`));
                callback("You have left the room "+params.room);
            }
        });
        //Update location of bus (Doar autobuzele trimit eventul asta,useri doar primesc)
        socket.on("busMoved",function(locationData){
            //emit update to all sockets in room of the bus
            var bus=buses.getBus(socket.id);//Daca exista in array acest bus(Daca este intr-un room)
            if(bus)
            {
                console.log("BUS MOVED:"+bus.busName+" ROOM:"+bus.room+" Location:"+locationData.latitude+","+locationData.longitude);
               // trackingNsp.to(bus.room).emit('busMoved',generateLocationMessage(bus.busName,locationData.latitude, locationData.longitude));
                //trimite locatia autobuzului la useri din room
                socket.broadcast.to(bus.room).emit('busMoved',generateLocationMessage(bus.busName,locationData.latitude, locationData.longitude));
            }
        });
    
        socket.on('disconnect',function(){//cand se deconecteaza automat il si scoate din roooms
          
            numSockets--;
            //console.log("Nr of Sockets Connected to tracking namespace:"+numSockets);
            
            var bus=buses.removeBus(socket.id);
            if(bus){
                //emit to his room that a bus has left
                console.log('bus disconnected'+bus.busName);
                socket.broadcast.to(bus.room).emit('busLeftRoom',generateMessage(bus.busName,`has left room ${bus.room}`));
            }
            else{
                console.log('user disconnected');
            }
        });
    });


    var chatGroups=io.of("/chat-groups");

    chatGroups.on("connection",function(socket){
        console.log('New user connected chat groups ',socket.id);
        socket.emit('socketID',{id:socket.id});
        nrSocketsChat++;
        console.log("Nr of Sockets Connected to chatting namespace:"+nrSocketsChat);
        //When client wants to join a room event
        socket.on('join',function(params,callback){//datele cu care vine              //!!!!!!!!! EVERY USER,RIGHT AFTER CONNECT HE JOINS A ROOM
            //callback = awk function on the client , send packet to call that
            if(!isRealString(params.name) || !isRealString(params.room))
            {
                return callback({error:'Name and room name not OK'});

            }
            socket.join(params.room);
            //Add user to the list of connected sockets
            users.removeUser(socket.id);//make sure user isnt alrleady(remove them from any prev rooms and add them to the new one)
            //(REMOVE USER, DACA DA JOIN DINOU IL SCOATE SA NU SE REPETE)
            users.addUser(socket.id,params.name,params.room,'turist');//IL BAG IN LISTA DE CONEXIUNI CU CARE TIN EVIDENTA
            socket.broadcast.to(params.room).emit('updateUserList',users.getUserList(params.room));//le da clientilor lista de useri din camera lor
            socket.emit('newMessage', generateMessage('Server', `Welcome to the chat app room :${params.room}`));
            socket.emit('newMessage',generateMessage('Server',`There are:${users.getNrUsers(params.room)} users in this room `));
            socket.broadcast.to(params.room).emit('newMessage',generateMessage('Server', `${params.name} has joined the room`));
            console.log(params.name+" has joined room "+params.room);
            callback();//Room Exists , Room ok can procede to join room
        });
        //user sent message
        socket.on('createMessage', function (messageData, callback) { //Event Awk we need a callback to awknolage client we got message and its good
            console.log('Got Message:', messageData);
            var user=users.getUser(socket.id);//SEND MESSAGE TO HIS ROOM ONLY
            console.log(user);
            console.log(messageData.text);
            if(user && isRealString(messageData.text))
            {
                socket.broadcast.to(user.room).emit('newMessage',generateMessage(user.name,messageData.text));
                callback("Message Emitted to other users in room")
            }//Daca userul exita(Cand se conecteaza creez obiect userl si il bag)
            callback('This is an AWKNOLAGEMENT from the server'); 
        });
        //todo later,(un popup pe client cu locatia lui)(RecyclerView heterogneous views)
        socket.on('createLocationMessage', function (locationData) {//No AWK here for example
            var user=users.getUser(socket.id);
            if(user)
            {
                socket.broadcast.to(user.room).emit('newLocationMessage',generateLocationMessage(user.name,locationData.latitude, locationData.longitude));
            }
        });
        //disconnect
        //basicly i leave room on disconnect
        socket.on('disconnect', function () {
            console.log( 'User has disconnected group chat' + socket.id);
            nrSocketsChat--;
            console.log(nrSocketsChat);
            var user=users.removeUser(socket.id);
            console.log(socket.id)
            if(user){
                socket.broadcast.to(user.room).emit('updateUserList',users.getUserList(user.room));
                socket.broadcast.to(user.room).emit('newMessage',generateMessage('Server',`${user.name} has left the room!`));
            }
        });
    });
    console.log("SOCKET IO INITIATED");
}

module.exports = sockets;