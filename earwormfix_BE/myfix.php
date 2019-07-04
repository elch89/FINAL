<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
    // json response array
    $response = array();
    if(isset($_GET["uid"])){
        $fetch_list = $db->load_list($_GET["uid"]);
        if($fetch_list){
            for( $i = 0; $i<count($fetch_list);$i++){
                $response[$i]["mid"] = $fetch_list[$i]["mid"];
                $response[$i]["desc"] = $fetch_list[$i]["description"];
                $response[$i]["video"] = $fetch_list[$i]["video"];
                $response[$i]["thumbnail"] = $fetch_list[$i]["thumbnail"];
            }
            echo json_encode($response);
        }
        else{
            $response[]=array("error"=> TRUE,"error_msg"=>"Empty query");
            echo json_encode($response);
        }
    }
    else{
        $response[]=array("error"=> TRUE,"error_msg"=>"Unknown error occurred");
        echo json_encode($response);
    }

