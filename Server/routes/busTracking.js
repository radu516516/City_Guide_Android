var express = require('express');
var router = express.Router();
const busTrackingController=require('../controllers/busTrackingController');



router.post('/login',busTrackingController.check_login);

router.get('/buses',busTrackingController.buses);

router.get('/routes/:id',busTrackingController.route_detail);//display route info (dus intors trips si statii)

router.get('/routes/city/:id',busTrackingController.getAllRouteTripsInCity);//Give to users toate routes care contin max 2 trips from the city they are in(they somehow find whna tcity they are in)

router.get('/trips/city/:id',busTrackingController.getTripsCity);

router.get('/cityid',busTrackingController.getCityId);

module.exports=router;