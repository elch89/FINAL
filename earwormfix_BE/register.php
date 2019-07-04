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
 
/** @var type $_POST */
if (isset($_POST['name']) && isset($_POST['email']) && isset($_POST['password'])
        && isset($_POST['full_name'])&& 
        isset($_POST['phone']) &&  isset($_POST['gender']) &&
        isset($_POST['birth']) && isset($_POST['genre'])) {
 
    // receiving the post params
    $name = $_POST['name'];
    $email = $_POST['email'];
    $password = $_POST['password'];
    $full_name = $_POST['full_name'];
    $phone = $_POST['phone'];
    $gender = $_POST['gender'];
    $birth = $_POST['birth'];
    $genre = $_POST['genre'];
    $uuid = uniqid('', true);// create unique id
    // send to directory on server
    $target_dir = "profile/".$uuid;
    if ( ! is_dir($target_dir)) {
        mkdir($target_dir);
    }
    $target_img_file = $target_dir."/".basename($_FILES['image']['name']);
    // check if user is already existed with the same email
    if ($db->isUserExisted($email)) {
        // user already existed
        $response["error"] = TRUE;
        $response["error_msg"] = "User already existed with " . $email;
        echo json_encode($response);
    } 
    else 
    {
        if(move_uploaded_file($_FILES['image']['tmp_name'],$target_img_file)) {
			$user = $db->storeUser($name, $email, $password, $full_name, $phone,
                $gender,$birth, $genre,$uuid,$target_img_file);// add uuid to params
            if ($user) {
                // user stored successfully
                $response["error"] = FALSE;
                $response["error_msg"] = "File successfuly uploaded.";
                $response["uid"] = $user["unique_id"];
                $response["name"] = $user["name"];
                $response["email"] = $user["email"];
                $response["full_name"] = $user["full_name"];
                $response["phone"] = $user["phone"];
                $response["gender"] = $user["gender"];
                $response["birth"] = $user["birth"];
                $response["genre"] = $user["genre"];
                $response["photo"] = $user["photo"];
                $response["created_at"] = $user["created_at"];
            
                echo json_encode($response);
            } 
            else {
                // user failed to store
                $response["error"] = TRUE;
                $response["error_msg"] = $user;"Unknown error occurred in registration!";
                echo json_encode($response);
            }
		}
		else{
		    $response["error"] = TRUE;
            $response["error_msg"] = "Image upload failure!";
            echo json_encode($response);
		}
    }
} 
else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters are missing!";
    echo json_encode($response);
}

