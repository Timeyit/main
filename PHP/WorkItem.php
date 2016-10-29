<?php

include_once 'Settings.php';
include_once 'Session.php';

class WorkItem{    
    
    // database connection and table name
    public $table_name = "workItem";
    public $table_name_time = "TIMELOG";
    
    // create product
    function create()
    {
        $session = new Session();
        if($session->verifySession() != "OK")
        {
            return "ERRORSESSION";
        }
        
        $userName = $session->getUsername();
        
        // Create connection
        $Settings = new Settings();
        $conn = mysql_connect($Settings->host, $Settings->username, $Settings->password);

        // Check connection
        if (!$conn)
        {
            die('Could not connect: ' . mysql_error());
        }

        mysql_select_db($Settings->db_name, $conn);

        // Get data from client POST
        $data = json_decode(file_get_contents("php://input"));
        $nameWorkItem = mysql_real_escape_string($data->itemName);
        

        // query to insert workItem
        $query = "INSERT INTO  " . $this->table_name . " (nameWorkItem, description, user_username, duration) 
                VALUES ('" . $nameWorkItem . "', 'describe', '" . $userName . "',0)";

        // execute query
        if (mysql_query($query))
        {
            mysql_close($conn);
            return true;
        }
        else
        {
            echo "SQL ERROR could not create task in WorkItem()";
            mysql_close($conn);
            return false;
        }
    }

    //Save task time update
    function saveTaskTime()
    {   
        $session = new Session();
        if($session->verifySession() != "OK")
        {
            return "ERRORSESSION";
        }
        
        // Create connection
        $Settings = new Settings();
        $conn = mysql_connect($Settings->host, $Settings->username, $Settings->password);

        // Check connection
        if (!$conn)
        {
            die('Could not connect: ' . mysql_error());
        }

        mysql_select_db($Settings->db_name, $conn);

        // Get data from client POST
        $data = json_decode(file_get_contents("php://input"));
        $duration = mysql_real_escape_string($data->duration);
        //$nameWorkItem = mysql_real_escape_string($data->nameWorkItem);
        $idworkItem = mysql_real_escape_string($data->idworkItem);
        
        // query to insert workItem
        $query = "UPDATE " . $this->table_name . 
            " SET duration=". $duration . 
            " WHERE idworkItem ='". $idworkItem ."'";
        
        // execute query
        if (mysql_query($query))
        {
            mysql_close($conn);
            return true;
        }
        else
        {
            echo "SQL ERROR could not save task in WorkItem()";
            mysql_close($conn);
            return false;
        }
    }

    function saveLapTime()
    {
        error_reporting(E_ERROR);
        
        $session = new Session();
        if($session->verifySession() != "OK")
        {
            return "ERRORSESSION";
        }
        
        // Create connection
        $Settings = new Settings();
        $conn = mysql_connect($Settings->host, $Settings->username, $Settings->password);

        // Check connection
        if (!$conn)
        {
            die('Could not connect: ' . mysql_error());
        }

        mysql_select_db($Settings->db_name, $conn);

        // Get data from client POST
        $data = json_decode(file_get_contents("php://input"));
        $idworkItem = mysql_real_escape_string($data->idworkItem);
        $durationLap = mysql_real_escape_string($data->durationLap);
        $idTimeLog = mysql_real_escape_string($data->idTimeLog);
        
        $query = "";
        if($idTimeLog < 0)
        {
            $query = "INSERT INTO  " . $this->table_name_time . " (idWorkItem, TimeStart, Duration) 
                VALUES ('" . $idworkItem . "', '" . date("Y-m-d H:i:s") . "',0)";
        }
        else
        {
            $query = "UPDATE " . $this->table_name_time . 
            " SET Duration=". $durationLap . 
            " WHERE IDTimeLog = ". $idTimeLog ."";
            echo $query;
        }
        
        $result = mysql_query($query);
        if (!$result)
        {
            echo "SQL ERROR could not get tasks in Workitem()";
            mysql_close($conn);
            return "error";
        }
        else
        {
            //echo $result;
            $result = mysql_query("SELECT LAST_INSERT_ID() AS myid");
            
            //create an array
            $emparray = array();
            while($row = mysql_fetch_assoc($result))
            {
                $emparray[] = $row;
            }
            echo json_encode($emparray);
            
            //close the db connection
            mysql_close($conn);

            return json_encode($emparray);


        }
    }
    
    // get tasks for user
    function getTask()
    {
        error_reporting(E_ERROR);
        
        $session = new Session();
        if($session->verifySession() != "OK")
        {
            return "ERRORSESSION";
        }
        
        $user_username = $session->getUsername();
        
        // Create connection
        $Settings = new Settings();
        $conn = mysql_connect($Settings->host, $Settings->username, $Settings->password);

        // Check connection
        if (!$conn)
        {
            die('Could not connect: ' . mysql_error());
        }

        mysql_select_db($Settings->db_name, $conn);

        // Get data from client POST
        $data = json_decode(file_get_contents("php://input"));
        // query to insert workItem
        $query = "SELECT nameWorkItem, duration, idworkItem FROM " . $this->table_name . " WHERE user_username ='". $user_username . "'";

        // execute query
        $result = mysql_query($query);
        if (!$result)
        {
            //echo "SQL ERROR could not get tasks in Workitem()";
            mysql_close($conn);
            return false;
        }
        else
        {
            //create an array
            $emparray = array();
            while($row = mysql_fetch_assoc($result))
            {
                $emparray[] = $row;
            }
            echo json_encode($emparray);
            //close the db connection
            mysql_close($conn);

            return json_encode($emparray);


        }
    }
    
    // get tasks for user
    function getTimeLog()
    {   
        error_reporting(E_ERROR);
        
        error_reporting(E_ERROR);
        
        $session = new Session();
        if($session->verifySession() != "OK")
        {
            return "ERRORSESSION";
        }
        
        $user_username = $session->getUsername();
        
        // Create connection
        $Settings = new Settings();
        $conn = mysql_connect($Settings->host, $Settings->username, $Settings->password);

        // Check connection
        if (!$conn)
        {
            die('Could not connect: ' . mysql_error());
        }

        mysql_select_db($Settings->db_name, $conn);

        // Get data from client POST
        $data = json_decode(file_get_contents("php://input"));

        // query to insert workItem
        $query = "SELECT * FROM " . $this->table_name_time . 
            " LEFT JOIN " . $this->table_name . 
            " ON " . $this->table_name_time . ".idworkItem = " . $this->table_name . ".idworkItem" . 
            " WHERE user_username='" . $user_username . "'";
        
        // execute query
        $result = mysql_query($query);
        if (!$result)
        {
            //echo "SQL ERROR could not get tasks in Workitem()";
            mysql_close($conn);
            return false;
        }
        else
        {
            //create an array
            $emparray = array();
            while($row = mysql_fetch_assoc($result))
            {
                $emparray[] = $row;
            }
            echo json_encode($emparray);
            //close the db connection
            mysql_close($conn);

            return json_encode($emparray);


        }
    }
    
    // create product
    function deleteTask()
    {   
        error_reporting(E_ERROR);
        
        $session = new Session();
        if($session->verifySession() != "OK")
        {
            return "ERRORSESSION";
        }
        
        // Create connection
        $Settings = new Settings();
        $conn = mysql_connect($Settings->host, $Settings->username, $Settings->password);

        // Check connection
        if (!$conn)
        {
            die('Could not connect: ' . mysql_error());
        }

        mysql_select_db($Settings->db_name, $conn);

        // Get data from client POST
        $data = json_decode(file_get_contents("php://input"));
        $idworkItem = mysql_real_escape_string($data->idworkItem);

        // query to insert workItem
        $query = "DELETE FROM " . $this->table_name . 
            " WHERE idworkItem = " . $idworkItem . "";

        // execute query
        if (mysql_query($query))
        {
            mysql_close($conn);
            return true;
        }
        else
        {
            echo "SQL ERROR could not delete workitem in WorkItem()";
            mysql_close($conn);
            return false;
        }
    }
}