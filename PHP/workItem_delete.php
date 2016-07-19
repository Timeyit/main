<?php
// instantiate WorkItem object
include_once 'WorkItem.php';
$WorkItem = new WorkItem();
     
// create the WorkItem
if($WorkItem->deleteTask()){
    echo "WorkItem was deleted.";
}
 
// if unable to create the WorkItem, tell the user
else{
    echo "Unable to delete WorkItem.";
}
?>