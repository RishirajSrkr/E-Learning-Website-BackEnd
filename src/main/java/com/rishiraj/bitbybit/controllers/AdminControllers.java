package com.rishiraj.bitbybit.controllers;
import com.rishiraj.bitbybit.implementations.UserServicesImpl;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin")
public class AdminControllers {

    private static final Logger log = LoggerFactory.getLogger(AdminControllers.class);


    @GetMapping
    public ResponseEntity<String> greetingFromAdmin(){
        return new ResponseEntity<>("Hello from ADMIN!!", HttpStatus.OK);
    }
}
