<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

/*
$maxsize = 524288000; // 500MB*/

$result = array("error" => FALSE,"error_msg"=>"");
$thumbName = basename($_FILES['image']['name']);
// "user" is current users unique_id
if(isset($_POST["user"])&& isset($_POST["length"])&& isset($_POST["path"])){
    $desc = "";
    if(isset($_POST["desc"])){
        $desc = $_POST["desc"];
    }
    $user = $_POST["user"];
    $target_dir = "post/".$user;
    if ( ! is_dir($target_dir)) {
        mkdir($target_dir);
    }
	$len = $_POST["length"];

	$target_img_file = $target_dir."/".$thumbName;
	$target_file = $target_dir."/" .$_POST["path"];
	//$size = filesize($target_file);// size from server not upload

	if(move_uploaded_file($_FILES['image']['tmp_name'],$target_img_file)) {
                
    	$upload = $db->uploadPost($user,$target_file,$target_img_file,$len,$desc);
    	if($upload){
    		$result["error"] = FALSE;
            $result["error_msg"] = "File successfuly uploaded.";
            echo json_encode($result);
    	}
    	else{
    		$result["error"] = TRUE;
            $result["error_msg"] = "Failed to insert query!!!";
            echo json_encode($result);
    	}
	}
	else{
		$result["error"] = TRUE;
        $result["error_msg"] = "Failed to upload image. ";
        echo json_encode($result);
	}
}
else{
    $result["error"] = TRUE;
    $result["error_msg"] =  "error reading post parameters";
    echo json_encode($result);
}

?>