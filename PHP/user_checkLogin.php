<?php
// instantiate WorkItem object
include_once 'User.php';
$User = new User();
     
// create the WorkItem
if($User->checkLogin()){
    //echo "User was created.";
}
 
// if unable to create the WorkItem, tell the user
else{
    //echo "Unable to create User.";
}
?>