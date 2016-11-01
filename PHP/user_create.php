<?php
// instantiate WorkItem object
include_once 'User.php';
$User = new User();
     
// create user
echo $User->createUser();
    
?>