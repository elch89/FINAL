<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
$response = array("error" => FALSE);
// delete user
if(isset($_POST["uid"])){
    $directoryPost = "post/".$_POST["uid"];
    $directoryProfile = "profile/".$_POST["uid"];
    if (is_dir($directoryPost)){
        array_map('unlink', glob("$directoryPost/*.*"));
        $removed = rmdir($directoryPost);
        if(!$removed){
            $response["error"] = TRUE;
            $response["error_msg"] = "Failed to delete files ";
            echo json_encode($response);
        }
    }
    if (is_dir($directoryProfile)){
        array_map('unlink', glob("$directoryProfile/*.*"));
        $removed = rmdir($directoryProfile);
        if(!$removed){
            $response["error"] = TRUE;
            $response["error_msg"] = "Failed to delete files ";
            echo json_encode($response);
        }
    }
    
    $deleted1 = $db->deleteUser($_POST["uid"]);
    if($deleted1){
        $response["error"] = FALSE;
        $response["error_msg"] = "Success ";
        echo json_encode($response);
    }
    else{
        $response["error"] = TRUE;
        $response["error_msg"] = "Failed to remove from server ";
        echo json_encode($response);
    }
    
}
// delete post
else if(isset($_POST["pid"]) && isset($_POST["vid"])&&isset($_POST["pic"])){
    if(!unlink($_POST["pic"]) || !unlink($_POST["vid"])){
        $response["error"] = TRUE;
        $response["error_msg"] = "Failed to delete files ";
        echo json_encode($response);
    }
    $pid = $_POST["pid"];
    $vid = $_POST["vid"];
    $deleted = $db->deletePost($pid, $vid);
    if($deleted){
        $response["error"] = FALSE;
        $response["error_msg"] = "Success ";
        echo json_encode($response);
    }
    else{
        $response["error"] = TRUE;
        $response["error_msg"] = "Failed to remove from server ";
        echo json_encode($response);
    }
}
// remove friend
else if(isset($_POST["uid"]) && isset($_POST["fid"])){
    $uid = $_POST["uid"];
    $fid = $_POST["fid"];
    $deleted = $db->removeFriend($pid, $fid);
    if($deleted){
        $response["error"] = FALSE;
        $response["error_msg"] = "Success ";
        echo json_encode($response);
    }
    else{
        $response["error"] = TRUE;
        $response["error_msg"] = "Failed to remove from server ";
        echo json_encode($response);
    }
}
else{
    $response["error"] = TRUE;
    $response["error_msg"] = "Failed to get post params ";
    echo json_encode($response);
}

