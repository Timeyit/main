<?php
// instantiate WorkItem object
include_once 'WorkItem.php';
$WorkItem = new WorkItem();
     
// create the WorkItem
if($WorkItem->saveLapTime() != "error"){
    //echo "Updating work item time";
}
 
// if unable to create the WorkItem, tell the user
else{
    echo "Unable to update work item time";
}
?>