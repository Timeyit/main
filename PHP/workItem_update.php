<?php
// instantiate WorkItem object
include_once 'WorkItem.php';
$WorkItem = new WorkItem();
     
// create the WorkItem
if($WorkItem->saveTaskTime()){
    echo "OK"
}
 
// if unable to create the WorkItem, tell the user
else{
    echo "ERROR";
}
?>