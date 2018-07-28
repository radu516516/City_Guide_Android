

//Interactiune BD
const connection=require('../configdb');//its cached(Export conexiunea)



//Check login BUS(nr inmatriculare unic daca exista)
exports.getBus=function(registration_number,callback){
    //console.log(registration_number);
    var sql="SELECT bus.registration_number,bus.password,bus.bus_route_id,bus_route.bus_route_name FROM bus INNER JOIN bus_route ON bus.bus_route_id = bus_route.bus_route_id WHERE registration_number=?;";//name should be unique
    connection.query(sql,[registration_number],function(err,result){
        if(err){
            callback(true);//error
            return;
        }
       // console.log(result);
        callback(false,result);
    })
}

exports.getAllBuses=function(callback){
    var sql="SELECT * FROM bus;";
    connection.query(sql,function(err,results){
        if(err){
            console.log(err);
            callback(true);
            return;
        }
        callback(false,results);
    });
}

//Pt soferi (Ia Trips Care au ID=bus_route_id)
exports.getBusRouteInfo=function(bus_route_id,callback){
    var sql="SELECT bus_route_name,bus_route_trip.bus_route_id,bus_route_trip.trip_id,bus_route_trip.trip_name,bus_route_trip.direction,bus_route_trip_details.stop_order,bus_route_trip_details.stop_id,bus_stop.stop_name,bus_stop.lat,bus_stop.lng  FROM bus_route_trip  INNER JOIN bus_route_trip_details  ON bus_route_trip.trip_id = bus_route_trip_details.trip_id INNER JOIN bus_stop ON bus_stop.id = bus_route_trip_details.stop_id INNER JOIN bus_route route ON bus_route_trip.bus_route_id = route.bus_route_id   WHERE bus_route_trip.bus_route_id=? ORDER BY bus_route_trip.direction,bus_route_trip_details.stop_order;"
    connection.query(sql,[bus_route_id],function(err,results){
        if(err){
            callback(true);
            return;
        }
        callback(false,results);
    });
}

//PT useri sa stie toate trips dintr-un oras (Contine cu totu toate statiile de la fiecare ruta)
exports.getCityRoutesInfo=function(city_id,callback){
    var sql="SELECT city.id,city.city_name,bus_route.bus_route_id,bus_route_name,bus_route_trip.trip_id,trip_name,direction,stop_order,stop_id,stop2.stop_name,lat,lng FROM bus_route INNER JOIN city ON city.id=bus_route.city_id INNER JOIN bus_route_trip ON bus_route.bus_route_id = bus_route_trip.bus_route_id INNER JOIN bus_route_trip_details brtd ON bus_route_trip.trip_id = brtd.trip_id INNER JOIN bus_stop stop2 ON brtd.stop_id = stop2.id WHERE bus_route.city_id=? ORDER BY bus_route_id,direction,stop_order;"
    connection.query(sql,[city_id],function(err,results){
        if(err){
            callback(true);
            return;
        }
        callback(false,results);
    });
}


exports.getCityRoutes=function(city_id,callback){//gets only the name of the routes nu si statiile care le alcatuiesc
    var sql="SELECT bus_route.city_id,bus_route.bus_route_id,bus_route_name,bus_route_trip.trip_id,trip_name,direction FROM bus_route INNER JOIN bus_route_trip ON bus_route.bus_route_id = bus_route_trip.bus_route_id  WHERE bus_route.city_id=? ;";
    connection.query(sql,[city_id],function(err,results){
        if(err){
            callback(true);
            return;
        }
        callback(false,results);
    });

}

exports.getCityId=function(city_name,callback){
    var sql="SELECT id FROM CITY WHERE city_name LIKE ?";
    connection.query(sql,[city_name],function(err,results){
        if(err){
            callback(true);
            return;
        }
        callback(false,results);
    });
}