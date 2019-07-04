<?php

require_once 'include/DB_Functions.php';
$db = new DB_Functions();

$response = array("error" => FALSE);
if(isset($_POST['fixed'])){
    // fixed is 'pid'
    $fix = $db->fixedIncrement($_POST['fixed']);
    if($fix){
        $response["error"] = FALSE;
        $response["error_msg"] = "Success";
        echo json_encode($response);
    }
    else{
        $response["error"] = TRUE;
        $response["error_msg"] = "Failed to give feedback ";
        echo json_encode($response);
    }
    
}
else if(isset($_POST['comment'])&&isset($_POST['uid'])&&isset($_POST['pid'])){
    // adding a comment with input,uid and cid
    $comm = $db->addComment($_POST['comment'], $_POST['uid'], $_POST['pid']);
    if($comm){
        $response["error"] = FALSE;
        $response["error_msg"] = "Success";
        echo json_encode($response);
    }
    else{
        $response["error"] = TRUE;
        $response["error_msg"] = "Failed to add comment ";
        echo json_encode($response);
    }
}
else if(isset($_POST['my_id'])&&isset($_POST['other'])){// add new friend
    $add = $db->addFriend($_POST['my_id'],$_POST['other']);
    if($add){
        $response["error"] = FALSE;
        $response["error_msg"] = "Success";
        echo json_encode($response);
    }
    else{
        $response["error"] = TRUE;
        $response["error_msg"] = "Failed to add friend ";
        echo json_encode($response);
    }
}
else if(isset($_POST['uid']) && isset($_POST['desc']) && isset($_POST['vid']) && isset($_POST['thumb'])){
    $uid = $_POST['uid'];
    $desc = $_POST['desc'];
    $vid = $_POST['vid'];
    $thumb = $_POST['thumb'];
    
    $addToList = $db->addToList($uid, $desc, $vid, $thumb);
    if($addToList){
        $response["error"] = FALSE;
        $response["error_msg"] = "Success";
        echo json_encode($response); 
    }
    else{
       $response[]=array("error"=> TRUE,"error_msg"=>"Unknown error occurred");
        echo json_encode($response); 
    }
}
else{
    $response["error"] = TRUE;
    $response["error_msg"] = "Unknown error ";
    echo json_encode($response);
}
?>

