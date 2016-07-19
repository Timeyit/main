<?php
// instantiate WorkItem object
include_once 'WorkItem.php';
$WorkItem = new WorkItem();
     
// create the WorkItem
if($WorkItem->getTimeLog()){
    //echo "Getting all work items";
}
 
// if unable to create the WorkItem, tell the user
else{
    echo "Unable to get Work Items.";
}
?>