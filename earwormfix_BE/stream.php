<?php
    require_once 'include/DB_Functions.php';
    $db = new DB_Functions();
    // json response array
    $response = array();
   
    if(isset($_GET["user"])&& isset($_GET["page"])&& isset($_GET["page_size"])){
        $fetchVideos = $db->loadPosts($_GET["user"],$_GET["page_size"],$_GET["page"]);
        //$i = $_GET["id"];
        if($fetchVideos){
            //$response["error"] = FALSE;

           for( $i = 0; $i<count($fetchVideos);$i++){// get size
                $response[$i]["id"] = $fetchVideos[$i]["id"];
                $response[$i]["uid"] = $fetchVideos[$i]["uid"];
                $response[$i]["name"] = $fetchVideos[$i]["name"];
                $response[$i]["url"] = $fetchVideos[$i]["url"];
                $response[$i]["description"] = $fetchVideos[$i]["description"];
                $response[$i]["length"] = $fetchVideos[$i]["length"];
                $response[$i]["created_at"] = $fetchVideos[$i]["created_at"];
                $response[$i]["fixed"] = $fetchVideos[$i]["fixed"];
                $response[$i]["thumbnail"] = $fetchVideos[$i]["thumbnail"];
                $response[$i]["comments"] =$fetchVideos[$i]["comments"];
                $response[$i]["pid"] = $fetchVideos[$i]["pid"];
                $response[$i]["photo"] = $fetchVideos[$i]["photo"];
            }


            echo json_encode($response);
        }
        else{
            $response["error_msg"] = "video details failed to be fetched";
            echo json_encode($response);

        }
    }
    else{
        $response[]=array("error"=> TRUE,"error_msg"=>"Unknown error occurred");
        //$response["error"] = TRUE;
      // $response["error_msg"] = "Unknown error occurred";
        echo json_encode($response);
    }
?>