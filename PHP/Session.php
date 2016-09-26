<?php

include_once 'Settings.php';

class Sessions {
    
    $Settings = new Settings();
    
    // database connection and table name
    public $table_name = "sessions";

    public $host = $Settings->host;
    public $db_name = $Settings->db_name; 
    public $username = $Settings->username; 
    public $password = $Settings->password;

}