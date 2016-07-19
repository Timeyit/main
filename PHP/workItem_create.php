<?php
// instantiate WorkItem object
include_once 'WorkItem.php';
$WorkItem = new WorkItem();
     
// create the WorkItem
if($WorkItem->create()){
    echo "WorkItem was created.";
}
 
// if unable to create the WorkItem, tell the user
else{
    echo "Unable to create WorkItem.";
}
?>