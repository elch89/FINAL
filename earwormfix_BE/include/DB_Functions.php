<?php

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
class DB_Functions {
 
    private $conn;
 
    // constructor
    function __construct() {
        require_once 'DB_Connect.php';
        // connecting to database
        $db = new Db_Connect();
        $this->conn = $db->connect();
    }
 
    // destructor
    function __destruct() {
         
    }
 
    /**
     * Storing new user
     * returns user details
     */
    public function storeUser($name, $email, $password,$full_name, $phone, $gender, $birth, 
            $genre, $uuid,$photo) {
        $hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt
        
        
 
        $stmt = $this->conn->prepare("INSERT INTO user_profile(unique_id, name, email, encrypted_password, salt,full_name, "
                    . "phone, gender, birth, genre, photo, created_at) VALUES(?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, NOW())");
        $stmt->bind_param("sssssssssss", $uuid, $full_name, $email, $encrypted_password, $salt, $name , $phone, $gender, $birth, $genre, $photo);
        $result = $stmt->execute();
        $stmt->close();
 
        // check for successful store
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM user_profile WHERE email = ?");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            // add administrator as friend
            $this->addFriend($uuid,'5d1452171c94b6.91933981');
            return $user;
        } else {
            return false;
        }
    }
 
    /**
     * Get user by email and password
     */
    public function getUserByEmailAndPassword($email, $password) {
 
        $stmt = $this->conn->prepare("SELECT * FROM user_profile WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        if ($stmt->execute()) {
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
 
            // verifying user password
            $salt = $user['salt'];
            $encrypted_password = $user['encrypted_password'];
            $hash = $this->checkhashSSHA($salt, $password);
            // check for password equality
            if ($encrypted_password == $hash) {
                // user authentication details are correct
                return $user;
            }
        } else {
            return NULL;
        }
    }
 
    /**
     * Check user is existed or not
     */
    public function isUserExisted($email) {
        $stmt = $this->conn->prepare("SELECT email from user_profile WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        $stmt->execute();
 
        $stmt->store_result();
 
        if ($stmt->num_rows > 0) {
            // user existed 
            $stmt->close();
            return true;
        } else {
            // user not existed
            $stmt->close();
            return false;
        }
    }
    /**
     * Encrypting password
     * @param password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {
 
        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }
 
    /**
     * Decrypting password
     * @param salt, password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {
 
        $hash = base64_encode(sha1($password . $salt, true) . $salt);
 
        return $hash;
    }
    /*update the user information*/
    public function updateUser($col, $email, $param) {
        $stmt = $this->conn->prepare("UPDATE user_profile SET $col=?, updated_at=NOW() WHERE email=?");
        $stmt->bind_param("ss", $param, $email);
        $result = $stmt->execute();
        $stmt->close();
        if($result){
            if($col == "email"){
                $email=$param;
            }
            $stmt = $this->conn->prepare("SELECT * FROM user_profile WHERE email = ?");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $profile = $stmt->get_result()->fetch_assoc();
            $stmt->close();
 
            return $profile;
        }
        return false;
    }
   
    
    /**
     * fetch profile of user
     */
    public function getProfile($email){
        if($this->isUserExisted($email)){
            $stmta = $this->conn->prepare("SELECT * FROM user_profile WHERE email = ?");
            $stmta->bind_param("s", $email);
            $stmta->execute();
            $profiles = $stmta->get_result()->fetch_assoc();
            $stmta->close();
 
            return $profiles;
            
        }
        return false;
        
    }
    
    /** 
     * upload a video to db
     * description, url, uid
     */
    public function uploadPost($directory, $file,$img_file, $length, $desc){
        $pid = uniqid();
        $name = $this->getUserName($directory);
        if(!$name){
            return false;
        }
        $size = filesize($file);
        $query = $this->conn->prepare("INSERT INTO post(description,uid,pid,file_name,url,thumbnail,length,size,created_at,fixed) VALUES(?,?,?,?,?,?,?,?,NOW(),0)");
        $query->bind_param("sssssssi",$desc, $directory, $pid, $name['name'], $file,$img_file, $length, $size);
	    $query->execute();
        $results = $query->get_result()->fetch_assoc();
        $query->close();
        if($results){
            return $results;
        }
        return false;
        
    }
    function getUserName($uid){
        $query = $this->conn->prepare("SELECT name FROM user_profile WHERE unique_id=?");
        if($query){
            $query->bind_param("s",$uid);
            $query->execute();
            $results = $query->get_result()->fetch_assoc();
            $query->close();
            if($results){
                return $results;
            }
        }
        return false;
        
    }
    /* get posts from database*/
    public function loadPosts($user, $pageSize, $page){
        $results = array();
        $comments = array();
        $friends = array();// returns a list of selected friends
        
        $stmt = $this->conn->prepare("SELECT * FROM user_friends WHERE (pid=? OR fuid=?)");
        if($stmt){
            $stmt->bind_param("ss",$user,$user);
            $stmt->execute();
            $resuu = $stmt->get_result();
            
            while($row =$resuu->fetch_assoc()){
                if($row['pid'] == $user){
                     $friends[] = $row['fuid'];
                }
                if($row['fuid'] == $user){
                     $friends[] = $row['pid'];
                }
               
            }
            $friends[] = $user;
            $stmt->close();
        }
        else{
            return false;
        }
        if(count($friends)>0){
            $in = join(',', array_fill(0, count($friends), '?'));     
            $select = "SELECT * FROM post WHERE uid in($in) ORDER BY created_at DESC";
            $stmt = $this->conn->prepare($select);
            if($stmt){
                $stmt->bind_param(str_repeat('s', count($friends)), ...$friends);
                $stmt->execute();
                $stmt->bind_result($id,$pid, $description,  $location,$thumbnail, $unique,$name,  $size,$length, $fixed ,$fixed_by, $created);
                $ids = 1;
                $pageStart = ($page-1)*$pageSize;
                $pageEnd = $pageSize * $page;
                if($page == 1){
                    $pageStart = $page;
                }
            
                while($stmt->fetch()){
                    if($ids >= $pageStart && $ids < $pageEnd){
                        $temp = [
                        'id'=>$id,
                        'pid'=>$pid,
                        'description'=>$description,
                        'url'=>$location,
                        'uid'=>$unique,
                        'name'=>$name,
                        'size'=>$size,
                        'thumbnail'=>$thumbnail,
                        'length'=>$length,
                        'fixed'=>$fixed,
                        'fixed_by'=>$fixed_by,
                        'created_at'=>$created,
                        'comments'=> null,
                        'photo'=>"0"
                        ];
                        array_push($results, $temp); 
                    }
                    $ids = $ids+1;
                }
                $stmt->close();
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
        for($i=0;$i<count($results);$i++){
            $comments = $this->getComments($results[$i]['pid']);
            $profilePic = $this->getProfilePic($results[$i]['uid']);
            $results[$i]['comments']=$comments;
            $results[$i]['photo']=$profilePic['photo'];
        }
        
        return $results;
    }
    function getProfilePic($uid){
        $query = $this->conn->prepare("SELECT photo FROM user_profile WHERE unique_id=?");
        if($query){
            $query->bind_param("s",$uid);
            $query->execute();
            $results = $query->get_result()->fetch_assoc();
            $query->close();
            if($results){
                return $results;
            }
        }
        return "0";
    }
    
    function getComments($post){
        $result = array();
        $sql = "SELECT pid,uid,created_at,user_input FROM comments WHERE pid=? ORDER BY created_at DESC";
        $stmts = $this->conn->prepare($sql);
        if($stmts){
//        print_r($this->conn->error_list);}
            $stmts->bind_param("s",$post);
            $stmts->execute();
            $stmts->bind_result($pid,$unique, $created,  $input);
            while($stmts->fetch()){
                $temp = [
                    'pid'=>$pid,
                    'uid'=>$unique,
                    'created_at'=>$created,
                    'user_input'=>$input
                    ];
                    array_push($result, $temp); 
            }
            $stmts->close();
    
            if($result){
                return $result;
            }
            return null;
        }
        return null;
    }
    /**
     * Add comment
     * PID - is actually post_id
     * uid is name!!
     */
    public function addComment($inp, $user_id, $post_id){
        $name = $this->getUserName($user_id);
        //$valid_date = date( 'm/d/y g:i A');
        $query = "INSERT INTO comments(pid,uid,created_at,user_input)VALUES(?,?,NOW(),?)";
        $stmt = $this->conn->prepare($query);
        if($stmt){
            $stmt->bind_param("sss",$post_id, $name['name'], $inp);
            $stmt->execute();
            $stmt->close();
            return true;
        }
        return false;   
    }
    public function addFriend($my_id,$other){
        $alredy_friends = $this->areFriends($my_id,$other);
        if($alredy_friends){
            return $alredy_friends;
        }
        $query = "INSERT INTO user_friends(pid,fuid)VALUES(?,?)";
        $stmt = $this->conn->prepare($query);
        if($stmt){
            $stmt->bind_param("ss",$my_id, $other);
            $stmt->execute();
            $stmt->close();
            return true;
        }
        return false;   
        
    }
// Insert fixed+1 on post by pid
    public function fixedIncrement($pid){
        $query="UPDATE post 
            SET fixed = fixed + 1
                WHERE pid=?";
        $stmt = $this->conn->prepare($query);
        if($stmt){
            $stmt->bind_param("s",$pid);
            $stmt->execute();
            $stmt->close();
            return true;
        }
        return false; 
    }
    public function browseFriends($mid){
        $results = array();
        $query = "SELECT unique_id,name,full_name,email,photo FROM user_profile WHERE unique_id<>'$mid' ORDER BY full_name ASC";
        $stmt = $this->conn->prepare($query);
        if($stmt){
            $stmt->execute();
            $stmt->bind_result($uid,$name,$fullName, $email,$photo);
            while($stmt->fetch()){
                $temp = [
                    'uid'=>$uid,
                    'full_name'=>$fullName,
                    'name'=>$name,
                    'email'=>$email,
                    'photo'=>$photo,
                    'are_friends'=>FALSE
                    ];
                array_push($results, $temp); 
            }
            $stmt->close();
            for($i=0;$i<count($results);$i++){
                if($this->areFriends($mid,$results[$i]['uid'])){
                    $results[$i]['are_friends'] = TRUE;
                }
            }
            return $results;
        }
        return false;   
    }
    
    
    public function load_list($uid){
        $results = array();
        $stmt = $this->conn->prepare("SELECT description, url AS video, thumbnail  FROM post
                    WHERE uid=?
                    UNION
                    SELECT description, video, thumbnail FROM fix_list
                    WHERE uid=?;");
        if($stmt){
            $stmt->bind_param("ss",$uid,$uid);
            $stmt->execute();
            $id = 1;
            $stmt->bind_result($description,$vid, $thumb);
            while($stmt->fetch()){
                $temp = [
                    'mid'=>$id,
                    'description'=>$description,
                    'video'=>$vid,
                    'thumbnail'=>$thumb
                    ];
                    $id = $id+1;
                array_push($results, $temp); 
            }
            $stmt->close();
            return $results;
            
        }
        return false;   
    }
    public function addToList($uid,$desc,$vid,$thumb){
        $stmt = $this->conn->prepare("INSERT INTO fix_list(uid,description,video, thumbnail)VALUES(?,?,?,?)");
        if($stmt){
            $stmt->bind_param("ssss",$uid,$desc,$vid,$thumb);
            $stmt->execute();
            $stmt->close();
            return true;
        }
        return false;
        
    }
    public function areFriends($pid,$fuid){
        $query = "SELECT pid FROM user_friends WHERE (pid=? AND fuid=?) OR (pid=? AND fuid=?)";
        $stmt = $this->conn->prepare($query);
 
        $stmt->bind_param("ssss", $pid,$fuid,$fuid,$pid);
 
        $stmt->execute();
 
        $stmt->store_result();
 
        if ($stmt->num_rows > 0) {
            // are freinds
            $stmt->close();
            return true;
        } else {
            // not friends
            $stmt->close();
            return false;
        }
    }
    public function deletePost($pid,$vid){
        $query = "DELETE FROM post WHERE pid=?";
        $stmt = $this->conn->prepare($query);
        if($stmt){
            $stmt->bind_param("s", $pid);
            $stmt->execute();
            $stmt->close();
            $query1 = "DELETE FROM fix_list WHERE video=?";
            $stmt1 = $this->conn->prepare($query1);
            if($stmt1){
                $stmt1->bind_param("s", $vid);
                $stmt1->execute();
                $stmt1->close();
                return true;
            }
        }
        return false;
    }
    public function deleteUser($uid){
        $query = "DELETE FROM user_profile WHERE unique_id=?";
        $stmt = $this->conn->prepare($query);
        if($stmt){
            $stmt->bind_param("s", $uid);
            $stmt->execute();
            $stmt->close();
            
            $query1 = "DELETE FROM post WHERE uid=?";
            $stmt1 = $this->conn->prepare($query1);
            $stmt1->bind_param("s", $uid);
            $stmt1->execute();
            $stmt1->close();
                
            $query2 = "DELETE FROM fix_list WHERE uid=?";
            $stmt2 = $this->conn->prepare($query2);
            $stmt2->bind_param("s", $uid);
            $stmt2->execute();
            $stmt2->close();
            $this->removeFriend($uid,false);
            return true;
            
            
        }
        return false;
    }
    
    public function removeFriend($uid,$fid){
        $query= "";
        if($fid == false){
            $query = "DELETE FROM user_friends WHERE pid=?";
            $stmt = $this->conn->prepare($query);
            if($stmt){
                $stmt->bind_param("s", $uid);
            }
            else {return false;}
        }
        else{
            $query = "DELETE FROM user_friends WHERE pid=? AND fuid=?";
            $stmt = $this->conn->prepare($query);
            if($stmt){
                $stmt->bind_param("ss", $uid,$fid);
            }
            else {return false;}
        }
        $stmt->execute();
        $stmt->close();
        return true;
    }
    
}

