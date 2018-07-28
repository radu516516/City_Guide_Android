var express = require('express');
var router = express.Router();
const user_controller=require('../controllers/usersController');

const { check, validationResult } = require('express-validator/check');
const { matchedData, sanitize } = require('express-validator/filter');

//verificare date register login

const validateBody = [

  check('name').isLength({min:3}).trim(),
  check('pass','password at least 5 lenght and contain a number').isLength({min:5}).matches(/\d/),
 
]

/* GET users listing. */
router.get('/', function(req, res, next) {
  res.send('respond with a resource');
});


//Register
router.post('/register',validateBody,user_controller.user_create_post);

router.post('/login',user_controller.check_login);

router.delete('/:id',user_controller.user_delete);

//router.put('/:id',user_controller.updateUser);//not yet implemented
module.exports = router;
