<?php
include_once 'Settings.php';

class User {    
    
    
    // database connection and table name
    public $table_name = "user";

    // create product
    function checkLogin()
    {   
        error_reporting(E_ERROR);
        
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
        $username = mysql_real_escape_string($data->username);
        $password = mysql_real_escape_string($data->password);        

        // query to insert workItem
        $query = "SELECT * FROM " . $this->table_name . " WHERE username='" . $username . "' AND password = '" . $password . "'";

        // execute query
        $result = mysql_query($query);
        if (!$result)
        {
            echo "Fail";
            mysql_close($conn);
            return false;
        }
        else
        {
            if(mysql_num_rows($result) > 0)
            {
                echo "OK";
                //close the db connection
                mysql_close($conn);

                return true;
            }
            else
            {
                echo "Fail";
                mysql_close($conn);
                return false;
            }
        }
    }

    // create product
    function createUser()
    {   
        error_reporting(E_ERROR);
        
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
        $username = mysql_real_escape_string($data->username);
        $password = mysql_real_escape_string($data->password);
        $email = mysql_real_escape_string($data->email);

        $result = mysql_query("SELECT * FROM " . $this->table_name . " WHERE username='" . $username . "'", $conn);
        $num_rows = mysql_num_rows($result);
        if($num_rows > 0)
        {
            echo "exists";
            return false;
        }
        
        // query to insert workItem
        $query = "INSERT INTO  " . $this->table_name . " (username, password, email) VALUES ('" . $username . "','". $password ."','". $email ."')";


        // execute query
        if (mysql_query($query))
        {
            echo "OK";
            mysql_close($conn);
            return true;
        }
        else
        {
            echo "SQL ERROR Creating User in User()";
            mysql_close($conn);
            return false;
        }
    }

    //Save task time update
    function deleteUser()
    {   
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
        $username = mysql_real_escape_string($data->username);

        // query to insert workItem
        $query = "DELETE FROM " . $this->table_name . " WHERE username = ". $username ."";



        // execute query
        if (mysql_query($query))
        {
            mysql_close($conn);
            return true;
        }
        else
        {
            echo "SQL ERROR deleting user in User()";
            mysql_close($conn);
            return false;
        }
    }

}