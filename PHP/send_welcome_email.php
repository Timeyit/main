<?php

// Get data from client POST
$data = json_decode(file_get_contents("php://input"));
$username = $data->username;
$email = $data->email;

$email_text = file_get_contents('welcome_email.htm');
$headers = "From: Timey <hello@timey.it>\r\n". 
           "MIME-Version: 1.0" . "\r\n" . 
           "Content-type: text/html; charset=UTF-8" . "\r\n";

mail($email, 'Welcome to Timey', $email_text, $headers, '-fhello@timey.it');

?>