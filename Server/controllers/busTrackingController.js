
var busTrackingModel=require('../models/busTrackingModel');


exports.check_login=function(req,res){
    
    console.log(req.body);

    busTrackingModel.getBus(req.body.registration_number,function(err,result){
            if(err){
                res.status(401).json({message:"Error"});
            }
            else{
                console.log(result);
                 if(result.length>0){//login bun bus exist
                    if(result[0].password==req.body.password){
                        //password correct
                        console.log("Succes login");
                        res.status(200).json({message:"Succes Login",bus_route_id:result[0].bus_route_id});
                        //result contains the bus route id 
                    }
                    else{
                        //password incorrect
                        res.status(401).json({message:"Bus number and password do not match"});

                    }
                }
                else{
                    res.status(401).json({message:"Bus number does not exist"});     
                    
                }
            }
    });
}

exports.route_detail=function(req,res){

    busTrackingModel.getBusRouteInfo(req.params.id,function(err,rows){
        if(err){
           
            res.status(400).json({message:"Error"});
        }
        else{
          if(rows.length>0)
            {         
                var finalJson = [];
                var bus_route_trips = [];
                //format into array of trips containing arrray of bus stations
                var trips = [...new Set(rows.map(item => item.trip_id))];//2 Trips or 1
                //console.log(trips);
           
                //Parcurg trip cu trip
                //2.Tmp1=Info despre un trip is un array cu statiile sale
                //3.Adauga tmp ca object nou in final json
                trips.forEach((trip,i)=>{
                   // console.log(trip);
                   // console.log(i);
                    var tmp1={};//final data object empty
                    bus_route_trips = [];//statiile din ruta asta
                    rows.forEach(d => {
                        if (d.trip_id === trip) {
                            bus_route_trips.push(d);//rows care are trip id curent cu trip ca dupaia sa iau set de stops pt acest trip id
                            tmp1.trip_id = d.trip_id;//setez id si nume la tmp 1
                            tmp1.trip_name = d.trip_name;
                            tmp1.direction=d.direction;
                        }
                    });
                    //console.log(bus_route_trips);
                    tmp1.nr_statii=bus_route_trips.length;
                    var statii = [...new Set(bus_route_trips.map(item => item.stop_id))];//statiile pt acest trip
                    //console.log(statii);
                    //console.log(tmp1);
                    tmp1.statii=[];//Json Array
                    statii.forEach((statie,j)=>{
                        var tmp2={}//o statie
                        bus_route_trips.forEach(trip=>{
                            if(trip.stop_id==statie){
                                tmp2.stop_id=trip.stop_id;
                                tmp2.stop_name=trip.stop_name;
                                tmp2.order=trip.stop_order;
                                tmp2.lat=trip.lat;
                                tmp2.lng=trip.lng;
                                tmp1.statii.push(tmp2);//adaug in temp array de staty de la acel trip
                            }
                        });  
                    });
                    finalJson.push(tmp1);//final data
                  // var products = [...new Set(cats[i].map(item => item.stop_id))];
                  // console.log(products)
                });
                //console.log(finalJson);
                res.status(200).json({bus_route_id:rows[0].bus_route_id,bus_route_name:rows[0].bus_route_name,trips:finalJson});
            }
            else
            {
                res.status(400).json({message:'Bus route doesnt exist'})
            }   
        }
    })
}
exports.getAllRouteTripsInCity=function(req,res){
    busTrackingModel.getCityRoutesInfo(req.params.id,function(err,rows){
        if(err){
           
            res.status(400).json({message:"Error"});
        }
        else{
            if(rows.length>0)
            {

               
                //clean json response
                var finalJson = [];
                var routes = [...new Set(rows.map(item => item.bus_route_id))];
                console.log(routes);
                routes.forEach((route)=>{

                    var newRoute={};
                    var r=[];
                    rows.forEach(d=>{
                        if(d.bus_route_id==route){//toate rows care au leagatura cu ruta asta
                            r.push(d);
                            newRoute.bus_route_id=d.bus_route_id;
                            newRoute.bus_route_name=d.bus_route_name;
                            newRoute.city_name=d.city_name;
                        }
                    });
                    //console.log(newRoute);

                    //trips pt fiecare ruta
                    var trips = [...new Set(r.map(item => item.trip_id))];
                    //console.log(trips);

                    newRoute.trips=[];

                    trips.forEach(trip=>{
                        var newTrip={};
                        var t=[];

                        r.forEach(route=>{
                            if(route.trip_id==trip){
                                t.push(route);//din ruta asta care sunt din tripu asta
                                newTrip.trip_id=route.trip_id;
                                newTrip.trip_name=route.trip_name;
                                newTrip.direction=route.direction;
                            }
                        });
                        //t contine statiile care fac parte din acest trip

                       

                        var statii = [...new Set(t.map(item => item.stop_id))];//statiile pt acest trip
                        newTrip.nr_statii=statii.length;
                        newTrip.statii=[];
                      
                        console.log(statii);
                        statii.forEach(statie=>{
                            var newStatie={};
                            t.forEach(trip=>{
                                if(trip.stop_id==statie){
                                    newStatie.stop_name=trip.stop_name;
                                    newStatie.stop_id=trip.stop_id;
                                    newStatie.order=trip.stop_order;
                                    newStatie.lat=trip.lat;
                                    newStatie.lng=trip.lng;
                                    newTrip.statii.push(newStatie);//adauga statiile acestui trip
                                }
                            });
                        });
                        newRoute.trips.push(newTrip);
                    });
                    finalJson.push(newRoute);
                });
               // console.log(finalJson);
               res.status(200).json({routes:finalJson});
            }
            else{
                res.status(400).json({message:'City doest have any bus routes'})
            }
        }
    });
}
exports.getTripsCity=function(req,res){
    busTrackingModel.getCityRoutes(req.params.id,function(err,rows){
        if(err){
           
            res.status(400).json({message:"Error"});
        }
        else{
            if(rows.length>0)
            {
                res.json(rows);
            }
            else{
                res.status(400).json({message:'City doest have any bus routes'})
            }
        }

    });
}
exports.getCityId=function(req,res){
    busTrackingModel.getCityId(req.query.city_name,function(err,rows){
        if(err){
           
            res.status(400).json({message:"Error"});
        }
        else{
            if(rows.length>0)
            {
                res.status(200).json({message:"Succes",id:rows[0].id});
            }
            else{
                res.status(400).json({message:'City not supported'})
            }
        }
    });
}

//test get all the buses
exports.buses=function(req,res){
    
    busTrackingModel.getAllBuses(function(err,rows){
        if(err){
          console.log(err);
          res.json({message:"Error"});
        }
        else{
          res.status(200).json({//respond with json
            count:rows.length,
            users:rows});
        }
      });
};