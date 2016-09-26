<?php

include_once 'Settings.php';
include_once 'User.php';

class Session {

    // database connection and table name
    public $table_name = "sessions";

    function createSession()
    {   
        error_reporting(E_ERROR);

        // Get data from client POST
        $data = json_decode(file_get_contents("php://input"));
        $username = mysql_real_escape_string($data->username);
        $password = mysql_real_escape_string($data->password);
        $sessionkey = $this->GUID();
        // Verify log-in

        // See if credentials match
        $User = new User();
        if($User->checkLogin() != 'OK')
        {
            return "USERERROR";
            //echo "ERROR Login Failed";
            mysql_close($conn);
            //return false;
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

        // query to insert workItem
        $query = "INSERT INTO " . $this->table_name . " (username, sessionkey, time_start, time_lastseen) VALUES ('" . $username . "','". $sessionkey ."', NOW(), NOW())";

        // execute query
        if (mysql_query($query))
        {
            mysql_close($conn);
            return $sessionkey;
        }
        else
        {
            echo mysql_errno($conn) . ": " . mysql_error($conn) . "\n";
            mysql_close($conn);
            return "SQLERROR";
        }
    }

    function verifySession()
    {
        error_reporting(E_ERROR);

        // Get data from client POST
        $raw_data = file_get_contents("php://input");
        $data = json_decode($raw_data, false);
        $sessionkey = $data->sessionkey;

        // Create connection
        $Settings = new Settings();
        $conn = mysql_connect($Settings->host, $Settings->username, $Settings->password);

        // Check connection
        if (!$conn)
        {
            die('Could not connect: ' . mysql_error());
            return "SQLERROR";
        }

        mysql_select_db($Settings->db_name, $conn);

        $query = "SELECT * FROM " . $this->table_name . " WHERE sessionkey='" . $sessionkey . "' AND time_lastseen >= NOW() - INTERVAL 30 MINUTE";

        $result = mysql_query($query, $conn);
        $num_rows = mysql_num_rows($result);
        if($num_rows > 0)
        {
            return "OK";
            //return false;
        }
        else
        {
            return "fail";
        }
    }

    function refreshSession()
    {

    }

    function GUID()
    {
        /*if (function_exists('com_create_guid') === true)
        {
            return trim(com_create_guid(), '{}');
        }*/

        return sprintf('%04X%04X%04X%04X%04X%04X%04X%04X', mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(16384, 20479), mt_rand(32768, 49151), mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535));

    }
}