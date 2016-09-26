<?php
// instantiate WorkItem object
include_once 'User.php';
$User = new User();
     
// create the WorkItem
if($User->checkLogin() == 'OK'){
    echo "OK";
}
 
// if unable to create the WorkItem, tell the user
else{
    echo "fail";
}
?>