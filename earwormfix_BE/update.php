<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
// json response array
$response = array("error" => FALSE);

if (isset($_POST['col']) && isset($_POST['email']) && 
        isset($_POST['param'])){
    
    $email = $_POST['email'];
    $col = $_POST['col'];
    $param = $_POST['param'];
    
    
    if ($db->isUserExisted($email)){
        $update = $db->updateUser($col, $email, $param);
        if($update){
            $response["error"] = FALSE;
            $response["profile"]["email"] = $update["email"];
            $response["profile"]["col"] = $col;
            $response["profile"]["param"] = $param;
            $response["profile"]["updated_at"] = $update["updated_at"];
            echo json_encode($response);  
        }
        else {
            $response["error"] = TRUE;
            $response["error_msg"] = "Failed to update user " . $email;
            echo json_encode($response);
        }
        
    }
    else{
        // user doesn't existed
        $response["error"] = TRUE;
        $response["error_msg"] = "User doesn't existed with " . $email;
        echo json_encode($response);
    }
 
}
$response["error"] = TRUE;
$response["error_msg"] = "Failed to POST";
echo json_encode($response);

