<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
$response = array();
if(isset($_GET['uid'])){
    $browse = $db->browseFriends($_GET['uid']);
    if($browse){
            for($i=0; $i<count($browse) ;$i++){
                $response[$i]['uid'] = $browse[$i]['uid'];
                $response[$i]['full_name'] = $browse[$i]['full_name'];
                $response[$i]['name'] = $browse[$i]['name'];
                $response[$i]['email'] = $browse[$i]['email'];
                $response[$i]['photo'] = $browse[$i]['photo'];
                $response[$i]['are_friends'] = $browse[$i]['are_friends'];
                $response[$i]["error"] = FALSE;
                $response[$i]["error_msg"] = "Profiles fetch success";
            }
            echo json_encode($response);
    }  
    else{
        $response[0]["error"] = TRUE;
        $response[0]["error_msg"] = "Failed to fetch users from data base";
        echo json_encode($response);
    }
}
else{
    $response[0]["error"] = TRUE;
    $response[0]["error_msg"] = "GET parameter failed";
    echo json_encode($response);
}


