var express = require('express');
var router = express.Router();
const chat_controller=require('../controllers/chatController');
const checkAuth=require('../middleware/check-auth');



/* GET CHAT GROUPS PAGINATION */
router.get('/', chat_controller.chat_groups_pagination);

//Create chat group
router.post('/',checkAuth,chat_controller.create_group);

module.exports = router;