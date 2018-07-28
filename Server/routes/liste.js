var express = require('express');
var router = express.Router();
const list_controller=require('../controllers/listController');
const multer=require('multer');


//MULTIPART = file uploads

//ADDS A BODY AND A FILE to a request
const storage=multer.diskStorage({
    destination:function(req,file,callback){
      callback(null,'public/uploads/listThumbnails');
    },
    filename:function(req,file,callback){
      callback(null,Date.now()+'_'+ file.originalname);//unique name
    }
});
const fileFilter=(req,file,callback)=>{
  if(file.mimetype==='image/jpeg' ||file.mimetype==='image/png'||file.mimetype==='image/jpg'||file.mimetype=='image/gif' )//property automaticly populated by multer
  {   //rember i can upload other type of data if i want
      callback(null,true);
  }
  else{
      callback(null,false);
  }
};
const upload=multer({
  storage:storage,
  limits:{
  fileSize:1024*1024*10
  },
  fileFilter:fileFilter
});

//.any works for any
//upload.array("files",15) (Trebuia sa dau la fie)
router.post('/createList/upload',upload.array("listItemImages",15),list_controller.upload_list);


router.post('/',upload.single("image"),function(req,res){
  console.log(req.body);
  console.log(req.file);
  res.status(200).json({message:"good"});
  if(!req.file)
  {
      //res.json({message:"File Not uploaded"});
  }
  else{
     // console.log(req.file);
     //res.json({message:"Uploaded"});
  }

});

//normal ar fi fost checkAuth middleware token to verfiy but not rn
//router.post('/',upload.single('listImage'),list_controller.create_list);
//Add list item to list
//router.post('/:id',upload.single('listItemImage'),list_controller.create_list_item);

router.get('/',list_controller.liste_list_pagination_test);//Dont show all list at once to improve performance
//Ia continutul unei liste
router.get('/:id',list_controller.get_list_items);

//test function
router.get('/testduplicates/test',list_controller.test_duplicates);

module.exports=router;


