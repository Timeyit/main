<?php
// instantiate WorkItem object
include_once 'Session.php';
$Session = new Session();

$result = $Session->createSession();

if (strpos($result, 'ERROR') !== false) {
    echo "fail";
}
// if unable to create the WorkItem, tell the user
else{
    echo $result;
}
?>