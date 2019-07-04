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

if(isset($_POST['email'])){
    $m_email = $_POST['email'];
    if ($db->isUserExisted($m_email)){
        $prof = $db->getProfile($m_email);
        if($prof){
            $response["error"] = FALSE;
            $response["uid"] = $prof["unique_id"];
            $response["profile"]["full_name"] = $prof["full_name"];
            $response["profile"]["email"] = $prof["email"];
            $response["profile"]["phone"] = $prof["phone"];
            $response["profile"]["gender"] = $prof["gender"];
            $response["profile"]["birth"] = $prof["birth"];
            $response["profile"]["genre"] = $prof["genre"];
            $response["profile"]["photo"] = $prof["photo"];
            $response["profile"]["created_at"] = $prof["created_at"];
            $response["profile"]["updated_at"] = $prof["updated_at"];
            echo json_encode($response);
        }
        else{
            // profile failed to be fetched
            $response["error"] = TRUE;
            $response["error_msg"] = "Unknown error occurred in registration!";
            echo json_encode($response);
            
        }
    }
    
}
if (isset($_POST['full_name']) && isset($_POST['email']) && 
        isset($_POST['phone']) &&  isset($_POST['gender']) &&
        isset($_POST['birth']) && isset($_POST['genre'])
        && isset($_POST['avatar'])){
 
    // receiving the post params
    $full_name = $_POST['full_name'];
    $email = $_POST['email'];
    $phone = $_POST['phone'];
    $gender = $_POST['gender'];
    $birth = $_POST['birth'];
    $genre = $_POST['genre'];
    $avatar = $_POST['avatar'];
 
    // check if user is already existed with the same email
    if ($db->isUserExisted($email)) {
        // user already existed
        $response["error"] = TRUE;
        $response["error_msg"] = "User already existed with " . $email;
        echo json_encode($response);
    } else {
        // create a profile
        $user = $db->storeProfile($full_name, $email, $phone, $gender, $birth, $genre, $avatar);
        if ($user) {
            // profile stored successfully
            $response["error"] = FALSE;
            $response["uid"] = $user["unique_id"];
            $response["profile"]["full_name"] = $user["full_name"];
            $response["profile"]["email"] = $user["email"];
            $response["profile"]["phone"] = $user["phone"];
            $response["profile"]["gender"] = $user["gender"];
            $response["profile"]["birth"] = $user["birth"];
            $response["profile"]["genre"] = $user["genre"];
            $response["profile"]["avatar"] = $user["avatar"];
            $response["profile"]["created_at"] = $user["created_at"];
            $response["profile"]["updated_at"] = $user["updated_at"];
            echo json_encode($response);
        } else {
            // profile failed to store
            $response["error"] = TRUE;
            $response["error_msg"] = "Unknown error occurred in registration!";
            echo json_encode($response);
        }
    }
} else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters is missing!";
    echo json_encode($response);
}

